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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.mongodb.entities.VariantSourceMongo;
import uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.DbsnpBatchToVariantSourceProcessor;
import uk.ac.ebi.eva.dbsnpimporter.parameters.Parameters;
import uk.ac.ebi.eva.dbsnpimporter.test.DbsnpTestDatasource;
import uk.ac.ebi.eva.dbsnpimporter.test.configuration.JobTestConfiguration;
import uk.ac.ebi.eva.dbsnpimporter.test.configuration.MongoTestConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.DbsnpBatchToVariantSourceProcessorTest.DBSNP_BUILD;

@RunWith(SpringRunner.class)
@TestPropertySource({"classpath:application.properties"})
@DirtiesContext
@ContextConfiguration(classes = {ImportVariantsJobConfiguration.class, MongoTestConfiguration.class,
        JobTestConfiguration.class})
public class ImportSamplesStepConfigurationTest {

    private static final String DBSNP_BATCH_ID = "11825";

    private static final String DBSNP_BATCH_NAME = "CHICKEN_SNPS_BROILER";

    private static final String FIRST_SAMPLE = "RJF";

    private static final String SECOND_SAMPLE = "BROILER";

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
    public void loadSamples() throws Exception {
        assertEquals(0, mongoOperations.getCollection(parameters.getFilesCollection()).count());

        JobExecution jobExecution = jobLauncherTestUtils.launchStep(ImportSamplesStepConfiguration.IMPORT_SAMPLES_STEP);
        assertCompleted(jobExecution);

        DBCollection collection = mongoOperations.getCollection(parameters.getFilesCollection());
        List<DBObject> dbObjects = collection.find().toArray();

        assertEquals(1, dbObjects.size());

        DBObject dbObject = dbObjects.get(0);

        assertEquals(DBSNP_BATCH_NAME, dbObject.get(VariantSourceMongo.FILEID_FIELD));
        assertEquals(DBSNP_BATCH_NAME, dbObject.get(VariantSourceMongo.FILENAME_FIELD));
        assertEquals(DBSNP_BATCH_NAME, dbObject.get(VariantSourceMongo.STUDYID_FIELD));
        assertEquals(DBSNP_BATCH_NAME, dbObject.get(VariantSourceMongo.STUDYNAME_FIELD));

        Map<String, Integer> expectedSamplesPosition = new HashMap<>();
        expectedSamplesPosition.put(FIRST_SAMPLE, 0);
        expectedSamplesPosition.put(SECOND_SAMPLE, 1);
        assertEquals(expectedSamplesPosition, dbObject.get(VariantSourceMongo.SAMPLES_FIELD));

        Map<String, Object> expectedMetadata = new HashMap<>();
        expectedMetadata.put(DbsnpBatchToVariantSourceProcessor.DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        expectedMetadata.put(DbsnpBatchToVariantSourceProcessor.DBSNP_BATCH_KEY, String.valueOf(DBSNP_BATCH_ID));
        assertEquals(expectedMetadata, dbObject.get(VariantSourceMongo.METADATA_FIELD));
    }

    public static void assertCompleted(JobExecution jobExecution) {
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    }
}