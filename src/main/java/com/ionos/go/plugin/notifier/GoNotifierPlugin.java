package com.ionos.go.plugin.notifier;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ionos.go.plugin.notifier.message.*;
import com.ionos.go.plugin.notifier.message.incoming.AgentStatusRequest;
import com.ionos.go.plugin.notifier.message.outgoing.NotificationsInterestedInResponse;
import com.ionos.go.plugin.notifier.util.Helper;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.DefaultGoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.hc.core5.http.HttpStatus;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.ionos.go.plugin.notifier.util.JsonUtil.fromJsonString;
import static com.ionos.go.plugin.notifier.util.JsonUtil.toJsonString;
import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.error;
import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.success;

/**
 * The Go CD notifier plugin.
 * @see <a href="https://plugin-api.gocd.org/current/notifications/">GoCD Notification Plugin API</a>
 */
@Extension
public class GoNotifierPlugin implements GoPlugin {

    /** The logging instance for this class. */
    private static final Logger LOGGER = Logger.getLoggerFor(GoNotifierPlugin.class);

    /** The plugin extension type. */
    public static final String EXTENSION_TYPE = "notification";

    /** The plugin extension version. */
    public static final String EXTENSION_VERSION = "4.0";

    /** The map of message handlers. */
    private final Map<String, GoPluginApiRequestHandler> handlerMap;

    /** The go application accessor. */
    private GoApplicationAccessor goApplicationAccessor;

    @Getter(AccessLevel.MODULE)
    private final ConfigurationProperties configurationProperties;

    /** Plugin parameter template is a freemarker template. */

    private static final String DEFAULT_TEMPLATE = "${stageStatus.pipeline.group}/${stageStatus.pipeline.name}/${stageStatus.pipeline.stage.name} is ${stageStatus.pipeline.stage.state}";

    private static final String DEFAULT_CONDITION = "${stageStatus.pipeline.stage.result == 'failed'}";

    private static final String DEFAULT_URL = "https://chat.googleapis.com/v1/spaces/.../messages?key=...&token=...";

    /** Constructs this plugin and initializes the message handlers. */
    public GoNotifierPlugin() {
        LOGGER.debug("C'tor start");
        LOGGER.info("GoNotifierPlugin is here");

        configurationProperties = new ConfigurationProperties();
        configurationProperties.addConfigurationProperty(Constants.PARAM_TEMPLATE, ConfigurationProperty.builder()
                .displayName("EL message template")
                .defaultValue(DEFAULT_TEMPLATE)
                .required(true)
                .displayOrder("0")
                .build());
        configurationProperties.addConfigurationProperty(Constants.PARAM_CONDITION, ConfigurationProperty.builder()
                .displayName("EL condition template")
                .defaultValue(DEFAULT_CONDITION)
                .required(true)
                .displayOrder("1")
                .build());
        configurationProperties.addConfigurationProperty(Constants.PARAM_WEBHOOK_URL, ConfigurationProperty.builder()
                .displayName("Google Chat Webhook URL")
                .defaultValue(DEFAULT_URL)
                .required(true)
                .displayOrder("2")
                .build());
        configurationProperties.addConfigurationProperty(Constants.PARAM_PROXY_URL, ConfigurationProperty.builder()
                .displayName("Optional HTTP proxy URL, i.e. http://my.proxy:3128/")
                .required(false)
                .displayOrder("3")
                .build());

        handlerMap = new HashMap<>();
        handlerMap.put(Constants.PLUGIN_NOTIFICATIONS_INTERESTED_IN, this::handleNotificationsInterestedIn);
        handlerMap.put(Constants.PLUGIN_STAGE_STATUS, this::handleStageStatus);
        handlerMap.put(Constants.PLUGIN_AGENT_STATUS, this::handleAgentStatus);
        handlerMap.put(Constants.PLUGIN_GET_CONFIGURATION, this::handleGetConfiguration);
        handlerMap.put(Constants.PLUGIN_VALIDATE_CONFIGURATION, this::handleValidateConfiguration);
        handlerMap.put(Constants.PLUGIN_GET_VIEW, this::handleGetView);
        handlerMap.put(Constants.PLUGIN_SETTINGS_CHANGED, this::handlePluginSettingsChanged);

        LOGGER.debug("C'tor end");
    }

    private GoPluginApiResponse handleValidateConfiguration(GoPluginApiRequest request) {
        GoPluginApiRequestHandler goPluginApiRequestHandler = new ValidateConfigurationHandler(getServerInfo());
        return goPluginApiRequestHandler.handle(request);
    }

    private GoPluginApiResponse handleNotificationsInterestedIn(GoPluginApiRequest request) {
        return success(toJsonString(new NotificationsInterestedInResponse(new String[] {"stage-status"})));
    }

