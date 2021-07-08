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
import argparse
from itertools import groupby
from operator import itemgetter
from ebi_eva_common_pyutils.pg_utils import get_all_results_for_query, execute_query
from ebi_eva_common_pyutils.metadata_utils import get_variant_warehouse_db_name_from_assembly_and_taxonomy, \
    get_metadata_connection_handle
from ebi_eva_common_pyutils.mongo_utils import get_primary_mongo_connection_handle
from ebi_eva_common_pyutils.logger import logging_config


logger = logging_config.get_logger(__name__)


def get_from_variant_warehouse(mongo_handle, metadata_handle, projects):
    projects_for_query = get_projects(metadata_handle, projects)
    query = "select a.vcf_reference_accession, p.project_accession, a.analysis_accession, af.file_id, " \
            "f.filename, f.file_type, asm.taxonomy_id " \
            "from analysis a " \
            "left join analysis_file af on (af.analysis_accession = a.analysis_accession)" \
            "left join file f on (f.file_id = af.file_id)" \
            "left join project_analysis p on (p.analysis_accession = a.analysis_accession)" \
            "left join assembly asm on (asm.assembly_accession = a.vcf_reference_accession)" \
            "where f.file_type = 'VCF' " \
            "and p.project_accession in {0}" \
            "order by a.vcf_reference_accession, p.project_accession, a.analysis_accession".format(projects_for_query)
    logger.info("Querying EVAPRO")
    result_evapro = get_all_results_for_query(metadata_handle, query)

    for project, rows_by_project in groupby(result_evapro, key=itemgetter(1)):
        analyses = []
        analysis_filename = {}
        for row in rows_by_project:
            insert_into_stats(metadata_handle, row)
            assembly, project, analysis, filename, taxonomy = row[0], row[1], row[2], row[4], row[6]
            analyses.append(analysis)
            analysis_filename[analysis] = filename
        try:
            database_name = get_variant_warehouse_db_name_from_assembly_and_taxonomy(metadata_handle, assembly, taxonomy)
            if database_name:
                logger.info(database_name)
                get_counts_from_variant_warehouse(assembly, project, analyses, metadata_handle, mongo_handle, database_name)
                get_dates_from_variant_warehouse(assembly, project, analysis_filename, metadata_handle, mongo_handle,
                                                 database_name)
            else:
                logger.info(f'No database for assembly {assembly} and taxonomy {taxonomy} found')
        except ValueError as err:
            logger.error(err)


def get_projects(metadata_handle, projects):
    """
    If the projects were not provided it will compare the evapro.projects table and eva_stats.stats tables to get the
    difference. It also replaces brackets with parenthesis to be valid in the sql query
    """
    if projects:
        projects_for_query = projects
    else:
        query = "select p.project_accession from evapro.project p " \
                "where p.project_accession not in (select s.project_accession from eva_stats.stats s)"
        result = get_all_results_for_query(metadata_handle, query)
        projects_for_query = [row[0] for row in result]
    projects_for_query = "('{0}')".format("','".join(projects_for_query))
    return projects_for_query


def insert_into_stats(metadata_handle, row):
    assembly, project, analysis, file_id, filename, file_type, taxonomy_id = row
    check_exist_query = "select * from eva_stats.stats where assembly_accession = '{0}' and project_accession = " \
                        "'{1}' and analysis_accession = '{2}'".format(assembly, project, analysis)
    if len(get_all_results_for_query(metadata_handle, check_exist_query)) == 0:
        insert_query = "insert into eva_stats.stats(assembly_accession, project_accession, analysis_accession, " \
                       "file_id, filename, file_type, taxonomy_id) values ('{0}','{1}','{2}','{3}','{4}','{5}', " \
                       "{6})".format(assembly, project, analysis, file_id, filename, file_type, taxonomy_id)
        logger.info("Insert data for {0}, {1}, {2} in eva_stats table".format(assembly, project, analysis))
        execute_query(metadata_handle, insert_query)
    else:
        logger.info("Already exists {0}, {1}, {2} in eva_stats table".format(assembly, project, analysis))


def get_counts_from_variant_warehouse(assembly, project, analyses, metadata_handle, mongo_handle, database_name):
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
    cursor_variants = variants_collection.aggregate(pipeline=pipeline, allowDiskUse=True)
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


def get_dates_from_variant_warehouse(assembly, project, analysis_filename, metadata_handle, mongo_handle,
                                     database_name):
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
    mongo_handle = get_primary_mongo_connection_handle("production", private_config_xml_file)
    metadata_handle = get_metadata_connection_handle("development", private_config_xml_file)
    return mongo_handle, metadata_handle


def get_stats(private_config_xml_file, projects):
    logger.info("Started stats counts from variant warehouse")
    mongo_handle, metadata_handle = get_handles(private_config_xml_file)
    logger.info("Got connection handles to mongo and postgres")
    get_from_variant_warehouse(mongo_handle, metadata_handle, projects)
    logger.info("Counts finished")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Get stats from variant warehouse', add_help=False)
    parser.add_argument("--private-config-xml-file", help="ex: /path/to/eva-maven-settings.xml", required=True)
    parser.add_argument("--project-list", help="Project list e.g. PRJEB27233 PRJEB36318", required=False, nargs='+')
    args = {}
    try:
        args = parser.parse_args()
        get_stats(args.private_config_xml_file, args.project_list)
    except Exception as ex:
        logger.exception(ex)
        sys.exit(1)
    sys.exit(0)
