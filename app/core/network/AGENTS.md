# core:network

Provides the shared HTTP client (Ktor) and pagination DTO infrastructure used by all data modules. Fully KMP — all code in `commonMain`.

## Public API

| Symbol | Description |
|--------|-------------|
| `NetworkModule` | Koin module that provides the configured `HttpClient`. Registered in `app:shared`'s `AppModule`. |
| `PageDtoMapper` | Maps `com.momosi.trucktrack.shared.common.PageDto<T>` (from the separate `shared` build, not defined here) → `Page<T>` (domain model from `core:common`). |

## Key Files

```
commonMain/
  di/NetworkModule.kt              ← Ktor HttpClient setup, Auth plugin, base URL
  AuthTokenCacheInvalidation.kt    ← HttpClient.invalidateAuthTokensOn: clears Ktor's cached bearer
                                      token whenever AuthManager.authenticationState changes
  dto/PageDtoMapper.kt             ← maps shared's PageDto<T>; no local PageDto class
```

## Depends On

- `:core:common` — `ConnectivityManager`, `Logger`, `Page<T>`
- `:core:user` — `AuthManager` (for bearer token injection via Ktor Auth plugin)
- `com.momosi.trucktrack:shared` — `PageDto<T>` (separate build, see `../../../shared/AGENTS.md`)

## Configuration

- **Base URL**: `https://tt.momosi.org/`
- **Engine**: `ktor-client-okhttp` (Android). Swap to `ktor-client-darwin` for iOS.
- **Serialization**: Kotlinx Serialization JSON via `ContentNegotiation` plugin.
- **Auth**: Bearer token injected via Ktor `Auth` plugin wired to `AuthManager.token()`. Ktor's
  `bearer { }` provider caches the loaded token internally and only reloads it after a 401, so
  `buildHttpClient` also calls `HttpClient.invalidateAuthTokensOn` (`AuthTokenCacheInvalidation.kt`),
  which clears the cache on every `AuthManager.authenticationState` transition (sign-in/sign-out) —
  without this, requests made right after switching accounts in the same process would still carry
  the previous user's token.
