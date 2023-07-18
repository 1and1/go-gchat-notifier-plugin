package com.ionos.go.plugin.notifier.template;

import com.ionos.go.plugin.notifier.message.incoming.StageStatusRequest;
import lombok.Getter;

import javax.el.*;

class MyContext extends ELContext {

    @Getter
    private final MyVariableMapper variableMapper;
    @Getter
    private final MyFunctionMapper functionMapper;
    @Getter
    private final CompositeELResolver resolver;

    MyContext(StageStatusRequest stageStatusRequest)  {
        variableMapper = new MyVariableMapper();
        functionMapper = new MyFunctionMapper();
        resolver = new CompositeELResolver();

        resolver.add(new ArrayELResolver());
        resolver.add(new BeanELResolver());
        resolver.add(new CompositeELResolver());
        resolver.add(new ListELResolver());
        resolver.add(new MapELResolver());
        resolver.add(new ResourceBundleELResolver());
    }

    @Override
    public ELResolver getELResolver() {
        return resolver;
    }

    @Override
    public FunctionMapper getFunctionMapper() {
        return functionMapper;
    }

    @Override
    public VariableMapper getVariableMapper() {
        return variableMapper;
    }
}
