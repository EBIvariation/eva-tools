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
package uk.ac.ebi.eva.vcfdump;

import htsjdk.tribble.readers.LineIteratorImpl;
import htsjdk.tribble.readers.LineReaderUtil;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of HTSJDK LineIterator that can filter out VCF Header fields lines when iterating
 */
public class VcfHeaderFilteringLineIterator extends LineIteratorImpl {

    private Set<String> fieldsToExclude;

    public VcfHeaderFilteringLineIterator(InputStream inputStream, String ... fieldsToExclude) {
        super(LineReaderUtil.fromBufferedStream(inputStream));
        this.fieldsToExclude =
                Arrays.stream(fieldsToExclude).map(field -> "##" + field + "=").collect(Collectors.toSet());
    }

    /**
     * Return the next header line, excluding the ones starting with any of the fields to filter
     * @return The following not filtered header line
     */
    protected String advance() {
        String line = super.advance();
        while (line != null && shouldExcludeLine(line)) {
            line = super.advance();
        }
        return line;
    }

    private boolean shouldExcludeLine(String line) {
        return fieldsToExclude.stream().anyMatch(line::startsWith);
    }
}
