/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.server;

import ch.epfl.scala.bsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.gradle.internal.operations.BuildOperationCategory;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import org.gradle.tooling.events.SuccessResult;
import org.gradle.tooling.events.lifecycle.BuildPhaseFinishEvent;
import org.gradle.tooling.events.lifecycle.BuildPhaseStartEvent;
import org.gradle.tooling.events.task.TaskProgressEvent;

import java.util.UUID;
import java.util.concurrent.CancellationException;

public class BuildClientProgressListener implements ProgressListener {
    private final BuildClient client;
    private final CancelChecker buildCancelToken;
    private final CancellationTokenSource gradleCancelToken;
    private final String originId;

    BuildClientProgressListener(BuildClient client, CancelChecker buildCancelToken, CancellationTokenSource gradleCancelToken, String originId) {
        this.client = client;
        this.buildCancelToken = buildCancelToken;
        this.gradleCancelToken = gradleCancelToken;
        this.originId = originId != null ? originId : UUID.randomUUID().toString();
    }

    void checkCancelled() {
        try {
            buildCancelToken.checkCanceled();
        } catch (CancellationException e) {
            gradleCancelToken.cancel();
        }
    }

    @Override
    public void statusChanged(ProgressEvent event) {
        checkCancelled();

        var taskId = new TaskId(originId);

        if (event instanceof BuildPhaseStartEvent) {
            var buildPhaseStartEvent = (BuildPhaseStartEvent) event;
            var buildPhase = buildPhaseStartEvent.getDescriptor().getBuildPhase();
            if (buildPhase.equals(BuildOperationCategory.RUN_MAIN_TASKS.name())) {
                var taskStartParams = new TaskStartParams(taskId);
                taskStartParams.setEventTime(buildPhaseStartEvent.getEventTime());
                taskStartParams.setMessage(buildPhaseStartEvent.getDisplayName());
                client.onBuildTaskStart(taskStartParams);
            }
        } else if (event instanceof TaskProgressEvent) {
            var taskProgressEvent = (TaskProgressEvent) event;
            var taskProgressParams = new TaskProgressParams(taskId);
            taskProgressParams.setEventTime(taskProgressEvent.getEventTime());
            taskProgressParams.setMessage(taskProgressEvent.getDisplayName());
            client.onBuildTaskProgress(taskProgressParams);
        } else if (event instanceof BuildPhaseFinishEvent) {
            var buildPhaseFinishEvent = (BuildPhaseFinishEvent) event;
            var buildPhase = buildPhaseFinishEvent.getDescriptor().getBuildPhase();
            if (buildPhase.equals(BuildOperationCategory.RUN_MAIN_TASKS.name())) {
                var buildResult = buildPhaseFinishEvent.getResult();
                var statusCode = buildResult instanceof SuccessResult ? StatusCode.OK : StatusCode.ERROR;
                var taskFinishParams = new TaskFinishParams(taskId, statusCode);
                taskFinishParams.setEventTime(buildPhaseFinishEvent.getEventTime());
                taskFinishParams.setMessage(buildPhaseFinishEvent.getDisplayName());
                client.onBuildTaskFinish(taskFinishParams);
            }
        }
    }
}
