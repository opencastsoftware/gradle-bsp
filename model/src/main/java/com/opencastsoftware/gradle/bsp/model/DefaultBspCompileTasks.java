/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.util.Map;
import java.util.Objects;

public class DefaultBspCompileTasks implements BspCompileTasks {
    private final Map<String, String> compileTasks;

    public DefaultBspCompileTasks(Map<String, String> compileTasks) {
        this.compileTasks = compileTasks;
    }

    @Override
    public Map<String, String> getCompileTasks() {
        return compileTasks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspCompileTasks that = (DefaultBspCompileTasks) o;
        return Objects.equals(compileTasks, that.compileTasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(compileTasks);
    }

    @Override
    public String toString() {
        return "DefaultBspCompileTasks[" +
                "compileTasks=" + compileTasks +
                ']';
    }
}
