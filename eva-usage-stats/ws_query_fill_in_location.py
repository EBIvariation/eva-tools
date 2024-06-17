#!/usr/bin/python
import datetime
from argparse import ArgumentParser
from functools import lru_cache

import requests
from ebi_eva_common_pyutils.logger import logging_config

from ebi_eva_common_pyutils.metadata_utils import get_metadata_connection_handle
from ebi_eva_common_pyutils.pg_utils import get_all_results_for_query
from requests import HTTPError
from retry import retry

logger = logging_config.get_logger(__name__)

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


def main():
    parser = ArgumentParser(description='')
    parser.add_argument("--private-config-xml-file", help="ex: /path/to/eva-maven-settings.xml", required=True)
    args = parser.parse_args()
    logging_config.add_stdout_handler()
    logger.info("Job ran at " + str(datetime.datetime.now()))

    postgres_conn_handle = get_metadata_connection_handle("production_processing", args.private_config_xml_file)
    chunk_size = 100
    update_query = (
        'UPDATE eva_web_srvc_stats.ws_traffic SET client_country_code=%s, client_country_name=%s, '
        'client_city=%s, client_postal=%s, client_latitude=%s, client_longitude=%s, client_state=%s '
        'WHERE client_ip=%s AND client_country_code is null;'
    )
    nb_ip = 0
    nb_row_updated = 0
    while True:
        query = (f"SELECT distinct client_ip FROM eva_web_srvc_stats.ws_traffic "
                 f"where client_country_code is null limit {chunk_size};")
        ip_addresses = list(get_all_results_for_query(postgres_conn_handle, query))

        if len(ip_addresses) == 0:
            break
        for ip_address, in ip_addresses:
            location_dict = get_location(ip_address)
            with postgres_conn_handle.cursor() as update_cursor:
                # execute the UPDATE statement
                update_cursor.execute(
                    update_query,
                    (location_dict.get('country_code'), location_dict.get('country_name'), location_dict.get('city'),
                     location_dict.get('postal'), location_dict.get('latitude'), location_dict.get('longitude'),
                     location_dict.get('state'), ip_address)
                )
                updated_row_count = update_cursor.rowcount
                logger.info(f'Updated {updated_row_count} record for {ip_address}')
                nb_ip += 1
                nb_row_updated += updated_row_count
        # commit the changes to the database
        logger.info(f'Committed {nb_row_updated} record for {nb_ip} IP addresses')
        postgres_conn_handle.commit()
    logger.info(f'Updated {nb_row_updated} record for {nb_ip} IP addresses')


if __name__ == '__main__':
    main()
