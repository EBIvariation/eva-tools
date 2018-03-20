/*
 * Copyright 2015 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.vcfdump;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.entities.VariantMongo;
import uk.ac.ebi.eva.commons.mongodb.entities.subdocuments.AnnotationIndexMongo;
import uk.ac.ebi.eva.commons.mongodb.repositories.VariantRepository;
import uk.ac.ebi.eva.commons.mongodb.services.VariantSourceService;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.ac.ebi.eva.vcfdump.VariantExporterController.ANNOTATION_EXCLUSION;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoRepositoryTestConfiguration.class})
@UsingDataSet(locations = {
        "/db-dump/eva_hsapiens_grch37/files_2_0.json",
        "/db-dump/eva_hsapiens_grch37/variants_2_0.json"})
public class VariantExporterControllerTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("test-db");


    private static final String OUTPUT_DIR = "/tmp/";
    private static final String SHEEP_STUDY_ID = "PRJEB14685";
    private static final String SHEEP_FILE_1_ID = "ERZ324588";
    private static final String SHEEP_FILE_2_ID = "ERZ324596";

    private static final String HUMAN_TEST_DB = "eva_hsapiens_grch37";
    private static final String COW_TEST_DB = "eva_btaurus_umd31_test";
    private static final String SHEEP_TEST_DB = "eva_oaries_oarv31";

    private static final Map<String, String> databaseMapping = new HashMap<>();


    @Autowired
    private VariantWithSamplesAndAnnotationsService variantService;

    @Autowired
    private VariantRepository variantRepository;

    @Autowired
    private VariantSourceService variantSourceService;

    private static final Logger logger = LoggerFactory.getLogger(VariantExporterControllerTest.class);

    private QueryParams emptyFilter = new QueryParams();

    private static List<String> testOutputFiles;

    private static Properties evaTestProperties;

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);

    private MockServerClient mockServerClient;

    @BeforeClass
    public static void setUpClass() throws IOException{

        evaTestProperties = new Properties();
        evaTestProperties.load(VariantExporterControllerTest.class.getResourceAsStream("/evaTest.properties"));

        testOutputFiles = new ArrayList<>();

        databaseMapping.put(HUMAN_TEST_DB, UUID.randomUUID().toString());
        databaseMapping.put(COW_TEST_DB, UUID.randomUUID().toString());
        databaseMapping.put(SHEEP_TEST_DB, UUID.randomUUID().toString());
    }

    @Before
    public void setUp() {
        MockServerClientHelper.hSapiensGrch37(mockServerClient, databaseMapping.get(HUMAN_TEST_DB));
        MockServerClientHelper.oAriesOarv31(mockServerClient, databaseMapping.get(SHEEP_TEST_DB));

        int port = mockServerRule.getPort();
        evaTestProperties.setProperty("eva.rest.url", String.format("http://localhost:%s/eva/webservices/rest/", port));
    }

    /**
     * Clears and populates the Mongo collection used during the tests.
     *
     */
    @AfterClass
    public static void tearDownClass() {
        testOutputFiles.forEach(f -> new File(f).delete());
    }

    @After
    public void tearDown() {
        testOutputFiles.forEach(f -> new File(f).delete());
    }

    @Test
    public void testVcfExportOneStudy()
            throws URISyntaxException, IOException {
        String studyId = "7";
        List<String> studies = Collections.singletonList(studyId);

        VariantExporterController controller = new VariantExporterController(
                databaseMapping.get(HUMAN_TEST_DB), variantSourceService, variantService,
                                                                             studies, Collections.emptyList(),
                                                                             OUTPUT_DIR, evaTestProperties,
                                                                             emptyFilter);
        controller.run();

        ////////// checks
        String outputFile = controller.getOuputFilePath();
        testOutputFiles.add(outputFile);
        assertEquals(0, controller.getFailedVariants());   // test file should not have failed variants

        long variantCountInDb = getVariantCountInDb(variant -> containStudyId(variant, studies));
        assertTrue(variantCountInDb != 0);
        assertEqualLinesFilesAndDB(outputFile, variantCountInDb);
        assertVcfOrderedByCoordinate(outputFile);
    }

    @Test
    public void testVcfExportSeveralStudies() throws Exception {
        String study7 = "7";
        String study8 = "8";
        List<String> studies = Arrays.asList(study7, study8);

        VariantExporterController controller = new VariantExporterController(databaseMapping.get(HUMAN_TEST_DB),
                                                                             variantSourceService, variantService,
                                                                             studies, Collections.emptyList(),
                                                                             OUTPUT_DIR, evaTestProperties, emptyFilter);
        controller.run();

        ////////// checks
        String outputFile = controller.getOuputFilePath();
        testOutputFiles.add(outputFile);
        assertEquals(0, controller.getFailedVariants());   // test file should not have failed variants

        long variantCountInDb = getVariantCountInDb(variant -> containStudyId(variant, studies));
        assertTrue(variantCountInDb != 0);
        assertEqualLinesFilesAndDB(outputFile, variantCountInDb);
        assertVcfOrderedByCoordinate(outputFile);
    }

    @Test
    @UsingDataSet(locations = {
            "/db-dump/eva_oaries_oarv31/files_2_0.json",
            "/db-dump/eva_oaries_oarv31/variants_2_0.json"})
    public void testVcfExportOneFileFromOneStudyThatHasTwoFiles()
            throws URISyntaxException, IOException {
        QueryParams params = new QueryParams();
        List<String> studies = Collections.singletonList(SHEEP_STUDY_ID);
        List<String> files =
                Arrays.asList(SHEEP_FILE_1_ID, SHEEP_FILE_2_ID);
        params.setStudies(studies);
        VariantExporterController controller = new VariantExporterController(
                databaseMapping.get(SHEEP_TEST_DB),
                variantSourceService, variantService,
                studies, files, OUTPUT_DIR, evaTestProperties, params);
        controller.run();

        ////////// checks
        String outputFile = controller.getOuputFilePath();
        testOutputFiles.add(outputFile);
        assertEquals(0, controller.getFailedVariants());   // test file should not have failed variants

        long variantCountInDb = getVariantCountInDb(variant -> containStudyIdAndFileIds(variant, studies, files));
        assertTrue(variantCountInDb != 0);
        assertEqualLinesFilesAndDB(outputFile, variantCountInDb);
        assertVcfOrderedByCoordinate(outputFile);
    }

    @Test
    public void testConsequenceTypeFilter() throws Exception {
        String study7 = "7";
        String study8 = "8";
        List<String> studies = Arrays.asList(study7, study8);
        QueryParams params = new QueryParams();
        params.setConsequenceType(Collections.singletonList("1627"));
        VariantExporterController controller = new VariantExporterController(
                databaseMapping.get(HUMAN_TEST_DB),
                variantSourceService, variantService,
                studies, Collections.emptyList(), OUTPUT_DIR, evaTestProperties, params);
        controller.run();

        ////////// checks
        String outputFile = controller.getOuputFilePath();
        testOutputFiles.add(outputFile);
        assertEquals(0, controller.getFailedVariants());   // test file should not have failed variants

        long variantCountInDb = getVariantCountInDb(variant -> containConseqType(variant, 1627));
        assertTrue(variantCountInDb != 0);
        assertEqualLinesFilesAndDB(outputFile, variantCountInDb);
        assertVcfOrderedByCoordinate(outputFile);
    }

    private long getVariantCountInDb(Predicate<VariantMongo> predicate) {
        List<VariantMongo> variants = variantRepository.findAll();
        return variants.stream().filter(predicate).count();
    }

    private boolean containStudyId(VariantMongo variant, List<String> studies) {
        return variant.getSourceEntries().stream().anyMatch(s -> studies.contains(s.getStudyId()));
    }

    private boolean containConseqType(VariantMongo variant, Integer conseqType) {
        return variant.getIndexedAnnotations().stream().anyMatch(a -> a.getSoAccessions().contains(conseqType));
    }

    private boolean containStudyIdAndFileIds(VariantMongo variant, List<String> studies, List<String> fileIds) {
        return variant.getSourceEntries().stream().anyMatch(s -> studies.contains(s.getStudyId()) && fileIds.contains(s.getFileId()));
    }

    @Test
    public void testConsequenceTypeAndRegionFilter() throws Exception {
        String study7 = "7";
        String study8 = "8";
        List<String> studies = Arrays.asList(study7, study8);

        QueryParams filter = new QueryParams();
        filter.setRegion("20:60000-61000");
        filter.setConsequenceType(Collections.singletonList("1628"));
        VariantExporterController controller = new VariantExporterController(
                databaseMapping.get(HUMAN_TEST_DB),
                variantSourceService, variantService,
                studies, Collections.emptyList(), OUTPUT_DIR, evaTestProperties, filter);
        controller.run();

        ////////// checks
        String outputFile = controller.getOuputFilePath();
        testOutputFiles.add(outputFile);
        assertEquals(0, controller.getFailedVariants());   // test file should not have failed variants

        long variantCountInDb = 0;
        List<VariantMongo> variants = variantRepository.findByRegionsAndComplexFilters(Collections.singletonList(
                new Region("20",60000L, 61000L)), null, null, new PageRequest(0, 1000));
        for (VariantMongo variant: variants) {
            Set<AnnotationIndexMongo> annotSet = variant.getIndexedAnnotations();
            for (AnnotationIndexMongo annot: annotSet) {
                if (annot.getSoAccessions().contains(1628)) {
                    variantCountInDb++;
                    break;
                }
            }
        }

        assertTrue(variantCountInDb != 0);
        assertEqualLinesFilesAndDB(outputFile, variantCountInDb);
        assertVcfOrderedByCoordinate(outputFile);
    }


    @Test
    public void testFilterUsingIntersectingRegions() throws Exception {
        String study7 = "7";
        String study8 = "8";
        List<String> studies = Arrays.asList(study7, study8);

        // tell all variables to filter with
        QueryParams filter = new QueryParams();
        filter.setRegion("20:61000-66000,20:63000-69000");

        VariantExporterController controller = new VariantExporterController(
                databaseMapping.get(HUMAN_TEST_DB),
                variantSourceService, variantService,
                studies, Collections.emptyList(), OUTPUT_DIR, evaTestProperties, filter);
        controller.run();

        ////////// checks
        String outputFile = controller.getOuputFilePath();
        testOutputFiles.add(outputFile);
        assertEquals(0, controller.getFailedVariants());   // test file should not have failed variants

        List<Region> regionList = Arrays.asList(new Region("20",61000L, 66000L), new Region("20",63000L, 69000L));
        long variantCountInDb = variantRepository.countByRegionsAndComplexFilters(regionList, Collections.emptyList());

        assertTrue(variantCountInDb != 0);
        assertEqualLinesFilesAndDB(outputFile, variantCountInDb);

        assertVcfOrderedByCoordinate(outputFile);
    }

    @Test
    public void testDivideChromosomeInChunks() throws Exception {
        String studyId = "7";
        List<String> studies = Collections.singletonList(studyId);
        QueryParams filter = new QueryParams();
        int blockSize = Integer.parseInt(evaTestProperties.getProperty("eva.htsget.blocksize"));
        VariantExporterController controller = new VariantExporterController(
                databaseMapping.get(HUMAN_TEST_DB),
                variantSourceService, variantService,
                studies, evaTestProperties, filter, blockSize);

        List<Region> regions = controller.divideChromosomeInChunks("1", 500, 1499);
        assertEquals(1, regions.size());
        assertTrue(regions.contains(new Region("1", 500L, 1499L)));

        regions = controller.divideChromosomeInChunks("1", 500, 2500);
        assertEquals(3, regions.size());
        assertTrue(regions.contains(new Region("1", 500L, 1499L)));
        assertTrue(regions.contains(new Region("1", 1500L, 2499L)));
        assertTrue(regions.contains(new Region("1", 2500L, 2500L)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingStudy() throws Exception {
        List<String> studies = Arrays.asList("7", "9"); // study 9 doesn't exist

        VariantExporterController controller = new VariantExporterController(
                databaseMapping.get(HUMAN_TEST_DB),
                variantSourceService, variantService,
                studies, Collections.emptyList(), OUTPUT_DIR, evaTestProperties, new QueryParams());

        controller.run();
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullDbnameThrowsIllegalArgumentException() throws Exception {
        List<String> studies = Collections.singletonList("8");
        new VariantExporterController(null, variantSourceService, variantService, studies, Collections.emptyList(), OUTPUT_DIR, evaTestProperties,
                                      emptyFilter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emtpyStudiesThrowsIllegalArgumentException() throws Exception {
        new VariantExporterController(databaseMapping.get(HUMAN_TEST_DB), variantSourceService, variantService,
                                      Collections.emptyList(),
                                      Collections.emptyList(), OUTPUT_DIR, evaTestProperties, emptyFilter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullOutputDirThrowsIllegalArgumentException() throws Exception {
        List<String> studies = Collections.singletonList("8");
        String outputDir = null;
        new VariantExporterController(databaseMapping.get(HUMAN_TEST_DB), variantSourceService, variantService,
                                      studies, Collections.emptyList(),
                                      outputDir, evaTestProperties, emptyFilter);
    }

    private void assertEqualLinesFilesAndDB(String fileName, long variantCountInD) throws IOException {
        List<VariantWithSamplesAndAnnotation> exportedVariants = getVariantsFromOutputFile(fileName);

        assertEquals(variantCountInD, exportedVariants.size());
    }

    private List<VariantWithSamplesAndAnnotation> getVariantsFromOutputFile(String fileName) throws IOException {
        List<VariantWithSamplesAndAnnotation> variantIds = new ArrayList<>();
        BufferedReader file = new BufferedReader(
                new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName))));
        String line;
        while ((line = file.readLine()) != null) {
            if (line.charAt(0) != '#') {
                String[] fields = line.split("\t", 6);
                VariantWithSamplesAndAnnotation variant = new VariantWithSamplesAndAnnotation(fields[0], Integer.parseInt(fields[1]), Integer.parseInt(fields[1]),
                                                                      fields[3], fields[4]);
                //variant.setEnd(variant.getStart() + variant.getLength() - 1);
                if (variant.getAlternate().substring(0, 1).equals(variant.getReference().substring(0, 1))) {
                    //variant.setAlternate(variant.getAlternate().substring(1));
                    //variant.setReference(variant.getReference().substring(1));
                }
                variantIds.add(variant);
            }
        }
        file.close();
        return variantIds;
    }

    private void assertVcfOrderedByCoordinate(String fileName) {
        logger.info("Checking that {} is sorted by coordinate", fileName);
        Set<String> finishedContigs = new HashSet<>();
        VCFFileReader vcfReader = new VCFFileReader(new File(fileName), false);
        String lastContig = null;
        int previousStart = -1;

        for (VariantContext variant : vcfReader) {
            // check chromosome
            if (lastContig == null || !variant.getContig().equals(lastContig)) {
                if (lastContig != null) {
                    finishedContigs.add(lastContig);
                }
                lastContig = variant.getContig();
                assertFalse("The variants should by grouped by contig in the vcf output",
                            finishedContigs.contains(lastContig));
                previousStart = -1;
            }
            assertTrue("The vcf is not sorted by coordinate: " + variant.getContig() + ":" + variant.getStart() + ":" +
                               variant.getReference() + "->" + variant
                               .getAlternateAlleles() + "; Previous variant start: " + previousStart,
                       variant.getStart() >= previousStart);
            previousStart = variant.getStart();
        }

    }

    @Test
    @UsingDataSet(locations = {
            "/db-dump/eva_oaries_oarv31/files_2_0.json",
            "/db-dump/eva_oaries_oarv31/annotations_2_0.json",
            "/db-dump/eva_oaries_oarv31/annotationMetadata_2_0.json",
            "/db-dump/eva_oaries_oarv31/variants_2_0.json"})
    public void checkCsqIsIncluded() throws URISyntaxException, IOException {
        QueryParams params = new QueryParams();
        List<String> studies = Collections.singletonList(SHEEP_STUDY_ID);
        List<String> files = Arrays.asList(SHEEP_FILE_1_ID, SHEEP_FILE_2_ID);
        params.setStudies(studies);
        VariantExporterController controller = new VariantExporterController(
                databaseMapping.get(SHEEP_TEST_DB),
                variantSourceService, variantService,
                studies, files, OUTPUT_DIR, evaTestProperties, params);
        controller.run();

        ////////// checks
        String outputFile = controller.getOuputFilePath();
        testOutputFiles.add(outputFile);
        assertEquals(0, controller.getFailedVariants());   // test file should not have failed variants

        long variantCountInDb = getVariantCountInDb(variant -> containStudyIdAndFileIds(variant, studies, files));
        assertTrue(variantCountInDb != 0);
        assertEqualLinesFilesAndDB(outputFile, variantCountInDb);
        assertVcfOrderedByCoordinate(outputFile);
        BufferedReader file = new BufferedReader(
                new InputStreamReader(new GZIPInputStream(new FileInputStream(outputFile))));
        String line;
        int linesChecked = 0;
        while ((line = file.readLine()) != null) {
            if (line.charAt(0) != '#') {
                String[] fields = line.split("\t", 9);
                assertTrue(fields[7].contains("CSQ="));
                linesChecked++;
            }
        }
        assertNotEquals(0, linesChecked);
    }

    @Test
    @UsingDataSet(locations = {
            "/db-dump/eva_oaries_oarv31/files_2_0.json",
            "/db-dump/eva_oaries_oarv31/annotations_2_0.json",
            "/db-dump/eva_oaries_oarv31/annotationMetadata_2_0.json",
            "/db-dump/eva_oaries_oarv31/variants_2_0.json"})
    public void checkCsqIsExcluded() throws URISyntaxException, IOException {
        QueryParams params = new QueryParams();
        List<String> studies = Collections.singletonList(SHEEP_STUDY_ID);
        List<String> files = Arrays.asList(SHEEP_FILE_1_ID, SHEEP_FILE_2_ID);
        params.setStudies(studies);
        params.setExclusions(Collections.singletonList(ANNOTATION_EXCLUSION));
        VariantExporterController controller = new VariantExporterController(
                databaseMapping.get(SHEEP_TEST_DB),
                variantSourceService, variantService,
                studies, files, OUTPUT_DIR, evaTestProperties, params);
        controller.run();

        ////////// checks
        String outputFile = controller.getOuputFilePath();
        testOutputFiles.add(outputFile);
        assertEquals(0, controller.getFailedVariants());   // test file should not have failed variants

        long variantCountInDb = getVariantCountInDb(variant -> containStudyIdAndFileIds(variant, studies, files));
        assertTrue(variantCountInDb != 0);
        assertEqualLinesFilesAndDB(outputFile, variantCountInDb);
        assertVcfOrderedByCoordinate(outputFile);
        BufferedReader file = new BufferedReader(
                new InputStreamReader(new GZIPInputStream(new FileInputStream(outputFile))));
        String line;
        int linesChecked = 0;
        while ((line = file.readLine()) != null) {
            if (line.charAt(0) != '#') {
                String[] fields = line.split("\t", 9);
                assertFalse(fields[7].contains("CSQ="));
                linesChecked++;
            }
        }
        assertNotEquals(0, linesChecked);
    }
}

// same with exclude=annotation
// assertFalse(fields[8].contains("CSQ="));
