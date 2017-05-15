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
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.eva.dbmigration.mongodb.ExtractAnnotationFromVariant.ANNOT_FIELD;
import static uk.ac.ebi.eva.dbmigration.mongodb.ExtractAnnotationFromVariant.CACHE_VERSION_FIELD;
import static uk.ac.ebi.eva.dbmigration.mongodb.ExtractAnnotationFromVariant.CHROMOSOME_FIELD;
import static uk.ac.ebi.eva.dbmigration.mongodb.ExtractAnnotationFromVariant.CONSEQUENCE_TYPE_FIELD;
import static uk.ac.ebi.eva.dbmigration.mongodb.ExtractAnnotationFromVariant.END_FIELD;
import static uk.ac.ebi.eva.dbmigration.mongodb.ExtractAnnotationFromVariant.ID_FIELD;
import static uk.ac.ebi.eva.dbmigration.mongodb.ExtractAnnotationFromVariant.POLYPHEN_FIELD;
import static uk.ac.ebi.eva.dbmigration.mongodb.ExtractAnnotationFromVariant.SCORE_FIELD;
import static uk.ac.ebi.eva.dbmigration.mongodb.ExtractAnnotationFromVariant.SIFT_FIELD;
import static uk.ac.ebi.eva.dbmigration.mongodb.ExtractAnnotationFromVariant.SO_FIELD;
import static uk.ac.ebi.eva.dbmigration.mongodb.ExtractAnnotationFromVariant.START_FIELD;
import static uk.ac.ebi.eva.dbmigration.mongodb.ExtractAnnotationFromVariant.VEP_VERSION_FIELD;
import static uk.ac.ebi.eva.dbmigration.mongodb.ExtractAnnotationFromVariant.XREFS_FIELD;
import static uk.ac.ebi.eva.dbmigration.mongodb.ExtractAnnotationFromVariant.XREF_ID_FIELD;

/**
 * Test {@link ExtractAnnotationFromVariant}
 */
public class ExtractAnnotationFromVariantTest {

    private static final String VEP_VERSION = "88";

    private static final String CACHE_VERSION = "90";

    private static final String VARIANT_COLLECTION_NAME = "variantsCollection";

    private static final String ANNOTATION_COLLECTION_NAME = "annotationCollection";

    private static final String ANNOTATION_METADATA_COLLECTION_NAME = "annotationMetadataCollection";

    private static final String READ_PREFERENCE = "primary";

    private ExtractAnnotationFromVariant extractAnnotationFromVariant;

    @Before
    public void setUp() throws Exception {
        extractAnnotationFromVariant = new ExtractAnnotationFromVariant();
    }

    @Test
    public void variantWithoutAnnotationShouldNotChange() {
        String dbName = "variantWithoutAnnotation";

        Properties properties = new Properties();
        properties.put(DatabaseParameters.VEP_VERSION, VEP_VERSION);
        properties.put(DatabaseParameters.VEP_CACHE_VERSION, CACHE_VERSION);
        properties.put(DatabaseParameters.DB_NAME, dbName);
        properties.put(DatabaseParameters.DB_COLLECTIONS_VARIANTS_NAME, VARIANT_COLLECTION_NAME);
        properties.put(DatabaseParameters.DB_COLLECTIONS_ANNOTATIONS_NAME, ANNOTATION_COLLECTION_NAME);
        properties.put(DatabaseParameters.DB_COLLECTIONS_ANNOTATION_METADATA_NAME, ANNOTATION_METADATA_COLLECTION_NAME);
        properties.put(DatabaseParameters.DB_READ_PREFERENCE, READ_PREFERENCE);
        DatabaseParameters databaseParameters = new DatabaseParameters();
        databaseParameters.load(properties);
        ExtractAnnotationFromVariant.setDatabaseParameters(databaseParameters);

        MongoDatabase database = new Fongo("testServer").getMongo().getDatabase(dbName);
        MongoCollection<Document> variantsCollection = database.getCollection(VARIANT_COLLECTION_NAME);
        MongoCollection<Document> annotationCollection = database.getCollection(ANNOTATION_COLLECTION_NAME);


        Document variantWithoutAnnot = Document.parse(VariantData.VARIANT_WITHOUT_ANNOT);
        variantsCollection.insertOne(variantWithoutAnnot);

        extractAnnotationFromVariant.migrateAnnotation(database);
        extractAnnotationFromVariant.reduceAnnotationFromVariants(database);

        try (MongoCursor<Document> variantCursor = variantsCollection.find().iterator()) {
            while (variantCursor.hasNext()) {
                Document variantObj = variantCursor.next();
                assertNull(variantObj.get(ANNOT_FIELD));
            }
        }

        assertEquals(0, variantsCollection.count(new BasicDBObject(ANNOT_FIELD, new BasicDBObject("$exists", true))));
        assertEquals(0, annotationCollection.count());
    }

