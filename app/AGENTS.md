# TruckTrack Client — Agent Instructions

Kotlin Multiplatform client (Android + iOS), own Gradle build (own `settings.gradle.kts` and
`build-logic/`), composite-built alongside `server/` and `shared/` — see the repo root `../AGENTS.md`
for the monorepo layout, the reason for three separate Gradle builds, and repo-wide security policy.

* **UI** — Compose Multiplatform (`org.jetbrains.compose`) only. Never use XML layouts.
* **Dependency Injection** — Koin. KMP-compatible, no annotation processing required.
* **Libraries** — Use entries from `../gradle/libs.versions.toml`. Before adding a new one, confirm
  it publishes Kotlin/Native artifacts for `iosArm64` and `iosSimulatorArm64`.
* **Concurrency** — Kotlin coroutines and Flows exclusively. No RxJava, no callbacks.

See `README.md` (this directory) for build/run instructions, tech stack, CI, and releasing.

## Project Structure

Multi-module KMP architecture rooted at `app/`. Root package: `com.momosi.trucktrack`.

Physical paths below are relative to `app/`; Gradle module paths (e.g. `:app:android`) are relative
to `app/`'s own `settings.gradle.kts`, and get an extra `:app:` prefix (`:app:app:android`) only when
addressed from the *repo* root via the composite build.

* **`app/android`** (`:app:android`) and **`app/ios`** — thin platform shells. Entrypoints only; no
  screens, no business logic. `app/ios` is a separate Xcode project, **not a Gradle module** — it
  consumes `app:shared`'s Kotlin/Native `Shared.framework` via
  `:app:app:shared:embedAndSignAppleFrameworkForXcode`.
* **`app/shared`** (`:app:shared`) — root Composable, root ViewModel, Koin aggregation, platform
  bootstrap. The wiring point every other module feeds into. Its root observer sends signed-out users
  to sign-in and signed-in users without notification permission to the blocking notification
  permission gate, an ordinary root destination.
* **`feature/*`** — one product feature each, always split into `api` (navigation keys only) and
  `impl` (ViewModel, Composables, state/actions/events). Today: `sign-in`, `issues`, `profile`,
  `checklist`, `notification-permission`.
* **`core/*`** — shared domain logic and infrastructure: `common`, `network`, `user`, `issue`,
  `vehicle`, `push`, `checklist`, `navigation`, `ui-library`, `app-links`.
* **`core/app-links`** — parses URLs into typed `AppLinkAction`s and delivers them through the
  Koin singleton `AppLinksManager` backed by internal `AppLinksManagerImpl`, with `AuthManager`
  from `core:user` and logging from `core:common`.
  Signed-out links are ignored; navigation mapping stays in the app-layer consumer.

Every module has its own `AGENTS.md` describing its responsibilities and public API — read it before
changing that module, and record module-specific rules there rather than here.

Network DTOs come from the separate `:shared` build, not from `core/*` — see `../shared/AGENTS.md`.
Modules that talk to the API depend on it via `implementation("com.momosi.trucktrack:shared")`.

### Module Dependency Rules

* `feature/*/api` — depends only on `:core:navigation`. Contains only `NavKey` objects. No business
  logic.
* `feature/*/impl` — depends on its own `api` module + any `core` modules needed. It may also depend
  on **another feature's `api` module**, solely to reference that feature's `NavKey` when registering
  a navigation target — that's what `api` modules exist for. **Never depend on another feature's
  `impl`**: no importing its ViewModel, state, or Composables. If two features need the same logic,
  it belongs in a `core` module.
* `core` modules — may depend on other `core` modules in a directed acyclic graph, plus
  `com.momosi.trucktrack:shared` for DTOs. **Never depend on `feature` modules.**
* `app:shared` — depends on all `feature/*/impl` and all `core` modules.
* `app:android` — depends on `app:shared`, `core:common`, `core:network`. Thin Android shell only.

---

## Coding Guidelines

### Architecture — MVVM

