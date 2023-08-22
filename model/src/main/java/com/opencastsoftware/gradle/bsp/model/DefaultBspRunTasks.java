/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class DefaultBspRunTasks implements BspRunTasks {

    private final Map<URI, String> runTasks;

    public DefaultBspRunTasks(Map<URI, String> runTasks) {
        this.runTasks = runTasks;
    }

    @Override
    public Map<URI, String> getRunTasks() {
        return runTasks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspRunTasks that = (DefaultBspRunTasks) o;
        return Objects.equals(runTasks, that.runTasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(runTasks);
    }

    @Override
    public String toString() {
        return "DefaultBspRunTasks[" +
                "runTasks=" + runTasks +
                ']';
    }
}
