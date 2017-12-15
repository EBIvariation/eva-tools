--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.7
-- Dumped by pg_dump version 9.5.7
/*
SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;
*/

-- assembly Gallus_gallus-5.0
CREATE TABLE dbsnp_variant_load_d8c757988871529f37061fa9c79477a5 (
    ss_id bigint NULL,
    rs_id bigint NULL,
    batch_id integer NULL,
    batch_name varchar(64) NULL,
    hgvs_c_string varchar(300) NULL,
    hgvs_c_start integer NULL,
    hgvs_c_stop integer NULL,
    reference_c varchar(1000) NULL,
    hgvs_t_string varchar(300) NULL,
    hgvs_t_start integer NULL,
    hgvs_t_stop integer NULL,
    reference_t varchar(1000) NULL,
    alternate varchar(300) NULL,
    alleles varchar(1024) NULL,
    contig_name varchar(63) NULL,
    contig_start integer NULL,
    contig_end integer NULL,
    loc_type smallint NULL,
    chromosome varchar(32) NULL,
    chromosome_start integer NULL,
    chromosome_end integer NULL,
    hgvs_c_orientation integer NULL,
    hgvs_t_orientation integer NULL,
    snp_orientation integer NULL,
    contig_orientation integer NULL,
    subsnp_orientation integer NULL,
    genotypes_string text NULL,
    freq_info text NULL,
    load_order integer NOT NULL
);

-- assembly Gallus_gallus-4.0
CREATE TABLE dbsnp_variant_load_8A503D989BF1F58E95A7861FC999EA1D (
    ss_id bigint NULL,
    rs_id bigint NULL,
    batch_id integer NULL,
    batch_name varchar(64) NULL,
    hgvs_c_string varchar(300) NULL,
    hgvs_c_start integer NULL,
    hgvs_c_stop integer NULL,
    reference_c varchar(1000) NULL,
    hgvs_t_string varchar(300) NULL,
    hgvs_t_start integer NULL,
    hgvs_t_stop integer NULL,
    reference_t varchar(1000) NULL,
    alternate varchar(300) NULL,
    alleles varchar(1024) NULL,
    contig_name varchar(63) NULL,
    contig_start integer NULL,
    contig_end integer NULL,
    loc_type smallint NULL,
    chromosome varchar(32) NULL,
    chromosome_start integer NULL,
    chromosome_end integer NULL,
    hgvs_c_orientation integer NULL,
    hgvs_t_orientation integer NULL,
    snp_orientation integer NULL,
    contig_orientation integer NULL,
    subsnp_orientation integer NULL,
    genotypes_string text NULL,
    freq_info text NULL,
    load_order integer NOT NULL
);


CREATE TABLE batch_id_equiv (
    subind_batch_id integer,
    subsnp_batch_id integer
);

--
-- Name: allelefreqbysspop; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE allelefreqbysspop (
    subsnp_id integer NOT NULL,
    pop_id integer NOT NULL,
    allele_id integer NOT NULL,
    source character(2) NOT NULL,
    cnt real NOT NULL,
    freq real NOT NULL,
    last_updated_time timestamp without time zone NOT NULL
);

CREATE TABLE DBSNP_VARIANT_LOAD_328719E1B4583D1BBD745B39809D7A82 (
    batch_id INTEGER NOT NULL,
    load_order INTEGER NOT NULL,
    subsnp_id integer NOT NULL,
    pop_id integer NOT NULL,
    allele_id integer NOT NULL,
    source character(2) NOT NULL,
    cnt real NOT NULL,
    freq real NOT NULL,
    last_updated_time timestamp without time zone NOT NULL
);


--
-- Name: b150_contiginfo; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE b150_contiginfo (
    ctg_id bigint NOT NULL,
    tax_id integer NOT NULL,
    contig_acc character varying(32) NOT NULL,
    contig_ver smallint NOT NULL,
    contig_name character varying(63),
    contig_chr character varying(32),
    contig_start integer,
    contig_end integer,
    orient smallint,
    contig_gi bigint NOT NULL,
    group_term character varying(32),
    group_label character varying(32),
    contig_label character varying(32),
    primary_fl smallint NOT NULL,
    genbank_gi bigint,
    genbank_acc character varying(32),
    genbank_ver smallint,
    build_id integer NOT NULL,
    build_ver integer NOT NULL,
    last_updated_time timestamp without time zone DEFAULT now() NOT NULL,
    placement_status smallint NOT NULL,
    asm_acc character varying(32) NOT NULL,
    asm_version smallint NOT NULL,
    chr_gi bigint,
    par_fl smallint,
    top_level_fl smallint NOT NULL,
    gen_rgn character varying(32),
    contig_length integer
);


--
-- Name: b150_maplink; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE b150_maplink (
    snp_type character(2),
    snp_id bigint,
    gi bigint,
    accession_how_cd smallint,
    "offset" integer,
    asn_to integer,
    lf_ngbr integer,
    rf_ngbr integer,
    lc_ngbr integer,
    rc_ngbr integer,
    loc_type smallint,
    build_id integer NOT NULL,
    process_time timestamp without time zone,
    process_status smallint,
    orientation smallint,
    allele character varying(1024),
    aln_quality double precision,
    num_mism integer,
    num_del integer,
    num_ins integer,
    tier integer,
    ctg_gi bigint,
    ctg_from integer,
    ctg_to integer,
    ctg_orient smallint,
    source character varying(64)
);


--
-- Name: b150_maplinkinfo; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE b150_maplinkinfo (
    gi bigint NOT NULL,
    accession character varying(32) NOT NULL,
    accession_ver smallint NOT NULL,
    acc character varying(32) NOT NULL,
    version smallint NOT NULL,
    status character varying(32),
    create_dt timestamp without time zone,
    update_dt timestamp without time zone,
    cds_from integer,
    cds_to integer
);


