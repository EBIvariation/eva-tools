/*
 * Copyright 2015-2016 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.vcfdump;

import htsjdk.tribble.FeatureCodecHeader;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import htsjdk.variant.vcf.VCFUtils;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBIterator;
import org.opencb.opencga.storage.core.variant.adaptors.VariantSourceDBAdaptor;
import org.opencb.opencga.storage.mongodb.variant.DBObjectToVariantSourceConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class VariantExporter {

    private static final Logger logger = LoggerFactory.getLogger(VariantExporter.class);

    /**
     * Read only. Keeps track of the total failed variants across several dumps. To accumulate, use the same instance of
     * VariantExporter to dump several VCFs. If you just want to count on one dump, use a `new VariantExporter` each time.
     */
    private int failedVariants;

    private BiodataVariantToVariantContextConverter variantToVariantContextConverter;

    private Set<String> outputSampleNames;

    public VariantExporter() {
        outputSampleNames = new HashSet<>();
    }

    public List<VariantContext> export(VariantDBIterator iterator, Region region) {
        List<VariantContext> variantsToExport = new ArrayList<>();
        failedVariants = 0;

        // region sequence contains the last exported region: we set it to null to get the new region sequence from cellbase if needed

        while (iterator.hasNext()) {
            Variant variant = iterator.next();
            if (region.contains(variant.getChromosome(), variant.getStart())) {
                try {
                    VariantContext variantContext = variantToVariantContextConverter.transform(variant);
                    variantsToExport.add(variantContext);
                } catch (Exception e) {
                    logger.warn("Variant {}:{}:{}>{} dump failed: {}", variant.getChromosome(), variant.getStart(),
                                variant.getReference(),
                                variant.getAlternate(), e.getMessage());
                    failedVariants++;
                }
            }
        }
        return variantsToExport;
    }

    public List<VariantSource> getSources(VariantSourceDBAdaptor sourceDBAdaptor,
                                          List<String> studyIds, List<String> fileIds)
            throws IllegalArgumentException {
        
        // get sources
        QueryOptions queryOptions = new QueryOptions();
        if (fileIds != null && !fileIds.isEmpty()) {
            queryOptions.put(VariantDBAdaptor.FILE_ID, fileIds);
        }
        List<VariantSource> sourcesList = sourceDBAdaptor.getAllSourcesByStudyIds(studyIds, queryOptions).getResult();
        checkIfThereAreSourceForEveryStudy(studyIds, sourcesList);

        // check if there are conflicts in sample names and create new ones if needed
        Map<String, Map<String, String>> studiesSampleNamesMapping = createNonConflictingSampleNames(sourcesList);
        variantToVariantContextConverter = new BiodataVariantToVariantContextConverter(sourcesList,
                                                                                       studiesSampleNamesMapping);

        return sourcesList;
    }

    private void checkIfThereAreSourceForEveryStudy(List<String> studyIds,
                                                    List<VariantSource> sourcesList) throws IllegalArgumentException {
        List<String> missingStudies =
                studyIds.stream()
                        .filter(study -> sourcesList.stream().noneMatch(source -> source.getStudyId().equals(study)))
                        .collect(Collectors.toList());
        if (!missingStudies.isEmpty()) {
            throw new IllegalArgumentException("Study(ies) " + String.join(", ", missingStudies) + " not found");
        }
    }

    public Map<String, Map<String, String>> createNonConflictingSampleNames(Collection<VariantSource> sources) {
        Map<String, Map<String, String>> filesSampleNamesMapping = null;

        // create a list containing the sample names of every input study
        // if a sample name is in more than one study, it will be several times in the list)
        List<String> originalSampleNames = sources.stream().map(VariantSource::getSamples).flatMap(l -> l.stream())
                                                  .collect(Collectors.toList());
        boolean someSampleNameInMoreThanOneStudy = false;
        if (sources.size() > 1) {
            // if there are several studies, check if there are duplicate elements
            someSampleNameInMoreThanOneStudy = originalSampleNames.stream()
                                                                  .anyMatch(s -> Collections
                                                                          .frequency(originalSampleNames, s) > 1);
            if (someSampleNameInMoreThanOneStudy) {
                filesSampleNamesMapping = resolveConflictsInSampleNamesPrefixingFileId(sources);
            }
        }

        if (!someSampleNameInMoreThanOneStudy) {
            outputSampleNames.addAll(originalSampleNames);
        }

        return filesSampleNamesMapping;
    }

    private Map<String, Map<String, String>> resolveConflictsInSampleNamesPrefixingFileId(
            Collection<VariantSource> sources) {
        // each study will have a map translating from original sample name to "conflict free" one
        Map<String, Map<String, String>> filesSampleNamesMapping = new HashMap<>();
        for (VariantSource source : sources) {
            // create a map from original to "conflict free" sample name (prefixing with study id)
            Map<String, String> fileSampleNamesMapping = new HashMap<>();
            source.getSamples().stream()
                  .forEach(name -> fileSampleNamesMapping.put(name, source.getFileId() + "_" + name));

            // add "conflict free" names to output sample names set
            outputSampleNames.addAll(fileSampleNamesMapping.values());

            // add study map to the "super map" containing all studies
            filesSampleNamesMapping.put(source.getFileId(), fileSampleNamesMapping);
        }
        return filesSampleNamesMapping;
    }

    /**
     * postconditions:
     * - returns one header per study (one header for each key in `sources`).
     *
     * @throws IOException
     * @param sources
     */
    public Map<String, VCFHeader> getVcfHeaders(List<VariantSource> sources) throws IOException {
        Map<String, VCFHeader> headers = new TreeMap<>();

        for (VariantSource source : sources) {
            Object headerObject = source.getMetadata().get(DBObjectToVariantSourceConverter.HEADER_FIELD);

            if (headerObject instanceof String) {
                VCFHeader headerValue = getVcfHeaderFilteringInfoLines((String) headerObject);

                headers.put(source.getStudyId(), headerValue);
            } else {
                throw new IllegalArgumentException("File headers not available for study " + source.getStudyId());
            }
        }

        return headers;
    }

    private VCFHeader getVcfHeaderFilteringInfoLines(String headerObject) throws IOException {
        VCFCodec vcfCodec = new VCFCodec();
        ByteArrayInputStream bufferedInputStream = new ByteArrayInputStream(headerObject.getBytes());
        LineIterator filteringLineIterator = new VcfHeaderFilteringLineIterator(bufferedInputStream, "FILTER", "FORMAT",
                                                                                "INFO");
        FeatureCodecHeader featureCodecHeader = vcfCodec.readHeader(filteringLineIterator);
        return (VCFHeader) featureCodecHeader.getHeaderValue();
    }

    public VCFHeader getMergedVcfHeader(List<VariantSource> sources) throws IOException {
        Map<String, VCFHeader> headers = getVcfHeaders(sources);

        Set<VCFHeaderLine> mergedHeaderLines = VCFUtils.smartMergeHeaders(headers.values(), true);
        VCFHeader header = new VCFHeader(mergedHeaderLines, outputSampleNames);

        header = addMissingMetadataLines(header);

        return header;
    }

    private VCFHeader addMissingMetadataLines(VCFHeader header) {
        // GT line
        if (header.getFormatHeaderLine("GT") == null) {
            header.addMetaDataLine(new VCFFormatHeaderLine("GT", 1, VCFHeaderLineType.String, "Genotype"));
        }
        // CSQ line
        if (header.getFormatHeaderLine("CSQ") == null) {
            header.addMetaDataLine(new VCFInfoHeaderLine("CSQ", 1, VCFHeaderLineType.String,
                                                         "Consequence annotations from Ensembl VEP. " +
                                                                   "Format: Allele|Consequence|SYMBOL|Gene|" +
                                                                   "Feature|BIOTYPE|cDNA_position|CDS_position"));
        }
        return header;
    }

    public int getFailedVariants() {
        return failedVariants;
    }
}
