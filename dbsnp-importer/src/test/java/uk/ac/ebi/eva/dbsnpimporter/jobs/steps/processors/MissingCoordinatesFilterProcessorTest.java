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
import org.junit.Test;

import uk.ac.ebi.eva.dbsnpimporter.models.LocusType;
import uk.ac.ebi.eva.dbsnpimporter.models.Orientation;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MissingCoordinatesFilterProcessorTest {

    private MissingCoordinatesFilterProcessor filter;

    @Before
    public void setUp() {
        filter = new MissingCoordinatesFilterProcessor();
    }

    @Test
    public void keepAllCoordinatesPresent() {
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                 "NT_455866.1", 1L, 1L, Orientation.FORWARD,
                                                                 LocusType.SNP, "4", 1L, 1L, "T", "T", "A", "T/A", "",
                                                                 null, null, Orientation.FORWARD, null, null, null,
                                                                 Orientation.FORWARD, "batch");
        assertNotNull(filter.process(subSnpCoreFields));
    }

    @Test
    public void keepChromosomeCoordinatesPresent() {
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                  "NT_455866.1", null, null, Orientation.FORWARD,
                                                                  LocusType.SNP, "4", 1L, 1L, "T", "T", "A", "T/A", "",
                                                                  null, null, Orientation.FORWARD, null, null, null,
                                                                  Orientation.FORWARD, "batch");
        assertNotNull(filter.process(subSnpCoreFields));
    }

    @Test
    public void keepContigCoordinatesPresent() {
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                 "NT_455866.1", 1L, 1L, Orientation.FORWARD,
                                                                 LocusType.SNP, "4", null, null, "T", "T", "A", "T/A", "",
                                                                 null, null, Orientation.FORWARD, null, null, null,
                                                                 Orientation.FORWARD, "batch");
        assertNotNull(filter.process(subSnpCoreFields));
    }

    @Test
    public void removeStartCoordinatesMissing() {
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                 "NT_455866.1", null, 1L, Orientation.FORWARD,
                                                                 LocusType.SNP, "4", null, 1L, "T", "T", "A", "T/A", "",
                                                                 null, null, Orientation.FORWARD, null, null, null,
                                                                 Orientation.FORWARD, "batch");
        assertNull(filter.process(subSnpCoreFields));
    }

    @Test
    public void removeEndCoordinatesMissing() {
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                 "NT_455866.1", 1L, null, Orientation.FORWARD,
                                                                 LocusType.SNP, "4", 1L, null, "T", "T", "A", "T/A", "",
                                                                 null, null, Orientation.FORWARD, null, null, null,
                                                                 Orientation.FORWARD, "batch");
        assertNull(filter.process(subSnpCoreFields));
    }

    @Test
    public void removeAllCoordinatesMissing() {
        SubSnpCoreFields subSnpCoreFields = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                 "NT_455866.1", null, null, Orientation.FORWARD,
                                                                 LocusType.SNP, "4", null, null, "T", "T", "A", "T/A", "",
                                                                 null, null, Orientation.FORWARD, null, null, null,
                                                                 Orientation.FORWARD, "batch");
        assertNull(filter.process(subSnpCoreFields));
    }

}
