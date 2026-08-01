---
applyTo: "shared/**"
---

You are editing the DTO contract module consumed by both `app/` and `server/`. Read
`shared/AGENTS.md` before making changes.

Two constraints that catch people out: this module has zero framework dependencies (no Ktor, Koin,
Spring, Jackson, or Swagger annotations — only `kotlinx.serialization`), and it must compile for
JVM, Android, and iOS, so `java.time.*` and `java.util.UUID` are unavailable. Any change here
affects both consumers.
