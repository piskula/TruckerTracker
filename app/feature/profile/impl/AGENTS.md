# feature:profile:impl

Profile screen showing the current user's info, a push notifications section, a sign-out action, an app/server version info dialog, and an in-app language selector (English/Slovak).

## Files

| File | Description |
|------|-------------|
| `ProfileScreen.kt` | Displays user name, username, roles, the "Push Notifications" section, Sign Out button, a toolbar info icon opening a version `InfoDialog` (app version + server version), and a toolbar language icon opening a language-selection `InfoDialog` (English/Slovak, current choice checked). The app version row is tappable, forwarding taps to `ProfileAction.TapAppVersion`. Selecting a language that needs a restart (iOS) shows a second, plain `InfoDialog` restart notice. The push section is a private composable driven by `ProfileState.pushNotifications`: a "Receive notifications on this device" switch — on when this device holds the push slot; off with "Notifications currently go to {name}." when another device does; off when none does. Load failures show an inline message with "Try Again". Turning it on while another device holds the slot asks for confirmation first. |
| `ProfileViewModel.kt` | Loads user from `UserRepository`, signs out with `AuthManager.signOut()` (core:push unregisters the device through its `SignOutHook`; navigates to sign-in on `Success`, reports `ApiException.NoConnection` to `ErrorReporter` on `Failed.NoInternet`), reads the app's own version from `AppVersionProvider`, fetches the server's build info from `VersionRepository`, delegates `TapAppVersion` to `TestCrashManager`, reads/writes the language choice via `LanguageRepository` and shows the restart notice when `LanguageRepository.requiresRestartToApply` is true. For push it refreshes `PushRegistrationManager.state` when the screen opens, registers/unregisters on toggle and refreshes afterwards; action failures go to `ErrorReporter` (snackbar), the load failure is a `FailureReason` in state. |
| `ProfileState.kt` | `user`, `isSigningOut`, `appVersion`, `isVersionDialogVisible`, `serverVersion` (`ServerVersionContent`: `Loading`/`Loaded`/`Error`), `language` (`AppLanguage`), `isLanguageDialogVisible`, `isRestartNoticeVisible`, `pushNotifications` (`PushNotificationsState`: `target` — `Loading`/`LoadFailed`/`ThisDevice`/`OtherDevice`/`NoDevice` — and `request` — `Idle`/`Updating`/`ConfirmingTakeover`) |
| `ProfileAction.kt` | `SignOut`, `ShowVersionInfo`, `DismissVersionInfo`, `TapAppVersion`, `ShowLanguageSelector`, `DismissLanguageSelector`, `SelectLanguage(AppLanguage)`, `DismissRestartNotice`, `ChangeReceivePushNotifications(enabled)`, `ConfirmPushNotificationsTakeover`, `DismissPushNotificationsTakeover`, `RetryPushNotifications` |
| `ProfileEvent.kt` | `NavigateToSignIn` after sign-out |
| `TestCrashManager.kt` / `TestCrashManagerImpl.kt` | Counts rapid taps on the app version row; 3 taps within 1 second throws `TestCrashException` (a real, uncaught crash) — but only when `AppEnvironment.isDebug` is true, so this is a no-op in release builds. Used to verify Crashlytics reporting end-to-end. |
| `TestCrashException.kt` | The exception thrown by `TestCrashManagerImpl`, left uncaught so Crashlytics' own exception handler captures it. |
| `navigation/ProfileEntryProvider.kt` | Registers screen entry for `ProfileNavKey` |

## Package

`com.momosi.trucktrack.feature.profile.impl`

## Depends On

- `:feature:profile:api`
- `:core:user` — `UserRepository`, `User`, `UserRole`
- `:core:push` — `PushRegistrationManager`
- `:core:navigation` — `Navigator`
- `:core:ui-library`
- `:core:common` — `AppVersionProvider`, `DateFormatter`, `VersionRepository`, `AppEnvironment`, `LanguageRepository`, `AppLanguage`

