package com.ionos.go.plugin.notifier;

import com.ionos.go.plugin.notifier.message.outgoing.StageAndAgentStatusChangedResponse;
import com.ionos.go.plugin.notifier.util.Helper;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class GoNotifierPluginStageStatusTest extends GoNotifierPluginBase {

    private static EmbeddedHttpServer embeddedHttpServer;
    private static int embeddedHttpPort;

    @BeforeAll
    public static void setUpLocalWebServer() {
        embeddedHttpServer = new EmbeddedHttpServer().withServlet(GoogleMockServlet.class, "/gchat");
        embeddedHttpServer.start();
        embeddedHttpPort = embeddedHttpServer.getRunningPort();
    }

    @BeforeEach
    public void setupConfig() {
        getPluginSettings().put(Constants.PARAM_WEBHOOK_URL, "http://localhost:" + embeddedHttpPort + "/gchat");
        getPluginSettings().put(Constants.PARAM_CONDITION, "true");
        getPluginSettings().put(Constants.PARAM_TEMPLATE, "${stageStatus.pipeline.group}");
    }

    @BeforeEach
    public void initServlet() {
        GoogleMockServlet.reset();
        GoogleMockServlet.setStatusToReturn(HttpStatus.SC_OK);
    }

    @AfterAll
    public static void stopLocalWebServer() {
        embeddedHttpServer.stop();
    }

    @Test
    void testHandleStageStatusNoSendingConditionFalse() throws IOException {
        String stageStatusJson = Helper.readResource("/stageStatus.json");
        getPluginSettings().put(Constants.PARAM_CONDITION, "false");

        GoPluginApiResponse response = getGoNotifierPlugin().handle(
                GoCdObjects.request(Constants.PLUGIN_STAGE_STATUS, stageStatusJson));

        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());

        StageAndAgentStatusChangedResponse stageResponse = getGson().fromJson(response.responseBody(), StageAndAgentStatusChangedResponse.class);
        assertEquals(StageAndAgentStatusChangedResponse.Status.success, stageResponse.getStatus());
        assertEquals(Collections.emptyList(), stageResponse.getMessages());

        assertEquals(0, GoogleMockServlet.getInvocations());
    }

    @Test
    void testHandleStageStatusGoodWeather() throws IOException {
        String stageStatusJson = Helper.readResource("/stageStatus.json");

        GoPluginApiResponse response = getGoNotifierPlugin().handle(
                GoCdObjects.request(Constants.PLUGIN_STAGE_STATUS, stageStatusJson));

        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());

        StageAndAgentStatusChangedResponse stageResponse = getGson().fromJson(response.responseBody(), StageAndAgentStatusChangedResponse.class);
        assertEquals(StageAndAgentStatusChangedResponse.Status.success, stageResponse.getStatus());
        assertEquals(Collections.emptyList(), stageResponse.getMessages());

        assertEquals(1, GoogleMockServlet.getInvocations());
        assertEquals("application/json; charset=UTF-8", GoogleMockServlet.getCapturedRequestContentType());
        assertEquals("{\"text\":\"Github\"}", GoogleMockServlet.getCapturedRequestBody());
    }

    @Test
    void testHandleStageStatusWithRemoteBadRequest() throws IOException {
        GoogleMockServlet.setStatusToReturn(HttpStatus.SC_BAD_REQUEST);
        String stageStatusJson = Helper.readResource("/stageStatus.json");

        GoPluginApiResponse response = getGoNotifierPlugin().handle(GoCdObjects.request(Constants.PLUGIN_STAGE_STATUS, stageStatusJson));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());

        StageAndAgentStatusChangedResponse stageResponse = getGson().fromJson(response.responseBody(), StageAndAgentStatusChangedResponse.class);
        assertEquals(StageAndAgentStatusChangedResponse.Status.failure, stageResponse.getStatus());
        assertEquals(Collections.singletonList("GChat sending problem: Google chat url returned http status 400 Bad Request"), stageResponse.getMessages());

        assertEquals(1, GoogleMockServlet.getInvocations());
        assertEquals("application/json; charset=UTF-8", GoogleMockServlet.getCapturedRequestContentType());
        assertEquals("{\"text\":\"Github\"}", GoogleMockServlet.getCapturedRequestBody());
    }
}
