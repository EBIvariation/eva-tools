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

import uk.ac.ebi.eva.commons.core.models.IVariant;
import uk.ac.ebi.eva.commons.core.models.IVariantSourceEntry;
import uk.ac.ebi.eva.commons.core.models.VariantStatistics;
import uk.ac.ebi.eva.commons.core.models.VariantType;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.pipeline.VariantSourceEntry;
import uk.ac.ebi.eva.commons.mongodb.entities.VariantMongo;
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
import static uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.RenormalizationProcessor.AMBIGUOUS_VARIANT_KEY;
import static uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.RenormalizationProcessor.AMBIGUOUS_VARIANT_VALUE;
import static uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.SubSnpCoreFieldsToVariantProcessor.DBSNP_BUILD_KEY;


/**
 * Check that the RenormalizationProcessor only modifies ambiguous variants, and changes the positions and alleles
 * correctly otherwise.
 *
 * The tests use the fasta in the resources folder, which should start with "TGCGCCA".
 */
public class RenormalizationProcessorTest {

    private static final double FLOAT_DELTA = 0.001;

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
        assertNonAmbiguousDoesNotChange(3, "C", "T");   // 3:C>T
        assertNonAmbiguousDoesNotChange(3, "CG", "GC"); // 2:GCG>GGC
    }

    @Test
    public void nonAmbiguousInsertions() throws Exception {
        assertNonAmbiguousDoesNotChange(3, "", "A");    // 2:G>GA
        assertNonAmbiguousDoesNotChange(3, "", "AA");   // 2:G>GAA
        assertNonAmbiguousDoesNotChange(3, "", "AAA");  // 2:G>GAAA
        assertNonAmbiguousDoesNotChange(3, "", "C");    // 2:G>GC
        assertNonAmbiguousDoesNotChange(3, "", "CGC");  // 2:G>GCGC
        assertNonAmbiguousDoesNotChange(5, "", "CGC");  // 4:G>GCGC
        assertNonAmbiguousDoesNotChange(5, "", "CC");   // 2:G>GCC
    }

    @Test
    public void nonAmbiguousDeletions() throws Exception {
        assertNonAmbiguousDoesNotChange(3, "C", "");    // 2:GC>G
        assertNonAmbiguousDoesNotChange(3, "CGC", "");  // 2:GCGC>G
        assertNonAmbiguousDoesNotChange(5, "CGC", "");  // 4:GCGC>G
        assertNonAmbiguousDoesNotChange(5, "CC", "");   // 4:GCC>G
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
        assertMatchesExpected(3, "", "G", 2, 2, "", "G");   // 2:G>GG
        assertMatchesExpected(3, "", "CG", 2, 3, "", "GC"); // 2:G>GCG
        assertMatchesExpected(5, "", "CG", 4, 5, "", "GC"); // 4:G>GCG
        assertMatchesExpected(7, "", "C", 6, 6, "", "C");   // 6:C>CC
        assertMatchesExpected(7, "", "CC", 6, 7, "", "CC"); // 6:C>CCC
        assertMatchesExpected(7, "", "CCC", 6, 8, "", "CCC");   // 6:C>CCCC
    }

    @Test
    public void ambiguousDeletions() throws Exception {
        assertMatchesExpected(3, "CG", "", 2, 3, "GC", ""); // 2:GCG>G
        assertMatchesExpected(5, "CG", "", 4, 5, "GC", ""); // 4:GCG>G
        assertMatchesExpected(6, "C", "", 5, 5, "C", "");   // 5:CC>C
    }

    private void assertMatchesExpected(int position, String reference, String alternate, int expectedStart,
                                       int expectedEnd, String expectedReference,
                                       String expectedAlternate) throws Exception {
        int endPosition = computeEnd(position, reference, alternate);
        Variant variant = new Variant("22", position, endPosition, reference, alternate);
        IVariant renormalized = renormalizer.process(variant);
        assertNotNull(renormalized);
        assertEquals(expectedStart, renormalized.getStart());
        assertEquals(expectedEnd, renormalized.getEnd());
        assertEquals(expectedReference, renormalized.getReference());
        assertEquals(expectedAlternate, renormalized.getAlternate());
    }

    @Test
    public void variantIsCopiedCompletely() throws Exception {
        //given
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
                                                     alternate, "0/1", 1, 2, 3, (float) 0.4, (float) 0.5,
                                                     (float) 0.6, (float) 0.7));

        Map<String, String> attributes = new HashMap<>();
        attributes.put(DBSNP_BUILD_KEY, "test_release");

        List<Map<String, String>> samplesData = new ArrayList<>();
        samplesData.add(Collections.singletonMap("GT", "0.1"));

        VariantSourceEntry variantSourceEntry = new VariantSourceEntry("fileId", "studyId", new String[]{"T"}, "GT",
                                                                       cohortStats, attributes, samplesData);
        variant.addSourceEntry(variantSourceEntry);

        //when
        IVariant renormalized = renormalizer.process(variant);

        //then
        assertEquals(variant.getChromosome(), renormalized.getChromosome());
        assertEquals(renormalizedAlternate, renormalized.getAlternate());
        assertEquals(variant.getDbsnpIds(), renormalized.getDbsnpIds());
        assertEquals(variant.getMainId(), renormalized.getMainId());
        assertEquals(variant.getIds(), renormalized.getIds());
        assertEquals(variant.getHgvs(), renormalized.getHgvs());
        assertEquals(variant.getLength(), renormalized.getLength());
        assertEquals(variant.getType(), renormalized.getType());

        assertEquals(variant.getSourceEntries().size(), renormalized.getSourceEntries().size());
        Iterator<VariantSourceEntry> variantSourceEntryIterator = variant.getSourceEntries().iterator();
        Iterator<? extends IVariantSourceEntry> renormalizedSourceEntryIterator = renormalized.getSourceEntries().iterator();

        while (variantSourceEntryIterator.hasNext() && renormalizedSourceEntryIterator.hasNext()) {
            VariantSourceEntry sourceEntry = variantSourceEntryIterator.next();
            IVariantSourceEntry renormalizedSourceEntry = renormalizedSourceEntryIterator.next();
            assertEquals(sourceEntry.getStudyId(), renormalizedSourceEntry.getStudyId());
            assertEquals(sourceEntry.getFileId(), renormalizedSourceEntry.getFileId());
            assertEquals(sourceEntry.getFormat(), renormalizedSourceEntry.getFormat());
            assertVariantStatisticsEquals(sourceEntry, renormalizedSourceEntry, reference, renormalizedAlternate);
            assertEquals(sourceEntry.getSamplesData(), renormalizedSourceEntry.getSamplesData());
            assertAttributesEquals(sourceEntry, renormalizedSourceEntry, variant);

            // TODO jmmut: should this change?
            assertArrayEquals(sourceEntry.getSecondaryAlternates(), renormalizedSourceEntry.getSecondaryAlternates());
        }
    }

    private void assertVariantStatisticsEquals(VariantSourceEntry sourceEntry,
                                               IVariantSourceEntry renormalizedSourceEntry,
                                               String renormalizedReference,
                                               String renormalizedAlternate) {
        assertEquals(sourceEntry.getCohortStats().size(), renormalizedSourceEntry.getCohortStats().size());
        Iterator<Map.Entry<String, VariantStatistics>> statsIterator =
                sourceEntry.getCohortStats().entrySet().iterator();
        Iterator<Map.Entry<String, VariantStatistics>> renormalizedStatsIterator =
                renormalizedSourceEntry.getCohortStats().entrySet().iterator();
        while (statsIterator.hasNext() && renormalizedStatsIterator.hasNext()) {
            Map.Entry<String, VariantStatistics> statsEntry = statsIterator.next();
            Map.Entry<String, VariantStatistics> renormalizedStatsEntry = renormalizedStatsIterator.next();
            assertEquals(statsEntry.getKey(), renormalizedStatsEntry.getKey());
            VariantStatistics renormalizedStats = renormalizedStatsEntry.getValue();
            assertEquals(renormalizedReference, renormalizedStats.getRefAllele());
            assertEquals(renormalizedAlternate, renormalizedStats.getAltAllele());
            assertEquals(renormalizedAlternate, renormalizedStats.getMafAllele());
            VariantStatistics stats = statsEntry.getValue();
            assertEquals(stats.getRefAlleleCount(), renormalizedStats.getRefAlleleCount());
            assertEquals(stats.getRefAlleleFreq(), renormalizedStats.getRefAlleleFreq(), FLOAT_DELTA);
            assertEquals(stats.getAltAlleleCount(), renormalizedStats.getAltAlleleCount());
            assertEquals(stats.getAltAlleleFreq(), renormalizedStats.getAltAlleleFreq(), FLOAT_DELTA);
            assertEquals(stats.getMaf(), renormalizedStats.getMaf(), FLOAT_DELTA);
            assertEquals(stats.getMgf(), renormalizedStats.getMgf(), FLOAT_DELTA);
            assertEquals(stats.getMgfGenotype(), renormalizedStats.getMgfGenotype());
            assertEquals(stats.getMissingAlleles(), renormalizedStats.getMissingAlleles());
            assertEquals(stats.getMissingGenotypes(), renormalizedStats.getMissingGenotypes());
        }
    }

    private void assertAttributesEquals(VariantSourceEntry sourceEntry, IVariantSourceEntry renormalizedSourceEntry,
                                        Variant variant) {
        Map<String, String> renormalizedAttributes = renormalizedSourceEntry.getAttributes();
        for (Map.Entry<String, String> attribute : sourceEntry.getAttributes().entrySet()) {
            assertEquals(attribute.getValue(), renormalizedAttributes.get(attribute.getKey()));
        }
        assertEquals(AMBIGUOUS_VARIANT_VALUE, renormalizedAttributes.get(AMBIGUOUS_VARIANT_KEY));
        assertEquals(variant.getStart(), Integer.parseInt(renormalizedAttributes.get(VariantMongo.START_FIELD)));
        assertEquals(variant.getReference(), renormalizedAttributes.get(VariantMongo.REFERENCE_FIELD));
        assertEquals(variant.getAlternate(), renormalizedAttributes.get(VariantMongo.ALTERNATE_FIELD));
    }
}
