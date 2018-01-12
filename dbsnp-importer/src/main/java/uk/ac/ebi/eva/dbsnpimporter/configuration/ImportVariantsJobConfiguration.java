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
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import uk.ac.ebi.eva.dbsnpimporter.jobs.deciders.SkipStepOnEmptyBatchDecider;

import static uk.ac.ebi.eva.dbsnpimporter.configuration.ImportSamplesStepConfiguration.IMPORT_SAMPLES_STEP_BEAN;
import static uk.ac.ebi.eva.dbsnpimporter.configuration.ImportVariantsStepConfiguration.IMPORT_VARIANTS_STEP_BEAN;
import static uk.ac.ebi.eva.dbsnpimporter.parameters.Parameters.JOB;

@Configuration
@EnableBatchProcessing
@Import({ImportSamplesStepConfiguration.class, ImportVariantsStepConfiguration.class})
public class ImportVariantsJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ImportVariantsJobConfiguration.class);

    public static final String IMPORT_VARIANTS_JOB = "IMPORT_VARIANTS_JOB";

    public static final String IMPORT_VARIANTS_JOB_BEAN = "IMPORT_VARIANTS_JOB_BEAN";

    @Autowired
    @Qualifier(IMPORT_VARIANTS_STEP_BEAN)
    private Step importVariantsStep;

    @Autowired
    @Qualifier(IMPORT_SAMPLES_STEP_BEAN)
    private Step importSamplesStep;


    @Bean(IMPORT_VARIANTS_JOB_BEAN)
    @Scope("prototype")
    @ConditionalOnProperty(name = JOB, havingValue = IMPORT_VARIANTS_JOB)
    public Job importBatchJob(JobBuilderFactory jobBuilderFactory) {
        logger.debug("Building '" + IMPORT_VARIANTS_JOB + "'");

        JobBuilder jobBuilder = jobBuilderFactory.get(IMPORT_VARIANTS_JOB)
                                                 .incrementer(new RunIdIncrementer());

        SkipStepOnEmptyBatchDecider decider = new SkipStepOnEmptyBatchDecider();
        return jobBuilder.start(importVariantsStep)
                         .next(decider).on(SkipStepOnEmptyBatchDecider.DO_STEP).to(importSamplesStep)
                         .from(decider).on(SkipStepOnEmptyBatchDecider.SKIP_STEP).end(BatchStatus.COMPLETED.toString())
                         .build().build();
    }

}
