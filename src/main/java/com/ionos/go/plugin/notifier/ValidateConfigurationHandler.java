package com.ionos.go.plugin.notifier;

import com.ionos.go.plugin.notifier.message.GoPluginApiRequestHandler;
import com.ionos.go.plugin.notifier.message.incoming.StageStatusRequest;
import com.ionos.go.plugin.notifier.message.incoming.ValidateConfigurationRequest;
import com.ionos.go.plugin.notifier.message.outgoing.ValidateConfigurationResponse;
import com.ionos.go.plugin.notifier.template.TemplateHandler;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import lombok.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.ionos.go.plugin.notifier.util.JsonUtil.fromJsonString;
import static com.ionos.go.plugin.notifier.util.JsonUtil.toJsonString;
import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.success;

class ValidateConfigurationHandler implements GoPluginApiRequestHandler {
    private static final Logger LOGGER = Logger.getLoggerFor(GoNotifierPlugin.class);

    private final Map<String, String> serverInfo;

    ValidateConfigurationHandler(@NonNull Map<String, String> serverInfo) {
        this.serverInfo = serverInfo;
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
    public DefaultGoPluginApiResponse handle(@NonNull GoPluginApiRequest request) {
        LOGGER.info("Request: " + request.requestBody());
        ValidateConfigurationRequest validateRequest = fromJsonString(request.requestBody(), ValidateConfigurationRequest.class);
        List<ValidateConfigurationResponse> response = new ArrayList<>();

        validateWebhookUrl(validateRequest, response);
        validateCondition(validateRequest, response);
        validateTemplate(validateRequest, response);
        validateProxyUrl(validateRequest, response);

        return success(toJsonString(response));
    }

    private static void validateProxyUrl(ValidateConfigurationRequest validateRequest, List<ValidateConfigurationResponse> response) {
        String proxyUrl = validateRequest.getPluginSettings().getOrDefault(Constants.PARAM_PROXY_URL, Collections.emptyMap()).get(Constants.FIELD_VALUE);
        if (proxyUrl != null && !proxyUrl.isEmpty()) {
            try {
                new URL(proxyUrl);
            } catch (MalformedURLException e) {
                response.add(new ValidateConfigurationResponse(Constants.PARAM_PROXY_URL, "Malformed url: " + e.getMessage()));
            }
        }
    }

    private void validateTemplate(ValidateConfigurationRequest validateRequest, List<ValidateConfigurationResponse> response) {
        String template = validateRequest.getPluginSettings().getOrDefault(Constants.PARAM_TEMPLATE, Collections.emptyMap()).get(Constants.FIELD_VALUE);
        if (template.isEmpty()) {
            response.add(new ValidateConfigurationResponse(Constants.PARAM_TEMPLATE, Constants.PARAM_TEMPLATE + " is empty"));
        } else {
            try {
                TemplateHandler handler = new TemplateHandler("template", template);
                handler.eval(newSampleStageStatusRequest(), serverInfo);
            }
            catch (Exception e) {
                LOGGER.warn("Exception in " + Constants.PARAM_TEMPLATE, e);
                response.add(new ValidateConfigurationResponse(Constants.PARAM_TEMPLATE, "Malformed template: " + e.getMessage()));
            }
        }
    }

    private void validateCondition(ValidateConfigurationRequest validateRequest, List<ValidateConfigurationResponse> response) {
        String condition = validateRequest.getPluginSettings().getOrDefault(Constants.PARAM_CONDITION, Collections.emptyMap()).get(Constants.FIELD_VALUE);
        if (condition.isEmpty()) {
            response.add(new ValidateConfigurationResponse(Constants.PARAM_CONDITION, Constants.PARAM_CONDITION + " is empty"));
        } else {
            try {
                TemplateHandler handler = new TemplateHandler(Constants.PARAM_CONDITION, condition);
                String shouldBeBool = handler.eval(newSampleStageStatusRequest(), serverInfo);
                if (!(shouldBeBool.equals("true") || shouldBeBool.equals("false"))) {
                    response.add(new ValidateConfigurationResponse(Constants.PARAM_CONDITION, "Condition should eval to true or false, but evals to: " + shouldBeBool));
                }
            }
            catch (Exception e) {
                LOGGER.warn("Exception in " + Constants.PARAM_CONDITION, e);
                response.add(new ValidateConfigurationResponse(Constants.PARAM_CONDITION, "Malformed condition: " + e.getMessage()));
            }
        }
    }

    private static void validateWebhookUrl(ValidateConfigurationRequest validateRequest, List<ValidateConfigurationResponse> response) {
        try {
            String webhookUrl = validateRequest.getPluginSettings().getOrDefault(Constants.PARAM_WEBHOOK_URL, Collections.emptyMap()).get(Constants.FIELD_VALUE);
            new URL(webhookUrl);
        } catch (MalformedURLException e) {
            response.add(new ValidateConfigurationResponse(Constants.PARAM_WEBHOOK_URL, "Malformed url: " + e.getMessage()));
        }
    }

}
