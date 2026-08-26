package com.momosi.trucktrack

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.momosi.trucktrack.utils.CompileSdk
import com.momosi.trucktrack.utils.MinSdk
import com.momosi.trucktrack.utils.configureKotlin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class LibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.kotlin.multiplatform.library")
            apply("trucktrack.spotless")
            apply("trucktrack.detekt")
        }

        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(25)

            applyDefaultHierarchyTemplate()

            iosArm64()
            iosSimulatorArm64()

            targets.withType<KotlinMultiplatformAndroidLibraryTarget> {
                compileSdk = CompileSdk
                minSdk = MinSdk
            }
        }

        configureKotlin()
    }
}
