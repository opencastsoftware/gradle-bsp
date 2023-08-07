/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.*;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;

public abstract class GenerateBspConfig extends DefaultTask {
    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getTaskClasspath();

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getBuildServerClasspath();

    @Input
    public abstract SetProperty<String> getLanguages();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @Inject
    public abstract WorkerExecutor getWorkerExecutor();

    @TaskAction
    public void generate() {
        WorkQueue workQueue = getWorkerExecutor().classLoaderIsolation(workerSpec -> {
            workerSpec.getClasspath().from(getTaskClasspath());
        });

        workQueue.submit(GenerateBspConfigAction.class, parameters -> {
            parameters.getBuildServerClasspath().from(getBuildServerClasspath());
            parameters.getLanguages().set(getLanguages());
            parameters.getOutputFile().set(getOutputFile());
        });
    }
}
