package com.ionos.go.plugin.notifier;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ionos.go.plugin.notifier.message.*;
import com.ionos.go.plugin.notifier.message.incoming.AgentStatusRequest;
import com.ionos.go.plugin.notifier.message.incoming.StageStatusRequest;
import com.ionos.go.plugin.notifier.message.incoming.ValidateConfigurationRequest;
import com.ionos.go.plugin.notifier.message.outgoing.NotificationsInterestedInResponse;
import com.ionos.go.plugin.notifier.message.outgoing.StageAndAgentStatusChangedResponse;
import com.ionos.go.plugin.notifier.message.outgoing.ValidateConfigurationResponse;
import com.ionos.go.plugin.notifier.template.TemplateHandler;
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    private final ConfigurationProperties configurationProperties;

    private final static String PARAM_TEMPLATE = "template";
    private final static String PARAM_CONDITION = "condition";
    private final static String PARAM_WEBHOOK_URL = "webhook_url";
    private final static String PARAM_PROXY_URL = "proxy_url";

    private final static String DEFAULT_TEMPLATE = "${stageStatus.pipeline.group}/${stageStatus.pipeline.name}/${stageStatus.pipeline.stage.name} is ${stageStatus.pipeline.stage.state}";

    private final static String DEFAULT_CONDITION = "${stageStatus.pipeline.stage.result == 'failed'}";

    private final static String DEFAULT_URL = "https://chat.googleapis.com/v1/spaces/.../messages?key=...&token=...";

    /** Constructs this plugin and initializes the message handlers. */
    public GoNotifierPlugin() {
        LOGGER.debug("C'tor start");
        LOGGER.info("GoNotifierPlugin is here");

        configurationProperties = new ConfigurationProperties();
        configurationProperties.addConfigurationProperty(PARAM_TEMPLATE, ConfigurationProperty.builder()
                .displayName("EL message template")
                .defaultValue(DEFAULT_TEMPLATE)
                .required(true)
                .displayOrder("0")
                .build());
        configurationProperties.addConfigurationProperty(PARAM_CONDITION, ConfigurationProperty.builder()
                .displayName("EL condition template")
                .defaultValue(DEFAULT_CONDITION)
                .required(true)
                .displayOrder("1")
                .build());
        configurationProperties.addConfigurationProperty(PARAM_WEBHOOK_URL, ConfigurationProperty.builder()
                .displayName("Google Chat Webhook URL")
                .defaultValue(DEFAULT_URL)
                .required(true)
                .displayOrder("2")
                .build());
        configurationProperties.addConfigurationProperty(PARAM_PROXY_URL, ConfigurationProperty.builder()
                .displayName("Optional HTTP proxy URL, i.e. http://my.proxy:3128/")
                .required(false)
                .displayOrder("3")
                .build());

        handlerMap = new HashMap<>();
        handlerMap.put("notifications-interested-in", this::handleNotificationsInterestedIn);
        handlerMap.put("stage-status", this::handleStageStatus);
        handlerMap.put("agent-status", this::handleAgentStatus);
        handlerMap.put("go.plugin-settings.get-configuration", this::handleGetConfigururation);
        handlerMap.put("go.plugin-settings.validate-configuration", this::handleValidateConfiguration);
        handlerMap.put("go.plugin-settings.get-view", this::handleGetView);
        handlerMap.put("go.plugin-settings.plugin-settings-changed", this::handlePluginSettingsChanged);

        LOGGER.debug("C'tor end");
    }

    private GoPluginApiResponse handleNotificationsInterestedIn(GoPluginApiRequest request) {
        return success(toJsonString(new NotificationsInterestedInResponse(new String[] {"stage-status"})));
    }

    private GoPluginApiResponse handleStageStatus(GoPluginApiRequest request) {
        StageStatusRequest stageStatus = fromJsonString(request.requestBody(), StageStatusRequest.class);
        Map<String, String> settings = getSettings();
        String condition = settings.get(PARAM_CONDITION);
        String template = settings.get(PARAM_TEMPLATE);
        String webhookUrl = settings.get(PARAM_WEBHOOK_URL);
        String proxy = settings.get(PARAM_PROXY_URL);

        StageStatusHandler stageStatusHandler = new StageStatusHandler(condition, template, webhookUrl, proxy);
        StageAndAgentStatusChangedResponse response = stageStatusHandler.handle(stageStatus);
        return success(toJsonString(response));
    }

    private GoPluginApiResponse handlePluginSettingsChanged(GoPluginApiRequest request) {
        Map<?, ?> settings = fromJsonString(request.requestBody(), Map.class);
        LOGGER.debug("Plugin settings changed: " + settings);
        return success("");
    }


    private static final String GET_VIEW_TEMPLATE = "/get-view.html";

    private GoPluginApiResponse handleGetConfigururation(GoPluginApiRequest request) {
        return success(toJsonString(configurationProperties.getPropertyMap()));
    }

    private GoPluginApiResponse handleAgentStatus(GoPluginApiRequest request) {
        AgentStatusRequest agentStatus = fromJsonString(request.requestBody(), AgentStatusRequest.class);
        return success(toJsonString(new StageAndAgentStatusChangedResponse(StageAndAgentStatusChangedResponse.Status.failure, "i did oops")));
    }

    private GoPluginApiResponse handleGetView(GoPluginApiRequest request) {
        try {
            String template = readResource(GET_VIEW_TEMPLATE);
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

    private String readResource(String resource) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(resource)), StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[256];
            int length;
            while ((length = reader.read(buffer)) >= 0) {
                sb.append(buffer, 0, length);
            }
            LOGGER.debug("Read resource with length " + sb.length());
            return sb.toString();
        }
    }

    private Map<String, String> getSettings() {
        Gson gson = new Gson();
        // create a request
        DefaultGoApiRequest request = new DefaultGoApiRequest(
                "go.processor.plugin-settings.get",
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

    private DefaultGoPluginApiResponse handleValidateConfiguration(GoPluginApiRequest request) {
        LOGGER.info("Request: " + request.requestBody());
        ValidateConfigurationRequest validateRequest = fromJsonString(request.requestBody(), ValidateConfigurationRequest.class);
        List<ValidateConfigurationResponse> response = new ArrayList<>();

        try {
            String webhookUrl = validateRequest.getPluginSettings().getOrDefault(PARAM_WEBHOOK_URL, Collections.emptyMap()).get("value");
            new URL(webhookUrl);
        } catch (MalformedURLException e) {
            response.add(new ValidateConfigurationResponse(PARAM_WEBHOOK_URL, "Malformed url: " + e.getMessage()));
        }

        String condition = validateRequest.getPluginSettings().getOrDefault(PARAM_CONDITION, Collections.emptyMap()).get("value");
        if (condition.isEmpty()) {
            response.add(new ValidateConfigurationResponse(PARAM_CONDITION, PARAM_CONDITION + " is empty"));
        } else {
            try {
                TemplateHandler handler = new TemplateHandler("condition", condition, newSampleStageStatusRequest());
                String shouldBeBool = handler.eval();
                if (!(shouldBeBool.equals("true") || shouldBeBool.equals("false"))) {
                    response.add(new ValidateConfigurationResponse(PARAM_CONDITION, "Condition should eval to true or false, but evals to: " + shouldBeBool));
                }
            }
            catch (Exception e) {
                LOGGER.warn("Exception in " + PARAM_CONDITION, e);
                response.add(new ValidateConfigurationResponse(PARAM_CONDITION, "Malformed condition: " + e.getMessage()));
            }
        }

        String template = validateRequest.getPluginSettings().getOrDefault(PARAM_TEMPLATE, Collections.emptyMap()).get("value");
        if (template.isEmpty()) {
            response.add(new ValidateConfigurationResponse(PARAM_TEMPLATE, PARAM_TEMPLATE + " is empty"));
        } else {
            try {
                TemplateHandler handler = new TemplateHandler("template", template, newSampleStageStatusRequest());
                handler.eval();
            }
            catch (Exception e) {
                LOGGER.warn("Exception in " + PARAM_TEMPLATE, e);
                response.add(new ValidateConfigurationResponse(PARAM_TEMPLATE, "Malformed template: " + e.getMessage()));
            }
        }

        String proxyUrl = validateRequest.getPluginSettings().getOrDefault(PARAM_PROXY_URL, Collections.emptyMap()).get("value");
        if (proxyUrl != null && !proxyUrl.isEmpty()) {
            try {
                new URL(proxyUrl);
            } catch (MalformedURLException e) {
                response.add(new ValidateConfigurationResponse(PARAM_PROXY_URL, "Malformed url: " + e.getMessage()));
            }
        }

        return success(toJsonString(response));
    }

    /** Creates a sample object for testing the condition and the template with.
     * */
    private StageStatusRequest newSampleStageStatusRequest() {
        StageStatusRequest response = new StageStatusRequest();
        StageStatusRequest.Pipeline pipeline = new StageStatusRequest.Pipeline();
        StageStatusRequest.Stage stage = new StageStatusRequest.Stage();
        StageStatusRequest.Job job = new StageStatusRequest.Job();

        job.setName("jobname");
        job.setAssignTime(ZonedDateTime.now());
        job.setCompleteTime(ZonedDateTime.now());
        job.setScheduleTime(ZonedDateTime.now());
        job.setState("failed");
        job.setResult("cancelled");

        stage.setName("stagename");
        stage.setApprovedBy("John Doe");
        stage.setCounter("1");
        stage.setPreviousStageCounter(0);
        stage.setApprovalType("foo");
        stage.setState("failed");
        stage.setResult("cancelled");
        stage.setCreateTime(ZonedDateTime.now());
        stage.setJobs(new StageStatusRequest.Job[] {job});

        pipeline.setStage(stage);
        pipeline.setName("pipelinename");
        pipeline.setCounter("0");
        pipeline.setGroup("pipelinegroup");
        pipeline.setBuildCause(new ArrayList<>());

        response.setPipeline(pipeline);

        return response;
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
