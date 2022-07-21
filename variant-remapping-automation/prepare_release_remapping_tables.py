import argparse
import traceback

import pymongo
from ebi_eva_common_pyutils.logger import logging_config
from ebi_eva_common_pyutils.metadata_utils import get_metadata_connection_handle
from ebi_eva_common_pyutils.mongodb import MongoDatabase
from ebi_eva_common_pyutils.pg_utils import get_all_results_for_query, execute_query
from ebi_eva_common_pyutils.taxonomy.taxonomy import get_scientific_name_from_ensembl
from pymongo.read_concern import ReadConcern

from genome_target_tracker import check_supported_target_assembly

logging_config.add_stdout_handler()
logger = logging_config.get_logger(__name__)


def prepare_remapping_table(private_config_xml_file, remapping_version, release_version,
                            scientific_names, eva_assemblies, dbsnp_assemblies, last_release_data):
    res = check_supported_target_assembly(private_config_xml_file)
    taxonomy_with_mismatch_assembly = res[0]

    with get_metadata_connection_handle('production_processing', private_config_xml_file) as pg_conn:
        for tax_id, diff_asm in taxonomy_with_mismatch_assembly.items():
            scientific_name = get_scientific_name(tax_id, scientific_names)
            sources = get_assembly_sources(diff_asm['eva'], eva_assemblies, dbsnp_assemblies, tax_id, last_release_data)
            # TODO: num_studies, num_ss_ids are non-null columns (either make them nullable  or get values from db and fill)
            query = f"insert into eva_progress_tracker.remapping_tracker (source, taxonomy, scientific_name, " \
                    f"origin_assembly_accession, assembly_accession, remapping_version, release_version, " \
                    f"num_studies, num_ss_ids ) " \
                    f"values('source_placeholder',{tax_id},'{scientific_name}','{diff_asm['eva']}','{diff_asm['source']}'," \
                    f"'{remapping_version}', {release_version}, " \
                    f"1, 1) "
            if 'EVA' in sources:
                query_eva = query.replace('source_placeholder', 'EVA')
                logger.info(f"{query_eva}")
                execute_query(pg_conn, query_eva)
            if 'DBSNP' in sources:
                query_dbsnp = query.replace('source_placeholder', 'DBSNP')
                logger.info(f"{query_dbsnp}")
                execute_query(pg_conn, query_dbsnp)


def prepare_release_table(private_config_xml_file, release_version, last_release_data, scientific_names,
                          eva_assemblies, dbsnp_assemblies):
    # insert assemblies from previous release
    with get_metadata_connection_handle('production_processing', private_config_xml_file) as pg_conn:
        logger.info(f"Inserting previous release data")
        for taxonomy in last_release_data:
            for assembly in last_release_data[taxonomy].keys():
                scientific_name = get_scientific_name(taxonomy, scientific_names)
                sources = get_assembly_sources(assembly, eva_assemblies, dbsnp_assemblies, taxonomy, last_release_data)
                release_folder_name = scientific_name.lower().replace(' ', '_')
                query = release_insert_query(sources, taxonomy, scientific_name, assembly, release_version,
                                             release_folder_name)
                logger.info(f"{query}")
                execute_query(pg_conn, query)

        logger.info(f"Inserting entries for new assemblies in eva")
        for assembly in eva_assemblies:
            taxonomy = eva_assemblies[assembly]
            if assembly in last_release_data[taxonomy].keys():
                continue
            scientific_name = get_scientific_name(taxonomy, scientific_names)
            sources = get_assembly_sources(assembly, eva_assemblies, dbsnp_assemblies, taxonomy, last_release_data)
            release_folder_name = scientific_name.lower().replace(' ', '_')
            query = release_insert_query(sources, taxonomy, scientific_name, assembly, release_version,
                                         release_folder_name)
            logger.info(f"{query}")
            execute_query(pg_conn, query)

        logger.info(f"Inserting entries for new assemblies in dbsnp")
        for assembly in dbsnp_assemblies:
            taxonomy = dbsnp_assemblies[assembly]
            if assembly in last_release_data[taxonomy].keys():
                continue
            if assembly in eva_assemblies and eva_assemblies[taxonomy] == dbsnp_assemblies[taxonomy]:
                continue
            scientific_name = get_scientific_name(taxonomy, scientific_names)
            sources = get_assembly_sources(assembly, eva_assemblies, dbsnp_assemblies, taxonomy, last_release_data)
            release_folder_name = scientific_name.lower().replace(' ', '_')
            query = release_insert_query(sources, taxonomy, scientific_name, assembly, release_version,
                                         release_folder_name)
            logger.info(f"{query}")
            execute_query(pg_conn, query)


def release_insert_query(sources, taxonomy, scientific_name, assembly, release_version, release_folder_name):
    return f"insert into eva_progress_tracker.clustering_release_tracker(sources, taxonomy, scientific_name, " \
           f"assembly_accession, release_version, release_folder_name, should_be_released  ) " \
           f"values('{sources}',{taxonomy},'{scientific_name}','{assembly}',{release_version}," \
           f"'{release_folder_name}', True)"


def get_eva_asm_tax(private_config_xml_file):
    eva_asm_tax = {}
    with get_metadata_connection_handle("production_processing", private_config_xml_file) as pg_conn:
        query = f"select distinct assembly_id, taxonomy_id FROM evapro.supported_assembly_tracker " \
                f"order by taxonomy_id , assembly_id"
        for assembly, tax_id in get_all_results_for_query(pg_conn, query):
            eva_asm_tax[assembly] = tax_id
    return eva_asm_tax


