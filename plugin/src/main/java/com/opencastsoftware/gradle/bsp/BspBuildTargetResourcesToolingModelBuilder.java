/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import com.opencastsoftware.gradle.bsp.model.*;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BspBuildTargetResourcesToolingModelBuilder extends BspModelBuilder implements ToolingModelBuilder {
    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals(BspBuildTargetResources.class.getName());
    }

    private Set<String> getResourcesFor(SourceDirectorySet srcDirSet){
       return srcDirSet.getSrcDirs().stream()
               .map(srcDir -> srcDir.toURI().toString())
               .collect(Collectors.toSet());
    }

    @Override
    public BspBuildTargetResources buildAll(String modelName, Project rootProject) {
        var resources = new HashMap<String, Set<String>>();

        rootProject.getAllprojects().forEach(project -> {
            var javaExtension = project.getExtensions().findByType(JavaPluginExtension.class);
            if (javaExtension != null) {
                var projectResources = new HashSet<String>();

                javaExtension.getSourceSets().forEach(sourceSet -> {
                    var sourceSetTargetId = getBuildTargetIdFor(project, sourceSet).uri().toString();
                    var sourceSetResourceItems = getResourcesFor(sourceSet.getResources());
                    resources.put(sourceSetTargetId, sourceSetResourceItems);
                    projectResources.addAll(sourceSetResourceItems);
                });

                var projectTargetId = getBuildTargetIdFor(project).uri().toString();
                resources.put(projectTargetId, projectResources);
            }
        });

        return new DefaultBspBuildTargetResources(resources);
    }
}
