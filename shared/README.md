# TruckTrack — Shared Contract

Contract module: DTOs and enums shared between `app/` (the Kotlin Multiplatform client) and
`server/` (the Spring Boot backend). This is `shared/` — its own Gradle build, part of the
`TruckTrack` composite build. See the repo root `README.md` for how this fits alongside `app/` and
`server/`.

## Why this exists

Before this module existed, both sides hand-duplicated the same network types — and drifted:
`java.time.OffsetDateTime` vs. a client-side string, `java.util.UUID` vs. a client-side string,
untyped `status`/`priority` strings instead of real enums. Sharing one KMP module means both sides
compile against the exact same type, and drift like that isn't possible anymore.

## What's here

`@Serializable` (kotlinx.serialization) data classes and enums, package
`com.momosi.trucktrack.shared.<domain>`:

- **`common`** — `PageDto`, `PageableDto`, `ErrorDto`
- **`issue`** — `IssueDto`, `IssueCreateDto`, `IssueFilterDto`, `IssueHistoryDto`, `AccountDto`,
  plus status/priority/history-event enums
- **`vehicle`** — `VehicleDto`, `VehicleTypeDto`

## Rules

Pure Kotlin Multiplatform + `kotlinx.serialization` only — no Ktor, no Koin, no Spring, no
Spring MVC/Jackson annotations, no Android/iOS-only APIs. It has to compile for a Spring Boot JVM
process and for a KMP client (Android + iOS) alike. See `AGENTS.md` for the full conventions
(naming, date/UUID type choices, how the server bridges those types to JSON).

## Consuming it

```kotlin
// build.gradle.kts, in any module that needs a DTO
dependencies {
    implementation("com.momosi.trucktrack:shared")
    // or api(...) if the type appears in your module's own public API
}
```

Never `project(":shared")` — `shared` is a separate Gradle build, wired in only via composite-build
dependency substitution (see the root `README.md`/`AGENTS.md` "Repository Layout" section).

## Docs

- **`AGENTS.md`** — package structure, naming/type conventions, consumers.
- **`../server/module-api/AGENTS.md`** — how the API contract layer imports these DTOs.
- **`../server/module-server/AGENTS.md`** — how the server maps these DTOs to/from domain models.
