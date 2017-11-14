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

import uk.ac.ebi.eva.commons.core.models.IVariant;
import uk.ac.ebi.eva.commons.core.models.VariantCoreFields;
import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

/**
 * The variant returned doesn't have VariantSourceEntries because it will be used for variants already submitted to EVA.
 */
public class SubSnpCoreFieldsToEvaSubmittedVariantProcessor implements ItemProcessor<SubSnpCoreFields, IVariant> {

    @Override
    public Variant process(SubSnpCoreFields subSnpCoreFields) throws Exception {
        VariantCoreFields variantCoreFields = subSnpCoreFields.getVariantCoreFields();

        Variant variant = new Variant(variantCoreFields.getChromosome(), variantCoreFields.getStart(),
                                      variantCoreFields.getEnd(), variantCoreFields.getReference(),
                                      variantCoreFields.getAlternate());
        if (subSnpCoreFields.getRsId() != null) {
            String rsId = "rs" + subSnpCoreFields.getRsId();
            variant.setMainId(rsId);
            variant.addDbsnpId(rsId);
        }
        String ssId = "ss" + subSnpCoreFields.getSsId();
        variant.addDbsnpId(ssId);

        return variant;
    }
}
