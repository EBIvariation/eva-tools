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
package embl.ebi.variation.eva.vcfdump;

import embl.ebi.variation.eva.vcfdump.cellbasewsclient.CellbaseWSClient;
import embl.ebi.variation.eva.vcfdump.regionutils.RegionFactory;
import htsjdk.samtools.SAMException;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFHeader;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.config.DataStoreServerAddress;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.StorageManagerException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBIterator;
import org.opencb.opencga.storage.core.variant.adaptors.VariantSourceDBAdaptor;
import org.opencb.opencga.storage.mongodb.utils.MongoCredentials;
import org.opencb.opencga.storage.mongodb.variant.VariantMongoDBAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jmmut on 2016-01-20.
 *
 * @author Jose Miguel Mut Lopez &lt;jmmut@ebi.ac.uk&gt;
 */
public class VariantExporterController {

    private static final Logger logger = LoggerFactory.getLogger(VariantExporterController.class);
    private static final int WINDOW_SIZE = 10000;

    private final CellbaseWSClient cellBaseClient;
    private final String species;
    private final List<String> studies;
    private Properties evaProperties;
    private List<String> files;
    private String outputDir;
    private final VariantSourceDBAdaptor variantSourceDBAdaptor;
    private final VariantDBAdaptor variantDBAdaptor;
    private final QueryOptions query;
    private final RegionFactory regionFactory;
    private final VariantExporter exporter;
    private OutputStream outputStream;
    private Path outputFilePath;
    private int failedVariants;
    private int totalExportedVariants;
    private String outputFileName;

    // Constructor used in WS
    public VariantExporterController(String species, String dbName, List<String> studies, OutputStream outputStream,
                                     Properties evaProperties, MultivaluedMap<String, String> queryParameters)
            throws IllegalAccessException, ClassNotFoundException, InstantiationException, StorageManagerException, URISyntaxException, IllegalOpenCGACredentialsException, UnknownHostException {
        this(species, dbName, studies, Collections.EMPTY_LIST, evaProperties, queryParameters);
        this.outputStream = outputStream;
        LocalDateTime now = LocalDateTime.now();
        outputFileName = species + "_exported_" + now + ".vcf";

    }

    // Constructor used in CLI
    public VariantExporterController(String species, String dbName, List<String> studies, List<String> files, String outputDir,
                                     Properties evaProperties, MultivaluedMap<String, String> queryParameters)
            throws IllegalAccessException, ClassNotFoundException, InstantiationException, URISyntaxException, IllegalOpenCGACredentialsException, UnknownHostException {
        this(species, dbName, studies, files, evaProperties, queryParameters);
        checkParams(species, studies, outputDir, dbName);
        this.outputDir = outputDir;
    }

    // private constructor with common parameters
    private VariantExporterController(String species, String dbName, List<String> studies, List<String> files, Properties evaProperties,
                                      MultivaluedMap<String, String> queryParameters)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, URISyntaxException, UnknownHostException, IllegalOpenCGACredentialsException {
        this.species = species;
        this.studies = studies;
        this.files = files;
        this.evaProperties = evaProperties;
        variantDBAdaptor = getVariantDBAdaptor(species, dbName, evaProperties);
        query = getQuery(queryParameters);
        cellBaseClient = getCellbaseClient(species, evaProperties);
        variantSourceDBAdaptor = variantDBAdaptor.getVariantSourceDBAdaptor();
        regionFactory = new RegionFactory(WINDOW_SIZE, variantDBAdaptor, query);
        exporter = new VariantExporter(cellBaseClient);
        failedVariants = 0;
        totalExportedVariants = 0;
    }

    private void checkParams(String species, List<String> studies, String outputDir, String dbName) {
        if (species == null || species.isEmpty()) {
            throw new IllegalArgumentException("'species' is required");
        } else if (studies == null || studies.isEmpty()) {
            throw new IllegalArgumentException("'studies' is required");
        } else if (outputDir == null || outputDir.isEmpty()) {
            throw new IllegalArgumentException("'outputDir' is required");
        } else if (dbName == null || dbName.isEmpty()) {
            throw new IllegalArgumentException("'dbName' is required");
        }
    }

