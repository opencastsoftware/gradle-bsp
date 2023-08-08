initscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        classpath("com.opencastsoftware.gradle:gradle-bsp-plugin:%%BSP_PLUGIN_VERSION%%")
    }
}

allprojects {
    repositories {
        maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
    }
}

projectsEvaluated {
    rootProject.subprojects {
        apply<com.opencastsoftware.gradle.bsp.BspPlugin>()
    }
}
