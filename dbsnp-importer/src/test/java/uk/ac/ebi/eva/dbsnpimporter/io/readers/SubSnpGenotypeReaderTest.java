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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpGenotype;

import javax.sql.DataSource;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.ac.ebi.eva.dbsnpimporter.test.TestUtils.assertContains;

@RunWith(SpringRunner.class)
@JdbcTest
public class SubSnpGenotypeReaderTest extends ReaderTest {

    private static final int PAGE_SIZE = 2000;

    private static final int BATCH = 12068;

    @Autowired
    private DataSource dataSource;

    private SubSnpGenotypeReader reader;

    private List<SubSnpGenotype> expectedSubSnpGenotyes;

    @Before
    public void setUp() {
        expectedSubSnpGenotyes = new ArrayList<>();

        expectedSubSnpGenotyes.add(new SubSnpGenotype(12068, "CHICKEN_SNPs_SILKIE",
                26505332, "C/C,T/T"));
        expectedSubSnpGenotyes.add(new SubSnpGenotype(12068, "CHICKEN_SNPs_SILKIE",
                26505333, "G/G,T/T"));
        expectedSubSnpGenotyes.add(new SubSnpGenotype(12068, "CHICKEN_SNPs_SILKIE",
                26505335, "A/A,C/C"));
        expectedSubSnpGenotyes.add(new SubSnpGenotype(12068, "CHICKEN_SNPs_SILKIE",
                26505336, "C/C,T/T"));
        expectedSubSnpGenotyes.add(new SubSnpGenotype(12068, "CHICKEN_SNPs_SILKIE",
                26505337, "G/G,T/T"));
        expectedSubSnpGenotyes.add(new SubSnpGenotype(12068, "CHICKEN_SNPs_SILKIE",
                26505339, "C/C,T/T"));
        expectedSubSnpGenotyes.add(new SubSnpGenotype(12068, "CHICKEN_SNPs_SILKIE",
                26505340, "C/C,T/T"));
        expectedSubSnpGenotyes.add(new SubSnpGenotype(12068, "CHICKEN_SNPs_SILKIE",
                26505342, "C/C,T/T"));
        expectedSubSnpGenotyes.add(new SubSnpGenotype(12068, "CHICKEN_SNPs_SILKIE",
                26505343, "-/-,C/C"));
    }

    private SubSnpGenotypeReader buildReader(int batch, int pageSize) throws Exception {
        SubSnpGenotypeReader fieldsReader = new SubSnpGenotypeReader(batch, dataSource, pageSize);
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
        reader = buildReader(BATCH, PAGE_SIZE);
        assertNotNull(reader);
        assertEquals(PAGE_SIZE, reader.getPageSize());
    }

    @Test
    public void testQuery() throws Exception {
        reader = buildReader(BATCH, PAGE_SIZE);
        List<SubSnpGenotype> readSnps = readAll(reader);

        assertEquals(9, readSnps.size());
        for (SubSnpGenotype expectedSubSnpGenotype : expectedSubSnpGenotyes) {
            assertContains(readSnps, expectedSubSnpGenotype);
        }
    }

    @Test
    public void testQueryWithDifferentBatch() throws Exception {

        List<SubSnpGenotype> expectedSubSnpGenotyes_case2 = new ArrayList<>();
        expectedSubSnpGenotyes_case2.add(new SubSnpGenotype(12069, "CHICKEN_SNPs_LAYER",
                26011365, "-/-,TAAAAG/TAAAAG,-/-"));
        expectedSubSnpGenotyes_case2.add(new SubSnpGenotype(12069, "CHICKEN_SNPs_LAYER",
                26011366, "-/-,A/A,T/T"));
        expectedSubSnpGenotyes_case2.add(new SubSnpGenotype(12069, "CHICKEN_SNPs_LAYER",
                26011367, "-/-,AT/AT,./."));
        expectedSubSnpGenotyes_case2.add(new SubSnpGenotype(12069, "CHICKEN_SNPs_LAYER",
                26011368, "C/C,T/T,C/C"));
        expectedSubSnpGenotyes_case2.add(new SubSnpGenotype(12069, "CHICKEN_SNPs_LAYER",
                26011369, "A/A,G/G,G/G"));


        reader = buildReader(12069, PAGE_SIZE);
        List<SubSnpGenotype> readSnps = readAll(reader);
        assertEquals(5, readSnps.size());
        for (SubSnpGenotype expectedSubSnpGenotype : expectedSubSnpGenotyes_case2) {
            assertContains(readSnps, expectedSubSnpGenotype);
        }
    }

    @Test
    public void testQueryWithNonExistingBatch() throws Exception {
        int nonExistingBatch = 42;
        reader = buildReader(nonExistingBatch, PAGE_SIZE);
        List<SubSnpGenotype> list = readAll(reader);
        assertEquals(0, list.size());
    }
}
