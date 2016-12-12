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

import com.github.fakemongo.Fongo;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

/**
 * Test {@link ExtractStatisticsFromVariant}
 */
public class ExtractStatisticsFromVariantTest {
    private ExtractStatisticsFromVariant extractStatisticsFromVariant;

    private static final String VARIANT_COLLECTION_NAME = "variantsCollection";
    private static final String STATISTICS_COLLECTION_NAME = "statisticsCollection";

    @Before
    public void setUp() throws Exception {
        extractStatisticsFromVariant = new ExtractStatisticsFromVariant();
        extractStatisticsFromVariant.setVariantsCollectionName(VARIANT_COLLECTION_NAME);
        extractStatisticsFromVariant.setStatisticsCollectionName(STATISTICS_COLLECTION_NAME);
    }

    @Test
    public void variantWithoutStatisticsShouldNotMigrate(){
        MongoTemplate mongoTemplate = new MongoTemplate(new Fongo("testServer").getMongo(), "variantWithoutStatistics");
        DBCollection variantsCollection = mongoTemplate.getCollection(VARIANT_COLLECTION_NAME);
        DBCollection statisticsCollection = mongoTemplate.getCollection(STATISTICS_COLLECTION_NAME);

        DBObject variantWithoutSt = (DBObject) JSON.parse("{ '_id' : 'Chr10_10000010_C_T', 'chr' : 'Chr10', 'start' : 10000010, 'files' : [ { 'fid' : 'ERZ123186', 'sid' : 'PRJEB10964', 'attrs' : { 'QUAL' : '255.0', 'CNV' : '64', 'TA' : 'Intergenic'}, 'fm' : 'GT:GL:GP:GQ:DP:AAC:LP', 'samp' : { 'def' : '0/0', '-1/-1' : [ 0, 1, 9, 10, 11, 12, 15, 18 ], '1/1' : [ 43 ] } } ], 'ids' : [ ], 'type' : 'SNV', 'end' : 10000010, 'len' : 1, 'ref' : 'C', 'alt' : 'T', '_at' : { 'chunkIds' : [ 'Chr10_10000_1k', 'Chr10_1000_10k' ] }, 'hgvs' : [ { 'type' : 'genomic', 'name' : 'Chr10:g.10000010C>T' } ] }");
        variantsCollection.insert(variantWithoutSt);

        extractStatisticsFromVariant.migrateStatistics(mongoTemplate);
        extractStatisticsFromVariant.removeStatisticsFromVariants(mongoTemplate);

        DBCursor variantCursor = variantsCollection.find();
        while (variantCursor.hasNext()) {
            DBObject variantObj = variantCursor.next();

            assertNull(variantObj.get("st"));
            assertNull(variantObj.get("maf"));
        }

        assertEquals(0, statisticsCollection.count());
    }

