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
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.opencb.datastore.core.QueryOptions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.StudyType;
import uk.ac.ebi.eva.commons.core.models.VariantSource;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.services.VariantSourceService;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoRepositoryTestConfiguration.class})
public class VariantExporterTest {

    private static VariantExporter variantExporter;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private VariantWithSamplesAndAnnotationsService variantService;

    @Autowired
    private VariantSourceService variantSourceService;

    private static ArrayList<String> s1s6SampleList;

    private static ArrayList<String> s2s3SampleList;

    private static ArrayList<String> c1c6SampleList;

    private static final String FILE_1 = "file_1";

    private static final String FILE_2 = "file_2";

    private static final String FILE_3 = "file_3";

    public static final String SHEEP_STUDY_ID = "PRJEB14685";
    public static final String SHEEP_FILE_1_ID = "ERZ324588";
    public static final String SHEEP_FILE_2_ID = "ERZ324596";
    public static final int NUMBER_OF_SAMPLES_IN_SHEEP_FILES = 453;



    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("test-db");

    //@Autowired
    //private VariantRepository variantRepository;


    /**
     * Clears and populates the Mongo collection used during the tests.
     *
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    @BeforeClass
    public static void setUpClass() {
        //cowVariantService = mongoRule.getVariantMongoDBAdaptor(COW_TEST_DB);
        //cowVariantSourceService = cowVariantService.getVariantSourceDBAdaptor();
        //sheepVariantService = mongoRule
        //        .getVariantMongoDBAdaptor(SHEEP_TEST_DB);
        //sheepVariantSourceService = sheepVariantService.getVariantSourceDBAdaptor();

        // example samples list
        s1s6SampleList = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            s1s6SampleList.add("s" + i);
        }
        s2s3SampleList = new ArrayList<>();
        for (int i = 2; i <= 4; i++) {
            s2s3SampleList.add("s" + i);
        }
        c1c6SampleList = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            c1c6SampleList.add("c" + i);
        }

        variantExporter = new VariantExporter();
    }


    @Test
    @UsingDataSet(locations = {
            "/db-dump/eva_hsapiens_grch37/files_2_0.json",
            "/db-dump/eva_hsapiens_grch37/variants_2_0.json"})
    public void getSourcesOneStudyWithEmptyFilesFilter() {
        // one study
        String study7Id = "7";
        List<String> studies = Collections.singletonList(study7Id);
        List<VariantSource> sources =
                variantExporter.getSources(variantSourceService, studies, Collections.emptyList());
        assertEquals(1, sources.size());
        VariantSource file = sources.get(0);

        assertEquals(study7Id, file.getStudyId());
        assertEquals("6", file.getFileId());
    }

    @Test
    @UsingDataSet(locations = {
            "/db-dump/eva_hsapiens_grch37/files_2_0.json",
            "/db-dump/eva_hsapiens_grch37/variants_2_0.json"})
    public void getSourcesTwoStudiesWithEmptyFilesFilter() {
        // two studies
        String study7Id = "7";
        String study8Id = "8";
        List<String> studies = Arrays.asList(study7Id, study8Id);
        List<VariantSource> sources = variantExporter
                .getSources(variantSourceService, studies, Collections.emptyList());
        assertEquals(2, sources.size());
        VariantSource file = sources.stream().filter(s -> s.getStudyId().equals(study7Id)).findFirst().get();
        assertEquals(study7Id, file.getStudyId());
        assertEquals("6", file.getFileId());
        assertEquals(2504, file.getSamplesPosition().size());
        file = sources.stream().filter(s -> s.getStudyId().equals(study8Id)).findFirst().get();
        assertEquals(study8Id, file.getStudyId());
        assertEquals("5", file.getFileId());
        assertEquals(2504, file.getSamplesPosition().size());
    }

    @Test
    public void getSourcesOneStudyThatHasTwoFilesWithEmptyFilesFilter() {
        // one study with two files, without asking for any particular file
        List<String> sheepStudy = Collections.singletonList(SHEEP_STUDY_ID);
        List<VariantSource> sources = variantExporter
                .getSources(variantSourceService, sheepStudy, Collections.emptyList());
        assertEquals(2, sources.size());
        boolean correctStudyId = sources.stream()
                                        .allMatch(s -> s.getStudyId().equals(SHEEP_STUDY_ID));
        assertTrue(correctStudyId);
        assertTrue(sources.stream().anyMatch(s -> s.getFileId().equals(SHEEP_FILE_1_ID)));
        assertTrue(sources.stream().anyMatch(s -> s.getFileId().equals(SHEEP_FILE_2_ID)));
        assertTrue(sources.stream().allMatch(
                s -> s.getSamplesPosition().size() == NUMBER_OF_SAMPLES_IN_SHEEP_FILES));
    }

    @Test
    @UsingDataSet(locations = {
            "/db-dump/eva_oaries_oarv31/files_2_0.json",
            "/db-dump/eva_oaries_oarv31/variants_2_0.json"})
    public void getSourcesOneStudyThatHasTwoFiles() {
        // one study with two files, asking for both files
        List<String> sheepStudy = Collections.singletonList(SHEEP_STUDY_ID);
        List<VariantSource> sources = variantExporter.getSources(variantSourceService, sheepStudy,
                                                                 Arrays.asList(SHEEP_FILE_1_ID,
                                                                               SHEEP_FILE_2_ID));
        assertEquals(2, sources.size());
        boolean correctStudyId = sources.stream()
                                        .allMatch(s -> s.getStudyId().equals(SHEEP_STUDY_ID));
        assertTrue(correctStudyId);
        assertTrue(sources.stream().anyMatch(s -> s.getFileId().equals(SHEEP_FILE_1_ID)));
        assertTrue(sources.stream().anyMatch(s -> s.getFileId().equals(SHEEP_FILE_2_ID)));
        assertTrue(sources.stream().allMatch(
                s -> s.getSamplesPosition().size() == NUMBER_OF_SAMPLES_IN_SHEEP_FILES));
    }

    @Test
    @UsingDataSet(locations = {
            "/db-dump/eva_oaries_oarv31/files_2_0.json",
            "/db-dump/eva_oaries_oarv31/variants_2_0.json"})
    public void getSourcesOneStudyThatHasTwoFilesWithOneFileInFilter() {
        // one study with two files, asking just for a file
        List<String> sheepStudy = Collections.singletonList(SHEEP_STUDY_ID);
        List<VariantSource> sources = variantExporter
                .getSources(variantSourceService, sheepStudy,
                            Collections.singletonList(SHEEP_FILE_1_ID));
        assertEquals(1, sources.size());
        boolean correctStudyId = sources.stream()
                                        .allMatch(s -> s.getStudyId().equals(SHEEP_STUDY_ID));
        assertTrue(correctStudyId);
        assertTrue(sources.stream().anyMatch(s -> s.getFileId().equals(SHEEP_FILE_1_ID)));
        assertTrue(sources.stream().allMatch(
                s -> s.getSamplesPosition().size() == NUMBER_OF_SAMPLES_IN_SHEEP_FILES));
    }

    @Test
    @UsingDataSet(locations = {
            "/db-dump/eva_hsapiens_grch37/files_2_0.json",
            "/db-dump/eva_hsapiens_grch37/variants_2_0.json"})
    public void getSourcesEmptyStudiesFilter() {
        // empty study filter
        List<VariantSource> sources = variantExporter
                .getSources(variantSourceService, Collections.emptyList(), Collections.emptyList());
        assertEquals(0, sources.size());
    }

    @Test(expected = IllegalArgumentException.class)
    @UsingDataSet(locations = {
            "/db-dump/eva_hsapiens_grch37/files_2_0.json",
            "/db-dump/eva_hsapiens_grch37/variants_2_0.json"})
    public void notExistingSourceShouldThrowException() {
        VariantExporter variantExporter = new VariantExporter();
        // The study with id "2" is not in database
        List<String> study = Collections.singletonList("2");
        variantExporter.getSources(variantSourceService, study, Collections.emptyList());
    }

    @Test
    @UsingDataSet(locations = {
            "/db-dump/eva_hsapiens_grch37/files_2_0.json",
            "/db-dump/eva_hsapiens_grch37/variants_2_0.json"})
    public void checkSampleNamesConflicts() {
        VariantSource variantSource = createTestVariantSource(FILE_1, s1s6SampleList);
        VariantSource variantSource2 = createTestVariantSource(FILE_2, c1c6SampleList);
        VariantSource variantSource3 = createTestVariantSource(FILE_3, s2s3SampleList);

        VariantExporter variantExporter = new VariantExporter();

        // sutdy 1 and 2 don't share sample names
        assertNull(variantExporter.createNonConflictingSampleNames((Arrays.asList(variantSource, variantSource2))));

        // sutdy 2 and 3 don't share sample names
        assertNull(variantExporter.createNonConflictingSampleNames((Arrays.asList(variantSource2, variantSource3))));

        // sutdy 1 and 3 share sample some names
        Map<String, Map<String, String>> file1And3SampleNameTranslations = variantExporter
                .createNonConflictingSampleNames((Arrays.asList(variantSource, variantSource3)));
        s1s6SampleList.forEach(sampleName -> file1And3SampleNameTranslations.get(FILE_1).get(sampleName)
                                                                            .equals(FILE_1 + "_" + sampleName));
        s2s3SampleList.forEach(sampleName -> file1And3SampleNameTranslations.get(FILE_3).get(sampleName)
                                                                            .equals(FILE_3 + "_" + sampleName));


        // sutdy 1 and 3 (but not 2) share sample some names
        Map<String, Map<String, String>> file1And2And3SampleNameTranslations = variantExporter
                .createNonConflictingSampleNames((Arrays.asList(variantSource, variantSource2, variantSource3)));
        s1s6SampleList
                .forEach(sampleName -> file1And2And3SampleNameTranslations.get(FILE_1).get(sampleName)
                                                                          .equals(FILE_1 + "_" + sampleName));
        c1c6SampleList
                .forEach(sampleName -> file1And2And3SampleNameTranslations.get(FILE_2).get(sampleName)
                                                                          .equals(FILE_2 + "_" + sampleName));
        s2s3SampleList
                .forEach(sampleName -> file1And2And3SampleNameTranslations.get(FILE_3).get(sampleName)
                                                                          .equals(FILE_3 + "_" + sampleName));
    }

    @Test
    @UsingDataSet(locations = {
            "/db-dump/eva_hsapiens_grch37/files_2_0.json",
            "/db-dump/eva_hsapiens_grch37/variants_2_0.json"})
    public void getVcfHeaders() throws IOException {
        VariantExporter variantExporter = new VariantExporter();
        String study7Id = "7";
        String study8Id = "8";
        List<String> studies = Arrays.asList(study7Id, study8Id);
        List<VariantSource> sources =
                variantExporter.getSources(variantSourceService, studies, Collections.emptyList());

        Map<String, VCFHeader> headers = variantExporter.getVcfHeaders(sources);
        VCFHeader header = headers.get(study7Id);
        assertEquals(2504, header.getSampleNamesInOrder().size());
        assertTrue(header.hasGenotypingData());
        header = headers.get(study8Id);
        assertEquals(2504, header.getSampleNamesInOrder().size());
        assertTrue(header.hasGenotypingData());
    }

    @Test
    @UsingDataSet(locations = {
            "/db-dump/eva_btaurus_umd31/files_2_0.json",
            "/db-dump/eva_btaurus_umd31/variants_2_0.json"})
    public void mergeVcfHeaders() throws IOException {
        VariantExporter variantExporter = new VariantExporter();
        List<String> cowStudyIds = Arrays.asList("PRJEB6119", "PRJEB7061");
        List<VariantSource> cowSources =
                variantExporter.getSources(variantSourceService, cowStudyIds, Collections.emptyList());
        VCFHeader header = variantExporter.getMergedVcfHeader(cowSources, false);

        // assert
        assertEquals(1, header.getContigLines().size());
        // the INFO, FORMAT and FILTER header lines are being filtered out, but a FORMAT GT line is being added
        assertEquals(1, header.getInfoHeaderLines().size());
        assertEquals(0, header.getFilterLines().size());
        assertEquals(1, header.getFormatHeaderLines().size());
        assertNotNull(header.getFormatHeaderLine("GT"));
    }

    @Test
    @UsingDataSet(locations = {
            "/db-dump/eva_hsapiens_grch37/files_2_0.json",
            "/db-dump/eva_hsapiens_grch37/variants_2_0.json"})
    public void testExportOneStudy() throws Exception {
        List<String> studies = Collections.singletonList("7");
        String region = "20:61000-69000";
        QueryOptions query = new QueryOptions();
        List<VariantContext> exportedVariants = exportAndCheck(variantSourceService, variantService, query, studies,
                                                               Collections.EMPTY_LIST, region);
        checkExportedVariants(variantService, query, exportedVariants);
    }

    @Test
    @UsingDataSet(locations = {
            "/db-dump/eva_hsapiens_grch37/files_2_0.json",
            "/db-dump/eva_hsapiens_grch37/variants_2_0.json"})
    public void testExportTwoStudies() throws Exception {
        List<String> studies = Arrays.asList("7", "8");
        String region = "20:61000-69000";
        QueryOptions query = new QueryOptions();
        List<VariantContext> exportedVariants = exportAndCheck(variantSourceService, variantService, query, studies,
                                                               Collections.EMPTY_LIST, region);
        checkExportedVariants(variantService, query, exportedVariants);
    }

    @Test
    @UsingDataSet(locations = {
            "/db-dump/eva_btaurus_umd31/files_2_0.json",
            "/db-dump/eva_btaurus_umd31/variants_2_0.json"})
    public void testExportOneStudyThatHasNotSourceLines() throws Exception {
        List<String> studies = Collections.singletonList("PRJEB6119");
        String region = "21:820000-830000";
        QueryOptions query = new QueryOptions();
        exportAndCheck(variantSourceService, variantService, query, studies, Collections.EMPTY_LIST, region,
                       4);
    }

    @Test
    @UsingDataSet(locations = {
            "/db-dump/eva_oaries_oarv31/files_2_0.json",
            "/db-dump/eva_oaries_oarv31/variants_2_0.json"})
    public void testExportOneFileFromOneStudyThatHasTwoFiles() throws Exception {
        List<String> studies = Collections.singletonList(SHEEP_STUDY_ID);
        List<String> files = Collections.singletonList(SHEEP_FILE_1_ID);
        String region = "14:10250000-10259999";
        QueryOptions query = new QueryOptions();
        List<VariantContext> exportedVariants =
                exportAndCheck(variantSourceService, variantService, query, studies, files, region);
        checkExportedVariants(variantService, query, exportedVariants);
        boolean samplesNumberCorrect =
                exportedVariants.stream().allMatch(
                        v -> v.getGenotypes().size() == NUMBER_OF_SAMPLES_IN_SHEEP_FILES);
        assertTrue(samplesNumberCorrect);
    }



    private List<VariantContext> exportAndCheck(VariantSourceService variantSourceService,
                                                VariantWithSamplesAndAnnotationsService variantService, QueryOptions query,
                                                List<String> studies, List<String> files, String region) {
        return exportAndCheck(variantSourceService, variantService, query, studies, files, region, 0);
    }

    private List<VariantContext> exportAndCheck(VariantSourceService variantSourceService,
                                                VariantWithSamplesAndAnnotationsService variantService, QueryOptions query,
                                                List<String> studies, List<String> files,
                                                String region, int expectedFailedVariants) {
        VariantExporter variantExporter = new VariantExporter();
        //query.put(VariantDBAdaptor.STUDIES, studies);
        //query.add(VariantDBAdaptor.REGION, region);

//        VariantDBIterator iterator = variantDBAdaptor.iterator(query);

        // we need to call 'getSources' before 'export' becausxe it check if there are sample name conflicts and initialize some dependencies
        variantExporter.getSources(variantSourceService, studies, files);
        List<VariantContext> exportedVariants = variantExporter.export(variantService, new QueryParams(), new Region(region));

        assertEquals(expectedFailedVariants, variantExporter.getFailedVariants());

        return exportedVariants;
    }

    private void checkExportedVariants(VariantWithSamplesAndAnnotationsService variantService, QueryOptions query,
                                       List<VariantContext> exportedVariants) {
        //VariantDBIterator iterator;
        // todo: check from db. a predefined number of variants are not checked?
        long iteratorSize = 0;
        //iterator = variantDBAdaptor.iterator(query);
        //while (iterator.hasNext()) {
        //    VariantWithSamplesAndAnnotation variant = iterator.next();
        //    assertTrue(variantInExportedVariantsCollection(variant, exportedVariants));
        //    iteratorSize++;
        //}

        assertEquals(iteratorSize, exportedVariants.size());
    }

    private static boolean variantInExportedVariantsCollection(VariantWithSamplesAndAnnotation variant, List<VariantContext> exportedVariants) {
        if (exportedVariants.stream().anyMatch(v -> sameVariant(variant, v))) {
            return true;
        }

        return false;
    }

    private static boolean sameVariant(VariantWithSamplesAndAnnotation v1, VariantContext v2) {
        if (v2.getContig().equals(v1.getChromosome()) && sameStart(v1, v2)) {
            if (v1.getReference().equals("")) {
                // insertion
                return v2.getAlternateAlleles()
                         .contains(Allele.create(v2.getReference().getBaseString() + v1.getAlternate()));
            } else if (v1.getAlternate().equals("")) {
                // deletion
                return v2.getAlternateAlleles().stream()
                         .anyMatch(alt -> v2.getReference().getBaseString()
                                            .equals(alt.getBaseString() + v1.getReference()));
            } else {
                return v1.getReference().equals(v2.getReference().getBaseString()) && v2.getAlternateAlleles()
                                                                                        .contains(Allele.create(
                                                                                                v1.getAlternate()));
            }
        }
        return false;
    }

    private static boolean sameStart(VariantWithSamplesAndAnnotation v1, VariantContext v2) {
        if (v1.getReference().equals("") || v1.getAlternate().equals("")) {
            return v2.getStart() == (v1.getStart() - 1);
        } else {
            return v2.getStart() == v1.getStart();
        }
    }

    private VariantSource createTestVariantSource(String fileId, List<String> sampleList) {
        Map<String, Integer> samplesPosition = new HashMap<>();
        int index = sampleList.size();
        for (String s : sampleList) {
            samplesPosition.put(s, index++);
        }
        final VariantSource variantSource = new VariantSource(fileId, "name", "studyId", "studyName", StudyType.AGGREGATE,
                                                              null, null, samplesPosition, null, null);
        return variantSource;
    }
}
