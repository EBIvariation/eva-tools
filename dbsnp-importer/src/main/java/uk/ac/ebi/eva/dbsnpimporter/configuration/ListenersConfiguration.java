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
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.ac.ebi.eva.commons.core.models.IVariant;
import uk.ac.ebi.eva.commons.core.models.IVariantSource;
import uk.ac.ebi.eva.dbsnpimporter.models.Sample;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;
import uk.ac.ebi.eva.dbsnpimporter.parameters.Parameters;

import java.util.List;

@Configuration
public class ListenersConfiguration {

    @Bean
    public StepListenerSupport<SubSnpCoreFields, IVariant> variantImportListener(Parameters parameters) {
        return new VariantImportListener(parameters);
    }

    @Bean
    public StepListenerSupport<List<Sample>, IVariantSource> sampleImportListener(Parameters parameters) {
        return new SampleImportListener(parameters);
    }

    private static class VariantImportListener extends StepListenerSupport<SubSnpCoreFields, IVariant> {

        private static final Logger logger = LoggerFactory.getLogger(VariantImportListener.class);

        private Parameters parameters;

        private long numItemsRead;

        public VariantImportListener(Parameters parameters) {
            this.parameters = parameters;
            this.numItemsRead = 0;
        }

        @Override
        public void beforeStep(StepExecution stepExecution) {
            logger.debug("Starting a step");
        }

        @Override
        public void beforeChunk(ChunkContext context) {
            logger.debug("Starting a chunk");
        }

        @Override
        public void beforeRead() {
            if (numItemsRead % parameters.getChunkSize() == 0) {
                logger.debug("About to read item {}", numItemsRead);
            }
            numItemsRead++;
        }

        @Override
        public void afterRead(SubSnpCoreFields item) {
            if (numItemsRead % parameters.getChunkSize() == 0) {
                logger.debug("Read {} items", numItemsRead);
            }
        }

        @Override
        public void beforeWrite(List<? extends IVariant> items) {
            logger.debug("About to write chunk");
        }

        @Override
        public void afterWrite(List<? extends IVariant> items) {
            IVariant lastItem = items.get(items.size() - 1);
            logger.debug("Written chunk of {} items. Last item was {}: {}", items.size(), lastItem.getMainId(),
                         lastItem);
        }

        @Override
        public void afterChunk(ChunkContext context) {
            String stepName = context.getStepContext().getStepName();
            long numTotalItemsRead = context.getStepContext().getStepExecution().getReadCount();
            long numTotalItemsWritten = context.getStepContext().getStepExecution().getWriteCount();

            logger.info("{}: Items read = {}, items written = {}", stepName, numTotalItemsRead, numTotalItemsWritten);
        }

        @Override
        public ExitStatus afterStep(StepExecution stepExecution) {
            logger.debug("Finished a step");
            return stepExecution.getExitStatus();
        }
    }


    private static class SampleImportListener extends StepListenerSupport<List<Sample>, IVariantSource> {

        private static final Logger logger = LoggerFactory.getLogger(SampleImportListener.class);

        private Parameters parameters;

        private long numItemsRead;

        public SampleImportListener(Parameters parameters) {
            this.parameters = parameters;
            this.numItemsRead = 0;
        }

        @Override
        public void beforeStep(StepExecution stepExecution) {
            logger.debug("Starting a step");
        }

        @Override
        public void beforeChunk(ChunkContext context) {
            logger.debug("Starting a chunk");
        }

        @Override
        public void beforeRead() {
            if (numItemsRead % parameters.getChunkSize() == 0) {
                logger.debug("About to read item {}", numItemsRead);
            }
            numItemsRead++;
        }

        @Override
        public void afterRead(List<Sample> item) {
            if (numItemsRead % parameters.getChunkSize() == 0) {
                logger.debug("Read {} items", numItemsRead);
            }
        }

        @Override
        public void beforeWrite(List<? extends IVariantSource> items) {
            logger.debug("About to write chunk");
        }

        @Override
        public void afterWrite(List<? extends IVariantSource> items) {
            IVariantSource lastItem = items.get(items.size() - 1);
            logger.debug("Written chunk of {} items. Last item was {}: {}", items.size(), lastItem.getStudyId(),
                         lastItem);
        }

        @Override
        public void afterChunk(ChunkContext context) {
            String stepName = context.getStepContext().getStepName();
            long numTotalItemsRead = context.getStepContext().getStepExecution().getReadCount();
            long numTotalItemsWritten = context.getStepContext().getStepExecution().getWriteCount();

            logger.info("{}: Items read = {}, items written = {}", stepName, numTotalItemsRead, numTotalItemsWritten);
        }

        @Override
        public ExitStatus afterStep(StepExecution stepExecution) {
            logger.debug("Finished a step");
            return stepExecution.getExitStatus();
        }
    }
}
