/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import ch.epfl.scala.bsp4j.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.ExitCode;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class GradleBspServer implements BuildServer {
    private static Logger logger = LoggerFactory.getLogger(GradleBspServer.class);

    private int exitCode = ExitCode.OK;
    private BuildClient client;
    private final AtomicReference<BspWorkspace> workspaceModel;

    private ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("gradle-buildserver-%d")
            .setUncaughtExceptionHandler((t, ex) -> {
                logger.error("Uncaught exception in thread {}", t.getName(), ex);
            })
            .build();

    private ExecutorService executor = Executors.newCachedThreadPool(threadFactory);

    public GradleBspServer(BspWorkspace workspaceModel) {
        this.workspaceModel = new AtomicReference<>(workspaceModel);
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public BuildClient getClient() {
        return client;
    }

    public int getExitCode() {
        return exitCode;
    }

    @Override
    public CompletableFuture<InitializeBuildResult> buildInitialize(InitializeBuildParams params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildInitialize'");
    }

    @Override
    public void onBuildInitialized() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onBuildInitialized'");
    }

    @Override
    public CompletableFuture<Object> buildShutdown() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildShutdown'");
    }

    @Override
    public void onBuildExit() {
        try {
            executor.shutdown();

            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                exitCode = ExitCode.SOFTWARE;
            }
        } catch (InterruptedException e) {
            exitCode = ExitCode.SOFTWARE;
        }
    }

    @Override
    public CompletableFuture<WorkspaceBuildTargetsResult> workspaceBuildTargets() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'workspaceBuildTargets'");
    }

    @Override
    public CompletableFuture<Object> workspaceReload() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'workspaceReload'");
    }

    @Override
    public CompletableFuture<SourcesResult> buildTargetSources(SourcesParams params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildTargetSources'");
    }

    @Override
    public CompletableFuture<InverseSourcesResult> buildTargetInverseSources(InverseSourcesParams params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildTargetInverseSources'");
    }

    @Override
    public CompletableFuture<DependencySourcesResult> buildTargetDependencySources(DependencySourcesParams params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildTargetDependencySources'");
    }

    @Override
    public CompletableFuture<ResourcesResult> buildTargetResources(ResourcesParams params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildTargetResources'");
    }

    @Override
    public CompletableFuture<CompileResult> buildTargetCompile(CompileParams params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildTargetCompile'");
    }

    @Override
    public CompletableFuture<TestResult> buildTargetTest(TestParams params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildTargetTest'");
    }

    @Override
    public CompletableFuture<RunResult> buildTargetRun(RunParams params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildTargetRun'");
    }

    @Override
    public CompletableFuture<CleanCacheResult> buildTargetCleanCache(CleanCacheParams params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildTargetCleanCache'");
    }

    @Override
    public CompletableFuture<DependencyModulesResult> buildTargetDependencyModules(DependencyModulesParams params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildTargetDependencyModules'");
    }

    @Override
    public void onConnectWithClient(BuildClient client) {
        this.client = client;
    }
}
