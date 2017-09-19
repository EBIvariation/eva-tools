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

import uk.ac.ebi.eva.commons.core.models.pedigree.Sex;

import java.util.Map;

/**
 * Bean to represent an individual, his parents and the cohorts he belongs to. This class is related to
 * uk.ac.ebi.eva.commons.core.models.pedigree.Individual
 */
public class Sample {

    private String id;

    private Sex sex;

    private String father;

    private String mother;

    private Map<String, String> cohorts;

    public Sample(String id, Sex sex, String father, String mother, Map<String, String> cohorts) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID must be a non-empty string");
        }
        if (sex == null) {
            throw new IllegalArgumentException("Sex must not be null");
        }
        this.id = id;
        this.sex = sex;
        this.father = father;
        this.mother = mother;
        this.cohorts = cohorts;
    }

    public String getId() {
        return id;
    }

    public Sex getSex() {
        return sex;
    }

    public String getFather() {
        return father;
    }

    public String getMother() {
        return mother;
    }

    public Map<String, String> getCohorts() {
        return cohorts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sample sample = (Sample) o;

        if (!id.equals(sample.id)) return false;
        if (sex != null ? !sex.equals(sample.sex) : sample.sex != null) return false;
        if (father != null ? !father.equals(sample.father) : sample.father != null) return false;
        if (mother != null ? !mother.equals(sample.mother) : sample.mother != null) return false;
        return cohorts != null ? cohorts.equals(sample.cohorts) : sample.cohorts == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (sex != null ? sex.hashCode() : 0);
        result = 31 * result + (father != null ? father.hashCode() : 0);
        result = 31 * result + (mother != null ? mother.hashCode() : 0);
        result = 31 * result + (cohorts != null ? cohorts.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Sample{" +
                "id='" + id + '\'' +
                ", sex=" + sex +
                ", father='" + father + '\'' +
                ", mother='" + mother + '\'' +
                ", cohorts=" + cohorts +
                '}';
    }
}
