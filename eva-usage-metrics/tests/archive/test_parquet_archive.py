import os
from datetime import date

import pyarrow.parquet as pq
import pytest

from eva_usage_metrics.archive import parquet_archive
from eva_usage_metrics.archive.parquet_datasets import FTP_TRANSFER as FTP
from eva_usage_metrics.archive.parquet_archive import day_path, days_to_load, existing_days, write_day
from tests.records import record

FTP_RECORD = record(FTP, '2026-11-10T03:27:46Z', es_index='index1', es_id='ftp1', bytes=0)
TODAY = date(2026, 11, 26)


def test_days_to_load_fills_gaps_and_reloads_recent_days(tmp_path):
    root = str(tmp_path)
    # Days 12 to 23 archived, 15 missing
    for day_number in range(12, 24):
        if day_number != 15:
            write_day(day_path(root, 'ftp_transfer', date(2026, 11, day_number)), [], FTP.schema)
    days = days_to_load(root, 'ftp_transfer', today=TODAY)
    assert days == [date(2026, 11, 15), date(2026, 11, 24), date(2026, 11, 25)]


def test_days_to_load_never_includes_today(tmp_path):
    days = days_to_load(str(tmp_path), 'ftp_transfer', today=TODAY)
    assert len(days) == 14
    assert days[0] == date(2026, 11, 12)
    assert days[-1] == date(2026, 11, 25)


def test_write_day_deduplicates_and_reads_back(tmp_path):
    path = day_path(str(tmp_path), 'ftp_transfer', date(2026, 11, 10))
    record = FTP_RECORD
    other = {**record, 'es_id': 'ftp2'}
    assert write_day(path, [record, other, dict(record)], FTP.schema) == 2
    table = pq.read_table(path)
    assert table.schema.names == FTP.schema.names
    assert sorted(table.column('es_id').to_pylist()) == ['ftp1', 'ftp2']
    assert existing_days(str(tmp_path), 'ftp_transfer') == {date(2026, 11, 10)}


def test_write_day_leaves_no_file_on_failure(tmp_path, monkeypatch):
    path = day_path(str(tmp_path), 'ftp_transfer', date(2026, 11, 10))

    def failing_write(table, where, **kwargs):
        open(where, 'w').close()
        raise IOError('disk full')

    monkeypatch.setattr(parquet_archive.pq, 'write_table', failing_write)
    with pytest.raises(IOError):
        write_day(path, [], FTP.schema)
    assert os.listdir(os.path.dirname(path)) == []
    assert existing_days(str(tmp_path), 'ftp_transfer') == set()


def test_register_views_exposes_the_day(tmp_path):
    root = str(tmp_path)
    write_day(day_path(root, 'ftp_transfer', date(2026, 11, 10)), [FTP_RECORD], FTP.schema)
    connection = parquet_archive.connect_duckdb()
    assert parquet_archive.register_views(connection, root, ['ftp_transfer', 'http_transfer']) == ['ftp_transfer']
    assert connection.execute('SELECT day, bytes FROM ftp_transfer').fetchall() == [(date(2026, 11, 10), 0)]
