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
package uk.ac.ebi.eva.dbsnpimporter.jobs.steps.processors;


import uk.ac.ebi.eva.dbsnpimporter.models.Orientation;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

class ForwardStrandAlleleMapper {

    private SubSnpCoreFields subSnpCoreFields;

    public ForwardStrandAlleleMapper(SubSnpCoreFields subSnpCoreFields) {
        this.subSnpCoreFields = subSnpCoreFields;
    }

    public String getReferenceInForwardStrand() {
        String allele;
        Orientation orientation;

        if (subSnpCoreFields.getHgvsCString() != null) {
            allele = subSnpCoreFields.getHgvsCReference();
            orientation = subSnpCoreFields.getHgvsCOrientation();
        } else if (subSnpCoreFields.getHgvsTString() != null) {
            allele = subSnpCoreFields.getHgvsTReference();
            orientation = subSnpCoreFields.getHgvsTOrientation();
        } else {
            throw new IllegalArgumentException("Neither the HGVS_C nor HGVS_T strings are defined");
        }

        if (orientation.equals(Orientation.FORWARD)) {
            return allele;
        } else {
            return calculateReverseComplement(allele);
        }
    }

    public String getAlternateInForwardStrand() {
        String allele = subSnpCoreFields.getAlternate();
        Orientation orientation;

        if (subSnpCoreFields.getHgvsCString() != null) {
            orientation = subSnpCoreFields.getHgvsCOrientation();
        } else if (subSnpCoreFields.getHgvsTString() != null) {
            orientation = subSnpCoreFields.getHgvsTOrientation();
        } else {
            throw new IllegalArgumentException("Neither the HGVS_C nor HGVS_T strings are defined");
        }

        if (orientation.equals(Orientation.FORWARD)) {
            return allele;
        } else {
            return calculateReverseComplement(allele);
        }

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

}
