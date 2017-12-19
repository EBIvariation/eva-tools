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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ebi.eva.commons.core.models.IVariant;
import uk.ac.ebi.eva.commons.core.models.IVariantSourceEntry;
import uk.ac.ebi.eva.commons.core.models.VariantStatistics;
import uk.ac.ebi.eva.dbsnpimporter.models.AlleleFrequency;
import uk.ac.ebi.eva.dbsnpimporter.models.Orientation;
import uk.ac.ebi.eva.dbsnpimporter.models.PopulationFrequencies;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariantStatisticsBuilder {

    private final ObjectMapper objectMapper;

    private final TypeReference<List<PopulationFrequencies>> typeReference;

    public VariantStatisticsBuilder() {
        objectMapper = new ObjectMapper();
        typeReference = new TypeReference<List<PopulationFrequencies>>() { };
    }

    public Map<String, VariantStatistics> build(IVariant variant, String frequenciesInfo, Orientation orientation)
            throws IOException {
        if (frequenciesInfo == null || frequenciesInfo.trim().isEmpty()) {
            return null;
        } else {
            Map<String, VariantStatistics> statistics = new HashMap<>();
            List<PopulationFrequencies> frequencies = objectMapper.readValue(frequenciesInfo, typeReference);

            for (PopulationFrequencies freq : frequencies) {
                AlleleFrequency maf = freq.getAlleleFrequencies().stream().min(Comparator.naturalOrder()).get();
                maf.setAllele(SubSnpCoreFields.getNormalizedAllele(maf.getAllele(), orientation));

                if (!allelesMatch(variant, maf.getAllele())) {
                    throw new IllegalArgumentException("Variant and frequencies alleles do not match");
                }

                VariantStatistics stats = new VariantStatistics(variant.getReference(), variant.getAlternate(),
                                                                variant.getType(), (float) maf.getFrequency(), -1,
                                                                maf.getAllele(), null, 0, -1, -1, -1, -1, -1, -1);
                statistics.put(freq.getPopulationName(), stats);
            }

            return statistics;
        }
    }

    private boolean allelesMatch(IVariant variant, String mafAllele) {
        if(mafAllele.equals(variant.getReference())) {
            return true;
        }

        if(mafAllele.equals(variant.getAlternate())) {
            return true;
        }

        for (IVariantSourceEntry sourceEntry : variant.getSourceEntries()) {
            for (String secondaryAlternate : sourceEntry.getSecondaryAlternates()) {
                if (mafAllele.equals(secondaryAlternate)) {
                    return true;
                }
            }
        }

        return false;
    }

}
