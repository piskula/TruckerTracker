# feature:checklist:impl

The vehicle checklist screen: a driver or mechanic picks a truck and a trailer, answers every check as OK or Problem, and submits. Problem answers are recorded only; they do not create issues.

## Files

| File | Description |
|------|-------------|
| `ChecklistScreen.kt` | Toolbar, then one of five phases (`ChecklistState.phase`): shimmer skeleton, full-screen `ErrorState`, the form, the submitted confirmation, or the read-only "already completed for this week" summary. The form is a `LazyColumn` with a vehicles card (two `VehicleSelector`s from `:core:ui-library`, one per `VehicleType`), a "Checks" label and the questions as one grouped card of `CheckRow`s. On `ChecklistEvent.ScrollToMissing` it scrolls to the vehicles card or the first unanswered check; list indices come from the `LeadingItem` enum (items above the questions) plus the question index, never hard-coded positions. Hosts the discard `ConfirmationDialog` and a `BackHandler` that routes system back through `RequestClose` |
| `CheckRow.kt` | One compact question row: question number and server text (`bodyMedium`) on the left, a ? / ? segmented toggle on the right (48 dp tall segments, radio semantics, spoken labels "Problem"/"OK"). Rows are grouped into one card via `itemsPositioned` (rounded ends, dividers between rows). After a failed submit an unanswered row turns `errorContainer` and shows a short hint |
| `ChecklistSubmitBar.kt` | Sticky bottom bar: "x of y checked", problem count, a progress bar that turns positive when complete, the Submit button and the submit failure message |
| `ChecklistSubmittedContent.kt` | Summary of a submission: a title, plates, submit time (`DateFormatter`), "No problems found" or the plural problem count, an optional note, and Done. Used after a successful submit ("Checklist submitted") and when the week's checklist was already completed ("Checklist already completed for this week" plus "next one from Monday") |
| `ChecklistViewModel.kt` | Refreshes the weekly status (`ChecklistRepository.refreshStatus`) and loads questions (`ChecklistRepository`) and vehicles (`VehicleRepository`) in parallel. The visible content follows `ChecklistRepository.status`: whenever it is `Completed` (on opening, or learned later, e.g. after a `409`) the screen shows `AlreadyCompleted` with that response's summary instead of the form, so the user can't hit the `409`; otherwise the form. Preselects the remembered truck and trailer. Validates and submits (`true` for OK). On success records both vehicles with `recordVehicleUsage`. Submit failures are mapped to a `FailureReason` and shown under the Submit button; answers are kept. A `409` (the server's "already filled this week" rule) becomes `AlreadySubmittedThisWeek` instead, with its own message and no retry hint. `RequestClose` opens the discard dialog only when unsubmitted answers exist |
| `ChecklistState.kt` | `ChecklistState` (separate flows for content, selected truck/trailer, expanded picker, answers keyed by identifier, submit status, discard dialog; derived `phase`) plus `ChecklistContent` (`Loading`/`Error(FailureReason)`/`Loaded(questions, trucks, trailers)`/`AlreadyCompleted(summary)`), `ChecklistPhase`, `CheckResult`, `ChecklistSubmitStatus` (`Idle`/`ValidationError`/`InProgress`/`Failed(FailureReason)`/`AlreadySubmittedThisWeek`/`Submitted(summary)`) and `ChecklistSummary` |
| `ChecklistAction.kt` | `Retry`, `TogglePicker`, `SelectVehicle`, `Answer`, `Submit`, `RequestClose`, `ConfirmDiscard`, `DismissDiscard` |
| `ChecklistEvent.kt` | `Close`, `ScrollToMissing(MissingTarget)` (`Vehicles` or `Check(questionIndex)`), sent on every failed validation so repeated Submit taps scroll again |
| `navigation/ChecklistEntryProvider.kt` | Registers `ChecklistNavKey` with the bottom-sheet-style entry transition |
| `di/ChecklistFeatureModule.kt` | `checklistFeatureModule` with the ViewModel. Named to avoid clashing with `:core:checklist`'s `checklistModule` |

Entry points: the Checklist button stacked above the + button on the Issues screen (shown while this week's checklist is pending), the `https://transroute.org/checklist` app link from the reminder push, and the automatic redirect from `app:shared` (see `../../../app/shared/AGENTS.md` and `../../../../docs/checklist.md`).

## Tests

`src/commonTest` — `ChecklistViewModelTest`: a `409` submit failure shows `AlreadySubmittedThisWeek`, other failures stay a retryable `Failed`; a `Completed` status shows the read-only summary (also when learned while the form is open), and a failed status check still opens the form. Run `.\gradlew.bat :feature:checklist:impl:testAndroidHostTest` from `app/`.

## Package

`com.momosi.trucktrack.feature.checklist.impl`

## Depends On

- `:feature:checklist:api`
- `:core:checklist` — `ChecklistRepository`, checklist models
- `:core:vehicle` — `VehicleRepository`, `Vehicle`, `VehicleType`
- `:core:common` — `Logger`, `DateFormatter`, `FailureReason`, `ApiException`
- `:core:ui-library` — all UI, including `VehicleSelector`
- `:core:navigation` — `Navigator`
