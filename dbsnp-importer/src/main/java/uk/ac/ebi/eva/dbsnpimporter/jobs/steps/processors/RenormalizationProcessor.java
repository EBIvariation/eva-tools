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
import uk.ac.ebi.eva.commons.core.models.VariantStatistics;
import uk.ac.ebi.eva.commons.core.models.VariantType;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.pipeline.VariantSourceEntry;
import uk.ac.ebi.eva.commons.mongodb.entities.VariantMongo;
import uk.ac.ebi.eva.dbsnpimporter.sequence.FastaSequenceReader;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Normalizes the reference and alternate alleles of a dbSNP ambiguous INDEL to make it follow the same
 * normalization process as EVA ones.
 */
public class RenormalizationProcessor implements ItemProcessor<IVariant, IVariant> {

    private static final Logger logger = LoggerFactory.getLogger(RenormalizationProcessor.class);

    public static final String AMBIGUOUS_VARIANT_KEY = "ambiguous";

    public static final String AMBIGUOUS_VARIANT_VALUE = "true";

    private FastaSequenceReader fastaSequenceReader;

    public RenormalizationProcessor(FastaSequenceReader fastaSequenceReader) {
        this.fastaSequenceReader = fastaSequenceReader;
    }

    @Override
    public IVariant process(IVariant variant) throws Exception {
        if (isAmbiguous(variant)) {
            return renormalize(variant);
        }
        return variant;
    }

    /**
     * We define the requirements to be an ambiguous variant as: being an indel (one allele is empty) and the non empty
     * allele ends with the same nucleotide as what is in the reference assembly right before the variant.
     *
     * The reason for this is because we know the variants being processed have removed the leftmost nucleotide as
     * redundant context, and we want it to have removed the rightmost nucleotide. This only yield different results
     * if the original context nucleotide appears in the beginning and end of the other allele.
     *
     * Examples: let's say in position 10 there's A, and an insertion happens between bases 10 and 11 of GGT.
     * This is represented as 11: "" > "GGT". This won't be ambiguous because T is not the same as A (last nucleotide !=
     * nucleotide before the variant). In contrast, if the insertion was "GGA" it is ambiguous. It would have appeared
     * originally as 10: "A" > "AGGA", which can be interpreted as 11: "" > "GGA" (variant being processed) or as
     * 10: "" > "AGG" (desired representation). For deletions the explanation is the same.
     *
     * For the needed change,
     * @see RenormalizationProcessor#renormalize(uk.ac.ebi.eva.commons.core.models.IVariant)
     * @see RenormalizationProcessor#renormalizeAllele(java.lang.String)
     */
    private boolean isAmbiguous(IVariant variant) {
        try {
            boolean isIndel = variant.getType() == VariantType.INDEL;
            boolean oneAlleleIsEmpty = variant.getReference().isEmpty() ^ variant.getAlternate().isEmpty();
            return isIndel && oneAlleleIsEmpty && areContextAndLastNucleotideEqual(variant);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            // if something went wrong with the fasta, we can not say it's ambiguous
            return false;
        }
    }

    private boolean areContextAndLastNucleotideEqual(IVariant variant) {
        String nonEmptyAllele = variant.getReference().isEmpty() ? variant.getAlternate() : variant.getReference();
        char lastNucleotideInAllele = nonEmptyAllele.charAt(nonEmptyAllele.length() - 1);
        char contextBaseInAssembly = getContextBaseInAssembly(variant);

        return lastNucleotideInAllele == contextBaseInAssembly;
    }

    private char getContextBaseInAssembly(IVariant variant) {
        long contextPosition = variant.getStart() - 1;
        String sequence = fastaSequenceReader.getSequence(variant.getChromosome(), contextPosition,
                                                          contextPosition);
        if (sequence == null || sequence.length() != 1) {
            throw new RuntimeException(
                    "Reference sequence could not be retrieved correctly for chromosome=\"" + variant.getChromosome()
                            + "\", position=" + contextPosition);
        }
        char contextBaseInAssembly = sequence.charAt(0);
        return contextBaseInAssembly;
    }

    /**
     * Change the positions and the non empty allele to the desired representation
     *
     * For a complete explanation,
     * @see RenormalizationProcessor#isAmbiguous(uk.ac.ebi.eva.commons.core.models.IVariant)
     * @see RenormalizationProcessor#renormalizeAllele(java.lang.String)
     */
    private Variant renormalize(IVariant variant) {
        String renormalizedReference = variant.getReference();
        String renormalizedAlternate = variant.getAlternate();
        long renormalizedStart = variant.getStart() - 1;
        long renormalizedEnd = variant.getEnd() - 1;
        if (variant.getReference().isEmpty()) {
            renormalizedAlternate = renormalizeAllele(variant.getAlternate());
        } else if (variant.getAlternate().isEmpty()) {
            renormalizedReference = renormalizeAllele(variant.getReference());
        } else {
            throw new AssertionError("Can not re-normalize due to non-standard INDEL: " + variant);
        }
        Variant renormalized = renormalizeVariant(variant, renormalizedAlternate, renormalizedReference, renormalizedStart,
                                                  renormalizedEnd);
        return renormalized;
    }

