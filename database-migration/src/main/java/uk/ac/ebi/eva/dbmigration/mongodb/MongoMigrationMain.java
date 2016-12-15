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

import com.github.mongobee.Mongobee;
import com.github.mongobee.exception.MongobeeException;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Script that exetact the statistics from a Variant into a new collection using mongobee.
 * <p>
 * https://github.com/mongobee/mongobee/wiki/How-to-use-mongobee
 * https://gist.github.com/jmmut/8439d830dac1954e93f9697051bf5a69
 * <p>
 * ================
 * Usage:
 * java -jar database-migration-0.1-jar-with-dependencies.jar
 * <p>
 * Mongo URI example
 * local: 127.0.0.1:27017
 * remote: username:password@host:27017/admin
 */
public class MongoMigrationMain {

    private static final Logger logger = LoggerFactory.getLogger(MongoMigrationMain.class);

    public static final String APPLICATION_PROPERTIES = "application.properties";

    private static String dbName;

    private static String mongoUri;

    public static void main(String[] args) throws MongobeeException {
        readProperties();

        logger.info("Starting Mongo migration in database {}. ", dbName);
        Mongobee runner = new Mongobee(String.format("mongodb://%s", mongoUri));
        runner.setDbName(dbName);
        runner.setChangeLogsScanPackage("uk.ac.ebi.eva.dbmigration.mongodb"); // package to scan for changesets
        runner.setEnabled(true);         // optional: default is true
        runner.execute();
    }

    private static void readProperties() {
        Properties prop = new Properties();
        try {
            prop.load(MongoMigrationMain.class.getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES));
            dbName = prop.getProperty("db.name");
            mongoUri = prop.getProperty("mongo.uri");

            if (Strings.isNullOrEmpty(dbName) || dbName.trim().length() == 0) {
                System.out.println("The database name must not be empty");
                System.exit(1);
            }

            if (Strings.isNullOrEmpty(mongoUri) || mongoUri.trim().length() == 0) {
                System.out.println("The Mongo URI must not be empty");
                System.exit(1);
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

}