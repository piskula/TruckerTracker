package com.momosi.trucktrack

import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.extensions.FailOnSeverity
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class DetektPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("dev.detekt")
        dependencies.add("detektPlugins", "com.momosi.trucktrack.buildlogic:trucktrack-detekt-rules")

        extensions.configure<DetektExtension> {
            buildUponDefaultConfig.set(true)
            config.setFrom(
                rootProject.layout.projectDirectory.file("../.detekt/detekt.yml"),
                rootProject.layout.projectDirectory.file("detekt-rules.yml"),
            )
            basePath.set(rootProject.layout.projectDirectory.dir(".."))
            parallel.set(true)
            failOnSeverity.set(FailOnSeverity.Error)
        }
    }
}
