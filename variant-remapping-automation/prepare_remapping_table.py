import argparse
import glob
import gzip
from urllib.parse import urlsplit

import psycopg2
from ebi_eva_common_pyutils.config_utils import get_accession_pg_creds_for_profile
from ebi_eva_common_pyutils.logger import logging_config
from ebi_eva_common_pyutils.metadata_utils import get_metadata_connection_handle
from ebi_eva_common_pyutils.pg_utils import get_all_results_for_query, execute_query
from ebi_eva_common_pyutils.taxonomy.taxonomy import get_scientific_name_from_ensembl

from genome_target_tracker import get_tax_latest_asm_from_eva

logging_config.add_stdout_handler()
logger = logging_config.get_logger(__name__)


def prepare_remapping_table(private_config_xml_file, remapping_version, release_version, noah_prj_dir, codon_prj_dir):
    eva_assemblies = get_eva_asm_tax(args.private_config_xml_file)
    dbsnp_assemblies = get_dbsnp_asm_tax()
    scientific_names_from_release_table = get_scientific_name_from_release_table(args.private_config_xml_file)
    # get a dictionary of taxonomy and the latest assembly it supports
    tax_latest_support_asm = get_tax_latest_asm_from_eva(private_config_xml_file)
    # get a dictionary of taxonomy and all the assemblies associated with it
    tax_all_supported_asm = get_tax_all_supported_assembly_from_eva(private_config_xml_file)
    # get dict of assembly and all the studies that were accessioned for this assembly after it was last remapped
    asm_studies_acc_after_remapping = get_studies_for_remapping(private_config_xml_file)
    # get dict of assembly and the total number of ss ids in all the studies that needs to be remapped
    asm_num_of_ss_ids = get_asm_no_ss_ids(asm_studies_acc_after_remapping, noah_prj_dir, codon_prj_dir)

    # insert entries for the case where a study was accessioned into an asembly which is not current and was previously remapped
    insert_entries_for_new_studies(private_config_xml_file, remapping_version, release_version, eva_assemblies,
                                   dbsnp_assemblies, scientific_names_from_release_table, tax_latest_support_asm,
                                   tax_all_supported_asm, asm_studies_acc_after_remapping, asm_num_of_ss_ids)


def insert_entries_for_new_studies(private_config_xml_file, remapping_version, release_version, eva_assemblies,
                                   dbsnp_assemblies, scientific_names_from_release_table, tax_latest_support_asm,
                                   tax_all_supported_asm, asm_studies_acc_after_remapping, asm_num_of_ss_ids):
    """
    This method handles the case where there are submissions to assemblies other than the current one
    let's say in table we have:
                                tax-asm3(latest)
                                tax-asm2
                                tax-asm1
    and submissions where made to asm1 (study1) and asm2 (study2), we need to remap these to the latest assembly asm3
    Entries for Remapping will look like
                                asm2 (study2) -> asm3
                                asm1 (study1) -> asm3
    """
    for tax, assemblies in tax_all_supported_asm.items():
        scientific_name = get_scientific_name(tax, scientific_names_from_release_table)
        for asm in assemblies:
            # No need for any remapping if the assembly is the latest assembly supported by taxonomy
            # or if there are no studies accessioned for that assembly
            if asm == tax_latest_support_asm[tax]['assembly'] or asm not in asm_studies_acc_after_remapping:
                continue
            # else remap the studies in the asssembly to the latest assembly supported by that taxonomy
            sources = get_assembly_sources(asm, eva_assemblies, dbsnp_assemblies)
            insert_entry_into_db(private_config_xml_file, sources, tax, scientific_name, asm,
                                 tax_latest_support_asm[tax]['assembly'], remapping_version,
                                 release_version, len(asm_studies_acc_after_remapping[asm]), asm_num_of_ss_ids[asm],
                                 "'{" + ",".join(asm_studies_acc_after_remapping[asm]) + "}'")


def insert_entry_into_db(private_config_xml_file, sources, tax, scientific_name, org_asm, target_asm, remapping_version,
                         release_version, num_studies, num_ss_ids, study_accessions):
    """
    Makes entry into the remapping tracking table
    """
    with get_metadata_connection_handle('production_processing', private_config_xml_file) as pg_conn:
        query = f"insert into eva_progress_tracker.remapping_tracker (source, taxonomy, scientific_name, " \
                f"origin_assembly_accession, assembly_accession, remapping_version, release_version, " \
                f"num_studies, num_ss_ids, study_accessions) " \
                f"values ('source_placeholder',{tax},'{scientific_name}','{org_asm}','{target_asm}'," \
                f"'{remapping_version}', {release_version}, " \
                f"{num_studies}, {num_ss_ids}, {study_accessions}) "
        if 'EVA' in sources:
            query_eva = query.replace('source_placeholder', 'EVA')
            logger.info(f"{query_eva}")
            execute_query(pg_conn, query_eva)
        if 'DBSNP' in sources:
            query_dbsnp = query.replace('source_placeholder', 'DBSNP')
            logger.info(f"{query_dbsnp}")
            execute_query(pg_conn, query_dbsnp)


