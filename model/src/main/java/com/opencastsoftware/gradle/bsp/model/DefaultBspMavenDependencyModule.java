/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.util.Objects;
import java.util.Set;

public class DefaultBspMavenDependencyModule implements BspMavenDependencyModule {
    private final String organization;
    private final String name;
    private final String version;
    private final Set<BspMavenDependencyModuleArtifact> artifacts;

    public DefaultBspMavenDependencyModule(String organization, String name, String version, Set<BspMavenDependencyModuleArtifact> artifacts, String scope) {
        this.organization = organization;
        this.name = name;
        this.version = version;
        this.artifacts = artifacts;
        this.scope = scope;
    }

    private final String scope;

    @Override
    public String organization() {
        return organization;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public Set<BspMavenDependencyModuleArtifact> artifacts() {
        return artifacts;
    }

    @Override
    public String scope() {
        return scope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspMavenDependencyModule that = (DefaultBspMavenDependencyModule) o;
        return Objects.equals(organization, that.organization) && Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(artifacts, that.artifacts) && Objects.equals(scope, that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organization, name, version, artifacts, scope);
    }

    @Override
    public String toString() {
        return "DefaultBspMavenDependencyModule[" +
                "organization='" + organization + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", artifacts=" + artifacts +
                ", scope='" + scope + '\'' +
                ']';
    }
}
