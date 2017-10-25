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
import uk.ac.ebi.eva.commons.core.models.VariantCoreFields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wrapper for an SS ID, associated RS ID if any, along with its contig and (optionally) chromosome coordinates.
 */
public class SubSnpCoreFields {

    private long ssId;

    private Long rsId;

    private final LocusType locusType;

    private Region contigRegion;

    private Region chromosomeRegion;

    private Orientation snpOrientation;

    private Orientation contigOrientation;

    private String hgvsCReference;

    private String hgvsTReference;

    private String alternate;

    private String alleles;

    private Orientation subSnpOrientation;

    private String hgvsCString;

    private Long hgvsCStart;

    private Long hgvsCStop;

    private Orientation hgvsCOrientation;

    private String hgvsTString;

    private Long hgvsTStart;

    private Long hgvsTStop;

    private Orientation hgvsTOrientation;

    private String batch;

    /**
     * @param subSnpId          Unique SS ID identifier
     * @param subSnpOrientation Orientation of the ssid to the rsid (1 for forward, -1 for reverse)
     * @param snpOrientation    Orientation of the SS ID (1 for forward, -1 for reverse)
     * @param contig            Contig name
     * @param contigStart       Start coordinate in contig
     * @param contigEnd         End coordinate in contig
     * @param contigOrientation Orientation of the contig (1 for forward, -1 for reverse)
     * @param locusType         Locus type
     * @param chromosome        Chromosome name, can be null if the contig is not mapped to a chromosome
     * @param chromosomeStart   Start coordinate of the variant in chromosome, null if the contig is not fully mapped
     *                          to a chromosome
     * @param chromosomeEnd     End coordinate of the variant in chromosome, null if the contig is not fully mapped
     *                          to a chromosome
     * @param hgvsCReference    reference allele from HGVS table, when mapped into a chromosome
     * @param hgvsTReference    reference allele from HGVS table, when mapped into a contig
     * @param alternate         alternate allele
     * @param alleles           reference and alternates alleles as submitted to DbSNP
     * @param hgvsCString       HGVS annotation, mapping to a chromosome
     * @param hgvsCStart        start of the variant in a chromosome according to HGVS
     * @param hgvsCStop         end of the variant in a chromosome according to HGVS
     * @param hgvsCOrientation  Orientation of the snp to the chromosome according to HGVS (1 for forward, -1 for
     *                          reverse)
     * @param hgvsTString       HGVS annotation, mapping to a contig
     * @param hgvsTStart        start of the variant in a contig according to HGVS
     * @param hgvsTStop         end of the variant in a contig according to HGVS
     * @param hgvsTOrientation  Orientation of the contig to the chromosome (1 for forward, -1 for reverse)
     * @param batch             name of the submitted batch to dbSNP, from the column loc_batch_id_upp (similar to study name)
     */
    public SubSnpCoreFields(long subSnpId, Orientation subSnpOrientation, Long snpId, Orientation snpOrientation,
                            String contig, Long contigStart, Long contigEnd, Orientation contigOrientation,
                            LocusType locusType, String chromosome, Long chromosomeStart, Long chromosomeEnd,
                            String hgvsCReference, String hgvsTReference, String alternate, String alleles,
                            String hgvsCString, Long hgvsCStart, Long hgvsCStop, Orientation hgvsCOrientation,
                            String hgvsTString, Long hgvsTStart, Long hgvsTStop, Orientation hgvsTOrientation,
                            String batch) {

        if (contigStart < 0 || contigEnd < 0) {
            throw new IllegalArgumentException("Contig coordinates must be non-negative numbers");
        }
        if ((chromosomeStart != null && chromosomeStart < 0) || (chromosomeEnd != null && chromosomeEnd < 0)) {
            throw new IllegalArgumentException("Chromosome coordinates must be non-negative numbers");
        }

        this.ssId = subSnpId;
        this.rsId = snpId;
        this.contigRegion = createRegion(contig, contigStart, contigEnd);
        this.chromosomeRegion = createRegion(chromosome, chromosomeStart, chromosomeEnd);
        this.snpOrientation = snpOrientation;
        this.contigOrientation = contigOrientation;
        this.locusType = locusType;
        this.hgvsCReference = hgvsCReference;
        this.hgvsTReference = hgvsTReference;
        this.alternate = alternate;
        this.alleles = alleles;
        this.subSnpOrientation = subSnpOrientation;
        this.hgvsCString = hgvsCString;
        this.hgvsCStart = hgvsCStart;
        this.hgvsCStop = hgvsCStop;
        this.hgvsCOrientation = hgvsCOrientation;
        this.hgvsTString = hgvsTString;
        this.hgvsTStart = hgvsTStart;
        this.hgvsTStop = hgvsTStop;
        this.hgvsTOrientation = hgvsTOrientation;
        this.batch = batch;
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

    public LocusType getLocusType() {
        return locusType;
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

    public String getHgvsCReference() {
        return hgvsCReference;
    }

    public String getHgvsTReference() {
        return hgvsTReference;
    }

    public String getAlternate() {
        return alternate;
    }

    public String getAlleles() {
        return alleles;
    }

    public Orientation getSubSnpOrientation() {
        return subSnpOrientation;
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

    public Orientation getHgvsCOrientation() {
        return hgvsCOrientation;
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

    public Orientation getHgvsTOrientation() {
        return hgvsTOrientation;
    }

    public String getBatch() {
        return batch;
    }

    public String getReferenceInForwardStrand() {
        String allele;
        Orientation orientation;

        if (this.getHgvsCString() != null) {
            allele = this.getHgvsCReference();
            orientation = this.getHgvsCOrientation();
        } else if (this.getHgvsTString() != null) {
            allele = this.getHgvsTReference();
            orientation = this.getHgvsTOrientation();
        } else {
            throw new IllegalArgumentException("Neither the HGVS_C nor HGVS_T strings are defined");
        }

        return getTrimmedAllele(getAlleleInForwardStrand(allele, orientation));
    }

    public String getAlternateInForwardStrand() {
        String allele = this.getAlternate();
        Orientation orientation;

        if (this.getHgvsCString() != null) {
            orientation = this.getHgvsCOrientation();
        } else if (this.getHgvsTString() != null) {
            orientation = this.getHgvsTOrientation();
        } else {
            throw new IllegalArgumentException("Neither the HGVS_C nor HGVS_T strings are defined");
        }

        return getTrimmedAllele(getAlleleInForwardStrand(allele, orientation));
    }

    /**
     * Removes leading and trailing spaces. Replaces a dash allele with an empty string.
     */
    private String getTrimmedAllele(String allele) {
        if (allele == null || allele.equals("-")) {
            return "";
        } else {
            return allele.trim();
        }
    }

    private String getAlleleInForwardStrand(String allele, Orientation orientation) {
        if (orientation.equals(Orientation.FORWARD)) {
            return allele;
        } else {
            return calculateReverseComplement(allele);
        }
    }

    /**
     * Return the field "alleles" in forward strand.
     *
     * The 3 orientations (snp_orientation, contig_orientation, subsnp_orientation) are relevant to put the field
     * "alleles" (which comes from the column obsvariation.pattern) in the forward strand.
     *
     * As example, look at the next rs, using the hgvs strings and orientations to know if the
     * ref_allele_c, ref_allele_t and alternate are forward or reverse, thus
     * knowing if "alleles" is in forward or reverse.
     *
     * - rs13677177 : "alleles" are in forward when orientations are 1 1 1, reverse when orientations 1 1 -1
     * - rs739617577 : "alleles" are in forward with orientations -1 -1 1
     * - rs10721689 :  "alleles" are reverse when orientations are 1 -1 1, forward when 1 -1 -1
     */
    public String getAllelesInForwardStrand() {
        boolean forward = this.getSubSnpOrientation().equals(Orientation.FORWARD)
                ^ this.getSnpOrientation().equals(Orientation.FORWARD)
                ^ this.getContigOrientation().equals(Orientation.FORWARD);

        String alleles = this.getAlleles();
        String forwardAlleles = forward ? alleles : calculateReverseComplement(alleles);

        String[] splitAlleles = forwardAlleles.split("/", -1);
        return Stream.of(splitAlleles).map(this::getTrimmedAllele).collect(Collectors.joining("/"));
    }

    public String[] getSecondaryAlternatesInForwardStrand() {
        String[] alleles = this.getAllelesInForwardStrand().split("/", -1);
        List<String> secondaryAlternates = new ArrayList<>(Arrays.asList(alleles));
        for (String allele : alleles) {
            if (allele.equals(this.getReferenceInForwardStrand())
                    || allele.equals(this.getAlternateInForwardStrand())) {
                secondaryAlternates.remove(allele);
            }
        }
        String[] secondaryAlternatesArray = new String[secondaryAlternates.size()];
        return secondaryAlternates.toArray(secondaryAlternatesArray);
    }

    private String calculateReverseComplement(String alleleInReverseStrand) {
        StringBuilder alleleInForwardStrand = new StringBuilder(alleleInReverseStrand).reverse();
        for (int i = 0; i < alleleInForwardStrand.length(); i++) {
            switch (alleleInForwardStrand.charAt(i)) {
                // Capitalization holds a special meaning for dbSNP so we need to preserve it.
                // See https://www.ncbi.nlm.nih.gov/books/NBK44414/#_Reports_Lowercase_Small_Sequence_Letteri_
                case 'A':
                    alleleInForwardStrand.setCharAt(i, 'T');
                    break;
                case 'a':
                    alleleInForwardStrand.setCharAt(i, 't');
                    break;
                case 'C':
                    alleleInForwardStrand.setCharAt(i, 'G');
                    break;
                case 'c':
                    alleleInForwardStrand.setCharAt(i, 'g');
                    break;
                case 'G':
                    alleleInForwardStrand.setCharAt(i, 'C');
                    break;
                case 'g':
                    alleleInForwardStrand.setCharAt(i, 'c');
                    break;
                case 'T':
                    alleleInForwardStrand.setCharAt(i, 'A');
                    break;
                case 't':
                    alleleInForwardStrand.setCharAt(i, 'a');
                    break;
            }
        }
        return alleleInForwardStrand.toString();
    }

    /**
     * Returns the variant chromosome (or contig if the variant is not mapped against a chromosome) coordinates
     * normalized according to the EVA variation model
     *
     * @return Region object containing the normalized chromosome or contig coordinates
     */
    public Region getVariantCoordinates() {
        Region variantRegion = chromosomeRegion != null ? chromosomeRegion : contigRegion;

        // adjust start and end for insertions
        if (locusType.equals(LocusType.INSERTION)) {
            variantRegion.setStart(variantRegion.getStart() + 1);
            variantRegion.setEnd(variantRegion.getEnd() + getAlternate().length() - 1);
        }

        return variantRegion;
    }

    /**
     * Return the left aligned, normalised, variant coordinates and the alleles in the forward strand
     * @return Object containing normalised variant coordinates and forward strand alleles
     */
    public VariantCoreFields getVariantCoreFields() {
        Region variantRegion = getVariantCoordinates();
        return new VariantCoreFields(variantRegion.getChromosome(), variantRegion.getStart(),
                                    getReferenceInForwardStrand(), getAlternateInForwardStrand());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubSnpCoreFields that = (SubSnpCoreFields) o;

        if (ssId != that.ssId) return false;
        if (rsId != null ? !rsId.equals(that.rsId) : that.rsId != null) return false;
        if (locusType != that.locusType) return false;
        if (contigRegion != null ? !contigRegion.equals(that.contigRegion) : that.contigRegion != null) return false;
        if (chromosomeRegion != null ? !chromosomeRegion.equals(that.chromosomeRegion) : that.chromosomeRegion != null)
            return false;
        if (snpOrientation != that.snpOrientation) return false;
        if (contigOrientation != that.contigOrientation) return false;
        if (hgvsCReference != null ? !hgvsCReference.equals(that.hgvsCReference) : that.hgvsCReference != null)
            return false;
        if (hgvsTReference != null ? !hgvsTReference.equals(that.hgvsTReference) : that.hgvsTReference != null)
            return false;
        if (alternate != null ? !alternate.equals(that.alternate) : that.alternate != null) return false;
        if (alleles != null ? !alleles.equals(that.alleles) : that.alleles != null) return false;
        if (subSnpOrientation != that.subSnpOrientation) return false;
        if (hgvsCString != null ? !hgvsCString.equals(that.hgvsCString) : that.hgvsCString != null) return false;
        if (hgvsCStart != null ? !hgvsCStart.equals(that.hgvsCStart) : that.hgvsCStart != null) return false;
        if (hgvsCStop != null ? !hgvsCStop.equals(that.hgvsCStop) : that.hgvsCStop != null) return false;
        if (hgvsCOrientation != that.hgvsCOrientation) return false;
        if (hgvsTString != null ? !hgvsTString.equals(that.hgvsTString) : that.hgvsTString != null) return false;
        if (hgvsTStart != null ? !hgvsTStart.equals(that.hgvsTStart) : that.hgvsTStart != null) return false;
        if (hgvsTStop != null ? !hgvsTStop.equals(that.hgvsTStop) : that.hgvsTStop != null) return false;
        if (hgvsTOrientation != that.hgvsTOrientation) return false;
        return batch != null ? batch.equals(that.batch) : that.batch == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (ssId ^ (ssId >>> 32));
        result = 31 * result + (rsId != null ? rsId.hashCode() : 0);
        result = 31 * result + (locusType != null ? locusType.hashCode() : 0);
        result = 31 * result + (contigRegion != null ? contigRegion.hashCode() : 0);
        result = 31 * result + (chromosomeRegion != null ? chromosomeRegion.hashCode() : 0);
        result = 31 * result + (snpOrientation != null ? snpOrientation.hashCode() : 0);
        result = 31 * result + (contigOrientation != null ? contigOrientation.hashCode() : 0);
        result = 31 * result + (hgvsCReference != null ? hgvsCReference.hashCode() : 0);
        result = 31 * result + (hgvsTReference != null ? hgvsTReference.hashCode() : 0);
        result = 31 * result + (alternate != null ? alternate.hashCode() : 0);
        result = 31 * result + (alleles != null ? alleles.hashCode() : 0);
        result = 31 * result + (subSnpOrientation != null ? subSnpOrientation.hashCode() : 0);
        result = 31 * result + (hgvsCString != null ? hgvsCString.hashCode() : 0);
        result = 31 * result + (hgvsCStart != null ? hgvsCStart.hashCode() : 0);
        result = 31 * result + (hgvsCStop != null ? hgvsCStop.hashCode() : 0);
        result = 31 * result + (hgvsCOrientation != null ? hgvsCOrientation.hashCode() : 0);
        result = 31 * result + (hgvsTString != null ? hgvsTString.hashCode() : 0);
        result = 31 * result + (hgvsTStart != null ? hgvsTStart.hashCode() : 0);
        result = 31 * result + (hgvsTStop != null ? hgvsTStop.hashCode() : 0);
        result = 31 * result + (hgvsTOrientation != null ? hgvsTOrientation.hashCode() : 0);
        result = 31 * result + (batch != null ? batch.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SubSnpCoreFields{" +
                "ssId=" + ssId +
                ", rsId=" + rsId +
                ", locusType=" + locusType +
                ", contigRegion=" + contigRegion +
                ", chromosomeRegion=" + chromosomeRegion +
                ", snpOrientation=" + snpOrientation +
                ", contigOrientation=" + contigOrientation +
                ", hgvsCReference='" + hgvsCReference + '\'' +
                ", hgvsTReference='" + hgvsTReference + '\'' +
                ", alternate='" + alternate + '\'' +
                ", alleles='" + alleles + '\'' +
                ", subSnpOrientation=" + subSnpOrientation +
                ", hgvsCString='" + hgvsCString + '\'' +
                ", hgvsCStart=" + hgvsCStart +
                ", hgvsCStop=" + hgvsCStop +
                ", hgvsCOrientation=" + hgvsCOrientation +
                ", hgvsTString='" + hgvsTString + '\'' +
                ", hgvsTStart=" + hgvsTStart +
                ", hgvsTStop=" + hgvsTStop +
                ", hgvsTOrientation=" + hgvsTOrientation +
                ", batch='" + batch + '\'' +
                '}';
    }
}
