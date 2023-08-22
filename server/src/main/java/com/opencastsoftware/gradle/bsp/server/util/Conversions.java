/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.server.util;

import ch.epfl.scala.bsp4j.*;
import com.opencastsoftware.gradle.bsp.model.*;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Conversions {
    public static BuildTargetIdentifier toBspBuildTargetId(BspBuildTargetId targetId) {
        return new BuildTargetIdentifier(targetId.uri().toString());
    }

    public static BuildTargetCapabilities toBspBuildTargetCapabilities(BspBuildTargetCapabilities capabilities) {
        return new BuildTargetCapabilities(
                capabilities.canCompile(),
                capabilities.canTest(),
                capabilities.canRun(),
                capabilities.canDebug()
        );
    }

    public static BuildTarget toBspBuildTarget(BspBuildTarget buildTarget) {
        var bspTarget = new BuildTarget(
                toBspBuildTargetId(buildTarget.id()),
                buildTarget.tags(),
                buildTarget.languageIds(),
                buildTarget.dependencies().stream()
                        .map(Conversions::toBspBuildTargetId)
                        .collect(Collectors.toList()),
                Conversions.toBspBuildTargetCapabilities(buildTarget.capabilities())
        );

        if (buildTarget.displayName() != null) {
            bspTarget.setDisplayName(buildTarget.displayName());
        }

        if (buildTarget.baseDirectory() != null) {
            bspTarget.setBaseDirectory(buildTarget.baseDirectory().toString());
        }

        if (buildTarget.dataKind() != null && buildTarget.data() != null) {
            var dataKind = buildTarget.dataKind();
            var data = buildTarget.data();

            bspTarget.setDataKind(buildTarget.dataKind());
            // Tooling API classes are loaded into a different classloader,
            // which makes this whole process a total nightmare
            if (dataKind.equals("jvm")) {
                bspTarget.setData(Conversions.toBspJvmBuildTarget(data));
            } else if (dataKind.equals("scala")) {
                bspTarget.setData(Conversions.toBspScalaBuildTarget(data));
            } else {
                bspTarget.setData(data);
            }
        }

        return bspTarget;
    }

    public static JvmBuildTarget toBspJvmBuildTarget(Serializable data) {
        try {
            var javaHomeMethod = data.getClass().getDeclaredMethod("javaHome");
            var javaVersionMethod = data.getClass().getDeclaredMethod("javaVersion");
            return new JvmBuildTarget(javaHomeMethod.invoke(data).toString(), (String) javaVersionMethod.invoke(data));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return null;
        }
    }

    public static ScalaPlatform toBspScalaPlatform(Object scalaPlatform) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        var valueMethod = scalaPlatform.getClass().getDeclaredMethod("value");
        return ScalaPlatform.forValue((Integer) valueMethod.invoke(scalaPlatform));
    }

    public static ScalaBuildTarget toBspScalaBuildTarget(Serializable data) {
        try {
            var scalaOrgMethod = data.getClass().getDeclaredMethod("scalaOrganization");
            var scalaVersionMethod = data.getClass().getDeclaredMethod("scalaVersion");
            var scalaBinaryVersionMethod = data.getClass().getDeclaredMethod("scalaBinaryVersion");
            var scalaPlatformMethod = data.getClass().getDeclaredMethod("scalaPlatform");
            var jarsMethod = data.getClass().getMethod("jars");
            return new ScalaBuildTarget(
                    (String) scalaOrgMethod.invoke(data),
                    (String) scalaVersionMethod.invoke(data),
                    (String) scalaBinaryVersionMethod.invoke(data),
                    Conversions.toBspScalaPlatform(scalaPlatformMethod.invoke(data)),
                    Arrays.stream((URI[]) jarsMethod.invoke(data)).map(URI::toString).collect(Collectors.toList())
            );
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return null;
        }
    }
}