--
-- Name: b150_proteininfo; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE b150_proteininfo (
    gi bigint NOT NULL,
    acc character varying(32) NOT NULL,
    version smallint NOT NULL,
    prot_gi bigint NOT NULL,
    prot_acc character varying(32) NOT NULL,
    prot_ver smallint NOT NULL,
    status character varying(32),
    create_dt timestamp without time zone,
    update_dt timestamp without time zone
);


--
-- Name: b150_snp_bitfield; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE b150_snp_bitfield (
    snp_id integer NOT NULL,
    ver_code smallint,
    link_prop_b1 smallint,
    link_prop_b2 smallint,
    gene_prop_b1 smallint,
    gene_prop_b2 smallint,
    map_prop smallint,
    freq_prop smallint,
    gty_prop smallint,
    hapmap_prop smallint,
    pheno_prop smallint,
    variation_class smallint NOT NULL,
    quality_check smallint,
    upd_time timestamp without time zone NOT NULL
--    encoding bytea
);


--
-- Name: b150_snpchrposonref; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE b150_snpchrposonref (
    snp_id bigint,
    chr character varying(32),
    pos integer,
    orien smallint,
    neighbor_snp_list integer,
    ispar character varying(1)
);


--
-- Name: b150_snpcontigloc; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE b150_snpcontigloc (
    snp_type character varying(2),
    snp_id bigint,
    ctg_id bigint,
    asn_from integer,
    asn_to integer,
    lf_ngbr integer,
    rf_ngbr integer,
    lc_ngbr integer,
    rc_ngbr integer,
    loc_type smallint,
    phys_pos_from integer,
    snp_bld_id integer NOT NULL,
    last_updated_time timestamp without time zone,
    process_status smallint,
    orientation smallint,
    allele character varying(1024),
    loc_sts_uid integer,
    aln_quality real,
    num_mism integer,
    num_del integer,
    num_ins integer,
    tier smallint
);


--
-- Name: b150_snpcontiglocusid; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE b150_snpcontiglocusid (
    snp_id bigint,
    contig_acc character varying(32) NOT NULL,
    contig_ver smallint,
    asn_from integer,
    asn_to integer,
    locus_id integer,
    locus_symbol character varying(64),
    mrna_acc character varying(32) NOT NULL,
    mrna_ver smallint NOT NULL,
    protein_acc character varying(32),
    protein_ver smallint,
    fxn_class integer,
    reading_frame integer,
    allele character varying(1000),
    residue character varying(341),
    aa_position integer,
    build_id character varying(3) NOT NULL,
    ctg_id bigint,
    mrna_start integer,
    mrna_stop integer,
    codon character varying(1000),
    protres character(3),
    contig_gi bigint,
    mrna_gi bigint,
    mrna_orien integer,
    cp_mrna_ver integer,
    cp_mrna_gi integer,
    vercomp integer
);


--
-- Name: b150_snphgvslink; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE b150_snphgvslink (
    hlink_id bigint NOT NULL,
    hgvs_c character varying(300),
    gi_c bigint,
    start_c integer,
    stop_c integer,
    orient_c smallint,
    ref_allele_c character varying(1000),
    gene_loc_type_c integer,
    err_code_c integer,
    err_msg_c character varying(512),
    hgvs_t character varying(300),
    gi_t bigint,
    start_t integer,
    stop_t integer,
    orient_t smallint,
    ref_allele_t character varying(1000),
    gene_loc_type_t integer,
    err_code_t integer,
    err_msg_t character varying(512),
    hgvs_g character varying(300),
    gi_g bigint,
    start_g integer,
    stop_g integer,
    orient_g smallint,
    ref_allele_g character varying(1000),
    gene_loc_type_g integer,
    err_code_g integer,
    err_msg_g character varying(512),
    hgvs_m character varying(300),
    gi_m bigint,
    start_m integer,
    stop_m integer,
    orient_m smallint,
    ref_allele_m character varying(1000),
    gene_loc_type_m integer,
    err_code_m integer,
    err_msg_m character varying(512),
    hgvs_p character varying(300),
    gi_p bigint,
    start_p integer,
    stop_p integer,
    ref_aa character varying(1000),
    var_allele_p character varying(1000),
    codon_frame smallint,
    err_code_p integer,
    err_msg_p character varying(512),
    var_allele character varying(300),
    loc_type smallint,
    locus_id integer,
    effect integer,
    snp_link integer,
    session_id character varying(64),
    hgvs_sub character varying(300),
    processed_time timestamp without time zone,
    err_code_sub integer,
    err_msg_sub character varying(512),
    loc_type_g smallint,
    loc_type_m smallint,
    variation_type integer,
    ref_codon character varying(300),
    rs_orient smallint,
    pseudo_gene smallint,
    locus_symbol character varying(64),
    so_terms_m character varying(200),
    so_term_int integer,
    so_terms_p character varying(200),
    so_term_int_p integer,
    var_allele_m character varying(300)
);


--
-- Name: b150_snpmapinfo; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE b150_snpmapinfo (
    snp_type character varying(2) NOT NULL,
    snp_id bigint,
    chr_cnt integer NOT NULL,
    contig_cnt integer NOT NULL,
    loc_cnt integer NOT NULL,
    weight integer NOT NULL,
    hap_cnt integer,
    placed_cnt integer NOT NULL,
    unlocalized_cnt integer NOT NULL,
    unplaced_cnt integer NOT NULL,
    aligned_cnt integer NOT NULL,
    md5 character(32),
    asm_acc character varying(32),
    asm_version smallint,
    assembly character varying(32)
);


--
-- Name: batch; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE batch (
    batch_id integer NOT NULL,
    handle character varying(20) NOT NULL,
    loc_batch_id character varying(64) NOT NULL,
    loc_batch_id_upp character varying(64) NOT NULL,
    batch_type character(3) NOT NULL,
    status smallint,
    simul_sts_status smallint NOT NULL,
    moltype character varying(8) NOT NULL,
    method_id integer NOT NULL,
    samplesize integer,
    synonym_type character varying(255),
    submitted_time timestamp without time zone NOT NULL,
    linkout_url character varying(255),
    pop_id integer,
    last_updated_time timestamp without time zone,
    success_rate_int integer,
    build_id integer,
    tax_id integer NOT NULL,
    ss_cnt integer
);


