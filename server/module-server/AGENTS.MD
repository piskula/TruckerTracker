# module-server

Spring Boot 4 backend (Kotlin, Java 25). Base package: `sk.momosilabs.truckTrack`.

## Layering Rules

```
Controller → UseCase → Persistence (or shared Service)
```

- **Controllers** implement the `module-api` interface and inject use cases only. No persistence calls, no business logic.
- **Use cases** contain business logic. They call persistence or shared services. **Use cases never call other use cases.** If two use cases need the same query, both call persistence independently.
- **Persistence** handles all data access.

Use case package and class names must not use plural nouns — use descriptive action + singular noun: `getIssueList`, `getVehicleList`, `createIssue`, `startIssue`, etc.

Each use case lives in its own package under `.service.<useCaseName>/` with two files:
- `<UseCaseName>UseCase.kt` — interface
- `<UseCaseName>.kt` — `@Service class` implementing the interface, with a security annotation and `@Transactional` on the override method

The single method on the interface must not be named `execute`. Name it after the action: `get`, `create`, `start`, `resolve`, `delete`, etc.

**Every use case override method must have exactly one of:**
- `@IsDriver` — `hasRole('DRIVER')` — drivers only
- `@IsMechanic` — `hasRole('MECHANIC')` — mechanics only
- `@IsUser` — `hasAnyRole('DRIVER', 'MECHANIC')` — any authenticated user with a known role

This is a hard rule — no use case method may be left without a security annotation. `@IsUser` is the default for operations accessible to all roles. Annotation goes above `@Transactional`.

## Entity & Persistence Patterns

**Entity class:**
- Regular Kotlin class (NOT a data class). Fields as constructor properties.
- `@Entity(name = "table_name")` with explicit lowercase snake_case name. No `@Table` annotation.
- ID: `val id: Long = 0L` with `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`. Default `0L` allows construction without a DB round-trip.
- For externally-assigned PKs (e.g. Keycloak UUID): `val id: UUID` with no `@GeneratedValue` — caller provides the value.
- Non-nullable fields: Kotlin non-nullable type + `@field:NotNull` (note the `@field:` target prefix).
- Nullable fields: Kotlin `?` suffix only.
- `val` for immutable fields (id, FK refs); `var` for fields that get updated after creation.
- **Relationships: always `@ManyToOne` only — never `@OneToMany`, never bidirectional.** This is a deliberate rule.
- Non-nullable `@ManyToOne`: use `@ManyToOne(optional = false)` + `@field:NotNull`. Omit `@JoinColumn` unless a custom column name is required.
- Annotation order on a parameter: JPA/domain annotation first (`@Enumerated`, `@ManyToOne`), then `@field:NotNull` on the next line.
- Blank line between every constructor parameter block for readability.
- No shared base entity, no `@MappedSuperclass`, no `@Embeddable`.
- `LocalDateTime` for timestamps in entities; field names suffixed with `Utc` (e.g. `startedAtUtc`).
- `AccountEntity` (package `account`) stores only `id` (Keycloak UUID), `username`, `fullName` — no `role` column. Role is read from the JWT at runtime.

**Model class:**
- `data class` in `model/` package. `id = 0L` or `UUID(0,0)` signals a new (unsaved) record.
- Timestamps use `OffsetDateTime` — no `Utc` suffix (e.g. `createdAt: OffsetDateTime`).
- Mapper converts: entity → model via `.toUtcOffsetDateTime()`; model → entity via `.toUtcLocalDateTime()`. Both are extension functions in `util/TimeExtensions.kt`. Never use raw `.atOffset(ZoneOffset.UTC)` or `.toLocalDateTime()` — the latter strips the offset without converting the instant.

**Package layout per domain:**
```
<domain>/
  controller/
    <Name>Controller.kt               ← @RestController; implements module-api interface; injects use cases only
  entity/
    <Name>Entity.kt
  model/
    <Name>Model.kt                    ← data class; id = 0L / UUID(0,0) for new records
  service/
    <Name>Persistence.kt              ← interface owned by the service layer (DIP); returns models, never entities
    <useCaseName>/
      <UseCaseName>UseCase.kt
      <UseCaseName>.kt
  persistence/
    <Name>PersistenceProvider.kt      ← @Repository class implementing the interface
    repository/
      <Name>Repository.kt             ← JpaRepository<Entity, Id> interface
    mapper/
      <Name>EntityMapper.kt           ← extension fns: fun Entity.toModel() = Model(...)
```

Controllers live inside their domain package (`<domain>/controller/`). There is no shared top-level `controller/` package.

**Filter data classes:**
- When a persistence `findPage` (or similar) has multiple optional filter params, wrap them in a `data class <Domain>ListFilter(...)` with nullable fields defaulting to `null`.
- Place this class in the `service/` package (not a use-case subpackage) so the persistence interface can import it.

**Persistence provider:**
- `@Repository class` implementing the `<Name>Persistence` interface.
- Constructor-injects repositories (and `EntityManager` if QueryDSL is needed).
- Read methods: `@Transactional(readOnly = true)`. Write methods: `@Transactional`.
- Throws `GlobalNotFoundException("fieldName=value not found")` for missing records.
- Never combine create and update in a single `save()` method. Expose `create(model)` and `update(model)` as separate functions on both the interface and the provider.
- Prefer `getReferenceById` over `findById.orElseThrow` — it avoids a SELECT and lets JPA throw `EntityNotFoundException` on access. Exception: `update()` must use `findById` to load the full entity before mutating fields.
- In `create()` and `saveHistory()`: name the local variable `entityToSave`; return `repository.save(entityToSave).toModel()` directly — do not do `val saved = ...; model.copy(id = saved.id)`.
- In `update()`: extract mutation into a private extension function `private fun EntityClass.updateWith(toUpdate: ModelClass)` on the entity. Within a `@Transactional` method the entity is already managed — call `entity.updateWith(model)` then return `entity.toModel()` directly; no explicit `repository.save()` needed (Hibernate dirty checking flushes on commit).
- When building a `Specification`, start with `var spec = Specification<EntityType> { _, _, cb -> cb.conjunction() }` (always-true base). `Specification.where(null)` does not compile — Spring Data JPA 3.x exposes only non-nullable overloads in its Kotlin API. Add conditions with `spec.and(Specification { root, _, cb -> ... })` using explicit `Specification<T> { }` SAM syntax to avoid ambiguity with the `PredicateSpecification` overload.

