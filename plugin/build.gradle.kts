import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.gradleJavaConventions)
}

group = "com.opencastsoftware"

description = "A Gradle plugin providing models for the Build Server Protocol"

java { toolchain { languageVersion.set(JavaLanguageVersion.of(11)) } }

dependencies {
    api(project(":gradle-bsp-model"))
    testImplementation(libs.junitJupiter)
}

gradlePlugin {
    plugins.create("gradleBsp") {
        id = "com.opencastsoftware.gradle.bsp"
        displayName = "Gradle Build Server Protocol Plugin"
        implementationClass = "com.opencastsoftware.gradle.bsp.GradleBspPlugin"
    }
}

testing {
    suites {
        register<JvmTestSuite>("functionalTest") {
            sources {
                gradlePlugin.testSourceSets(this)

                dependencies {
                    implementation(project())
                    implementation(libs.junitJupiter)
                }
            }
        }
    }
}

tasks.check { dependsOn(testing.suites.named("functionalTest")) }

mavenPublishing {
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
            connection.set(
                "scm:git:https://github.com/opencastsoftware/gradle-bsp.git"
            )
            developerConnection.set(
                "scm:git:git@github.com:opencastsoftware/gradle-bsp.git"
            )
            url.set("https://github.com/opencastsoftware/gradle-bsp")
        }
    }
}