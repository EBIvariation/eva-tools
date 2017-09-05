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

import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@JdbcTest
public class SubSnpCoreFieldsReaderTest {

    @Autowired
    private DataSource dataSource;

    private SubSnpCoreFieldsReader reader;

    @Before
    public void setUp() throws Exception {
        String assembly = "Btau_5.0.1";
        List<String> assemblyTypes = new LinkedList<>();
        assemblyTypes.add("Primary_Assembly");
        int pageSize = 2000;

        reader = new SubSnpCoreFieldsReader(assembly, assemblyTypes, dataSource, pageSize);
        reader.afterPropertiesSet();
        ExecutionContext executionContext = new ExecutionContext();
        reader.open(executionContext);
    }

    @After
    public void tearDown() throws Exception {
        reader.close();
    }

    @Test
    public void test() {
        assertNotNull(reader);
        assertEquals(2000, reader.getPageSize());
    }

    @Test
    public void testQuery() throws Exception {
        List<SubSnpCoreFields> list = new ArrayList<>();
        SubSnpCoreFields subSnpCoreFields = reader.read();
        while (subSnpCoreFields != null) {
            subSnpCoreFields = reader.read();
        }
        assertEquals(5, list.size());
    }

    @Test
    public void basicTest() throws Exception {
        SnpIdReader snpIdReader = new SnpIdReader("", Collections.emptyList(), dataSource, 100);
        snpIdReader.afterPropertiesSet();
        ExecutionContext executionContext = new ExecutionContext();
        snpIdReader.open(executionContext);

        Long read = snpIdReader.read();
        assertNotNull(read);
    }
}
