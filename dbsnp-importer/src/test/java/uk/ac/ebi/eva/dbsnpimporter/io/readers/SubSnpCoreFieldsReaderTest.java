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

import uk.ac.ebi.eva.dbsnpimporter.models.LocusType;
import uk.ac.ebi.eva.dbsnpimporter.models.Orientation;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;
import uk.ac.ebi.eva.dbsnpimporter.test.DbsnpTestDatasource;
import uk.ac.ebi.eva.dbsnpimporter.test.configuration.MongoTestConfiguration;
import uk.ac.ebi.eva.dbsnpimporter.test.configuration.TestConfiguration;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static uk.ac.ebi.eva.dbsnpimporter.test.TestUtils.assertContains;

@RunWith(SpringRunner.class)
@TestPropertySource({"classpath:application.properties"})
@ContextConfiguration(classes = {MongoTestConfiguration.class, TestConfiguration.class})
public class SubSnpCoreFieldsReaderTest extends ReaderTest {

    private static final String CHICKEN_ASSEMBLY_4 = "Gallus_gallus-4.0";

    private static final String CHICKEN_ASSEMBLY_5 = "Gallus_gallus-5.0";

    private static final String PRIMARY_ASSEMBLY = "Primary_Assembly";

    private static final String NON_NUCLEAR = "non-nuclear";

    private static final int BATCH_1 = 11825;

    private static final int BATCH_2 = 11828;

    private static final int BATCH_3 = 11831;

    private static final int BATCH_4 = 1061908;

    private static final String BATCH_NAME_1 = "CHICKEN_SNPS_BROILER";

    private static final String BATCH_NAME_2 = "CHICKEN_SNPS_LAYER";

    private static final String BATCH_NAME_3 = "CHICKEN_SNPS_SILKIE";

    private static final int DBSNP_BUILD = 150;

    private DataSource dataSource;

    @Autowired
    private DbsnpTestDatasource dbsnpTestDatasource;

    private SubSnpCoreFieldsReader reader;

    private List<SubSnpCoreFields> expectedSubsnpsBatch2;

    private List<SubSnpCoreFields> expectedSubsnpsBatch3;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        dataSource = dbsnpTestDatasource.getDatasource();
        expectedSubsnpsBatch2 = new ArrayList<>();

        // 3 multiallelic ss clustered under one rs
        expectedSubsnpsBatch2.add(new SubSnpCoreFields(26201546, Orientation.FORWARD,
                                                       13677177L, Orientation.FORWARD,
                                                       "NT_455866.1",
                                                       1766472L,
                                                       1766472L,
                                                       Orientation.FORWARD,
                                                       LocusType.SNP,
                                                       "4",
                                                       91223961L,
                                                       91223961L,
                                                       "T", "T", "A", "T/A",
                                                       "NC_006091.4:g.91223961T>A", 91223961L, 91223961L, Orientation.FORWARD,
                                                       "NT_455866.1:g.1766472T>A", 1766472L, 1766472L, Orientation.FORWARD,
                                                       null,
                                                       BATCH_NAME_2));
        expectedSubsnpsBatch2.add(new SubSnpCoreFields(26201546, Orientation.FORWARD,
                                                       13677177L, Orientation.FORWARD,
                                                       "NT_455866.1",
                                                       1766472L,
                                                       1766472L,
                                                       Orientation.FORWARD,
                                                       LocusType.SNP,
                                                       "4",
                                                       91223961L,
                                                       91223961L,
                                                       "T", "T", "C", "T/A",
                                                       "NC_006091.4:g.91223961T>C", 91223961L, 91223961L, Orientation.FORWARD,
                                                       "NT_455866.1:g.1766472T>C", 1766472L, 1766472L, Orientation.FORWARD,
                                                       null,
                                                       BATCH_NAME_2));


