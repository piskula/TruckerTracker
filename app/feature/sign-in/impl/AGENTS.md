# feature:sign-in:impl

Sign-in screen and ViewModel. Triggers the OAuth/OIDC browser flow via `AuthManager`.

## Files

| File | Description |
|------|-------------|
| `SignInScreen.kt` | Single screen with a "Sign In" button. Shows loading and error states. |
| `SignInViewModel.kt` | Calls `AuthManager.signIn()` and maps failures to error states. It doesn't navigate: once the user is signed in, `app:shared`'s `AppAccess` moves them on (to the notification permission gate or the issues list). No push code here: `core:push` registers the device on `AuthManager.loginCompletions` by itself. |
| `SignInState.kt` | `Idle`, `Loading`, `Error` (`NoInternet`, `NoActivity`, `Generic`) |
| `SignInAction.kt` | `SignInClick` |
| `navigation/SignInEntryProvider.kt` | Registers the screen entry for `SignInNavKey` |

## Rules

- Sign-in never navigates away. Leaving it is `app:shared`'s `AppAccess` decision — don't add navigation here.

## Package

`com.momosi.trucktrack.feature.signin.impl`

## Depends On

- `:feature:sign-in:api`
- `:core:user` — `AuthManager`, `AuthActionResult`
- `:core:ui-library`
- `:core:common`