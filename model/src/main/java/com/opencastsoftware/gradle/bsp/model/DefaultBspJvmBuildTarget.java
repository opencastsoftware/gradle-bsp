/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.net.URI;
import java.util.Objects;

public class DefaultBspJvmBuildTarget implements BspJvmBuildTarget {
    private final URI javaHome;
    private final String javaVersion;

    public DefaultBspJvmBuildTarget(URI javaHome, String javaVersion) {
        this.javaHome = javaHome;
        this.javaVersion = javaVersion;
    }

    @Override
    public URI javaHome() {
        return this.javaHome;
    }

    @Override
    public String javaVersion() {
        return this.javaVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspJvmBuildTarget that = (DefaultBspJvmBuildTarget) o;
        return Objects.equals(javaHome, that.javaHome) && Objects.equals(javaVersion, that.javaVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(javaHome, javaVersion);
    }

    @Override
    public String toString() {
        return "DefaultBspJvmBuildTarget[" +
                "javaHome=" + javaHome +
                ", javaVersion='" + javaVersion + '\'' +
                ']';
    }
}
