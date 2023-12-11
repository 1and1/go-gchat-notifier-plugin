package com.ionos.go.plugin.notifier;

import com.ionos.go.plugin.notifier.message.incoming.ValidateConfigurationRequest;
import com.ionos.go.plugin.notifier.message.outgoing.ValidateConfigurationResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GoNotifierPluginValidateConfigurationTest extends GoNotifierPluginBase {

    private ValidateConfigurationRequest request;

    @BeforeEach
    public void setupTest() {
        request = new ValidateConfigurationRequest();
    }

    @Test
    void testHandleValidateConfigurationWithBadRequest() {
        GoPluginApiResponse response = getGoNotifierPlugin().handle(
                GoCdObjects.request(Constants.PLUGIN_VALIDATE_CONFIGURATION, getGson().toJson(request)));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
    }

    @Test
    void testHandleValidateConfigurationWithGoodRequestMultipleErrors() {
        request.setPluginSettings(new HashMap<>());
        GoPluginApiResponse response = getGoNotifierPlugin().handle(
                GoCdObjects.request(Constants.PLUGIN_VALIDATE_CONFIGURATION, getGson().toJson(request)));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        ValidateConfigurationResponse[] validateConfigurationResponses = getGson().fromJson(response.responseBody(), ValidateConfigurationResponse[].class);
        // minus proxy url
        assertEquals(getGoNotifierPlugin().getConfigurationProperties().size() - 1, validateConfigurationResponses.length);
    }

    @Test
    void testHandleValidateConfigurationWithGoodRequestNoErrors() {
        Map<String, Map<String, String>> pluginSettings = newGoodPluginSettingsTemplate();
        request.setPluginSettings(pluginSettings);
        GoPluginApiResponse response = getGoNotifierPlugin().handle(
                GoCdObjects.request(Constants.PLUGIN_VALIDATE_CONFIGURATION, getGson().toJson(request)));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        ValidateConfigurationResponse[] validateConfigurationResponses = getGson().fromJson(response.responseBody(), ValidateConfigurationResponse[].class);
        assertEquals(0, validateConfigurationResponses.length);
    }

    @Test
    void testHandleValidateConfigurationWithMalformedCondition() {
        Map<String, Map<String, String>> pluginSettings = newGoodPluginSettingsTemplate();
        pluginSettings.put(Constants.PARAM_CONDITION, Collections.singletonMap(Constants.FIELD_VALUE, "${ error"));
        request.setPluginSettings(pluginSettings);
        GoPluginApiResponse response = getGoNotifierPlugin().handle(
                GoCdObjects.request(Constants.PLUGIN_VALIDATE_CONFIGURATION, getGson().toJson(request)));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        ValidateConfigurationResponse[] validateConfigurationResponses = getGson().fromJson(response.responseBody(), ValidateConfigurationResponse[].class);
        assertEquals(1, validateConfigurationResponses.length);
        assertEquals(Constants.PARAM_CONDITION, validateConfigurationResponses[0].getKey());
    }

    @Test
    void testHandleValidateConfigurationWithMalformedTemplate() {
        Map<String, Map<String, String>> pluginSettings = newGoodPluginSettingsTemplate();
        pluginSettings.put(Constants.PARAM_TEMPLATE, Collections.singletonMap(Constants.FIELD_VALUE, "${ error"));
        request.setPluginSettings(pluginSettings);
        GoPluginApiResponse response = getGoNotifierPlugin().handle(
                GoCdObjects.request(Constants.PLUGIN_VALIDATE_CONFIGURATION, getGson().toJson(request)));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        ValidateConfigurationResponse[] validateConfigurationResponses = getGson().fromJson(response.responseBody(), ValidateConfigurationResponse[].class);
        assertEquals(1, validateConfigurationResponses.length);
        assertEquals(Constants.PARAM_TEMPLATE, validateConfigurationResponses[0].getKey());
    }

    @Test
    void testHandleValidateConfigurationWithTemplateAccessingUndefinedProperty() {
        Map<String, Map<String, String>> pluginSettings = newGoodPluginSettingsTemplate();
        pluginSettings.put(Constants.PARAM_TEMPLATE, Collections.singletonMap(Constants.FIELD_VALUE, "${ doesntexist.foobar }"));
        request.setPluginSettings(pluginSettings);
        GoPluginApiResponse response = getGoNotifierPlugin().handle(
                GoCdObjects.request(Constants.PLUGIN_VALIDATE_CONFIGURATION, getGson().toJson(request)));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        ValidateConfigurationResponse[] validateConfigurationResponses = getGson().fromJson(response.responseBody(), ValidateConfigurationResponse[].class);
        assertEquals(1, validateConfigurationResponses.length);
        assertEquals(Constants.PARAM_TEMPLATE, validateConfigurationResponses[0].getKey());
    }

    @Test
    void testHandleValidateConfigurationWithConditionAccessingUndefinedProperty() {
        Map<String, Map<String, String>> pluginSettings = newGoodPluginSettingsTemplate();
        pluginSettings.put(Constants.PARAM_CONDITION, Collections.singletonMap(Constants.FIELD_VALUE, "${ doesntexist.foobar }"));
        request.setPluginSettings(pluginSettings);
        GoPluginApiResponse response = getGoNotifierPlugin().handle(
                GoCdObjects.request(Constants.PLUGIN_VALIDATE_CONFIGURATION, getGson().toJson(request)));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        ValidateConfigurationResponse[] validateConfigurationResponses = getGson().fromJson(response.responseBody(), ValidateConfigurationResponse[].class);
        assertEquals(1, validateConfigurationResponses.length);
        assertEquals(Constants.PARAM_CONDITION, validateConfigurationResponses[0].getKey());
    }

    @Test
    void testHandleValidateConfigurationWithConditionNotTrueOrFalse() {
        Map<String, Map<String, String>> pluginSettings = newGoodPluginSettingsTemplate();
        pluginSettings.put(Constants.PARAM_CONDITION, Collections.singletonMap(Constants.FIELD_VALUE, "tralse"));
        request.setPluginSettings(pluginSettings);
        GoPluginApiResponse response = getGoNotifierPlugin().handle(
                GoCdObjects.request(Constants.PLUGIN_VALIDATE_CONFIGURATION, getGson().toJson(request)));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        ValidateConfigurationResponse[] validateConfigurationResponses = getGson().fromJson(response.responseBody(), ValidateConfigurationResponse[].class);
        assertEquals(1, validateConfigurationResponses.length);
        assertEquals(Constants.PARAM_CONDITION, validateConfigurationResponses[0].getKey());
        assertTrue(validateConfigurationResponses[0].getMessage().contains("true or false"), "Should contain 'true or false'");
    }

    @Test
    void testHandleValidateConfigurationWithMalformedProxyUrl() {
        Map<String, Map<String, String>> pluginSettings = newGoodPluginSettingsTemplate();
        pluginSettings.put(Constants.PARAM_PROXY_URL, Collections.singletonMap(Constants.FIELD_VALUE, "hppt://foo.bar"));
        request.setPluginSettings(pluginSettings);
        GoPluginApiResponse response = getGoNotifierPlugin().handle(
                GoCdObjects.request(Constants.PLUGIN_VALIDATE_CONFIGURATION, getGson().toJson(request)));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        ValidateConfigurationResponse[] validateConfigurationResponses = getGson().fromJson(response.responseBody(), ValidateConfigurationResponse[].class);
        assertEquals(1, validateConfigurationResponses.length);
        assertEquals(Constants.PARAM_PROXY_URL, validateConfigurationResponses[0].getKey());
        assertEquals("Malformed url: unknown protocol: hppt", validateConfigurationResponses[0].getMessage());
    }

    @Test
    void testHandleValidateConfigurationWithMalformedWebhookUrl() {
        Map<String, Map<String, String>> pluginSettings = newGoodPluginSettingsTemplate();
        pluginSettings.put(Constants.PARAM_WEBHOOK_URL, Collections.singletonMap(Constants.FIELD_VALUE, "hppt://foo.bar"));
        request.setPluginSettings(pluginSettings);
        GoPluginApiResponse response = getGoNotifierPlugin().handle(
                GoCdObjects.request(Constants.PLUGIN_VALIDATE_CONFIGURATION, getGson().toJson(request)));
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        ValidateConfigurationResponse[] validateConfigurationResponses = getGson().fromJson(response.responseBody(), ValidateConfigurationResponse[].class);
        assertEquals(1, validateConfigurationResponses.length);
        assertEquals(Constants.PARAM_WEBHOOK_URL, validateConfigurationResponses[0].getKey());
        assertEquals("Malformed url: unknown protocol: hppt", validateConfigurationResponses[0].getMessage());
    }
}
