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

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BspBuildTargetResourcesToolingModelBuilder extends BspModelBuilder implements ToolingModelBuilder {
    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals(BspBuildTargetResources.class.getName());
    }

    private Set<URI> getResourcesFor(SourceDirectorySet srcDirSet){
       return srcDirSet.getSrcDirs().stream()
               .map(File::toURI)
               .collect(Collectors.toSet());
    }

    @Override
    public BspBuildTargetResources buildAll(String modelName, Project rootProject) {
        var resources = new HashMap<URI, Set<URI>>();

        rootProject.getAllprojects().forEach(project -> {
            var javaExtension = project.getExtensions().findByType(JavaPluginExtension.class);
            if (javaExtension != null) {
                var projectResources = new HashSet<URI>();

                javaExtension.getSourceSets().forEach(sourceSet -> {
                    var sourceSetTargetId = getBuildTargetIdFor(project, sourceSet).uri();
                    var sourceSetResourceItems = getResourcesFor(sourceSet.getResources());
                    resources.put(sourceSetTargetId, sourceSetResourceItems);
                    projectResources.addAll(sourceSetResourceItems);
                });

                var projectTargetId = getBuildTargetIdFor(project).uri();
                resources.put(projectTargetId, projectResources);
            }
        });

        return new DefaultBspBuildTargetResources(resources);
    }
}
