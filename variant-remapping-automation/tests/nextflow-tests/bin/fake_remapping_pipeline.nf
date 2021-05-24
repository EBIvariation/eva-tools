#!/usr/bin/env nextflow


outfile_basename = file(params.outfile).getName()

process remap_vcf {

    publishDir workflow.launchDir

    output:
    path "${outfile_basename}" into remapped_vcf
    path "${outfile_basename}.yml" into remapped_yml

    script:
    """
    touch ${outfile_basename}
    touch ${outfile_basename}.yml
    """
}
