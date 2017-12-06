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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;

import uk.ac.ebi.eva.commons.mongodb.writers.VariantSourceMongoWriter;
import uk.ac.ebi.eva.dbsnpimporter.parameters.Parameters;

@Configuration
@EnableConfigurationProperties(Parameters.class)
public class VariantSourceWriterConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(VariantSourceWriterConfiguration.class);

    public static final String VARIANT_SOURCE_WRITER = "VARIANT_SOURCE_WRITER";

    @Bean(name = VARIANT_SOURCE_WRITER)
    VariantSourceMongoWriter variantSourceWriter(Parameters parameters, MongoOperations mongoOperations) throws Exception {
        logger.debug("Injecting VariantSourceMongoWriter");
        return new VariantSourceMongoWriter(mongoOperations, parameters.getVariantsCollection());
    }

}
