/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import com.opencastsoftware.gradle.bsp.model.*;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

public abstract class BspLanguageModelBuilder extends BspModelBuilder {

    public abstract String getLanguageId();

    public abstract boolean isEnabledFor(SourceSet sourceSet);

    @Nullable
    protected abstract String getDisplayNameFor(Project project, SourceSet sourceSet);

    @Override
    protected BspBuildTargetId getBuildTargetIdFor(Project project, SourceSet sourceSet) {
        var sourceSetName = sourceSet.getTaskName(null, getLanguageId());
        var buildTargetUri = project.getProjectDir().toURI().resolve("?sourceSet=" + sourceSetName);
        return new DefaultBspBuildTargetId(buildTargetUri);
    }

    @Nullable
    protected abstract String getBuildTargetDataKindFor(Project project, SourceSet sourceSet);

    @Nullable
    protected abstract Serializable getBuildTargetDataFor(Project project, SourceSet sourceSet);

    public @Nullable BspBuildTarget getBuildTargetFor(Project project, SourceSet sourceSet) {
        if (!isEnabledFor(sourceSet)) {
            return null;
        }

        var data = getBuildTargetDataFor(project, sourceSet);

        if (data == null) {
            return new DefaultBspBuildTarget(
                    getBuildTargetIdFor(project, sourceSet),
                    getDisplayNameFor(project, sourceSet),
                    getBaseDirectoryFor(project),
                    getBuildTargetTagsFor(project, sourceSet),
                    List.of(getLanguageId()),
                    List.of(),
                    getBuildTargetCapabilitiesFor(project, sourceSet)
            );
        } else {
            return new DefaultBspBuildTarget(
                    getBuildTargetIdFor(project, sourceSet),
                    getDisplayNameFor(project, sourceSet),
                    getBaseDirectoryFor(project),
                    getBuildTargetTagsFor(project, sourceSet),
                    List.of(getLanguageId()),
                    List.of(),
                    getBuildTargetCapabilitiesFor(project, sourceSet),
                    getBuildTargetDataKindFor(project, sourceSet),
                    data
            );
        }
    }
}
