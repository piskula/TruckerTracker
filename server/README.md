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
| File storage | AWS SDK v2 (`S3Client`), S3-compatible — MinIO locally, Hetzner Object Storage in production |
| Contract DTOs | `com.momosi.trucktrack:shared` — see `../shared/README.md` |

## Getting started

### Prerequisites

| Tool | Needed for | Check |
|---|---|---|
| JDK 25 | Gradle toolchain | `java -version` |
| Docker + Docker Compose | Local PostgreSQL + MinIO (S3-compatible, used for local file storage) | `docker --version` |

### Run locally

```bash
# Start local PostgreSQL + MinIO
docker-compose up -d

# Full build
./gradlew clean build

# Run
java -jar module-server/build/libs/module-server-dev.jar
```

The app connects to a running Keycloak instance for auth (`https://sso.momosi.org` by default —
see [Configuration](#configuration) to point at a different one) and to the Postgres/MinIO
containers started above.

## Continuous integration

`build-server.yml` runs Detekt and `./gradlew clean build` for relevant pull requests,
pushes to `main`, manual runs, and `server-X.Y.Z` tags. Both checks must pass before publishing
or deploying.

| Trigger | Deployment | Version |
|---|---|---|
| Relevant push to `main` or manual run on `main` | Automatic staging | `dev.<run-number>.<short-commit>` |
| Push `server-X.Y.Z` | Publish GitHub Release, then deploy production | `X.Y.Z` |
| Pull request or manual run on another branch | None | Development build |

To release, tag a commit already on `main` and push only that tag:

```bash
git fetch origin main --tags
git tag server-1.2.3 origin/main
git push origin server-1.2.3
```

Versions must increase within the server tag family, without leading zeros or prerelease
suffixes. The release contains `trucktrack-server-X.Y.Z.jar`, its SHA256 checksum, server/shared
changes since the previous server tag, and the source commit. CI verifies `build.version` and
`build.commit` inside the executable JAR; `/api/v1/version` reports the release version.
Local builds default to `dev`; `-PserverVersion` and `-PserverCommit` override build metadata.

Production deploys the same checked artifact, verifies its transfer checksum, invokes the VM
deployment script, and checks service health and the running version. Deployments are serialized
and not cancelled by newer runs. A release can be published even if its subsequent deployment
fails; inspect the linked Actions run. Pushing `main`, mobile tags, or manually running the
workflow never deploys production.

### Deployment environments and secrets

Both deployment environments belong to **`piskula/TruckerTracker`**, not
`piskula/truckTrack-devOps`. In Settings → Environments, configure:

| Environment | Deployment branches/tags | Trigger | Required reviewers |
|---|---|---|---|
| `staging` | Selected branch `main` only | Matching pushes and manual runs on `main` | None for unattended deployment |
| `production` | Selected **tag** pattern `server-*` only | Server release tag | Optional approval gate |

Add these under each environment's **Environment secrets**:

| Environment | Secret | Expected value |
|---|---|---|
| `staging` | `DEPLOY_HOST` | Staging SSH hostname or IP |
| `staging` | `DEPLOY_USER` | Existing staging SSH deployment account |
| `staging` | `DEPLOY_SSH_KEY` | Private key for that account, usable non-interactively |
| `staging` | `DEPLOY_HOST_KEY` | SHA256 SSH host-key fingerprint, including the `SHA256:` prefix |
| `production` | `DEPLOY_HOST` | Production SSH hostname or IP |
| `production` | `DEPLOY_USER` | Production SSH deployment account (`deploy` for the documented VM setup) |
| `production` | `DEPLOY_SSH_KEY` | Dedicated private key for that production account, usable non-interactively |
| `production` | `DEPLOY_SSH_KNOWN_HOSTS` | Complete OpenSSH `known_hosts` entry matching `DEPLOY_HOST` |

`DEPLOY_HOST`, `DEPLOY_USER`, and `DEPLOY_SSH_KEY` use the same names in both environments,
with separate values selected by the deployment job's environment.

`DEPLOY_HOST_KEY` is a fingerprint used by both staging SSH actions, not a public-key line
or a `known_hosts` entry. `DEPLOY_SSH_KNOWN_HOSTS` is a full `known_hosts` entry, not a
fingerprint. Verify both through a trusted channel. Never reuse staging credentials for
production, commit secret values, or print them in workflow logs.

No GitHub **Environment variables** (`vars.*`) are required. Backend runtime variables
(database, authentication, object storage, Firebase, and production `SERVER_PORT=8080`) remain
on their respective VMs; they are not deployment secrets and are not uploaded by CI.

### Staging secret migration and VM expectations

The repository owner must create both environments with the restrictions above; referencing
an environment in YAML does not configure its secrets or protection rules. Re-enter staging's
four `DEPLOY_*` values from their secure source. GitHub cannot reveal existing secret values.
After a successful staging deployment, remove their repository-level copies; otherwise GitHub
can fall back to those when environment secrets are missing. Leave mobile secrets unchanged.

The staging `DEPLOY_USER` must be able to write `~/module-server-tt`, replace the JAR, and execute
`systemctl stop trucker-service.service` and `systemctl start trucker-service.service`
non-interactively. Staging permissions and runtime configuration are unchanged.

### Production VM setup

Before the first production run:

1. Configure the `production` environment and its four secrets as described above.
2. On the production VM, authorize the corresponding public key for `deploy`. Verify that
   `/opt/trucktrack/scripts/deploy-app.sh` is executable, `/opt/trucktrack/app` is writable by
   `deploy`, and `curl`, `jq`, `sha256sum`, and Java 25 are installed. Install the systemd unit,
   runtime configuration, and script according to the
   [production runbook](https://github.com/piskula/truckTrack-devOps/blob/main/docs/07-app-systemd.md).
   Allow passwordless `/bin/systemctl restart trucktrack` for `deploy` as described there.
3. Ensure the running application exposes `http://127.0.0.1:8080/actuator/health`. The backend
   already includes and permits this endpoint; the older runbook's note about missing
   Actuator is obsolete. The workflow adds the health check after the existing VM script,
   so updating that script is not required for CI deployment.

Restarting production briefly interrupts service. SSH output and health responses are kept
out of public Actions logs; investigate failures on the VM. Previous JARs remain under
`/opt/trucktrack/app/releases`. Rollback is manual and requires checking Liquibase migration
compatibility first; a failed deployment does not undo migrations.

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
| `S3_ENDPOINT` | `http://localhost:9000` |
| `S3_REGION` | `us-east-1` |
| `S3_ACCESS_KEY` / `S3_SECRET_KEY` | `minioadmin` |
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
