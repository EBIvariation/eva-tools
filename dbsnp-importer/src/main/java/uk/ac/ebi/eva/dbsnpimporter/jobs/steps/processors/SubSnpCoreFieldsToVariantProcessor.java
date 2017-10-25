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

import uk.ac.ebi.eva.commons.core.models.pipeline.Variant;
import uk.ac.ebi.eva.commons.core.models.pipeline.VariantSourceEntry;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

/**
 * Maps {@link SubSnpCoreFields} to {@link uk.ac.ebi.eva.commons.core.models.IVariant},
 * extending {@link SubSnpCoreFieldsToEvaSubmittedVariantProcessor}, and
 * adding a VariantSourceEntry, because the batch (or study) won't be present in EVA.
 */
public class SubSnpCoreFieldsToVariantProcessor extends SubSnpCoreFieldsToEvaSubmittedVariantProcessor {

    public static final String DBSNP_BUILD_KEY = "dbSNP build";

    private final String dbsnpBuild;

    private final String batch;

    public SubSnpCoreFieldsToVariantProcessor(int dbsnpBuild, int batch) {
        this.dbsnpBuild = String.valueOf(dbsnpBuild);
        this.batch = String.valueOf(batch);
    }

    @Override
    public Variant process(SubSnpCoreFields subSnpCoreFields) throws Exception {
        Variant variant = super.process(subSnpCoreFields);

        VariantSourceEntry variantSourceEntry = new VariantSourceEntry(batch, batch);
        variantSourceEntry.addAttribute(DBSNP_BUILD_KEY, dbsnpBuild);
        variantSourceEntry.setSecondaryAlternates(subSnpCoreFields.getSecondaryAlternatesInForwardStrand());
        variant.addSourceEntry(variantSourceEntry);

        return variant;
    }

}
