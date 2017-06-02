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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Properties;

import static java.lang.System.exit;

/**
 * Script that extract the annotation from a Variant into a new collection using mongobee.
 * <p>
 * https://github.com/mongobee/mongobee/wiki/How-to-use-mongobee
 * <p>
 * ================
 * Usage:
 * java -jar database-migration-0.1-jar-with-dependencies.jar your_migration.properties
 *
 * In src/main/resources/example-mongodb.properties there's an example of how to fill this properties file.
 */
public class MongoMigrationMain {

    private static final Logger logger = LoggerFactory.getLogger(MongoMigrationMain.class);

    public static void main(String[] args) throws MongobeeException {
        if (args.length != 1) {
            logger.error("Please provide the path to a properties file with the MongoDB connection details");
            exit(1);
        }
        DatabaseParameters databaseParameters = new DatabaseParameters();
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(args[0]));
            databaseParameters.load(properties);
        } catch (Exception exception) {
            logger.error("Error reading properties: ", exception);
            exit(1);
        }
        ExtractAnnotationFromVariant.setDatabaseParameters(databaseParameters);
        Mongobee runner = MongobeeHelper.buildMongobee(databaseParameters);
        runner.setChangeLogsScanPackage("uk.ac.ebi.eva.dbmigration.mongodb"); // package to scan for changesets
        runner.setEnabled(true);         // optional: default is true
        runner.execute();
    }

}
