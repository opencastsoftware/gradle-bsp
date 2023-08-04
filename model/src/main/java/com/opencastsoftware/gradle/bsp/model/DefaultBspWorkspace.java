/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.util.List;
import java.util.Objects;

public class DefaultBspWorkspace implements BspWorkspace {
    private final List<BspBuildTarget> buildTargets;

    public DefaultBspWorkspace(List<BspBuildTarget> buildTargets) {
        this.buildTargets = buildTargets;
    }

    @Override
    public List<BspBuildTarget> buildTargets() {
        return buildTargets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspWorkspace that = (DefaultBspWorkspace) o;
        return Objects.equals(buildTargets, that.buildTargets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buildTargets);
    }

    @Override
    public String toString() {
        return "DefaultBspWorkspace[" +
                "buildTargets=" + buildTargets +
                ']';
    }
}
