/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import org.gradle.internal.UncheckedException;
import org.gradle.internal.jvm.Jvm;
import org.gradle.util.GradleVersion;
import org.gradle.workers.WorkAction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BspConfigAction implements WorkAction<BspConfigParameters> {

    @Override
    public void execute() {
        Set<String> languageIds = getParameters().getLanguages().get();

        String buildServerClasspath = getParameters()
                .getBuildServerClasspath()
                .getFiles()
                .stream()
                .map(File::getPath)
                .collect(Collectors.joining(File.pathSeparator));

        Path outputFile = getParameters()
                .getOutputFile()
                .get().getAsFile().toPath();

        JSONArray languages = new JSONArray();
        languages.putAll(languageIds);

        JSONArray argv = new JSONArray();
        argv.put(Jvm.current().getJavaExecutable().toString());
        argv.put("-cp");
        argv.put(buildServerClasspath);
        argv.put("com.opencastsoftware.gradle.bsp.server.GradleBspServerLauncher");
        argv.put("--stdio");

        JSONObject bspConfig = new JSONObject();
        bspConfig.put("name", "Gradle");
        bspConfig.put("version", GradleVersion.current().getVersion());
        bspConfig.put("bspVersion", BuildInfo.bspVersion);
        bspConfig.put("languages", languages);
        bspConfig.put("argv", argv);

        try {
            Files.createDirectories(outputFile.getParent());
            Files.writeString(outputFile, bspConfig.toString());
        } catch (IOException e) {
            UncheckedException.throwAsUncheckedException(e);
        }
    }
}
