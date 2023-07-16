/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Command(name = "Gradle BSP Server", mixinStandardHelpOptions = true)
public class GradleBspServerLauncher implements Callable<Integer> {
    private static Logger logger = LoggerFactory.getLogger(GradleBspServerLauncher.class);
    private static Path initScriptPath;

    public static void main(String[] args) throws IOException {
        initScriptPath = createInitScript();
        int exitCode = new CommandLine(new GradleBspServerLauncher()).execute(args);
        System.exit(exitCode);
    }

    private Path findProjectRoot() {
        var currentDir = Paths.get(".")
                .toAbsolutePath()
                .normalize();

        var parentDir = currentDir.getParent();

        while (Files.exists(parentDir.resolve("settings.gradle")) ||
                Files.exists(parentDir.resolve("settings.gradle.kts"))) {
            currentDir = parentDir;
            parentDir = parentDir.getParent();
        }

        return currentDir;
    }

    private static Path createInitScript() throws IOException {
        var initScriptPath = Files.createTempFile("init", ".gradle.kts");
        var initGradleStream = GradleBspServerLauncher.class.getResourceAsStream("/init.gradle.kts");
        try (var reader = new BufferedReader(new InputStreamReader(initGradleStream))) {
            var initGradleLines = reader.lines()
                    .map(line -> line.replace("%%BSP_PLUGIN_VERSION%%", BuildInfo.version))
                    .collect(Collectors.toList());
            Files.write(initScriptPath, initGradleLines);
        }
        return initScriptPath;
    }

    private <T> T getCustomModel(ProjectConnection connection, Class<T> customModelClass) {
        return connection.model(customModelClass)
            .addArguments("--init-script", initScriptPath.toString())
            .get();
    }

    @Override
    public Integer call() throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((t, ex) -> {
            logger.error("Uncaught exception in thread {}", t.getName(), ex);
        });

        var connector = GradleConnector.newConnector()
                .forProjectDirectory(findProjectRoot().toFile());

        try (ProjectConnection connection = connector.connect()) {
            var workspaceModel = getCustomModel(connection, BspWorkspace.class);

            System.err.println(workspaceModel);

            // var server = new GradleBspServer(null);

            // var launcher = new Launcher.Builder<BuildClient>()
            // .setInput(System.in)
            // .setOutput(System.out)
            // .setLocalService(server)
            // .setRemoteInterface(BuildClient.class)
            // .create();

            // server.onConnectWithClient(launcher.getRemoteProxy());
            // launcher.startListening().get();
            // var exitCode = server.getExitCode();
            // return exitCode;
            return 0;
        }
    }
}
