/*
 *
 * Copyright 2020 EMBL - European Bioinformatics Institute
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
 *
 */
package uk.ac.ebi.eva.vcfdump.server.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.vcfdump.server.model.HtsGetResponse;
import uk.ac.ebi.eva.vcfdump.server.model.UrlResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VcfDumperIntegrationTest {

    private static final String EVA_ECABALLUS_20_DB = "eva_ecaballus_20";

    private static final String EVA_NO_VARIANTS_DB = "eva_no_variants";

    private static final String FILES_COLLECTION = "files";

    private static final String VARIANTS_COLLECTION = "variants";

    private static final String ANNOTATIONS_COLLECTION = "annotations";

    private static final String ANNOTATIONS_METADATA_COLLECTION = "annotationsMetadata";

    private static final int BLOCK_SIZE = 1000;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        createHorseDatabase();
        createNoVariantsDatabase();
    }

    private void createHorseDatabase() throws IOException, URISyntaxException {
        mongoClient.getDatabase(EVA_ECABALLUS_20_DB).drop();
        MongoDatabase eva_ecaballus_20 = mongoClient.getDatabase(EVA_ECABALLUS_20_DB);
        eva_ecaballus_20.createCollection(FILES_COLLECTION);
        insertIntoCollectionFromFile(eva_ecaballus_20, FILES_COLLECTION, "test-data/files.json");
        eva_ecaballus_20.createCollection(VARIANTS_COLLECTION);
        insertIntoCollectionFromFile(eva_ecaballus_20, VARIANTS_COLLECTION, "test-data/variants.json");
        eva_ecaballus_20.createCollection(ANNOTATIONS_COLLECTION);
        insertIntoCollectionFromFile(eva_ecaballus_20, ANNOTATIONS_COLLECTION, "test-data/annotations.json");
        eva_ecaballus_20.createCollection(ANNOTATIONS_METADATA_COLLECTION);
        insertIntoCollectionFromFile(eva_ecaballus_20, ANNOTATIONS_METADATA_COLLECTION,
                                     "test-data/annotationsMetadata.json");
    }

    private void insertIntoCollectionFromFile(MongoDatabase mongoDatabase, String collection, String path)
            throws IOException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        URI uri = Objects.requireNonNull(classLoader.getResource(path)).toURI();
        String json = new String(Files.readAllBytes(Paths.get(uri)), StandardCharsets.UTF_8);
        Document document = Document.parse(json);
        mongoDatabase.getCollection(collection).insertOne(document);
    }

    private void createNoVariantsDatabase() {
        mongoClient.getDatabase(EVA_NO_VARIANTS_DB).drop();
        MongoDatabase eva_no_variants = mongoClient.getDatabase(EVA_NO_VARIANTS_DB);
        eva_no_variants.createCollection(FILES_COLLECTION);
        insertOneIntoFilesCollection(eva_no_variants, "1");
    }

    private void insertOneIntoFilesCollection(MongoDatabase mongoDatabase, String sid) {
        Document file = new Document();
        file.put("sid", sid);
        mongoDatabase.getCollection(FILES_COLLECTION).insertOne(file);
    }

    @After
    public void tearDown() {
        mongoClient.getDatabase(EVA_ECABALLUS_20_DB).drop();
        mongoClient.getDatabase(EVA_NO_VARIANTS_DB).drop();
    }

    /**
     * Test segments endpoint from {@link HtsgetVcfController#getHtsgetUrls}
     */
    @Test
    public void getHtsgetUrls() {
        String url = "/v1/variants/PRJEB9799?format=VCF&referenceName=1&species=ecaballus_20&start=3000000&end=3010000";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertUrls(response, 3000000, 3010000);
    }

    private void assertUrls(ResponseEntity<String> response, int start, int end) {
        HtsGetResponse htsGetResponse = getUrlsFromResponse(response);
        assertEquals("VCF", htsGetResponse.getFormat());
        List<UrlResponse> urls = htsGetResponse.getUrls();
        long numberOfHeaderUrls = urls.stream().filter(u -> u.getUrlClass().equals("header")).count();
        assertEquals(1, numberOfHeaderUrls);
        long numberOfBodyUrls = urls.stream().filter(u -> u.getUrlClass().equals("body")).count();
        int expectedNumberOfBlocks = expectedNumberOfBlocks(start, end);
        assertEquals(expectedNumberOfBlocks, numberOfBodyUrls);
    }

    private HtsGetResponse getUrlsFromResponse(ResponseEntity<String> response) {
        Configuration configuration = Configuration.defaultConfiguration()
                                                   .jsonProvider(new JacksonJsonProvider())
                                                   .mappingProvider(new JacksonMappingProvider(objectMapper))
                                                   .addOptions(Option.SUPPRESS_EXCEPTIONS);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        HtsGetResponse htsGetResponse = JsonPath.using(configuration)
                                                .parse(response.getBody())
                                                .read("$['htsget']", new TypeRef<HtsGetResponse>() {});
        return htsGetResponse;
    }

    private int expectedNumberOfBlocks(int start, int end) {
        double positions = end - start;
        double blocks = Math.ceil(positions / BLOCK_SIZE);
        return (int) blocks;
    }

    /**
     * Test segments endpoint from {@link HtsgetVcfController#getHtsgetUrls}
     * To retrieve the HTSGET urls it is only needed that the study is present in the files collection. It will return
     * them even if there is not variants collection.
     */
    @Test
    public void getHtsgetUrlsFromNoVariantsDatabase() {
        String url = "/v1/variants/1?format=VCF&referenceName=1&species=no_variants&start=1000&end=2000";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertUrls(response, 1000, 2000);
    }

    /**
     * Test segments endpoint from {@link HtsgetVcfController#getHtsgetHeaders}
     */
    @Test
    public void getHtsgetHeaders() {
        String url = "/v1/variants/headers?species=ecaballus_20&studies=PRJEB9799";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertVcfHeader(response.getBody(), 41);
    }

    private void assertVcfHeader(String header, int expectedNumberOfLines) {
        List<String> headerLines = Arrays.asList(header.split("\n"));
        assertEquals(1, grep(headerLines, "##fileformat=VCFv4.2").size());
        assertEquals(1, grep(headerLines, "##FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">").size());
        assertEquals(1, grep(headerLines, "##INFO=<ID=CSQ,Number=1,Type=String,Description=\"Consequence annotations " +
                "from Ensembl VEP. Format: Allele\\|Consequence\\|SYMBOL\\|Gene\\|Feature\\|BIOTYPE\\|cDNA_position" +
                "\\|CDS_position\">").size());
        assertEquals(1, grep(headerLines, "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO.*").size());
        assertEquals(expectedNumberOfLines, grep(headerLines, "^#.*").size());
    }

    private List<String> grep(List<String> lines, String regex) {
        List<String> matchedLines = new ArrayList<>();
        for (String line : lines) {
            if (line.matches(regex)) {
                matchedLines.add(line);
            }
        }
        return matchedLines;
    }

    /**
     * Test segments endpoint from {@link HtsgetVcfController#getHtsgetBlocks}
     */
    @Test
    public void getHtsgetBlocks() {
        String url = "/v1/variants/block?studies=PRJEB9799&species=ecaballus_20&region=1:3000000-3000999";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertBody(response.getBody(), 1);
        String expected = "1\t3000829\t.\tC\tT\t.\t.\tCSQ=T|intergenic_variant||||||\tGT\t0/1\t0/0\t0/0\t0/0\t0/0\t0" +
                "/0\n";
        String data = response.getBody();
        assertEquals(expected, data);
    }

    private void assertBody(String body, int expectedNumberOfLines) {
        List<String> lines = Arrays.asList(body.split("\n"));
        int numberOfDataLines = grep(lines, "^(?!#).*").size();
        assertEquals(expectedNumberOfLines, numberOfDataLines);
    }

    /**
     * Test segments endpoint from {@link VcfDumperWSServer#getVariantsByRegionStreamingOutput}
     */
    @Test
    public void vcfDumper() {
        String url = "/v1/segments/1:3000800-3000900/variants?species=ecaballus_20&studies=PRJEB9799";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertVcfHeader(response.getBody(), 41);
        assertBody(response.getBody(), 1);
    }
}
