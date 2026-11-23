# core:network

Provides the shared HTTP client (Ktor) and pagination DTO infrastructure used by all data modules. Fully KMP — all code in `commonMain`.

## Public API

| Symbol | Description |
|--------|-------------|
| `NetworkModule` | Koin module that provides the configured `HttpClient`. Registered in `app:shared`'s `AppModule`. |
| `PageDtoMapper` | Maps `com.momosi.trucktrack.shared.common.PageDto<T>` (from the separate `shared` build, not defined here) → `Page<T>` (domain model from `core:common`). |
| `installOptInRequestRetry()` | `HttpClientConfig` extension installing Ktor's `HttpRequestRetry` with retries off (`noRetry()`). Used by `buildHttpClient`; tests in other modules call it to build a client that behaves like the real one. |

## Key Files

```
commonMain/
  di/NetworkModule.kt              ← Ktor HttpClient setup, Auth plugin, base URL
  AuthTokenCacheInvalidation.kt    ← HttpClient.invalidateAuthTokensOn: clears Ktor's cached bearer
                                      token whenever AuthManager.authenticationState changes
  dto/PageDtoMapper.kt             ← maps shared's PageDto<T>; no local PageDto class
  OptInRequestRetry.kt             ← installOptInRequestRetry(): HttpRequestRetry, off unless a request opts in
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

- After Ktor's authentication retry, a final HTTP 401 invalidates the local session using the
  rejected request's bearer token. HTTP 403, server errors, and connectivity failures do not
  invalidate authentication. Both the API and image clients use this behavior.

- **Retries are opt-in per request.** `buildHttpClient` installs `HttpRequestRetry` with
  `noRetry()`, so by default every request is sent once. A single request opts in with
  `HttpRequestBuilder.retry { }` (through a Ktorfit `@ReqBuilder` parameter), which overrides the
  plugin's predicates, delay and `maxRetries` for that request only. Opt in only idempotent requests,
  only for transport errors and 5xx, with a short backoff (set `respectRetryAfterHeader = false`, or a
  server's `Retry-After` can stretch it to minutes). Today only `core:push`'s registration `PUT` opts in
  (`registrationRetry()`).
- Plugin order: Ktor installs `HttpCallValidator` before any plugin configured here, so its `Send`
  hook is outermost. `HttpRequestRetry` therefore sees raw 4xx/5xx responses (a 4xx is never turned
  into an exception it could retry), and only the final outcome reaches `installApiExceptionMapping`,
  which maps it to the same `ApiException` as a request that wasn't retried.

## Tests

`src/commonTest` covers final unauthorized responses, successful authentication retries, forbidden
and server errors, connectivity failures, and unauthenticated requests (`SessionInvalidationTest`),
and the opt-in retry (`OptInRequestRetryTest`: a request without opt-in isn't retried on 503; an
opted-in one is retried after a 503 and a transport error, isn't retried on 409, and its final 503
maps to `ApiException.HttpError`), all using Ktor's mock engine through the real `buildHttpClient`
(`TestHttpClient.kt`: `withClient`, `FakeAuthManager`).
`trucktrack.test` provides the shared test dependencies and Android host-test target; this module
adds only the Ktor mock-engine dependency.
Run `.\gradlew.bat :core:network:testAndroidHostTest` from `app/` on Windows.
