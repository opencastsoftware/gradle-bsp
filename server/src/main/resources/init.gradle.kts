allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    apply(plugin = "com.opencastsoftware.gradle.bsp")
}
