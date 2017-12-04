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
package uk.ac.ebi.eva.dbsnpimporter.sequence;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.Paths;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static uk.ac.ebi.eva.dbsnpimporter.sequence.FastaSequenceReader.CONTIG_NOT_PRESENT_EXCEPTION_MESSAGE;
import static uk.ac.ebi.eva.dbsnpimporter.sequence.FastaSequenceReader.END_LESS_THAN_START_EXCEPTION_MESSAGE;
import static uk.ac.ebi.eva.dbsnpimporter.sequence.FastaSequenceReader.QUERY_PAST_END_OF_CONTIG_MESSAGE;
import static uk.ac.ebi.eva.dbsnpimporter.sequence.FastaSequenceReader.START_NEGATIVE_EXCEPTION_MESSAGE;

public class FastaSequenceReaderTest {

    private FastaSequenceReader reader;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        reader = new FastaSequenceReader(
                Paths.get("src/test/resources/Gallus_gallus.Gallus_gallus-5.0.dna.chromosome.22.fa"));
    }

    @Test
    public void getFirstNucleotideOfContig() throws Exception {
        assertEquals("T", reader.getSequence("22", 1, 1));
    }

    @Test
    public void getLastNucleotideOfContig() throws Exception {
        assertEquals("G", reader.getSequence("22", 4729743, 4729743));
    }

    @Test
    public void getSequence() throws Exception {
        // this sequence is splitted between three lines in the fasta file
        assertEquals("GTTTCAAGTGGTTGTGACCCCCGCTGCACAGTCAGTTGGGTTAGGGTTAGGGTTAGGGTCAGTCACAGTCAGTTGTCAGACTGGTGTTTA",
                     reader.getSequence("22", 59986, 60075));
    }

    @Test
    public void endMustBeGreaterOrEqualsThanStart() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(END_LESS_THAN_START_EXCEPTION_MESSAGE);
        reader.getSequence("22", 1000, 999);
    }

    @Test
    public void justPositiveCoordinatesAreAllowed() {
        thrown.expect(IndexOutOfBoundsException.class);
        thrown.expectMessage(START_NEGATIVE_EXCEPTION_MESSAGE);
        reader.getSequence("22", -1, 5);
    }

    @Test
    public void coordinatesExcedingEndOfChromosomeAreNotAllowed() {
        thrown.expect(IndexOutOfBoundsException.class);
        thrown.expectMessage(QUERY_PAST_END_OF_CONTIG_MESSAGE);
        reader.getSequence("22", 4729740, 4729750);
    }

    @Test
    public void notExistentChromosome() {
        thrown.expect(NoSuchElementException.class);
        thrown.expectMessage(CONTIG_NOT_PRESENT_EXCEPTION_MESSAGE);
        reader.getSequence("23", 1, 1);
    }
}