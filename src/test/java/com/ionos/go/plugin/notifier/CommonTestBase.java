package com.ionos.go.plugin.notifier;

import com.google.gson.Gson;
import com.ionos.go.plugin.notifier.util.Helper;
import com.thoughtworks.go.plugin.api.logging.Logger;
import lombok.Getter;
import org.junit.Before;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class CommonTestBase {
    @Getter
    private final Logger logger;

    @Getter
    private Gson gson;

    @Getter
    private Map<String, String> serverInfo = new HashMap<>();

    @Getter
    private Map<String, String> pluginSettings = new HashMap<>();

    protected CommonTestBase() {
        logger = Logger.getLoggerFor(getClass());
    }

    @Before
    public void setupObjects() throws IOException {
        this.gson = new Gson();
        this.serverInfo = gson.fromJson(Helper.readResource("/serverInfo.json"), Map.class);
        this.pluginSettings = newGoodDirectSettingsTemplate();
    }

    static Map<String, String> newGoodDirectSettingsTemplate() {
        Map<String, String> pluginSettings = new HashMap<>();
        pluginSettings.put(Constants.PARAM_TEMPLATE, "${stageStatus.pipeline.name}");
        pluginSettings.put(Constants.PARAM_CONDITION, "${(stageStatus.pipeline.stage.state == 'Failed')?string('true', 'false')}");
        pluginSettings.put(Constants.PARAM_WEBHOOK_URL, "https://localhost/");
        pluginSettings.put(Constants.PARAM_PROXY_URL, "");
        return pluginSettings;
    }

    static Map<String, Map<String, String>> toMapWithValueLayer(Map<String, String> settings) {
        Map<String, Map<String, String>> result = new HashMap<>();
        settings.entrySet().stream().forEach(entry -> {
            result.put(entry.getKey(), Collections.singletonMap(Constants.FIELD_VALUE, entry.getValue()));
        });
        return result;
    }

    static Map<String, Map<String, String>> newGoodPluginSettingsTemplate() {
       return toMapWithValueLayer(newGoodDirectSettingsTemplate());
    }
}
