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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.mongodb.services.VariantSourceService;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.vcfdump.QueryParams;
import uk.ac.ebi.eva.vcfdump.VariantExporterController;
import uk.ac.ebi.eva.vcfdump.server.configuration.DBAdaptorConnector;
import uk.ac.ebi.eva.vcfdump.server.configuration.MultiMongoDbFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @Autowired
    private VariantSourceService variantSourceService;
    @Autowired
    private VariantWithSamplesAndAnnotationsService variantService;

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
            @RequestParam(name = "start", required = false) Long start,
            @RequestParam(name = "end", required = false) Long end,
            @RequestParam(name = "fields", required = false) List<String> fields,
            @RequestParam(name = "tags", required = false, defaultValue = "") String tags,
            @RequestParam(name = "notags", required = false, defaultValue = "") String notags,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IllegalAccessException, InstantiationException, IOException,
            URISyntaxException, ClassNotFoundException {

        if (!VCF.equals(format)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("htsget",
                            new HtsGetError("UnsupportedFormat", "Specified format is not supported by this server")));
        }

        String dbName = DBAdaptorConnector.getDBName(species);
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(dbName);

        int blockSize = Integer.parseInt(evaProperties.getProperty("eva.htsget.blocksize"));

        VariantExporterController controller = new VariantExporterController(dbName,
                                                                             variantSourceService,
                                                                             variantService,
                                                                             Arrays.asList(id.split(",")),
                                                                             evaProperties,
                                                                             new QueryParams(), blockSize);
        ResponseEntity errorResponse = validateRequest(referenceName, start, controller);
        if (errorResponse != null) {
            return errorResponse;
        }

        if (start != null && end != null && end <= start) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("htsget",
                    new HtsGetError("InvalidRange", "The requested range cannot be satisfied")));
        }

        if (start == null) {
            start = controller.getCoordinateOfFirstVariant(referenceName);
        }
        if (end == null) {
            end = controller.getCoordinateOfLastVariant(referenceName);
        }

        if (end <= start) {
            // Applies to valid requests such as chromosome 1, start: 1.000.000, end: empty.
            // If variants exist only in region 200.000 to 800.000, getCoordinateOfLastVariant() will return 800.000.
            // Given that 800.000 < 1.000.000, no region can be found.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("htsget",
                                                                                             new HtsGetError("NotFound", "The resource requested was not found")));
        }

        List<Region> regionList = controller.divideChromosomeInChunks(referenceName, start, end);

        HtsGetResponse htsGetResponse = new HtsGetResponse(VCF, request.getLocalName() + ":" + request.getLocalPort(),
                                                           request.getContextPath(), id, referenceName, species,
                                                           regionList);
        return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("htsget",  htsGetResponse));
    }

    private ResponseEntity validateRequest(String referenceName, Long start, VariantExporterController controller) {
        if (!controller.validateSpecies()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Collections.singletonMap("htsget", new HtsGetError("InvalidInput", "The requested species is not available")));
        }
        if (!controller.validateStudies()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Collections.singletonMap("htsget", new HtsGetError("InvalidInput", "The requested study(ies) is not available")));
        }
        if (start != null && referenceName == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Collections.singletonMap("htsget", new HtsGetError("InvalidInput", "Reference name is not specified when start is specified")));
        }
        return null;
    }

    @RequestMapping(value = "/headers", method = RequestMethod.GET, produces = "application/octet-stream")
    public StreamingResponseBody getHtsgetHeaders(
            @RequestParam(name = "species") String species,
            @RequestParam(name = "studies") List<String> studies,
            HttpServletResponse response)
            throws IllegalAccessException, InstantiationException, IOException,
            URISyntaxException, ClassNotFoundException {

        String dbName = "eva_" + species;
        StreamingResponseBody responseBody = getStreamingHeaderResponse(dbName, studies, evaProperties,
                                                                        new QueryParams(), response);
        return responseBody;
    }


    @RequestMapping(value = "/block", method = RequestMethod.GET, produces = "application/octet-stream")
    public StreamingResponseBody getHtsgetBlocks(
            @RequestParam(name = "species") String species,
            @RequestParam(name = "studies") List<String> studies,
            @RequestParam(name = "region") String chrRegion,
            HttpServletResponse response) {

        String dbName = "eva_" + species;

        QueryParams queryParameters = new QueryParams();
        queryParameters.setRegion(chrRegion);

        StreamingResponseBody responseBody = getStreamingBlockResponse(dbName, studies, evaProperties,
                                                                       queryParameters, response);
        return responseBody;
    }


    private StreamingResponseBody getStreamingHeaderResponse(String dbName, List<String> studies,
                                                             Properties evaProperties,
                                                             QueryParams queryParameters,
                                                             HttpServletResponse response) {
        return outputStream -> {
            VariantExporterController controller;
            try {
                MultiMongoDbFactory.setDatabaseNameForCurrentThread(dbName);
                controller = new VariantExporterController(dbName, variantSourceService, variantService, studies, outputStream, evaProperties,
                                                           queryParameters);
                // tell the client that the file is an attachment, so it will download it instead of showing it
                response.addHeader(HttpHeaders.CONTENT_DISPOSITION,
                                   "attachment;filename=" + controller.getOutputFileName());
                controller.exportHeader();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private StreamingResponseBody getStreamingBlockResponse(String dbName, List<String> studies,
                                                            Properties evaProperties,
                                                            QueryParams queryParameters,
                                                            HttpServletResponse response) {
        return outputStream -> {
            VariantExporterController controller;
            try {
                MultiMongoDbFactory.setDatabaseNameForCurrentThread(dbName);
                controller = new VariantExporterController(dbName, variantSourceService,
                                                           variantService, studies, outputStream, evaProperties,
                                                           queryParameters);
                // tell the client that the file is an attachment, so it will download it instead of showing it
                response.addHeader(HttpHeaders.CONTENT_DISPOSITION,
                                   "attachment;filename=" + controller.getOutputFileName());
                controller.exportBlock();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}
