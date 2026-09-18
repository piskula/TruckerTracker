package com.momosi.trucktrack

import com.momosi.trucktrack.utils.androidMainDependencies
import com.momosi.trucktrack.utils.commonMainDependencies
import com.momosi.trucktrack.utils.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("DEPRECATION")
class ComposePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.compose")
            apply("org.jetbrains.kotlin.plugin.compose")
        }

        val compose = extensions.getByType<ComposeExtension>().dependencies

        commonMainDependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(libs.findLibrary("compose-ui-tooling-preview").get())
            implementation(libs.findBundle("navigation3").get())
        }

        bridgeComposeResourcesForAndroid()
    }

    private fun Project.bridgeComposeResourcesForAndroid() {
        afterEvaluate {
            val composeExt = extensions.getByType<ComposeExtension>()
            val resourcesExt = (composeExt as ExtensionAware)
                .extensions.findByType(ResourcesExtension::class.java)
                ?: return@afterEvaluate

            val packageName = resourcesExt.packageOfResClass
            if (packageName.isNullOrEmpty()) return@afterEvaluate

            val preparedDir = layout.buildDirectory.dir(
                "generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"
            )
            val outputDir = layout.buildDirectory.dir(
                "generated/compose/androidComposeResources"
            )

            val syncTask = tasks.register<Sync>("syncComposeResourcesForAndroid") {
                dependsOn(tasks.matching {
                    it.name.startsWith("prepareComposeResourcesTask") ||
                        it.name.startsWith("convertXmlValueResources") ||
                        it.name.startsWith("copyNonXmlValueResources")
                })
                from(preparedDir)
                into(outputDir.map { it.dir("composeResources/$packageName") })
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.getByName("androidMain") {
                    resources.srcDir(syncTask.map { outputDir.get() })
                }
            }
        }
    }
}
