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
    packageName.set("com.opencastsoftware.gradle.bsp")
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

application { mainClass.set("com.opencastsoftware.gradle.bsp.GradleBspServerLauncher") }

tasks.named("run") {
    dependsOn(":gradle-bsp-model:publishToMavenLocal")
    dependsOn(":gradle-bsp-plugin:publishToMavenLocal")
}

tasks.withType<Jar> { dependsOn("generateBuildInfo") }
