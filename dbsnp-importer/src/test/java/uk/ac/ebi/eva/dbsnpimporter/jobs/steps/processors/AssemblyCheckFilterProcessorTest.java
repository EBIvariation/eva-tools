/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.ebi.eva.dbsnpimporter.models.LocusType;
import uk.ac.ebi.eva.dbsnpimporter.models.Orientation;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;
import uk.ac.ebi.eva.dbsnpimporter.io.FastaSequenceReader;

import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AssemblyCheckFilterProcessorTest {

    private static FastaSequenceReader fastaSequenceReader;

    private static AssemblyCheckFilterProcessor assemblyChecker;

    @BeforeClass
    public static void setUpClass() throws Exception {
        fastaSequenceReader = new FastaSequenceReader(Paths.get("src/test/resources/Gallus_gallus-5.0.test.fa"));
        assemblyChecker = new AssemblyCheckFilterProcessor(fastaSequenceReader);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        fastaSequenceReader.close();
    }

    @Test
    public void snpWithValidReferenceAllele() throws Exception {
        SubSnpCoreFields snpWithValidReference = new SubSnpCoreFields(1552148096L, Orientation.FORWARD, 737513389L,
                                                                      Orientation.FORWARD, "NT_455997.1", 1340646L,
                                                                      1340646L, Orientation.REVERSE, LocusType.SNP,
                                                                      "22", 2147L, 2147L, "A", "A", "G", "A/G",
                                                                      "NC_006109.4:g.2147T>C", 2147L, 2147L,
                                                                      Orientation.REVERSE, "NT_455997.1:g.1340646A>G",
                                                                      1340646L, 1340646L, Orientation.FORWARD, null,
                                                                      null, "1062063");

        assertNotNull(assemblyChecker.process(snpWithValidReference));
    }

    @Test
    public void snpWithInvalidReferenceAllele() throws Exception {
        SubSnpCoreFields snpWithInvalidReference = new SubSnpCoreFields(1552148096L, Orientation.FORWARD, 737513389L,
                                                                      Orientation.FORWARD, "NT_455997.1", 1340646L,
                                                                      1340646L, Orientation.REVERSE, LocusType.SNP,
                                                                      "22", 2147L, 2147L, "C", "C", "G", "C/G",
                                                                      "NC_006109.4:g.2147G>C", 2147L, 2147L,
                                                                      Orientation.REVERSE, "NT_455997.1:g.1340646C>G",
                                                                      1340646L, 1340646L, Orientation.FORWARD, null,
                                                                      null, "1062063");

        assertNull(assemblyChecker.process(snpWithInvalidReference));
    }

    @Test
    public void insertionWithEmptyReference() throws Exception {
        SubSnpCoreFields insertionWithValidReference = new SubSnpCoreFields(26508264L, Orientation.FORWARD, 13725276L,
                                                                            Orientation.REVERSE, "AADN04000814.1",
                                                                            1325045L, 1325046L, Orientation.REVERSE,
                                                                            LocusType.INSERTION, "22", 17747L, 17748L,
                                                                            "-", "-", "C", "-/C",
                                                                            "NC_006109.4:g.17747_17748insC", 17747L,
                                                                            17748L, Orientation.FORWARD,
                                                                            "NT_455997.1:g.1325045_1325046insG",
                                                                            1325045L, 1325046L, Orientation.REVERSE,
                                                                            null, null, "11828");

        assertNotNull(assemblyChecker.process(insertionWithValidReference));
    }

    @Test
    public void deletionWithValidReferenceAllele() throws Exception {
        SubSnpCoreFields deletionWithValidReference = new SubSnpCoreFields(1545663038L, Orientation.FORWARD, 738583051L,
                                                                           Orientation.REVERSE, "NT_455997.1", 1330026L,
                                                                           1330028L, Orientation.REVERSE,
                                                                           LocusType.DELETION, "22", 12765L, 12767L,
                                                                           "ACA", "ACA", null, "ACA/-",
                                                                           "NC_006109.4:g.12765_12767delACA", 12765L,
                                                                           12767L, Orientation.FORWARD,
                                                                           "NT_455997.1:g.1330026_1330028delTGT",
                                                                           1330026L, 1330028L, Orientation.REVERSE,
                                                                           null, null, "1062064");

        assertNotNull(assemblyChecker.process(deletionWithValidReference));
    }

    @Test
    public void deletionWithInvalidReferenceAllele() throws Exception {
        SubSnpCoreFields deletionWithInvalidReference = new SubSnpCoreFields(1545663038L, Orientation.FORWARD,
                                                                             738583051L, Orientation.REVERSE,
                                                                             "NT_455997.1", 1330026L, 1330028L,
                                                                             Orientation.REVERSE, LocusType.DELETION,
                                                                             "22", 12765L, 12767L, "ATA", "ATA", null,
                                                                             "ATA/-", "NC_006109.4:g.12765_12767delATA",
                                                                             12765L, 12767L, Orientation.FORWARD,
                                                                             "NT_455997.1:g.1330026_1330028delTAT",
                                                                             1330026L, 1330028L, Orientation.REVERSE,
                                                                             null, null, "1062064");

        assertNull(assemblyChecker.process(deletionWithInvalidReference));
    }

    
    @Test
    public void indelWithValidReferenceAllele() throws Exception {
        SubSnpCoreFields indelithValidReference = new SubSnpCoreFields(317288161L, Orientation.FORWARD, 431853804L,
                                                                      Orientation.FORWARD, "NT_455997.1", 1340260L,
                                                                      1340261L, Orientation.REVERSE,
                                                                      LocusType.SHORTER_ON_CONTIG, "22", 2532L, 2533L,
                                                                      "TC", "TC", "A", "A/C",
                                                                      "NC_006109.4:g.2532_2533delGAinsT", 2532L, 2533L,
                                                                      Orientation.REVERSE,
                                                                      "NT_455997.1:g.1340260_1340261delTCinsA",
                                                                      1340260L, 1340261L, Orientation.FORWARD,
                                                                      null, null, "1055116");

        assertNotNull(assemblyChecker.process(indelithValidReference));
    }

    @Test
    public void indelWithInvalidReferenceAllele() throws Exception {
        SubSnpCoreFields indelWithInvalidReference = new SubSnpCoreFields(317288161L, Orientation.FORWARD, 431853804L,
                                                                        Orientation.FORWARD, "NT_455997.1", 1340260L,
                                                                        1340261L, Orientation.REVERSE,
                                                                        LocusType.SHORTER_ON_CONTIG, "22", 2532L, 2533L,
                                                                        "TT", "TT", "A", "A/C",
                                                                        "NC_006109.4:g.2532_2533delGGinsT", 2532L,
                                                                        2533L, Orientation.REVERSE,
                                                                        "NT_455997.1:g.1340260_1340261delTTinsA",
                                                                        1340260L, 1340261L, Orientation.FORWARD, null,
                                                                        null, "1055116");

        assertNull(assemblyChecker.process(indelWithInvalidReference));
    }

    @Test
    public void insertionWithNonExistentContig() throws Exception {
        SubSnpCoreFields insertionWithNonExistentContig = new SubSnpCoreFields(26508264L, Orientation.FORWARD, 13725276L,
                                                                               Orientation.REVERSE, "AADN04000815.1",
                                                                               1325045L, 1325046L, Orientation.REVERSE,
                                                                               LocusType.INSERTION, null, null, null,
                                                                               "-", "-", "C", "-/C",
                                                                               "NC_006109.4:g.17747_17748insC", 17747L,
                                                                               17748L, Orientation.FORWARD,
                                                                               "NT_455997.1:g.1325045_1325046insG",
                                                                               1325045L, 1325046L, Orientation.REVERSE,
                                                                               null, null, "11828");

        assertNull(assemblyChecker.process(insertionWithNonExistentContig));
    }

    @Test
    public void snpWithNoChromosomeCoordinatesAndValidContigAllele() throws Exception {
        // "RefseqToGenbankMappingProcessor" replaces the Refseq contig for an GenBank one in the "contig" field of the
        // SubSnpCoreFields class, but it does not change hgvsTString. I've replaced the contig but not hgvsTString in
        // this variant, to be consistent with that
        SubSnpCoreFields snpWithValidReference = new SubSnpCoreFields(4387292L, Orientation.FORWARD, 3137071L,
                                                                      Orientation.REVERSE, "AADN04000814.1", 25589L,
                                                                      25589L, Orientation.FORWARD, LocusType.SNP, null,
                                                                      null, null, null, "T", "G", "T/G", null, null,
                                                                      null, Orientation.FORWARD,
                                                                      "NT_464165.1:g.25589A>C", 25589L, 25589L,
                                                                      Orientation.REVERSE, null, null, "5246");

        assertNotNull(assemblyChecker.process(snpWithValidReference));
    }

    @Test
    public void snpWithNoChromosomeCoordinatesAndInvalidContigAllele() throws Exception {
        // "RefseqToGenbankMappingProcessor" replaces the Refseq contig for an GenBank one in the "contig" field of the
        // SubSnpCoreFields class, but it does not change hgvsTString. I've replaced the contig but not hgvsTString in
        // this variant, to be consistent with that
        SubSnpCoreFields snpWithInvalidReference = new SubSnpCoreFields(4387292L, Orientation.FORWARD, 3137071L,
                                                                      Orientation.REVERSE, "AADN04000814.1", 25589L,
                                                                      25589L, Orientation.FORWARD, LocusType.SNP, null,
                                                                      null, null, null, "A", "G", "A/G", null, null,
                                                                      null, Orientation.FORWARD,
                                                                      "NT_464165.1:g.25589T>C", 25589L, 25589L,
                                                                      Orientation.REVERSE, null, null, "5246");

        assertNull(assemblyChecker.process(snpWithInvalidReference));
    }
}