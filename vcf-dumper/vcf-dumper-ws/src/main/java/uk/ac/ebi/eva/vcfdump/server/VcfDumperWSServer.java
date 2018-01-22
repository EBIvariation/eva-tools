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

package uk.ac.ebi.eva.vcfdump.server;

import io.swagger.annotations.Api;
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
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

@RestController
@RequestMapping(value = "/v1/segments")
@Api(tags = {"segments"})
public class VcfDumperWSServer {

    public Properties evaProperties;

    @Autowired
    private VariantSourceService variantSourceService;
    @Autowired
    private VariantWithSamplesAndAnnotationsService variantService;

    public VcfDumperWSServer() throws IOException {
        evaProperties = new Properties();
        evaProperties.load(VcfDumperWSServer.class.getResourceAsStream("/eva.properties"));
    }


    @RequestMapping(value = "/{regionId}/variants", method = RequestMethod.GET, produces = "application/octet-stream")
    public StreamingResponseBody getVariantsByRegionStreamingOutput(
            @PathVariable("regionId") String region,
            @RequestParam(name = "species") String species,
            @RequestParam(name = "studies") List<String> studies,
            @RequestParam(name = "annot-ct", required = false) List<String> consequenceType,
            @RequestParam(name = "maf", required = false, defaultValue = "") String maf,
            @RequestParam(name = "polyphen", required = false, defaultValue = "") String polyphenScore,
            @RequestParam(name = "sift", required = false, defaultValue = "") String siftScore,
            @RequestParam(name = "ref", required = false, defaultValue = "") String reference,
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

        return new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream)  {
                VariantExporterController controller;
                try {
                    controller = new VariantExporterController(dbName, variantSourceService, variantService, studies, outputStream, evaProperties,
                                                               queryParameters);
                    // tell the client that the file is an attachment, so it will download it instead of showing it
                    response.addHeader(HttpHeaders.CONTENT_DISPOSITION,
                                       "attachment;filename=" + controller.getOutputFileName());
                    controller.run();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public void setVariantSourceService(VariantSourceService variantSourceService) {
        this.variantSourceService = variantSourceService;
    }

    public void setVariantService(VariantWithSamplesAndAnnotationsService variantService) {
        this.variantService = variantService;
    }

    private QueryParams parseQueryParams(String region,
                                         List<String> consequenceType,
                                         String maf,
                                         String polyphenScore,
                                         String siftScore,
                                         String reference,
                                         String alternate,
                                         String missingAlleles,
                                         String missingGenotypes,
                                         List<String> exclude) {
        QueryParams queryParameters = new QueryParams();

        queryParameters.setRegion(region);

        if (consequenceType != null && !consequenceType.isEmpty()) {
            queryParameters.setConsequenceType(consequenceType);
        }
        if (!maf.isEmpty()) {
            queryParameters.setMaf(maf);
        }
        if (!polyphenScore.isEmpty()) {
            queryParameters.setPolyphenScore(polyphenScore);
        }
        if (!siftScore.isEmpty()) {
            queryParameters.setSiftScore(siftScore);
        }
        if (!reference.isEmpty()) {
            queryParameters.setReference(reference);
        }
        if (!alternate.isEmpty()) {
            queryParameters.setAlternate(alternate);
        }
        if (!missingAlleles.isEmpty()) {
            queryParameters.setMissingAlleles(missingAlleles);
        }
        if (!missingGenotypes.isEmpty()) {
            queryParameters.setMissingGenotypes(missingGenotypes);
        }
        if (exclude != null && !exclude.isEmpty()) {
            queryParameters.setExclusions(exclude);
        }

        return queryParameters;
    }

}
