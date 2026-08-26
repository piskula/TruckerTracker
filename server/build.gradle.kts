plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.detekt)
}

allprojects {
    group = "sk.momosilabs.truckTrack"
    version = "0.0.6"
}

subprojects {
    apply(plugin = "dev.detekt")

    extensions.configure<dev.detekt.gradle.extensions.DetektExtension> {
        buildUponDefaultConfig.set(true)
        config.setFrom(rootProject.layout.projectDirectory.file("../.detekt/detekt.yml"))
        basePath.set(rootProject.layout.projectDirectory.dir(".."))
        parallel.set(true)
        failOnSeverity.set(dev.detekt.gradle.extensions.FailOnSeverity.Error)
    }
}
