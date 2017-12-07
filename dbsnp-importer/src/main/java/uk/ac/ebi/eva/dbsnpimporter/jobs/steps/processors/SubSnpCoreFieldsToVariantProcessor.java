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

import uk.ac.ebi.eva.commons.core.models.genotype.Genotype;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.pipeline.VariantSourceEntry;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
    public static final String IUPAC_ALLELE_REGEX = ".*[URYSWKMBDHVN]+.*";
    private static final Pattern genotypePattern = Pattern.compile("/|\\|");

    private final String dbsnpBuild;

    public SubSnpCoreFieldsToVariantProcessor(int dbsnpBuild) {
        this.dbsnpBuild = String.valueOf(dbsnpBuild);
    }

    @Override
    public Variant process(SubSnpCoreFields subSnpCoreFields) throws Exception {
        Variant variant = super.process(subSnpCoreFields);

        VariantSourceEntry variantSourceEntry = new VariantSourceEntry(subSnpCoreFields.getBatch(),
                                                                       subSnpCoreFields.getBatch());
        variantSourceEntry.addAttribute(DBSNP_BUILD_KEY, dbsnpBuild);
        variantSourceEntry.setSecondaryAlternates(subSnpCoreFields.getSecondaryAlternatesInForwardStrand());
        getIUPACSubstitutedGenotypes(subSnpCoreFields).forEach(variantSourceEntry::addSampleData);
        variant.addSourceEntry(variantSourceEntry);

        return variant;
    }

    private List<Map<String, String>> getIUPACSubstitutedGenotypes (SubSnpCoreFields subSnpCoreFields)
    {
        return getGenotypesFromString(subSnpCoreFields.getRawGenotypesString(),
                                      subSnpCoreFields.getReferenceInForwardStrand(),
                                      subSnpCoreFields.getAlternateInForwardStrand(),
                                      subSnpCoreFields.getAlleleOrientation());
    }

    private List<Map<String, String>> getGenotypesFromString(String genotypes_string, String ref, String alt,
                                                             boolean alleleOrientation) {
        if (genotypes_string != null && !genotypes_string.isEmpty()) {
            return Arrays.stream(genotypes_string.split(",", -1))
                         .map(this::substituteIUPACAllelesWithMissing)
                         .map(genotypeString -> getForwardOrientedGenotype(alleleOrientation, genotypeString))
                         .map(genotypeString -> getGenotypeMapFromString(genotypeString, ref, alt))
                         .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private String substituteIUPACAllelesWithMissing(String genotypeString) {
        String outputDelimiterToUse = genotypeString.contains("|") ? "|" : "/";
        if (genotypeString.matches(IUPAC_ALLELE_REGEX)) {
            return "." + outputDelimiterToUse + ".";
        }
        return genotypeString;
    }

    private String getForwardOrientedGenotype(boolean forward, String genotypeString) {
        String outputDelimiterToUse = genotypeString.contains("|") ? "|" : "/";
        return Arrays.stream(genotypePattern.split(genotypeString, -1))
                     .map(allele -> getForwardOrientedAllele(forward, allele))
                     .collect(Collectors.joining(outputDelimiterToUse));
    }

    private Map<String, String> getGenotypeMapFromString (String genotypeString, String ref, String alt) {
        return Collections.singletonMap("GT", new Genotype(genotypeString, ref, alt).toString());
    }

    private String getForwardOrientedAllele(boolean forward, String allele) {
        return SubSnpCoreFields.getTrimmedAllele(forward ? allele : SubSnpCoreFields.calculateReverseComplement(allele));
    }
}
