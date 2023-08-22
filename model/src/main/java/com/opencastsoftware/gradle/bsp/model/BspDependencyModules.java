/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import java.util.Set;

public interface BspDependencyModules extends Serializable {
    Map<URI, Set<BspDependencyModule>> dependencyModules();
}
