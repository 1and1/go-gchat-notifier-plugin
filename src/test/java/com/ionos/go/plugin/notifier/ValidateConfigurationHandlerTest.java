package com.ionos.go.plugin.notifier;

import com.ionos.go.plugin.notifier.util.Helper;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class ValidateConfigurationHandlerTest extends CommonTestBase {

    @Test
    public void toFlatSettingsWithTwoEntries()  {
        Map<String, Map<String, String>> input = new HashMap<>();
        input.put("foo", Collections.singletonMap(Constants.FIELD_VALUE, "bar"));
        input.put("my", Collections.singletonMap(Constants.FIELD_VALUE, "value"));

        Map<String, String> actual = ValidateConfigurationHandler.toFlatSettings(input);
        Map<String, String> expected = new HashMap<>();
        expected.put("foo", "bar");
        expected.put("my", "value");
        assertEquals(expected, actual);
    }

    @Test
    public void toFlatSettingsWithOneNonValue()  {
        Map<String, Map<String, String>> input = new HashMap<>();
        input.put("foo", Collections.singletonMap(Constants.FIELD_VALUE, "bar"));
        input.put("my", Collections.singletonMap("baz", "value"));

        Map<String, String> actual = ValidateConfigurationHandler.toFlatSettings(input);
        Map<String, String> expected = new HashMap<>();
        expected.put("foo", "bar");
        assertEquals(expected, actual);
    }
}
