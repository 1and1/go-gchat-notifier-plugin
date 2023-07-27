package com.ionos.go.plugin.notifier;

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class GoNotifierPluginTest extends GoNotifierPluginBase {

    @Test
    public void testPluginIdentifier() {
        assertEquals("notification", getGoNotifierPlugin().pluginIdentifier().getExtension());
        assertTrue(getGoNotifierPlugin().pluginIdentifier().getSupportedExtensionVersions().contains("4.0"));
    }

    @Test
    public void testHandleNotificationsInterestedIn() {
        // request get conf
        GoPluginApiResponse response = getGoNotifierPlugin().handle(GoCdObjects.request(Constants.PLUGIN_NOTIFICATIONS_INTERESTED_IN, null));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        assertEquals("{\"notifications\":[\"stage-status\"]}", response.responseBody());
    }

    @Test
    public void testHandleGetView() {
        GoPluginApiResponse response = getGoNotifierPlugin().handle(GoCdObjects.request(Constants.PLUGIN_GET_VIEW, null));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        assertFalse("needs to be non empty", response.responseBody().isEmpty());
        assertFalse("needs to contain input html element", response.responseBody().contains("<input"));
    }

    @Test
    public void testHandleAgentStatus() {
        GoPluginApiResponse response = getGoNotifierPlugin().handle(GoCdObjects.request(Constants.PLUGIN_AGENT_STATUS, null));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
    }

    @Test
    public void testHandleGetConfiguration() {
        GoPluginApiResponse response = getGoNotifierPlugin().handle(
                GoCdObjects.request(Constants.PLUGIN_GET_CONFIGURATION, null));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        Map<String, Object> map = getGson().fromJson(response.responseBody(), Map.class);
        assertTrue("contains a config key", map.containsKey(Constants.PARAM_CONDITION));
        assertTrue("contains a config key", map.containsKey(Constants.PARAM_TEMPLATE));
        assertTrue("contains a config key", map.containsKey(Constants.PARAM_PROXY_URL));
        assertTrue("contains a config key", map.containsKey(Constants.PARAM_WEBHOOK_URL));
    }
}
