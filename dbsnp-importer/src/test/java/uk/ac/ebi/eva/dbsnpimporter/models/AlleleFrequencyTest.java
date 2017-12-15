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
package uk.ac.ebi.eva.dbsnpimporter.models;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AlleleFrequencyTest {

    @Test
    public void compareTo() throws Exception {
        // all the fields but frequency are the same because we want to be sure that we are comparing frequencies
        AlleleFrequency minorFreq = new AlleleFrequency("A", 10, 0.2);
        AlleleFrequency greaterFreq = new AlleleFrequency("A", 10, 0.5);
        AlleleFrequency otherGreaterFreq = new AlleleFrequency("A", 10, 0.5);

        assertTrue(minorFreq.compareTo(greaterFreq) < 0);
        assertTrue(greaterFreq.compareTo(minorFreq) > 0);
        assertTrue(greaterFreq.compareTo(otherGreaterFreq) == 0);
        assertTrue(otherGreaterFreq.compareTo(greaterFreq) == 0);
    }

}