/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import com.opencastsoftware.gradle.bsp.model.*;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.internal.component.external.descriptor.MavenScope;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BspDependencyModulesToolingModelBuilder extends BspModelBuilder implements ToolingModelBuilder {
    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals(BspDependencyModules.class.getName());
    }

    Set<BspMavenDependencyModuleArtifact> getArtifactsOf(ResolvedDependency dependency) {
        return dependency.getModuleArtifacts().stream()
                .map(artifact -> {
                    var uri = artifact.getFile().toURI();
                    var classifier = artifact.getClassifier();
                    return new DefaultBspMavenDependencyModuleArtifact(uri, classifier);
                })
                .collect(Collectors.toSet());
    }

    BspMavenDependencyModule getMavenDependencyOf(ResolvedDependency dependency, String mavenScope) {
        var group = dependency.getModuleGroup();
        var name = dependency.getModuleName();
        var version = dependency.getModuleVersion();
        var artifacts = getArtifactsOf(dependency);
        return new DefaultBspMavenDependencyModule(group, name, version, artifacts, mavenScope);
    }

    BspDependencyModule getDependencyOf(ResolvedDependency dependency, String mavenScope) {
        var name = dependency.getModuleGroup() + ":" + dependency.getModuleName();
        var version = dependency.getModuleVersion();
        var mavenModule = getMavenDependencyOf(dependency, mavenScope);
        return new DefaultBspDependencyModule(name, version, "maven", mavenModule);
    }

    Stream<BspDependencyModule> getDependenciesOf(Configuration configuration, String mavenScope) {
        if (configuration.isCanBeResolved()) {
            return configuration
                    .getResolvedConfiguration()
                    .getLenientConfiguration()
                    .getAllModuleDependencies()
                    .stream().map(dep -> getDependencyOf(dep, mavenScope));
        } else {
            return Stream.empty();
        }
    }

    Set<BspDependencyModule> getDependenciesOf(Project project, SourceSet sourceSet) {
        var isTestSourceSet = getTestSourceSets(project).contains(sourceSet);

        var providedScope = isTestSourceSet ? MavenScope.Test : MavenScope.Provided;

        var compileOnly = project
                .getConfigurations()
                .findByName(sourceSet.getCompileOnlyConfigurationName());

        var compileOnlyDependencies = Stream
                .ofNullable(compileOnly)
                .flatMap(config -> getDependenciesOf(config, providedScope.getLowerName()))
                .collect(Collectors.toSet());

        var compileScope = isTestSourceSet ? MavenScope.Test : MavenScope.Compile;

        var compileClasspath = project
                .getConfigurations()
                .findByName(sourceSet.getCompileClasspathConfigurationName());

        var compileDependencies = Stream
                .ofNullable(compileClasspath)
                .flatMap(config -> getDependenciesOf(config, compileScope.getLowerName()))
                .filter(compileDep -> compileOnlyDependencies.stream().noneMatch(providedDep -> {
                    return providedDep.name().equals(compileDep.name());
                }))
                .collect(Collectors.toSet());

        var runtimeScope = isTestSourceSet ? MavenScope.Test : MavenScope.Runtime;

        var runtimeClasspath = project
                .getConfigurations()
                .findByName(sourceSet.getRuntimeClasspathConfigurationName());

        var runtimeDependencies = Stream
                .ofNullable(runtimeClasspath)
                .flatMap(config -> getDependenciesOf(config, runtimeScope.getLowerName()))
                .filter(runtimeDep -> compileOnlyDependencies.stream().noneMatch(providedDep -> {
                    return providedDep.name().equals(runtimeDep.name());
                }))
                .filter(runtimeDep -> compileDependencies.stream().noneMatch(compileDep -> {
                    return compileDep.name().equals(runtimeDep.name());
                }))
                .collect(Collectors.toSet());

        return Stream.concat(
                Stream.concat(
                        compileOnlyDependencies.stream(),
                        compileDependencies.stream()),
                runtimeDependencies.stream()
        ).collect(Collectors.toSet());
    }

    @Override
    public BspDependencyModules buildAll(String modelName, Project rootProject) {
        var dependencyModules = new HashMap<URI, Set<BspDependencyModule>>();

        rootProject.getAllprojects().forEach(project -> {
            var projectDependencies = new HashSet<BspDependencyModule>();
            var javaExtension = project.getExtensions().findByType(JavaPluginExtension.class);
            if (javaExtension != null) {
                javaExtension.getSourceSets().forEach(sourceSet -> {
                    var sourceSetTargetId = getBuildTargetIdFor(project, sourceSet).uri();
                    var sourceSetDependencies = getDependenciesOf(project, sourceSet);
                    projectDependencies.addAll(sourceSetDependencies);
                    dependencyModules.put(sourceSetTargetId, sourceSetDependencies);
                });
                var projectTargetId = getBuildTargetIdFor(project).uri();
                dependencyModules.put(projectTargetId, projectDependencies);
            }
        });

        return new DefaultBspDependencyModules(dependencyModules);
    }
}
