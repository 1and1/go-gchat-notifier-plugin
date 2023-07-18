package com.ionos.go.plugin.notifier;

import com.ionos.go.plugin.notifier.gchat.GoogleChatWebhookSender;
import com.ionos.go.plugin.notifier.message.incoming.StageStatusRequest;
import com.ionos.go.plugin.notifier.message.outgoing.StageAndAgentStatusChangedResponse;
import com.ionos.go.plugin.notifier.template.TemplateHandler;
import com.thoughtworks.go.plugin.api.logging.Logger;
import lombok.NonNull;

import java.io.IOException;

public class StageStatusHandler {

    private static final Logger LOGGER = Logger.getLoggerFor(StageStatusHandler.class);

    private final String condition;
    private final String template;
    private final String webhookUrl;
    private final String proxUrl;

    public StageStatusHandler(@NonNull String condition, @NonNull String template, @NonNull String webhookUrl, String proxy) {
        this.condition = condition;
        this.template = template;
        this.webhookUrl = webhookUrl;
        this.proxUrl = proxy;
    }

    public StageAndAgentStatusChangedResponse handle(@NonNull StageStatusRequest stageStatusRequest) {
        TemplateHandler<Boolean> conditionHandler = new TemplateHandler(condition, stageStatusRequest, Boolean.class);
        Boolean conditionValue = conditionHandler.eval();
        LOGGER.debug("Instance condition: " + conditionValue);

        if (conditionValue.equals(Boolean.FALSE)) {
            LOGGER.info("Condition '" + condition + "' is false, not notifying");
            return new StageAndAgentStatusChangedResponse(StageAndAgentStatusChangedResponse.Status.success);
        }

        TemplateHandler<String> templateHandler = new TemplateHandler(template, stageStatusRequest, String.class);
        String instanceTemplate = templateHandler.eval();
        LOGGER.debug("Instance template: " + instanceTemplate);

        if (instanceTemplate == null || instanceTemplate.isEmpty()) {
            instanceTemplate = "ERROR: Template instance was null or empty";
        }

        GoogleChatWebhookSender googleChatWebhookSender = new GoogleChatWebhookSender(proxUrl);
        try {
            googleChatWebhookSender.send(webhookUrl, instanceTemplate);
            return new StageAndAgentStatusChangedResponse(StageAndAgentStatusChangedResponse.Status.success);
        } catch (IOException e) {
            return new StageAndAgentStatusChangedResponse(StageAndAgentStatusChangedResponse.Status.failure, e.getMessage());
        }
    }
}
