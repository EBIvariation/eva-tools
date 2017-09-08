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

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SubSnpCoreFieldsTest {

    @Test
    public void testContigAndChromosomeCoordinates() {
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(12345, 123, 1, "contigName", 1, 10, -1,
                                                                 "chromosomeName", 5L, new BigDecimal(50L));
        assertEquals(12345, subSnpCoreFields.getSsId());
        assertNotNull(subSnpCoreFields.getRsId());
        assertEquals(123, (long) subSnpCoreFields.getRsId());
        assertEquals(Orientation.FORWARD, subSnpCoreFields.getSnpOrientation());
        assertEquals(new Region("contigName", 1, 10), subSnpCoreFields.getContigRegion());
        assertEquals(Orientation.REVERSE, subSnpCoreFields.getContigOrientation());
        assertEquals(new Region("chromosomeName", 5, 50), subSnpCoreFields.getChromosomeRegion());
    }

    @Test
    public void testContigCoordinatesOnly() {
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(12345, 123, 1, "contigName", 1, 10, -1, null, null, null);
        assertEquals(12345, subSnpCoreFields.getSsId());
        assertEquals(Orientation.FORWARD, subSnpCoreFields.getSnpOrientation());
        assertEquals(new Region("contigName", 1, 10), subSnpCoreFields.getContigRegion());
        assertEquals(Orientation.REVERSE, subSnpCoreFields.getContigOrientation());
        assertNull(subSnpCoreFields.getChromosomeRegion());
    }

    @Test
    public void testWithoutChromosomeCoordinates() {
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(12345, 123,1, "contigName", 1, 10, -1, "chromosomeName", null, null);
        assertEquals(12345, subSnpCoreFields.getSsId());
        assertEquals(Orientation.FORWARD, subSnpCoreFields.getSnpOrientation());
        assertEquals(new Region("contigName", 1, 10), subSnpCoreFields.getContigRegion());
        assertEquals(Orientation.REVERSE, subSnpCoreFields.getContigOrientation());
        assertEquals(new Region("chromosomeName"), subSnpCoreFields.getChromosomeRegion());
    }

    @Test
    public void testRsIdDefinition() {
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(1, 123, 1, "contigName", 1, 10, -1, "chromosomeName",
                                                                  5L, new BigDecimal(50L));
        SubSnpCoreFields subSnpCoreFields2 = new SubSnpCoreFields(2, null, 1, "contigName", 1, 10, -1, "chromosomeName",
                                                                  5l, new BigDecimal(50L));

        assertEquals(1, subSnpCoreFields1.getSsId());
        assertNotNull(subSnpCoreFields1.getRsId());
        assertEquals(123, (long) subSnpCoreFields1.getRsId());

        assertEquals(2, subSnpCoreFields2.getSsId());
        assertNull(subSnpCoreFields2.getRsId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void failWithNegativeContigCoordinates() {
        new SubSnpCoreFields(12345, 123, 1, "contigName", -1, 10, -1, "chromosomeName", null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failWithNegativeChromosomeCoordinates() {
        new SubSnpCoreFields(12345, 123, 1, "contigName", 1, 10, -1, "chromosomeName", -5L, new BigDecimal(50L));
    }

}