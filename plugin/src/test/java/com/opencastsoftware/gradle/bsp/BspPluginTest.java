/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class BspPluginTest {
    @Test
    void registersBspConfigTask() {
        var project = ProjectBuilder.builder().build();
        project.getPlugins().apply("com.opencastsoftware.gradle.bsp");
        assertThat(project.getTasks().getByName("bspConfig"), is(notNullValue()));
    }

    @Test
    void registersBspConfigConfiguration() {
        var project = ProjectBuilder.builder().build();
        project.getPlugins().apply("com.opencastsoftware.gradle.bsp");
        assertThat(project.getConfigurations().getByName("bspConfig"), is(notNullValue()));
    }

    @Test
    void registersBspServerConfiguration() {
        var project = ProjectBuilder.builder().build();
        project.getPlugins().apply("com.opencastsoftware.gradle.bsp");
        assertThat(project.getConfigurations().getByName("bspServer"), is(notNullValue()));
    }

    @Test
    void registersBspExtension() {
        var project = ProjectBuilder.builder().build();
        project.getPlugins().apply("com.opencastsoftware.gradle.bsp");
        var extensionByType = project.getExtensions().getByType(BspExtension.class);
        assertThat(extensionByType, is(notNullValue()));
        var extensionByName = project.getExtensions().getByName("bsp");
        assertThat(extensionByName, is(notNullValue()));
        assertThat(extensionByName, isA(BspExtension.class));
    }

    @Test
    void registersSupportedLanguages() {
        var project = ProjectBuilder.builder().build();
        project.getPlugins().apply("com.opencastsoftware.gradle.bsp");
        var extension = project.getExtensions().getByType(BspExtension.class);
        assertThat(extension.getSupportedLanguages().get(), containsInAnyOrder("java", "groovy", "scala", "antlr"));
    }

    @Test
    void registersBspLanguageModelBuilders() {
        var project = ProjectBuilder.builder().build();
        project.getPlugins().apply("com.opencastsoftware.gradle.bsp");
        var extension = project.getExtensions().getByType(BspExtension.class);
        assertThat(extension.getLanguageModelBuilders().get(), containsInAnyOrder(
                isA(BspJavaLanguageModelBuilder.class),
                isA(BspGroovyLanguageModelBuilder.class),
                isA(BspScalaLanguageModelBuilder.class),
                isA(BspAntlrLanguageModelBuilder.class)
        ));
    }
}
