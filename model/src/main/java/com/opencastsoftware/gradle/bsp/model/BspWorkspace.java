/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.io.Serializable;
import java.util.List;

public interface BspWorkspace extends Serializable {
    List<BspBuildTarget> buildTargets();
    BspCompileTasks compileTasks();
    BspTestTasks testTasks();
    BspRunTasks runTasks();
    BspCleanTasks cleanTasks();
    BspBuildTargetSources buildTargetSources();
    BspDependencyModules buildTargetDependencies();
}
