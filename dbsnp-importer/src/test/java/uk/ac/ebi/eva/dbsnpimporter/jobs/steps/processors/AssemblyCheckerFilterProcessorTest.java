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
import uk.ac.ebi.eva.dbsnpimporter.sequence.FastaSequenceReader;
import uk.ac.ebi.eva.dbsnpimporter.sequence.SequenceReader;

import java.nio.file.Paths;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

public class AssemblyCheckerFilterProcessorTest {

    private static SequenceReader sequenceReader;

    private static AssemblyCheckerFilterProcessor assemblyChecker;

    @BeforeClass
    public static void setUpClass() throws Exception {
        sequenceReader = new FastaSequenceReader(
                Paths.get("src/test/resources/Gallus_gallus.Gallus_gallus-5.0.dna.chromosome.22.fa"));
        assemblyChecker = new AssemblyCheckerFilterProcessor(sequenceReader);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        sequenceReader.close();
    }

    @Test
    public void validateSnp() throws Exception {
        // position 10 is G
        SubSnpCoreFields snpWithValidReference = new SubSnpCoreFields(1552148096L, Orientation.FORWARD, 737513389L,
                                                                      Orientation.FORWARD, "NT_455997.1", 1340646L,
                                                                      1340646L, Orientation.REVERSE, LocusType.SNP,
                                                                      "22", 2147L, 2147L, "A", "A", "G", "A/G",
                                                                      "NC_006109.4:g.2147T>C", 2147L, 2147L,
                                                                      Orientation.REVERSE, "NT_455997.1:g.1340646A>G",
                                                                      1340646L, 1340646L, Orientation.FORWARD,
                                                                      "1062063");

        SubSnpCoreFields processedSnp = assemblyChecker.process(snpWithValidReference);
        assertNotNull(processedSnp);

        SubSnpCoreFields snpWithInValidReference = new SubSnpCoreFields(1552148096L, Orientation.FORWARD, 737513389L,
                                                                      Orientation.FORWARD, "NT_455997.1", 1340646L,
                                                                      1340646L, Orientation.REVERSE, LocusType.SNP,
                                                                      "22", 2147L, 2147L, "C", "C", "G", "C/G",
                                                                      "NC_006109.4:g.2147T>C", 2147L, 2147L,
                                                                      Orientation.REVERSE, "NT_455997.1:g.1340646C>G",
                                                                      1340646L, 1340646L, Orientation.FORWARD,
                                                                      "1062063");

        processedSnp = assemblyChecker.process(snpWithInValidReference);
        assertNull(processedSnp);

    }



    // MNV, insertion con ref, insertion sin ref, deletion one, deletion several, no chromosome coordinates? 

}