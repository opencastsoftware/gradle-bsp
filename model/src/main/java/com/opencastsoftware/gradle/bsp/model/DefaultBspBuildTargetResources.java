/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DefaultBspBuildTargetResources implements BspBuildTargetResources {
    private final Map<URI, Set<URI>> resources;

    public DefaultBspBuildTargetResources(Map<URI, Set<URI>> resources) {
        this.resources = resources;
    }

    @Override
    public Map<URI, Set<URI>> getResources() {
        return resources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspBuildTargetResources that = (DefaultBspBuildTargetResources) o;
        return Objects.equals(resources, that.resources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resources);
    }

    @Override
    public String toString() {
        return "DefaultBspBuildTargetResources[" +
                "resources=" + resources +
                ']';
    }
}
