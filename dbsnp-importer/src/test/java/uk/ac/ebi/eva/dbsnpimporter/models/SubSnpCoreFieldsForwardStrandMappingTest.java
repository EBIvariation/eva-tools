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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests the mapping of alleles in a SubSnpCoreFields object to the forward strand. Please note that HGVS strings have
 * been manually generated and may not be fully complaint, but this doesn't affect the correctness of the tests.
 */
public class SubSnpCoreFieldsForwardStrandMappingTest {

    @Test
    public void snpAllelesInForwardStrandMustNotChange() throws Exception {
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(26201546,
                                                                  Orientation.FORWARD, 13677177L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  Orientation.FORWARD,
                                                                  LocusType.SNP,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "T", "T", "A", "T/A",
                                                                  "NC_006091.4:g.91223961T>A", 91223961L, 91223961L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1:g.1766472T>A", 1766472L, 1766472L,
                                                                  Orientation.FORWARD);

        assertEquals("T", subSnpCoreFields1.getReferenceInForwardStrand());
        assertEquals("A", subSnpCoreFields1.getAlternateInForwardStrand());
        assertEquals("T/A", subSnpCoreFields1.getAllelesInForwardStrand());
    }

    @Test
    public void insertionAllelesInForwardStrandMustNotChange() throws Exception {
        // Insertion with non-empty alleles
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(26201546,
                                                                  Orientation.FORWARD, 13677177L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  Orientation.FORWARD,
                                                                  LocusType.INSERTION,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "T", "T", "TAGA", "T/TAGA",
                                                                  "NC_006091.4:g.91223962insAGA", 91223961L, 91223961L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1:g.1766473insAGA", 1766472L, 1766472L,
                                                                  Orientation.FORWARD);

        assertEquals("T", subSnpCoreFields1.getReferenceInForwardStrand());
        assertEquals("TAGA", subSnpCoreFields1.getAlternateInForwardStrand());

        // Insertion with dash in reference
        SubSnpCoreFields subSnpCoreFields2 = new SubSnpCoreFields(26201546,
                                                                  Orientation.FORWARD, 13677177L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  Orientation.FORWARD,
                                                                  LocusType.INSERTION,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "-", "-", "TA", "-/TA",
                                                                  "NC_006091.4:g.91223962insA", 91223961L, 91223961L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1:g.1766473insA", 1766472L, 1766472L,
                                                                  Orientation.FORWARD);

        assertEquals("-", subSnpCoreFields2.getReferenceInForwardStrand());
        assertEquals("TA", subSnpCoreFields2.getAlternateInForwardStrand());

        // Insertion with null reference
        SubSnpCoreFields subSnpCoreFields3 = new SubSnpCoreFields(26201546,
                                                                  Orientation.FORWARD, 13677177L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  Orientation.FORWARD,
                                                                  LocusType.INSERTION,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  null, null, "TA", "-/TA",
                                                                  "NC_006091.4:g.91223962insA", 91223961L, 91223961L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1:g.1766473insA", 1766472L, 1766472L,
                                                                  Orientation.FORWARD);

        assertEquals("", subSnpCoreFields3.getReferenceInForwardStrand());
        assertEquals("TA", subSnpCoreFields3.getAlternateInForwardStrand());
    }

    @Test
    public void deletionAllelesInForwardStrandMustNotChange() throws Exception {
        // Deletion with non-empty alleles
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(26201546,
                                                                  Orientation.FORWARD, 13677177L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  Orientation.FORWARD,
                                                                  LocusType.DELETION,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "TAGA", "TAGA", "T", "TAGA/T",
                                                                  "NC_006091.4:g.91223962delAGA", 91223961L, 91223961L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1:g.17664723delAGA", 1766472L, 1766472L,
                                                                  Orientation.FORWARD);

        assertEquals("TAGA", subSnpCoreFields1.getReferenceInForwardStrand());
        assertEquals("T", subSnpCoreFields1.getAlternateInForwardStrand());

        // Deletion with dash in alternate
        SubSnpCoreFields subSnpCoreFields2 = new SubSnpCoreFields(26201546,
                                                                  Orientation.FORWARD, 13677177L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  Orientation.FORWARD,
                                                                  LocusType.DELETION,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "TA", "TA", "-", "TA/-",
                                                                  "NC_006091.4:g.91223961delTA", 91223961L, 91223961L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1:g.1766472delTA", 1766472L, 1766472L,
                                                                  Orientation.FORWARD);

        assertEquals("TA", subSnpCoreFields2.getReferenceInForwardStrand());
        assertEquals("-", subSnpCoreFields2.getAlternateInForwardStrand());

        // Deletion with null alternate
        SubSnpCoreFields subSnpCoreFields3 = new SubSnpCoreFields(26201546,
                                                                  Orientation.FORWARD, 13677177L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  Orientation.FORWARD,
                                                                  LocusType.DELETION,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "TA", "TA", null, "TA/-",
                                                                  "NC_006091.4:g.91223961delTA", 91223961L, 91223961L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1:g.1766472delTA", 1766472L, 1766472L,
                                                                  Orientation.FORWARD);

        assertEquals("TA", subSnpCoreFields3.getReferenceInForwardStrand());
        assertEquals("", subSnpCoreFields3.getAlternateInForwardStrand());
    }

