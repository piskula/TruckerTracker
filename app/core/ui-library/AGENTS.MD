# core:ui-library

The single source of truth for all UI — theme, icons, and every reusable Compose Multiplatform component.
Feature modules must never import `androidx.compose.material3` directly; use this module instead.
KMP module: components in `commonMain`, `@Preview` functions in `androidMain`.

## Theme

| Symbol | Usage |
|--------|-------|
| `TruckTrackTheme { }` | Root theme wrapper. Use in every `@Preview`. |
| `AppTheme.colors` | Color tokens: `primary`, `onPrimary`, `surface`, `onSurface`, `onSurfaceVariant`, `negative`, `warning`, `positive`, `primaryContainer`, `warningContainer`, `positiveContainer`, etc. |
| `AppTheme.typography` | Text styles: `titleLarge`, `titleMedium`, `titleSmall`, `bodyLarge`, `bodyMedium`, `bodySmall`, `labelSmall`. |
| `Shapes.CardShape` | Standard card corner radius. |

## Icons

`TruckTrackIcons` — single object containing all icon references. Always add new icons here, never import Material icon classes directly in feature modules.

Key icons: `Person`, `Build`, `AccountCircle`, `Add`, `Back`, `CheckCircle`, `RadioButtonUnchecked`, `ArrowUpward`, `ArrowDownward`, `Remove`, `Truck` (custom), `Trailer` (custom), `Search`, `MoreVert`, `Send`, `ChatBubbleOutline`, `Info`.

## Components

| Component | File |
|-----------|------|
| `Text` | Wraps `BasicText` with `AppTheme.typography` defaults |
| `Icon` | Wraps `androidx.compose.foundation` icon |
| `Button`, `IconButton`, `TopBarIconButton` | Styled buttons |
| `FloatingActionButton` | FAB with icon |
| `Chip`, `FilterChipRow` | Filter chips for list screens |
| `DashboardTopBar` | Top bar with title + subtitle + actions slot |
| `Toolbar` | Standard back-arrow toolbar |
| `BottomSheet` | Styled modal bottom sheet |
| `ConfirmationDialog` | Two-button confirmation dialog |
| `InfoDialog` | Single-dismiss-button read-only info dialog with a generic content slot |
| `LoadingSpinner` | Full-screen loading indicator |
| `SkeletonBox` | Shimmer loading placeholder |
| `ShimmerGroup` (composable, `modifier` package) | Wraps a subtree so every `SkeletonBox`/`Modifier.shimmer()` inside shares one synchronized animation instead of pulsing independently |
| `PullToRefresh` | Pull-to-refresh wrapper |
| `SectionLabel` | Section header label |
| `Switch`, `Checkbox` | Form controls |
| `SearchBarActive`, `SearchBarInactive` | Search UI |
| `DateRangePickerDialog` | Date range picker |
| `NavigationBar` | Bottom navigation bar |

## Modifiers

| Modifier | Purpose |
|----------|---------|
| `Modifier.shimmer()` | Shimmer loading effect. Syncs to the nearest `ShimmerGroup` if composed within one, otherwise animates independently. |
| `Modifier.cardModifier()` | Standard card shape + background |
| `Modifier.collapsingToolbar()` | Scrolling toolbar collapse |
| `Modifier.sharedTransitionModifier()` | Shared element transitions |

## Animations

`NavEntryTransitions` — standard enter/exit transitions for Navigation 3 entries.

## Depends On

- Compose Multiplatform (`org.jetbrains.compose`): runtime, foundation, material3, icons
- `androidx.navigation3:navigation3-ui`

## Rules

- All new reusable components go here.
- Never expose `material3` types in public function signatures — wrap them.
- Always add a `@Preview` for every new component (in `androidMain`).
