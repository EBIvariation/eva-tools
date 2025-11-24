#!/usr/bin/python
import datetime
import json
import os
import traceback
from argparse import ArgumentParser
from pprint import pprint

import psycopg2
import requests
from ebi_eva_common_pyutils.logger import logging_config
from ebi_eva_common_pyutils.metadata_utils import get_metadata_connection_handle
from psycopg2.extras import execute_batch

from requests.auth import HTTPBasicAuth
from requests import HTTPError

from retry import retry

logger = logging_config.get_logger(__name__)
logging_config.add_stdout_handler()

@retry(tries=5, delay=8, backoff=1.2, jitter=(1, 3))
def _get_location(ip_address):
    response = requests.get('https://geolocation-db.com/json/' + ip_address)
    response.raise_for_status()
    return response.json()
    # {
    #    "country_code":"NL",
    #    "country_name":"Netherlands",
    #    "city":"Amsterdam",
    #    "postal":"1105",
    #    "latitude":52.2965,
    #    "longitude":4.9542,
    #    "IPv4":"82.196.6.158",
    #    "state":"North Holland"
    # }


@lru_cache(maxsize=None)
def get_location(ip_address):
    try:
        return _get_location(ip_address)
    except HTTPError:
        return {}

@retry(tries=4, delay=2, backoff=1.2, jitter=(1, 3))
def query(kibana_host, basic_auth,  batch_size, most_recent_timestamp=None):
    first_query_url = os.path.join(kibana_host, '_search?scroll=24h')
    query_conditions = [{'wildcard': {'url.path.keyword': {'value': '/eva/webservices/*'}}}]
    if most_recent_timestamp:
        query_conditions.append({'range': {'@timestamp': {'gt': most_recent_timestamp}}})
    post_query = {
        'size': str(batch_size),
        'query': {'bool': {'must': query_conditions}}
    }

    response = requests.post(first_query_url, auth=basic_auth, json=post_query)
    if response.status_code != requests.codes.ok:
        print(response.json())
    response.raise_for_status()
    data = response.json()
    total = data['hits']['total']
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

def load_batch_to_table(batch, private_config_xml_file, ftp_table_name):
    sql_dicts = []
    for record in batch:
        sql_dicts.append(map_log_to_sql(record))

    # Extract columns from the first row dict
    columns = list(sql_dicts[0].keys())
    placeholder = ", ".join(["%s"] * len(columns))
    columns_sql = ", ".join(columns)

    sql_insert_query = f"""INSERT INTO {ftp_table_name} ({columns_sql}) VALUES ({placeholder})"""

    # Convert rows of dicts → list of tuples
    values = [
        tuple(row[col] for col in columns)
        for row in sql_dicts
    ]
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
    # parser.add_argument('--private-config-xml-file', help='ex: /path/to/eva-maven-settings.xml', required=True)
    args = parser.parse_args()

    kibana_host = args.kibana_host
    basic_auth = HTTPBasicAuth(args.kibana_user, args.kibana_pass)
    # private_config_xml_file = args.private_config_xml_file
    batch_size = args.batch_size

    loaded_so_far = 0
    scroll_id, total, batch = query(kibana_host, basic_auth, batch_size)
    if not batch:
        return
    logger.info(f'{total} results found.')
    loaded_so_far += len(batch)
    for record in batch:
        pprint(record['_source'])
    while loaded_so_far < total:
        logger.info(f'Loaded {loaded_so_far} records...')
        batch = scroll(kibana_host, basic_auth, scroll_id)
        load_batch_to_table(batch, private_config_xml_file, ftp_table_name)
        loaded_so_far += len(batch)

    logger.info(f'Done. Loaded {loaded_so_far} total records.')



def map_log_to_sql(record):
    """
    Convert a single JSON log document (dict) into a dict
    matching the SQL table schema.
    """
    # Syslog message sample:
    # "Nov 7 06:31:45 pg-www-lb2.ebi.ac.uk zeus.zxtm[778303] 2025-11-07 06:31:45|0.979933|..."
    syslog_msg = record.get("message", "")
    parts = syslog_msg.split()

    try:
        syslog_timestamp = " ".join(parts[0:3])
        syslog_hostname = parts[3]
    except Exception:
        syslog_timestamp = None
        syslog_hostname = None

    # URL components
    uri = record.get("url.path")
    if uri and "?" in uri:
        request_uri_path, request_query = uri.split("?", 1)
    else:
        request_uri_path = uri
        request_query = None

    # Is HTTPS?
    is_https = 1 if record.get("http.version") == "HTTPS" else 0

    # Server pool / backend target
    server_node = record.get("server.address")
    pool_name = server_node.split(":")[0] if server_node else None

    location_dict = get_location(record.get("client.address"))

    # SQL row result
    result = {
        "event_ts_txt": record.get("@timestamp"),
        "event_ts": record.get("@timestamp"),
        "http_req_type": record.get("http", {}).get("request", {}).get("method"),
        "host": record.get("destination.address"),
        "path": record.get("url.path"),
        "syslog_pri": None,  # not present
        "syslog_timestamp": syslog_timestamp,
        "syslog_hostname": syslog_hostname,
        "remote_host": record.get("client.address"),
        "request_ts": record.get("event.created"),
        "client_ip": record.get("client.address"),
        "bytes_out": record.get("http", {}).get("response", {}).get("body.bytes"),
        "bytes_in": None,  # not in document,
        "duration": record.get("server.wait_time"),
        "pool_name": pool_name,
        "server_node": server_node,
        "user_agent": record.get("user_agent.original"),
        "request_type": record.get("http", {}).get("request", {}).get("method"),
        "http_status":  record.get("http", {}).get("response", {}).get("status_code"),
        "is_https": is_https,
        "virtual_host": record.get("destination.address"),
        "request_uri_path": request_uri_path,
        "request_query": request_query,
        "cookie_header": record.get("http", {}).get("request", {}).get("cookie"),
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
    return result

if __name__ == '__main__':
    main()
