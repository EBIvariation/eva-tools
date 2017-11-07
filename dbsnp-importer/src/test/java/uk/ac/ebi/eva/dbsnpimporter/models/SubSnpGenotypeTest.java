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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class SubSnpGenotypeTest {

    @Test
    public void testSubSnpGenotypeFields() {
        List<String> genotypeList = Arrays.stream("A,A, G".split(",")).map(String::trim)
                .collect(Collectors.toList());
        SubSnpGenotype subsnpgenotype = new SubSnpGenotype(14484,"DBSNP.2005.1.20.12.27",
                32479939, "A,A, G");
        assertEquals(14484, subsnpgenotype.getBatchId());
        assertEquals("DBSNP.2005.1.20.12.27", subsnpgenotype.getLocBatchId());
        assertEquals(32479939, subsnpgenotype.getSsId());
        assertEquals(genotypeList, subsnpgenotype.getGenotypes());
        assertEquals(3, subsnpgenotype.getGenotypes().size());

        subsnpgenotype = new SubSnpGenotype(2147483647,"DBSNP.2005.1.20.12.27",
                9223372036854775807L, "");
        assertEquals(2147483647, subsnpgenotype.getBatchId());
        assertEquals("DBSNP.2005.1.20.12.27", subsnpgenotype.getLocBatchId());
        assertEquals(9223372036854775807L, subsnpgenotype.getSsId());
        assertEquals(0, subsnpgenotype.getGenotypes().size());
    }
}
