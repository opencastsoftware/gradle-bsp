/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.net.URI;
import java.util.Objects;

public class DefaultBspSourceItem implements BspSourceItem {
    private final URI uri;
    private final Boolean generated;

    public DefaultBspSourceItem(URI uri) {
        this.uri = uri;
        this.generated = Boolean.FALSE;
    }

    public DefaultBspSourceItem(URI uri, Boolean generated) {
        this.uri = uri;
        this.generated = generated;
    }

    @Override
    public URI uri() {
        return uri;
    }

    @Override
    public Boolean generated() {
        return generated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspSourceItem that = (DefaultBspSourceItem) o;
        return Objects.equals(uri, that.uri) && Objects.equals(generated, that.generated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, generated);
    }

    @Override
    public String toString() {
        return "DefaultBspSourceItem[" +
                "uri=" + uri +
                ", generated=" + generated +
                ']';
    }
}
