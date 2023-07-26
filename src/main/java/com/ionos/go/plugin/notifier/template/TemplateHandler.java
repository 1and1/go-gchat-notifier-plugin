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

/** A wrapper around Freemarker to instantiate a template for
 * either text expansion or condition evaluation.
 * */
public class TemplateHandler {

    private static final Logger LOGGER = Logger.getLoggerFor(TemplateHandler.class);

    /** The template text to expand. */
    private final String templateString;

    /** The template instance object after compiling templateString. */
    private final Template template;

    /** Creates a new instance.
     * @param templateName the name of the template for re-using already compared Freemarker templates.
     *                     Needs to be the same for the same template text.
     * @param template the freemarker syntax template to expand.
     * */
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

    /** Expands the template with the given parameters.
     * */
    public String eval(@NonNull TemplateContext context) throws TemplateException, IOException {

        Writer out = new StringWriter();

        template.process(context, out);

        final String value = out.toString();
        LOGGER.debug("Value is: " + value);
        return value;
    }
}
