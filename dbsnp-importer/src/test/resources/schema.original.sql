--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.7
-- Dumped by pg_dump version 9.5.7

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: dbsnp_cow; Type: SCHEMA; Schema: -; Owner: dbsnp
--

CREATE SCHEMA dbsnp_cow;


ALTER SCHEMA dbsnp_cow OWNER TO dbsnp;

SET search_path = dbsnp_cow, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: allelefreqbysspop; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
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


ALTER TABLE allelefreqbysspop OWNER TO dbsnp;

--
-- Name: b148_contiginfo; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE b148_contiginfo (
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


ALTER TABLE b148_contiginfo OWNER TO dbsnp;

--
-- Name: b148_maplink; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE b148_maplink (
    snp_type character(2),
    snp_id integer,
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


ALTER TABLE b148_maplink OWNER TO dbsnp;

--
-- Name: b148_maplinkinfo; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE b148_maplinkinfo (
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


ALTER TABLE b148_maplinkinfo OWNER TO dbsnp;

--
-- Name: b148_proteininfo; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE b148_proteininfo (
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


ALTER TABLE b148_proteininfo OWNER TO dbsnp;

--
-- Name: b148_snp_bitfield; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE b148_snp_bitfield (
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
    upd_time timestamp without time zone NOT NULL,
    encoding bytea
);


ALTER TABLE b148_snp_bitfield OWNER TO dbsnp;

--
-- Name: b148_snpchrposonref; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE b148_snpchrposonref (
    snp_id integer,
    chr character varying(32),
    pos integer,
    orien smallint,
    neighbor_snp_list integer,
    ispar character varying(1)
);


ALTER TABLE b148_snpchrposonref OWNER TO dbsnp;

--
-- Name: b148_snpcontigloc; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE b148_snpcontigloc (
    snp_type character varying(2),
    snp_id integer,
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


ALTER TABLE b148_snpcontigloc OWNER TO dbsnp;

--
-- Name: b148_snpcontiglocusid; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE b148_snpcontiglocusid (
    snp_id integer,
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


ALTER TABLE b148_snpcontiglocusid OWNER TO dbsnp;

--
-- Name: b148_snpmapinfo; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE b148_snpmapinfo (
    snp_type character(2) NOT NULL,
    snp_id integer NOT NULL,
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


ALTER TABLE b148_snpmapinfo OWNER TO dbsnp;

--
-- Name: batch; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
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


ALTER TABLE batch OWNER TO dbsnp;

--
-- Name: batchcita; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE batchcita (
    batch_id integer NOT NULL,
    "position" integer NOT NULL,
    pub_id integer NOT NULL,
    citation character varying(255) NOT NULL,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone
);


ALTER TABLE batchcita OWNER TO dbsnp;

--
-- Name: batchcommline; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE batchcommline (
    batch_id integer NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone
);


ALTER TABLE batchcommline OWNER TO dbsnp;

--
-- Name: batchcultivar; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE batchcultivar (
    batch_id integer NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255),
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone
);


ALTER TABLE batchcultivar OWNER TO dbsnp;

--
-- Name: batchmeexline; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE batchmeexline (
    batch_id integer NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone
);


ALTER TABLE batchmeexline OWNER TO dbsnp;

--
-- Name: batchstrain; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE batchstrain (
    batch_id integer NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone
);


ALTER TABLE batchstrain OWNER TO dbsnp;

--
-- Name: batchvalcode; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE batchvalcode (
    batch_id integer NOT NULL,
    validation_status smallint NOT NULL
);


ALTER TABLE batchvalcode OWNER TO dbsnp;

--
-- Name: contact; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
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


ALTER TABLE contact OWNER TO dbsnp;

--
-- Name: dn_batchcount; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
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


ALTER TABLE dn_batchcount OWNER TO dbsnp;

--
-- Name: dn_handlecount; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE dn_handlecount (
    handle character varying(20) NOT NULL,
    batch_type character(3) NOT NULL,
    ss_cnt integer NOT NULL,
    rs_cnt integer,
    rs_validated_cnt integer,
    create_time timestamp without time zone NOT NULL
);


ALTER TABLE dn_handlecount OWNER TO dbsnp;

--
-- Name: dn_ind_batch_pop; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE dn_ind_batch_pop (
    batch_id smallint NOT NULL,
    pop_id integer NOT NULL,
    update_time timestamp without time zone NOT NULL
);


ALTER TABLE dn_ind_batch_pop OWNER TO dbsnp;

--
-- Name: dn_ind_batchcount; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE dn_ind_batchcount (
    batch_id integer NOT NULL,
    pop_id integer NOT NULL,
    ss_cnt integer NOT NULL,
    rs_cnt integer NOT NULL,
    ind_cnt integer NOT NULL,
    create_time timestamp without time zone NOT NULL
);


ALTER TABLE dn_ind_batchcount OWNER TO dbsnp;

--
-- Name: dn_populationindgrp; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE dn_populationindgrp (
    pop_id integer NOT NULL,
    ind_grp_name character varying(64) NOT NULL,
    ind_grp_code smallint NOT NULL
);


ALTER TABLE dn_populationindgrp OWNER TO dbsnp;

--
-- Name: dn_snpfxncnt; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
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


ALTER TABLE dn_snpfxncnt OWNER TO dbsnp;

--
-- Name: dn_table_rowcount; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
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


ALTER TABLE dn_table_rowcount OWNER TO dbsnp;

--
-- Name: freqsummarybysspop; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
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


ALTER TABLE freqsummarybysspop OWNER TO dbsnp;

--
-- Name: geneidtoname; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
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


ALTER TABLE geneidtoname OWNER TO dbsnp;

--
-- Name: gtyfreqbysspop; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
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


ALTER TABLE gtyfreqbysspop OWNER TO dbsnp;

--
-- Name: indgrpcode; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE indgrpcode (
    code smallint NOT NULL,
    name character varying(64) NOT NULL,
    descrip character varying(255) NOT NULL
);


ALTER TABLE indgrpcode OWNER TO dbsnp;

--
-- Name: indivbysource; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE indivbysource (
    ind_id integer NOT NULL,
    src_id integer NOT NULL,
    src_ind_id character varying(64) NOT NULL,
    create_time timestamp without time zone NOT NULL,
    src_ind_grp character varying(64)
);


ALTER TABLE indivbysource OWNER TO dbsnp;

--
-- Name: individual; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE individual (
    ind_id integer NOT NULL,
    descrip character varying(255),
    create_time timestamp without time zone NOT NULL,
    tax_id integer,
    ind_grp smallint
);


ALTER TABLE individual OWNER TO dbsnp;

--
-- Name: indivsourcecode; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE indivsourcecode (
    code integer NOT NULL,
    name character varying(22) NOT NULL,
    descrip character varying(255),
    create_time timestamp without time zone NOT NULL,
    src_type character varying(10),
    display_order smallint
);


ALTER TABLE indivsourcecode OWNER TO dbsnp;

--
-- Name: pedigree; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE pedigree (
    ped_id numeric NOT NULL,
    curator character varying(12) NOT NULL,
    curator_ped_id character varying(20) NOT NULL,
    create_time timestamp without time zone NOT NULL
);


ALTER TABLE pedigree OWNER TO dbsnp;

--
-- Name: pedigreeindividual; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE pedigreeindividual (
    ped_id numeric NOT NULL,
    ind_id integer NOT NULL,
    ma_ind_id integer,
    pa_ind_id integer,
    sex character(1),
    create_time timestamp without time zone NOT NULL
);


ALTER TABLE pedigreeindividual OWNER TO dbsnp;

--
-- Name: popline; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE popline (
    pop_id integer NOT NULL,
    line_num integer NOT NULL,
    line character varying(255) NOT NULL,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone
);


ALTER TABLE popline OWNER TO dbsnp;

--
-- Name: popmandline; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE popmandline (
    pop_id integer NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone
);


ALTER TABLE popmandline OWNER TO dbsnp;

--
-- Name: population; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
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


ALTER TABLE population OWNER TO dbsnp;

--
-- Name: rsmergearch; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE rsmergearch (
    rshigh integer NOT NULL,
    rslow integer NOT NULL,
    build_id integer,
    orien smallint NOT NULL,
    create_time timestamp without time zone NOT NULL,
    last_updated_time timestamp without time zone NOT NULL,
    rscurrent integer,
    orien2current smallint,
    comment character varying(255)
);


ALTER TABLE rsmergearch OWNER TO dbsnp;

--
-- Name: snp; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE snp (
    snp_id integer NOT NULL,
    avg_heterozygosity real,
    het_se real,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone,
    cpg_code smallint,
    tax_id integer,
    validation_status smallint,
    exemplar_subsnp_id integer NOT NULL,
    univar_id integer,
    cnt_subsnp integer,
    map_property smallint
);


ALTER TABLE snp OWNER TO dbsnp;

--
-- Name: snp3d; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE snp3d (
    snp_id integer NOT NULL,
    protein_acc character(20) NOT NULL,
    master_gi integer NOT NULL,
    neighbor_gi integer NOT NULL,
    aa_position integer NOT NULL,
    var_res character(1) NOT NULL,
    contig_res character(1) NOT NULL,
    neighbor_res character(1) NOT NULL,
    neighbor_pos integer NOT NULL,
    var_color integer NOT NULL,
    var_label integer NOT NULL
);


ALTER TABLE snp3d OWNER TO dbsnp;

--
-- Name: snp_bitfield; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
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
    upd_time timestamp without time zone NOT NULL,
    encoding bytea
);


ALTER TABLE snp_bitfield OWNER TO dbsnp;

--
-- Name: snpallelefreq; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE snpallelefreq (
    snp_id integer NOT NULL,
    allele_id integer NOT NULL,
    chr_cnt double precision,
    freq double precision,
    last_updated_time timestamp without time zone NOT NULL
);


ALTER TABLE snpallelefreq OWNER TO dbsnp;

--
-- Name: snpancestralallele; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE snpancestralallele (
    snp_id integer NOT NULL,
    ancestral_allele_id integer NOT NULL
);


ALTER TABLE snpancestralallele OWNER TO dbsnp;

--
-- Name: snpgtyfreq; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE snpgtyfreq (
    snp_id integer NOT NULL,
    unigty_id integer NOT NULL,
    ind_cnt double precision,
    freq double precision,
    last_updated_time timestamp without time zone NOT NULL
);


ALTER TABLE snpgtyfreq OWNER TO dbsnp;

--
-- Name: snphistory; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE snphistory (
    snp_id integer NOT NULL,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone NOT NULL,
    history_create_time timestamp without time zone,
    comment character varying(255),
    reactivated_time timestamp without time zone
);


ALTER TABLE snphistory OWNER TO dbsnp;

--
-- Name: snphwprob; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE snphwprob (
    snp_id integer NOT NULL,
    df smallint,
    chisq real,
    hwp real,
    ind_cnt smallint,
    last_updated_time timestamp without time zone
);


ALTER TABLE snphwprob OWNER TO dbsnp;

--
-- Name: snppubmed; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE snppubmed (
    snp_id integer NOT NULL,
    subsnp_id integer NOT NULL,
    pubmed_id integer NOT NULL,
    type character varying(16) NOT NULL,
    score integer NOT NULL,
    upd_date timestamp without time zone NOT NULL
);


ALTER TABLE snppubmed OWNER TO dbsnp;

--
-- Name: snpsubsnplink; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE snpsubsnplink (
    subsnp_id integer,
    snp_id integer,
    substrand_reversed_flag smallint,
    create_time timestamp without time zone,
    last_updated_time timestamp without time zone,
    build_id integer,
    comment character varying(255)
);


ALTER TABLE snpsubsnplink OWNER TO dbsnp;

--
-- Name: snpsubsnplinkhistory; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE snpsubsnplinkhistory (
    subsnp_id integer,
    snp_id integer,
    build_id integer,
    history_create_time timestamp without time zone NOT NULL,
    link_create_time timestamp without time zone,
    link_last_updated_time timestamp without time zone,
    orien smallint,
    build_id_when_history_made integer,
    comment character varying(255)
);


ALTER TABLE snpsubsnplinkhistory OWNER TO dbsnp;

--
-- Name: snpval; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE snpval (
    batch_id integer NOT NULL,
    snp_id integer NOT NULL
);


ALTER TABLE snpval OWNER TO dbsnp;

--
-- Name: subind; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
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


ALTER TABLE subind OWNER TO dbsnp;

--
-- Name: submittedindividual; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
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


ALTER TABLE submittedindividual OWNER TO dbsnp;

--
-- Name: subpop; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE subpop (
    batch_id integer NOT NULL,
    subsnp_id integer NOT NULL,
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


ALTER TABLE subpop OWNER TO dbsnp;

--
-- Name: subpopallele; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE subpopallele (
    batch_id integer NOT NULL,
    subsnp_id integer NOT NULL,
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


ALTER TABLE subpopallele OWNER TO dbsnp;

--
-- Name: subpopgty; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE subpopgty (
    subpop_id integer NOT NULL,
    gty_id integer NOT NULL,
    gty_str character varying(255),
    cnt real,
    freq real,
    last_updated_time timestamp without time zone NOT NULL
);


ALTER TABLE subpopgty OWNER TO dbsnp;

--
-- Name: subsnp; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE subsnp (
    subsnp_id integer NOT NULL,
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


ALTER TABLE subsnp OWNER TO dbsnp;

--
-- Name: subsnp_top_or_bot; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE subsnp_top_or_bot (
    subsnp_id integer NOT NULL,
    top_or_bot character(1),
    step smallint,
    last_updated_time timestamp without time zone
);


ALTER TABLE subsnp_top_or_bot OWNER TO dbsnp;

--
-- Name: subsnpacc; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE subsnpacc (
    subsnp_id integer NOT NULL,
    acc_type_ind character(1) NOT NULL,
    acc_part character varying(16) NOT NULL,
    acc_ver integer
);


ALTER TABLE subsnpacc OWNER TO dbsnp;

--
-- Name: subsnpcommline; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE subsnpcommline (
    subsnp_id integer NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL
);


ALTER TABLE subsnpcommline OWNER TO dbsnp;

--
-- Name: subsnplinkout; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE subsnplinkout (
    subsnp_id integer NOT NULL,
    url_val character varying(255) NOT NULL,
    updated_time timestamp without time zone,
    link_type character varying(3) DEFAULT 'NA'::character varying NOT NULL
);


ALTER TABLE subsnplinkout OWNER TO dbsnp;

--
-- Name: subsnpmdfailln; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE subsnpmdfailln (
    subsnp_id integer NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL
);


ALTER TABLE subsnpmdfailln OWNER TO dbsnp;

--
-- Name: subsnpnovariseq; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE subsnpnovariseq (
    subsnp_id integer NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL
);


ALTER TABLE subsnpnovariseq OWNER TO dbsnp;

--
-- Name: subsnppubmed; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE subsnppubmed (
    subsnp_id integer NOT NULL,
    line_num integer NOT NULL,
    pubmed_id integer NOT NULL,
    updated_time timestamp without time zone
);


ALTER TABLE subsnppubmed OWNER TO dbsnp;

--
-- Name: subsnpseq3; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE subsnpseq3 (
    subsnp_id integer NOT NULL,
    type smallint NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL
);


ALTER TABLE subsnpseq3 OWNER TO dbsnp;

--
-- Name: subsnpseq5; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE subsnpseq5 (
    subsnp_id integer NOT NULL,
    type smallint NOT NULL,
    line_num smallint NOT NULL,
    line character varying(255) NOT NULL
);


ALTER TABLE subsnpseq5 OWNER TO dbsnp;

--
-- Name: subsnpseqpos; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
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


ALTER TABLE subsnpseqpos OWNER TO dbsnp;

--
-- Name: synonym; Type: TABLE; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE TABLE synonym (
    subsnp_id integer NOT NULL,
    type character varying(64) NOT NULL,
    name character varying(64)
);


ALTER TABLE synonym OWNER TO dbsnp;

--
-- Name: b148_contiginfo_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY b148_contiginfo
    ADD CONSTRAINT b148_contiginfo_pkey PRIMARY KEY (contig_gi);


--
-- Name: b148_snp_bitfield_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY b148_snp_bitfield
    ADD CONSTRAINT b148_snp_bitfield_pkey PRIMARY KEY (snp_id);


--
-- Name: batch_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY batch
    ADD CONSTRAINT batch_pkey PRIMARY KEY (batch_id);


--
-- Name: batchcita_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY batchcita
    ADD CONSTRAINT batchcita_pkey PRIMARY KEY (batch_id, "position");


--
-- Name: batchcommline_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY batchcommline
    ADD CONSTRAINT batchcommline_pkey PRIMARY KEY (batch_id, line_num);


--
-- Name: batchcultivar_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY batchcultivar
    ADD CONSTRAINT batchcultivar_pkey PRIMARY KEY (batch_id, line_num);


--
-- Name: batchmeexline_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY batchmeexline
    ADD CONSTRAINT batchmeexline_pkey PRIMARY KEY (batch_id, line_num);


--
-- Name: batchstrain_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY batchstrain
    ADD CONSTRAINT batchstrain_pkey PRIMARY KEY (batch_id, line_num);


--
-- Name: batchvalcode_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY batchvalcode
    ADD CONSTRAINT batchvalcode_pkey PRIMARY KEY (batch_id);


--
-- Name: contact_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_pkey PRIMARY KEY (batch_id, handle);


--
-- Name: dn_batchcount_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY dn_batchcount
    ADD CONSTRAINT dn_batchcount_pkey PRIMARY KEY (batch_id);


--
-- Name: dn_handlecount_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY dn_handlecount
    ADD CONSTRAINT dn_handlecount_pkey PRIMARY KEY (handle, batch_type);


--
-- Name: dn_ind_batchcount_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY dn_ind_batchcount
    ADD CONSTRAINT dn_ind_batchcount_pkey PRIMARY KEY (batch_id, pop_id);


--
-- Name: dn_populationindgrp_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY dn_populationindgrp
    ADD CONSTRAINT dn_populationindgrp_pkey PRIMARY KEY (pop_id);


--
-- Name: freqsummarybysspop_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY freqsummarybysspop
    ADD CONSTRAINT freqsummarybysspop_pkey PRIMARY KEY (subsnp_id, pop_id);


--
-- Name: geneidtoname_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY geneidtoname
    ADD CONSTRAINT geneidtoname_pkey PRIMARY KEY (gene_id);


--
-- Name: indgrpcode_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY indgrpcode
    ADD CONSTRAINT indgrpcode_pkey PRIMARY KEY (code);


--
-- Name: indivbysource_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY indivbysource
    ADD CONSTRAINT indivbysource_pkey PRIMARY KEY (src_id, src_ind_id);


--
-- Name: individual_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY individual
    ADD CONSTRAINT individual_pkey PRIMARY KEY (ind_id);


--
-- Name: pedigree_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY pedigree
    ADD CONSTRAINT pedigree_pkey PRIMARY KEY (ped_id);


--
-- Name: pedigreeindividual_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY pedigreeindividual
    ADD CONSTRAINT pedigreeindividual_pkey PRIMARY KEY (ped_id, ind_id);


--
-- Name: popline_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY popline
    ADD CONSTRAINT popline_pkey PRIMARY KEY (pop_id, line_num);


--
-- Name: popmandline_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY popmandline
    ADD CONSTRAINT popmandline_pkey PRIMARY KEY (pop_id, line_num);


--
-- Name: population_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY population
    ADD CONSTRAINT population_pkey PRIMARY KEY (pop_id);


--
-- Name: rsmergearch_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY rsmergearch
    ADD CONSTRAINT rsmergearch_pkey PRIMARY KEY (rshigh);


--
-- Name: snp_pk; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY snp
    ADD CONSTRAINT snp_pk PRIMARY KEY (snp_id);


--
-- Name: snpallelefreq_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY snpallelefreq
    ADD CONSTRAINT snpallelefreq_pkey PRIMARY KEY (snp_id, allele_id);


--
-- Name: snpancestralallele_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY snpancestralallele
    ADD CONSTRAINT snpancestralallele_pkey PRIMARY KEY (snp_id);


--
-- Name: snpgtyfreq_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY snpgtyfreq
    ADD CONSTRAINT snpgtyfreq_pkey PRIMARY KEY (snp_id, unigty_id);


--
-- Name: snphistory_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY snphistory
    ADD CONSTRAINT snphistory_pkey PRIMARY KEY (snp_id);


--
-- Name: snphwprob_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY snphwprob
    ADD CONSTRAINT snphwprob_pkey PRIMARY KEY (snp_id);


--
-- Name: snpval_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY snpval
    ADD CONSTRAINT snpval_pkey PRIMARY KEY (batch_id, snp_id);


--
-- Name: submittedindividual_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY submittedindividual
    ADD CONSTRAINT submittedindividual_pkey PRIMARY KEY (pop_id, loc_ind_id_upp);


--
-- Name: subpop_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY subpop
    ADD CONSTRAINT subpop_pkey PRIMARY KEY (batch_id, subsnp_id, pop_id, type);


--
-- Name: subpopgty_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY subpopgty
    ADD CONSTRAINT subpopgty_pkey PRIMARY KEY (subpop_id, gty_id);


--
-- Name: subsnp_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY subsnp
    ADD CONSTRAINT subsnp_pkey PRIMARY KEY (subsnp_id);


--
-- Name: subsnp_top_or_bot_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY subsnp_top_or_bot
    ADD CONSTRAINT subsnp_top_or_bot_pkey PRIMARY KEY (subsnp_id);


--
-- Name: subsnpacc_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY subsnpacc
    ADD CONSTRAINT subsnpacc_pkey PRIMARY KEY (subsnp_id, acc_type_ind, acc_part);


--
-- Name: subsnpcommline_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY subsnpcommline
    ADD CONSTRAINT subsnpcommline_pkey PRIMARY KEY (subsnp_id, line_num);


--
-- Name: subsnplinkout_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY subsnplinkout
    ADD CONSTRAINT subsnplinkout_pkey PRIMARY KEY (subsnp_id, link_type);


--
-- Name: subsnpmdfailln_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY subsnpmdfailln
    ADD CONSTRAINT subsnpmdfailln_pkey PRIMARY KEY (subsnp_id, line_num);


--
-- Name: subsnpnovariseq_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY subsnpnovariseq
    ADD CONSTRAINT subsnpnovariseq_pkey PRIMARY KEY (subsnp_id, line_num);


--
-- Name: subsnppubmed_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY subsnppubmed
    ADD CONSTRAINT subsnppubmed_pkey PRIMARY KEY (subsnp_id, line_num);


--
-- Name: subsnpseq3_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY subsnpseq3
    ADD CONSTRAINT subsnpseq3_pkey PRIMARY KEY (subsnp_id, type, line_num);


--
-- Name: subsnpseq5_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY subsnpseq5
    ADD CONSTRAINT subsnpseq5_pkey PRIMARY KEY (subsnp_id, type, line_num);


--
-- Name: subsnpseqpos_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY subsnpseqpos
    ADD CONSTRAINT subsnpseqpos_pkey PRIMARY KEY (subsnp_id);


--
-- Name: synonym_pkey; Type: CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY synonym
    ADD CONSTRAINT synonym_pkey PRIMARY KEY (subsnp_id, type);


--
-- Name: allelefreqbysspop_subsnp_id_pop_id_allele_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX allelefreqbysspop_subsnp_id_pop_id_allele_id_idx ON allelefreqbysspop USING btree (subsnp_id, pop_id, allele_id);


--
-- Name: b148_maplink_snp_id_gi_offset_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX b148_maplink_snp_id_gi_offset_idx ON b148_maplink USING btree (snp_id, gi, "offset");


--
-- Name: b148_maplink_source_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX b148_maplink_source_idx ON b148_maplink USING btree (source);


--
-- Name: b148_maplinkinfo_gi_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX b148_maplinkinfo_gi_idx ON b148_maplinkinfo USING btree (gi);


--
-- Name: b148_proteininfo_gi_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX b148_proteininfo_gi_idx ON b148_proteininfo USING btree (gi);


--
-- Name: b148_snp_bitfield_link_prop_b2_snp_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX b148_snp_bitfield_link_prop_b2_snp_id_idx ON b148_snp_bitfield USING btree (link_prop_b2, snp_id);


--
-- Name: b148_snpchrposonref_chr_pos_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX b148_snpchrposonref_chr_pos_idx ON b148_snpchrposonref USING btree (chr, pos);


--
-- Name: b148_snpchrposonref_snp_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX b148_snpchrposonref_snp_id_idx ON b148_snpchrposonref USING btree (snp_id);


--
-- Name: b148_snpcontigloc_snp_id_ctg_id_asn_from_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX b148_snpcontigloc_snp_id_ctg_id_asn_from_idx ON b148_snpcontigloc USING btree (snp_id, ctg_id, asn_from);


--
-- Name: b148_snpcontiglocusid_snp_id_contig_acc_asn_from_locus_id_a_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX b148_snpcontiglocusid_snp_id_contig_acc_asn_from_locus_id_a_idx ON b148_snpcontiglocusid USING btree (snp_id, contig_acc, asn_from, locus_id, allele, mrna_start, mrna_gi);


--
-- Name: b148_snpcontiglocusid_snp_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX b148_snpcontiglocusid_snp_id_idx ON b148_snpcontiglocusid USING btree (snp_id);


--
-- Name: b148_snpmapinfo_snp_id_asm_acc_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX b148_snpmapinfo_snp_id_asm_acc_idx ON b148_snpmapinfo USING btree (snp_id, asm_acc);


--
-- Name: b148_snpmapinfo_snp_id_assembly_weight_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX b148_snpmapinfo_snp_id_assembly_weight_idx ON b148_snpmapinfo USING btree (snp_id, assembly, weight);


--
-- Name: batch_batch_type_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX batch_batch_type_idx ON batch USING btree (batch_type);


--
-- Name: batch_handle_loc_batch_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX batch_handle_loc_batch_id_idx ON batch USING btree (handle, loc_batch_id);


--
-- Name: batch_last_updated_time_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX batch_last_updated_time_idx ON batch USING btree (last_updated_time);


--
-- Name: batch_loc_batch_id_upp_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX batch_loc_batch_id_upp_idx ON batch USING btree (loc_batch_id_upp);


--
-- Name: batch_method_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX batch_method_id_idx ON batch USING btree (method_id);


--
-- Name: batch_pop_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX batch_pop_id_idx ON batch USING btree (pop_id);


--
-- Name: batch_submitted_time_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX batch_submitted_time_idx ON batch USING btree (submitted_time);


--
-- Name: batch_success_rate_int_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX batch_success_rate_int_idx ON batch USING btree (success_rate_int);


--
-- Name: batch_tax_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX batch_tax_id_idx ON batch USING btree (tax_id);


--
-- Name: batchcita_pub_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX batchcita_pub_id_idx ON batchcita USING btree (pub_id);


--
-- Name: batchcultivar_line_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX batchcultivar_line_idx ON batchcultivar USING btree (line);


--
-- Name: batchstrain_line_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX batchstrain_line_idx ON batchstrain USING btree (line);


--
-- Name: dn_ind_batch_pop_pop_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX dn_ind_batch_pop_pop_id_idx ON dn_ind_batch_pop USING btree (pop_id);


--
-- Name: freqsummarybysspop_last_updated_time_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX freqsummarybysspop_last_updated_time_idx ON freqsummarybysspop USING btree (last_updated_time);


--
-- Name: gtyfreqbysspop_subsnp_id_pop_id_unigty_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX gtyfreqbysspop_subsnp_id_pop_id_unigty_id_idx ON gtyfreqbysspop USING btree (subsnp_id, pop_id, unigty_id);


--
-- Name: pedigree_curator_curator_ped_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX pedigree_curator_curator_ped_id_idx ON pedigree USING btree (curator, curator_ped_id);


--
-- Name: popmandline_pop_id_line_num_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX popmandline_pop_id_line_num_idx ON popmandline USING btree (pop_id, line_num);


--
-- Name: population_handle_loc_pop_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX population_handle_loc_pop_id_idx ON population USING btree (handle, loc_pop_id);


--
-- Name: population_handle_loc_pop_id_upp_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX population_handle_loc_pop_id_upp_idx ON population USING btree (handle, loc_pop_id_upp);


--
-- Name: snp_exemplar_subsnp_id_snp_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX snp_exemplar_subsnp_id_snp_id_idx ON snp USING btree (exemplar_subsnp_id, snp_id);


--
-- Name: snp_snp_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX snp_snp_id_idx ON snp USING btree (snp_id);


--
-- Name: snphistory_history_create_time_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX snphistory_history_create_time_idx ON snphistory USING btree (history_create_time);


--
-- Name: snpsubsnplink_snp_id_subsnp_id_substrand_reversed_flag_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX snpsubsnplink_snp_id_subsnp_id_substrand_reversed_flag_idx ON snpsubsnplink USING btree (snp_id, subsnp_id, substrand_reversed_flag);


--
-- Name: snpsubsnplink_subsnp_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX snpsubsnplink_subsnp_id_idx ON snpsubsnplink USING btree (subsnp_id);


--
-- Name: snpsubsnplinkhistory_build_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX snpsubsnplinkhistory_build_id_idx ON snpsubsnplinkhistory USING btree (build_id);


--
-- Name: snpsubsnplinkhistory_build_id_when_history_made_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX snpsubsnplinkhistory_build_id_when_history_made_idx ON snpsubsnplinkhistory USING btree (build_id_when_history_made);


--
-- Name: snpsubsnplinkhistory_snp_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX snpsubsnplinkhistory_snp_id_idx ON snpsubsnplinkhistory USING btree (snp_id);


--
-- Name: snpval_snp_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX snpval_snp_id_idx ON snpval USING btree (snp_id);


--
-- Name: subind_batch_id_subsnp_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subind_batch_id_subsnp_id_idx ON subind USING btree (batch_id, subsnp_id);


--
-- Name: subind_batch_id_subsnp_id_submitted_ind_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subind_batch_id_subsnp_id_submitted_ind_id_idx ON subind USING btree (batch_id, subsnp_id, submitted_ind_id);


--
-- Name: submittedindividual_ind_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX submittedindividual_ind_id_idx ON submittedindividual USING btree (ind_id);


--
-- Name: submittedindividual_submitted_ind_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX submittedindividual_submitted_ind_id_idx ON submittedindividual USING btree (submitted_ind_id);


--
-- Name: subpopallele_allele_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subpopallele_allele_id_idx ON subpopallele USING btree (allele_id);


--
-- Name: subpopallele_batch_id_subsnp_id_pop_id_allele_other_type_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subpopallele_batch_id_subsnp_id_pop_id_allele_other_type_idx ON subpopallele USING btree (batch_id, subsnp_id, pop_id, allele, other, type);


--
-- Name: subpopallele_freq_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subpopallele_freq_idx ON subpopallele USING btree (freq);


--
-- Name: subpopallele_freq_max_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subpopallele_freq_max_idx ON subpopallele USING btree (freq_max);


--
-- Name: subpopallele_freq_min_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subpopallele_freq_min_idx ON subpopallele USING btree (freq_min);


--
-- Name: subpopallele_last_updated_time_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subpopallele_last_updated_time_idx ON subpopallele USING btree (last_updated_time);


--
-- Name: subpopallele_subpop_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subpopallele_subpop_id_idx ON subpopallele USING btree (subpop_id);


--
-- Name: subpopallele_subpop_id_type_allele_other_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subpopallele_subpop_id_type_allele_other_idx ON subpopallele USING btree (subpop_id, type, allele, other);


--
-- Name: subpopallele_subsnp_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subpopallele_subsnp_id_idx ON subpopallele USING btree (subsnp_id);


--
-- Name: subpopallele_type_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subpopallele_type_idx ON subpopallele USING btree (type);


--
-- Name: subpopgty_gty_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subpopgty_gty_id_idx ON subpopgty USING btree (gty_id);


--
-- Name: subpopgty_subpop_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subpopgty_subpop_id_idx ON subpopgty USING btree (subpop_id);


--
-- Name: subsnp_batch_id_subsnp_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subsnp_batch_id_subsnp_id_idx ON subsnp USING btree (batch_id, subsnp_id);


--
-- Name: subsnp_loc_snp_id_upp_subsnp_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subsnp_loc_snp_id_upp_subsnp_id_idx ON subsnp USING btree (loc_snp_id_upp, subsnp_id);


--
-- Name: subsnp_subsnp_id_batch_id_loc_snp_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subsnp_subsnp_id_batch_id_loc_snp_id_idx ON subsnp USING btree (subsnp_id, batch_id, loc_snp_id);


--
-- Name: subsnpacc_acc_part_acc_type_ind_subsnp_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subsnpacc_acc_part_acc_type_ind_subsnp_id_idx ON subsnpacc USING btree (acc_part, acc_type_ind, subsnp_id);


--
-- Name: subsnpmdfailln_subsnp_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subsnpmdfailln_subsnp_id_idx ON subsnpmdfailln USING btree (subsnp_id);


--
-- Name: subsnppubmed_pubmed_id_idx; Type: INDEX; Schema: dbsnp_cow; Owner: dbsnp
--

CREATE INDEX subsnppubmed_pubmed_id_idx ON subsnppubmed USING btree (pubmed_id);


--
-- Name: fk_b148_snpcontigloc_rs; Type: FK CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY b148_snpcontigloc
    ADD CONSTRAINT fk_b148_snpcontigloc_rs FOREIGN KEY (snp_id) REFERENCES snp(snp_id) MATCH FULL;


--
-- Name: fk_b148_snpmapinfo_rs; Type: FK CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY b148_snpmapinfo
    ADD CONSTRAINT fk_b148_snpmapinfo_rs FOREIGN KEY (snp_id) REFERENCES snp(snp_id) MATCH FULL;


--
-- Name: fk_subpop_batch_id; Type: FK CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY subpop
    ADD CONSTRAINT fk_subpop_batch_id FOREIGN KEY (batch_id) REFERENCES batch(batch_id) MATCH FULL;


--
-- Name: fk_subpop_pop; Type: FK CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY subpop
    ADD CONSTRAINT fk_subpop_pop FOREIGN KEY (pop_id) REFERENCES population(pop_id) MATCH FULL;


--
-- Name: fk_subpopallele_bid_ss_pop_type; Type: FK CONSTRAINT; Schema: dbsnp_cow; Owner: dbsnp
--

ALTER TABLE ONLY subpopallele
    ADD CONSTRAINT fk_subpopallele_bid_ss_pop_type FOREIGN KEY (batch_id, subsnp_id, pop_id, type) REFERENCES subpop(batch_id, subsnp_id, pop_id, type) MATCH FULL;


--
-- Name: dbsnp_cow; Type: ACL; Schema: -; Owner: dbsnp
--

REVOKE ALL ON SCHEMA dbsnp_cow FROM PUBLIC;
REVOKE ALL ON SCHEMA dbsnp_cow FROM dbsnp;
GRANT ALL ON SCHEMA dbsnp_cow TO dbsnp;
GRANT USAGE ON SCHEMA dbsnp_cow TO dbsnp_ro;


--
-- Name: allelefreqbysspop; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE allelefreqbysspop FROM PUBLIC;
REVOKE ALL ON TABLE allelefreqbysspop FROM dbsnp;
GRANT ALL ON TABLE allelefreqbysspop TO dbsnp;
GRANT SELECT ON TABLE allelefreqbysspop TO dbsnp_ro;


--
-- Name: b148_contiginfo; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE b148_contiginfo FROM PUBLIC;
REVOKE ALL ON TABLE b148_contiginfo FROM dbsnp;
GRANT ALL ON TABLE b148_contiginfo TO dbsnp;
GRANT SELECT ON TABLE b148_contiginfo TO dbsnp_ro;


--
-- Name: b148_maplink; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE b148_maplink FROM PUBLIC;
REVOKE ALL ON TABLE b148_maplink FROM dbsnp;
GRANT ALL ON TABLE b148_maplink TO dbsnp;
GRANT SELECT ON TABLE b148_maplink TO dbsnp_ro;


--
-- Name: b148_maplinkinfo; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE b148_maplinkinfo FROM PUBLIC;
REVOKE ALL ON TABLE b148_maplinkinfo FROM dbsnp;
GRANT ALL ON TABLE b148_maplinkinfo TO dbsnp;
GRANT SELECT ON TABLE b148_maplinkinfo TO dbsnp_ro;


--
-- Name: b148_proteininfo; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE b148_proteininfo FROM PUBLIC;
REVOKE ALL ON TABLE b148_proteininfo FROM dbsnp;
GRANT ALL ON TABLE b148_proteininfo TO dbsnp;
GRANT SELECT ON TABLE b148_proteininfo TO dbsnp_ro;


--
-- Name: b148_snp_bitfield; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE b148_snp_bitfield FROM PUBLIC;
REVOKE ALL ON TABLE b148_snp_bitfield FROM dbsnp;
GRANT ALL ON TABLE b148_snp_bitfield TO dbsnp;
GRANT SELECT ON TABLE b148_snp_bitfield TO dbsnp_ro;


--
-- Name: b148_snpchrposonref; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE b148_snpchrposonref FROM PUBLIC;
REVOKE ALL ON TABLE b148_snpchrposonref FROM dbsnp;
GRANT ALL ON TABLE b148_snpchrposonref TO dbsnp;
GRANT SELECT ON TABLE b148_snpchrposonref TO dbsnp_ro;


--
-- Name: b148_snpcontigloc; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE b148_snpcontigloc FROM PUBLIC;
REVOKE ALL ON TABLE b148_snpcontigloc FROM dbsnp;
GRANT ALL ON TABLE b148_snpcontigloc TO dbsnp;
GRANT SELECT ON TABLE b148_snpcontigloc TO dbsnp_ro;


--
-- Name: b148_snpcontiglocusid; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE b148_snpcontiglocusid FROM PUBLIC;
REVOKE ALL ON TABLE b148_snpcontiglocusid FROM dbsnp;
GRANT ALL ON TABLE b148_snpcontiglocusid TO dbsnp;
GRANT SELECT ON TABLE b148_snpcontiglocusid TO dbsnp_ro;


--
-- Name: b148_snpmapinfo; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE b148_snpmapinfo FROM PUBLIC;
REVOKE ALL ON TABLE b148_snpmapinfo FROM dbsnp;
GRANT ALL ON TABLE b148_snpmapinfo TO dbsnp;
GRANT SELECT ON TABLE b148_snpmapinfo TO dbsnp_ro;


--
-- Name: batch; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE batch FROM PUBLIC;
REVOKE ALL ON TABLE batch FROM dbsnp;
GRANT ALL ON TABLE batch TO dbsnp;
GRANT SELECT ON TABLE batch TO dbsnp_ro;


--
-- Name: batchcita; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE batchcita FROM PUBLIC;
REVOKE ALL ON TABLE batchcita FROM dbsnp;
GRANT ALL ON TABLE batchcita TO dbsnp;
GRANT SELECT ON TABLE batchcita TO dbsnp_ro;


--
-- Name: batchcommline; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE batchcommline FROM PUBLIC;
REVOKE ALL ON TABLE batchcommline FROM dbsnp;
GRANT ALL ON TABLE batchcommline TO dbsnp;
GRANT SELECT ON TABLE batchcommline TO dbsnp_ro;


--
-- Name: batchcultivar; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE batchcultivar FROM PUBLIC;
REVOKE ALL ON TABLE batchcultivar FROM dbsnp;
GRANT ALL ON TABLE batchcultivar TO dbsnp;
GRANT SELECT ON TABLE batchcultivar TO dbsnp_ro;


--
-- Name: batchmeexline; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE batchmeexline FROM PUBLIC;
REVOKE ALL ON TABLE batchmeexline FROM dbsnp;
GRANT ALL ON TABLE batchmeexline TO dbsnp;
GRANT SELECT ON TABLE batchmeexline TO dbsnp_ro;


--
-- Name: batchstrain; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE batchstrain FROM PUBLIC;
REVOKE ALL ON TABLE batchstrain FROM dbsnp;
GRANT ALL ON TABLE batchstrain TO dbsnp;
GRANT SELECT ON TABLE batchstrain TO dbsnp_ro;


--
-- Name: batchvalcode; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE batchvalcode FROM PUBLIC;
REVOKE ALL ON TABLE batchvalcode FROM dbsnp;
GRANT ALL ON TABLE batchvalcode TO dbsnp;
GRANT SELECT ON TABLE batchvalcode TO dbsnp_ro;


--
-- Name: contact; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE contact FROM PUBLIC;
REVOKE ALL ON TABLE contact FROM dbsnp;
GRANT ALL ON TABLE contact TO dbsnp;
GRANT SELECT ON TABLE contact TO dbsnp_ro;


--
-- Name: dn_batchcount; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE dn_batchcount FROM PUBLIC;
REVOKE ALL ON TABLE dn_batchcount FROM dbsnp;
GRANT ALL ON TABLE dn_batchcount TO dbsnp;
GRANT SELECT ON TABLE dn_batchcount TO dbsnp_ro;


--
-- Name: dn_handlecount; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE dn_handlecount FROM PUBLIC;
REVOKE ALL ON TABLE dn_handlecount FROM dbsnp;
GRANT ALL ON TABLE dn_handlecount TO dbsnp;
GRANT SELECT ON TABLE dn_handlecount TO dbsnp_ro;


--
-- Name: dn_ind_batch_pop; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE dn_ind_batch_pop FROM PUBLIC;
REVOKE ALL ON TABLE dn_ind_batch_pop FROM dbsnp;
GRANT ALL ON TABLE dn_ind_batch_pop TO dbsnp;
GRANT SELECT ON TABLE dn_ind_batch_pop TO dbsnp_ro;


--
-- Name: dn_ind_batchcount; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE dn_ind_batchcount FROM PUBLIC;
REVOKE ALL ON TABLE dn_ind_batchcount FROM dbsnp;
GRANT ALL ON TABLE dn_ind_batchcount TO dbsnp;
GRANT SELECT ON TABLE dn_ind_batchcount TO dbsnp_ro;


--
-- Name: dn_populationindgrp; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE dn_populationindgrp FROM PUBLIC;
REVOKE ALL ON TABLE dn_populationindgrp FROM dbsnp;
GRANT ALL ON TABLE dn_populationindgrp TO dbsnp;
GRANT SELECT ON TABLE dn_populationindgrp TO dbsnp_ro;


--
-- Name: dn_snpfxncnt; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE dn_snpfxncnt FROM PUBLIC;
REVOKE ALL ON TABLE dn_snpfxncnt FROM dbsnp;
GRANT ALL ON TABLE dn_snpfxncnt TO dbsnp;
GRANT SELECT ON TABLE dn_snpfxncnt TO dbsnp_ro;


--
-- Name: dn_table_rowcount; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE dn_table_rowcount FROM PUBLIC;
REVOKE ALL ON TABLE dn_table_rowcount FROM dbsnp;
GRANT ALL ON TABLE dn_table_rowcount TO dbsnp;
GRANT SELECT ON TABLE dn_table_rowcount TO dbsnp_ro;


--
-- Name: freqsummarybysspop; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE freqsummarybysspop FROM PUBLIC;
REVOKE ALL ON TABLE freqsummarybysspop FROM dbsnp;
GRANT ALL ON TABLE freqsummarybysspop TO dbsnp;
GRANT SELECT ON TABLE freqsummarybysspop TO dbsnp_ro;


--
-- Name: geneidtoname; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE geneidtoname FROM PUBLIC;
REVOKE ALL ON TABLE geneidtoname FROM dbsnp;
GRANT ALL ON TABLE geneidtoname TO dbsnp;
GRANT SELECT ON TABLE geneidtoname TO dbsnp_ro;


--
-- Name: gtyfreqbysspop; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE gtyfreqbysspop FROM PUBLIC;
REVOKE ALL ON TABLE gtyfreqbysspop FROM dbsnp;
GRANT ALL ON TABLE gtyfreqbysspop TO dbsnp;
GRANT SELECT ON TABLE gtyfreqbysspop TO dbsnp_ro;


--
-- Name: indgrpcode; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE indgrpcode FROM PUBLIC;
REVOKE ALL ON TABLE indgrpcode FROM dbsnp;
GRANT ALL ON TABLE indgrpcode TO dbsnp;
GRANT SELECT ON TABLE indgrpcode TO dbsnp_ro;


--
-- Name: indivbysource; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE indivbysource FROM PUBLIC;
REVOKE ALL ON TABLE indivbysource FROM dbsnp;
GRANT ALL ON TABLE indivbysource TO dbsnp;
GRANT SELECT ON TABLE indivbysource TO dbsnp_ro;


--
-- Name: individual; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE individual FROM PUBLIC;
REVOKE ALL ON TABLE individual FROM dbsnp;
GRANT ALL ON TABLE individual TO dbsnp;
GRANT SELECT ON TABLE individual TO dbsnp_ro;


--
-- Name: indivsourcecode; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE indivsourcecode FROM PUBLIC;
REVOKE ALL ON TABLE indivsourcecode FROM dbsnp;
GRANT ALL ON TABLE indivsourcecode TO dbsnp;
GRANT SELECT ON TABLE indivsourcecode TO dbsnp_ro;


--
-- Name: pedigree; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE pedigree FROM PUBLIC;
REVOKE ALL ON TABLE pedigree FROM dbsnp;
GRANT ALL ON TABLE pedigree TO dbsnp;
GRANT SELECT ON TABLE pedigree TO dbsnp_ro;


--
-- Name: pedigreeindividual; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE pedigreeindividual FROM PUBLIC;
REVOKE ALL ON TABLE pedigreeindividual FROM dbsnp;
GRANT ALL ON TABLE pedigreeindividual TO dbsnp;
GRANT SELECT ON TABLE pedigreeindividual TO dbsnp_ro;


--
-- Name: popline; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE popline FROM PUBLIC;
REVOKE ALL ON TABLE popline FROM dbsnp;
GRANT ALL ON TABLE popline TO dbsnp;
GRANT SELECT ON TABLE popline TO dbsnp_ro;


--
-- Name: popmandline; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE popmandline FROM PUBLIC;
REVOKE ALL ON TABLE popmandline FROM dbsnp;
GRANT ALL ON TABLE popmandline TO dbsnp;
GRANT SELECT ON TABLE popmandline TO dbsnp_ro;


--
-- Name: population; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE population FROM PUBLIC;
REVOKE ALL ON TABLE population FROM dbsnp;
GRANT ALL ON TABLE population TO dbsnp;
GRANT SELECT ON TABLE population TO dbsnp_ro;


--
-- Name: rsmergearch; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE rsmergearch FROM PUBLIC;
REVOKE ALL ON TABLE rsmergearch FROM dbsnp;
GRANT ALL ON TABLE rsmergearch TO dbsnp;
GRANT SELECT ON TABLE rsmergearch TO dbsnp_ro;


--
-- Name: snp; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE snp FROM PUBLIC;
REVOKE ALL ON TABLE snp FROM dbsnp;
GRANT ALL ON TABLE snp TO dbsnp;
GRANT SELECT ON TABLE snp TO dbsnp_ro;


--
-- Name: snp3d; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE snp3d FROM PUBLIC;
REVOKE ALL ON TABLE snp3d FROM dbsnp;
GRANT ALL ON TABLE snp3d TO dbsnp;
GRANT SELECT ON TABLE snp3d TO dbsnp_ro;


--
-- Name: snp_bitfield; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE snp_bitfield FROM PUBLIC;
REVOKE ALL ON TABLE snp_bitfield FROM dbsnp;
GRANT ALL ON TABLE snp_bitfield TO dbsnp;
GRANT SELECT ON TABLE snp_bitfield TO dbsnp_ro;


--
-- Name: snpallelefreq; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE snpallelefreq FROM PUBLIC;
REVOKE ALL ON TABLE snpallelefreq FROM dbsnp;
GRANT ALL ON TABLE snpallelefreq TO dbsnp;
GRANT SELECT ON TABLE snpallelefreq TO dbsnp_ro;


--
-- Name: snpancestralallele; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE snpancestralallele FROM PUBLIC;
REVOKE ALL ON TABLE snpancestralallele FROM dbsnp;
GRANT ALL ON TABLE snpancestralallele TO dbsnp;
GRANT SELECT ON TABLE snpancestralallele TO dbsnp_ro;


--
-- Name: snpgtyfreq; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE snpgtyfreq FROM PUBLIC;
REVOKE ALL ON TABLE snpgtyfreq FROM dbsnp;
GRANT ALL ON TABLE snpgtyfreq TO dbsnp;
GRANT SELECT ON TABLE snpgtyfreq TO dbsnp_ro;


--
-- Name: snphistory; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE snphistory FROM PUBLIC;
REVOKE ALL ON TABLE snphistory FROM dbsnp;
GRANT ALL ON TABLE snphistory TO dbsnp;
GRANT SELECT ON TABLE snphistory TO dbsnp_ro;


--
-- Name: snphwprob; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE snphwprob FROM PUBLIC;
REVOKE ALL ON TABLE snphwprob FROM dbsnp;
GRANT ALL ON TABLE snphwprob TO dbsnp;
GRANT SELECT ON TABLE snphwprob TO dbsnp_ro;


--
-- Name: snppubmed; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE snppubmed FROM PUBLIC;
REVOKE ALL ON TABLE snppubmed FROM dbsnp;
GRANT ALL ON TABLE snppubmed TO dbsnp;
GRANT SELECT ON TABLE snppubmed TO dbsnp_ro;


--
-- Name: snpsubsnplink; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE snpsubsnplink FROM PUBLIC;
REVOKE ALL ON TABLE snpsubsnplink FROM dbsnp;
GRANT ALL ON TABLE snpsubsnplink TO dbsnp;
GRANT SELECT ON TABLE snpsubsnplink TO dbsnp_ro;


--
-- Name: snpsubsnplinkhistory; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE snpsubsnplinkhistory FROM PUBLIC;
REVOKE ALL ON TABLE snpsubsnplinkhistory FROM dbsnp;
GRANT ALL ON TABLE snpsubsnplinkhistory TO dbsnp;
GRANT SELECT ON TABLE snpsubsnplinkhistory TO dbsnp_ro;


--
-- Name: snpval; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE snpval FROM PUBLIC;
REVOKE ALL ON TABLE snpval FROM dbsnp;
GRANT ALL ON TABLE snpval TO dbsnp;
GRANT SELECT ON TABLE snpval TO dbsnp_ro;


--
-- Name: subind; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE subind FROM PUBLIC;
REVOKE ALL ON TABLE subind FROM dbsnp;
GRANT ALL ON TABLE subind TO dbsnp;
GRANT SELECT ON TABLE subind TO dbsnp_ro;


--
-- Name: submittedindividual; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE submittedindividual FROM PUBLIC;
REVOKE ALL ON TABLE submittedindividual FROM dbsnp;
GRANT ALL ON TABLE submittedindividual TO dbsnp;
GRANT SELECT ON TABLE submittedindividual TO dbsnp_ro;


--
-- Name: subpop; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE subpop FROM PUBLIC;
REVOKE ALL ON TABLE subpop FROM dbsnp;
GRANT ALL ON TABLE subpop TO dbsnp;
GRANT SELECT ON TABLE subpop TO dbsnp_ro;


--
-- Name: subpopallele; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE subpopallele FROM PUBLIC;
REVOKE ALL ON TABLE subpopallele FROM dbsnp;
GRANT ALL ON TABLE subpopallele TO dbsnp;
GRANT SELECT ON TABLE subpopallele TO dbsnp_ro;


--
-- Name: subpopgty; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE subpopgty FROM PUBLIC;
REVOKE ALL ON TABLE subpopgty FROM dbsnp;
GRANT ALL ON TABLE subpopgty TO dbsnp;
GRANT SELECT ON TABLE subpopgty TO dbsnp_ro;


--
-- Name: subsnp; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE subsnp FROM PUBLIC;
REVOKE ALL ON TABLE subsnp FROM dbsnp;
GRANT ALL ON TABLE subsnp TO dbsnp;
GRANT SELECT ON TABLE subsnp TO dbsnp_ro;


--
-- Name: subsnp_top_or_bot; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE subsnp_top_or_bot FROM PUBLIC;
REVOKE ALL ON TABLE subsnp_top_or_bot FROM dbsnp;
GRANT ALL ON TABLE subsnp_top_or_bot TO dbsnp;
GRANT SELECT ON TABLE subsnp_top_or_bot TO dbsnp_ro;


--
-- Name: subsnpacc; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE subsnpacc FROM PUBLIC;
REVOKE ALL ON TABLE subsnpacc FROM dbsnp;
GRANT ALL ON TABLE subsnpacc TO dbsnp;
GRANT SELECT ON TABLE subsnpacc TO dbsnp_ro;


--
-- Name: subsnpcommline; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE subsnpcommline FROM PUBLIC;
REVOKE ALL ON TABLE subsnpcommline FROM dbsnp;
GRANT ALL ON TABLE subsnpcommline TO dbsnp;
GRANT SELECT ON TABLE subsnpcommline TO dbsnp_ro;


--
-- Name: subsnplinkout; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE subsnplinkout FROM PUBLIC;
REVOKE ALL ON TABLE subsnplinkout FROM dbsnp;
GRANT ALL ON TABLE subsnplinkout TO dbsnp;
GRANT SELECT ON TABLE subsnplinkout TO dbsnp_ro;


--
-- Name: subsnpmdfailln; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE subsnpmdfailln FROM PUBLIC;
REVOKE ALL ON TABLE subsnpmdfailln FROM dbsnp;
GRANT ALL ON TABLE subsnpmdfailln TO dbsnp;
GRANT SELECT ON TABLE subsnpmdfailln TO dbsnp_ro;


--
-- Name: subsnpnovariseq; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE subsnpnovariseq FROM PUBLIC;
REVOKE ALL ON TABLE subsnpnovariseq FROM dbsnp;
GRANT ALL ON TABLE subsnpnovariseq TO dbsnp;
GRANT SELECT ON TABLE subsnpnovariseq TO dbsnp_ro;


--
-- Name: subsnppubmed; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE subsnppubmed FROM PUBLIC;
REVOKE ALL ON TABLE subsnppubmed FROM dbsnp;
GRANT ALL ON TABLE subsnppubmed TO dbsnp;
GRANT SELECT ON TABLE subsnppubmed TO dbsnp_ro;


--
-- Name: subsnpseq3; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE subsnpseq3 FROM PUBLIC;
REVOKE ALL ON TABLE subsnpseq3 FROM dbsnp;
GRANT ALL ON TABLE subsnpseq3 TO dbsnp;
GRANT SELECT ON TABLE subsnpseq3 TO dbsnp_ro;


--
-- Name: subsnpseq5; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE subsnpseq5 FROM PUBLIC;
REVOKE ALL ON TABLE subsnpseq5 FROM dbsnp;
GRANT ALL ON TABLE subsnpseq5 TO dbsnp;
GRANT SELECT ON TABLE subsnpseq5 TO dbsnp_ro;


--
-- Name: subsnpseqpos; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE subsnpseqpos FROM PUBLIC;
REVOKE ALL ON TABLE subsnpseqpos FROM dbsnp;
GRANT ALL ON TABLE subsnpseqpos TO dbsnp;
GRANT SELECT ON TABLE subsnpseqpos TO dbsnp_ro;


--
-- Name: synonym; Type: ACL; Schema: dbsnp_cow; Owner: dbsnp
--

REVOKE ALL ON TABLE synonym FROM PUBLIC;
REVOKE ALL ON TABLE synonym FROM dbsnp;
GRANT ALL ON TABLE synonym TO dbsnp;
GRANT SELECT ON TABLE synonym TO dbsnp_ro;


--
-- PostgreSQL database dump complete
--