--
-- Name: batchcita; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE batchcita (
    batch_id integer NOT NULL,
    "position" integer NOT NULL,
    pub_id integer NOT NULL,
    citation character varying(255) NOT NULL,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone
);


--
-- Name: batchcommline; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE batchcommline (
    batch_id integer NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone
);


--
-- Name: batchcultivar; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE batchcultivar (
    batch_id integer NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255),
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone
);


--
-- Name: batchmeexline; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE batchmeexline (
    batch_id integer NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone
);


--
-- Name: batchstrain; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE batchstrain (
    batch_id integer NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone
);


--
-- Name: batchvalcode; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE batchvalcode (
    batch_id integer NOT NULL,
    validation_status smallint NOT NULL
);


--
-- Name: contact; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE contact (
    batch_id integer NOT NULL,
    handle character varying(20) NOT NULL,
    name character varying(255) NOT NULL,
    fax character varying(255),
    phone character varying(255),
    email character varying(255),
    lab character varying(255),
    institution character varying(255),
    address character varying(255),
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone
);


--
-- Name: dn_batchcount; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE dn_batchcount (
    batch_id integer NOT NULL,
    ss_cnt integer NOT NULL,
    rs_cnt integer NOT NULL,
    rs_validated_cnt integer NOT NULL,
    create_time timestamp without time zone NOT NULL,
    pop_cnt integer,
    ind_cnt integer
);


--
-- Name: dn_handlecount; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE dn_handlecount (
    handle character varying(20) NOT NULL,
    batch_type character(3) NOT NULL,
    ss_cnt integer NOT NULL,
    rs_cnt integer,
    rs_validated_cnt integer,
    create_time timestamp without time zone NOT NULL
);


--
-- Name: dn_ind_batch_pop; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE dn_ind_batch_pop (
    batch_id smallint NOT NULL,
    pop_id integer NOT NULL,
    update_time timestamp without time zone NOT NULL
);


--
-- Name: dn_ind_batchcount; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE dn_ind_batchcount (
    batch_id integer NOT NULL,
    pop_id integer NOT NULL,
    ss_cnt integer NOT NULL,
    rs_cnt integer NOT NULL,
    ind_cnt integer NOT NULL,
    create_time timestamp without time zone NOT NULL
);


--
-- Name: dn_populationindgrp; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE dn_populationindgrp (
    pop_id integer NOT NULL,
    ind_grp_name character varying(32) NOT NULL,
    ind_grp_code smallint NOT NULL
);


--
-- Name: dn_snpfxncnt; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE dn_snpfxncnt (
    build_id integer NOT NULL,
    fxn_class smallint,
    snp_cnt integer NOT NULL,
    gene_cnt integer NOT NULL,
    create_time timestamp without time zone NOT NULL,
    last_updated_time timestamp without time zone NOT NULL,
    tax_id integer NOT NULL
);


--
-- Name: dn_table_rowcount; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE dn_table_rowcount (
    tabname character varying(64) NOT NULL,
    row_cnt integer NOT NULL,
    build_id integer NOT NULL,
    update_time timestamp without time zone NOT NULL,
    rows_in_spaceused integer,
    reserved_kb_spaceused integer,
    data_kb_spaceused integer,
    index_size_kb_spaceused integer,
    unused_kb_spaceused integer
);


--
-- Name: freqsummarybysspop; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE freqsummarybysspop (
    subsnp_id integer NOT NULL,
    pop_id integer NOT NULL,
    source character varying(1) NOT NULL,
    chr_cnt integer NOT NULL,
    ind_cnt integer NOT NULL,
    non_founder_ind_cnt integer NOT NULL,
    chisq real,
    df smallint,
    hwp real,
    het real,
    het_se real,
    last_updated_time timestamp without time zone NOT NULL
);


--
-- Name: geneidtoname; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE geneidtoname (
    gene_id integer NOT NULL,
    gene_symbol character varying(64) NOT NULL,
    gene_name character varying(255),
    gene_type character varying(255),
    tax_id integer NOT NULL,
    last_update_time timestamp without time zone NOT NULL,
    ref_tax_id integer NOT NULL,
    dbsnp_tax_id integer NOT NULL,
    ins_time timestamp without time zone
);


--
-- Name: gtyfreqbysspop; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE gtyfreqbysspop (
    subsnp_id integer,
    pop_id integer,
    unigty_id integer,
    source character varying(1),
    cnt real,
    freq real,
    last_updated_time timestamp without time zone NOT NULL
);


--
-- Name: indgrpcode; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE indgrpcode (
    code smallint NOT NULL,
    name character varying(32) NOT NULL,
    descrip character varying(255) NOT NULL
);


--
-- Name: indivbysource; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE indivbysource (
    ind_id integer NOT NULL,
    src_id integer NOT NULL,
    src_ind_id character varying(64) NOT NULL,
    create_time timestamp without time zone NOT NULL,
    src_ind_grp character varying(64)
);


--
-- Name: individual; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE individual (
    ind_id integer NOT NULL,
    descrip character varying(255),
    create_time timestamp without time zone NOT NULL,
    tax_id integer,
    ind_grp smallint
);


--
-- Name: indivsourcecode; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE indivsourcecode (
    code integer NOT NULL,
    name character varying(22) NOT NULL,
    descrip character varying(255),
    create_time timestamp without time zone NOT NULL,
    src_type character varying(10),
    display_order smallint
);


--
-- Name: pedigree; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE pedigree (
    ped_id numeric NOT NULL,
    curator character varying(12) NOT NULL,
    curator_ped_id character varying(12) NOT NULL,
    create_time timestamp without time zone NOT NULL
);


--
-- Name: pedigreeindividual; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE pedigreeindividual (
    ped_id numeric NOT NULL,
    ind_id integer NOT NULL,
    ma_ind_id integer,
    pa_ind_id integer,
    sex character(1),
    create_time timestamp without time zone NOT NULL
);


