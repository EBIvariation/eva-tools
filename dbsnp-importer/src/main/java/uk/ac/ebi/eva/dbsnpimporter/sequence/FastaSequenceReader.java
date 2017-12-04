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

import htsjdk.samtools.SAMException;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.NoSuchElementException;

/**
 * Implementation of SequenceReader for indexed fasta files
 */
public class FastaSequenceReader implements SequenceReader {

    static final String END_LESS_THAN_START_EXCEPTION_MESSAGE = "'end' should be greater or equal than 'start'";

    static final String START_NEGATIVE_EXCEPTION_MESSAGE = "'start' and 'end' should be positive integers";

    static final String CONTIG_NOT_PRESENT_EXCEPTION_MESSAGE = "contig not present in fasta file";

    static final String QUERY_PAST_END_OF_CONTIG_MESSAGE = "Query asks for data past end of contig";

    private final ReferenceSequenceFile fastaSequenceFile;

    public FastaSequenceReader(Path fastaFile) throws FileNotFoundException {
        fastaSequenceFile = ReferenceSequenceFileFactory.getReferenceSequenceFile(fastaFile, true);
    }

    @Override
    public String getSequence(String contig, long start, long end)  throws IndexOutOfBoundsException,
            NoSuchElementException, IllegalArgumentException {
        if (end < start) {
            throw new IllegalArgumentException(END_LESS_THAN_START_EXCEPTION_MESSAGE);
        } else if (start < 1) {
            throw new IndexOutOfBoundsException(START_NEGATIVE_EXCEPTION_MESSAGE);
        }

        try {
            return fastaSequenceFile.getSubsequenceAt(contig, start, end).getBaseString();
        } catch (SAMException e) {
            if (e.getMessage().contains("Unable to find entry for contig")) {
                throw new NoSuchElementException(CONTIG_NOT_PRESENT_EXCEPTION_MESSAGE);
            } else if (e.getMessage().contains(QUERY_PAST_END_OF_CONTIG_MESSAGE)) {
                throw new IndexOutOfBoundsException(QUERY_PAST_END_OF_CONTIG_MESSAGE);
            } else {
                throw e;
            }
        }
    }
}
