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

public class PopulationFrequenciesList {

    private List<PopulationFrequencies> populationFrequenciesList;

    public PopulationFrequenciesList(List<PopulationFrequencies> populationFrequenciesList) {
        this.populationFrequenciesList = populationFrequenciesList;
    }

    public List<PopulationFrequencies> getPopulationFrequenciesList() {
        return populationFrequenciesList;
    }

    public void setPopulationFrequenciesList(List<PopulationFrequencies> populationFrequenciesList) {
        this.populationFrequenciesList = populationFrequenciesList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PopulationFrequenciesList that = (PopulationFrequenciesList) o;

        return populationFrequenciesList.equals(that.populationFrequenciesList);
    }

    @Override
    public int hashCode() {
        return populationFrequenciesList.hashCode();
    }
}
