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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.ac.ebi.eva.dbsnpimporter.io.readers.BatchReader;
import uk.ac.ebi.eva.dbsnpimporter.io.readers.SampleReader;
import uk.ac.ebi.eva.dbsnpimporter.io.readers.WindingItemStreamReader;
import uk.ac.ebi.eva.dbsnpimporter.models.DbsnpBatch;
import uk.ac.ebi.eva.dbsnpimporter.parameters.DbsnpDatasource;
import uk.ac.ebi.eva.dbsnpimporter.parameters.Parameters;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties({Parameters.class, DbsnpDatasource.class})
public class BatchReaderConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(BatchReaderConfiguration.class);

    public static final String BATCH_READER = "BATCH_READER";

    @Bean(name = BATCH_READER)
    @StepScope
    ItemStreamReader<DbsnpBatch> batchReader(Parameters parameters, DbsnpDatasource dbsnpDatasource) throws Exception {
        logger.info("Injecting BatchReader with parameters: {}, {}", parameters, dbsnpDatasource);
        DataSource dataSource = dbsnpDatasource.getDatasource();
        return new BatchReader(parameters.getBatchId(), dataSource, parameters.getPageSize());
    }
}
