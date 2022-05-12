from argparse import ArgumentParser

import requests
from ebi_eva_common_pyutils.logger import logging_config
from ebi_eva_common_pyutils.metadata_utils import get_metadata_connection_handle
from ebi_eva_common_pyutils.pg_utils import get_all_results_for_query
from ebi_eva_common_pyutils.taxonomy.taxonomy import get_scientific_name_from_ensembl

logger = logging_config.get_logger(__name__)
logging_config.add_stdout_handler()

remapping_genome_target_table = 'evapro.remapping_genome_target_tracker'
ensembl_url = 'http://rest.ensembl.org/info/assembly'


def get_all_taxonomies_from_eva(private_config_xml_file):
    taxonomy_list = []
    with get_metadata_connection_handle("development", private_config_xml_file) as pg_conn:
        query = 'SELECT DISTINCT taxonomy_id FROM evapro.taxonomy'
        for taxonomy in get_all_results_for_query(pg_conn, query):
            taxonomy_list.append(taxonomy[0])

    return taxonomy_list


def get_tax_asm_from_eva(private_config_xml_file):
    eva_tax_asm = {}
    with get_metadata_connection_handle("development", private_config_xml_file) as pg_conn:
        query = f"""SELECT DISTINCT taxonomy_id, assembly_id FROM {remapping_genome_target_table} 
        WHERE current_previous='current'"""
        for tax_id, asm in get_all_results_for_query(pg_conn, query):
            eva_tax_asm[tax_id] = asm

    return eva_tax_asm


def get_tax_asm_from_ensembl(taxonomy_list):
    ensembl_tax_asm = {}
    for tax_id in taxonomy_list:
        try:
            logger.info(f'Query Ensembl for species name using taxonomy {tax_id}')
            sp_name = get_scientific_name_from_ensembl(tax_id)
        except Exception:
            logger.warning(f'Could not get species name for taxonomy {tax_id}')
            continue
        logger.info(f'Query ensembl for supported assembly for taxonomy {tax_id}')
        asm = get_supported_asm_from_ensembl(sp_name)
        if asm != 'None':
            ensembl_tax_asm[tax_id] = asm

    return ensembl_tax_asm


def get_supported_asm_from_ensembl(scientific_name):
    url = ensembl_url + '/' + scientific_name.lower().replace(' ', '_')
    response = requests.get(url, params={'content-type': 'application/json'})
    data = response.json()
    assembly_accession = str(data.get('assembly_accession'))
    return assembly_accession


def check_supported_target_assembly(private_config_xml_file):
    taxonomy_list = get_all_taxonomies_from_eva(private_config_xml_file)
    eva_tax_asm = get_tax_asm_from_eva(private_config_xml_file)
    ensembl_tax_asm = get_tax_asm_from_ensembl(taxonomy_list)

    taxonomy_not_present_in_eva = []
    taxonomy_not_present_in_ensembl = []

    for tax_id in taxonomy_list:
        if tax_id in eva_tax_asm and tax_id in ensembl_tax_asm:
            if eva_tax_asm[tax_id] != ensembl_tax_asm[tax_id]:
                logger.warning(f'Taxonomy {tax_id} has different supported assembly '
                               f'in EVA({eva_tax_asm[tax_id]}) and Ensembl({ensembl_tax_asm[tax_id]})')
        elif tax_id not in eva_tax_asm:
            taxonomy_not_present_in_eva.append(tax_id)
        elif tax_id not in ensembl_tax_asm:
            taxonomy_not_present_in_ensembl.append(tax_id)

    logger.info(f'The following taxonomy were not present in EVA: {taxonomy_not_present_in_eva}')
    logger.info(f'The following taxonomy were not present in Ensembl: {taxonomy_not_present_in_ensembl}')


def main():
    argparse = ArgumentParser(description='Track the currently supported assembly by Ensembl for every species '
                                          'and report if its matches with what is supported by EVA')
    argparse.add_argument('--private_config_xml_file', required=True,
                          help='Path to the file containing the username/passwords tp access '
                               'production and development databases')
    args = argparse.parse_args()

    check_supported_target_assembly(args.private_config_xml_file)


if __name__ == "__main__":
    main()
