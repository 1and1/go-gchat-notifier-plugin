package com.ionos.go.plugin.notifier.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class HelperTest {

    @Test
    void testReadResource() throws IOException {
        String actual = Helper.readResource("/serverInfo.json");

        String expected = "{\n" +
                "  \"server_id\": \"df0cb9be-2696-4689-8d46-1ef3c4e4447c\",\n" +
                "  \"site_url\": \"http://example.com:8153/go\",\n" +
                "  \"secure_site_url\": \"https://example.com:8154/go\"\n" +
                "}\n";

        assertEquals(expected, actual);
    }
}