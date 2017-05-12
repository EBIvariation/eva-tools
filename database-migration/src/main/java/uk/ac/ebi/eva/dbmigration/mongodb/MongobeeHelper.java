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

public class MongobeeHelper {

    public static Mongobee buildMongobee(DatabaseParameters databaseParameters) {
        List<ServerAddress> servers = getServers(databaseParameters);
        List<MongoCredential> credentials = getCredentials(databaseParameters);
        MongoClientOptions options = getOptions(databaseParameters);
        return new Mongobee(new MongoClient(servers, credentials, options));
    }

    private static List<ServerAddress> getServers(DatabaseParameters databaseParameters) {
        List<ServerAddress> hosts = new ArrayList<>();

        if (databaseParameters.getDbPort() != null) {
            int port = Integer.parseInt(databaseParameters.getDbPort());
            for (String host : databaseParameters.getDbHosts().split(",")) {
                hosts.add(new ServerAddress(host, port));
            }
        } else {
            for (String host : databaseParameters.getDbHosts().split(",")) {
                hosts.add(new ServerAddress(host));
            }
        }

        return hosts;
    }

    private static List<MongoCredential> getCredentials(DatabaseParameters databaseParameters) {
        MongoCredential mongoCredential = MongoCredential
                .createCredential(databaseParameters.getDbUsername(),
                                  databaseParameters.getDbAuthenticationDatabase(),
                                  databaseParameters.getDbPassword().toCharArray());

        return Collections.singletonList(mongoCredential);
    }


    private static MongoClientOptions getOptions(DatabaseParameters databaseParameters) {
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        builder.readPreference(ReadPreference.valueOf(databaseParameters.getDbReadPreference()));
        return builder.build();
    }
}
