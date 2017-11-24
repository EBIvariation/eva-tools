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

public class AssemblyReportParser {

    private FlatFileItemReader<String> reader;

    private String file;

    private Map<String, String> contigMap;

    public AssemblyReportParser(String url) {
        this.file = url;
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
                addContigSynonymIfAssemblyIsIdentical(line, contigMap);
            }
        }
        return contigMap;
    }

    private void addContigSynonymIfAssemblyIsIdentical(String line, Map<String, String> contigMap) {
        int genbankColumn = 4;
        int relationshipColumn = 5;
        int refseqColumn = 6;
        String identicalSequence = "=";
        String[] columns = line.split("\t", -1);
        if (columns[relationshipColumn].equals(identicalSequence)) {
            contigMap.put(columns[refseqColumn], columns[genbankColumn]);
        }
    }
}
