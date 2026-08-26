plugins {
    `kotlin-dsl`
}

group = "com.momosi.trucktrack.buildlogic"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.compose.multiplatform.gradle.plugin)
    implementation(libs.spotless.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.ktorfit.gradle.plugin)
    implementation(project(":detekt"))
}

gradlePlugin {
    plugins {
        register("trucktrack.library") {
            id = "trucktrack.library"
            implementationClass = "com.momosi.trucktrack.LibraryPlugin"
        }
        register("trucktrack.feature.api") {
            id = "trucktrack.feature.api"
            implementationClass = "com.momosi.trucktrack.FeatureApiPlugin"
        }
        register("trucktrack.feature.impl") {
            id = "trucktrack.feature.impl"
            implementationClass = "com.momosi.trucktrack.FeatureImplPlugin"
        }
        register("trucktrack.koin") {
            id = "trucktrack.koin"
            implementationClass = "com.momosi.trucktrack.KoinPlugin"
        }
        register("trucktrack.spotless") {
            id = "trucktrack.spotless"
            implementationClass = "com.momosi.trucktrack.SpotlessPlugin"
        }
        register("trucktrack.compose") {
            id = "trucktrack.compose"
            implementationClass = "com.momosi.trucktrack.ComposePlugin"
        }
        register("trucktrack.ktor") {
            id = "trucktrack.ktor"
            implementationClass = "com.momosi.trucktrack.KtorPlugin"
        }
        register("trucktrack.android.signing") {
            id = "trucktrack.android.signing"
            implementationClass = "com.momosi.trucktrack.AndroidSigningPlugin"
        }
        register("trucktrack.firebase") {
            id = "trucktrack.firebase"
            implementationClass = "com.momosi.trucktrack.FirebasePlugin"
        }
    }
}
