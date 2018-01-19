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

import uk.ac.ebi.eva.commons.core.models.VariantStatistics;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.pipeline.VariantSourceEntry;
import uk.ac.ebi.eva.commons.mongodb.entities.subdocuments.VariantStatisticsMongo;
import uk.ac.ebi.eva.dbsnpimporter.exception.UndefinedHgvsAlleleException;
import uk.ac.ebi.eva.dbsnpimporter.models.Orientation;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Maps {@link SubSnpCoreFields} to {@link uk.ac.ebi.eva.commons.core.models.IVariant},
 * extending {@link SubSnpCoreFieldsToEvaSubmittedVariantProcessor}, and
 * adding a VariantSourceEntry, because the batch (or study) won't be present in EVA.
 */
public class SubSnpCoreFieldsToVariantProcessor extends SubSnpCoreFieldsToEvaSubmittedVariantProcessor {

    public static final String DBSNP_BUILD_KEY = "dbsnp-build";

    public static final String INVALID_GENOTYPE_REGEX = ".*[^,./|ATCGN -].*";

    private static final Pattern invalidGenotypePattern = Pattern.compile(INVALID_GENOTYPE_REGEX);

    private static final Pattern genotypePattern = Pattern.compile("/|\\|");

    private static final Logger logger = LoggerFactory.getLogger(SubSnpCoreFieldsToVariantProcessor.class);

    private final String dbsnpBuild;

    private VariantStatisticsBuilder frequenciesBuilder;

    public SubSnpCoreFieldsToVariantProcessor(int dbsnpBuild) {
        this.dbsnpBuild = String.valueOf(dbsnpBuild);
        frequenciesBuilder = new VariantStatisticsBuilder();
    }

    @Override
    public Variant process(SubSnpCoreFields subSnpCoreFields) throws Exception {
        if (areGenotypesEmpty(subSnpCoreFields) && areFrequenciesEmpty(subSnpCoreFields)) {
            logger.debug("Variant filtered out because neither genotype(s) or frequencies are specified {}",
                         subSnpCoreFields);
            return null;
        }

        if (!areGenotypesEmpty(subSnpCoreFields) && areGenotypesInvalid(subSnpCoreFields)) {
            logger.debug("Variant filtered out because genotype(s) contained bases different from A,C,G,T,N: " +
                                 "genotypes are {} in {}", subSnpCoreFields.getRawGenotypesString(), subSnpCoreFields);
            return null;
        }

        Variant variant = super.process(subSnpCoreFields);
        if (variant == null) {
            return null;
        }

        VariantSourceEntry variantSourceEntry = new VariantSourceEntry(subSnpCoreFields.getBatch(),
                                                                       subSnpCoreFields.getBatch());
        variantSourceEntry.addAttribute(DBSNP_BUILD_KEY, dbsnpBuild);

        try {
            variantSourceEntry.setSecondaryAlternates(subSnpCoreFields.getSecondaryAlternatesInForwardStrand());
            addGenotypesToVariantSourceEntry(subSnpCoreFields, variantSourceEntry);
        } catch (UndefinedHgvsAlleleException hgvsAlleleUndefined) {
            logger.debug("Variant filtered out because allele is not defined: {} ({})", subSnpCoreFields,
                         hgvsAlleleUndefined);
            return null;
        }
        try {
            addFrequenciesToVariantSourceEntry(subSnpCoreFields, variant, variantSourceEntry);
        } catch (Exception e) {
            logger.warn("Variant filtered out because: {}. {}", e.getMessage(), subSnpCoreFields);
            return null;
        }

        variant.addSourceEntry(variantSourceEntry);

        return variant;
    }

    private boolean areGenotypesEmpty(SubSnpCoreFields subSnpCoreFields) {
        String genotypesString = subSnpCoreFields.getRawGenotypesString();
        return genotypesString == null || genotypesString.trim().isEmpty();
    }

