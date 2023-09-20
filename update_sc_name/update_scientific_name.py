import os.path
import shutil
from argparse import ArgumentParser

from ebi_eva_common_pyutils import command_utils
from ebi_eva_common_pyutils.metadata_utils import get_metadata_connection_handle
from ebi_eva_common_pyutils.pg_utils import execute_query
from ebi_eva_common_pyutils.taxonomy.taxonomy import get_scientific_name_from_eva, normalise_taxon_scientific_name


def update_path_reference_sequence(taxonomy, old_sc_name, ref_seq_dir, new_sc_name):
    nmz_old_sc_name = normalise_taxon_scientific_name(old_sc_name)
    old_species_ref_dir = os.path.join(ref_seq_dir, nmz_old_sc_name)
    nmz_new_sc_name = normalise_taxon_scientific_name(new_sc_name)
    new_species_ref_dir = os.path.join(ref_seq_dir, nmz_new_sc_name)

    if not os.path.exists(old_species_ref_dir):
        raise Exception(f'For taxonomy {taxonomy}, could not find any directory with name '
                        f'"{nmz_old_sc_name}" in reference sequence directory {ref_seq_dir}')
    else:
        if os.path.exists(new_species_ref_dir):
            sync_command = f"rsync -a --ignore-existing {old_species_ref_dir}/ {new_species_ref_dir}/"
            command_utils.run_command_with_output("merge old_species_dir and new_species_dir", sync_command)
            shutil.rmtree(old_species_ref_dir)
            os.symlink(new_species_ref_dir, old_species_ref_dir)
        else:
            os.rename(old_species_ref_dir, new_species_ref_dir)
            os.symlink(new_species_ref_dir, old_species_ref_dir)


def update_scientific_name_in_eva_db(private_config_xml_file, profile, taxonomy_id, new_sc_name):
    if "'" in new_sc_name:
        new_sc_name = new_sc_name.replace("'", "\''")
    query = f"update evapro.taxonomy set scientific_name='{new_sc_name}' where taxonomy_id={taxonomy_id}"
    with get_metadata_connection_handle(profile, private_config_xml_file) as pg_conn:
        execute_query(pg_conn, query)


def qc_updates(private_config_xml_file, profile, taxonomy, old_sc_name, new_sc_name, ref_seq_dir):
    # check if reference sequence dir is created with new name
    old_species_ref_dir = os.path.join(ref_seq_dir, normalise_taxon_scientific_name(old_sc_name))
    new_species_ref_dir = os.path.join(ref_seq_dir, normalise_taxon_scientific_name(new_sc_name))
    assert os.path.exists(new_species_ref_dir)
    assert os.path.exists(old_species_ref_dir)
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
                                                     (put in "" for names having space in between)""", required=True)
    argparser.add_argument("--ref-seq-dir", help="path to reference seq directory", required=True)
    args = argparser.parse_args()

    # update directory name in reference sequence directory
    old_sc_name = get_scientific_name_from_eva(args.taxonomy, args.private_config_xml_file, args.profile)
    update_path_reference_sequence(args.taxonomy, old_sc_name, args.ref_seq_dir, args.scientific_name)

    # update scientific name in evapro db
    update_scientific_name_in_eva_db(args.private_config_xml_file, args.profile, args.taxonomy, args.scientific_name)

    # check if everything is successfully updated
    qc_updates(args.private_config_xml_file, args.profile, args.taxonomy, old_sc_name, args.scientific_name,
               args.ref_seq_dir)


if __name__ == "__main__":
    main()
