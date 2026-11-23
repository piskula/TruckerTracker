plugins {
    alias(libs.plugins.trucktrack.feature.impl)
}

compose.resources {
    packageOfResClass = "com.momosi.trucktrack.feature.profile.impl.resources"
    generateResClass = always
}

kotlin {
    android {
        namespace = "com.momosi.trucktrack.feature.profile.impl"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.profile.api)
            implementation(projects.feature.signIn.api)
            implementation(projects.core.common)
            implementation(projects.core.user)
            implementation(projects.core.push)
        }
    }
}
