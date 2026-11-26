#!/usr/bin/env python
"""
Export the legacy Postgres usage tables (ws_traffic, ws_traffic_hist and ftp_traffic) to the Parquet archive, with the
same columns as the datasets collected from neometer. Run once, before the legacy tables are dropped.

The export of each dataset stops at the first day collected from neometer, so that no day is covered by both. The
number of records per day in Postgres and in the archive are compared at the end: the command fails if they differ.
The legacy tables are never modified.
"""
import glob
import os
import sys
import tempfile
from argparse import ArgumentParser
from datetime import date
from urllib.parse import urlparse

from ebi_eva_common_pyutils.logger import logging_config
from ebi_eva_internal_pyutils.config_utils import get_metadata_creds_for_profile
from ebi_eva_internal_pyutils.metadata_utils import get_metadata_connection_handle

from eva_usage_metrics.archive.config import get_archive_dir, get_maven_settings, load_config
from eva_usage_metrics.archive.endpoints import normalise_path
from eva_usage_metrics.archive.storage import connect_duckdb, existing_days, parquet_glob
from eva_usage_metrics.legacy.queries import ENDPOINT_PATH, LEGACY_EXPORTS

logger = logging_config.get_logger(__name__)

DUCKDB_TYPES = {'string': 'VARCHAR', 'int64': 'BIGINT', 'double': 'DOUBLE', 'timestamp[us, tz=UTC]': 'TIMESTAMPTZ'}


def register_endpoint_function(duckdb_connection):
    duckdb_connection.create_function('normalise_endpoint', lambda path: normalise_path(path) if path else None,
                                      ['VARCHAR'], 'VARCHAR', null_handling='special')


def legacy_query(export, tables, cutoff):
    """Postgres query returning the records of the legacy tables before the cutoff day."""
    union = ' UNION ALL '.join(export.select_sql(table) for table in tables)
    conditions = ['event_ts IS NOT NULL']
    if cutoff:
        conditions.append(f"event_ts < '{cutoff.isoformat()}T00:00:00Z'::timestamp with time zone")
    return f"SELECT * FROM ({union}) legacy WHERE {' AND '.join(conditions)}"


def legacy_column_types(export):
    """DuckDB types of the columns of the legacy query."""
    field_types = {field.name: DUCKDB_TYPES[str(field.type)] for field in export.dataset.schema}
    return {name: 'VARCHAR' if name == ENDPOINT_PATH else field_types[name] for name in export.columns}


def archive_select(export, relation):
    """Select the archive columns, in the order of the schema, from the records of the legacy query."""
    columns = []
    for field in export.dataset.schema:
        if field.name == 'endpoint':
            columns.append(f'normalise_endpoint({ENDPOINT_PATH}) AS endpoint')
        else:
            columns.append(f'CAST({field.name} AS {DUCKDB_TYPES[str(field.type)]}) AS {field.name}')
    return f"SELECT {', '.join(columns)}, CAST(event_ts AS DATE) AS day FROM {relation}"


def libpq_connection_string(profile, private_config_xml_file):
    jdbc_url, user, password = get_metadata_creds_for_profile(profile, private_config_xml_file)
    url = urlparse(jdbc_url[len('jdbc:'):] if jdbc_url.startswith('jdbc:') else jdbc_url)
    parts = {'host': url.hostname, 'port': url.port or 5432, 'dbname': url.path.lstrip('/'),
             'user': user, 'password': password}
    return ' '.join(f"{key}='{str(value)}'" for key, value in parts.items())


def postgres_relation_with_duckdb(duckdb_connection, query, profile, private_config_xml_file):
    """Stream the query through the DuckDB postgres extension (needs to download the extension)."""
    duckdb_connection.execute('INSTALL postgres')
    duckdb_connection.execute('LOAD postgres')
    connection_string = libpq_connection_string(profile, private_config_xml_file).replace("'", "''")
    duckdb_connection.execute(f"ATTACH '{connection_string}' AS pg (TYPE postgres, READ_ONLY)")
    escaped_query = query.replace("'", "''")
    return f"postgres_query('pg', '{escaped_query}')"


