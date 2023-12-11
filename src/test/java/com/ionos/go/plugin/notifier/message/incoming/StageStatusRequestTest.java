package com.ionos.go.plugin.notifier.message.incoming;

import com.ionos.go.plugin.notifier.util.Helper;
import com.ionos.go.plugin.notifier.util.JsonUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StageStatusRequestTest {
    /** Validate JSON to Java mapping with real world example. */
    @Test
    void testMapping() throws IOException {
        String stageStatusRequest = Helper.readResource("/bigStateStatusFromGocd.json");
        StageStatusRequest req = JsonUtil.fromJsonString(stageStatusRequest, StageStatusRequest.class);

        assertNotNull(req.getPipeline());
        assertEquals(2, req.getPipeline().getBuildCause().size());

        // build cause 0
        assertEquals("a1e945018e590e1b1f48949f34278a4bb8f70079c936ea5d1a882d356dcd0919",
                req.getPipeline().getBuildCause().get(0).getMaterial().getFingerprint());
        assertEquals(false,
                req.getPipeline().getBuildCause().get(0).getChanged());
        assertNull(
                req.getPipeline().getBuildCause().get(0).getMaterial().getPipelineConfiguration());

        assertEquals("master",
                req.getPipeline().getBuildCause().get(0).getMaterial().getGitConfiguration().getBranch());
        assertEquals(false,
                req.getPipeline().getBuildCause().get(0).getMaterial().getGitConfiguration().getShallowClone());
        assertEquals("ssh://git@foobar/baz/chimp-integration.git",
                req.getPipeline().getBuildCause().get(0).getMaterial().getGitConfiguration().getUrl());
        assertEquals("git",
                req.getPipeline().getBuildCause().get(0).getMaterial().getType());

        assertEquals(1,
                req.getPipeline().getBuildCause().get(0).getModifications().size());
        assertEquals(0,
                req.getPipeline().getBuildCause().get(0).getModifications().get(0).getData().size());
        assertEquals(ZonedDateTime.parse("2023-05-31T11:22:21.000+00:00"),
                req.getPipeline().getBuildCause().get(0).getModifications().get(0).getModifiedTime());
        assertEquals("39211dea6abf99f5cdbef1013c732e9efe65fee6",
                req.getPipeline().getBuildCause().get(0).getModifications().get(0).getRevision());

        // build cause 1
        assertEquals("e747bc6cfd25bf9c96868a39b87f8d0eec9cf32acfc1d05dff63d63110355c1a",
                req.getPipeline().getBuildCause().get(1).getMaterial().getFingerprint());
        assertEquals(true,
                req.getPipeline().getBuildCause().get(1).getChanged());
        assertNull(
                req.getPipeline().getBuildCause().get(1).getMaterial().getGitConfiguration());
        assertEquals("chimp-sandbox",
                req.getPipeline().getBuildCause().get(1).getMaterial().getPipelineConfiguration().getPipelineName());
        assertEquals("Deploy",
                req.getPipeline().getBuildCause().get(1).getMaterial().getPipelineConfiguration().getStageName());
        assertEquals("pipeline",
                req.getPipeline().getBuildCause().get(1).getMaterial().getType());

        // modifications
        assertEquals(1,
                req.getPipeline().getBuildCause().get(1).getModifications().size());
        assertEquals(0,
                req.getPipeline().getBuildCause().get(1).getModifications().get(0).getData().size());
        assertEquals(ZonedDateTime.parse("2023-07-19T10:23:52.219+00:00"),
                req.getPipeline().getBuildCause().get(1).getModifications().get(0).getModifiedTime());
        assertEquals("chimp-sandbox/815/Deploy/1",
                req.getPipeline().getBuildCause().get(1).getModifications().get(0).getRevision());

        // pipeline
        assertEquals("8176",
                req.getPipeline().getCounter());
        assertEquals("Chimp",
                req.getPipeline().getGroup());
        assertEquals("3d119f92dd1c296b466399fb00b36e32b25c5c82",
                req.getPipeline().getLabel());
        assertEquals("chimp-integration-test",
                req.getPipeline().getName());

        // stage
        assertEquals("success",
                req.getPipeline().getStage().getApprovalType());
        assertEquals("changes",
                req.getPipeline().getStage().getApprovedBy());
        assertEquals("1",
                req.getPipeline().getStage().getCounter());
        assertEquals(ZonedDateTime.parse("2023-07-19T10:23:52.697+00:00"),
                req.getPipeline().getStage().getCreateTime());
        assertEquals(ZonedDateTime.parse("2023-07-19T10:29:22.206+00:00"),
                req.getPipeline().getStage().getLastTransitionTime());
        assertEquals("Deploy",
                req.getPipeline().getStage().getName());
        assertEquals(Integer.valueOf(0),
                req.getPipeline().getStage().getPreviousStageCounter());
        assertNull(
                req.getPipeline().getStage().getPreviousStageName());
        assertEquals("Passed",
                req.getPipeline().getStage().getResult());
        assertEquals("Passed",
                req.getPipeline().getStage().getState());

        assertEquals(3,
                req.getPipeline().getStage().getJobs().length);

        // job 0
        assertEquals("d6d98e4f-d2c8-47a0-8326-dc7785194c58",
                req.getPipeline().getStage().getJobs()[0].getAgentUUID());
        assertEquals("Change",
                req.getPipeline().getStage().getJobs()[0].getName());
        assertEquals("Completed",
                req.getPipeline().getStage().getJobs()[0].getState());
        assertEquals("Passed",
                req.getPipeline().getStage().getJobs()[0].getResult());
        assertEquals(ZonedDateTime.parse("2023-07-19T10:24:00.394+00:00"),
                req.getPipeline().getStage().getJobs()[0].getAssignTime());
        assertEquals(ZonedDateTime.parse("2023-07-19T10:24:06.320+00:00"),
                req.getPipeline().getStage().getJobs()[0].getCompleteTime());
        assertEquals(ZonedDateTime.parse("2023-07-19T10:23:52.697+00:00"),
                req.getPipeline().getStage().getJobs()[0].getScheduleTime());
    }
}
