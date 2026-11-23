# feature:notification-permission:api

Navigation key for the notification permission gate. No business logic here.

## Nav Keys

| Key | Description |
|-----|-------------|
| `NotificationPermissionNavKey` | The blocking permission gate, a root destination that only `app:shared`'s `AppAccess` navigates to — never pushed on top of another screen. |

## Package

`com.momosi.trucktrack.feature.notificationpermission.api`

## Depends On

`:core:navigation` only (via `trucktrack.feature.api` convention plugin).
