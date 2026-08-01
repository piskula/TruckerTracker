# core:user

Handles authentication (OAuth/OIDC against a Keycloak realm via [kotlin-multiplatform-oidc](https://github.com/kalinjul/kotlin-multiplatform-oidc)), JWT verification, user profile, and session state. KMP module — almost entirely `commonMain`; only the auth-flow browser factory and token storage backing store are platform-specific.

## Public API

| Symbol | Description |
|--------|-------------|
| `AuthManager` | Interface. Exposes `authenticationState: StateFlow<AuthenticationState>`, `authenticationInProgress: StateFlow<Boolean>`, `signIn()`, `signOut()`, `token(): TokenResponse`. |
| `UserRepository` | Interface. Exposes `user: StateFlow<User?>`. |
| `AuthenticationState` | Enum: `Guest`, `Authorized`. |
| `AuthActionResult` | Sealed class: `Success`, `Failed` (`UserCancelled`, `NoInternet`, `NoActivity`, `Error`). |
| `User` | Domain model with id, name, email, roles. |
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

## Depends On

- `:core:common` — `Logger`, `DispatcherProvider`, `ConnectivityManager`
- `:core:network` — networking infrastructure
