# TruckTrack Server — Agent Instructions

Spring Boot backend for the TruckTrack issue-tracking system. Own Gradle build (`server/`, plain `kotlin.jvm`), composite-built alongside `app/` and `shared/` — see the root `AGENTS.MD` for how the three fit together.

## Build Commands

```bash
# Full build
./gradlew clean build

# Run locally (after build)
java -jar module-server/build/libs/module-server-*-SNAPSHOT.jar

# Start local PostgreSQL
docker-compose up -d
```

## Module Architecture

**`module-api`** — API contract layer. See `module-api/AGENTS.MD` for details.

**`module-server`** — Spring Boot 4 backend (Kotlin, Java 25). See `module-server/AGENTS.MD` for details.

**`shared`** (separate build, `com.momosi.trucktrack:shared`) — DTOs and enums used across the API contract, both here and on the KMP client. See `../shared/AGENTS.MD`.

## Key Configuration

**OAuth2/Keycloak:** Issuer URI is `https://sso.momosi.org/realms/trucktrack`. Realm name is `trucktrack`, client ID is `trucktrack-app`. The backend is a resource server — it validates JWT tokens. Roles are in `realm_access.roles` claim and mapped as `ROLE_DRIVER` / `ROLE_MECHANIC`.

**Database:** PostgreSQL with Liquibase migrations. JPA is set to `ddl-auto: validate`. Liquibase requires `spring-boot-starter-liquibase` (not just `liquibase-core`) in Spring Boot 4.x — the autoconfiguration bean is only wired via the starter.

**Swagger UI OAuth2 PKCE:** Configured in `config/SpringDocConfig.kt` (`@SecurityScheme`) + `application.yml` `springdoc:` block. Swagger UI at `/swagger-ui.html` is permitted without auth. The Keycloak client must have `http://localhost:8080/swagger-ui/oauth2-redirect.html` as a valid redirect URI.

**Local environment variables needed:**
- `POSTGRES_DB_URL` — defaults to `jdbc:postgresql://localhost:5435/truckTrack?stringtype=unspecified`
- `POSTGRES_DB_PASSWORD` — defaults to `truckTrack-password`
- `MINIO_URL` — defaults to `http://localhost:9000`
- `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` — default to `minioadmin`
- `KEYCLOAK_URL` — defaults to `https://sso.momosi.org`
- `KEYCLOAK_REALM` — defaults to `trucktrack`

## Runtime
Currently, application, when deployed on PROD, uses Docker on Ubuntu VM. In Docker there is keycloak, postgres and NGINX. Application itself is running as a service in Linux with `java -jar` command. There is 1 shared postgres with 2 schemas, 1 for keycloak and 1 for Spring Boot app.
