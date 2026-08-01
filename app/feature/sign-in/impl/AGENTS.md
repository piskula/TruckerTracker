# feature:sign-in:impl

Sign-in screen and ViewModel. Triggers the OAuth/OIDC browser flow via `AuthManager`.

## Files

| File | Description |
|------|-------------|
| `SignInScreen.kt` | Single screen with a "Sign In" button. Shows loading and error states. |
| `SignInViewModel.kt` | Calls `AuthManager.signIn()`, emits events on result. |
| `SignInState.kt` | `Idle`, `Loading` |
| `SignInAction.kt` | `SignIn` |
| `SignInEvent.kt` | `NavigateToIssues` on success, `ShowError` on failure |
| `navigation/SignInEntryProvider.kt` | Registers the screen entry for `SignInNavKey` |

## Package

`com.momosi.trucktrack.feature.signin.impl`

## Depends On

- `:feature:sign-in:api`
- `:core:user` — `AuthManager`, `AuthActionResult`
- `:core:navigation` — `Navigator`
- `:core:ui-library`
- `:core:common`

