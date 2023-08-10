/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DefaultBspTestTasks implements BspTestTasks {
    private final Map<String, Set<String>> testTasks;

    public DefaultBspTestTasks(Map<String, Set<String>> testTasks) {
        this.testTasks = testTasks;
    }

    @Override
    public Map<String, Set<String>> getTestTasks() {
        return testTasks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspTestTasks that = (DefaultBspTestTasks) o;
        return Objects.equals(testTasks, that.testTasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testTasks);
    }

    @Override
    public String toString() {
        return "DefaultBspTestTasks[" +
                "testTasks=" + testTasks +
                ']';
    }
}
