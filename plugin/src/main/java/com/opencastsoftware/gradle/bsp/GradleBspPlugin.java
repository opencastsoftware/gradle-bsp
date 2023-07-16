/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import javax.inject.Inject;

/**
 * A simple 'hello world' plugin.
 */
public class GradleBspPlugin implements Plugin<Project> {
    private final ToolingModelBuilderRegistry builderRegistry;

    @Inject
    public GradleBspPlugin(ToolingModelBuilderRegistry builderRegistry) {
        this.builderRegistry = builderRegistry;
    }

    public void apply(Project project) {
        builderRegistry.register(new BspWorkspaceModelBuilder());
    }

    private static class BspWorkspaceModelBuilder implements ToolingModelBuilder {

        @Override
        public boolean canBuild(String modelName) {
            return modelName.equals(BspWorkspace.class.getName());
        }

        @Override
        public Object buildAll(String modelName, Project project) {
            return new DefaultBspWorkspace();
        }
    }
}
