package com.ionos.go.plugin.notifier.message.outgoing;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/** Response for the {@code stage-status} request.
 * @see <a href="https://plugin-api.gocd.org/current/notifications/#stage-status-changed">here</a>
 * */
@AllArgsConstructor
public class StageAndAgentStatusChangedResponse {

    public enum Status {
        success,
        failure
    }

    public StageAndAgentStatusChangedResponse(Status status, String... messages) {
        this.status = status;
        if (messages != null) {
            this.messages = Arrays.asList(messages);
        }
    }

    @Expose
    @Getter
    private Status status;

    @Expose
    @Getter
    private List<String> messages;
}