--
-- Name: popline; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE popline (
    pop_id integer NOT NULL,
    line_num integer NOT NULL,
    line character varying(255) NOT NULL,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone
);


--
-- Name: popmandline; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE popmandline (
    pop_id integer NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone
);


--
-- Name: population; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE population (
    pop_id integer NOT NULL,
    handle character varying(20) NOT NULL,
    loc_pop_id character varying(64) NOT NULL,
    loc_pop_id_upp character varying(64) NOT NULL,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone,
    src_id integer
);


--
-- Name: rsmergearch; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE rsmergearch (
    rshigh bigint NOT NULL,
    rslow bigint NOT NULL,
    build_id integer NOT NULL,
    orien smallint,
    create_time timestamp without time zone NOT NULL,
    last_updated_time timestamp without time zone NOT NULL,
    rscurrent integer,
    orien2current smallint,
    comment character varying(255)
);


--
-- Name: snp; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE snp (
    snp_id bigint NOT NULL,
    avg_heterozygosity real,
    het_se real,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone,
    cpg_code smallint,
    tax_id integer NOT NULL,
    validation_status smallint,
    exemplar_subsnp_id bigint NOT NULL,
    univar_id integer,
    cnt_subsnp integer,
    map_property smallint
);


--
-- Name: snp3d; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE snp3d (
    snp_id integer NOT NULL,
    protein_acc character(10) NOT NULL,
    master_gi bigint,
    neighbor_gi bigint,
    aa_position integer NOT NULL,
    var_res character(1) NOT NULL,
    contig_res character(1) NOT NULL,
    neighbor_res character(1) NOT NULL,
    neighbor_pos integer NOT NULL,
    var_color integer NOT NULL,
    var_label integer NOT NULL
);


--
-- Name: snp_bitfield; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE snp_bitfield (
    snp_id integer NOT NULL,
    ver_code smallint,
    link_prop_b1 smallint,
    link_prop_b2 smallint,
    gene_prop_b1 smallint,
    gene_prop_b2 smallint,
    map_prop smallint,
    freq_prop smallint,
    gty_prop smallint,
    hapmap_prop smallint,
    pheno_prop smallint,
    variation_class smallint NOT NULL,
    quality_check smallint,
    upd_time timestamp without time zone NOT NULL
--    encoding bytea
);


--
-- Name: snpallelefreq; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE snpallelefreq (
    snp_id bigint NOT NULL,
    allele_id integer NOT NULL,
    chr_cnt double precision,
    freq double precision,
    last_updated_time timestamp without time zone NOT NULL
);


--
-- Name: snpancestralallele; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE snpancestralallele (
    snp_id integer NOT NULL,
    ancestral_allele_id integer NOT NULL
);


--
-- Name: snpgtyfreq; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE snpgtyfreq (
    snp_id bigint NOT NULL,
    unigty_id integer NOT NULL,
    ind_cnt double precision,
    freq double precision,
    last_updated_time timestamp without time zone NOT NULL
);


--
-- Name: snphistory; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE snphistory (
    snp_id integer NOT NULL,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone NOT NULL,
    history_create_time timestamp without time zone,
    comment character varying(255),
    reactivated_time timestamp without time zone
);


--
-- Name: snphwprob; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE snphwprob (
    snp_id integer NOT NULL,
    df smallint,
    chisq real,
    hwp real,
    ind_cnt smallint,
    last_updated_time timestamp without time zone
);


--
-- Name: snppubmed; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE snppubmed (
    snp_id integer NOT NULL,
    subsnp_id integer NOT NULL,
    pubmed_id integer NOT NULL,
    type character varying(16) NOT NULL,
    score integer NOT NULL,
    upd_date timestamp without time zone NOT NULL
);


--
-- Name: snpsubsnplink; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE snpsubsnplink (
    subsnp_id bigint NOT NULL,
    snp_id bigint,
    substrand_reversed_flag smallint,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone,
    build_id integer,
    comment character varying(255)
);


--
-- Name: snpsubsnplinkhistory; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE snpsubsnplinkhistory (
    subsnp_id bigint,
    snp_id integer,
    build_id integer,
    history_create_time timestamp without time zone NOT NULL,
    link_create_time timestamp without time zone,
    link_last_updated_time timestamp without time zone,
    orien smallint,
    build_id_when_history_made integer,
    comment character varying(255)
);


--
-- Name: snpval; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE snpval (
    batch_id integer NOT NULL,
    snp_id integer NOT NULL
);


--
-- Name: subind; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE subind (
    batch_id integer,
    subsnp_id integer NOT NULL,
    submitted_ind_id smallint NOT NULL,
    submitted_strand_code integer,
    allele_flag smallint,
    gty_id integer,
    submitted_rs integer
);


--
-- Name: submittedindividual; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE submittedindividual (
    submitted_ind_id integer NOT NULL,
    pop_id integer NOT NULL,
    loc_ind_id_upp character varying(64) NOT NULL,
    ind_id integer,
    create_time timestamp without time zone NOT NULL,
    last_updated_time timestamp without time zone,
    tax_id integer NOT NULL,
    loc_ind_alias character varying(64),
    loc_ind_id character varying(64),
    loc_ind_grp character varying(64),
    ploidy smallint DEFAULT 2
);


--
-- Name: subpop; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE subpop (
    batch_id integer NOT NULL,
    subsnp_id bigint NOT NULL,
    pop_id integer NOT NULL,
    type character(3) NOT NULL,
    samplesize integer NOT NULL,
    submitted_strand_code smallint,
    submitted_rs integer,
    allele_flag smallint,
    ambiguity_status smallint,
    sub_heterozygosity real,
    est_heterozygosity real,
    est_het_se_sq real,
    last_updated_time timestamp without time zone DEFAULT now() NOT NULL,
    observed character varying(255),
    sub_het_se_sq real,
    subpop_id integer NOT NULL
);


