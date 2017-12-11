package uk.ac.ebi.eva.dbsnpimporter.io;

import org.junit.Test;

import uk.ac.ebi.eva.dbsnpimporter.models.AlleleFrequency;
import uk.ac.ebi.eva.dbsnpimporter.models.PopulationFrequencies;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
public class FrequencyInfoParserTest {

    private static final String FREQUENCY_INFO = "[{\"pop_id\" : 1324, \"pop_name\" : \"RBLS\", \"freq_info\" : [{\"allele\" : \"ACAG\", \"cnt\" : 2.0, \"freq\" : 0.5}, {\"allele\" : \"-\", \"cnt\" : 2.0, \"freq\" : 0.5}]}]";

    @Test
    public void basicParsing() throws Exception {
        List<PopulationFrequencies> parsedPopulationFrequencies = new FrequencyInfoParser().parse(FREQUENCY_INFO);

        List<AlleleFrequency> alleleFrequencies = new ArrayList<>();
        alleleFrequencies.add(new AlleleFrequency("ACAG", 2.0, 0.5));
        alleleFrequencies.add(new AlleleFrequency("-", 2.0, 0.5));
        List<PopulationFrequencies> expectedFrequencies = new ArrayList<>();
        expectedFrequencies.add(new PopulationFrequencies(1324, "RBLS", alleleFrequencies));

        assertEquals(expectedFrequencies, parsedPopulationFrequencies);
    }
}
