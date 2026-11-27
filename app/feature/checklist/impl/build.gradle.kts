plugins {
    alias(libs.plugins.trucktrack.feature.impl)
    alias(libs.plugins.trucktrack.test)
}

compose.resources {
    packageOfResClass = "com.momosi.trucktrack.feature.checklist.impl.resources"
    generateResClass = always
}

kotlin {
    android {
        namespace = "com.momosi.trucktrack.feature.checklist.impl"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.checklist.api)
            implementation(projects.core.common)
            implementation(projects.core.checklist)
            implementation(projects.core.vehicle)
            implementation(libs.kotlinx.collections.immutable)
        }
    }
}
