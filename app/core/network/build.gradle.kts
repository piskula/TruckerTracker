plugins {
    alias(libs.plugins.trucktrack.library)
    alias(libs.plugins.trucktrack.test)
    alias(libs.plugins.trucktrack.koin)
    alias(libs.plugins.trucktrack.ktor)
}

kotlin {
    android {
        namespace = "com.momosi.trucktrack.core.network"
    }
    sourceSets {
        commonMain.dependencies {
            api("com.momosi.trucktrack:shared")
            implementation(projects.core.common)
            implementation(projects.core.user)
        }
        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
        }
    }
}
