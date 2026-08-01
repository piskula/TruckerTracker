# TruckTrack — Agent Instructions

## Project Overview

TruckTrack is a monorepo for a fleet-management product: a **Kotlin Multiplatform** client app and a **Spring Boot** backend, sharing a contract module of DTOs.

* **Language** — Kotlin 2.x for all development, client and server.
* **Libraries** — Use entries from `gradle/libs.versions.toml`, shared by all three builds.

This file covers the monorepo layout and repo-wide policy only. Each part has its own detailed `AGENTS.md` with module structure and coding conventions — read that before working inside it:

* **`app/AGENTS.md`** — Kotlin Multiplatform client: module map, MVVM architecture, Compose/Koin/Navigation conventions, KMP rules, testing, string resources.
* **`server/AGENTS.md`** — Spring Boot backend: build commands, module architecture, configuration. Plus `server/module-api/AGENTS.md` and `server/module-server/AGENTS.md` for layering/persistence conventions.
* **`shared/AGENTS.md`** — the DTO contract module: package structure, naming/type conventions, consumers.

### Repository Layout

Three independent Gradle builds, orchestrated from the repo root as a **composite build**:

```
/
  settings.gradle.kts   ← thin orchestrator: includeBuild("app"), includeBuild("server"), includeBuild("shared")
  gradle/libs.versions.toml   ← single version catalog, shared by all three builds via an explicit file reference
  app/       ← Kotlin Multiplatform client (own settings.gradle.kts, own build-logic/)
  server/    ← Spring Boot backend (own settings.gradle.kts, plain Kotlin/JVM)
  shared/    ← KMP contract module: DTOs used by both app/ and server/ (own settings.gradle.kts)
```

