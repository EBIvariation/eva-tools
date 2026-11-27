"""
Archive of the raw usage records: one Parquet file per dataset and per UTC day.

    <archive_root>/<dataset>/day=YYYY-MM-DD/data.parquet

A day file is either complete or absent: it is written to a temporary file which is then renamed.
"""
import glob
import os
from datetime import date, datetime, timedelta, timezone

import duckdb
import pyarrow as pa
import pyarrow.parquet as pq

FILE_NAME = 'data.parquet'
DAY_PREFIX = 'day='


def utc_today():
    return datetime.now(timezone.utc).date()


def day_directory(archive_root, dataset_name, day):
    return os.path.join(archive_root, dataset_name, f'{DAY_PREFIX}{day.isoformat()}')


def day_path(archive_root, dataset_name, day):
    return os.path.join(day_directory(archive_root, dataset_name, day), FILE_NAME)


def existing_days(archive_root, dataset_name):
    """Days with at least one Parquet file (the legacy export writes several files per day)."""
    days = set()
    for file_path in glob.glob(os.path.join(archive_root, dataset_name, f'{DAY_PREFIX}*', '*.parquet')):
        day_text = os.path.basename(os.path.dirname(file_path))[len(DAY_PREFIX):]
        days.add(date.fromisoformat(day_text))
    return days


def days_to_load(archive_root, dataset_name, today=None, max_age_days=14, reload_days=2):
    """
    Complete UTC days still available in the data stream that need loading: the ones not archived yet and the
    most recent ones, reloaded to include documents indexed late. Today is never returned since it is not complete.
    """
    today = today or utc_today()
    archived = existing_days(archive_root, dataset_name)
    window = [today - timedelta(days=age) for age in range(max_age_days, 0, -1)]
    return [day for day in window if day not in archived or (today - day).days <= reload_days]


def deduplicate(rows, key_columns=('es_index', 'es_id')):
    """Keep the last occurrence of each key. Rows without a complete key are all kept."""
    unique_rows = {}
    for position, row in enumerate(rows):
        key = tuple(row.get(column) for column in key_columns)
        unique_rows[key if None not in key else ('no key', position)] = row
    return list(unique_rows.values())


def write_day(path, rows, schema):
    """Write the rows of one day, replacing any previous file for that day. Returns the number of rows written."""
    table = pa.Table.from_pylist(deduplicate(rows), schema=schema)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    temporary_path = path + '.tmp'
    try:
        pq.write_table(table, temporary_path, compression='zstd')
        os.replace(temporary_path, path)
    finally:
        if os.path.exists(temporary_path):
            os.remove(temporary_path)
    return table.num_rows


def connect_duckdb():
    connection = duckdb.connect()
    # Days are UTC days: casting a timestamp to a date must not depend on the local time zone
    connection.execute("SET TimeZone = 'UTC'")
    return connection


def parquet_glob(archive_root, dataset_name):
    return os.path.join(archive_root, dataset_name, f'{DAY_PREFIX}*', '*.parquet')


def register_views(connection, archive_root, dataset_names):
    """
    Create one DuckDB view per dataset over all its day files, with the day as a column.
    Returns the names of the datasets that have data.
    """
    registered = []
    for dataset_name in dataset_names:
        if not glob.glob(parquet_glob(archive_root, dataset_name)):
            continue
        file_glob = parquet_glob(archive_root, dataset_name).replace("'", "''")
        connection.execute(
            f'CREATE OR REPLACE VIEW {dataset_name} AS SELECT * FROM read_parquet('
            f"'{file_glob}', hive_partitioning = true, hive_types = {{'day': DATE}}, union_by_name = true)"
        )
        registered.append(dataset_name)
    return registered
