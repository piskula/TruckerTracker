---
name: create-feature-module
description: Use when creating a new product feature from scratch. Triggered by phrases like "create a new feature", "add a feature module", "scaffold a feature", "new feature module", "create feature/*/api", "create feature/*/impl".
---

# Skill: Create a New Feature Module

> Scaffolds a complete `app/feature/<name>/api` + `app/feature/<name>/impl` module pair from scratch, wired into the app. All modules are KMP with `commonMain` source sets.

## Triggers

Load this skill when the task matches **any** of these:
- Adding a brand-new product feature that doesn't exist yet
- Creating a new `feature/*/api` or `feature/*/impl` module
- Task says "create a new feature", "add a feature module", or "scaffold a feature"

## Prerequisites

- `app/settings.gradle.kts` is accessible — the new module paths must be added there (this is `app/`'s own settings file, not the thin composite-build one at the repo root)
- `app/app/shared/build.gradle.kts` and `app/app/shared/src/commonMain/kotlin/.../app/di/AppModule.kt` are accessible for wiring

## Steps

### 1. Create directory structure

```
app/feature/<name>/
  api/
    build.gradle.kts
    src/commonMain/kotlin/com/momosi/trucktrack/feature/<name>/api/
  impl/
    build.gradle.kts
    src/commonMain/kotlin/com/momosi/trucktrack/feature/<name>/impl/
```

### 2. Create `app/feature/<name>/api/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.trucktrack.feature.api)
}

kotlin {
    android {
        namespace = "com.momosi.trucktrack.feature.<name>.api"
    }
}
```

### 3. Create the entry-point nav key in `api/`

File: `app/feature/<name>/api/src/commonMain/kotlin/.../feature/<name>/api/<Name>NavKey.kt`

```kotlin
package com.momosi.trucktrack.feature.<name>.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object <Name>NavKey : NavKey
```

For nav keys with parameters, use `data class` instead of `data object`:
```kotlin
@Serializable
data class <Name>NavKey(val id: Long) : NavKey
```

### 4. Create `app/feature/<name>/impl/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.trucktrack.feature.impl)
}

kotlin {
    android {
        namespace = "com.momosi.trucktrack.feature.<name>.impl"
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.<name>.api)
            implementation(libs.kotlinx.collections.immutable)
            // Add as needed: projects.core.issue, projects.core.user, projects.core.vehicle
        }
    }
}
```

### 5. Create screen files — follow `add-screen-to-feature/SKILL.md` for each screen

Package: `app/feature/<name>/impl/src/commonMain/kotlin/.../feature/<name>/impl/<screen>/`
Required files: `<Screen>Screen.kt`, `<Screen>ViewModel.kt`, `<Screen>State.kt`, `<Screen>Action.kt`
Optional: `<Screen>Event.kt` (only when one-shot VM→UI signals are needed)

### 6. Create the entry provider in `impl/navigation/`

File: `app/feature/<name>/impl/src/commonMain/kotlin/.../feature/<name>/impl/navigation/<Name>EntryProvider.kt`

```kotlin
package com.momosi.trucktrack.feature.<name>.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.momosi.trucktrack.core.navigation.Navigator
import com.momosi.trucktrack.feature.<name>.api.<Name>NavKey
import com.momosi.trucktrack.feature.<name>.impl.<screen>.<Screen>Screen

fun EntryProviderScope<NavKey>.<name>Entries(navigator: Navigator) {
    entry<<Name>NavKey> {
        <Screen>Screen(
            onBack = navigator::goBack,
        )
    }
}
```

### 7. Create the Koin module in `impl/di/`

File: `app/feature/<name>/impl/src/commonMain/kotlin/.../feature/<name>/impl/di/<Name>Module.kt`

```kotlin
package com.momosi.trucktrack.feature.<name>.impl.di

import com.momosi.trucktrack.feature.<name>.impl.<screen>.<Screen>ViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val <name>Module = module {
    viewModel { <Screen>ViewModel(get()) }
}
```

### 8. Register modules in `app/settings.gradle.kts`

Add to the `include(...)` block:
```kotlin
":feature:<name>:api",
":feature:<name>:impl",
```

### 9. Wire into `app:shared`

In `app/app/shared/build.gradle.kts`, add to `commonMain.dependencies`:
```kotlin
implementation(projects.feature.<name>.impl)
```

In `app/app/shared/src/commonMain/kotlin/.../app/di/AppModule.kt`, add `<name>Module` to the modules list.

In `app/app/shared/src/commonMain/kotlin/.../app/TruckTrackApp.kt`, call `<name>Entries(navigator)` inside the `entryProvider` block.

### 10. Create module AGENTS.MD files

Create `app/feature/<name>/api/AGENTS.MD` and `app/feature/<name>/impl/AGENTS.MD` (plus matching `CLAUDE.md` stubs — see any existing `feature/*` module for the one-line `@AGENTS.MD` pattern).
Document the nav keys, screens, and dependencies. See existing modules for examples.

## Verification

- [ ] `app/feature/<name>/api/` contains `build.gradle.kts` and one `*NavKey.kt` in `src/commonMain/kotlin/`
- [ ] `app/feature/<name>/impl/` contains `build.gradle.kts`, at least one screen package, an `EntryProvider`, and a Koin module — all in `src/commonMain/kotlin/`
- [ ] Both module paths appear in `app/settings.gradle.kts`
- [ ] `app/app/shared/build.gradle.kts` references `projects.feature.<name>.impl`
- [ ] `AppModule.kt` includes the new Koin module
- [ ] `TruckTrackApp.kt` calls the new `<name>Entries(navigator)`
- [ ] `cd app && ./gradlew :feature:<name>:impl:assembleDebug` passes (or `:app:feature:<name>:impl:assembleDebug` from the repo root)
- [ ] `cd app && ./gradlew spotlessCheck` passes


