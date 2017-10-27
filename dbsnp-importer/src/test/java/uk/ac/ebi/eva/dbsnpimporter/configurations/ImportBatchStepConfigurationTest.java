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
package uk.ac.ebi.eva.dbsnpimporter.configurations;

import com.mongodb.DBCollection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.dbsnpimporter.Parameters;
import uk.ac.ebi.eva.dbsnpimporter.test.MongoTestConfiguration;
import uk.ac.ebi.eva.dbsnpimporter.test.TestConfiguration;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@TestPropertySource({"classpath:application.properties"})
@JdbcTest
@ContextConfiguration(classes = {ImportBatchJobConfiguration.class, MongoTestConfiguration.class,
        TestConfiguration.class})
public class ImportBatchStepConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Parameters parameters;

    @Autowired
    private MongoOperations mongoOperations;

    @Test
    public void loadVariants() throws Exception {

        JobParameters jobParameters = new JobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchStep(ImportBatchStepConfiguration.LOAD_VARIANTS_STEP,
                                                                    jobParameters);

        DBCollection collection = mongoOperations.getCollection(parameters.getVariantsCollection());
        assertEquals(23, collection.count());

    }
}