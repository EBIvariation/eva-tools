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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import static uk.ac.ebi.eva.dbsnpimporter.configuration.ImportSamplesStepConfiguration.IMPORT_SAMPLES_STEP_BEAN;
import static uk.ac.ebi.eva.dbsnpimporter.configuration.ImportVariantsStepConfiguration.IMPORT_VARIANTS_STEP_BEAN;

@Configuration
@EnableBatchProcessing
@Import({ImportVariantsStepConfiguration.class})
public class ImportEvaSubmittedVariantsJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ImportEvaSubmittedVariantsJobConfiguration.class);

    public static final String IMPORT_VARIANTS_JOB = "IMPORT_VARIANTS_JOB";

    public static final String IMPORT_EVASUBMITTED_VARIANTS_JOB_BEAN = "IMPORT_EVASUBMITTED_VARIANTS_JOB_BEAN";

    @Autowired
    @Qualifier(IMPORT_VARIANTS_STEP_BEAN)
    private Step importVariantsStep;


    @Bean(IMPORT_EVASUBMITTED_VARIANTS_JOB_BEAN)
    @Scope("prototype")
    public Job importEvaSubmittedBatchJob(JobBuilderFactory jobBuilderFactory) {
        logger.debug("Building '" + IMPORT_VARIANTS_JOB + "'");

        JobBuilder jobBuilder = jobBuilderFactory.get(IMPORT_VARIANTS_JOB)
                                                 .incrementer(new RunIdIncrementer());

        return jobBuilder.start(importVariantsStep).build();
    }

}
