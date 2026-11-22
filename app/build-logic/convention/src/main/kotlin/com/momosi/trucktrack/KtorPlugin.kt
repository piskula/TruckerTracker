package com.momosi.trucktrack

import com.momosi.trucktrack.utils.androidMainDependencies
import com.momosi.trucktrack.utils.commonMainDependencies
import com.momosi.trucktrack.utils.iosMainDependencies
import com.momosi.trucktrack.utils.libs
import org.gradle.api.Plugin
import org.gradle.api.Project

class KtorPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.plugin.serialization")
            apply("com.google.devtools.ksp")
            apply("de.jensklingenberg.ktorfit")
        }

        commonMainDependencies {
            implementation(libs.findLibrary("ktor-client-core").get())
            implementation(libs.findLibrary("ktor-client-content-negotiation").get())
            implementation(libs.findLibrary("ktor-client-auth").get())
            implementation(libs.findLibrary("ktor-client-logging").get())
            implementation(libs.findLibrary("ktor-serialization-kotlinx-json").get())
            implementation(libs.findLibrary("kotlinx-serialization-json-mobile").get())
            implementation(libs.findLibrary("ktorfit-lib").get())
        }
        androidMainDependencies {
            implementation(libs.findLibrary("ktor-client-okhttp").get())
        }
        iosMainDependencies {
            implementation(libs.findLibrary("ktor-client-darwin").get())
        }
    }
}
