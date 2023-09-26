import logging
import os.path
import shutil
from argparse import ArgumentParser

from ebi_eva_common_pyutils import command_utils
from ebi_eva_common_pyutils.logger import logging_config
from ebi_eva_common_pyutils.metadata_utils import get_metadata_connection_handle
from ebi_eva_common_pyutils.pg_utils import execute_query, get_all_results_for_query
from ebi_eva_common_pyutils.taxonomy.taxonomy import normalise_taxon_scientific_name, get_scientific_name_from_ensembl

logger = logging_config.get_logger(__name__, logging.DEBUG)


def get_scientific_name_from_eva(taxonomy_id, private_config_xml_file, profile):
    with get_metadata_connection_handle(profile, private_config_xml_file) as pg_conn:
        query = f"select scientific_name from evapro.taxonomy where taxonomy_id={taxonomy_id}"
        result = get_all_results_for_query(pg_conn, query)
        return result[0][0]


def update_path_reference_sequence(taxonomy, old_sc_name, new_sc_name, ref_seq_dir, ref_seq_dir_name, retain_old_dir):
    nmz_old_sc_name = normalise_taxon_scientific_name(old_sc_name)
    old_species_ref_dir = os.path.join(ref_seq_dir, ref_seq_dir_name) \
        if ref_seq_dir_name else os.path.join(ref_seq_dir, nmz_old_sc_name)
    nmz_new_sc_name = normalise_taxon_scientific_name(new_sc_name)
    new_species_ref_dir = os.path.join(ref_seq_dir, nmz_new_sc_name)

    logger.warning(f"normalize old scientific name : {nmz_old_sc_name}, old species ref dir: {old_species_ref_dir}, "
                   f"normalize new scientific name: {nmz_new_sc_name}, new species ref dir: {new_species_ref_dir}")

    if old_species_ref_dir == new_species_ref_dir:
        logger.warning(f"new species ref dir is same as old species ref dir. No need to update path reference")
        return

    if not os.path.exists(old_species_ref_dir):
        raise Exception(f'For taxonomy {taxonomy}, could not find any directory with name "{old_species_ref_dir}"')
    else:
        if os.path.exists(new_species_ref_dir):
            logger.warning(f"new species reference directory exists. syncing files between old and new dirs")
            sync_command = f"rsync -a --ignore-existing {old_species_ref_dir}/ {new_species_ref_dir}/"
            command_utils.run_command_with_output("merge old_species_dir and new_species_dir", sync_command)
            if not retain_old_dir:
                shutil.rmtree(old_species_ref_dir)
                os.symlink(new_species_ref_dir, old_species_ref_dir)
        else:
            if not retain_old_dir:
                os.rename(old_species_ref_dir, new_species_ref_dir)
                os.symlink(new_species_ref_dir, old_species_ref_dir)


def update_scientific_name_in_eva_db(private_config_xml_file, profile, taxonomy_id, new_sc_name):
    if "'" in new_sc_name:
        new_sc_name = new_sc_name.replace("'", "\''")
    query = f"update evapro.taxonomy set scientific_name='{new_sc_name}' where taxonomy_id={taxonomy_id}"
    with get_metadata_connection_handle(profile, private_config_xml_file) as pg_conn:
        execute_query(pg_conn, query)


def qc_updates(private_config_xml_file, profile, taxonomy, old_sc_name, new_sc_name, ref_seq_dir, ref_seq_dir_name,
               retain_old_dir):
    # check if reference sequence dir is created with new name
    old_species_ref_dir = os.path.join(ref_seq_dir, ref_seq_dir_name) \
        if ref_seq_dir_name else os.path.join(ref_seq_dir, normalise_taxon_scientific_name(old_sc_name))
    new_species_ref_dir = os.path.join(ref_seq_dir, normalise_taxon_scientific_name(new_sc_name))
    assert os.path.exists(new_species_ref_dir)
    assert os.path.exists(old_species_ref_dir)
    if not retain_old_dir and old_species_ref_dir != new_species_ref_dir:
        assert os.path.islink(old_species_ref_dir)

    # check if scientific name is updated in evapro db
    sc_name_from_db = get_scientific_name_from_eva(taxonomy, private_config_xml_file, profile)
    assert sc_name_from_db == new_sc_name


def main():
    argparser = ArgumentParser(description='Update scientific name for a taxonomy in db and update related '
                                           'dir path and names')
    argparser.add_argument("--private-config-xml-file", help="ex: /path/to/eva-maven-settings.xml",
                           required=True)
    argparser.add_argument("--profile", help="ex: development, production_processing",
                           default="production_processing", required=False)
    argparser.add_argument("--taxonomy", type=int, help="taxonomy id e.g. 9606", required=True)
    argparser.add_argument("--scientific-name", help=f"""new scientific name for taxonomy
                                                     (put in "" for names having space in between)""", required=False)
    argparser.add_argument("--ref-seq-dir", help="path to reference seq directory", required=True)
    argparser.add_argument("--ref-seq-dir-name", help="name of the reference directory if it could not be "
                                                      "derived by normalizing scientific name in table", required=False)
    argparser.add_argument("--retain-old-dir", action='store_true', default=False,
                           help="Don't convert old reference dir to a link if its a valid dir for some other taxonomy")
    args = argparser.parse_args()

    # update directory name in reference sequence directory
    old_sc_name = get_scientific_name_from_eva(args.taxonomy, args.private_config_xml_file, args.profile)
    new_sc_name = args.scientific_name or get_scientific_name_from_ensembl(args.taxonomy)
    logger.warning(f"old scientific name: {old_sc_name}, new scientific name: {new_sc_name}")

    update_path_reference_sequence(args.taxonomy, old_sc_name, new_sc_name, args.ref_seq_dir, args.ref_seq_dir_name,
                                   args.retain_old_dir)

    # update scientific name in evapro db
    update_scientific_name_in_eva_db(args.private_config_xml_file, args.profile, args.taxonomy, new_sc_name)

    # check if everything is successfully updated
    qc_updates(args.private_config_xml_file, args.profile, args.taxonomy, old_sc_name, new_sc_name, args.ref_seq_dir,
               args.ref_seq_dir_name, args.retain_old_dir)


if __name__ == "__main__":
    main()
