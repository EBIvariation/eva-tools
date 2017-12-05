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

import java.util.NoSuchElementException;

/**
 * Interface for readers than returns nucleotide subsequences of a given sequence source (like a fasta file or a WS)
 */
public interface SequenceReader {

    /**
     * Returns the nucleotide subsequence for a given query
     * @param contig contig name
     * @param start 1-based start position in the contig of the sequence to be retrieved
     * @param end 1-based end position in the contig of the sequence to be retrieve
     * @return Subsequence for the query coordinates
     * @throws IndexOutOfBoundsException If start or end are outside the boundaries of the sequence file
     * @throws NoSuchElementException If the contig is not present in the source
     * @throws IllegalArgumentException If the query is not correct
     */
    String getSequence(String contig, long start,
                       long end) throws IndexOutOfBoundsException, NoSuchElementException, IllegalArgumentException;

    /**
     * Close the reader
     * @throws Exception
     */
    void close() throws Exception;
}
