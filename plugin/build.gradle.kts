plugins {
    `java-gradle-plugin`
    `jacoco-report-aggregation`
    alias(libs.plugins.gradleJavaConventions)
    // TODO: remove this once https://github.com/gradle/gradle/issues/17559 is fixed
    alias(libs.plugins.gradleBuildInfo)
}

group = "com.opencastsoftware.gradle"

description =
    "A Gradle plugin providing connection file generation and custom tooling models for the Build Server Protocol"

java { toolchain { languageVersion.set(JavaLanguageVersion.of(11)) } }

val pluginId = "com.opencastsoftware.gradle.bsp"

buildInfo {
    packageName.set("com.opencastsoftware.gradle.bsp")
    properties.set(
        mapOf(
            "pluginId" to pluginId,
            "version" to project.version.toString(),
            "bspVersion" to libs.versions.bsp4j.get(),
            "jsonJavaVersion" to libs.versions.jsonJava.get()
        )
    )
}

dependencies {
    api(project(":gradle-bsp-model"))
    compileOnly(libs.jsonJava)
}

gradlePlugin {
    plugins.create("gradleBsp") {
        id = pluginId
        displayName = "Gradle Build Server Protocol Plugin"
        implementationClass = "com.opencastsoftware.gradle.bsp.BspPlugin"
    }
}

testing {
    suites {
        getByName<JvmTestSuite>("test") {
            useJUnitJupiter()

            dependencies { implementation(libs.hamcrest) }
        }

        register<JvmTestSuite>("functionalTest") {
            sources {
                gradlePlugin.testSourceSets(this)

                dependencies {
                    implementation(project())
                    implementation(libs.junitJupiter)
                    implementation(libs.hamcrest)
                    implementation(libs.jsonJava)
                }

                targets.all {
                    testTask.configure {
                        dependsOn(":gradle-bsp-model:publishToMavenLocal")
                        dependsOn(":gradle-bsp-server:publishToMavenLocal")
                    }
                }
            }
        }
    }
}

tasks.check { dependsOn(testing.suites.named("functionalTest")) }

mavenPublishing {
    coordinates("com.opencastsoftware.gradle", "gradle-bsp-plugin", project.version.toString())

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
