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
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Script that executes the following steps using mongobee (https://github.com/mongobee/mongobee/wiki/How-to-use-mongobee):
 * - Extracts the 'st' field from a Variant stored in MongoDB into a new statistics object
 * - _id, chr, start, end, ref and alt are added into the Statistics object
 * - The Statistics object is added into the statistics collection
 * - The 'maf' field is added into the Variant object
 * - The 'st' field is removed from the Variant object
 */
@ChangeLog
public class ExtractStatisticsFromVariant {
    private static final Logger logger = LoggerFactory.getLogger(ExtractStatisticsFromVariant.class);

    private final static int BULK_SIZE = 1000;

    @ChangeSet(order = "001", id = "migrateStatistics", author = "EVA")
    public void migrateStatistics(MongoTemplate mongoTemplate) {
        logger.info("1) migrate statistics {}", MongoMigrationMain.variantsCollectionName);

        final DBCollection statisticsCollection = mongoTemplate
                .getCollection(MongoMigrationMain.statisticsCollectionName);
        final DBCollection variantsCollection = mongoTemplate.getCollection(MongoMigrationMain.variantsCollectionName);

        BulkWriteOperation bulkInsertMaf = variantsCollection.initializeUnorderedBulkOperation();
        BulkWriteOperation bulkInsertStats = statisticsCollection.initializeUnorderedBulkOperation();

        int counter = 0;
        DBCursor variantCursor = variantsCollection.find();

        while (variantCursor.hasNext()) {
            DBObject variantObj = variantCursor.next();

            DBObject statsObj = (DBObject) variantObj.get("st");

            if (statsObj != null) {
                for (String standaloneStatsObj : statsObj.keySet()) {
                    BasicDBObject iStatsObj = (BasicDBObject) statsObj.get(standaloneStatsObj);

                    iStatsObj.put("vid", variantObj.get("_id"));
                    iStatsObj.put("chr", variantObj.get("chr"));
                    iStatsObj.put("start", variantObj.get("start"));
                    iStatsObj.put("ref", variantObj.get("ref"));
                    iStatsObj.put("alt", variantObj.get("alt"));

                    bulkInsertStats.insert(iStatsObj);

                    if (iStatsObj.containsField("maf")) {
                        DBObject update = new BasicDBObject("$addToSet",
                                                            new BasicDBObject("maf", iStatsObj.get("maf")));
                        DBObject find = new BasicDBObject("_id", variantObj.get("_id"));
                        bulkInsertMaf.find(find).updateOne(update);
                    }

                    counter++;
                    if (counter % BULK_SIZE == 0) {
                        bulkInsertStats.execute();
                        bulkInsertStats = statisticsCollection.initializeUnorderedBulkOperation();
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

        //before executing the next changeSet check that the count of read and written statistics documents match
        if (counter != statisticsCollection.count()) {
            logger.error(
                    "The number of processed Variants ({}) is different from the number of new statistics inserted ({})." +
                            " The 'st' field will not be removed from the {} collection.",
                    counter, statisticsCollection.count(), MongoMigrationMain.variantsCollectionName);
            System.exit(1);
        }
    }

    @ChangeSet(order = "002", id = "removeStatisticsFromVariants", author = "EVA")
    public void removeStatisticsFromVariants(MongoTemplate mongoTemplate) {
        logger.info("2) remove st field from variants");

        final DBCollection variantsCollection = mongoTemplate.getCollection(MongoMigrationMain.variantsCollectionName);
        variantsCollection.updateMulti(new BasicDBObject(), new BasicDBObject("$unset", new BasicDBObject("st", "")));
    }
}
