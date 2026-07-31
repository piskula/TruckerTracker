# TruckTrack Client — Agent Instructions

Kotlin Multiplatform client, own Gradle build (own `settings.gradle.kts` and `build-logic/`), composite-built alongside `server/` and `shared/` — see the repo root `../AGENTS.MD` for the monorepo layout, the reason for three separate Gradle builds, and repo-wide security policy.

* **UI** — Compose Multiplatform (`org.jetbrains.compose`) only. Never use XML layouts.
* **Dependency Injection** — Koin. KMP-compatible, no annotation processing required.
* **Libraries** — Use entries from `../gradle/libs.versions.toml`. Do not add new libraries without checking KMP compatibility first.
* **Concurrency** — Kotlin coroutines and Flows exclusively. No RxJava, no callbacks.

See `README.md` (this directory) for build/run instructions, tech stack, CI, and releasing.

## Project Structure

Multi-module KMP architecture, its own Gradle build rooted at `app/`. Root package: `com.momosi.trucktrack`.

### Modules

Physical paths below are relative to `app/`; Gradle module paths (e.g. `:app:android`) are relative to `app/`'s own `settings.gradle.kts`, and get an extra `:app:` prefix (`:app:app:android`) only when addressed from the *repo* root via the composite build.

* **`app/android`** (Gradle path `:app:android`) — Thin Android shell. `TruckTrackApplication`, `TruckTrackActivity`. Contains only Android entrypoints (`Application`, `Activity`). Depends on `app/shared`.
* **`app/shared`** (Gradle path `:app:shared`) — Shared KMP app module. `TruckTrackApp` (root Composable), `TruckTrackViewModel`, `AppModule` (Koin wiring), `AppInitializer` (`startKoin`). Depends on all `core` and `feature` modules. Source in `src/commonMain/kotlin/`.
* **`app/ios`** — SwiftUI iOS entry point at `app/ios/iosApp.xcodeproj`. A separate Xcode project consuming `app:shared`'s Kotlin/Native `Shared.framework` via direct integration (Gradle's `embedAndSignAppleFrameworkForXcode`, invoked from the repo root as `:app:app:shared:embedAndSignAppleFrameworkForXcode`), not a Gradle module itself.
* **`feature`** (multiple modules, at `feature/*`) — UI screens and logic for each product feature. Always split into:
  * `api` — navigation keys only (`@Serializable` `NavKey` objects). Source in `src/commonMain/kotlin/`.
  * `impl` — ViewModel, screen Composables, state, actions, events. Source in `src/commonMain/kotlin/`.
  * Current features: `sign-in`, `issues`, `profile`.
* **`core`** (multiple modules, at `core/*`) — Shared domain logic, utilities, and infrastructure:
  * `common` — `DispatcherProvider`, `ConnectivityManager`, `CurrentActivityHelper`, `Logger` (Kermit wrapper), `Page<T>` model. Pure Kotlin in `commonMain`; Android impls in `androidMain`.
  * `network` — `NetworkModule` (Ktor client setup). Fully in `commonMain`.
  * `user` — `AuthManager`, `UserRepository`, OAuth/OIDC via AppAuth, JWT parsing, `UserStorage`. Interfaces in `commonMain`; AppAuth/storage impls in `androidMain`.
  * `issue` — `Issue`, `IssueStatus`, `IssuePriority`, `Account` models; `IssueRepository`. Fully in `commonMain`.
  * `vehicle` — `Vehicle`, `VehicleType` models; `VehicleRepository`. Fully in `commonMain`.
  * `navigation` — `NavigationState`, `Navigator` wrapping Navigation 3. Fully in `commonMain`.
  * `ui-library` — `TruckTrackTheme`, `AppTheme`, `TruckTrackIcons`, all reusable Compose Multiplatform components. Components in `commonMain`; `@Preview` functions in `androidMain`.

