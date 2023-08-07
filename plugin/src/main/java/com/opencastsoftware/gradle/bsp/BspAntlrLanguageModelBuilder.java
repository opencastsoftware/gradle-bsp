/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import org.gradle.api.Project;
import org.gradle.api.plugins.antlr.AntlrSourceDirectorySet;
import org.gradle.api.tasks.SourceSet;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public abstract class BspAntlrLanguageModelBuilder implements BspLanguageModelBuilder {
    private static final String ANTLR_LANGUAGE_ID = "antlr";

    @Override
    public String getLanguageId() {
        return ANTLR_LANGUAGE_ID;
    }

    @Override
    public boolean isEnabledFor(SourceSet sourceSet) {
        return sourceSet.getExtensions().findByType(AntlrSourceDirectorySet.class) != null;
    }

    @Nullable
    @Override
    public String getDisplayNameFor(Project project, SourceSet sourceSet) {
        return sourceSet.getExtensions()
                .getByType(AntlrSourceDirectorySet.class)
                .getDisplayName();
    }

    @Nullable
    @Override
    public String getBuildTargetDataKindFor(Project project, SourceSet sourceSet) {
        return null;
    }

    @Nullable
    @Override
    public Serializable getBuildTargetDataFor(Project project, SourceSet sourceSet) {
        return null;
    }
}
