---
paths:
  - "app/**/androidMain/**"
---

# Compose Previews

`@Preview` functions live only in `src/androidMain/kotlin/` — they are Android Studio tooling and
must not be added to `commonMain`.

* Every file with `@Composable` functions has `@Preview` functions for its key composables.
* Wrap the previewed content in `TruckTrackTheme { }`.
* Preview functions are `private` and named with a `Preview` suffix — e.g. `IssueCardDriverPreview`.
* Populate sample data with values a real fleet would produce — a plausible licence plate, a real
  sentence for a description. Don't preview blank or `null` fields unless that empty variant is the
  thing being previewed.
* Never preview a top-level screen composable that requires a `ViewModel`. Preview the inner
  stateless content composable instead.
