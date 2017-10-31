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
package uk.ac.ebi.eva.dbsnpimporter.test.configurations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class JobRepositoryTestConfiguration extends DefaultBatchConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(JobRepositoryTestConfiguration.class);

    private Environment environment;

    public JobRepositoryTestConfiguration() {
        super.setDataSource(null);
    }

    @Override
    public void setDataSource(DataSource ignored) {
        logger.info("Ignoring datasource " + ignored.toString() + " for job repository, using in-memory jobRepository");
    }

}
