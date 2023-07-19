package com.ionos.go.plugin.notifier;

public class Constants {
    static final String PARAM_TEMPLATE = "template";

    /** Plugin parameter condition is a freemarker template evaluating towards {@code true} or {@code false}. */
    static final String PARAM_CONDITION = "condition";

    /** Plugin parameter webhook_url is gchat webhook url. */
    static final String PARAM_WEBHOOK_URL = "webhook_url";

    /** Plugin parameter proxy_url is an optional HTTP proxy url to use. */
    static final String PARAM_PROXY_URL = "proxy_url";

    /** A JSON field that is used. */
    static final String FIELD_VALUE = "value";
}
