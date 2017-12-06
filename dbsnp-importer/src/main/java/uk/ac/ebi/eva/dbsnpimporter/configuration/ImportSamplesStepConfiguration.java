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
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.listener.StepListenerSupport;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import uk.ac.ebi.eva.commons.core.models.IVariantSource;
import uk.ac.ebi.eva.dbsnpimporter.configuration.processors.SamplesToVariantSourceProcessorConfiguration;
import uk.ac.ebi.eva.dbsnpimporter.models.Sample;
import uk.ac.ebi.eva.dbsnpimporter.parameters.Parameters;

import java.util.List;

import static uk.ac.ebi.eva.dbsnpimporter.configuration.SampleReaderConfiguration.SAMPLE_READER;
import static uk.ac.ebi.eva.dbsnpimporter.configuration.VariantSourceWriterConfiguration.VARIANT_SOURCE_WRITER;

@Configuration
@EnableBatchProcessing
@Import({SampleReaderConfiguration.class, SamplesToVariantSourceProcessorConfiguration.class,
        VariantSourceWriterConfiguration.class, ListenersConfiguration.class})
public class ImportSamplesStepConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ImportSamplesStepConfiguration.class);

    public static final String IMPORT_SAMPLES_STEP = "IMPORT_SAMPLES_STEP";

    public static final String IMPORT_SAMPLES_STEP_BEAN = "IMPORT_SAMPLES_STEP_BEAN";

    @Autowired
    @Qualifier(SAMPLE_READER)
    private ItemStreamReader<List<Sample>> reader;

    @Autowired
    private ItemProcessor<List<Sample>, IVariantSource> processor;

    @Autowired
    @Qualifier(VARIANT_SOURCE_WRITER)
    private ItemWriter<IVariantSource> writer;

    @Autowired
    private StepListenerSupport<Sample, IVariantSource> listenerLogger;

    @Bean
    public SimpleCompletionPolicy chunkSizecompletionPolicy(Parameters parameters) {
        return new SimpleCompletionPolicy(parameters.getChunkSize());
    }

    @Bean(IMPORT_SAMPLES_STEP_BEAN)
    public Step importSamplesStep(StepBuilderFactory stepBuilderFactory,
                                  SimpleCompletionPolicy chunkSizeCompletionPolicy) {
        logger.debug("Building '" + IMPORT_SAMPLES_STEP + "'");

        return stepBuilderFactory.get(IMPORT_SAMPLES_STEP)
                .<List<Sample>, IVariantSource>chunk(chunkSizeCompletionPolicy)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener((StepExecutionListener) listenerLogger)
                .listener((ChunkListener) listenerLogger)
                .listener((ItemReadListener) listenerLogger)
                .listener((ItemWriteListener) listenerLogger)
                .build();
    }

}
