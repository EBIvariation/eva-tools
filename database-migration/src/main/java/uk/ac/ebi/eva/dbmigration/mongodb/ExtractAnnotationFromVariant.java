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

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateOneModel;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import static com.mongodb.client.model.Updates.set;

/**
 * Script that executes the following steps using mongobee (https://github.com/mongobee/mongobee/wiki/How-to-use-mongobee):
 * - Extracts the 'annot' field from a Variant stored in MongoDB into a new annotation object
 * - Leaves only some fields in the variants collection
 * - update the annotationMetadata collection with the VEP versions provided
 */
@ChangeLog
public class ExtractAnnotationFromVariant {

    private static final Logger logger = LoggerFactory.getLogger(ExtractAnnotationFromVariant.class);

    private final static int BULK_SIZE = 1000;

    static final String ID_FIELD = "_id";

    static final String CHROMOSOME_FIELD = "chr";

    static final String START_FIELD = "start";

    static final String END_FIELD = "end";

    static final String ANNOT_FIELD = "annot";

    static final String XREFS_FIELD = "xrefs";

    static final String CONSEQUENCE_TYPE_FIELD = "ct";

    static final String SO_FIELD = "so";

    static final String SIFT_FIELD = "sift";

    static final String POLYPHEN_FIELD = "polyphen";

    static final String VEP_VERSION_FIELD = "vepVersion";

    static final String CACHE_VERSION_FIELD = "cacheVersion";

    static final String SCORE_FIELD = "sc";

    static final String XREF_ID_FIELD = "id";

    private static DatabaseParameters databaseParameters;

    public static void setDatabaseParameters(DatabaseParameters databaseParameters) {
        ExtractAnnotationFromVariant.databaseParameters = databaseParameters;
    }

    @ChangeSet(order = "001", id = "migrateAnnotation", author = "EVA")
    public void migrateAnnotation(MongoDatabase mongoDatabase) {
        final MongoCollection<Document> variantsCollection = getVariantsCollection(mongoDatabase);
        final MongoCollection<Document> annotationCollection = getAnnotationsCollection(mongoDatabase);
        logger.info("1) migrate annotation from collection {}", variantsCollection.getNamespace());

        long counter = 0;
        long inserted = 0;
        BulkWriteOptions unorderedBulk = new BulkWriteOptions().ordered(false);
        try (MongoCursor<Document> cursor = variantsCollection.find().iterator()) {
            boolean cursorIsConsumed;
            while (true) {
                List<InsertOneModel<Document>> annotationsToInsert = getBatch(cursor, BULK_SIZE, this::buildInsertion);
                cursorIsConsumed = annotationsToInsert.isEmpty();
                if (cursorIsConsumed) {
                    break;
                }

                counter += annotationsToInsert.size();
                BulkWriteResult bulkInsert = annotationCollection.bulkWrite(annotationsToInsert, unorderedBulk);
                inserted += bulkInsert.getInsertedCount();
            }
        }

        //before executing the next changeSet check that the count of read and written annotation documents match
        if (counter != inserted) {
            throw new RuntimeException(
                    "The number of processed Variants (" + counter + ") is different from the number of new annotation "
                            + "inserted (" + inserted + "). The '" + ANNOT_FIELD + "' field will not be removed "
                            + "from the " + variantsCollection.getNamespace() + " collection.");
        }
    }

    private MongoCollection<Document> getVariantsCollection(MongoDatabase mongoDatabase) {
        String variantsCollectionName = databaseParameters.getDbCollectionsVariantsName();
        Objects.requireNonNull(variantsCollectionName, "please provide the variants collection name");
        return mongoDatabase.getCollection(variantsCollectionName);
    }

    private MongoCollection<Document> getAnnotationsCollection(MongoDatabase mongoDatabase) {
        String collectionName = databaseParameters .getDbCollectionsAnnotationsName();
        Objects.requireNonNull(collectionName, "please provide the annotations collection name");
        return mongoDatabase.getCollection(collectionName);
    }

    /**
     * Return a batch of Documents processed, advancing the cursor provided.
     * @param cursor won't be closed, please close it outside this function.
     * @return A list with processed documents, or an empty list if there are no more elements in the cursor.
     */
    private <T> List<T> getBatch(MongoCursor<Document> cursor, int bulkSize, Function<Document, T> transformation) {
        List<T> batch = new ArrayList<>();
        int counter = 0;
        while (cursor.hasNext()) {
            T element = transformation.apply(cursor.next());

            if (element != null) {
                counter++;
                batch.add(element);
                if (counter % bulkSize == 0) {
                    return batch;
                }
            }
        }
        return batch;
    }

    private InsertOneModel<Document> buildInsertion(Document variantDocument) {
        Document annotationSubdocument = (Document) variantDocument.get(ANNOT_FIELD);
        if (annotationSubdocument == null) {
            return null;
        } else {
            return getInsertionDocument(variantDocument, annotationSubdocument);
        }
    }

    private static InsertOneModel<Document> getInsertionDocument(Document variantDocument, Document annotationSubdocument) {
        annotationSubdocument.put(ID_FIELD, buildAnnotationId(variantDocument));
        annotationSubdocument.put(CHROMOSOME_FIELD, variantDocument.get("chr"));
        annotationSubdocument.put(START_FIELD, variantDocument.get("start"));
        annotationSubdocument.put(END_FIELD, variantDocument.get("end"));
        annotationSubdocument.put(VEP_VERSION_FIELD, databaseParameters.getVepVersion());
        annotationSubdocument.put(CACHE_VERSION_FIELD, databaseParameters.getVepCacheVersion());
        return new InsertOneModel<>(annotationSubdocument);
    }

