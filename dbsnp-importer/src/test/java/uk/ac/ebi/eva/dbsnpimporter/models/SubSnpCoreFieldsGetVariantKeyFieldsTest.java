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

import uk.ac.ebi.eva.commons.core.models.VariantCoreFields;

import static org.junit.Assert.*;

public class SubSnpCoreFieldsGetVariantKeyFieldsTest {

    @Test
    public void snpCoordinatesMustNotChange() throws Exception {
        SubSnpCoreFields snp = new SubSnpCoreFields(1092414368L, 1, 526595372L, 1, "NW_003104285.1", 12108029L, 12108029L,
                                                    1, LocusType.SNP, "10", 100002924L, 100002924L, "C", "C",
                                                    "T", null, "AC_000167.1:g.100002924C>T", 100002924L, 100002924L, 1,
                                                    "NW_003104285.1:g.12108029C>T", 12108029L, 12108029L, 1);

        assertEquals(new VariantCoreFields("10", 100002924L, "C", "T"), snp.getVariantKeyFields());
    }

    @Test
    public void contigCoordinatesReturnedForSnpNotMappedToChromosome() throws Exception {
        SubSnpCoreFields snp = new SubSnpCoreFields(1107437104L, 1, 524908995L, 1, "NW_003101163.1", 943L, 943L, 1,
                                                    LocusType.SNP, null, null, null, null, "C", "A", null,
                                                    null, null, null, 1, "NW_003101163.1:g.943C>A", 943L, 943L, 1);

        assertEquals(new VariantCoreFields("NW_003101163.1", 943L, "C", "A"), snp.getVariantKeyFields());
    }

    @Test
    public void singleNucleotideDeletionCoordinatesShouldNotChange() throws Exception {
        SubSnpCoreFields deletion = new SubSnpCoreFields(1093365488L, 1, 433288923L, 1, "4332889n23", 1591551L, 1591551L,
                                                         1, LocusType.SNP, "12", 10144047L, 10144047L, "G",
                                                         "G", null, null, "AC_000169.1:g.10144047delG", 10144047L,
                                                         10144047L, 1, "AC_000169.1:g.10144047delG", 1591551L, 1591551L,
                                                         1);

        assertEquals(new VariantCoreFields("12", 10144047L, "G", ""), deletion.getVariantKeyFields());
    }

    @Test
    public void multiNucleotideDeletionCoordinatesShouldNotChange() throws Exception {
        SubSnpCoreFields deletion = new SubSnpCoreFields(1085240363L, 1, 384020033L, 1, "NW_003103847.1", 1056819L,
                                                         1056821L, 1, LocusType.DELETION, "2", 100306584L,
                                                         100306586L, "TCA", "TCA", null, null,
                                                         "AC_000159.1:g.100306584_100306586delTCA", 100306584L,
                                                         100306586L, 1, "NW_003103847.1:g.1056819_1056821delTCA",
                                                         1056819L, 1056821L, -1);

        assertEquals(new VariantCoreFields("2", 100306584L, "TCA", ""), deletion.getVariantKeyFields());
    }

    @Test
    public void contigCoordinatesReturnedForDeletionNotMappedToChromosome() throws Exception {
        SubSnpCoreFields deletion = new SubSnpCoreFields(1107437081L, 1, 524371323L, 1, "NW_003101162.1", 229L, 232L, 1,
                                                         LocusType.DELETION, null, null, null, null, "TTTC",
                                                         null, null, null, null, null, 1,
                                                         "NW_003101162.1:g.229_232delTTTC", 229L, 232L, 1);

        assertEquals(new VariantCoreFields("NW_003101162.1", 229L, "TTTC", ""), deletion.getVariantKeyFields());
    }

    @Test
    public void singleNucleotideInsertionStartShouldBeAdjusted() throws Exception {
        SubSnpCoreFields insertion = new SubSnpCoreFields(1092414490L, 1, 522748169L, 1, "NW_003104285.1", 12118757L,
                                                          12118758L, 1, LocusType.INSERTION, "10",
                                                          100013652L, 100013653L, "-", "-", "A", "-/A",
                                                          "AC_000167.1:g.100013652_100013653insA", 100013652L,
                                                          100013653L, 1, "NW_003104285.1:g.12118757_12118758insA",
                                                          12118757L, 12118758L, 1);

        assertEquals(new VariantCoreFields("10", 100013653L, "-", "A"), insertion.getVariantKeyFields());

    }

    @Test
    public void multioNucleotideInsertionStartAndEndShouldBeAdjusted() throws Exception {
        SubSnpCoreFields insertion = new SubSnpCoreFields(1513871941L, 1, 379115400L, 1, "NW_003103939.1", 12276L, 12277L,
                                                          1, LocusType.INSERTION, "5", 100080173L,
                                                          100080174L, "-", "-", "TTGCA", "-/TTGCA",
                                                          "AC_000162.1:g.100080173_100080174insTTGCA", 100080173L,
                                                          100080174L, 1, "NW_003103939.1:g.12276_12277insTTGCA", 12276L,
                                                          12277L, 1);

        assertEquals(new VariantCoreFields("5", 100080174L, "-", "TTGCA"), insertion.getVariantKeyFields());
    }

    @Test
    public void contigCoordinatesReturnedForInsertionNotMappedToChromosome() throws Exception {
        SubSnpCoreFields insertion = new SubSnpCoreFields(1107437080L, 1, 520781897L, 1, "NW_003101162.1", 189L, 190L, 1,
                                                          LocusType.INSERTION, null, null, null, null, "-",
                                                          "AA", "-/AA", null, null, null, 1,
                                                          "NW_003101162.1:g.189_190insAA", 189L, 190L, 1);

        assertEquals(new VariantCoreFields("NW_003101162.1", 190L, "-", "AA"), insertion.getVariantKeyFields());

    }
}