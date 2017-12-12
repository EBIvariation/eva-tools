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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.core.models.pedigree.Sex;
import uk.ac.ebi.eva.dbsnpimporter.models.Sample;
import uk.ac.ebi.eva.dbsnpimporter.test.DbsnpTestDatasource;
import uk.ac.ebi.eva.dbsnpimporter.test.configuration.TestConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@TestPropertySource({"classpath:application.properties"})
@ContextConfiguration(classes = {TestConfiguration.class})
public class SampleReaderTest extends ReaderTest {

    private static final String CHICKEN_ASSEMBLY_4 = "Gallus_gallus-4.0";

    private static final String CHICKEN_ASSEMBLY_5 = "Gallus_gallus-5.0";

    private static final String PRIMARY_ASSEMBLY = "Primary_Assembly";

    private static final String NON_NUCLEAR = "non-nuclear";

    private static final int PAGE_SIZE = 2000;

    public static final int BATCH_ID = 12070;

    public static final String BATCH_NAME = "CHICKEN_SNPS_BROILER";

    public static final String FIRST_INDIVIDUAL_NAME = "RJF";

    public static final String SECOND_INDIVIDUAL_NAME = "BROILER";

    public static final int DBSNP_BUILD = 150;

    @Autowired
    private DbsnpTestDatasource dbsnpTestDatasource;

    private DataSource dataSource;

    private SampleReader reader;

    private List<Sample> expectedSamples;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        dataSource = dbsnpTestDatasource.getDatasource();
        reader = buildReader(DBSNP_BUILD, BATCH_ID, CHICKEN_ASSEMBLY_5, Collections.singletonList(PRIMARY_ASSEMBLY),
                             PAGE_SIZE);

        Map<String, String> cohorts = new HashMap<>();
        cohorts.put(SampleRowMapper.POPULATION, "RBLS");

        expectedSamples = new ArrayList<>();
        expectedSamples.add(new Sample(BATCH_NAME, FIRST_INDIVIDUAL_NAME, Sex.MALE, null, null, cohorts));
        expectedSamples.add(new Sample(BATCH_NAME, SECOND_INDIVIDUAL_NAME, Sex.UNKNOWN_SEX, null, null, cohorts));
    }

    private SampleReader buildReader(int dbsnpBuild, int batch, String assembly, List<String> assemblyTypes,
                                     int pageSize) throws Exception {
        SampleReader fieldsReader = new SampleReader(dbsnpBuild, batch, assembly, assemblyTypes, dataSource, pageSize);
        fieldsReader.afterPropertiesSet();
        ExecutionContext executionContext = new ExecutionContext();
        fieldsReader.open(executionContext);
        return fieldsReader;
    }

    @After
    public void tearDown() throws Exception {
        reader.close();
    }

    @Test
    public void testLoadData() {
        assertNotNull(reader);
        assertEquals(PAGE_SIZE, reader.getPageSize());
    }

    @Test
    public void testQuery() throws Exception {
        List<Sample> list = readAll(reader);
        assertEquals(2, list.size());
        for (Sample sample : list) {
            assertNotNull(sample);
            assertTrue("Retrieved an unexpected sample: " + sample.toString(), expectedSamples.contains(sample));
        }
    }

    @Test
    public void testQueryWithDifferentRelease() throws Exception {
        int dbsnpBuild = 130;

        exception.expect(RuntimeException.class);
        buildReader(dbsnpBuild, BATCH_ID, CHICKEN_ASSEMBLY_5, Collections.singletonList(PRIMARY_ASSEMBLY), PAGE_SIZE);
    }

    @Test
    public void testQueryWithDifferentAssembly() throws Exception {
        exception.expect(NoSuchElementException.class);
        buildReader(DBSNP_BUILD, BATCH_ID, CHICKEN_ASSEMBLY_4, Collections.singletonList(PRIMARY_ASSEMBLY), PAGE_SIZE);
    }

    @Test
    public void testQueryWithDifferentAssemblyType() throws Exception {
        exception.expect(NoSuchElementException.class);
        buildReader(DBSNP_BUILD, BATCH_ID, CHICKEN_ASSEMBLY_5, Collections.singletonList(NON_NUCLEAR), PAGE_SIZE);
    }

    @Test
    public void testQueryWithDifferentBatch() throws Exception {
        int nonExistingBatchId = -1;

        exception.expect(NoSuchElementException.class);
        buildReader(DBSNP_BUILD, nonExistingBatchId, CHICKEN_ASSEMBLY_5, Collections.singletonList(PRIMARY_ASSEMBLY),
                    PAGE_SIZE);
    }

}
