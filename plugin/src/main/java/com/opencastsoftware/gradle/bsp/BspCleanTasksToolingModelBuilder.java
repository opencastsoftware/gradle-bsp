/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import com.opencastsoftware.gradle.bsp.model.BspCleanTasks;
import com.opencastsoftware.gradle.bsp.model.DefaultBspCleanTasks;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.language.base.internal.plugins.CleanRule;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Optional;

public class BspCleanTasksToolingModelBuilder extends BspModelBuilder implements ToolingModelBuilder {
    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals(BspCleanTasks.class.getName());
    }

    private String capitalise(String taskName) {
        return Character.toTitleCase(taskName.charAt(0)) + taskName.substring(1);
    }

    @Override
    public BspCleanTasks buildAll(String modelName, Project rootProject) {
        var cleanTasks = new HashMap<URI, String>();

        rootProject.getAllprojects().forEach(project -> {
            var javaExtension = project.getExtensions().findByType(JavaPluginExtension.class);
            if (javaExtension != null) {
                javaExtension.getSourceSets().forEach(sourceSet -> {
                    var sourceSetTargetId = getBuildTargetIdFor(project, sourceSet).uri();
                    var classesTaskName = sourceSet.getClassesTaskName();
                    var classesTask = project.getTasks().findByName(classesTaskName);
                    Optional.ofNullable(classesTask).ifPresent(classes -> {
                        var cleanTaskName = CleanRule.CLEAN + capitalise(classesTaskName);
                        var cleanTask = project.getTasks().findByName(cleanTaskName);
                        Optional.ofNullable(cleanTask).ifPresent(clean -> {
                            cleanTasks.put(sourceSetTargetId, clean.getPath());
                        });
                    });
                });
                var projectTargetId = getBuildTargetIdFor(project).uri();
                var classesTask = project.getTasks().findByName(LifecycleBasePlugin.CLEAN_TASK_NAME);
                Optional.ofNullable(classesTask).ifPresent(task -> cleanTasks.put(projectTargetId, task.getPath()));
            }
        });

        return new DefaultBspCleanTasks(cleanTasks);
    }
}
