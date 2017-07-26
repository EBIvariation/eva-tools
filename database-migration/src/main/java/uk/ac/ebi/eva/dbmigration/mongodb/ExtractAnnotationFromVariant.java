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
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.util.stream.Collectors.toList;

/**
 * Script that executes the following steps using mongobee (https://github.com/mongobee/mongobee/wiki/How-to-use-mongobee):
 * - Extracts the 'annot' field from a variant stored in MongoDB into a new annotations collection
 * - Drops the indexes for the old annotation
 * - Leaves only some fields in the variants collection
 * - update the annotationMetadata collection with the VEP versions provided
 * - creates the new indexes in the variants and annotations collections
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

    static final String VEP_VERSION_FIELD = "vepv";

    static final String CACHE_VERSION_FIELD = "cachev";

    static final String SCORE_FIELD = "sc";

    static final String XREF_ID_FIELD = "id";

    private static final String LEGACY_ANNOTATION_CT_SO_INDEX = "annot.ct.so_1";

    private static final String LEGACY_ANNOTATION_XREF_ID_INDEX = "annot.xrefs.id_1";

    private static final Document EXISTS = new Document("$exists", true);

    public static final String DEFAULT_VERSION_FIELD = "is_default";

    private static DatabaseParameters databaseParameters;

    public static void setDatabaseParameters(DatabaseParameters databaseParameters) {
        ExtractAnnotationFromVariant.databaseParameters = databaseParameters;
    }

    @ChangeSet(order = "001", id = "migrateAnnotation", author = "EVA")
    public void migrateAnnotation(MongoDatabase mongoDatabase) {
        final MongoCollection<Document> variantsCollection = mongoDatabase.getCollection(
                databaseParameters.getDbCollectionsVariantsName());
        final MongoCollection<Document> annotationCollection = mongoDatabase.getCollection(
                databaseParameters.getDbCollectionsAnnotationsName());
        logger.info("1) migrate annotation from collection {}", variantsCollection.getNamespace());

        long annotationsReadCount = 0;
        long annotationsWrittenCount = 0;
        BulkWriteOptions unorderedBulk = new BulkWriteOptions().ordered(false);
        Document onlyAnnotatedVariants = new Document(ANNOT_FIELD, EXISTS);
        try (MongoCursor<Document> cursor = variantsCollection.find(onlyAnnotatedVariants).iterator()) {
            while (true) {
                List<InsertOneModel<Document>> annotationsToInsert = getBatch(cursor, BULK_SIZE)
                        .stream()
                        .map(this::buildInsertionDocument)
                        .collect(toList());

                if (annotationsToInsert.isEmpty()) {
                    break;
                }

                annotationsReadCount += annotationsToInsert.size();
                BulkWriteResult bulkInsert = annotationCollection.bulkWrite(annotationsToInsert, unorderedBulk);
                annotationsWrittenCount += bulkInsert.getInsertedCount();
            }
        }

        //before executing the next changeSet check that the count of read and written annotation documents match
        if (annotationsReadCount != annotationsWrittenCount) {
            throw new RuntimeException(
                    "The number of processed Variants (" + annotationsReadCount
                            + ") is different from the number of new annotation inserted (" + annotationsWrittenCount
                            + "). The '" + ANNOT_FIELD + "' field will not be removed from the "
                            + variantsCollection.getNamespace() + " collection.");
        }
    }

    /**
     * Return a batch of elements, advancing the Iterator provided.
     * @param iterator won't be closed, please close it outside this function.
     * @param bulkSize maximum size for the batch. The list returned can be smaller.
     * @return A list with elements, or an empty list if there are no more elements in the iterator.
     */
    private <T> List<T> getBatch(Iterator<T> iterator, int bulkSize) {
        List<T> batch = new ArrayList<>();
        int counter = 0;
        while (iterator.hasNext()) {
            T element = iterator.next();
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

    private InsertOneModel<Document> buildInsertionDocument(Document variantDocument) {
        Document annotationSubdocument = (Document) variantDocument.get(ANNOT_FIELD);
        Assert.notNull(annotationSubdocument);

        annotationSubdocument.put(ID_FIELD, buildAnnotationId(variantDocument));
        annotationSubdocument.put(CHROMOSOME_FIELD, variantDocument.get("chr"));
        annotationSubdocument.put(START_FIELD, variantDocument.get("start"));
        annotationSubdocument.put(END_FIELD, variantDocument.get("end"));
        annotationSubdocument.put(VEP_VERSION_FIELD, databaseParameters.getVepVersion());
        annotationSubdocument.put(CACHE_VERSION_FIELD, databaseParameters.getVepCacheVersion());
        return new InsertOneModel<>(annotationSubdocument);
    }

    private String buildAnnotationId(Document variantDocument) {
        return variantDocument.get("_id")
                + "_" + databaseParameters.getVepVersion()
                + "_" + databaseParameters.getVepCacheVersion();
    }


    @ChangeSet(order = "002", id = "dropIndexes", author = "EVA")
    public void dropIndexes(MongoDatabase mongoDatabase) {
        final MongoCollection<Document> variantsCollection = mongoDatabase.getCollection(
                databaseParameters.getDbCollectionsVariantsName());
        logger.info("2) drop indexes from annot field from collection {}", variantsCollection.getNamespace());

        variantsCollection.dropIndex(LEGACY_ANNOTATION_CT_SO_INDEX);
        variantsCollection.dropIndex(LEGACY_ANNOTATION_XREF_ID_INDEX);
    }

    @ChangeSet(order = "003", id = "reduceAnnotationFromVariants", author = "EVA")
    public void reduceAnnotationFromVariants(MongoDatabase mongoDatabase) {
        final MongoCollection<Document> variantsCollection = mongoDatabase.getCollection(
                databaseParameters.getDbCollectionsVariantsName());
        logger.info("3) reduce annotation field from collection {}", variantsCollection.getNamespace());

        long annotationsReadCount = 0;
        long annotationsUpdatedCount = 0;
        BulkWriteOptions unorderedBulk = new BulkWriteOptions().ordered(false);
        Document onlyAnnotatedVariants = new Document(ANNOT_FIELD, EXISTS);
        try (MongoCursor<Document> cursor = variantsCollection.find(onlyAnnotatedVariants).iterator()) {
            while (true) {
                List<UpdateOneModel<Document>> annotationsToUpdate = getBatch(cursor, BULK_SIZE)
                        .stream()
                        .map(this::buildUpdateDocument)
                        .collect(toList());

                if (annotationsToUpdate.isEmpty()) {
                    break;
                }
                annotationsReadCount += annotationsToUpdate.size();
                BulkWriteResult bulkInsert = variantsCollection.bulkWrite(annotationsToUpdate, unorderedBulk);
                annotationsUpdatedCount += bulkInsert.getModifiedCount();
            }
        }
        if (annotationsReadCount != annotationsUpdatedCount) {
            throw new RuntimeException(
                    "The number of processed Variants (" + annotationsReadCount + ") is different from the number of annotation "
                            + "updated (" + annotationsUpdatedCount + ").");
        }
    }

    private UpdateOneModel<Document> buildUpdateDocument(Document variantDocument) {
        Document annotationSubdocument = (Document) variantDocument.get(ANNOT_FIELD);
        Assert.notNull(annotationSubdocument);

        Set<Integer> soSet = computeSoSet(annotationSubdocument);
        Set<String> xrefSet = computeXrefSet(annotationSubdocument);
        List<Double> sift = computeMinAndMaxScore(annotationSubdocument, SIFT_FIELD);
        List<Double> polyphen = computeMinAndMaxScore(annotationSubdocument, POLYPHEN_FIELD);

        Document newAnnotationSubdocument = new Document()
                .append(VEP_VERSION_FIELD, databaseParameters.getVepVersion())
                .append(CACHE_VERSION_FIELD, databaseParameters.getVepCacheVersion());

        if (!soSet.isEmpty()) {
            newAnnotationSubdocument.append(SO_FIELD, soSet);
        }
        if (!xrefSet.isEmpty()) {
            newAnnotationSubdocument.append(XREFS_FIELD, xrefSet);
        }
        if (!sift.isEmpty()) {
            newAnnotationSubdocument.append(SIFT_FIELD, sift);
        }
        if (!polyphen.isEmpty()) {
            newAnnotationSubdocument.append(POLYPHEN_FIELD, polyphen);
        }

        List<Document> newAnnotationArray = Collections.singletonList(newAnnotationSubdocument);

        Document query = new Document(ID_FIELD, variantDocument.get(ID_FIELD));
        Bson update = Updates.set(ANNOT_FIELD, newAnnotationArray);
        return new UpdateOneModel<>(query, update);
    }

    private Set<Integer> computeSoSet(Document originalAnnotationField) {
        Set<Integer> soSet = new TreeSet<>();

        List<Document> cts = (List<Document>) originalAnnotationField.get(CONSEQUENCE_TYPE_FIELD);
        if (cts != null) {
            for (Document ct : cts) {
                Object sos = ct.get(SO_FIELD);
                if (sos != null) {
                    soSet.addAll((List<Integer>) sos);
                }
            }
        }

        return soSet;
    }

    private Set<String> computeXrefSet(Document originalAnnotationField) {
        Set<String> xrefSet = new TreeSet<>();

        List<Document> cts = (List<Document>) originalAnnotationField.get(XREFS_FIELD);
        if (cts != null) {
            for (Document ct : cts) {
                String xref = ct.getString(XREF_ID_FIELD);
                if (xref != null) {
                    xrefSet.add(xref);
                }
            }
        }

        return xrefSet;
    }

    private List<Double> computeMinAndMaxScore(Document originalAnnotationField, String scoreType) {
        Double min = Double.POSITIVE_INFINITY;
        Double max = Double.NEGATIVE_INFINITY;
        boolean thereIsAtLeastOneScore = false;

        List<Document> cts = (List<Document>) originalAnnotationField.get(CONSEQUENCE_TYPE_FIELD);
        if (cts != null) {
            for (Document ct : cts) {
                Document document = ((Document) ct.get(scoreType));
                if (document != null) {
                    Double score = (Double) document.get(SCORE_FIELD);
                    if (score != null) {
                        min = Math.min(min, score);
                        max = Math.max(max, score);
                        thereIsAtLeastOneScore = true;
                    }
                }
            }
        }
        if (thereIsAtLeastOneScore) {
            return Arrays.asList(min, max);
        } else {
            return Collections.emptyList();
        }
    }

    @ChangeSet(order = "004", id = "updateAnnotationMetadata", author = "EVA")
    public void updateAnnotationMetadata(MongoDatabase mongoDatabase) {
        final MongoCollection<Document> annotationMetadataCollection = mongoDatabase.getCollection(
                databaseParameters.getDbCollectionsAnnotationMetadataName());
        logger.info("4) update annotation metadata in collection {}", annotationMetadataCollection.getNamespace());

        String id = databaseParameters.getVepVersion() + "_" + databaseParameters.getVepCacheVersion();
        Document metadata = new Document(ID_FIELD, id);
        if (annotationMetadataCollection.count(metadata) == 0) {
            metadata.append(VEP_VERSION_FIELD, databaseParameters.getVepVersion())
                    .append(CACHE_VERSION_FIELD, databaseParameters.getVepCacheVersion());

            annotationMetadataCollection.insertOne(metadata);
        }
    }

    @ChangeSet(order = "005", id = "createIndexes", author = "EVA")
    public void createIndexes(MongoDatabase mongoDatabase) {
        final MongoCollection<Document> variantsCollection = mongoDatabase.getCollection(
                databaseParameters.getDbCollectionsVariantsName());
        final MongoCollection<Document> annotationsCollection = mongoDatabase.getCollection(
                databaseParameters.getDbCollectionsAnnotationsName());
        logger.info("5) create indexes collections {} and {}",
                    annotationsCollection.getNamespace(), variantsCollection.getNamespace());

        IndexOptions background = new IndexOptions().background(true);
        variantsCollection.createIndex(new Document(ANNOT_FIELD + "." + XREFS_FIELD, 1), background);
        variantsCollection.createIndex(new Document(ANNOT_FIELD + "." + SO_FIELD, 1), background);

        annotationsCollection.createIndex(new Document(CONSEQUENCE_TYPE_FIELD + "." + SO_FIELD, 1), background);
        annotationsCollection.createIndex(new Document(XREFS_FIELD + "." + XREF_ID_FIELD, 1), background);
        annotationsCollection.createIndex(new Document(CHROMOSOME_FIELD, 1).append(START_FIELD, 1).append(END_FIELD, 1),
                                          background);
    }
    @ChangeSet(order = "006", id = "addDefaultVersionInAnnotationMetadata", author = "EVA")
    public void addDefaultVersion(MongoDatabase mongoDatabase) {
        final MongoCollection<Document> annotationMetadataCollection = mongoDatabase.getCollection(
                databaseParameters.getDbCollectionsAnnotationMetadataName());
        logger.info("6) add default annotation version to collection {} ", annotationMetadataCollection.getNamespace());

        Document allVersions = new Document();
        Document setDefaultToFalse = new Document("$set", new Document(DEFAULT_VERSION_FIELD, false));
        annotationMetadataCollection.updateMany(allVersions, setDefaultToFalse);

        String id = databaseParameters.getVepVersion() + "_" + databaseParameters.getVepCacheVersion();
        Document defaultVersionDocument = new Document(ID_FIELD, id);
        Document setDefaultToTrue = new Document("$set", new Document(DEFAULT_VERSION_FIELD, true));
        UpdateResult updateResult = annotationMetadataCollection.updateOne(defaultVersionDocument, setDefaultToTrue);
        Assert.state(updateResult.getModifiedCount() == 1);
    }
}
