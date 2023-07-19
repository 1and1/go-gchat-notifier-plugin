package com.ionos.go.plugin.notifier.util;

import com.ionos.go.plugin.notifier.GoNotifierPlugin;
import com.ionos.go.plugin.notifier.message.incoming.StageStatusRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class JsonUtilTest {

    @Test
    public void testZoneDateTimeConversion() {
        String parseMe =
            "{ " +
                "\"pipeline\": {" +
                    "\"stage\": {" +
                        "\"create-time\": \"2023-07-19T09:15:32.166+0000\"" +
                    "}" +
                "}" +
            "}";

        StageStatusRequest request = JsonUtil.fromJsonString(parseMe, StageStatusRequest.class);
        assertNotNull(request);
        assertNotNull(request.getPipeline().getStage().getCreateTime());
        assertEquals("2023-07-19T09:15:32.166Z",
                request.getPipeline().getStage().getCreateTime().format(DateTimeFormatter. ISO_OFFSET_DATE_TIME));
    }
}