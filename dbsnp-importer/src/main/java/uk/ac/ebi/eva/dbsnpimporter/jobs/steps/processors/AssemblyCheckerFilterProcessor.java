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
package uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;
import uk.ac.ebi.eva.dbsnpimporter.sequence.SequenceReader;

import java.util.NoSuchElementException;

public class AssemblyCheckerFilterProcessor implements ItemProcessor<SubSnpCoreFields, SubSnpCoreFields> {

    private static final Logger logger = LoggerFactory.getLogger(AssemblyCheckerFilterProcessor.class);

    private SequenceReader assemblyReader;

    public AssemblyCheckerFilterProcessor(SequenceReader assemblyReader) {
        this.assemblyReader = assemblyReader;
    }

    @Override
    public SubSnpCoreFields process(SubSnpCoreFields subSnpCoreFields) throws Exception {
        String referenceAllele = subSnpCoreFields.getReferenceInForwardStrand();
        // check if reference is not empty
        if (referenceIsNotEmpty(referenceAllele)) {
            Region region = subSnpCoreFields.getVariantCoordinates();
            try {
                // catch exceptions
                String sequenceInAssembly = assemblyReader.getSequence(region.getChromosome(), region.getStart(),
                                                                       region.getEnd());
                if (referenceAllele.equals(sequenceInAssembly)) {
                    return subSnpCoreFields;
                } else {
                    logger.warn("Assembly check failed for variant {}. {} expected in {}, {} found",
                                subSnpCoreFields.getSsId(), sequenceInAssembly, region, referenceAllele);
                    return null;
                }
            } catch (IndexOutOfBoundsException  | NoSuchElementException | IllegalArgumentException e) {
                logger.error("Cannot read sequence in {}: {}", region, e.getMessage());
            }
        }
        return subSnpCoreFields;
    }

    private boolean referenceIsNotEmpty(String referenceAllele) {
        return referenceAllele != null && referenceAllele.trim().length() > 0 && !referenceAllele.equals("-");
    }
}
