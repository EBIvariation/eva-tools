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
package uk.ac.ebi.eva.dbsnpimporter.configurations;


import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.ac.ebi.eva.dbsnpimporter.DbsnpDatasource;
import uk.ac.ebi.eva.dbsnpimporter.Parameters;

import java.util.HashMap;

@Configuration
public class JobParametersConfiguration {

    @Bean
    JobParameters jobParameters(Parameters parameters, DbsnpDatasource dbsnpDatasource,
                                MongoProperties mongoProperties) throws IllegalAccessException {

        HashMap<String, JobParameter> parametersMap = new HashMap<>();

        parametersMap.put("assembly", new JobParameter(parameters.getAssembly()));
        parametersMap.put("assemblyTypes", new JobParameter(String.join(",", parameters.getAssemblyTypes())));
        parametersMap.put("batchId", new JobParameter(new Long(parameters.getBatchId())));
        parametersMap.put("chunkSize", new JobParameter(new Long(parameters.getChunkSize())));
        parametersMap.put("dbsnpBuild", new JobParameter(new Long(parameters.getDbsnpBuild())));
        parametersMap.put("pageSize", new JobParameter(new Long(parameters.getPageSize())));
        parametersMap.put("processor", new JobParameter(parameters.getProcessor()));
        parametersMap.put("variantsCollection", new JobParameter(parameters.getVariantsCollection()));

        parametersMap.put("driverClassName", new JobParameter(dbsnpDatasource.getDriverClassName()));
        parametersMap.put("url", new JobParameter(dbsnpDatasource.getUrl()));
        parametersMap.put("username", new JobParameter(dbsnpDatasource.getUsername()));
        // NOTE: not putting the password on purpose. is it safe to put a readonly password in the jobRepository?

        parametersMap.put("mongoAuthenticationDatabase", new JobParameter(mongoProperties.getAuthenticationDatabase()));
        parametersMap.put("mongoDatabase", new JobParameter(mongoProperties.getDatabase()));
        parametersMap.put("mongoHost", new JobParameter(mongoProperties.getHost()));
        parametersMap.put("mongoPort", new JobParameter(new Long(mongoProperties.getPort())));
        parametersMap.put("mongoUri", new JobParameter(mongoProperties.getUri()));
        parametersMap.put("mongoUsername", new JobParameter(mongoProperties.getUsername()));
        // NOTE: not putting the password on purpose. is it safe to put a write password in the jobRepository?

        return new JobParameters(parametersMap);
    }
}
