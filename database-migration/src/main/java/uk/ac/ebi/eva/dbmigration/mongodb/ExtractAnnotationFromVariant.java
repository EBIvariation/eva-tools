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
import com.google.common.base.Strings;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * Script that executes the following steps using mongobee (https://github.com/mongobee/mongobee/wiki/How-to-use-mongobee):
 * - Extracts the 'st' field from a Variant stored in MongoDB into a new statistics object
 * - _id, chr, start, end, ref and alt are added into the Statistics object
 * - The Statistics object is added into the statistics collection
 * - The 'fid' field is removed form the Statistics object
 * - The 'maf' field is added into the Variant object
 * - The 'st' field is removed from the Variant object
 */
@ChangeLog
public class ExtractAnnotationFromVariant {

    private static final Logger logger = LoggerFactory.getLogger(ExtractAnnotationFromVariant.class);

    private final static int BULK_SIZE = 1000;

    public static final String ANNOTATION_COLLECTION = "annotation.collection";

    public static final String VARIANTS_COLLECTION = "variants.collection";

    public static final String APPLICATION_PROPERTIES = "application.properties";

    private String variantsCollectionName;

    private String annotationCollectionName;

    public ExtractAnnotationFromVariant() {
            readProperties();
    }

    public ExtractAnnotationFromVariant(String variantsCollectionName, String annotationCollectionName) {
        this.variantsCollectionName = variantsCollectionName;
        this.annotationCollectionName = annotationCollectionName;
    }

    @ChangeSet(order = "001", id = "migrateAnnotation", author = "EVA")
    public void migrateAnnotation(MongoTemplate mongoTemplate) {
        logger.info("1) migrate annotation {}", variantsCollectionName);
        Objects.requireNonNull(variantsCollectionName);
        Objects.requireNonNull(annotationCollectionName);

        final DBCollection annotationCollection = mongoTemplate.getCollection(annotationCollectionName);
        final DBCollection variantsCollection = mongoTemplate.getCollection(variantsCollectionName);

        BulkWriteOperation bulkInsertMaf = variantsCollection.initializeUnorderedBulkOperation();
        BulkWriteOperation bulkInsertStats = annotationCollection.initializeUnorderedBulkOperation();

        int counter = 0;
        DBCursor variantCursor = variantsCollection.find();

        while (variantCursor.hasNext()) {
            DBObject variantObj = variantCursor.next();

            DBObject statsObj = (DBObject) variantObj.get("st");

            if (statsObj != null) {
                for (String statsObjIndex : statsObj.keySet()) {
                    BasicDBObject standaloneStatsObj = (BasicDBObject) statsObj.get(statsObjIndex);

                    standaloneStatsObj.put("vid", variantObj.get("_id"));
                    standaloneStatsObj.put("chr", variantObj.get("chr"));
                    standaloneStatsObj.put("start", Integer.valueOf(variantObj.get("start").toString()));
                    standaloneStatsObj.put("ref", variantObj.get("ref"));
                    standaloneStatsObj.put("alt", variantObj.get("alt"));

                    standaloneStatsObj.remove("fid");

                    bulkInsertStats.insert(standaloneStatsObj);

                    if (standaloneStatsObj.containsField("maf")) {
                        DBObject update = new BasicDBObject("$addToSet", new BasicDBObject("maf", Double.parseDouble(
                                standaloneStatsObj.getString("maf"))));
                        DBObject find = new BasicDBObject("_id", variantObj.get("_id"));
                        bulkInsertMaf.find(find).updateOne(update);
                    }

                    counter++;
                    if (counter % BULK_SIZE == 0) {
                        bulkInsertStats.execute();
                        bulkInsertStats = annotationCollection.initializeUnorderedBulkOperation();
                        bulkInsertMaf.execute();
                        bulkInsertMaf = variantsCollection.initializeUnorderedBulkOperation();
                    }
                }
            }

        }

        if (counter % BULK_SIZE != 0) {
            bulkInsertStats.execute();
            bulkInsertMaf.execute();
        }

        //before executing the next changeSet check that the count of read and written annotation documents match
        if (counter != annotationCollection.count()) {
            throw new RuntimeException(
                    "The number of processed Variants (" + counter + ") is different from the number of new annotation " +
                            "inserted (" + annotationCollection.count() + "). The 'st' field will not be removed " +
                            "from the " + variantsCollectionName + " collection.");
        }
    }

    @ChangeSet(order = "002", id = "removeAnnotationFromVariants", author = "EVA")
    public void removeAnnotationFromVariants(MongoTemplate mongoTemplate) {
        logger.info("2) remove st field from variants");

        Objects.requireNonNull(variantsCollectionName);

        final DBCollection variantsCollection = mongoTemplate.getCollection(variantsCollectionName);
        variantsCollection.updateMulti(new BasicDBObject(), new BasicDBObject("$unset", new BasicDBObject("st", "")));
    }

    private void readProperties(){
        Properties prop = new Properties();
        try {
            prop.load(ExtractAnnotationFromVariant.class.getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES));
            variantsCollectionName = prop.getProperty(VARIANTS_COLLECTION);
            annotationCollectionName = prop.getProperty(ANNOTATION_COLLECTION);

            if (Strings.isNullOrEmpty(variantsCollectionName) || variantsCollectionName.trim().length() == 0) {
                System.out.println("The variants collection name must not be empty");
                System.exit(1);
            }

            if (Strings.isNullOrEmpty(annotationCollectionName) || annotationCollectionName.trim().length() == 0) {
                System.out.println("The annotation collection name must not be empty");
                System.exit(1);
            }

            logger.info("Annotation will be migrated from collection {} into the new {} collection. ", variantsCollectionName, annotationCollectionName);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
