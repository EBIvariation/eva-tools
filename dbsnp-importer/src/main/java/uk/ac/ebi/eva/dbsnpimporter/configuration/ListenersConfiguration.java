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
package uk.ac.ebi.eva.dbsnpimporter.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.listener.StepListenerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.ac.ebi.eva.commons.core.models.IVariant;
import uk.ac.ebi.eva.dbsnpimporter.models.SubSnpCoreFields;

import java.util.List;

@Configuration
public class ListenersConfiguration {

    @Bean
    public ItemWriteListener<IVariant> variantsWriteListener() {
        return new writerListener();
    }

    private static class writerListener extends StepListenerSupport<SubSnpCoreFields, IVariant> {

        private static final Logger logger = LoggerFactory.getLogger(writerListener.class);

        @Override
        public void afterWrite(List<? extends IVariant> items) {
            IVariant lastElement = items.get(items.size() - 1);
            logger.info("Wrote another batch of {} elements. Last element was {}: {}", items.size(),
                        lastElement.getMainId(), lastElement);
        }
    }
}