    @Test
    public void variantWithStatisticsShouldMigrate(){
        MongoTemplate mongoTemplate = new MongoTemplate(new Fongo("testServer").getMongo(), "variantWithStatistics");
        DBCollection variantsCollection = mongoTemplate.getCollection(VARIANT_COLLECTION_NAME);
        DBCollection statisticsCollection = mongoTemplate.getCollection(STATISTICS_COLLECTION_NAME);

        DBObject variantWithSt = (DBObject) JSON.parse("{ '_id' : 'Chr10_10000022_A_G', 'chr' : 'Chr10', 'start' : 10000022, 'files' : [ { 'fid' : 'ERZ123186', 'sid' : 'PRJEB10964', 'attrs' : { 'QUAL' : '255.0', 'CNV' : '64', 'TA' : 'Intergenic'}, 'fm' : 'GT:GL:GP:GQ:DP:AAC:LP', 'samp' : { '0/0' : [ 3, 21, 28, 35, 41, 42, 43, 46, 48, 51, 67, 68,  89, 98, 103 ], 'def' : '-1/-1', '0/1' : [ 36, 97 ], '1/1' : [ 13, 14, 16, 17, 18, 19, 33, 34, 37, 38, 39, 40, 44, 45, 47, 50, 52, 53, 55, 56, 58, 61, 62, 64, 70, 76, 83, 84, 85, 87, 88, 90, 92, 93, 96, 99, 100, 101 ] } } ], 'ids' : [ ], 'type' : 'SNV', 'end' : 10000022, 'len' : 1, 'ref' : 'A', 'alt' : 'G', '_at' : { 'chunkIds' : [ 'Chr10_10000_1k', 'Chr10_1000_10k' ] }, 'hgvs' : [ { 'type' : 'genomic', 'name' : 'Chr10:g.10000022A>G' } ], 'st' : [ { 'maf' : 0.3709677457809448, 'mgf' : 0.032258063554763794, 'mafAl' : 'A', 'mgfGt' : '0/1', 'missAl' : 84, 'missGt' : 42, 'numGt' : { '1/1' : 38, '0/1' : 2, '0/0' : 22, '-1/-1' : 42 }, 'cid' : 'ALL', 'sid' : 'PRJEB10964', 'fid' : 'ERZ123186' } ] }");
        variantsCollection.insert(variantWithSt);

        extractStatisticsFromVariant.migrateStatistics(mongoTemplate);
        extractStatisticsFromVariant.removeStatisticsFromVariants(mongoTemplate);

        assertEquals(1, variantsCollection.count());
        DBObject variantObj = variantsCollection.findOne();
        assertNull(variantObj.get("st"));
        assertNotNull(variantObj.get("maf"));
        double newMafInVariant = (Double) ((BasicDBList) variantObj.get("maf")).get(0);
        double mafInStVariant = (Double)((BasicDBObject)((BasicDBList)variantWithSt.get("st")).get(0)).get("maf");

        assertEquals(mafInStVariant, newMafInVariant,0);

        assertEquals(1, statisticsCollection.count());
        DBObject statisticsObj = statisticsCollection.findOne();
        assertNull(statisticsObj.get("fid"));

        assertNotNull(statisticsObj.get("vid"));
        assertNotNull(statisticsObj.get("chr"));
        assertNotNull(statisticsObj.get("start"));
        assertNotNull(statisticsObj.get("ref"));
        assertNotNull(statisticsObj.get("alt"));
        assertNotNull(statisticsObj.get("cid"));
        assertNotNull(statisticsObj.get("sid"));
        assertNotNull(statisticsObj.get("maf"));
        assertNotNull(statisticsObj.get("mgf"));
        assertNotNull(statisticsObj.get("mafAl"));
        assertNotNull(statisticsObj.get("mgfGt"));
        assertNotNull(statisticsObj.get("missAl"));
        assertNotNull(statisticsObj.get("missGt"));
        assertNotNull(statisticsObj.get("numGt"));
    }

