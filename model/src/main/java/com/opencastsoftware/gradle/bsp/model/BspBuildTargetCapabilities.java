/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.io.Serializable;

public interface BspBuildTargetCapabilities extends Serializable {
    boolean canCompile();
    boolean canTest();
    boolean canRun();
    boolean canDebug();
}
