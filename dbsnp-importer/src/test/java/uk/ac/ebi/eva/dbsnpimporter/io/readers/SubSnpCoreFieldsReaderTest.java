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
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.dbsnpimporter.models.LocusType;
import uk.ac.ebi.eva.dbsnpimporter.models.Orientation;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static uk.ac.ebi.eva.dbsnpimporter.test.TestUtils.assertContains;

@RunWith(SpringRunner.class)
@JdbcTest
public class SubSnpCoreFieldsReaderTest extends ReaderTest {

    private static final String CHICKEN_ASSEMBLY_4 = "Gallus_gallus-4.0";

    private static final String CHICKEN_ASSEMBLY_5 = "Gallus_gallus-5.0";

    private static final String PRIMARY_ASSEMBLY = "Primary_Assembly";

    private static final int PAGE_SIZE = 2000;

    private static final String NON_NUCLEAR = "non-nuclear";

    private static final int BATCH = 11825;

    public static final int DBSNP_BUILD = 150;

    @Autowired
    private DataSource dataSource;

    private SubSnpCoreFieldsReader reader;

    private List<SubSnpCoreFields> expectedSubsnps;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        expectedSubsnps = new ArrayList<>();

        // 3 multiallelic ss clustered under one rs
        expectedSubsnps.add(new SubSnpCoreFields(26201546, 1,
                                                 13677177L, 1,
                                                 "NT_455866.1",
                                                 1766472L,
                                                 1766472L,
                                                 1,
                                                 LocusType.SNP.intValue(),
                                                 "4",
                                                 91223961L,
                                                 91223961L,
                                                 "T", "T", "A", "T/A",
                                                 "NC_006091.4:g.91223961T>A", 91223961L, 91223961L, 1,
                                                 "NT_455866.1:g.1766472T>A", 1766472L, 1766472L, 1));
        expectedSubsnps.add(new SubSnpCoreFields(26201546, 1,
                                                 13677177L, 1,
                                                 "NT_455866.1",
                                                 1766472L,
                                                 1766472L,
                                                 1,
                                                 LocusType.SNP.intValue(),
                                                 "4",
                                                 91223961L,
                                                 91223961L,
                                                 "T", "T", "C", "T/A",
                                                 "NC_006091.4:g.91223961T>C", 91223961L, 91223961L, 1,
                                                 "NT_455866.1:g.1766472T>C", 1766472L, 1766472L, 1));
        expectedSubsnps.add(new SubSnpCoreFields(26954817, -1,
                                                 13677177L, 1,
                                                 "NT_455866.1",
                                                 1766472L,
                                                 1766472L,
                                                 1,
                                                 LocusType.SNP.intValue(),
                                                 "4",
                                                 91223961L,
                                                 91223961L,
                                                 "T", "T", "A", "G/A",
                                                 "NC_006091.4:g.91223961T>A", 91223961L, 91223961L, 1,
                                                 "NT_455866.1:g.1766472T>A", 1766472L, 1766472L, 1));
        expectedSubsnps.add(new SubSnpCoreFields(26963037, 1,
                                                 13677177L, 1,
                                                 "NT_455866.1",
                                                 1766472L,
                                                 1766472L,
                                                 1,
                                                 LocusType.SNP.intValue(),
                                                 "4",
                                                 91223961L,
                                                 91223961L,
                                                 "T", "T", "A", "T/A",
                                                 "NC_006091.4:g.91223961T>A", 91223961L, 91223961L, 1,
                                                 "NT_455866.1:g.1766472T>A", 1766472L, 1766472L, 1));
        expectedSubsnps.add(new SubSnpCoreFields(26963037, 1,
                                                 13677177L, 1,
                                                 "NT_455866.1",
                                                 1766472L,
                                                 1766472L,
                                                 1,
                                                 LocusType.SNP.intValue(),
                                                 "4",
                                                 91223961L,
                                                 91223961L,
                                                 "T", "T", "C", "T/A",
                                                 "NC_006091.4:g.91223961T>C", 91223961L, 91223961L, 1,
                                                 "NT_455866.1:g.1766472T>C", 1766472L, 1766472L, 1));
    }

    private SubSnpCoreFieldsReader buildReader(int dbsnpBuild, int batch, String assembly, List<String> assemblyTypes,
                                               int pageSize) throws Exception {
        SubSnpCoreFieldsReader fieldsReader = new SubSnpCoreFieldsReader(dbsnpBuild, batch, assembly, assemblyTypes,
                                                                         dataSource, pageSize);
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
    public void testLoadData() throws Exception {
        reader = buildReader(DBSNP_BUILD, BATCH, CHICKEN_ASSEMBLY_5, Collections.singletonList(PRIMARY_ASSEMBLY),
                             PAGE_SIZE);
        assertNotNull(reader);
        assertEquals(PAGE_SIZE, reader.getPageSize());
    }

    @Test
    public void testQuery() throws Exception {
        reader = buildReader(DBSNP_BUILD, BATCH, CHICKEN_ASSEMBLY_5, Collections.singletonList(PRIMARY_ASSEMBLY),
                             PAGE_SIZE);
        List<SubSnpCoreFields> readSnps = readAll(reader);

        assertEquals(23, readSnps.size());
        for (SubSnpCoreFields expectedSnp : expectedSubsnps) {
            assertTrue(readSnps.contains(expectedSnp));
        }
        // check all possible orientation combinations
        checkSnpOrientation(readSnps, 13677177L, Orientation.FORWARD, Orientation.FORWARD);
        checkSnpOrientation(readSnps, 1060492716L, Orientation.FORWARD, Orientation.REVERSE);
        checkSnpOrientation(readSnps, 1060492473L, Orientation.REVERSE, Orientation.FORWARD);
        checkSnpOrientation(readSnps, 733889725L, Orientation.REVERSE, Orientation.REVERSE);
    }

    private void checkSnpOrientation(List<SubSnpCoreFields> readSnps, Long snpId, Orientation snpOrientation,
                                     Orientation contigOrientation) {
        Optional<SubSnpCoreFields> snp = readSnps.stream().filter(s -> s.getRsId().equals(snpId)).findAny();
        assertTrue(snp.isPresent());
        assertEquals(snpOrientation, snp.get().getSnpOrientation());
        assertEquals(contigOrientation, snp.get().getContigOrientation());
    }

    @Test
    public void testQueryWithDifferentRelease() throws Exception {
        int dbsnpBuild = 130;
        reader = buildReader(dbsnpBuild, BATCH, CHICKEN_ASSEMBLY_5, Collections.singletonList(PRIMARY_ASSEMBLY),
                             PAGE_SIZE);

        exception.expect(SQLException.class);
        List<SubSnpCoreFields> list = readAll(reader);
    }

    @Test
    public void testQueryWithDifferentAssembly() throws Exception {
        // snp with coordinates in a not default assembly
        List<SubSnpCoreFields> snpsInDifferentAssembly = new ArrayList<>();
        snpsInDifferentAssembly.add(new SubSnpCoreFields(1540359250, 1,
                                                         739617577L, -1,
                                                         "NT_455837.1",
                                                         11724980L,
                                                         11724983L,
                                                         -1,
                                                         LocusType.DELETION.intValue(),
                                                         "3",
                                                         47119827L,
                                                         47119830L,
                                                         "TCGG", "TCGG", null, "TCGG/-",
                                                         "NC_006090.4:g.47119827_47119830delTCGG",
                                                         47119827L, 47119830L, 1,
                                                         "NT_455837.1:g.11724980_11724983delCCGA",
                                                         11724980L, 11724983L, -1));
        reader = buildReader(DBSNP_BUILD, 1062064, CHICKEN_ASSEMBLY_4, Collections.singletonList(PRIMARY_ASSEMBLY),
                             PAGE_SIZE);
        List<SubSnpCoreFields> list = readAll(reader);

        assertEquals(1, list.size());
        assertEquals(snpsInDifferentAssembly, list);
    }

    @Test
    public void testQueryWithDifferentAssemblyType() throws Exception {
        reader = buildReader(DBSNP_BUILD, BATCH, CHICKEN_ASSEMBLY_5, Collections.singletonList(NON_NUCLEAR), PAGE_SIZE);
        List<SubSnpCoreFields> list = readAll(reader);
        assertEquals(0, list.size());
    }

    @Test
    public void testQueryWithDifferentBatch() throws Exception {
        reader = buildReader(DBSNP_BUILD, 1062063, CHICKEN_ASSEMBLY_5, Collections.singletonList(PRIMARY_ASSEMBLY),
                             PAGE_SIZE);
        List<SubSnpCoreFields> list = readAll(reader);
        assertEquals(1, list.size());
    }

    @Test
    public void testQueryWithNonExistingBatch() throws Exception {
        int nonExistingBatch = 42;
        reader = buildReader(DBSNP_BUILD, nonExistingBatch, CHICKEN_ASSEMBLY_5,
                             Collections.singletonList(PRIMARY_ASSEMBLY), PAGE_SIZE);
        List<SubSnpCoreFields> list = readAll(reader);
        assertEquals(0, list.size());
    }
}
