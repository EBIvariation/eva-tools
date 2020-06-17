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
package uk.ac.ebi.eva.vcfdump.server.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
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
import uk.ac.ebi.eva.vcfdump.configuration.DBAdaptorConnector;
import uk.ac.ebi.eva.vcfdump.server.configuration.MultiMongoDbFactory;
import uk.ac.ebi.eva.vcfdump.server.model.HtsGetError;
import uk.ac.ebi.eva.vcfdump.server.model.HtsGetResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static uk.ac.ebi.eva.vcfdump.server.configuration.SwaggerParameterDescriptions.END_POSITION_DESCRIPTION;
import static uk.ac.ebi.eva.vcfdump.server.configuration.SwaggerParameterDescriptions.FORMAT_DESCRIPTION;
import static uk.ac.ebi.eva.vcfdump.server.configuration.SwaggerParameterDescriptions.REFERENCE_SEQUENCE_NAME_DESCRIPTION;
import static uk.ac.ebi.eva.vcfdump.server.configuration.SwaggerParameterDescriptions.REGION_DESCRIPTION;
import static uk.ac.ebi.eva.vcfdump.server.configuration.SwaggerParameterDescriptions.SPECIES_DESCRIPTION;
import static uk.ac.ebi.eva.vcfdump.server.configuration.SwaggerParameterDescriptions.START_POSITION_DESCRIPTION;
import static uk.ac.ebi.eva.vcfdump.server.configuration.SwaggerParameterDescriptions.STUDY_DESCRIPTION;

@RestController
@RequestMapping(value = "/v1/variants/")
@Api(tags = {"htsget"})
public class HtsgetVcfController {

    private static final String VCF = "VCF";

    private Properties evaProperties;

    private VariantSourceService variantSourceService;

    private VariantWithSamplesAndAnnotationsService variantService;

