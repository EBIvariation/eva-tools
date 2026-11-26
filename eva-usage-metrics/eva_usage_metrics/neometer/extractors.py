"""
Extraction of the records of each neometer data stream into the columns of its dataset. Each extractor only defines
where its records come from and how to read its fields.
"""
from datetime import datetime

from eva_usage_metrics.archive.datasets import FTP_TRANSFER, GLOBUS_TRANSFER, HTTP_TRANSFER, WEB_SERVICE
from eva_usage_metrics.archive.endpoints import normalise_path
from eva_usage_metrics.neometer.elastic import get_field, get_first_field
from eva_usage_metrics.neometer.geolocation import get_location

# File transfers of EVA data, whether they record a file path or a URL
EVA_PATH_PATTERN = '*/pub/databases/eva/*'

NETWORK_DIRECTIONS = {'egress': 'download', 'ingress': 'upload'}
HTTP_METHOD_DIRECTIONS = {'GET': 'download', 'HEAD': 'download', 'PUT': 'upload', 'POST': 'upload'}


def parse_timestamp(value):
    if value is None or isinstance(value, datetime):
        return value
    # Python before 3.11 does not accept the 'Z' suffix
    return datetime.fromisoformat(value.replace('Z', '+00:00'))


def to_int(value):
    return None if value in (None, '') else int(value)


def to_float(value):
    return None if value in (None, '') else float(value)


def to_str(value):
    return None if value is None else str(value)


class Extractor:
    dataset = None         # dataset of the archive the records are written to
    data_stream = None     # Elasticsearch data stream
    es_filters = []        # filters selecting the EVA records in the data stream

    def extract_fields(self, doc_source):
        """Return the dataset-specific values, and any common column that is not read from the default field."""
        raise NotImplementedError

    def map_record(self, hit):
        doc_source = hit['_source']
        record = {
            'event_ts': get_field(doc_source, '@timestamp'),
            'es_index': hit.get('_index'),
            'es_id': hit.get('_id'),
            'country_code': get_field(doc_source, 'geo.country_iso_code'),
            'region': get_field(doc_source, 'geo.region_name'),
            'city': get_field(doc_source, 'geo.city_name'),
        }
        record.update(self.extract_fields(doc_source))
        column_names = self.dataset.schema.names
        unknown_columns = set(record) - set(column_names)
        if unknown_columns:
            raise ValueError(f'{self.dataset.name} has no columns {sorted(unknown_columns)}')
        record['event_ts'] = parse_timestamp(record['event_ts'])
        return {column: record.get(column) for column in column_names}


def get_latitude_longitude(geo_location):
    """Extract (latitude, longitude) from a geo_point stored either as a [lon, lat] array or as a dict."""
    if isinstance(geo_location, dict):
        return to_float(geo_location.get('lat')), to_float(geo_location.get('lon'))
    if isinstance(geo_location, (list, tuple)) and len(geo_location) == 2:
        longitude, latitude = geo_location
        return to_float(latitude), to_float(longitude)
    return None, None


class WebServiceExtractor(Extractor):
    dataset = WEB_SERVICE
    data_stream = 'logs-web-realtime'
    es_filters = [{'wildcard': {'url.path': {'value': '/eva/webservices/*'}}}]

    def __init__(self, geolocate=True):
        # Look up the location of the clients that neometer could not locate: records cannot be updated once archived
        self.geolocate = geolocate

    def extract_fields(self, doc_source):
        client_ip = get_field(doc_source, 'client.ip')
        endpoint_path = get_first_field(doc_source, 'url.path_base', 'url.path')
        latitude, longitude = get_latitude_longitude(get_field(doc_source, 'geo.location'))
        fields = {
            'client_id': client_ip,
            'path': get_field(doc_source, 'url.path'),
            'bytes': to_int(get_field(doc_source, 'http.response.bytes')),
            'duration_secs': to_float(get_field(doc_source, 'http.response.time_to_first_byte')),
            'status': to_str(get_field(doc_source, 'http.response.status_code')),
            'endpoint': normalise_path(endpoint_path) if endpoint_path else None,
            'query': get_field(doc_source, 'url.query'),
            'http_method': get_field(doc_source, 'http.request.method'),
            'user_agent': get_field(doc_source, 'user_agent.original'),
            'server_node': get_field(doc_source, 'destination.address'),
            'latitude': latitude,
            'longitude': longitude,
        }
        if self.geolocate and not get_field(doc_source, 'geo.country_iso_code'):
            location = get_location(client_ip)
            fields.update({
                'country_code': location.get('country_code'),
                'region': location.get('state'),
                'city': location.get('city'),
                'latitude': to_float(location.get('latitude')),
                'longitude': to_float(location.get('longitude')),
            })
        return fields