--
-- Name: subpopallele; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE subpopallele (
    batch_id integer NOT NULL,
    subsnp_id bigint NOT NULL,
    pop_id integer NOT NULL,
    allele character(1) NOT NULL,
    other character varying(255),
    freq real,
    cnt_int integer,
    freq_min real,
    freq_max real,
    data_src character varying(6),
    type character(3),
    last_updated_time timestamp without time zone,
    allele_flag smallint,
    cnt real,
    allele_id integer,
    subpop_id integer NOT NULL
);


--
-- Name: subpopgty; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE subpopgty (
    subpop_id integer NOT NULL,
    gty_id integer NOT NULL,
    gty_str character varying(255),
    cnt real,
    freq real,
    last_updated_time timestamp without time zone NOT NULL
);


--
-- Name: subsnp; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE subsnp (
    subsnp_id bigint NOT NULL,
    known_snp_handle character varying(20),
    known_snp_loc_id character varying(64),
    known_snp_loc_id_upp character varying(64),
    batch_id integer NOT NULL,
    loc_snp_id character varying(64),
    loc_snp_id_upp character varying(64),
    synonym_names character varying(255),
    loc_sts_id character varying(64),
    loc_sts_id_upp character varying(64),
    segregate character(1) NOT NULL,
    indiv_homozygosity_detected character(1),
    pcr_confirmed_ind character(1),
    gene_name character varying(64),
    sequence_len integer,
    samplesize integer,
    expressed_sequence_ind character(1),
    somatic_ind character(1),
    sub_locus_id integer,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone,
    ancestral_allele character varying(255),
    cpg_code smallint,
    variation_id integer,
    top_or_bot_strand character(1),
    validation_status smallint,
    snp_id integer,
    tax_id integer NOT NULL,
    chr_id smallint
);


--
-- Name: subsnp_top_or_bot; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE subsnp_top_or_bot (
    subsnp_id integer NOT NULL,
    top_or_bot character(1),
    step smallint,
    last_updated_time timestamp without time zone
);


--
-- Name: subsnpacc; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE subsnpacc (
    subsnp_id bigint NOT NULL,
    acc_type_ind character(1) NOT NULL,
    acc_part character varying(16) NOT NULL,
    acc_ver integer
);


--
-- Name: subsnpcommline; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE subsnpcommline (
    subsnp_id bigint NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL
);


--
-- Name: subsnplinkout; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE subsnplinkout (
    subsnp_id bigint NOT NULL,
    url_val character varying(255) NOT NULL,
    updated_time timestamp without time zone,
    link_type character varying(3) DEFAULT 'NA' NOT NULL
);


--
-- Name: subsnpmdfailln; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE subsnpmdfailln (
    subsnp_id integer NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL
);


--
-- Name: subsnpnovariseq; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE subsnpnovariseq (
    subsnp_id integer NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL
);


--
-- Name: subsnppubmed; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE subsnppubmed (
    subsnp_id integer NOT NULL,
    line_num integer NOT NULL,
    pubmed_id integer NOT NULL,
    updated_time timestamp without time zone
);


--
-- Name: subsnpseq3; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE subsnpseq3 (
    subsnp_id bigint NOT NULL,
    type smallint NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL
);


--
-- Name: subsnpseq5; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE subsnpseq5 (
    subsnp_id bigint NOT NULL,
    type smallint NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL
);


--
-- Name: subsnpseqpos; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE subsnpseqpos (
    subsnp_id integer NOT NULL,
    contig_acc character varying(20) NOT NULL,
    contig_pos integer NOT NULL,
    chr character varying(2),
    upstream_len integer NOT NULL,
    downstream_len integer NOT NULL,
    last_update_time timestamp without time zone NOT NULL
);


--
-- Name: synonym; Type: TABLE; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE TABLE synonym (
    subsnp_id integer NOT NULL,
    type character varying(64) NOT NULL,
    name character varying(64)
);


--
-- Name: b150_contiginfo_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE b150_contiginfo
    ADD CONSTRAINT b150_contiginfo_pkey PRIMARY KEY (contig_gi);


--
-- Name: b150_snp_bitfield_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE b150_snp_bitfield
    ADD CONSTRAINT b150_snp_bitfield_pkey PRIMARY KEY (snp_id);


--
-- Name: batch_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE batch
    ADD CONSTRAINT batch_pkey PRIMARY KEY (batch_id);


--
-- Name: batchcita_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE batchcita
    ADD CONSTRAINT batchcita_pkey PRIMARY KEY (batch_id, "position");


--
-- Name: batchcommline_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE batchcommline
    ADD CONSTRAINT batchcommline_pkey PRIMARY KEY (batch_id, line_num);


--
-- Name: batchcultivar_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE batchcultivar
    ADD CONSTRAINT batchcultivar_pkey PRIMARY KEY (batch_id, line_num);


--
-- Name: batchmeexline_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE batchmeexline
    ADD CONSTRAINT batchmeexline_pkey PRIMARY KEY (batch_id, line_num);


--
-- Name: batchstrain_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE batchstrain
    ADD CONSTRAINT batchstrain_pkey PRIMARY KEY (batch_id, line_num);


--
-- Name: batchvalcode_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE batchvalcode
    ADD CONSTRAINT batchvalcode_pkey PRIMARY KEY (batch_id);


--
-- Name: contact_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE contact
    ADD CONSTRAINT contact_pkey PRIMARY KEY (batch_id, handle);


--
-- Name: dn_batchcount_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE dn_batchcount
    ADD CONSTRAINT dn_batchcount_pkey PRIMARY KEY (batch_id);


--
-- Name: dn_handlecount_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE dn_handlecount
    ADD CONSTRAINT dn_handlecount_pkey PRIMARY KEY (handle, batch_type);


--
-- Name: dn_ind_batchcount_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE dn_ind_batchcount
    ADD CONSTRAINT dn_ind_batchcount_pkey PRIMARY KEY (batch_id, pop_id);