    private Variant renormalizeVariant(IVariant variant, String renormalizedAlternate, String renormalizedReference,
                                       long renormalizedStart, long renormalizedEnd) {
        Variant copied = new Variant(variant.getChromosome(), renormalizedStart, renormalizedEnd, renormalizedReference,
                                     renormalizedAlternate);
        copied.setIds(variant.getIds());
        copied.setMainId(variant.getMainId());
        copied.setDbsnpIds(variant.getDbsnpIds());

        copied.addSourceEntries(variant.getSourceEntries()
                                       .stream()
                                       .map(variantSourceEntry -> renormalizeVariantSourceEntry(variantSourceEntry,
                                                                                                variant,
                                                                                                renormalizedReference,
                                                                                                renormalizedAlternate))
                                       .collect(Collectors.toList()));
        return copied;
    }

    private VariantSourceEntry renormalizeVariantSourceEntry(IVariantSourceEntry variantSourceEntry,
                                                             IVariant variant,
                                                             String renormalizedReference,
                                                             String renormalizedAlternate) {
        return new VariantSourceEntry(
                variantSourceEntry.getFileId(),
                variantSourceEntry.getStudyId(),
                variantSourceEntry.getSecondaryAlternates(),
                variantSourceEntry.getFormat(),
                renormalizeStatistics(variantSourceEntry, renormalizedReference, renormalizedAlternate),
                renormalizeAttributes(variantSourceEntry.getAttributes(), variant, renormalizedReference,
                                      renormalizedAlternate),
                variantSourceEntry.getSamplesData()
        );
    }

    private Map<String, VariantStatistics> renormalizeStatistics(IVariantSourceEntry variantSourceEntry,
                                                                 String renormalizedReference,
                                                                 String renormalizedAlternate) {
        Map<String, VariantStatistics> renormalizedStatistics = new HashMap<>();
        for (Map.Entry<String, VariantStatistics> statisticsEntry : variantSourceEntry.getCohortStats().entrySet()) {
            VariantStatistics stats = statisticsEntry.getValue();
            String renormalizedMafAllele = renormalizeMafAllele(stats, renormalizedReference, renormalizedAlternate);
            VariantStatistics variantStatistics = new VariantStatistics(renormalizedReference,
                                                                        renormalizedAlternate,
                                                                        stats.getVariantType(),
                                                                        stats.getMaf(),
                                                                        stats.getMgf(),
                                                                        renormalizedMafAllele,
                                                                        stats.getMgfGenotype(),
                                                                        stats.getMissingAlleles(),
                                                                        stats.getMissingGenotypes(),
                                                                        stats.getMendelianErrors(),
                                                                        stats.getCasesPercentDominant(),
                                                                        stats.getControlsPercentDominant(),
                                                                        stats.getCasesPercentRecessive(),
                                                                        stats.getControlsPercentRecessive());
            variantStatistics.setRefAlleleCount(stats.getRefAlleleCount());
            variantStatistics.setAltAlleleCount(stats.getAltAlleleCount());
            variantStatistics.setRefAlleleFreq(stats.getRefAlleleFreq());
            variantStatistics.setAltAlleleFreq(stats.getAltAlleleFreq());

            renormalizedStatistics.put(statisticsEntry.getKey(), variantStatistics);
        }
        return renormalizedStatistics;
    }

    private Map<String, String> renormalizeAttributes(Map<String, String> attributes,
                                                      IVariant variant,
                                                      String renormalizedReference,
                                                      String renormalizedAlternate) {
        HashMap<String, String> renormalizedAttributes = new HashMap<>(attributes);
        renormalizedAttributes.put(AMBIGUOUS_VARIANT_KEY, AMBIGUOUS_VARIANT_VALUE);
        renormalizedAttributes.put(VariantMongo.START_FIELD, Long.toString(variant.getStart()));
        renormalizedAttributes.put(VariantMongo.REFERENCE_FIELD, variant.getReference());
        renormalizedAttributes.put(VariantMongo.ALTERNATE_FIELD, variant.getAlternate());
        return renormalizedAttributes;
    }

    private String renormalizeMafAllele(VariantStatistics stats, String renormalizedReference,
                                        String renormalizedAlternate) {
        if (stats.getRefAllele().equals(stats.getMafAllele())) {
            return renormalizedReference;
        } else if (stats.getAltAllele().equals(stats.getMafAllele())) {
            return renormalizedAlternate;
        } else {
            return stats.getMafAllele();    // a multiallelic could have a secondary alternate as maf allele
        }
    }

    /**
     * Just move the last nucleotide of the allele to the first position
     */
    private String renormalizeAllele(String allele) {
        int length = allele.length();
        char nucleotideThatShouldBeAtTheBeginning = allele.charAt(length - 1);
        String alleleWithoutLastNucleotide = allele.substring(0, length - 1);
        String renormalizedAllele = nucleotideThatShouldBeAtTheBeginning + alleleWithoutLastNucleotide;
        return renormalizedAllele;
    }
}
