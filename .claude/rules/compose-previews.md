---
paths:
  - "app/**/*.kt"
---

# Compose Previews

Keep `@Preview` functions in the same Kotlin file as the composable they preview.
Shared composables and their previews live together in `commonMain`; `trucktrack.compose`
already provides the multiplatform preview annotation there. Platform-specific composables
keep their previews beside them in the matching platform source set.

* Every file with `@Composable` functions has `@Preview` functions for its key composables.
* Wrap the previewed content in `TruckTrackTheme { }`.
* Preview functions are `private` and named with a `Preview` suffix — e.g. `IssueCardDriverPreview`.
* Populate sample data with values a real fleet would produce — a plausible licence plate, a real
  sentence for a description. Don't preview blank or `null` fields unless that empty variant is the
  thing being previewed.
* Never preview a top-level screen composable that requires a `ViewModel`. Preview the inner
  stateless content composable instead.
