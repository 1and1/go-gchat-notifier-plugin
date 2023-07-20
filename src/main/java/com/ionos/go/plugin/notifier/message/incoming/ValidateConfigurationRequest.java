package com.ionos.go.plugin.notifier.message.incoming;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/** Request for the {@code validate-configuration} request.
 * @see <a href="https://plugin-api.gocd.org/current/notifications/#validate-plugin-configuration">here</a>
 * */
public class ValidateConfigurationRequest {

    @Expose
    @SerializedName("plugin-settings")
    @Getter
    @Setter
    private Map<String, Map<String, String>> pluginSettings;
}
