plugins {
    java
    application
    alias(libs.plugins.gradleJavaConventions)
    alias(libs.plugins.gradleBuildInfo)
    // This isn't working yet because Gradle can't install the native-image tool for some reason
    // alias(libs.plugins.graalVmNativeImage)
}

group = "com.opencastsoftware.gradle"

buildInfo {
    packageName.set("com.opencastsoftware.gradle.bsp")
    properties.set(
        mapOf("version" to project.version.toString(), "bspVersion" to libs.versions.bsp4j.get())
    )
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
}

spotless { java { targetExclude("build/**") } }

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
        // See above
        // vendor.set(JvmVendorSpec.GRAAL_VM)
    }
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
    // Logging
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
