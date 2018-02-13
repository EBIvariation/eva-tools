/*
 * Copyright 2018 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.eva.dbsnpimporter.io.readers;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.dbsnpimporter.models.DbsnpBatch;
import uk.ac.ebi.eva.dbsnpimporter.test.DbsnpTestDatasource;
import uk.ac.ebi.eva.dbsnpimporter.test.configuration.TestConfiguration;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@TestPropertySource({"classpath:application.properties"})
@ContextConfiguration(classes = {TestConfiguration.class})
public class BatchReaderTest extends ReaderTest {

    private static final int BATCH = 11825;

    private static final String BATCH_NAME = "CHICKEN_SNPS_BROILER";

    private static final int PAGE_SIZE = 10;

    @Autowired
    private DbsnpTestDatasource dbsnpTestDatasource;

    private BatchReader reader;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @After
    public void tearDown() throws Exception {
        reader.close();
    }

    @Test
    public void testQuery() throws Exception {
        reader = buildReader(BATCH, dbsnpTestDatasource.getDatasource(), PAGE_SIZE);
        List<DbsnpBatch> batches = readAll(reader);

        assertEquals(1, batches.size());
        DbsnpBatch batch = batches.get(0);
        assertEquals(BATCH, batch.getBatchId());
        assertEquals(BATCH_NAME, batch.getBatchName());
        assertEquals(3, batch.getSamples().size());
    }

    @Test
    public void testNotExistingBatch() throws Exception {
        int batchidNotExistingInDB = -9999;
        reader = buildReader(batchidNotExistingInDB, dbsnpTestDatasource.getDatasource(), PAGE_SIZE);

        thrown.expect(IllegalArgumentException.class);
        readAll(reader);
    }

    private BatchReader buildReader(int batch, DataSource dataSource, int pageSize) throws Exception {
        BatchReader fieldsReader = new BatchReader(batch, dataSource, pageSize);
        fieldsReader.afterPropertiesSet();
        ExecutionContext executionContext = new ExecutionContext();
        fieldsReader.open(executionContext);
        return fieldsReader;
    }

}