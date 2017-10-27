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
package uk.ac.ebi.eva.dbsnpimporter.test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;

public class TestUtils {
    public static <T> void assertContains(Collection<T> collection, T element) {
        if (!collection.contains(element)) {
            fail("Collection doesn't contain element. Element: " + element + ".\n Collection: " + collection.toString());
        }
    }

    public static Set<String> buildIds(long subSnpId, long snpId) {
        HashSet<String> ids = new HashSet<>();
        ids.add("ss" + subSnpId);
        ids.add("rs" + snpId);
        return ids;
    }
}
