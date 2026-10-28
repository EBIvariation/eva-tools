#!/usr/bin/python
import os
from argparse import ArgumentParser
from functools import lru_cache

import psycopg2
import requests
from ebi_eva_common_pyutils.logger import logging_config
from ebi_eva_common_pyutils.metadata_utils import get_metadata_connection_handle
from ebi_eva_common_pyutils.pg_utils import get_all_results_for_query
from psycopg2.extras import execute_batch

from requests.auth import HTTPBasicAuth
from requests import HTTPError

from retry import retry

logger = logging_config.get_logger(__name__)
logging_config.add_stdout_handler()

WS_TABLE_NAME = 'eva_web_srvc_stats.ws_traffic'
WS_DATA_STREAM = 'logs-web-realtime'

@retry(tries=5, delay=8, backoff=1.2, jitter=(1, 3))
def _get_location(ip_address):
    if ip_address:
        response = requests.get('https://geolocation-db.com/json/' + ip_address)
        response.raise_for_status()
        return response.json()
    else:
        return {}

@lru_cache(maxsize=None)
def get_location(ip_address):
    try:
        return _get_location(ip_address)
    except HTTPError:
        return {}

def get_most_recent_timestamp(private_config_xml_file):
    with get_metadata_connection_handle('production_processing', private_config_xml_file) as metadata_connection_handle:
        results = get_all_results_for_query(
            metadata_connection_handle,
            f"select max(event_ts_txt) as recent_ts from {WS_TABLE_NAME};"
        )
        if results and results[0][0]:
            return results[0][0]
    return None


@retry(tries=4, delay=2, backoff=1.2, jitter=(1, 3))
def query(kibana_host, basic_auth,  batch_size, most_recent_timestamp=None):
    first_query_url = os.path.join(kibana_host, f'{WS_DATA_STREAM}/_search?scroll=24h')
    query_conditions = [{'wildcard': {'url.path': {'value': '/eva/webservices/*'}}}]
    if most_recent_timestamp:
        query_conditions.append({'range': {'@timestamp': {'gt': most_recent_timestamp}}})
    post_query = {
        'size': str(batch_size),
        'track_total_hits': True,
        'query': {'bool': {'must': query_conditions}}
    }

    response = requests.post(first_query_url, auth=basic_auth, json=post_query)
    response.raise_for_status()
    data = response.json()
    total = data['hits']['total']['value']
    if total == 0:
        logger.info('No results found')
        return None, None, None
    scroll_id = data['_scroll_id']
    return scroll_id, total, data['hits']['hits']

@retry(tries=4, delay=2, backoff=1.2, jitter=(1, 3))
def scroll(kibana_host, basic_auth, scroll_id):
    query_url = os.path.join(kibana_host, '_search/scroll')
    response = requests.post(query_url, auth=basic_auth, json={'scroll': '24h', 'scroll_id': scroll_id})
    response.raise_for_status()
    data = response.json()
    return data['hits']['hits']

def load_batch_to_table(batch, private_config_xml_file, dry_run=False):
    sql_dicts = []
    for record in batch:
        mapped_dict = map_log_to_sql(record)
        sql_dicts.append(mapped_dict)

    # Extract columns from the first row dict
    column_list = list(sql_dicts[0].keys())
    placeholder = ", ".join(["%s"] * len(column_list))
    columns_sql = ", ".join(column_list)

    sql_insert_query = f"INSERT INTO {WS_TABLE_NAME} ({columns_sql}) VALUES ({placeholder})"

    # Convert rows of dicts → list of tuples
    values = [
        tuple(row[col] for col in column_list)
        for row in sql_dicts
    ]
    if dry_run:
        logger.info(f'Dry run: would insert {len(values)} records into {WS_TABLE_NAME}. First record: {sql_dicts[0]}')
        return
    with get_metadata_connection_handle('production_processing',
                                        private_config_xml_file) as metadata_connection_handle:
        with metadata_connection_handle.cursor() as cursor:
            psycopg2.extras.execute_batch(cursor, sql_insert_query, values)
        metadata_connection_handle.commit()


