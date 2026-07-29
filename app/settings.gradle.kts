val gitHooksDir = File(rootDir, "../.git/hooks")
if (gitHooksDir.exists()) {
    val target = File(gitHooksDir, "pre-commit")
    val source = File(rootDir, "../.githooks/pre-commit")
    if (!target.exists() || target.readText() != source.readText()) {
        source.copyTo(target, overwrite = true)
        target.setExecutable(true)
    }
}

pluginManagement {
    includeBuild("build-logic")

    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

includeBuild("../shared")

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

rootProject.name = "app"
include(
    ":app:android",
    ":app:shared",
    ":core:common",
    ":core:ui-library",
    ":core:navigation",
    ":core:network",
    ":core:user",
    ":core:vehicle",
    ":core:issue",
    ":feature:sign-in:api",
    ":feature:sign-in:impl",
    ":feature:issues:api",
    ":feature:issues:impl",
    ":feature:profile:api",
    ":feature:profile:impl",
)
