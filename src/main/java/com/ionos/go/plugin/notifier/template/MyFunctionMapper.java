package com.ionos.go.plugin.notifier.template;

import javax.el.FunctionMapper;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class MyFunctionMapper extends FunctionMapper {

    /** The variable mappings. */
    private final Map<String, Method> functions = new HashMap<>();

    /**
     * Creates a key from prefix and localname for function lookup.
     *
     * @param prefix
     *            the prefix
     * @param localname
     *            the localname
     * @return the key for function lookup
     */
    private String createKey(final String prefix, final String localname) {
        return prefix + ":" + localname;
    }

    public void putFunction(final String prefix, final String localname, final Method m) {
        functions.put(createKey(prefix, localname), m);
    }

    @Override
    public Method resolveFunction(final String prefix, final String localname) {
        final Method result = functions.get(createKey(prefix, localname));
        return result;
    }
}
