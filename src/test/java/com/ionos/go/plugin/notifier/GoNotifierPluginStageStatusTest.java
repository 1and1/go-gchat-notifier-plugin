package com.ionos.go.plugin.notifier;

import com.google.gson.Gson;
import com.ionos.go.plugin.notifier.message.outgoing.StageAndAgentStatusChangedResponse;
import com.ionos.go.plugin.notifier.util.Helper;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class GoNotifierPluginStageStatusTest {
    private static final Logger LOGGER = Logger.getLoggerFor(GoNotifierPluginStageStatusTest.class);

    private static EmbeddedHttpServer embeddedHttpServer;
    private static int embeddedHttpPort;
    private GoNotifierPlugin goNotifierPlugin;

    private Gson gson;

    private Map<String, String> serverInfo = new HashMap<>();
    private Map<String, String> pluginSettings = new HashMap<>();

    @BeforeClass
    public static void setUpLocalWebServer() {
        embeddedHttpServer = new EmbeddedHttpServer().withServlet(GoogleMockServlet.class, "/gchat");
        embeddedHttpServer.start();
        embeddedHttpPort = embeddedHttpServer.getRunningPort();
    }

    private static String servletRequestContentType;
    private static String servletRequestBody;
    private static int servletResponseStatus;

    public static class GoogleMockServlet extends HttpServlet {
        protected void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            LOGGER.debug("Got a POST request");
            servletRequestContentType = req.getHeader("Content-Type");
            BufferedReader reader = req.getReader();
            StringWriter writer = new StringWriter();
            reader.lines().forEach(line -> writer.append(line));
            servletRequestBody = writer.toString();
            resp.setStatus(servletResponseStatus);
        }
    }

    @AfterClass
    public static void stopLocalWebServer() {
        embeddedHttpServer.stop();
    }

    @Before
    public void setupPlugin() throws IOException {
        this.goNotifierPlugin = new GoNotifierPlugin();
        this.goNotifierPlugin.initializeGoApplicationAccessor(new GoApplicationAccessor() {
            @Override
            public GoApiResponse submit(GoApiRequest goApiRequest) {
                LOGGER.debug("Server request: " + goApiRequest.api());
                if (goApiRequest.api().equals(Constants.SERVER_SERVER_INFO_GET)) {
                    return GoCdObjects.apiResponse(HttpStatus.SC_OK, Collections.emptyMap(), gson.toJson(serverInfo));
                }
                if (goApiRequest.api().equals(Constants.SERVER_PLUGIN_SETTINGS_GET)) {
                    return GoCdObjects.apiResponse(HttpStatus.SC_OK, Collections.emptyMap(), gson.toJson(pluginSettings));
                }
                LOGGER.error("Unknown server request: " + goApiRequest.api());
                return GoCdObjects.apiResponse(HttpStatus.SC_NOT_IMPLEMENTED, Collections.emptyMap(), "Not implemented, bro!");
            }
        });
        this.gson = new Gson();
        this.serverInfo = gson.fromJson(Helper.readResource("/serverInfo.json"), Map.class);
    }

    @Test
    public void testHandleStageStatusGoodWeather() throws IOException {
        servletResponseStatus = HttpStatus.SC_OK;
        String stageStatusJson = Helper.readResource("/stageStatus.json");
        pluginSettings.put(Constants.PARAM_WEBHOOK_URL, "http://localhost:" + embeddedHttpPort + "/gchat");
        pluginSettings.put(Constants.PARAM_CONDITION, "true");
        pluginSettings.put(Constants.PARAM_TEMPLATE, "${stageStatus.pipeline.group}");

        GoPluginApiResponse response = goNotifierPlugin.handle(GoCdObjects.request(Constants.PLUGIN_STAGE_STATUS, stageStatusJson));

        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());

        StageAndAgentStatusChangedResponse stageResponse = gson.fromJson(response.responseBody(), StageAndAgentStatusChangedResponse.class);
        assertEquals(StageAndAgentStatusChangedResponse.Status.success, stageResponse.getStatus());
        assertEquals(Collections.emptyList(), stageResponse.getMessages());

        assertEquals("application/json; charset=UTF-8", servletRequestContentType);
        assertEquals("{\"text\":\"Github\"}", servletRequestBody);
    }

    @Test
    public void testHandleStageStatuBadWeather() throws IOException {
        servletResponseStatus = HttpStatus.SC_BAD_REQUEST;
        String stageStatusJson = Helper.readResource("/stageStatus.json");
        pluginSettings.put(Constants.PARAM_WEBHOOK_URL, "http://localhost:" + embeddedHttpPort + "/gchat");
        pluginSettings.put(Constants.PARAM_CONDITION, "true");
        pluginSettings.put(Constants.PARAM_TEMPLATE, "${stageStatus.pipeline.group}");

        GoPluginApiResponse response = goNotifierPlugin.handle(GoCdObjects.request(Constants.PLUGIN_STAGE_STATUS, stageStatusJson));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());

        StageAndAgentStatusChangedResponse stageResponse = gson.fromJson(response.responseBody(), StageAndAgentStatusChangedResponse.class);
        assertEquals(StageAndAgentStatusChangedResponse.Status.failure, stageResponse.getStatus());
        assertEquals(Arrays.asList("GChat sending problem: Google chat url returned http status 400 Bad Request"), stageResponse.getMessages());

        assertEquals("application/json; charset=UTF-8", servletRequestContentType);
        assertEquals("{\"text\":\"Github\"}", servletRequestBody);
    }
}