--
-- Name: dn_populationindgrp_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE dn_populationindgrp
    ADD CONSTRAINT dn_populationindgrp_pkey PRIMARY KEY (pop_id);


--
-- Name: freqsummarybysspop_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE freqsummarybysspop
    ADD CONSTRAINT freqsummarybysspop_pkey PRIMARY KEY (subsnp_id, pop_id);


--
-- Name: geneidtoname_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE geneidtoname
    ADD CONSTRAINT geneidtoname_pkey PRIMARY KEY (gene_id);


--
-- Name: indgrpcode_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE indgrpcode
    ADD CONSTRAINT indgrpcode_pkey PRIMARY KEY (code);


--
-- Name: indivbysource_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE indivbysource
    ADD CONSTRAINT indivbysource_pkey PRIMARY KEY (src_id, src_ind_id);


--
-- Name: individual_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE individual
    ADD CONSTRAINT individual_pkey PRIMARY KEY (ind_id);


--
-- Name: pedigree_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE pedigree
    ADD CONSTRAINT pedigree_pkey PRIMARY KEY (ped_id);


--
-- Name: pedigreeindividual_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE pedigreeindividual
    ADD CONSTRAINT pedigreeindividual_pkey PRIMARY KEY (ped_id, ind_id);


--
-- Name: popline_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE popline
    ADD CONSTRAINT popline_pkey PRIMARY KEY (pop_id, line_num);


--
-- Name: popmandline_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE popmandline
    ADD CONSTRAINT popmandline_pkey PRIMARY KEY (pop_id, line_num);


--
-- Name: population_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE population
    ADD CONSTRAINT population_pkey PRIMARY KEY (pop_id);


--
-- Name: snp_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE snp
    ADD CONSTRAINT snp_pkey PRIMARY KEY (snp_id);


--
-- Name: snpallelefreq_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE snpallelefreq
    ADD CONSTRAINT snpallelefreq_pkey PRIMARY KEY (snp_id, allele_id);


--
-- Name: snpancestralallele_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE snpancestralallele
    ADD CONSTRAINT snpancestralallele_pkey PRIMARY KEY (snp_id);


--
-- Name: snpgtyfreq_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE snpgtyfreq
    ADD CONSTRAINT snpgtyfreq_pkey PRIMARY KEY (snp_id, unigty_id);


--
-- Name: snphistory_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE snphistory
    ADD CONSTRAINT snphistory_pkey PRIMARY KEY (snp_id);


--
-- Name: snphwprob_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE snphwprob
    ADD CONSTRAINT snphwprob_pkey PRIMARY KEY (snp_id);


--
-- Name: snpval_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE snpval
    ADD CONSTRAINT snpval_pkey PRIMARY KEY (batch_id, snp_id);


--
-- Name: submittedindividual_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE submittedindividual
    ADD CONSTRAINT submittedindividual_pkey PRIMARY KEY (pop_id, loc_ind_id_upp);


--
-- Name: subpop_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE subpop
    ADD CONSTRAINT subpop_pkey PRIMARY KEY (batch_id, subsnp_id, pop_id, type);


--
-- Name: subpopgty_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE subpopgty
    ADD CONSTRAINT subpopgty_pkey PRIMARY KEY (subpop_id, gty_id);


--
-- Name: subsnp_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE subsnp
    ADD CONSTRAINT subsnp_pkey PRIMARY KEY (subsnp_id);


--
-- Name: subsnp_top_or_bot_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE subsnp_top_or_bot
    ADD CONSTRAINT subsnp_top_or_bot_pkey PRIMARY KEY (subsnp_id);


--
-- Name: subsnpacc_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE subsnpacc
    ADD CONSTRAINT subsnpacc_pkey PRIMARY KEY (subsnp_id, acc_type_ind, acc_part);


--
-- Name: subsnpcommline_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE subsnpcommline
    ADD CONSTRAINT subsnpcommline_pkey PRIMARY KEY (subsnp_id, line_num);


--
-- Name: subsnplinkout_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE subsnplinkout
    ADD CONSTRAINT subsnplinkout_pkey PRIMARY KEY (subsnp_id, link_type);


--
-- Name: subsnpmdfailln_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE subsnpmdfailln
    ADD CONSTRAINT subsnpmdfailln_pkey PRIMARY KEY (subsnp_id, line_num);


--
-- Name: subsnpnovariseq_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE subsnpnovariseq
    ADD CONSTRAINT subsnpnovariseq_pkey PRIMARY KEY (subsnp_id, line_num);


--
-- Name: subsnppubmed_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE subsnppubmed
    ADD CONSTRAINT subsnppubmed_pkey PRIMARY KEY (subsnp_id, line_num);


--
-- Name: subsnpseq3_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE subsnpseq3
    ADD CONSTRAINT subsnpseq3_pkey PRIMARY KEY (subsnp_id, type, line_num);


--
-- Name: subsnpseq5_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE subsnpseq5
    ADD CONSTRAINT subsnpseq5_pkey PRIMARY KEY (subsnp_id, type, line_num);


--
-- Name: subsnpseqpos_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE subsnpseqpos
    ADD CONSTRAINT subsnpseqpos_pkey PRIMARY KEY (subsnp_id);


--
-- Name: synonym_pkey; Type: CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE synonym
    ADD CONSTRAINT synonym_pkey PRIMARY KEY (subsnp_id, type);



--
-- Name: allelefreqbysspop_subsnp_id_pop_id_allele_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX allelefreqbysspop_subsnp_id_pop_id_allele_id_idx ON allelefreqbysspop (subsnp_id, pop_id, allele_id);


--
-- Name: b150_maplink_snp_id_gi_offset_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX b150_maplink_snp_id_gi_offset_idx ON b150_maplink (snp_id, gi, "offset");


--
-- Name: b150_maplink_source_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX b150_maplink_source_idx ON b150_maplink (source);


--
-- Name: b150_maplinkinfo_gi_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX b150_maplinkinfo_gi_idx ON b150_maplinkinfo (gi);


