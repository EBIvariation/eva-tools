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
package uk.ac.ebi.eva.dbsnpimporter.models;

import uk.ac.ebi.eva.commons.core.models.Region;

/**
 * Wrapper for an SS ID, associated RS ID if any, along with its contig and (optionally) chromosome coordinates.
 */
public class SubSnpCoreFields {

    private long ssId;

    private Long rsId;

    private Region contigRegion;

    private Region chromosomeRegion;

    private Orientation snpOrientation;

    private Orientation contigOrientation;

    /**
     * @param ssId Unique SS ID identifier
     * @param rsId Unique RS ID identifier, can be null if the SS ID has not been clustered yet
     * @param snpOrientation Orientation of the SS ID (forward/reverse)
     * @param contig Contig name
     * @param contigStart Start coordinate in contig
     * @param contigEnd End coordinate in contig
     * @param contigOrientation Orientation of the contig (forward/reverse)
     * @param chromosome Chromosome name, can be null if the contig is not mapped to a chromosome
     * @param chromosomeStart Start coordinate in chromosome, null if the contig is not fully mapped to a chromosome
     * @param chromosomeEnd End coordinate in chromosome, null if the contig is not fully mapped to a chromosome
     */
    public SubSnpCoreFields(long ssId, Long rsId, int snpOrientation, String contig, int contigStart, int contigEnd,
                            int contigOrientation, String chromosome, Integer chromosomeStart, Integer chromosomeEnd) {
        if (contigStart < 0 || contigEnd < 0) {
            throw new IllegalArgumentException("Contig coordinates must be non-negative numbers");
        }
        if ((chromosomeStart != null && chromosomeStart < 0) || (chromosomeEnd != null && chromosomeEnd < 0)) {
            throw new IllegalArgumentException("Chromosome coordinates must be non-negative numbers");
        }

        this.ssId = ssId;
        this.rsId = rsId;
        this.contigRegion = createRegion(contig, contigStart, contigEnd);
        this.chromosomeRegion = createRegion(chromosome, chromosomeStart, chromosomeEnd);
        this.snpOrientation = Orientation.getOrientation(snpOrientation);
        this.contigOrientation = Orientation.getOrientation(contigOrientation);
    }

    public long getSsId() {
        return ssId;
    }

    public Long getRsId() {
        return rsId;
    }

    public Region getContigRegion() {
        return contigRegion;
    }

    public Region getChromosomeRegion() {
        return chromosomeRegion;
    }

    public Orientation getSnpOrientation() {
        return snpOrientation;
    }

    public Orientation getContigOrientation() {
        return contigOrientation;
    }

    private Region createRegion(String sequenceName, Integer start, Integer end) {
        if (sequenceName == null) {
            // This should happen only with chromosomes, when a contig-to-chromosome mapping is not available
            return null;
        }

        StringBuilder regionString = new StringBuilder(sequenceName);
        if (start != null) {
            regionString.append(":");
            regionString.append(start);

            if (end != null) {
                regionString.append(":");
                regionString.append(end);
            }
        }

        return new Region(regionString.toString());
    }

}
