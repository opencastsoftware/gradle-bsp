/*
 * SPDX-FileCopyrightText:  © 2023 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.gradle.bsp.server;

import ch.epfl.scala.bsp4j.BuildClient;
import com.opencastsoftware.gradle.bsp.server.util.DaemonThreadFactory;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Command(name = "Gradle BSP Server", version = BuildInfo.version, mixinStandardHelpOptions = true)
public class GradleBspServerLauncher implements Callable<Integer> {
    private static Logger logger = LoggerFactory.getLogger(GradleBspServerLauncher.class);

    private final Path initScriptPath;

    @ArgGroup(exclusive = true, multiplicity = "1")
    private Transport transport;

    private static class Transport {
        @Option(names = { "--stdio" }, required = true, description = "Use standard input / output streams.")
        boolean useStdio;

        @Option(names = { "--socket" }, required = true, description = "Use TCP socket on the given port.")
        int socketPort;

        @Option(names = { "--pipe" }, required = true, description = "Use named pipe at the given location.")
        String pipeName;
    }

    public GradleBspServerLauncher(Path initScriptPath) {
        this.initScriptPath = initScriptPath;
    }

    public static void main(String[] args) throws IOException {
        var launcher = new GradleBspServerLauncher(createInitScript());
        var commandLine = new CommandLine(launcher);
        System.exit(commandLine.execute(args));
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

    private File findProjectRoot() {
        var currentDir = Paths.get(".")
                .toAbsolutePath()
                .normalize();

        var parentDir = currentDir.getParent();

        while (Files.exists(parentDir.resolve("settings.gradle")) ||
                Files.exists(parentDir.resolve("settings.gradle.kts"))) {
            currentDir = parentDir;
            parentDir = parentDir.getParent();
        }

        return currentDir.toFile();
    }

    private int listenOn(InputStream in, OutputStream out) throws InterruptedException, ExecutionException {
        var connector = GradleConnector.newConnector()
                .forProjectDirectory(findProjectRoot())
                .useBuildDistribution();

        try (ProjectConnection connection = connector.connect()) {
            var server = new GradleBspServer(connection, initScriptPath);

            var threadFactory = DaemonThreadFactory.create(logger, "gradle-buildserver-listener-%d");
            var executor = Executors.newSingleThreadExecutor(threadFactory);

            var launcher = new Launcher.Builder<BuildClient>()
                    .setLocalService(server)
                    .setRemoteInterface(BuildClient.class)
                    .setInput(in)
                    .setOutput(out)
                    .setExecutorService(executor)
                    .traceMessages(new PrintWriter(System.err))
                    .validateMessages(true)
                    .create();

            server.onConnectWithClient(launcher.getRemoteProxy());

            launcher.startListening().get();

            var exitCode = server.getExitCode();

            logger.info("Server exiting with exit code {}", exitCode);

            return exitCode;
        }
    }

    @Override
    public Integer call() throws Exception {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        Thread.setDefaultUncaughtExceptionHandler((t, ex) -> {
            logger.error("Uncaught exception in thread {}", t.getName(), ex);
        });

        if (transport.useStdio) {
            logger.debug("Using standard input/output streams");
            return listenOn(System.in, System.out);
        } else if (transport.pipeName != null) {
            logger.debug("Using pipe {}", transport.pipeName);
            var sockAddress = AFUNIXSocketAddress.of(new File(transport.pipeName));
            try (Socket socket = AFUNIXSocket.connectTo(sockAddress)) {
                return listenOn(socket.getInputStream(), socket.getOutputStream());
            }
        } else {
            logger.debug("Using local socket {}", transport.socketPort);
            try (Socket socket = new Socket("127.0.0.1", transport.socketPort)) {
                return listenOn(socket.getInputStream(), socket.getOutputStream());
            }
        }
    }
}
