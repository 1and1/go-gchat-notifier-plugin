package com.ionos.go.plugin.notifier.template;

import com.ionos.go.plugin.notifier.message.incoming.StageStatusRequest;
import com.thoughtworks.go.plugin.api.logging.Logger;
import lombok.Getter;
import lombok.NonNull;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

public class TemplateHandler<T> {

    private static final Logger LOGGER = Logger.getLoggerFor(TemplateHandler.class);

    private final ExpressionFactory factory;

    private final String template;

    @Getter
    private final MyContext context;

    private final Class<T> clazz;

    public TemplateHandler(@NonNull String template, @NonNull StageStatusRequest stageStatusRequest, @NonNull  Class<T> clazz) {
        this.template = template;
        this.factory = ExpressionFactory.newInstance();
        this.context = new MyContext(stageStatusRequest);
        this.clazz = clazz;
        context.getVariableMapper().setVariable("stageStatus", new SimpleValueExpression<>(stageStatusRequest));
    }

    public T eval() {
        final ValueExpression exp = factory.createValueExpression(context, template, clazz);
        final T value = clazz.cast( exp.getValue(context) );
        LOGGER.debug("Value is: " + value);
        return value;
    }
}
