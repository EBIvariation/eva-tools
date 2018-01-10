/*
 * Copyright 2018 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.dbsnpimporter.jobs.deciders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

import java.util.Optional;

import static uk.ac.ebi.eva.dbsnpimporter.configuration.ImportVariantsStepConfiguration.IMPORT_VARIANTS_STEP;

public class SkipStepOnEmptyBatchDecider implements JobExecutionDecider {
    private static final Logger logger = LoggerFactory.getLogger(SkipStepOnEmptyBatchDecider.class);

    public static final String SKIP_STEP = "SKIP_STEP";

    public static final String DO_STEP = "DO_STEP";

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        Optional<StepExecution> previousStepExecution = jobExecution.getStepExecutions().stream()
                                                                    .filter(previousStep -> IMPORT_VARIANTS_STEP
                                                                            .equals(previousStep.getStepName()))
                                                                    .findFirst();
        if (previousStepExecution.isPresent()) {
            if (previousStepExecution.get().getWriteCount() == 0) {
                logger.info("Skipping step because the " + IMPORT_VARIANTS_STEP + " didn't write any variants");
                return new FlowExecutionStatus(SKIP_STEP);
            }
        }
        return new FlowExecutionStatus(DO_STEP);
    }
}
