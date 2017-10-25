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
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.dbsnpimporter.models.LocusType;
import uk.ac.ebi.eva.dbsnpimporter.models.Orientation;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import static org.junit.Assert.assertEquals;

public class SubSnpCoreFieldsToEvaSubmittedVariantProcessorTest {

    public static final int DBSNP_BUILD = 150;

    public static final String DBSNP_BATCH = "some_study";

    private SubSnpCoreFieldsToVariantProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new SubSnpCoreFieldsToVariantProcessor(DBSNP_BUILD);
    }

    @Test
    public void testSnp() throws Exception {
        SubSnpCoreFields subSnpCoreFields =
                new SubSnpCoreFields(26201546L, Orientation.FORWARD, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                     1766472L, 1766472L, Orientation.FORWARD, LocusType.SNP, "4", 91223961L,
                                     91223961L, "T", "T", "A", "T/A", "NC_006091.4:g.91223961T>A", 91223961L,
                                     91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>A", 1766472L,
                                     1766472L, Orientation.FORWARD, DBSNP_BATCH);
        Variant variant = new Variant("4", 91223961L, 91223961L, "T", "A");
        variant.setMainId("rs" + 13677177L);

        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testReverseSnp() throws Exception {
        SubSnpCoreFields subSnpCoreFields =
                new SubSnpCoreFields(26201546L, Orientation.REVERSE, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                     1766472L, 1766472L, Orientation.FORWARD, LocusType.SNP, "4", 91223961L,
                                     91223961L, "A", "A", "G", "T/C", "NC_006091.4:g.91223961T>A", 91223961L,
                                     91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>A", 1766472L,
                                     1766472L, Orientation.FORWARD, DBSNP_BATCH);
        Variant variant = new Variant("4", 91223961L, 91223961L, "A", "G");
        variant.setMainId("rs" + 13677177L);

        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testInsertion() throws Exception {
        SubSnpCoreFields subSnpCoreFields =
                new SubSnpCoreFields(26201546L, Orientation.FORWARD, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                     1766472L, 1766472L, Orientation.FORWARD, LocusType.INSERTION, "4", 91223960L,
                                     91223961L, "-", "-", "A", "-/A", "NC_006091.4:g.91223961T>A", 91223961L,
                                     91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>A", 1766472L,
                                     1766472L, Orientation.FORWARD, DBSNP_BATCH);
        Variant variant = new Variant("4", 91223961L, 91223961L, "", "A");
        variant.setMainId("rs" + 13677177L);
        
        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testDeletion() throws Exception {
        SubSnpCoreFields subSnpCoreFields =
                new SubSnpCoreFields(26201546L, Orientation.FORWARD, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                     1766472L, 1766472L, Orientation.FORWARD, LocusType.DELETION, "4", 91223961L,
                                     91223961L, "A", "A", "-", "A/-", "NC_006091.4:g.91223961T>A", 91223961L,
                                     91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>A", 1766472L,
                                     1766472L, Orientation.FORWARD, DBSNP_BATCH);
        Variant variant = new Variant("4", 91223961L, 91223961L, "A", "");
        variant.setMainId("rs" + 13677177L);

        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    @Test
    public void testMultiallelicReverse() throws Exception {
        SubSnpCoreFields subSnpCoreFields =
                new SubSnpCoreFields(26201546L, Orientation.REVERSE, 13677177L, Orientation.FORWARD, "NT_455866.1",
                                     1766472L, 1766472L, Orientation.FORWARD, LocusType.LONGER_ON_CONTIG, "4", 91223961L,
                                     91223961L, "GTA", "GTA", "T", "TAC/A/CC", "NC_006091.4:g.91223961T>A", 91223961L,
                                     91223961L, Orientation.FORWARD, "NT_455866.1:g.1766472T>A", 1766472L,
                                     1766472L, Orientation.FORWARD, DBSNP_BATCH);
        Variant variant = new Variant("4", 91223961L, 91223963L, "GTA", "T");
        variant.setMainId("rs" + 13677177L);

        assertVariantEquals(variant, processor.process(subSnpCoreFields));
    }

    private void assertVariantEquals(IVariant variant, IVariant processedVariant) {
        assertEquals(variant, processedVariant);
        assertEquals(variant.getMainId(), processedVariant.getMainId());
    }
}