    private boolean areFrequenciesEmpty(SubSnpCoreFields subSnpCoreFields) {
        String frequenciesString = subSnpCoreFields.getRawFrequenciesInfo();
        return frequenciesString == null || frequenciesString.trim().isEmpty();
    }

    private boolean areGenotypesInvalid(SubSnpCoreFields subSnpCoreFields) {
        String genotypesString = subSnpCoreFields.getRawGenotypesString();
        return invalidGenotypePattern.matcher(genotypesString).matches();
    }

    private void addGenotypesToVariantSourceEntry(SubSnpCoreFields subSnpCoreFields,
                                                  VariantSourceEntry variantSourceEntry) {
        if (areGenotypesEmpty(subSnpCoreFields)) {
            return;
        }

        variantSourceEntry.setFormat("GT");
        getSamplesData(subSnpCoreFields).forEach(variantSourceEntry::addSampleData);
    }

    private List<Map<String, String>> getSamplesData(SubSnpCoreFields subSnpCoreFields) {
        return getSamplesDataFromGenotypes(subSnpCoreFields.getRawGenotypesString(),
                                           subSnpCoreFields.getReferenceInForwardStrand(),
                                           subSnpCoreFields.getAlternateInForwardStrand(),
                                           subSnpCoreFields.getSecondaryAlternatesInForwardStrand(),
                                           subSnpCoreFields.getAlleleOrientation());
    }

    private List<Map<String, String>> getSamplesDataFromGenotypes(String genotypes, String ref, String alt,
                                                                  String[] secondaryAlternates,
                                                                  Orientation orientation) {
        return Arrays.stream(genotypes.split(",", -1))
                     .map(genotype -> getForwardOrientedGenotypeCode(genotype, ref, alt, secondaryAlternates,
                                                                     orientation))
                     .map(this::getSampleDataFromGenotypeCode)
                     .collect(Collectors.toList());
    }

    private String getForwardOrientedGenotypeCode(String genotype, String ref, String alt,
                                                  String[] secondaryAlternates, Orientation orientation) {
        String alleleDelimiter = genotype.contains("|") ? "|" : "/";
        return Arrays.stream(genotypePattern.split(genotype, -1))
                     .map(allele -> SubSnpCoreFields.getNormalizedAllele(allele, orientation))
                     .map(allele -> getAlleleIndex(allele, ref, alt, secondaryAlternates))
                     .map(String::valueOf)
                     .collect(Collectors.joining(alleleDelimiter));
    }

    private int getAlleleIndex(String allele, String ref, String alt, String[] secondaryAlternates) {
        if (allele.equals(ref)) {
            return 0;
        } else if (allele.equals(alt)) {
            return 1;
        } else {
            int index = 2;
            for (String secondaryAlternate : secondaryAlternates) {
                if (allele.equals(secondaryAlternate)) {
                    return index;
                }
                ++index;
            }
            return -1;
        }
    }

    private Map<String, String> getSampleDataFromGenotypeCode(String genotypeCode) {
        return Collections.singletonMap("GT", genotypeCode);
    }

    private void addFrequenciesToVariantSourceEntry(SubSnpCoreFields subSnpCoreFields, Variant variant,
                                                    VariantSourceEntry variantSourceEntry) throws IOException {
        if (areFrequenciesEmpty(subSnpCoreFields)) {
            return;
        }

        Map<String, VariantStatistics> variantStats = frequenciesBuilder.build(variant,
                                                                               subSnpCoreFields.getRawFrequenciesInfo(),
                                                                               subSnpCoreFields.getAlleleOrientation());
        if (variantStats != null) {
            variantSourceEntry.setCohortStats(variantStats);
            for (Map.Entry<String, VariantStatistics> populationStatistics : variantStats.entrySet()) {
                variantSourceEntry.addAttribute(buildPopulationMafField(populationStatistics.getKey()),
                                                Float.toString(populationStatistics.getValue().getMaf()));
            }
        }
    }

    public String buildPopulationMafField(String population) {
        return population + "_" + VariantStatisticsMongo.MAF_FIELD;
    }
}