--
-- Name: b150_proteininfo_gi_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX b150_proteininfo_gi_idx ON b150_proteininfo (gi);


--
-- Name: b150_snp_bitfield_link_prop_b2_snp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX b150_snp_bitfield_link_prop_b2_snp_id_idx ON b150_snp_bitfield (link_prop_b2, snp_id);


--
-- Name: b150_snpchrposonref_chr_pos_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX b150_snpchrposonref_chr_pos_idx ON b150_snpchrposonref (chr, pos);


--
-- Name: b150_snpchrposonref_snp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX b150_snpchrposonref_snp_id_idx ON b150_snpchrposonref (snp_id);


--
-- Name: b150_snpcontigloc_snp_id_ctg_id_asn_from_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX b150_snpcontigloc_snp_id_ctg_id_asn_from_idx ON b150_snpcontigloc (snp_id, ctg_id, asn_from);


--
-- Name: b150_snpcontiglocusid_snp_id_contig_acc_asn_from_locus_id_a_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX b150_snpcontiglocusid_snp_id_contig_acc_asn_from_locus_id_a_idx ON b150_snpcontiglocusid (snp_id, contig_acc, asn_from, locus_id, allele, mrna_start, mrna_gi);


--
-- Name: b150_snpcontiglocusid_snp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX b150_snpcontiglocusid_snp_id_idx ON b150_snpcontiglocusid (snp_id);


--
-- Name: b150_snphgvslink_snp_link_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX b150_snphgvslink_snp_link_idx ON b150_snphgvslink (snp_link);


--
-- Name: b150_snpmapinfo_snp_id_asm_acc_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX b150_snpmapinfo_snp_id_asm_acc_idx ON b150_snpmapinfo (snp_id, asm_acc);


--
-- Name: b150_snpmapinfo_snp_id_assembly_weight_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX b150_snpmapinfo_snp_id_assembly_weight_idx ON b150_snpmapinfo (snp_id, assembly, weight);


--
-- Name: batch_batch_type_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX batch_batch_type_idx ON batch (batch_type);


--
-- Name: batch_handle_loc_batch_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX batch_handle_loc_batch_id_idx ON batch (handle, loc_batch_id);


--
-- Name: batch_last_updated_time_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX batch_last_updated_time_idx ON batch (last_updated_time);


--
-- Name: batch_loc_batch_id_upp_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX batch_loc_batch_id_upp_idx ON batch (loc_batch_id_upp);


--
-- Name: batch_method_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX batch_method_id_idx ON batch (method_id);


--
-- Name: batch_pop_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX batch_pop_id_idx ON batch (pop_id);


--
-- Name: batch_submitted_time_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX batch_submitted_time_idx ON batch (submitted_time);


--
-- Name: batch_success_rate_int_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX batch_success_rate_int_idx ON batch (success_rate_int);


--
-- Name: batch_tax_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX batch_tax_id_idx ON batch (tax_id);


--
-- Name: batchcita_pub_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX batchcita_pub_id_idx ON batchcita (pub_id);


--
-- Name: batchcultivar_line_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX batchcultivar_line_idx ON batchcultivar (line);


--
-- Name: batchstrain_line_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX batchstrain_line_idx ON batchstrain (line);


--
-- Name: dn_ind_batch_pop_pop_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX dn_ind_batch_pop_pop_id_idx ON dn_ind_batch_pop (pop_id);


--
-- Name: freqsummarybysspop_last_updated_time_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX freqsummarybysspop_last_updated_time_idx ON freqsummarybysspop (last_updated_time);


--
-- Name: gtyfreqbysspop_subsnp_id_pop_id_unigty_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX gtyfreqbysspop_subsnp_id_pop_id_unigty_id_idx ON gtyfreqbysspop (subsnp_id, pop_id, unigty_id);


--
-- Name: pedigree_curator_curator_ped_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX pedigree_curator_curator_ped_id_idx ON pedigree (curator, curator_ped_id);


--
-- Name: popmandline_pop_id_line_num_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX popmandline_pop_id_line_num_idx ON popmandline (pop_id, line_num);


--
-- Name: population_handle_loc_pop_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX population_handle_loc_pop_id_idx ON population (handle, loc_pop_id);


--
-- Name: population_handle_loc_pop_id_upp_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX population_handle_loc_pop_id_upp_idx ON population (handle, loc_pop_id_upp);


--
-- Name: snp3d_snp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX snp3d_snp_id_idx ON snp3d (snp_id);


--
-- Name: snp_exemplar_subsnp_id_snp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX snp_exemplar_subsnp_id_snp_id_idx ON snp (exemplar_subsnp_id, snp_id);


--
-- Name: snp_snp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX snp_snp_id_idx ON snp (snp_id);


--
-- Name: snpcontigloc_ctgid_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX snpcontigloc_ctgid_idx ON b150_snpcontigloc (ctg_id);


--
-- Name: snphistory_history_create_time_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX snphistory_history_create_time_idx ON snphistory (history_create_time);


--
-- Name: snpsubsnplink_snp_id_subsnp_id_substrand_reversed_flag_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX snpsubsnplink_snp_id_subsnp_id_substrand_reversed_flag_idx ON snpsubsnplink (snp_id, subsnp_id, substrand_reversed_flag);


--
-- Name: snpsubsnplink_subsnp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX snpsubsnplink_subsnp_id_idx ON snpsubsnplink (subsnp_id);


--
-- Name: snpsubsnplinkhistory_build_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX snpsubsnplinkhistory_build_id_idx ON snpsubsnplinkhistory (build_id);


--
-- Name: snpsubsnplinkhistory_build_id_when_history_made_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX snpsubsnplinkhistory_build_id_when_history_made_idx ON snpsubsnplinkhistory (build_id_when_history_made);


--
-- Name: snpsubsnplinkhistory_snp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX snpsubsnplinkhistory_snp_id_idx ON snpsubsnplinkhistory (snp_id);


--
-- Name: snpval_snp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX snpval_snp_id_idx ON snpval (snp_id);