Network DTOs (`IssueDto`, `VehicleDto`, `PageDto`, etc.) no longer live in `core/network`/`core/issue`/`core/vehicle` — they come from the separate `:shared` build (see `../shared/AGENTS.MD` and the root `../AGENTS.MD`'s "Repository Layout"). `core/*` modules that talk to the API depend on it via `implementation("com.momosi.trucktrack:shared")`.

### Module Dependency Rules

* `feature/*/api` — depends only on `:core:navigation`. Contains only `NavKey` objects. No business logic.
* `feature/*/impl` — depends on its own `api` module + any `core` modules needed. No cross-feature dependencies.
* `core` modules — may depend on other `core` modules in a directed acyclic graph, plus `com.momosi.trucktrack:shared` for DTOs. Never depend on `feature` modules.
* `app:shared` — depends on all `feature/*/impl` and all `core` modules. The shared wiring point.
* `app:android` — depends on `app:shared`, `core:common`, `core:network`. Thin Android shell only.

### Package Structure

* Root: `com.momosi.trucktrack`
* Features: `com.momosi.trucktrack.feature.<name>.api` / `.impl`
* Core: `com.momosi.trucktrack.core.<name>`
* App: `com.momosi.trucktrack.app` (shared Composable entry, DI wiring)
* Android shell: `com.momosi.trucktrack` (Application, Activity)
* Shared contract (separate build): `com.momosi.trucktrack.shared.<name>`

### Source Set Structure (KMP)

All modules use the Kotlin Multiplatform source set layout:

```
module/
  src/
    commonMain/kotlin/    ← Shared pure-Kotlin and Compose Multiplatform code
    androidMain/kotlin/   ← Android-specific implementations (expect/actual, @Preview)
    iosMain/kotlin/       ← iOS-specific implementations (future)
```

---

## Coding Guidelines

### Architecture — MVVM

#### Component Responsibilities

* **Composable** — Presenter. Receives immutable `state`, delegates interactions via callbacks named `on<Action>` (present tense — never `on<Something>Clicked`). Never holds or mutates state directly.
* **ViewModel** — Extends `ViewModel()` directly (no base class). Exposes a single `StateFlow<State>`. Handles `Action`s. Talks to `core` layer only.

#### ViewModel Pattern

```kotlin
// Always use a private backing field
private val state = StateFlow<FeatureState> 
    field = MutableStateFlow<FeatureState>(FeatureState.Loading)
```

* Expose state with `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)`.
* Collect state in Composables with `collectAsStateWithLifecycle()`.
* One-shot events: `SharedFlow` or `Channel`.

**Prefer multiple focused `MutableStateFlow`s over one shared flow mutated with `.update{}`.** Give each independently-owned piece of state its own `MutableStateFlow`, so every field has exactly one action/call site that writes it, then `combine(...)` them into the exposed `State`. Avoid a single backing flow updated via `.copy(...)` from many unrelated action handlers — it forces every writer to know the whole state shape and makes it easy for concurrent updates to clobber each other's intent. `kotlinx.coroutines.flow.combine` only has typed overloads up to 5 flows; `core:common`'s `Flows.kt` provides typed `combine` overloads beyond that (currently up to 8) — add another overload there following the same pattern if you need more, rather than falling back to the untyped vararg/array form. Only group multiple fields into one flow when they're inherently a single concern with one owner (e.g. a submit/request status) — and for that kind of grouped, mutually-exclusive state, prefer a `sealed interface` over several booleans, so illegal combinations (e.g. "submitting" and "failed" both true) can't be represented.

#### State / Action / Event

* **State** — `sealed interface` + `StateFlow`. All data classes marked `@Immutable`. **No lambdas in State** — use `Action` instead.
* **Action** — `sealed interface` for UI-to-VM intents.
* **Event** — `sealed interface` for one-off VM-to-UI signals (e.g., navigate away).

#### File Structure

One package per feature screen. Keep related files together:
`IssuesScreen.kt`, `IssuesViewModel.kt`, `IssuesState.kt`, `IssuesAction.kt`, `IssuesEvent.kt`

#### Data Layer

* **Repository** — data retrieval, persistence, source abstraction.
* **Manager** — complex business logic, orchestration.
* Always define a public **interface**; place implementation in the same module (e.g., `IssueRepository` + `IssueRepositoryImpl`). Bind with Koin `single<Interface> { Impl(get()) }`.
* Use **data classes** for domain models.
* **Never throw across public interfaces.** Express failure with:
  * `Result<T>` — for recoverable operations with error context (use `runCatching` internally).
  * `T?` — for optional values with no useful error detail.
  * `List<T>` — for collections (empty = none found).

### Navigation

* **Navigation 3** (`androidx.navigation3`) for all navigation.
* Navigation keys are `@Serializable` objects implementing `NavKey`, in `feature/*/api`.
* Screen entries registered via `EntryProviderScope<NavKey>` extensions in `feature/*/impl/navigation/`.

### Koin Dependency Injection

* **Koin** (`io.insert-koin:koin-core`) is the DI framework — KMP-compatible, runtime-based.
* ViewModels: declare with `viewModel { MyViewModel(get()) }` in a Koin module. Inject in Composables via `koinViewModel()`.
* Modules: use `val myModule = module { }`. Group by feature/core module. Single top-level `startKoin { }` in `AppInitializer` (called from `TruckTrackApplication`).
* Prefer `single { }` for repositories and managers (singleton scope).
* Prefer `factory { }` for use-cases and other short-lived objects.
* No `@Inject` annotations on constructors — pass dependencies explicitly in the `module { }` DSL.
* Bind interfaces to implementations with `single<IssueRepository> { IssueRepositoryImpl(get()) }`.

### UI Library

* All reusable Compose components must live in `:core:ui-library`.
* Feature and `app:shared` modules must **not** import `androidx.compose.material3` directly. Use only `androidx.compose.foundation`, `androidx.compose.ui`, and components from `:core:ui-library`.
* Theme access via `AppTheme` (colors, typography, shapes) provided exclusively by `:core:ui-library`.

### Compose Stability

* Wrap list state/parameters in `kotlinx.collections.immutable` (`ImmutableList`, `persistentListOf`).
* Annotate State data classes with `@Immutable`.
* Annotate non-data-class Composable parameters with `@Stable` if they are stable but the compiler cannot infer it.

### Compose Previews

* Every file with `@Composable` functions must have `@Preview` functions for key composables.
* Wrap in `TruckTrackTheme { }`.
* Preview functions must be `private`, named with a `Preview` suffix (e.g., `IssueCardDriverPreview`).
* Use realistic sample data. Avoid empty/null states unless explicitly testing that variant.
* Never preview top-level screen composables that require a `ViewModel`. Preview the inner stateless content composable instead.
* `@Preview` functions live in `src/androidMain/kotlin/` (Android Studio tooling only).

### Logging

* Use `Logger` from `core:common` — never call Kermit directly.
* Always provide a tag: `Logger.d("FeatureName", "message")`.
* Levels: `v`, `d`, `i`, `w`, `e`. Throwable variants: `Logger.e("Tag", throwable, "message")`.

### Networking

* HTTP client: **Ktor Client** (`io.ktor:ktor-client-*`). Configured in `core:network`. KMP-compatible.
* Engine: `ktor-client-okhttp` on Android (swap to `ktor-client-darwin` on iOS).
* Serialization: **Kotlinx Serialization JSON** via `ktor-client-content-negotiation` + `ktor-serialization-kotlinx-json`.
* All API interfaces/clients live in the relevant `core` module (e.g., `core:issue`, `core:user`), and request/response types come from `com.momosi.trucktrack:shared` (the `:shared` build) — not module-local DTOs.
* Base URL: `https://tt.momosi.org/`.
* Authentication: inject a bearer token via Ktor's `Auth` plugin, wired to `AuthManager`.
* To use Ktor in a module: apply `trucktrack.ktor` plugin or add `implementation(libs.bundles.ktor)` to `commonMain` dependencies.

### Serialization

* **Kotlinx Serialization** — for all DTO serialization and navigation key serialization (`@Serializable`). KMP-compatible.
* Avoid `Parcelable` / `@Parcelize` for new code. Use Kotlinx Serialization instead.

### Convention Plugins (build-logic)

All modules are KMP. Convention plugins configure `kotlin("multiplatform")` + `com.android.kotlin.multiplatform.library`.

Use these in module `build.gradle.kts` files:

* `trucktrack.library` — base KMP library (Kotlin Multiplatform, Android target with compileSdk/minSdk, JDK 25 toolchain, Spotless).
* `trucktrack.feature.api` — feature API module (library + serialization + Navigation 3 runtime).
* `trucktrack.feature.impl` — feature impl module (library + Koin + Compose + `:core:ui-library` + `:core:navigation`).
* `trucktrack.koin` — Koin core (`commonMain`) + Koin Android + Koin Compose (`androidMain`).
* `trucktrack.compose` — Compose Multiplatform plugin + Material3 + icons + resources + Navigation 3 bundles.
* `trucktrack.ktor` — Ktor Client core + serialization for KMP.
* `trucktrack.spotless` — ktlint formatting via Spotless (auto-included by `trucktrack.library`).

### Style & Conventions

* Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).
* Use `data object` instead of plain `object` for singleton data carriers.
* Run `./gradlew spotlessApply` to auto-format before committing.
* No wildcard imports. Always import specific symbols.
* No comments, KDoc, or Javadoc unless explicitly requested. Code must be self-explanatory.
* `public` only when part of the public API. Prefer `private`, then `internal`.

