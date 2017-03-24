/*
 * Copyright 2016 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.vcfdump.cellbasewsclient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.opencb.cellbase.core.common.core.Chromosome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChromosomesWSOutput {

    private ChromosomesWSResponse chromosomesWSResponse;

    public ChromosomesWSOutput() {
    }

    public ChromosomesWSResponse getChromosomesWSResponse() {
        return chromosomesWSResponse;
    }

    public void setChromosomesWSResponse(
            ChromosomesWSResponse chromosomesWSResponse) {
        this.chromosomesWSResponse = chromosomesWSResponse;
    }

    Set<String> getAllChromosomeNames() {
        return new HashSet<>(chromosomesWSResponse.getChromosomesWSResult().getChromosomes());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private class ChromosomesWSResponse {

        private ChromosomesWSResult chromosomesWSResult;

        public ChromosomesWSResponse() {
        }

        public ChromosomesWSResult getChromosomesWSResult() {
            return chromosomesWSResult;
        }

        public void setChromosomesWSResult(
                ChromosomesWSResult chromosomesWSResult) {
            this.chromosomesWSResult = chromosomesWSResult;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private class ChromosomesWSResult {

        private List<String> chromosomes;

        public ChromosomesWSResult() {
        }

        public List<String> getChromosomes() {
            return Collections.unmodifiableList(chromosomes);
        }

        public void setChromosomes(List<String> chromosomes) {
            this.chromosomes = new ArrayList<>(chromosomes);
        }
    }
}
