package uk.ac.ebi.eva.vcfdump;

import java.util.List;

public class QueryParams {

    private String region;
    private String species;
    private List<String> studies;
    private List<String> consequenceType;
    private String maf;
    private String polyphenScore;
    private String siftScore;
    private String reference;
    private String alternate;
    private String missingAlleles;
    private String missingGenotypes;
    List<String> exclusions;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public List<String> getStudies() {
        return studies;
    }

    public void setStudies(List<String> studies) {
        this.studies = studies;
    }

    public List<String> getConsequenceType() {
        return consequenceType;
    }

    public void setConsequenceType(List<String> consequenceType) {
        this.consequenceType = consequenceType;
    }

    public String getMaf() {
        return maf;
    }

    public void setMaf(String maf) {
        this.maf = maf;
    }

    public String getPolyphenScore() {
        return polyphenScore;
    }

    public void setPolyphenScore(String polyphenScore) {
        this.polyphenScore = polyphenScore;
    }

    public String getSiftScore() {
        return siftScore;
    }

    public void setSiftScore(String siftScore) {
        this.siftScore = siftScore;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getAlternate() {
        return alternate;
    }

    public void setAlternate(String alternate) {
        this.alternate = alternate;
    }

    public String getMissingAlleles() {
        return missingAlleles;
    }

    public void setMissingAlleles(String missingAlleles) {
        this.missingAlleles = missingAlleles;
    }

    public String getMissingGenotypes() {
        return missingGenotypes;
    }

    public void setMissingGenotypes(String missingGenotypes) {
        this.missingGenotypes = missingGenotypes;
    }

    public List<String> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<String> exclusions) {
        this.exclusions = exclusions;
    }
}