### Naming Conventions

* `camelCase` — functions, variables, properties.
* `PascalCase` — classes, interfaces, objects, enum values, `@Composable` functions.
* `UPPER_SNAKE_CASE` — constants.

---

## Standard Libraries

| Library | Purpose | KMP-compatible |
|---------|---------|---------------|
| `kotlinx-coroutines` | Concurrency | ✅ |
| `kotlinx-serialization-json` | JSON, nav keys | ✅ |
| `kotlinx-collections-immutable` | Stable Compose state | ✅ |
| Coil 3 (`coil-compose`, `coil-network-ktor`) | Image loading | ✅ |
| Ktor Client (`ktor-client-core`, `ktor-client-okhttp`) | HTTP client | ✅ |
| Koin (`koin-core`, `koin-android`, `koin-androidx-compose`) | Dependency injection | ✅ |
| Kermit (`co.touchlab:kermit`) via `Logger` | Logging | ✅ |
| Compose Multiplatform (`org.jetbrains.compose`) | UI framework | ✅ |
| Navigation 3 (`androidx.navigation3`) | Navigation | ✅ |
| AndroidX ViewModel (`lifecycle-viewmodel`) | ViewModel | ✅ |
| AppAuth (`net.openid:appauth`) | OAuth/OIDC | ❌ Android only (`androidMain`) |
| jjwt | JWT parsing | ❌ JVM only (`androidMain`) |

