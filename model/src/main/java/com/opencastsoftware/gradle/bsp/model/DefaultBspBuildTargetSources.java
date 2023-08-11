/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DefaultBspBuildTargetSources implements BspBuildTargetSources {
    private final Map<String, Set<BspSourceItem>> sources;

    public DefaultBspBuildTargetSources(Map<String, Set<BspSourceItem>> sources) {
        this.sources = sources;
    }

    @Override
    public Map<String, Set<BspSourceItem>> getSources() {
        return sources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspBuildTargetSources that = (DefaultBspBuildTargetSources) o;
        return Objects.equals(sources, that.sources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sources);
    }

    @Override
    public String toString() {
        return "DefaultBspBuildTargetSources[" +
                "sources=" + sources +
                ']';
    }
}
