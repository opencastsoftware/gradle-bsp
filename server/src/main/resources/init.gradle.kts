initscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        classpath("com.opencastsoftware.gradle:gradle-bsp-plugin:%%BSP_PLUGIN_VERSION%%")
    }
}

allprojects { apply<com.opencastsoftware.gradle.bsp.GradleBspPlugin>() }
