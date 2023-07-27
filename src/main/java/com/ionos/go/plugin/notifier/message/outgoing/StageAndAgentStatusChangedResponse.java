package com.ionos.go.plugin.notifier.message.outgoing;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;

/** Response for the {@code stage-status} request.
 * @see <a href="https://plugin-api.gocd.org/current/notifications/#stage-status-changed">here</a>
 * */
@AllArgsConstructor
public class StageAndAgentStatusChangedResponse {

    /** The status of processing a stage status or agent status challenge. */
    public enum Status {
        /** Plugin processed the request correctly. */
        success,
        /** Plugin failed. */
        failure
    }

    /**
     * Creates a new response.
      * @param status sthe status of the response.
     * @param messages the description of the status, usually only in case of a failure.
     */
    public StageAndAgentStatusChangedResponse(@NonNull Status status, String... messages) {
        this.status = status;
        if (messages != null) {
            this.messages = Arrays.asList(messages);
        }
    }

    /** The status of the response. */
    @Expose
    @Getter
    private Status status;

    /** More descriptive messages regarding the status. */
    @Expose
    @Getter
    private List<String> messages;
}
