/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.server;

import ch.epfl.scala.bsp4j.*;
import com.opencastsoftware.gradle.bsp.model.BspWorkspace;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.gradle.tooling.ProjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.ExitCode;

import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GradleBspServer implements BuildServer {
    private static final Logger logger = LoggerFactory.getLogger(GradleBspServer.class);

    private int exitCode = ExitCode.OK;
    private BuildClient client;

    private final Path initScriptPath;

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final AtomicReference<ProjectConnection> gradleConnection;
    private final AtomicReference<BspWorkspace> workspaceModel;
    private final AtomicReference<BuildClientCapabilities> clientCapabilities = new AtomicReference<>();

    private final ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicLong count = new AtomicLong(0);
        private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (t, ex) -> {
            logger.error("Uncaught exception in thread {}", t.getName(), ex);
        };

        @Override
        public Thread newThread(Runnable runnable) {
            var thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setDaemon(true);
            thread.setName(String.format("gradle-buildserver-%d", count.getAndIncrement()));
            thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            return thread;
        }
    };

    private final ExecutorService executor = Executors.newCachedThreadPool(threadFactory);

    public GradleBspServer(ProjectConnection gradleConnection, Path initScriptPath) {
        this.initScriptPath = initScriptPath;
        this.gradleConnection = new AtomicReference<>(gradleConnection);
        this.workspaceModel = new AtomicReference<>(getCustomModel(gradleConnection, BspWorkspace.class));
    }

    private <T> T getCustomModel(ProjectConnection connection, Class<T> customModelClass) {
        return connection
                .model(customModelClass)
                .addArguments("--init-script", initScriptPath.toString())
                .get();
    }

    private <T> CompletableFuture<T> getCustomModelFuture(ProjectConnection connection, Class<T> customModelClass) {
        var modelBuilder = connection
                .model(customModelClass)
                .addArguments("--init-script", initScriptPath.toString());

        return GradleResults.handle(modelBuilder::get);
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    public boolean isShutdown() {
        return shutdown.get();
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

    public <A> CompletableFuture<A> ifInitialized(Function<CancelChecker, A> action) {
        if (isInitialized() && !isShutdown()) {
            return CompletableFutures.computeAsync(executor, action);
        } else {
            var error = isShutdown()
                    ? new ResponseError(ResponseErrorCode.InvalidRequest, "Server has been shut down", null)
                    : new ResponseError(ResponseErrorCode.ServerNotInitialized, "Server was not initialized", null);
            var result = new CompletableFuture<A>();
            result.completeExceptionally(new ResponseErrorException(error));
            return result;
        }
    }

    public <A> CompletableFuture<A> ifInitializedAsync(Function<CancelChecker, CompletableFuture<A>> action) {
        return ifInitialized(action).thenCompose(x -> x);
    }

    public void ifShouldNotify(Runnable action) {
        if (isInitialized() && !isShutdown()) {
            action.run();
        }
    }

    @Override
    public CompletableFuture<InitializeBuildResult> buildInitialize(InitializeBuildParams params) {
        return CompletableFutures.computeAsync(executor, cancelToken -> {
            cancelToken.checkCanceled();

            clientCapabilities.set(params.getCapabilities());

            var serverCapabilities = new BuildServerCapabilities();

            serverCapabilities.setCanReload(true);

            return new InitializeBuildResult(
                    "Gradle Build Server",
                    BuildInfo.version,
                    BuildInfo.bspVersion,
                    serverCapabilities);
        });
    }

    @Override
    public void onBuildInitialized() {
        initialized.set(true);
    }

    @Override
    public CompletableFuture<Object> buildShutdown() {
        return ifInitialized(cancelToken -> {
            performShutdown();
            shutdown.set(true);
            return null;
        });
    }

    void performShutdown() {
    }

    @Override
    public void onBuildExit() {
        try {
            if (!isShutdown()) {
                logger.error("Server exit request received before shutdown request");
                performShutdown();
                exitCode = ExitCode.SOFTWARE;
            }

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
        return ifInitialized(cancelToken -> {
            cancelToken.checkCanceled();
            var buildTargets = workspaceModel.get()
                    .buildTargets().stream()
                    .map(Conversions::toBspBuildTarget)
                    .collect(Collectors.toList());
            return new WorkspaceBuildTargetsResult(buildTargets);
        });
    }

    @Override
    public CompletableFuture<Object> workspaceReload() {
        return ifInitializedAsync(cancelToken -> {
            cancelToken.checkCanceled();
            return getCustomModelFuture(this.gradleConnection.get(), BspWorkspace.class)
                    .thenApply(workspaceModel -> {
                        cancelToken.checkCanceled();
                        this.workspaceModel.set(workspaceModel);
                        return null;
                    });
        });
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
    public CompletableFuture<OutputPathsResult> buildTargetOutputPaths(OutputPathsParams params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildTargetOutputPaths'");
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
    public CompletableFuture<DependencyModulesResult> buildTargetDependencyModules(DependencyModulesParams params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildTargetDependencyModules'");
    }

    @Override
    public CompletableFuture<DebugSessionAddress> debugSessionStart(DebugSessionParams params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'debugSessionStart'");
    }

    @Override
    public CompletableFuture<CleanCacheResult> buildTargetCleanCache(CleanCacheParams params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildTargetCleanCache'");
    }

    @Override
    public void onConnectWithClient(BuildClient client) {
        this.client = client;
    }
}
