#!/usr/bin/env nextflow

process ingest_vcf_into_mongo {

    publishDir workflow.launchDir


    output:
    path "remapped_vcf.vcf" into remapped_vcf
    path "remapped_vcf.vcf.yml" into remapped_yml

    script:
    """
    touch remapped_vcf.vcf remapped_vcf.vcf.yml
    """
}