    @Test
    public void mafFieldShouldBeFloatingPoint(){
        MongoTemplate mongoTemplate = new MongoTemplate(new Fongo("testServer").getMongo(), "variantWithStatistics");
        DBCollection variantsCollection = mongoTemplate.getCollection(VARIANT_COLLECTION_NAME);

        DBObject variantWithIntegerMaf = (DBObject) JSON.parse("{ '_id' : 'Chr10_10000022_A_G', 'chr' : 'Chr10', 'start' : 10000022, 'files' : [ { 'fid' : 'ERZ123186', 'sid' : 'PRJEB10964', 'attrs' : { 'QUAL' : '255.0', 'CNV' : '64', 'TA' : 'Intergenic'}, 'fm' : 'GT:GL:GP:GQ:DP:AAC:LP', 'samp' : { '0/0' : [ 3, 21, 28, 35, 41, 42, 43, 46, 48, 51, 67, 68,  89, 98, 103 ], 'def' : '-1/-1', '0/1' : [ 36, 97 ], '1/1' : [ 13, 14, 16, 17, 18, 19, 33, 34, 37, 38, 39, 40, 44, 45, 47, 50, 52, 53, 55, 56, 58, 61, 62, 64, 70, 76, 83, 84, 85, 87, 88, 90, 92, 93, 96, 99, 100, 101 ] } } ], 'ids' : [ ], 'type' : 'SNV', 'end' : 10000022, 'len' : 1, 'ref' : 'A', 'alt' : 'G', '_at' : { 'chunkIds' : [ 'Chr10_10000_1k', 'Chr10_1000_10k' ] }, 'hgvs' : [ { 'type' : 'genomic', 'name' : 'Chr10:g.10000022A>G' } ], 'st' : [ { 'maf' : 3, 'mgf' : 0.032258063554763794, 'mafAl' : 'A', 'mgfGt' : '0/1', 'missAl' : 84, 'missGt' : 42, 'numGt' : { '1/1' : 38, '0/1' : 2, '0/0' : 22, '-1/-1' : 42 }, 'cid' : 'ALL', 'sid' : 'PRJEB10964', 'fid' : 'ERZ123186' } ] }");
        variantsCollection.insert(variantWithIntegerMaf);

        extractStatisticsFromVariant.migrateStatistics(mongoTemplate);
        extractStatisticsFromVariant.removeStatisticsFromVariants(mongoTemplate);

        assertEquals(1, variantsCollection.count());
        DBObject variantObj = variantsCollection.findOne();
        assertNotNull(variantObj.get("maf"));

        BasicDBList mafs = (BasicDBList)variantObj.get("maf");
        assertNotNull(mafs.get(0));
        assertEquals(3.0, (double)mafs.get(0), 0);
    }

    @Test(expected = NumberFormatException.class)
    public void startFieldShouldBeInteger(){
        MongoTemplate mongoTemplate = new MongoTemplate(new Fongo("testServer").getMongo(), "variantWithStatistics");
        DBCollection variantsCollection = mongoTemplate.getCollection(VARIANT_COLLECTION_NAME);
        DBCollection statisticsCollection = mongoTemplate.getCollection(STATISTICS_COLLECTION_NAME);

        DBObject variantWithSt = (DBObject) JSON.parse("{ '_id' : 'Chr10_10000022_A_G', 'chr' : 'Chr10', 'start' : 10000022.0, 'files' : [ { 'fid' : 'ERZ123186', 'sid' : 'PRJEB10964', 'attrs' : { 'QUAL' : '255.0', 'CNV' : '64', 'TA' : 'Intergenic'}, 'fm' : 'GT:GL:GP:GQ:DP:AAC:LP', 'samp' : { '0/0' : [ 3, 21, 28, 35, 41, 42, 43, 46, 48, 51, 67, 68,  89, 98, 103 ], 'def' : '-1/-1', '0/1' : [ 36, 97 ], '1/1' : [ 13, 14, 16, 17, 18, 19, 33, 34, 37, 38, 39, 40, 44, 45, 47, 50, 52, 53, 55, 56, 58, 61, 62, 64, 70, 76, 83, 84, 85, 87, 88, 90, 92, 93, 96, 99, 100, 101 ] } } ], 'ids' : [ ], 'type' : 'SNV', 'end' : 10000022, 'len' : 1, 'ref' : 'A', 'alt' : 'G', '_at' : { 'chunkIds' : [ 'Chr10_10000_1k', 'Chr10_1000_10k' ] }, 'hgvs' : [ { 'type' : 'genomic', 'name' : 'Chr10:g.10000022A>G' } ], 'st' : [ { 'maf' : 0.3709677457809448, 'mgf' : 0.032258063554763794, 'mafAl' : 'A', 'mgfGt' : '0/1', 'missAl' : 84, 'missGt' : 42, 'numGt' : { '1/1' : 38, '0/1' : 2, '0/0' : 22, '-1/-1' : 42 }, 'cid' : 'ALL', 'sid' : 'PRJEB10964', 'fid' : 'ERZ123186' } ] }");
        variantsCollection.insert(variantWithSt);

        extractStatisticsFromVariant.migrateStatistics(mongoTemplate);
        extractStatisticsFromVariant.removeStatisticsFromVariants(mongoTemplate);

        assertEquals(1, statisticsCollection.count());

        DBObject statisticsObj = statisticsCollection.findOne();

        assertNotNull(statisticsObj.get("start"));
        assertEquals(10000022, statisticsObj.get("start"));
    }

