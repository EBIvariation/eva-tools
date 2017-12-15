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
package uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import uk.ac.ebi.eva.commons.core.models.IVariant;
import uk.ac.ebi.eva.commons.core.models.IVariantSourceEntry;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.pipeline.VariantSourceEntry;
import uk.ac.ebi.eva.dbsnpimporter.sequence.FastaSequenceReader;

import java.util.stream.Collectors;

public class RenormalizationProcessor implements ItemProcessor<Variant, Variant> {

    private static final Logger logger = LoggerFactory.getLogger(RenormalizationProcessor.class);

    private FastaSequenceReader fastaSequenceReader;

    public RenormalizationProcessor(FastaSequenceReader fastaSequenceReader) {
        this.fastaSequenceReader = fastaSequenceReader;
    }

    @Override
    public Variant process(Variant variant) throws Exception {
        if (isAmbiguous(variant)) {
            return renormalize(variant);
        }
        return variant;
    }

    private boolean isAmbiguous(Variant variant) {
        boolean isIndel = variant.getReference().isEmpty() ^ variant.getAlternate().isEmpty();
        try {
            return isIndel && areContextAndLastNucleotideEqual(variant);
        } catch (Exception e) {
            // TODO jmmut: should we throw exception and stop the whole job if one variant is not found in the assembly? (or if there was another problem?)
            logger.warn(e.getMessage(), e);
            // if something went wrong with the fasta, we can not say it's ambiguous
            return false;
        }
    }

    private boolean areContextAndLastNucleotideEqual(Variant variant) {
        String allele = variant.getReference().isEmpty() ? variant.getAlternate() : variant.getAlternate();
        char lastNucleotideInAllele = allele.charAt(allele.length() - 1);
        char contextBaseInAssembly = getContextBaseInAssembly(variant);
        return lastNucleotideInAllele == contextBaseInAssembly;
    }

    private char getContextBaseInAssembly(Variant variant) {
        long contextPosition = variant.getStart() - 1;
        String sequence = fastaSequenceReader.getSequence(variant.getChromosome(), contextPosition,
                                                          contextPosition);
        if (sequence == null || sequence.length() != 1) {
            throw new RuntimeException(
                    "Reference sequence could not be retrieved correctly for chromosome=\"" + variant .getChromosome()
                            + "\", position=" + contextPosition);
        }
        char contextBaseInAssembly = sequence.charAt(sequence.length() - 1);
        return contextBaseInAssembly;
    }

    private Variant renormalize(IVariant variant) {
        String renormalizedAlternate = variant.getReference();
        String renormalizedReference = variant.getAlternate();
        long renormalizedStart;
        long renormalizedEnd;
        if (variant.getReference().isEmpty()) {
            renormalizedAlternate = renormalizeAllele(variant.getAlternate());
            renormalizedStart = variant.getStart() - 1;
            renormalizedEnd = variant.getStart() - 1;
        } else if (variant.getAlternate().isEmpty()) {
            renormalizedReference = renormalizeAllele(variant.getReference());
            renormalizedStart = variant.getStart() - 1;
            renormalizedEnd = variant.getStart() - 1;
        } else {
            throw new AssertionError("No alleles were empty, this ambiguous case won't be handled");
        }
        Variant renormalized = copyVariant(variant, renormalizedAlternate, renormalizedReference, renormalizedStart,
                                           renormalizedEnd);
        return renormalized;
    }

    private Variant copyVariant(IVariant variant, String renormalizedAlternate, String renormalizedReference,
                                long renormalizedStart, long renormalizedEnd) {
        Variant copied = new Variant(variant.getChromosome(), renormalizedStart, renormalizedEnd, renormalizedReference,
                                     renormalizedAlternate);

        copied.addSourceEntries(variant.getSourceEntries()
                                       .stream()
                                       .map(this::copyVariantSourceEntry)
                                       .collect(Collectors.toList()));
        return copied;
    }

    private VariantSourceEntry copyVariantSourceEntry(IVariantSourceEntry i) {
        return new VariantSourceEntry(
                i.getFileId(),
                i.getStudyId(),
                i.getSecondaryAlternates(),
                i.getFormat(),
                i.getCohortStats(),
                i.getAttributes(),
                i.getSamplesData()
        );
    }

    /**
     * Move the last nucleotide of the allele to the first position
     *
     * @param alternate
     * @return
     */
    private String renormalizeAllele(String alternate) {
        int length = alternate.length();
        char nucleotideThatShouldBeAtTheBeginning = alternate.charAt(length - 1);
        String alleleCore = alternate.substring(0, length - 1);
        String renormalizeAllele = nucleotideThatShouldBeAtTheBeginning + alleleCore;
        return renormalizeAllele;
    }
}
