#!/bin/bash

set -Eeuo pipefail

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
SOURCE_DIR="$(dirname $(dirname $SCRIPT_DIR))"

cwd=${PWD}
cd ${SCRIPT_DIR}

mkdir -p ${SCRIPT_DIR}/genomes
PATH=${SCRIPT_DIR}/bin:$PATH

# note public_dir needs to be an absolute path, unlike others in config
printf "\e[32m===== REMAPPING PIPELINE =====\e[0m\n"
nextflow run ${SOURCE_DIR}/remapping_process.nf -params-file test_remapping_config.yaml \
   --source_assembly_accession GCA_0000001 \
	 --target_assembly_accession GCA_0000002 \
	 --species_name "Thingy thungus" \
	 --genome_assembly_dir ${SCRIPT_DIR}/genomes \
	 --template_properties ${SCRIPT_DIR}/template.properties \
	 --output_dir ${SCRIPT_DIR}/output

ls ${SCRIPT_DIR}/output/dbsnp/GCA_0000001_extraction_dbsnp_remapped.vcf \
   ${SCRIPT_DIR}/output/dbsnp/GCA_0000001_extraction_dbsnp_remapped.vcf.yml \
   ${SCRIPT_DIR}/output/eva/GCA_0000001_extraction_eva_remapped.vcf \
   ${SCRIPT_DIR}/output/eva/GCA_0000001_extraction_eva_remapped.vcf.yml

# Test we have 3 log files in the logs directory
[[ $(find ${SCRIPT_DIR}/output/logs/ -type f -name "*.log" | wc -l) -eq 3 ]]

# Test we have 3 properties files in the properties directory
[[ $(find ${SCRIPT_DIR}/output/properties/ -type f -name "*.properties" | wc -l) -eq 3 ]]

# clean up
rm -rf work .nextflow* output
cd ${cwd}
