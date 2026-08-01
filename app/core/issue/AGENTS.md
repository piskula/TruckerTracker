# core:issue

Domain models, repositories, and DTOs for the issue (maintenance request) domain. Fully KMP — all code in `commonMain`.

## Public API

### Repositories

| Interface | Description |
|-----------|-------------|
| `IssueRepository` | CRUD + lifecycle actions on issues. See below for full method list. |
| `IssueAttachmentRepository` | Photo upload, download, and listing for an issue. |

**`IssueRepository` methods:**
```kotlin
getIssues(statuses, vehicleIds, accountIds, page, size): Result<Page<Issue>>
getIssue(id: Long): Result<Issue>
createIssue(issueCreate: IssueCreate): Result<Issue>
startIssue(id: Long): Result<Issue>
resolveIssue(id: Long): Result<Issue>
assignIssue(id: Long): Result<Issue>
addComment(issueId: Long, comment: String): Result<IssueHistory>
getIssueHistory(issueId, page, size): Result<Page<IssueHistory>>
getIssuesPagingSource(statuses, vehicleIds, accountIds): PagingSource<Int, Issue>
```

**`IssueAttachmentRepository` methods:**
```kotlin
getPhotoUrl(issueId: Long, attachmentId: Long): String
getPhotos(issueId, page, size, sort): Result<Page<IssueAttachment>>
uploadPhoto(issueId: Long, fileName: String, fileBytes: ByteArray, contentType: String): Result<IssueAttachment>
downloadPhoto(issueId: Long, attachmentId: Long): Result<ByteArray>
```

Photo payloads cross the repository boundary as `ByteArray` + filename, never as a platform file
handle or response type — this module is `commonMain` only, so `java.io.File` and OkHttp's
`ResponseBody` are unavailable. Platform file pickers convert to bytes before calling in.

### Domain Models

| Model | Description |
|-------|-------------|
| `Issue` | Core entity: id, title, description, status, priority, vehicle, reportedBy, assignedTo, createdAt, updatedAt |
| `IssueStatus` | `Open`, `InProgress`, `Done` |
| `IssuePriority` | `High`, `Medium`, `Low` |
| `Account` | Minimal user reference: id, username, firstName, lastName. Has `fullName` computed property. |
| `IssueCreate` | Input model for creating an issue |
| `IssueHistory` | A history entry (comment or status change) |
| `IssueHistoryType` | `Comment`, `StatusChange`, `Assignment` |
| `IssueAttachment` | Photo metadata: id, filename, url |

## Key Files

```
commonMain/
  IssueRepository.kt
  IssueRepositoryImpl.kt
  IssueAttachmentRepository.kt
  IssueAttachmentRepositoryImpl.kt
  model/                     ← All domain models — pure Kotlin
  dto/DtoMappers.kt          ← maps shared's DTOs (IssueDto, AccountDto, etc.) -> domain models; no local DTO classes
  di/IssueModule.kt          ← Koin bindings
```

## Depends On

- `:core:common` — `Page<T>`, `Logger`, `DispatcherProvider`
- `:core:network` — Ktor `HttpClient`, `PageDto` mapper
- `:core:user` — `AuthManager` (via network)
- `:core:vehicle` — `Vehicle` model referenced in `Issue`
- `com.momosi.trucktrack:shared` — `IssueDto`, `AccountDto`, `IssueCreateDto`, `IssueFilterDto`, `IssueHistoryDto`, status/priority enums (separate build, see `../../../shared/AGENTS.md`)
