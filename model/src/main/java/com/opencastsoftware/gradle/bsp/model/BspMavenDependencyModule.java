/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.io.Serializable;
import java.util.Set;

public interface BspMavenDependencyModule extends Serializable {
    String organization();
    String name();
    String version();
    Set<BspMavenDependencyModuleArtifact> artifacts();
    String scope();
}
