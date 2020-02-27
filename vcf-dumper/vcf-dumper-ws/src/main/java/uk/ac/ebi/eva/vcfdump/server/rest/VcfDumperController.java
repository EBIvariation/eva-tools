/*
 *
 *  * Copyright 2016 EMBL - European Bioinformatics Institute
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package uk.ac.ebi.eva.vcfdump.server.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import uk.ac.ebi.eva.commons.mongodb.services.VariantSourceService;
import uk.ac.ebi.eva.commons.mongodb.services.VariantWithSamplesAndAnnotationsService;
import uk.ac.ebi.eva.vcfdump.QueryParams;
import uk.ac.ebi.eva.vcfdump.VariantExporterController;
import uk.ac.ebi.eva.vcfdump.server.configuration.DBAdaptorConnector;
import uk.ac.ebi.eva.vcfdump.server.configuration.MultiMongoDbFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static uk.ac.ebi.eva.vcfdump.server.configuration.SwaggerParameterDescriptions.ALTERNATE_ALLELE_DESCRIPTION;
import static uk.ac.ebi.eva.vcfdump.server.configuration.SwaggerParameterDescriptions.ANNOTATION_CONSEQUENCE_TYPE_DESCRIPTION;
import static uk.ac.ebi.eva.vcfdump.server.configuration.SwaggerParameterDescriptions.MINOR_ALLELE_FREQUENCY_DESCRIPTION;
import static uk.ac.ebi.eva.vcfdump.server.configuration.SwaggerParameterDescriptions.POLYPHEN_DESCRIPTION;
import static uk.ac.ebi.eva.vcfdump.server.configuration.SwaggerParameterDescriptions.REFERENCE_ALLELE_DESCRIPTION;
import static uk.ac.ebi.eva.vcfdump.server.configuration.SwaggerParameterDescriptions.REGION_DESCRIPTION;
import static uk.ac.ebi.eva.vcfdump.server.configuration.SwaggerParameterDescriptions.SIFT_DESCRIPTION;
import static uk.ac.ebi.eva.vcfdump.server.configuration.SwaggerParameterDescriptions.SPECIES_DESCRIPTION;
import static uk.ac.ebi.eva.vcfdump.server.configuration.SwaggerParameterDescriptions.STUDY_LIST_DESCRIPTION;

@RestController
@RequestMapping(value = "/v1/segments")
@Api(tags = {"segments"})
public class VcfDumperController {

    private Properties evaProperties;

    private VariantSourceService variantSourceService;

    private VariantWithSamplesAndAnnotationsService variantService;

    public VcfDumperController(VariantSourceService variantSourceService,
                               VariantWithSamplesAndAnnotationsService variantService) throws IOException {
        this.variantSourceService = variantSourceService;
        this.variantService = variantService;
        evaProperties = new Properties();
        evaProperties.load(VcfDumperController.class.getResourceAsStream("/eva.properties"));
    }

    @RequestMapping(value = "/{regionId}/variants", method = RequestMethod.GET)
    public StreamingResponseBody getVariantsByRegionStreamingOutput(
            @ApiParam(value = REGION_DESCRIPTION, required = true)
            @PathVariable("regionId") String region,
            @ApiParam(value = SPECIES_DESCRIPTION, required = true)
            @RequestParam(name = "species") String species,
            @ApiParam(value = STUDY_LIST_DESCRIPTION, required = true)
            @RequestParam(name = "studies") List<String> studies,
            @ApiParam(value = ANNOTATION_CONSEQUENCE_TYPE_DESCRIPTION)
            @RequestParam(name = "annot-ct", required = false) List<String> consequenceType,
            @ApiParam(value = MINOR_ALLELE_FREQUENCY_DESCRIPTION)
            @RequestParam(name = "maf", required = false) String maf,
            @ApiParam(value = POLYPHEN_DESCRIPTION)
            @RequestParam(name = "polyphen", required = false) String polyphenScore,
            @ApiParam(value = SIFT_DESCRIPTION)
            @RequestParam(name = "sift", required = false) String siftScore,
            @ApiParam(value = REFERENCE_ALLELE_DESCRIPTION)
            @RequestParam(name = "ref", required = false, defaultValue = "") String reference,
            @ApiParam(value = ALTERNATE_ALLELE_DESCRIPTION)
            @RequestParam(name = "alt", required = false, defaultValue = "") String alternate,
            @RequestParam(name = "miss_alleles", required = false, defaultValue = "") String missingAlleles,
            @RequestParam(name = "miss_gts", required = false, defaultValue = "") String missingGenotypes,
            @RequestParam(name = "exclude", required = false) List<String> exclude,
            HttpServletResponse response) {

        QueryParams queryParameters = parseQueryParams(region, consequenceType, maf, polyphenScore, siftScore,
                                                       reference, alternate,missingAlleles, missingGenotypes, exclude);

        String dbName = DBAdaptorConnector.getDBName(species);
        MultiMongoDbFactory.setDatabaseNameForCurrentThread(dbName);
        StreamingResponseBody responseBody = getStreamingResponseBody(dbName, studies, evaProperties,
                                                                      queryParameters, response);

        return responseBody;
    }

    private StreamingResponseBody getStreamingResponseBody(String dbName, List<String> studies,
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
                controller.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private QueryParams parseQueryParams(String region, List<String> consequenceType, String maf, String polyphenScore,
                                         String siftScore, String reference, String alternate, String missingAlleles,
                                         String missingGenotypes, List<String> exclude) {
        QueryParams queryParameters = new QueryParams();
        queryParameters.setRegion(region);
        if (consequenceType != null && !consequenceType.isEmpty()) {
            queryParameters.setConsequenceType(consequenceType);
        }
        if (!StringUtils.isEmpty(maf)) {
            queryParameters.setMaf(maf);
        }
        if (!StringUtils.isEmpty(polyphenScore)) {
            queryParameters.setPolyphenScore(polyphenScore);
        }
        if (!StringUtils.isEmpty(siftScore)) {
            queryParameters.setSiftScore(siftScore);
        }
        if (!StringUtils.isEmpty(reference)) {
            queryParameters.setReference(reference);
        }
        if (!StringUtils.isEmpty(alternate)) {
            queryParameters.setAlternate(alternate);
        }
        if (!StringUtils.isEmpty(missingAlleles)) {
            queryParameters.setMissingAlleles(missingAlleles);
        }
        if (!StringUtils.isEmpty(missingGenotypes)) {
            queryParameters.setMissingGenotypes(missingGenotypes);
        }
        if (exclude != null && !exclude.isEmpty()) {
            queryParameters.setExclusions(exclude);
        }
        return queryParameters;
    }
}
