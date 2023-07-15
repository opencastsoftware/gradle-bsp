plugins {
    java
    alias(libs.plugins.gradleJavaConventions)
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(11)) } }
