from datetime import datetime, timezone

import pyarrow as pa
import pytest

from eva_usage_metrics.neometer.data_stream_extractors import EXTRACTORS, WebServiceExtractor
from tests.samples import FTP_FAILURE, GLOBUS, HTTP_PARTIAL, WEB_SERVICE


def map_and_check(extractor, hit):
    record = extractor.map_record(hit)
    assert list(record) == extractor.dataset.schema.names
    # The record can be written with the schema of the dataset
    pa.Table.from_pylist([record], schema=extractor.dataset.schema)
    return record


def test_ftp_failure_keeps_file_size_of_zero():
    record = map_and_check(EXTRACTORS['ftp_transfer'], FTP_FAILURE)
    assert record['bytes'] == 0
    assert record['status'] == 'failure'
    assert record['direction'] == 'download'
    assert record['duration_secs'] == 1.0
    assert record['access_mode'] == 'a'
    assert record['client_id'] == 'iNu6'
    assert record['path'] == '/pub/databases/eva/Association'
    assert record['event_ts'] == datetime(2026, 11, 10, 3, 27, 46, tzinfo=timezone.utc)
    assert (record['country_code'], record['region'], record['city']) == ('US', 'Maryland', 'Gaithersburg')
    assert (record['es_index'], record['es_id']) == (FTP_FAILURE['_index'], 'ftp1')


def test_http_partial_response():
    record = map_and_check(EXTRACTORS['http_transfer'], HTTP_PARTIAL)
    assert record['path'] == '/pub/databases/eva/PRJEB97324/chr22.vcf.csi'
    assert record['bytes'] == 8981
    assert record['status'] == '206'
    assert record['direction'] == 'download'
    assert record['duration_secs'] is None


def test_globus_duration_from_start_and_end():
    record = map_and_check(EXTRACTORS['globus_transfer'], GLOBUS)
    assert record['duration_secs'] == pytest.approx(441.119)
    assert record['user_name'] == 'globus-ebi'
    assert record['bytes'] == 67108864
    assert record['country_code'] is None


def test_web_service_request():
    record = map_and_check(WebServiceExtractor(geolocate=False), WEB_SERVICE)
    assert record['endpoint'] == '/eva/webservices/rest/v1/variants/{variant_id}'
    assert record['status'] == '200'
    assert record['bytes'] == 1234
    assert record['direction'] is None
    assert (record['latitude'], record['longitude']) == (39.14, -77.2)
    assert record['client_id'] == '192.0.2.10'
    assert record['server_node'] == 'wp-p1m-01:8080'


def test_web_service_geolocation_fallback(monkeypatch):
    lookups = []

    def fake_get_location(ip_address):
        lookups.append(ip_address)
        return {'country_code': 'GB', 'city': 'Hinxton', 'state': 'England', 'latitude': 52.08, 'longitude': 0.18}

    monkeypatch.setattr('eva_usage_metrics.neometer.data_stream_extractors.get_location', fake_get_location)
    hit = {**WEB_SERVICE, '_source': {key: value for key, value in WEB_SERVICE['_source'].items()
                                      if not key.startswith('geo.')}}
    record = WebServiceExtractor(geolocate=True).map_record(hit)
    assert lookups == ['192.0.2.10']
    assert (record['country_code'], record['region'], record['city']) == ('GB', 'England', 'Hinxton')
    # Located by neometer: no lookup
    WebServiceExtractor(geolocate=True).map_record(WEB_SERVICE)
    assert lookups == ['192.0.2.10']


def test_every_data_stream_fills_its_dataset():
    for name, extractor in EXTRACTORS.items():
        assert extractor.dataset.name == name
        assert extractor.data_stream and extractor.es_filters
