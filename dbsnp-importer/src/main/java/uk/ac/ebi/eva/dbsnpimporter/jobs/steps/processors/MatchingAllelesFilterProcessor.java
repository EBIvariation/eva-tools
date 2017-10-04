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

import org.springframework.batch.item.ItemProcessor;

import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

public class MatchingAllelesFilterProcessor implements ItemProcessor<SubSnpCoreFields, SubSnpCoreFields> {

    @Override
    public SubSnpCoreFields process(SubSnpCoreFields subSnpCoreFields) throws Exception {
        String[] alleles = subSnpCoreFields.getAlleles().split("/");
        String reference = subSnpCoreFields.getReferenceInForwardStrand();
        boolean referenceMatches = false;
        int referenceIndex = -1;
        for (int i = 0; i < alleles.length; i++) {
            if (reference.equals(alleles[i])) {
                referenceMatches = true;
                referenceIndex = i;
                break;
            }
        }
        if (!referenceMatches) {
            // can the pattern be in reverse?
            return null;
        }

        String alternate = subSnpCoreFields.getAlternateInForwardStrand();
        boolean alternateMatches = false;
        int alternateIndex = -1;
        for (int i = 0; i < alleles.length; i++) {
            if (alternate.equals(alleles[i]) && i != referenceIndex) {
                alternateMatches = true;
                alternateIndex = i;
                break;
            }
        }
        if (!alternateMatches) {
            // can the pattern be in reverse?
            return null;
        }
        return subSnpCoreFields;
    }

}
