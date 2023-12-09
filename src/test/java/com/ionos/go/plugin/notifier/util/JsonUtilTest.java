package com.ionos.go.plugin.notifier.util;

import com.ionos.go.plugin.notifier.message.incoming.StageStatusRequest;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class JsonUtilTest {

    @Test
    void testZoneDateTimeConversion() {
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