def get_tax_all_supported_assembly_from_eva(private_config_xml_file):
    """
     Get all assemblies for taxonomy :
     tax1 -> asm1, asm2, asm3
     tax2 -> asm4, asm5, asm6
    """
    with get_metadata_connection_handle('production_processing', private_config_xml_file) as pg_conn:
        query = "select taxonomy_id, assembly_id from evapro.supported_assembly_tracker"

        tax_asm_list = {}
        for tax, asm in get_all_results_for_query(pg_conn, query):
            if tax not in tax_asm_list:
                tax_asm_list[tax] = {asm}
            else:
                tax_asm_list[tax].add(asm)

        return tax_asm_list


def get_assembly_latest_remapping_time(private_config_xml_file):
    """
     Search through the job tracker table to figure out the latest remapping time for a assembly
     Check all the entries for a assembly where job status is completed and take the latest (max) date for that assembly
    """
    asm_remaptime = {}
    # TODO: change to production_processing after filling data
    with get_pg_accession_connection_handle('development', private_config_xml_file) as pg_conn:
        query = f"""select bjep.string_val, max(bje.start_time)
                    from batch_job_execution_params bjep 
                    join batch_job_execution bje on bjep.job_execution_id = bje.job_execution_id 
                    join batch_job_instance bji on bji.job_instance_id = bje.job_instance_id 
                    where bjep.key_name = 'assemblyAccession'
                    and bji.job_name = 'INGEST_REMAPPED_VARIANTS_FROM_VCF_JOB'
                    and bje.status = 'COMPLETED' and bje.exit_code = 'COMPLETED'
                    group by bjep.string_val 
                    order by bjep.string_val"""

        for assembly, remap_time in get_all_results_for_query(pg_conn, query):
            asm_remaptime[assembly] = remap_time
        return asm_remaptime


def get_studies_for_remapping(private_config_xml_file):
    """
    Find out which studies need to be remapped for an assembly:
    1. From the job tracker find the latest remapping time for an assembly
    2. From the job tracker final all the studies that were accessioned after the remapping time for an assembly,
        these are the ones that needs to be remapped
    """
    asm_remaptime = get_assembly_latest_remapping_time(private_config_xml_file)
    with get_pg_accession_connection_handle('production_processing', private_config_xml_file) as pg_conn:
        query = f"""select bjep.job_execution_id, bjep.key_name, bjep.string_val, bje.start_time
                    from batch_job_execution_params bjep 
                    join batch_job_execution bje on bjep.job_execution_id = bje.job_execution_id 
                    join batch_job_instance bji on bji.job_instance_id = bje.job_instance_id 
                    where bjep.key_name in ('projectAccession', 'assemblyAccession') 
                    and bji.job_name = 'CREATE_SUBSNP_ACCESSION_JOB'
                    and bje.status = 'COMPLETED' and bje.exit_code = 'COMPLETED'
                    order by job_execution_id """

        res = {}
        for id, key, value, acc_time in get_all_results_for_query(pg_conn, query):
            if id not in res:
                res[id] = {key: value}
            else:
                res[id][key] = value
                res[id]['start_time'] = acc_time

        asm_studies_acc_after_remapping = {}
        for id, value in res.items():
            asm = value['assemblyAccession']
            proj = value['projectAccession']
            acc_time = value['start_time']

            if asm not in asm_remaptime or (asm in asm_remaptime and acc_time > asm_remaptime[asm]):
                if asm not in asm_studies_acc_after_remapping:
                    asm_studies_acc_after_remapping[asm] = {proj}
                else:
                    asm_studies_acc_after_remapping[asm].add(proj)

        return asm_studies_acc_after_remapping


def get_pg_accession_connection_handle(profile, private_config_xml_file):
    pg_url, pg_user, pg_pass = get_accession_pg_creds_for_profile(profile, private_config_xml_file)
    return psycopg2.connect(urlsplit(pg_url).path, user=pg_user, password=pg_pass)


