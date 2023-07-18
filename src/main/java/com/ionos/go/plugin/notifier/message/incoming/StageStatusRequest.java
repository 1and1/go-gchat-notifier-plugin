package com.ionos.go.plugin.notifier.message.incoming;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

/** Request for the {@code stage-status} request.
 * @see <a href="https://plugin-api.gocd.org/current/notifications/#request-response-basics">here</a>
 * */
@AllArgsConstructor
@NoArgsConstructor
public class StageStatusRequest {
    @Expose
    @Getter
    @Setter
    private Pipeline pipeline;

    @AllArgsConstructor
    @NoArgsConstructor
    public static class Pipeline {
        @Expose
        @Getter
        @Setter
        private String name;

        @Expose
        @Getter
        @Setter
        private String counter;

        @Expose
        @Getter
        @Setter
        private String group;

        // TODO
        @Expose
        @Getter
        @Setter
        @SerializedName("build-cause")
        private List<Object> buildCause;

        @Expose
        @Getter
        @Setter
        private Stage stage;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class Stage {
        @Expose
        @Getter
        @Setter
        private String name;

        @Expose
        @Getter
        @Setter
        private String counter;

        @Expose
        @Getter
        @Setter
        @SerializedName("approval-type")
        private String approvalType;

        @Expose
        @Getter
        @Setter
        @SerializedName("approved-by")
        private String approvedBy;

        @Expose
        @Getter
        @Setter
        @SerializedName("previous-stage-name")
        private String previousStageName;

        @Expose
        @Getter
        @Setter
        @SerializedName("previous-stage-counter")
        private Integer previousStageCounter;

        @Expose
        @Getter
        @Setter
        private String state;

        @Expose
        @Getter
        @Setter
        private String result;

        @Expose
        @Getter
        @Setter
        @SerializedName("create-time")
        private ZonedDateTime createTime;

        @Expose
        @Getter
        @Setter
        private Job[] jobs;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class Job {
        @Expose
        @Getter
        @Setter
        private String name;

        @Expose
        @Getter
        @Setter
        private String counter;

        @Expose
        @Getter
        @Setter
        private String state;

        @Expose
        @Getter
        @Setter
        private String result;

        @Expose
        @Getter
        @Setter
        @SerializedName("schedule-time")
        private ZonedDateTime scheduleTime;

        @Expose
        @Getter
        @Setter
        @SerializedName("assign-time")
        private ZonedDateTime assignTime;

        @Expose
        @Getter
        @Setter
        @SerializedName("complete-time")
        private ZonedDateTime completeTime;
    }
}
