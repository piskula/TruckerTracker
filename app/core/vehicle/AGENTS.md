# core:vehicle

Domain models and repository for the vehicle domain. Fully KMP — all code in `commonMain`.

## Public API

| Symbol | Description |
|--------|-------------|
| `VehicleRepository` | `suspend fun getVehicles(): Result<CategorizedVehicles>`; `fun recordVehicleUsage(vehicle: Vehicle)` — both resolve the current user internally via `UserRepository`, callers never pass a userId |
| `Vehicle` | Domain model: id, licensePlate, make, model, type |
| `VehicleType` | `Truck`, `Trailer` |
| `CategorizedVehicles` | `getVehicles()`'s result: `preferredTruck`, `preferredTrailer`, `otherVehicles`, `defaultVehicle` (the one to preselect, based on which type was used most recently) — the driver's remembered truck/trailer folded directly into the vehicle list, local-only (no backend API) |

## Key Files

```
commonMain/
  VehicleRepository.kt
  VehicleRepositoryImpl.kt                ← also owns categorize(): splits the fetched vehicle list against local preference storage
  model/Vehicle.kt
  model/VehicleType.kt
  model/CategorizedVehicles.kt
  dto/VehicleDtoMapper.kt              ← maps shared's VehicleDto -> Vehicle; no local DTO class
  api/VehicleApi.kt                    ← Ktor API client, returns com.momosi.trucktrack.shared.vehicle.VehicleDto
  internal/PreferredVehicleStorage.kt  ← interface, key-value storage of the remembered truck/trailer, scoped per userId
  di/VehicleModule.kt                  ← Koin bindings (common part) + expect platformVehicleModule()
androidMain/
  internal/PreferredVehicleStorageImpl.kt ← SharedPreferences-backed
  di/VehicleModule.android.kt
iosMain/
  internal/PreferredVehicleStorageImpl.kt ← NSUserDefaults-backed
  di/VehicleModule.ios.kt
```

The remembered truck/trailer is intentionally local-only storage — there is no backend endpoint for it and none is planned; it exists purely to preselect and surface a driver's usual truck/trailer in `feature:issues:impl`'s vehicle picker. `getVehicles()` returns it pre-categorized rather than as a flat list plus separate ids, so consumers never filter the vehicle list themselves.

Both `getVehicles()`'s categorization and `recordVehicleUsage()` are gated to `UserRole.Driver` (via `VehicleRepositoryImpl.currentDriverUserId()`) — a mechanic can also create/edit issues (see `feature:issues:impl`'s `IssuesScreen`), but has no "usual truck" concept, so for them `getVehicles()` always returns everything in `otherVehicles` with `preferredTruck`/`preferredTrailer`/`defaultVehicle` all `null`, and `recordVehicleUsage()` is a no-op.

## Depends On

- `:core:common` — `Logger`, `DispatcherProvider`
- `:core:network` — Ktor `HttpClient`
- `:core:user` — `UserRepository`, to resolve the current user for scoping the remembered truck/trailer
- `com.momosi.trucktrack:shared` — `VehicleDto`, `VehicleTypeDto` (separate build, see `../../../shared/AGENTS.md`)