    private static String buildAnnotationId(Document variantDocument) {
        return variantDocument.get("_id") + "_" + databaseParameters.getVepVersion() + "_" + databaseParameters
                .getVepCacheVersion();
    }

    @ChangeSet(order = "002", id = "reduceAnnotationFromVariants", author = "EVA")
    public void reduceAnnotationFromVariants(MongoDatabase mongoDatabase) {
        final MongoCollection<Document> variantsCollection = getVariantsCollection(mongoDatabase);
        logger.info("2) reduce annotation field from collection {}", variantsCollection.getNamespace());

        long counter = 0;
        long updated = 0;
        BulkWriteOptions unorderedBulk = new BulkWriteOptions().ordered(false);
        try (MongoCursor<Document> cursor = variantsCollection.find().iterator()) {
            boolean cursorIsConsumed;
            while (true) {
                List<UpdateOneModel<Document>> annotationsToUpdate = getBatch(cursor, BULK_SIZE, this::buildUpdate);
                cursorIsConsumed = annotationsToUpdate.isEmpty();
                if (cursorIsConsumed) {
                    break;
                }
                counter += annotationsToUpdate.size();
                BulkWriteResult bulkInsert = variantsCollection.bulkWrite(annotationsToUpdate, unorderedBulk);
                updated += bulkInsert.getModifiedCount();
            }
        }
        if (counter != updated) {
            throw new RuntimeException(
                    "The number of processed Variants (" + counter + ") is different from the number of annotation "
                            + "updated (" + updated + ").");
        }
    }

    private UpdateOneModel<Document> buildUpdate(Document variantDocument) {
        Document annotationSubdocument = (Document) variantDocument.get(ANNOT_FIELD);
        if (annotationSubdocument == null) {
            return null;
        } else {
            return getUpdateDocument(variantDocument, annotationSubdocument);
        }
    }

    private static UpdateOneModel<Document> getUpdateDocument(Document variantDocument, Document annotationSubdocument) {
        Set<Integer> soSet = computeSoSet(annotationSubdocument, CONSEQUENCE_TYPE_FIELD, SO_FIELD);
        Set<String> xrefSet = computeXrefSet(annotationSubdocument, XREFS_FIELD, XREF_ID_FIELD);
        List<Double> sift = computeMinAndMaxScore(annotationSubdocument, SIFT_FIELD);
        List<Double> polyphen = computeMinAndMaxScore(annotationSubdocument, POLYPHEN_FIELD);

        Document newAnnotationSubdocument = new Document()
                .append(VEP_VERSION_FIELD, databaseParameters.getVepVersion())
                .append(CACHE_VERSION_FIELD, databaseParameters.getVepCacheVersion())
                .append(SO_FIELD, soSet)
                .append(XREFS_FIELD, xrefSet)
                .append(SIFT_FIELD, sift)
                .append(POLYPHEN_FIELD, polyphen);
        List<Document> newAnnotationArray = Collections.singletonList(newAnnotationSubdocument);

        Document query = new Document(ID_FIELD, variantDocument.get(ID_FIELD));
        Bson update = set(ANNOT_FIELD, newAnnotationArray);
        return new UpdateOneModel<>(query, update);
    }

    private static Set<Integer> computeSoSet(Document originalAnnotField, String outerField, String innerField) {
        Set<Integer> soSet = new TreeSet<>();

        List<Document> cts = (List<Document>) originalAnnotField.get(outerField);
        if (cts != null) {
            for (Document ct : cts) {
                soSet.addAll(((List<Integer>) ct.get(innerField)));
            }
        }

        return soSet;
    }

    private static Set<String> computeXrefSet(Document originalAnnotField, String outerField, String innerField) {
        Set<String> xrefSet = new TreeSet<>();

        List<Document> cts = (List<Document>) originalAnnotField.get(outerField);
        if (cts != null) {
            for (Document ct : cts) {
                xrefSet.add(ct.getString(innerField));
            }
        }

        return xrefSet;
    }

    private static List<Double> computeMinAndMaxScore(Document originalAnnotField, String field) {
        Double min = Double.POSITIVE_INFINITY;
        Double max = Double.NEGATIVE_INFINITY;

        List<Document> cts = (List<Document>) originalAnnotField.get(CONSEQUENCE_TYPE_FIELD);
        if (cts != null) {
            for (Document ct : cts) {
                Document document = ((Document) ct.get(field));

                if (document != null) {
                    Double score = (Double) document.get(SCORE_FIELD);

                    min = Math.min(min, score);
                    max = Math.max(max, score);
                }
            }
        }
        return Arrays.asList(min, max);
    }

    @ChangeSet(order = "003", id = "updateAnnotationMetadata", author = "EVA")
    public void updateAnnotationMetadata(MongoDatabase mongoDatabase) {

        final MongoCollection<Document> annotationMetadataCollection = getAnnotationMetadataCollection(mongoDatabase);
        logger.info("3) update annotation metadata in collection {}", annotationMetadataCollection.getNamespace());

        Document metadata = new Document(VEP_VERSION_FIELD, databaseParameters.getVepVersion())
                .append(CACHE_VERSION_FIELD, databaseParameters.getVepCacheVersion());
        annotationMetadataCollection.insertOne(metadata);
    }

    private MongoCollection<Document> getAnnotationMetadataCollection(MongoDatabase mongoDatabase) {
        String collectionName = databaseParameters .getDbCollectionsAnnotationMetadataName();
        Objects.requireNonNull(collectionName, "please provide the annotationMetadata collection name");
        return mongoDatabase.getCollection(collectionName);
    }
}
