plugins {
    alias(libs.plugins.trucktrack.library)
    alias(libs.plugins.trucktrack.compose)
}

compose.resources {
    packageOfResClass = "com.momosi.trucktrack.core.uilibrary.resources"
    generateResClass = always
}

kotlin {
    android {
        namespace = "com.momosi.trucktrack.core.uilibrary"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.coil.core)
            implementation(libs.coil.compose)
            implementation(projects.core.common)
            api(projects.core.vehicle)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}
