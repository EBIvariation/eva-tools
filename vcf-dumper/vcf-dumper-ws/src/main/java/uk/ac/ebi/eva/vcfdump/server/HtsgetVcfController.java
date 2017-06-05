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
package uk.ac.ebi.eva.vcfdump.server;

import io.swagger.annotations.Api;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.StorageManagerException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import uk.ac.ebi.eva.vcfdump.VariantExporterController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by amilamanoj on 13.05.17.
 */
@RestController
@RequestMapping(value = "/variants/")
@Api(tags = {"htsget"})
public class HtsgetVcfController {

    private static final String VCF = "VCF";

    private static final int BLOCK_SIZE = 250;

    private Properties evaProperties;

    public HtsgetVcfController() throws IOException {
        evaProperties = new Properties();
        evaProperties.load(VcfDumperWSServer.class.getResourceAsStream("/eva.properties"));
    }

    @RequestMapping(value = "/{id}/", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity getHtsgetUrls(
            @PathVariable("id") String id,
            @RequestParam(name = "format", required = false) String format,
            @RequestParam(name = "referenceName", required = false) String referenceName,
            @RequestParam(name = "species", required = false) String species,
            @RequestParam(name = "start", required = false, defaultValue = "0") int start,
            @RequestParam(name = "end", required = false, defaultValue = "0") int end,
            @RequestParam(name = "fields", required = false) List<String> fields,
            @RequestParam(name = "tags", required = false, defaultValue = "") String tags,
            @RequestParam(name = "notags", required = false, defaultValue = "") String notags,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IllegalAccessException, IllegalOpenCGACredentialsException, InstantiationException, IOException,
            StorageManagerException, URISyntaxException, ClassNotFoundException {

        if (!VCF.equals(format)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new HtsGetError("UnsupportedFormat", "Specified format is not supported by this server"));
        }

        if (end <= start) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new HtsGetError("InvalidRange", "The requested range cannot be satisfied"));
        }


        HtsGetResponse htsGetResponse = new HtsGetResponse();
        htsGetResponse.setFormat(VCF);
        htsGetResponse
                .setUrls(getUrls(request.getLocalName() + ":" + request.getLocalPort(), id, referenceName, species,
                                 start, end));
        return ResponseEntity.status(HttpStatus.OK).body(htsGetResponse);

    }


    @RequestMapping(value = "/headers", method = RequestMethod.GET, produces = "application/octet-stream")
    public StreamingResponseBody getHtsgetHeaders(
            @RequestParam(name = "species") String species,
            @RequestParam(name = "studies") List<String> studies,
            HttpServletResponse response)
            throws IllegalAccessException, IllegalOpenCGACredentialsException, InstantiationException, IOException,
            StorageManagerException, URISyntaxException, ClassNotFoundException {

        String dbName = "eva_" + species;

        StreamingResponseBody responseBody = getStreamingHeaderResponse(dbName, studies, evaProperties,
                                                                        new MultivaluedHashMap<>(), response);

        return responseBody;
    }


    @RequestMapping(value = "/block", method = RequestMethod.GET, produces = "application/octet-stream")
    public StreamingResponseBody getHtsgetBlocks(
            @RequestParam(name = "species") String species,
            @RequestParam(name = "studies") List<String> studies,
            @RequestParam(name = "chr") String chrRegion,
            HttpServletResponse response)
            throws IllegalAccessException, IllegalOpenCGACredentialsException, InstantiationException, IOException,
            StorageManagerException, URISyntaxException, ClassNotFoundException {

        String dbName = "eva_" + species;

        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
        queryParameters.put(VariantDBAdaptor.REGION, Collections
                .singletonList(chrRegion));

        StreamingResponseBody responseBody = getStreamingBlockResponse(dbName, studies, evaProperties,
                                                                       queryParameters, response);
        return responseBody;
    }

    private List<Map<String, String>> getUrls(String host, String id, String chromosome, String species,
                                              int start, int end) {
        List<Map<String, String>> urls = new ArrayList<>();

        String headerUrl = host + "/variants/headers?species=" + species + "&studies=" + id;
        Map<String, String> urlMap = new HashMap<>();
        urlMap.put("url", headerUrl);
        urls.add(urlMap);

        String baseUrl = host + "/variants/block";
        baseUrl = baseUrl + "?studies=" + id + "&species=" + species + "&chr=" + chromosome + ":";
        return getUrlsByBlockSize(urls, baseUrl, start, end, BLOCK_SIZE);
    }

    private List<Map<String, String>> getUrlsByBlockSize(List<Map<String, String>> urls, String baseUrl,
                                                         int start,
                                                         int end, int blockSize) {
        int range = end - start;

        //int blockSize = range / blockCount; // in case block count is used to divide blocks
        int blockCount = range / blockSize;
        int remainder = range % blockSize;
        for (int i = 0; i < blockCount; i++) {
            String url = baseUrl + (start + (blockSize * i + (i > 0 ? 1 : 0))) + "-" + (start + blockSize * (i + 1));
            Map<String, String> urlMap = new HashMap<>();
            urlMap.put("url", url);
            urls.add(urlMap);
        }
        if (remainder > 0) {
            String url = baseUrl + (start + (blockSize * blockCount) + 1) + "-" + end;
            Map<String, String> urlMap = new HashMap<>();
            urlMap.put("url", url);
            urls.add(urlMap);
        }
        return urls;
    }

    private StreamingResponseBody getStreamingHeaderResponse(String dbName, List<String> studies,
                                                             Properties evaProperties,
                                                             MultivaluedMap<String, String> queryParameters,
                                                             HttpServletResponse response) {
        return outputStream -> {
            VariantExporterController controller;
            try {
                controller = new VariantExporterController(dbName, studies, outputStream, evaProperties,
                                                           queryParameters);
                // tell the client that the file is an attachment, so it will download it instead of showing it
                response.addHeader(HttpHeaders.CONTENT_DISPOSITION,
                                   "attachment;filename=" + controller.getOutputFileName());
                controller.writeHeader();
            } catch (Exception e) {
                throw new WebApplicationException(e);
            }
        };
    }

    private StreamingResponseBody getStreamingBlockResponse(String dbName, List<String> studies,
                                                            Properties evaProperties,
                                                            MultivaluedMap<String, String> queryParameters,
                                                            HttpServletResponse response) {
        return outputStream -> {
            VariantExporterController controller;
            try {
                controller = new VariantExporterController(dbName, studies, outputStream, evaProperties,
                                                           queryParameters);
                // tell the client that the file is an attachment, so it will download it instead of showing it
                response.addHeader(HttpHeaders.CONTENT_DISPOSITION,
                                   "attachment;filename=" + controller.getOutputFileName());
                controller.writeBlock();
            } catch (Exception e) {
                throw new WebApplicationException(e);
            }
        };
    }

    private class HtsGetResponse {
        private String format;

        private List<Map<String, String>> urls;

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public List<Map<String, String>> getUrls() {
            return urls;
        }

        public void setUrls(List<Map<String, String>> urls) {
            this.urls = urls;
        }
    }

    private class HtsGetError {
        private String error;

        private String message;

        public HtsGetError(String error, String message) {
            this.error = error;
            this.message = message;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
