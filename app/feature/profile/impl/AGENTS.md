# feature:profile:impl

Profile screen showing the current user's info, a sign-out action, and an app/server version info dialog.

## Files

| File | Description |
|------|-------------|
| `ProfileScreen.kt` | Displays user name, username, roles, Sign Out button, and a toolbar info icon opening a version `InfoDialog` (app version + server version). The app version row is tappable, forwarding taps to `ProfileAction.TapAppVersion`. |
| `ProfileViewModel.kt` | Loads user from `UserRepository`, calls `AuthManager.signOut()`, reads the app's own version from `AppVersionProvider`, fetches the server's build info from `VersionRepository`, delegates `TapAppVersion` to `TestCrashManager`. |
| `ProfileState.kt` | `user`, `isSigningOut`, `appVersion`, `isVersionDialogVisible`, `serverVersion` (`ServerVersionContent`: `Loading`/`Loaded`/`Error`) |
| `ProfileAction.kt` | `SignOut`, `ShowVersionInfo`, `DismissVersionInfo`, `TapAppVersion` |
| `ProfileEvent.kt` | `NavigateToSignIn` after sign-out |
| `TestCrashManager.kt` / `TestCrashManagerImpl.kt` | Counts rapid taps on the app version row; 3 taps within 1 second throws `TestCrashException` (a real, uncaught crash) — but only when `AppEnvironment.isDebug` is true, so this is a no-op in release builds. Used to verify Crashlytics reporting end-to-end. |
| `TestCrashException.kt` | The exception thrown by `TestCrashManagerImpl`, left uncaught so Crashlytics' own exception handler captures it. |
| `navigation/ProfileEntryProvider.kt` | Registers screen entry for `ProfileNavKey` |

## Package

`com.momosi.trucktrack.feature.profile.impl`

## Depends On

- `:feature:profile:api`
- `:core:user` — `AuthManager`, `UserRepository`, `User`, `UserRole`
- `:core:navigation` — `Navigator`
- `:core:ui-library`
- `:core:common` — `AppVersionProvider`, `DateFormatter`, `VersionRepository`, `AppEnvironment`

