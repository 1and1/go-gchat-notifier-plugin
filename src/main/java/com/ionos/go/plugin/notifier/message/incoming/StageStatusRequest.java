package com.ionos.go.plugin.notifier.message.incoming;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

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

    /** Summary of the state of a pipeline with its stage and jobs. */
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

        @Expose
        @Getter
        @Setter
        @SerializedName("build-cause")
        private List<BuildCause> buildCause;

        @Expose
        @Getter
        @Setter
        private Stage stage;
    }

    /** The build cause of a {@link Stage}. */
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BuildCause {
        @Expose
        @Getter
        @Setter
        private Boolean changed;

        @Expose
        @Getter
        @Setter
        private Material material;

        @Expose
        @Getter
        @Setter
        List<Modification> modifications;
    }

    /** The material of a {@link BuildCause}. */
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Material {
        @Expose
        @Getter
        @Setter
        private String fingerprint;

        @Expose
        @Getter
        @Setter
        @SerializedName("git-configuration")
        private GitConfiguration gitConfiguration;

        @Expose
        @Getter
        @Setter
        String type;
    }

    /** The git config of a {@link Material}. */
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GitConfiguration {
        @Expose
        @Getter
        @Setter
        private String branch;

        @Expose
        @Getter
        @Setter
        @SerializedName("shallow-clone")
        private Boolean shallowClone;

        @Expose
        @Getter
        @Setter
        String url;
    }

    /** A modification within a {@link BuildCause}. */
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Modification {
        @Expose
        @Getter
        @Setter
        private Map<Object, Object> data;

        @Expose
        @Getter
        @Setter
        @SerializedName("modified-time")
        private ZonedDateTime modifiedTime;

        @Expose
        @Getter
        @Setter
        String revision;
    }

    /** The state summary of a stage within a {@link Pipeline}. */
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

    /** The state summary of a job within a {@link Stage}. */
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
