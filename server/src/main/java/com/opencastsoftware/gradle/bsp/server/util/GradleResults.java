/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.server.util;

import ch.epfl.scala.bsp4j.*;
import org.gradle.tooling.*;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class GradleResults {
    public static <T> CompletableFuture<T> handle(Consumer<ResultHandler<? super T>> op) {
        CompletableFuture<T> resultFuture = new CompletableFuture<>();
        op.accept(handler(resultFuture));
        return resultFuture;
    }

    public static <T> ResultHandler<T> handler(CompletableFuture<T> resultFuture) {
        return new GradleResultHandler<>(resultFuture);
    }

    public static CompletableFuture<CompileResult> handleCompile(CompileResult compileResult, BuildLauncher launcher) {
        var resultFuture = new CompletableFuture<CompileResult>();
        launcher.run(compileHandler(compileResult, resultFuture));
        return resultFuture;
    }

    public static ResultHandler<Void> compileHandler(CompileResult compileResult, CompletableFuture<CompileResult> resultFuture) {
        return new GradleCompileResultHandler(compileResult, resultFuture);
    }

    public static CompletableFuture<TestResult> handleTest(TestResult testResult, TestLauncher launcher) {
        var resultFuture = new CompletableFuture<TestResult>();
        launcher.run(testHandler(testResult, resultFuture));
        return resultFuture;
    }

    public static ResultHandler<Void> testHandler(TestResult testResult, CompletableFuture<TestResult> resultFuture) {
        return new GradleTestResultHandler(testResult, resultFuture);
    }

    public static CompletableFuture<RunResult> handleRun(RunResult runResult, BuildLauncher launcher) {
        var resultFuture = new CompletableFuture<RunResult>();
        launcher.run(runHandler(runResult, resultFuture));
        return resultFuture;
    }

    public static ResultHandler<Void> runHandler(RunResult runResult, CompletableFuture<RunResult> resultFuture) {
        return new GradleRunResultHandler(runResult, resultFuture);
    }

    private static class GradleResultHandler<T> implements ResultHandler<T> {
        private final CompletableFuture<T> resultFuture;

        GradleResultHandler(CompletableFuture<T> resultFuture) {
            this.resultFuture = resultFuture;
        }

        @Override
        public void onComplete(T result) {
            resultFuture.complete(result);
        }

        @Override
        public void onFailure(GradleConnectionException failure) {
            resultFuture.completeExceptionally(failure);
        }
    }

    private static class GradleCompileResultHandler implements ResultHandler<Void> {
        private final CompileResult compileResult;
        private final CompletableFuture<CompileResult> resultFuture;

        GradleCompileResultHandler(CompileResult compileResult, CompletableFuture<CompileResult> resultFuture) {
            this.compileResult = compileResult;
            this.resultFuture = resultFuture;
        }

        @Override
        public void onComplete(Void result) {
            compileResult.setStatusCode(StatusCode.OK);
            resultFuture.complete(compileResult);
        }

        @Override
        public void onFailure(GradleConnectionException failure) {
            if (failure instanceof BuildCancelledException) {
                compileResult.setStatusCode(StatusCode.CANCELLED);
            } else {
                compileResult.setStatusCode(StatusCode.ERROR);
            }
            resultFuture.complete(compileResult);
        }
    }

    private static class GradleTestResultHandler implements ResultHandler<Void> {
        private final TestResult testResult;
        private final CompletableFuture<TestResult> resultFuture;

        GradleTestResultHandler(TestResult testResult, CompletableFuture<TestResult> resultFuture) {
            this.testResult = testResult;
            this.resultFuture = resultFuture;
        }

        @Override
        public void onComplete(Void result) {
            testResult.setStatusCode(StatusCode.OK);
            resultFuture.complete(testResult);
        }

        @Override
        public void onFailure(GradleConnectionException failure) {
            if (failure instanceof BuildCancelledException) {
                testResult.setStatusCode(StatusCode.CANCELLED);
            } else {
                testResult.setStatusCode(StatusCode.ERROR);
            }
            resultFuture.complete(testResult);
        }
    }

    private static class GradleRunResultHandler implements ResultHandler<Void> {
        private final RunResult runResult;
        private final CompletableFuture<RunResult> resultFuture;

        GradleRunResultHandler(RunResult runResult, CompletableFuture<RunResult> resultFuture) {
            this.runResult = runResult;
            this.resultFuture = resultFuture;
        }

        @Override
        public void onComplete(Void result) {
            runResult.setStatusCode(StatusCode.OK);
            resultFuture.complete(runResult);
        }

        @Override
        public void onFailure(GradleConnectionException failure) {
            if (failure instanceof BuildCancelledException) {
                runResult.setStatusCode(StatusCode.CANCELLED);
            } else {
                runResult.setStatusCode(StatusCode.ERROR);
            }
            resultFuture.complete(runResult);
        }
    }
}
