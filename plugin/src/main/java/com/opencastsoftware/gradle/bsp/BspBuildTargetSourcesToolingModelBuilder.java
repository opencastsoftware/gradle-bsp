/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import com.opencastsoftware.gradle.bsp.model.BspBuildTargetSources;
import com.opencastsoftware.gradle.bsp.model.BspSourceItem;
import com.opencastsoftware.gradle.bsp.model.DefaultBspBuildTargetSources;
import com.opencastsoftware.gradle.bsp.model.DefaultBspSourceItem;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BspBuildTargetSourcesToolingModelBuilder extends BspModelBuilder implements ToolingModelBuilder {
    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals(BspBuildTargetSources.class.getName());
    }

    private Set<BspSourceItem> getSourceItemsFor(Path buildDir, SourceDirectorySet srcDirSet){
       return srcDirSet.getSrcDirs().stream().map(srcDir -> {
           var isInBuildDir = srcDir.toPath().startsWith(buildDir);
           return (BspSourceItem) new DefaultBspSourceItem(srcDir.toURI(), isInBuildDir);
       }).collect(Collectors.toSet());
    }

    @Override
    public BspBuildTargetSources buildAll(String modelName, Project rootProject) {
        var sources = new HashMap<URI, Set<BspSourceItem>>();

        rootProject.getAllprojects().forEach(project -> {
            var bspExtension = project.getExtensions().getByType(BspExtension.class);
            var supportedLanguages = bspExtension.getSupportedLanguages();
            var buildDir = project.getLayout().getBuildDirectory().getAsFile().get().toPath();

            var javaExtension = project.getExtensions().findByType(JavaPluginExtension.class);
            if (javaExtension != null) {
                var projectSources = new HashSet<BspSourceItem>();

                javaExtension.getSourceSets().forEach(sourceSet -> {
                    bspExtension.getLanguageModelBuilders().get().forEach(modelBuilder -> {
                        var isSupportedLanguage = supportedLanguages.get()
                                .contains(modelBuilder.getLanguageId());
                        if (isSupportedLanguage && modelBuilder.isEnabledFor(sourceSet)) {
                            var sourceDirSetTargetId = modelBuilder.getBuildTargetIdFor(project, sourceSet).uri();
                            var sourceDirSet = modelBuilder.getSourceDirectorySetFor(sourceSet);
                            var sourceDirSetSourceItems = getSourceItemsFor(buildDir, sourceDirSet);
                            sources.put(sourceDirSetTargetId, sourceDirSetSourceItems);
                        }
                    });

                    var sourceSetTargetId = getBuildTargetIdFor(project, sourceSet).uri();
                    var sourceSetSourceItems = getSourceItemsFor(buildDir, sourceSet.getAllSource());
                    sources.put(sourceSetTargetId, sourceSetSourceItems);
                    projectSources.addAll(sourceSetSourceItems);
                });

                var projectTargetId = getBuildTargetIdFor(project).uri();
                sources.put(projectTargetId, projectSources);
            }
        });

        return new DefaultBspBuildTargetSources(sources);
    }
}
