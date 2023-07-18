package com.ionos.go.plugin.notifier.template;

import com.ionos.go.plugin.notifier.message.incoming.StageStatusRequest;
import com.thoughtworks.go.plugin.api.logging.Logger;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.NonNull;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class TemplateHandler {

    private static final Logger LOGGER = Logger.getLoggerFor(TemplateHandler.class);

    private final String templateString;
    private final Template template;

    public TemplateHandler(
            @NonNull String templateName,
            @NonNull String template) throws IOException {
        this.templateString = template;

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());

        this.template = new Template(templateName, new StringReader(templateString),
                cfg);
    }

    public String eval(@NonNull StageStatusRequest stageStatusRequest, Map<String,String> serverInfo) throws TemplateException, IOException {

        Writer out = new StringWriter();
        Map<Object, Object> model = new HashMap<>();
        model.put("stageStatus", stageStatusRequest);
        model.put("serverInfo", serverInfo);

        template.process(model, out);

        final String value = out.toString();
        LOGGER.debug("Value is: " + value);
        return value;
    }
}
