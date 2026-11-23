# app:shared

Shared KMP app module — the wiring point every other `app/` module ultimately feeds into. Depends on all `feature/*/impl` and all `core` modules. Not to be confused with the top-level `shared` build (`com.momosi.trucktrack:shared`), which is DTOs only.

## Key Files

```
commonMain/
  TruckTrackApp.kt         ← Root Composable: Koin viewModel, NavDisplay, theme, shared-element transition scope, start key and the root navigation observer
  TruckTrackViewModel.kt    ← Root ViewModel: authentication state, notification permission status (+ refresh), errors
  AppInitializer.kt         ← startKoin { } entry point, called by both platform shells
  AppImageLoaderFactory.kt  ← Coil ImageLoader wiring
  AppNavKeySerializers.kt   ← polymorphic NavKey serializer registration for saved-state
  di/AppModule.kt           ← allModules: List<Module> — every Koin module in the app, aggregated here
iosMain/
  MainViewController.kt     ← ComposeUIViewController { TruckTrackApp() } entry point, exposed to Swift
  IosAppInitializer.kt      ← bootstrapIosApp(), Swift-facing counterpart to AppInitializer
  IosPushBridge.kt          ← IosPushBridge object, Swift-facing (IosPushBridge.shared): the Firebase Installation ID, foreground push data, the device name and the notification settings URL from the Swift shell, forwarded to core:push's IosPushEvents
```

## Responsibilities

- `TruckTrackApp()` — the single root Composable both `app:android`'s `TruckTrackActivity` and `app:ios`'s `MainViewController` render. Owns navigation state, the shared-element transition scope, and routes to each feature's `*Entries` via `entryProvider { }`.
- Root navigation is decided in one place, `AppAccess.kt`. `TruckTrackViewModel.access` combines the authentication state with `NotificationPermission.currentStatus` into `AppAccess` with fixed precedence: `SignedOut` › `NotificationPermissionRequired` › `Ready` (`Unknown` while a signed-in user's permission status is still loading — nothing navigates then). The start key is `access.startKey()`, and the root's single observer calls `navigateToRoot(access.redirectFrom(currentKey))` whenever access changes:
  - `SignedOut` → sign-in unless it's already shown. This covers sign-out, failed token refreshes and API session invalidation, and back navigation cannot reopen the previous authenticated screens.
  - `NotificationPermissionRequired` → the gate unless it's already shown (restored session without permission, permission revoked in the OS settings).
  - `Ready` → the issues list, but only when leaving sign-in or the gate; any other screen stays.
  Sign-in and the gate never navigate themselves — their work changes `access`. The permission is re-checked on every resume (`LifecycleResumeEffect`). Covered by `AppAccessTest` (`commonTest`, `:app:shared:testAndroidHostTest`).
- Collect the singleton app-link flow only inside `repeatOnLifecycle(Lifecycle.State.RESUMED)`. Background roots must not consume one-shot links intended for the foreground root.
- `core:app-links` owns the Koin singleton `AppLinksManager`, backed by internal `AppLinksManagerImpl` for URL parsing, authentication checks, logging, and action delivery. Aggregate its `appLinksModule` here. Android injects the interface directly; iOS uses the `openIosAppLink` bootstrap bridge. Signed-out URLs are rejected immediately. A conflated channel bridges delivery before Compose starts, rechecks authentication when consumed, and never replays handled links. The root collects `AppLinkAction`s through `TruckTrackViewModel` and translates them into root/destination keys in `TruckTrackApp.kt`. Delegate backstack replacement and duplicate prevention to `Navigator.navigate(root, destination)` in `core:navigation`. Keep navigation out of `core:app-links`.
- `TruckTrackViewModel.appLinkActions` collects app links only while access is `Ready`. Until then a pending link waits in `AppLinksManager`'s conflated channel and opens as soon as the user gets past the gate.
- `AppModule.kt` — the only place Koin modules are aggregated (`allModules`). Every `core`/`feature` module's own Koin module gets listed here; nothing else calls `startKoin` directly.
- `AppInitializer`/`IosAppInitializer` — platform-specific bootstrap, but both ultimately call into this module's `startKoin { modules(allModules) }`.

## Rules

- Push needs no wiring here: `core:push` starts itself through `createdAtStart` singles (`PushRegistrationManager` for registration, `PushMessageDispatcher` for incoming messages), the same pattern as `CrashReportingSync`.
- New screens/navigation entries never go here — add them to the relevant `feature/*/impl` module and wire the `*Entries` function into `TruckTrackApp()`.
- New Koin bindings go in the owning `core`/`feature` module's own `module { }`; only the aggregation list lives here.
- This module must not import `androidx.compose.material3` directly — use `:core:ui-library` components only, per the root `AGENTS.md`'s UI Library rule.

## Depends On

All `feature/*/impl` modules, all `core/*` modules.

## Depended On By

- `app:android` — Android shell (`TruckTrackApplication`, `TruckTrackActivity`).
- `app:ios` — SwiftUI shell, via the Kotlin/Native `Shared.framework` this module produces.
