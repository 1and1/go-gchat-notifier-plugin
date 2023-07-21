package com.ionos.go.plugin.notifier;

import com.google.gson.Gson;
import com.ionos.go.plugin.notifier.message.incoming.ValidateConfigurationRequest;
import com.ionos.go.plugin.notifier.message.outgoing.ValidateConfigurationResponse;
import com.ionos.go.plugin.notifier.util.Helper;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class GoNotifierPluginBase extends CommonTestBase {
    @Getter
    private final Logger logger;

    @Getter
    private GoNotifierPlugin goNotifierPlugin;

    protected GoNotifierPluginBase() {
        logger = Logger.getLoggerFor(getClass());
    }

    @Before
    public void setupPlugin() throws IOException {
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
