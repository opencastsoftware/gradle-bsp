/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import com.opencastsoftware.gradle.bsp.model.*;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.maven.MavenPomScm;
import org.gradle.api.tasks.SourceSet;
import org.gradle.internal.component.external.descriptor.MavenScope;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BspDependencyModulesToolingModelBuilder extends BspModelBuilder implements ToolingModelBuilder {
    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals(BspDependencyModules.class.getName());
    }

    String getScopeOf(Configuration configuration, SourceSet sourceSet) {
        return MavenScope.Compile.getLowerName();
    }

    Set<BspMavenDependencyModuleArtifact> getArtifactsOf(ResolvedDependency dependency) {
        return dependency.getModuleArtifacts().stream()
                .map(artifact -> {
                    var uri = artifact.getFile().toURI().toString();
                    var classifier = artifact.getClassifier();
                    return new DefaultBspMavenDependencyModuleArtifact(uri, classifier);
                })
                .collect(Collectors.toSet());
    }

    BspMavenDependencyModule getMavenDependencyOf(Configuration configuration, SourceSet sourceSet, ResolvedDependency dependency) {
        var group = dependency.getModuleGroup();
        var name = dependency.getModuleName();
        var version = dependency.getModuleVersion();
        var artifacts = getArtifactsOf(dependency);
        var scope = getScopeOf(configuration, sourceSet);
        return new DefaultBspMavenDependencyModule(group, name, version, artifacts, scope);
    }

    Set<BspDependencyModule> getDependenciesOf(Configuration configuration, SourceSet sourceSet) {
        if (configuration.isCanBeResolved()) {
            return configuration
                    .getResolvedConfiguration()
                    .getFirstLevelModuleDependencies()
                    .stream().map(dep -> {
                        var name = dep.getModuleName();
                        var version = dep.getModuleVersion();
                        var mavenModule = getMavenDependencyOf(configuration, sourceSet, dep);
                        return new DefaultBspDependencyModule(name, version, "maven", mavenModule);
                    }).collect(Collectors.toSet());
        } else {
            return Set.of();
        }
    }

    @Override
    public BspDependencyModules buildAll(String modelName, Project rootProject) {
        var dependencyModules = new HashMap<String, Set<BspDependencyModule>>();

        rootProject.getAllprojects().forEach(project -> {
            var javaExtension = project.getExtensions().findByType(JavaPluginExtension.class);
            if (javaExtension != null) {
                javaExtension.getSourceSets().forEach(sourceSet -> {
                    var sourceSetTargetId = getBuildTargetIdFor(project, sourceSet).uri().toString();
                });
                var projectTargetId = getBuildTargetIdFor(project).uri().toString();
            }
        });

        return new DefaultBspDependencyModules(dependencyModules);
    }
}
