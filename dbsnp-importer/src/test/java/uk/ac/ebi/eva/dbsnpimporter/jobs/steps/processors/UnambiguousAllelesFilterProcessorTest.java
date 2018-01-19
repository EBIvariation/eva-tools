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

import org.junit.Test;

import uk.ac.ebi.eva.dbsnpimporter.models.LocusType;
import uk.ac.ebi.eva.dbsnpimporter.models.Orientation;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class UnambiguousAllelesFilterProcessorTest {

    private UnambiguousAllelesFilterProcessor filter = new UnambiguousAllelesFilterProcessor();

    @Test
    public void keepUnambiguousAlleles() {
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                  "NT_455866.1", 1L, 1L, Orientation.FORWARD,
                                                                  LocusType.SNP, "4", 1L, 1L, "T", "T", "A", "T/A", "",
                                                                  null, null, Orientation.FORWARD, null, null, null,
                                                                  Orientation.FORWARD, null, null, "batch");

        SubSnpCoreFields subSnpCoreFields2 = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                  "NT_455866.1", 1L, 1L, Orientation.FORWARD,
                                                                  LocusType.SNP, "4", 1L, 1L, "TAGC", "TAGC", "A",
                                                                  "TAGC/A", "", null, null, Orientation.FORWARD, null,
                                                                  null, null, Orientation.FORWARD, null, null, "batch");

        SubSnpCoreFields subSnpCoreFields3 = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                  "NT_455866.1", 1L, 1L, Orientation.FORWARD,
                                                                  LocusType.SNP, "4", 1L, 1L, "aCgTAcGt", "aCgTAcGt",
                                                                  "AAACCTg", "aCgTAcGt/AAACCTg", "", null, null,
                                                                  Orientation.FORWARD, null, null, null,
                                                                  Orientation.FORWARD, null, null, "batch");

        SubSnpCoreFields subSnpCoreFields4 = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                  "NT_455866.1", 1L, 1L, Orientation.FORWARD,
                                                                  LocusType.SNP, "4", 1L, 1L, "", "", "ACgt", "/ACgt",
                                                                  "", null, null, Orientation.FORWARD, null, null, null,
                                                                  Orientation.FORWARD, null, null, "batch");

        assertNotNull(filter.process(subSnpCoreFields1));
        assertNotNull(filter.process(subSnpCoreFields2));
        assertNotNull(filter.process(subSnpCoreFields3));
        assertNotNull(filter.process(subSnpCoreFields4));
    }

    @Test
    public void removeAmbiguousReferenceAllele() {
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                  "NT_455866.1", 1L, 1L, Orientation.FORWARD,
                                                                  LocusType.SNP, "4", 1L, 1L, "N", "N", "A", "N/A", "",
                                                                  null, null, Orientation.FORWARD, null, null, null,
                                                                  Orientation.FORWARD, null, null, "batch");

        SubSnpCoreFields subSnpCoreFields2 = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                  "NT_455866.1", 1L, 1L, Orientation.FORWARD,
                                                                  LocusType.SNP, "4", 1L, 1L, "AGYT", "AGYT", "A",
                                                                  "AGYT/A", "", null, null, Orientation.FORWARD, null,
                                                                  null, null, Orientation.FORWARD, null, null, "batch");

        SubSnpCoreFields subSnpCoreFields3 = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                  "NT_455866.1", 1L, 1L, Orientation.FORWARD,
                                                                  LocusType.SNP, "4", 1L, 1L, "rxyz", "rxyz",
                                                                  "AAACCTg", "rxyz/AAACCTg", "", null, null,
                                                                  Orientation.FORWARD, null, null, null,
                                                                  Orientation.FORWARD, null, null, "batch");

        SubSnpCoreFields subSnpCoreFields4 = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                  "NT_455866.1", 1L, 1L, Orientation.FORWARD,
                                                                  LocusType.SNP, "4", 1L, 1L, "y", "y", "ACg", "y/ACg",
                                                                  "", null, null, Orientation.FORWARD, null, null, null,
                                                                  Orientation.FORWARD, null, null, "batch");

        assertNull(filter.process(subSnpCoreFields1));
        assertNull(filter.process(subSnpCoreFields2));
        assertNull(filter.process(subSnpCoreFields3));
        assertNull(filter.process(subSnpCoreFields4));
    }

    @Test
    public void removeAmbiguousAlternateAllele() {
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                  "NT_455866.1", 1L, 1L, Orientation.FORWARD,
                                                                  LocusType.SNP, "4", 1L, 1L, "A", "A", "N", "A/N", "",
                                                                  null, null, Orientation.FORWARD, null, null, null,
                                                                  Orientation.FORWARD, null, null, "batch");

        SubSnpCoreFields subSnpCoreFields2 = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                  "NT_455866.1", 1L, 1L, Orientation.FORWARD,
                                                                  LocusType.SNP, "4", 1L, 1L, "A", "A", "AGYT",
                                                                  "A/AGYT", "", null, null, Orientation.FORWARD, null,
                                                                  null, null, Orientation.FORWARD, null, null, "batch");

        SubSnpCoreFields subSnpCoreFields3 = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                  "NT_455866.1", 1L, 1L, Orientation.FORWARD,
                                                                  LocusType.SNP, "4", 1L, 1L, "AAACCTg", "AAACCTg",
                                                                  "rxyz", "AAACCTg/rxyz", "", null, null,
                                                                  Orientation.FORWARD, null, null, null,
                                                                  Orientation.FORWARD, null, null, "batch");

        SubSnpCoreFields subSnpCoreFields4 = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                  "NT_455866.1", 1L, 1L, Orientation.FORWARD,
                                                                  LocusType.SNP, "4", 1L, 1L, "Ag", "Ag", "y", "y/Ag",
                                                                  "", null, null, Orientation.FORWARD, null, null, null,
                                                                  Orientation.FORWARD, null, null, "batch");

        assertNull(filter.process(subSnpCoreFields1));
        assertNull(filter.process(subSnpCoreFields2));
        assertNull(filter.process(subSnpCoreFields3));
        assertNull(filter.process(subSnpCoreFields4));
    }

    @Test
    public void removeUndefinedHgvs() {
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(1L, Orientation.FORWARD, 1L, Orientation.FORWARD,
                                                                  "NT_455866.1", 1L, 1L, Orientation.FORWARD,
                                                                  LocusType.SNP, "4", 1L, 1L, "T", "T", "A", "T/A",
                                                                  null, null, null, Orientation.FORWARD, null, null, null,
                                                                  Orientation.FORWARD, null, null, "batch");
        assertNull(filter.process(subSnpCoreFields1));
    }
}