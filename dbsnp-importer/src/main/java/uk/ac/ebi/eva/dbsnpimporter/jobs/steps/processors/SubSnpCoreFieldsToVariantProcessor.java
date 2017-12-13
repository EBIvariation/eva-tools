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

import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.pipeline.VariantSourceEntry;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

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

    public SubSnpCoreFieldsToVariantProcessor(int dbsnpBuild) {
        this.dbsnpBuild = String.valueOf(dbsnpBuild);
    }

    @Override
    public Variant process(SubSnpCoreFields subSnpCoreFields) throws Exception {
        if (areGenotypesInvalid(subSnpCoreFields)) {
            logger.debug("Filter out variant because of invalid genotypes: {}, {}", subSnpCoreFields,
                         subSnpCoreFields.getRawGenotypesString());
            return null;
        }

        Variant variant = super.process(subSnpCoreFields);

        VariantSourceEntry variantSourceEntry = new VariantSourceEntry(subSnpCoreFields.getBatch(),
                                                                       subSnpCoreFields.getBatch());
        variantSourceEntry.addAttribute(DBSNP_BUILD_KEY, dbsnpBuild);
        variantSourceEntry.setSecondaryAlternates(subSnpCoreFields.getSecondaryAlternatesInForwardStrand());
         variantSourceEntry.setFormat("GT");

        getGenotypes(subSnpCoreFields).forEach(variantSourceEntry::addSampleData);
        variant.addSourceEntry(variantSourceEntry);

        return variant;
    }

    private boolean areGenotypesInvalid(SubSnpCoreFields subSnpCoreFields) {
        String genotypesString = subSnpCoreFields.getRawGenotypesString();
        return genotypesString == null
                || genotypesString.isEmpty()
                || hasInvalidCharacters(genotypesString);
    }

    private boolean hasInvalidCharacters(String genotypesString) {
        return invalidGenotypePattern.matcher(genotypesString).matches();
    }

    private List<Map<String, String>> getGenotypes (SubSnpCoreFields subSnpCoreFields) {
        return getGenotypesFromString(subSnpCoreFields.getRawGenotypesString(),
                                      subSnpCoreFields.getReferenceInForwardStrand(),
                                      subSnpCoreFields.getAlternateInForwardStrand(),
                                      subSnpCoreFields.getSecondaryAlternatesInForwardStrand(),
                                      subSnpCoreFields.getAlleleOrientation());
    }

    private List<Map<String, String>> getGenotypesFromString(String genotypesString, String ref, String alt,
                                                             String[] secondaryAlternates,
                                                             boolean alleleOrientation) {
        return Arrays.stream(genotypesString.split(",", -1))
                     .map(genotypeString -> getForwardOrientedGenotypeCodes(alleleOrientation, genotypeString, ref, alt,
                                                                            secondaryAlternates))
                     .map(genotypeCodesString -> getGenotypeMapFromString(genotypeCodesString))
                     .collect(Collectors.toList());
    }

    private String getForwardOrientedGenotypeCodes(boolean forward, String genotypeString, String ref,
                                                   String alt, String[] secondaryAlternates) {
        String outputDelimiterToUse = genotypeString.contains("|") ? "|" : "/";
        return Arrays.stream(genotypePattern.split(genotypeString, -1))
                     .map(allele -> getForwardOrientedAndTrimmedAllele(forward, allele))
                     .map(allele -> getAlleleCode(allele, ref, alt, secondaryAlternates))
                     .map(Object::toString)
                     .collect(Collectors.joining(outputDelimiterToUse));
    }

    private String getForwardOrientedAndTrimmedAllele(boolean forward, String allele) {
        String forwardAllele = forward ? allele : SubSnpCoreFields.calculateReverseComplement(allele);
        return SubSnpCoreFields.getTrimmedAllele(forwardAllele);
    }

    private Integer getAlleleCode(String allele, String ref, String alt, String[] secondaryAlternates) {
        if (allele.equals(ref)) {
            return 0;
        } else if (allele.equals(alt)) {
            return 1;
        } else {
            Integer index = 2;
            for (String secondaryAlternate : secondaryAlternates) {
                if (allele.equals(secondaryAlternate)) {
                    return index;
                }
                ++index;
            }
            return -1;
        }
    }

    private Map<String, String> getGenotypeMapFromString(String genotypeCodeString) {
        return Collections.singletonMap("GT", genotypeCodeString);
    }
}
