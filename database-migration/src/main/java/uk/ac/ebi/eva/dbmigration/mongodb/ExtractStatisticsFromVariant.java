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

import java.util.Objects;

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
public class ExtractStatisticsFromVariant {
    private static final Logger logger = LoggerFactory.getLogger(ExtractStatisticsFromVariant.class);

    private final static int BULK_SIZE = 1000;

    private String variantsCollectionName;

    private String statisticsCollectionName;

    public ExtractStatisticsFromVariant() {
        if (Objects.isNull(variantsCollectionName)) {
            variantsCollectionName = MongoMigrationMain.variantsCollectionName;
        }
        if (Objects.isNull(statisticsCollectionName)) {
            statisticsCollectionName = MongoMigrationMain.statisticsCollectionName;
        }
    }

    @ChangeSet(order = "001", id = "migrateStatistics", author = "EVA")
    public void migrateStatistics(MongoTemplate mongoTemplate) {
        logger.info("1) migrate statistics {}", variantsCollectionName);
        Objects.requireNonNull(variantsCollectionName);
        Objects.requireNonNull(statisticsCollectionName);

        final DBCollection statisticsCollection = mongoTemplate.getCollection(statisticsCollectionName);
        final DBCollection variantsCollection = mongoTemplate.getCollection(variantsCollectionName);

        BulkWriteOperation bulkInsertMaf = variantsCollection.initializeUnorderedBulkOperation();
        BulkWriteOperation bulkInsertStats = statisticsCollection.initializeUnorderedBulkOperation();

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

        counter++;
        //before executing the next changeSet check that the count of read and written statistics documents match
        if (counter != statisticsCollection.count()) {
            throw new RuntimeException(
                    "The number of processed Variants (" + counter + ") is different from the number of new statistics " +
                            "inserted (" + statisticsCollection.count() + "). The 'st' field will not be removed " +
                            "from the " + variantsCollectionName + " collection.");
        }
    }

    @ChangeSet(order = "002", id = "removeStatisticsFromVariants", author = "EVA")
    public void removeStatisticsFromVariants(MongoTemplate mongoTemplate) {
        logger.info("2) remove st field from variants");

        Objects.requireNonNull(variantsCollectionName);

        final DBCollection variantsCollection = mongoTemplate.getCollection(variantsCollectionName);
        variantsCollection.updateMulti(new BasicDBObject(), new BasicDBObject("$unset", new BasicDBObject("st", "")));
    }

    public void setVariantsCollectionName(String variantsCollectionName) {
        this.variantsCollectionName = variantsCollectionName;
    }

    public void setStatisticsCollectionName(String statisticsCollectionName) {
        this.statisticsCollectionName = statisticsCollectionName;
    }
}