    @Test
    public void variantWithAnnotationShouldMigrate() {
        // given
        String dbName = "variantWithAnnotation";

        Properties properties = new Properties();
        properties.put(DatabaseParameters.VEP_VERSION, VEP_VERSION);
        properties.put(DatabaseParameters.VEP_CACHE_VERSION, CACHE_VERSION);
        properties.put(DatabaseParameters.DB_NAME, dbName);
        properties.put(DatabaseParameters.DB_COLLECTIONS_VARIANTS_NAME, VARIANT_COLLECTION_NAME);
        properties.put(DatabaseParameters.DB_COLLECTIONS_ANNOTATIONS_NAME, ANNOTATION_COLLECTION_NAME);
        properties.put(DatabaseParameters.DB_COLLECTIONS_ANNOTATION_METADATA_NAME, ANNOTATION_METADATA_COLLECTION_NAME);
        properties.put(DatabaseParameters.DB_READ_PREFERENCE, READ_PREFERENCE);
        DatabaseParameters databaseParameters = new DatabaseParameters();
        databaseParameters.load(properties);
        ExtractAnnotationFromVariant.setDatabaseParameters(databaseParameters);

        MongoDatabase database = new Fongo("testServer").getMongo().getDatabase(dbName);
        MongoCollection<Document> variantsCollection = database.getCollection(VARIANT_COLLECTION_NAME);
        MongoCollection<Document> annotationCollection = database.getCollection(ANNOTATION_COLLECTION_NAME);

        Document variantWithAnnot = Document.parse(VariantData.VARIANT_WITH_ANNOT_1);
        variantsCollection.insertOne(variantWithAnnot);

        Document originalVariant = variantsCollection.find().first();
        Document originalAnnotField = (Document) originalVariant.get(ANNOT_FIELD);

        // when
        extractAnnotationFromVariant.migrateAnnotation(database);

        // then
        assertEquals(1, annotationCollection.count());

        Document annotation = annotationCollection.find().first();

        String versionSuffix = "_" + databaseParameters.getVepVersion() + "_" + databaseParameters.getVepCacheVersion();
        assertEquals(originalVariant.get(ID_FIELD) + versionSuffix, annotation.get(ID_FIELD));
        assertEquals(originalVariant.get(CHROMOSOME_FIELD), annotation.get(CHROMOSOME_FIELD));
        assertEquals(originalVariant.get(START_FIELD), annotation.get(START_FIELD));
        assertEquals(originalVariant.get(END_FIELD), annotation.get(END_FIELD));
        assertEquals(VEP_VERSION, annotation.get(VEP_VERSION_FIELD));
        assertEquals(CACHE_VERSION, annotation.get(CACHE_VERSION_FIELD));
        assertEquals(originalAnnotField.get(CONSEQUENCE_TYPE_FIELD), annotation.get(CONSEQUENCE_TYPE_FIELD));
        assertEquals(originalAnnotField.get(XREFS_FIELD), annotation.get(XREFS_FIELD));
    }

    @Test
    public void variantWithAnnotationShouldKeepSomeFields() {
        // given
        String dbName = "variantWithAnnotation";

        Properties properties = new Properties();
        properties.put(DatabaseParameters.VEP_VERSION, VEP_VERSION);
        properties.put(DatabaseParameters.VEP_CACHE_VERSION, CACHE_VERSION);
        properties.put(DatabaseParameters.DB_NAME, dbName);
        properties.put(DatabaseParameters.DB_COLLECTIONS_VARIANTS_NAME, VARIANT_COLLECTION_NAME);
        properties.put(DatabaseParameters.DB_COLLECTIONS_ANNOTATIONS_NAME, ANNOTATION_COLLECTION_NAME);
        properties.put(DatabaseParameters.DB_COLLECTIONS_ANNOTATION_METADATA_NAME, ANNOTATION_METADATA_COLLECTION_NAME);
        properties.put(DatabaseParameters.DB_READ_PREFERENCE, READ_PREFERENCE);
        DatabaseParameters databaseParameters = new DatabaseParameters();
        databaseParameters.load(properties);
        ExtractAnnotationFromVariant.setDatabaseParameters(databaseParameters);

        MongoDatabase database = new Fongo("testServer").getMongo().getDatabase(dbName);
        MongoCollection<Document> variantsCollection = database.getCollection(VARIANT_COLLECTION_NAME);

        Document variantWithAnnot = Document.parse(VariantData.VARIANT_WITH_ANNOT_2);
        variantsCollection.insertOne(variantWithAnnot);

        Document originalVariant = variantsCollection.find().first();
        Document originalAnnotField = (Document) originalVariant.get(ANNOT_FIELD);

        // when
        extractAnnotationFromVariant.reduceAnnotationFromVariants(database);

        // then
        assertEquals(1, variantsCollection.count());

        Document variant = variantsCollection.find().first();
        List newAnnotField = (List) variant.get(ANNOT_FIELD);
        Document newAnnotElement = (Document) newAnnotField.get(0);

        assertEquals(VEP_VERSION, newAnnotElement.get(VEP_VERSION_FIELD));
        assertEquals(CACHE_VERSION, newAnnotElement.get(CACHE_VERSION_FIELD));
        ArrayList<Integer> so = (ArrayList<Integer>) newAnnotElement.get(SO_FIELD);
        Set<Integer> expectedSo = computeSo(originalAnnotField);
        assertEquals(expectedSo.size(), so.size());
        assertEquals(expectedSo, new TreeSet<>(so));
        ArrayList<String> xrefs = (ArrayList<String>) newAnnotElement.get(XREFS_FIELD);
        Set<String> expectedXref = computeXref(originalAnnotField);
        assertEquals(expectedXref.size(), xrefs.size());
        assertEquals(expectedXref, new TreeSet<>(xrefs));
        assertEquals(computeSift(originalAnnotField), newAnnotElement.get(SIFT_FIELD));
        assertEquals(computePolyphen(originalAnnotField), newAnnotElement.get(POLYPHEN_FIELD));
    }

