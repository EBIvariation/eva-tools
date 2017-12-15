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

package uk.ac.ebi.eva.dbsnpimporter.frequencies;

import uk.ac.ebi.eva.commons.core.models.VariantStatistics;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.dbsnpimporter.models.AlleleFrequency;
import uk.ac.ebi.eva.dbsnpimporter.models.PopulationFrequencies;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariantFrequenciesBuilder {

    private FrequenciesInfoParser frequenciesInfoParser;

    public VariantFrequenciesBuilder(FrequenciesInfoParser frequenciesInfoParser) {
        this.frequenciesInfoParser = frequenciesInfoParser;
    }

    // TODO: new exception?
    public Map<String, VariantStatistics> build(Variant variant, String frequenciesInfo) throws IOException {
        if (frequenciesInfo == null || frequenciesInfo.trim().isEmpty()) {
            return null;
        } else {
            Map<String, VariantStatistics> statistics = new HashMap<>();

            List<PopulationFrequencies> frequencies = frequenciesInfoParser.parse(frequenciesInfo);

            for (PopulationFrequencies freq : frequencies) {
                AlleleFrequency maf = freq.getAlleleFrequencies().stream().min(
                        (o1, o2) -> (int) (o1.getFrequency() - o2.getFrequency())).get();

                VariantStatistics stats = new VariantStatistics(variant.getReference(), variant.getAlternate(),
                                                                variant.getType(), (float) maf.getFrequency(), -1,
                                                                maf.getAllele(), null, 0, -1, -1, -1, -1, -1, -1);
                statistics.put(freq.getPopulationName(), stats);
            }

            return statistics;
        }
    }

}
