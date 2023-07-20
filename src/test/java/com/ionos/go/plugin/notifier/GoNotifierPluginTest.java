package com.ionos.go.plugin.notifier;

import com.google.gson.Gson;
import com.ionos.go.plugin.notifier.message.incoming.ValidateConfigurationRequest;
import com.ionos.go.plugin.notifier.message.outgoing.ValidateConfigurationResponse;
import com.ionos.go.plugin.notifier.util.Helper;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class GoNotifierPluginTest {
    private static final Logger LOGGER = Logger.getLoggerFor(GoNotifierPluginTest.class);

    private GoNotifierPlugin goNotifierPlugin;

    private Gson gson;

    private Map<String, String> serverInfo = new HashMap<>();
    private Map<String, String> pluginSettings = new HashMap<>();

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
    public void testPluginIdentifier() {
        assertEquals("notification", goNotifierPlugin.pluginIdentifier().getExtension());
        assertTrue(goNotifierPlugin.pluginIdentifier().getSupportedExtensionVersions().contains("4.0"));
    }

    @Test
    public void testHandleNotificationsInterestedIn() throws Exception {
        // request get conf
        GoPluginApiResponse response = goNotifierPlugin.handle(GoCdObjects.request(Constants.PLUGIN_NOTIFICATIONS_INTERESTED_IN, null));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        assertEquals("{\"notifications\":[\"stage-status\"]}", response.responseBody());
    }

    @Test
    public void testHandleGetView() {
        GoPluginApiResponse response = goNotifierPlugin.handle(GoCdObjects.request(Constants.PLUGIN_GET_VIEW, null));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        assertFalse("needs to be non empty", response.responseBody().isEmpty());
        assertFalse("needs to contain input html element", response.responseBody().contains("<input"));
    }

    @Test
    public void testHandleAgentStatus() {
        GoPluginApiResponse response = goNotifierPlugin.handle(GoCdObjects.request(Constants.PLUGIN_AGENT_STATUS, null));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
    }

    @Test
    public void testHandleGetConfiguration() {
        GoPluginApiResponse response = goNotifierPlugin.handle(GoCdObjects.request(Constants.PLUGIN_GET_CONFIGURATION, null));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        Map<String, Object> map = gson.fromJson(response.responseBody(), Map.class);
        assertTrue("contains a config key", map.containsKey(Constants.PARAM_CONDITION));
        assertTrue("contains a config key", map.containsKey(Constants.PARAM_TEMPLATE));
        assertTrue("contains a config key", map.containsKey(Constants.PARAM_PROXY_URL));
        assertTrue("contains a config key", map.containsKey(Constants.PARAM_WEBHOOK_URL));
    }

    @Test
    public void testHandleValidateConfigurationWithBadRequest() {
        ValidateConfigurationRequest request = new ValidateConfigurationRequest();
        GoPluginApiResponse response = goNotifierPlugin.handle(GoCdObjects.request(Constants.PLUGIN_VALIDATE_CONFIGURATION, gson.toJson(request)));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
    }

    @Test
    public void testHandleValidateConfigurationWithGoodRequestMultipleErrors() {
        ValidateConfigurationRequest request = new ValidateConfigurationRequest();
        request.setPluginSettings(new HashMap<>());
        GoPluginApiResponse response = goNotifierPlugin.handle(GoCdObjects.request(Constants.PLUGIN_VALIDATE_CONFIGURATION, gson.toJson(request)));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        ValidateConfigurationResponse[] validateConfigurationResponses = gson.fromJson(response.responseBody(), ValidateConfigurationResponse[].class);
        // minus proxy url
        assertEquals(goNotifierPlugin.getConfigurationProperties().getPropertyMap().size() - 1, validateConfigurationResponses.length);
    }

    @Test
    public void testHandleValidateConfigurationWithGoodRequestNoErrors() {
        ValidateConfigurationRequest request = new ValidateConfigurationRequest();
        Map<String, Map<String, String>> pluginSettings = new HashMap<>();
        pluginSettings.put(Constants.PARAM_TEMPLATE, Collections.singletonMap(Constants.FIELD_VALUE, "${stageStatus.pipeline.name}"));
        pluginSettings.put(Constants.PARAM_CONDITION, Collections.singletonMap(Constants.FIELD_VALUE,"${(stageStatus.pipeline.stage.state == 'Failed')?string('true', 'false')}"));
        pluginSettings.put(Constants.PARAM_WEBHOOK_URL, Collections.singletonMap(Constants.FIELD_VALUE,"https://localhost/"));
        pluginSettings.put(Constants.PARAM_PROXY_URL, Collections.singletonMap(Constants.FIELD_VALUE, ""));
        request.setPluginSettings(pluginSettings);
        GoPluginApiResponse response = goNotifierPlugin.handle(GoCdObjects.request(Constants.PLUGIN_VALIDATE_CONFIGURATION, gson.toJson(request)));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        ValidateConfigurationResponse[] validateConfigurationResponses = gson.fromJson(response.responseBody(), ValidateConfigurationResponse[].class);
        assertEquals(0, validateConfigurationResponses.length);
    }
}