        expectedSubsnpsBatch3 = new ArrayList<>();
        expectedSubsnpsBatch3.add(new SubSnpCoreFields(26954817, Orientation.REVERSE,
                                                       13677177L, Orientation.FORWARD,
                                                       "NT_455866.1",
                                                       1766472L,
                                                       1766472L,
                                                       Orientation.FORWARD,
                                                       LocusType.SNP,
                                                       "4",
                                                       91223961L,
                                                       91223961L,
                                                       "T", "T", "A", "G/A",
                                                       "NC_006091.4:g.91223961T>A", 91223961L, 91223961L, Orientation.FORWARD,
                                                       "NT_455866.1:g.1766472T>A", 1766472L, 1766472L, Orientation.FORWARD,
                                                       "A/A,G/G",
                                                       BATCH_NAME_3));
        expectedSubsnpsBatch3.add(new SubSnpCoreFields(26954817, Orientation.REVERSE,
                                                       13677177L, Orientation.FORWARD,
                                                       "NT_455866.1",
                                                       1766472L,
                                                       1766472L,
                                                       Orientation.FORWARD,
                                                       LocusType.SNP,
                                                       "4",
                                                       91223961L,
                                                       91223961L,
                                                       "T", "T", "C", "G/A",
                                                       "NC_006091.4:g.91223961T>C", 91223961L, 91223961L, Orientation.FORWARD,
                                                       "NT_455866.1:g.1766472T>C", 1766472L, 1766472L, Orientation.FORWARD,
                                                        "A/A,G/G",
                                                       BATCH_NAME_3));
        expectedSubsnpsBatch3.add(new SubSnpCoreFields(26963037, Orientation.FORWARD,
                                                       13677177L, Orientation.FORWARD,
                                                       "NT_455866.1",
                                                       1766472L,
                                                       1766472L,
                                                       Orientation.FORWARD,
                                                       LocusType.SNP,
                                                       "4",
                                                       91223961L,
                                                       91223961L,
                                                       "T", "T", "A", "T/A",
                                                       "NC_006091.4:g.91223961T>A", 91223961L, 91223961L, Orientation.FORWARD,
                                                       "NT_455866.1:g.1766472T>A", 1766472L, 1766472L, Orientation.FORWARD,
                                                       "A/A,T/T",
                                                       BATCH_NAME_3));
        expectedSubsnpsBatch3.add(new SubSnpCoreFields(26963037, Orientation.FORWARD,
                                                       13677177L, Orientation.FORWARD,
                                                       "NT_455866.1",
                                                       1766472L,
                                                       1766472L,
                                                       Orientation.FORWARD,
                                                       LocusType.SNP,
                                                       "4",
                                                       91223961L,
                                                       91223961L,
                                                       "T", "T", "C", "T/A",
                                                       "NC_006091.4:g.91223961T>C", 91223961L, 91223961L, Orientation.FORWARD,
                                                       "NT_455866.1:g.1766472T>C", 1766472L, 1766472L, Orientation.FORWARD,
                                                       "A/A,T/T",
                                                       BATCH_NAME_3));
    }

    private SubSnpCoreFieldsReader buildReader(int batch, String assembly, int pageSize)
            throws Exception {
        SubSnpCoreFieldsReader fieldsReader = new SubSnpCoreFieldsReader(batch, assembly, dataSource, pageSize);
        fieldsReader.afterPropertiesSet();
        ExecutionContext executionContext = new ExecutionContext();
        fieldsReader.open(executionContext);
        return fieldsReader;
    }

    @After
    public void tearDown() throws Exception {
        if (reader != null) {
            reader.close();
        }
    }

    @Test
    public void testHash() throws Exception {
        reader = buildReader(BATCH_1, CHICKEN_ASSEMBLY_5, 100);
        assertEquals("d8c757988871529f37061fa9c79477a5", reader.hash(CHICKEN_ASSEMBLY_5));
    }

    @Test
    public void testLoadData() throws Exception {
        reader = buildReader(BATCH_1, CHICKEN_ASSEMBLY_5, 100);
        assertNotNull(reader);
    }

    @Test
    public void testQuery() throws Exception {
        reader = buildReader(BATCH_3, CHICKEN_ASSEMBLY_5, 100);
        List<SubSnpCoreFields> readSnps = readAll(reader);

        assertEquals(4, readSnps.size());
        for (SubSnpCoreFields expectedSnp : expectedSubsnpsBatch3) {
            assertContains(readSnps, expectedSnp);
        }
    }

    @Test
    public void testSnpOrientations() throws Exception {
        reader = buildReader(BATCH_1, CHICKEN_ASSEMBLY_5, 100);
        List<SubSnpCoreFields> readSnps = readAll(reader);
        reader = buildReader(BATCH_4, CHICKEN_ASSEMBLY_5, 100);
        List<SubSnpCoreFields> readSnps2 = readAll(reader);

        // check all possible orientation combinations
        checkSnpOrientation(readSnps, 13511401L, Orientation.FORWARD, Orientation.FORWARD);
        checkSnpOrientation(readSnps, 3136864L, Orientation.FORWARD, Orientation.REVERSE);
        checkSnpOrientation(readSnps, 13713751L, Orientation.REVERSE, Orientation.FORWARD);
        checkSnpOrientation(readSnps2, 733889725L, Orientation.REVERSE, Orientation.REVERSE);
    }

    private void checkSnpOrientation(List<SubSnpCoreFields> readSnps, Long snpId, Orientation snpOrientation,
                                     Orientation contigOrientation) {
        Optional<SubSnpCoreFields> snp = readSnps.stream().filter(s -> s.getRsId().equals(snpId)).findAny();
        assertTrue(snp.isPresent());
        assertEquals(snpOrientation, snp.get().getSnpOrientation());
        assertEquals(contigOrientation, snp.get().getContigOrientation());
    }

    @Test
    public void testQueryWithDifferentAssembly() throws Exception {
        // snp with coordinates in a not default assembly
        List<SubSnpCoreFields> snpsInDifferentAssembly = new ArrayList<>();
        snpsInDifferentAssembly.add(new SubSnpCoreFields(1L, Orientation.FORWARD,
                                                         1L, Orientation.REVERSE,
                                                         "NT_455837.1",
                                                         11724980L,
                                                         11724983L,
                                                         Orientation.REVERSE,
                                                         LocusType.DELETION,
                                                         "3",
                                                         47119827L,
                                                         47119830L,
                                                         "TCGG", "TCGG", null, "TCGG/-",
                                                         "NC_006090.4:g.47119827_47119830delTCGG",
                                                         47119827L, 47119830L, Orientation.FORWARD,
                                                         "NT_455837.1:g.11724980_11724983delCCGA",
                                                         11724980L, 11724983L, Orientation.REVERSE,
                                                         null,
                                                         "CHICKEN_INDEL_DWBURT"));
        int fakeBatch = 1;
        reader = buildReader(fakeBatch, CHICKEN_ASSEMBLY_4, 100);
        List<SubSnpCoreFields> list = readAll(reader);

        assertEquals(1, list.size());
        assertEquals(snpsInDifferentAssembly, list);
    }

    @Test
    public void testQueryWithDifferentBatch() throws Exception {
        reader = buildReader(BATCH_2, CHICKEN_ASSEMBLY_5, 100);
        List<SubSnpCoreFields> list = readAll(reader);
        assertEquals(4, list.size());
    }

    @Test
    public void testQueryWithNonExistingBatch() throws Exception {
        int nonExistingBatch = 42;
        reader = buildReader(nonExistingBatch, CHICKEN_ASSEMBLY_5, 100);
        List<SubSnpCoreFields> list = readAll(reader);
        assertEquals(0, list.size());
    }
}