--
-- Name: subind_batch_id_subsnp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subind_batch_id_subsnp_id_idx ON subind (batch_id, subsnp_id);


--
-- Name: subind_batch_id_subsnp_id_submitted_ind_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subind_batch_id_subsnp_id_submitted_ind_id_idx ON subind (batch_id, subsnp_id, submitted_ind_id);


--
-- Name: subind_submitted_ind_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subind_submitted_ind_id_idx ON subind (submitted_ind_id);


--
-- Name: submittedindividual_ind_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX submittedindividual_ind_id_idx ON submittedindividual (ind_id);


--
-- Name: submittedindividual_submitted_ind_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX submittedindividual_submitted_ind_id_idx ON submittedindividual (submitted_ind_id);


--
-- Name: subpop_last_updated_time_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpop_last_updated_time_idx ON subpop (last_updated_time);


--
-- Name: subpop_pop_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpop_pop_id_idx ON subpop (pop_id);


--
-- Name: subpop_pop_id_subsnp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpop_pop_id_subsnp_id_idx ON subpop (pop_id, subsnp_id);


--
-- Name: subpop_subpop_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpop_subpop_id_idx ON subpop (subpop_id);


--
-- Name: subpop_subpop_id_idx1; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpop_subpop_id_idx1 ON subpop (subpop_id);


--
-- Name: subpop_subsnp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpop_subsnp_id_idx ON subpop (subsnp_id);


--
-- Name: subpop_subsnp_id_idx1; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpop_subsnp_id_idx1 ON subpop (subsnp_id);


--
-- Name: subpop_type_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpop_type_idx ON subpop (type);


--
-- Name: subpopallele_allele_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpopallele_allele_id_idx ON subpopallele (allele_id);


--
-- Name: subpopallele_batch_id_subsnp_id_pop_id_allele_other_type_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpopallele_batch_id_subsnp_id_pop_id_allele_other_type_idx ON subpopallele (batch_id, subsnp_id, pop_id, allele, other, type);


--
-- Name: subpopallele_freq_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpopallele_freq_idx ON subpopallele (freq);


--
-- Name: subpopallele_freq_max_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpopallele_freq_max_idx ON subpopallele (freq_max);


--
-- Name: subpopallele_freq_min_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpopallele_freq_min_idx ON subpopallele (freq_min);


--
-- Name: subpopallele_last_updated_time_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpopallele_last_updated_time_idx ON subpopallele (last_updated_time);


--
-- Name: subpopallele_subpop_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpopallele_subpop_id_idx ON subpopallele (subpop_id);


--
-- Name: subpopallele_subpop_id_type_allele_other_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpopallele_subpop_id_type_allele_other_idx ON subpopallele (subpop_id, type, allele, other);


--
-- Name: subpopallele_subsnp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpopallele_subsnp_id_idx ON subpopallele (subsnp_id);


--
-- Name: subpopallele_type_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpopallele_type_idx ON subpopallele (type);


--
-- Name: subpopgty_gty_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpopgty_gty_id_idx ON subpopgty (gty_id);


--
-- Name: subpopgty_subpop_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subpopgty_subpop_id_idx ON subpopgty (subpop_id);


--
-- Name: subsnp_batch_id_subsnp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subsnp_batch_id_subsnp_id_idx ON subsnp (batch_id, subsnp_id);


--
-- Name: subsnp_loc_snp_id_upp_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subsnp_loc_snp_id_upp_idx ON subsnp (loc_snp_id_upp);


--
-- Name: subsnp_snp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subsnp_snp_id_idx ON subsnp (snp_id);


--
-- Name: subsnp_subsnp_id_batch_id_loc_snp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subsnp_subsnp_id_batch_id_loc_snp_id_idx ON subsnp (subsnp_id, batch_id, loc_snp_id);


--
-- Name: subsnp_variation_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subsnp_variation_id_idx ON subsnp (variation_id);


--
-- Name: subsnpacc_acc_part_acc_type_ind_subsnp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subsnpacc_acc_part_acc_type_ind_subsnp_id_idx ON subsnpacc (acc_part, acc_type_ind, subsnp_id);


--
-- Name: subsnpmdfailln_subsnp_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subsnpmdfailln_subsnp_id_idx ON subsnpmdfailln (subsnp_id);


--
-- Name: subsnppubmed_pubmed_id_idx; Type: INDEX; Schema: dbsnp_chicken_9031; Owner: -
--

CREATE INDEX subsnppubmed_pubmed_id_idx ON subsnppubmed (pubmed_id);


--
-- Name: b150_snphgvslink_snp_fk; Type: FK CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE b150_snphgvslink
    ADD CONSTRAINT b150_snphgvslink_snp_fk FOREIGN KEY (snp_link) REFERENCES snp(snp_id);


--
-- Name: fk_b150_snpcontigloc_rs; Type: FK CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE b150_snpcontigloc
    ADD CONSTRAINT fk_b150_snpcontigloc_rs FOREIGN KEY (snp_id) REFERENCES snp(snp_id) MATCH FULL;


--
-- Name: fk_b150_snpmapinfo_rs; Type: FK CONSTRAINT; Schema: dbsnp_chicken_9031; Owner: -
--

ALTER TABLE b150_snpmapinfo
    ADD CONSTRAINT fk_b150_snpmapinfo_rs FOREIGN KEY (snp_id) REFERENCES snp(snp_id) MATCH FULL;


--
-- Name: dbsnp_shared; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA dbsnp_shared;

CREATE TABLE dbsnp_shared.obsvariation (
    var_id integer NOT NULL,
    pattern character varying(1024) NOT NULL,
    create_time timestamp without time zone NOT NULL,
    last_updated_time timestamp without time zone,
    univar_id integer,
    var_flag smallint,
    pattern_left character varying(900)
);

ALTER TABLE dbsnp_shared.obsvariation
    ADD CONSTRAINT dbsnp_shared.obsvariation_pkey PRIMARY KEY (var_id);


--
-- PostgreSQL database dump complete
--

