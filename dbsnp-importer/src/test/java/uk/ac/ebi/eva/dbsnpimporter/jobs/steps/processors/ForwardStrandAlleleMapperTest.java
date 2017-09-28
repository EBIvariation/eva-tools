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

import uk.ac.ebi.eva.dbsnpimporter.models.Orientation;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import static org.junit.Assert.*;

public class ForwardStrandAlleleMapperTest {

    @Test
    public void snpAllelesInForwardStrandMustNotChange() throws Exception {
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(26201546,
                                                                  13677177L,
                                                                  1,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  1,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "T", "T", "A", "T/A",
                                                                  "NC_006091.4:g.91223961T>A", 91223961L, 91223961L, 1,
                                                                  "NT_455866.1:g.1766472T>A", 1766472L, 1766472L, 1);
        ForwardStrandAlleleMapper mapper1 = new ForwardStrandAlleleMapper(subSnpCoreFields1);

        assertEquals("T", mapper1.getReferenceInForwardStrand());
        assertEquals("A", mapper1.getAlternateInForwardStrand());
    }

    @Test
    public void insertionAllelesInForwardStrandMustNotChange() throws Exception {
        // Insertion with non-empty alleles
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(26201546,
                                                                 13677177L,
                                                                 1,
                                                                 "NT_455866.1",
                                                                 1766472L,
                                                                 1766472L,
                                                                 1,
                                                                 "4",
                                                                 91223961L,
                                                                 91223961L,
                                                                 "T", "T", "TAGA", "T/TAGA",
                                                                 "NC_006091.4:g.91223962insAGA", 91223961L, 91223961L, 1,
                                                                 "NT_455866.1:g.1766473insAGA", 1766472L, 1766472L, 1);
        ForwardStrandAlleleMapper mapper1 = new ForwardStrandAlleleMapper(subSnpCoreFields1);

        assertEquals("T", mapper1.getReferenceInForwardStrand());
        assertEquals("TAGA", mapper1.getAlternateInForwardStrand());

        // Insertion with dash in reference
        SubSnpCoreFields subSnpCoreFields2 = new SubSnpCoreFields(26201546,
                                                                 13677177L,
                                                                 1,
                                                                 "NT_455866.1",
                                                                 1766472L,
                                                                 1766472L,
                                                                 1,
                                                                 "4",
                                                                 91223961L,
                                                                 91223961L,
                                                                 "-", "-", "TA", "-/TA",
                                                                 "NC_006091.4:g.91223962insA", 91223961L, 91223961L, 1,
                                                                 "NT_455866.1:g.1766473insA", 1766472L, 1766472L, 1);
        ForwardStrandAlleleMapper mapper2 = new ForwardStrandAlleleMapper(subSnpCoreFields2);

        assertEquals("-", mapper2.getReferenceInForwardStrand());
        assertEquals("TA", mapper2.getAlternateInForwardStrand());

        // Insertion with null reference
        SubSnpCoreFields subSnpCoreFields3 = new SubSnpCoreFields(26201546,
                                                                  13677177L,
                                                                  1,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  1,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  null, null, "TA", "-/TA",
                                                                  "NC_006091.4:g.91223962insA", 91223961L, 91223961L, 1,
                                                                  "NT_455866.1:g.1766473insA", 1766472L, 1766472L, 1);
        ForwardStrandAlleleMapper mapper3 = new ForwardStrandAlleleMapper(subSnpCoreFields3);

        assertNull(mapper3.getReferenceInForwardStrand());
        assertEquals("TA", mapper3.getAlternateInForwardStrand());
    }

    @Test
    public void deletionAllelesInForwardStrandMustNotChange() throws Exception {
        // Deletion with non-empty alleles
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(26201546,
                                                                  13677177L,
                                                                  1,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  1,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "TAGA", "TAGA", "T", "TAGA/T",
                                                                  "NC_006091.4:g.91223962delAGA", 91223961L, 91223961L, 1,
                                                                  "NT_455866.1:g.17664723nsAGA", 1766472L, 1766472L, 1);
        ForwardStrandAlleleMapper mapper1 = new ForwardStrandAlleleMapper(subSnpCoreFields1);

        assertEquals("TAGA", mapper1.getReferenceInForwardStrand());
        assertEquals("T", mapper1.getAlternateInForwardStrand());

        // Deletion with dash in alternate
        SubSnpCoreFields subSnpCoreFields2 = new SubSnpCoreFields(26201546,
                                                                 13677177L,
                                                                 1,
                                                                 "NT_455866.1",
                                                                 1766472L,
                                                                 1766472L,
                                                                 1,
                                                                 "4",
                                                                 91223961L,
                                                                 91223961L,
                                                                 "TA", "TA", "-", "TA/-",
                                                                 "NC_006091.4:g.91223961delTA", 91223961L, 91223961L, 1,
                                                                 "NT_455866.1:g.1766472delTA", 1766472L, 1766472L, 1);
        ForwardStrandAlleleMapper mapper2 = new ForwardStrandAlleleMapper(subSnpCoreFields2);

        assertEquals("TA", mapper2.getReferenceInForwardStrand());
        assertEquals("-", mapper2.getAlternateInForwardStrand());

        // Deletion with null alternate
        SubSnpCoreFields subSnpCoreFields3 = new SubSnpCoreFields(26201546,
                                                                  13677177L,
                                                                  1,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  1,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "TA", "TA", null, "TA/-",
                                                                  "NC_006091.4:g.91223961delTA", 91223961L, 91223961L, 1,
                                                                  "NT_455866.1:g.1766472delTA", 1766472L, 1766472L, 1);
        ForwardStrandAlleleMapper mapper3 = new ForwardStrandAlleleMapper(subSnpCoreFields3);

        assertEquals("TA", mapper3.getReferenceInForwardStrand());
        assertNull(mapper3.getAlternateInForwardStrand());
    }

