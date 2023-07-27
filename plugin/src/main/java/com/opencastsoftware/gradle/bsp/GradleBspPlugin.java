/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import com.opencastsoftware.gradle.bsp.model.BspWorkspace;
import com.opencastsoftware.gradle.bsp.model.DefaultBspWorkspace;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.RegularFile;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class GradleBspPlugin implements Plugin<Project> {
    private final ToolingModelBuilderRegistry builderRegistry;
    private static final String JSON_JAVA_MAVEN_COORD = "org.json:json:" + BuildInfo.jsonJavaVersion;
    private static final String BSP_SERVER_MAVEN_COORD = "com.opencastsoftware.gradle:gradle-bsp-server:" + BuildInfo.version;

    @Inject
    public GradleBspPlugin(ToolingModelBuilderRegistry builderRegistry) {
        this.builderRegistry = builderRegistry;
    }

    public void apply(Project project) {
        builderRegistry.register(new BspWorkspaceModelBuilder());
        Configuration bspConfig = createBspConfigConfiguration(project);
        Configuration bspServer = createBspServerConfiguration(project);
        List<String> languages = getLanguages(project);
        registerBspConfigTask(project, languages, bspConfig, bspServer);
    }

    private Configuration createBspConfigConfiguration(Project project) {
        return project.getConfigurations().create("bspConfig", config -> {
            config.setVisible(false);
            config.setCanBeConsumed(false);
            config.setCanBeResolved(true);
            config.setDescription(
                    "Dependencies of the gradle-bsp plugin connection file generation task");
            config.defaultDependencies(
                    deps -> deps.add(project.getDependencies().create(JSON_JAVA_MAVEN_COORD)));
        });
    }

    private Configuration createBspServerConfiguration(Project project) {
        return project.getConfigurations().create("bspServer", config -> {
            config.setVisible(false);
            config.setCanBeConsumed(false);
            config.setCanBeResolved(true);
            config.setDescription(
                    "Dependencies of the gradle-bsp build server");
            config.defaultDependencies(
                    deps -> deps.add(project.getDependencies().create(BSP_SERVER_MAVEN_COORD)));
        });
    }

    private List<String> getLanguages(Project project) {
        List<String> languages = new ArrayList<>();
        languages.add("java");
        languages.add("groovy");
        languages.add("scala");
        languages.add("antlr");
        return languages;
    }

    private void registerBspConfigTask(Project project, List<String> languages, Configuration bspConfig, Configuration bspServer) {
        RegularFile outputFile = project.getRootProject()
                .getLayout()
                .getProjectDirectory()
                .dir(".bsp")
                .file("gradle.json");

        project.getTasks().register("bspConfig", GenerateBspConfig.class, task -> {
            task.getTaskClasspath().from(bspConfig);
            task.getBuildServerClasspath().from(bspServer);
            task.getLanguages().convention(languages);
            task.getOutputFile().convention(outputFile);
        });
    }

    private static class BspWorkspaceModelBuilder implements ToolingModelBuilder {

        @Override
        public boolean canBuild(String modelName) {
            return modelName.equals(BspWorkspace.class.getName());
        }

        @Override
        public BspWorkspace buildAll(String modelName, Project project) {
            return new DefaultBspWorkspace();
        }
    }
}
