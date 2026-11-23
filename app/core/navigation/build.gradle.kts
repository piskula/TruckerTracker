plugins {
    alias(libs.plugins.trucktrack.library)
    alias(libs.plugins.trucktrack.compose)
    alias(libs.plugins.trucktrack.test)
}

kotlin {
    android {
        namespace = "com.momosi.trucktrack.core.navigation"
    }
}
