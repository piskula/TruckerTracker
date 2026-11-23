package com.momosi.trucktrack

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.DetektCreateBaselineTask
import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.extensions.FailOnSeverity
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTreeElement
import org.gradle.api.specs.Spec
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

class DetektPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("dev.detekt")

        extensions.configure<DetektExtension> {
            buildUponDefaultConfig.set(true)
            config.setFrom(rootProject.layout.projectDirectory.file("../.detekt/detekt.yml"))
            basePath.set(rootProject.layout.projectDirectory.dir(".."))
            parallel.set(true)
            failOnSeverity.set(FailOnSeverity.Error)
        }

        val generatedSourcesDirectory = layout.buildDirectory.dir("generated").get().asFile
        val generatedSources = Spec<FileTreeElement> { it.file.startsWith(generatedSourcesDirectory) }
        val sourceGenerationTasks = tasks.named { it.startsWith("ksp") }
        tasks.withType<Detekt>().configureEach {
            exclude(generatedSources)
            mustRunAfter(sourceGenerationTasks)
        }
        tasks.withType<DetektCreateBaselineTask>().configureEach {
            exclude(generatedSources)
            mustRunAfter(sourceGenerationTasks)
        }
    }
}