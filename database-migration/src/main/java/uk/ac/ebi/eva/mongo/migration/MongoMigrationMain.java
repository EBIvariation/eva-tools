package uk.ac.ebi.eva.mongo.migration;

import com.github.mongobee.Mongobee;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.github.mongobee.exception.MongobeeException;
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
 * Script that executes the following steps using mongobee:
 *  - Extracts the 'st' field from a Variant stored in MongoDB into a new statistics object
 *  - _id, chr, start, end, ref and alt are added into the Statistics object
 *  - The Statistics object is added into the statistics collection
 *  - The 'maf' field is added into the Variant object
 *  - The 'st' field is removed from the Variant object
 *
 * https://github.com/mongobee/mongobee/wiki/How-to-use-mongobee
 * https://gist.github.com/jmmut/8439d830dac1954e93f9697051bf5a69
 *
 * ================
 * Usage:
 * java -jar database-migration-0.1-jar-with-dependencies.jar database_name collection_name
 *
 */
@ChangeLog
public class MongoMigrationMain {
    private static final Logger logger = LoggerFactory.getLogger(MongoMigrationMain.class);

    private static final String MONGO_URI = "TO FILL!!!"; // mongodb://127.0.0.1:27017 OR mongodb://username:password@host:27017/admin

    private static String dbName;
    private static String variantsCollectionName;

    public static void main(String[] args) throws MongobeeException {
        dbName = Objects.requireNonNull(args[0], "dbName must not be empty");
        variantsCollectionName = Objects.requireNonNull(args[1], "variantsCollectionName must not be empty");

        logger.info("Starting mongo migration in db {} on collection {}", dbName, variantsCollectionName);

        Mongobee runner = new Mongobee(MONGO_URI);
        runner.setDbName(dbName);  // host must be set if not set in URI
        runner.setChangeLogsScanPackage("uk.ac.ebi.eva.mongo.migration"); // package to scan for changesets
        runner.setEnabled(true);         // optional: default is true
        runner.execute();
    }

    @ChangeSet(order = "001", id = "migrateStatistics", author = "Diego")
    public void migrateStatistics(MongoTemplate mongoTemplate) {
        logger.info("1) migrate statistics");

        final DBCollection statisticsCollection = mongoTemplate.getCollection("statistics");
        final DBCollection variantsCollection = mongoTemplate.getCollection(variantsCollectionName);

        BulkWriteOperation bulkInsertMaf = variantsCollection.initializeUnorderedBulkOperation();
        BulkWriteOperation bulkInsertStats = statisticsCollection.initializeUnorderedBulkOperation();

        int counter=0;
        int missingMafFlag = Integer.MIN_VALUE;

        DBCursor variantCursor = variantsCollection.find();
        while (variantCursor.hasNext()) {

            DBObject variantObj = variantCursor.next();

            DBObject statsObj = (DBObject) variantObj.get("st");

            if(statsObj!=null){
                for(String s : statsObj.keySet()){
                    BasicDBObject iStatsObj = (BasicDBObject) statsObj.get(s);

                    iStatsObj.put("vid", variantObj.get("_id"));
                    iStatsObj.put("chr", variantObj.get("chr"));
                    iStatsObj.put("start", variantObj.get("start"));
                    iStatsObj.put("ref", variantObj.get("ref"));
                    iStatsObj.put("alt", variantObj.get("alt"));

                    bulkInsertStats.insert(iStatsObj);

                    //
                    double maf = iStatsObj.getDouble("maf",  missingMafFlag);
                    if(maf != missingMafFlag){
                        DBObject update = new BasicDBObject("$addToSet", new BasicDBObject("maf", maf));
                        DBObject find = new BasicDBObject("_id", variantObj.get("_id"));
                        bulkInsertMaf.find(find).updateOne(update);
                    }

                    counter++;
                    if(counter % 1000 == 0){
                        bulkInsertStats.execute();
                        bulkInsertStats = statisticsCollection.initializeUnorderedBulkOperation();
                        bulkInsertMaf.execute();
                        bulkInsertMaf = variantsCollection.initializeUnorderedBulkOperation();
                    }
                }
            }

        }

        if(counter % 1000 != 0){
            bulkInsertStats.execute();
            bulkInsertMaf.execute();
        }

    }

    @ChangeSet(order = "002", id = "removeStatisticsFromVariants", author = "Diego")
    public void removeStatisticsFromVariants(MongoTemplate mongoTemplate) {
        logger.info("2) remove st field from variants");

        final DBCollection variantsCollection = mongoTemplate.getCollection(variantsCollectionName);
        variantsCollection.updateMulti(new BasicDBObject(), new BasicDBObject("$unset", new BasicDBObject("st", "")));
    }

}
