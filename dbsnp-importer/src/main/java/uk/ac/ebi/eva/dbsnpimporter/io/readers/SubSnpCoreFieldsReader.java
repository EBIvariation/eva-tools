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

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementSetter;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static uk.ac.ebi.eva.dbsnpimporter.io.readers.SubSnpCoreFieldsRowMapper.*;

/**
    SELECT
        *
    FROM
        dbsnp_variant_load_$assembly_hash
    WHERE
        batch_id = $batch
    ORDER BY load_order;
 */
public class SubSnpCoreFieldsReader extends JdbcCursorItemReader<SubSnpCoreFields> {

    private static final Logger logger = LoggerFactory.getLogger(SubSnpCoreFieldsReader.class);

    public SubSnpCoreFieldsReader(int batch, String assembly, DataSource dataSource, int pageSize) throws Exception {
        setDataSource(dataSource);
        setSql(buildSql(assembly));
        setPreparedStatementSetter(buildPreparedStatementSetter(batch));
        setRowMapper(new SubSnpCoreFieldsRowMapper());
        setFetchSize(pageSize);
    }

    @Override
    protected void openCursor(Connection connection) {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set autocommit=false", e);
        }
        super.openCursor(connection);
    }

    private String buildSql(String assembly) throws Exception {
        String tableName = "dbsnp_variant_load_" + hash(assembly);
        logger.debug("querying table {} for assembly {}", tableName, assembly);
        String sql =
                "SELECT " +
                        SUBSNP_ID_COLUMN +
                        "," + SUBSNP_ORIENTATION_COLUMN +
                        "," + REFSNP_ID_COLUMN +
                        "," + SNP_ORIENTATION_COLUMN +
                        "," + CONTIG_NAME_COLUMN +
                        "," + CONTIG_START_COLUMN +
                        "," + CONTIG_END_COLUMN +
                        "," + CONTIG_ORIENTATION_COLUMN +
                        "," + LOC_TYPE_COLUMN +
                        "," + CHROMOSOME_COLUMN +
                        "," + CHROMOSOME_START_COLUMN +
                        "," + CHROMOSOME_END_COLUMN +
                        "," + REFERENCE_C +
                        "," + REFERENCE_T +
                        "," + ALTERNATE +
                        "," + ALLELES +
                        "," + HGVS_C_STRING +
                        "," + HGVS_C_START +
                        "," + HGVS_C_STOP +
                        "," + HGVS_C_ORIENTATION +
                        "," + HGVS_T_STRING +
                        "," + HGVS_T_START +
                        "," + HGVS_T_STOP +
                        "," + HGVS_T_ORIENTATION +
                        "," + GENOTYPES_COLUMN +
                        "," + BATCH_COLUMN +
                        " FROM " + tableName +
                        " WHERE batch_id = ? " +
                        " ORDER BY " + LOAD_ORDER_COLUMN;

        return sql;
    }

    String hash(String string) {
        return DigestUtils.md5Hex(string);
    }

    private PreparedStatementSetter buildPreparedStatementSetter(int batch) {
        PreparedStatementSetter preparedStatementSetter = new ArgumentPreparedStatementSetter(
                new Object[]{batch}
        );
        return preparedStatementSetter;
    }
}
