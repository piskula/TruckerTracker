package com.momosi.trucktrack

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.momosi.trucktrack.utils.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class TestPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.withPlugin("com.android.kotlin.multiplatform.library") {
            extensions.configure<KotlinMultiplatformExtension> {
                targets.withType<KotlinMultiplatformAndroidLibraryTarget> {
                    withHostTestBuilder {}
                }

                sourceSets.commonTest.dependencies {
                    implementation(libs.findLibrary("kotlin-test").get())
                    implementation(libs.findLibrary("kotlinx-coroutines-test").get())
                }
            }
        }
    }
}
