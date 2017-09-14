-- columns ctg_id, tax_id, contig_acc, contig_ver, contig_name, contig_chr, contig_start, contig_end, orient, contig_gi, group_term, group_label, contig_label, primary_fl, genbank_gi, genbank_acc, genbank_ver, build_id, build_ver, last_updated_time, placement_status, asm_acc, asm_version, chr_gi, par_fl, top_level_fl, gen_rgn, contig_length
INSERT INTO b150_contiginfo VALUES(966748913, 9031, 'NT_455866', 1, 'NT_455866.1', '4', 89457489, 91282655, 0, 966748913, 'Primary_Assembly', 'Gallus_gallus-5.0', NULL, 1, 964013695, 'KQ759087', 1, 103, 0, '2016-08-31 08:03:00.000', 1, 'GCF_000002315', 4, 966749128, NULL, 0, NULL, 1825167);
INSERT INTO b150_contiginfo VALUES(966740651, 9031, 'NT_464114', 1, 'NT_464114.1', 'LGE64', NULL, NULL, NULL, 966740651, 'Primary_Assembly', 'Gallus_gallus-5.0', NULL, 1, 963275947, 'AADN04005929', 1, 103, 0, '2016-08-31 08:03:00.000', 2, 'GCF_000002315', 4, 966749097, NULL, 1, NULL, 12754);
INSERT INTO b150_contiginfo VALUES(966748672, 9031, 'NT_456093', 1, 'NT_456093.1', 'LGE64', 856351, 897575, 1, 966748672, 'Primary_Assembly', 'Gallus_gallus-5.0', NULL, 1, 964011153, 'KQ759333', 1, 103, 0, '2016-08-31 08:03:00.000', 1, 'GCF_000002315', 4, 966749097, NULL, 0, NULL, 41225);
INSERT INTO b150_contiginfo VALUES(966748673, 9031, 'NT_456092', 1, 'NT_456092.1', 'LGE64', 429077, 856250, 0, 966748673, 'Primary_Assembly', 'Gallus_gallus-5.0', NULL, 1, 964011158, 'KQ759332', 1, 103, 0, '2016-08-31 08:03:00.000', 1, 'GCF_000002315', 4, 966749097, NULL, 0, NULL, 427174);
INSERT INTO b150_contiginfo VALUES(966748945, 9031, 'NT_455837', 1, 'NT_455837.1', '3', 36431259, 58844808, 1, 966748945, 'Primary_Assembly', 'Gallus_gallus-4.0', NULL, 1, 964013905, 'KQ759055', 1, 103, 0, '2016-08-31 08:03:00.000', 1, 'GCF_000002315', 4, 966749129, NULL, 0, NULL, 22413550);


-- columns snp_id, avg_heterozygosity, het_se, create_time, last_updated_time, cpg_code, tax_id, validation_status, exemplar_subsnp_id, univar_id, cnt_subsnp, map_property
INSERT INTO snp VALUES(13677177, 0.611111104, 0.124225996, '2004-07-02 16:03:00.000', '2017-05-05 16:40:15.803', NULL, 9031, 3, 26201546, 128, 3, NULL);
INSERT INTO snp VALUES(13808689, 0.5, 0, '2004-07-02 16:03:00.000', '2015-03-27 13:06:41.95', NULL, 9031, 3, 25920661, 570, 2, NULL);
INSERT INTO snp VALUES(739617577, NULL, NULL, '2015-03-25 15:37:00.000', '2015-03-25 15:36:57.023', NULL, 9031, NULL, 1540359250, 238, 1, NULL);


