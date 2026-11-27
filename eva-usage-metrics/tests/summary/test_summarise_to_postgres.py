from datetime import date

from eva_usage_metrics.archive.parquet_datasets import DATASETS, FTP_TRANSFER, HTTP_TRANSFER, WEB_SERVICE
from eva_usage_metrics.archive.parquet_archive import connect_duckdb, day_path, register_views, write_day
from eva_usage_metrics.summary.summarise_to_postgres import (daily_usage_sql, distinct_clients_sql, month_chunks, period_bounds,
                                                 summarise)
from tests.records import record

RELEASE_FILE = '/pub/databases/eva/rs_releases/release_5/by_species/homo_sapiens/GRCh38/x.vcf.gz'
STUDY_FILE = '/pub/databases/eva/PRJEB97324/chr22.vcf.csi'
ENDPOINT = '/eva/webservices/rest/v1/variants/{variant_id}'


def ftp(es_id, event_ts, client_id, status='success', path=RELEASE_FILE, nb_bytes=100):
    return record(FTP_TRANSFER, event_ts, es_id=es_id, client_id=client_id, direction='download', path=path,
                  bytes=nb_bytes, status=status)


def build_archive(root):
    day1, day2 = date(2026, 11, 1), date(2026, 11, 2)
    # FTP: client A downloads the release file on both days, client B once on day 1 and fails once on day 2
    ftp_day1 = [ftp('f1', '2026-11-01T01:00:00Z', 'A'), ftp('f2', '2026-11-01T02:00:00Z', 'B')]
    ftp_day2 = [ftp('f3', '2026-11-02T01:00:00Z', 'A'),
                ftp('f4', '2026-11-02T02:00:00Z', 'B', status='failure', path='/pub/databases/eva/Association',
                    nb_bytes=0)]
    # HTTP: client A again (a different client hash space in practice, but the same value here)
    http_day2 = [record(HTTP_TRANSFER, '2026-11-02T03:00:00Z', es_id='h1', client_id='A', direction='download',
                        path=STUDY_FILE, bytes=8981, status='206', http_method='GET')]
    ws_day1 = [record(WEB_SERVICE, f'2026-11-01T0{hour}:00:00Z', es_id=f'w{hour}', client_id='192.0.2.10',
                      path='/eva/webservices/rest/v1/variants/rs123', endpoint=ENDPOINT, bytes=1234, status='200')
               for hour in (5, 6)]
    for dataset, day, records in ((FTP_TRANSFER, day1, ftp_day1), (FTP_TRANSFER, day2, ftp_day2),
                                  (HTTP_TRANSFER, day2, http_day2), (WEB_SERVICE, day1, ws_day1),
                                  (HTTP_TRANSFER, day1, [])):
        write_day(day_path(root, dataset.name, day), records, dataset.schema)
    connection = connect_duckdb()
    datasets = [DATASETS[name] for name in register_views(connection, root, DATASETS)]
    return connection, datasets


def test_daily_usage(tmp_path):
    connection, datasets = build_archive(str(tmp_path))
    rows = connection.execute(
        f'SELECT * FROM ({daily_usage_sql(datasets, date(2026, 11, 1), date(2026, 11, 2))}) ORDER BY ALL'
    ).fetchall()
    by_key = {(row[0].day, row[1], row[4]): row for row in rows}
    # day, source, direction, item, status_class, country, release_species, study, n_events, n_clients, bytes, dur
    assert by_key[(1, 'ftp_transfer', 'success')][3] == RELEASE_FILE
    assert by_key[(1, 'ftp_transfer', 'success')][6] == 'release_5/homo_sapiens'
    assert by_key[(1, 'ftp_transfer', 'success')][8:11] == (2, 2, 200)
    assert by_key[(2, 'ftp_transfer', 'failure')][8:11] == (1, 1, 0)
    assert by_key[(2, 'http_transfer', '2xx')][7] == 'PRJEB97324'
    assert by_key[(2, 'http_transfer', '2xx')][8:11] == (1, 1, 8981)
    ws = by_key[(1, 'web_service', '2xx')]
    assert ws[2] is None and ws[3] == ENDPOINT
    assert ws[6:11] == (None, None, 2, 1, 2468)
    assert by_key[(2, 'ftp_transfer', 'success')][8:11] == (1, 1, 100)
    assert len(rows) == 5


