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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import uk.ac.ebi.eva.commons.core.models.IVariant;
import uk.ac.ebi.eva.dbsnpimporter.configuration.processors.AssemblyCheckFilterProcessorConfiguration;
import uk.ac.ebi.eva.dbsnpimporter.configuration.processors.RefseqToGenbankMappingProcessorConfiguration;
import uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.AssemblyCheckerFilterProcessor;
import uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.MatchingAllelesFilterProcessor;
import uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.MissingCoordinatesFilterProcessor;
import uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.RefseqToGenbankMappingProcessor;
import uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.SubSnpCoreFieldsToEvaSubmittedVariantProcessor;
import uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.SubSnpCoreFieldsToVariantProcessor;
import uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.UnambiguousAllelesFilterProcessor;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;
import uk.ac.ebi.eva.dbsnpimporter.parameters.Parameters;
import uk.ac.ebi.eva.dbsnpimporter.sequence.FastaSequenceReader;

import java.util.Arrays;
import java.util.List;

import static uk.ac.ebi.eva.dbsnpimporter.parameters.Parameters.PROCESSOR;

@Configuration
@EnableConfigurationProperties(Parameters.class)
@Import({RefseqToGenbankMappingProcessorConfiguration.class, AssemblyCheckFilterProcessorConfiguration.class})
public class VariantsProcessorConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(VariantsProcessorConfiguration.class);

    public static final String VARIANTS_PROCESSOR = "VARIANTS_PROCESSOR";

    @Autowired
    private RefseqToGenbankMappingProcessor refseqToGenbankMappingProcessor;

    @Autowired
    private AssemblyCheckerFilterProcessor assemblyCheckFilterProcessor;

    @Bean(name = VARIANTS_PROCESSOR)
    @ConditionalOnProperty(name = PROCESSOR, havingValue = "SubSnpCoreFieldsToVariantProcessor")
    ItemProcessor<SubSnpCoreFields, IVariant> subSnpCoreFieldsToVariantProcessor(Parameters parameters) {
        logger.debug("Injecting SubSnpCoreFieldsToVariantProcessor");
        List<ItemProcessor<SubSnpCoreFields, ?>> delegates = Arrays.asList(
                new MissingCoordinatesFilterProcessor(),
                new UnambiguousAllelesFilterProcessor(),
                new MatchingAllelesFilterProcessor(),
                refseqToGenbankMappingProcessor,
                assemblyCheckFilterProcessor,
                new SubSnpCoreFieldsToVariantProcessor(parameters.getDbsnpBuild()));
        CompositeItemProcessor<SubSnpCoreFields, IVariant> compositeProcessor = new CompositeItemProcessor<>();
        compositeProcessor.setDelegates(delegates);
        return compositeProcessor;
    }

    @Bean(name = VARIANTS_PROCESSOR)
    @ConditionalOnProperty(name = PROCESSOR, havingValue = "SubSnpCoreFieldsToEvaSubmittedVariantProcessor")
    ItemProcessor<SubSnpCoreFields, IVariant> subSnpCoreFieldsToEvaSubmittedVariantProcessor() {
        logger.debug("Injecting SubSnpCoreFieldsToEvaSubmittedVariantProcessor");
        List<ItemProcessor<SubSnpCoreFields, ?>> delegates = Arrays.asList(
                new MissingCoordinatesFilterProcessor(),
                new UnambiguousAllelesFilterProcessor(),
                new MatchingAllelesFilterProcessor(),
                refseqToGenbankMappingProcessor,
                new SubSnpCoreFieldsToEvaSubmittedVariantProcessor());
        CompositeItemProcessor<SubSnpCoreFields, IVariant> compositeProcessor = new CompositeItemProcessor<>();
        compositeProcessor.setDelegates(delegates);
        return compositeProcessor;
    }

}

