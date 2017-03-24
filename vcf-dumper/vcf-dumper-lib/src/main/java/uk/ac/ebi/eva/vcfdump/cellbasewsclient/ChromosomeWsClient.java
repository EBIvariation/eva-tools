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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URISyntaxException;
import java.util.Set;

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
        String uri = UriComponentsBuilder.fromHttpUrl(url + "/" + restVersion + "/segments")
                                         .queryParam("species", species)
                                         .toUriString();

        RestTemplate restTemplate = new RestTemplate();
        ChromosomesWSOutput chromosomesWSOutput = restTemplate.getForObject(uri, ChromosomesWSOutput.class);
        return chromosomesWSOutput.getAllChromosomeNames();
    }

    public String getVersion() {
        return restVersion;
    }
}
