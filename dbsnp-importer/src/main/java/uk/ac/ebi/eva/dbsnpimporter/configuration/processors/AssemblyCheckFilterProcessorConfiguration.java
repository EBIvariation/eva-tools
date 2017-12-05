/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.eva.dbsnpimporter.configuration.processors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.AssemblyCheckFilterProcessor;
import uk.ac.ebi.eva.dbsnpimporter.parameters.Parameters;
import uk.ac.ebi.eva.dbsnpimporter.sequence.FastaSequenceReader;

import java.nio.file.Path;
import java.nio.file.Paths;

import static uk.ac.ebi.eva.dbsnpimporter.configuration.processors.RefseqToGenbankMappingProcessorConfiguration
        .NOT_TEST_PROFILE;

@Configuration
public class AssemblyCheckFilterProcessorConfiguration {

    @Bean
    @Profile(NOT_TEST_PROFILE)
    AssemblyCheckFilterProcessor assemblyCheckFilterProcessor(Parameters parameters) {
        Path referenceFastaFile = Paths.get(parameters.getReferenceFastaFile());
        return new AssemblyCheckFilterProcessor(new FastaSequenceReader(referenceFastaFile));
    }

}
