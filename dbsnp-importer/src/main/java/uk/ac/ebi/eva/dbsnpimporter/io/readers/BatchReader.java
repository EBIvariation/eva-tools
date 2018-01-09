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
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SingleColumnRowMapper;

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

    private int batch;

    private JdbcCursorItemReader<String> batchNameReader;

    private ItemStreamReader<List<Sample>> samplesReader;

    private boolean alreadyConsumed;

    public BatchReader(int batch, DataSource dataSource, int pageSize) throws Exception {
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }

        this.batch = batch;

        batchNameReader = new JdbcCursorItemReader<>();
        batchNameReader.setDataSource(dataSource);
        batchNameReader.setSql(buildSql());
        batchNameReader.setPreparedStatementSetter(buildPreparedStatementSetter());
        batchNameReader.setRowMapper(new SingleColumnRowMapper<>());
        batchNameReader.setFetchSize(pageSize);
        batchNameReader.afterPropertiesSet();

        SampleReader sampleReader = new SampleReader(batch, dataSource, pageSize);
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
                        + "    batch.loc_batch_id_upp AS " + SampleRowMapper.BATCH_NAME
                        + " FROM"
                        + "    batch"
                        + " WHERE"
                        + "    batch.batch_id = ?";
        return sql;
    }

    private PreparedStatementSetter buildPreparedStatementSetter() throws Exception {
        PreparedStatementSetter preparedStatementSetter = new ArgumentPreparedStatementSetter(
                new Object[]{
                        batch
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
            String batchName = batchNameReader.read();
            List<Sample> samples = samplesReader.read();
            return new DbsnpBatch(batch, batchName, samples);
        }
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        batchNameReader.open(executionContext);
        samplesReader.open(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        batchNameReader.close();
        samplesReader.close();
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        batchNameReader.update(executionContext);
        samplesReader.update(executionContext);
    }
}
