/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.util.Objects;

public class DefaultBspBuildTargetCapabilities implements BspBuildTargetCapabilities {
    private final boolean canCompile;
    private final boolean canTest;
    private final boolean canRun;
    private final boolean canDebug;

    public DefaultBspBuildTargetCapabilities(boolean canCompile, boolean canTest, boolean canRun, boolean canDebug) {
        this.canCompile = canCompile;
        this.canTest = canTest;
        this.canRun = canRun;
        this.canDebug = canDebug;
    }

    @Override
    public boolean canCompile() {
        return this.canCompile;
    }

    @Override
    public boolean canTest() {
        return this.canTest;
    }

    @Override
    public boolean canRun() {
        return this.canRun;
    }

    @Override
    public boolean canDebug() {
        return this.canDebug;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspBuildTargetCapabilities that = (DefaultBspBuildTargetCapabilities) o;
        return canCompile == that.canCompile && canTest == that.canTest && canRun == that.canRun && canDebug == that.canDebug;
    }

    @Override
    public int hashCode() {
        return Objects.hash(canCompile, canTest, canRun, canDebug);
    }

    @Override
    public String toString() {
        return "DefaultBspBuildTargetCapabilities[" +
                "canCompile=" + canCompile +
                ", canTest=" + canTest +
                ", canRun=" + canRun +
                ", canDebug=" + canDebug +
                ']';
    }
}
