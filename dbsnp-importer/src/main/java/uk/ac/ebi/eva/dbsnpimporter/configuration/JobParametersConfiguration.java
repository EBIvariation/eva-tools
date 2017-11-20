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
package uk.ac.ebi.eva.dbsnpimporter.configuration;


import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.ac.ebi.eva.dbsnpimporter.parameters.DbsnpDatasource;
import uk.ac.ebi.eva.dbsnpimporter.parameters.Parameters;

import java.util.HashMap;

@Configuration
public class JobParametersConfiguration {

    private JobParametersBuilder parametersBuilder;

    @Bean
    JobParameters jobParameters(Parameters parameters, DbsnpDatasource dbsnpDatasource,
                                MongoProperties mongoProperties) {
        parametersBuilder = new JobParametersBuilder();

        addParameter("assembly", parameters.getAssembly());
        addParameter("assemblyTypes", String.join(",", parameters.getAssemblyTypes()));
        addParameter("batchId", parameters.getBatchId());
        addParameter("chunkSize", parameters.getChunkSize());
        addParameter("dbsnpBuild", parameters.getDbsnpBuild());
        addParameter("pageSize", parameters.getPageSize());
        addParameter("processor", parameters.getProcessor());
        addParameter("variantsCollection", parameters.getVariantsCollection());

        addParameter("driverClassName", dbsnpDatasource.getDriverClassName());
        addParameter("url", dbsnpDatasource.getUrl());
        addParameter("username", dbsnpDatasource.getUsername());
        // NOTE: not putting the password on purpose. is it safe to put a readonly password in the jobRepository?

        addParameter("mongoAuthenticationDatabase", mongoProperties.getAuthenticationDatabase());
        addParameter("mongoDatabase", mongoProperties.getDatabase());
        addParameter("mongoHost", mongoProperties.getHost());
        addParameter("mongoPort", mongoProperties.getPort());
        addParameter("mongoUri", mongoProperties.getUri());
        addParameter("mongoUsername", mongoProperties.getUsername());
        // NOTE: not putting the password on purpose. is it safe to put a write password in the jobRepository?

        return parametersBuilder.toJobParameters();
    }

    private void addNonIdentifyingParameter(String key, String value) {
        if (value != null) {
            parametersBuilder.addString(key, value);
        }
    }

    private void addParameter(String key, String value) {
        if (value != null) {
            parametersBuilder.addString(key, value);
        }
    }

    private void addParameter(String key, int value) {
        parametersBuilder.addLong(key, (long) value);
    }

}
