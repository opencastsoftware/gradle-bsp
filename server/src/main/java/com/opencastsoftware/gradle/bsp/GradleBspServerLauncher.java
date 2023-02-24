package com.opencastsoftware.gradle.bsp;

import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.gradle.BasicGradleProject;
import org.gradle.tooling.model.idea.IdeaProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.epfl.scala.bsp4j.BuildClient;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "Gradle BSP Server", mixinStandardHelpOptions = true)
public class GradleBspServerLauncher implements Callable<Integer> {
    private static Logger logger = LoggerFactory.getLogger(GradleBspServerLauncher.class);

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GradleBspServerLauncher()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((t, ex) -> {
            logger.error("Uncaught exception in thread {}", t.getName(), ex);
        });

        ProjectConnection gradleConnection = GradleConnector.newConnector()
            .forProjectDirectory(Paths.get(".").toFile())
            .useGradleVersion("8.1-20230208002420+0000")
            .connect();

        BasicGradleProject projectModel = gradleConnection.getModel(BasicGradleProject.class);

        var server = new GradleBspServer(null);

        var launcher = new Launcher.Builder<BuildClient>()
            .setInput(System.in)
            .setOutput(System.out)
            .setLocalService(server)
            .setRemoteInterface(BuildClient.class)
            .create();

        server.onConnectWithClient(launcher.getRemoteProxy());
        launcher.startListening().get();
        var exitCode = server.getExitCode();
        return exitCode;
    }
}
