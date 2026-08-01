---
name: add-repository
description: Use when adding a new repository, manager, or API data source to a core module. Triggered by phrases like "add a repository", "add a manager", "fetch from API", "expose data to features", "add data source", "new repository", "call a new endpoint".
---

# Skill: Add a Repository to a Core Module

> Adds a repository (or manager) to an existing `app/core/*` module: domain model, DTO mapper, Ktor API client, repository interface + implementation, and Koin binding. All files go in `src/commonMain/kotlin/`. DTOs themselves come from the separate `shared` build (`com.momosi.trucktrack:shared`), not from this module — see Step 3.

## Triggers

Load this skill when the task matches **any** of these:
- Adding a new data source or backend resource to an `app/core/*` module
- Task says "add a repository", "add a manager", "fetch X from the API", "expose X to feature modules"
- A new API endpoint needs to be called from the app

## Prerequisites

- The target `app/core/<module>` already exists with a `build.gradle.kts`
- `core:network` is available and provides a Koin-managed `HttpClient` with base URL and auth pre-configured, plus `PageDtoMapper.toPage` for paginated responses
- The API endpoint is known (see OpenAPI at `https://tt.momosi.org/v3/api-docs`, or `server/module-api` directly)

## Steps

### 1. Create the domain model in `model/`

Place in `app/core/<module>/src/commonMain/kotlin/com/momosi/trucktrack/core/<module>/model/<Thing>.kt`.
Pure Kotlin only — no Android imports, no `Context`, no `@StringRes`.

```kotlin
package com.momosi.trucktrack.core.<module>.model

data class <Thing>(
    val id: Long,
    val name: String,
)
```

### 2. Create the repository interface

```kotlin
package com.momosi.trucktrack.core.<module>

import com.momosi.trucktrack.core.<module>.model.<Thing>
import com.momosi.trucktrack.core.common.model.Page

interface <Thing>Repository {

    suspend fun get<Things>(): Result<Page<<Thing>>>

    suspend fun get<Thing>(id: Long): Result<<Thing>>
}
```

Rules: all methods `suspend`; never throw — return `Result<T>`, `T?`, or `List<T>`; no Android types.

### 3. Use (or add) the DTO in `shared`

DTOs live in the separate `shared` build (`shared/src/commonMain/kotlin/com/momosi/trucktrack/shared/<domain>/`), not in this module — check there first for an existing `<Thing>Dto`. Only add a new one if it genuinely doesn't exist yet:

```kotlin
// shared/src/commonMain/kotlin/com/momosi/trucktrack/shared/<domain>/<Thing>Dto.kt
package com.momosi.trucktrack.shared.<domain>

import kotlinx.serialization.Serializable

@Serializable
data class <Thing>Dto(
    val id: Long,
    val name: String,
)
```

See `shared/AGENTS.md` for naming/type conventions (`kotlin.time.Instant` for dates, `kotlin.uuid.Uuid` for UUIDs, no framework annotations). Adding a new DTO here also needs a matching type on the `server/module-api`/`module-server` side — see `server/module-api/AGENTS.md`.

### 4. Create the DTO mapper in `dto/`

No local DTO class here — the mapper converts `shared`'s DTO straight to the domain model:

```kotlin
package com.momosi.trucktrack.core.<module>.dto

import com.momosi.trucktrack.core.<module>.model.<Thing>
import com.momosi.trucktrack.shared.<domain>.<Thing>Dto

fun <Thing>Dto.to<Thing>(): <Thing> = <Thing>(
    id = id,
    name = name,
)
```

### 5. Create the Ktor API client in `api/`

Returns `shared`'s DTO types directly — no local response wrapper.

```kotlin
package com.momosi.trucktrack.core.<module>.api

import com.momosi.trucktrack.shared.<domain>.<Thing>Dto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class <Thing>Api(private val client: HttpClient) {

    suspend fun get<Things>(): List<<Thing>Dto> = client.get("api/v1/<things>").body()

    suspend fun get<Thing>(id: Long): <Thing>Dto = client.get("api/v1/<things>/$id").body()
}
```

For a paginated endpoint, wrap the response in `shared`'s `PageDto<T>` and convert with `core:network`'s `PageDtoMapper`:

```kotlin
suspend fun get<Things>(page: Int?, size: Int?): PageDto<<Thing>Dto> =
    client.get("api/v1/<things>") {
        page?.let { parameter("page", it) }
        size?.let { parameter("size", it) }
    }.body()
```
```kotlin
// in the repository impl
api.get<Things>(page, size).toPage { it.to<Thing>() }
```

For POST with a JSON body:
```kotlin
suspend fun create<Thing>(body: <Thing>CreateDto): <Thing>Dto =
    client.post("api/v1/<things>") {
        contentType(ContentType.Application.Json)
        setBody(body)
    }.body()
```

### 6. Create the repository implementation

```kotlin
package com.momosi.trucktrack.core.<module>

import com.momosi.trucktrack.core.<module>.api.<Thing>Api
import com.momosi.trucktrack.core.<module>.dto.to<Thing>
import com.momosi.trucktrack.core.<module>.model.<Thing>

class <Thing>RepositoryImpl(
    private val api: <Thing>Api,
) : <Thing>Repository {

    override suspend fun get<Things>(): Result<List<<Thing>>> = runCatching {
        api.get<Things>().map { it.to<Thing>() }
    }

    override suspend fun get<Thing>(id: Long): Result<<Thing>> = runCatching {
        api.get<Thing>(id).to<Thing>()
    }
}
```

### 7. Add Koin bindings in `di/`

Add to the module's existing `di/<Module>Module.kt`, or create it if it doesn't exist.
`get()` for `HttpClient` is resolved from `networkModule` in `core:network`.

```kotlin
package com.momosi.trucktrack.core.<module>.di

import com.momosi.trucktrack.core.<module>.<Thing>Repository
import com.momosi.trucktrack.core.<module>.<Thing>RepositoryImpl
import com.momosi.trucktrack.core.<module>.api.<Thing>Api
import org.koin.dsl.module

val <module>Module = module {
    single { <Thing>Api(get()) }
    single<<Thing>Repository> { <Thing>RepositoryImpl(get()) }
}
```

### 8. Register in `AppModule` (new Koin module files only)

If this is a new `val xxxModule`, add it to `app/app/shared/src/commonMain/kotlin/com/momosi/trucktrack/app/di/AppModule.kt`.
If adding to an existing module file, no app-level change is needed.

### 9. Update `app/core/<module>/AGENTS.md`

Add the new repository interface and domain model to the Public API table.
Add the new files to the Key Files section. If you added a new DTO in `shared`, add it to `shared/AGENTS.md`'s domain list too.

## Verification

- [ ] Domain model in `model/` contains no Android imports — placed in `src/commonMain/kotlin/`
- [ ] No local `<Thing>Dto` class in `app/core/<module>` — it's in `shared/src/commonMain/kotlin/com/momosi/trucktrack/shared/<domain>/`, reused if it already existed
- [ ] Repository interface returns only `Result<T>`, `T?`, or `List<T>` — never throws
- [ ] DTO (in `shared`) uses `@Serializable`
- [ ] Koin module has `single { <Thing>Api(get()) }` and `single<<Thing>Repository> { ... }`
- [ ] `cd app && ./gradlew :core:<module>:assembleDebug` passes (or `:app:core:<module>:assembleDebug` from the repo root)
- [ ] `cd app && ./gradlew spotlessCheck` passes
