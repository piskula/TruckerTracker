plugins {
    alias(libs.plugins.trucktrack.feature.impl)
}

compose.resources {
    packageOfResClass = "com.momosi.trucktrack.feature.issues.impl.resources"
    generateResClass = always
}

kotlin {
    android {
        namespace = "com.momosi.trucktrack.feature.issues.impl"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.issues.api)
            implementation(projects.feature.profile.api)
            implementation(projects.feature.checklist.api)
            implementation(projects.core.common)
            implementation(projects.core.user)
            implementation(projects.core.issue)
            implementation(projects.core.vehicle)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.coil.compose)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.androidx.paging.compose)
        }
    }
}
