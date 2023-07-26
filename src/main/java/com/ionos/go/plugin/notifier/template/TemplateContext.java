package com.ionos.go.plugin.notifier.template;

import com.ionos.go.plugin.notifier.message.incoming.StageStatusRequest;
import lombok.Getter;
import lombok.Value;

import java.util.Map;

/** The context mapping for the template instantiation.
 * @see TemplateHandler
 * */
@Value
@Getter
public class TemplateContext {
    private final StageStatusRequest stageStatus;
    private final Map<String, String> serverInfo;
}