    @Test
    public void allelesInForwardStrandAndNullHgvsCMustNotChange() throws Exception {
        // SNP
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(26201546,
                                                                  Orientation.FORWARD, 13677177L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  Orientation.FORWARD,
                                                                  LocusType.SNP,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  null, "T", "A", "T/A",
                                                                  null, null, null, Orientation.FORWARD,
                                                                  "NT_455866.1:g.1766472T>A", 1766472L, 1766472L,
                                                                  Orientation.FORWARD);

        assertEquals("T", subSnpCoreFields1.getReferenceInForwardStrand());
        assertEquals("A", subSnpCoreFields1.getAlternateInForwardStrand());

        // Insertion
        SubSnpCoreFields subSnpCoreFields2 = new SubSnpCoreFields(26201546,
                                                                  Orientation.FORWARD, 13677177L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  Orientation.FORWARD,
                                                                  LocusType.INSERTION,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "-", "-", "TA", "-/TA",
                                                                  null, null, null, Orientation.FORWARD,
                                                                  "NT_455866.1:g.1766473insA", 1766472L, 1766472L,
                                                                  Orientation.FORWARD);

        assertEquals("-", subSnpCoreFields2.getReferenceInForwardStrand());
        assertEquals("TA", subSnpCoreFields2.getAlternateInForwardStrand());

        // Deletion
        SubSnpCoreFields subSnpCoreFields3 = new SubSnpCoreFields(26201546,
                                                                  Orientation.FORWARD, 13677177L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  Orientation.FORWARD,
                                                                  LocusType.DELETION,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "TAGA", "TAGA", "T", "TAGA/T",
                                                                  null, null, null, Orientation.FORWARD,
                                                                  "NT_455866.1:g.1766473delAGA", 1766472L, 1766472L,
                                                                  Orientation.FORWARD);

        assertEquals("TAGA", subSnpCoreFields3.getReferenceInForwardStrand());
        assertEquals("T", subSnpCoreFields3.getAlternateInForwardStrand());
    }

    @Test
    public void hgvsCReverseHgvsTForwardStrandMustChange() throws Exception {
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(26201546,
                                                                  Orientation.FORWARD, 13677177L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  Orientation.FORWARD,
                                                                  LocusType.INSERTION,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "T", "T", "TAGA", "T/TAGA",
                                                                  "NC_006091.4:g.91223962insAGA", 91223961L, 91223961L,
                                                                  Orientation.REVERSE,
                                                                  "NT_455866.1:g.1766473insAGA", 1766472L, 1766472L,
                                                                  Orientation.FORWARD);

        assertEquals("A", subSnpCoreFields1.getReferenceInForwardStrand());
        assertEquals("TCTA", subSnpCoreFields1.getAlternateInForwardStrand());

        // Insertion
        SubSnpCoreFields subSnpCoreFields2 = new SubSnpCoreFields(2018365557,
                                                                  Orientation.FORWARD, 1060492716L,
                                                                  Orientation.FORWARD,
                                                                  "NT_456010.1",
                                                                  107452L,
                                                                  107453L,
                                                                  Orientation.REVERSE,
                                                                  LocusType.INSERTION,
                                                                  "25",
                                                                  89000L,
                                                                  89001L,
                                                                  "-", "-", "G", "-/G",
                                                                  "NC_006112.3:g.88998_88999insC", 88997L, 88998L,
                                                                  Orientation.REVERSE,
                                                                  "NT_456010.1:g.107453_107454insG", 107452L, 107453L,
                                                                  Orientation.FORWARD);

        assertEquals("-", subSnpCoreFields2.getReferenceInForwardStrand());
        assertEquals("C", subSnpCoreFields2.getAlternateInForwardStrand());
    }

