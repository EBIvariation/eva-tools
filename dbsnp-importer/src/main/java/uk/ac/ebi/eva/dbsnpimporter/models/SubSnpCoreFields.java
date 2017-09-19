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

    private String referenceC;

    private String referenceT;

    private String alternate;

    private String alleles;

    private String hgvsCString;

    private Long hgvsCStart;

    private Long hgvsCStop;

    private String hgvsTString;

    private Long hgvsTStart;

    private Long hgvsTStop;


    /**
     * @param ssId Unique SS ID identifier
     * @param rsId Unique RS ID identifier, can be null if the SS ID has not been clustered yet
     * @param snpOrientation Orientation of the SS ID (forward/reverse)
     * @param contig Contig name
     * @param contigStart Start coordinate in contig
     * @param contigEnd End coordinate in contig
     * @param contigOrientation Orientation of the contig (forward/reverse)
     * @param chromosome Chromosome name, can be null if the contig is not mapped to a chromosome
     * @param chromosomeStart Start coordinate of the variant in chromosome, null if the contig is not fully mapped to a chromosome
     * @param chromosomeEnd End coordinate of the variant in chromosome, null if the contig is not fully mapped to a chromosome
     * @param referenceC reference allele from hgvs table, when mapped into a chromosome
     * @param referenceT reference allele from hgvs table, when mapped into a contig
     * @param alternate alternate allele
     * @param alleles reference and alternates alleles as submitted to DbSNP
     * @param hgvsCString hgvs annotation, mapping to a chromosome
     * @param hgvsCStart start of the variant in a chromosome according to hgvs
     * @param hgvsCStop end of the variant in a chromosome according to hgvs
     * @param hgvsTString hgvs annotation, mapping to a contig
     * @param hgvsTStart start of the variant in a contig according to hgvs
     * @param hgvsTStop end of the variant in a contig according to hgvs
     */
    public SubSnpCoreFields(long ssId, Long rsId, int snpOrientation, String contig, Long contigStart, Long contigEnd,
                            int contigOrientation, String chromosome, Long chromosomeStart, Long chromosomeEnd,
                            String referenceC, String referenceT, String alternate, String alleles,
                            String hgvsCString, Long hgvsCStart, Long hgvsCStop, String hgvsTString,
                            Long hgvsTStart, Long hgvsTStop) {

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
        this.referenceC = referenceC;
        this.referenceT = referenceT;
        this.alternate = alternate;
        this.alleles = alleles;
        this.hgvsCString = hgvsCString;
        this.hgvsCStart = hgvsCStart;
        this.hgvsCStop = hgvsCStop;
        this.hgvsTString = hgvsTString;
        this.hgvsTStart = hgvsTStart;
        this.hgvsTStop = hgvsTStop;
    }

    private Region createRegion(String sequenceName, Long start, Long end) {
        if (sequenceName != null) {
            if (start != null) {
                if (end != null) {
                    return new Region(sequenceName, start, end);
                }
                return new Region(sequenceName, start);
            }
            return new Region(sequenceName);
        }
        // This should happen only with chromosomes, when a contig-to-chromosome mapping is not available
        return null;
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

    public String getReferenceC() {
        return referenceC;
    }

    public String getReferenceT() {
        return referenceT;
    }

    public String getAlternate() {
        return alternate;
    }

    public String getAlleles() {
        return alleles;
    }

    public String getHgvsCString() {
        return hgvsCString;
    }

    public Long getHgvsCStart() {
        return hgvsCStart;
    }

    public Long getHgvsCStop() {
        return hgvsCStop;
    }

    public String getHgvsTString() {
        return hgvsTString;
    }

    public Long getHgvsTStart() {
        return hgvsTStart;
    }

    public Long getHgvsTStop() {
        return hgvsTStop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubSnpCoreFields that = (SubSnpCoreFields) o;

        if (ssId != that.ssId) return false;
        if (rsId != null ? !rsId.equals(that.rsId) : that.rsId != null) return false;
        if (contigRegion != null ? !contigRegion.equals(that.contigRegion) : that.contigRegion != null) return false;
        if (chromosomeRegion != null ? !chromosomeRegion.equals(that.chromosomeRegion) : that.chromosomeRegion != null)
            return false;
        if (snpOrientation != that.snpOrientation) return false;
        if (contigOrientation != that.contigOrientation) return false;
        if (referenceC != null ? !referenceC.equals(that.referenceC) : that.referenceC != null) return false;
        if (referenceT != null ? !referenceT.equals(that.referenceT) : that.referenceT != null) return false;
        if (alternate != null ? !alternate.equals(that.alternate) : that.alternate != null) return false;
        if (alleles != null ? !alleles.equals(that.alleles) : that.alleles != null) return false;
        if (hgvsCString != null ? !hgvsCString.equals(that.hgvsCString) : that.hgvsCString != null) return false;
        if (hgvsCStart != null ? !hgvsCStart.equals(that.hgvsCStart) : that.hgvsCStart != null) return false;
        if (hgvsCStop != null ? !hgvsCStop.equals(that.hgvsCStop) : that.hgvsCStop != null) return false;
        if (hgvsTString != null ? !hgvsTString.equals(that.hgvsTString) : that.hgvsTString != null) return false;
        if (hgvsTStart != null ? !hgvsTStart.equals(that.hgvsTStart) : that.hgvsTStart != null) return false;
        return hgvsTStop != null ? hgvsTStop.equals(that.hgvsTStop) : that.hgvsTStop == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (ssId ^ (ssId >>> 32));
        result = 31 * result + (rsId != null ? rsId.hashCode() : 0);
        result = 31 * result + (contigRegion != null ? contigRegion.hashCode() : 0);
        result = 31 * result + (chromosomeRegion != null ? chromosomeRegion.hashCode() : 0);
        result = 31 * result + snpOrientation.hashCode();
        result = 31 * result + contigOrientation.hashCode();
        result = 31 * result + (referenceC != null ? referenceC.hashCode() : 0);
        result = 31 * result + (referenceT != null ? referenceT.hashCode() : 0);
        result = 31 * result + (alternate != null ? alternate.hashCode() : 0);
        result = 31 * result + (alleles != null ? alleles.hashCode() : 0);
        result = 31 * result + (hgvsCString != null ? hgvsCString.hashCode() : 0);
        result = 31 * result + (hgvsCStart != null ? hgvsCStart.hashCode() : 0);
        result = 31 * result + (hgvsCStop != null ? hgvsCStop.hashCode() : 0);
        result = 31 * result + (hgvsTString != null ? hgvsTString.hashCode() : 0);
        result = 31 * result + (hgvsTStart != null ? hgvsTStart.hashCode() : 0);
        result = 31 * result + (hgvsTStop != null ? hgvsTStop.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SubSnpCoreFields{" +
                "ssId=" + ssId +
                ", rsId=" + rsId +
                ", contigRegion=" + contigRegion +
                ", chromosomeRegion=" + chromosomeRegion +
                ", snpOrientation=" + snpOrientation +
                ", contigOrientation=" + contigOrientation +
                ", referenceC='" + referenceC + '\'' +
                ", referenceT='" + referenceT + '\'' +
                ", alternate='" + alternate + '\'' +
                ", alleles='" + alleles + '\'' +
                ", hgvsCString='" + hgvsCString + '\'' +
                ", hgvsCStart='" + hgvsCStart + '\'' +
                ", hgvsCStop='" + hgvsCStop + '\'' +
                ", hgvsTString='" + hgvsTString + '\'' +
                ", hgvsTStart='" + hgvsTStart + '\'' +
                ", hgvsTStop='" + hgvsTStop + '\'' +
                '}';
    }
}
