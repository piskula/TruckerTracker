# shared

Contract module: DTOs and enums shared between `app/` (the KMP client) and `server/` (the Spring Boot backend). Own Gradle build (own `settings.gradle.kts`), composite-built alongside `app/` and `server/` — see the root `AGENTS.md` for how the three fit together.

## Rules

- **Pure Kotlin Multiplatform + `kotlinx.serialization` only.** No Ktor, no Koin, no Spring, no Spring MVC/Jackson annotations, no Android-only or iOS-only APIs. It must stay usable from both a Spring Boot JVM process and a KMP client (Android + iOS).
- No `build-logic`/convention plugins of its own — plugins are applied directly in `build.gradle.kts`: `alias(libs.plugins.kotlin.multiplatform)`, `alias(libs.plugins.android.multiplatform.library)`, `alias(libs.plugins.kotlin.serialization)`.
- Targets: `jvm()` (consumed by `server/`), `androidTarget` via `android { }`, `iosArm64()`/`iosSimulatorArm64()` (consumed by `app/`). All source in `commonMain` — there's no platform-specific code here today.
- Changes here affect both `app/` and `server/`. Before merging, build both consumers from the repo root: `./gradlew :app:app:android:assembleDebug :server:module-server:bootJar`.

## Package Structure

```
com.momosi.trucktrack.shared.<domain>/
  <Name>Dto.kt          ← @Serializable data class, e.g. IssueDto, VehicleDto
  <EnumName>Dto.kt       ← @Serializable enum, e.g. IssueStatusDto, IssuePriorityDto
```

Current domains: `common` (`PageDto`, `PageableDto`, `ErrorDto`), `issue` (`IssueDto`, `IssueCreateDto`, `IssueFilterDto`, `IssueHistoryDto`, `AccountDto`, status/priority/history-event enums), `vehicle` (`VehicleDto`, `VehicleTypeDto`).

## Conventions

- Naming: `<Name>Dto` (not `<Name>DTO`), `<EnumName>Dto`.
- Dates: `kotlin.time.Instant`, never `java.time.OffsetDateTime` — not available on non-JVM KMP targets. `server/module-server`'s `config/JacksonConfig.kt` bridges this to/from JSON as a plain ISO-8601 string, so the wire format doesn't change.
- IDs that are UUIDs: `kotlin.uuid.Uuid`, never `java.util.UUID` — same KMP-availability reasoning. `JacksonConfig.kt` bridges this too; convert with `.toKotlinUuid()`/`.toJavaUuid()` at the mapper boundary on the server side.
- No framework annotations (Spring, Swagger, Jackson) on any type here — only `@Serializable`/`@SerialName` from `kotlinx.serialization`.

## Consumers

- `server/module-api` — imports these types directly into its Spring MVC contract interfaces.
- `app/core/issue`, `app/core/vehicle`, `app/core/user`, and other `app/core/*` modules that talk to the API.

Both declare it as a regular external coordinate: `implementation("com.momosi.trucktrack:shared")` (or `api(...)` if the type appears in the consuming module's own public API) — never `project(":shared")`, since `shared` is a separate Gradle build.

## See Also

- Root `AGENTS.md` — "Repository Layout" section, for why these are three separate Gradle builds instead of one.
- `../server/module-api/AGENTS.md` and `../server/module-server/AGENTS.md` — how the server maps these DTOs to/from domain models.