    private GoPluginApiResponse handleStageStatus(GoPluginApiRequest request) {
        StageStatusHandler stageStatusHandler = new StageStatusHandler(getServerInfo(), getSettings());
        return stageStatusHandler.handle(request);
    }

    private GoPluginApiResponse handlePluginSettingsChanged(GoPluginApiRequest request) {
        Map<?, ?> settings = fromJsonString(request.requestBody(), Map.class);
        LOGGER.debug("Plugin settings changed: " + settings);
        return success("");
    }


    private static final String GET_VIEW_TEMPLATE = "/get-view.html";

    private GoPluginApiResponse handleGetConfiguration(GoPluginApiRequest request) {
        return success(toJsonString(configurationProperties.getPropertyMap()));
    }

    private GoPluginApiResponse handleAgentStatus(GoPluginApiRequest request) {
        AgentStatusRequest agentStatus = fromJsonString(request.requestBody(), AgentStatusRequest.class);
        return new DefaultGoPluginApiResponse(HttpStatus.SC_NOT_IMPLEMENTED, "Not Implemented");
    }

    private GoPluginApiResponse handleGetView(GoPluginApiRequest request) {
        try {
            String template = Helper.readResource(GET_VIEW_TEMPLATE);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("template", template);
            DefaultGoPluginApiResponse defaultGoPluginApiResponse = new DefaultGoPluginApiResponse(200);
            defaultGoPluginApiResponse.setResponseBody(toJsonString(jsonObject));
            return defaultGoPluginApiResponse;
        } catch (IOException e) {
            LOGGER.error("Resource not found: " + GET_VIEW_TEMPLATE, e);
            return error(e.getMessage());
        }
    }

    private Map<String, String> getSettings() {
        Gson gson = new Gson();
        // create a request
        DefaultGoApiRequest request = new DefaultGoApiRequest(
                Constants.SERVER_PLUGIN_SETTINGS_GET,
                "1.0",
                pluginIdentifier()
        );

        // set the request body
        Map<String, String> map = new HashMap<>();
        map.put("plugin-id", "com.ionos.gchat.notifier");
        request.setRequestBody(gson.toJson(map));

        GoApiResponse response = this.goApplicationAccessor.submit(request);

        // check status
        if (response.responseCode() != 200) {
            LOGGER.error("The server sent an unexpected status code " + response.responseCode() + " with the response body " + response.responseBody());
        }

        // parse the response, using a json parser of your choice
        return gson.fromJson(response.responseBody(), Map.class);
    }

    private Map<String, String> getServerInfo() {
        Gson gson = new Gson();
        // create a request
        DefaultGoApiRequest request = new DefaultGoApiRequest(
                Constants.SERVER_SERVER_INFO_GET,
                "1.0",
                pluginIdentifier()
        );

        // set the request body
        Map<String, String> map = new HashMap<>();
        map.put("plugin-id", "com.ionos.gchat.notifier");
        request.setRequestBody(gson.toJson(map));

        GoApiResponse response = this.goApplicationAccessor.submit(request);

        // check status
        if (response.responseCode() != 200) {
            LOGGER.error("The server sent an unexpected status code " + response.responseCode() + " with the response body " + response.responseBody());
        }

        // parse the response, using a json parser of your choice
        return gson.fromJson(response.responseBody(), Map.class);
    }

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        LOGGER.debug("initializeGoApplicationAccessor()");
        this.goApplicationAccessor = goApplicationAccessor;
    }

    @Override
    public GoPluginApiResponse handle(final GoPluginApiRequest goPluginApiRequest) {
        String requestName = goPluginApiRequest.requestName();
        LOGGER.debug("Got request with name '" + requestName + "' and body '" + goPluginApiRequest.requestBody() + "'");
        try {
            if (handlerMap.containsKey(requestName)) {
                LOGGER.debug("Have a handler for that request");
                GoPluginApiResponse response = handlerMap.get(requestName).handle(goPluginApiRequest);
                LOGGER.debug("Request with name '" + requestName + "' resulted in response code " + response.responseCode() +
                        " and body " + response.responseBody());
                return response;
            }
            LOGGER.warn("Invalid request '" + requestName + "' and body '" + goPluginApiRequest.requestBody() + "'");
            return DefaultGoPluginApiResponse.badRequest(String.format("Invalid request name %s", requestName));
        } catch (final Exception e) {
            LOGGER.error("could not handle request with name '" + requestName + "' and body '" + goPluginApiRequest.requestBody() + "'", e);
            return DefaultGoPluginApiResponse.error(e.getMessage());
        }
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION_TYPE, Collections.singletonList(EXTENSION_VERSION));
    }
}
