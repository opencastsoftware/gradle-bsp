/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ResultHandler;

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

    private static class GradleResultHandler<T> implements ResultHandler<T> {
        public CompletableFuture<T> resultFuture;

        public GradleResultHandler(CompletableFuture<T> resultFuture) {
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
}