    @Test
    public void variantWithMultipleStatisticsShouldMigrate(){
        MongoTemplate mongoTemplate = new MongoTemplate(new Fongo("testServer").getMongo(), "variantWithMultipleStatistics");
        DBCollection variantsCollection = mongoTemplate.getCollection(VARIANT_COLLECTION_NAME);

        DBObject variant = (DBObject) JSON.parse("{ '_id' : 'Chr10_10000096_C_T', 'chr' : 'Chr10', 'start' : 10000096, 'files' : [ { 'fid' : 'ERZ123186', 'sid' : 'PRJEB10964', 'attrs' : { 'QUAL' : '40.0', 'CNV' : '64', 'TA' : 'Intergenic'}, 'fm' : 'GT:GL:GP:GQ:DP:AAC:LP', 'samp' : { 'def' : '0/0', '-1/-1' : [ 0, 1, 5, 6, 11, 13, 15, 18, 22, 25, 27, 30, 32, 40, 43, 48, 57, 59, 60, 69, 82, 89 ], '0/1' : [ 9 ] } } ], 'ids' : [ ], 'type' : 'SNV', 'end' : 10000096, 'len' : 1, 'ref' : 'C', 'alt' : 'T', '_at' : { 'chunkIds' : [ 'Chr10_10000_1k', 'Chr10_1000_10k' ] }, 'hgvs' : [ { 'type' : 'genomic', 'name' : 'Chr10:g.10000096C>T' } ], 'st' : [ { 'maf' : 0.006097560748457909, 'mgf' : 0, 'mafAl' : 'T', 'mgfGt' : '1/1', 'missAl' : 44, 'missGt' : 22, 'numGt' : { '-1/-1' : 22, '0/1' : 1, '0/0' : 81 }, 'cid' : 'ALL', 'sid' : 'PRJEB10964', 'fid' : 'ERZ123186' }, { 'maf' : 11111, 'mgf' : 1, 'mafAl' : 'T', 'mgfGt' : '1/1', 'missAl' : 11, 'missGt' : 11, 'numGt' : { '-1/-1' : 11, '1/1' : 1, '0/0' : 81 }, 'cid' : 'ALL', 'sid' : 'PRJEB10964', 'fid' : 'ERZ123186' }] }");
        variantsCollection.insert(variant);

        extractStatisticsFromVariant.migrateStatistics(mongoTemplate);
        extractStatisticsFromVariant.removeStatisticsFromVariants(mongoTemplate);

        DBCursor variantCursor = variantsCollection.find();
        while (variantCursor.hasNext()) {
            DBObject variantObj = variantCursor.next();

            assertNull(variantObj.get("st"));
            assertNotNull(variantObj.get("maf"));
            assertEquals(2, ((BasicDBList) variantObj.get("maf")).size());
        }
    }

