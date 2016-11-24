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

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBIterator;
import org.opencb.opencga.storage.core.variant.adaptors.VariantSourceDBAdaptor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class VariantExporterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static VariantDBAdaptor variantDBAdaptor;

    private static VariantSourceDBAdaptor variantSourceDBAdaptor;

    private static VariantDBAdaptor cowVariantDBAdaptor;

    private static VariantDBAdaptor sheepVariantDBAdaptor;

    private static VariantSourceDBAdaptor sheepVariantSourceDBAdaptor;

    private static VariantSourceDBAdaptor cowVariantSourceDBAdaptor;

    private static ArrayList<String> s1s6SampleList;

    private static ArrayList<String> s2s3SampleList;

    private static ArrayList<String> c1c6SampleList;

    private static final String FILE_1 = "file_1";

    private static final String FILE_2 = "file_2";

    private static final String FILE_3 = "file_3";

    /**
     * Clears and populates the Mongo collection used during the tests.
     *
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    @BeforeClass
    public static void setUpClass()
            throws IOException, InterruptedException, URISyntaxException, IllegalAccessException,
            ClassNotFoundException,
            InstantiationException, IllegalOpenCGACredentialsException {
        VariantExporterTestDB.cleanDBs();
        VariantExporterTestDB.fillDB();

        variantDBAdaptor = VariantExporterTestDB.getVariantMongoDBAdaptor(VariantExporterTestDB.TEST_DB_NAME);
        variantSourceDBAdaptor = variantDBAdaptor.getVariantSourceDBAdaptor();
        cowVariantDBAdaptor = VariantExporterTestDB.getVariantMongoDBAdaptor(VariantExporterTestDB.COW_TEST_DB_NAME);
        cowVariantSourceDBAdaptor = cowVariantDBAdaptor.getVariantSourceDBAdaptor();
        sheepVariantDBAdaptor = VariantExporterTestDB.getVariantMongoDBAdaptor(VariantExporterTestDB.SHEEP_TEST_DB_NAME);
        sheepVariantSourceDBAdaptor = sheepVariantDBAdaptor.getVariantSourceDBAdaptor();

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
    }

    /**
     * Clears and populates the Mongo collection used during the tests.
     *
     * @throws UnknownHostException
     */
    @AfterClass
    public static void tearDownClass() throws UnknownHostException {
        VariantExporterTestDB.cleanDBs();
    }

    @Test
    public void getSources() {
        VariantExporter variantExporter = new VariantExporter();

        // one study
        String study7Id = "7";
        List<String> studies = Collections.singletonList(study7Id);
        List<VariantSource> sources =
                variantExporter.getSources(variantSourceDBAdaptor, studies, Collections.EMPTY_LIST);
        assertEquals(1, sources.size());
        VariantSource file = sources.get(0);

        assertEquals(study7Id, file.getStudyId());
        assertEquals("6", file.getFileId());

        // two studies
        String study8Id = "8";
        studies = Arrays.asList(study7Id, study8Id);
        sources = variantExporter.getSources(variantSourceDBAdaptor, studies, Collections.EMPTY_LIST);
        assertEquals(2, sources.size());
        file = sources.stream().filter(s -> s.getStudyId().equals(study7Id)).findFirst().get();
        assertEquals(study7Id, file.getStudyId());
        assertEquals("6", file.getFileId());
        assertEquals(2504, file.getSamples().size());
        file = sources.stream().filter(s -> s.getStudyId().equals(study8Id)).findFirst().get();
        assertEquals(study8Id, file.getStudyId());
        assertEquals("5", file.getFileId());
        assertEquals(2504, file.getSamples().size());

        // one study with two files, without asking for any particular file
        List<String> sheepStudy = Collections.singletonList(VariantExporterTestDB.SHEEP_STUDY_ID);
        sources = variantExporter.getSources(sheepVariantSourceDBAdaptor, sheepStudy, Collections.EMPTY_LIST);
        assertEquals(2, sources.size());
        boolean correctStudyId = sources.stream().allMatch(s -> s.getStudyId().equals(VariantExporterTestDB.SHEEP_STUDY_ID));
        assertTrue(correctStudyId);
        assertTrue(sources.stream().anyMatch(s -> s.getFileId().equals(VariantExporterTestDB.SHEEP_FILE_1_ID)));
        assertTrue(sources.stream().anyMatch(s -> s.getFileId().equals(VariantExporterTestDB.SHEEP_FILE_2_ID)));
        assertTrue(sources.stream().allMatch(
                s -> s.getSamples().size() == VariantExporterTestDB.NUMBER_OF_SAMPLES_IN_SHEEP_FILES));

        // one study with two files, asking for both files
        sources = variantExporter.getSources(sheepVariantSourceDBAdaptor, sheepStudy, Collections.EMPTY_LIST);
        assertEquals(2, sources.size());
        correctStudyId = sources.stream().allMatch(s -> s.getStudyId().equals(VariantExporterTestDB.SHEEP_STUDY_ID));
        assertTrue(correctStudyId);
        assertTrue(sources.stream().anyMatch(s -> s.getFileId().equals(VariantExporterTestDB.SHEEP_FILE_1_ID)));
        assertTrue(sources.stream().anyMatch(s -> s.getFileId().equals(VariantExporterTestDB.SHEEP_FILE_2_ID)));
        assertTrue(sources.stream().allMatch(
                s -> s.getSamples().size() == VariantExporterTestDB.NUMBER_OF_SAMPLES_IN_SHEEP_FILES));

        // one study with two files, asking just for a file
        sources = variantExporter
                .getSources(sheepVariantSourceDBAdaptor, sheepStudy, Collections.singletonList(VariantExporterTestDB.SHEEP_FILE_1_ID));
        assertEquals(1, sources.size());
        correctStudyId = sources.stream().allMatch(s -> s.getStudyId().equals(VariantExporterTestDB.SHEEP_STUDY_ID));
        assertTrue(correctStudyId);
        assertTrue(sources.stream().anyMatch(s -> s.getFileId().equals(VariantExporterTestDB.SHEEP_FILE_1_ID)));
        assertTrue(sources.stream().allMatch(
                s -> s.getSamples().size() == VariantExporterTestDB.NUMBER_OF_SAMPLES_IN_SHEEP_FILES));

        // empty study filter
        studies = new ArrayList<>();
        sources = variantExporter.getSources(variantSourceDBAdaptor, studies, Collections.EMPTY_LIST);
        assertEquals(0, sources.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void notExistingSourceShouldThrowException() {
        VariantExporter variantExporter = new VariantExporter();
        // The study with id "2" is not in database
        List<String> study = Collections.singletonList("2");
        variantExporter.getSources(variantSourceDBAdaptor, study, Collections.EMPTY_LIST);
    }

    @Test
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
    public void getVcfHeaders() throws IOException {
        VariantExporter variantExporter = new VariantExporter();
        String study7Id = "7";
        String study8Id = "8";
        List<String> studies = Arrays.asList(study7Id, study8Id);
        List<VariantSource> sources =
                variantExporter.getSources(variantSourceDBAdaptor, studies, Collections.EMPTY_LIST);

        Map<String, VCFHeader> headers = variantExporter.getVcfHeaders(sources);
        VCFHeader header = headers.get(study7Id);
        assertEquals(2504, header.getSampleNamesInOrder().size());
        assertTrue(header.hasGenotypingData());
        header = headers.get(study8Id);
        assertEquals(2504, header.getSampleNamesInOrder().size());
        assertTrue(header.hasGenotypingData());
    }

    @Test
    public void mergeVcfHeaders() throws IOException {
        VariantExporter variantExporter = new VariantExporter();
        List<String> cowStudyIds = Arrays.asList("PRJEB6119", "PRJEB7061");
        List<VariantSource> cowSources =
                variantExporter.getSources(cowVariantSourceDBAdaptor, cowStudyIds, Collections.EMPTY_LIST);
        VCFHeader header = variantExporter.getMergedVcfHeader(cowSources);

        // assert
        assertEquals(1, header.getContigLines().size());
        assertEquals(4, header.getInfoHeaderLines().size());
        assertEquals(2, header.getFormatHeaderLines().size());
    }

    @Test
    public void testExportOneStudy() throws Exception {
        List<String> studies = Collections.singletonList("7");
        String region = "20:61000-69000";
        QueryOptions query = new QueryOptions();
        List<VariantContext> exportedVariants = exportAndCheck(variantSourceDBAdaptor, variantDBAdaptor, query, studies,
                                                               Collections.EMPTY_LIST, region);
        checkExportedVariants(variantDBAdaptor, query, exportedVariants);
    }

    @Test
    public void testExportTwoStudies() throws Exception {
        List<String> studies = Arrays.asList("7", "8");
        String region = "20:61000-69000";
        QueryOptions query = new QueryOptions();
        List<VariantContext> exportedVariants = exportAndCheck(variantSourceDBAdaptor, variantDBAdaptor, query, studies,
                                                               Collections.EMPTY_LIST, region);
        checkExportedVariants(variantDBAdaptor, query, exportedVariants);
    }

    @Test
    public void testExportOneStudyThatHasNotSourceLines() throws Exception {
        List<String> studies = Collections.singletonList("PRJEB6119");
        String region = "21:820000-830000";
        QueryOptions query = new QueryOptions();
        exportAndCheck(cowVariantSourceDBAdaptor, cowVariantDBAdaptor, query, studies, Collections.EMPTY_LIST, region,
                       4);
    }

    @Test
    public void testExportOneFileFromOneStudyThatHasTwoFiles() throws Exception {
        List<String> studies = Collections.singletonList(VariantExporterTestDB.SHEEP_STUDY_ID);
        List<String> files = Collections.singletonList(VariantExporterTestDB.SHEEP_FILE_1_ID);
        String region = "14:10250000-10259999";
        QueryOptions query = new QueryOptions();
        List<VariantContext> exportedVariants =
                exportAndCheck(sheepVariantSourceDBAdaptor, sheepVariantDBAdaptor, query, studies, files, region);
        checkExportedVariants(sheepVariantDBAdaptor, query, exportedVariants);
        boolean samplesNumberCorrect =
                exportedVariants.stream().allMatch(
                        v -> v.getGenotypes().size() == VariantExporterTestDB.NUMBER_OF_SAMPLES_IN_SHEEP_FILES);
        assertTrue(samplesNumberCorrect);
    }

    // TODO: this test is not going to work as expected because ID and Region are an OR filter. Add annotation data to the test data
    //       and write a test filtering by annotation
//    @Test
//    public void textExportWithFilter() {
//        QueryOptions query = new QueryOptions();
//        query.put(VariantDBAdaptor.ID, "rs544625796");
//       //     query.put(VariantDBAdaptor.REFERENCE, "A");
//        List<String> studies = Collections.singletonList("7");
//        String region = "20:61000-69000";
//        Map<String, List<VariantContext>> exportedVariants = exportAndCheck(variantDBAdaptor, query, studies, region);
//        checkExportedVariants(variantDBAdaptor, query, studies, exportedVariants);
//                // annot-ct=SO%3A0001583
//
//    }

    private List<VariantContext> exportAndCheck(VariantSourceDBAdaptor variantSourceDBAdaptor,
                                                VariantDBAdaptor variantDBAdaptor, QueryOptions query,
                                                List<String> studies, List<String> files, String region) {
        return exportAndCheck(variantSourceDBAdaptor, variantDBAdaptor, query, studies, files, region, 0);
    }

    private List<VariantContext> exportAndCheck(VariantSourceDBAdaptor variantSourceDBAdaptor,
                                                VariantDBAdaptor variantDBAdaptor, QueryOptions query,
                                                List<String> studies, List<String> files,
                                                String region, int expectedFailedVariants) {
        VariantExporter variantExporter = new VariantExporter();
        query.put(VariantDBAdaptor.STUDIES, studies);
        query.add(VariantDBAdaptor.REGION, region);

        VariantDBIterator iterator = variantDBAdaptor.iterator(query);

        // we need to call 'getSources' before 'export' because it check if there are sample name conflicts and initialize some dependencies
        variantExporter.getSources(variantSourceDBAdaptor, studies, files);
        List<VariantContext> exportedVariants = variantExporter.export(iterator, new Region(region));

        assertEquals(expectedFailedVariants, variantExporter.getFailedVariants());

        return exportedVariants;
    }

    private void checkExportedVariants(VariantDBAdaptor variantDBAdaptor, QueryOptions query,
                                       List<VariantContext> exportedVariants) {
        VariantDBIterator iterator;

        long iteratorSize = 0;
        iterator = variantDBAdaptor.iterator(query);
        while (iterator.hasNext()) {
            Variant variant = iterator.next();
            assertTrue(variantInExportedVariantsCollection(variant, exportedVariants));
            iteratorSize++;
        }

        assertEquals(iteratorSize, exportedVariants.size());
    }

    private static boolean variantInExportedVariantsCollection(Variant variant, List<VariantContext> exportedVariants) {
        if (exportedVariants.stream().anyMatch(v -> sameVariant(variant, v))) {
            return true;
        }

        return false;
    }

    private static boolean sameVariant(Variant v1, VariantContext v2) {
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

    private static boolean sameStart(Variant v1, VariantContext v2) {
        if (v1.getReference().equals("") || v1.getAlternate().equals("")) {
            return v2.getStart() == (v1.getStart() - 1);
        } else {
            return v2.getStart() == v1.getStart();
        }
    }

    private VariantSource createTestVariantSource(String fileId, List<String> sampleList) {
        final VariantSource variantSource = new VariantSource("name", fileId, "studyId", "studyName");
        variantSource.setSamples(sampleList);
        return variantSource;
    }
}
