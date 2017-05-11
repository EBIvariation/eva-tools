/*
 * Copyright 2016 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.dbmigration.mongodb;

public class VariantData {

    static final String VARIANT_WITHOUT_ST = "{" +
            "'_id' : 'Chr10_10000010_C_T', " +
            "'chr' : 'Chr10', " +
            "'start' : 10000010, " +
            "'files' : [ { 'fid' : 'ERZ123186', 'sid' : 'PRJEB10964', 'attrs' : { 'QUAL' : '255.0', 'CNV' : '64', 'TA' : 'Intergenic'}, 'fm' : 'GT:GL:GP:GQ:DP:AAC:LP', 'samp' : { 'def' : '0/0', '-1/-1' : [ 0, 1, 9, 10, 11, 12, 15, 18 ], '1/1' : [ 43 ] } } ], " +
            "'ids' : [ ], " +
            "'type' : 'SNV', " +
            "'end' : 10000010, " +
            "'len' : 1, " +
            "'ref' : 'C', " +
            "'alt' : 'T', " +
            "'_at' : { 'chunkIds' : [ 'Chr10_10000_1k', 'Chr10_1000_10k' ] }, " +
            "'hgvs' : [ { 'type' : 'genomic', 'name' : 'Chr10:g.10000010C>T' } ] }";

    static final String VARIANT_WITH_ST_1 = "{ " +
            "'_id' : 'Chr10_10000022_A_G', " +
            "'chr' : 'Chr10', " +
            "'start' : 10000022, " +
            "'files' : [ { 'fid' : 'ERZ123186', 'sid' : 'PRJEB10964', 'attrs' : { 'QUAL' : '255.0', 'CNV' : '64', 'TA' : 'Intergenic'}, 'fm' : 'GT:GL:GP:GQ:DP:AAC:LP', 'samp' : { '0/0' : [ 3, 21, 28, 35, 41, 42, 43, 46, 48, 51, 67, 68,  89, 98, 103 ], 'def' : '-1/-1', '0/1' : [ 36, 97 ], '1/1' : [ 13, 14, 16, 17, 18, 19, 33, 34, 37, 38, 39, 40, 44, 45, 47, 50, 52, 53, 55, 56, 58, 61, 62, 64, 70, 76, 83, 84, 85, 87, 88, 90, 92, 93, 96, 99, 100, 101 ] } } ], " +
            "'ids' : [ ], " +
            "'type' : 'SNV', " +
            "'end' : 10000022, " +
            "'len' : 1, " +
            "'ref' : 'A', " +
            "'alt' : 'G', " +
            "'_at' : { 'chunkIds' : [ 'Chr10_10000_1k', 'Chr10_1000_10k' ] }, " +
            "'hgvs' : [ { 'type' : 'genomic', 'name' : 'Chr10:g.10000022A>G' } ], " +
            "'st' : [ { 'maf' : 0.3709677457809448, 'mgf' : 0.032258063554763794, 'mafAl' : 'A', 'mgfGt' : '0/1', 'missAl' : 84, 'missGt' : 42, 'numGt' : { '1/1' : 38, '0/1' : 2, '0/0' : 22, '-1/-1' : 42 }, 'cid' : 'ALL', 'sid' : 'PRJEB10964', 'fid' : 'ERZ123186' } ] }";

    static final String VARIANT_WITH_ST_2 = "{ " +
            "'_id' : 'Chr10_10000058_T_G', " +
            "'chr' : 'Chr10', " +
            "'start' : 10000058, " +
            "'files' : [ { 'fid' : 'ERZ123186', 'sid' : 'PRJEB10964', 'attrs' : { 'QUAL' : '255.0', 'CNV' : '64', 'TA' : 'Intergenic'}, 'fm' : 'GT:GL:GP:GQ:DP:AAC:LP', 'samp' : { 'def' : '0/0', '-1/-1' : [ 0, 1, 9, 11, 12, 15, 22, 24, 27, 30, 31, 34, 48, 49, 59, 60, 69 ], '1/1' : [ 10, 50, 84, 94, 96 ] } } ], " +
            "'ids' : [ ], " +
            "'type' : 'SNV', " +
            "'end' : 10000058, " +
            "'len' : 1, " +
            "'ref' : 'T', " +
            "'alt' : 'G', " +
            "'_at' : { 'chunkIds' : [ 'Chr10_10000_1k', 'Chr10_1000_10k' ] }, " +
            "'hgvs' : [ { 'type' : 'genomic', 'name' : 'Chr10:g.10000058T>G' } ], " +
            "'st' : [ { 'maf' : 0.05747126415371895, 'mgf' : 0, 'mafAl' : 'G', 'mgfGt' : '0/1', 'missAl' : 34, 'missGt' : 17, 'numGt' : { '1/1' : 5, '-1/-1' : 17, '0/0' : 82 }, 'cid' : 'ALL', 'sid' : 'PRJEB10964', 'fid' : 'ERZ123186' } ] }";

    static final String VARIANT_WITH_ST_3 = "{ " +
            "'_id' : 'Chr10_10000062_T_C', " +
            "'chr' : 'Chr10', " +
            "'start' : 10000062, " +
            "'files' : [ { 'fid' : 'ERZ123186', 'sid' : 'PRJEB10964', 'attrs' : { 'QUAL' : '62.0', 'CNV' : '64', 'TA' : 'Intergenic'}, 'fm' : 'GT:GL:GP:GQ:DP:AAC:LP', 'samp' : { 'def' : '0/0', '-1/-1' : [ 0, 1, 2, 5, 9,  48, 52, 59, 60, 64, 69, 74, 76, 79, 91, 102 ], '1/1' : [ 54 ] } } ], " +
            "'ids' : [ ], " +
            "'type' : 'SNV', " +
            "'end' : 10000062, " +
            "'len' : 1, " +
            "'ref' : 'T', " +
            "'alt' : 'C', " +
            "'_at' : { 'chunkIds' : [ 'Chr10_10000_1k', 'Chr10_1000_10k' ] }, " +
            "'hgvs' : [ { 'type' : 'genomic', 'name' : 'Chr10:g.10000062T>C' } ], " +
            "'st' : [ { 'maf' : 0.012820512987673283, 'mgf' : 0, 'mafAl' : 'C', 'mgfGt' : '0/1', 'missAl' : 52, 'missGt' : 26, 'numGt' : { '-1/-1' : 26, '1/1' : 1, '0/0' : 77 }, 'cid' : 'ALL', 'sid' : 'PRJEB10964', 'fid' : 'ERZ123186' } ] }";

    static final String VARIANT_WITH_INTEGER_MAF = "{" +
            "'_id' : 'Chr10_10000022_A_G', " +
            "'chr' : 'Chr10', " +
            "'start' : 10000022, " +
            "'files' : [ { 'fid' : 'ERZ123186', 'sid' : 'PRJEB10964', 'attrs' : { 'QUAL' : '255.0', 'CNV' : '64', 'TA' : 'Intergenic'}, 'fm' : 'GT:GL:GP:GQ:DP:AAC:LP', 'samp' : { '0/0' : [ 3, 21, 28, 35, 41, 42, 43, 46, 48, 51, 67, 68,  89, 98, 103 ], 'def' : '-1/-1', '0/1' : [ 36, 97 ], '1/1' : [ 13, 14, 16, 17, 18, 19, 33, 34, 37, 38, 39, 40, 44, 45, 47, 50, 52, 53, 55, 56, 58, 61, 62, 64, 70, 76, 83, 84, 85, 87, 88, 90, 92, 93, 96, 99, 100, 101 ] } } ], " +
            "'ids' : [ ], " +
            "'type' : 'SNV', " +
            "'end' : 10000022, " +
            "'len' : 1, " +
            "'ref' : 'A', " +
            "'alt' : 'G', " +
            "'_at' : { 'chunkIds' : [ 'Chr10_10000_1k', 'Chr10_1000_10k' ] }, " +
            "'hgvs' : [ { 'type' : 'genomic', 'name' : 'Chr10:g.10000022A>G' } ], " +
            "'st' : [ { 'maf' : 3, 'mgf' : 0.032258063554763794, 'mafAl' : 'A', 'mgfGt' : '0/1', 'missAl' : 84, 'missGt' : 42, 'numGt' : { '1/1' : 38, '0/1' : 2, '0/0' : 22, '-1/-1' : 42 }, 'cid' : 'ALL', 'sid' : 'PRJEB10964', 'fid' : 'ERZ123186' } ] }";

    static final String VARIANT_WITH_FLOATING_START = "{" +
            "'_id' : 'Chr10_10000022_A_G', " +
            "'chr' : 'Chr10', " +
            "'start' : 10000022.0, " +
            "'files' : [ { 'fid' : 'ERZ123186', 'sid' : 'PRJEB10964', 'attrs' : { 'QUAL' : '255.0', 'CNV' : '64', 'TA' : 'Intergenic'}, 'fm' : 'GT:GL:GP:GQ:DP:AAC:LP', 'samp' : { '0/0' : [ 3, 21, 28, 35, 41, 42, 43, 46, 48, 51, 67, 68,  89, 98, 103 ], 'def' : '-1/-1', '0/1' : [ 36, 97 ], '1/1' : [ 13, 14, 16, 17, 18, 19, 33, 34, 37, 38, 39, 40, 44, 45, 47, 50, 52, 53, 55, 56, 58, 61, 62, 64, 70, 76, 83, 84, 85, 87, 88, 90, 92, 93, 96, 99, 100, 101 ] } } ], " +
            "'ids' : [ ], " +
            "'type' : 'SNV', " +
            "'end' : 10000022, " +
            "'len' : 1, " +
            "'ref' : 'A', " +
            "'alt' : 'G', " +
            "'_at' : { 'chunkIds' : [ 'Chr10_10000_1k', 'Chr10_1000_10k' ] }, " +
            "'hgvs' : [ { 'type' : 'genomic', 'name' : 'Chr10:g.10000022A>G' } ], " +
            "'st' : [ { 'maf' : 0.3709677457809448, 'mgf' : 0.032258063554763794, 'mafAl' : 'A', 'mgfGt' : '0/1', 'missAl' : 84, 'missGt' : 42, 'numGt' : { '1/1' : 38, '0/1' : 2, '0/0' : 22, '-1/-1' : 42 }, 'cid' : 'ALL', 'sid' : 'PRJEB10964', 'fid' : 'ERZ123186' } ] }";

    static final String VARIANT_WITH_MULTIPLE_ST = "{" +
            "'_id' : 'Chr10_10000096_C_T', " +
            "'chr' : 'Chr10', " +
            "'start' : 10000096, " +
            "'files' : [ { 'fid' : 'ERZ123186', 'sid' : 'PRJEB10964', 'attrs' : { 'QUAL' : '40.0', 'CNV' : '64', 'TA' : 'Intergenic'}, 'fm' : 'GT:GL:GP:GQ:DP:AAC:LP', 'samp' : { 'def' : '0/0', '-1/-1' : [ 0, 1, 5, 6, 11, 13, 15, 18, 22, 25, 27, 30, 32, 40, 43, 48, 57, 59, 60, 69, 82, 89 ], '0/1' : [ 9 ] } } ], " +
            "'ids' : [ ], " +
            "'type' : 'SNV', " +
            "'end' : 10000096, " +
            "'len' : 1, " +
            "'ref' : 'C', " +
            "'alt' : 'T', " +
            "'_at' : { 'chunkIds' : [ 'Chr10_10000_1k', 'Chr10_1000_10k' ] }, " +
            "'hgvs' : [ { 'type' : 'genomic', 'name' : 'Chr10:g.10000096C>T' } ], " +
            "'st' : [ { 'maf' : 0.006097560748457909, 'mgf' : 0, 'mafAl' : 'T', 'mgfGt' : '1/1', 'missAl' : 44, 'missGt' : 22, 'numGt' : { '-1/-1' : 22, '0/1' : 1, '0/0' : 81 }, 'cid' : 'ALL', 'sid' : 'PRJEB10964', 'fid' : 'ERZ123186' }, " +
            "{ 'maf' : 11111, 'mgf' : 1, 'mafAl' : 'T', 'mgfGt' : '1/1', 'missAl' : 11, 'missGt' : 11, 'numGt' : { '-1/-1' : 11, '1/1' : 1, '0/0' : 81 }, 'cid' : 'ALL', 'sid' : 'PRJEB10964', 'fid' : 'ERZ123186' }] }";
}
