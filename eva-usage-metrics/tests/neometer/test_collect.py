from datetime import date

import pyarrow.parquet as pq

from eva_usage_metrics.archive.storage import day_path, existing_days
from eva_usage_metrics.neometer.collect import collect
from eva_usage_metrics.neometer.extractors import EXTRACTORS
from tests.samples import FTP_FAILURE, HTTP_PARTIAL


class FakeElasticClient:
    def __init__(self, hits_per_stream, failing_streams=()):
        self.hits_per_stream = hits_per_stream
        self.failing_streams = failing_streams
        self.requests = []

    def iter_day(self, data_stream, filters, day):
        self.requests.append((data_stream, day))
        if data_stream in self.failing_streams:
            raise ConnectionError('Elasticsearch unavailable')
        yield from self.hits_per_stream.get(data_stream, [])


def test_collect_writes_one_file_per_day_and_carries_on_after_a_failure(tmp_path):
    root = str(tmp_path)
    ftp, http = EXTRACTORS['ftp_transfer'], EXTRACTORS['http_transfer']
    client = FakeElasticClient({ftp.data_stream: [FTP_FAILURE]}, failing_streams=[http.data_stream])
    days = [date(2026, 11, 10), date(2026, 11, 11)]
    failed = collect(client, [http, ftp], root, days=days)
    assert failed == ['http_transfer']
    assert existing_days(root, 'ftp_transfer') == set(days)
    assert existing_days(root, 'http_transfer') == set()
    assert pq.read_table(day_path(root, 'ftp_transfer', days[0])).num_rows == 1


def test_collect_writes_empty_days_and_nothing_on_dry_run(tmp_path):
    root = str(tmp_path)
    http = EXTRACTORS['http_transfer']
    client = FakeElasticClient({http.data_stream: [HTTP_PARTIAL]})
    assert collect(client, [http], root, days=[date(2026, 11, 19)], dry_run=True) == []
    assert existing_days(root, 'http_transfer') == set()

    client = FakeElasticClient({})
    assert collect(client, [http], root, days=[date(2026, 11, 19)]) == []
    assert pq.read_table(day_path(root, 'http_transfer', date(2026, 11, 19))).num_rows == 0
