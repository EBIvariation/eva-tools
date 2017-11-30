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
import org.junit.Ignore;
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
import java.util.Collections;
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

    private static final String PRIMARY_ASSEMBLY = "Primary_Assembly";

    private static final int BATCH = 856370;

    private static final int BATCH_2 = 84764;

    private static final int DBSNP_BUILD = 150;

    private static final String HORSE_ASSEMBLY = "EquCab2.0";

    private static final String HORSE_ASSEMBLY_2 = "EquCab42.0";

    private static final String HORSE_ASSEMBLY_HASH = "6b5b53a74e4c0aa493ceeb6e75c72436";

    private DataSource dataSource;

    @Autowired
    private DbsnpTestDatasource dbsnpTestDatasource;

    private SubSnpCoreFieldsReader reader;

    private List<SubSnpCoreFields> expectedSubsnps;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        dataSource = dbsnpTestDatasource.getDatasource();
        expectedSubsnps = new ArrayList<>();

        // simple variant
        expectedSubsnps.add(new SubSnpCoreFields(105428809, Orientation.FORWARD,
                                                 68447105L, Orientation.FORWARD,
                                                 "NW_001867405.1",
                                                 37435034L,
                                                 37435034L,
                                                 Orientation.FORWARD,
                                                 LocusType.SNP,
                                                 "2",
                                                 111039326L,
                                                 111039326L,
                                                 "T", "T", "A", "A/T",
                                                 "NC_009145.2:g.111039326T>A",
                                                 111039326L,111039326L, Orientation.FORWARD,
                                                 "NW_001867405.1:g.37435035T>A",
                                                 37435035L,37435035L, Orientation.FORWARD,
                                                 "BROAD_EQUCAB2.0:2008.08.08"));
    }

    private SubSnpCoreFieldsReader buildReader(int dbsnpBuild, int batch, String assembly, List<String> assemblyTypes)
            throws Exception {
        int pageSize = 10;
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
        reader = buildReader(DBSNP_BUILD, BATCH, HORSE_ASSEMBLY, Collections.singletonList(PRIMARY_ASSEMBLY));
        assertEquals(HORSE_ASSEMBLY_HASH, reader.hash(HORSE_ASSEMBLY));
    }

    @Test
    public void testLoadData() throws Exception {
        reader = buildReader(DBSNP_BUILD, BATCH, HORSE_ASSEMBLY, Collections.singletonList(PRIMARY_ASSEMBLY));
        assertNotNull(reader);
    }

    @Test
    public void testBasicQuery() throws Exception {
        reader = buildReader(DBSNP_BUILD, BATCH, HORSE_ASSEMBLY, Collections.singletonList(PRIMARY_ASSEMBLY));
        List<SubSnpCoreFields> readSnps = readAll(reader);

        assertEquals(163, readSnps.size());
        for (SubSnpCoreFields expectedSnp : expectedSubsnps) {
            assertContains(readSnps, expectedSnp);
        }
    }

    @Ignore("TODO get more test data with some reverse orientations")
    public void testOrientations() throws Exception {
        reader = buildReader(DBSNP_BUILD, BATCH, HORSE_ASSEMBLY, Collections.singletonList(PRIMARY_ASSEMBLY));
        List<SubSnpCoreFields> readSnps = readAll(reader);
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
    public void testQueryWithDifferentAssembly() throws Exception {
        List<SubSnpCoreFields> snpsInDifferentAssembly = new ArrayList<>();
        snpsInDifferentAssembly.add(new SubSnpCoreFields(552, Orientation.FORWARD,
                                                         252L, Orientation.FORWARD,
                                                         "NW_001867405.1",
                                                         37435034L,
                                                         37435034L,
                                                         Orientation.FORWARD,
                                                         LocusType.SNP,
                                                         "2",
                                                         111039326L,
                                                         111039326L,
                                                         "T", "T", "A", "A/T",
                                                         "NC_009145.2:g.111039326T>A",
                                                         111039326L,111039326L, Orientation.FORWARD,
                                                         "NW_001867405.1:g.37435035T>A",
                                                         37435035L,37435035L, Orientation.FORWARD,
                                                         "BROAD_EQUCAB2.0:2008.08.08"));
        reader = buildReader(DBSNP_BUILD, BATCH, HORSE_ASSEMBLY_2, Collections.singletonList(PRIMARY_ASSEMBLY));
        List<SubSnpCoreFields> list = readAll(reader);

        assertEquals(1, list.size());
        assertEquals(snpsInDifferentAssembly, list);
    }

    @Test
    public void testQueryWithDifferentBatch() throws Exception {
        List<SubSnpCoreFields> snpsInDifferentAssembly = new ArrayList<>();
        snpsInDifferentAssembly.add(new SubSnpCoreFields(55, Orientation.FORWARD,
                                                         25L, Orientation.FORWARD,
                                                         "NW_001867405.1",
                                                         37435034L,
                                                         37435034L,
                                                         Orientation.FORWARD,
                                                         LocusType.SNP,
                                                         "2",
                                                         111039326L,
                                                         111039326L,
                                                         "T", "T", "A", "A/T",
                                                         "NC_009145.2:g.111039326T>A",
                                                         111039326L,111039326L, Orientation.FORWARD,
                                                         "NW_001867405.1:g.37435035T>A",
                                                         37435035L,37435035L, Orientation.FORWARD,
                                                         "BROAD_EQUCAB2.0:2008.08.08"));
        reader = buildReader(DBSNP_BUILD, BATCH_2, HORSE_ASSEMBLY, Collections.singletonList(PRIMARY_ASSEMBLY));
        List<SubSnpCoreFields> list = readAll(reader);
        assertEquals(1, list.size());
        assertEquals(snpsInDifferentAssembly, list);
    }

    @Test
    public void testQueryWithNonExistingBatch() throws Exception {
        int nonExistingBatch = 42;
        reader = buildReader(DBSNP_BUILD, nonExistingBatch, HORSE_ASSEMBLY,
                             Collections.singletonList(PRIMARY_ASSEMBLY));
        List<SubSnpCoreFields> list = readAll(reader);
        assertEquals(0, list.size());
    }
}
