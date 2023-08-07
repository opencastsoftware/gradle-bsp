/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import javax.inject.Inject;
import java.util.List;

public class BspPlugin implements Plugin<Project> {
    private final ObjectFactory objectFactory;
    private final ToolingModelBuilderRegistry builderRegistry;
    private static final String JSON_JAVA_MAVEN_COORD = "org.json:json:" + BuildInfo.jsonJavaVersion;
    private static final String BSP_SERVER_MAVEN_COORD = "com.opencastsoftware.gradle:gradle-bsp-server:" + BuildInfo.version;
    private static final List<String> DEFAULT_SUPPORTED_LANGUAGES = List.of("java", "groovy", "scala", "antlr");

    @Inject
    public BspPlugin(ObjectFactory objectFactory, ToolingModelBuilderRegistry builderRegistry) {
        this.objectFactory = objectFactory;
        this.builderRegistry = builderRegistry;
    }

    public void apply(Project project) {
        // Only apply the plugin once
        if (!project.getPlugins().hasPlugin(BuildInfo.pluginId)) {
            var bspExtension = project.getExtensions().create("bsp", DefaultBspExtension.class, objectFactory);
            bspExtension.getSupportedLanguages().addAll(DEFAULT_SUPPORTED_LANGUAGES);
            bspExtension.getLanguageModelBuilders().add(objectFactory.newInstance(BspJavaLanguageModelBuilder.class));
            bspExtension.getLanguageModelBuilders().add(objectFactory.newInstance(BspGroovyLanguageModelBuilder.class));
            bspExtension.getLanguageModelBuilders().add(objectFactory.newInstance(BspScalaLanguageModelBuilder.class));
            bspExtension.getLanguageModelBuilders().add(objectFactory.newInstance(BspAntlrLanguageModelBuilder.class));
            builderRegistry.register(new BspToolingModelBuilder());
            Configuration bspConfig = createBspConfigConfiguration(project);
            Configuration bspServer = createBspServerConfiguration(project);
            registerBspConfigTask(project, bspExtension, bspConfig, bspServer);
        }
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

    private void registerBspConfigTask(Project project, BspExtension bspExtension, Configuration bspConfig, Configuration bspServer) {
        RegularFile outputFile = project.getRootProject()
                .getLayout()
                .getProjectDirectory()
                .dir(".bsp")
                .file("gradle.json");

        project.getTasks().register("bspConfig", GenerateBspConfig.class, task -> {
            task.getTaskClasspath().from(bspConfig);
            task.getBuildServerClasspath().from(bspServer);
            task.getLanguages().set(bspExtension.getSupportedLanguages());
            task.getOutputFile().convention(outputFile);
        });
    }

}
