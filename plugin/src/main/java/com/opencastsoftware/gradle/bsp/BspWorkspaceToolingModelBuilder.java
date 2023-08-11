/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import com.opencastsoftware.gradle.bsp.model.*;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BspWorkspaceToolingModelBuilder extends BspModelBuilder implements ToolingModelBuilder {
    BspCompileTasksToolingModelBuilder compileTasksBuilder;
    BspTestTasksToolingModelBuilder testTasksBuilder;
    BspRunTasksToolingModelBuilder runTasksBuilder;
    BspCleanTasksToolingModelBuilder cleanTasksBuilder;

    public BspWorkspaceToolingModelBuilder() {
       this.compileTasksBuilder = new BspCompileTasksToolingModelBuilder();
       this.testTasksBuilder = new BspTestTasksToolingModelBuilder();
       this.runTasksBuilder = new BspRunTasksToolingModelBuilder();
       this.cleanTasksBuilder = new BspCleanTasksToolingModelBuilder();
    }

    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals(BspWorkspace.class.getName());
    }

    BspBuildTarget buildSourceSetBuildTarget(Project project, BspJvmBuildTarget jvmBuildTarget, List<String> tags, SourceSet sourceSet, List<BspBuildTarget> buildTargets) {
        var languageIds = buildTargets.stream()
                .flatMap(buildTarget -> buildTarget.languageIds().stream())
                .collect(Collectors.toSet());

        if (jvmBuildTarget == null) {
            return new DefaultBspBuildTarget(
                    getBuildTargetIdFor(project, sourceSet),
                    sourceSet.getAllSource().getDisplayName(),
                    getBaseDirectoryFor(project),
                    tags,
                    List.copyOf(languageIds),
                    List.of(),
                    getBuildTargetCapabilitiesFor(project, sourceSet)
            );
        } else {
            return new DefaultBspBuildTarget(
                    getBuildTargetIdFor(project, sourceSet),
                    sourceSet.getAllSource().getDisplayName(),
                    getBaseDirectoryFor(project),
                    tags,
                    List.copyOf(languageIds),
                    List.of(),
                    getBuildTargetCapabilitiesFor(project, sourceSet),
                    "jvm",
                    jvmBuildTarget
            );
        }
    }

    List<BspBuildTargetId> getProjectDependenciesFor(Project project) {
        var projectDependencies = new ArrayList<BspBuildTargetId>();

        var implConfig = project.getConfigurations().findByName("implementation");
        projectDependencies.addAll(getProjectDependenciesFor(implConfig));

        var apiConfig = project.getConfigurations().findByName("api");
        projectDependencies.addAll(getProjectDependenciesFor(apiConfig));

        return projectDependencies;
    }

    List<BspBuildTargetId> getProjectDependenciesFor(Configuration configuration) {
        if (configuration == null) {
            return List.of();
        } else {
            return configuration
                    .getDependencies().stream()
                    .filter(dependency -> dependency instanceof ProjectDependency)
                    .map(dependency -> (ProjectDependency) dependency)
                    .map(projectDependency -> getBuildTargetIdFor(projectDependency.getDependencyProject()))
                    .collect(Collectors.toList());
        }
    }

    BspBuildTarget buildProjectBuildTarget(Project project, BspJvmBuildTarget jvmBuildTarget, List<BspBuildTarget> buildTargets) {
        var languageIds = buildTargets.stream()
                .flatMap(buildTarget -> buildTarget.languageIds().stream())
                .collect(Collectors.toSet());

        if (jvmBuildTarget == null) {
            return new DefaultBspBuildTarget(
                    getBuildTargetIdFor(project),
                    project.getDisplayName(),
                    getBaseDirectoryFor(project),
                    getBuildTargetTagsFor(project),
                    List.copyOf(languageIds),
                    getProjectDependenciesFor(project),
                    getBuildTargetCapabilitiesFor(project)
            );
        } else {
            return new DefaultBspBuildTarget(
                    getBuildTargetIdFor(project),
                    project.getDisplayName(),
                    getBaseDirectoryFor(project),
                    getBuildTargetTagsFor(project),
                    List.copyOf(languageIds),
                    getProjectDependenciesFor(project),
                    getBuildTargetCapabilitiesFor(project),
                    "jvm",
                    jvmBuildTarget
            );
        }

    }

    @Override
    public BspWorkspace buildAll(String modelName, Project rootProject) {
        var compileTasksName = BspCompileTasks.class.getName();
        var testTasksName = BspTestTasks.class.getName();
        var runTasksName = BspRunTasks.class.getName();
        var cleanTasksName = BspCleanTasks.class.getName();

        var compileTasks = compileTasksBuilder.buildAll(compileTasksName, rootProject);
        var testTasks = testTasksBuilder.buildAll(testTasksName, rootProject);
        var runTasks = runTasksBuilder.buildAll(runTasksName, rootProject);
        var cleanTasks = cleanTasksBuilder.buildAll(cleanTasksName, rootProject);

        var buildTargets = new ArrayList<BspBuildTarget>();

        rootProject.getAllprojects().forEach(project -> {
            var bspExtension = project.getExtensions().getByType(BspExtension.class);
            var supportedLanguages = bspExtension.getSupportedLanguages();

            var javaExtension = project.getExtensions().findByType(JavaPluginExtension.class);
            if (javaExtension != null) {
                var jvmBuildTarget = getJvmBuildTargetFor(project);

                javaExtension.getSourceSets().forEach(sourceSet -> {
                    var sourceSetBuildTargets = new ArrayList<BspBuildTarget>();
                    var sourceSetTags = getBuildTargetTagsFor(project, sourceSet);

                    bspExtension.getLanguageModelBuilders().get().forEach(modelBuilder -> {
                        var isSupportedLanguage = supportedLanguages.get()
                                .contains(modelBuilder.getLanguageId());

                        if (isSupportedLanguage) {
                            Optional.ofNullable(modelBuilder.getBuildTargetFor(project, sourceSet))
                                    .ifPresent(sourceSetBuildTargets::add);
                        }
                    });

                    var sourceSetBuildTarget = buildSourceSetBuildTarget(project, jvmBuildTarget, sourceSetTags, sourceSet, sourceSetBuildTargets);
                    sourceSetBuildTargets.add(sourceSetBuildTarget);

                    buildTargets.addAll(sourceSetBuildTargets);
                });

                var projectBuildTarget = buildProjectBuildTarget(project, jvmBuildTarget, buildTargets);
                buildTargets.add(projectBuildTarget);
            }
        });

        return new DefaultBspWorkspace(buildTargets, compileTasks, testTasks, runTasks, cleanTasks);
    }
}
