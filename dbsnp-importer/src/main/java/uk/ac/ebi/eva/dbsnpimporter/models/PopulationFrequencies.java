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

import java.util.List;

public class PopulationFrequencies {

    private long populationId;

    private String populationName;

    private List<AlleleFrequency> alleleFrequencies;

    public PopulationFrequencies(long populationId, String populationName, List<AlleleFrequency> alleleFrequencies) {
        this.populationId = populationId;
        this.populationName = populationName;
        this.alleleFrequencies = alleleFrequencies;
    }

    public long getPopulationId() {
        return populationId;
    }

    public void setPopulationId(long populationId) {
        this.populationId = populationId;
    }

    public String getPopulationName() {
        return populationName;
    }

    public void setPopulationName(String populationName) {
        this.populationName = populationName;
    }

    public List<AlleleFrequency> getAlleleFrequencies() {
        return alleleFrequencies;
    }

    public void setAlleleFrequencies(List<AlleleFrequency> alleleFrequencies) {
        this.alleleFrequencies = alleleFrequencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PopulationFrequencies that = (PopulationFrequencies) o;

        if (populationId != that.populationId) return false;
        if (!populationName.equals(that.populationName)) return false;
        return alleleFrequencies.equals(that.alleleFrequencies);
    }

    @Override
    public int hashCode() {
        int result = (int) (populationId ^ (populationId >>> 32));
        result = 31 * result + populationName.hashCode();
        result = 31 * result + alleleFrequencies.hashCode();
        return result;
    }
}
