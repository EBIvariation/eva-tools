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

public class AlleleFrequency {

    private String allele;

    /**
     * dbsnp puts this count as real "to preserve precision". see
     * https://www.ncbi.nlm.nih.gov/projects/SNP/snp_db_table_description.cgi?t=AlleleFreqBySsPop
     */
    private double count;

    private double frequency;

    public AlleleFrequency(String allele, double count, double frequency) {
        this.allele = allele;
        this.count = count;
        this.frequency = frequency;
    }

    public String getAllele() {
        return allele;
    }

    public void setAllele(String allele) {
        this.allele = allele;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AlleleFrequency that = (AlleleFrequency) o;

        if (Double.compare(that.count, count) != 0) return false;
        if (Double.compare(that.frequency, frequency) != 0) return false;
        return allele.equals(that.allele);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = allele.hashCode();
        temp = Double.doubleToLongBits(count);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(frequency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
