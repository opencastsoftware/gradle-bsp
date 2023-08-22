/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.server;

import ch.epfl.scala.bsp4j.*;
import com.opencastsoftware.gradle.bsp.model.*;
import com.opencastsoftware.gradle.bsp.server.util.Conversions;
import com.opencastsoftware.gradle.bsp.server.util.DaemonThreadFactory;
import com.opencastsoftware.gradle.bsp.server.util.GradleResults;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.gradle.tooling.ConfigurableLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.events.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.ExitCode;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GradleBspServer implements BuildServer {
    private static final Logger logger = LoggerFactory.getLogger(GradleBspServer.class);

    private int exitCode = ExitCode.OK;
    private BuildClient client;

    private final Path initScriptPath;

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final AtomicReference<ProjectConnection> gradleConnection;
    private final AtomicReference<BspWorkspace> workspace;
    private final AtomicReference<BuildClientCapabilities> clientCapabilities = new AtomicReference<>();

    private final ThreadFactory threadFactory = DaemonThreadFactory.create(logger, "gradle-buildserver-%d");
    private final ExecutorService executor = Executors.newCachedThreadPool(threadFactory);

    public GradleBspServer(ProjectConnection gradleConnection, Path initScriptPath) {
        this.initScriptPath = initScriptPath;
        this.gradleConnection = new AtomicReference<>(gradleConnection);
        this.workspace = new AtomicReference<>(getCustomModel(gradleConnection, BspWorkspace.class));
        logger.info("Retrieved workspace model {}", this.workspace.get());
    }

    private <T> T getCustomModel(ProjectConnection connection, Class<T> customModelClass) {
        return connection
                .model(customModelClass)
                .setStandardOutput(System.err)
                .setStandardError(System.err)
                .addArguments("--init-script", initScriptPath.toString())
                .get();
    }

    private <T> CompletableFuture<T> getCustomModelFuture(ProjectConnection connection, Class<T> customModelClass) {
        var modelBuilder = connection
                .model(customModelClass)
                .setStandardOutput(System.err)
                .setStandardError(System.err)
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

    List<String> getLanguageIds(Predicate<? super BspBuildTarget> targetFilter) {
        return workspace.get().buildTargets().stream()
                .filter(targetFilter)
                .flatMap(target -> target.languageIds().stream())
                .distinct().collect(Collectors.toList());
    }

    BuildServerCapabilities getCapabilities() {
        var serverCapabilities = new BuildServerCapabilities();

        // This is a very loose approximation - targets can contain multiple languages
        // and e.g. ANTLR can't be run even if it's contained in a Java target that can
        var compilableLanguageIds = getLanguageIds(t -> t.capabilities().canCompile());
        var testableLanguageIds = getLanguageIds(t -> t.capabilities().canTest());
        var runnableLanguageIds = getLanguageIds(t -> t.capabilities().canRun());
        var compileCapabilities = new CompileProvider(compilableLanguageIds);
        var testCapabilities = new TestProvider(testableLanguageIds);
        var runCapabilities = new RunProvider(runnableLanguageIds);

        serverCapabilities.setCanReload(true);

        if (!compilableLanguageIds.isEmpty()) {
            serverCapabilities.setCompileProvider(compileCapabilities);
        }

        if (!testableLanguageIds.isEmpty()) {
            serverCapabilities.setTestProvider(testCapabilities);
        }

        if (!runnableLanguageIds.isEmpty()) {
            serverCapabilities.setRunProvider(runCapabilities);
        }

        serverCapabilities.setResourcesProvider(Boolean.TRUE);
        serverCapabilities.setInverseSourcesProvider(Boolean.TRUE);
        serverCapabilities.setDependencyModulesProvider(Boolean.TRUE);

        return serverCapabilities;
    }

    @Override
    public CompletableFuture<InitializeBuildResult> buildInitialize(InitializeBuildParams params) {
        return CompletableFutures.computeAsync(executor, cancelToken -> {
            cancelToken.checkCanceled();

            clientCapabilities.set(params.getCapabilities());

            return new InitializeBuildResult(
                    "Gradle Build Server",
                    BuildInfo.version,
                    BuildInfo.bspVersion,
                    getCapabilities());
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

    private boolean hasClientSupportedLanguage(BspBuildTarget target) {
        return clientCapabilities.get()
                .getLanguageIds().stream()
                .anyMatch(supportedLanguage -> target.languageIds().contains(supportedLanguage));
    }

    @Override
    public CompletableFuture<WorkspaceBuildTargetsResult> workspaceBuildTargets() {
        return ifInitialized(cancelToken -> {
            cancelToken.checkCanceled();
            var buildTargets = workspace.get()
                    .buildTargets().stream()
                    .filter(this::hasClientSupportedLanguage)
                    .map(Conversions::toBspBuildTarget)
                    .collect(Collectors.toList());
            return new WorkspaceBuildTargetsResult(buildTargets);
        });
    }

    @Override
    public CompletableFuture<Object> workspaceReload() {
        return ifInitializedAsync(cancelToken -> {
            cancelToken.checkCanceled();
            return getCustomModelFuture(this.gradleConnection.get(), BspWorkspace.class).thenApply(workspace -> {
                cancelToken.checkCanceled();
                this.workspace.set(workspace);
                return null;
            });
        });
    }

    List<String> getTargetUris(SourcesParams params) {
        return params.getTargets().stream()
                .map(BuildTargetIdentifier::getUri)
                .collect(Collectors.toList());
    }

    List<SourcesItem> getSourcesFrom(List<String> targetUris) {
        var sourceDirectoriesMapping = workspace.get().buildTargetSources().getSources();

        return targetUris.stream().flatMap(target -> {
            return Stream.ofNullable(sourceDirectoriesMapping.get(target))
                    .map(srcDirs -> {
                        var id = new BuildTargetIdentifier(target);
                        var sourceItems = srcDirs.stream()
                                .map(srcDir -> new SourceItem(srcDir.uri(), SourceItemKind.DIRECTORY, srcDir.generated()))
                                .collect(Collectors.toList());
                        return new SourcesItem(id, sourceItems);
                    });
        }).collect(Collectors.toList());
    }

    @Override
    public CompletableFuture<SourcesResult> buildTargetSources(SourcesParams params) {
        return ifInitialized(cancelToken -> {
            var targetUris = getTargetUris(params);
            var sourcesItems = getSourcesFrom(targetUris);
            return new SourcesResult(sourcesItems);
        });
    }

    boolean sourceItemContains(BspSourceItem sourceItem, Path documentPath) {
        var sourceItemPath = Paths.get(URI.create(sourceItem.uri()));
        return documentPath.normalize().startsWith(sourceItemPath.normalize());
    }

    boolean entryContains(Map.Entry<String, Set<BspSourceItem>> entry, Path documentPath)  {
        return entry.getValue().stream()
                .anyMatch(sourceItem -> sourceItemContains(sourceItem, documentPath));
    }

    @Override
    public CompletableFuture<InverseSourcesResult> buildTargetInverseSources(InverseSourcesParams params) {
        return ifInitialized(cancelToken -> {
            var documentUri = params.getTextDocument().getUri();
            var documentPath = Paths.get(URI.create(documentUri));
            var buildTargets = workspace.get()
                    .buildTargetSources().getSources()
                    .entrySet().stream()
                    .filter(entry -> entryContains(entry, documentPath))
                    .map(entry -> new BuildTargetIdentifier(entry.getKey()))
                    .collect(Collectors.toList());
            return new InverseSourcesResult(buildTargets);
        });
    }

    @Override
    public CompletableFuture<DependencySourcesResult> buildTargetDependencySources(DependencySourcesParams params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildTargetDependencySources'");
    }


    List<String> getTargetUris(ResourcesParams params) {
        return params.getTargets().stream()
                .map(BuildTargetIdentifier::getUri)
                .collect(Collectors.toList());
    }

    List<ResourcesItem> getResourcesFrom(List<String> targetUris) {
        var resourceDirectoriesMapping = workspace.get().buildTargetResources().getResources();

        return targetUris.stream().flatMap(target -> {
            return Stream.ofNullable(resourceDirectoriesMapping.get(target)).map(srcDirs -> {
                var id = new BuildTargetIdentifier(target);
                return new ResourcesItem(id, new ArrayList<>(srcDirs));
            });
        }).collect(Collectors.toList());
    }

    @Override
    public CompletableFuture<ResourcesResult> buildTargetResources(ResourcesParams params) {
        return ifInitialized(cancelToken -> {
            var targetUris = getTargetUris(params);
            var resourcesItems = getResourcesFrom(targetUris);
            return new ResourcesResult(resourcesItems);
        });
    }

    @Override
    public CompletableFuture<OutputPathsResult> buildTargetOutputPaths(OutputPathsParams params) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildTargetOutputPaths'");
    }

    <T extends ConfigurableLauncher<T>> T configureBuildLauncher(CancelChecker cancelToken, String originId, Function<ProjectConnection, T> launcherFn) {
        var gradleCanceller = GradleConnector.newCancellationTokenSource();
        var operationTypes = Set.of(OperationType.BUILD_PHASE, OperationType.TASK);
        var progressListener = new BuildClientProgressListener(client, cancelToken, gradleCanceller, originId);
        return launcherFn.apply(gradleConnection.get())
                .setStandardOutput(System.err)
                .setStandardError(System.err)
                .withCancellationToken(gradleCanceller.token())
                .addProgressListener(progressListener, operationTypes);
    }

    List<String> getTargetUris(CompileParams params) {
        return params.getTargets().stream()
                .map(BuildTargetIdentifier::getUri)
                .collect(Collectors.toList());
    }

    String[] getCompileTasksFrom(List<String> targetUris) {
        var compileTaskMapping = workspace.get().compileTasks().getCompileTasks();

        return targetUris.stream().flatMap(target -> {
            return Stream.ofNullable(compileTaskMapping.get(target));
        }).distinct().toArray(String[]::new);
    }

    @Override
    public CompletableFuture<CompileResult> buildTargetCompile(CompileParams params) {
        return ifInitializedAsync(cancelToken -> {
            var compileResult = new CompileResult(StatusCode.OK);
            compileResult.setOriginId(params.getOriginId());

            var targetUris = getTargetUris(params);
            var targetCompileTasks = getCompileTasksFrom(targetUris);

            if (targetCompileTasks.length == 0) {
                logger.error("No compile tasks could be found for build targets {}", String.join(", ", targetUris));
                compileResult.setStatusCode(StatusCode.ERROR);
                return CompletableFuture.completedFuture(compileResult);
            }

            logger.info("Running build tasks {}", String.join(", ", targetCompileTasks));

            var build = configureBuildLauncher(cancelToken, params.getOriginId(), ProjectConnection::newBuild);

            return GradleResults.handleCompile(compileResult, build.forTasks(targetCompileTasks));
        });
    }

    List<String> getTargetUris(TestParams params) {
        return params.getTargets().stream()
                .map(BuildTargetIdentifier::getUri)
                .collect(Collectors.toList());
    }

    String[] getTestTasksFrom(List<String> targetUris) {
        var testTaskMapping = workspace.get().testTasks().getTestTasks();

        return targetUris.stream().flatMap(target -> {
            return Stream.ofNullable(testTaskMapping.get(target))
                    .flatMap(Collection::stream);
        }).distinct().toArray(String[]::new);
    }

    @Override
    public CompletableFuture<TestResult> buildTargetTest(TestParams params) {
        return ifInitializedAsync(cancelToken -> {
            var testResult = new TestResult(StatusCode.OK);
            testResult.setOriginId(params.getOriginId());

            var targetUris = getTargetUris(params);
            var targetTestTasks = getTestTasksFrom(targetUris);

            if (targetTestTasks.length == 0) {
                logger.error("No test tasks could be found for build targets {}", String.join(", ", targetUris));
                testResult.setStatusCode(StatusCode.ERROR);
                return CompletableFuture.completedFuture(testResult);
            }

            logger.info("Running build tasks {}", String.join(", ", targetTestTasks));

            var build = configureBuildLauncher(cancelToken, params.getOriginId(), ProjectConnection::newTestLauncher);

            return GradleResults.handleTest(testResult, build.forTasks(targetTestTasks));
        });
    }

    String[] getRunTaskFor(String targetUri) {
        var runTaskMapping = workspace.get().runTasks().getRunTasks();
        return Stream.ofNullable(runTaskMapping.get(targetUri)).toArray(String[]::new);
    }

    @Override
    public CompletableFuture<RunResult> buildTargetRun(RunParams params) {
        return ifInitializedAsync(cancelToken -> {
            var runResult = new RunResult(StatusCode.OK);
            runResult.setOriginId(params.getOriginId());

            var targetUri = params.getTarget().getUri().toString();
            var targetRunTasks = getRunTaskFor(targetUri);

            if (targetRunTasks.length == 0) {
                logger.error("No run tasks could be found for build target {}", targetUri);
                runResult.setStatusCode(StatusCode.ERROR);
                return CompletableFuture.completedFuture(runResult);
            }

            logger.info("Running build tasks {}", String.join(", ", targetRunTasks));

            var build = configureBuildLauncher(cancelToken, params.getOriginId(), ProjectConnection::newBuild);

            return GradleResults.handleRun(runResult, build.forTasks(targetRunTasks));
        });
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

    List<String> getTargetUris(CleanCacheParams params) {
        return params.getTargets().stream()
                .map(BuildTargetIdentifier::getUri)
                .collect(Collectors.toList());
    }

    String[] getCleanTasksFrom(List<String> targetUris) {
        var cleanTaskMapping = workspace.get().cleanTasks().getCleanTasks();

        return targetUris.stream().flatMap(target -> {
            return Stream.ofNullable(cleanTaskMapping.get(target));
        }).distinct().toArray(String[]::new);
    }

    @Override
    public CompletableFuture<CleanCacheResult> buildTargetCleanCache(CleanCacheParams params) {
        return ifInitializedAsync(cancelToken -> {
            var cleanResult = new CleanCacheResult(null, Boolean.TRUE);

            var targetUris = getTargetUris(params);
            var targetCleanTasks = getCleanTasksFrom(targetUris);

            if (targetCleanTasks.length == 0) {
                logger.error("No clean tasks could be found for build targets {}", String.join(", ", targetUris));
                cleanResult.setCleaned(Boolean.FALSE);
                return CompletableFuture.completedFuture(cleanResult);
            }

            logger.info("Running build tasks {}", String.join(", ", targetCleanTasks));

            var build = configureBuildLauncher(cancelToken, null, ProjectConnection::newBuild);

            return GradleResults.handleClean(cleanResult, build.forTasks(targetCleanTasks));
        });
    }

    @Override
    public void onConnectWithClient(BuildClient client) {
        this.client = client;
    }
}
