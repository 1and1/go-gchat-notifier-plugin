package com.ionos.go.plugin.notifier;

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class GoNotifierPluginTest extends GoNotifierPluginBase {

    @Test
    void testPluginIdentifier() {
        assertEquals("notification", getGoNotifierPlugin().pluginIdentifier().getExtension());
        assertTrue(getGoNotifierPlugin().pluginIdentifier().getSupportedExtensionVersions().contains("4.0"));
    }

    @Test
    void testHandleNotificationsInterestedIn() {
        // request get conf
        GoPluginApiResponse response = getGoNotifierPlugin().handle(GoCdObjects.request(Constants.PLUGIN_NOTIFICATIONS_INTERESTED_IN, null));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        assertEquals("{\"notifications\":[\"stage-status\"]}", response.responseBody());
    }

    @Test
    void testHandleGetView() {
        GoPluginApiResponse response = getGoNotifierPlugin().handle(GoCdObjects.request(Constants.PLUGIN_GET_VIEW, null));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        assertFalse(response.responseBody().isEmpty(), "needs to be non empty");
        assertFalse(response.responseBody().contains("<input"), "needs to contain input html element");
    }

    @Test
    void testHandleGetConfiguration() {
        GoPluginApiResponse response = getGoNotifierPlugin().handle(
                GoCdObjects.request(Constants.PLUGIN_GET_CONFIGURATION, null));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        Map<String, Object> map = getGson().fromJson(response.responseBody(), Map.class);
        assertTrue(map.containsKey(Constants.PARAM_CONDITION), "contains a config key");
        assertTrue(map.containsKey(Constants.PARAM_TEMPLATE), "contains a config key");
        assertTrue(map.containsKey(Constants.PARAM_PROXY_URL), "contains a config key");
        assertTrue(map.containsKey(Constants.PARAM_WEBHOOK_URL), "contains a config key");
    }
}
