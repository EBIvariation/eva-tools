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
package uk.ac.ebi.eva.dbsnpimporter.configuration;

import com.mongodb.BasicDBList;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.dbsnpimporter.parameters.Parameters;
import uk.ac.ebi.eva.dbsnpimporter.test.DbsnpTestDatasource;
import uk.ac.ebi.eva.dbsnpimporter.test.configuration.JobTestConfiguration;
import uk.ac.ebi.eva.dbsnpimporter.test.configuration.MongoTestConfiguration;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@TestPropertySource({"classpath:application.properties"})
@ContextConfiguration(classes = {ImportVariantsJobConfiguration.class, MongoTestConfiguration.class,
        JobTestConfiguration.class})
public class ImportVariantsStepConfigurationTest {

    @Autowired
    private DbsnpTestDatasource dbsnpTestDatasource;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private Parameters parameters;

    @Autowired
    private MongoOperations mongoOperations;

    @Test
    public void loadVariants() throws Exception {
        JobParameters jobParameters = new JobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchStep(ImportVariantsStepConfiguration.IMPORT_VARIANTS_STEP,
                                                                    jobParameters);
        assertCompleted(jobExecution);

        DBCollection collection = mongoOperations.getCollection(parameters.getVariantsCollection());
        List<DBObject> dbObjects = collection.find().toArray();
        int totalSubsnps = 0;
        int totalSnps = 0;
        for (DBObject dbObject : dbObjects) {
            BasicDBList ids = (BasicDBList) dbObject.get("dbsnpIds");
            totalSnps += ids.stream().filter(o -> ((String) o).startsWith("rs")).count();
            totalSubsnps += ids.stream().filter(o -> ((String) o).startsWith("ss")).count();
        }

        assertEquals(8, dbObjects.size());
        assertEquals(8, totalSnps);
        assertEquals(11, totalSubsnps);
    }

    public static void assertCompleted(JobExecution jobExecution) {
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    }
}
