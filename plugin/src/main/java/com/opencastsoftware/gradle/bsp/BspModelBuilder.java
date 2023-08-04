/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import com.opencastsoftware.gradle.bsp.model.*;
import org.gradle.api.Project;
import org.gradle.api.attributes.TestSuiteType;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.antlr.AntlrSourceDirectorySet;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.tasks.GroovySourceDirectorySet;
import org.gradle.api.tasks.ScalaRuntime;
import org.gradle.api.tasks.ScalaSourceDirectorySet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.scala.ScalaCompile;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.testing.base.TestingExtension;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.util.Path;
import org.gradle.util.internal.VersionNumber;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BspModelBuilder implements ToolingModelBuilder {
    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals(BspWorkspace.class.getName());
    }

    List<String> buildTargetTags(Project project, boolean isApplication, boolean isLibrary, boolean isTest, boolean isIntegrationTest) {
        var tags = new ArrayList<String>();

        if (isApplication) {
            tags.add("application");
        }

        if (isLibrary) {
            tags.add("library");
        }

        if (isTest) {
            tags.add("test");
        }

        if (isIntegrationTest) {
            tags.add("integration-test");
        }

        return tags;
    }

    BspJvmBuildTarget buildJvmBuildTarget(Project project) {
        var javaToolChainService = project.getExtensions().findByType(JavaToolchainService.class);

        var javaToolchain = javaToolChainService.launcherFor(spec -> {
        }).get();

        var javaToolchainMetadata = javaToolchain.getMetadata();

        return new DefaultBspJvmBuildTarget(
                javaToolchainMetadata.getInstallationPath().getAsFile().toURI(),
                javaToolchainMetadata.getLanguageVersion().toString()
        );
    }

    BspBuildTarget buildJavaBuildTarget(Project project, URI projectURI, BspJvmBuildTarget jvmBuildTarget, List<String> tags, SourceSet sourceSet, boolean isTest, boolean isApplication, boolean isLibrary) {
        var displayName = project.getPath().equals(":")
                ? project.getPath() + sourceSet.getName()
                : project.getPath() + Path.SEPARATOR + sourceSet.getName();

        return new DefaultBspBuildTarget(
                new DefaultBspBuildTargetId(projectURI.resolve(sourceSet.getName())),
                displayName,
                project.getProjectDir().getAbsoluteFile().toURI().toString(),
                tags,
                List.of("java"),
                List.of(),
                new DefaultBspBuildTargetCapabilities(true, isTest, isApplication, false),
                "jvm",
                jvmBuildTarget
        );
    }

    BspBuildTarget buildScalaBuildTarget(Project project, URI projectURI, BspJvmBuildTarget jvmBuildTarget, List<String> tags, SourceSet sourceSet, ScalaSourceDirectorySet scalaSourceDirectorySet, boolean isTest, boolean isApplication, boolean isLibrary) {
        var scalaRuntime = project.getExtensions().findByType(ScalaRuntime.class);

        // Get the classpath of the compile task
        var scalaCompileTaskName = sourceSet.getCompileTaskName("scala");
        var scalaCompileTask = project.getTasks().named(scalaCompileTaskName, ScalaCompile.class).get();
        var scalaCompileClasspath = scalaCompileTask.getClasspath();

        // Use this to find the Scala library JARs
        var scalaLibraryJar = scalaRuntime.findScalaJar(scalaCompileClasspath, "library");
        var scala3LibraryJar = scalaRuntime.findScalaJar(scalaCompileClasspath, "library_3");

        boolean isScala3 = scala3LibraryJar != null;

        // This is apparently the legitimate way to find the Scala version used in a Gradle project!
        var scalaVersion = scalaRuntime.getScalaVersion(isScala3 ? scala3LibraryJar : scalaLibraryJar);

        // Parse out the binary version
        var scalaVersionNumber = VersionNumber.parse(scalaVersion);
        var scalaBinaryVersion = String.format("%d.%d", scalaVersionNumber.getMajor(), scalaVersionNumber.getMinor());

        var scalaBuildTarget = new DefaultBspScalaBuildTarget(
                "org.scala-lang",
                scalaVersion,
                scalaBinaryVersion,
                BspScalaPlatform.JVM,
                new URI[]{},
                jvmBuildTarget
        );

        var displayName = project.getPath().equals(":")
                ? project.getPath() + scalaSourceDirectorySet.getName()
                : project.getPath() + Path.SEPARATOR + scalaSourceDirectorySet.getName();

        return new DefaultBspBuildTarget(
                new DefaultBspBuildTargetId(projectURI.resolve(scalaSourceDirectorySet.getName())),
                displayName,
                project.getProjectDir().getAbsoluteFile().toURI().toString(),
                tags,
                List.of("scala"),
                List.of(),
                new DefaultBspBuildTargetCapabilities(true, isTest, isApplication, false),
                "scala",
                scalaBuildTarget
        );
    }

    BspBuildTarget buildGroovyBuildTarget(Project project, URI projectURI, BspJvmBuildTarget jvmBuildTarget, List<String> tags, GroovySourceDirectorySet groovySourceDirectorySet, boolean isTest, boolean isApplication, boolean isLibrary) {
        var displayName = project.getPath().equals(":")
                ? project.getPath() + groovySourceDirectorySet.getName()
                : project.getPath() + Path.SEPARATOR + groovySourceDirectorySet.getName();

        return new DefaultBspBuildTarget(
                new DefaultBspBuildTargetId(projectURI.resolve(groovySourceDirectorySet.getName())),
                displayName,
                project.getProjectDir().getAbsoluteFile().toURI().toString(),
                tags,
                List.of("groovy"),
                List.of(),
                new DefaultBspBuildTargetCapabilities(true, isTest, isApplication, false),
                "jvm",
                jvmBuildTarget
        );
    }

    BspBuildTarget buildAntlrBuildTarget(Project project, URI projectURI, AntlrSourceDirectorySet antlrSourceDirectorySet) {
        var displayName = project.getPath().equals(":")
                ? project.getPath() + antlrSourceDirectorySet.getName()
                : project.getPath() + Path.SEPARATOR + antlrSourceDirectorySet.getName();

        return new DefaultBspBuildTarget(
                new DefaultBspBuildTargetId(projectURI.resolve(antlrSourceDirectorySet.getName())),
                displayName,
                project.getProjectDir().getAbsoluteFile().toURI().toString(),
                List.of(),
                List.of("antlr"),
                List.of(),
                new DefaultBspBuildTargetCapabilities(true, false, false, false)
        );
    }

    List<SourceSet> getTestSourceSets(Project project) {
        var testingExtension = project.getExtensions().findByType(TestingExtension.class);

        return Optional.ofNullable(testingExtension)
                .map(testExt -> testExt.getSuites()
                        .withType(JvmTestSuite.class).stream()
                        .map(JvmTestSuite::getSources)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    List<SourceSet> getIntegrationTestSourceSets(Project project) {
        var testingExtension = project.getExtensions().findByType(TestingExtension.class);

        return Optional.ofNullable(testingExtension)
                .map(testExt -> testExt.getSuites()
                        .withType(JvmTestSuite.class).stream()
                        .filter(suite -> suite.getTestType().get().equals(TestSuiteType.INTEGRATION_TEST))
                        .map(JvmTestSuite::getSources)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    @Override
    public BspWorkspace buildAll(String modelName, Project rootProject) {
        var buildTargets = new ArrayList<BspBuildTarget>();

        rootProject.getAllprojects().forEach(project -> {
            var projectURI = project.getProjectDir().getAbsoluteFile().toURI();
            var testSourceSets = getTestSourceSets(project);
            var integrationTestSourceSets = getIntegrationTestSourceSets(project);

            var javaExtension = project.getExtensions().findByType(JavaPluginExtension.class);
            if (javaExtension != null) {
                var jvmBuildTarget = buildJvmBuildTarget(project);

                javaExtension.getSourceSets().forEach(sourceSet -> {
                    var isApplication = SourceSet.isMain(sourceSet) && project.getPlugins().hasPlugin(ApplicationPlugin.class);
                    var isLibrary = SourceSet.isMain(sourceSet) && project.getPlugins().hasPlugin(JavaLibraryPlugin.class);
                    var isTest = testSourceSets.contains(sourceSet);
                    var isIntegrationTest = integrationTestSourceSets.contains(sourceSet);
                    var tags = buildTargetTags(project, isApplication, isLibrary, isTest, isIntegrationTest);

                    buildTargets.add(buildJavaBuildTarget(project, projectURI, jvmBuildTarget, tags, sourceSet, isTest, isApplication, isLibrary));

                    var sourceSetExtensions = sourceSet.getExtensions();

                    var scalaSourceDirectorySet = sourceSetExtensions.findByType(ScalaSourceDirectorySet.class);
                    if (scalaSourceDirectorySet != null) {
                        buildTargets.add(buildScalaBuildTarget(project, projectURI, jvmBuildTarget, tags, sourceSet, scalaSourceDirectorySet, isTest, isApplication, isLibrary));
                    }

                    var groovySourceDirectorySet = sourceSetExtensions.findByType(GroovySourceDirectorySet.class);
                    if (groovySourceDirectorySet != null) {
                        buildTargets.add(buildGroovyBuildTarget(project, projectURI, jvmBuildTarget, tags, groovySourceDirectorySet, isTest, isApplication, isLibrary));
                    }

                    var antlrSourceDirectorySet = sourceSetExtensions.findByType(AntlrSourceDirectorySet.class);
                    if (antlrSourceDirectorySet != null) {
                        buildTargets.add(buildAntlrBuildTarget(project, projectURI, antlrSourceDirectorySet));
                    }
                });
            }
        });

        return new DefaultBspWorkspace(buildTargets);
    }
}
