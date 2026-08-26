plugins {
    alias(libs.plugins.trucktrack.library)
    alias(libs.plugins.trucktrack.koin)
    alias(libs.plugins.trucktrack.firebase)
    alias(libs.plugins.trucktrack.ktor)
}

kotlin {
    android {
        namespace = "com.momosi.trucktrack.core.common"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kermit)
            implementation(libs.kotlinx.datetime)
            implementation("com.momosi.trucktrack:shared")
        }
        androidMain.dependencies {
            implementation(libs.androidx.appcompat)
        }
    }
}
