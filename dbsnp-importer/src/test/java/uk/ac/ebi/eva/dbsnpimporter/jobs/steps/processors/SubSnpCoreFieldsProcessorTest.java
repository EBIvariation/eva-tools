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
        SubSnpCoreFields trueSnp = new SubSnpCoreFields(1092414368, 526595372, 1, "NW_003104285.1", 12108029, 12108029,
                                                        1, "10", 100002924, 100002924);

        IVariant variant = subSnpCoreFieldsToVariantProcessor.process(trueSnp);

        assertVariant(variant, "10",100002924, 100002924);
    }

    @Test
    public void processSNPNotInChromosome() throws Exception {
        SubSnpCoreFields trueSnp = new SubSnpCoreFields(1107437104, 524908995, 1, "NW_003101163.1", 943, 943, 1, null,
                                                        null, null);

        IVariant variant = subSnpCoreFieldsToVariantProcessor.process(trueSnp);

        assertVariant(variant, "NW_003101163.1", 943, 943);
    }

    private void assertVariant(IVariant variant, String expectedChromosome, int expectedStart, int expectedEnd) {
        assertEquals(expectedChromosome, variant.getChromosome());
        assertEquals(expectedStart, variant.getStart());
        assertEquals(expectedEnd, variant.getEnd());
    }

}