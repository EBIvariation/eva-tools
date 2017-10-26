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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;

import uk.ac.ebi.eva.commons.core.models.IVariant;
import uk.ac.ebi.eva.commons.mongodb.writers.VariantMongoWriter;
import uk.ac.ebi.eva.dbsnpimporter.Parameters;
import uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpCoreFieldsReader;
import uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.SubSnpCoreFieldsToEvaSubmittedVariantProcessor;
import uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.SubSnpCoreFieldsToVariantProcessor;

import javax.sql.DataSource;

import static uk.ac.ebi.eva.dbsnpimporter.Parameters.PROCESSOR;

@Configuration
@EnableConfigurationProperties(Parameters.class)
public class ImportBatchStepConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ImportBatchStepConfiguration.class);

    public static final String VARIANTS_READER = "VARIANT_READER";

    public static final String VARIANTS_PROCESSOR = "VARIANTS_PROCESSOR";

    public static final String VARIANTS_WRITER = "VARIANTS_WRITER";

    @Bean(name = VARIANTS_READER)
    SubSnpCoreFieldsReader subSnpCoreFieldsReader(Parameters parameters, DataSource dataSource) throws Exception {
        logger.debug("Injecting SubSnpCoreFieldsReader");
        return new SubSnpCoreFieldsReader(parameters.getDbsnpBuild(), parameters.getBatchId(), parameters.getAssembly(),
                                          parameters.getAssemblyTypes(), dataSource, parameters.getPageSize());
    }

    @Bean(name = VARIANTS_PROCESSOR)
    @ConditionalOnProperty(name = PROCESSOR, havingValue = "SubSnpCoreFieldsToVariantProcessor")
    SubSnpCoreFieldsToVariantProcessor subSnpCoreFieldsToVariantProcessor(Parameters parameters) {
        logger.debug("Injecting SubSnpCoreFieldsToVariantProcessor");
        return new SubSnpCoreFieldsToVariantProcessor(parameters.getDbsnpBuild());
    }

    @Bean(name = VARIANTS_PROCESSOR)
    @ConditionalOnProperty(name = PROCESSOR, havingValue = "SubSnpCoreFieldsToEvaSubmittedVariantProcessor")
    SubSnpCoreFieldsToEvaSubmittedVariantProcessor subSnpCoreFieldsToEvaSubmittedVariantProcessor() {
        logger.debug("Injecting SubSnpCoreFieldsToEvaSubmittedVariantProcessor");
        return new SubSnpCoreFieldsToEvaSubmittedVariantProcessor();
    }

    @Bean(name = VARIANTS_WRITER)
    VariantMongoWriter variantMongoWriter(Parameters parameters, MongoOperations mongoOperations) throws Exception {
        logger.debug("Injecting VariantMongoWriter");
        boolean includeSamples = true;
        boolean includeStats = true;
        return new VariantMongoWriter(parameters.getVariantsCollection(), mongoOperations,
                                      includeStats, includeSamples);
    }

    @Bean
    @StepScope
    public SimpleCompletionPolicy chunkSizecompletionPolicy(Parameters parameters) {
        return new SimpleCompletionPolicy(parameters.getChunkSize());
    }

}

