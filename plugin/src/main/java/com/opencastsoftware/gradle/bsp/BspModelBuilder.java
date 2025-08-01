/*
 * SPDX-FileCopyrightText:  © 2023-2025 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import com.opencastsoftware.gradle.bsp.model.*;
import org.gradle.api.Project;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.tasks.SourceSet;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.testing.base.TestingExtension;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BspModelBuilder {
    String APPLICATION_TAG = "application";
    String LIBRARY_TAG = "library";
    String TEST_TAG = "test";
    String INTEGRATION_TEST_TAG = "integration-test";

    protected BspBuildTargetId getBuildTargetIdFor(Project project) {
        return new DefaultBspBuildTargetId(project.getProjectDir().toURI());
    }

    protected BspBuildTargetId getBuildTargetIdFor(Project project, SourceSet sourceSet) {
        var sourceSetName = sourceSet.getTaskName(null, null);
        var buildTargetUri = project.getProjectDir().toURI().resolve("?sourceSet=" + sourceSetName);
        return new DefaultBspBuildTargetId(buildTargetUri);
    }

    protected URI getBaseDirectoryFor(Project project) {
        return project.getProjectDir().toURI();
    }

    protected List<String> getBuildTargetTagsFor(Project project) {
        var tags = new ArrayList<String>();

        var isApplication = project.getPlugins().hasPlugin(ApplicationPlugin.class);
        if (isApplication) {
            tags.add(APPLICATION_TAG);
        }

        var isLibrary = project.getPlugins().hasPlugin(JavaLibraryPlugin.class);
        if (isLibrary) {
            tags.add(LIBRARY_TAG);
        }

        return tags;
    }

    protected List<String> getBuildTargetTagsFor(Project project, SourceSet sourceSet) {
        var tags = new ArrayList<String>();

        var isApplication = SourceSet.isMain(sourceSet) && project.getPlugins().hasPlugin(ApplicationPlugin.class);
        if (isApplication) {
            tags.add(APPLICATION_TAG);
        }

        var isLibrary = SourceSet.isMain(sourceSet) && project.getPlugins().hasPlugin(JavaLibraryPlugin.class);
        if (isLibrary) {
            tags.add(LIBRARY_TAG);
        }

        var testSourceSets = getTestSourceSets(project);
        var isTest = testSourceSets.contains(sourceSet);

        if (isTest) {
            tags.add(TEST_TAG);
        }

        return tags;
    }

    protected BspBuildTargetCapabilities getBuildTargetCapabilitiesFor(Project project) {
        var isApplication =  project.getPlugins().hasPlugin(ApplicationPlugin.class);
        return new DefaultBspBuildTargetCapabilities(true, true, isApplication, false);
    }

    protected BspBuildTargetCapabilities getBuildTargetCapabilitiesFor(Project project, SourceSet sourceSet) {
        var isApplication = SourceSet.isMain(sourceSet) && project.getPlugins().hasPlugin(ApplicationPlugin.class);
        var isTest = getTestSourceSets(project).contains(sourceSet);
        return new DefaultBspBuildTargetCapabilities(true, isTest, isApplication, false);
    }

    @Nullable
    protected BspJvmBuildTarget getJvmBuildTargetFor(Project project) {
        var javaToolChainService = project.getExtensions().findByType(JavaToolchainService.class);

        if (javaToolChainService == null) {
            return null;
        }

        var javaToolchain = javaToolChainService.launcherFor(spec -> {
        }).get();

        var javaToolchainMetadata = javaToolchain.getMetadata();

        return new DefaultBspJvmBuildTarget(
                javaToolchainMetadata.getInstallationPath().getAsFile().toURI(),
                javaToolchainMetadata.getLanguageVersion().toString()
        );
    }

    protected Set<SourceSet> getTestSourceSets(Project project) {
        var testingExtension = project.getExtensions().findByType(TestingExtension.class);

        return Optional.ofNullable(testingExtension)
                .map(ext -> ext.getSuites()
                        .withType(JvmTestSuite.class).stream()
                        .map(JvmTestSuite::getSources)
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }
}
