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
package uk.ac.ebi.eva.dbsnpimporter.models;

import org.junit.Test;

import uk.ac.ebi.eva.commons.core.models.Region;

import static org.junit.Assert.*;

public class SubSnpCoreFieldsGetVariantCoordinatesTest {

    @Test
    public void snpCoordinatesMustNotChange() throws Exception {
        SubSnpCoreFields snp = new SubSnpCoreFields(1092414368L, Orientation.FORWARD, 526595372L, Orientation.FORWARD,
                                                    "NW_003104285.1", 12108029L, 12108029L, Orientation.FORWARD,
                                                    LocusType.SNP, "10", 100002924L, 100002924L, null, null, null, null,
                                                    null, null, null, Orientation.REVERSE, null, null, null,
                                                    Orientation.REVERSE);

        assertEquals(new Region("10", 100002924L, 100002924L), snp.getVariantCoordinates());
    }

    @Test
    public void contigCoordinatesReturnedForSnpNotMappedToChromosome() throws Exception {
        SubSnpCoreFields snp = new SubSnpCoreFields(1107437104L, Orientation.FORWARD, 524908995L, Orientation.FORWARD,
                                                    "NW_003101163.1", 943L, 943L, Orientation.FORWARD, LocusType.SNP,
                                                    null, null, null, null, null, null, null, null, null, null,
                                                    Orientation.REVERSE, null, null, null, Orientation.REVERSE);

        assertEquals(new Region("NW_003101163.1", 943L, 943L), snp.getVariantCoordinates());
    }

    @Test
    public void singleNucleotideDeletionCoordinatesShouldNotChange() throws Exception {
        SubSnpCoreFields deletion = new SubSnpCoreFields(1093365488L, Orientation.FORWARD, 433288923L,
                                                         Orientation.FORWARD, "4332889n23", 1591551L, 1591551L,
                                                         Orientation.FORWARD, LocusType.DELETION, "12", 10144047L,
                                                         10144047L, null, null, null, null, null, null, null,
                                                         Orientation.REVERSE, null, null, null, Orientation.REVERSE);

        assertEquals(new Region("12", 10144047L, 10144047L), deletion.getVariantCoordinates());
    }

    @Test
    public void multiNucleotideDeletionCoordinatesShouldNotChange() throws Exception {
        SubSnpCoreFields deletion = new SubSnpCoreFields(1085240363L, Orientation.FORWARD, 384020033L,
                                                         Orientation.FORWARD, "NW_003103847.1", 1056819L, 1056821L,
                                                         Orientation.FORWARD, LocusType.DELETION, "2", 100306584L,
                                                         100306586L, null, null, null, null, null, null, null,
                                                         Orientation.REVERSE, null, null, null, Orientation.REVERSE);

        assertEquals(new Region("2", 100306584L, 100306586L), deletion.getVariantCoordinates());
    }

    @Test
    public void contigCoordinatesReturnedForDeletionNotMappedToChromosome() throws Exception {
        SubSnpCoreFields deletion = new SubSnpCoreFields(1107437081L, Orientation.FORWARD, 524371323L,
                                                         Orientation.FORWARD, "NW_003101162.1", 229L, 232L,
                                                         Orientation.FORWARD, LocusType.DELETION, null, null, null,
                                                         null, null, null, null, null, null, null, Orientation.REVERSE,
                                                         null, null, null, Orientation.REVERSE);

        assertEquals(new Region("NW_003101162.1", 229L, 232L), deletion.getVariantCoordinates());
    }

    @Test
    public void singleNucleotideInsertionStartShouldBeAdjusted() throws Exception {
        SubSnpCoreFields insertion = new SubSnpCoreFields(1092414490L, Orientation.FORWARD, 522748169L,
                                                          Orientation.FORWARD, "NW_003104285.1", 12118757L, 12118758L,
                                                          Orientation.FORWARD, LocusType.INSERTION, "10", 100013652L,
                                                          100013653L, "-", "-", "A", "-/A",
                                                          "AC_000167.1:g.100013652_100013653insA", 100013652L,
                                                          100013653L, Orientation.FORWARD,
                                                          "NW_003104285.1:g.12118757_12118758insA", 12118757L,
                                                          12118758L, Orientation.FORWARD);

        assertEquals(new Region("10", 100013653L, 100013653L), insertion.getVariantCoordinates());

    }

    @Test
    public void multiNucleotideInsertionStartAndEndShouldBeAdjusted() throws Exception {
        SubSnpCoreFields insertion = new SubSnpCoreFields(1513871941L, Orientation.FORWARD, 379115400L,
                                                          Orientation.FORWARD, "NW_003103939.1", 12276L, 12277L,
                                                          Orientation.FORWARD, LocusType.INSERTION, "5", 100080173L,
                                                          100080174L, "-", "-", "TTGCA", "-/TTGCA",
                                                          "AC_000162.1:g.100080173_100080174insTTGCA", 100080173L,
                                                          100080174L, Orientation.FORWARD,
                                                          "NW_003103939.1:g.12276_12277insTTGCA", 12276L, 12277L,
                                                          Orientation.FORWARD);

        assertEquals(new Region("5", 100080174L, 100080178L), insertion.getVariantCoordinates());
    }

    @Test
    public void contigCoordinatesReturnedForInsertionNotMappedToChromosome() throws Exception {
        SubSnpCoreFields insertion = new SubSnpCoreFields(1107437080L, Orientation.FORWARD, 520781897L,
                                                          Orientation.FORWARD, "NW_003101162.1", 189L, 190L,
                                                          Orientation.FORWARD, LocusType.INSERTION, null, null, null,
                                                          null, "-", "AA", "-/AA", null, null, null,
                                                          Orientation.FORWARD, "NW_003101162.1:g.189_190insAA", 189L,
                                                          190L, Orientation.FORWARD);

        assertEquals(new Region("NW_003101162.1", 190L, 191L), insertion.getVariantCoordinates());

    }

}