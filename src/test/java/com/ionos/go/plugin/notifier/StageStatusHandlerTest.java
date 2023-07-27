package com.ionos.go.plugin.notifier;

import com.ionos.go.plugin.notifier.util.Helper;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class StageStatusHandlerTest extends CommonTestBase {

    @Test
    public void testInit() {
        StageStatusHandler handler = new StageStatusHandler(getServerInfo(), getPluginSettings());
    }

    @Test
    public void testHandle() throws IOException {
        String stageStatusJson = Helper.readResource("/stageStatus.json");
        GoPluginApiRequest request = GoCdObjects.request(Constants.PLUGIN_STAGE_STATUS, stageStatusJson);
        StageStatusHandler handler = new StageStatusHandler(getServerInfo(), getPluginSettings());
        GoPluginApiResponse response = handler.handle(request);
        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.responseCode());
    }
}
