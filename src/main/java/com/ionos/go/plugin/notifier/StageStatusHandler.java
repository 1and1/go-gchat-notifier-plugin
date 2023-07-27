package com.ionos.go.plugin.notifier;

import com.ionos.go.plugin.notifier.gchat.GoogleChatWebhookSender;
import com.ionos.go.plugin.notifier.message.GoPluginApiRequestHandler;
import com.ionos.go.plugin.notifier.message.incoming.StageStatusRequest;
import com.ionos.go.plugin.notifier.message.outgoing.StageAndAgentStatusChangedResponse;
import com.ionos.go.plugin.notifier.template.TemplateContext;
import com.ionos.go.plugin.notifier.template.TemplateHandler;
import com.ionos.go.plugin.notifier.util.Helper;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.TemplateException;
import lombok.NonNull;

import java.io.IOException;
import java.util.Map;

import static com.ionos.go.plugin.notifier.util.JsonUtil.fromJsonString;
import static com.ionos.go.plugin.notifier.util.JsonUtil.toJsonString;
import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.success;

/**
 * Processes an update of a stage status. Will
 * check all preconditions, instantiate templates
 * and send a GChat message.
 * @see Constants#PLUGIN_STAGE_STATUS
 * */
public class StageStatusHandler implements GoPluginApiRequestHandler {

    private static final Logger LOGGER = Logger.getLoggerFor(StageStatusHandler.class);

    /** Server info for mapping into the template context. */
    private final Map<String, String> serverInfo;

    /** Plugin settings.. */
    private final Map<String, String> settings;

    StageStatusHandler(@NonNull Map<String, String> serverInfo, @NonNull Map<String, String> settings) {
        this.serverInfo = serverInfo;
        this.settings = settings;
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        Helper.debugDump(request.requestBody());

        StageStatusRequest stageStatus = fromJsonString(request.requestBody(), StageStatusRequest.class);
        StageAndAgentStatusChangedResponse response = new StageAndAgentStatusChangedResponse(StageAndAgentStatusChangedResponse.Status.success);;
        String condition = settings.get(Constants.PARAM_CONDITION);
        String template = settings.get(Constants.PARAM_TEMPLATE);
        String webhookUrl = settings.get(Constants.PARAM_WEBHOOK_URL);
        String proxyUrl = settings.get(Constants.PARAM_PROXY_URL);

        String instanceTemplate = null;
        Helper.debugDump(stageStatus);
        boolean conditionEval;

        try {
            TemplateHandler conditionHandler = new TemplateHandler("condition", condition);
            String conditionValue = conditionHandler.eval(new TemplateContext(stageStatus, serverInfo));
            LOGGER.debug("Instance condition: " + conditionValue);

            conditionEval = Boolean.parseBoolean(conditionValue);
            if (!Boolean.parseBoolean(conditionValue)) {
                LOGGER.info("Condition '" + condition + "' is false, not notifying");
            }
        }
        catch (TemplateException | IOException e) {
            LOGGER.warn("Exception for condition " + condition, e);
            conditionEval = false; // default to false
            response = new StageAndAgentStatusChangedResponse(StageAndAgentStatusChangedResponse.Status.failure, "Condition problem: " + e.getMessage());
        }

        if (conditionEval) {
            response = prepareAndSendGChatMessage(stageStatus, response, condition, template, webhookUrl, proxyUrl);
        }
        return success(toJsonString(response));
    }

    private StageAndAgentStatusChangedResponse prepareAndSendGChatMessage(StageStatusRequest stageStatus, StageAndAgentStatusChangedResponse response, String condition, String template, String webhookUrl, String proxyUrl) {
        String instanceTemplate;
        try {
            TemplateHandler templateHandler = new TemplateHandler("template", template);
            instanceTemplate = templateHandler.eval(new TemplateContext(stageStatus, serverInfo));
            LOGGER.debug("Instance template: " + instanceTemplate);
            GoogleChatWebhookSender googleChatWebhookSender = new GoogleChatWebhookSender(proxyUrl);
            try {
                googleChatWebhookSender.send(webhookUrl, instanceTemplate);
            } catch (IOException e) {
                response = new StageAndAgentStatusChangedResponse(StageAndAgentStatusChangedResponse.Status.failure, "GChat sending problem: " + e.getMessage());
            }
        } catch (TemplateException | IOException e) {
            LOGGER.warn("Exception for template " + condition, e);
            response = new StageAndAgentStatusChangedResponse(StageAndAgentStatusChangedResponse.Status.failure, "Template problem: " + e.getMessage());
        }
        return response;
    }
}
