# Copyright 2026 EMBL - European Bioinformatics Institute
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import argparse
import csv
import json
import os
from collections import defaultdict
from datetime import datetime, timezone, timedelta
from functools import cached_property

import matplotlib.pyplot as plt
import numpy as np
from ebi_eva_common_pyutils.common_utils import pretty_print
from ebi_eva_common_pyutils.logger import logging_config
from ebi_eva_internal_pyutils.metadata_utils import get_metadata_connection_handle

logger = logging_config.get_logger(__name__)

TABLE_NAME = 'eva_submissions.call_home_event'


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def load_excluded_deployment_ids(path):
    if not path:
        return set()
    with open(path) as f:
        return {line.strip() for line in f if line.strip()}


def print_section(title, header, rows):
    print(f"\n=== {title} ===")
    str_rows = [tuple(str(cell) for cell in row) for row in rows]
    pretty_print(header, str_rows)


def write_csv(output_dir, filename, header, rows):
    path = os.path.join(output_dir, filename)
    with open(path, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(header)
        writer.writerows(rows)
    logger.info(f"Written {path}")


# ---------------------------------------------------------------------------
# Stats collector class
# ---------------------------------------------------------------------------

class CLIUsageStats:

    def __init__(self, private_config_xml_file, profile, output_dir, since, error_since, excluded_ids):
        self.private_config_xml_file = private_config_xml_file
        self.profile = profile
        self.output_dir = output_dir
        self.since = since
        self.error_since = error_since
        self.excluded_ids = excluded_ids
        self.since_label = since.strftime('%Y-%m-%d')
        self.error_since_label = error_since.strftime('%Y-%m-%d')

    @cached_property
    def postgres_handle(self):
        return get_metadata_connection_handle(self.profile, self.private_config_xml_file)

    def query(self, sql, params=()):
        with self.postgres_handle.cursor() as cursor:
            cursor.execute(sql, params)
            return cursor.fetchall()

    def excl(self):
        """Return a SQL AND clause to exclude deployment IDs, or empty string."""
        if not self.excluded_ids:
            return ""
        ids = ', '.join(f"'{id_}'" for id_ in self.excluded_ids)
        return f"AND deployment_id NOT IN ({ids})"

    def output(self, title, header, rows, filename):
        print_section(title, header, rows)
        write_csv(self.output_dir, filename, header, rows)

    # -------------------------------------------------------------------------
    # CLI metrics
    # -------------------------------------------------------------------------

    def successful_runs(self, task):
        """Count distinct run_ids for a given task that have END but no FAILURE."""
        sql = f"""
            SELECT COUNT(DISTINCT run_id)
            FROM {TABLE_NAME}
            WHERE tasks LIKE %s
              {self.excl()}
              AND run_id IN (SELECT run_id FROM {TABLE_NAME} WHERE event_type = 'END' {self.excl()})
              AND run_id NOT IN (SELECT run_id FROM {TABLE_NAME} WHERE event_type = 'FAILURE' {self.excl()})
        """
        rows = self.query(sql, (f'%{task}%',))
        return rows[0][0] if rows else 0

    def runs_per_week(self):
        sql = f"""
            WITH run_start AS (
                SELECT run_id, MIN(created_at) AS start_time
                FROM {TABLE_NAME}
                WHERE event_type = 'START'
                  {self.excl()}
                GROUP BY run_id
            )
            SELECT DATE_TRUNC('week', start_time)::date AS week, COUNT(DISTINCT run_id) AS run_count
            FROM run_start
            WHERE start_time >= %s
            GROUP BY week
            ORDER BY week
        """
        return self.query(sql, (self.since,))

    def submitters_per_week(self):
        sql = f"""
            WITH run_start AS (
                SELECT run_id, deployment_id, MIN(created_at) AS start_time
                FROM {TABLE_NAME}
                WHERE event_type = 'START'
                  {self.excl()}
                GROUP BY run_id, deployment_id
            )
            SELECT DATE_TRUNC('week', start_time)::date AS week, COUNT(DISTINCT deployment_id) AS submitter_count
            FROM run_start
            WHERE start_time >= %s
            GROUP BY week
            ORDER BY week
        """
        return self.query(sql, (self.since,))

    def runs_per_version_per_week(self):
        sql = f"""
            WITH run_start AS (
                SELECT e.run_id, MIN(e.created_at) AS start_time, e.cli_version
                FROM {TABLE_NAME} e
                WHERE e.event_type = 'START'
                  {self.excl()}
                GROUP BY e.run_id, e.cli_version
            )
            SELECT DATE_TRUNC('week', start_time)::date AS week, cli_version, COUNT(DISTINCT run_id) AS run_count
            FROM run_start
            WHERE start_time >= %s
            GROUP BY week, cli_version
            ORDER BY week, cli_version
        """
        return self.query(sql, (self.since,))

    def runs_per_executor(self):
        sql = f"""
            SELECT executor, COUNT(DISTINCT run_id) AS run_count
            FROM {TABLE_NAME}
            WHERE TRUE {self.excl()}
            GROUP BY executor
            ORDER BY run_count DESC
        """
        return self.query(sql)

    def tasks_per_week(self):
        """
        Fetch distinct run_id + tasks + week; split tasks on comma in Python;
        count run_ids per individual task per week.
        """
        sql = f"""
            WITH run_start AS (
                SELECT run_id, MIN(created_at) AS start_time
                FROM {TABLE_NAME}
                WHERE event_type = 'START'
                  {self.excl()}
                GROUP BY run_id
            ),
            run_tasks AS (
                SELECT DISTINCT e.run_id, e.tasks, DATE_TRUNC('week', rs.start_time)::date AS week
                FROM {TABLE_NAME} e
                JOIN run_start rs ON e.run_id = rs.run_id
                WHERE rs.start_time >= %s
                  AND e.tasks IS NOT NULL
                  {self.excl()}
            )
            SELECT week, tasks, run_id FROM run_tasks
        """
        raw = self.query(sql, (self.since,))
        counts = defaultdict(lambda: defaultdict(set))
        for week, tasks_str, run_id in raw:
            for task in [t.strip() for t in tasks_str.split(',')]:
                if task:
                    counts[week][task].add(run_id)
        return sorted(
            (week, task, len(run_ids))
            for week, task_map in counts.items()
            for task, run_ids in task_map.items()
        )

    def shallow_validation_per_week(self):
        """
        For VALIDATION_COMPLETED events, parse raw_payload JSON.
        Check presence of 'shallow_validation' in validation_result.
        Report % of run_ids that used shallow validation per week.
        """
        sql = f"""
            WITH run_start AS (
                SELECT run_id, MIN(created_at) AS start_time
                FROM {TABLE_NAME}
                WHERE event_type = 'START'
                  {self.excl()}
                GROUP BY run_id
            )
            SELECT e.run_id, DATE_TRUNC('week', rs.start_time)::date AS week, e.raw_payload
            FROM {TABLE_NAME} e
            JOIN run_start rs ON e.run_id = rs.run_id
            WHERE e.event_type = 'VALIDATION_COMPLETED'
              AND rs.start_time >= %s
              {self.excl()}
        """
        raw = self.query(sql, (self.since,))
        week_total = defaultdict(set)
        week_shallow = defaultdict(set)
        for run_id, week, json_payload in raw:
            week_total[week].add(run_id)
            try:
                if json_payload.get('validation_result', {}).get('shallow_validation'):
                    week_shallow[week].add(run_id)
            except (json.JSONDecodeError, AttributeError):
                pass
        return sorted(
            (week, len(week_total[week]), len(week_shallow[week]),
             f"{100 * len(week_shallow[week]) / len(week_total[week]):.1f}%" if week_total[week] else "N/A")
            for week in week_total
        )

    def exception_descriptions(self):
        sql = f"""
            SELECT raw_payload
            FROM {TABLE_NAME}
            WHERE event_type = 'FAILURE'
              AND created_at >= %s
              {self.excl()}
        """
        raw = self.query(sql, (self.error_since,))
        counts = defaultdict(int)
        for (json_payload,) in raw:
            try:
                stacktrace = json_payload.get('exceptionStacktrace') or ''
                last_line = next(
                    (line.strip() for line in reversed(stacktrace.splitlines()) if line.strip()),
                    'unknown'
                )
                counts[last_line.split(':')[0].strip()] += 1
            except (json.JSONDecodeError, AttributeError):
                counts['(unparseable payload)'] += 1
        return sorted(counts.items(), key=lambda x: -x[1])

    def validation_error_patterns(self):
        """For VALIDATION_COMPLETED events in the error window, report pass/fail per check type."""
        sql = f"""
            SELECT raw_payload
            FROM {TABLE_NAME}
            WHERE event_type = 'VALIDATION_COMPLETED'
              AND created_at >= %s
              {self.excl()}
        """
        raw = self.query(sql, (self.error_since,))
        check_keys = set()
        counts = {}
        for (json_payload,) in raw:
            try:
                vr = json_payload.get('validation_result', {})
                for check in vr:
                    if check.endswith('_check'):
                        if check not in counts:
                            check_keys.add(check)
                            counts[check] = {'pass': 0, 'fail': 0}
                        if vr.get(check).get('pass') and vr.get(check).get('run_status'):
                            counts[check]['pass'] += 1
                        else:
                            counts[check]['fail'] += 1
            except (json.JSONDecodeError, AttributeError):
                pass
        return [(k, counts[k]['pass'], counts[k]['fail']) for k in check_keys]

    def run_id_issue_counts(self):
        """Count run IDs per issue category: missing START, missing END, validate without submit."""
        sql = f"""
            SELECT
                NOT BOOL_OR(event_type = 'START') AS missing_start,
                NOT BOOL_OR(event_type = 'END')   AS missing_end,
                BOOL_OR(tasks LIKE '%%validate%%') AND NOT BOOL_OR(tasks LIKE '%%submit%%') AS validate_no_submit
            FROM {TABLE_NAME}
            WHERE created_at >= %s
              {self.excl()}
            GROUP BY run_id
        """
        raw = self.query(sql, (self.error_since,))
        counts = {'missing START': 0, 'missing END': 0, 'Validate without submit': 0}
        for missing_start, missing_end, validate_no_submit in raw:
            if missing_start:
                counts['missing START'] += 1
            if missing_end:
                counts['missing END'] += 1
            if validate_no_submit:
                counts['Validate without submit'] += 1
        return [(category, count) for category, count in counts.items()]

    # -------------------------------------------------------------------------
    # Plots
    # -------------------------------------------------------------------------

    def _save_fig(self, fig, filename):
        path = os.path.join(self.output_dir, filename)
        fig.savefig(path, bbox_inches='tight')
        plt.close(fig)
        logger.info(f"Written {path}")

    def plot_line(self, filename, title, x_labels, series):
        """Generic line chart. series: list of (label, y_values)."""
        fig, ax = plt.subplots(figsize=(10, 5))
        for label, y_values in series:
            ax.plot(x_labels, y_values, marker='o', label=label)
        ax.set_title(title)
        ax.set_xlabel('Week')
        ax.tick_params(axis='x', rotation=45)
        ax.legend()
        ax.grid(axis='y', linestyle='--', alpha=0.5)
        self._save_fig(fig, filename)

    def plot_bar(self, filename, title, x_labels, series, ylabel='Count'):
        """Generic bar chart. series: list of (label, y_values). Grouped if multiple series."""
        fig, ax = plt.subplots(figsize=(max(6, int(len(x_labels) * 1.5)), 5))
        n_series = len(series)
        width = 0.8 / n_series
        x = np.arange(len(x_labels))
        for i, (label, y_values) in enumerate(series):
            offset = (i - (n_series - 1) / 2) * width
            ax.bar(x + offset, y_values, width=width, label=label)
        ax.set_title(title)
        ax.set_ylabel(ylabel)
        ax.set_xticks(x)
        ax.set_xticklabels(x_labels, rotation=45, ha='right')
        ax.legend()
        ax.grid(axis='y', linestyle='--', alpha=0.5)
        self._save_fig(fig, filename)

    # -------------------------------------------------------------------------
    # Report methods (data + table output + plot)
    # -------------------------------------------------------------------------

    def report_usage_summary(self):
        title, basename = 'Usage Summary', 'usage_summary'
        n_submit = self.successful_runs('submit')
        n_validate = self.successful_runs('validate')
        self.output(title, ['Metric', 'Count'],
                    [('Successful submissions', n_submit), ('Successful validations', n_validate)],
                    f'{basename}.csv')

    def report_runs_per_week(self):
        title, basename = f'Run IDs per Week (since {self.since_label})', 'runs_per_week'
        rows = self.runs_per_week()
        self.output(title, ['Week', 'Run Count'], rows, f'{basename}.csv')
        if rows:
            self.plot_line(f'{basename}.png', title,
                           [str(r[0]) for r in rows], [('runs', [r[1] for r in rows])])

    def report_submitters_per_week(self):
        title, basename = f'Distinct Submitters per Week (since {self.since_label})', 'submitters_per_week'
        rows = self.submitters_per_week()
        self.output(title, ['Week', 'Submitter Count'], rows, f'{basename}.csv')
        if rows:
            self.plot_line(f'{basename}.png', title,
                           [str(r[0]) for r in rows], [('submitters', [r[1] for r in rows])])

    def report_runs_per_version_per_week(self):
        title, basename = f'Run IDs per Version per Week (since {self.since_label})', 'runs_per_version_week'
        rows = self.runs_per_version_per_week()
        self.output(title, ['Week', 'CLI Version', 'Run Count'], rows, f'{basename}.csv')
        if rows:
            versions = sorted({r[1] for r in rows})
            all_weeks = sorted({r[0] for r in rows})
            by_version = {v: {r[0]: r[2] for r in rows if r[1] == v} for v in versions}
            self.plot_line(f'{basename}.png', title,
                           [str(w) for w in all_weeks],
                           [(v, [by_version[v].get(w, 0) for w in all_weeks]) for v in versions])

    def report_runs_per_executor(self):
        title, basename = 'Run IDs per Executor', 'runs_per_executor'
        rows = self.runs_per_executor()
        self.output(title, ['Executor', 'Run Count'], rows, f'{basename}.csv')

    def report_tasks_per_week(self):
        title, basename = f'Tasks per Week (since {self.since_label})', 'tasks_per_week'
        rows = self.tasks_per_week()
        self.output(title, ['Week', 'Task', 'Run Count'], rows, f'{basename}.csv')
        if rows:
            task_names = sorted({r[1] for r in rows})
            all_weeks = sorted({r[0] for r in rows})
            by_task = {t: {r[0]: r[2] for r in rows if r[1] == t} for t in task_names}
            self.plot_line(f'{basename}.png', title,
                           [str(w) for w in all_weeks],
                           [(t, [by_task[t].get(w, 0) for w in all_weeks]) for t in task_names])

    def report_shallow_validation_per_week(self):
        title, basename = f'Shallow Validation Usage per Week (since {self.since_label})', 'shallow_validation_per_week'
        rows = self.shallow_validation_per_week()
        self.output(title, ['Week', 'Total Runs', 'Shallow Validation Runs', '% Usage'],
                    rows, f'{basename}.csv')
        if rows:
            pct = [float(r[3].rstrip('%')) if r[3] != 'N/A' else 0 for r in rows]
            plot_title = f'Shallow Validation Usage % per Week (since {self.since_label})'
            self.plot_line(f'{basename}.png', plot_title,
                           [str(r[0]) for r in rows], [('% usage', pct)])

    def report_exception_descriptions(self):
        title, basename = f'Exception Descriptions (since {self.error_since_label})', 'exceptions_last_2w'
        rows = self.exception_descriptions()
        self.output(title, ['Exception', 'Count'], rows, f'{basename}.csv')
        if rows:
            self.plot_bar(f'{basename}.png', title,
                          [r[0] for r in rows], [('count', [r[1] for r in rows])])

    def report_validation_error_patterns(self):
        title, basename = f'Validation Error Patterns (since {self.error_since_label})', 'validation_error_patterns'
        rows = self.validation_error_patterns()
        self.output(title, ['Check', 'Pass', 'Fail'], rows, f'{basename}.csv')
        if rows:
            self.plot_bar(f'{basename}.png', title,
                          [r[0] for r in rows],
                          [('pass', [r[1] for r in rows]), ('fail', [r[2] for r in rows])])

    def report_run_id_issue_counts(self):
        title, basename = f'Run ID Issue Counts (since {self.error_since_label})', 'run_id_issues'
        rows = self.run_id_issue_counts()
        self.output(title, ['Category', 'Run Count'], rows, f'{basename}.csv')
        if rows:
            self.plot_bar(f'{basename}.png', title,
                          [r[0] for r in rows], [('count', [r[1] for r in rows])])

    # -------------------------------------------------------------------------
    # Report orchestration
    # -------------------------------------------------------------------------

    def collect_and_report(self):
        os.makedirs(self.output_dir, exist_ok=True)
        self.report_usage_summary()
        self.report_runs_per_week()
        self.report_submitters_per_week()
        self.report_runs_per_version_per_week()
        self.report_runs_per_executor()
        self.report_tasks_per_week()
        self.report_shallow_validation_per_week()
        self.report_exception_descriptions()
        self.report_validation_error_patterns()
        self.report_run_id_issue_counts()


def main():
    parser = argparse.ArgumentParser(description='Collect CLI telemetry usage statistics.')
    parser.add_argument('--private-config-xml-file', required=True,
                        help='Path to the Maven settings XML file with database credentials.')
    parser.add_argument('--profile', default='production_processing',
                        help='Profile name in the Maven settings XML (default: production_processing).')
    parser.add_argument('--output-dir', default='.',
                        help='Directory where CSV files will be written (default: current directory).')
    parser.add_argument('--days', type=int, default=84,
                        help='Number of days back for weekly metrics (default: 84).')
    parser.add_argument('--error-days', type=int, default=14,
                        help='Number of days back for error/pipeline metrics (default: 14).')
    parser.add_argument('--exclude-deployment-ids-file',
                        help='File with deployment IDs to exclude (one per line).')
    args = parser.parse_args()
    now = datetime.now(timezone.utc)
    excluded_ids = load_excluded_deployment_ids(args.exclude_deployment_ids_file)
    if excluded_ids:
        logger.info(f"Excluding {len(excluded_ids)} deployment ID(s)")
    CLIUsageStats(
        profile=args.profile,
        private_config_xml_file=args.private_config_xml_file,
        output_dir=args.output_dir,
        since=now - timedelta(days=args.days),
        error_since=now - timedelta(days=args.error_days),
        excluded_ids=excluded_ids,
    ).collect_and_report()

if __name__ == '__main__':
    main()
