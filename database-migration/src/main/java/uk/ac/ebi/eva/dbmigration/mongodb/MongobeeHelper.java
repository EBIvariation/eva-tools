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
package uk.ac.ebi.eva.dbmigration.mongodb;

import com.github.mongobee.Mongobee;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;


public class MongobeeHelper {

    public static Mongobee buildMongobee(DatabaseParameters databaseParameters) {
        List<ServerAddress> servers = getServers(databaseParameters);
        List<MongoCredential> credentials = getCredentials(databaseParameters);
        MongoClientOptions options = getOptions(databaseParameters);

        Mongobee mongobee = new Mongobee(new MongoClient(servers, credentials, options));
        mongobee.setDbName(databaseParameters.getDbName());
        return mongobee;
    }

    private static List<ServerAddress> getServers(DatabaseParameters databaseParameters) {
        List<ServerAddress> addresses = new ArrayList<>();

        int port = ServerAddress.defaultPort();
        if (hasText(databaseParameters.getDbPort())) {
            port = Integer.parseInt(databaseParameters.getDbPort());
        }

        String hosts = databaseParameters.getDbHosts();
        if (!hasText(hosts)) {
            hosts = ServerAddress.defaultHost();
        }

        for (String host : hosts.split(",")) {
            addresses.add(new ServerAddress(host, port));
        }

        return addresses;
    }

    private static List<MongoCredential> getCredentials(DatabaseParameters databaseParameters) {
        if (hasText(databaseParameters.getDbUsername()) && hasText(databaseParameters.getDbPassword())) {
            String authenticationDatabase = databaseParameters.getDbAuthenticationDatabase();
            if (!hasText(authenticationDatabase)) {
                authenticationDatabase = databaseParameters.getDbName();
            }

            MongoCredential mongoCredential = MongoCredential.createCredential(
                    databaseParameters.getDbUsername(),
                    authenticationDatabase,
                    databaseParameters.getDbPassword().toCharArray());

            return Collections.singletonList(mongoCredential);
        } else {
            return Collections.emptyList();
        }
    }


    private static MongoClientOptions getOptions(DatabaseParameters databaseParameters) {
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        builder.readPreference(ReadPreference.valueOf(databaseParameters.getDbReadPreference()));
        return builder.build();
    }
}