---

## KMP Architecture

The project has completed its **Kotlin Multiplatform** migration. All modules use KMP source sets. See `README.md` (this directory) for the tech stack decisions.

### Core Principle

> When choosing a technology, always check for a KMP-compatible alternative first. Never introduce a new Android-only or JVM-only dependency unless there is no KMP option and Android-only scope is explicitly accepted.

### Agent Skills

Reusable Agent Skills are in `.claude/skills/` (scoped to this directory — they're all
client-specific). Both Claude Code and GitHub Copilot discover this location and load skills on
demand. Use them for recurring tasks:

| Skill | When to use |
|-------|-------------|
| [`create-feature-module/SKILL.md`](.claude/skills/create-feature-module/SKILL.md) | Scaffolding a new `feature/*/api` + `feature/*/impl` pair |
| [`add-screen-to-feature/SKILL.md`](.claude/skills/add-screen-to-feature/SKILL.md) | Adding a new screen (ViewModel + Composable + State/Action/Event) |
| [`add-repository/SKILL.md`](.claude/skills/add-repository/SKILL.md) | Adding a repository/manager to a `core/*` module |
| [`fix-spotless-issues/SKILL.md`](.claude/skills/fix-spotless-issues/SKILL.md) | Running Spotless or fixing `spotlessCheck`/ktlint violations |
| [`analyze-ci-failure/SKILL.md`](.claude/skills/analyze-ci-failure/SKILL.md) | Checking a GitHub Actions build/run status or diagnosing why one failed |
| [`release-app/SKILL.md`](.claude/skills/release-app/SKILL.md) | Cutting a signed Android release build (tagging, versioning, publishing) |
| [`setup-local-tools/SKILL.md`](.claude/skills/setup-local-tools/SKILL.md) | Onboarding a new machine — checking/installing JDK, Android SDK, Xcode, `gh`, Node, `firebase-tools`, and Firebase MCP auth |
| [`manage-ios-signing/SKILL.md`](.claude/skills/manage-ios-signing/SKILL.md) | Adding/removing an iOS tester device, regenerating the ad-hoc provisioning profile, or renewing the iOS distribution certificate |

