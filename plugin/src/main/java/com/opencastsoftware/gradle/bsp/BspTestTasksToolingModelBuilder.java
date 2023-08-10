/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import com.opencastsoftware.gradle.bsp.model.BspTestTasks;
import com.opencastsoftware.gradle.bsp.model.DefaultBspTestTasks;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.tasks.SourceSet;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.testing.base.TestingExtension;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BspTestTasksToolingModelBuilder extends BspModelBuilder implements ToolingModelBuilder {
    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals(BspTestTasks.class.getName());
    }

    Optional<JvmTestSuite> testSuiteFor(NamedDomainObjectSet<JvmTestSuite> testSuites, SourceSet sourceSet) {
        return testSuites.stream()
                .filter(suite -> suite.getSources().equals(sourceSet))
                .findFirst();
    }

    @Override
    public BspTestTasks buildAll(String modelName, Project rootProject) {
        var testTasks = new HashMap<String, Set<String>>();

        rootProject.getAllprojects().forEach(project -> {
            var javaExtension = project.getExtensions().findByType(JavaPluginExtension.class);
            var testingExtension = project.getExtensions().findByType(TestingExtension.class);
            if (javaExtension != null && testingExtension != null) {
                var jvmTestSuites = testingExtension.getSuites().withType(JvmTestSuite.class);

                javaExtension.getSourceSets().forEach(sourceSet -> {
                    var sourceSetTargetId = getBuildTargetIdFor(project, sourceSet).uri().toString();
                    var testSuite = testSuiteFor(jvmTestSuites, sourceSet);

                    testSuite.ifPresent(jvmTestSuite -> {
                        var targetTestTasks = jvmTestSuite.getTargets().stream().map(target -> {
                            return target.getTestTask()
                                    .get().getPath();
                        }).collect(Collectors.toSet());

                        testTasks.put(sourceSetTargetId, targetTestTasks);
                    });
                });

                var projectTargetId = getBuildTargetIdFor(project).uri().toString();
                var checkTask = project.getTasks().findByName(LifecycleBasePlugin.CHECK_TASK_NAME);
                Optional.ofNullable(checkTask).ifPresent(task -> testTasks.put(projectTargetId, Set.of(task.getPath())));
            }
        });

        return new DefaultBspTestTasks(testTasks);
    }
}
