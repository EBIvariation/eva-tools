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

import uk.ac.ebi.eva.commons.core.models.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HtsGetResponse {

    private String format;

    private List<Map<String, String>> urls;

    HtsGetResponse(String format, String host, String contextPath, String id, String chromosome,
                   String species,
                   List<Region> regions) {
        this.format = format;
        this.urls = constructUrls(host, contextPath, id, chromosome, species, regions);
    }

    public List<Map<String, String>> getUrls() {
        return urls;
    }

    private List<Map<String, String>> constructUrls(String host, String contextPath, String id, String chromosome,
                                                    String species, List<Region> regions) {

        List<Map<String, String>> resUrls = new ArrayList<>();

        String headerUrl = host + contextPath + "/v1/variants/headers?species=" + species + "&studies=" + id;
        Map<String, String> urlMap = new HashMap<>();
        urlMap.put("url", headerUrl);
        resUrls.add(urlMap);

        String baseUrl = host + contextPath + "/v1/variants/block?studies=" + id + "&species=" + species + "&region=" + chromosome + ":";

        for (Region region : regions) {
            String url = baseUrl + region.getStart() + "-" + region.getEnd();
            Map<String, String> blockUrlMap = new HashMap<>();
            blockUrlMap.put("url", url);
            resUrls.add(blockUrlMap);
        }
        return resUrls;
    }
}
