plugins {
    alias(libs.plugins.trucktrack.library)
    alias(libs.plugins.trucktrack.koin)
    alias(libs.plugins.trucktrack.ktor)
}

kotlin {
    android {
        namespace = "com.momosi.trucktrack.core.issue"
    }

    sourceSets {
        commonMain.dependencies {
            api("com.momosi.trucktrack:shared")
            implementation(projects.core.common)
            implementation(projects.core.network)
            implementation(projects.core.user)
            implementation(projects.core.vehicle)
            implementation(libs.androidx.paging.common)
        }
    }
}
