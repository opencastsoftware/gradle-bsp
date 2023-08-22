/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Objects;

public class DefaultBspBuildTarget implements BspBuildTarget {
    private final BspBuildTargetId id;
    private final String displayName;
    private final URI baseDirectory;
    private final List<String> tags;
    private final List<String> languageIds;
    private final List<BspBuildTargetId> dependencies;
    private final BspBuildTargetCapabilities capabilities;
    private final String dataKind;
    private final Serializable data;

    public DefaultBspBuildTarget(BspBuildTargetId id, String displayName, URI baseDirectory, List<String> tags, List<String> languageIds, List<BspBuildTargetId> dependencies, BspBuildTargetCapabilities capabilities) {
        this.id = id;
        this.displayName = displayName;
        this.baseDirectory = baseDirectory;
        this.tags = tags;
        this.languageIds = languageIds;
        this.dependencies = dependencies;
        this.capabilities = capabilities;
        this.dataKind = null;
        this.data = null;
    }

    public DefaultBspBuildTarget(BspBuildTargetId id, String displayName, URI baseDirectory, List<String> tags, List<String> languageIds, List<BspBuildTargetId> dependencies, BspBuildTargetCapabilities capabilities, String dataKind, Serializable data) {
       this.id = id;
       this.displayName = displayName;
       this.baseDirectory = baseDirectory;
       this.tags = tags;
       this.languageIds = languageIds;
       this.dependencies = dependencies;
       this.capabilities = capabilities;
       this.dataKind = dataKind;
       this.data = data;
    }

    @Override
    public BspBuildTargetId id() {
        return this.id;
    }

    @Override
    public String displayName() {
        return this.displayName;
    }

    @Override
    public URI baseDirectory() {
        return this.baseDirectory;
    }

    @Override
    public List<String> tags() {
        return this.tags;
    }

    @Override
    public List<String> languageIds() {
        return this.languageIds;
    }

    @Override
    public List<BspBuildTargetId> dependencies() {
        return this.dependencies;
    }

    @Override
    public BspBuildTargetCapabilities capabilities() {
        return this.capabilities;
    }

    @Override
    public String dataKind() {
        return this.dataKind;
    }

    @Override
    public Serializable data() {
        return this.data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspBuildTarget that = (DefaultBspBuildTarget) o;
        return Objects.equals(id, that.id) && Objects.equals(displayName, that.displayName) && Objects.equals(baseDirectory, that.baseDirectory) && Objects.equals(tags, that.tags) && Objects.equals(languageIds, that.languageIds) && Objects.equals(dependencies, that.dependencies) && Objects.equals(capabilities, that.capabilities) && Objects.equals(dataKind, that.dataKind) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, baseDirectory, tags, languageIds, dependencies, capabilities, dataKind, data);
    }

    @Override
    public String toString() {
        return "DefaultBspBuildTarget[" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                ", baseDirectory=" + baseDirectory +
                ", tags=" + tags +
                ", languageIds=" + languageIds +
                ", dependencies=" + dependencies +
                ", capabilities=" + capabilities +
                ", dataKind='" + dataKind + '\'' +
                ", data=" + data +
                ']';
    }
}
