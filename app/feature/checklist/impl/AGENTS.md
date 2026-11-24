# feature:checklist:impl

The vehicle checklist screen: a driver or mechanic picks a truck and a trailer, answers every check as OK or Problem, and submits. Problem answers are recorded only; they do not create issues.

## Files

| File | Description |
|------|-------------|
| `ChecklistScreen.kt` | Toolbar, then one of four phases: shimmer skeleton, full-screen `ErrorState`, the form, or the submitted confirmation. The form is a `LazyColumn` with a vehicles card (two `VehicleSelector`s from `:core:ui-library`, one per `VehicleType`), a "Checks" label and the questions as one grouped card of `CheckRow`s. On `ChecklistEvent.ScrollToMissing` it scrolls to the vehicles card or the first unanswered check; list indices come from the `LeadingItem` enum (items above the questions) plus the question index, never hard-coded positions. Hosts the discard `ConfirmationDialog` and a `BackHandler` that routes system back through `RequestClose` |
| `CheckRow.kt` | One compact question row: question number and server text (`bodyMedium`) on the left, a ? / ? segmented toggle on the right (48 dp tall segments, radio semantics, spoken labels "Problem"/"OK"). Rows are grouped into one card via `itemsPositioned` (rounded ends, dividers between rows). After a failed submit an unanswered row turns `errorContainer` and shows a short hint |
| `ChecklistSubmitBar.kt` | Sticky bottom bar: "x of y checked", problem count, a progress bar that turns positive when complete, the Submit button and the submit failure message |
| `ChecklistSubmittedContent.kt` | Confirmation shown after a successful submit: plates, submit time (`DateFormatter`), "No problems found" or the plural problem count, and Done |
| `ChecklistViewModel.kt` | Loads questions (`ChecklistRepository`) and vehicles (`VehicleRepository`) in parallel and preselects the remembered truck and trailer. Validates and submits (`true` for OK). On success records both vehicles with `recordVehicleUsage`. Submit failures are mapped to a `FailureReason` and shown under the Submit button; answers are kept. `RequestClose` opens the discard dialog only when unsubmitted answers exist |
| `ChecklistState.kt` | `ChecklistState` (separate flows for content, selected truck/trailer, expanded picker, answers keyed by identifier, submit status, discard dialog) plus `ChecklistContent` (`Loading`/`Error(FailureReason)`/`Loaded(questions, trucks, trailers)`), `CheckResult`, `ChecklistSubmitStatus` (`Idle`/`ValidationError`/`InProgress`/`Failed(FailureReason)`/`Submitted(summary)`) and `ChecklistSummary` |
| `ChecklistAction.kt` | `Retry`, `TogglePicker`, `SelectVehicle`, `Answer`, `Submit`, `RequestClose`, `ConfirmDiscard`, `DismissDiscard` |
| `ChecklistEvent.kt` | `Close`, `ScrollToMissing(MissingTarget)` (`Vehicles` or `Check(questionIndex)`), sent on every failed validation so repeated Submit taps scroll again |
| `navigation/ChecklistEntryProvider.kt` | Registers `ChecklistNavKey` with the bottom-sheet-style entry transition |
| `di/ChecklistFeatureModule.kt` | `checklistFeatureModule` with the ViewModel. Named to avoid clashing with `:core:checklist`'s `checklistModule` |

Entry point: the Checklist button stacked above the + button on the Issues screen, shown to drivers only.

## Package

`com.momosi.trucktrack.feature.checklist.impl`

## Depends On

- `:feature:checklist:api`
- `:core:checklist` — `ChecklistRepository`, checklist models
- `:core:vehicle` — `VehicleRepository`, `Vehicle`, `VehicleType`
- `:core:common` — `Logger`, `DateFormatter`, `FailureReason`, `ApiException`
- `:core:ui-library` — all UI, including `VehicleSelector`
- `:core:navigation` — `Navigator`