    public VariantDBAdaptor getVariantDBAdaptor(String species, String dbName, Properties properties) throws IllegalOpenCGACredentialsException, UnknownHostException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        MongoCredentials credentials = getCredentials(species, dbName, properties);
        return new VariantMongoDBAdaptor(credentials, properties.getProperty("eva.mongo.collections.variants"),
                properties.getProperty("eva.mongo.collections.files"));

    }

    private MongoCredentials getCredentials(String species, String dbName, Properties properties) throws IllegalOpenCGACredentialsException {
        if (species == null || species.isEmpty()) {
            throw new IllegalArgumentException("Please specify a species");
        }

        String[] hosts = properties.getProperty("eva.mongo.host").split(",");
        List<DataStoreServerAddress> servers = new ArrayList();

        // Get the list of hosts (optionally including the port number)
        for (String host : hosts) {
            String[] params = host.split(":");
            if (params.length > 1) {
                servers.add(new DataStoreServerAddress(params[0], Integer.parseInt(params[1])));
            } else {
                servers.add(new DataStoreServerAddress(params[0], 27017));
            }
        }

        if (dbName == null) {
            dbName = "eva_" + species;
        }
        MongoCredentials credentials = new MongoCredentials(servers,
                dbName,
                properties.getProperty("eva.mongo.user"),
                properties.getProperty("eva.mongo.passwd"));

        // Set authentication database, if specified in the configuration
        credentials.setAuthenticationDatabase(properties.getProperty("eva.mongo.auth.db", null));

        return credentials;
    }

    private CellbaseWSClient getCellbaseClient(String species, Properties evaProperties) throws URISyntaxException {
        return new CellbaseWSClient(species, evaProperties.getProperty("cellbase.rest.url"), evaProperties.getProperty("cellbase.version"));
    }

    public QueryOptions getQuery(MultivaluedMap<String, String> queryParameters) {
        QueryOptions query = new QueryOptions();
        query.put(VariantDBAdaptor.STUDIES, studies);
        if (files != null && files.size() > 0) {
            query.put(VariantDBAdaptor.FILES, files);
            if (files.size() == 1) {
                // this will reduce the data fetched by the mongo driver, improving drastically the performance for databases when many
                // projects have been previously loaded
                query.add(VariantDBAdaptor.FILE_ID, files.get(0));
            }
        }

        queryParameters.forEach((parameterName, parameterValues) -> {
            if (VariantDBAdaptor.QueryParams.acceptedValues.contains(parameterName)) {
                query.add(parameterName, String.join(",", parameterValues));
            }
        });

        // exclude fields not needed
        query.put("exclude", "sourceEntries.cohortStats");
        query.put("exclude", "annotation");

        return query;
    }

    public void run() {
        VCFHeader header = getOutputVcfHeader();
        VariantContextWriter writer = getWriter(header);
        writer.writeHeader(header);

        // get all chromosomes in the query or organism, and export the variants for each chromosome
        Set<String> chromosomes = getChromosomes(query);
        for (String chromosome : chromosomes) {
            exportChromosomeVariants(writer, chromosome);
        }

        writer.close();
        logger.info("VCF export summary");
        logger.info("Variants processed: {}", totalExportedVariants + failedVariants);
        logger.info("Variants successfully exported: {}", totalExportedVariants);
        logger.info("Variants with errors: {}", failedVariants);
    }

    private VCFHeader getOutputVcfHeader() {
        // get VCF header(s) and write them to output file(s)
        logger.info("Generating VCF header ...");
        Map<String, VariantSource> sources = exporter.getSources(variantSourceDBAdaptor, studies);
        VCFHeader header = null;
        try {
            header = exporter.getMergedVCFHeader(sources);
        } catch (IOException e) {
            logger.error("Error getting VCF header: {}", e.getMessage());
        }
        return header;
    }

    private void exportChromosomeVariants(VariantContextWriter writer, String chromosome) {
        logger.info("Exporting variants for chromosome {} ...", chromosome);
        List<Region> allRegionsInChromosome = regionFactory.getRegionsForChromosome(chromosome);
        for (Region region : allRegionsInChromosome) {
            VariantDBIterator regionVariantsIterator = variantDBAdaptor.iterator(getRegionQuery(region));
            List<VariantContext> exportedVariants = exporter.export(regionVariantsIterator, region);
            Collections.sort(exportedVariants, (v1, v2) -> v1.getStart() - v2.getStart());
            failedVariants += exporter.getFailedVariants();
            exportedVariants.forEach(writer::add);
            logger.debug("{} variants exported from region {}", exportedVariants.size(), region);
            totalExportedVariants += exportedVariants.size();
        }
    }

    private QueryOptions getRegionQuery(Region region) {
        QueryOptions regionQuery = new QueryOptions(query);
        regionQuery.put(VariantDBAdaptor.REGION, region.toString());
        return regionQuery;
    }

    private VariantContextWriter getWriter(VCFHeader vcfHeader) {
        // get sequence dictionary from header
        SAMSequenceDictionary sequenceDictionary;
        try {
            sequenceDictionary = vcfHeader.getSequenceDictionary();
        } catch (SAMException e) {
            logger.warn("Incorrect sequence / contig meta-data: ", e.getMessage());
            logger.warn("It won't be included in output VCF header");
            sequenceDictionary = null;
        }

        VariantContextWriter writer;
        if (outputDir != null) {
            writer = buildVCFFileWriter(sequenceDictionary);
        } else {
            writer = buildVCFOutputStreamWriter(sequenceDictionary);
        }

        return writer;
    }

    private VariantContextWriter buildVCFFileWriter(SAMSequenceDictionary sequenceDictionary) {
        LocalDateTime now = LocalDateTime.now();
        String fileName = species + "_exported_" + now + ".vcf.gz";
        outputFilePath = Paths.get(outputDir).resolve(fileName);

        VariantContextWriterBuilder builder = new VariantContextWriterBuilder();
        VariantContextWriter writer = builder.setOutputFile(outputFilePath.toFile())
                .setReferenceDictionary(sequenceDictionary)
                .unsetOption(Options.INDEX_ON_THE_FLY)
                .build();
        return writer;
    }

    private VariantContextWriter buildVCFOutputStreamWriter(SAMSequenceDictionary sequenceDictionary) {
        VariantContextWriterBuilder builder = new VariantContextWriterBuilder();
        VariantContextWriter writer = builder.setOutputVCFStream(outputStream)
                .setReferenceDictionary(sequenceDictionary)
                .unsetOption(Options.INDEX_ON_THE_FLY)
                .build();
        return writer;
    }

    private Set<String> getChromosomes(QueryOptions options) {
        Set<String> chromosomes;

        List<String> regions = options.getAsStringList(VariantDBAdaptor.REGION);
        if (regions.size() > 0) {
            chromosomes = getChromosomesFromRegionFilter(regions);
        } else {
            chromosomes = cellBaseClient.getChromosomes();
        }
        if (chromosomes.isEmpty()) {
            throw new RuntimeException("Chromosomes for species " + species + " not found");
            // TODO distinct query for getting all the chromosomes from the database
        }
        logger.debug("Chromosomes: {}", String.join(", ", chromosomes));
        return chromosomes;
    }

    private Set<String> getChromosomesFromRegionFilter(List<String> regions) {
        return regions.stream().map(r -> r.split(":")[0]).collect(Collectors.toSet());
    }

    /*
     @deprecated this method is not going to work well because the sequence dictionaries are not stored correctly in the files collection
      */
    @Deprecated
    private Set<String> getChromosomesFromVCFHeader(VCFHeader header, List<String> studyIds) {
        Set<String> chromosomes = new HashSet<>();
        // setup writers
        for (String studyId : studyIds) {
            SAMSequenceDictionary sequenceDictionary = header.getSequenceDictionary();
            chromosomes.addAll(sequenceDictionary.getSequences().stream().map(SAMSequenceRecord::getSequenceName).collect(Collectors.toSet()));
        }

        return chromosomes;
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
}
