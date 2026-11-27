#!/usr/bin/env python
"""
Summarise the archived usage records into Postgres tables.

usage_daily: one row per day, source, direction, item (web service endpoint or file path), status class and country.
usage_distinct_clients: distinct clients per day, month and year, overall and per country, RS release species and
study. Distinct clients cannot be added up across days or groups, which is why they are computed for each period.

Caveats:
 - Client identifiers are client IPs for the web services, client hashes for the neometer file transfers and the
   legacy host identifier for the legacy FTP transfers: distinct clients cannot be compared across these.
 - HTTP partial (206) responses make n_events overestimate the number of downloads of a file. For file transfers,
   n_distinct_clients per file and day is a better estimate of the number of downloads.
 - Failed FTP and Globus transfers are counted with status_class 'failure'.

Each run replaces whole periods, so it can be run again safely.
"""
from argparse import ArgumentParser
from datetime import date, timedelta

from ebi_eva_common_pyutils.logger import logging_config
from ebi_eva_internal_pyutils.metadata_utils import get_metadata_connection_handle
from psycopg2.extras import execute_values

from eva_usage_metrics.archive.usage_metrics_config import get_archive_dir, get_maven_settings, load_config
from eva_usage_metrics.archive.parquet_datasets import DATASETS
from eva_usage_metrics.archive.parquet_archive import connect_duckdb, register_views, utc_today
from eva_usage_metrics.summary.file_path_metadata import release_species_sql, study_sql

logger = logging_config.get_logger(__name__)

DEFAULT_SCHEMA = 'eva_web_srvc_stats'
DAILY_COLUMNS = ['day', 'source', 'direction', 'item', 'status_class', 'country_code', 'release_species', 'study',
                 'n_events', 'n_distinct_clients', 'bytes', 'total_duration_secs']
DISTINCT_COLUMNS = ['period_type', 'period_start', 'source', 'direction', 'dimension', 'dimension_value',
                    'n_distinct_clients']
PERIOD_TYPES = ('day', 'month', 'year')
# Groups of the distinct clients for each period type
DIMENSIONS = {
    'day': ('all',),
    'month': ('all', 'country', 'release_species', 'study'),
    'year': ('all', 'country', 'release_species', 'study'),
}
DIMENSION_COLUMNS = {'all': 'NULL', 'country': 'country_code', 'release_species': 'release_species', 'study': 'study'}
ALL_TRANSFERS = 'all_transfers'

# HTTP status codes are grouped by class, FTP and Globus outcomes are kept as they are
STATUS_CLASS_SQL = "CASE WHEN regexp_matches(status, '^[0-9]{3}$') THEN substr(status, 1, 1) || 'xx' ELSE status END"


def create_tables(pg_connection, schema=DEFAULT_SCHEMA):
    with pg_connection.cursor() as cursor:
        cursor.execute(
            f'CREATE TABLE IF NOT EXISTS {schema}.usage_daily ('
            'day DATE NOT NULL, source TEXT NOT NULL, direction TEXT, item TEXT, status_class TEXT, country_code TEXT,'
            ' release_species TEXT, study TEXT, n_events BIGINT NOT NULL, n_distinct_clients BIGINT NOT NULL,'
            ' bytes BIGINT, total_duration_secs DOUBLE PRECISION)'
        )
        cursor.execute(f'CREATE INDEX IF NOT EXISTS usage_daily_day_idx ON {schema}.usage_daily (day)')
        cursor.execute(
            f'CREATE TABLE IF NOT EXISTS {schema}.usage_distinct_clients ('
            'period_type TEXT NOT NULL, period_start DATE NOT NULL, source TEXT NOT NULL, direction TEXT,'
            ' dimension TEXT NOT NULL, dimension_value TEXT, n_distinct_clients BIGINT NOT NULL)'
        )
        cursor.execute(f'CREATE INDEX IF NOT EXISTS usage_distinct_clients_period_idx '
                       f'ON {schema}.usage_distinct_clients (period_type, period_start)')
    pg_connection.commit()


def _derived_columns(dataset):
    if dataset.is_transfer:
        return release_species_sql('path'), study_sql('path')
    return 'NULL', 'NULL'


def _day_filter(start, end):
    return f"day BETWEEN DATE '{start.isoformat()}' AND DATE '{end.isoformat()}'"


def daily_usage_sql(datasets, start, end):
    selects = []
    for dataset in datasets:
        release_species, study = _derived_columns(dataset)
        selects.append(f"""
            SELECT day, '{dataset.metric_source}' AS source, direction, {dataset.item_column} AS item,
                   {STATUS_CLASS_SQL} AS status_class, country_code,
                   {release_species} AS release_species, {study} AS study,
                   count(*) AS n_events, count(DISTINCT client_id) AS n_distinct_clients,
                   CAST(sum(bytes) AS BIGINT) AS bytes, sum(duration_secs) AS total_duration_secs
            FROM {dataset.name}
            WHERE {_day_filter(start, end)}
            GROUP BY ALL""")
    return ' UNION ALL '.join(selects)


