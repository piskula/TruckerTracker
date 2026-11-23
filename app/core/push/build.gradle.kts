plugins {
    alias(libs.plugins.trucktrack.library)
    alias(libs.plugins.trucktrack.test)
    alias(libs.plugins.trucktrack.koin)
    alias(libs.plugins.trucktrack.compose)
    alias(libs.plugins.trucktrack.ktor)
}

compose.resources {
    packageOfResClass = "com.momosi.trucktrack.core.push.resources"
}

kotlin {
    android {
        namespace = "com.momosi.trucktrack.core.push"
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api("com.momosi.trucktrack:shared")
            implementation(projects.core.common)
            implementation(projects.core.network)
            implementation(projects.core.user)
        }
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.messaging)
            implementation(libs.androidx.activity.compose)
            implementation(libs.android.device.names)
        }
        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
        }
    }
}