    @Test
    public void hgvsCForwardHgvsTReverseStrandMustNotChange() throws Exception {
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(26201546,
                                                                  Orientation.FORWARD, 13677177L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  Orientation.FORWARD,
                                                                  LocusType.INSERTION,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "T", "T", "TAGA", "T/TAGA",
                                                                  "NC_006091.4:g.91223962insAGA", 91223961L, 91223961L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1:g.1766473insAGA", 1766472L, 1766472L,
                                                                  Orientation.REVERSE);

        assertEquals("T", subSnpCoreFields1.getReferenceInForwardStrand());
        assertEquals("TAGA", subSnpCoreFields1.getAlternateInForwardStrand());
    }

    @Test
    public void hgvsCNullHgvsTReverseStrandMustChange() throws Exception {
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(26201546,
                                                                  Orientation.FORWARD, 13677177L,
                                                                  Orientation.FORWARD,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  Orientation.FORWARD,
                                                                  LocusType.INSERTION,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "T", "T", "TAGA", "T/TAGA",
                                                                  null, null, null, Orientation.FORWARD,
                                                                  "NT_455866.1:g.1766473insAGA", 1766472L, 1766472L,
                                                                  Orientation.REVERSE);

        assertEquals("A", subSnpCoreFields1.getReferenceInForwardStrand());
        assertEquals("TCTA", subSnpCoreFields1.getAlternateInForwardStrand());
    }

    @Test
    public void allelesReverseStrandMustChange() throws Exception {
        assertEquals("T/C", buildSubSnpCoreFieldsWithOrientations("G/A", Orientation.REVERSE, Orientation.FORWARD,
                                                                  Orientation.FORWARD).getAllelesInForwardStrand());
        assertEquals("T/C", buildSubSnpCoreFieldsWithOrientations("G/A", Orientation.FORWARD, Orientation.REVERSE,
                                                                  Orientation.FORWARD).getAllelesInForwardStrand());
        assertEquals("T/C", buildSubSnpCoreFieldsWithOrientations("G/A", Orientation.FORWARD, Orientation.FORWARD,
                                                                  Orientation.REVERSE).getAllelesInForwardStrand());
        assertEquals("T/C", buildSubSnpCoreFieldsWithOrientations("G/A", Orientation.REVERSE, Orientation.REVERSE,
                                                                  Orientation.REVERSE).getAllelesInForwardStrand());
    }

    @Test
    public void allelesReverseStrandMustNotChange() throws Exception {
        assertEquals("T/C", buildSubSnpCoreFieldsWithOrientations("T/C", Orientation.FORWARD, Orientation.FORWARD,
                                                                  Orientation.FORWARD).getAllelesInForwardStrand());
        assertEquals("T/C", buildSubSnpCoreFieldsWithOrientations("T/C", Orientation.REVERSE, Orientation.REVERSE,
                                                                  Orientation.FORWARD).getAllelesInForwardStrand());
        assertEquals("T/C", buildSubSnpCoreFieldsWithOrientations("T/C", Orientation.FORWARD, Orientation.REVERSE,
                                                                  Orientation.REVERSE).getAllelesInForwardStrand());
        assertEquals("T/C", buildSubSnpCoreFieldsWithOrientations("T/C", Orientation.REVERSE, Orientation.FORWARD,
                                                                  Orientation.REVERSE).getAllelesInForwardStrand());
    }

    private SubSnpCoreFields buildSubSnpCoreFieldsWithOrientations(String alleles, Orientation subsnpOrientation,
                                                                   Orientation snpOrientation,
                                                                   Orientation contigOrientation) {
        return new SubSnpCoreFields(0, subsnpOrientation, 0L, snpOrientation,
                                    "", 0L, 0L, contigOrientation,
                                    LocusType.SNP, "", 0L, 0L,
                                    "", "", "", alleles,
                                    "", 0L, 0L, Orientation.FORWARD,
                                    "", 0L, 0L, Orientation.FORWARD);
    }

    @Test
    public void longAllelesReverseStrandMustChange() throws Exception {
        assertEquals("AGGG/TCC",
                     buildSubSnpCoreFieldsWithOrientations("GGA/CCCT", Orientation.REVERSE, Orientation.FORWARD,
                                                           Orientation.FORWARD).getAllelesInForwardStrand());
    }
}
