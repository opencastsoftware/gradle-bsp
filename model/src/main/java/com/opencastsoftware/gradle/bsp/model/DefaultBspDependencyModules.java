/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DefaultBspDependencyModules implements BspDependencyModules {
    private final Map<URI, Set<BspDependencyModule>> dependencyModules;

    public DefaultBspDependencyModules(Map<URI, Set<BspDependencyModule>> dependencyModules) {
        this.dependencyModules = dependencyModules;
    }

    @Override
    public Map<URI, Set<BspDependencyModule>> dependencyModules() {
        return dependencyModules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspDependencyModules that = (DefaultBspDependencyModules) o;
        return Objects.equals(dependencyModules, that.dependencyModules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencyModules);
    }

    @Override
    public String toString() {
        return "DefaultBspDependencyModules[" +
                "dependencyModules=" + dependencyModules +
                ']';
    }
}
