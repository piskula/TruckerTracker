# core:common

Base dependency for all other modules. Otherwise contains shared utilities with no business logic — `version/VersionRepository` is a deliberate, narrowly-scoped exception (see Notes). KMP module with `commonMain`, `androidMain`, and `iosMain` source sets.

## Public API

| Symbol | Description |
|--------|-------------|
| `DispatcherProvider` | Coroutine dispatcher abstraction (`main`, `io`, `default`). Inject instead of hardcoding `Dispatchers.*`. |
| `ConnectivityManager` | Interface in `commonMain`. `ConnectivityManagerImpl` — Android impl (`android.net.ConnectivityManager`) in `androidMain`, iOS impl (`NWPathMonitor`) in `iosMain`. Use the interface everywhere. |
| `AppVersionProvider` | Interface in `commonMain` exposing `versionName`/`versionCode` of the running app. `AppVersionProviderImpl` — Android impl (`PackageManager`) in `androidMain`, iOS impl (`NSBundle.mainBundle`) in `iosMain`. |
| `AppEnvironment` | Singleton object (same pattern as `Logger`/`CrashReporting` — no DI) exposing `isDebug: Boolean`. Set once via `AppEnvironment.init(isDebug)` from `app:shared`'s `initApp(isDebug)`, which itself gets the flag from each platform's own build-type check (`BuildConfig.DEBUG` on Android, `#if DEBUG` on iOS). Read directly wherever a debug-only gate is needed. |
| `VersionRepository` | `suspend fun getServerVersion(): Result<ServerVersion>` — fetches the backend's build info from `GET /api/v1/version` (public endpoint). Uses its own standalone `HttpClient` (`VersionApi`), same pattern as `core:user`'s `AuthApi` — see Notes. |
| `Logger` | Kermit wrapper. Always use this, never call Kermit directly. |
| `Page<T>` | Generic pagination model returned by all paginated repository calls. |

## Key Files

```
commonMain/
  logger/Logger.kt
  coroutines/DispatcherProvider.kt
  coroutines/Flows.kt              ← Flow utility extensions
  network/ConnectivityManager.kt   ← Interface (pure Kotlin)
  environment/AppEnvironment.kt    ← Singleton object holding isDebug
  version/AppVersionProvider.kt    ← Interface (pure Kotlin)
  version/VersionRepository.kt / VersionRepositoryImpl.kt
  version/model/ServerVersion.kt
  version/dto/ServerVersionDtoMapper.kt   ← maps shared's BuildInfoDto -> ServerVersion; no local DTO class
  version/api/VersionApi.kt               ← standalone Ktor HttpClient, returns com.momosi.trucktrack.shared.version.BuildInfoDto
  model/Page.kt
  di/CommonModule.kt               ← Koin bindings (common part)
androidMain/
  network/ConnectivityManagerImpl.kt
  version/AppVersionProviderImpl.kt ← PackageManager-backed
  di/CommonModule.android.kt       ← Koin bindings needing androidContext()
iosMain/
  network/ConnectivityManagerImpl.kt ← NWPathMonitor-backed
  version/AppVersionProviderImpl.kt  ← NSBundle-backed
  di/CommonModule.ios.kt
```

## Depends On

No `core:*`/`feature:*` project dependencies — still the base module. Does use `com.momosi.trucktrack:shared` (external coordinate, separate build — see `../../../shared/AGENTS.MD`) for `BuildInfoDto`, and applies the `trucktrack.ktor` plugin for `VersionApi`'s standalone `HttpClient`.

## Notes

- `VersionApi` builds its own bare `HttpClient` directly (no shared/authenticated client, no Ktorfit) rather than depending on `:core:network` — mirrors `core:user`'s `AuthApi`, which does the same for its own unauthenticated Keycloak calls. This keeps `core:common` free of a `:core:network` dependency (which would be circular, since `core:network` itself depends on `core:common`) while still allowing a from-base-module API call for endpoints that don't need auth.
- **Do not treat `VersionRepository` as license to add further bespoke network calls here.** If a second, unrelated feature ever needs another small standalone endpoint, give it its own `core:*` module instead of repeating this pattern — otherwise `core:common` quietly becomes a dumping ground for one-off API calls.
