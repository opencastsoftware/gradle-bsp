/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import com.opencastsoftware.gradle.bsp.model.BspScalaPlatform;
import com.opencastsoftware.gradle.bsp.model.DefaultBspScalaBuildTarget;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.ScalaRuntime;
import org.gradle.api.tasks.ScalaSourceDirectorySet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.scala.ScalaCompile;
import org.gradle.util.internal.VersionNumber;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.net.URI;

public abstract class BspScalaLanguageModelBuilder extends BspLanguageModelBuilder {
    private static final String SCALA_LANGUAGE_ID = "scala";

    @Override
    public String getLanguageId() {
        return SCALA_LANGUAGE_ID;
    }

    @Override
    protected SourceDirectorySet getSourceDirectorySetFor(SourceSet sourceSet) {
        return sourceSet.getExtensions().findByType(ScalaSourceDirectorySet.class);
    }

    @Nullable
    @Override
    protected String getBuildTargetDataKindFor(Project project, SourceSet sourceSet) {
        return "scala";
    }

    @Nullable
    @Override
    protected Serializable getBuildTargetDataFor(Project project, SourceSet sourceSet) {
        var scalaRuntime = project.getExtensions().findByType(ScalaRuntime.class);

        if (scalaRuntime == null) {
            return null;
        }

        // Get the classpath of the compile task
        var scalaCompileTaskName = sourceSet.getCompileTaskName(SCALA_LANGUAGE_ID);
        var scalaCompileTask = project.getTasks().named(scalaCompileTaskName, ScalaCompile.class).get();
        var scalaCompileClasspath = scalaCompileTask.getClasspath();

        // Use this to find the Scala library JARs
        var scalaLibraryJar = scalaRuntime.findScalaJar(scalaCompileClasspath, "library");
        var scala3LibraryJar = scalaRuntime.findScalaJar(scalaCompileClasspath, "library_3");

        boolean isScala3 = scala3LibraryJar != null;

        var chosenScalaLibraryJar = isScala3 ? scala3LibraryJar : scalaLibraryJar;

        if (chosenScalaLibraryJar == null) {
            // The `scala` plugin was added but no Scala library dependencies were added
            return null;
        }

        // This is apparently the legitimate way to find the Scala version used in a Gradle project!
        var scalaVersion = scalaRuntime.getScalaVersion(chosenScalaLibraryJar);

        // Parse out the binary version
        var scalaVersionNumber = VersionNumber.parse(scalaVersion);
        var scalaBinaryVersion = String.format("%d.%d", scalaVersionNumber.getMajor(), scalaVersionNumber.getMinor());

        return new DefaultBspScalaBuildTarget(
                "org.scala-lang",
                scalaVersion,
                scalaBinaryVersion,
                BspScalaPlatform.JVM,
                new URI[]{},
                getJvmBuildTargetFor(project)
        );
    }
}
