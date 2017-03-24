package uk.ac.ebi.eva.vcfdump.cellbasewsclient;

import org.junit.Before;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.mockserver.model.Parameter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

public class ChromosomeWsClientTest {

    private MockServerClient mockServer;

    @Before
    public void setUp() {
        mockServer = startClientAndServer(1080);
        mockServer
                .when(
                        HttpRequest.request()
                                   .withMethod("GET")
                                   .withPath("/eva/webservices/rest/v1/segments")
                                   .withQueryStringParameter(
                                           new Parameter("species", "hsapiens_grch37")
                                   )
                )
                .respond(
                        HttpResponse.response()
                                    .withHeader("Content-Type", "application/json", "charset=UTF-8")
                                    .withStatusCode(HttpStatus.OK.value())
                                    .withBody( new JsonBody("{\"apiVersion\":\"v1\",\"warning\":\"\",\"error\":\"\",\"response\":[{\"id\":\"\",\"time\":0,\"dbTime\":-1,\"numResults\":25,\"numTotalResults\":25,\"warningMsg\":\"\",\"errorMsg\":\"\",\"resultType\":\"\",\"result\":[\"1\",\"10\",\"11\",\"12\",\"13\",\"14\",\"15\",\"16\",\"17\",\"18\",\"19\",\"2\",\"20\",\"21\",\"22\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"MT\",\"X\",\"Y\"]}]}"))
                );
    }

    @Test
    public void getChromosomes() throws Exception {
        ChromosomeWsClient chromosomeWsClient = new ChromosomeWsClient("eva_hsapiens_grch37",
                                                                       "http://localhost:1080/eva/webservices/rest/",
                                                                       "v1");

        assertEquals(
                new HashSet<>(Arrays.asList((new String[] {"1", "10", "11", "12", "13", "14", "15", "16", "17", "18",
                        "19", "2", "20", "21", "22", "3", "4", "5", "6", "7", "8", "9", "MT", "X", "Y"}))),
                chromosomeWsClient.getChromosomes()
        );
    }

}