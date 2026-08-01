# TruckTrack — Server

Spring Boot backend for TruckTrack, a fleet-management issue-tracking system for drivers and mechanics. This is `server/` — its own Gradle build (plain `kotlin.jvm`), part of the `TruckTrack` composite build. See the repo root `README.md` for how this fits alongside `app/` and `shared/`.

## Contents

- [Tech stack](#tech-stack)
- [Getting started](#getting-started)
- [Continuous integration](#continuous-integration)
- [Project structure](#project-structure)
- [Configuration](#configuration)
- [Runtime](#runtime)
- [Docs](#docs)

## Tech stack

| Concern | Choice |
|---------|--------|
| Framework | Spring Boot 4 (Kotlin, Java 25) |
| API docs | SpringDoc / Swagger UI, OAuth2 PKCE |
| Auth | Keycloak (OAuth2 resource server, JWT validation) |
| Persistence | PostgreSQL + Spring Data JPA, Liquibase migrations |
| File storage | MinIO (S3-compatible) |
| Contract DTOs | `com.momosi.trucktrack:shared` — see `../shared/README.md` |

## Getting started

### Prerequisites

| Tool | Needed for | Check |
|---|---|---|
| JDK 25 | Gradle toolchain | `java -version` |
| Docker + Docker Compose | Local PostgreSQL + MinIO | `docker --version` |

### Run locally

```bash
# Start local PostgreSQL + MinIO
docker-compose up -d

# Full build
./gradlew clean build

# Run
java -jar module-server/build/libs/module-server-*-SNAPSHOT.jar
```

The app connects to a running Keycloak instance for auth (`https://sso.momosi.org` by default —
see [Configuration](#configuration) to point at a different one) and to the Postgres/MinIO
containers started above.

## Continuous integration

`build-server.yml` (repo root `.github/workflows/`) runs on every push to `main` that touches
`server/`, `shared/`, or shared root config (skipped for `app/**`-only, doc-only, or
`.claude/**`-only changes) — also triggerable manually via `workflow_dispatch`.

- **`build`** — `./gradlew clean build` from inside `server/`, uploads
  `module-server/build/libs/module-server-*.jar` as a workflow artifact.
- **`dependency-submission`** — submits a Gradle dependency graph for `server/` so Dependabot can
  alert on vulnerable dependencies.

There's no release/deploy workflow for the server yet — see [Runtime](#runtime) for how it's
currently deployed.

## Project structure

Two modules:

- **`module-api`** — Spring MVC contract interfaces only (`@Tag`, `@Operation`, `@GetMapping`,
  etc.), importing DTOs from `com.momosi.trucktrack:shared`. No DTOs of its own. See
  `module-api/AGENTS.md`.
- **`module-server`** — the implementation: controllers, JPA entities/repositories, use cases.
  Implements `module-api`'s interfaces, mapping domain models to/from `shared`'s DTOs. See
  `module-server/AGENTS.md`.

```
Controller → UseCase → Persistence (or shared Service)
```

DTOs and enums (`IssueDto`, `VehicleDto`, `PageDto`, etc.) live in the separate `shared` build
(`../shared/`), consumed by both this backend and the KMP client — not duplicated here.

## Configuration

**OAuth2/Keycloak** — Issuer URI `https://sso.momosi.org/realms/trucktrack`, realm `trucktrack`,
client ID `trucktrack-app`. The backend is a resource server — it validates JWT tokens. Roles come
from the `realm_access.roles` claim, mapped to `ROLE_DRIVER` / `ROLE_MECHANIC`. See
`devOps/README.md` for setting up a Keycloak realm from scratch.

**Database** — PostgreSQL with Liquibase migrations, `ddl-auto: validate`.

**Swagger UI** — `/swagger-ui.html` (unauthenticated), OAuth2 PKCE configured in
`config/SpringDocConfig.kt` + `application.yml`.

**Local environment variables** (all have working defaults for the `docker-compose.yml` setup
above):

| Variable | Default |
|---|---|
| `POSTGRES_DB_URL` | `jdbc:postgresql://localhost:5435/truckTrack?stringtype=unspecified` |
| `POSTGRES_DB_PASSWORD` | `truckTrack-password` |
| `MINIO_URL` | `http://localhost:9000` |
| `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` | `minioadmin` |
| `KEYCLOAK_URL` | `https://sso.momosi.org` |
| `KEYCLOAK_REALM` | `trucktrack` |

## Runtime

Production deployment is Docker on an Ubuntu VM: Keycloak, PostgreSQL, and NGINX all run as
containers; the Spring Boot app itself runs as a Linux service via `java -jar`. One shared Postgres
instance holds two schemas — one for Keycloak, one for the app.

## Docs

- **`AGENTS.md`** (this directory), **`module-api/AGENTS.md`**, **`module-server/AGENTS.md`** —
  layering rules, entity/persistence conventions, controller conventions, and other
  agent-facing coding conventions.
- **`devOps/README.md`** — step-by-step Keycloak realm/client/role setup.
- **`../shared/README.md`** — the DTO contract module this backend depends on.
