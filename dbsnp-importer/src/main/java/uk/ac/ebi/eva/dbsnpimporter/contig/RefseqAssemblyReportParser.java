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
package uk.ac.ebi.eva.dbsnpimporter.contig;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class RefseqAssemblyReportParser {

    private static final int GENBANK_COLUMN = 4;

    private static final int RELATIONSHIP_COLUMN = 5;

    private static final int REFSEQ_COLUMN = 6;

    private static final String IDENTICAL_SEQUENCE = "=";

    private FlatFileItemReader<String> reader;

    private Map<String, String> contigMap;

    public RefseqAssemblyReportParser(String url) {
        this.contigMap = null;
        initializeReader(url);
    }

    private void initializeReader(String url) {
        reader = new FlatFileItemReader<>();
        try {
            reader.setResource(new UrlResource(url));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("File location is invalid: " + url, e);
        }
        reader.setLineMapper(new PassThroughLineMapper());
        reader.open(new ExecutionContext());
    }

    public Map<String, String> getContigMap() throws Exception {
        if (contigMap == null) {
            String line;
            contigMap = new HashMap<>();
            while ((line = reader.read()) != null) {
                addContigSynonym(line, contigMap);
            }
            reader.close();
        }
        return contigMap;
    }

    private void addContigSynonym(String line, Map<String, String> contigMap) {
        String[] columns = line.split("\t", -1);
        if (columns[RELATIONSHIP_COLUMN].equals(IDENTICAL_SEQUENCE)) {
            contigMap.put(columns[REFSEQ_COLUMN], columns[GENBANK_COLUMN]);
        }
    }
}
