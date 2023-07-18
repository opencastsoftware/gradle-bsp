plugins {
    java
    application
    alias(libs.plugins.gradleJavaConventions)
    alias(libs.plugins.gradleBuildInfo)
    // This isn't working yet because Gradle can't install the native-image tool for some reason
    // alias(libs.plugins.graalVmNativeImage)
}

group = "com.opencastsoftware.gradle"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
        // See above
        // vendor.set(JvmVendorSpec.GRAAL_VM)
    }
}

buildInfo {
    packageName.set("com.opencastsoftware.gradle.bsp.server")
    properties.set(
        mapOf("version" to project.version.toString(), "bspVersion" to libs.versions.bsp4j.get())
    )
}

dependencies {
    // Gradle Tooling API model
    implementation(project(":gradle-bsp-model"))
    // Gradle Tooling API
    implementation(libs.gradleToolingApi)
    // Build Server Protocol bindings
    implementation(libs.bsp4j)
    // Command line argument parsing
    implementation(libs.picocli)
    annotationProcessor(libs.picocliCodegen)
    // Unix Socket Support
    // We must use a pom type dependency here, because Gradle's platform()
    // doesn't actually resolve the transitive dependencies so they would
    // have to be added separately
    implementation("${libs.junixSocket.get()}@pom") { isTransitive = true }
    // Logging
    implementation(libs.bundles.slf4j)
    runtimeOnly(libs.logback)
}

tasks.withType<JavaCompile> {
    val compilerArgs = options.compilerArgs
    compilerArgs.add("-Aproject=${project.group}/${project.name}")
}

application { mainClass.set("com.opencastsoftware.gradle.bsp.server.GradleBspServerLauncher") }

tasks.named("run") {
    dependsOn(":gradle-bsp-model:publishToMavenLocal")
    dependsOn(":gradle-bsp-plugin:publishToMavenLocal")
}

mavenPublishing {
    coordinates("com.opencastsoftware.gradle", "gradle-bsp-server", project.version.toString())

    pom {
        description.set(project.description)
        url.set("https://github.com/opencastsoftware/gradle-bsp")
        inceptionYear.set("2023")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        organization {
            name.set("Opencast Software Europe Ltd")
            url.set("https://opencastsoftware.com")
        }
        developers {
            developer {
                id.set("DavidGregory084")
                name.set("David Gregory")
                organization.set("Opencast Software Europe Ltd")
                organizationUrl.set("https://opencastsoftware.com/")
                timezone.set("Europe/London")
                url.set("https://github.com/DavidGregory084")
            }
        }
        ciManagement {
            system.set("Github Actions")
            url.set("https://github.com/opencastsoftware/gradle-bsp/actions")
        }
        issueManagement {
            system.set("GitHub")
            url.set("https://github.com/opencastsoftware/gradle-bsp/issues")
        }
        scm {
            connection.set("scm:git:https://github.com/opencastsoftware/gradle-bsp.git")
            developerConnection.set("scm:git:git@github.com:opencastsoftware/gradle-bsp.git")
            url.set("https://github.com/opencastsoftware/gradle-bsp")
        }
    }
}