**Mapper:**
- File-level extension functions only — no mapper class.
- `fun EntityClass.toModel() = ModelClass(...)` for entity → model.
- `fun ModelToCreate.asNewEntity(resolver: (id) -> RelatedEntity) = Entity(...)` for model → entity when FK resolution is needed.

## Shared Utilities (`util/`)

**`util/TimeExtensions.kt`** — timezone-safe conversion helpers. Always use these; never raw `.toLocalDateTime()` or `.atOffset(ZoneOffset.UTC)`:
- `OffsetDateTime.toUtcLocalDateTime()` — converts to UTC then strips offset (entity storage)
- `LocalDateTime.toUtcOffsetDateTime()` — attaches UTC offset (model hydration)

**`util/PaginationExtensions.kt`** — shared pagination helpers used by all controllers:
```kotlin
inline fun <T : Any, U> Page<T>.toDto(transform: (T) -> U): PageDto<U>

fun PageableDto.toModel(): Pageable   // converts shared DTO → Spring Pageable

fun String?.toSortModel(): Sort       // parses "property,direction;property2,direction2"
                                      // multiple columns separated by ";"
```
`PageDto`/`PageableDto` come from `com.momosi.trucktrack.shared.common` (the separate `:shared` build), not from module-api. Controllers import `toDto` and `toModel` from this file — do not re-implement them locally.

## Controller Conventions

- Controllers map between `shared`'s DTO enums and server-side enums via `.name` / `valueOf`:
  - DTO → server: `ServerEnum.valueOf(dto.name)`
  - server → DTO: `EnumDto.valueOf(serverEnum.name)`
- Private mapping functions (`toDto()`, `toModel()`) are file-level extension functions, typically in a sibling `<Domain>Mapper.kt` file.
- Use `pageable.toModel()` (from `util/PaginationExtensions.kt`) to convert `PageableDto` to Spring `Pageable`.
- Use `.toDto { it.toDto() }` (from `util/PaginationExtensions.kt`) to map `Page<Model>` to `PageDto<Dto>`.
- Timestamps: domain models use `OffsetDateTime`; `shared`'s DTOs use `kotlin.time.Instant`. Convert with `.toInstant().toKotlinInstant()` (model → DTO) — the `toKotlinInstant()` extension is `kotlin.time.toKotlinInstant()` from the Kotlin stdlib, not `kotlinx.datetime`.
- UUIDs: domain models use `java.util.UUID`; `shared`'s DTOs use `kotlin.uuid.Uuid`. Convert with `.toKotlinUuid()` / `.toJavaUuid()` (`kotlin.uuid.*`, `@OptIn(ExperimentalUuidApi::class)`).

## Jackson 3.x / Spring Boot 4.x — Kotlin Data Class Deserialization

Spring Boot 4.x uses Jackson 3.x (`tools.jackson.databind.ObjectMapper`). The `jackson-module-kotlin` artifact on the classpath is version 2.x and is **incompatible** with Jackson 3.x — it silently does nothing.

Two config items are required for Kotlin data class deserialization to work:

1. **`-java-parameters` compiler flag** (in root `build.gradle.kts`):
   ```kotlin
   compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict", "-java-parameters") }
   ```
   This preserves constructor parameter names in standard Java bytecode so Jackson 3.x can read them.

2. **`spring.jackson.constructor-detector: use-properties-based`** (in `application.yml`):
   Tells Jackson 3.x to match JSON fields to constructor parameters by name instead of requiring `@JsonCreator`.

Without both, `@RequestBody` deserialization of Kotlin data classes fails at runtime with `InvalidDefinitionException: no Creators`.

## UUID Primary Keys — Never Use `@GeneratedValue(GenerationType.UUID)` with Caller-Provided UUIDs

Hibernate 7.x fails with "Row was already updated or deleted by another transaction" when:
- An entity has `@GeneratedValue(strategy = GenerationType.UUID)`
- But the caller pre-assigns a UUID (e.g. `UUID.randomUUID()`) before calling `repository.save()`

Spring Data JPA sees a non-null UUID and calls `merge()` instead of `persist()`. Hibernate 7.x's merge path for a `@GeneratedValue` entity with a caller-provided UUID hits a conflict.

**Rule:** If the UUID is assigned by the caller (e.g. `UUID.randomUUID()` in a use case), declare the entity `@Id val id: UUID` with **no** `@GeneratedValue`. This tells Hibernate the caller owns the ID, and `merge()` correctly inserts a new row when no row exists.

## MinIO File Storage

`MinioFileStorageService.upload()` checks for bucket existence before uploading and creates it if missing:
```kotlin
if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
}
```
Download and delete do not check — a missing bucket/object should surface as a MinIO exception.

## See Also

- `../AGENTS.MD` — server-wide conventions, build commands, local env vars.
- `../module-api/AGENTS.MD` — the API contract layer this module implements.