    @Test
    public void multipleVariantsMigration(){
        MongoTemplate mongoTemplate = new MongoTemplate(new Fongo("testServer").getMongo(), "multipleVariants");
        DBCollection variantsCollection = mongoTemplate.getCollection(VARIANT_COLLECTION_NAME);

        DBObject var1 = (DBObject) JSON.parse("{ '_id' : 'Chr10_10000010_C_T', 'chr' : 'Chr10', 'start' : 10000010, 'files' : [ { 'fid' : 'ERZ123186', 'sid' : 'PRJEB10964', 'attrs' : { 'QUAL' : '255.0', 'CNV' : '64', 'TA' : 'Intergenic'}, 'fm' : 'GT:GL:GP:GQ:DP:AAC:LP', 'samp' : { 'def' : '0/0', '-1/-1' : [ 0, 1, 9, 10, 11, 12, 15, 18 ], '1/1' : [ 43 ] } } ], 'ids' : [ ], 'type' : 'SNV', 'end' : 10000010, 'len' : 1, 'ref' : 'C', 'alt' : 'T', '_at' : { 'chunkIds' : [ 'Chr10_10000_1k', 'Chr10_1000_10k' ] }, 'hgvs' : [ { 'type' : 'genomic', 'name' : 'Chr10:g.10000010C>T' } ] }");
        DBObject var2 = (DBObject) JSON.parse("{ '_id' : 'Chr10_10000022_A_G', 'chr' : 'Chr10', 'start' : 10000022, 'files' : [ { 'fid' : 'ERZ123186', 'sid' : 'PRJEB10964', 'attrs' : { 'QUAL' : '255.0', 'CNV' : '64', 'TA' : 'Intergenic'}, 'fm' : 'GT:GL:GP:GQ:DP:AAC:LP', 'samp' : { '0/0' : [ 3, 21, 28, 35, 41, 42, 43, 46, 48, 51, 67, 68,  89, 98, 103 ], 'def' : '-1/-1', '0/1' : [ 36, 97 ], '1/1' : [ 13, 14, 16, 17, 18, 19, 33, 34, 37, 38, 39, 40, 44, 45, 47, 50, 52, 53, 55, 56, 58, 61, 62, 64, 70, 76, 83, 84, 85, 87, 88, 90, 92, 93, 96, 99, 100, 101 ] } } ], 'ids' : [ ], 'type' : 'SNV', 'end' : 10000022, 'len' : 1, 'ref' : 'A', 'alt' : 'G', '_at' : { 'chunkIds' : [ 'Chr10_10000_1k', 'Chr10_1000_10k' ] }, 'hgvs' : [ { 'type' : 'genomic', 'name' : 'Chr10:g.10000022A>G' } ], 'st' : [ { 'maf' : 0.3709677457809448, 'mgf' : 0.032258063554763794, 'mafAl' : 'A', 'mgfGt' : '0/1', 'missAl' : 84, 'missGt' : 42, 'numGt' : { '1/1' : 38, '0/1' : 2, '0/0' : 22, '-1/-1' : 42 }, 'cid' : 'ALL', 'sid' : 'PRJEB10964', 'fid' : 'ERZ123186' } ] }");
        DBObject var3 = (DBObject) JSON.parse("{ '_id' : 'Chr10_10000058_T_G', 'chr' : 'Chr10', 'start' : 10000058, 'files' : [ { 'fid' : 'ERZ123186', 'sid' : 'PRJEB10964', 'attrs' : { 'QUAL' : '255.0', 'CNV' : '64', 'TA' : 'Intergenic'}, 'fm' : 'GT:GL:GP:GQ:DP:AAC:LP', 'samp' : { 'def' : '0/0', '-1/-1' : [ 0, 1, 9, 11, 12, 15, 22, 24, 27, 30, 31, 34, 48, 49, 59, 60, 69 ], '1/1' : [ 10, 50, 84, 94, 96 ] } } ], 'ids' : [ ], 'type' : 'SNV', 'end' : 10000058, 'len' : 1, 'ref' : 'T', 'alt' : 'G', '_at' : { 'chunkIds' : [ 'Chr10_10000_1k', 'Chr10_1000_10k' ] }, 'hgvs' : [ { 'type' : 'genomic', 'name' : 'Chr10:g.10000058T>G' } ], 'st' : [ { 'maf' : 0.05747126415371895, 'mgf' : 0, 'mafAl' : 'G', 'mgfGt' : '0/1', 'missAl' : 34, 'missGt' : 17, 'numGt' : { '1/1' : 5, '-1/-1' : 17, '0/0' : 82 }, 'cid' : 'ALL', 'sid' : 'PRJEB10964', 'fid' : 'ERZ123186' } ] }");
        DBObject var4 = (DBObject) JSON.parse("{ '_id' : 'Chr10_10000062_T_C', 'chr' : 'Chr10', 'start' : 10000062, 'files' : [ { 'fid' : 'ERZ123186', 'sid' : 'PRJEB10964', 'attrs' : { 'QUAL' : '62.0', 'CNV' : '64', 'TA' : 'Intergenic'}, 'fm' : 'GT:GL:GP:GQ:DP:AAC:LP', 'samp' : { 'def' : '0/0', '-1/-1' : [ 0, 1, 2, 5, 9,  48, 52, 59, 60, 64, 69, 74, 76, 79, 91, 102 ], '1/1' : [ 54 ] } } ], 'ids' : [ ], 'type' : 'SNV', 'end' : 10000062, 'len' : 1, 'ref' : 'T', 'alt' : 'C', '_at' : { 'chunkIds' : [ 'Chr10_10000_1k', 'Chr10_1000_10k' ] }, 'hgvs' : [ { 'type' : 'genomic', 'name' : 'Chr10:g.10000062T>C' } ], 'st' : [ { 'maf' : 0.012820512987673283, 'mgf' : 0, 'mafAl' : 'C', 'mgfGt' : '0/1', 'missAl' : 52, 'missGt' : 26, 'numGt' : { '-1/-1' : 26, '1/1' : 1, '0/0' : 77 }, 'cid' : 'ALL', 'sid' : 'PRJEB10964', 'fid' : 'ERZ123186' } ] }");
        DBObject var5 = (DBObject) JSON.parse("{ '_id' : 'Chr10_10000096_C_T', 'chr' : 'Chr10', 'start' : 10000096, 'files' : [ { 'fid' : 'ERZ123186', 'sid' : 'PRJEB10964', 'attrs' : { 'QUAL' : '40.0', 'CNV' : '64', 'TA' : 'Intergenic'}, 'fm' : 'GT:GL:GP:GQ:DP:AAC:LP', 'samp' : { 'def' : '0/0', '-1/-1' : [ 0, 1, 5, 6, 11, 13, 15, 18, 22, 25, 27, 30, 32, 40, 43, 48, 57, 59, 60, 69, 82, 89 ], '0/1' : [ 9 ] } } ], 'ids' : [ ], 'type' : 'SNV', 'end' : 10000096, 'len' : 1, 'ref' : 'C', 'alt' : 'T', '_at' : { 'chunkIds' : [ 'Chr10_10000_1k', 'Chr10_1000_10k' ] }, 'hgvs' : [ { 'type' : 'genomic', 'name' : 'Chr10:g.10000096C>T' } ], 'st' : [ { 'maf' : 0.006097560748457909, 'mgf' : 0, 'mafAl' : 'T', 'mgfGt' : '1/1', 'missAl' : 44, 'missGt' : 22, 'numGt' : { '-1/-1' : 22, '0/1' : 1, '0/0' : 81 }, 'cid' : 'ALL', 'sid' : 'PRJEB10964', 'fid' : 'ERZ123186' }, { 'maf' : 11111, 'mgf' : 1, 'mafAl' : 'T', 'mgfGt' : '1/1', 'missAl' : 11, 'missGt' : 11, 'numGt' : { '-1/-1' : 11, '1/1' : 1, '0/0' : 81 }, 'cid' : 'ALL', 'sid' : 'PRJEB10964', 'fid' : 'ERZ123186' }] }");

        variantsCollection.insert(var1);
        variantsCollection.insert(var2);
        variantsCollection.insert(var3);
        variantsCollection.insert(var4);
        variantsCollection.insert(var5);

        extractStatisticsFromVariant.migrateStatistics(mongoTemplate);
        extractStatisticsFromVariant.removeStatisticsFromVariants(mongoTemplate);

        assertEquals(5, variantsCollection.count());
    }

}
