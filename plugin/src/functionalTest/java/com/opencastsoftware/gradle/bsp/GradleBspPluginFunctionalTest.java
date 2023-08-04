/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.util.GradleVersion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

class GradleBspPluginFunctionalTest {

    private File getBuildFile(File projectDir) {
        return new File(projectDir, "build.gradle");
    }

    private File getSettingsFile(File projectDir) {
        return new File(projectDir, "settings.gradle");
    }

    private BuildResult runBspConfigTask(File projectDir) {
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("bspConfig", "--info", "--full-stacktrace");
        runner.withProjectDir(projectDir);
        return runner.build();
    }

    @Test
    void generatesDefaultConfig(@TempDir File projectDir) throws IOException {
        writeString(getSettingsFile(projectDir), "");
        writeString(getBuildFile(projectDir),
                "plugins {\n" +
                        "  id('com.opencastsoftware.gradle.bsp')\n" +
                        "}\n" +
                        "repositories {\n" +
                        "  mavenLocal()\n" +
                        "  mavenCentral()\n" +
                        "  maven { url 'https://repo.gradle.org/gradle/libs-releases' }\n"+
                        "}");

        var result = runBspConfigTask(projectDir);
        var bspConfigTask = result.task(":bspConfig");
        var bspFolder = projectDir.toPath().resolve(".bsp");
        var gradleJson = bspFolder.resolve("gradle.json");

        assertThat(bspConfigTask.getOutcome(), is(TaskOutcome.SUCCESS));

        var jsonObj = new JSONObject(Files.readString(gradleJson));
        assertThat(jsonObj.get("name"), is("Gradle"));
        assertThat(jsonObj.get("languages"), is(instanceOf(JSONArray.class)));
        assertThat(jsonObj.get("version"), is(GradleVersion.current().getVersion()));
        assertThat(jsonObj.get("bspVersion"), is(BuildInfo.bspVersion));
        assertThat(jsonObj.get("argv"), is(instanceOf(JSONArray.class)));
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}
