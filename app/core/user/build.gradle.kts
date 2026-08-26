plugins {
    alias(libs.plugins.trucktrack.library)
    alias(libs.plugins.trucktrack.koin)
    alias(libs.plugins.trucktrack.ktor)
}

kotlin {
    android {
        namespace = "com.momosi.trucktrack.core.user"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(libs.oidc.core)
            api(libs.oidc.appsupport)
        }
    }
}
