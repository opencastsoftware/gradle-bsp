/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import com.jparams.verifier.tostring.ToStringVerifier;
import com.opencastsoftware.gradle.bsp.model.*;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class BspModelTest {
    @Test
    void testBspBuildTarget() {
        EqualsVerifier.forClass(DefaultBspBuildTarget.class).usingGetClass().verify();
        ToStringVerifier.forClass(DefaultBspBuildTarget.class).verify();
    }
    @Test
    void testBspBuildTargetCapabilities() {
        EqualsVerifier.forClass(DefaultBspBuildTargetCapabilities.class).usingGetClass().verify();
        ToStringVerifier.forClass(DefaultBspBuildTargetCapabilities.class).verify();
    }

    @Test
    void testBspBuildTargetId() {
        EqualsVerifier.forClass(DefaultBspBuildTargetId.class).usingGetClass().verify();
        ToStringVerifier.forClass(DefaultBspBuildTargetId.class).verify();
    }

    @Test
    void testBspJvmBuildTarget() {
        EqualsVerifier.forClass(DefaultBspJvmBuildTarget.class).usingGetClass().verify();
        ToStringVerifier.forClass(DefaultBspJvmBuildTarget.class).verify();
    }

    @Test
    void testBspScalaBuildTarget() {
        EqualsVerifier.forClass(DefaultBspScalaBuildTarget.class).usingGetClass().verify();
        ToStringVerifier.forClass(DefaultBspScalaBuildTarget.class).verify();
    }

    @Test
    void testBspWorkspace() {
        EqualsVerifier.forClass(DefaultBspWorkspace.class).usingGetClass().verify();
        ToStringVerifier.forClass(DefaultBspWorkspace.class).verify();
    }

    @Test
    void testBspCompileTasks() {
        EqualsVerifier.forClass(DefaultBspCompileTasks.class).usingGetClass().verify();
        ToStringVerifier.forClass(DefaultBspCompileTasks.class).verify();
    }

    @Test
    void testBspTestTasks() {
        EqualsVerifier.forClass(DefaultBspTestTasks.class).usingGetClass().verify();
        ToStringVerifier.forClass(DefaultBspTestTasks.class).verify();
    }

    @Test
    void testBspRunTasks() {
        EqualsVerifier.forClass(DefaultBspRunTasks.class).usingGetClass().verify();
        ToStringVerifier.forClass(DefaultBspRunTasks.class).verify();
    }

    @Test
    void testBspCleanTasks() {
        EqualsVerifier.forClass(DefaultBspCleanTasks.class).usingGetClass().verify();
        ToStringVerifier.forClass(DefaultBspCleanTasks.class).verify();
    }

    @Test
    void testBspSourceItem() {
        EqualsVerifier.forClass(DefaultBspSourceItem.class).usingGetClass().verify();
        ToStringVerifier.forClass(DefaultBspSourceItem.class).verify();
    }

    @Test
    void testBspBuildTargetSources() {
        EqualsVerifier.forClass(DefaultBspBuildTargetSources.class).usingGetClass().verify();
        ToStringVerifier.forClass(DefaultBspBuildTargetSources.class).verify();
    }

    @Test
    void testBspBuildTargetResources() {
        EqualsVerifier.forClass(DefaultBspBuildTargetResources.class).usingGetClass().verify();
        ToStringVerifier.forClass(DefaultBspBuildTargetResources.class).verify();
    }

    @Test
    void testBspDependencyModule() {
        EqualsVerifier.forClass(DefaultBspDependencyModule.class).usingGetClass().verify();
        ToStringVerifier.forClass(DefaultBspDependencyModule.class).verify();
    }

    @Test
    void testBspDependencyModules() {
        EqualsVerifier.forClass(DefaultBspDependencyModules.class).usingGetClass().verify();
        ToStringVerifier.forClass(DefaultBspDependencyModules.class).verify();
    }

    @Test
    void testBspMavenDependencyModule() {
        EqualsVerifier.forClass(DefaultBspMavenDependencyModule.class).usingGetClass().verify();
        ToStringVerifier.forClass(DefaultBspMavenDependencyModule.class).verify();
    }

    @Test
    void testBspMavenDependencyModuleArtifact() {
        EqualsVerifier.forClass(DefaultBspMavenDependencyModuleArtifact.class).usingGetClass().verify();
        ToStringVerifier.forClass(DefaultBspMavenDependencyModuleArtifact.class).verify();
    }
}
