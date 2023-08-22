/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import com.opencastsoftware.gradle.bsp.model.BspSourceItem;
import com.opencastsoftware.gradle.bsp.model.BspWorkspace;
import com.opencastsoftware.gradle.bsp.model.DefaultBspSourceItem;
import org.gradle.internal.classpath.DefaultClassPath;
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.internal.consumer.DefaultModelBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ToolingModelTest {
    private File getBuildFile(File projectDir) {
        return new File(projectDir, "build.gradle");
    }

    private File getSettingsFile(File projectDir) {
        return new File(projectDir, "settings.gradle");
    }

    private URI getBuildTargetId(File projectDir) throws IOException {
        return projectDir.toPath().toRealPath().toUri();
    }

    private URI getBuildTargetId(File projectDir, String sourceSet) throws IOException {
        return projectDir.toPath().toRealPath().toUri().resolve("?sourceSet=" + sourceSet);
    }

    private URI getSourceUri(File projectDir, String... sourceDirs) throws IOException {
        var sourcePath = projectDir.toPath().toRealPath();
        for (var sourceDir : sourceDirs) {
            sourcePath = sourcePath.resolve(sourceDir);
        }
        return sourcePath.toUri();
    }

    private BspWorkspace fetchWorkspaceModel(ProjectConnection connection) {
        var builder = (DefaultModelBuilder<BspWorkspace>) connection.model(BspWorkspace.class);
        var pluginClasspath = PluginUnderTestMetadataReading.readImplementationClasspath();
        builder.withInjectedClassPath(DefaultClassPath.of(pluginClasspath));
        return builder.get();
    }

    @Test
    void registersWorkspaceModel(@TempDir File projectDir) throws IOException {
        writeString(getSettingsFile(projectDir), "");
        writeString(getBuildFile(projectDir),
                "plugins {\n" +
                        "  id('com.opencastsoftware.gradle.bsp')\n" +
                        "}");

        var connector = GradleConnector.newConnector()
                .forProjectDirectory(projectDir)
                .useBuildDistribution();

        try (var connection = connector.connect()) {
            var workspace = fetchWorkspaceModel(connection);
            assertThat(workspace, is(notNullValue()));
            assertThat(workspace.buildTargets(), is(empty()));
        }
    }

    @Test
    void returnsJavaBuildTargets(@TempDir File projectDir) throws IOException {
        writeString(getSettingsFile(projectDir), "");
        writeString(getBuildFile(projectDir),
                "plugins {\n" +
                        "  id('java')\n" +
                        "  id('com.opencastsoftware.gradle.bsp')\n" +
                        "}");

        var connector = GradleConnector.newConnector()
                .forProjectDirectory(projectDir)
                .useBuildDistribution();

        try (var connection = connector.connect()) {
            var workspace = fetchWorkspaceModel(connection);
            assertThat(workspace, is(notNullValue()));

            assertThat(workspace.buildTargets(), is(not(empty())));

            assertThat(workspace.compileTasks().getCompileTasks(), allOf(
                    hasEntry(equalTo(getBuildTargetId(projectDir)), equalTo(":classes")),
                    hasEntry(equalTo(getBuildTargetId(projectDir, "main")), equalTo(":classes")),
                    hasEntry(equalTo(getBuildTargetId(projectDir, "test")), equalTo(":testClasses"))
            ));

            assertThat(workspace.testTasks().getTestTasks(), allOf(
                    hasEntry(equalTo(getBuildTargetId(projectDir)), contains(":check")),
                    hasEntry(equalTo(getBuildTargetId(projectDir, "test")), contains(":test"))
            ));

            assertThat(workspace.cleanTasks().getCleanTasks(), allOf(
                    hasEntry(equalTo(getBuildTargetId(projectDir)), equalTo(":clean")),
                    hasEntry(equalTo(getBuildTargetId(projectDir, "main")), equalTo(":cleanClasses")),
                    hasEntry(equalTo(getBuildTargetId(projectDir, "test")), equalTo(":cleanTestClasses"))
            ));

            // Equality checks for tooling models don't work due to Tooling API classloader issues...
            // assertThat(workspace.buildTargetSources().getSources(), allOf(
            //         hasEntry(equalTo(getBuildTargetId(projectDir, "main")), containsInAnyOrder(
            //                 new DefaultBspSourceItem(getSourceUri(projectDir, "src", "main", "java")),
            //                 new DefaultBspSourceItem(getSourceUri(projectDir, "src", "main", "resources"))
            //         )),
            //         hasEntry(equalTo(getBuildTargetId(projectDir, "test")), containsInAnyOrder(
            //                 new DefaultBspSourceItem(getSourceUri(projectDir, "src", "test", "java")),
            //                 new DefaultBspSourceItem(getSourceUri(projectDir, "src", "test", "resources"))
            //         )),
            //         hasEntry(equalTo(getBuildTargetId(projectDir, "java")), containsInAnyOrder(
            //                 new DefaultBspSourceItem(getSourceUri(projectDir, "src", "main", "java"))
            //         )),
            //         hasEntry(equalTo(getBuildTargetId(projectDir, "testJava")), contains(
            //                 new DefaultBspSourceItem(getSourceUri(projectDir, "src", "test", "java"))
            //         ))
            // ));

            assertThat(workspace.runTasks().getRunTasks(), is(anEmptyMap()));
        }
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}