* **Composable** — Presenter. Receives immutable `state`, delegates interactions via callbacks named
  `on<Action>` (present tense — never `on<Something>Clicked`). Never holds or mutates state directly.
* **ViewModel** — Extends `ViewModel()` directly (no base class). Exposes a single `StateFlow<State>`.
  Handles `Action`s. Talks to the `core` layer only.

#### ViewModel Pattern

```kotlin
private val state = StateFlow<FeatureState>
    field = MutableStateFlow<FeatureState>(FeatureState.Loading)
```

* Expose state with `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)`.
* Collect state in Composables with `collectAsStateWithLifecycle()`.
* One-shot events: `SharedFlow` or `Channel`.

**Prefer multiple focused `MutableStateFlow`s over one shared flow mutated with `.update{}`:**

* Give each independently-owned piece of state its own `MutableStateFlow`, so every field has exactly
  one call site that writes it, then `combine(...)` them into the exposed `State`.
* **Don't** keep one backing flow updated via `.copy(...)` from many unrelated action handlers — it
  forces every writer to know the whole state shape, and lets concurrent updates clobber each other.
* Group fields into one flow only when they're inherently a single concern with one owner (e.g. a
  submit/request status). For that grouped, mutually-exclusive state use a `sealed interface`, not
  several booleans, so "submitting" and "failed" can't both be true.
* Need to combine more than 5 flows? `kotlinx.coroutines.flow.combine` stops having typed overloads
  there. `core:common`'s `Flows.kt` carries typed overloads up to 8 — add the next one there rather
  than falling back to the untyped vararg/array form.

#### State / Action / Event

* **State** — `sealed interface` + `StateFlow`. All data classes marked `@Immutable`. **No lambdas in
  State** — use `Action` instead.
* **Action** — `sealed interface` for UI-to-VM intents.
* **Event** — `sealed interface` for one-off VM-to-UI signals (e.g. navigate away).

One package per feature screen, related files together: `IssuesScreen.kt`, `IssuesViewModel.kt`,
`IssuesState.kt`, `IssuesAction.kt`, `IssuesEvent.kt`.

#### Data Layer

* **Repository** — data retrieval, persistence, source abstraction.
* **Manager** — complex business logic, orchestration.
* Always define a public **interface**; place the implementation in the same module (e.g.
  `IssueRepository` + `IssueRepositoryImpl`). Bind with Koin `single<Interface> { Impl(get()) }`.
* Use **data classes** for domain models.
* **Never throw across public interfaces.** Express failure with:
  * `Result<T>` — recoverable operations with error context.
  * `T?` — optional values with no useful error detail.
  * `List<T>` — collections (empty = none found).

#### Error Handling

* **Inside a suspend function, use `runCatchingCancellable { }` / `mapCatchingCancellable { }` from
  `core:common`, never `runCatching { }`.** Plain `runCatching` catches `CancellationException` and
  turns a cancelled coroutine into a `Result.failure`, so the cancellation is swallowed and the
  caller's scope never unwinds. Plain `runCatching` is acceptable only in non-suspending code with
  no cancellation to propagate (e.g. `Json.decodeFromString`, `enum valueOf`).
* Failures reaching the UI are `ApiException` (`core:common/network`) — `NoConnection`,
  `Unauthorized`, or `HttpError(statusCode, serverMessage)`. The Ktor client maps raw transport and
  HTTP errors into these via `installApiExceptionMapping`; repositories propagate them inside
  `Result.failure` rather than catching and re-wrapping.
* Never build a user-facing error string in a ViewModel. Composables convert a `Throwable` with
  `toMessage(rememberApiErrorMessages())` in `:core:ui-library`, which picks the localized string
  and only trusts `serverMessage` for 4xx responses.
* Never put a `Throwable` in UI `State`. When a screen needs to tell failures apart, the ViewModel
  maps it with `toFailureReason()` (`core:common/network`) and the state carries the `FailureReason`.
  Full-screen load failures render with `ErrorState` from `:core:ui-library`.

