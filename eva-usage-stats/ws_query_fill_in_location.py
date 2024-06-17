#!/usr/bin/python
import datetime
from argparse import ArgumentParser
from functools import lru_cache

import requests

from ebi_eva_common_pyutils.metadata_utils import get_metadata_connection_handle
from ebi_eva_common_pyutils.pg_utils import get_all_results_for_query
from psycopg2.extras import execute_batch
from requests import HTTPError
from retry import retry


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


update_query = (
    'UPDATE eva_web_srvc_stats.ws_traffic SET client_country_code=$1, client_country_name=$2, '
    'client_city=$3, client_postal=$4, client_latitude=$5, client_longitude=$6, client_state=$7 '
    'WHERE client_ip=$8 AND client_country_code is null;'
)


def main():
    parser = ArgumentParser(description='')
    parser.add_argument("--private-config-xml-file", help="ex: /path/to/eva-maven-settings.xml", required=True)
    args = parser.parse_args()
    print("Job ran at " + str(datetime.datetime.now()))

    postgres_conn_handle = get_metadata_connection_handle("production_processing", args.private_config_xml_file)
    # Get the number of IP to update
    query = 'SELECT count(distinct client_ip) FROM eva_web_srvc_stats.ws_traffic where client_country_code is null;'
    res = get_all_results_for_query(postgres_conn_handle, query)
    nb_ip_address = res[0][0]
    ip_updated = 0
    chunk_size = 1000
    print(f'{nb_ip_address} IP addresses to update the location for {chunk_size} at a time')

    while ip_updated < nb_ip_address:
        with postgres_conn_handle.cursor(name='fetch_large_result') as cursor:
            cursor.itersize = chunk_size
            cursor.execute(f"SELECT distinct client_ip FROM eva_web_srvc_stats.ws_traffic "
                           f"where client_country_code is null limit {chunk_size};")

            location_dict_list = []
            for row in cursor:
                ip_address, = row
                location_dict = get_location(ip_address)
                location_dict['ip_address'] = ip_address
                location_dict_list.append(location_dict)

            update_many_and_commit(postgres_conn_handle, location_dict_list, page_size=100)
            ip_updated += len(location_dict_list)
            print(f'Updated {chunk_size} ip addresses out of {nb_ip_address}')


def update_many_and_commit(postgres_conn_handle, location_dict_list, page_size=100):
    cur = postgres_conn_handle.cursor()
    cur.execute(f"PREPARE updateStmt AS {update_query}")
    execute_batch(cur,
                  "EXECUTE updateStmt (%(country_code)s, %(country_name)s, %(city)s, %(postal)s, %(latitude)s, "
                  "%(longitude)s, %(state)s, %(ip_address)s)",
                  location_dict_list,
                  page_size=page_size)
    cur.execute("DEALLOCATE updateStmt")
    postgres_conn_handle.commit()


if __name__ == '__main__':
    main()
