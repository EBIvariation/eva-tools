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

import psycopg2
import click
from ebi_eva_common_pyutils.pg_utils import get_all_results_for_query, execute_query
from ebi_eva_common_pyutils.mongo_utils import get_mongo_connection_handle
from ebi_eva_common_pyutils.config_utils import get_pg_metadata_uri_for_eva_profile, get_properties_from_xml_file
from ebi_eva_common_pyutils.logger import logging_config


logger = logging_config.get_logger(__name__)


def get_database_name_from_assembly(metadata_handle, assembly):
    query = "select t.taxonomy_code, a.assembly_code " \
            "from assembly a " \
            "left join taxonomy t on (t.taxonomy_id = a.taxonomy_id)" \
            "where assembly_accession = '{0}'".format(assembly)
    result = get_all_results_for_query(metadata_handle, query)
    database_name = 'eva_{0}_{1}'.format(result[0][0], result[0][1])
    return database_name


def get_mongo_primary_host_and_port(mongo_hosts_and_ports):
    """
    :param mongo_hosts_and_ports: All host and ports stored in the private settings xml
    :return: mongo primary host and port
    """
    for host_and_port in mongo_hosts_and_ports.split(','):
        if '001' in host_and_port:
            properties = host_and_port.split(':')
            return properties[0], properties[1]


def get_from_variant_warehouse(mongo_handle, metadata_handle, projects):
    projects_evapro = "('" + projects.replace(",", "','") + "')"
    query = "select a.vcf_reference_accession, p.project_accession, a.analysis_accession, af.file_id, " \
            "f.filename, f.file_type, asm.taxonomy_id " \
            "from analysis a " \
            "left join analysis_file af on (af.analysis_accession = a.analysis_accession)" \
            "left join file f on (f.file_id = af.file_id)" \
            "left join project_analysis p on (p.analysis_accession = a.analysis_accession)" \
            "left join assembly asm on (asm.assembly_accession = a.vcf_reference_accession)" \
            "where f.file_type = 'VCF' " \
            "and p.project_accession in {0}" \
            "order by a.vcf_reference_accession, p.project_accession, a.analysis_accession".format(projects_evapro)
    logger.info("Querying EVAPRO")
    result_evapro = get_all_results_for_query(metadata_handle, query)

    analyses = []
    analysis_filename = {}
    for index, row in enumerate(result_evapro):
        insert_into_stats(metadata_handle, row)
        analysis = row[2]
        filename = row[4]
        analyses.append(analysis)
        analysis_filename[analysis] = filename
        # All except last row
        if index < len(result_evapro) - 1:
            next_row = result_evapro[index + 1]
            if row[1] != next_row[1]:
                # if next row is a new project
                assembly = row[0]
                project = row[1]
                get_counts_from_variant_warehouse(assembly, project, analyses, metadata_handle, mongo_handle)
                get_dates_from_variant_warehouse(assembly, project, analysis_filename, metadata_handle, mongo_handle)
                analyses = []
                analysis_filename = {}
        else:
            assembly = row[0]
            project = row[1]
            get_counts_from_variant_warehouse(assembly, project, analyses, metadata_handle, mongo_handle)
            get_dates_from_variant_warehouse(assembly, project, analysis_filename, metadata_handle, mongo_handle)


def insert_into_stats(metadata_handle, row):
    assembly, project, analysis, file_id, filename, file_type, taxonomy_id = row[0], row[1], row[2], row[3], row[4], \
                                                                             row[5], row[6]
    check_exist_query = "select * from eva_stats.stats where assembly_accession = '{0}' and project_accession = '{1}'" \
                        " and analysis_accession = '{2}'".format(assembly, project, analysis)
    insert_query = "insert into eva_stats.stats(assembly_accession, project_accession, analysis_accession, file_id, " \
                   "filename, file_type, taxonomy_id) values ('{0}','{1}','{2}','{3}','{4}','{5}', {6})".format(
        assembly, project, analysis, file_id, filename, file_type, taxonomy_id)
    if len(get_all_results_for_query(metadata_handle, check_exist_query)) == 0:
        logger.info("Insert data for {0}, {1}, {2} in eva_stats table".format(assembly, project, analysis))
        execute_query(metadata_handle, insert_query)
    else:
        logger.info("Already exists {0}, {1}, {2} in eva_stats table".format(assembly, project, analysis))


def get_counts_from_variant_warehouse(assembly, project, analyses, metadata_handle, mongo_handle):
    database_name = get_database_name_from_assembly(metadata_handle, assembly)
    logger.info("Database name to query: {0}".format(database_name))
    logger.info("Getting counts from variants collection for {0}, {1}, {2}".format(assembly, project, analyses))
    variants_collection = mongo_handle[database_name]['variants_2_0']
    pipeline = [
        {"$match": {"files.sid": project, "files.fid": {"$in": analyses}}},
        {"$project": {"_id": 0, "files.fid": 1}},
        {"$unwind": "$files"},
        {"$unwind": "$files.fid"},
        {"$match": {"files.fid": {"$in": analyses}}},
        {"$group": {"_id": "$files.fid", "count": {"$sum": 1}}},
        {"$project": {"_id": 0, "files.fid": "$_id", "count": 1}}
    ]
    cursor_variants = variants_collection.aggregate(pipeline)
    for stat in cursor_variants:
        count = stat['count']
        analysis = stat['files']['fid']
        logger.info("Update counts for {0}, {1}, {2}".format(assembly, project, analysis))
        update_counts_query = "update eva_stats.stats " \
                              "set variants_variant_warehouse = {3}" \
                              "where assembly_accession = '{0}'" \
                              "and project_accession = '{1}'" \
                              "and analysis_accession = '{2}'".format(assembly, project, analysis, count)
        execute_query(metadata_handle, update_counts_query)


def get_dates_from_variant_warehouse(assembly, project, analysis_filename, metadata_handle, mongo_handle):
    database_name = get_database_name_from_assembly(metadata_handle, assembly)
    logger.info("Database name to query: {0}".format(database_name))
    logger.info("Getting counts from files collection for {0}, {1}, {2}".format(assembly, project, analysis_filename))
    files_collection = mongo_handle[database_name]['files_2_0']

    where_clause = []
    for analysis, filename in analysis_filename.items():
        where = {'sid': project, 'fid': analysis, 'fname': filename}
        where_clause.append(where)
    cursor_files = files_collection.find({"$or": where_clause},
                                         {"fid": 1, "sid": 1, "date": 1, "_id": 0})

    for dates in cursor_files:
        analysis = dates['fid']
        date = dates['date']
        logger.info("Update counts for {0}, {1}, {2}".format(assembly, project, analysis))
        update_date_query = "update eva_stats.stats " \
                            "set date_processed = '{3}'" \
                            "where assembly_accession = '{0}'" \
                            "and project_accession = '{1}'" \
                            "and analysis_accession = '{2}'".format(assembly, project, analysis, date)
        execute_query(metadata_handle, update_date_query)


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


@click.option("--private-config-xml-file", help="ex: /path/to/eva-maven-settings.xml", required=True)
@click.option("--projects", help="PRJEB27233,PRJEB36318", required=False)
@click.command()
def get_stats(private_config_xml_file, projects):
    logger.info("Started stats counts from variant warehouse")
    mongo_handle, metadata_handle = get_handles(private_config_xml_file)
    logger.info("Got connection handles to mongo and postgres")
    get_from_variant_warehouse(mongo_handle, metadata_handle, projects)
    logger.info("Counts finished")


if __name__ == "__main__":
    get_stats()
