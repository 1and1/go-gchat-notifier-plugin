package com.ionos.go.plugin.notifier.template;

import javax.el.ValueExpression;
import javax.el.VariableMapper;
import java.util.HashMap;
import java.util.Map;

class MyVariableMapper extends VariableMapper {
    /** The variable mappings. */
    private final Map<String, ValueExpression> variables = new HashMap<>();

    @Override
    public ValueExpression resolveVariable(final String name) {
        final ValueExpression result = variables.get(name);
        return result;
    }

    @Override
    public ValueExpression setVariable(final String name, final ValueExpression value) {
        return variables.put(name, value);
    }
}
