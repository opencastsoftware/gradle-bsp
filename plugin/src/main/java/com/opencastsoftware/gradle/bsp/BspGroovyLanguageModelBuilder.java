/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import org.gradle.api.Project;
import org.gradle.api.tasks.GroovySourceDirectorySet;
import org.gradle.api.tasks.SourceSet;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public abstract class BspGroovyLanguageModelBuilder extends BspLanguageModelBuilder {
    private static final String GROOVY_LANGUAGE_ID = "groovy";

    @Override
    public String getLanguageId() {
        return GROOVY_LANGUAGE_ID;
    }

    @Override
    public boolean isEnabledFor(SourceSet sourceSet) {
        return sourceSet.getExtensions().findByType(GroovySourceDirectorySet.class) != null;
    }

    @Nullable
    @Override
    String getDisplayNameFor(Project project, SourceSet sourceSet) {
        return sourceSet.getExtensions()
                .getByType(GroovySourceDirectorySet.class)
                .getDisplayName();
    }

    @Nullable
    @Override
    String getBuildTargetDataKindFor(Project project, SourceSet sourceSet) {
        return "jvm";
    }

    @Nullable
    @Override
    Serializable getBuildTargetDataFor(Project project, SourceSet sourceSet) {
        return getJvmBuildTargetFor(project);
    }
}
