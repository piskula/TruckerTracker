# core:navigation

Wraps Navigation 3 (`androidx.navigation3`) to provide a simple backstack-based navigation API. Fully KMP — all code in `commonMain`. Navigation 3 is KMP-compatible.

## Public API

| Symbol | Description |
|--------|-------------|
| `NavigationState` | Holds `backStack: NavBackStack<NavKey>` and `currentKey`. Created via `rememberNavigationState(startKey)`. |
| `Navigator` | Imperative navigation: `navigate(key)`, `navigate(root, destination)`, `navigateToRoot(key)`, `goBack()`. Inject via Koin `single`. |
| `NavigationState.toEntries(entryProvider)` | Composable extension. Converts the backstack to decorated `NavEntry` list with ViewModel + SaveableState support. |
| `ResultStore` | Mechanism to pass results back to the previous screen. |

## Key Files

```
commonMain/
  NavigationState.kt         ← rememberNavigationState(), toEntries()
  Navigator.kt
  ResultStore.kt
```

## Depends On

- `androidx.navigation3:navigation3-runtime`
- `androidx.lifecycle:lifecycle-viewmodel-navigation3`

`navigate(root, destination)` atomically replaces the backstack with the supplied root and destination
inside a mutable Compose snapshot, preserving the remembered backstack instance,
without duplicating a destination that is already current or adding the root twice when it is
the destination. Callers translate app-specific actions into navigation keys; this module has no
app-link or feature dependencies. Shared navigation tests run with `:core:navigation:testAndroidHostTest`.

## Usage Pattern

```kotlin
// In app:shared's TruckTrackApp
val navState = rememberNavigationState(startKey = SignInNavKey)
val navigator = remember { Navigator(navState) }

NavDisplay(entries = navState.toEntries { key ->
    entryProvider(key)
})
```


