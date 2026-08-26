# feature:profile:impl

Profile screen showing the current user's info, a sign-out action, an app/server version info dialog, and an in-app language selector (English/Slovak).

## Files

| File | Description |
|------|-------------|
| `ProfileScreen.kt` | Displays user name, username, roles, Sign Out button, a toolbar info icon opening a version `InfoDialog` (app version + server version), and a toolbar language icon opening a language-selection `InfoDialog` (English/Slovak, current choice checked). The app version row is tappable, forwarding taps to `ProfileAction.TapAppVersion`. Selecting a language that needs a restart (iOS) shows a second, plain `InfoDialog` restart notice. |
| `ProfileViewModel.kt` | Loads user from `UserRepository`, calls `AuthManager.signOut()`, reads the app's own version from `AppVersionProvider`, fetches the server's build info from `VersionRepository`, delegates `TapAppVersion` to `TestCrashManager`, reads/writes the language choice via `LanguageRepository` and shows the restart notice when `LanguageRepository.requiresRestartToApply` is true. |
| `ProfileState.kt` | `user`, `isSigningOut`, `appVersion`, `isVersionDialogVisible`, `serverVersion` (`ServerVersionContent`: `Loading`/`Loaded`/`Error`), `language` (`AppLanguage`), `isLanguageDialogVisible`, `isRestartNoticeVisible` |
| `ProfileAction.kt` | `SignOut`, `ShowVersionInfo`, `DismissVersionInfo`, `TapAppVersion`, `ShowLanguageSelector`, `DismissLanguageSelector`, `SelectLanguage(AppLanguage)`, `DismissRestartNotice` |
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
- `:core:common` — `AppVersionProvider`, `DateFormatter`, `VersionRepository`, `AppEnvironment`, `LanguageRepository`, `AppLanguage`

