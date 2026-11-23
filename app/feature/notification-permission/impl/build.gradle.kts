plugins {
    alias(libs.plugins.trucktrack.feature.impl)
}

compose.resources {
    packageOfResClass = "com.momosi.trucktrack.feature.notificationpermission.impl.resources"
    generateResClass = always
}

kotlin {
    android {
        namespace = "com.momosi.trucktrack.feature.notificationpermission.impl"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.notificationPermission.api)
            implementation(projects.core.common)
            implementation(projects.core.push)
            implementation(projects.core.user)
        }
    }
}
