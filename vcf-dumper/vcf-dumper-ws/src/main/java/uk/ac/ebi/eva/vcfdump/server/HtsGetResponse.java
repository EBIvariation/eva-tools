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

    private static final int BLOCK_SIZE = 250;

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

    void constructUrls(String host, String id, String chromosome, String species,
                       int start, int end) {
        List<Map<String, String>> urls = new ArrayList<>();

        String headerUrl = host + "/variants/headers?species=" + species + "&studies=" + id;
        Map<String, String> urlMap = new HashMap<>();
        urlMap.put("url", headerUrl);
        urls.add(urlMap);

        String baseUrl = host + "/variants/block";
        baseUrl = baseUrl + "?studies=" + id + "&species=" + species + "&chr=" + chromosome + ":";
        this.urls = getUrlsByBlockSize(urls, baseUrl, start, end, BLOCK_SIZE);
    }

    private List<Map<String, String>> getUrlsByBlockSize(List<Map<String, String>> urls, String baseUrl,
                                                         int start,
                                                         int end, int blockSize) {
        int range = end - start;

        //int blockSize = range / blockCount; // in case block count is used to divide blocks
        int blockCount = range / blockSize;
        int remainder = range % blockSize;
        for (int i = 0; i < blockCount; i++) {
            String url = baseUrl + (start + (blockSize * i + (i > 0 ? 1 : 0))) + "-" + (start + blockSize * (i + 1));
            Map<String, String> urlMap = new HashMap<>();
            urlMap.put("url", url);
            urls.add(urlMap);
        }
        if (remainder > 0) {
            String url = baseUrl + (start + (blockSize * blockCount) + 1) + "-" + end;
            Map<String, String> urlMap = new HashMap<>();
            urlMap.put("url", url);
            urls.add(urlMap);
        }
        return urls;
    }
}
