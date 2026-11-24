pluginManagement {
    includeBuild("build-logic")

    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

plugins {
    id("trucktrack.git-hooks")
}

includeBuild("../shared")
includeBuild("detekt-rules")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "trucktrack-app"
include(
    ":app:android",
    ":app:shared",
    ":core:common",
    ":core:app-links",
    ":core:ui-library",
    ":core:navigation",
    ":core:network",
    ":core:user",
    ":core:vehicle",
    ":core:issue",
    ":core:push",
    ":core:checklist",
    ":feature:sign-in:api",
    ":feature:sign-in:impl",
    ":feature:issues:api",
    ":feature:issues:impl",
    ":feature:profile:api",
    ":feature:profile:impl",
    ":feature:notification-permission:api",
    ":feature:notification-permission:impl",
    ":feature:checklist:api",
    ":feature:checklist:impl",
)
