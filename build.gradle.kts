import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

allprojects {
    group = "sk.momosilabs.truckTrack"
    version = "0.0.3-SNAPSHOT"
}

subprojects {
    // Every module gets Kotlin + Spring BOM-managed dependency resolution.
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.spring.dependency-management")

    repositories {
        mavenCentral()
    }

    configure<KotlinJvmProjectExtension> {
        jvmToolchain(21)
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict", "-java-parameters")
        }
    }

    // SpringBootPlugin.BOM_COORDINATES is a constant on the plugin class itself —
    // no need to fish the version out of the catalog inside a receiver lambda.
    configure<DependencyManagementExtension> {
        imports {
            mavenBom(SpringBootPlugin.BOM_COORDINATES)
        }
    }
}
