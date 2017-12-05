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

import uk.ac.ebi.eva.commons.core.models.Aggregation;
import uk.ac.ebi.eva.commons.core.models.IVariantGlobalStats;
import uk.ac.ebi.eva.commons.core.models.IVariantSource;
import uk.ac.ebi.eva.commons.core.models.StudyType;
import uk.ac.ebi.eva.commons.core.models.VariantSource;
import uk.ac.ebi.eva.commons.mongodb.entities.VariantSourceMongo;
import uk.ac.ebi.eva.dbsnpimporter.models.Sample;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Packs a set of {@link Sample} under a {@link uk.ac.ebi.eva.commons.core.models.IVariantSource} object, adding some
 * additional batch information.
 */
public class SamplesToVariantSourceProcessor implements ItemProcessor<List<Sample>, IVariantSource> {

    public static final String DBSNP_BUILD_KEY = "dbsnp-build";

    public static final String DBSNP_BATCH_KEY = "dbsnp-batch-id";

    private final String dbsnpBuild;

    private final String batchId;

    public SamplesToVariantSourceProcessor(int dbsnpBuild, int batchId) {
        this.dbsnpBuild = String.valueOf(dbsnpBuild);
        this.batchId = String.valueOf(batchId);
    }

    @Override
    public IVariantSource process(List<Sample> samples) throws Exception {
        if (areSamplesFromMultipleBatches(samples)) {
            throw new IllegalArgumentException("Samples must belong to a single batch");
        }

        if (isSampleDuplicated(samples)) {
            throw new IllegalArgumentException("The batch contains duplicate samples");
        }

        // Study ID, file ID, study name, file name
        String batchName = samples.get(0).getBatch();
        String studyId = batchName;
        String studyName = batchName;
        String fileId = batchName;
        String fileName = batchName;

        // Map of samples to position - must follow existing order
        Map<String, Integer> samplesPosition = getSamplesPosition(samples);

        // Add dbSNP build and batch ID as metadata for future reference
        Map<String, Object> metadata = getMetadata();

        // Build any kind of IVariantSource
        return new VariantSource(fileId, fileName, studyId, studyName, StudyType.COLLECTION, Aggregation.NONE,
                                 new Date(), samplesPosition, metadata, null);
    }

    private boolean areSamplesFromMultipleBatches(List<Sample> samples) {
        String batchName = null;

        for (Sample s : samples) {
            if (batchName == null) {
                batchName = s.getBatch();
            } else {
                if (!batchName.equals(s.getBatch())) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isSampleDuplicated(List<Sample> samples) {
        Set<Sample> sampleSet = new HashSet<>(samples);
        return sampleSet.size() < samples.size();
    }

    private Map<String, Integer> getSamplesPosition(List<Sample> samples) {
        Map<String, Integer> samplePositions = new HashMap<>();
        for (int i = 0; i < samples.size(); i++) {
            Sample sample = samples.get(i);
            samplePositions.put(sample.getName(), i);
        }

        return samplePositions;
    }

    private Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(DBSNP_BUILD_KEY, dbsnpBuild);
        metadata.put(DBSNP_BATCH_KEY, batchId);

        return metadata;
    }
}
