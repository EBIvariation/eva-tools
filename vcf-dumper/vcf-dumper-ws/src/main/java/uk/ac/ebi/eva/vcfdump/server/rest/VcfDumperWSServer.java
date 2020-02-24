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
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping(value = "/v1/segments")
@Api(tags = {"segments"})
public class VcfDumperWSServer {

    private Properties evaProperties;

    private VariantSourceService variantSourceService;

    private VariantWithSamplesAndAnnotationsService variantService;

    public VcfDumperWSServer(VariantSourceService variantSourceService,
                             VariantWithSamplesAndAnnotationsService variantService) throws IOException {
        this.variantSourceService = variantSourceService;
        this.variantService = variantService;
        evaProperties = new Properties();
        evaProperties.load(VcfDumperWSServer.class.getResourceAsStream("/eva.properties"));
    }

    @RequestMapping(value = "/{regionId}/variants", method = RequestMethod.GET)
    public StreamingResponseBody getVariantsByRegionStreamingOutput(
            @ApiParam(value = "Comma separated genomic regions in the format chr:start-end.", required = true)
            @PathVariable("regionId") String region,
            @ApiParam(value = "First letter of the genus, followed by the full species name, e.g. ecaballus_20. " +
                    "Allowed values can be looked up in https://www.ebi.ac.uk/eva/webservices/rest/v1/meta/species/list/" +
                    " concatenating the fields 'taxonomyCode' and 'assemblyCode' (separated by underscore).",
                    required = true)
            @RequestParam(name = "species") String species,
            @ApiParam(value = "Study identifiers, e.g. PRJEB9799. Each individual identifier of studies can be looked " +
                    "up in https://www.ebi.ac.uk/eva/webservices/rest/v1/meta/studies/all in the field named 'id'.",
                    required = true)
            @RequestParam(name = "studies") List<String> studies,
            @ApiParam(value = "Retrieve only variants with exactly this consequence type (as stated by Ensembl VEP)")
            @RequestParam(name = "annot-ct", required = false) List<String> consequenceType,
            @ApiParam(value = "Retrieve only variants whose Minor Allele Frequency is less than (<), less" +
                    " than or equals (<=), greater than (>), greater than or equals (>=) or equals (=) the" +
                    " provided number. e.g. <0.1")
            @RequestParam(name = "maf", required = false) String maf,
            @ApiParam(value = "Retrieve only variants whose PolyPhen score as stated by Ensembl VEP is less than" +
                    " (<), less than or equals (<=), greater than (>), greater than or equals (>=) or equals (=) " +
                    "the provided number. e.g. <0.1")
            @RequestParam(name = "polyphen", required = false) String polyphenScore,
            @ApiParam(value = "Retrieve only variants whose SIFT score as stated by Ensembl VEP is less than (<)," +
                    " less than or equals (<=), greater than (>), greater than or equals (>=) or equals (=) the " +
                    "provided number. e.g. <0.1")
            @RequestParam(name = "sift", required = false) String siftScore,
            @ApiParam(value = "Reference allele, e.g. A")
            @RequestParam(name = "ref", required = false, defaultValue = "") String reference,
            @ApiParam(value = "Alternate allele, e.g. T")
            @RequestParam(name = "alt", required = false, defaultValue = "") String alternate,
            @RequestParam(name = "miss_alleles", required = false, defaultValue = "") String missingAlleles,
            @RequestParam(name = "miss_gts", required = false, defaultValue = "") String missingGenotypes,
            @RequestParam(name = "exclude", required = false) List<String> exclude,
            HttpServletResponse response) {

        QueryParams queryParameters =
                parseQueryParams(region, consequenceType, maf, polyphenScore, siftScore, reference, alternate,
                                 missingAlleles,
                                 missingGenotypes, exclude);

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
