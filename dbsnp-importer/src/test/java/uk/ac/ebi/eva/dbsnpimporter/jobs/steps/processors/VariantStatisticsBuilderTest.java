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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.ac.ebi.eva.commons.core.models.IVariant;
import uk.ac.ebi.eva.commons.core.models.VariantStatistics;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.pipeline.VariantSourceEntry;
import uk.ac.ebi.eva.dbsnpimporter.models.Orientation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class VariantStatisticsBuilderTest {

    private String pop1, pop2, pop3;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        pop1 = "{\"pop_id\" : 1324, \"pop_name\" : \"POP1\", \"freq_info\" : " +
                "[{\"allele\" : \"ACAG\", \"cnt\" : 2.0, \"freq\" : 0.5}, " +
                "{\"allele\" : \"-\", \"cnt\" : 2.0, \"freq\" : 0.5}]}";

        pop2 = "{\"pop_id\" : 1325, \"pop_name\" : \"POP2\", \"freq_info\" : " +
                "[{\"allele\" : \"A\", \"cnt\" : 6.0, \"freq\" : 0.3}, " +
                "{\"allele\" : \"T\", \"cnt\" : 10.0, \"freq\" : 0.5}, " +
                "{\"allele\" : \"G\", \"cnt\" : 4.0, \"freq\" : 0.2}]}";

        pop3 = "{\"pop_id\" : 1326, \"pop_name\" : \"POP3\", \"freq_info\" : " +
                "[{\"allele\" : \"A\", \"cnt\" : 6.0, \"freq\" : 0.6}, " +
                "{\"allele\" : \"G\", \"cnt\" : 4.0, \"freq\" : 0.4}]}";


    }

    @Test
    public void testBuildStatisticsForOnePopulationInsertion() throws Exception {
        IVariant variant = new Variant("1", 1000, 1000, "", "ACAG");
        String singlePopulationJson = "[" + pop1 + "]";

        Map<String, VariantStatistics> variantStatistics = new VariantStatisticsBuilder().build(variant,
                                                                                                singlePopulationJson,
                                                                                                Orientation.FORWARD);

        assertEquals(1, variantStatistics.size());

        for (Map.Entry<String, VariantStatistics> entry : variantStatistics.entrySet()) {
            String populationName = entry.getKey();
            assertEquals("POP1", populationName);

            VariantStatistics populationStatistics = entry.getValue();
            assertEquals("", populationStatistics.getRefAllele());
            assertEquals("ACAG", populationStatistics.getAltAllele());

            assertEquals(0.5, populationStatistics.getMaf(), 0.01);
            assertEquals("ACAG", populationStatistics.getMafAllele());
        }
    }

    @Test
    public void testBuildStatisticsForOnePopulationDeletion() throws Exception {
        IVariant variant = new Variant("1", 1000, 1003, "ACAG", "");
        String singlePopulationJson = "[" + pop1 + "]";

        Map<String, VariantStatistics> variantStatistics = new VariantStatisticsBuilder().build(variant,
                                                                                                singlePopulationJson,
                                                                                                Orientation.FORWARD);
        assertEquals(1, variantStatistics.size());

        for (Map.Entry<String, VariantStatistics> entry : variantStatistics.entrySet()) {
            String populationName = entry.getKey();
            assertEquals("POP1", populationName);

            VariantStatistics populationStatistics = entry.getValue();
            assertEquals("ACAG", populationStatistics.getRefAllele());
            assertEquals("", populationStatistics.getAltAllele());

            assertEquals(0.5, populationStatistics.getMaf(), 0.01);
            assertEquals("ACAG", populationStatistics.getMafAllele());
        }
    }

    @Test
    public void testBuildStatisticsForSeveralPopulations() throws Exception {
        Variant variant = new Variant("1", 1000, 1000, "A", "T");
        VariantSourceEntry sourceEntry = new VariantSourceEntry("FILEID", "STUDYID", new String[]{"G"}, "DP");
        variant.addSourceEntry(sourceEntry);

        String severalPopulationsJson = "[" + String.join(",", Arrays.asList(pop2, pop3))+ "]";

        Map<String, VariantStatistics> variantStatistics = new VariantStatisticsBuilder().build(variant,
                                                                                                severalPopulationsJson,
                                                                                                Orientation.FORWARD);
        assertEquals(2, variantStatistics.size());

        assertNull(variantStatistics.get("POP1"));

        VariantStatistics statisticsPop2 = variantStatistics.get("POP2");
        assertEquals("A", statisticsPop2.getRefAllele());
        assertEquals("T", statisticsPop2.getAltAllele());

        assertEquals(0.2, statisticsPop2.getMaf(), 0.01);
        assertEquals("G", statisticsPop2.getMafAllele());

        VariantStatistics statisticsPop3 = variantStatistics.get("POP3");
        assertEquals("A", statisticsPop3.getRefAllele());
        assertEquals("T", statisticsPop3.getAltAllele());

        assertEquals(0.4, statisticsPop3.getMaf(), 0.01);
        assertEquals("G", statisticsPop3.getMafAllele());
    }

    @Test
    public void failWithInvalidJson() throws IOException {
        String jsonWithNoWrappingBrackets = "{\"pop_id\" : 1324, \"pop_name\" : \"POP1\", \"freq_info\" : " +
                "[{\"allele\" : \"ACAG\", \"cnt\" : 2.0, \"freq\" : 0.5}, " +
                "{\"allele\" : \"-\", \"cnt\" : 2.0, \"freq\" : 0.5}]}";
        IVariant variant = new Variant("1", 1000, 1000, "A", "T");

        thrown.expect(IOException.class);
        new VariantStatisticsBuilder().build(variant, jsonWithNoWrappingBrackets, Orientation.FORWARD);
    }

    @Test
    public void failWithMismatchingVariantAndFrequenciesAlleles() throws IOException {
        IVariant variant = new Variant("1", 1000, 1000, "A", "T");
        String singlePopulationJson = "[" + pop1 + "]";

        thrown.expect(IllegalArgumentException.class);
        new VariantStatisticsBuilder().build(variant, singlePopulationJson, Orientation.FORWARD);
    }

    @Test
    public void testVariantAndFrequenciesAllelesMatchWithReverseOrientation() throws IOException {
        IVariant variant = new Variant("1", 1000, 1000, "CTGT", "");
        String singlePopulationJson = "[" + pop1 + "]";

        new VariantStatisticsBuilder().build(variant, singlePopulationJson, Orientation.REVERSE);
    }
}
