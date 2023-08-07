/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import org.gradle.api.provider.SetProperty;

public interface BspExtension {
    SetProperty<String> getSupportedLanguages();
    SetProperty<BspLanguageModelBuilder> getLanguageModelBuilders();
}