def get_eva_asm_tax(private_config_xml_file):
    eva_asm_tax = {}
    with get_metadata_connection_handle("production_processing", private_config_xml_file) as pg_conn:
        query = f"select distinct assembly_id, taxonomy_id FROM evapro.supported_assembly_tracker " \
                f"order by taxonomy_id , assembly_id"
        for assembly, tax_id in get_all_results_for_query(pg_conn, query):
            eva_asm_tax[assembly] = tax_id
    return eva_asm_tax


def get_dbsnp_asm_tax():
    # retrieved from mongo db
    # hardcoded as no newer assemblies are going to be added to this list
    return {'GCA_000001215.2': 7227, 'GCA_000001215.4': 7227, 'GCA_000001515.4': 9598, 'GCA_000001515.5': 9598,
            'GCA_000001545.3': 9601, 'GCA_000001635.4': 10090, 'GCA_000001635.5': 10090, 'GCA_000001635.6': 10090,
            'GCA_000001635.9': 10090, 'GCA_000001735.1': 3702, 'GCA_000001895.3': 10116, 'GCA_000001895.4': 10116,
            'GCA_000002035.2': 7955, 'GCA_000002035.3': 7955, 'GCA_000002035.4': 7955, 'GCA_000002175.2': 9598,
            'GCA_000002195.1': 7460, 'GCA_000002255.2': 9544, 'GCA_000002265.1': 10116, 'GCA_000002285.1': 9615,
            'GCA_000002285.2': 9615, 'GCA_000002305.1': 9796, 'GCA_000002315.1': 9031, 'GCA_000002315.2': 9031,
            'GCA_000002315.3': 9031, 'GCA_000002315.5': 9031, 'GCA_000002335.3': 7070, 'GCA_000002655.1': 330879.0,
            'GCA_000002775.1': 3694, 'GCA_000002775.3': 3694, 'GCA_000003025.4': 9823, 'GCA_000003025.6': 9823,
            'GCA_000003055.3': 9913, 'GCA_000003055.5': 9913, 'GCA_000003195.1': 4558, 'GCA_000003195.3': 4558,
            'GCA_000003205.1': 9913, 'GCA_000003205.4': 9913, 'GCA_000003205.6': 9913, 'GCA_000003625.1': 9986,
            'GCA_000003745.2': 29760, 'GCA_000004515.2': 3847, 'GCA_000004515.3': 3847, 'GCA_000004515.4': 3847,
            'GCA_000004665.1': 9483, 'GCA_000005005.5': 4577, 'GCA_000005005.6': 4577, 'GCA_000005425.2': 4530,
            'GCA_000005575.1': 7165, 'GCA_000146605.2': 9103, 'GCA_000146605.3': 9103, 'GCA_000146605.4': 9103,
            'GCA_000146795.3': 61853, 'GCA_000148765.2': 3750, 'GCA_000151805.2': 59729, 'GCA_000151905.1': 9593,
            'GCA_000151905.3': 9593, 'GCA_000181335.3': 9685, 'GCA_000181335.4': 9685, 'GCA_000188115.1': 4081,
            'GCA_000188115.2': 4081, 'GCA_000188115.3': 4081, 'GCA_000188235.2': 8128, 'GCA_000219495.1': 3880,
            'GCA_000219495.2': 3880, 'GCA_000224145.1': 7719, 'GCA_000224145.2': 7719, 'GCA_000233375.4': 8030,
            'GCA_000247795.2': 9915, 'GCA_000247815.2': 59894, 'GCA_000298735.1': 9940, 'GCA_000298735.2': 9940,
            'GCA_000309985.1': 3711, 'GCA_000317375.1': 79684, 'GCA_000317765.1': 9925, 'GCA_000331145.1': 3827,
            'GCA_000355885.1': 8839, 'GCA_000364345.1': 9541, 'GCA_000372685.1': 7994, 'GCA_000409795.2': 60711,
            'GCA_000442705.1': 51953, 'GCA_000512255.2': 4072, 'GCA_000686985.1': 3708, 'GCA_000695525.1': 3712,
            'GCA_000710875.1': 4072, 'GCA_000751015.1': 3708, 'GCA_000765115.1': 9940, 'GCA_000772875.3': 9544,
            'GCA_000966335.1': 7950, 'GCA_000987745.1': 3635, 'GCA_001433935.1': 4530, 'GCA_001465895.2': 105023,
            'GCA_001522545.1': 9157, 'GCA_001522545.2': 9157, 'GCA_001577835.1': 93934, 'GCA_001625215.1': 79200,
            'GCA_001704415.1': 9925, 'GCA_001858045.3': 8128, 'GCA_002114115.1': 3750, 'GCA_002263795.2': 9913}


