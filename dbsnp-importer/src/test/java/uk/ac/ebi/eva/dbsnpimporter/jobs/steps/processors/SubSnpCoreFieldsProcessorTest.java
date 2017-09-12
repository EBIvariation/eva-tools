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

import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.ebi.eva.commons.core.models.IVariant;
import uk.ac.ebi.eva.dbsnpimporter.models.LocationType;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import static org.junit.Assert.assertEquals;

public class SubSnpCoreFieldsProcessorTest {

    private static SubSnpCoreFieldsToVariantProcessor subSnpCoreFieldsToVariantProcessor;

    @BeforeClass
    public static void setUpClass() throws Exception {
        subSnpCoreFieldsToVariantProcessor = new SubSnpCoreFieldsToVariantProcessor();
    }

    @Test
    public void processSNPInChromosome() throws Exception {
        SubSnpCoreFields trueSnp = new SubSnpCoreFields(1092414368L, 526595372L, 1, "NW_003104285.1", 12108029L,
                                                        12108029L, 1, LocationType.SNP, "10", 100002924L, 100002924L,
                                                        null, null, null, null, null, null, null, -1, null, null, null,
                                                        -1);

        IVariant variant = subSnpCoreFieldsToVariantProcessor.process(trueSnp);

        assertVariant(variant, "10", 100002924L, 100002924L);
    }

    @Test
    public void processSNPNotInChromosome() throws Exception {
        SubSnpCoreFields trueSnp = new SubSnpCoreFields(1107437104L, 524908995L, 1, "NW_003101163.1", 943L, 943L, 1,
                                                        LocationType.SNP, null, null, null, null, null, null, null,
                                                        null, null, null, -1, null, null, null, -1);

        IVariant variant = subSnpCoreFieldsToVariantProcessor.process(trueSnp);

        // TODO: test translation of Refseq contig name to Genbank one
        assertVariant(variant, "NW_003101163.1", 943L, 943L);
    }

    @Test
    public void processSingleNucleotideDeletionInChromosome() throws Exception {
        SubSnpCoreFields trueSnp = new SubSnpCoreFields(1093365488L, 433288923L, 1, "433288923", 1591551L, 1591551L, 1,
                                                        LocationType.SNP, "12", 10144047L, 10144047L, null, null, null,
                                                        null, null, null, null, -1, null, null, null, -1);

        IVariant variant = subSnpCoreFieldsToVariantProcessor.process(trueSnp);

        assertVariant(variant, "12", 10144047L, 10144047L);
    }

    @Test
    public void processMultiNucleotideDeletionInChromosome() throws Exception {
        SubSnpCoreFields trueSnp = new SubSnpCoreFields(1085240363L, 384020033L, 1, "NW_003103847.1", 1056819L,
                                                        1056821L, 1, LocationType.DELETION, "2", 100306584L, 100306586L,
                                                        null, null, null, null, null, null, null, -1, null, null, null,
                                                        -1);

        IVariant variant = subSnpCoreFieldsToVariantProcessor.process(trueSnp);

        assertVariant(variant, "2", 100306584L, 100306586L);
    }

    @Test
    public void processDeletionNotInChromosome() throws Exception {
        SubSnpCoreFields trueSnp = new SubSnpCoreFields(1107437081L, 524371323L, 1, "NW_003101162.1", 229L, 232L, 1,
                                                        LocationType.DELETION, null, null, null, null, null, null, null,
                                                        null, null, null, -1, null, null, null, -1);

        IVariant variant = subSnpCoreFieldsToVariantProcessor.process(trueSnp);

        // TODO: test translation of Refseq contig name to Genbank one
        assertVariant(variant, "NW_003101162.1", 229L, 232L);
    }

    @Test
    public void processSingleNucleotideInsertionInChromosome() throws Exception {
        SubSnpCoreFields trueSnp = new SubSnpCoreFields(1092414490L, 522748169L, 1, "NW_003104285.1", 12118757L,
                                                        12118758L, 1, LocationType.INSERTION, "10", 100013652L,
                                                        100013653L, null, null, null, null, null, null, null, -1, null,
                                                        null, null, -1);

        IVariant variant = subSnpCoreFieldsToVariantProcessor.process(trueSnp);

        assertVariant(variant, "10", 100013653L, 100013653L);
    }

    @Test
    public void processMultiNucleotideInsertionInChromosome() throws Exception {
        SubSnpCoreFields trueSnp = new SubSnpCoreFields(1088123446L, 379115400L, 1, "NW_003103939.1", 12276L, 12277L, 1,
                                                        LocationType.INSERTION, "5", 100080173L, 100080174L, null, null,
                                                        null, null, null, null, null, -1, null, null, null, -1);

        IVariant variant = subSnpCoreFieldsToVariantProcessor.process(trueSnp);

        assertVariant(variant, "5", 100080174L, 100080178L);
    }

    @Test
    public void processInsertionNotInChromosome() throws Exception {
        SubSnpCoreFields trueSnp = new SubSnpCoreFields(1107437080L, 520781897L, 1, "NW_003101162.1", 189L, 190L, 1,
                                                        LocationType.INSERTION, null, null, null, null, null, null,
                                                        null, null, null, null, -1, null, null, null, -1);

        IVariant variant = subSnpCoreFieldsToVariantProcessor.process(trueSnp);

        // TODO: test translation of Refseq contig name to Genbank one
        assertVariant(variant, "NW_003101162.1", 190L, 191L);
    }

    private void assertVariant(IVariant variant, String expectedChromosome, long expectedStart, long expectedEnd) {
        assertEquals(expectedChromosome, variant.getChromosome());
        assertEquals(expectedStart, variant.getStart());
        assertEquals(expectedEnd, variant.getEnd());
    }

}