def postgres_relation_with_csv(query, column_types, pg_connection, temp_dir):
    """Dump the query to a temporary CSV file with psycopg2, for when the postgres extension is unavailable."""
    csv_path = os.path.join(temp_dir, 'legacy_export.csv')
    with pg_connection.cursor() as cursor, open(csv_path, 'w') as open_file:
        cursor.execute("SET TIME ZONE 'UTC'")
        cursor.copy_expert(f'COPY ({query}) TO STDOUT WITH (FORMAT csv, HEADER true)', open_file)
    types_sql = ', '.join(f"'{name}': '{column_type}'" for name, column_type in column_types.items())
    return f"read_csv('{csv_path}', header = true, columns = {{{types_sql}}})"


def count_per_day_in_postgres(pg_connection, query):
    with pg_connection.cursor() as cursor:
        cursor.execute(f"SELECT CAST(event_ts AT TIME ZONE 'UTC' AS date), count(*) FROM ({query}) records GROUP BY 1")
        return dict(cursor.fetchall())


def count_per_day_in_archive(duckdb_connection, archive_root, dataset_name):
    file_glob = parquet_glob(archive_root, dataset_name).replace("'", "''")
    return dict(duckdb_connection.execute(
        f"SELECT day, count(*) FROM read_parquet('{file_glob}', hive_partitioning = true, "
        f"hive_types = {{'day': DATE}}) GROUP BY day"
    ).fetchall())


def compare_counts(postgres_counts, archive_counts):
    """Return the days with a different number of records, with both counts."""
    return {
        day: (postgres_counts.get(day, 0), archive_counts.get(day, 0))
        for day in sorted(set(postgres_counts) | set(archive_counts))
        if postgres_counts.get(day, 0) != archive_counts.get(day, 0)
    }


def get_cutoff(archive_root, export, cutoff, no_cutoff):
    if cutoff or no_cutoff:
        return cutoff
    live_days = existing_days(archive_root, export.live_dataset.name)
    return min(live_days) if live_days else None


def export_dataset(export, tables, archive_root, cutoff, method, profile, private_config_xml_file, temp_dir,
                   overwrite=False):
    dataset_name = export.dataset.name
    target = os.path.join(archive_root, dataset_name)
    if glob.glob(parquet_glob(archive_root, dataset_name)) and not overwrite:
        raise FileExistsError(f'{target} already contains records: use --overwrite to export {dataset_name} again')
    query = legacy_query(export, tables, cutoff)
    duckdb_connection = connect_duckdb()
    register_endpoint_function(duckdb_connection)
    with get_metadata_connection_handle(profile, private_config_xml_file) as pg_connection:
        if method == 'duckdb':
            relation = postgres_relation_with_duckdb(duckdb_connection, query, profile, private_config_xml_file)
        else:
            relation = postgres_relation_with_csv(query, legacy_column_types(export), pg_connection, temp_dir)
        logger.info(f'Exporting {", ".join(tables)} to {target} (cutoff: {cutoff or "none"})')
        duckdb_connection.execute(
            f"COPY ({archive_select(export, relation)}) TO '{target}' "
            "(FORMAT parquet, COMPRESSION zstd, PARTITION_BY (day), OVERWRITE true, FILENAME_PATTERN 'data_{i}')"
        )
        logger.info(f'Comparing the number of records per day of {dataset_name}')
        differences = compare_counts(count_per_day_in_postgres(pg_connection, query),
                                     count_per_day_in_archive(duckdb_connection, archive_root, dataset_name))
    for day, (postgres_count, archive_count) in list(differences.items())[:20]:
        logger.error(f'{dataset_name} {day}: {postgres_count} records in Postgres, {archive_count} in the archive')
    return differences


