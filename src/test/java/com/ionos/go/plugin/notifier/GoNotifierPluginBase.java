package com.ionos.go.plugin.notifier;

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import lombok.Getter;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.Collections;


public class GoNotifierPluginBase extends CommonTestBase {
    @Getter
    private final Logger logger;

    @Getter
    private GoNotifierPlugin goNotifierPlugin;

    protected GoNotifierPluginBase() {
        logger = Logger.getLoggerFor(getClass());
    }

    @BeforeEach
    public void setupPlugin() {
        this.goNotifierPlugin = new GoNotifierPlugin();
        this.goNotifierPlugin.initializeGoApplicationAccessor(new GoApplicationAccessor() {
            @Override
            public GoApiResponse submit(GoApiRequest goApiRequest) {
                getLogger().debug("Server request: " + goApiRequest.api());
                if (goApiRequest.api().equals(Constants.SERVER_SERVER_INFO_GET)) {
                    return GoCdObjects.apiResponse(HttpStatus.SC_OK, Collections.emptyMap(), getGson().toJson(getServerInfo()));
                }
                if (goApiRequest.api().equals(Constants.SERVER_PLUGIN_SETTINGS_GET)) {
                    return GoCdObjects.apiResponse(HttpStatus.SC_OK, Collections.emptyMap(), getGson().toJson(getPluginSettings()));
                }
                getLogger().error("Unknown server request: " + goApiRequest.api());
                return GoCdObjects.apiResponse(HttpStatus.SC_NOT_IMPLEMENTED, Collections.emptyMap(), "Not implemented, bro!");
            }
        });
    }
}