def test_distinct_clients(tmp_path):
    connection, datasets = build_archive(str(tmp_path))
    start, end = date(2026, 11, 1), date(2026, 11, 30)
    months = {(row[2], row[3], row[4], row[5]): row[6] for row in connection.execute(
        distinct_clients_sql('month', datasets, start, end)).fetchall()}
    # Clients are counted once over the month, not once per day
    assert months[('ftp_transfer', 'download', 'all', None)] == 2
    assert months[('http_transfer', 'download', 'all', None)] == 1
    assert months[('all_transfers', 'download', 'all', None)] == 2
    assert months[('web_service', None, 'all', None)] == 1
    assert months[('ftp_transfer', 'download', 'release_species', 'release_5/homo_sapiens')] == 2
    assert months[('http_transfer', 'download', 'study', 'PRJEB97324')] == 1
    assert months[('web_service', None, 'country', 'US')] == 1
    assert ('web_service', None, 'release_species', None) not in months

    days = connection.execute(distinct_clients_sql('day', datasets, start, end)).fetchall()
    ftp_days = sorted((row[1].day, row[6]) for row in days if row[2] == 'ftp_transfer')
    assert ftp_days == [(1, 2), (2, 2)]
    assert {row[4] for row in days} == {'all'}


def test_period_bounds_and_chunks():
    assert period_bounds('month', date(2026, 11, 12), date(2026, 12, 3)) == (date(2026, 11, 1), date(2026, 12, 31))
    assert period_bounds('year', date(2026, 11, 12), date(2026, 11, 25)) == (date(2026, 1, 1), date(2026, 12, 31))
    assert period_bounds('day', date(2026, 11, 12), date(2026, 11, 25)) == (date(2026, 11, 12), date(2026, 11, 25))
    assert list(month_chunks(date(2026, 11, 20), date(2027, 1, 5))) == [
        (date(2026, 11, 20), date(2026, 11, 30)), (date(2026, 12, 1), date(2026, 12, 31)),
        (date(2027, 1, 1), date(2027, 1, 5)),
    ]


class FakeCursor:
    def __init__(self, statements):
        self.statements = statements
        self.rowcount = 0

    def __enter__(self):
        return self

    def __exit__(self, *args):
        pass

    def execute(self, sql, parameters=None):
        self.statements.append((sql, parameters))

    @property
    def connection(self):
        return self


class FakePgConnection:
    def __init__(self):
        self.statements = []
        self.inserted = {}
        self.commits = 0

    def cursor(self):
        return FakeCursor(self.statements)

    def commit(self):
        self.commits += 1


def test_summarise_replaces_whole_periods(tmp_path, monkeypatch):
    connection, datasets = build_archive(str(tmp_path))
    pg_connection = FakePgConnection()

    def fake_execute_values(cursor, sql, rows, page_size):
        table = sql.split()[2]
        pg_connection.inserted.setdefault(table, []).extend(rows)

    monkeypatch.setattr('eva_usage_metrics.summary.summarise_to_postgres.execute_values', fake_execute_values)
    summarise(connection, pg_connection, datasets, date(2026, 11, 1), date(2026, 11, 2), schema='test')
    deletes = [parameters for sql, parameters in pg_connection.statements if sql.startswith('DELETE')]
    assert deletes == [
        (date(2026, 11, 1), date(2026, 11, 2)),
        ('day', date(2026, 11, 1), date(2026, 11, 2)),
        ('month', date(2026, 11, 1), date(2026, 11, 30)),
        ('year', date(2026, 1, 1), date(2026, 12, 31)),
    ]
    assert len(pg_connection.inserted['test.usage_daily']) == 5
    assert pg_connection.commits == 4