Each of `app/`, `server/`, `shared/` is a **fully self-contained Gradle build** (own `gradlew`, own `settings.gradle.kts`) — not a subproject of one giant build. This is deliberate, not incidental: mixing the client's `kotlin.multiplatform` plugin and the server's plain `kotlin.jvm` plugin in one shared build classpath (e.g. via a single `build-logic` included build serving both) triggers a reproducible Kotlin 2.4.0 / Gradle 9.6.0 crash (`NoClassDefFoundError: GradleBuildTimeMetric` in Kotlin's classpath-entry-snapshot transform) — reproduced in isolation outside this repo. Keeping the three builds structurally separate, connected only by Gradle's composite-build dependency substitution (matching `com.momosi.trucktrack:shared` coordinates to the real `:shared` project), avoids it entirely.

Practical implications:
- **Building everything**: `./gradlew :app:app:android:assembleDebug :server:module-server:bootJar` from the repo root drives both builds via composite task routing.
- **Working on just the client**: `cd app && ./gradlew ...` — a fully independent build, doesn't need `server/` or the root at all. Same for `cd server && ./gradlew ...`.
- **Opening in an IDE**: open the repo root for the full linked composite workspace, or open `app/` (or `server/`) alone for a faster, decoupled single-purpose workspace.
- **Adding a dependency on `shared`**: declare it as a regular external coordinate — `implementation("com.momosi.trucktrack:shared")` (or `api(...)` if the type appears in the consuming module's own public API) — never `project(":shared")`, since `shared` is a different Gradle build.
- **`shared`'s own convention**: pure Kotlin + `kotlinx.serialization` only. No Ktor, no Koin, no Spring, no Android/iOS-only APIs. It has no `build-logic` of its own — plugins are applied directly via `alias(libs.plugins.kotlin.multiplatform)`/`alias(libs.plugins.kotlin.serialization)` in its `build.gradle.kts`. Changes there affect both `app/` and `server/` — check both consumers before merging.

---

## Code Comments — Don't Write Them

**Never generate code comments.** This applies to every language and every part of the repo — no explanatory `//` comments, no block comments, no KDoc/Javadoc on classes, functions, properties or parameters, no "why" notes, no section banners, no TODO markers.

* Express intent through naming and structure instead. If a line seems to need a comment, extract it into a well-named function, constant or type.
* Do not add comments when modifying existing code, and do not "helpfully" document surrounding code you happen to touch.
* Leave existing comments alone unless the code they describe is being changed or removed — don't do comment-removal sweeps unasked, and don't preserve a comment that has become wrong.
* Narrow exceptions, only when they carry information the code genuinely cannot: license/copyright headers, tool directives that must be comments (`// ktlint-disable`, `// noinspection`, linter pragmas), and generated-file banners.
* This rule is about source code. Markdown docs, `AGENTS.md` files, and commit messages are prose and are still expected to explain things.

---

## Security — This Repository Is Public

`piskula/TruckerTracker` is a **public** GitHub repository. Commits, file contents, and GitHub Actions logs are visible to anyone on the internet, not just collaborators — treat every commit and every workflow run as world-readable before it happens, not after.

* **Never commit secret values** — passwords, private keys, certificates, API tokens, `.p12`/`.mobileprovision`/`.keystore` files, or any credential material. Only secret *names* belong in tracked files (e.g. `${{ secrets.FOO }}` in a workflow, or `` `FOO_SECRET` `` in docs) — never the value.
* **Never log secret values or content derived from them** in CI. This includes not just the literal secret string (GitHub auto-masks exact matches, but that's a narrow safety net) but also anything decoded/derived from it — a decoded certificate, a decoded provisioning profile's fields, a device UDID, a tester's name. Debug output should be limited to byte counts, file types, variable *lengths*, and exit codes — never `cat`/`head`/dump actual decoded content.
* **Don't hardcode environment-specific identifiers** (Team IDs, Firebase project/app IDs, specific tester or profile names) into committed skills, docs, or code comments, even if they aren't technically secret — this repo's git history is permanent and public. Prefer lookup commands (`gh secret list`, `firebase apps:list`, portal links) so agents/humans fetch current values instead of reading them off a public page.
* Before running any command that could touch git history (rebase, filter-branch, force-push) to redact something, remember the exposure already happened the moment it was pushed — rewriting history helps going forward but does not undo it. Flag any accidental exposure to the user immediately rather than quietly fixing it in a follow-up commit.
* If unsure whether something is sensitive, treat it as sensitive — ask before logging/committing it, don't decide unilaterally that "no one important will see it."

---

## Project Structure — `app/`, `server/`, and `shared/`

* **`app/`** — Kotlin Multiplatform client (Android + iOS), Compose Multiplatform UI. See `app/AGENTS.md` for the full module map and conventions.
* **`server/module-api`** — Spring MVC contract interfaces only (`@Tag`, `@Operation`, `@GetMapping`, etc.), importing DTOs from `com.momosi.trucktrack:shared`. No DTOs of its own. See `server/module-api/AGENTS.md`.
* **`server/module-server`** — Spring Boot 4 implementation: controllers, JPA entities/repositories, use cases. Implements `module-api`'s interfaces, mapping domain models to/from `shared`'s DTOs. Includes `config/JacksonConfig.kt`, which bridges `kotlin.time.Instant` and `kotlin.uuid.Uuid` (the types `shared`'s DTOs use) to/from JSON the same plain-string way `kotlinx.serialization` already represents them on the client — the wire format is unaffected. See `server/module-server/AGENTS.md`.
* **`shared`** — pure KMP module (`commonMain` + `jvm()`/`androidTarget()`/iOS targets), `kotlinx.serialization`-annotated DTOs and enums (`IssueDto`, `VehicleDto`, `PageDto`, `AccountDto`, status/priority/type enums, etc.), package `com.momosi.trucktrack.shared.*`. No framework dependencies (no Ktor, Koin, Spring, Spring MVC annotations) — it must stay usable from both a Spring Boot JVM process and a KMP client. See `shared/AGENTS.md`.
