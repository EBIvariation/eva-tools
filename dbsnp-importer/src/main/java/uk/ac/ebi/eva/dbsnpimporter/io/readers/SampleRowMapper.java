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
package uk.ac.ebi.eva.dbsnpimporter.io.readers;

import org.springframework.jdbc.core.RowMapper;

import uk.ac.ebi.eva.commons.core.models.pedigree.Sex;
import uk.ac.ebi.eva.dbsnpimporter.models.Sample;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Maps the database fields that correspond to an individual, which belongs to a batch and a population
 */
public class SampleRowMapper implements RowMapper<Sample> {

    public static final String HANDLE = "handle";

    public static final String BATCH_ID = "batch_id";

    public static final String BATCH_NAME = "batch_name";

    public static final String INDIVIDUAL_NAME = "individual_name";

    public static final String POPULATION = "population";

    public static final String INDIVIDUAL_ID = "individual_id";

    public static final String SUBMITTED_INDIVIDUAL_ID = "submitted_individual_id";

    public static final String FATHER_ID = "father_id";

    public static final String MOTHER_ID = "mother_id";

    public static final String SEX = "sex";

    @Override
    public Sample mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        HashMap<String, String> cohorts = new HashMap<>();
        cohorts.put(POPULATION, resultSet.getString(POPULATION));

        return new Sample(resultSet.getString(BATCH_NAME),
                          resultSet.getString(INDIVIDUAL_NAME),
                          getSex(resultSet.getString(SEX)),
                          resultSet.getString(FATHER_ID),
                          resultSet.getString(MOTHER_ID),
                          cohorts);
    }

    private static Sex getSex(String sexString) {
        Character sex = sexString != null ? sexString.charAt(0) : null;

        if (sex == null) {
            return Sex.UNKNOWN_SEX;
        } else if (Character.toUpperCase(sex) == 'M') {
            return Sex.MALE;
        } else if (Character.toUpperCase(sex) == 'F') {
            return Sex.FEMALE;
        } else {
            throw new IllegalArgumentException("Sex must be 'M', 'F', or null");
        }
    }
}
