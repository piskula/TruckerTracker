plugins {
    alias(libs.plugins.trucktrack.feature.impl)
}

compose.resources {
    packageOfResClass = "com.momosi.trucktrack.feature.signin.impl.resources"
    generateResClass = always
}

kotlin {
    android {
        namespace = "com.momosi.trucktrack.feature.signin.impl"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.signIn.api)
            implementation(projects.core.common)
            implementation(projects.core.user)
        }
    }
}
