package com.ionos.go.plugin.notifier.message.incoming;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

/** Request for the {@code stage-status} request.
 * @see <a href="https://plugin-api.gocd.org/current/notifications/#agent-status-changed">here</a>
 * */
@AllArgsConstructor
@NoArgsConstructor
public class AgentStatusRequest {
    /*
    "agent_config_state": "enabled",
            "agent_state": "building",
            "build_state": "building",
            "is_elastic": true,
            "free_space": "100",
            "host_name": "agent_hostname",
            "ip_address": "127.0.0.1",
            "operating_system": "rh",
            "transition_time": "2018-02-15T06:31:28.998+0000",
            "uuid": "agent_uuid"
    */


    enum AgentConfigState {
        enabled,
        disabled,
        pending;
    }

    @Expose
    @SerializedName("agent_config_state")
    @Getter
    @Setter
    private AgentConfigState agentConfigState;

    enum AgentState {
        idle, building, lostcontact, missing, cancelled, unknown;
    }

    @Expose
    @SerializedName("agent_state")
    @Getter
    @Setter
    private AgentState agentState;

    enum BuildState {
        idle, building, cancelled, unknown;
    }

    @Expose
    @SerializedName("build_state")
    @Getter
    @Setter
    private BuildState buildState;

    @Expose
    @SerializedName("is_elastic")
    @Getter
    @Setter
    private Boolean elastic;

    @Expose
    @SerializedName("free_space")
    @Getter
    @Setter
    private String freeSpace;

    @Expose
    @SerializedName("host_name")
    @Getter
    @Setter
    private String hostname;

    @Expose
    @SerializedName("ip_address")
    @Getter
    @Setter
    private String ipAddress;

    @Expose
    @SerializedName("operating_system")
    @Getter
    @Setter
    private String operatingSystem;

    @Expose
    @SerializedName("transition_time")
    @Getter
    @Setter
    private ZonedDateTime transitionTime;

    @Expose
    @Getter
    @Setter
    private String uuid;
}
