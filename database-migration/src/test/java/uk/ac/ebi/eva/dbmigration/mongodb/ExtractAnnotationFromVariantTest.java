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

import java.util.Set;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

/**
 * Test {@link ExtractAnnotationFromVariant}
 */
public class ExtractAnnotationFromVariantTest {
    private ExtractAnnotationFromVariant extractAnnotationFromVariant;

    private static final String VARIANT_COLLECTION_NAME = "variantsCollection";

    private static final String ANNOTATION_COLLECTION_NAME = "annotationCollection";

    @Before
    public void setUp() throws Exception {
        extractAnnotationFromVariant = new ExtractAnnotationFromVariant(VARIANT_COLLECTION_NAME,
                                                                        ANNOTATION_COLLECTION_NAME);
    }

    @Test
    public void variantWithoutAnnotationShouldNotChange() {
        MongoTemplate mongoTemplate = new MongoTemplate(new Fongo("testServer").getMongo(), "variantWithoutAnnotation");
        DBCollection variantsCollection = mongoTemplate.getCollection(VARIANT_COLLECTION_NAME);
        DBCollection annotationCollection = mongoTemplate.getCollection(ANNOTATION_COLLECTION_NAME);

        DBObject variantWithoutSt = (DBObject) JSON.parse(VariantData.VARIANT_WITHOUT_ST);
        variantsCollection.insert(variantWithoutSt);

        extractAnnotationFromVariant.migrateAnnotation(mongoTemplate);
        extractAnnotationFromVariant.removeAnnotationFromVariants(mongoTemplate);

        DBCursor variantCursor = variantsCollection.find();
        while (variantCursor.hasNext()) {
            DBObject variantObj = variantCursor.next();

            assertNull(variantObj.get("st"));
            assertNull(variantObj.get("maf"));
        }

        assertEquals(variantsCollection.find(new BasicDBObject("st", new BasicDBObject("$exists", true))).count(),
                     annotationCollection.count());
    }

    /**
     * - st field should migrate form Variant to Annotation
     * - maf field should be added into Variant
     * - fid field should be removed from Annotation
     */
    @Test
    public void variantWithAnnotationShouldMigrate() {
        MongoTemplate mongoTemplate = new MongoTemplate(new Fongo("testServer").getMongo(), "variantWithAnnotation");
        DBCollection variantsCollection = mongoTemplate.getCollection(VARIANT_COLLECTION_NAME);
        DBCollection annotationCollection = mongoTemplate.getCollection(ANNOTATION_COLLECTION_NAME);

        DBObject variantWithSt = (DBObject) JSON.parse(VariantData.VARIANT_WITH_ST_1);
        variantsCollection.insert(variantWithSt);

        BasicDBObject originalStField = (BasicDBObject) ((BasicDBList) variantsCollection.findOne().get("st")).get(0);

        Set<String> idsInVariantsCollection = retrieveIdsFromVariantsWithSt(variantsCollection);

        extractAnnotationFromVariant.migrateAnnotation(mongoTemplate);

        assertEquals(1, variantsCollection.count());
        DBObject variantObj = variantsCollection.findOne();

        assertNotNull(variantObj.get("maf"));
        double newMafInVariant = (Double) ((BasicDBList) variantObj.get("maf")).get(0);
        double mafInStVariant = (Double) ((BasicDBObject) ((BasicDBList) variantWithSt.get("st")).get(0)).get("maf");

        assertEquals(mafInStVariant, newMafInVariant, 0);

        assertEquals(variantsCollection.find(new BasicDBObject("st", new BasicDBObject("$exists", true))).count(),
                     annotationCollection.count());
        DBObject annotationObj = annotationCollection.findOne();
        assertNull(annotationObj.get("fid"));

        assertEquals(variantObj.get("_id"), annotationObj.get("vid"));
        assertEquals(variantObj.get("chr"), annotationObj.get("chr"));
        assertEquals(variantObj.get("start"), annotationObj.get("start"));
        assertEquals(variantObj.get("ref"), annotationObj.get("ref"));
        assertEquals(variantObj.get("alt"), annotationObj.get("alt"));
        assertEquals(originalStField.get("cid"), annotationObj.get("cid"));
        assertEquals(originalStField.get("sid"), annotationObj.get("sid"));
        assertEquals(originalStField.get("maf"), annotationObj.get("maf"));
        assertEquals(originalStField.get("mgf"), annotationObj.get("mgf"));
        assertEquals(originalStField.get("mafAl"), annotationObj.get("mafAl"));
        assertEquals(originalStField.get("mgfGt"), annotationObj.get("mgfGt"));
        assertEquals(originalStField.get("missAl"), annotationObj.get("missAl"));
        assertEquals(originalStField.get("missGt"), annotationObj.get("missGt"));
        assertEquals(originalStField.get("numGt"), annotationObj.get("numGt"));

        Set<String> idsInAnnotationCollection = retrieveIdsFromAnnotationCollection(annotationCollection);

        assertEquals(idsInVariantsCollection, idsInAnnotationCollection);
    }

    @Test
    public void stFieldShouldBeRemovedFromVariant() {
        MongoTemplate mongoTemplate = new MongoTemplate(new Fongo("testServer").getMongo(),
                                                        "stFieldShouldBeRemovedFromVariant");
        DBCollection variantsCollection = mongoTemplate.getCollection(VARIANT_COLLECTION_NAME);

        DBObject variantWithSt = (DBObject) JSON.parse(VariantData.VARIANT_WITH_ST_1);
        variantsCollection.insert(variantWithSt);

        extractAnnotationFromVariant.removeAnnotationFromVariants(mongoTemplate);

        assertEquals(1, variantsCollection.count());
        DBObject variantObj = variantsCollection.findOne();
        assertNull(variantObj.get("st"));
    }

