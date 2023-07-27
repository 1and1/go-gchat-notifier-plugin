package com.ionos.go.plugin.notifier;

import com.google.gson.Gson;
import com.ionos.go.plugin.notifier.message.GoPluginApiRequestHandler;
import com.ionos.go.plugin.notifier.message.incoming.StageStatusRequest;
import com.ionos.go.plugin.notifier.message.incoming.ValidateConfigurationRequest;
import com.ionos.go.plugin.notifier.message.outgoing.ValidateConfigurationResponse;
import com.ionos.go.plugin.notifier.template.TemplateContext;
import com.ionos.go.plugin.notifier.template.TemplateHandler;
import com.ionos.go.plugin.notifier.util.Helper;
import com.ionos.go.plugin.notifier.util.JsonUtil;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import lombok.NonNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ionos.go.plugin.notifier.util.JsonUtil.fromJsonString;
import static com.ionos.go.plugin.notifier.util.JsonUtil.toJsonString;
import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.success;

/** Handles the validation of plugin settings.
 * @see Constants#PLUGIN_VALIDATE_CONFIGURATION
 * */
class ValidateConfigurationHandler implements GoPluginApiRequestHandler {
    private static final Logger LOGGER = Logger.getLoggerFor(GoNotifierPlugin.class);

    private final Map<String, String> serverInfo;

    ValidateConfigurationHandler(@NonNull Map<String, String> serverInfo) {
        this.serverInfo = serverInfo;
    }

    /** Creates sample objects for testing the condition and the template with.
     * */
    private List<StageStatusRequest> newSampleStageStatusRequests() throws IOException {
        List<StageStatusRequest> response = new ArrayList<>();

        StageStatusRequest success = JsonUtil.fromJsonString(
                Helper.readResource("/sampleSuccess.json"),
                StageStatusRequest.class);
        response.add(success);

        StageStatusRequest failed = JsonUtil.fromJsonString(
                Helper.readResource("/sampleFailed.json"),
                StageStatusRequest.class);
        response.add(failed);

        return response;
    }

    /** Convert the settings to a flat map.
     * @param settingsWithValue settings in GoCD format.
     * @return a flat map of settings-key to settings-value.
     * */
    static Map<String, String> toFlatSettings(Map<String, Map<String, String>> settingsWithValue) {
        return settingsWithValue.entrySet()
                .stream()
                .filter(e -> e.getValue().containsKey(Constants.FIELD_VALUE))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().get(Constants.FIELD_VALUE)));
    }

    @Override
    public DefaultGoPluginApiResponse handle(@NonNull GoPluginApiRequest request) {
        LOGGER.info("Request: " + request.requestBody());
        ValidateConfigurationRequest validateRequest = fromJsonString(request.requestBody(), ValidateConfigurationRequest.class);

        Map<String, String> flatSettings = toFlatSettings(validateRequest.getPluginSettings());
        List<ValidateConfigurationResponse> response = new ArrayList<>();

        if (validateRequest.getPluginSettings() != null) {
            if (validateNonNull(flatSettings, response, Constants.PARAM_WEBHOOK_URL)) {
                validateWebhookUrl(flatSettings, response);
            }
            if (validateNonNull(flatSettings, response, Constants.PARAM_CONDITION)) {
                validateCondition(flatSettings, response);
            }
            if (validateNonNull(flatSettings, response, Constants.PARAM_TEMPLATE)) {
                validateTemplate(flatSettings, response);
            }
            validateProxyUrl(flatSettings, response);
        } else {
            return DefaultGoPluginApiResponse.error("Illegal request");
        }

        return success(toJsonString(response));
    }

    private static boolean validateNonNull(Map<String, String> validateRequest, List<ValidateConfigurationResponse> response, String parameterName) {
        if (validateRequest != null) {
            if (validateRequest.containsKey(parameterName)) {
                return true;
            } else {
                response.add(new ValidateConfigurationResponse(parameterName, "Request pluginSettings parameter '"+parameterName+"' value is missing"));
            }
        } else {
            response.add(new ValidateConfigurationResponse(parameterName, "Request is null"));
        }
        return false;
    }


    private static void validateProxyUrl(Map<String, String> validateRequest, List<ValidateConfigurationResponse> response) {
        if (validateRequest == null
                || !validateRequest.containsKey(Constants.PARAM_PROXY_URL)) {
            return;
        }
        String proxyUrl = validateRequest.get(Constants.PARAM_PROXY_URL);
        if (!proxyUrl.isEmpty()) {
            try {
                new URL(proxyUrl);
            } catch (MalformedURLException e) {
                response.add(new ValidateConfigurationResponse(Constants.PARAM_PROXY_URL, "Malformed url: " + e.getMessage()));
            }
        }
    }

    private void validateTemplate(Map<String, String> validateRequest, List<ValidateConfigurationResponse> response) {
        String template = validateRequest.get(Constants.PARAM_TEMPLATE);
        if (template.isEmpty()) {
            response.add(new ValidateConfigurationResponse(Constants.PARAM_TEMPLATE, Constants.PARAM_TEMPLATE + " is empty"));
        } else {
            try {
                TemplateHandler handler = new TemplateHandler("template", template);
                for (StageStatusRequest sample : newSampleStageStatusRequests()) {
                    handler.eval(new TemplateContext(sample, serverInfo));
                }
            }
            catch (Exception e) {
                LOGGER.warn("Exception in " + Constants.PARAM_TEMPLATE, e);
                response.add(new ValidateConfigurationResponse(Constants.PARAM_TEMPLATE, "Malformed template: " + e.getMessage()));
            }
        }
    }

    private void validateCondition(Map<String, String> validateRequest, List<ValidateConfigurationResponse> response) {
        String condition = validateRequest.get(Constants.PARAM_CONDITION);
        if (condition.isEmpty()) {
            response.add(new ValidateConfigurationResponse(Constants.PARAM_CONDITION, Constants.PARAM_CONDITION + " is empty"));
        } else {
            try {
                String nonTrueOrFalse = null;
                TemplateHandler handler = new TemplateHandler(Constants.PARAM_CONDITION, condition);
                for (StageStatusRequest sample : newSampleStageStatusRequests()) {
                    String shouldBeBool = handler.eval(new TemplateContext(sample, serverInfo));
                    if (!(shouldBeBool.equals("true") || shouldBeBool.equals("false"))) {
                        nonTrueOrFalse = shouldBeBool;
                    }
                }
                if (nonTrueOrFalse != null) {
                    response.add(new ValidateConfigurationResponse(Constants.PARAM_CONDITION, "Condition should eval to true or false, but evals to: " + nonTrueOrFalse));
                }
            }
            catch (Exception e) {
                LOGGER.warn("Exception in " + Constants.PARAM_CONDITION, e);
                response.add(new ValidateConfigurationResponse(Constants.PARAM_CONDITION, "Malformed condition: " + e.getMessage()));
            }
        }
    }

    private static void validateWebhookUrl(Map<String, String> validateRequest, List<ValidateConfigurationResponse> response) {
        try {
            String webhookUrl = validateRequest.get(Constants.PARAM_WEBHOOK_URL);
            new URL(webhookUrl);
        } catch (NullPointerException | MalformedURLException e) {
            response.add(new ValidateConfigurationResponse(Constants.PARAM_WEBHOOK_URL, "Malformed url: " + e.getMessage()));
        }
    }

}
