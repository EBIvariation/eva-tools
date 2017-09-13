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
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@JdbcTest
public class SubSnpCoreFieldsReaderTest {

    @Autowired
    private DataSource dataSource;

    private SubSnpCoreFieldsReader reader;

    private List<SubSnpCoreFields> expectedSnps;

    @Before
    public void setUp() throws Exception {
        String assembly = "Gallus_gallus-5.0";
        List<String> assemblyTypes = new LinkedList<>();
        assemblyTypes.add("Primary_Assembly");
        int pageSize = 2000;

        reader = buildReader(assembly, assemblyTypes, pageSize);

        expectedSnps = new ArrayList<>();
        expectedSnps.add(new SubSnpCoreFields(26201546,
                                              13677177L,
                                              1,
                                              "NT_455866.1",
                                              1766472,
                                              1766472,
                                              1,
                                              "4",
                                              91223961,
                                              91223961
        ));

        expectedSnps.add(new SubSnpCoreFields(26954817,
                                              13677177L,
                                              1,
                                              "NT_455866.1",
                                              1766472,
                                              1766472,
                                              1,
                                              "4",
                                              91223961,
                                              91223961
        ));

        expectedSnps.add(new SubSnpCoreFields(26963037,
                                              13677177L,
                                              1,
                                              "NT_455866.1",
                                              1766472,
                                              1766472,
                                              1,
                                              "4",
                                              91223961,
                                              91223961
        ));
    }

    private SubSnpCoreFieldsReader buildReader(String assembly, List<String> assemblyTypes, int pageSize)
            throws Exception {
        SubSnpCoreFieldsReader fieldsReader = new SubSnpCoreFieldsReader(assembly, assemblyTypes, dataSource, pageSize);
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
        assertEquals(2000, reader.getPageSize());
    }

    @Test
    public void testQuery() throws Exception {
        List<SubSnpCoreFields> list = readAll(reader);
        assertEquals(3, list.size());
        for (SubSnpCoreFields subSnpCoreFields : list) {
            assertNotNull(subSnpCoreFields);
            Optional<SubSnpCoreFields> expectedSnp = expectedSnps.stream().filter(s -> s.getSsId() == subSnpCoreFields.getSsId()).findFirst();
            assertTrue(expectedSnp.isPresent());
            assertEquals(expectedSnp.get(), subSnpCoreFields);
        }
    }

    private List<SubSnpCoreFields> readAll(SubSnpCoreFieldsReader fieldsReader) throws Exception {
        List<SubSnpCoreFields> list = new ArrayList<>();
        SubSnpCoreFields subSnpCoreFields = fieldsReader.read();
        while (subSnpCoreFields != null) {
            list.add(subSnpCoreFields);
            subSnpCoreFields = fieldsReader.read();
        }
        return list;
    }

//    @Test
//    public void testQueryWithDifferentAssembly() throws Exception {
//        SubSnpCoreFieldsReader fieldsReader = buildReader("Bos_taurus_UMD_3.1.1",
//                                                          Collections.singletonList("Primary_Assembly"), 2000);
//        List<SubSnpCoreFields> list = readAll(fieldsReader);
//        assertEquals(2, list.size());
//        for (SubSnpCoreFields subSnpCoreFields : list) {
//            assertNotNull(subSnpCoreFields);
//            assertTrue(expectedSnps.containsKey(subSnpCoreFields.getRsId()));
//        }
//    }

    @Test
    public void testQueryWithDifferentAssemblyType() throws Exception {
        SubSnpCoreFieldsReader fieldsReader = buildReader("Gallus_gallus-5.0", Collections.singletonList("non-nuclear"), 2000);
        List<SubSnpCoreFields> list = readAll(fieldsReader);
        assertEquals(0, list.size());
    }
}
