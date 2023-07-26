package com.ionos.go.plugin.notifier;

/** Constants that are re-used within the plugin. */
public class Constants {
    private Constants() {
        // no instance
    }

    static final String PARAM_TEMPLATE = "template";

    /** Plugin parameter condition is a freemarker template evaluating towards {@code true} or {@code false}. */
    static final String PARAM_CONDITION = "condition";

    /** Plugin parameter webhook_url is gchat webhook url. */
    static final String PARAM_WEBHOOK_URL = "webhook_url";

    /** Plugin parameter proxy_url is an optional HTTP proxy url to use. */
    static final String PARAM_PROXY_URL = "proxy_url";

    /** A JSON field that is used. */
    static final String FIELD_VALUE = "value";

    static final String PLUGIN_SETTINGS_CHANGED = "go.plugin-settings.plugin-settings-changed";
    static final String PLUGIN_NOTIFICATIONS_INTERESTED_IN = "notifications-interested-in";
    static final String PLUGIN_STAGE_STATUS = "stage-status";
    static final String PLUGIN_AGENT_STATUS = "agent-status";
    static final String PLUGIN_GET_CONFIGURATION = "go.plugin-settings.get-configuration";
    static final String PLUGIN_VALIDATE_CONFIGURATION = "go.plugin-settings.validate-configuration";
    static final String PLUGIN_GET_VIEW = "go.plugin-settings.get-view";
    static final String SERVER_PLUGIN_SETTINGS_GET = "go.processor.plugin-settings.get";
    static final String SERVER_SERVER_INFO_GET = "go.processor.server-info.get";
}
