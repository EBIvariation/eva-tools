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

import uk.ac.ebi.eva.vcfdump.VariantExporterController;

import io.swagger.annotations.Api;
import org.opencb.opencga.lib.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.StorageManagerException;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by pagarcia on 03/08/2016.
 */
@RestController
@RequestMapping(value = "/v1/segments")
@Api(tags = { "segments" })
public class VcfDumperWSServer {

    public Properties evaProperties;

    public VcfDumperWSServer() throws IOException {
        evaProperties = new Properties();
        evaProperties.load(VcfDumperWSServer.class.getResourceAsStream("/eva.properties"));
    }


    @RequestMapping(value = "/{regionId}/variants", method = RequestMethod.GET, produces = "application/octet-stream")
    public StreamingResponseBody getVariantsByRegionStreamingOutput(@PathVariable("regionId") String region,
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
                                                                    HttpServletResponse response)
            throws IllegalAccessException, IllegalOpenCGACredentialsException, InstantiationException, IOException, StorageManagerException,
            URISyntaxException, ClassNotFoundException {
        MultivaluedMap<String, String> queryParameters =
                parseQueryParams(region, consequenceType, maf, polyphenScore, siftScore, reference, alternate, missingAlleles, missingGenotypes);

        String dbName = "eva_" + species;

        StreamingResponseBody responseBody = getStreamingResponseBody(species, dbName, studies, evaProperties, queryParameters, response);

        return responseBody;
    }

    private StreamingResponseBody getStreamingResponseBody(String species, String dbName, List<String> studies, Properties evaProperties,
                                                           MultivaluedMap<String, String> queryParameters, HttpServletResponse response) {
        return new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException, WebApplicationException {
                VariantExporterController controller;
                try {
                    controller = new VariantExporterController(species, dbName, studies, outputStream, evaProperties, queryParameters);
                    // tell the client that the file is an attachment, so it will download it instead of showing it
                    response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + controller.getOutputFileName());
                    controller.run();
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                }
            }
        };
    }


    private MultivaluedMap<String, String> parseQueryParams(String region,
                                                            List<String> consequenceType,
                                                            String maf,
                                                            String polyphenScore,
                                                            String siftScore,
                                                            String reference,
                                                            String alternate,
                                                            String missingAlleles,
                                                            String missingGenotypes)
    {
        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();

        queryParameters.put(VariantDBAdaptor.REGION, Collections.singletonList(region));

        if (consequenceType != null && !consequenceType.isEmpty()) {
            queryParameters.put(VariantDBAdaptor.ANNOT_CONSEQUENCE_TYPE, consequenceType);
        }
        if (!maf.isEmpty()) {
            queryParameters.put(VariantDBAdaptor.MAF, Collections.singletonList(maf));
        }
        if (!polyphenScore.isEmpty()) {
            queryParameters.put(VariantDBAdaptor.POLYPHEN, Collections.singletonList(polyphenScore));
        }
        if (!siftScore.isEmpty()) {
            queryParameters.put(VariantDBAdaptor.SIFT, Collections.singletonList(siftScore));
        }
        if (!reference.isEmpty()) {
            queryParameters.put(VariantDBAdaptor.REFERENCE, Collections.singletonList(reference));
        }
        if (!alternate.isEmpty()) {
            queryParameters.put(VariantDBAdaptor.ALTERNATE, Collections.singletonList(alternate));
        }
        if (!missingAlleles.isEmpty()) {
            queryParameters.put(VariantDBAdaptor.MISSING_ALLELES, Collections.singletonList(missingAlleles));
        }
        if (!missingGenotypes.isEmpty()) {
            queryParameters.put(VariantDBAdaptor.MISSING_GENOTYPES, Collections.singletonList(missingGenotypes));
        }

        return queryParameters;
    }

}
