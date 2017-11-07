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
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpGenotype;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps the database fields that correspond to an SS ID along with the associated genotypes.
 */
public class SubSnpGenotypeRowMapper implements RowMapper<SubSnpGenotype> {

    public static final String BATCH_ID_COLUMN = "batch_id";

    public static final String LOC_BATCH_ID_COLUMN = "loc_batch_id";

    public static final String SUBSNP_ID_COLUMN = "ss_id";

    public static final String GENOTYPES_COLUMN = "genotypes_string";

    public static final String GENOTYPE_DELIMITER = ",";

    /**
     * Maps ResultSet to SubSnpCoreFields.
     *
     * It makes getObject (instead of getInt or getString) for those that are nullable.
     *
     * The casts are safe because the DB types are integers. The types Long and BigDecimal are introduced by the query
     * and it won't change the values more that +-1.
     */
    @Override
    public SubSnpGenotype mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new SubSnpGenotype(resultSet.getInt(BATCH_ID_COLUMN), resultSet.getString(LOC_BATCH_ID_COLUMN),
                resultSet.getLong(SUBSNP_ID_COLUMN), resultSet.getString(GENOTYPES_COLUMN));
    }
}