    @Test
    public void mafFieldShouldBeFloatingPoint() {
        MongoTemplate mongoTemplate = new MongoTemplate(new Fongo("testServer").getMongo(),
                                                        "mafFieldShouldBeFloatingPoint");
        DBCollection variantsCollection = mongoTemplate.getCollection(VARIANT_COLLECTION_NAME);

        DBObject variantWithIntegerMaf = (DBObject) JSON.parse(VariantData.VARIANT_WITH_INTEGER_MAF);
        variantsCollection.insert(variantWithIntegerMaf);

        extractAnnotationFromVariant.migrateAnnotation(mongoTemplate);
        extractAnnotationFromVariant.removeAnnotationFromVariants(mongoTemplate);

        assertEquals(1, variantsCollection.count());
        DBObject variantObj = variantsCollection.findOne();
        assertNotNull(variantObj.get("maf"));

        BasicDBList mafs = (BasicDBList) variantObj.get("maf");
        assertNotNull(mafs.get(0));
        assertEquals(3.0, (double) mafs.get(0), 0);
    }

    @Test(expected = NumberFormatException.class)
    public void startFieldShouldBeInteger() {
        MongoTemplate mongoTemplate = new MongoTemplate(new Fongo("testServer").getMongo(),
                                                        "startFieldShouldBeInteger");
        DBCollection variantsCollection = mongoTemplate.getCollection(VARIANT_COLLECTION_NAME);

        DBObject variantWithSt = (DBObject) JSON.parse(VariantData.VARIANT_WITH_FLOATING_START);
        variantsCollection.insert(variantWithSt);

        extractAnnotationFromVariant.migrateAnnotation(mongoTemplate);
    }

    @Test
    public void variantWithMultipleAnnotationShouldMigrate() {
        MongoTemplate mongoTemplate = new MongoTemplate(new Fongo("testServer").getMongo(),
                                                        "variantWithMultipleAnnotation");
        DBCollection variantsCollection = mongoTemplate.getCollection(VARIANT_COLLECTION_NAME);

        DBObject variant = (DBObject) JSON.parse(VariantData.VARIANT_WITH_MULTIPLE_ST);
        variantsCollection.insert(variant);

        extractAnnotationFromVariant.migrateAnnotation(mongoTemplate);
        extractAnnotationFromVariant.removeAnnotationFromVariants(mongoTemplate);

        DBCursor variantCursor = variantsCollection.find();
        while (variantCursor.hasNext()) {
            DBObject variantObj = variantCursor.next();

            assertNull(variantObj.get("st"));
            assertNotNull(variantObj.get("maf"));
            assertEquals(2, ((BasicDBList) variantObj.get("maf")).size());
        }
    }

    @Test
    public void multipleVariantsMigration() {
        MongoTemplate mongoTemplate = new MongoTemplate(new Fongo("testServer").getMongo(), "multipleVariants");
        DBCollection variantsCollection = mongoTemplate.getCollection(VARIANT_COLLECTION_NAME);
        DBCollection annotationCollection = mongoTemplate.getCollection(ANNOTATION_COLLECTION_NAME);

        DBObject var1 = (DBObject) JSON.parse(VariantData.VARIANT_WITHOUT_ST);
        DBObject var2 = (DBObject) JSON.parse(VariantData.VARIANT_WITH_ST_1);
        DBObject var3 = (DBObject) JSON.parse(VariantData.VARIANT_WITH_ST_2);
        DBObject var4 = (DBObject) JSON.parse(VariantData.VARIANT_WITH_ST_3);
        DBObject var5 = (DBObject) JSON.parse(VariantData.VARIANT_WITH_MULTIPLE_ST);

        variantsCollection.insert(var1);
        variantsCollection.insert(var2);
        variantsCollection.insert(var3);
        variantsCollection.insert(var4);
        variantsCollection.insert(var5);

        Set<String> idsInVariantsCollection = retrieveIdsFromVariantsWithSt(variantsCollection);

        extractAnnotationFromVariant.migrateAnnotation(mongoTemplate);
        extractAnnotationFromVariant.removeAnnotationFromVariants(mongoTemplate);

        assertEquals(5, variantsCollection.count());
        assertEquals(5, annotationCollection.count());

        Set<String> idsInAnnotationCollection = retrieveIdsFromAnnotationCollection(annotationCollection);

        assertEquals(idsInVariantsCollection, idsInAnnotationCollection);
    }

    private Set<String> retrieveIdsFromVariantsWithSt(DBCollection collection) {
        return collection.find(new BasicDBObject("st", new BasicDBObject("$exists", true)), new BasicDBObject("_id", 1))
                .toArray().stream().map(vidMap -> (String) vidMap.get("_id")).collect(Collectors.toSet());
    }

    private Set<String> retrieveIdsFromAnnotationCollection(DBCollection collection) {
        return collection.find(new BasicDBObject(), new BasicDBObject("vid", 1).append("_id", false)).toArray().stream()
                .map(vidMap -> (String) vidMap.get("vid")).collect(Collectors.toSet());
    }
}