def main():
    parser = ArgumentParser(description='Retrieves data from Kibana and dumps into a local postgres instance')
    parser.add_argument('--kibana-host', help='Kibana host to query, e.g. http://example.ebi.ac.uk:9200', required=True)
    parser.add_argument('--kibana-user', help='Kibana API username', required=True)
    parser.add_argument('--kibana-pass', help='Kibana API password', required=True)
    parser.add_argument('--batch-size', help='Number of records to load at a time', type=int, default=100)
    parser.add_argument('--private-config-xml-file', help='ex: /path/to/eva-maven-settings.xml', required=True)
    parser.add_argument('--dry-run', help='Retrieve and map the records without loading them into the database',
                        action='store_true', default=False)
    args = parser.parse_args()

    kibana_host = args.kibana_host
    basic_auth = HTTPBasicAuth(args.kibana_user, args.kibana_pass)
    private_config_xml_file = args.private_config_xml_file
    batch_size = args.batch_size
    dry_run = args.dry_run

    loaded_so_far = 0
    most_recent_timestamp = get_most_recent_timestamp(private_config_xml_file)
    scroll_id, total, batch = query(kibana_host, basic_auth, batch_size, most_recent_timestamp)
    if not batch:
        return
    logger.info(f'{total} results found.')
    while batch:
        load_batch_to_table(batch, private_config_xml_file, dry_run)
        loaded_so_far += len(batch)
        logger.info(f'Loaded {loaded_so_far} records...')
        batch = scroll(kibana_host, basic_auth, scroll_id)

    logger.info(f'Done. Loaded {loaded_so_far} total records.')


def get_field(source, field_name):
    """
    Retrieve a dotted field (e.g. 'url.path') from a document source whether it is stored flat or nested.
    """
    if field_name in source:
        return source[field_name]
    prefix, _, remainder = field_name.partition('.')
    if remainder and isinstance(source.get(prefix), dict):
        return get_field(source[prefix], remainder)
    return None


def get_latitude_longitude(geo_location):
    """
    Extract (latitude, longitude) from a geo_point, which can be a dict, a GeoJSON point, a [lon, lat] array or a
    "lat,lon" string.
    """
    if isinstance(geo_location, dict):
        if 'coordinates' in geo_location:
            longitude, latitude = geo_location['coordinates'][:2]
            return latitude, longitude
        return geo_location.get('lat'), geo_location.get('lon')
    if isinstance(geo_location, (list, tuple)) and len(geo_location) >= 2:
        longitude, latitude = geo_location[:2]
        return latitude, longitude
    if isinstance(geo_location, str) and ',' in geo_location:
        latitude, longitude = (float(value) for value in geo_location.split(',', 1))
        return latitude, longitude
    return None, None


def map_log_to_sql(record):
    """
    Convert a single JSON log document (dict) from the logs-web-realtime data stream into a dict
    matching the SQL table schema.
    """
    # Grab the source
    source = record.get('_source')

    # Internal EBI server that handled the request
    server_node = get_field(source, 'destination.address')
    pool_name = server_node.split(":")[0] if server_node else None

    # Time to first byte is in seconds: store in milliseconds
    time_to_first_byte = get_field(source, 'http.response.time_to_first_byte')
    duration = float(time_to_first_byte) * 1000 if time_to_first_byte is not None else None

    # Geographic location
    client_ip = get_field(source, 'client.ip')
    if get_field(source, 'geo.country_iso_code'):
        latitude, longitude = get_latitude_longitude(get_field(source, 'geo.location'))
        location_dict = {
            'country_code': get_field(source, 'geo.country_iso_code'),
            'country_name': get_field(source, 'geo.country_name'),
            'city': get_field(source, 'geo.city_name'),
            'state': get_field(source, 'geo.region_name'),
            'latitude': latitude,
            'longitude': longitude
        }
    else:
        location_dict = get_location(client_ip)

    # SQL row result
    return {
        "event_ts_txt": get_field(source, "@timestamp"),
        "event_ts": get_field(source, "@timestamp"),
        "http_req_type": get_field(source, "http.request.method"),
        "host": get_field(source, "url.domain"),
        "path": get_field(source, "url.path"),
        "syslog_pri": None,  # not present
        "syslog_timestamp": None,  # not present
        "syslog_hostname": None,  # not present
        "remote_host": client_ip,
        "request_ts": get_field(source, "@timestamp"),
        "client_ip": client_ip,
        "bytes_out": get_field(source, "http.response.bytes"),
        "bytes_in": None,  # not present
        "duration": duration,
        "pool_name": pool_name,
        "server_node": server_node,
        "user_agent": get_field(source, "user_agent.original"),
        "request_type": get_field(source, "http.request.method"),
        "http_status": get_field(source, "http.response.status_code"),
        "is_https": None,  # not present
        "virtual_host": get_field(source, "url.domain"),
        "request_uri_path": get_field(source, "url.path_base"),
        "request_query": get_field(source, "url.query"),
        "cookie_header": None,  # not present
        "seg_len": None,
        "historic_data": None,
        "client_country_code": location_dict.get("country_code"),
        "client_country_name": location_dict.get("country_name"),
        "client_city": location_dict.get("city"),
        "client_postal": location_dict.get("postal"),
        "client_latitude": location_dict.get("latitude"),
        "client_longitude": location_dict.get("longitude"),
        "client_state": location_dict.get("state"),
    }


if __name__ == '__main__':
    main()
