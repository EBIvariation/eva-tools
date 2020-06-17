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

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementSetter;

import uk.ac.ebi.eva.dbsnpimporter.models.DbsnpBatch;
import uk.ac.ebi.eva.dbsnpimporter.models.Sample;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


/**
 Gets the information of a dbSNP batch (equivalent to our VariantSource).

 The batch information consists on a batchId, batchName and a list of samples (possibly an empty list).

 To retrieve the list of samples, a WindingItemStreamReader is used (fed with a SampleReader) and, the query to
 retrieve the batch name is done independently as:

    SELECT
       batch.loc_batch_id_upp AS batch_name
    FROM
       batch
    WHERE
       batch.batch_id = ?
 */
public class BatchReader extends JdbcCursorItemReader<DbsnpBatch> {

    private int batchId;

    private JdbcCursorItemReader<DbsnpBatch> batchReader;

    private ItemStreamReader<List<Sample>> samplesReader;

    private boolean alreadyConsumed;

    public BatchReader(int batchId, DataSource dataSource, int pageSize) throws Exception {
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }

        this.batchId = batchId;

        batchReader = new JdbcCursorItemReader<>();
        batchReader.setDataSource(dataSource);
        batchReader.setSql(buildSql());
        batchReader.setPreparedStatementSetter(buildPreparedStatementSetter());
        batchReader.setRowMapper(new BatchRowMapper());
        batchReader.setFetchSize(pageSize);
        batchReader.afterPropertiesSet();

        SampleReader sampleReader = new SampleReader(batchId, dataSource, pageSize);
        sampleReader.afterPropertiesSet();
        samplesReader = new WindingItemStreamReader<>(sampleReader);

        alreadyConsumed = false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
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

    private String buildSql() {
        String sql =
                "SELECT"
                        + "    batch.batch_id  AS " + BatchRowMapper.BATCH_ID
                        + "   ,batch.handle AS " + BatchRowMapper.HANDLE
                        + "   ,batch.loc_batch_id_upp AS " + BatchRowMapper.BATCH_NAME
                        + " FROM"
                        + "    batch"
                        + " WHERE"
                        + "    batch.batch_id = ?";
        return sql;
    }

    private PreparedStatementSetter buildPreparedStatementSetter() throws Exception {
        PreparedStatementSetter preparedStatementSetter = new ArgumentPreparedStatementSetter(
                new Object[]{
                        batchId
                }
        );
        return preparedStatementSetter;
    }

    @Override
    protected DbsnpBatch doRead() throws Exception {
        if (alreadyConsumed) {
            return null;
        } else {
            alreadyConsumed = true;

            DbsnpBatch batch = batchReader.read();
            if (batch == null) {
                throw new IllegalArgumentException("Batch " + batchId + " does not exist");
            }

            batch.setSamples(samplesReader.read());

            return batch;
        }
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        batchReader.open(executionContext);
        samplesReader.open(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        if (this.getDataSource() != null) {
            batchReader.close();
            samplesReader.close();
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        batchReader.update(executionContext);
        samplesReader.update(executionContext);
    }
}
