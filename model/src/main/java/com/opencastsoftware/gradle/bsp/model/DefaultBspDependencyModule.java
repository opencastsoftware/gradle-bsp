/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.io.Serializable;
import java.util.Objects;

public class DefaultBspDependencyModule implements BspDependencyModule {
    private final String name;
    private final String version;
    private final String dataKind;
    private final Serializable data;

    public DefaultBspDependencyModule(String name, String version) {
        this.name = name;
        this.version = version;
        this.dataKind = null;
        this.data = null;
    }

    public DefaultBspDependencyModule(String name, String version, String dataKind, Serializable data) {
        this.name = name;
        this.version = version;
        this.dataKind = dataKind;
        this.data = data;
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
    public String dataKind() {
        return dataKind;
    }

    @Override
    public Serializable data() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspDependencyModule that = (DefaultBspDependencyModule) o;
        return Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(dataKind, that.dataKind) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, dataKind, data);
    }

    @Override
    public String toString() {
        return "DefaultBspDependencyModule[" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", dataKind='" + dataKind + '\'' +
                ", data=" + data +
                ']';
    }
}
