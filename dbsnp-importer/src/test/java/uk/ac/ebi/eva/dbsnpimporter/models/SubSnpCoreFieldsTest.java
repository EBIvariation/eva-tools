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
package uk.ac.ebi.eva.dbsnpimporter.models;

import org.junit.Test;

import uk.ac.ebi.eva.commons.core.models.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SubSnpCoreFieldsTest {

    @Test
    public void testContigAndChromosomeCoordinates() {
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(12345, Orientation.FORWARD, 123L, Orientation.FORWARD,
                                                                 "contigName", 1L, 10L, Orientation.REVERSE,
                                                                 LocusType.SNP, "chromosomeName", 5L, 50L, "A", "A",
                                                                 "T", "T/A", "", null, null, Orientation.FORWARD, "",
                                                                 null, null, Orientation.FORWARD, null, "batch");

        assertEquals(12345, subSnpCoreFields.getSsId());
        assertNotNull(subSnpCoreFields.getRsId());
        assertEquals(123, (long) subSnpCoreFields.getRsId());
        assertEquals(Orientation.FORWARD, subSnpCoreFields.getSnpOrientation());
        assertEquals(new Region("contigName", 1L, 10L), subSnpCoreFields.getContigRegion());
        assertEquals(Orientation.REVERSE, subSnpCoreFields.getContigOrientation());
        assertEquals(new Region("chromosomeName", 5L, 50L), subSnpCoreFields.getChromosomeRegion());
    }

    @Test
    public void testContigCoordinatesOnly() {
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(12345, Orientation.FORWARD, 123L, Orientation.FORWARD,
                                                                 "contigName", 1L, 10L, Orientation.REVERSE,
                                                                 LocusType.SNP, null, null, null, "T", "T", "A", "T/A",
                                                                 "", null, null, Orientation.FORWARD, "", null, null,
                                                                 Orientation.FORWARD, null, "batch");

        assertEquals(12345, subSnpCoreFields.getSsId());
        assertEquals(Orientation.FORWARD, subSnpCoreFields.getSnpOrientation());
        assertEquals(new Region("contigName", 1L, 10L), subSnpCoreFields.getContigRegion());
        assertEquals(Orientation.REVERSE, subSnpCoreFields.getContigOrientation());
        assertNull(subSnpCoreFields.getChromosomeRegion());
    }

    @Test
    public void testWithoutChromosomeCoordinates() {
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(12345, Orientation.FORWARD, 123L, Orientation.FORWARD,
                                                                 "contigName", 1L, 10L, Orientation.REVERSE,
                                                                 LocusType.SNP, "chromosomeName", null, null, "T", "T",
                                                                 "A", "T/A", "", null, null, Orientation.FORWARD, "",
                                                                 null, null, Orientation.FORWARD, null, "batch");

        assertEquals(12345, subSnpCoreFields.getSsId());
        assertEquals(Orientation.FORWARD, subSnpCoreFields.getSnpOrientation());
        assertEquals(new Region("contigName", 1L, 10L), subSnpCoreFields.getContigRegion());
        assertEquals(Orientation.REVERSE, subSnpCoreFields.getContigOrientation());
        assertEquals(new Region("chromosomeName"), subSnpCoreFields.getChromosomeRegion());
    }

    @Test
    public void testWithoutContigCoordinates() {
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(12345, Orientation.FORWARD, 123L, Orientation.FORWARD,
                                                                 "contigName", null, null, Orientation.REVERSE,
                                                                 LocusType.SNP, "chromosomeName", 1L, 10L,"T", "T",
                                                                 "A", "T/A", "", null, null, Orientation.FORWARD, "",
                                                                 null, null, Orientation.FORWARD, null, "batch");

        assertEquals(12345, subSnpCoreFields.getSsId());
        assertEquals(Orientation.FORWARD, subSnpCoreFields.getSnpOrientation());
        assertEquals(new Region("contigName"), subSnpCoreFields.getContigRegion());
        assertEquals(Orientation.REVERSE, subSnpCoreFields.getContigOrientation());
        assertEquals(new Region("chromosomeName", 1L, 10L), subSnpCoreFields.getChromosomeRegion());
    }

    @Test
    public void testRsIdDefinition() {
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(1, Orientation.FORWARD, 123L, Orientation.FORWARD,
                                                                  "contigName", 1L, 10L, Orientation.REVERSE,
                                                                  LocusType.SNP, "chromosomeName", 5L, 50L, "T", "T",
                                                                  "A", "T/A", "", null, null, Orientation.FORWARD, "",
                                                                  null, null, Orientation.FORWARD, null, "batch");
        SubSnpCoreFields subSnpCoreFields2 = new SubSnpCoreFields(2, Orientation.FORWARD, null, Orientation.FORWARD,
                                                                  "contigName", 1L, 10L, Orientation.REVERSE,
                                                                  LocusType.SNP, "chromosomeName", 5L, 50L, "T", "T",
                                                                  "A", "T/A", "", null, null, Orientation.FORWARD, "",
                                                                  null, null, Orientation.FORWARD, null, "batch");

        assertEquals(1, subSnpCoreFields1.getSsId());
        assertNotNull(subSnpCoreFields1.getRsId());
        assertEquals(123, (long) subSnpCoreFields1.getRsId());

        assertEquals(2, subSnpCoreFields2.getSsId());
        assertNull(subSnpCoreFields2.getRsId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void failWithNegativeContigCoordinates() {
        new SubSnpCoreFields(12345, Orientation.FORWARD, 123L, Orientation.FORWARD, "contigName", -1L, 10L,
                             Orientation.REVERSE, LocusType.SNP, "chromosomeName", null, null, "T", "T", "A", "T/A", "",
                             null, null, Orientation.FORWARD, "", null, null, Orientation.FORWARD, null, "batch");

    }


    @Test(expected = IllegalArgumentException.class)
    public void failWithNegativeChromosomeCoordinates() {
        new SubSnpCoreFields(12345, Orientation.FORWARD, 123L, Orientation.FORWARD, "contigName", 1L, 10L,
                             Orientation.REVERSE, LocusType.SNP, "chromosomeName", -5L, 50L, "T", "T", "A", "T/A", "",
                             null, null, Orientation.FORWARD, "", null, null, Orientation.FORWARD, null, "batch");
    }

    @Test
    public void testGenotypesBiAllelicReverse() {
        //Test allele reverse orientation
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(3173433, Orientation.FORWARD, 2228714L, Orientation.REVERSE,
                "NC_003074.8", 23412070L, 23412070L, Orientation.FORWARD,
                LocusType.SNP, "3", 23412070L, 23412070L, "C", "C",
                "T", "G/A", "NC_003074.8:g.23412070C>T", 23412070L, 23412070L, Orientation.FORWARD, "NC_003074.8:g.23412070C>T",
                23412070L, 23412070L, Orientation.FORWARD, "G/G,A/A, G/ A, A |G, ./.", "batch");
        List<Map<String, String>> expectedGenotypes = new ArrayList<>();
        expectedGenotypes.add(createGenotypeMap("GT", "0/0"));
        expectedGenotypes.add(createGenotypeMap("GT", "1/1"));
        expectedGenotypes.add(createGenotypeMap("GT", "0/1"));
        expectedGenotypes.add(createGenotypeMap("GT", "1|0"));
        expectedGenotypes.add(createGenotypeMap("GT", "./."));
        assertEquals(expectedGenotypes, subSnpCoreFields.getGenotypes());
    }

    @Test
    public void testGenotypesBiAllelicForward() {
        //Test allele forward orientation
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(492296696, Orientation.REVERSE, 2228714L, Orientation.REVERSE,
                "NC_003074.8", 23412070L, 23412070L, Orientation.FORWARD,
                LocusType.SNP, "3", 23412070L, 23412070L, "G", "G",
                "A", "T/C", "NC_003074.8:g.23412070C>T", 23412070L, 23412070L, Orientation.REVERSE, "NC_003074.8:g.23412070C>T",
                23412070L, 23412070L, Orientation.REVERSE, "C/T, T/T, C/C, ./., T/C, C, T", "batch");
        List<Map<String, String>> expectedGenotypes = new ArrayList<>();
        expectedGenotypes.add(createGenotypeMap("GT", "0/1"));
        expectedGenotypes.add(createGenotypeMap("GT", "1/1"));
        expectedGenotypes.add(createGenotypeMap("GT", "0/0"));
        expectedGenotypes.add(createGenotypeMap("GT", "./."));
        expectedGenotypes.add(createGenotypeMap("GT", "1/0"));
        expectedGenotypes.add(createGenotypeMap("GT", "0"));
        expectedGenotypes.add(createGenotypeMap("GT", "1"));
        assertEquals(expectedGenotypes, subSnpCoreFields.getGenotypes());
    }

    @Test
    public void testGenotypesHyphenated() {
        //Test hyphenated genotypes
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(492296696, Orientation.REVERSE, 2228714L, Orientation.REVERSE,
                "NC_003074.8", 23412070L, 23412070L, Orientation.FORWARD,
                LocusType.SNP, "3", 23412070L, 23412070L, "G", "G",
                "", "C/-", "NC_003074.8:g.23412070C>T", 23412070L, 23412070L, Orientation.REVERSE, "NC_003074.8:g.23412070C>T",
                23412070L, 23412070L, Orientation.REVERSE, "-/ - ", "batch");
        List<Map<String, String>> expectedGenotypes = new ArrayList<>();
        expectedGenotypes.add(createGenotypeMap("GT", "1/1"));
        assertEquals(expectedGenotypes, subSnpCoreFields.getGenotypes());
    }

    @Test
    public void testGenotypesMultiAllelicReverse() {
        //Test multi-allelic genotypes - reverse orientation
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(492296696, Orientation.REVERSE, 2228714L, Orientation.REVERSE,
                null, 23412070L, 23412073L, Orientation.REVERSE,
                LocusType.SNP, "3", 23412070L, 23412073L, "TAC", "TAC",
                "GGC", "GTA/GCC/GC", "", 23412070L, 23412073L, Orientation.FORWARD, "",
                23412070L, 23412073L, Orientation.FORWARD, "GTA |GCC, GCC|GCC, GTA|GTA, GC/GC", "batch");
        List<Map<String, String>> expectedGenotypes = new ArrayList<>();
        expectedGenotypes.add(createGenotypeMap("GT", "0|1"));
        expectedGenotypes.add(createGenotypeMap("GT", "1|1"));
        expectedGenotypes.add(createGenotypeMap("GT", "0|0"));
        expectedGenotypes.add(createGenotypeMap("GT", "2/2"));
        assertEquals(expectedGenotypes, subSnpCoreFields.getGenotypes());
    }

    @Test
    public void testGenotypesMultiAllelicForward() {
        //Test multi-allelic genotypes - forward orientation
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(492296696, Orientation.REVERSE, 2228714L, Orientation.REVERSE,
                null, 23412070L, 23412073L, Orientation.FORWARD,
                LocusType.SNP, "3", 23412070L, 23412073L,"TAC", "TAC",
                "GGC", "TAC/GGC/CCT", "", 23412070L, 23412073L, Orientation.FORWARD, "",
                23412070L, 23412073L, Orientation.FORWARD, "TAC |GGC, GGC|GGC, TAC/TAC, CCT/GGC", "batch");
        List<Map<String, String>> expectedGenotypes = new ArrayList<>();
        expectedGenotypes.add(createGenotypeMap("GT", "0|1"));
        expectedGenotypes.add(createGenotypeMap("GT", "1|1"));
        expectedGenotypes.add(createGenotypeMap("GT", "0/0"));
        expectedGenotypes.add(createGenotypeMap("GT", "2/1"));
        assertEquals(expectedGenotypes, subSnpCoreFields.getGenotypes());
    }

    @Test
    public void testGenotypesNullEmpty() {
        //Test genotypes with null and empty values
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(492296696, Orientation.REVERSE, 2228714L, Orientation.REVERSE,
                null, 23412070L, 23412073L, Orientation.FORWARD,
                LocusType.SNP, "3", 23412070L, 23412073L,"TAC", "TAC",
                "GGC", "TAC/GGC/CCT", "", 23412070L, 23412073L, Orientation.FORWARD, "",
                23412070L, 23412073L, Orientation.FORWARD, null, "batch");
        List<Map<String, String>> expectedGenotypes = new ArrayList<>();
        assertEquals(expectedGenotypes, subSnpCoreFields.getGenotypes());
        subSnpCoreFields.setGenotypes("");
        assertEquals(expectedGenotypes, subSnpCoreFields.getGenotypes());
    }

    public static Map<String, String> createGenotypeMap (String key, String value) {
        Map<String, String> genotypeMap = new HashMap<>();
        genotypeMap.put(key, value);
        return  genotypeMap;
    }

}
