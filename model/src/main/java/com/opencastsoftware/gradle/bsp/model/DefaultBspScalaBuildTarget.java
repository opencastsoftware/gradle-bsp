/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.net.URI;
import java.util.Arrays;
import java.util.Objects;

public class DefaultBspScalaBuildTarget implements BspScalaBuildTarget {
    private final String scalaOrganization;
    private final String scalaVersion;
    private final String scalaBinaryVersion;
    private final BspScalaPlatform scalaPlatform;
    private final URI[] jars;
    private final BspJvmBuildTarget jvmBuildTarget;

    public DefaultBspScalaBuildTarget(String scalaOrganization, String scalaVersion, String scalaBinaryVersion, BspScalaPlatform scalaPlatform, URI[] jars, BspJvmBuildTarget jvmBuildTarget) {
        this.scalaOrganization = scalaOrganization;
        this.scalaVersion = scalaVersion;
        this.scalaBinaryVersion = scalaBinaryVersion;
        this.scalaPlatform = scalaPlatform;
        this.jars = jars;
        this.jvmBuildTarget = jvmBuildTarget;
    }

    public String scalaOrganization() {
        return scalaOrganization;
    }

    public String scalaVersion() {
        return scalaVersion;
    }

    public String scalaBinaryVersion() {
        return scalaBinaryVersion;
    }

    public BspScalaPlatform scalaPlatform() {
        return scalaPlatform;
    }

    public URI[] jars() {
        return jars;
    }

    public BspJvmBuildTarget jvmBuildTarget() {
        return jvmBuildTarget;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspScalaBuildTarget that = (DefaultBspScalaBuildTarget) o;
        return Objects.equals(scalaOrganization, that.scalaOrganization) && Objects.equals(scalaVersion, that.scalaVersion) && Objects.equals(scalaBinaryVersion, that.scalaBinaryVersion) && scalaPlatform == that.scalaPlatform && Arrays.equals(jars, that.jars) && Objects.equals(jvmBuildTarget, that.jvmBuildTarget);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(scalaOrganization, scalaVersion, scalaBinaryVersion, scalaPlatform, jvmBuildTarget);
        result = 31 * result + Arrays.hashCode(jars);
        return result;
    }

    @Override
    public String toString() {
        return "DefaultBspScalaBuildTarget[" +
                "scalaOrganization='" + scalaOrganization + '\'' +
                ", scalaVersion='" + scalaVersion + '\'' +
                ", scalaBinaryVersion='" + scalaBinaryVersion + '\'' +
                ", scalaPlatform=" + scalaPlatform +
                ", jars=" + Arrays.toString(jars) +
                ", jvmBuildTarget=" + jvmBuildTarget +
                ']';
    }
}
