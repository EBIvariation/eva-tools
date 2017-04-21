/*
 *
 *  * Copyright 2016 EMBL - European Bioinformatics Institute
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package uk.ac.ebi.eva.vcfdump;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.mongodb.utils.MongoCredentials;
import org.opencb.opencga.storage.mongodb.variant.VariantMongoDBAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class VariantExporterTestDB {

    public static final String HUMAN_TEST_DB_NAME = "eva_hsapiens_grch37";

    public static final String COW_TEST_DB_NAME = "eva_btaurus_umd31_test";

    public static final String SHEEP_TEST_DB_NAME = "eva_oaries_oarv31";

    public static final String SHEEP_STUDY_ID = "PRJEB14685";

    public static final String SHEEP_FILE_1_ID = "ERZ324588";

    public static final String SHEEP_FILE_2_ID = "ERZ324596";

    public static final int NUMBER_OF_SAMPLES_IN_SHEEP_FILES = 453;

    private static final Logger logger = LoggerFactory.getLogger(VariantExporterTestDB.class);

    public static void cleanDBs() throws UnknownHostException {
        logger.info("Cleaning test DBs ...");
        MongoClient mongoClient = new MongoClient("localhost");
        List<String> dbs = Arrays.asList(HUMAN_TEST_DB_NAME, COW_TEST_DB_NAME, SHEEP_TEST_DB_NAME);
        for (String dbName : dbs) {
            DB db = mongoClient.getDB(dbName);
            db.dropDatabase();
        }
        mongoClient.close();
    }

    public static void fillDB() throws IOException, InterruptedException {
        String testDumpDirectory = VariantExporterTest.class.getResource("/dump/").getFile();
        logger.info("restoring DB from " + testDumpDirectory);
        Process exec = Runtime.getRuntime().exec("mongorestore " + testDumpDirectory);
        exec.waitFor();
        String line;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
        while ((line = bufferedReader.readLine()) != null) {
            logger.info("mongorestore output:" + line);
        }
        bufferedReader.close();
        bufferedReader = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
        while ((line = bufferedReader.readLine()) != null) {
            logger.info("mongorestore errorOutput:" + line);
        }
        bufferedReader.close();

        logger.info("mongorestore exit value: " + exec.exitValue());
    }

    public static VariantMongoDBAdaptor getVariantMongoDBAdaptor(String dbName)
            throws IOException, IllegalOpenCGACredentialsException {
        Properties evaTestProperties = new Properties();
        evaTestProperties.load(VariantExporterTestDB.class.getResourceAsStream("/evaTest.properties"));

        String host = evaTestProperties.getProperty("eva.mongo.host");
        String server = host.split(":")[0];
        int port = Integer.parseInt(host.split(":")[1]);
        MongoCredentials credentials = new MongoCredentials(server, port, dbName, null, null);
        VariantMongoDBAdaptor variantDBAdaptor = new VariantMongoDBAdaptor(credentials,
                                                                           evaTestProperties.getProperty(
                                                                                   "eva.mongo.collections.variants"),
                                                                           evaTestProperties.getProperty(
                                                                                   "eva.mongo.collections.files"));

        return variantDBAdaptor;
    }
}
