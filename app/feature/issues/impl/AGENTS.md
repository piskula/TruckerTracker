# feature:issues:impl

UI and logic for the issues feature. Three screens: list, detail, create.

## Screens

### Issues List (`list/`)

| File | Description |
|------|-------------|
| `IssuesScreen.kt` | Top-level screen + `IssueList`, `LoadingContent` (shimmering skeleton list), `ErrorContent`, `EmptyContent` composables |
| `IssuesViewModel.kt` | Loads issues, handles filter selection and refresh |
| `IssuesState.kt` | `IssuesState` (sealed: `Loading`, `Error`, `Empty`, `Issues`) + `IssuesContent`, `IssueFilter` |
| `IssuesAction.kt` | `SelectFilter`, `Retry`, `Refresh` |
| `IssueCard.kt` | Card composable — role-aware (driver sees assignee, mechanic sees reporter) |
| `IssueCardSkeleton.kt` | Shimmer skeleton mirroring `IssueCard`'s layout, shown by `LoadingContent` during initial/refresh load |
| `IssueCardState.kt` | `@Immutable IssueCardState(issue, role)` — passed to `IssueCard` |
| `IssueCardRole.kt` | `sealed interface IssueCardRole { Driver, Mechanic }` |

**Role logic in `IssuesScreen`:**
- `isMechanic = !isDualRole && userInfo?.isMechanic == true`
- `isDriver = !isDualRole && !isMechanic`
- Dual-role defaults to `IssueCardRole.Driver`

### Issue Detail (`detail/`)

| File | Description |
|------|-------------|
| `IssueDetailScreen.kt` | Shows full issue: status, priority, description, history, photos, action buttons |
| `IssueDetailSkeleton.kt` | Shimmer skeleton mirroring the loaded layout (people strip, header, history, photos), shown while `content` is `Loading` |
| `IssueDetailViewModel.kt` | Loads issue + history + attachments, handles status transitions |
| `IssueDetailState.kt` | Sealed state with loading/error/content variants |
| `IssueDetailAction.kt` | `StartIssue`, `ResolveIssue`, `AssignIssue`, `AddComment`, `UploadPhoto`, `ResolveConfirmDismiss` |

### Create Issue (`create/`)

| File | Description |
|------|-------------|
| `CreateIssueScreen.kt` | Form for title, description, vehicle, priority, photos |
| `CreateIssueViewModel.kt` | Validates + submits new issue |
| `CreateIssueState.kt` | Form state with field values and validation |
| `CreateIssueAction.kt` | Field changes + submit |
| `CreateIssueEvent.kt` | `NavigateBack(issueId)` on success |

### Full Screen Photo (`FullScreenPhoto.kt`)

Standalone composable to display a single attachment full-screen.

## Navigation (`navigation/`)

| File | Description |
|------|-------------|
| `IssuesEntryProvider.kt` | Registers all screen entries for `IssuesNavKey`, `IssueDetailNavKey`, `CreateIssueNavKey`, `FullScreenPhotoNavKey` |
| `IssueDetailNavKey.kt` | Internal nav key carrying `issueId: Long` |
| `CreateIssueNavKey.kt` | Internal nav key |
| `FullScreenPhotoNavKey.kt` | Internal nav key carrying photo URL |

## Depends On

- `:feature:issues:api`
- `:core:issue` — `IssueRepository`, `IssueAttachmentRepository`, all domain models
- `:core:user` — `AuthManager`, `UserRepository`, `UserRole`
- `:core:vehicle` — `VehicleRepository`, `Vehicle`, `VehicleType`
- `:core:navigation` — `Navigator`
- `:core:ui-library` — all UI components
- `:core:common` — `Logger`, `DispatcherProvider`