-- columns snp_type, snp_id, ctg_id, asn_from, asn_to, lf_ngbr, rf_ngbr, lc_ngbr, rc_ngbr, loc_type, phys_pos_from, snp_bld_id, last_updated_time, process_status, orientation, allele, loc_sts_uid, aln_quality, num_mism, num_del, num_ins, tier
INSERT INTO b150_snpcontigloc VALUES('rs', 13677177, 966748913, 1766471, 1766471, NULL, NULL, 1766470, 1766472, 2, 91223960, 147, '2016-03-09 10:58:00.000', 128, 0, 'T', NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO b150_snpcontigloc VALUES('rs', 13808689, 966740651, 4944, 4948, NULL, NULL, 4943, 4949, 1, NULL, 147, '2016-03-09 10:58:00.000', 128, 1, 'CCTGA', NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO b150_snpcontigloc VALUES('rs', 13808689, 966748672, 38772, 38776, NULL, NULL, 38771, 38777, 1, 858799, 147, '2016-03-09 10:58:00.000', 128, 1, 'CCTGA', NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO b150_snpcontigloc VALUES('rs', 13808689, 966748673, 409649, 409653, NULL, NULL, 409648, 409654, 1, 838726, 147, '2016-03-09 10:58:00.000', 128, 0, 'TCAGG', NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO b150_snpcontigloc VALUES('rs', 739617577, 966748945, 11724979, 11724982, NULL, NULL, 11724978, 11724983, 1, 47119826, 147, '2016-03-09 10:58:00.000', 128, 1, 'CCGA', NULL, 1, NULL, NULL, NULL, NULL);


-- columns subsnp_id, snp_id, substrand_reversed_flag, create_time, last_updated_time, build_id, comment
INSERT INTO snpsubsnplink VALUES(26201546, 13677177, 0, '2004-07-02 15:53:00.000', '2004-07-02 15:53:00.000', 122, NULL);
INSERT INTO snpsubsnplink VALUES(26954817, 13677177, 1, '2004-07-02 16:01:00.000', '2010-03-25 21:23:00.000', 131, NULL);
INSERT INTO snpsubsnplink VALUES(26963037, 13677177, 0, '2004-07-02 15:53:00.000', '2004-07-02 15:53:00.000', 122, NULL);
INSERT INTO snpsubsnplink VALUES(25920661, 13808689, 0, '2004-07-02 15:53:00.000', '2004-07-02 15:53:00.000', 122, NULL);
INSERT INTO snpsubsnplink VALUES(25920795, 13808689, 0, '2004-07-02 15:53:00.000', '2004-07-02 15:53:00.000', 122, NULL);
INSERT INTO snpsubsnplink VALUES(1540359250, 739617577, 0, '2015-03-25 15:23:36.217', '2015-03-25 15:23:36.217', 144, 'RSM:ss->ss');


-- columns subsnp_id, known_snp_handle, known_snp_loc_id, known_snp_loc_id_upp, batch_id, loc_snp_id, loc_snp_id_upp, synonym_names, loc_sts_id, loc_sts_id_upp, segregate, indiv_homozygosity_detected, pcr_confirmed_ind, gene_name, sequence_len, samplesize, expressed_sequence_ind, somatic_ind, sub_locus_id, create_time, last_updated_time, ancestral_allele, cpg_code, variation_id, top_or_bot_strand, validation_status, snp_id, tax_id, chr_id
INSERT INTO subsnp VALUES (26201546, ' ', ' ', ' ', 11828, 'snp.46.161.27439.S.2', 'SNP.46.161.27439.S.2', ' ', ' ', ' ', '?', '?', '?', ' ', 81, 8, '?', '?', 0, '2004-06-24 12:10:00.000', '2004-08-05 15:30:00.000', ' ', 0, 16, 'T', 3, 13677177, 9031, 28);
INSERT INTO subsnp VALUES (26954817, ' ', ' ', ' ', 11831, 'snp.147.73.779.S.3', 'SNP.147.73.779.S.3', ' ', ' ', ' ', '?', '?', '?', ' ', 81, 8, '?', '?', 0, '2004-06-24 17:58:00.000', '2004-08-05 15:32:00.000', ' ', 4, 20, 'T', 3, 15554969, 9031, 28);
INSERT INTO subsnp VALUES (26963037, ' ', ' ', ' ', 11831, 'snp.46.161.27439.S.3', 'SNP.46.161.27439.S.3', ' ', ' ', ' ', '?', '?', '?', ' ', 81, 8, '?', '?', 0, '2004-06-24 18:04:00.000', '2004-08-05 15:32:00.000', ' ', 0, 16, 'T', 3, 13677177, 9031, 28);
INSERT INTO subsnp VALUES(25920661, ' ', ' ', ' ', 11825, 'snp.24851.1.563.D.1', 'SNP.24851.1.563.D.1', ' ', ' ', ' ', '?', '?', '?', ' ', 81, 8, '?', '?', 0, '2004-06-23 21:19:00.000', '2004-08-05 15:29:00.000', ' ', 0, 13946, 'B', 3, 13808689, 9031, 28);
INSERT INTO subsnp VALUES(25920795, ' ', ' ', ' ', 11825, 'snp.24938.1.334.D.1', 'SNP.24938.1.334.D.1', ' ', ' ', ' ', '?', '?', '?', ' ', 81, 8, '?', '?', 0, '2004-06-23 21:20:00.000', '2004-08-05 15:29:00.000', ' ', 0, 13946, 'B', 3, 13808689, 9031, 28);
INSERT INTO subsnp VALUES (1540359250, ' ', ' ', ' ', 1062064, '3_46315457', '3_46315457', '.', ' ', ' ', '?', '?', '?', '.', 51, 326, '?', '?', 0, '2015-01-21 17:13:00.000', '2015-01-21 17:13:00.000', ' ', NULL, 11680, NULL, 1, NULL, 9031, NULL);
