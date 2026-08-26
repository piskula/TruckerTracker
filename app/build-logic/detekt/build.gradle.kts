plugins {
    `kotlin-dsl`
}

group = "com.momosi.trucktrack.buildlogic"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.detekt.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("trucktrack.detekt") {
            id = "trucktrack.detekt"
            implementationClass = "com.momosi.trucktrack.DetektPlugin"
        }
    }
}