def get_scientific_name_from_release_table(private_config_xml_file):
    scientific_names = {}
    with get_metadata_connection_handle('production_processing', private_config_xml_file) as pg_conn:
        query = f"select distinct taxonomy, scientific_name from eva_progress_tracker.clustering_release_tracker "
        for taxonomy, scientific_name in get_all_results_for_query(pg_conn, query):
            scientific_names[taxonomy] = scientific_name

    return scientific_names


def get_scientific_name(taxonomy, scientific_names_in_release_table):
    """
    Try getting scientific name for a taxonomy either from release table (using existing entries) or from Ensembl
    """
    if taxonomy in scientific_names_in_release_table:
        return scientific_names_in_release_table[taxonomy]
    else:
        return get_scientific_name_from_ensembl(taxonomy)


def get_assembly_sources(assembly, eva_assemblies, dbsnp_assemblies):
    if assembly in dbsnp_assemblies and assembly in eva_assemblies:
        return 'DBSNP, EVA'
    elif assembly in dbsnp_assemblies:
        return 'DBSNP'
    elif assembly in eva_assemblies:
        return 'EVA'


def release_version_exists(private_config_xml_file, release_version):
    with get_metadata_connection_handle('production_processing', private_config_xml_file) as pg_conn:
        query = f"select * from eva_progress_tracker.remapping_tracker where release_version={release_version}"
        return get_all_results_for_query(pg_conn, query)


def get_asm_no_ss_ids(asm_studies, noah_prj_dir, codon_prj_dir):
    """
    For each assembly, get the total number of ssids to remap, by reading the accessioning report for all the studies
    which needs to be remapped
    """
    asm_ssids = {}
    for asm, studies in asm_studies.items():
        no_of_ss_ids_in_asm = 0
        for study in studies:
            # get path to accessioned file on both noah and codon
            noah_file_path, codon_file_path = get_accession_reports_for_study(study, noah_prj_dir, codon_prj_dir)
            for file in noah_file_path:
                no_of_ss_ids_in_asm += get_no_of_ss_ids_in_file(file)
            for file in codon_file_path:
                no_of_ss_ids_in_asm += get_no_of_ss_ids_in_file(file)

        logger.info(f"No of ss ids in assembly {asm} are {no_of_ss_ids_in_asm}")
        asm_ssids[asm] = no_of_ss_ids_in_asm

    return asm_ssids


def get_no_of_ss_ids_in_file(path):
    """
    Read a file to figure out the number of ss ids in that file
    skip line start with #
    read line where 3rd entry starts with ss (assuming all files have same sequence of headers and ss id is always 3rd)
    """
    no_of_ss_ids_in_file = 0
    with gzip.open(path, 'rt') as f:
        for line in f:
            if line.startswith('#'):
                continue
            elif line.split("\t")[2].startswith("ss"):
                no_of_ss_ids_in_file = no_of_ss_ids_in_file + 1

    print(f"No of ss ids in file {path} : {no_of_ss_ids_in_file}")
    return no_of_ss_ids_in_file


def get_accession_reports_for_study(study, noah_file_path, codon_file_path):
    """
    Given a study, find the accessioning report path for that study on both noah and codon
    look for a file ending with accessioned.vcf.gz (assuming only one file with the given pattern will be present)
    if more than one files are present, take the first one
    """

    noah_file = glob.glob(f"{noah_file_path}/{study}/60_eva_public/*accessioned.vcf.gz")
    codon_file = glob.glob(f"{codon_file_path}/{study}/60_eva_public/*accessioned.vcf.gz")

    if not noah_file and not codon_file:
        logger.error(f"Could not find any file in Noah or Codon for Study {study}")
    if not noah_file:
        logger.warning(f"No file found in Noah for Study {study}")
        noah_file = []
    if not codon_file:
        logger.warning(f"No file found in Codon for Study {study}")
        codon_file = []

    return noah_file, codon_file


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Prepare remapping table for release', add_help=False)
    parser.add_argument("--private-config-xml-file", help="ex: /path/to/eva-maven-settings.xml", required=True)
    parser.add_argument("--remapping-version", help="New Remapping Version (e.g. 2)", type=int, required=True)
    parser.add_argument("--release-version", help="Release Version (e.g. 4)", type=int, required=True)
    parser.add_argument("--noah-prj-dir", help="path to the project directory in noah", required=True)
    parser.add_argument("--codon-prj-dir", help="path to the project directory in codon", required=True)

    args = parser.parse_args()

    if release_version_exists(args.private_config_xml_file, args.release_version):
        logger.warning(f"Release Version {args.release_version} already exists in Remapping Tracker Table")

    prepare_remapping_table(args.private_config_xml_file, args.remapping_version, args.release_version,
                            args.noah_prj_dir, args.codon_prj_dir)
