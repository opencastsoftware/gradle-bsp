/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.net.URI;
import java.util.Objects;

public class DefaultBspBuildTargetId implements BspBuildTargetId {
    private final URI uri;

    public DefaultBspBuildTargetId(URI uri) {
        this.uri = uri;
    }

    @Override
    public URI uri() {
        return this.uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspBuildTargetId that = (DefaultBspBuildTargetId) o;
        return Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri);
    }

    @Override
    public String toString() {
        return "DefaultBspBuildTargetId[" +
                "uri=" + uri +
                ']';
    }
}
