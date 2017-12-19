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
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.mongodb.configuration.EvaRepositoriesConfiguration;
import uk.ac.ebi.eva.commons.mongodb.entities.VariantMongo;
import uk.ac.ebi.eva.commons.mongodb.entities.subdocuments.VariantSourceEntryMongo;
import uk.ac.ebi.eva.commons.mongodb.entities.subdocuments.VariantStatisticsMongo;
import uk.ac.ebi.eva.commons.mongodb.repositories.VariantRepository;
import uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors.AssemblyCheckFilterProcessor;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;
import uk.ac.ebi.eva.dbsnpimporter.parameters.Parameters;
import uk.ac.ebi.eva.dbsnpimporter.test.DbsnpTestDatasource;
import uk.ac.ebi.eva.dbsnpimporter.test.configuration.JobTestConfiguration;
import uk.ac.ebi.eva.dbsnpimporter.test.configuration.MongoTestConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@TestPropertySource({"classpath:application.properties"})
@ContextConfiguration(classes = {ImportVariantsJobConfiguration.class, MongoTestConfiguration.class,
        JobTestConfiguration.class, EvaRepositoriesConfiguration.class})
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

    @Autowired
    private VariantRepository variantRepository;

    // the assembly checker is mocked to avoid adding a large FASTA file to the resources directory
    @MockBean
    private AssemblyCheckFilterProcessor assemblyCheckerMock;

    @Before
    public void setUp() throws Exception {
        // the assembly checker mock will filter out one variant
        when(this.assemblyCheckerMock.process(anyObject())).thenAnswer(invocationOnMock -> {
            SubSnpCoreFields inputVariant = invocationOnMock.getArgumentAt(0, SubSnpCoreFields.class);
            if (inputVariant.getRsId() == 3136865) {
                return null;
            } else {
                return inputVariant;
            }
        });
    }

    @Test
    public void loadVariants() throws Exception {
        JobParameters jobParameters = new JobParameters();
        List<JobInstance> jobInstances = jobExplorer.getJobInstances(ImportVariantsJobConfiguration.IMPORT_VARIANTS_JOB, 0, 100);
        assertEquals(0, jobInstances.size());

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

        checkASnp();
        checkAnInsertion();

        checkGenotypes();
        checkStatistics();
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
        DBObject document = getDocumentBySubsnp(subsnpId);
        assertEquals(expectedStart, document.get("start"));
        assertEquals(expectedEnd, document.get("end"));
    }

    private DBObject getDocumentBySubsnp(int subsnp) {
        DBCollection collection = mongoOperations.getCollection(parameters.getVariantsCollection());
        String subsnpString = "ss" + subsnp;
        List<DBObject> dbObjects = collection.find(new BasicDBObject("dbsnpIds", subsnpString)).toArray();
        return dbObjects.get(0);
    }

    private void checkGenotypes() {
        List<VariantMongo> variants = variantRepository.findByChromosomeAndStartAndReference("1", 14157381, "");
        assertEquals(1, variants.size());
        Set<VariantSourceEntryMongo> sourceEntries = variants.get(0).getSourceEntries();
        assertEquals(1, sourceEntries.size());
        for (VariantSourceEntryMongo sourceEntry : sourceEntries) {
            List<Map<String, String>> samplesData = sourceEntry.deflateSamplesData(2);
            assertEquals(2, samplesData.size());
            assertEquals(Collections.singletonMap("GT", "0/0"), samplesData.get(0));
            assertEquals(Collections.singletonMap("GT", "1/1"), samplesData.get(1));
        }
    }

    private void checkStatistics() {
        List<VariantMongo> variants = variantRepository.findByChromosomeAndStartAndReference("1", 14157381, "");
        assertEquals(1, variants.size());

        Set<VariantStatisticsMongo> statistics = variants.get(0).getVariantStatsMongo();
        assertEquals(1, statistics.size());

        Optional<VariantStatisticsMongo> populationStatisticsWrapper = statistics.stream().findFirst();
        assertTrue(populationStatisticsWrapper.isPresent());
        VariantStatisticsMongo populationStatistics = populationStatisticsWrapper.get();

        assertEquals("RBLS", populationStatistics.getCohortId());
        assertEquals(0.5, populationStatistics.getMaf(), 0.01);
        assertEquals("ACAG", populationStatistics.getMafAllele());
    }
}
