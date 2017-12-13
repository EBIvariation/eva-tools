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
package uk.ac.ebi.eva.dbsnpimporter.io;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.eva.dbsnpimporter.models.AlleleFrequency;
import uk.ac.ebi.eva.dbsnpimporter.models.PopulationFrequencies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FrequencyInfoParserTest {

    private String singlePopulationJson;

    private String severalPopulationsJson;

    @Before
    public void setUp() throws Exception {
        String pop1 = "{\"pop_id\" : 1324, \"pop_name\" : \"POP1\", \"freq_info\" : " +
                "[{\"allele\" : \"ACAG\", \"cnt\" : 2.0, \"freq\" : 0.5}, " +
                " {\"allele\" : \"-\", \"cnt\" : 2.0, \"freq\" : 0.5}]}";

        String pop2 = "{\"pop_id\" : 1325, \"pop_name\" : \"POP2\", \"freq_info\" : " +
                "[{\"allele\" : \"A\", \"cnt\" : 6.0, \"freq\" : 0.3}, " +
                "{\"allele\" : \"T\", \"cnt\" : 10.0, \"freq\" : 0.5}, " +
                " {\"allele\" : \"G\", \"cnt\" : 4.0, \"freq\" : 0.2}]}";

        String pop3 = "{\"pop_id\" : 1326, \"pop_name\" : \"POP3\", \"freq_info\" : " +
                "[{\"allele\" : \"A\", \"cnt\" : 6.0, \"freq\" : 0.6}, " +
                " {\"allele\" : \"G\", \"cnt\" : 4.0, \"freq\" : 0.4}]}";

        singlePopulationJson = "[" + pop1 + "]";

        severalPopulationsJson = "[" + StringUtils.join(Arrays.asList(pop1, pop2, pop3), ",") + "]";

    }

    @Test
    public void parseJsonArrayContainingOnePopulation() throws Exception {
        List<PopulationFrequencies> parsedPopulationFrequencies = new FrequencyInfoParser().parse(singlePopulationJson);

        List<AlleleFrequency> alleleFrequencies = new ArrayList<>();
        alleleFrequencies.add(new AlleleFrequency("ACAG", 2.0, 0.5));
        alleleFrequencies.add(new AlleleFrequency("-", 2.0, 0.5));

        List<PopulationFrequencies> expectedFrequenciesList = new ArrayList<>();
        expectedFrequenciesList.add(new PopulationFrequencies(1324, "POP1", alleleFrequencies));

        assertEquals(expectedFrequenciesList, parsedPopulationFrequencies);
    }

    @Test
    public void parseJsonArrayContainingSeveralPopulations() throws Exception {
        List<PopulationFrequencies> parsedPopulationFrequencies = new FrequencyInfoParser().parse(
                severalPopulationsJson);

        List<PopulationFrequencies> expectedFrequenciesList = new ArrayList<>();

        List<AlleleFrequency> alleleFrequencies = new ArrayList<>();
        alleleFrequencies.add(new AlleleFrequency("ACAG", 2.0, 0.5));
        alleleFrequencies.add(new AlleleFrequency("-", 2.0, 0.5));
        expectedFrequenciesList.add(new PopulationFrequencies(1324, "POP1", alleleFrequencies));

        alleleFrequencies = new ArrayList<>();
        alleleFrequencies.add(new AlleleFrequency("A", 6.0, 0.3));
        alleleFrequencies.add(new AlleleFrequency("T", 10.0, 0.5));
        alleleFrequencies.add(new AlleleFrequency("G", 4.0, 0.2));
        expectedFrequenciesList.add(new PopulationFrequencies(1325, "POP2", alleleFrequencies));

        alleleFrequencies = new ArrayList<>();
        alleleFrequencies.add(new AlleleFrequency("A", 6.0, 0.6));
        alleleFrequencies.add(new AlleleFrequency("G", 4.0, 0.4));
        expectedFrequenciesList.add(new PopulationFrequencies(1326, "POP3", alleleFrequencies));

        assertEquals(expectedFrequenciesList, parsedPopulationFrequencies);
    }
}
