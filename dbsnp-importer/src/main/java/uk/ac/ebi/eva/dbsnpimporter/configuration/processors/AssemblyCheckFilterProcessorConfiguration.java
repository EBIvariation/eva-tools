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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepListenerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.AssemblyCheckFilterProcessor;
import uk.ac.ebi.eva.dbsnpimporter.parameters.Parameters;
import uk.ac.ebi.eva.dbsnpimporter.sequence.FastaSequenceReader;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class AssemblyCheckFilterProcessorConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AssemblyCheckFilterProcessorConfiguration.class);

    public static final String FASTA_SEQUENCE_READER_CLOSER = "FASTA_SEQUENCE_READER_CLOSER";

    private FastaSequenceReader fastaSequenceReader;

    @Bean
    AssemblyCheckFilterProcessor assemblyCheckFilterProcessor(Parameters parameters) {
        Path referenceFastaFile = Paths.get(parameters.getReferenceFastaFile());
        fastaSequenceReader = new FastaSequenceReader(referenceFastaFile);
        return new AssemblyCheckFilterProcessor(fastaSequenceReader);

    }

    @Bean(FASTA_SEQUENCE_READER_CLOSER)
    StepListenerSupport fastaSequenceReaderCloser() {
        return new StepListenerSupport() {
            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                try {
                    logger.debug("Closing fasta file reader used for assembly check");
                    fastaSequenceReader.close();
                } catch (Exception e) {
                    logger.warn("Error closing fasta file reader used for assembly check: {}", e.getMessage());
                }
                return stepExecution.getExitStatus();
            }
        };
    }
}