    private Set<Integer> computeSo(Document originalAnnotField) {
        Set<Integer> soSet = new TreeSet<>();

        List<Document> cts = (List<Document>) originalAnnotField.get(CONSEQUENCE_TYPE_FIELD);
        for (Document ct : cts) {
            soSet.addAll(((List<Integer>) ct.get(SO_FIELD)));
        }

        return soSet;
    }

    private Set<String> computeXref(Document originalAnnotField) {
        Set<String> xrefSet = new TreeSet<>();

        List<Document> cts = (List<Document>) originalAnnotField.get(XREFS_FIELD);
        for (Document ct : cts) {
            xrefSet.add(((String) ct.get(XREF_ID_FIELD)));
        }

        return xrefSet;
    }

    private List<Double> computeSift(Document originalAnnotField) {
        Double min = Double.POSITIVE_INFINITY;
        Double max = Double.NEGATIVE_INFINITY;

        List<Document> cts = (List<Document>) originalAnnotField.get(CONSEQUENCE_TYPE_FIELD);
        for (Document ct : cts) {
            Document document = (Document) ct.get(SIFT_FIELD);
            if (document != null) {
                Double score = (Double) document.get(SCORE_FIELD);

                min = Math.min(min, score);
                max = Math.max(max, score);
            }
        }

        return Arrays.asList(min, max);
    }

    private List<Double> computePolyphen(Document originalAnnotField) {
        Double min = Double.POSITIVE_INFINITY;
        Double max = Double.NEGATIVE_INFINITY;

        List<Document> cts = (List<Document>) originalAnnotField.get(CONSEQUENCE_TYPE_FIELD);
        for (Document ct : cts) {
            Document document = (Document) ct.get(POLYPHEN_FIELD);
            if (document != null) {
                Double score = (Double) document.get(SCORE_FIELD);

                min = Math.min(min, score);
                max = Math.max(max, score);
            }
        }
        return Arrays.asList(min, max);
    }

    @Test
    public void metadataShouldBeUpdated() throws Exception {
        // given
        String dbName = "DBForAnnotationMetadataCheck";

        Properties properties = new Properties();
        properties.put(DatabaseParameters.VEP_VERSION, VEP_VERSION);
        properties.put(DatabaseParameters.VEP_CACHE_VERSION, CACHE_VERSION);
        properties.put(DatabaseParameters.DB_NAME, dbName);
        properties.put(DatabaseParameters.DB_COLLECTIONS_VARIANTS_NAME, VARIANT_COLLECTION_NAME);
        properties.put(DatabaseParameters.DB_COLLECTIONS_ANNOTATIONS_NAME, ANNOTATION_COLLECTION_NAME);
        properties.put(DatabaseParameters.DB_COLLECTIONS_ANNOTATION_METADATA_NAME, ANNOTATION_METADATA_COLLECTION_NAME);
        properties.put(DatabaseParameters.DB_READ_PREFERENCE, READ_PREFERENCE);
        DatabaseParameters databaseParameters = new DatabaseParameters();
        databaseParameters.load(properties);
        ExtractAnnotationFromVariant.setDatabaseParameters(databaseParameters);

        MongoDatabase database = new Fongo("testServer").getMongo().getDatabase(dbName);
        MongoCollection<Document> variantsCollection = database.getCollection(VARIANT_COLLECTION_NAME);
        MongoCollection<Document> annotationMetadataCollection = database.getCollection(
                ANNOTATION_METADATA_COLLECTION_NAME);

        Document variantWithAnnot = Document.parse(VariantData.VARIANT_WITH_ANNOT_2);
        variantsCollection.insertOne(variantWithAnnot);

        // when
        extractAnnotationFromVariant.updateAnnotationMetadata(database);

        // then
        assertEquals(1, annotationMetadataCollection.count());
        assertEquals(VEP_VERSION, annotationMetadataCollection.find().first().get(VEP_VERSION_FIELD));
        assertEquals(CACHE_VERSION, annotationMetadataCollection.find().first().get(CACHE_VERSION_FIELD));
    }

}
