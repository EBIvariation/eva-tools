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

import uk.ac.ebi.eva.commons.core.models.IVariant;
import uk.ac.ebi.eva.commons.core.models.IVariantSourceEntry;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.pipeline.VariantSourceEntry;
import uk.ac.ebi.eva.dbsnpimporter.models.LocusType;
import uk.ac.ebi.eva.dbsnpimporter.models.Orientation;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.SubSnpCoreFieldsToVariantProcessor.DBSNP_BUILD_KEY;

public class SubSnpCoreFieldsToVariantProcessorTest {

    public static final int DBSNP_BUILD = 150;

    public static final int DBSNP_BATCH = 10;

    private SubSnpCoreFieldsToVariantProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new SubSnpCoreFieldsToVariantProcessor(DBSNP_BUILD, DBSNP_BATCH);
    }

    @Test
    public void testSnp() throws Exception {
        SubSnpCoreFields subSnpCoreFields =
                new SubSnpCoreFields(26201546L, Orientation.FORWARD, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                     1766472L, 1766472L, Orientation.FORWARD, LocusType.SNP, "4", 91223961L,
                                     91223961L, "T", "T", "A", "T/A", "NC_006091.4:g.91223961T>A", 91223961L,
                                     91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>A", 1766472L,
                                     1766472L, Orientation.FORWARD);
        Variant variant = new Variant("4", 91223961L, 91223961L, "T", "A");
        variant.setMainId("rs" + 13677177L);
        Map<String, String> attributes = Collections.singletonMap(DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        VariantSourceEntry sourceEntry = new VariantSourceEntry(String.valueOf(DBSNP_BATCH),
                                                                String.valueOf(DBSNP_BATCH), new String[0], null, null,
                                                                attributes, null);
        variant.addSourceEntry(sourceEntry);

        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testReverseSnp() throws Exception {
        SubSnpCoreFields subSnpCoreFields =
                new SubSnpCoreFields(26201546L, Orientation.REVERSE, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                     1766472L, 1766472L, Orientation.FORWARD, LocusType.SNP, "4", 91223961L,
                                     91223961L, "A", "A", "G", "T/C", "NC_006091.4:g.91223961T>A", 91223961L,
                                     91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>A", 1766472L,
                                     1766472L, Orientation.FORWARD);
        Variant variant = new Variant("4", 91223961L, 91223961L, "A", "G");
        variant.setMainId("rs" + 13677177L);
        Map<String, String> attributes = Collections.singletonMap(DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        VariantSourceEntry sourceEntry = new VariantSourceEntry(String.valueOf(DBSNP_BATCH),
                                                                String.valueOf(DBSNP_BATCH), new String[0], null, null,
                                                                attributes, null);
        variant.addSourceEntry(sourceEntry);

        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testInsertion() throws Exception {
        SubSnpCoreFields subSnpCoreFields =
                new SubSnpCoreFields(26201546L, Orientation.FORWARD, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                     1766472L, 1766472L, Orientation.FORWARD, LocusType.INSERTION, "4", 91223960L,
                                     91223961L, "-", "-", "A", "-/A", "NC_006091.4:g.91223961T>A", 91223961L,
                                     91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>A", 1766472L,
                                     1766472L, Orientation.FORWARD);
        Variant variant = new Variant("4", 91223961L, 91223961L, "", "A");
        variant.setMainId("rs" + 13677177L);
        Map<String, String> attributes = Collections.singletonMap(DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        VariantSourceEntry sourceEntry = new VariantSourceEntry(String.valueOf(DBSNP_BATCH),
                                                                String.valueOf(DBSNP_BATCH), new String[0], null, null,
                                                                attributes, null);
        variant.addSourceEntry(sourceEntry);

        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testDeletion() throws Exception {
        SubSnpCoreFields subSnpCoreFields =
                new SubSnpCoreFields(26201546L, Orientation.FORWARD, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                     1766472L, 1766472L, Orientation.FORWARD, LocusType.DELETION, "4", 91223961L,
                                     91223961L, "A", "A", "-", "A/-", "NC_006091.4:g.91223961T>A", 91223961L,
                                     91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>A", 1766472L,
                                     1766472L, Orientation.FORWARD);
        Variant variant = new Variant("4", 91223961L, 91223961L, "A", "");
        variant.setMainId("rs" + 13677177L);
        Map<String, String> attributes = Collections.singletonMap(DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        VariantSourceEntry sourceEntry = new VariantSourceEntry(String.valueOf(DBSNP_BATCH),
                                                                String.valueOf(DBSNP_BATCH), new String[0], null, null,
                                                                attributes, null);
        variant.addSourceEntry(sourceEntry);

        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testMultiallelicReverse() throws Exception {
        SubSnpCoreFields subSnpCoreFields =
                new SubSnpCoreFields(26201546L, Orientation.REVERSE, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                     1766472L, 1766472L, Orientation.FORWARD, LocusType.LONGER_ON_CONTIG, "4", 91223961L,
                                     91223961L, "GTA", "GTA", "T", "TAC/A/CC", "NC_006091.4:g.91223961T>A", 91223961L,
                                     91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>A", 1766472L,
                                     1766472L, Orientation.FORWARD);
        Variant variant = new Variant("4", 91223961L, 91223963L, "GTA", "T");
        variant.setMainId("rs" + 13677177L);
        Map<String, String> attributes = Collections.singletonMap(DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        VariantSourceEntry sourceEntry = new VariantSourceEntry(String.valueOf(DBSNP_BATCH),
                                                                String.valueOf(DBSNP_BATCH), new String[]{"GG"}, null,
                                                                null, attributes, null);
        variant.addSourceEntry(sourceEntry);

        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    private void assertVariantEquals(IVariant variant, IVariant processedVariant) {
        assertEquals(variant, processedVariant);
        assertEquals(variant.getMainId(), processedVariant.getMainId());

        assertEquals(1, processedVariant.getSourceEntries().size());
        IVariantSourceEntry sourceEntry = variant.getSourceEntries().iterator().next();
        IVariantSourceEntry actualSourceEntry = processedVariant.getSourceEntries().iterator().next();

        assertEquals(sourceEntry.getFileId(), actualSourceEntry.getFileId());
        assertEquals(sourceEntry.getStudyId(), actualSourceEntry.getStudyId());
        assertArrayEquals(sourceEntry.getSecondaryAlternates(), actualSourceEntry.getSecondaryAlternates());
        assertEquals(sourceEntry.getAttributes(), actualSourceEntry.getAttributes());
    }
}
