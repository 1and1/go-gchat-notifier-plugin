package com.ionos.go.plugin.notifier;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Collections;
import java.util.Map;


public class GoNotifierPluginTest {

    private static EmbeddedHttpServer embeddedHttpServer;
    private static int embeddedHttpPort;
    private GoNotifierPlugin goNotifierPlugin;

    @BeforeClass
    public static void setUpLocalWebServer() {
        embeddedHttpServer = new EmbeddedHttpServer().withPath(new File("src/test/resources/web"));
        embeddedHttpServer.start();
        embeddedHttpPort = embeddedHttpServer.getRunningPort();
    }

    @AfterClass
    public static void stopLocalWebServer() {
        embeddedHttpServer.stop();
    }

    @Before
    public void setupPlugin() {
        this.goNotifierPlugin = new GoNotifierPlugin();
    }

    @Test
    public void testHandleNotificationsInterestedIn() throws Exception {
        // request get conf
        GoPluginApiResponse response = goNotifierPlugin.handle(request("notifications-interested-in", null));
        assertNotNull(response);
        assertEquals(200, response.responseCode());
        assertEquals(Collections.emptyMap(), response.responseHeaders());
        assertEquals("{\"notifications\":[\"stage-status\"]}", response.responseBody());
    }

    @Test
    public void testPluginIdentifier() {
        assertEquals("notification", goNotifierPlugin.pluginIdentifier().getExtension());
        assertTrue(goNotifierPlugin.pluginIdentifier().getSupportedExtensionVersions().contains("4.0"));
    }

    private static GoPluginApiRequest request(final String requestName, final String requestBody) {
        return new GoPluginApiRequest() {
            @Override
            public String extension() {
                return null;
            }

            @Override
            public String extensionVersion() {
                return null;
            }

            @Override
            public String requestName() {
                return requestName;
            }

            @Override
            public Map<String, String> requestParameters() {
                return null;
            }

            @Override
            public Map<String, String> requestHeaders() {
                return null;
            }

            @Override
            public String requestBody() {
                return requestBody;
            }
        };
    }

}