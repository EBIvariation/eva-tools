/*
 * Copyright 2015 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.vcfdump.cellbasewsclient;

import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.Set;

/**
 * This class encapsulates CellBaseClient adding some operations to its API
 */
public class CellbaseWSClient {
    private final String species;

    private final String cellbaseRestURL;

    private final String cellbaseRestVersion;

    private static final Logger logger = LoggerFactory.getLogger(CellbaseWSClient.class);

    public CellbaseWSClient(String species, String cellbaseRestURL, String cellbaseRestVersion)
            throws URISyntaxException {
        this.species = species.split("_")[0];
        this.cellbaseRestURL = cellbaseRestURL;
        this.cellbaseRestVersion = cellbaseRestVersion;
    }

    public Set<String> getChromosomes() {
        try {
            // call cellbase chromosomes WS
            RestTemplate restTemplate = new RestTemplate();
            ParameterizedTypeReference<QueryResponse<QueryResult<CellbaseChromosomesWSOutput>>> responseType =
                    new ParameterizedTypeReference<QueryResponse<QueryResult<CellbaseChromosomesWSOutput>>>() {
                    };

            String cellbaseGetChromosomesUrl =
                    cellbaseRestURL + "/" + cellbaseRestVersion + "/" + species + "/genomic/chromosome/all";
            logger.debug("Getting chromosomes list from {} ...", cellbaseGetChromosomesUrl);
            ResponseEntity<QueryResponse<QueryResult<CellbaseChromosomesWSOutput>>> wsOutput =
                    restTemplate.exchange(cellbaseGetChromosomesUrl, HttpMethod.GET, null, responseType);

            // parse WS output and return all chromosome names
            QueryResponse<QueryResult<CellbaseChromosomesWSOutput>> response = wsOutput.getBody();
            QueryResult<CellbaseChromosomesWSOutput> result = response.getResponse().get(0);
            CellbaseChromosomesWSOutput results = result.getResult().get(0);
            return results.getAllChromosomeNames();
        } catch (Exception e) {
            logger.debug("Error retrieving list of chromosomes: {}", e.getMessage());
            throw new RuntimeException("Error retrieving list of chromosomes", e);
        }
    }

    public String getVersion() {
        return cellbaseRestVersion;
    }
}
