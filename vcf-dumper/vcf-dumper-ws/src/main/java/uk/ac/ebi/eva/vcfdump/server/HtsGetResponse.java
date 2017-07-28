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

import org.opencb.biodata.models.feature.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by amilamanoj on 07.06.17.
 */
class HtsGetResponse {
    private String format;

    private List<Map<String, String>> urls;


    public String getFormat() {
        return format;
    }

    void setFormat(String format) {
        this.format = format;
    }

    public List<Map<String, String>> getUrls() {
        return urls;
    }

    public void setUrls(List<Map<String, String>> urls) {
        this.urls = urls;
    }

    public void constructUrls(String host, String id, String chromosome, String species,
                       List<Region> regions) {
        urls = new ArrayList<>();

        String headerUrl = host + "/variants/headers?species=" + species + "&studies=" + id;
        Map<String, String> urlMap = new HashMap<>();
        urlMap.put("url", headerUrl);
        urls.add(urlMap);

        String baseUrl = host + "/variants/block?studies=" + id + "&species=" + species + "&region=" + chromosome + ":";

        for (Region region : regions) {
            String url = baseUrl + region.getStart() + "-" + region.getEnd();
            Map<String, String> blockUrlMap = new HashMap<>();
            blockUrlMap.put("url", url);
            urls.add(blockUrlMap);
        }
    }
}
