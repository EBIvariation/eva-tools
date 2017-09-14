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
import org.opencb.biodata.models.feature.Region;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@RestController
@RequestMapping(value = "/variants/")
@Api(tags = {"htsget"})
public class HtsgetVcfController {

    private static final String VCF = "VCF";

    private Properties evaProperties;

    public HtsgetVcfController() throws IOException {
        evaProperties = new Properties();
        evaProperties.load(VcfDumperWSServer.class.getResourceAsStream("/eva.properties"));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, consumes = "application/*",
            produces = "application/vnd.ga4gh.htsget.v0.2rc+json; charset=UTF-8")
    public ResponseEntity getHtsgetUrls(
            @PathVariable("id") String id,
            @RequestParam(name = "format", required = false) String format,
            @RequestParam(name = "referenceName", required = false) String referenceName,
            @RequestParam(name = "species", required = false) String species,
            @RequestParam(name = "start", required = false) Integer start,
            @RequestParam(name = "end", required = false) Integer end,
            @RequestParam(name = "fields", required = false) List<String> fields,
            @RequestParam(name = "tags", required = false, defaultValue = "") String tags,
            @RequestParam(name = "notags", required = false, defaultValue = "") String notags,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IllegalAccessException, IllegalOpenCGACredentialsException, InstantiationException, IOException,
            StorageManagerException, URISyntaxException, ClassNotFoundException {

        if (!VCF.equals(format)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("htsget",
                            new HtsGetError("UnsupportedFormat", "Specified format is not supported by this server")));
        }

        String dbName = "eva_" + species;
        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
        queryParameters.put(VariantDBAdaptor.REGION, Collections.singletonList(referenceName));

        int blockSize = Integer.parseInt(evaProperties.getProperty("eva.htsget.blocksize"));

        VariantExporterController controller = new VariantExporterController(dbName, Arrays.asList(id.split(",")),
                                                                             evaProperties,
                                                                             queryParameters, blockSize);
        if (!controller.validateSpecies()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("htsget",
                            new HtsGetError("InvalidInput", "The requested species is not available")));
        }
        if (!controller.validateStudies()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("htsget",
                            new HtsGetError("InvalidInput", "The requested study(ies) is not available")));
        }
        if (start != null && referenceName == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("htsget",
                    new HtsGetError("InvalidInput", "Reference name is not specified when start is specified")));
        }

        if (start == null) {
            start = controller.getCoordinateOfFirstVariant(referenceName);
        }
        if (end == null) {
            end = controller.getCoordinateOfLastVariant(referenceName);
        }

        if (end <= start) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("htsget",
                    new HtsGetError("InvalidRange", "The requested range cannot be satisfied")));
        }

        List<Region> regionList = controller.divideChromosomeInChunks(referenceName, start, end);

        HtsGetResponse htsGetResponse = new HtsGetResponse(VCF, request.getLocalName() + ":" + request.getLocalPort(),
                                                           id, referenceName, species, regionList);
        return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("htsget",  htsGetResponse));
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
            @RequestParam(name = "region") String chrRegion,
            HttpServletResponse response)
            throws IllegalAccessException, IllegalOpenCGACredentialsException, InstantiationException, IOException,
            StorageManagerException, URISyntaxException, ClassNotFoundException {

        String dbName = "eva_" + species;

        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
        queryParameters.put(VariantDBAdaptor.REGION, Collections.singletonList(chrRegion));

        StreamingResponseBody responseBody = getStreamingBlockResponse(dbName, studies, evaProperties,
                                                                       queryParameters, response);
        return responseBody;
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

}