def distinct_clients_sql(period_type, datasets, start, end):
    events = []
    for dataset in datasets:
        release_species, study = _derived_columns(dataset)
        events.append(f"""
            SELECT day, '{dataset.metric_source}' AS source, {str(dataset.is_transfer).upper()} AS is_transfer,
                   direction, client_id, country_code, {release_species} AS release_species, {study} AS study
            FROM {dataset.name}
            WHERE {_day_filter(start, end)}""")
    period_start = 'day' if period_type == 'day' else f"CAST(date_trunc('{period_type}', day) AS DATE)"
    selects = []
    for dimension in DIMENSIONS[period_type]:
        dimension_column = DIMENSION_COLUMNS[dimension]
        condition = f'{dimension_column} IS NOT NULL' if dimension != 'all' else 'TRUE'
        for source_column, source_condition in (('source', 'TRUE'), (f"'{ALL_TRANSFERS}'", 'is_transfer')):
            selects.append(f"""
                SELECT '{period_type}' AS period_type, {period_start} AS period_start, {source_column} AS source,
                       direction, '{dimension}' AS dimension, {dimension_column} AS dimension_value,
                       count(DISTINCT client_id) AS n_distinct_clients
                FROM events
                WHERE {condition} AND {source_condition}
                GROUP BY ALL""")
    return f"WITH events AS ({' UNION ALL '.join(events)}) {' UNION ALL '.join(selects)}"


def period_bounds(period_type, start, end):
    """Extend the range to whole periods: a month or a year must be computed over all its days."""
    if period_type == 'month':
        next_month = (end.replace(day=1) + timedelta(days=32)).replace(day=1)
        return start.replace(day=1), next_month - timedelta(days=1)
    if period_type == 'year':
        return start.replace(month=1, day=1), end.replace(month=12, day=31)
    return start, end


def month_chunks(start, end):
    """Split the range into calendar months to keep the number of rows fetched at once small."""
    chunk_start = start
    while chunk_start <= end:
        next_month = (chunk_start.replace(day=1) + timedelta(days=32)).replace(day=1)
        yield chunk_start, min(end, next_month - timedelta(days=1))
        chunk_start = next_month


def replace_rows(pg_connection, table, columns, rows, delete_condition, delete_parameters):
    with pg_connection.cursor() as cursor:
        cursor.execute(f'DELETE FROM {table} WHERE {delete_condition}', delete_parameters)
        nb_deleted = cursor.rowcount
        execute_values(cursor, f'INSERT INTO {table} ({", ".join(columns)}) VALUES %s', rows, page_size=10000)
    pg_connection.commit()
    return nb_deleted


def summarise(duckdb_connection, pg_connection, datasets, start, end, schema=DEFAULT_SCHEMA):
    for chunk_start, chunk_end in month_chunks(start, end):
        rows = duckdb_connection.execute(daily_usage_sql(datasets, chunk_start, chunk_end)).fetchall()
        nb_deleted = replace_rows(pg_connection, f'{schema}.usage_daily', DAILY_COLUMNS, rows,
                                  'day BETWEEN %s AND %s', (chunk_start, chunk_end))
        logger.info(f'usage_daily {chunk_start} to {chunk_end}: replaced {nb_deleted} rows with {len(rows)}')

    for period_type in PERIOD_TYPES:
        period_start, period_end = period_bounds(period_type, start, end)
        chunks = month_chunks(period_start, period_end) if period_type == 'day' else [(period_start, period_end)]
        for chunk_start, chunk_end in chunks:
            rows = duckdb_connection.execute(
                distinct_clients_sql(period_type, datasets, chunk_start, chunk_end)
            ).fetchall()
            nb_deleted = replace_rows(pg_connection, f'{schema}.usage_distinct_clients', DISTINCT_COLUMNS, rows,
                                      'period_type = %s AND period_start BETWEEN %s AND %s',
                                      (period_type, chunk_start, chunk_end))
            logger.info(f'usage_distinct_clients per {period_type} {chunk_start} to {chunk_end}: '
                        f'replaced {nb_deleted} rows with {len(rows)}')


def archived_day_range(duckdb_connection, datasets):
    bounds = [duckdb_connection.execute(f'SELECT min(day), max(day) FROM {dataset.name}').fetchone()
              for dataset in datasets]
    bounds = [bound for bound in bounds if bound[0] is not None]
    return min(bound[0] for bound in bounds), max(bound[1] for bound in bounds)


def main():
    parser = ArgumentParser(description='Summarise the archived usage records into Postgres tables')
    parser.add_argument('--schema', default=DEFAULT_SCHEMA, help='Postgres schema of the summary tables')
    period_group = parser.add_mutually_exclusive_group()
    period_group.add_argument('--days-back', type=int, default=15,
                              help='Summarise the complete days of this many last days (default: the neometer window)')
    period_group.add_argument('--from', dest='start', type=date.fromisoformat, help='First day to summarise')
    period_group.add_argument('--rebuild-all', action='store_true', default=False,
                              help='Summarise every day of the archive')
    parser.add_argument('--to', dest='end', type=date.fromisoformat, help='Last day to summarise, with --from')
    parser.add_argument('--create-tables', action='store_true', default=False,
                        help='Create the summary tables if they do not exist')
    args = parser.parse_args()
    logging_config.add_stdout_handler()
    load_config()
    profile, private_config_xml_file = get_maven_settings()
    archive_root = get_archive_dir()

    duckdb_connection = connect_duckdb()
    dataset_names = register_views(duckdb_connection, archive_root, DATASETS)
    if not dataset_names:
        parser.error(f'No records found in {archive_root}')
    datasets = [DATASETS[name] for name in dataset_names]

    yesterday = utc_today() - timedelta(days=1)
    if args.rebuild_all:
        start, end = archived_day_range(duckdb_connection, datasets)
    elif args.start:
        start, end = args.start, args.end or yesterday
    else:
        start, end = yesterday - timedelta(days=args.days_back - 1), yesterday
    logger.info(f'Summarising {", ".join(dataset_names)} from {start} to {end}')

    with get_metadata_connection_handle(profile, private_config_xml_file) as pg_connection:
        if args.create_tables:
            create_tables(pg_connection, args.schema)
        summarise(duckdb_connection, pg_connection, datasets, start, end, args.schema)


if __name__ == '__main__':
    main()
