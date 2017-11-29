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

import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementSetter;

import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import javax.sql.DataSource;

import static uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpCoreFieldsRowMapper.LOAD_ORDER_COLUMN;

/**
    SELECT
        *
    FROM
        dbsnp_variant_load_$assembly
    WHERE
        batch_id = $batch
    ORDER BY load_order;
 */
public class SubSnpCoreFieldsReader extends JdbcCursorItemReader<SubSnpCoreFields> {

    public SubSnpCoreFieldsReader(int batch, String assembly, DataSource dataSource) throws Exception {
        setDataSource(dataSource);
        setSql(buildSql(assembly));
        setPreparedStatementSetter(buildPreparedStatementSetter(batch));
        setRowMapper(new SubSnpCoreFieldsRowMapper());
    }

    private String buildSql(String assembly) throws Exception {
        String sql =
                "SELECT *" +
                " FROM " +
                        "dbsnp_variant_load_tair10" +
//                        "dbsnp_variant_load_" + assembly +
                " WHERE " +
                        "batch_id = ? " +
                " ORDER BY " +
                        LOAD_ORDER_COLUMN;

        return sql;
    }

    private PreparedStatementSetter buildPreparedStatementSetter(int batch) {
        PreparedStatementSetter preparedStatementSetter = new ArgumentPreparedStatementSetter(
                new Object[]{batch}
        );
        return preparedStatementSetter;
    }
}
