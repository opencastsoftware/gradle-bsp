plugins {
    java
    application
    // This isn't working yet because Gradle can't install the native-image tool for some reason
    // alias(libs.plugins.graalVmNativeImage)
}

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

dependencies {
    // Gradle Tooling API model
    implementation(project(":model"))
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

application {
    mainClass.set("com.opencastsoftware.gradle.bsp.GradleBspServerLauncher")
}