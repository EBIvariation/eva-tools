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

import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.mongodb.entities.VariantSourceMongo;
import uk.ac.ebi.eva.dbsnpimporter.configuration.mongo.MongoConfiguration;
import uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.DbsnpBatchToVariantSourceProcessor;
import uk.ac.ebi.eva.dbsnpimporter.parameters.Parameters;
import uk.ac.ebi.eva.dbsnpimporter.test.DbsnpTestDatasource;
import uk.ac.ebi.eva.dbsnpimporter.test.configuration.JobTestConfiguration;
import uk.ac.ebi.eva.dbsnpimporter.test.configuration.MongoTestConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.DbsnpBatchToVariantSourceProcessorTest.DBSNP_BUILD;

@RunWith(SpringRunner.class)
@TestPropertySource({"classpath:application.properties"})
@DirtiesContext
@ContextConfiguration(classes = {ImportVariantsJobConfiguration.class, MongoConfiguration.class,
        MongoTestConfiguration.class, JobTestConfiguration.class})
public class ImportSamplesStepConfigurationTest {

    private static final String DBSNP_BATCH_ID = "11825";

    private static final String DBSNP_BATCH_HANDLE = "BGI";

    private static final String DBSNP_BATCH_NAME = "CHICKEN_SNPS_BROILER";

    private static final String FIRST_SAMPLE = "R£JF";

    private static final String SECOND_SAMPLE = "BROILER";

    private static final String THIRD_SAMPLE = "UNKSEX";

    private static final String TEST_DB = "test-db";

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

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = MongoDbRule.MongoDbRuleBuilder.newMongoDbRule().defaultSpringMongoDb(TEST_DB);

    @Before
    public void setUp() {
        if (mongoOperations.collectionExists(parameters.getFilesCollection())) {
            mongoOperations.dropCollection(parameters.getFilesCollection());
        }
        if (mongoOperations.collectionExists(parameters.getVariantsCollection())) {
            mongoOperations.dropCollection(parameters.getVariantsCollection());
        }
    }

    @Test
    public void loadSamples() throws Exception {
        assertEquals(0, mongoOperations.getCollection(parameters.getFilesCollection()).count());

        JobExecution jobExecution = jobLauncherTestUtils.launchStep(ImportSamplesStepConfiguration.IMPORT_SAMPLES_STEP);
        assertCompleted(jobExecution);

        MongoCollection<Document> collection = mongoOperations.getCollection(parameters.getFilesCollection());
        FindIterable<Document> dbObjects = collection.find();

        int[] documentCount = {0};
        dbObjects.iterator().forEachRemaining(document -> {documentCount[0] += 1;});
        assertEquals(1, documentCount[0]);

        Document document = dbObjects.first();

        assertEquals(DBSNP_BATCH_NAME, document.get(VariantSourceMongo.FILEID_FIELD));
        assertEquals(DBSNP_BATCH_HANDLE + " - " + DBSNP_BATCH_NAME, document.get(VariantSourceMongo.FILENAME_FIELD));
        assertEquals(DBSNP_BATCH_NAME, document.get(VariantSourceMongo.STUDYID_FIELD));
        assertEquals(DBSNP_BATCH_HANDLE + " - " + DBSNP_BATCH_NAME, document.get(VariantSourceMongo.STUDYNAME_FIELD));

        Map<String, Integer> expectedSamplesPosition = new HashMap<>();
        expectedSamplesPosition.put(FIRST_SAMPLE, 0);
        expectedSamplesPosition.put(SECOND_SAMPLE, 1);
        expectedSamplesPosition.put(THIRD_SAMPLE, 2);
        assertEquals(expectedSamplesPosition, document.get(VariantSourceMongo.SAMPLES_FIELD));

        Map<String, Object> expectedMetadata = new HashMap<>();
        expectedMetadata.put(DbsnpBatchToVariantSourceProcessor.DBSNP_BUILD_KEY, String.valueOf(DBSNP_BUILD));
        expectedMetadata.put(DbsnpBatchToVariantSourceProcessor.DBSNP_BATCH_KEY, DBSNP_BATCH_ID);
        assertEquals(expectedMetadata, document.get(VariantSourceMongo.METADATA_FIELD));
    }

    public static void assertCompleted(JobExecution jobExecution) {
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    }
}