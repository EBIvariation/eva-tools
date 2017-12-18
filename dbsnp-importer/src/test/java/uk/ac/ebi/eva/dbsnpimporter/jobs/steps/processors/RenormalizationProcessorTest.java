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

import uk.ac.ebi.eva.commons.core.models.VariantStatistics;
import uk.ac.ebi.eva.commons.core.models.VariantType;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.pipeline.VariantSourceEntry;
import uk.ac.ebi.eva.dbsnpimporter.sequence.FastaSequenceReader;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.SubSnpCoreFieldsToVariantProcessor.DBSNP_BUILD_KEY;


/**
 * Check that the RenormalizationProcessor only modifies ambiguous variants, and changes the positions and alleles
 * correctly otherwise.
 *
 * The tests use the fasta in the resources folder, which should start with "TGCGCCA".
 */
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
        assertNonAmbiguousDoesNotChange(3, "C", "T");
        assertNonAmbiguousDoesNotChange(3, "CG", "GC");
    }
    @Test
    public void nonAmbiguousInsertions() throws Exception {
        assertNonAmbiguousDoesNotChange(3, "", "A");
        assertNonAmbiguousDoesNotChange(3, "", "AA");
        assertNonAmbiguousDoesNotChange(3, "", "AAA");
        assertNonAmbiguousDoesNotChange(3, "", "C");
        assertNonAmbiguousDoesNotChange(3, "", "CGC");
        assertNonAmbiguousDoesNotChange(5, "", "CGC");
        assertNonAmbiguousDoesNotChange(5, "", "CC");
    }

    @Test
    public void nonAmbiguousDeletions() throws Exception {
        assertNonAmbiguousDoesNotChange(3, "C", "");
        assertNonAmbiguousDoesNotChange(3, "CGC", "");
        assertNonAmbiguousDoesNotChange(5, "CGC", "");
        assertNonAmbiguousDoesNotChange(5, "CC", "");
    }

    private void assertNonAmbiguousDoesNotChange(int position, String reference, String alternate) throws Exception {
        int endPosition = computeEnd(position, reference, alternate);
        assertMatchesExpected(position, reference, alternate, position, endPosition, reference, alternate);
    }

    private int computeEnd(int position, String reference, String alternate) {
        return position + Math.max(reference.length(), alternate.length()) - 1;
    }

    @Test
    public void ambiguousInsertions() throws Exception {
        assertMatchesExpected(3, "", "G", 2, 2, "", "G");
        assertMatchesExpected(3, "", "CG", 2, 3, "", "GC");
        assertMatchesExpected(5, "", "CG", 4, 5, "", "GC");
        assertMatchesExpected(7, "", "C", 6, 6, "", "C");
        assertMatchesExpected(7, "", "CC", 6, 7, "", "CC");
        assertMatchesExpected(7, "", "CCC", 6, 8, "", "CCC");
    }

    @Test
    public void ambiguousDeletions() throws Exception {
        assertMatchesExpected(3, "CG", "", 2, 3, "GC", "");
        assertMatchesExpected(5, "CG", "", 4, 5, "GC", "");
        assertMatchesExpected(6, "C", "", 5, 5, "C", "");
    }

    private void assertMatchesExpected(int position, String reference, String alternate, int expectedStart,
                                       int expectedEnd, String expectedReference,
                                       String expectedAlternate) throws Exception {
        int endPosition = computeEnd(position, reference, alternate);
        Variant variant = new Variant("22", position, endPosition, reference, alternate);
        Variant renormalized = renormalizer.process(variant);
        assertNotNull(renormalized);
        assertEquals(expectedStart, renormalized.getStart());
        assertEquals(expectedEnd, renormalized.getEnd());
        assertEquals(expectedReference, renormalized.getReference());
        assertEquals(expectedAlternate, renormalized.getAlternate());
    }

    @Test
    public void variantIsCopiedCompletely() throws Exception {
        int start = 3;
        String reference = "";
        String alternate = "CG";
        String renormalizedAlternate = "GC";
        int endPosition = computeEnd(start, reference, alternate);
        Variant variant = new Variant("22", start, endPosition, reference, alternate);
        String mainId = "rsId";
        HashSet<String> dbsnpIds = new HashSet<>(Arrays.asList(mainId, "ssId_1", "ssId_2"));
        variant.setDbsnpIds(dbsnpIds);
        variant.setMainId(mainId);
        Map<String, VariantStatistics> cohortStats = new HashMap<>();
        cohortStats.put("ALL", new VariantStatistics(reference, alternate, VariantType.INDEL, (float) 0.2, (float) 0.3,
                                                     renormalizedAlternate, "0/1", 1, 2, 3, (float) 0.4, (float) 0.5,
                                                     (float) 0.6, (float) 0.7));

        Map<String, String> attributes = new HashMap<>();
        attributes.put(DBSNP_BUILD_KEY, "test_release");

        List<Map<String, String>> samplesData = new ArrayList<>();
        samplesData.add(Collections.singletonMap("GT", "0.1"));

        VariantSourceEntry variantSourceEntry = new VariantSourceEntry("fileId", "studyId", new String[]{"T"}, "GT", cohortStats, attributes, samplesData);
        variant.addSourceEntry(variantSourceEntry);

        Variant renormalized = renormalizer.process(variant);

        assertEquals(variant.getChromosome(), renormalized.getChromosome());
        assertEquals(variant.getAlternate(), renormalized.getAlternate());
        assertEquals(variant.getDbsnpIds(), renormalized.getDbsnpIds());
        assertEquals(variant.getMainId(), renormalized.getMainId());
        assertEquals(variant.getIds(), renormalized.getIds());
        assertEquals(variant.getHgvs(), renormalized.getHgvs());
        assertEquals(variant.getLength(), renormalized.getLength());
        assertEquals(variant.getType(), renormalized.getType());

        assertEquals(variant.getSourceEntries().size(), renormalized.getSourceEntries().size());
        Iterator<VariantSourceEntry> variantSourceEntryIterator = variant.getSourceEntries().iterator();
        Iterator<VariantSourceEntry> renormalizedSourceEntryIterator = renormalized.getSourceEntries().iterator();

        while (variantSourceEntryIterator.hasNext() && renormalizedSourceEntryIterator.hasNext()) {
            VariantSourceEntry sourceEntry = variantSourceEntryIterator.next();
            VariantSourceEntry renormalizedSourceEntry = renormalizedSourceEntryIterator.next();
            assertEquals(sourceEntry.getStudyId(), renormalizedSourceEntry.getStudyId());
            assertEquals(sourceEntry.getFileId(), renormalizedSourceEntry.getFileId());
            assertEquals(sourceEntry.getFormat(), renormalizedSourceEntry.getFormat());
            assertEquals(sourceEntry.getCohortStats(), renormalizedSourceEntry.getCohortStats());
            assertEquals(sourceEntry.getSamplesData(), renormalizedSourceEntry.getSamplesData());
            assertEquals(sourceEntry.getAttributes(), renormalizedSourceEntry.getAttributes());

            // TODO jmmut: should this change?
            assertArrayEquals(sourceEntry.getSecondaryAlternates(), renormalizedSourceEntry.getSecondaryAlternates());
        }
    }
}