---
name: add-repository
description: Use when adding a new repository, manager, or API data source to a core module. Triggered by phrases like "add a repository", "add a manager", "fetch from API", "expose data to features", "add data source", "new repository", "call a new endpoint".
---

# Skill: Add a Repository to a Core Module

> Adds a repository (or manager) to an existing `app/core/*` module: domain model, DTO mapper, Ktorfit API client, repository interface + implementation, and Koin binding. All files go in `src/commonMain/kotlin/`. DTOs themselves come from the separate `shared` build (`com.momosi.trucktrack:shared`), not from this module — see Step 3.

## Triggers

Load this skill when the task matches **any** of these:
- Adding a new data source or backend resource to an `app/core/*` module
- Task says "add a repository", "add a manager", "fetch X from the API", "expose X to feature modules"
- A new API endpoint needs to be called from the app

## Prerequisites

- The target `app/core/<module>` already exists with a `build.gradle.kts` that applies `alias(libs.plugins.trucktrack.ktor)` (KSP + Ktorfit + Ktor) — add it if missing
- `core:network` is available and provides a Koin-managed `Ktorfit` instance backed by the authenticated `HttpClient` (base URL and auth pre-configured), plus `PageDtoMapper.toPage` for paginated responses
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

### 5. Create the Ktorfit API client in `api/`

Every backend API client is a Ktorfit interface (see `app/AGENTS.md` → "Networking") — never a
hand-written class calling `HttpClient` directly. It returns `shared`'s DTO types — no local response
wrapper.

```kotlin
package com.momosi.trucktrack.core.<module>.api

import com.momosi.trucktrack.shared.<domain>.<Thing>Dto
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path

interface <Thing>Api {

    @GET("api/v1/<thing>")
    suspend fun get<Thing>List(): List<<Thing>Dto>

    @GET("api/v1/<thing>/{id}")
    suspend fun get<Thing>(@Path("id") id: Long): <Thing>Dto
}
```

For a paginated endpoint, return `shared`'s `PageDto<T>` and convert with `core:network`'s `PageDtoMapper`:

```kotlin
@GET("api/v1/<thing>")
suspend fun get<Thing>List(
    @Query("page") page: Int? = null,
    @Query("size") size: Int? = null,
): PageDto<<Thing>Dto>
```
```kotlin
// in the repository impl
api.get<Thing>List(page, size).toPage { it.to<Thing>() }
```

For a JSON body (the shared client already sets the JSON content type):
```kotlin
@POST("api/v1/<thing>")
suspend fun create<Thing>(@Body body: <Thing>CreateDto): <Thing>Dto
```

Non-2xx responses surface as `ApiException` (`core:common/network`) through the shared client's
`installApiExceptionMapping` — map specific status codes (e.g. `ApiException.HttpError` with 409) in
the repository, not in the API interface.

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
`get<Ktorfit>()` is resolved from `networkModule` in `core:network`; `create<Thing>Api()` is the
extension KSP generates for the `<Thing>Api` interface.

```kotlin
package com.momosi.trucktrack.core.<module>.di

import com.momosi.trucktrack.core.<module>.<Thing>Repository
import com.momosi.trucktrack.core.<module>.<Thing>RepositoryImpl
import com.momosi.trucktrack.core.<module>.api.create<Thing>Api
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.dsl.module

val <module>Module = module {
    single { get<Ktorfit>().create<Thing>Api() }
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
- [ ] `<Thing>Api` is a Ktorfit interface (no `HttpClient` calls) and the module applies `trucktrack.ktor`
- [ ] Koin module has `single { get<Ktorfit>().create<Thing>Api() }` and `single<<Thing>Repository> { ... }`
- [ ] `cd app && ./gradlew :core:<module>:assembleDebug` passes (or `:app:core:<module>:assembleDebug` from the repo root)
- [ ] `cd app && ./gradlew spotlessCheck` passes
