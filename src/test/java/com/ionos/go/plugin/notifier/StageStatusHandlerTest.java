package com.ionos.go.plugin.notifier;

import com.ionos.go.plugin.notifier.util.Helper;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class StageStatusHandlerTest extends CommonTestBase {

    @Test
    void testInit() {
        StageStatusHandler handler = new StageStatusHandler(getServerInfo(), getPluginSettings());
    }

    @Test
    void testHandle() throws IOException {
        String stageStatusJson = Helper.readResource("/stageStatus.json");
        GoPluginApiRequest request = GoCdObjects.request(Constants.PLUGIN_STAGE_STATUS, stageStatusJson);
        StageStatusHandler handler = new StageStatusHandler(getServerInfo(), getPluginSettings());
        GoPluginApiResponse response = handler.handle(request);
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
    }
}
