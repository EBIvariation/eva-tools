# Copyright 2021 EMBL - European Bioinformatics Institute
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import sys
import psycopg2
import argparse
from ebi_eva_common_pyutils.pg_utils import get_all_results_for_query, execute_query
from ebi_eva_common_pyutils.mongo_utils import get_mongo_connection_handle
from ebi_eva_common_pyutils.config_utils import get_pg_metadata_uri_for_eva_profile, get_properties_from_xml_file
from ebi_eva_common_pyutils.logger import logging_config


logger = logging_config.get_logger(__name__)


def get_handles(private_config_xml_file):
    properties = get_properties_from_xml_file("production", private_config_xml_file)
    mongo_hosts_and_ports = str(properties['eva.mongo.host'])
    mongo_host, mongo_port = get_mongo_primary_host_and_port(mongo_hosts_and_ports)
    mongo_username = str(properties['eva.mongo.user'])
    mongo_password = str(properties['eva.mongo.passwd'])

    mongo_handle = get_mongo_connection_handle(mongo_username, mongo_password, mongo_host)
    metadata_handle = psycopg2.connect(get_pg_metadata_uri_for_eva_profile(
        "development", private_config_xml_file), user="evadev")

    return mongo_handle, metadata_handle


def get_mongo_primary_host_and_port(mongo_hosts_and_ports):
    """
    :param mongo_hosts_and_ports: All host and ports stored in the private settings xml
    :return: mongo primary host and port
    """
    for host_and_port in mongo_hosts_and_ports.split(','):
        if '001' in host_and_port:
            properties = host_and_port.split(':')
            return properties[0], properties[1]


def get_stats_from_accessioning_db(mongo_handle, metadata_handle, provided_assemblies):
    logger.info("Getting counts for RS IDs (Clustered Variants)")
    store_accessioning_counts(mongo_handle, metadata_handle, 'clusteredVariantEntity', provided_assemblies)
    logger.info("Getting counts for SS IDs (Submitted Variants)")
    store_accessioning_counts(mongo_handle, metadata_handle, 'submittedVariantEntity', provided_assemblies)


def store_accessioning_counts(mongo_handle, metadata_handle, collection_name, provided_assemblies):
    collection = mongo_handle["eva_accession_sharded"][collection_name]
    assembly_field = "asm" if collection_name == "clusteredVariantEntity" else "seq"
    stats_table = "rs_stats" if collection_name == "clusteredVariantEntity" else "ss_stats"
    assemblies = provided_assemblies if provided_assemblies else collection.distinct(assembly_field)
    for assembly in assemblies:
        pipeline = [
            {"$match": {assembly_field: assembly}},
            {"$group": {"_id": assembly, "count": {"$sum": 1}}}
        ]
        cursor_stat = collection.aggregate(pipeline)
        for stat in cursor_stat:
            logger.info(stat)
            assembly = stat["_id"]
            count = stat["count"]
            query = "insert into eva_stats.{2} values ('{0}', {1}) " \
                    "on conflict(assembly) do update set num_variants = {1}".format(assembly, count, stats_table)
            execute_query(metadata_handle, query)


def get_stats(private_config_xml_file, assemblies):
    logger.info("Started stats counts from accessioning warehouse")
    mongo_handle, metadata_handle = get_handles(private_config_xml_file)
    logger.info("Got connection handles to mongo and postgres")
    get_stats_from_accessioning_db(mongo_handle, metadata_handle, assemblies)
    logger.info("Counts finished")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Get stats from accessioning database', add_help=False)
    parser.add_argument("--private-config-xml-file", help="ex: /path/to/eva-maven-settings.xml", required=True)
    parser.add_argument("--assembly-list", help="Assembly list e.g. GCA_000002285.2 GCA_000233375.4 ",
                        required=False, nargs='+')
    args = {}
    try:
        args = parser.parse_args()
        get_stats(args.private_config_xml_file, args.assembly_list)
    except Exception as ex:
        logger.exception(ex)
        sys.exit(1)
    sys.exit(0)