    public HtsgetVcfController(VariantSourceService variantSourceService,
                               VariantWithSamplesAndAnnotationsService variantService) throws IOException {
        this.variantSourceService = variantSourceService;
        this.variantService = variantService;
        evaProperties = new Properties();
        evaProperties.load(VcfDumperController.class.getResourceAsStream("/eva.properties"));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, consumes = "application/*",
            produces = "application/vnd.ga4gh.htsget.v0.2rc+json; charset=UTF-8")
    public ResponseEntity getHtsgetUrls(
            @ApiParam(value = STUDY_DESCRIPTION, required = true)
            @PathVariable("id") String id,
            @ApiParam(value = FORMAT_DESCRIPTION, defaultValue = VCF)
            @RequestParam(name = "format", required = false) String format,
            @ApiParam(value = REFERENCE_SEQUENCE_NAME_DESCRIPTION)
            @RequestParam(name = "referenceName", required = false) String referenceName,
            @ApiParam(value = SPECIES_DESCRIPTION, required = true)
            @RequestParam(name = "species", required = false) String species,
            @ApiParam(value = START_POSITION_DESCRIPTION)
            @RequestParam(name = "start", required = false) Long start,
            @ApiParam(value = END_POSITION_DESCRIPTION)
            @RequestParam(name = "end", required = false) Long end,
            HttpServletRequest request) throws URISyntaxException {

        if (end != null) {
            //end is exclusive
            end--;
        }
        Optional<ResponseEntity> validationsResponse = validateParameters(format, referenceName, start, end);
        if (validationsResponse.isPresent()) {
            return validationsResponse.get();
        }

        String dbName = DBAdaptorConnector.getDBName(species);
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(dbName);
        int blockSize = Integer.parseInt(evaProperties.getProperty("eva.htsget.blocksize"));
        VariantExporterController controller = new VariantExporterController(dbName, variantSourceService,
                                                                             variantService,
                                                                             Arrays.asList(id.split(",")),
                                                                             evaProperties, new QueryParams(),
                                                                             blockSize);

        if (start == null) {
            start = controller.getCoordinateOfFirstVariant(referenceName);
        }
        if (end == null) {
            end = controller.getCoordinateOfLastVariant(referenceName);
        }
        Optional<ResponseEntity> errorResponse = validateRequest(referenceName, start, end, controller);
        if (errorResponse.isPresent()) {
            return errorResponse.get();
        }

        List<Region> regionList = controller.divideChromosomeInChunks(referenceName, start, end);
        HtsGetResponse htsGetResponse = new HtsGetResponse(VCF, request.getServerName() + ":" + request.getServerPort(),
                                                           request.getContextPath(), id, referenceName, species,
                                                           regionList);
        return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("htsget", htsGetResponse));
    }

    private Optional<ResponseEntity> validateParameters(String format, String referenceName, Long start, Long end) {
        if (!VCF.equals(format)) {
            return Optional.of(getResponseEntity("UnsupportedFormat",
                                                 "The requested file format is not supported by the server",
                                                 HttpStatus.BAD_REQUEST));
        }
        if (start != null && end != null && end < start) {
            return Optional.of(getResponseEntity("InvalidRange", "The requested range cannot be satisfied",
                                                 HttpStatus.BAD_REQUEST));
        }
        if (start != null && referenceName == null) {
            return Optional.of(getResponseEntity("InvalidInput", "Reference name is not specified when start is " +
                    "specified", HttpStatus.BAD_REQUEST));
        }
        if (referenceName == null) {
            return Optional.of(getResponseEntity("Unsupported", "'referenceName' is required", HttpStatus.BAD_REQUEST));
        }
        return Optional.empty();
    }

    private ResponseEntity getResponseEntity(String error, String message, HttpStatus httpStatus) {
        HtsGetError htsGetError = new HtsGetError(error, message);
        return ResponseEntity.status(httpStatus).body(Collections.singletonMap("htsget", htsGetError));
    }

    private Optional<ResponseEntity> validateRequest(String referenceName, Long start, Long end,
                                                     VariantExporterController controller) {
        if (end < start) {
            // Applies to valid requests such as chromosome 1, start: 1.000.000, end: empty.
            // If variants exist only in region 200.000 to 800.000, getCoordinateOfLastVariant() will return 800.000.
            // Given that 800.000 < 1.000.000, no region can be found.
            return Optional.of(getResponseEntity("NotFound", "The resource requested was not found",
                                                 HttpStatus.NOT_FOUND));
        }
        if (!controller.validateSpecies()) {
            return Optional.of(getResponseEntity("InvalidInput", "The requested species is not available",
                                                 HttpStatus.BAD_REQUEST));
        }
        if (!controller.validateStudies()) {
            return Optional.of(getResponseEntity("InvalidInput", "The requested study(ies) is not available",
                                                 HttpStatus.BAD_REQUEST));
        }
        return Optional.empty();
    }

    @RequestMapping(value = "/headers", method = RequestMethod.GET, produces = "application/octet-stream")
    public StreamingResponseBody getHtsgetHeaders(
            @ApiParam(value = SPECIES_DESCRIPTION, required = true)
            @RequestParam(name = "species") String species,
            @ApiParam(value = STUDY_DESCRIPTION, required = true)
            @RequestParam(name = "studies") List<String> studies,
            HttpServletResponse response) {

        String dbName = DBAdaptorConnector.getDBName(species);
        StreamingResponseBody responseBody = getStreamingHeaderResponse(dbName, studies, evaProperties,
                                                                        new QueryParams(), response);
        return responseBody;
    }

    @RequestMapping(value = "/block", method = RequestMethod.GET, produces = "application/octet-stream")
    public StreamingResponseBody getHtsgetBlocks(
            @ApiParam(value = SPECIES_DESCRIPTION, required = true)
            @RequestParam(name = "species") String species,
            @ApiParam(value = STUDY_DESCRIPTION, required = true)
            @RequestParam(name = "studies") List<String> studies,
            @ApiParam(value = REGION_DESCRIPTION, required = true)
            @RequestParam(name = "region") String chrRegion,
            HttpServletResponse response) {

        String dbName = DBAdaptorConnector.getDBName(species);
        QueryParams queryParameters = new QueryParams();
        queryParameters.setRegion(chrRegion);
        StreamingResponseBody responseBody = getStreamingBlockResponse(dbName, studies, evaProperties, queryParameters,
                                                                       response);
        return responseBody;
    }

    private StreamingResponseBody getStreamingHeaderResponse(String dbName, List<String> studies,
                                                             Properties evaProperties, QueryParams queryParameters,
                                                             HttpServletResponse response) {
        return outputStream -> {
            VariantExporterController controller;
            try {
                MultiMongoDbFactory.setDatabaseNameForCurrentThread(dbName);
                controller = new VariantExporterController(dbName, variantSourceService, variantService, studies,
                                                           outputStream, evaProperties, queryParameters);
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