    @Test
    public void allelesInForwardStrandAndNullHgvsCMustNotChange() throws Exception {
        // SNP
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(26201546,
                                                                  13677177L,
                                                                  1,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  1,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  null, "T", "A", "T/A",
                                                                  null, null, null, 1,
                                                                  "NT_455866.1:g.1766472T>A", 1766472L, 1766472L, 1);
        ForwardStrandAlleleMapper mapper1 = new ForwardStrandAlleleMapper(subSnpCoreFields1);

        assertEquals("T", mapper1.getReferenceInForwardStrand());
        assertEquals("A", mapper1.getAlternateInForwardStrand());

        // Insertion
        SubSnpCoreFields subSnpCoreFields2 = new SubSnpCoreFields(26201546,
                                                                  13677177L,
                                                                  1,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  1,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "-", "-", "TA", "-/TA",
                                                                  null, null, null, 1,
                                                                  "NT_455866.1:g.1766473insA", 1766472L, 1766472L, 1);
        ForwardStrandAlleleMapper mapper2 = new ForwardStrandAlleleMapper(subSnpCoreFields2);

        assertEquals("-", mapper2.getReferenceInForwardStrand());
        assertEquals("TA", mapper2.getAlternateInForwardStrand());

        // Deletion
        SubSnpCoreFields subSnpCoreFields3 = new SubSnpCoreFields(26201546,
                                                                  13677177L,
                                                                  1,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  1,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "T", "T", "TAGA", "T/TAGA",
                                                                  null, null, null, 1,
                                                                  "NT_455866.1:g.1766473insAGA", 1766472L, 1766472L, 1);
        ForwardStrandAlleleMapper mapper3 = new ForwardStrandAlleleMapper(subSnpCoreFields3);

        assertEquals("T", mapper3.getReferenceInForwardStrand());
        assertEquals("TAGA", mapper3.getAlternateInForwardStrand());
    }

    @Test
    public void hgvsCReverseHgvsTForwardStrandMustChange() throws Exception {
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(26201546,
                                                                  13677177L,
                                                                  1,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  1,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "T", "T", "TAGA", "T/TAGA",
                                                                  "NC_006091.4:g.91223962insAGA", 91223961L, 91223961L, -1,
                                                                  "NT_455866.1:g.1766473insAGA", 1766472L, 1766472L, 1);
        ForwardStrandAlleleMapper mapper1 = new ForwardStrandAlleleMapper(subSnpCoreFields1);

        assertEquals("A", mapper1.getReferenceInForwardStrand());
        assertEquals("TCTA", mapper1.getAlternateInForwardStrand());
    }

    @Test
    public void hgvsCForwardHgvsTReverseStrandMustNotChange() throws Exception {
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(26201546,
                                                                  13677177L,
                                                                  1,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  1,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "T", "T", "TAGA", "T/TAGA",
                                                                  "NC_006091.4:g.91223962insAGA", 91223961L, 91223961L, 1,
                                                                  "NT_455866.1:g.1766473insAGA", 1766472L, 1766472L, -1);
        ForwardStrandAlleleMapper mapper1 = new ForwardStrandAlleleMapper(subSnpCoreFields1);

        assertEquals("T", mapper1.getReferenceInForwardStrand());
        assertEquals("TAGA", mapper1.getAlternateInForwardStrand());
    }

    @Test
    public void hgvsCNullHgvsTReverseStrandMustChange() throws Exception {
        SubSnpCoreFields subSnpCoreFields1 = new SubSnpCoreFields(26201546,
                                                                  13677177L,
                                                                  1,
                                                                  "NT_455866.1",
                                                                  1766472L,
                                                                  1766472L,
                                                                  1,
                                                                  "4",
                                                                  91223961L,
                                                                  91223961L,
                                                                  "T", "T", "TAGA", "T/TAGA",
                                                                  null, null, null, 1,
                                                                  "NT_455866.1:g.1766473insAGA", 1766472L, 1766472L, -1);
        ForwardStrandAlleleMapper mapper1 = new ForwardStrandAlleleMapper(subSnpCoreFields1);

        assertEquals("A", mapper1.getReferenceInForwardStrand());
        assertEquals("TCTA", mapper1.getAlternateInForwardStrand());
    }
}