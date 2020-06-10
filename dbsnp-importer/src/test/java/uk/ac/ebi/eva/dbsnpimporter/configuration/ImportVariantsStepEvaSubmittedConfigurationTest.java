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
import com.mongodb.BasicDBObject;
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
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.mongodb.configuration.EvaRepositoriesConfiguration;
import uk.ac.ebi.eva.commons.mongodb.entities.VariantMongo;
import uk.ac.ebi.eva.commons.mongodb.entities.subdocuments.VariantSourceEntryMongo;
import uk.ac.ebi.eva.commons.mongodb.entities.subdocuments.VariantStatisticsMongo;
import uk.ac.ebi.eva.commons.mongodb.repositories.VariantRepository;
import uk.ac.ebi.eva.dbsnpimporter.configuration.mongo.MongoConfiguration;
import uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.AssemblyCheckFilterProcessor;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;
import uk.ac.ebi.eva.dbsnpimporter.parameters.Parameters;
import uk.ac.ebi.eva.dbsnpimporter.test.DbsnpTestDatasource;
import uk.ac.ebi.eva.dbsnpimporter.test.configuration.JobTestConfiguration;
import uk.ac.ebi.eva.dbsnpimporter.test.configuration.MongoTestConfiguration;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@TestPropertySource({"classpath:application-eva-submitted.properties"})
@DirtiesContext
@ContextConfiguration(classes = {ImportEvaSubmittedVariantsJobConfiguration.class, MongoConfiguration.class,
        MongoTestConfiguration.class, JobTestConfiguration.class, EvaRepositoriesConfiguration.class})
public class ImportVariantsStepEvaSubmittedConfigurationTest {

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
    private VariantRepository variantRepository;

    // the assembly checker is mocked to avoid adding a large FASTA file to the resources directory
    @MockBean
    private AssemblyCheckFilterProcessor assemblyCheckerMock;

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = MongoDbRule.MongoDbRuleBuilder.newMongoDbRule().defaultSpringMongoDb(TEST_DB);

    @Before
    public void setUp() throws Exception {
        // the assembly checker mock will filter out one variant
        when(this.assemblyCheckerMock.process(anyObject())).thenAnswer(invocationOnMock -> {
            SubSnpCoreFields inputVariant = invocationOnMock.getArgument(0);
            if (inputVariant.getRsId() == 3136865) {
                return null;
            } else {
                return inputVariant;
            }
        });

        if (mongoOperations.collectionExists(parameters.getFilesCollection())) {
            mongoOperations.dropCollection(parameters.getFilesCollection());
        }
        if (mongoOperations.collectionExists(parameters.getVariantsCollection())) {
            mongoOperations.dropCollection(parameters.getVariantsCollection());
        }
    }

    @Test
    public void loadVariants() throws Exception {
        assertEquals(0, mongoOperations.getCollection(parameters.getVariantsCollection()).count());
        List<JobInstance> jobInstances = jobExplorer.getJobInstances(ImportVariantsJobConfiguration.IMPORT_VARIANTS_JOB, 0, 100);
        assertEquals(0, jobInstances.size());

        JobExecution jobExecution = jobLauncherTestUtils.launchStep(ImportVariantsStepConfiguration.IMPORT_VARIANTS_STEP);
        assertCompleted(jobExecution);

        MongoCollection<Document> collection = mongoOperations.getCollection(parameters.getVariantsCollection());
        FindIterable<Document> documents = collection.find();
        int totalSubsnps = 0;
        int totalSnps = 0;
        for (Document document : documents) {
            List<String> ids = (List<String>) document.get("dbsnpIds");
            totalSnps += ids.stream().filter(o -> o.startsWith("rs")).count();
            totalSubsnps += ids.stream().filter(o -> o.startsWith("ss")).count();
        }

        int[] documentCount = {0};
        documents.spliterator().forEachRemaining(document -> documentCount[0]++);
        assertEquals(8, documentCount[0]);
        assertEquals(8, totalSnps);
        assertEquals(11, totalSubsnps);

        checkASnp();
        checkAnInsertion();

        checkNoSourceEntries();
        checkNoStatistics();
    }

    private static void assertCompleted(JobExecution jobExecution) {
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    }

    private void checkASnp() throws Exception {
        assertCoordinatesEquals(25138411, 744588L, 744591L);
    }

    private void checkAnInsertion() throws Exception {
        assertCoordinatesEquals(24937730, 106586871L, 106586871L);
    }

    private void assertCoordinatesEquals(int subsnpId, long expectedStart, long expectedEnd) throws Exception {
        Document document = getDocumentBySubsnp(subsnpId);
        assertEquals(expectedStart, document.get("start"));
        assertEquals(expectedEnd, document.get("end"));
    }

    private Document getDocumentBySubsnp(int subsnp) {
        MongoCollection<Document> collection = mongoOperations.getCollection(parameters.getVariantsCollection());
        String subsnpString = "ss" + subsnp;
        FindIterable<Document> documents = collection.find(new BasicDBObject("dbsnpIds", subsnpString));
        return documents.first();
    }

    private void checkNoSourceEntries() {
        List<VariantMongo> variants = variantRepository.findByChromosomeAndStartAndReference("1", 14157381, "");
        assertEquals(1, variants.size());
        Set<VariantSourceEntryMongo> sourceEntries = variants.get(0).getSourceEntries();
        assertEquals(0, sourceEntries.size());
    }

    private void checkNoStatistics() {
        List<VariantMongo> variants = variantRepository.findByChromosomeAndStartAndReference("1", 14157381, "");
        assertEquals(1, variants.size());
        Set<VariantStatisticsMongo> statistics = variants.get(0).getVariantStatsMongo();
        assertEquals(0, statistics.size());
    }
}
