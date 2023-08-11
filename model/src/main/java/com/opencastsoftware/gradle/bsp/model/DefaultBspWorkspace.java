/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.util.List;
import java.util.Objects;

public class DefaultBspWorkspace implements BspWorkspace {
    private final List<BspBuildTarget> buildTargets;
    private final BspCompileTasks compileTasks;
    private final BspTestTasks testTasks;
    private final BspRunTasks runTasks;
    private final BspCleanTasks cleanTasks;
    private final BspBuildTargetSources buildTargetSources;
    private final BspDependencyModules buildTargetDependencies;

    public DefaultBspWorkspace(List<BspBuildTarget> buildTargets, BspCompileTasks compileTasks, BspTestTasks testTasks, BspRunTasks runTasks, BspCleanTasks cleanTasks, BspBuildTargetSources buildTargetSources, BspDependencyModules buildTargetDependencies) {
        this.buildTargets = buildTargets;
        this.compileTasks = compileTasks;
        this.testTasks = testTasks;
        this.runTasks = runTasks;
        this.cleanTasks = cleanTasks;
        this.buildTargetSources = buildTargetSources;
        this.buildTargetDependencies = buildTargetDependencies;
    }

    @Override
    public List<BspBuildTarget> buildTargets() {
        return buildTargets;
    }


    @Override
    public BspCompileTasks compileTasks() {
        return compileTasks;
    }

    @Override
    public BspTestTasks testTasks() {
        return testTasks;
    }

    @Override
    public BspRunTasks runTasks() {
        return runTasks;
    }

    @Override
    public BspCleanTasks cleanTasks() {
        return cleanTasks;
    }

    @Override
    public BspBuildTargetSources buildTargetSources() {
        return buildTargetSources;
    }

    @Override
    public BspDependencyModules buildTargetDependencies() {
        return buildTargetDependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBspWorkspace that = (DefaultBspWorkspace) o;
        return Objects.equals(buildTargets, that.buildTargets) && Objects.equals(compileTasks, that.compileTasks) && Objects.equals(testTasks, that.testTasks) && Objects.equals(runTasks, that.runTasks) && Objects.equals(cleanTasks, that.cleanTasks) && Objects.equals(buildTargetSources, that.buildTargetSources) && Objects.equals(buildTargetDependencies, that.buildTargetDependencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buildTargets, compileTasks, testTasks, runTasks, cleanTasks, buildTargetSources, buildTargetDependencies);
    }

    @Override
    public String toString() {
        return "DefaultBspWorkspace[" +
                "buildTargets=" + buildTargets +
                ", compileTasks=" + compileTasks +
                ", testTasks=" + testTasks +
                ", runTasks=" + runTasks +
                ", cleanTasks=" + cleanTasks +
                ", buildTargetSources=" + buildTargetSources +
                ", buildTargetDependencies=" + buildTargetDependencies +
                ']';
    }
}
