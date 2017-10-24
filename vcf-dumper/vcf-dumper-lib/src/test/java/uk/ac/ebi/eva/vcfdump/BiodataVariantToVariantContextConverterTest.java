/*
 *
 *  * Copyright 2016 EMBL - European Bioinformatics Institute
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package uk.ac.ebi.eva.vcfdump;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.ebi.eva.commons.core.models.Annotation;
import uk.ac.ebi.eva.commons.core.models.ConsequenceType;
import uk.ac.ebi.eva.commons.core.models.IConsequenceType;
import uk.ac.ebi.eva.commons.core.models.StudyType;
import uk.ac.ebi.eva.commons.core.models.VariantSource;
import uk.ac.ebi.eva.commons.core.models.factories.VariantVcfFactory;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.pipeline.VariantSourceEntry;
import uk.ac.ebi.eva.commons.core.models.ws.VariantSourceEntryWithSampleNames;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BiodataVariantToVariantContextConverterTest {

    public static final String FILE_ID = "fileId";

    private static VariantVcfFactory variantFactory;

    private static final String CHR_1 = "1";

    private static final String STUDY_1 = "study_1";

    private static ArrayList<String> s1s6SampleList;

    private static Map<String, Map<String, String>> noSampleNamesConflictSampleNameCorrections = null;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        variantFactory = new VariantVcfFactory();
        //Config.setOpenCGAHome(System.getenv("OPENCGA_HOME") != null ? System.getenv("OPENCGA_HOME") : "/opt/opencga");

        // example samples list
        s1s6SampleList = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            s1s6SampleList.add("s" + i);
        }
    }

    @Test
    public void singleStudySNV() {
        // create variant
        VariantSource variantSource = createTestVariantSource(STUDY_1);
        String variantLine = String
                .join("\t", CHR_1, "1000", "id", "C", "A", "100", "PASS", ".", "GT", "0|0", "0|0", "0|1", "1|1", "1|1",
                      "0|1");
        List<Variant> variants = variantFactory.create(CHR_1, STUDY_1 , variantLine);
        assertEquals(1, variants.size());
        VariantWithSamplesAndAnnotation variantSA = new VariantWithSamplesAndAnnotation(variants.get(0));

        // export variant
        BiodataVariantToVariantContextConverter variantConverter =
                new BiodataVariantToVariantContextConverter(Collections.singletonList(variantSource),
                                                            noSampleNamesConflictSampleNameCorrections);
        VariantContext variantContext = variantConverter.transform(variantSA);
        checkVariantContext(variantContext, CHR_1, 1000, 1000, "C", "A", variantSA.getSourceEntries(), false);
    }

    @Test
    public void singleStudySingleNucleotideInsertion() {
        // create variant
        VariantSource variantSource = createTestVariantSource(STUDY_1);
        String variantLine = String
                .join("\t", CHR_1, "1100", "id", "T", "TG", "100", "PASS", ".", "GT", "0|0", "0|0", "0|1", "1|1", "1|1",
                      "0|1");
        List<Variant> variants = variantFactory.create(CHR_1, STUDY_1 , variantLine);
        assertEquals(1, variants.size());
        VariantWithSamplesAndAnnotation variantSA = new VariantWithSamplesAndAnnotation(variants.get(0));

        // export variant
        BiodataVariantToVariantContextConverter variantConverter =
                new BiodataVariantToVariantContextConverter(Collections.singletonList(variantSource),
                                                            noSampleNamesConflictSampleNameCorrections);
        VariantContext variantContext = variantConverter.transform(variantSA);
        checkVariantContext(variantContext, CHR_1, 1100, 1100, "T", "TG", variantSA.getSourceEntries(), false);
    }

    @Test
    public void singleStudySeveralNucleotidesInsertion() {
        // create variant
        VariantSource variantSource = createTestVariantSource(STUDY_1);
        String variantLine = String
                .join("\t", CHR_1, "1100", "id", "T", "TGA", "100", "PASS", ".", "GT", "0|0", "0|0", "0|1", "1|1",
                      "1|1", "0|1");
        List<Variant> variants = variantFactory.create(CHR_1, STUDY_1 , variantLine);
        assertEquals(1, variants.size());
        VariantWithSamplesAndAnnotation variantSA = new VariantWithSamplesAndAnnotation(variants.get(0));

        // export variant
        BiodataVariantToVariantContextConverter variantConverter =
                new BiodataVariantToVariantContextConverter(Collections.singletonList(variantSource),
                                                            noSampleNamesConflictSampleNameCorrections);
        VariantContext variantContext = variantConverter.transform(variantSA);
        checkVariantContext(variantContext, CHR_1, 1100, 1100, "T", "TGA", variantSA.getSourceEntries(), false);
    }

    @Test
    public void singleStudySingleNucleotideDeletion() {
        // create variant
        VariantSource variantSource = createTestVariantSource(STUDY_1);
        String variantLine = String
                .join("\t", CHR_1, "1100", "id", "TA", "T", "100", "PASS", ".", "GT", "0|0", "0|0", "0|1", "1|1", "1|1",
                      "0|1");
        List<Variant> variants = variantFactory.create(CHR_1, STUDY_1 , variantLine);
        assertEquals(1, variants.size());
        VariantWithSamplesAndAnnotation variantSA = new VariantWithSamplesAndAnnotation(variants.get(0));

        // export variant
        BiodataVariantToVariantContextConverter variantConverter =
                new BiodataVariantToVariantContextConverter(Collections.singletonList(variantSource),
                                                            noSampleNamesConflictSampleNameCorrections);
        VariantContext variantContext = variantConverter.transform(variantSA);
        checkVariantContext(variantContext, CHR_1, 1100, 1101, "TA", "T", variantSA.getSourceEntries(), false);
    }

    @Test
    public void singleStudySeveralNucleotidesDeletion() {
        // create variant
        VariantSource variantSource = createTestVariantSource(STUDY_1);
        String variantLine = String
                .join("\t", CHR_1, "1100", "id", "TAG", "T", "100", "PASS", ".", "GT", "0|0", "0|0", "0|1", "1|1",
                      "1|1", "0|1");
        List<Variant> variants = variantFactory.create(CHR_1, STUDY_1 , variantLine);
        assertEquals(1, variants.size());
        VariantWithSamplesAndAnnotation variantSA = new VariantWithSamplesAndAnnotation(variants.get(0));

        // export variant
        BiodataVariantToVariantContextConverter variantConverter =
                new BiodataVariantToVariantContextConverter(Collections.singletonList(variantSource),
                                                            noSampleNamesConflictSampleNameCorrections);
        VariantContext variantContext = variantConverter.transform(variantSA);
        checkVariantContext(variantContext, CHR_1, 1100, 1102, "TAG", "T", variantSA.getSourceEntries(), false);
    }

    @Test
    public void singleStudyMultiAllelicVariant() {
        // create variant
        VariantSource variantSource = createTestVariantSource(STUDY_1);
        String variantLine = String
                .join("\t", CHR_1, "1000", "id", "C", "A,T", "100", "PASS", ".", "GT", "0|0", "0|2", "0|1", "1|1",
                      "1|2", "2|2");
        List<Variant> variants = variantFactory.create(CHR_1, STUDY_1 , variantLine);
        assertEquals(2, variants.size());
        VariantWithSamplesAndAnnotation variantSA1 = new VariantWithSamplesAndAnnotation(variants.get(0));
        VariantWithSamplesAndAnnotation variantSA2 = new VariantWithSamplesAndAnnotation(variants.get(1));

        // export variant
        BiodataVariantToVariantContextConverter variantConverter =
                new BiodataVariantToVariantContextConverter(Collections.singletonList(variantSource),
                                                            noSampleNamesConflictSampleNameCorrections);
        VariantContext variant1Context = variantConverter.transform(variantSA1);
        VariantContext variant2Context = variantConverter.transform(variantSA2);

        checkVariantContext(variant1Context, CHR_1, 1000, 1000, "C", "A", variantSA1.getSourceEntries(), false);
        checkVariantContext(variant2Context, CHR_1, 1000, 1000, "C", "T", variantSA2.getSourceEntries(), false);
    }

    @Test
    public void singleStudyMultiAllelicIndel() {
        // create variant
        VariantSource variantSource = createTestVariantSource(STUDY_1);
        String variantLine = String
                .join("\t", CHR_1, "1000", "id", "C", "CA,T", "100", "PASS", ".", "GT", "0|0", "0|2", "0|1", "1|1",
                      "1|2", "2|2");
        List<Variant> variants = variantFactory.create(CHR_1, STUDY_1 , variantLine);
        assertEquals(2, variants.size());
        VariantWithSamplesAndAnnotation variantSA1 = new VariantWithSamplesAndAnnotation(variants.get(0));
        VariantWithSamplesAndAnnotation variantSA2 = new VariantWithSamplesAndAnnotation(variants.get(1));

        // export variant
        BiodataVariantToVariantContextConverter variantConverter =
                new BiodataVariantToVariantContextConverter(Collections.singletonList(variantSource),
                                                            noSampleNamesConflictSampleNameCorrections);
        VariantContext variant1Context = variantConverter.transform(variantSA1);
        VariantContext variant2Context = variantConverter.transform(variantSA2);

        checkVariantContext(variant1Context, CHR_1, 1000, 1000, "C", "CA", variantSA1.getSourceEntries(), false);
        checkVariantContext(variant2Context, CHR_1, 1000, 1000, "C", "T", variantSA2.getSourceEntries(), false);
    }

    @Test
    public void complexVariant() {
        // create variant
        VariantSource variantSource = createTestVariantSource(STUDY_1);
        String variantLine = String
                .join("\t", CHR_1, "1000", "id", "TAC", "TACT,TC", "100", "PASS", ".", "GT", "0|0", "0|2", "0|1", "1|1",
                      "1|2", "2|2");
        List<Variant> variants = variantFactory.create(CHR_1, STUDY_1 , variantLine);
        assertEquals(2, variants.size());
        VariantWithSamplesAndAnnotation variantSA1 = new VariantWithSamplesAndAnnotation(variants.get(0));
        VariantWithSamplesAndAnnotation variantSA2 = new VariantWithSamplesAndAnnotation(variants.get(1));

        // export variant
        BiodataVariantToVariantContextConverter variantConverter =
                new BiodataVariantToVariantContextConverter(Collections.singletonList(variantSource),
                                                            noSampleNamesConflictSampleNameCorrections);
        VariantContext variant1Context = variantConverter.transform(variantSA1);
        VariantContext variant2Context = variantConverter.transform(variantSA2);

        checkVariantContext(variant1Context, CHR_1, 1002, 1002, "C", "CT", variantSA1.getSourceEntries(), false);
        checkVariantContext(variant2Context, CHR_1, 1000, 1001, "TA", "T", variantSA2.getSourceEntries(), false);
    }

    @Test
    public void singleNucleotideInsertionInPosition1() {
        // create SNV variant
        VariantSource variantSource = createTestVariantSource(STUDY_1);
        String variantLine = String
                .join("\t", CHR_1, "1", "id", "A", "TA", "100", "PASS", ".", "GT", "0|0", "0|0", "0|1", "1|1", "1|1",
                      "0|1");
        List<Variant> variants = variantFactory.create(CHR_1, STUDY_1 , variantLine);
        assertEquals(1, variants.size());
        VariantWithSamplesAndAnnotation variantSA = new VariantWithSamplesAndAnnotation(variants.get(0));

        // export variant
        BiodataVariantToVariantContextConverter variantConverter =
                new BiodataVariantToVariantContextConverter(Collections.singletonList(variantSource),
                                                            noSampleNamesConflictSampleNameCorrections);
        VariantContext variantContext = variantConverter.transform(variantSA);
        checkVariantContext(variantContext, CHR_1, 1, 1, "A", "TA", variantSA.getSourceEntries(), false);
    }

    @Test
    public void singleNucleotideDeletionInPosition1() {
        // create SNV variant
        VariantSource variantSource = createTestVariantSource(STUDY_1);
        String variantLine = String
                .join("\t", CHR_1, "1", "id", "AT", "T", "100", "PASS", ".", "GT", "0|0", "0|0", "0|1", "1|1", "1|1",
                      "0|1");
        List<Variant> variants = variantFactory.create(CHR_1, STUDY_1 , variantLine);
        assertEquals(1, variants.size());
        VariantWithSamplesAndAnnotation variantSA = new VariantWithSamplesAndAnnotation(variants.get(0));

        // export variant
        BiodataVariantToVariantContextConverter variantConverter =
                new BiodataVariantToVariantContextConverter(Collections.singletonList(variantSource),
                                                            noSampleNamesConflictSampleNameCorrections);
        VariantContext variantContext = variantConverter.transform(variantSA);
        checkVariantContext(variantContext, CHR_1, 1, 2, "AT", "T",variantSA.getSourceEntries(), false);
    }


    @Test
    public void severalNucleotidesInsertionInPosition1() {
        // create SNV variant
        VariantSource variantSource = createTestVariantSource(STUDY_1);
        String variantLine = String
                .join("\t", CHR_1, "1", "id", "A", "GGTA", "100", "PASS", ".", "GT", "0|0", "0|0", "0|1", "1|1", "1|1",
                      "0|1");
        List<Variant> variants = variantFactory.create(CHR_1, STUDY_1 , variantLine);
        assertEquals(1, variants.size());
        VariantWithSamplesAndAnnotation variantSA = new VariantWithSamplesAndAnnotation(variants.get(0));

        // export variant
        BiodataVariantToVariantContextConverter variantConverter =
                new BiodataVariantToVariantContextConverter(Collections.singletonList(variantSource),
                                                            noSampleNamesConflictSampleNameCorrections);
        VariantContext variantContext = variantConverter.transform(variantSA);
        checkVariantContext(variantContext, CHR_1, 1, 1, "A", "GGTA", variantSA.getSourceEntries(), false);
    }

    @Test
    public void severalNucleotidesDeletionInPosition1() {
        // create SNV variant
        VariantSource variantSource = createTestVariantSource(STUDY_1);
        String variantLine = String
                .join("\t", CHR_1, "1", "id", "ATTG", "G", "100", "PASS", ".", "GT", "0|0", "0|0", "0|1", "1|1", "1|1",
                      "0|1");
        List<Variant> variants = variantFactory.create(CHR_1, STUDY_1 , variantLine);
        assertEquals(1, variants.size());
        VariantWithSamplesAndAnnotation variantSA = new VariantWithSamplesAndAnnotation(variants.get(0));

        // export variant
        BiodataVariantToVariantContextConverter variantConverter =
                new BiodataVariantToVariantContextConverter(Collections.singletonList(variantSource),
                                                            noSampleNamesConflictSampleNameCorrections);
        VariantContext variantContext = variantConverter.transform(variantSA);
        System.out.println(variantContext);
        checkVariantContext(variantContext, CHR_1, 1, 4, "ATTG", "G", variantSA.getSourceEntries(), false);
    }

    @Test
    public void twoStudiesNoConflictingNamesSingleVariant() {
        // create test variant, with two studies and samples with not conflicting names
        VariantWithSamplesAndAnnotation variant = new VariantWithSamplesAndAnnotation(CHR_1, 1000, 1000, "T", "G");

        VariantSource source1 = new VariantSource("testFile1", "file_1", "study_1", "testStudy1", null, null, null, null, null, null);
        //source1.setSamples(Arrays.asList("SX_1", "SX_2", "SX_3", "SX_4"));
        VariantSourceEntryWithSampleNames study1Entry = null;// new VariantSourceEntryWithSampleNames("file_1", "study_1");
        addGenotype(study1Entry, "SX_1", "0|0");
        addGenotype(study1Entry, "SX_2", "0|1");
        addGenotype(study1Entry, "SX_3", "0|1");
        addGenotype(study1Entry, "SX_4", "0|0");
        variant.addSourceEntry(study1Entry);

        VariantSource source2 = new VariantSource("testFile2", "file_2", "study_2", "testStudy2", null, null, null, null, null, null);
        //source1.setSamples(Arrays.asList("SY_1", "SY_2", "SY_3", "SY_4", "SY_5", "SY_6"));
        VariantSourceEntryWithSampleNames study2Entry = null; //new VariantSourceEntry("file_2", "study_2");
        addGenotype(study2Entry, "SY_1", "0|0");
        addGenotype(study2Entry, "SY_2", "1|0");
        addGenotype(study2Entry, "SY_3", "1|1");
        addGenotype(study2Entry, "SY_4", "1|1");
        addGenotype(study2Entry, "SY_5", "0|0");
        addGenotype(study2Entry, "SY_6", "1|0");
        variant.addSourceEntry(study2Entry);

        // transform variant
        BiodataVariantToVariantContextConverter variantConverter = new BiodataVariantToVariantContextConverter(
                Arrays.asList(source1, source2), noSampleNamesConflictSampleNameCorrections);
        VariantContext variantContext = variantConverter.transform(variant);

        // check transformed variant
        //Set<String> sampleNames = new HashSet<>(source1.getSamples());
        //sampleNames.addAll(source2.getSamples());
        checkVariantContext(variantContext, CHR_1, 1000, 1000, "T", "G", variant.getSourceEntries(), false);
    }

    @Test
    public void twoStudiesConflictingNamesSingleVariant() {
        // create test variant, with two studies and samples with not conflicting names
        VariantWithSamplesAndAnnotation variant = new VariantWithSamplesAndAnnotation(CHR_1, 1000, 1000, "T", "G");

        // studies and samples names
        String study1 = "study_1";
        String study2 = "study_2";
        String file1 = "file_1";
        String file2 = "file_2";
        String sampleX1 = "SX_1";
        String sampleX2 = "SX_2";
        String sampleX3 = "SX_3";
        String sampleX4 = "SX_4";
        String sampleX5 = "SX_5";
        String sampleX6 = "SX_6";

        // sample name corrections map (usually created by VariantExporter)
        Map<String, Map<String, String>> sampleNamesCorrections = new HashMap<>();
        Map<String, String> file1SampleNameCorrections = new HashMap<>();
        file1SampleNameCorrections.put(sampleX1, file1 + "_" + sampleX1);
        file1SampleNameCorrections.put(sampleX2, file1 + "_" + sampleX2);
        file1SampleNameCorrections.put(sampleX3, file1 + "_" + sampleX3);
        file1SampleNameCorrections.put(sampleX4, file1 + "_" + sampleX4);
        sampleNamesCorrections.put(file1, file1SampleNameCorrections);
        Map<String, String> file2SampleNameCorrections = new HashMap<>();
        file2SampleNameCorrections.put(sampleX1, file2 + "_" + sampleX1);
        file2SampleNameCorrections.put(sampleX2, file2 + "_" + sampleX2);
        file2SampleNameCorrections.put(sampleX3, file2 + "_" + sampleX3);
        file2SampleNameCorrections.put(sampleX4, file2 + "_" + sampleX4);
        file2SampleNameCorrections.put(sampleX5, file2 + "_" + sampleX5);
        file2SampleNameCorrections.put(sampleX6, file2 + "_" + sampleX6);
        sampleNamesCorrections.put(file2, file2SampleNameCorrections);

        // variant sources
        VariantSource source1 = createTestVariantSource(study1, file1, "testStudy1", "testFile1",
                Arrays.asList(sampleX1, sampleX2, sampleX3, sampleX4));
        //source1.setSamples(Arrays.asList(sampleX1, sampleX2, sampleX3, sampleX4));
        VariantSourceEntry source1EntryWithoutSamples = new VariantSourceEntry(file1, study1);
        VariantSourceEntryWithSampleNames source1Entry = new VariantSourceEntryWithSampleNames(source1EntryWithoutSamples, new ArrayList<>());
        addGenotype(source1Entry, sampleX1, "0|0");
        addGenotype(source1Entry, sampleX2, "0|1");
        addGenotype(source1Entry, sampleX3, "0|1");
        addGenotype(source1Entry, sampleX4, "0|0");
        variant.addSourceEntry(source1Entry);
        VariantSource source2 = createTestVariantSource(study2, file2, "testStudy2", "testFile2",
                Arrays.asList(sampleX1, sampleX2, sampleX3, sampleX4, sampleX5, sampleX6));
        //source2.setSamples(Arrays.asList(sampleX1, sampleX2, sampleX3, sampleX4, sampleX5, sampleX6));
        VariantSourceEntry source2EntryWithoutSamples = new VariantSourceEntry(file1, study1);
        VariantSourceEntryWithSampleNames source2Entry = new VariantSourceEntryWithSampleNames(source2EntryWithoutSamples, new ArrayList<>());
        addGenotype(source2Entry, sampleX1, "0|0");
        addGenotype(source2Entry, sampleX2, "1|0");
        addGenotype(source2Entry, sampleX3, "1|1");
        addGenotype(source2Entry, sampleX4, "-1|-1");
        addGenotype(source2Entry, sampleX5, "0|0");
        addGenotype(source2Entry, sampleX6, "1|0");
        variant.addSourceEntry(source2Entry);

        // transform variant
        BiodataVariantToVariantContextConverter variantConverter = new BiodataVariantToVariantContextConverter(
                Arrays.asList(source1, source2), sampleNamesCorrections);
        VariantContext variantContext = variantConverter.transform(variant);

        // check transformed variant
        Set<String> sampleNames = source1.getSamplesPosition().keySet().stream().map(s -> source1Entry.getFileId() + "_" + s)
                                         .collect(Collectors.toSet());
        sampleNames.addAll(source2.getSamplesPosition().keySet().stream().map(s -> source2Entry.getFileId() + "_" + s)
                                  .collect(Collectors.toSet()));
        checkVariantContext(variantContext, CHR_1, 1000, 1000, "T", "G", variant.getSourceEntries(), true);
    }


    @Test
    public void csqAnnotation() {
        // create variant
        VariantSource variantSource = createTestVariantSource(STUDY_1);
        String variantLine = String.join(
                "\t", CHR_1, "1000", "id", "C", "A", "100", "PASS", ".", "GT", "0|0", "0|0", "0|1", "1|1", "1|1", "0|1");
        List<Variant> variants = variantFactory.create(CHR_1, STUDY_1 , variantLine);
        assertEquals(1, variants.size());
        VariantWithSamplesAndAnnotation variantSA = new VariantWithSamplesAndAnnotation(variants.get(0));

        Set<IConsequenceType> consequenceTypes = new HashSet<>();
        Set<Integer> soNames1 = new HashSet<>(Arrays.asList(1, 3));
        Set<Integer> soNames2 = new HashSet<>(Arrays.asList(2));

        ConsequenceType consequenceType = new ConsequenceType("gene", "ensembleGeneId",
                                                              "EnsembleTransId", "strand",
                                                              "bioType", 10, 10, 10,
                                                              "aaChange", "codon", null, null, soNames1, 0);
        ConsequenceType consequenceType2 = new ConsequenceType("gene2", null,
                                                               "EnsembleTransId2", "strand2",
                                                               "", 20, 20, 20,
                                                               "aaChange2", "codon2", null, null, soNames2, 0);
        consequenceTypes.add(consequenceType);
        consequenceTypes.add(consequenceType2);
        //variants.get(0).getAnnotation().setConsequenceTypes(consequenceTypes);

        // populate variant with test csq data
        Annotation annotation = new Annotation(variantSA.getChromosome(), variantSA.getStart(), variantSA.getEnd(),
                "", "", null, consequenceTypes);
//        variantSA.getAnnotation().setAlternativeAllele("A");
        variantSA.setAnnotation(annotation);

        // export variant
        BiodataVariantToVariantContextConverter variantConverter =
                new BiodataVariantToVariantContextConverter(Collections.singletonList(variantSource),
                                                            noSampleNamesConflictSampleNameCorrections);
        VariantContext variantContext = variantConverter.transform(variantSA);

        // test if CSQ is properly transformed
        assertFalse(variantContext.getCommonInfo().getAttributes().isEmpty());
        String csq = (String) variantContext.getCommonInfo().getAttribute("CSQ");
        assertNotNull(csq);
        assertEquals(
                "A|regulatory_region_ablation&3_prime_UTR_variant|gene|ensembleGeneId|EnsembleTransId|bioType|10|10,A|feature_elongation|gene2||EnsembleTransId2||20|20", csq);
    }

    @Test
    public void csqAnnotationWithoutSoTermsAndGeneName() {
        // create variant
        VariantSource variantSource = createTestVariantSource(STUDY_1);
        String variantLine = String.join(
                "\t", CHR_1, "1000", "id", "C", "A", "100", "PASS", ".", "GT", "0|0", "0|0", "0|1", "1|1", "1|1", "0|1");
        List<Variant> variants = variantFactory.create(CHR_1, STUDY_1 , variantLine);
        assertEquals(1, variants.size());
        VariantWithSamplesAndAnnotation variantSA = new VariantWithSamplesAndAnnotation(variants.get(0));

        Set<IConsequenceType> consequenceTypes = new HashSet<>();
        List<String> soNames1 = new ArrayList<>();
        List<String> soNames2 = new ArrayList<>();
        ConsequenceType consequenceType = new ConsequenceType(null, "ensembleGeneId",
                                                              "EnsembleTransId", "strand",
                                                              "bioType", 10, 10, 10,
                                                              "aaChange", "codon", null, null, null, 0);
        ConsequenceType consequenceType2 = new ConsequenceType("", null,
                                                               "EnsembleTransId2", "strand2",
                                                               "", 20, 20, 20,
                                                               "aaChange2", "codon2", null, null, null, 0);
        consequenceTypes.add(consequenceType);
        consequenceTypes.add(consequenceType2);

        // populate variant with test csq data
        Annotation annotation = new Annotation(variantSA.getChromosome(), variantSA.getStart(), variantSA.getEnd(),
                "", "", null, consequenceTypes);
//        variantSA.getAnnotation().setAlternativeAllele("A");
        variantSA.setAnnotation(annotation);

        // export variant
        BiodataVariantToVariantContextConverter variantConverter =
                new BiodataVariantToVariantContextConverter(Collections.singletonList(variantSource),
                                                            noSampleNamesConflictSampleNameCorrections);
        VariantContext variantContext = variantConverter.transform(variantSA);

        // test if CSQ is properly transformed
        assertFalse(variantContext.getCommonInfo().getAttributes().isEmpty());
        String csq = (String) variantContext.getCommonInfo().getAttribute("CSQ");
        assertNotNull(csq);
        assertEquals("A|||ensembleGeneId|EnsembleTransId|bioType|10|10,A||||EnsembleTransId2||20|20", csq);
    }

    @Test
    public void testNoCsqAnnotationCreatedFromVariantWithoutInfo() {
        // create variant
        VariantSource variantSource = createTestVariantSource(STUDY_1);
        String variantLine = String.join(
                "\t", CHR_1, "1000", "id", "C", "A", "100", "PASS", ".", "GT", "0|0", "0|0", "0|1", "1|1", "1|1", "0|1");
        List<Variant> variants = variantFactory.create(CHR_1, STUDY_1 , variantLine);
        assertEquals(1, variants.size());
        VariantWithSamplesAndAnnotation variantSA = new VariantWithSamplesAndAnnotation(variants.get(0));


        // export variant
        BiodataVariantToVariantContextConverter variantConverter =
                new BiodataVariantToVariantContextConverter(Collections.singletonList(variantSource),
                                                            noSampleNamesConflictSampleNameCorrections);
        VariantContext variantContext = variantConverter.transform(variantSA);

        // test if CSQ is not present
        assertTrue(variantContext.getCommonInfo().getAttributes().isEmpty());
        String csq = (String) variantContext.getCommonInfo().getAttribute("CSQ");
        assertNull(csq);
    }


    private void addGenotype(VariantSourceEntryWithSampleNames sourceEntry, String sampleName, String genotype) {
        Map<String, String> sampleData = new HashMap<>();
        sampleData.put("GT", genotype);
        //sourceEntry.addSampleData(sampleName, sampleData);
    }

    private void checkVariantContext(VariantContext variantContext, String chromosome, int start, int end, String ref,
                                     String alt,
                                     Collection<VariantSourceEntryWithSampleNames> sourceEntries, boolean sampleNameConflicts) {
        assertEquals(chromosome, variantContext.getContig());
        assertEquals(start, variantContext.getStart());
        assertEquals(end, variantContext.getEnd());
        assertEquals(Allele.create(ref, true), variantContext.getReference());
        assertEquals(Collections.singletonList(Allele.create(alt, false)), variantContext.getAlternateAlleles());
        assertTrue(variantContext.emptyID());
        assertTrue(variantContext.getFilters().isEmpty());
        assertEquals(0, variantContext.getCommonInfo().getAttributes().size());
        checkGenotypes(sourceEntries, variantContext, sampleNameConflicts);
    }

    private void checkGenotypes(Collection<VariantSourceEntryWithSampleNames> sourceEntries, VariantContext variantContext,
                                boolean sampleNameConflicts) {
        // check that variantContext has the same number of samples than the input variant
        int inputVariantsSampleCount = sourceEntries.stream()
                                                    .mapToInt(variantSourceEntry -> variantSourceEntry.getSamplesData()
                                                                                                      .size()).sum();
        assertEquals(inputVariantsSampleCount, variantContext.getSampleNames().size());

        //for (Map.Entry<String, VariantSourceEntryWithSampleNames> sourcesMapEntry : sourceEntries) {
        //    checkStudyGenotypes(sourcesMapEntry.getValue().getFileId(), sourcesMapEntry.getValue(), variantContext,
        //                        sampleNameConflicts);
        //}
    }

    private void checkStudyGenotypes(String fileId, VariantSourceEntryWithSampleNames sourceEntry, VariantContext variantContext,
                                     boolean sampleNameConflicts) {
        for (String sample : sourceEntry.getSamplesDataMap().keySet()) {
            String inputVariantSampleGenotype = null;//sourceEntry.getSampleData(sample, "GT");
            String sampleNameInOutputVariant;
            if (sampleNameConflicts) {
                sampleNameInOutputVariant = fileId + "_" + sample;
            } else {
                sampleNameInOutputVariant = sample;
            }
            Genotype outputVariantSampleGenotype = variantContext.getGenotype(sampleNameInOutputVariant);
            String[] inputAlleles = inputVariantSampleGenotype.split("/|\\|");
            compareAlleles(Integer.valueOf(inputAlleles[0]), outputVariantSampleGenotype.getAllele(0));
            compareAlleles(Integer.valueOf(inputAlleles[1]), outputVariantSampleGenotype.getAllele(1));
            assertEquals(inputVariantSampleGenotype.charAt(inputAlleles[0].length()) == '|',
                         outputVariantSampleGenotype.isPhased());
        }
    }

    private void compareAlleles(int inputAlleleIndex, Allele allele) {
        if (inputAlleleIndex == 0) {
            assertTrue(allele.isReference());
        } else {
            assertTrue(allele.isNonReference());
        }
    }

    private VariantSource createTestVariantSource(String studyId) {
        return createTestVariantSource(studyId, FILE_ID, "studyName", "name", s1s6SampleList);
    }

    private VariantSource createTestVariantSource(String studyId, String fileId, String studyName, String fileName, List<String> sampleList) {
        Map<String, Integer> samplesPosition = new HashMap<>();
        int index = sampleList.size();
        for (String s : sampleList) {
            samplesPosition.put(s, index++);
        }
        final VariantSource variantSource = new VariantSource(fileId, fileName, studyId, studyName, StudyType.AGGREGATE,
                                                              null, null, samplesPosition, null, null);
        return variantSource;
    }

}