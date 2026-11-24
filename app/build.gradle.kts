import dev.detekt.gradle.Detekt

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization.mobile) apply false
    alias(libs.plugins.parcelize) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.trucktrack.detekt)
}

tasks.register<Detekt>("checkIllustrationAccess") {
    description = "Checks that drawable illustrations are accessed through TruckTrackIllustrations."
    group = "verification"
    buildUponDefaultConfig.set(false)
    config.setFrom(layout.projectDirectory.file("detekt-rules.yml"))
    setSource(fileTree(projectDir) {
        include("**/src/**/*.kt")
        exclude("**/build/**", "build-logic/**", "detekt-rules/**")
    })
}
