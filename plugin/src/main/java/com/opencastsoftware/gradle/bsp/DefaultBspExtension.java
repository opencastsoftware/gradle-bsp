/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

public abstract class DefaultBspExtension implements BspExtension, HasPublicType {
    private final SetProperty<String> supportedLanguages;
    private final SetProperty<BspLanguageModelBuilder> languageModelBuilders;

    public DefaultBspExtension(ObjectFactory objectFactory) {
        supportedLanguages = objectFactory.setProperty(String.class);
        languageModelBuilders = objectFactory.setProperty(BspLanguageModelBuilder.class);
    }

    @Override
    public SetProperty<String> getSupportedLanguages() {
        return supportedLanguages;
    }

    @Override
    public SetProperty<BspLanguageModelBuilder> getLanguageModelBuilders() {
        return languageModelBuilders;
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(BspExtension.class);
    }
}
