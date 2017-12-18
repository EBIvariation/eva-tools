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
package uk.ac.ebi.eva.dbsnpimporter.io;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;

import java.nio.file.Path;

/**
 * Class used to read regions from a given FASTA file
 */
public class FastaSequenceReader {

    private final ReferenceSequenceFile fastaSequenceFile;

    private final SAMSequenceDictionary sequenceDictionary;

    public FastaSequenceReader(Path fastaFile) {
        fastaSequenceFile = ReferenceSequenceFileFactory.getReferenceSequenceFile(fastaFile, true);
        sequenceDictionary = fastaSequenceFile.getSequenceDictionary();
        if (sequenceDictionary == null || !fastaSequenceFile.isIndexed()) {
            throw new IllegalArgumentException("A sequence dictionary file and a Fasta index file are required");
        }
    }

    /**
     * Get the sequence delimited by the give coordinates from a FASTA file
     *
     * @param contig Sequence contig or chromosome
     * @param start  Sequence start coordinate in the contig
     * @param end    Sequence end coordinate in the contig
     * @return Sequence read from the FASTA file
     * @throws IllegalArgumentException If the coordinates are not correct
     */
    public String getSequence(String contig, long start, long end) throws IllegalArgumentException {
        checkArguments(contig, start, end);

        return fastaSequenceFile.getSubsequenceAt(contig, start, end).getBaseString();

    }

    private void checkArguments(String contig, long start, long end) throws IllegalArgumentException {
        if (end < start) {
            throw new IllegalArgumentException("'end' must be greater or equal than 'start'");
        } else if (start < 1) {
            throw new IllegalArgumentException("'start' and 'end' must be positive numbers");
        } else if (sequenceDictionary.getSequence(contig) == null) {
            throw new IllegalArgumentException("Sequence " + contig + " not found in reference fasta file");
        } else {
            int sequenceLengthInFastaFile = sequenceDictionary.getSequence(contig).getSequenceLength();
            if (end > sequenceLengthInFastaFile) {
                throw new IllegalArgumentException(
                        "Variant coordinate " + end + " greater than end of chromosome " + contig + ": " +
                                sequenceLengthInFastaFile);
            }
        }
    }

    /**
     * Close the underlying FASTA file
     * @throws Exception If the file cannot be closed
     */
    public void close() throws Exception {
        fastaSequenceFile.close();
    }
}
