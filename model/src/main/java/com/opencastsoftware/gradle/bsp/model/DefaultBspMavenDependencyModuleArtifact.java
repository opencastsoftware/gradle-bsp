/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.net.URI;
import java.util.Objects;

public class DefaultBspMavenDependencyModuleArtifact implements BspMavenDependencyModuleArtifact {
    private final URI uri;
    private final String classifier;

    public DefaultBspMavenDependencyModuleArtifact(URI uri, String classifier) {
        this.uri = uri;
        this.classifier = classifier;
    }

    @Override
    public URI uri() {
        return uri;
    }

    @Override
    public String classifier() {
        return classifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspMavenDependencyModuleArtifact that = (DefaultBspMavenDependencyModuleArtifact) o;
        return Objects.equals(uri, that.uri) && Objects.equals(classifier, that.classifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, classifier);
    }

    @Override
    public String toString() {
        return "DefaultBspMavenDependencyModuleArtifact[" +
                "uri=" + uri +
                ", classifier='" + classifier + '\'' +
                ']';
    }
}
