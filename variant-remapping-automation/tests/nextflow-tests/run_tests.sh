#!/bin/bash

set -Eeuo pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
SOURCE_DIR="$(dirname $(dirname $SCRIPT_DIR))"

cwd=${PWD}
cd ${SCRIPT_DIR}

mkdir -p ${SCRIPT_DIR}/genomes
PATH=${SCRIPT_DIR}/bin:$PATH

# run accession and variant load
# note public_dir needs to be an absolute path, unlike others in config
printf "\e[32m===== REMAPPING PIPELINE =====\e[0m\n"
nextflow run ${SOURCE_DIR}/remapping_process.nf -params-file test_remapping_config.yaml \
   --source_assembly_accession GCA_0000001 \
	 --target_assembly_accession GCA_0000002 \
	 --species_name "Thingy thungus" \
	 --genome_assembly_dir ${SCRIPT_DIR}/genomes \
	 --template_properties ${SCRIPT_DIR}/template.properties

# clean up
rm -rf work .nextflow*
cd ${cwd}