### Navigation

* **Navigation 3** (`androidx.navigation3`) for all navigation.
* Navigation keys are `@Serializable` objects implementing `NavKey`, in `feature/*/api`.
* Screen entries registered via `EntryProviderScope<NavKey>` extensions in `feature/*/impl/navigation/`.
* Every `NavKey` that can end up in a back stack is registered in `app:shared`'s
  `AppNavKeySerializers.kt`.
* App-wide gates are root destinations, never pushed on top of other screens. As the only entry of
  the back stack, a gate leaves nothing else reachable and Android back leaves the app. **Only
  `app:shared` navigates between them**, from a single `AppAccess` (`AppAccess.kt`) with fixed
  precedence — signed out (sign-in) › notification permission missing (the gate) › ready (the
  issues list) — whenever it changes. Sign-in and the gate never navigate away themselves: they
  sign in or request the permission, and the access change moves the user on (this also covers a
  login resumed after process death and a permission revoked in the OS settings). App links are
  only delivered while access is `Ready`; until then they wait in `AppLinksManager`.

### Push Notifications

* `core:push` owns device registration, the Firebase Installation ID, the notification permission and
  incoming push messages — see its `AGENTS.md` and `../docs/push-notifications.md`. It starts itself:
  `PushRegistrationManager` is the single owner of every registration request and reacts to logins,
  restored sessions and Firebase Installation ID changes on its own, and `PushMessageDispatcher`
  delivers incoming messages. Neither needs feature or `app:shared` code.
* A feature reacts to a push message type by implementing `core:push`'s `PushMessageHandler` and
  registering it in its Koin module with `bind PushMessageHandler::class`; UI that only needs to
  observe messages collects `PushMessageDispatcher.messages`.
* Features sign out with `AuthManager.signOut()`. The app sends nothing about the push device on
  sign-out: the Keycloak logout triggers the backend's back-channel logout, which removes the
  account's push devices. That is why sign-out needs a connection and is refused offline.

### Koin Dependency Injection

* ViewModels: declare with `viewModel { MyViewModel(get()) }`. Inject in Composables via
  `koinViewModel()`.
* Modules: `val myModule = module { }`, grouped by feature/core module, aggregated in `app:shared`'s
  `AppModule`. Only one top-level `startKoin { }`, in `AppInitializer`.
* `single { }` for repositories and managers; `factory { }` for use-cases and short-lived objects.
* No `@Inject` annotations — pass dependencies explicitly in the `module { }` DSL.
* Bind interfaces to implementations: `single<IssueRepository> { IssueRepositoryImpl(get()) }`.

### UI Library

* All reusable Compose components must live in `:core:ui-library`.
* Feature and `app:shared` modules must **not** import `androidx.compose.material3` directly. Use
  only `androidx.compose.foundation`, `androidx.compose.ui`, and components from `:core:ui-library`.
* Theme access via `AppTheme` (colors, typography, shapes), provided exclusively by
  `:core:ui-library`.
* Product illustrations follow the notification-permission artwork from PR #85. Read
  `../.claude/rules/illustrations.md` before creating or replacing illustration assets; it includes
  visual references, the palette, a reusable generation prompt, and integration requirements.
  Store and expose illustrations in `:core:ui-library` through `TruckTrackIllustrations`
  (`DrawableResource`); keep `TruckTrackIcons` for `ImageVector` icons.
  Name illustration resources with an `_illustration` suffix and never access them through
  `Res.drawable` outside `TruckTrackIllustrations.kt`; client Detekt enforces this boundary
  with `TruckTrack > UseTruckTrackIllustrations`. Photography is outside this illustration family.
  Run `./gradlew checkIllustrationAccess :detekt-rules:test` from `app/` to verify the boundary
  across all source sets and test the custom rule. CI runs both checks.

### Compose Stability

