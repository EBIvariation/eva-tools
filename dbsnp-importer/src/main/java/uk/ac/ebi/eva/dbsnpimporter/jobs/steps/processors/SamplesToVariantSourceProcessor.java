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

import uk.ac.ebi.eva.commons.core.models.IVariantSource;
import uk.ac.ebi.eva.dbsnpimporter.models.Sample;

import java.util.Collection;

/**
 * Packs a set of {@link Sample} under a {@link uk.ac.ebi.eva.commons.core.models.IVariantSource} object, adding some
 * additional batch information.
 */
public class SamplesToVariantSourceProcessor implements ItemProcessor<Collection<Sample>, IVariantSource> {

    @Override
    public IVariantSource process(Collection<Sample> samples) throws Exception {
        return null;
    }
}
