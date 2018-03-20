/*
 * Copyright 2016 EMBL - European Bioinformatics Institute
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

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.VariantSource;
import uk.ac.ebi.eva.commons.mongodb.filter.FilterBuilder;
import uk.ac.ebi.eva.commons.mongodb.filter.VariantRepositoryFilter;
import uk.ac.ebi.eva.commons.mongodb.services.VariantSourceService;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.vcfdump.evawsclient.EvaWsClient;
import uk.ac.ebi.eva.vcfdump.regionutils.RegionFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class VariantExporterController {

    private static final Logger logger = LoggerFactory.getLogger(VariantExporterController.class);

    private static final int WINDOW_SIZE = 10000;

    private final EvaWsClient evaWsClient;

    private final String dbName;

    private final List<String> studies;

    private List<String> files;

    private String outputDir;

    private final VariantSourceService variantSourceService;

    private final VariantWithSamplesAndAnnotationsService variantService;

    private final QueryParams query;

    private final RegionFactory regionFactory;

    private final VariantExporter exporter;

    private OutputStream outputStream;

    private Path outputFilePath;

    private int failedVariants;

    private int totalExportedVariants;

    private String outputFileName;

    // Constructor used in WS
    public VariantExporterController(String dbName,
                                     VariantSourceService variantSourceService,
                                     VariantWithSamplesAndAnnotationsService variantService,
                                     List<String> studies, OutputStream outputStream,
                                     Properties evaProperties, QueryParams queryParameters)
            throws URISyntaxException {
        this(dbName, variantSourceService, variantService, studies, Collections.emptyList(), evaProperties, queryParameters, WINDOW_SIZE);
        this.outputStream = outputStream;
        LocalDateTime now = LocalDateTime.now();
        outputFileName = dbName.replace("eva_", "") + "_exported_" + now + ".vcf";

    }

    // Constructor used in CLI
    public VariantExporterController(String dbName,
                                     VariantSourceService variantSourceService,
                                     VariantWithSamplesAndAnnotationsService variantService,
                                     List<String> studies, List<String> files,
                                     String outputDir,
                                     Properties evaProperties, QueryParams queryParameters)
            throws URISyntaxException {
        this(dbName, variantSourceService, variantService, studies, files, evaProperties, queryParameters, WINDOW_SIZE);
        if (outputDir == null || outputDir.isEmpty()) {
            throw new IllegalArgumentException("'outputDir' is required");
        }
        this.outputDir = outputDir;
    }

    // private constructor with common parameters
    private VariantExporterController(String dbName,
                                      VariantSourceService variantSourceService,
                                      VariantWithSamplesAndAnnotationsService variantService,
                                      List<String> studies, List<String> files,
                                      Properties evaProperties,
                                      QueryParams queryParameters, int windowSize)
            throws URISyntaxException {
        checkParams(studies, dbName);
        this.dbName = dbName;
        this.studies = studies;
        this.files = files;
        this.variantSourceService = variantSourceService;
        this.variantService = variantService;
        query = queryParameters;
        query.setStudies(studies);
        evaWsClient = getChromosomeWsClient(dbName, evaProperties);
        regionFactory = new RegionFactory(windowSize, variantService);
        exporter = new VariantExporter();
        failedVariants = 0;
        totalExportedVariants = 0;
    }

    // constructor for getting regions
    public VariantExporterController(String dbName,
                                     VariantSourceService variantSourceService,
                                     VariantWithSamplesAndAnnotationsService variantService,
                                     List<String> studies, Properties evaProperties,
                                     QueryParams queryParameters, int blockSize)
            throws URISyntaxException {
        this(dbName, variantSourceService, variantService, studies, null, evaProperties, queryParameters, blockSize);
    }

    private void checkParams(List<String> studies, String dbName) {
        if (studies == null || studies.isEmpty()) {
            throw new IllegalArgumentException("'studies' is required");
        } else if (dbName == null || dbName.isEmpty()) {
            throw new IllegalArgumentException("'dbName' is required");
        }
    }


    private EvaWsClient getChromosomeWsClient(String dbName, Properties evaProperties) throws URISyntaxException {
        return new EvaWsClient(dbName.replace("eva_", ""), evaProperties.getProperty("eva.rest.url"),
                               evaProperties.getProperty("eva.rest.version"));
    }

    public void run() {
        VCFHeader header = getOutputVcfHeader();
        VariantContextWriter writer = getWriter();
        writer.writeHeader(header);
        exportVariants(writer);
        writer.close();
    }

    public void exportHeader() {
        VCFHeader header = getOutputVcfHeader();
        VariantContextWriter writer = getWriter();
        writer.writeHeader(header);
        writer.close();
        logger.info("VCF headers exported");
    }

    public void exportBlock() {
        VCFHeader header = getOutputVcfHeader();
        VariantContextWriter writer = getWriter();
        writer.setHeader(header);
        exportVariants(writer);
        writer.close();
    }

    private void exportVariants(VariantContextWriter writer) {
        // get all chromosomes in the query or organism, and export the variants for each chromosome
        List<VariantRepositoryFilter> filters = new FilterBuilder()
                .getVariantEntityRepositoryFilters(query.getMaf(), query.getPolyphenScore(),
                        query.getSiftScore(), query.getStudies(), query.getConsequenceType());
        Set<String> chromosomes = getChromosomes(query.getRegionsAsList());
        for (String chromosome : chromosomes) {
            exportChromosomeVariants(writer, chromosome, filters);
        }
        logger.info("VCF export summary");
        logger.info("Variants processed: {}", totalExportedVariants + failedVariants);
        logger.info("Variants successfully exported: {}", totalExportedVariants);
        logger.info("Variants with errors: {}", failedVariants);
    }

    private VCFHeader getOutputVcfHeader() {
        // get VCF header(s) and write them to output file(s)
        logger.info("Generating VCF header ...");
        List<VariantSource> sources = exporter.getSources(variantSourceService, studies, files);
        VCFHeader header = null;
        try {
            List<String> exclusions = query.getExclusions();
            boolean excludeAnnotations = false;
            if (exclusions != null)  {
                excludeAnnotations = exclusions.contains("annotation");
            }
            header = exporter.getMergedVcfHeader(sources, excludeAnnotations);
        } catch (IOException e) {
            logger.error("Error getting VCF header: {}", e.getMessage());
        }
        return header;
    }

    private void exportChromosomeVariants(VariantContextWriter writer, String chromosome, List<VariantRepositoryFilter> filters) {
        logger.info("Exporting variants for chromosome {} ...", chromosome);
        List<Region> allRegionsInChromosome = regionFactory.getRegionsForChromosome(chromosome, query);

        for (Region region : allRegionsInChromosome) {
            List<VariantContext> exportedVariants = exporter.export(variantService, filters, region);
            Collections.sort(exportedVariants, (v1, v2) -> v1.getStart() - v2.getStart());
            failedVariants += exporter.getFailedVariants();
            exportedVariants.forEach(writer::add);
            logger.debug("{} variants exported from region {}", exportedVariants.size(), region);
            totalExportedVariants += exportedVariants.size();
        }
    }

    private VariantContextWriter getWriter() {
        VariantContextWriter writer;
        if (outputDir != null) {
            writer = buildVcfFileWriter();
        } else {
            writer = buildVcfOutputStreamWriter();
        }

        return writer;
    }

    private VariantContextWriter buildVcfFileWriter() {
        LocalDateTime now = LocalDateTime.now();
        String fileName = dbName + "_exported_" + now + ".vcf.gz";
        outputFilePath = Paths.get(outputDir).resolve(fileName);

        VariantContextWriterBuilder builder = new VariantContextWriterBuilder();
        return builder.setOutputFile(outputFilePath.toFile()).unsetOption(Options.INDEX_ON_THE_FLY).build();
    }

    private VariantContextWriter buildVcfOutputStreamWriter() {
        VariantContextWriterBuilder builder = new VariantContextWriterBuilder();
        return builder.setOutputVCFStream(outputStream).unsetOption(Options.INDEX_ON_THE_FLY).build();
    }

    private Set<String> getChromosomes(List<String> regions) {
        Set<String> chromosomes;

        if (regions.size() > 0) {
            chromosomes = getChromosomesFromRegionFilter(regions);
        } else {
            chromosomes = evaWsClient.getChromosomes();
        }
        if (chromosomes.isEmpty()) {
            throw new RuntimeException("Chromosomes for dbName " + dbName + " not found");
            // TODO distinct query for getting all the chromosomes from the database
        }
        logger.debug("Chromosomes: {}", String.join(", ", chromosomes));
        return chromosomes;
    }

    private Set<String> getChromosomesFromRegionFilter(List<String> regions) {
        return regions.stream().map(r -> r.split(":")[0]).collect(Collectors.toSet());
    }

    public String getOuputFilePath() {
        return outputFilePath.toString();
    }

    public int getFailedVariants() {
        return failedVariants;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public List<Region> divideChromosomeInChunks(String chromosome, long start, long end) {
        return regionFactory.divideChromosomeInChunks(chromosome, start, end);
    }

    public long getCoordinateOfFirstVariant(String chromosome) {
        return variantService.findChromosomeLowestReportedCoordinate(chromosome, studies);
    }

    public long getCoordinateOfLastVariant(String chromosome) {
        return variantService.findChromosomeHighestReportedCoordinate(chromosome, studies);
    }

    public boolean validateSpecies() {
        // todo add validation after spring data migration
        return true;
    }

    public boolean validateStudies() {
        try {
            List<VariantSource> sources = exporter.getSources(variantSourceService, studies, files);
            return !sources.isEmpty();
        } catch (Exception e) {
            logger.error("Error validating studies", e);
        }
        return false;
    }
}
