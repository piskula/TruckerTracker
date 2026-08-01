# core:vehicle

Domain models and repository for the vehicle domain. Fully KMP — all code in `commonMain`.

## Public API

| Symbol | Description |
|--------|-------------|
| `VehicleRepository` | `suspend fun getVehicles(): Result<List<Vehicle>>` |
| `Vehicle` | Domain model: id, licensePlate, make, model, type |
| `VehicleType` | `Truck`, `Trailer` |

## Key Files

```
commonMain/
  VehicleRepository.kt
  VehicleRepositoryImpl.kt
  model/Vehicle.kt
  model/VehicleType.kt
  dto/VehicleDtoMapper.kt    ← maps shared's VehicleDto -> Vehicle; no local DTO class
  api/VehicleApi.kt          ← Ktor API client, returns com.momosi.trucktrack.shared.vehicle.VehicleDto
  di/VehicleModule.kt        ← Koin bindings
```

## Depends On

- `:core:common` — `Logger`, `DispatcherProvider`
- `:core:network` — Ktor `HttpClient`
- `com.momosi.trucktrack:shared` — `VehicleDto`, `VehicleTypeDto` (separate build, see `../../../shared/AGENTS.md`)
