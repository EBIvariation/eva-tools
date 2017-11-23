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

package uk.ac.ebi.eva.dbsnpimporter.contig;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class ContigMappingTest {

    private static final String GENBANK_1 = "genbank_example_1";

    private static final String GENBANK_2 = "genbank_example_2";

    private static final String GENBANK_3 = "genbank_example_3";

    private static final String REFSEQ_1 = "refseq_example_1";

    private static final String REFSEQ_2 = "refseq_example_2";

    private static final String REFSEQ_3 = "refseq_example_3";

    private static final String GENBANK_CONTIG = "GL456213.1";

    private static final String REFSEQ_CONTIG = "NT_166283.1";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void useMapContructor() throws Exception {
        HashMap<String, String> contigMap = new HashMap<>();
        contigMap.put(REFSEQ_1, GENBANK_1);
        contigMap.put(REFSEQ_2, GENBANK_2);
        contigMap.put(REFSEQ_3, GENBANK_3);
        ContigMapping contigMapping = new ContigMapping(contigMap);

        assertEquals(GENBANK_1, contigMapping.getGenbank(REFSEQ_1));
        assertEquals(GENBANK_2, contigMapping.getGenbank(REFSEQ_2));
        assertEquals(GENBANK_3, contigMapping.getGenbank(REFSEQ_3));
    }

    @Test
    public void noAvailableMapping() throws Exception {
        HashMap<String, String> contigMap = new HashMap<>();
        contigMap.put(REFSEQ_1, GENBANK_1);
        ContigMapping contigMapping = new ContigMapping(contigMap);

        thrown.expect(IllegalArgumentException.class);
        contigMapping.getGenbank("unknown_refseq");
    }

    @Test
    public void useFileConstructor() throws Exception {
        String assemblyReport = "AssemblyReport.txt";
        String fakedFtpLocation = Thread.currentThread().getContextClassLoader().getResource(assemblyReport).toString();

        ContigMapping contigMapping = new ContigMapping(fakedFtpLocation);

        assertEquals(GENBANK_CONTIG, contigMapping.getGenbank(REFSEQ_CONTIG));
    }

    @Test
    @Ignore("This test does an external ftp request, which is too slow to be a comfortable test.")
    public void useFileConstructorAndActualFtp() throws Exception {
        String ftpLocation = "ftp://ftp.ncbi.nih.gov/genomes/refseq/vertebrate_mammalian/Mus_musculus/all_assembly_versions/GCF_000001635.26_GRCm38.p6/GCF_000001635.26_GRCm38.p6_assembly_report.txt";
        ContigMapping contigMapping = new ContigMapping(ftpLocation);

        assertEquals(GENBANK_CONTIG, contigMapping.getGenbank(REFSEQ_CONTIG));
    }
}