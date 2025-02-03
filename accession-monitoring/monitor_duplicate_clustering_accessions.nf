#!/usr/bin/env nextflow

nextflow.enable.dsl=2

def helpMessage() {
    log.info"""
    Extract rsids from the mongo database and check if they have duplicates

    Inputs:
            --mongodb_uri          Address of the mongodb server where the data can be found.
            --output_dir           Directory where the list of discovered duplicate will be provided
            --assembly_accession   Limit the search to rsids associated with specific assembly
            --chunk_size           number of rsids processed in each chunk during the duplicate detection stage
            --email_recipient      email address that should be contacted if duplicate RS are detected
    """
}

params.mongodb_uri = null
params.chunk_size = 1000000
params.help = null

// Show help message
if (params.help) exit 0, helpMessage()

// Test input
if (!params.mongodb_uri ) {
    if (!params.mongodb_uri) log.warn('Provide the mongodb uri line using --mongodb_uri')
    if (!params.output_dir) log.warn('Provide the location for the output file containing the duplicates using --output_dir')
    if (!params.email_recipient) log.warn('Provide the email address that should be contacted upon finding duplicates using --email_recipient')
    exit 1, helpMessage()
}


process export_mongo_cluster_accessions {
    label 'med_time', 'default_mem'

    output:
    path "dbsnp_rsid_output_file", optional: true, emit: dbsnp_rs_report_filename
    path "eva_rsid_output_file", optional: true, emit: eva_rs_report_filename

    script:
    query = ""
    if (params.assembly_accession){
        query = """--query  '{"asm": "$params.assembly_accession"}'"""
    }
    """
    mongoexport --uri $params.mongodb_uri $query --collection dbsnpClusteredVariantEntity --type=csv --fields accession -o dbsnp_rsid_output_file --noHeaderLine 2>&1
    mongoexport --uri $params.mongodb_uri $query --collection clusteredVariantEntity --type=csv --fields accession -o eva_rsid_output_file --noHeaderLine 2>&1
    """
}


process sort_unique_split_accessions {

    label 'med_time', 'med_mem'

    input:
    path dbsnp_rsid
    path eva_rsid

    output:
    path "accession_chunk-*", emit: accession_chunk

    script:
    """
    set -o pipefail
    cat $dbsnp_rsid $eva_rsid | sort -u -T . -S 2G | split -a 5 -d -l $params.chunk_size - accession_chunk-
    """
}



process detect_duplicates_in_chunk {

    label 'default_time', 'med_mem'

    maxForks 10

    input:
    each path(accession_chunk)

    output:
    path "accession_chunk-*_duplicates", emit: duplicate_accession_chunk

    script:
    def duplicate_accession_chunk = accession_chunk + "_duplicates"
    """
    java -Xmx6G -jar $params.clustering_jar --spring.config.location=file:$params.clustering_properties \
    --spring.batch.job.names=DUPLICATE_RS_ACC_QC_JOB --parameters.rsAccFile=$accession_chunk \
    --parameters.duplicateRSAccFile=$duplicate_accession_chunk
    """
}


process merge_duplicates_and_notify {

    publishDir "$params.output_dir", overwrite: true, mode: "copy"

    input:
    path duplicate_accession_chunks

    output:
    path "rs_duplicate_accession_*", emit: rs_duplicate_accession

    script:
    """
    TIMESTAMP=`date +\\%Y\\%m\\%d\\%H\\%M\\%S`
    cat $duplicate_accession_chunks > rs_duplicate_accession_\$TIMESTAMP.out
    NB_DUP=`wc -l <rs_duplicate_accession_\$TIMESTAMP.out`

    if [[ \$NB_DUP -ge 1 ]]
    then
      cat > email <<- EOF
    From: eva-noreply@ebi.ac.uk
    To: $params.email_recipient
    Subject: \$NB_DUP Duplicates RS accession detected \$TIMESTAMP
    During the execution of monitor_duplicate_clustering_accessions.nf on \$TIMESTAMP,
    \$NB_DUP were detected

    Find the list of accession in
    $params.output_dir/rs_duplicate_accession_\$TIMESTAMP.out
    EOF
      cat email | sendmail $params.email_recipient
    fi
    """

}

workflow {
    main:
        export_mongo_cluster_accessions()
        sort_unique_split_accessions(export_mongo_cluster_accessions.out.dbsnp_rs_report_filename, export_mongo_cluster_accessions.out.eva_rs_report_filename)
        detect_duplicates_in_chunk(sort_unique_split_accessions.out.accession_chunk)
        merge_duplicates_and_notify(detect_duplicates_in_chunk.out.duplicate_accession_chunk.collect())
}

