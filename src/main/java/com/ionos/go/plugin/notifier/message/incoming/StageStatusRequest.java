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
    private Pipeline pipeline;

    /** Summary of the state of a pipeline with its stage and jobs. */
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Pipeline {
        @Expose
        @Getter
        private String name;

        @Expose
        @Getter
        private String label;

        @Expose
        @Getter
        private String counter;

        @Expose
        @Getter
        private String group;

        @Expose
        @Getter
        @SerializedName("build-cause")
        private List<BuildCause> buildCause;

        @Expose
        @Getter
        private Stage stage;
    }

    /** The build cause of a {@link Stage}. */
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BuildCause {
        @Expose
        @Getter
        private Boolean changed;

        @Expose
        @Getter
        private Material material;

        @Expose
        @Getter
        List<Modification> modifications;
    }

    /** The material of a {@link BuildCause}. */
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Material {
        @Expose
        @Getter
        private String fingerprint;

        @Expose
        @Getter
        @SerializedName("git-configuration")
        private GitConfiguration gitConfiguration;

        @Expose
        @Getter
        @SerializedName("pipeline-configuration")
        private PipelineConfiguration pipelineConfiguration;

        @Expose
        @Getter
        String type;
    }

    /** The git config of a {@link Material}. */
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GitConfiguration {
        @Expose
        @Getter
        private String branch;

        @Expose
        @Getter
        @SerializedName("shallow-clone")
        private Boolean shallowClone;

        @Expose
        @Getter
        String url;
    }

    /** The pipeline config of a {@link Material}. */
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PipelineConfiguration {
        @Expose
        @Getter
        @SerializedName("pipeline-name")
        private String pipelineName;

        @Expose
        @Getter
        @SerializedName("stage-name")
        private String stageName;
    }

    /** A modification within a {@link BuildCause}. */
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Modification {
        @Expose
        @Getter
        private Map<Object, Object> data;

        @Expose
        @Getter
        @SerializedName("modified-time")
        private ZonedDateTime modifiedTime;

        @Expose
        @Getter
        String revision;
    }

    /** The state summary of a stage within a {@link Pipeline}. */
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Stage {
        @Expose
        @Getter
        private String name;

        @Expose
        @Getter
        private String counter;

        @Expose
        @Getter
        @SerializedName("approval-type")
        private String approvalType;

        @Expose
        @Getter
        @SerializedName("approved-by")
        private String approvedBy;

        @Expose
        @Getter
        @SerializedName("previous-stage-name")
        private String previousStageName;

        @Expose
        @Getter
        @SerializedName("previous-stage-counter")
        private Integer previousStageCounter;

        @Expose
        @Getter
        private String state;

        @Expose
        @Getter
        private String result;

        @Expose
        @Getter
        @SerializedName("create-time")
        private ZonedDateTime createTime;

        @Expose
        @Getter
        @SerializedName("last-transition-time")
        private ZonedDateTime lastTransitionTime;

        @Expose
        @Getter
        private Job[] jobs;
    }

    /** The state summary of a job within a {@link Stage}. */
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Job {
        @Expose
        @Getter
        @SerializedName("agent-uuid")
        private String agentUUID;

        @Expose
        @Getter
        private String name;

        @Expose
        @Getter
        private String state;

        @Expose
        @Getter
        private String result;

        @Expose
        @Getter
        @SerializedName("schedule-time")
        private ZonedDateTime scheduleTime;

        @Expose
        @Getter
        @SerializedName("assign-time")
        private ZonedDateTime assignTime;

        @Expose
        @Getter
        @SerializedName("complete-time")
        private ZonedDateTime completeTime;
    }
}
