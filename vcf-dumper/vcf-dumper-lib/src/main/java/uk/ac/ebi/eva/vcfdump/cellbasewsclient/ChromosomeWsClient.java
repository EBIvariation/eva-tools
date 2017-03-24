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
public class ChromosomeWsClient {
    private final String species;

    private final String url;

    private final String restVersion;

    private static final Logger logger = LoggerFactory.getLogger(ChromosomeWsClient.class);

    public ChromosomeWsClient(String species, String url, String restVersion)
            throws URISyntaxException {
        this.species = species.split("_")[0];
        this.url = url;
        this.restVersion = restVersion;
    }

    public Set<String> getChromosomes() {
        try {
            // call cellbase chromosomes WS
            RestTemplate restTemplate = new RestTemplate();
            ParameterizedTypeReference<QueryResponse<QueryResult<ChromosomesWSOutput>>> responseType =
                    new ParameterizedTypeReference<QueryResponse<QueryResult<ChromosomesWSOutput>>>() {
                    };

            String cellbaseGetChromosomesUrl =
                    url + "/" + restVersion + "/" + species + "/genomic/chromosome/all";
            logger.debug("Getting chromosomes list from {} ...", cellbaseGetChromosomesUrl);
            ResponseEntity<QueryResponse<QueryResult<ChromosomesWSOutput>>> wsOutput =
                    restTemplate.exchange(cellbaseGetChromosomesUrl, HttpMethod.GET, null, responseType);

            // parse WS output and return all chromosome names
            QueryResponse<QueryResult<ChromosomesWSOutput>> response = wsOutput.getBody();
            QueryResult<ChromosomesWSOutput> result = response.getResponse().get(0);
            ChromosomesWSOutput results = result.getResult().get(0);
            return results.getAllChromosomeNames();
        } catch (Exception e) {
            logger.debug("Error retrieving list of chromosomes: {}", e.getMessage());
            throw new RuntimeException("Error retrieving list of chromosomes", e);
        }
    }

    public String getVersion() {
        return restVersion;
    }
}
