# core:user

Handles authentication (OAuth/OIDC against a Keycloak realm via [kotlin-multiplatform-oidc](https://github.com/kalinjul/kotlin-multiplatform-oidc)), JWT verification, user profile, and session state. KMP module — almost entirely `commonMain`; only the auth-flow browser factory and token storage backing store are platform-specific.

## Public API

| Symbol | Description |
|--------|-------------|
| `AuthManager` | Interface. Exposes `authenticationState: StateFlow<AuthenticationState>`, `authenticationInProgress: StateFlow<Boolean>`, `loginCompletions: Flow<Unit>`, `signIn()`, `signOut(): AuthActionResult`, `token(): TokenResponse`, `invalidateSession(rejectedAccessToken)`. `signOut` needs a connection: offline, or when refreshing the access token or the end-session request fails on the network, it returns `Failed.NoInternet` and keeps the session (signing out must be able to unregister the push device). Otherwise it refreshes the access token, runs the Keycloak end-session flow and — unless the user cancelled it (`Failed.UserCancelled`, session kept) — runs every registered `SignOutHook` while the tokens are still stored (the refreshed access token is still accepted by the backend), then clears the session and returns `Success(Guest)`. Any other end-session failure still clears the session locally and returns `Failed.Error`. `loginCompletions` emits once per completed login — from `signIn()` and from a login resumed after Android killed the process during the browser step. It's backed by a buffered channel: a completion is kept until collected and delivered to one collector only (`core:push`'s `PushRegistrationManager`, which registers the device). |
| `SignOutHook` | `fun interface` with `suspend fun beforeSessionCleared()`. Other modules bind implementations in Koin (`bind SignOutHook::class`); `AuthManagerImpl` resolves them lazily (`getAll`) and runs each one during `signOut()` after the Keycloak logout and before clearing the session — failures are logged and don't stop sign-out. |
| `UserRepository` | Interface. Exposes `user: StateFlow<User?>`. |
| `AuthenticationState` | Enum: `Guest`, `Authorized`. |
| `AuthActionResult` | Sealed class: `Success`, `Failed` (`UserCancelled`, `NoInternet`, `NoActivity`, `Error`). |
| `User` | Domain model with id, name, email, username (login/`preferred_username` claim), roles. |
| `UserRole` | Enum: `Driver`, `Mechanic`. |
| `TokenResponse` | Sealed class: `Token`, `TokenError`, `GuestWithoutToken`. |

## Key Files

```
commonMain/
  AuthManager.kt                  ← Public interface
  AuthManagerImpl.kt              ← Orchestrates OpenIdConnectClient + CodeAuthFlowFactory + UserStorage + TokenVerifier
  UserRepository.kt / UserRepositoryImpl.kt
  model/User.kt, UserRole.kt, AuthenticationState.kt, AuthActionResult.kt, TokenResponse.kt
  model/StoredTokens.kt           ← Serializable token persistence model
  internal/UserStorage.kt         ← Interface: tokens, cached server public key, cached user claims
  internal/TokenVerifier.kt       ← RS256 verification via Jwt.parse (oidc-core) + RsaVerifier
  internal/RsaVerifier.kt         ← expect fun verifyRs256(...)
  internal/api/AuthApi.kt, RealmDto.kt ← fetches Keycloak realm's RS256 public key
  di/UserModule.kt                ← Koin bindings (common part) + expect platformUserModule()
androidMain/
  internal/UserStorageImpl.kt     ← SharedPreferences-backed
  internal/RsaVerifier.android.kt ← java.security.Signature
  di/UserModule.android.kt        ← binds AndroidCodeAuthFlowFactory (also as CodeAuthFlowFactory)
iosMain/
  internal/UserStorageImpl.kt     ← NSUserDefaults-backed
  internal/RsaVerifier.ios.kt     ← Security.framework (SecKeyVerifySignature)
  di/UserModule.ios.kt            ← binds IosCodeAuthFlowFactory (also as CodeAuthFlowFactory)
```

`AndroidCodeAuthFlowFactory` must be registered against the app's single Activity — `app:android`'s `TruckTrackActivity.onCreate()` injects it and calls `registerActivity(this)` before the redirect scheme (`oidcRedirectScheme` Gradle manifest placeholder) can resolve. iOS needs no such registration — `ASWebAuthenticationSession` is used internally by `IosCodeAuthFlowFactory` and needs no manifest/plist setup.

Token verification intentionally keeps today's Keycloak-specific `{realm}` endpoint (raw PEM public key) rather than switching to standard JWKS — that's a separate potential follow-up, not required by the OIDC library migration itself.

`invalidateSession` clears local tokens and user data without launching browser logout. It is serialized with token refresh and only invalidates the matching access token, so a delayed 401 cannot clear a newer session.

## Depends On

- `:core:common` — `Logger`, `DispatcherProvider`, `ConnectivityManager`
- `:core:network` — networking infrastructure