### What Lives in `commonMain` vs `androidMain`

| `commonMain` (shared) | `androidMain` (Android-only) |
|------------------------|------------------------------|
| Domain models, repository interfaces, repository impls | `ConnectivityManagerImpl` (Android `ConnectivityManager`) |
| Ktor API clients, DTOs, mappers | `CurrentActivityHelper` (Activity lifecycle) |
| ViewModels, State, Action, Event | AppAuth / `OpenIdManager` (OAuth browser flow) |
| Screen Composables (Compose Multiplatform) | `UserStorageImpl` (SharedPreferences/DataStore) |
| Koin module definitions (common bindings) | Koin Android-specific bindings (`androidContext()`) |
| Theme, icons, components (`core:ui-library`) | `@Preview` functions (Android Studio tooling) |
| Navigation keys, Navigator, NavigationState | `TruckTrackApplication`, `TruckTrackActivity` |

### Rules for New Code

1. **New `core` domain models** — pure Kotlin only. No Android imports. No `Context`, no `@StringRes`, no Android-specific types. Place in `commonMain`.
2. **New `core` repository/manager interfaces** — pure Kotlin. Return types use only KMP-compatible types (`String`, `Long`, `kotlinx.datetime`, data classes, `Flow`, `Result`, etc.). Place in `commonMain`.
3. **New libraries** — must be KMP-compatible or explicitly scoped to `androidMain`. Document the reason for any Android-only addition.
4. **Avoid `@Parcelize`** — use Kotlinx Serialization instead.
5. **Avoid `android.net.Uri`** — use `String` or `java.net.URI` for URIs.
6. **Avoid `android.content.Context` in `commonMain`** — pass only the data you need, not the Context.
7. **New Composables** — place in `commonMain`. Only `@Preview` functions go in `androidMain`.
8. **New source files** — always go in `src/commonMain/kotlin/` unless they use Android-specific APIs.

---

## Unit Testing

No tests exist yet. When adding tests, follow these conventions:

* **MockK** — for mocking.
* **Turbine** — for `Flow` testing.
* **kotlinx-coroutines-test** (`runTest`) — for coroutine testing.
* Place tests in `src/commonTest/kotlin/` for shared tests, `src/androidTest/kotlin/` for Android-specific tests. Mirror the main source package.

---

## String Resources & Copywriting

String resources use Compose Multiplatform resources (`compose.resources`). Each module that needs strings declares `compose.resources { packageOfResClass = "..." }`.

Every new or changed user-facing string must be added in both supported languages at the same time: English in the module's default `values/strings.xml` and Slovak in the matching `values-sk/strings.xml`.

When writing string resources, act as a **technical copywriter** — precise about domain concepts, but writing for a non-developer fleet operator.

* Write for a **non-technical user** — a driver or mechanic who understands their job but not software.
* Be **concise** — one or two short sentences maximum.
* **Plain English**. No jargon unless immediately explained.
* **Active voice**, **present tense**.
* Avoid vague phrases ("some data", "certain features"). Be specific.
* Be technically accurate about truck/fleet domain concepts.

## Every Module Has Its Own `AGENTS.MD`

Every `core/*` and `feature/*/api|impl` module has its own `AGENTS.MD` with module-specific responsibilities and rules — check there before changing a module's public API.
