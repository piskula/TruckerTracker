# feature:notification-permission:impl

The blocking notification permission gate. TruckTrack requires notification permission (see `../../../../docs/push-notifications.md`), so a signed-in user without it sees only this screen: an illustration, why drivers and mechanics need notifications, a primary button and "Sign Out".

## Files

| File | Description |
|------|-------------|
| `NotificationPermissionScreen.kt` | `NotificationPermissionScreen` (ViewModel-backed) and the stateless `NotificationPermissionContent`. The primary button is "Allow Notifications" (shows the system dialog through `core:push`'s `rememberNotificationPermissionRequester`) or "Open Settings" when the system dialog can't be shown any more. Re-checks the permission on every resume (`LifecycleResumeEffect`) and after the system dialog closes. |
| `NotificationPermissionViewModel.kt` | Maps `NotificationPermission.currentStatus` to the primary button, refreshes the status, opens settings, and signs out with `AuthManager.signOut()` (reports `ApiException.NoConnection` to `ErrorReporter` when it returns `Failed.NoInternet`). It doesn't navigate: once the status becomes `Granted`, `app:shared`'s `AppAccess` moves the user on. |
| `NotificationPermissionState.kt` | `requestRoute` (`PermissionRequestRoute.SystemDialog` / `Settings`), `isSigningOut` |
| `NotificationPermissionAction.kt` | `RefreshPermission`, `OpenSettings`, `SignOut` |
| `navigation/NotificationPermissionEntryProvider.kt` | `notificationPermissionEntries()` registers `NotificationPermissionNavKey` |
| `di/NotificationPermissionModule.kt` | Koin `viewModel` binding |
| `androidMain/.../NotificationPermissionScreenPreview.kt` | Previews of `NotificationPermissionContent` |

The illustration is `core:ui-library`'s `TruckTrackIllustrations.NotificationPermission`
(`notification_permission_illustration.xml`): a hand-authored vector of a phone with issue notifications,
a truck and a ringing bell on a primary-container circle, so it reads on light and dark surfaces.

## Rules

- The gate is an ordinary **root destination** and never navigates itself. `app:shared`'s `AppAccess` sends a signed-in user here when the permission is missing, moves them on once it's granted, and sends them to sign-in after signing out. Never push it on top of another screen; as the only back-stack entry it keeps the rest of the app unreachable and Android back leaves the app.

## Package

`com.momosi.trucktrack.feature.notificationpermission.impl`

## Depends On

- `:feature:notification-permission:api`
- `:core:push` — `NotificationPermission`, `rememberNotificationPermissionRequester`
- `:core:user` — `AuthManager`
- `:core:common` — `Logger`
- `:core:ui-library`, `:core:navigation` (via `trucktrack.feature.impl`)
