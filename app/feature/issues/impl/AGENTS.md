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
| `IssueDetailAction.kt` | `StartIssue`, `ResolveIssue`, `AssignIssue`, `AddComment`, `UploadPhoto`, `DeletePhoto`, `ResolveConfirmDismiss` |

#### Start Working (`detail/startworking/`)

A `BottomSheet` for picking the mandatory repair type + vehicle system, shown from `IssueDetailScreen`
when the mechanic taps "Start Working" — decomposed into its own screen-shaped unit (own ViewModel,
State, Action, Event) rather than folded into `IssueDetailViewModel`, since it owns its own submit
call independent of the rest of the issue detail screen's concerns. `RepairType`/`VehicleSystem` are
fixed enums (`core:issue/model`), so the sheet has no options to fetch — it renders `RepairType.entries`/
`VehicleSystem.entries` directly and only makes one network call, on confirm.

| File | Description |
|------|-------------|
| `StartWorkingSheet.kt` | `BottomSheet` content + a generic `RadioOptionList<T>` (always-visible vertical list, `CheckCircle`/`RadioButtonUnchecked` per row — explicit single-choice semantics, unlike a chip row) and the `displayName()` string-resource mapping (all private to this file) |
| `StartWorkingViewModel.kt` | Holds the two selections, calls `IssueRepository.startIssue(...)` on confirm |
| `StartWorkingState.kt` | `StartWorkingState` (selections, `isSubmitting`, `canConfirm`) |
| `StartWorkingAction.kt` | `SelectRepairType`, `SelectVehicleSystem`, `Confirm` |
| `StartWorkingEvent.kt` | `Started(issue: Issue)` — one-shot event `IssueDetailScreen` collects to dismiss the sheet and forward the updated `Issue` to `IssueDetailViewModel` via `IssueDetailAction.IssueStarted` |

Note: `koinViewModel()` scopes `StartWorkingViewModel` to the issue detail screen's own
`ViewModelStoreOwner`, so the same instance (and its last selections) persists across dismiss/reopen
within one visit to the issue — intentional, not a bug, since nothing about the issue changes between
opens.

### Create Issue (`create/`)

| File | Description |
|------|-------------|
| `CreateIssueScreen.kt` | Form for title, description, vehicle, priority, photos |
| `CreateIssueViewModel.kt` | Validates + submits new issue; preselects `VehicleRepository.getVehicles()`'s `defaultVehicle` (the driver's remembered truck, or trailer if no truck is remembered) and records the used vehicle on successful submit (`recordVehicleUsage`) |
| `CreateIssueState.kt` | Form state with field values and validation. `vehicles` is the shared `VehiclesContent` |
| `CreateIssueAction.kt` | Field changes + submit |
| `CreateIssueEvent.kt` | `NavigateBack(issueId)` on success |

### Shared Form Building Blocks

| File | Description |
|------|-------------|
| `IssueFormState.kt` | `VehiclesContent` (`Loading`/`Error`/`Loaded(preferredTruck, preferredTrailer, otherVehicles)`) and `SubmitStatus` state definitions, shared by create and edit |
| `VehiclesContentMapper.kt` | `CategorizedVehicles.toVehiclesContent()` — maps `VehicleRepository`'s result straight into `VehiclesContent.Loaded`. Both `CreateIssueViewModel` and `EditIssueViewModel` call it, so the truck/trailer split happens once per load, never in the UI layer |
| `IssueFormFields.kt` | `VehicleSelector` — the shared vehicle picker composable for both create and edit. When `vehicles` is `Loaded`, it lists `preferredTruck`/`preferredTrailer` (in that fixed order) with dividers above and below the pair whenever at least one is remembered, then `otherVehicles` (already sorted by plate ASC). It only composes the already-split fields — no filtering/searching at render time. Also `Card`, `InputField`, `PrioritySelector` |

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

