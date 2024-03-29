/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.SourceSet;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public abstract class BspJavaLanguageModelBuilder extends BspLanguageModelBuilder {
    private static final String JAVA_LANGUAGE_ID = "java";

    public String getLanguageId() {
        return JAVA_LANGUAGE_ID;
    }

    @Override
    public boolean isEnabledFor(SourceSet sourceSet) {
        return true;
    }

    @Override
    protected SourceDirectorySet getSourceDirectorySetFor(SourceSet sourceSet) {
        return sourceSet.getJava();
    }

    @Nullable
    @Override
    protected String getBuildTargetDataKindFor(Project project, SourceSet sourceSet) {
        return "jvm";
    }

    @Nullable
    @Override
    protected Serializable getBuildTargetDataFor(Project project, SourceSet sourceSet) {
        return getJvmBuildTargetFor(project);
    }
}
