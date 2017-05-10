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
package uk.ac.ebi.eva.vcfdump;

import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.mockserver.model.Parameter;
import org.springframework.http.HttpStatus;

public class MockServerClientHelper {

    public static void hSapiensGrch37(MockServerClient mockServerClient, String dbName) {
        String hsapiensGrch37ResponseBody = "{\"apiVersion\":\"v1\",\"warning\":\"\",\"error\":\"\",\"response\":[{\"id\":\"\",\"time\":0,\"dbTime\":-1,\"numResults\":25,\"numTotalResults\":25,\"warningMsg\":\"\",\"errorMsg\":\"\",\"resultType\":\"\",\"result\":[\"1\",\"10\",\"11\",\"12\",\"13\",\"14\",\"15\",\"16\",\"17\",\"18\",\"19\",\"2\",\"20\",\"21\",\"22\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"MT\",\"X\",\"Y\"]}]}";

        mockServerClient
                .when(
                        HttpRequest.request()
                                   .withMethod("GET")
                                   .withPath("/eva/webservices/rest/v1/segments")
                                   .withQueryStringParameter(
                                           new Parameter("species", dbName)
                                   )
                )
                .respond(
                        HttpResponse.response()
                                    .withHeader("Content-Type", "application/json", "charset=UTF-8")
                                    .withStatusCode(HttpStatus.OK.value())
                                    .withBody( new JsonBody(hsapiensGrch37ResponseBody))
                );
    }

    public static void oAriesOarv31(MockServerClient mockServerClient, String dbName) {
        String oariesOarv31ResponseBody = "{\"apiVersion\":\"v1\",\"warning\":\"\",\"error\":\"\",\"response\":[{\"id\":\"\",\"time\":0,\"dbTime\":-1,\"numResults\":5196,\"numTotalResults\":5196,\"warningMsg\":\"\",\"errorMsg\":\"\",\"resultType\":\"\",\"result\":[\"1\",\"10\",\"11\",\"12\",\"13\",\"14\",\"15\",\"16\",\"17\",\"18\",\"19\",\"2\",\"20\",\"21\",\"22\",\"23\",\"24\",\"25\",\"26\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"MT\",\"X\"]}]}";

        mockServerClient
                .when(
                        HttpRequest.request()
                                   .withMethod("GET")
                                   .withPath("/eva/webservices/rest/v1/segments")
                                   .withQueryStringParameter(
                                           new Parameter("species", dbName)
                                   )
                )
                .respond(
                        HttpResponse.response()
                                    .withHeader("Content-Type", "application/json", "charset=UTF-8")
                                    .withStatusCode(HttpStatus.OK.value())
                                    .withBody( new JsonBody(oariesOarv31ResponseBody))
                );
    }

}
