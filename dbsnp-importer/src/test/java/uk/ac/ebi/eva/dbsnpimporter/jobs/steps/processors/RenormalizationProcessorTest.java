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
package uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.dbsnpimporter.sequence.FastaSequenceReader;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RenormalizationProcessorTest {

    private static FastaSequenceReader fastaSequenceReader;

    private static RenormalizationProcessor renormalizer;

    @BeforeClass
    public static void setUpClass() throws Exception {
        fastaSequenceReader = new FastaSequenceReader(Paths.get("src/test/resources/Gallus_gallus-5.0.test.fa"));
        renormalizer = new RenormalizationProcessor(fastaSequenceReader);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        fastaSequenceReader.close();
    }

    @Test
    public void nonAmbiguousReplacements() throws Exception {
        checkNonAmbiguousDoesNotChange(3, "C", "T");
        checkNonAmbiguousDoesNotChange(3, "CG", "GC");
    }
    @Test
    public void nonAmbiguousInsertions() throws Exception {
        checkNonAmbiguousDoesNotChange(3, "", "A");
        checkNonAmbiguousDoesNotChange(3, "", "AA");
        checkNonAmbiguousDoesNotChange(3, "", "AAA");
        checkNonAmbiguousDoesNotChange(3, "", "C");
        checkNonAmbiguousDoesNotChange(3, "", "CG");
        checkNonAmbiguousDoesNotChange(3, "", "CGCG");
    }

    @Test
    public void nonAmbiguousDeletions() throws Exception {
        checkNonAmbiguousDoesNotChange(3, "C", "");
        checkNonAmbiguousDoesNotChange(3, "CGC", "");
    }

    private void checkNonAmbiguousDoesNotChange(int position, String reference, String alternate) throws Exception {
        int endPosition = position + Math.max(reference.length(), alternate.length());
        checkMatchesExpected(position, reference, alternate, position, endPosition, reference, alternate);
    }

    @Test
    public void ambiguousInsertions() throws Exception {
        checkMatchesExpected(3, "", "G", 2, 2, "", "G");
        checkMatchesExpected(3, "", "CG", 2, 3, "", "GC");
        checkMatchesExpected(5, "", "CG", 4, 5, "", "GC");
        checkMatchesExpected(7, "", "C", 6, 6, "", "C");
        checkMatchesExpected(7, "", "C", 6, 8, "", "CCC");
    }

    @Test
    public void ambiguousDeletions() throws Exception {
        checkMatchesExpected(3, "CG", "", 2, 3, "GC", "");
        checkMatchesExpected(5, "CG", "", 4, 5, "GC", "");
        checkMatchesExpected(6, "C", "", 5, 5, "C", "");
    }

    private void checkMatchesExpected(int position, String reference, String alternate, int expectedStart,
                                int expectedEnd, String expectedReference,
                                String expectedAlternate) throws Exception {
        int endPosition = position + Math.max(reference.length(), alternate.length());
        Variant variant = new Variant("22", position, endPosition, reference, alternate);
        Variant renormalized = renormalizer.process(variant);
        assertNotNull(renormalized);
        assertEquals(expectedStart, renormalized.getStart());
        assertEquals(expectedEnd, renormalized.getEnd());
        assertEquals(expectedReference, renormalized.getReference());
        assertEquals(expectedAlternate, renormalized.getAlternate());
    }
}