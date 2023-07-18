package com.ionos.go.plugin.notifier.message.outgoing;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Response for the {@code validate-configuration} request.
 * @see <a href="https://plugin-api.gocd.org/current/notifications/#validate-plugin-configuration">here</a>
 * */
@AllArgsConstructor
public class ValidateConfigurationResponse {
    @Expose
    @Getter
    private String key;

    @Expose
    @Getter
    private String message;
}
