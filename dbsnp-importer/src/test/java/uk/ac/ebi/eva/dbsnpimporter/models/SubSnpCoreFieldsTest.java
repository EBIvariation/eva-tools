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
                                                                 null, null, Orientation.FORWARD, "batch");

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
                                                                 Orientation.FORWARD, "batch");

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
                                                                 null, null, Orientation.FORWARD, "batch");

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
                                                                 null, null, Orientation.FORWARD, "batch");

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
                                                                  null, null, Orientation.FORWARD, "batch");
        SubSnpCoreFields subSnpCoreFields2 = new SubSnpCoreFields(2, Orientation.FORWARD, null, Orientation.FORWARD,
                                                                  "contigName", 1L, 10L, Orientation.REVERSE,
                                                                  LocusType.SNP, "chromosomeName", 5L, 50L, "T", "T",
                                                                  "A", "T/A", "", null, null, Orientation.FORWARD, "",
                                                                  null, null, Orientation.FORWARD, "batch");

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
                             null, null, Orientation.FORWARD, "", null, null, Orientation.FORWARD, "batch");

    }


    @Test(expected = IllegalArgumentException.class)
    public void failWithNegativeChromosomeCoordinates() {
        new SubSnpCoreFields(12345, Orientation.FORWARD, 123L, Orientation.FORWARD, "contigName", 1L, 10L,
                             Orientation.REVERSE, LocusType.SNP, "chromosomeName", -5L, 50L, "T", "T", "A", "T/A", "",
                             null, null, Orientation.FORWARD, "", null, null, Orientation.FORWARD, "batch");
    }

}
