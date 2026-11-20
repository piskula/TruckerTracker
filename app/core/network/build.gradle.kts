plugins {
    alias(libs.plugins.trucktrack.library)
    alias(libs.plugins.trucktrack.koin)
    alias(libs.plugins.trucktrack.ktor)
}

kotlin {
    android {
        namespace = "com.momosi.trucktrack.core.network"
        withHostTestBuilder {}
    }
    sourceSets {
        commonMain.dependencies {
            api("com.momosi.trucktrack:shared")
            implementation(projects.core.common)
            implementation(projects.core.user)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
