#!/usr/bin/env nextflow

def helpMessage() {
    log.info"""
    Remap submitted variant from one assembly version to another.

    Inputs:
            --source_assembly_accession     assembly accession of the submitted variants that needs to be remapped to the  as  accession
            --target_assembly_accession     assembly accession of the submitted variants that needs to be remapped to the  as  accession
            --species_name                  scientific name to be used for the species
            --genome_assembly_dir           path to the directory where the genome should be downloaded
            --template_properties           path to the template properties file
    """
}

params.source_assembly_accession = null
params.target_assembly_accession = null
params.species_name = null
// help
params.help = null

// Show help message
if (params.help) exit 0, helpMessage()

// Test input files
if (!params.source_assembly_accession || !params.target_assembly_accession || !params.species_name || !params.genome_assembly_dir ) {
    if (!params.source_assembly_accession) log.warn('Provide the source assembly using --source_assembly_accession')
    if (!params.target_assembly_accession) log.warn('Provide the target assembly using --target_assembly_accession')
    if (!params.species_name) log.warn('Provide a species name using --species_name')
    if (!params.genome_assembly_dir) log.warn('Provide a path to where the assembly should be downloaded using --genome_assembly_dir')
    exit 1, helpMessage()
}



species_name = params.species_name.toLowerCase().replace(" ", "_")


process retrieve_source_genome {

    output:
    path "${params.source_assembly_accession}.fa" into source_fasta
    path "${params.source_assembly_accession}_assembly_report.txt" into source_report

    """
    genome_downloader.py --assembly-accession ${params.source_assembly_accession} --species ${species_name} --output-directory ${params.genome_assembly_dir}
    ln -s ${params.genome_assembly_dir}/${species_name}/${params.source_assembly_accession}/${params.source_assembly_accession}.fa
    ln -s ${params.genome_assembly_dir}/${species_name}/${params.source_assembly_accession}/${params.source_assembly_accession}_assembly_report.txt
    """
}


process retrieve_target_genome {

    output:
    path "${params.target_assembly_accession}.fa" into target_fasta
    path "${params.target_assembly_accession}_assembly_report.txt" into target_report

    """
    genome_downloader.py --assembly-accession ${params.target_assembly_accession} --species ${species_name} --output-directory ${params.genome_assembly_dir}
    ln -s ${params.genome_assembly_dir}/${species_name}/${params.target_assembly_accession}/${params.target_assembly_accession}.fa
    ln -s ${params.genome_assembly_dir}/${species_name}/${params.target_assembly_accession}/${params.target_assembly_accession}_assembly_report.txt
    """
}


/*
 * Extract the submitted variants to remap from the accesioning warehouse and store them in a VCF file.
 */
process extract_vcf_from_mongo {
    input:
    path source_fasta from source_fasta
    path source_report from source_report

    output:
    path "${params.source_assembly_accession}_extraction.properties" into extraction_props
    // Store both vcfs (eva and dbsnp) into one channel
    path "*.vcf" into source_vcfs
    val "${params.source_assembly_accession}_vcf_extractor.log" into log_filename

    """
    cp ${params.template_properties} ${params.source_assembly_accession}_extraction.properties
    echo "parameters.fasta=${source_fasta}" >> ${params.source_assembly_accession}_extraction.properties
    echo "parameters.assemblyReportUrl=file://${source_report}"" >> ${params.source_assembly_accession}_extraction.properties
    echo "parameters.assemblyAccession=${params.source_assembly_accession}"" >> ${params.source_assembly_accession}_extraction.properties
    echo "parameters.outputFolder=." >> ${params.source_assembly_accession}_extraction.properties

    java -jar $params.jar.vcf_extractor --spring.config.name=${params.source_assembly_accession}_extraction.properties > ${params.source_assembly_accession}_vcf_extractor.log
    """
}


/*
 * variant remmapping pipeline
 */
process remap_variants {

    input:
    path source_fasta from source_fasta
    path target_fasta from target_fasta
    path source_vcf from source_vcfs


    output:
    path "remapped_vcf.vcf" into remapped_vcfs
    path "remapped_vcf.vcf.yml" into remapped_ymls

    """
    nextflow run $params.nextflow.remapping \
      --oldgenome ${source_fasta} \
      --newgenome ${target_fasta} \
      --vcffile ${source_vcf} \
      --outfile remapped_vcf.vcf
    """
}


/*
 * Ingest the remapped submitted variants from a VCF file into the accessioning warehouse.
 */
process ingest_vcf_into_mongo {
    input:
    path remapped_vcf from remapped_vcfs

    output:
    path "${params.source_assembly_accession}_ingestion.properties" into ingestion_props
    val "${params.source_assembly_accession}_vcf_ingestion.log" into ingestion_log_filename

    script:
    """
    cp ${params.template_properties} ${params.source_assembly_accession}_ingestion.properties
    echo "parameters.vcf=${remapped_vcf}" >> ${params.source_assembly_accession}_ingestion.properties
    echo "parameters.remappedFrom=${params.source_assembly_accession}" >> ${params.source_assembly_accession}_ingestion.properties
    echo "parameters.chunkSize=100" >> ${params.source_assembly_accession}_ingestion.properties
    java -jar $params.jar.vcf_ingestion --spring.config.name=${params.source_assembly_accession}_ingestion.properties > ${params.source_assembly_accession}_vcf_ingestion.log
    """
}
