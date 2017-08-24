/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.vcfdump.rules;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class TestDBRule extends ExternalResource {

    public static final String HUMAN_TEST_DB = "eva_hsapiens_grch37";
    public static final String COW_TEST_DB = "eva_btaurus_umd31_test";
    public static final String SHEEP_TEST_DB = "eva_oaries_oarv31";

    public static final String SHEEP_STUDY_ID = "PRJEB14685";
    public static final String SHEEP_FILE_1_ID = "ERZ324588";
    public static final String SHEEP_FILE_2_ID = "ERZ324596";

    public static final int NUMBER_OF_SAMPLES_IN_SHEEP_FILES = 453;

    private static final Logger logger = LoggerFactory.getLogger(TestDBRule.class);

    private static final Map<String, String> databaseMapping = new HashMap<>();

    private MongoClient mongoClient;


    @Override
    protected void after() {
        cleanDBs();
        mongoClient.close();
    }

    @Override
    protected void before() throws Throwable {
        mongoClient = new MongoClient();
        restoreDumpInTemporaryDatabase(HUMAN_TEST_DB);
        restoreDumpInTemporaryDatabase(COW_TEST_DB);
        restoreDumpInTemporaryDatabase(SHEEP_TEST_DB);
    }

    public static String getTemporaryDBName(String databaseName) {
        return databaseMapping.get(databaseName);
    }

    private String getRandomDatabaseName() {
        return UUID.randomUUID().toString();
    }

    private void cleanDBs() {
        for (String databaseName : databaseMapping.values()) {
            DB database = mongoClient.getDB(databaseName);
            database.dropDatabase();
        }
        databaseMapping.clear();
    }

    private String restoreDumpInTemporaryDatabase(String database) throws IOException, InterruptedException {
        URL testDumpDirectory = this.getClass().getResource("/dump/" + database);
        logger.info("restoring DB from " + testDumpDirectory);
        String randomDatabaseName = getRandomDatabaseName();
        databaseMapping.put(database, randomDatabaseName);
        restoreDump(testDumpDirectory, randomDatabaseName);
        return randomDatabaseName;
    }

    private void restoreDump(URL dumpLocation, String databaseName) throws IOException, InterruptedException {
        assert (dumpLocation != null);
        assert (databaseName != null && !databaseName.isEmpty());
        String file = dumpLocation.getFile();
        assert (file != null && !file.isEmpty());

        logger.info("restoring DB from " + file + " into database " + databaseName);

        Process exec = Runtime.getRuntime().exec(String.format("mongorestore --db %s %s", databaseName, file));
        exec.waitFor();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()))) {
            bufferedReader.lines().forEach(line -> logger.info("mongorestore output: " + line));
        }
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getErrorStream()))) {
            bufferedReader.lines().forEach(line -> logger.info("mongorestore errorOutput: " + line));
        }

        logger.info("mongorestore exit value: " + exec.exitValue());
    }

    public VariantWithSamplesAndAnnotationsService getVariantMongoDBAdaptor(String dbName)
            throws IOException {
        Properties evaTestProperties = new Properties();
        evaTestProperties.load(this.getClass().getResourceAsStream("/evaTest.properties"));

        String host = evaTestProperties.getProperty("eva.mongo.host");
        String server = host.split(":")[0];
        int port = Integer.parseInt(host.split(":")[1]);
        String randomDatabaseName = databaseMapping.get(dbName);
//        MongoCredentials credentials = new MongoCredentials(server, port, randomDatabaseName, null, null);
        //todo
        VariantWithSamplesAndAnnotationsService variantDBAdaptor = null;

        return variantDBAdaptor;
    }

}
