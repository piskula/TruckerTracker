plugins {
    alias(libs.plugins.trucktrack.library)
    alias(libs.plugins.trucktrack.compose)
    alias(libs.plugins.trucktrack.koin)
}

compose.resources {
    packageOfResClass = "com.momosi.trucktrack.app.resources"
}

kotlin {
    android {
        namespace = "com.momosi.trucktrack.app"
    }

    iosArm64 {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.coil.singleton)
            implementation(libs.coil.network.ktor)
            implementation(projects.core.appLinks)
            implementation(projects.core.common)
            implementation(projects.core.navigation)
            implementation(projects.core.network)
            implementation(projects.core.uiLibrary)
            implementation(projects.core.user)
            implementation(projects.core.vehicle)
            implementation(projects.core.issue)
            implementation(projects.core.checklist)

            implementation(projects.feature.signIn.api)
            implementation(projects.feature.signIn.impl)
            implementation(projects.feature.issues.api)
            implementation(projects.feature.issues.impl)
            implementation(projects.feature.profile.impl)
        }
    }
}
