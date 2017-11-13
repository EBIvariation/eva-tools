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


import uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpGenotypeRowMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Representation for Genotypes associated with a given SubSNP
 */
public class SubSnpGenotype {

    private int batchId;

    private String studyId;

    private long ssId;

    private List<String> genotypes;

    /**
     * @param batchId           Unique Batch identifier (maps to "fid" attribute in
     *                          files sub-document under the variants document in Mongo)
     * @param studyId        Unique study identifier (maps to "sid" attribute in
     *                          files sub-document under the variants document in Mongo)
     * @param subSnpId          Unique SS ID identifier
     * @param genotypes         Genotypes associated with the SS
     */
    public SubSnpGenotype(int batchId, String studyId, long subSnpId, String genotypes) {
        this.batchId = batchId;
        this.studyId = studyId;
        this.ssId = subSnpId;
        if (genotypes == null || genotypes.trim().equals("")) {
            this.genotypes = new ArrayList<>();
        } else {
            this.genotypes = Arrays.stream(genotypes.split(SubSnpGenotypeRowMapper.GENOTYPE_DELIMITER))
                    .map(String::trim).collect(Collectors.toList());
        }
    }

    public long getBatchId() {
        return batchId;
    }

    public String getStudyId() {
        return studyId;
    }

    public long getSsId() {
        return ssId;
    }

    public List<String> getGenotypes() {
        return genotypes;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubSnpGenotype that = (SubSnpGenotype) o;

        if (batchId != that.batchId) return false;
        if (ssId != that.ssId) return false;
        if (studyId != null ? !studyId.equals(that.studyId) : that.studyId != null) return false;
        return genotypes != null ? genotypes.equals(that.genotypes) : that.genotypes == null;
    }

    @Override
    public int hashCode() {
        int result = batchId;
        result = 31 * result + (studyId != null ? studyId.hashCode() : 0);
        result = 31 * result + (int) (ssId ^ (ssId >>> 32));
        result = 31 * result + (genotypes != null ? genotypes.hashCode() : 0);
        return result;
    }
}
