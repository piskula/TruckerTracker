# core:checklist

Domain models and repository for the checklist domain. Fully KMP — all code in `commonMain`.

## Public API

| Symbol | Description |
|--------|-------------|
| `ChecklistRepository` | `suspend fun getQuestions(): Result<List<ChecklistQuestion>>`; `suspend fun submitResponse(submission: ChecklistSubmission): Result<ChecklistResponse>`; `val status: StateFlow<ChecklistStatus>`, `suspend fun refreshStatus(): Result<ChecklistStatus>` (`GET api/v1/checklist-response/status`) and `fun clearStatus()` |
| `ChecklistStatus` | The current user's checklist for the current week: `Unknown` (not loaded, or cleared on sign-out), `Unavailable` (the last check failed), `NotRequired`, `Pending`, `Completed(response)`. A successful submit sets `Completed`; a `409` submit refreshes it. A status response is dropped if `clearStatus()` or a successful submit happened while it was in flight, so a previous user's or pre-submit status never overwrites the current one; `refreshStatus` then returns the current status. The server decides the week and who must fill it in. Covered by `ChecklistRepositoryImplTest` (`:core:checklist:testAndroidHostTest`) |
| `ChecklistQuestion` | identifier, text |
| `ChecklistSubmission` | truckId, trailerId, answers |
| `ChecklistAnswer` | identifier, answer |
| `ChecklistResponse` | id, truck, trailer, answeredBy, answeredAt, items |

## Depends On

- `:core:common` — `Logger`
- `:core:network` — `Ktorfit`
- `com.momosi.trucktrack:shared` — checklist DTOs
