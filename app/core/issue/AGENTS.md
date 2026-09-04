# core:issue

Domain models, repositories, and DTOs for the issue (maintenance request) domain. Fully KMP — all code in `commonMain`.

## Public API

### Repositories

| Interface | Description |
|-----------|-------------|
| `IssueRepository` | CRUD + lifecycle actions on issues. See below for full method list. |
| `IssueAttachmentRepository` | Photo upload, download, listing, and deletion for an issue. |
| `IssueCapabilityRepository` | Resolves what the current user can edit and do on a given issue. See below. |

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
getIssuesPagingSource(statuses, vehicleIds, accountIds, sort): PagingSource<Int, Issue>
```

**`IssueAttachmentRepository` methods:**
```kotlin
getPhotoUrl(issueId: Long, attachmentId: Long): String
getPhotos(issueId, page, size, sort): Result<Page<IssueAttachment>>
uploadPhoto(issueId: Long, fileName: String, fileBytes: ByteArray, contentType: String): Result<IssueAttachment>
downloadPhoto(issueId: Long, attachmentId: Long): Result<ByteArray>
deletePhoto(issueId: Long, attachmentId: Long): Result<Unit>
```

Photo payloads cross the repository boundary as `ByteArray` + filename, never as a platform file
handle or response type — this module is `commonMain` only, so `java.io.File` and OkHttp's
`ResponseBody` are unavailable. Platform file pickers convert to bytes before calling in.

**`IssueCapabilityRepository` methods:**
```kotlin
resolve(issue: Issue, user: User): IssueCapabilities
```
Pure, stateless resolution — no network or persistence involved. This is the single source of truth
for what the current user may edit and do on an issue; feature modules should read
`IssueCapabilities` rather than re-deriving role/ownership checks themselves.
`IssueCapabilities` has two nested groups:
- `editing: EditingCapabilities` — per-field edit permissions. Nothing is editable once the issue is
  `Done` or `Cancelled`, regardless of role. Otherwise, for the reporting driver
  (`issue.reportedBy.id == user.id`, `user.isDriver`): title and vehicle are editable only while
  `Open`; description and priority stay editable through `InProgress` too, so the reporter can still
  add detail or reprioritize while a mechanic is actively working the issue. The assigned mechanic
  (`issue.assignedTo.id == user.id`, `user.isMechanic`) can edit vehicle — note `assignedTo` stays set
  after the issue closes, so this path relies on the terminal-state guard above rather than checking
  status itself.
- `actions: ActionCapabilities` — lifecycle actions. `nextStateAction` (`IssueStateAction?`) is the
  single next action available to the current user on the issue — `StartWorking`/`ResolveIssue`/
  `Reassign` for an assigned/eligible mechanic, or `Cancel` for the reporting driver while `Open`.
  `canDeletePhotos` mirrors the reporting-driver-while-`Open` rule. Note: if a user is somehow both
  the reporting driver and an eligible mechanic on the same issue, the mechanic action wins — this
  is an accepted edge case, not a deliberate priority rule.

### Domain Models

| Model | Description |
|-------|-------------|
| `Issue` | Core entity: id, title, description, status, priority, vehicle, reportedBy, assignedTo, createdAt, updatedAt |
| `IssueStatus` | `Open`, `InProgress`, `Done` |
| `IssuePriority` | `High`, `Medium`, `Low` |
| `Account` | Minimal user reference: id, username, firstName, lastName. Has `fullName` computed property. |
| `IssueCreate` | Input model for creating an issue |
| `IssueHistory` | A `sealed interface` history entry — `StatusChange`, `AssigneeChange`, `Comment`, `Update` — mirroring `IssueHistoryDto`'s polymorphic wire shape. Each subtype carries only its own fields (e.g. `StatusChange.statusTo`, `Comment.commentText`); there's no flat "one field per possible type" shape and no separate discriminant enum. |
| `IssueUpdatedField` | `Title`, `Description`, `Priority`, `Vehicle` — which fields an `IssueHistory.Update` entry changed |
| `IssueAttachment` | Photo metadata: id, filename, url |
| `IssueCapabilities` | What the current user may edit (`editing: EditingCapabilities`) and do (`actions: ActionCapabilities`) on an issue |
| `IssueStateAction` | `StartWorking`, `ResolveIssue`, `Reassign`, `Cancel` — the current user's next available action on an issue |

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
- `:core:user` — `AuthManager` (via network), `User` model (for `IssueCapabilityRepository`)
- `:core:vehicle` — `Vehicle` model referenced in `Issue`
- `com.momosi.trucktrack:shared` — `IssueDto`, `AccountDto`, `IssueCreateDto`, `IssueFilterDto`, `IssueHistoryDto`, status/priority enums (separate build, see `../../../shared/AGENTS.md`)