* Wrap list state/parameters in `kotlinx.collections.immutable` (`ImmutableList`, `persistentListOf`).
* Annotate State data classes with `@Immutable`.
* Annotate non-data-class Composable parameters with `@Stable` if they are stable but the compiler
  cannot infer it.

### Logging & Crash Reporting

* Use `Logger` from `core:common` — never call Kermit or Crashlytics directly.
* Always provide a tag: `Logger.d("FeatureName", "message")`.
* Levels: `v`, `d`, `i`, `w`, `e`. Throwable variants: `Logger.e("Tag", throwable, "message")`.
* `Logger` already forwards to Crashlytics via `CrashlyticsLogWriter` — logging an error also
  attaches it to the next crash report. Don't report the same failure twice.
* `CrashReporting` (`core:common`) is a singleton, not a Koin binding. `setUserId` / `setCustomKey`
  add crash context; never pass anything you wouldn't publish, since crash reports leave the device.

### Networking

* HTTP client: **Ktor Client**, configured in `core:network`. Engine is `ktor-client-okhttp` on
  Android and `ktor-client-darwin` on iOS.
* Serialization: Kotlinx Serialization JSON via `ktor-client-content-negotiation` +
  `ktor-serialization-kotlinx-json`.
* API clients live in the relevant `core` module (e.g. `core:issue`, `core:push`); request/response
  types come from `com.momosi.trucktrack:shared`, never module-local DTOs.
* **Every backend API client is a Ktorfit interface.** Apply the `trucktrack.ktor` plugin (KSP +
  Ktorfit + Ktor), declare the endpoints with Ktorfit annotations (`@GET`, `@PATCH`, `@Path`,
  `@Body`, …) and create the client in the module's Koin module with
  `single { get<Ktorfit>().createXxxApi() }`, which reuses the authenticated client from
  `core:network`. Hand-written Ktor `HttpClient` calls are not allowed. The only exceptions are the
  unauthenticated bootstrap clients that can't depend on `core:network` — `core:common`'s `VersionApi`
  and `core:user`'s `AuthApi` (see their `AGENTS.md`).
* Backend URLs (API base, Keycloak realm), the OAuth client ID and the redirect scheme come from the
  Koin-injected `AppConfig` (`core:common`) — never hardcode them. Values are per-environment build
  config (Android product flavors, iOS `Environment.xcconfig`), see `README.md` → "Build variants".
* Authentication: bearer token injected via Ktor's `Auth` plugin, wired to `AuthManager`.
* Retries are **off by default**. `core:network` installs Ktor's `HttpRequestRetry` with
  `noRetry()`, so a failed request fails once and surfaces immediately. A request opts in
  individually — a `@ReqBuilder` parameter on the Ktorfit method, filled with
  `HttpRequestBuilder.retry { }` and a named policy in the owning module (only idempotent methods,
  only transport errors and 5xx, never 4xx or cancellation, a few seconds of backoff at most). Never
  turn retries on for the whole client. Today only `core:push`'s registration `PUT` opts in.
* To use Ktor in a module: apply the `trucktrack.ktor` plugin.

### Serialization

* **Kotlinx Serialization** for all DTO and navigation-key serialization (`@Serializable`).
* Never use `Parcelable` / `@Parcelize` — it has no Kotlin/Native equivalent.

### Convention Plugins (build-logic)

All modules are KMP. Convention plugins configure `kotlin("multiplatform")` +
`com.android.kotlin.multiplatform.library`. Registered ids, applied in module `build.gradle.kts`
files:

* `trucktrack.library` — base KMP library (Android target with compileSdk/minSdk, JDK 25 toolchain,
  Spotless).
* `trucktrack.test` — opt-in client KMP library testing: enables Android host tests and adds Kotlin
  test assertions and `kotlinx-coroutines-test` to `commonTest` when the Android KMP library plugin
  is applied. Does not apply production plugins and is independent of plugin application order.
  Apply `alias(libs.plugins.trucktrack.test)` in modules with tests; keep specialized test
  dependencies such as Ktor's mock engine in the module. This plugin belongs to the client build,
  not the independent server or shared DTO builds.