def get_dbsnp_asm_tax(mongo_source):
    collection = mongo_source.mongo_handle[mongo_source.db_name]['dbsnpSubmittedVariantEntity']
    cursor = collection.with_options(read_concern=ReadConcern("majority"),
                                     read_preference=pymongo.ReadPreference.PRIMARY) \
        .distinct('seq', no_cursor_timeout=True)
    records = []
    try:
        for result in cursor:
            records.append(result)
    except Exception as e:
        logger.exception(traceback.format_exc())
        raise e

    dbsnp_asm_tax = {}

    for assembly in records:
        taxonomy = get_taxonomy_for_dbsnp_assembly(assembly, mongo_source)
        if taxonomy is not None:
            dbsnp_asm_tax[assembly] = taxonomy
        else:
            logger.error(f"Skipping dbsnp assembly {assembly} as we couldnot find his taxonomy")

    return dbsnp_asm_tax


def get_taxonomy_for_dbsnp_assembly(assembly, mongo_source):
    collection = mongo_source.mongo_handle[mongo_source.db_name]['dbsnpSubmittedVariantEntity']
    document = collection.with_options(read_concern=ReadConcern("majority"),
                                       read_preference=pymongo.ReadPreference.PRIMARY) \
        .find_one({'seq': assembly}, no_cursor_timeout=True)
    if document is None:
        return None
    else:
        return document['tax']


def get_scientific_name(taxonomy, scientific_names):
    if taxonomy in scientific_names:
        return scientific_names[taxonomy]
    else:
        return get_scientific_name_from_ensembl(taxonomy)


def get_assembly_sources(assembly, eva_assemblies, dbsnp_assemblies, taxonomy, last_release_data):
    if assembly in dbsnp_assemblies and assembly in eva_assemblies:
        return 'DBSNP, EVA'
    elif assembly in dbsnp_assemblies:
        return 'DBSNP'
    elif assembly in eva_assemblies:
        return 'EVA'
    elif assembly in last_release_data[taxonomy]:
        return last_release_data[taxonomy][assembly]['sources']


def get_data_from_last_release(private_config_xml_file, last_release_version):
    last_release_records = {}
    scientific_names = {}
    with get_metadata_connection_handle('production_processing', private_config_xml_file) as pg_conn:
        query = f"select taxonomy, scientific_name, assembly_accession, sources " \
                f"from eva_progress_tracker.clustering_release_tracker " \
                f"where release_version={last_release_version} " \
                f"order by taxonomy, assembly_accession"

        for record in get_all_results_for_query(pg_conn, query):
            taxonomy = record[0]
            scientific_name = record[1]
            assembly_accession = record[2]
            sources = record[3]

            scientific_names[taxonomy] = scientific_name

            if assembly_accession == 'Unmapped':
                continue

            curr_record = {"taxonomy": taxonomy,
                           "scientific_name": scientific_name,
                           "assembly_accession": assembly_accession,
                           "sources": sources}
            if taxonomy in last_release_records:
                last_release_records[taxonomy][assembly_accession] = curr_record
            else:
                last_release_records[taxonomy] = {}
                last_release_records[taxonomy][assembly_accession] = curr_record

    return last_release_records, scientific_names


def release_version_exists(private_config_xml_file, release_version):
    with get_metadata_connection_handle('production_processing', private_config_xml_file) as pg_conn:
        query = f"select * from eva_progress_tracker.clustering_release_tracker where release_version={release_version}"
        return get_all_results_for_query(pg_conn, query)


def remapping_version_exists(private_config_xml_file, remapping_version):
    with get_metadata_connection_handle('production_processing', private_config_xml_file) as pg_conn:
        query = f"select * from eva_progress_tracker.remapping_tracker where remapping_version='{remapping_version}'"
        return get_all_results_for_query(pg_conn, query)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Prepare remapping and release tables for a new release version',
                                     add_help=False)
    parser.add_argument("--private-config-xml-file", help="ex: /path/to/eva-maven-settings.xml", required=True)
    parser.add_argument("--release-version", help="New Release Version (e.g. 4)", type=int, required=True)
    parser.add_argument("--remapping-version", help="New Remapping Version (e.g. 2)", type=int, required=True)
    parser.add_argument("--mongo-source-uri",
                        help="Mongo Source URI (ex: mongodb://user:@mongos-source-host:27017/admin)",
                        required=True)
    parser.add_argument("--mongo-source-secrets-file",
                        help="Full path to the Mongo Source secrets file (ex: /path/to/mongo/source/secret)",
                        required=True)

    args = parser.parse_args()

    mongo_source = MongoDatabase(uri=args.mongo_source_uri, secrets_file=args.mongo_source_secrets_file,
                                 db_name="eva_accession_sharded")

    if release_version_exists(args.private_config_xml_file, args.release_version):
        logger.error(f"Release Version {args.release_version} already exists in DB")
        exit(1)
    if remapping_version_exists(args.private_config_xml_file, args.remapping_version):
        logger.error(f"Remapping Version {args.remapping_version} already exists in DB")
        exit(1)

    eva_assemblies = get_eva_asm_tax(args.private_config_xml_file)
    dbsnp_assemblies = get_dbsnp_asm_tax(mongo_source)

    previous_release_version = args.release_version - 1
    last_release_data, scientific_names = get_data_from_last_release(args.private_config_xml_file,
                                                                     previous_release_version)

    prepare_release_table(args.private_config_xml_file, args.release_version, last_release_data, scientific_names,
                          eva_assemblies, dbsnp_assemblies)

    prepare_remapping_table(args.private_config_xml_file, args.remapping_version, args.release_version,
                            scientific_names, eva_assemblies, dbsnp_assemblies, last_release_data)