def dry_run(export, tables, cutoff, profile, private_config_xml_file):
    """Run the legacy query on a few records to check it against the actual tables."""
    query = legacy_query(export, tables, cutoff)
    with get_metadata_connection_handle(profile, private_config_xml_file) as pg_connection:
        with pg_connection.cursor() as cursor:
            for table in tables:
                cursor.execute(f'SELECT count(*) FROM ({legacy_query(export, [table], cutoff)}) records')
                logger.info(f'Dry run: {cursor.fetchone()[0]} records of {table} would be exported to '
                            f'{export.dataset.name}')
            cursor.execute(f'{query} LIMIT 5')
            column_names = [column.name for column in cursor.description]
            for row in cursor.fetchall():
                logger.info(f'Dry run: example record {dict(zip(column_names, row))}')


def main():
    parser = ArgumentParser(description=__doc__)
    parser.add_argument('--dataset', action='append', choices=sorted(LEGACY_EXPORTS),
                        help='Legacy dataset to export, can be repeated. All by default')
    parser.add_argument('--web-service-tables', nargs='+',
                        default=list(LEGACY_EXPORTS['legacy_web_service'].default_tables),
                        help='Legacy web service tables, with the columns of ws_traffic')
    parser.add_argument('--ftp-tables', nargs='+', default=list(LEGACY_EXPORTS['legacy_ftp_transfer'].default_tables),
                        help='Legacy FTP tables, with the columns of ftp_traffic')
    cutoff_group = parser.add_mutually_exclusive_group()
    cutoff_group.add_argument('--cutoff', type=date.fromisoformat,
                              help='Only export records before this UTC day (YYYY-MM-DD). '
                                   'Defaults to the first day collected from neometer')
    cutoff_group.add_argument('--no-cutoff', action='store_true', default=False,
                              help='Export all the legacy records, even if they overlap with the neometer records')
    parser.add_argument('--method', choices=('duckdb', 'csv'), default='duckdb',
                        help='Read Postgres with the DuckDB postgres extension, or through a temporary CSV file when '
                             'the extension cannot be installed')
    parser.add_argument('--temp-dir', help='Directory for the temporary CSV file of the csv method')
    parser.add_argument('--overwrite', action='store_true', default=False,
                        help='Replace a previous export of the same dataset')
    parser.add_argument('--dry-run', action='store_true', default=False,
                        help='Count the records to export and show a few of them, without writing anything')
    args = parser.parse_args()
    logging_config.add_stdout_handler()
    load_config()
    profile, private_config_xml_file = get_maven_settings()
    archive_root = get_archive_dir()

    tables = {'legacy_web_service': args.web_service_tables, 'legacy_ftp_transfer': args.ftp_tables}
    failed = False
    for dataset_name in args.dataset or LEGACY_EXPORTS:
        export = LEGACY_EXPORTS[dataset_name]
        cutoff = get_cutoff(archive_root, export, args.cutoff, args.no_cutoff)
        if not cutoff and not args.no_cutoff:
            # Without a cutoff, the days already collected from neometer would be counted twice
            parser.error(f'No {export.live_dataset.name} records found in {archive_root} to define the cutoff of '
                         f'{dataset_name}. Provide --cutoff or --no-cutoff.')
        if args.dry_run:
            dry_run(export, tables[dataset_name], cutoff, profile, private_config_xml_file)
            continue
        with tempfile.TemporaryDirectory(dir=args.temp_dir) as temp_dir:
            differences = export_dataset(export, tables[dataset_name], archive_root, cutoff, args.method,
                                         profile, private_config_xml_file, temp_dir, args.overwrite)
        if differences:
            logger.error(f'{dataset_name}: {len(differences)} days differ between Postgres and the archive')
            failed = True
        else:
            logger.info(f'{dataset_name}: the number of records per day match')
    if failed:
        sys.exit(1)


if __name__ == '__main__':
    main()
