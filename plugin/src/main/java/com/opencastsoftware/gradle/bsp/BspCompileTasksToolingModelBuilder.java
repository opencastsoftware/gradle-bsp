/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import com.opencastsoftware.gradle.bsp.model.BspCompileTasks;
import com.opencastsoftware.gradle.bsp.model.DefaultBspCompileTasks;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Optional;

public class BspCompileTasksToolingModelBuilder extends BspModelBuilder implements ToolingModelBuilder {
    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals(BspCompileTasks.class.getName());
    }

    @Override
    public BspCompileTasks buildAll(String modelName, Project rootProject) {
        var compileTasks = new HashMap<URI, String>();

        rootProject.getAllprojects().forEach(project -> {
            var javaExtension = project.getExtensions().findByType(JavaPluginExtension.class);
            if (javaExtension != null) {
                javaExtension.getSourceSets().forEach(sourceSet -> {
                    var sourceSetTargetId = getBuildTargetIdFor(project, sourceSet).uri();
                    var classesTask = project.getTasks().findByName(sourceSet.getClassesTaskName());
                    Optional.ofNullable(classesTask).ifPresent(task -> compileTasks.put(sourceSetTargetId, task.getPath()));
                });
                var projectTargetId = getBuildTargetIdFor(project).uri();
                var classesTask = project.getTasks().findByName(JavaPlugin.CLASSES_TASK_NAME);
                Optional.ofNullable(classesTask).ifPresent(task -> compileTasks.put(projectTargetId, task.getPath()));
            }
        });

        return new DefaultBspCompileTasks(compileTasks);
    }
}
