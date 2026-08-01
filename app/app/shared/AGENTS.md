# app:shared

Shared KMP app module — the wiring point every other `app/` module ultimately feeds into. Depends on all `feature/*/impl` and all `core` modules. Not to be confused with the top-level `shared` build (`com.momosi.trucktrack:shared`), which is DTOs only.

## Key Files

```
commonMain/
  TruckTrackApp.kt         ← Root Composable: Koin viewModel, NavDisplay, theme, shared-element transition scope
  TruckTrackViewModel.kt    ← Root ViewModel, exposes AuthenticationState to decide the nav start key
  AppInitializer.kt         ← startKoin { } entry point, called by both platform shells
  AppImageLoaderFactory.kt  ← Coil ImageLoader wiring
  AppNavKeySerializers.kt   ← polymorphic NavKey serializer registration for saved-state
  di/AppModule.kt           ← allModules: List<Module> — every Koin module in the app, aggregated here
iosMain/
  MainViewController.kt     ← ComposeUIViewController { TruckTrackApp() } entry point, exposed to Swift
  IosAppInitializer.kt      ← bootstrapIosApp(), Swift-facing counterpart to AppInitializer
```

## Responsibilities

- `TruckTrackApp()` — the single root Composable both `app:android`'s `TruckTrackActivity` and `app:ios`'s `MainViewController` render. Owns navigation state, the shared-element transition scope, and routes to each feature's `*Entries` via `entryProvider { }`.
- `AppModule.kt` — the only place Koin modules are aggregated (`allModules`). Every `core`/`feature` module's own Koin module gets listed here; nothing else calls `startKoin` directly.
- `AppInitializer`/`IosAppInitializer` — platform-specific bootstrap, but both ultimately call into this module's `startKoin { modules(allModules) }`.

## Rules

- New screens/navigation entries never go here — add them to the relevant `feature/*/impl` module and wire the `*Entries` function into `TruckTrackApp()`.
- New Koin bindings go in the owning `core`/`feature` module's own `module { }`; only the aggregation list lives here.
- This module must not import `androidx.compose.material3` directly — use `:core:ui-library` components only, per the root `AGENTS.md`'s UI Library rule.

## Depends On

All `feature/*/impl` modules, all `core/*` modules.

## Depended On By

- `app:android` — Android shell (`TruckTrackApplication`, `TruckTrackActivity`).
- `app:ios` — SwiftUI shell, via the Kotlin/Native `Shared.framework` this module produces.
