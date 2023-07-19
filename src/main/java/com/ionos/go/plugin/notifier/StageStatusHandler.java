package com.ionos.go.plugin.notifier;

import com.ionos.go.plugin.notifier.gchat.GoogleChatWebhookSender;
import com.ionos.go.plugin.notifier.message.incoming.StageStatusRequest;
import com.ionos.go.plugin.notifier.message.outgoing.StageAndAgentStatusChangedResponse;
import com.ionos.go.plugin.notifier.template.TemplateHandler;
import com.thoughtworks.go.plugin.api.logging.Logger;
import freemarker.template.TemplateException;
import lombok.NonNull;

import java.io.IOException;
import java.util.Map;

public class StageStatusHandler {

    private static final Logger LOGGER = Logger.getLoggerFor(StageStatusHandler.class);

    private final String condition;
    private final String template;
    private final String webhookUrl;
    private final String proxyUrl;

    public StageStatusHandler(@NonNull String condition, @NonNull String template, @NonNull String webhookUrl, String proxyUrl) {
        this.condition = condition;
        this.template = template;
        this.webhookUrl = webhookUrl;
        this.proxyUrl = proxyUrl;
    }

    public StageAndAgentStatusChangedResponse handle(@NonNull StageStatusRequest stageStatusRequest, @NonNull Map<String, String> serverInfo) {

        String instanceTemplate;

        try {
            TemplateHandler conditionHandler = new TemplateHandler("condition", condition);
            String conditionValue = conditionHandler.eval(stageStatusRequest, serverInfo);
            LOGGER.debug("Instance condition: " + conditionValue);

            if (!Boolean.parseBoolean(conditionValue)) {
                LOGGER.info("Condition '" + condition + "' is false, not notifying");
                return new StageAndAgentStatusChangedResponse(StageAndAgentStatusChangedResponse.Status.success);
            }
        }
        catch (TemplateException | IOException e) {
            LOGGER.warn("Exception for condition " + condition, e);
        }

        try {
            TemplateHandler templateHandler = new TemplateHandler("template", template);
            instanceTemplate = templateHandler.eval(stageStatusRequest, serverInfo);
            LOGGER.debug("Instance template: " + instanceTemplate);
        }
        catch (TemplateException | IOException e) {
            LOGGER.warn("Exception for template " + condition, e);
            instanceTemplate = "ERROR: Template instance had an error: " + e.getMessage();
        }

        GoogleChatWebhookSender googleChatWebhookSender = new GoogleChatWebhookSender(proxyUrl);
        try {
            googleChatWebhookSender.send(webhookUrl, instanceTemplate);
            return new StageAndAgentStatusChangedResponse(StageAndAgentStatusChangedResponse.Status.success);
        } catch (IOException e) {
            return new StageAndAgentStatusChangedResponse(StageAndAgentStatusChangedResponse.Status.failure, e.getMessage());
        }
    }
}