class HttpTransferExtractor(Extractor):
    dataset = HTTP_TRANSFER
    data_stream = 'logs-transfer.http-default'
    # HTTP records the requested address instead of the file path
    es_filters = [{'wildcard': {'url.original': {'value': EVA_PATH_PATTERN}}}]

    def extract_fields(self, doc_source):
        http_method = get_field(doc_source, 'http.request.method')
        return {
            'client_id': get_field(doc_source, 'client.hash'),
            'direction': HTTP_METHOD_DIRECTIONS.get(http_method),
            'path': get_field(doc_source, 'url.original'),
            # Partial (206) responses only record the bytes of the requested range
            'bytes': to_int(get_field(doc_source, 'http.response.body.bytes')),
            'status': to_str(get_field(doc_source, 'http.response.status_code')),
            'http_method': http_method,
        }


class FtpTransferExtractor(Extractor):
    dataset = FTP_TRANSFER
    data_stream = 'logs-transfer.ftp-default'
    es_filters = [{'wildcard': {'file.path': {'value': EVA_PATH_PATTERN}}}]

    def extract_fields(self, doc_source):
        return {
            'client_id': get_field(doc_source, 'client.hash'),
            'direction': NETWORK_DIRECTIONS.get(get_field(doc_source, 'network.direction')),
            'path': get_field(doc_source, 'file.path'),
            # Failed transfers record a file size of 0
            'bytes': to_int(get_field(doc_source, 'file.size')),
            'duration_secs': to_float(get_field(doc_source, 'ftp.transfer.duration_secs')),
            'status': get_field(doc_source, 'event.outcome'),
            'access_mode': get_field(doc_source, 'ftp.access.mode'),
            'user_name': get_field(doc_source, 'user.name'),
        }


class GlobusTransferExtractor(Extractor):
    dataset = GLOBUS_TRANSFER
    data_stream = 'logs-transfer.globus-default'
    es_filters = [{'wildcard': {'file.path': {'value': EVA_PATH_PATTERN}}}]

    def extract_fields(self, doc_source):
        event_start = parse_timestamp(get_field(doc_source, 'event.start'))
        event_end = parse_timestamp(get_field(doc_source, 'event.end'))
        duration_secs = None
        if isinstance(event_start, datetime) and isinstance(event_end, datetime):
            duration_secs = (event_end - event_start).total_seconds()
        return {
            'client_id': get_field(doc_source, 'client.hash'),
            'direction': NETWORK_DIRECTIONS.get(get_field(doc_source, 'network.direction')),
            'path': get_field(doc_source, 'file.path'),
            'bytes': to_int(get_field(doc_source, 'file.size')),
            'duration_secs': duration_secs,
            'status': get_field(doc_source, 'event.outcome'),
            'user_name': get_field(doc_source, 'user.name'),
            'event_start': event_start,
            'event_end': event_end,
        }


EXTRACTORS = {extractor.dataset.name: extractor for extractor in (
    WebServiceExtractor(), HttpTransferExtractor(), FtpTransferExtractor(), GlobusTransferExtractor()
)}
