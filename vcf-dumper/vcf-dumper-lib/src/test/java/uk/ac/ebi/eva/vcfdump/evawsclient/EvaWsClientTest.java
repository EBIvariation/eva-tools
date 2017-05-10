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
package uk.ac.ebi.eva.vcfdump.evawsclient;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;

import uk.ac.ebi.eva.vcfdump.MockServerClientHelper;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class EvaWsClientTest {

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);

    private MockServerClient mockServerClient;

    @Before
    public void setUp() {
        MockServerClientHelper.hSapiensGrch37(mockServerClient, "hsapiens_grch37");
    }

    @Test
    public void getChromosomes() throws Exception {
        EvaWsClient evaWsClient = new EvaWsClient("eva_hsapiens_grch37".replace("eva_", ""),
                                                  String.format("http://localhost:%s/eva/webservices/rest/", mockServerRule.getPort()),
                                                  "v1");

        assertEquals(
                new HashSet<>(Arrays.asList((new String[] {"1", "10", "11", "12", "13", "14", "15", "16", "17", "18",
                        "19", "2", "20", "21", "22", "3", "4", "5", "6", "7", "8", "9", "MT", "X", "Y"}))),
                evaWsClient.getChromosomes()
        );
    }

}