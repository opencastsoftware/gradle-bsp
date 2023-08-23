/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import com.opencastsoftware.gradle.bsp.model.*;
import org.gradle.internal.classpath.DefaultClassPath;
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.internal.consumer.DefaultModelBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;

import static com.opencastsoftware.gradle.bsp.PropertyMatcher.hasProperty;
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

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }

    private <A> A fetchModel(File projectDir, Class<A> modelClass) {
        var connector = GradleConnector.newConnector()
                .forProjectDirectory(projectDir)
                .useBuildDistribution();

        try (var connection = connector.connect()) {
            var builder = (DefaultModelBuilder<A>) connection.model(modelClass);
            var pluginClasspath = PluginUnderTestMetadataReading.readImplementationClasspath();
            builder.withInjectedClassPath(DefaultClassPath.of(pluginClasspath));
            return builder.get();
        }
    }

    private BspWorkspace fetchWorkspaceModel(File projectDir) {
        return fetchModel(projectDir, BspWorkspace.class);
    }

    @Test
    void returnsWorkspaceModel(@TempDir File projectDir) throws IOException {
        writeString(getSettingsFile(projectDir), "");
        writeString(getBuildFile(projectDir),
                "plugins {\n" +
                        "  id('com.opencastsoftware.gradle.bsp')\n" +
                        "}");

        var workspace = fetchWorkspaceModel(projectDir);

        assertThat(workspace, is(notNullValue()));

        assertThat(workspace.buildTargets(), is(empty()));
    }

    @Test
    void returnsCompileTasks(@TempDir File projectDir) throws IOException {
        writeString(getSettingsFile(projectDir), "");
        writeString(getBuildFile(projectDir),
                "plugins {\n" +
                        "  id('java')\n" +
                        "  id('com.opencastsoftware.gradle.bsp')\n" +
                        "}");

        var compileTasks = fetchModel(projectDir, BspCompileTasks.class);

        assertThat(compileTasks.getCompileTasks(), allOf(
                hasEntry(equalTo(getBuildTargetId(projectDir)), equalTo(":classes")),
                hasEntry(equalTo(getBuildTargetId(projectDir, "main")), equalTo(":classes")),
                hasEntry(equalTo(getBuildTargetId(projectDir, "test")), equalTo(":testClasses"))
        ));
    }

    @Test
    void returnsTestTasks(@TempDir File projectDir) throws IOException {
        writeString(getSettingsFile(projectDir), "");
        writeString(getBuildFile(projectDir),
                "plugins {\n" +
                        "  id('java')\n" +
                        "  id('com.opencastsoftware.gradle.bsp')\n" +
                        "}");

        var testTasks = fetchModel(projectDir, BspTestTasks.class);

        assertThat(testTasks.getTestTasks(), allOf(
                hasEntry(equalTo(getBuildTargetId(projectDir)), contains(":check")),
                hasEntry(equalTo(getBuildTargetId(projectDir, "test")), contains(":test"))
        ));
    }

    @Test
    void returnsNoRunTasksWithoutApplicationPlugin(@TempDir File projectDir) throws IOException {
        writeString(getSettingsFile(projectDir), "");
        writeString(getBuildFile(projectDir),
                "plugins {\n" +
                        "  id('java')\n" +
                        "  id('com.opencastsoftware.gradle.bsp')\n" +
                        "}");

        var runTasks = fetchModel(projectDir, BspRunTasks.class);

        assertThat(runTasks.getRunTasks(), is(anEmptyMap()));
    }

    @Test
    void returnsRunTasksWithApplicationPlugin(@TempDir File projectDir) throws IOException {
        writeString(getSettingsFile(projectDir), "");
        writeString(getBuildFile(projectDir),
                "plugins {\n" +
                        "  id('java')\n" +
                        "  id('application')\n" +
                        "  id('com.opencastsoftware.gradle.bsp')\n" +
                        "}");

        var workspace = fetchWorkspaceModel(projectDir);

        assertThat(workspace, is(notNullValue()));

        assertThat(workspace.runTasks().getRunTasks(), hasEntry(
                equalTo(getBuildTargetId(projectDir)),
                equalTo(":run")
        ));
    }

    @Test
    void returnsBuildTargetSources(@TempDir File projectDir) throws IOException {
        writeString(getSettingsFile(projectDir), "");
        writeString(getBuildFile(projectDir),
                "plugins {\n" +
                        "  id('java')\n" +
                        "  id('com.opencastsoftware.gradle.bsp')\n" +
                        "}");

        var buildTargetSources = fetchModel(projectDir, BspBuildTargetSources.class);

        // Tooling API models are loaded separately in a different classloader from the one that references the model classes in our tests.
        // This means that our assertions cannot do simple `equals` comparisons using those classes, as they will always be false.
        // We must compare individual properties instead.
        assertThat(buildTargetSources.getSources(), allOf(
                hasEntry(equalTo(getBuildTargetId(projectDir, "main")), containsInAnyOrder(
                        allOf(
                                hasProperty("uri", BspSourceItem::uri, equalTo(getSourceUri(projectDir, "src", "main", "java"))),
                                hasProperty("generated", BspSourceItem::generated, is(false))
                        ),
                        allOf(
                                hasProperty("uri", BspSourceItem::uri, equalTo(getSourceUri(projectDir, "src", "main", "resources"))),
                                hasProperty("generated", BspSourceItem::generated, is(false))
                        )
                )),
                hasEntry(equalTo(getBuildTargetId(projectDir, "test")), containsInAnyOrder(
                        allOf(
                                hasProperty("uri", BspSourceItem::uri, equalTo(getSourceUri(projectDir, "src", "test", "java"))),
                                hasProperty("generated", BspSourceItem::generated, is(false))
                        ),
                        allOf(
                                hasProperty("uri", BspSourceItem::uri, equalTo(getSourceUri(projectDir, "src", "test", "resources"))),
                                hasProperty("generated", BspSourceItem::generated, is(false))
                        )
                )),
                hasEntry(equalTo(getBuildTargetId(projectDir, "java")), contains(
                        allOf(
                                hasProperty("uri", BspSourceItem::uri, equalTo(getSourceUri(projectDir, "src", "main", "java"))),
                                hasProperty("generated", BspSourceItem::generated, is(false))
                        )
                )),
                hasEntry(equalTo(getBuildTargetId(projectDir, "testJava")), contains(
                        allOf(
                                hasProperty("uri", BspSourceItem::uri, equalTo(getSourceUri(projectDir, "src", "test", "java"))),
                                hasProperty("generated", BspSourceItem::generated, is(false))
                        )
                ))
        ));
    }

    @Test
    void returnsBuildTargetResources(@TempDir File projectDir) throws IOException {
        writeString(getSettingsFile(projectDir), "");
        writeString(getBuildFile(projectDir),
                "plugins {\n" +
                        "  id('java')\n" +
                        "  id('com.opencastsoftware.gradle.bsp')\n" +
                        "}");

        var buildTargetResources = fetchModel(projectDir, BspBuildTargetResources.class);

        assertThat(buildTargetResources.getResources(), allOf(
                hasEntry(equalTo(getBuildTargetId(projectDir, "main")), contains(
                        equalTo(getSourceUri(projectDir, "src", "main", "resources"))
                )),
                hasEntry(equalTo(getBuildTargetId(projectDir, "test")), contains(
                        equalTo(getSourceUri(projectDir, "src", "test", "resources"))
                ))
        ));
    }

    @Test
    void returnsScalaSourcesWithScalaPlugin(@TempDir File projectDir) throws IOException {
        writeString(getSettingsFile(projectDir), "");
        writeString(getBuildFile(projectDir),
                "plugins {\n" +
                        "  id('java')\n" +
                        "  id('scala')\n" +
                        "  id('com.opencastsoftware.gradle.bsp')\n" +
                        "}");

        var buildTargetSources = fetchModel(projectDir, BspBuildTargetSources.class);

        assertThat(buildTargetSources.getSources(), allOf(
                hasEntry(equalTo(getBuildTargetId(projectDir, "main")), containsInAnyOrder(
                        allOf(
                                hasProperty("uri", BspSourceItem::uri, equalTo(getSourceUri(projectDir, "src", "main", "java"))),
                                hasProperty("generated", BspSourceItem::generated, is(false))
                        ),
                        allOf(
                                hasProperty("uri", BspSourceItem::uri, equalTo(getSourceUri(projectDir, "src", "main", "scala"))),
                                hasProperty("generated", BspSourceItem::generated, is(false))
                        ),
                        allOf(
                                hasProperty("uri", BspSourceItem::uri, equalTo(getSourceUri(projectDir, "src", "main", "resources"))),
                                hasProperty("generated", BspSourceItem::generated, is(false))
                        )
                )),
                hasEntry(equalTo(getBuildTargetId(projectDir, "test")), containsInAnyOrder(
                        allOf(
                                hasProperty("uri", BspSourceItem::uri, equalTo(getSourceUri(projectDir, "src", "test", "java"))),
                                hasProperty("generated", BspSourceItem::generated, is(false))
                        ),
                        allOf(
                                hasProperty("uri", BspSourceItem::uri, equalTo(getSourceUri(projectDir, "src", "test", "scala"))),
                                hasProperty("generated", BspSourceItem::generated, is(false))
                        ),
                        allOf(
                                hasProperty("uri", BspSourceItem::uri, equalTo(getSourceUri(projectDir, "src", "test", "resources"))),
                                hasProperty("generated", BspSourceItem::generated, is(false))
                        )
                )),
                hasEntry(equalTo(getBuildTargetId(projectDir, "scala")), contains(
                        allOf(
                                hasProperty("uri", BspSourceItem::uri, equalTo(getSourceUri(projectDir, "src", "main", "scala"))),
                                hasProperty("generated", BspSourceItem::generated, is(false))
                        )
                )),
                hasEntry(equalTo(getBuildTargetId(projectDir, "testScala")), contains(
                        allOf(
                                hasProperty("uri", BspSourceItem::uri, equalTo(getSourceUri(projectDir, "src", "test", "scala"))),
                                hasProperty("generated", BspSourceItem::generated, is(false))
                        )
                ))
        ));
    }
}
