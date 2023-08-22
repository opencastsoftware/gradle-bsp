/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import com.opencastsoftware.gradle.bsp.model.BspRunTasks;
import com.opencastsoftware.gradle.bsp.model.DefaultBspRunTasks;
import org.gradle.api.Project;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaApplication;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Optional;

public class BspRunTasksToolingModelBuilder extends BspModelBuilder implements ToolingModelBuilder {
    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals(BspRunTasks.class.getName());
    }

    @Override
    public BspRunTasks buildAll(String modelName, Project rootProject) {
        var runTasks = new HashMap<URI, String>();

        rootProject.getAllprojects().forEach(project -> {
            var javaExtension = project.getExtensions().findByType(JavaPluginExtension.class);
            var applicationExtension = project.getExtensions().findByType(JavaApplication.class);
            if (javaExtension != null && applicationExtension != null) {
                var projectTargetId = getBuildTargetIdFor(project).uri();
                var runTask = project.getTasks().findByName(ApplicationPlugin.TASK_RUN_NAME);
                Optional.ofNullable(runTask).ifPresent(task -> runTasks.put(projectTargetId, task.getPath()));
            }
        });

        return new DefaultBspRunTasks(runTasks);
    }
}