* `trucktrack.feature.api` — feature API module (library + serialization + Navigation 3 runtime).
* `trucktrack.feature.impl` — feature impl module (library + Koin + Compose + `:core:ui-library` +
  `:core:navigation`).
* `trucktrack.koin` — Koin core (`commonMain`) + Koin Android and Koin Compose (`androidMain`).
* `trucktrack.compose` — Compose Multiplatform plugin + Material3 + icons + resources + Navigation 3.
* `trucktrack.ktor` — KSP + Ktorfit + Ktor Client core + serialization for KMP. Required by every
  module that declares a backend API client.
* `trucktrack.spotless` — ktlint via Spotless (auto-included by `trucktrack.library`).
* `trucktrack.android.signing` — release signing config for `app:android`.
* `trucktrack.firebase` — Firebase (Crashlytics) wiring. Firebase Cloud Messaging is not part of it:
  `core:push` adds `firebase-messaging` to its own `androidMain` through the Firebase BOM.

### Style & Conventions

* Run `./gradlew spotlessApply` before committing. Spotless (ktlint + compose-rules-ktlint) is the
  authority on formatting — if it passes, the formatting is correct.
* Use `data object` instead of plain `object` for singleton data carriers.
* No wildcard imports. Always import specific symbols.
* `public` only when part of the module's public API. Prefer `private`, then `internal`.
* `camelCase` for functions/variables/properties, `PascalCase` for classes/interfaces/objects/enum
  values/`@Composable` functions, `UPPER_SNAKE_CASE` for constants.
* Code comments: see the root `../AGENTS.md`. That rule is absolute and has no client-specific
  exception.

---

## KMP Rules for New Code

The Kotlin Multiplatform migration is complete — every module uses KMP source sets, and
`commonMain` / `androidMain` / `iosMain` all carry real code today.

> When choosing a technology, always look for a KMP-compatible alternative first. Never introduce a
> new Android-only or JVM-only dependency unless there is no KMP option.

1. **New source files** go in `src/commonMain/kotlin/` unless they call platform-specific APIs.
2. **New `core` domain models** — pure Kotlin. No Android imports, no `Context`, no `@StringRes`.
3. **New `core` repository/manager interfaces** — pure Kotlin, with return types available on all
   targets (`String`, `Long`, `kotlin.time.Instant`, data classes, `Flow`, `Result`).
4. **New Composables** go in `commonMain`, with `@Preview` functions in the same file.
   Platform-specific composables keep their previews beside them in the matching platform source set.
5. **Platform-specific behaviour** — declare the interface (or `expect`) in `commonMain` and provide
   an implementation in *both* `androidMain` and `iosMain`. An Android-only implementation with no
   iOS counterpart breaks the iOS build.
6. **Avoid `android.net.Uri`** — use `String`.
7. **Avoid `android.content.Context` in `commonMain`** — pass the data you need, not the Context.
8. **Adding an Android-only dependency** requires a note in the owning module's `AGENTS.md` saying
   which API forced it and what the iOS path is.

---

## Unit Testing

`core:network` has shared session-invalidation regression tests in `src/commonTest`, runnable with
`:core:network:testAndroidHostTest`; `core:push` has push registration tests, runnable with
`:core:push:testAndroidHostTest`. When adding further tests:

* **MockK** for mocking, **Turbine** for `Flow` testing, **kotlinx-coroutines-test** (`runTest`) for
  coroutines.
* Shared tests in `src/commonTest/kotlin/`, Android-specific in `src/androidTest/kotlin/`, mirroring
  the main source package.

---

## Agent Skills

Client-specific Agent Skills live in `.claude/skills/`. Claude Code discovers them automatically and
surfaces each one's trigger conditions — don't re-list them here. Scaffolding a feature or screen,
adding a repository, fixing Spotless, diagnosing CI, releasing, and iOS signing all have one; prefer
the skill over improvising the steps.
