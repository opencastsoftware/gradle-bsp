/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.model;

import java.io.Serializable;
import java.util.List;

public interface BspBuildTarget extends Serializable {
    BspBuildTargetId id();
    String displayName();
    String baseDirectory();
    List<String> tags();
    List<String> languageIds();
    List<BspBuildTargetId> dependencies();
    BspBuildTargetCapabilities capabilities();
    String dataKind();
    Serializable data();
}
