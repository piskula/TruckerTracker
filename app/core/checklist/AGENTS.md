# core:checklist

Domain models and repository for the checklist domain. Fully KMP — all code in `commonMain`.

## Public API

| Symbol | Description |
|--------|-------------|
| `ChecklistRepository` | `suspend fun getQuestions(): Result<List<ChecklistQuestion>>`; `suspend fun submitResponse(submission: ChecklistSubmission): Result<ChecklistResponse>` |
| `ChecklistQuestion` | identifier, text |
| `ChecklistSubmission` | truckId, trailerId, answers |
| `ChecklistAnswer` | identifier, answer |
| `ChecklistResponse` | id, truck, trailer, answeredBy, answeredAt, items |

## Depends On

- `:core:common` — `Logger`
- `:core:network` — `Ktorfit`
- `com.momosi.trucktrack:shared` — checklist DTOs
