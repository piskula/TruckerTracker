plugins {
    alias(libs.plugins.trucktrack.library)
    alias(libs.plugins.trucktrack.koin)
    alias(libs.plugins.trucktrack.test)
}

kotlin {
    android {
        namespace = "com.momosi.trucktrack.core.applinks"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.user)
            api(libs.kotlinx.coroutines.core)
            implementation(projects.core.common)
            implementation(libs.ktor.http)
        }
    }
}
