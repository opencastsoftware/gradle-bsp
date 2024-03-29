/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class DefaultBspCleanTasks implements BspCleanTasks {
    private final Map<URI, String> cleanTasks;

    public DefaultBspCleanTasks(Map<URI, String> cleanTasks) {
        this.cleanTasks = cleanTasks;
    }

    @Override
    public Map<URI, String> getCleanTasks() {
        return cleanTasks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspCleanTasks that = (DefaultBspCleanTasks) o;
        return Objects.equals(cleanTasks, that.cleanTasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cleanTasks);
    }

    @Override
    public String toString() {
        return "DefaultBspCleanTasks[" +
                "cleanTasks=" + cleanTasks +
                ']';
    }
}
