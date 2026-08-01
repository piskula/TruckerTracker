---
name: fix-spotless-issues
description: Use when running Spotless (ktlint + compose-rules-ktlint) or fixing spotlessCheck/spotlessApply failures. Triggered by phrases like "run spotless", "fix spotless", "spotlessCheck failing", "ktlint violation", "format the code", "fix formatting", "spotlessApply".
---

# Skill: Run Spotless and Fix Violations

> Formats and lints all Kotlin/KTS files via the `trucktrack.spotless` convention plugin (`app/build-logic/convention/src/main/kotlin/com/momosi/trucktrack/SpotlessPlugin.kt`). Applies standard ktlint plus the `compose-rules-ktlint` custom rule set (`io.nlopez.compose.rules:ktlint:0.6.2`) to every `**/*.kt` file in `app/` — Spotless is only configured for the client build, not `server/`.

## Triggers

Load this skill when the task matches **any** of these:
- "run spotless", "format the code", "fix formatting"
- `spotlessCheck` or CI/build fails with a ktlint diff
- A ktlint or compose-rules violation is reported and needs a manual fix

## Commands

Run from inside `app/` (its own Gradle build); prefix tasks with `:app:` instead if running from the repo root (e.g. `:app:core:issue:spotlessApply`).

```bash
cd app
./gradlew spotlessApply    # auto-format all Kotlin/KTS files — run this first, always
./gradlew spotlessCheck    # fails with a diff if any file still needs formatting
```

Scope to one module when iterating quickly:
```bash
./gradlew :core:issue:spotlessApply
./gradlew :feature:issues:impl:spotlessCheck
```

If either task fails with a configuration cache error, retry with `--no-configuration-cache`.

**Always run `spotlessApply` before `spotlessCheck`.** Most violations (import order, wildcard imports, indentation, spacing, function-signature multiline) are mechanical and auto-fixed. Only what `spotlessApply` can't rewrite — mostly semantic `compose-rules-ktlint` violations — needs a manual edit. CI does not run `spotlessCheck`, but unformatted code routinely breaks `assembleDebug`, so always apply before committing.

## Reading the failure output

`spotlessCheck` / ktlint errors look like:
```
path/to/File.kt:12:1: Wildcard import (cannot be auto-corrected) (standard:no-wildcard-imports)
path/to/File.kt:34:5: Composable functions... (compose:compose-modifier-missing-check)
```
The `(rule-id)` at the end tells you which rule fired — use it to look up the fix below rather than guessing from the message alone.

## Project-specific rule config

From `SpotlessPlugin.kt`'s `editorConfigOverride` — these are **not** stock ktlint defaults:

| Override | Effect |
|---|---|
| `ktlint_function_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than = 3` | Any function/constructor with ≥ 3 params must be written one-param-per-line |
| `ktlint_compose_compositionlocal-allowlist = disabled` | `compose-rules-ktlint`'s CompositionLocal allowlist check is off — no violation to fix |
| `ktlint_compose_lambda-param-in-effect = disabled` | Lambda-params-in-`LaunchedEffect`/`remember` check is off — no violation to fix |
| `ktlint_function_naming_ignore_when_annotated_with = Composable` | `@Composable` functions are exempt from `camelCase`-function-name rule — `PascalCase` is correct there, not a violation |

## Common violations & fixes

| Rule | Symptom | Fix |
|---|---|---|
| `standard:no-wildcard-imports` | `import androidx.compose.foundation.layout.*` | Import each symbol used, individually |
| `standard:import-ordering` | Imports out of order/grouped wrong | Let `spotlessApply` reorder — do not hand-sort |
| `standard:function-signature` | 3+ param function/constructor on one line | Break into one parameter per line with a trailing comma; `spotlessApply` usually does this automatically |
| `compose:modifier-missing-check` | Root-level `@Composable` emitting UI has no `modifier` param | Add `modifier: Modifier = Modifier` and apply it to the outermost element |
| `compose:modifier-reused-check` | Same `Modifier` instance applied to more than one element | Apply the incoming `modifier` only to the outermost element; give inner elements their own `Modifier` |
| `compose:multiple-emitters-check` | Composable emits more than one top-level UI element | Wrap in a single root container (`Column`, `Box`, etc.) |
| `compose:param-order-check` | Params ordered wrong | Required params first, then `modifier`, then other optional params, trailing-lambda `content`/`onX` last |
| `compose:unstable-collections-check` | `List<T>` / `Map<K,V>` in a stable Composable param or `@Immutable` state | Use `ImmutableList<T>` / `persistentListOf()` from `kotlinx.collections.immutable` (matches `AGENTS.md` Compose Stability rules) |
| `compose:preview-naming-check` | `@Preview` function named with past tense (`OnClickedPreview`) | Rename to present tense, `Preview` suffix, e.g. `IssueCardDriverPreview` |
| `standard:trailing-comma-on-declaration-site` / `-on-call-site` | Missing trailing comma in multiline call/decl | `spotlessApply` adds it — no manual fix needed |

If a rule isn't in this table, treat the ktlint message as authoritative and search `io.nlopez.compose.rules:ktlint` (v0.6.2) docs by rule id for anything Compose-specific (`compose:*`); everything else is stock ktlint.

## Workflow

1. `./gradlew spotlessApply` (whole project, or scope to touched modules for speed).
2. `./gradlew spotlessCheck` (or just `assembleDebug`) to confirm the diff is gone.
3. For anything still failing, read the `(rule-id)` from the error, match it in the table above, and hand-fix — these are semantic (Compose structure, stability) and can't be auto-formatted.
4. Repeat step 1 after manual fixes, since hand edits can reintroduce formatting issues.

## Verification

- [ ] `./gradlew spotlessApply` ran with no errors
- [ ] `./gradlew spotlessCheck` passes clean (no diff)
- [ ] No manual reformatting was done by hand where `spotlessApply` could have done it
- [ ] Any remaining `compose:*` violations were fixed per the table above, not suppressed
