---
name: analyze-ci-failure
description: Use to check GitHub Actions build status or diagnose why a workflow run failed and propose a fix. Triggered by phrases like "check the latest build", "did CI pass", "why did the build fail", "check the GitHub Actions run", "analyze this workflow run", "check run <id>", "check the build for this PR".
---

# Skill: Analyze a GitHub Actions Run and Propose a Fix

> Uses the `gh` CLI (already authenticated) to inspect workflow runs on GitHub Actions, isolate the actual failing step from a noisy log, and turn it into a root cause + a proposed fix.

## Workflows in this repo

Both workflows live at the repo root (`.github/workflows/`), even though the jobs they run mostly build `app/`.

| File | Trigger | Jobs |
|---|---|---|
| `.github/workflows/build-app.yml` | push to `main` | `build-android`, `build-ios` → `publish-release` (`if: always()`, publishes whichever artifacts exist) → `distribute-android`, `distribute-ios` |
| `.github/workflows/release-app.yml` | push tag `v*.*.*` | `release-android`, `release-ios` |

## Step 1 — Find the run

| Given | Command |
|---|---|
| "latest build" / "latest run" | `gh run list --branch main --limit 5` |
| Latest run of a specific workflow | `gh run list --workflow build-app.yml --limit 5` |
| A run ID or run URL | `gh run view <run-id>` |
| A PR number | `gh pr checks <pr-number>` |
| Only failures | `gh run list --status failure --limit 10` |

## Step 2 — Check job-level status, not just the run-level conclusion

`gh run view <run-id>` prints a ✓/X per job. **Always read this before pulling logs** — a run can be marked `failure` even when the job that matters to you succeeded, and vice versa the job you care about can be masked by a downstream job's recovery logic.

Concretely in this repo: `publish-release` has `if: always()` and downloads each artifact with `continue-on-error: true`, so it only fails if *neither* `build-android` nor `build-ios` produced anything. If `build-ios` fails but `build-android` succeeds, `publish-release` still runs and publishes a partial release — but the overall run is still reported as `failure` because `build-ios` failed. **Identify the specific job(s) that actually failed**, don't assume the run summary tells the whole story.

## Step 3 — Pull only the failing job's log, then grep for the signal

`gh run view <run-id> --log-failed` dumps every line of every failed step verbatim — runner provisioning info, Node deprecation warnings, Gradle cache upload progress, etc. Reading it top-to-bottom or `tail`-ing it is unreliable; the real error is often buried above generic exit-code lines. Grep for it instead:

```bash
gh run view <run-id> --log-failed | grep -inE "error:|FAILURE:|Execution failed|exception|##\[error\]"
```

Then pull context around the matching line number(s):

```bash
gh run view <run-id> --log-failed | sed -n '<start>,<end>p'
```

To scope to one job (find its ID from Step 2's job list):

```bash
gh run view <run-id> --job <job-id> --log-failed
```

If grep finds nothing useful, the failure may be a plain `BUILD FAILED` with no separate `error:` line — in that case find the last `> Task :...` line before the failure; that's the failing Gradle task.

## Step 4 — Known failure signatures in this repo

| Symptom in the log | Root cause | Fix |
|---|---|---|
| `error: Unknown iOS simulator arch: 'x86_64'` during `build-ios` / `syncComposeResourcesForIos` | Xcode/Compose Multiplatform resource sync targeting a legacy x86_64 simulator slice not supported on the `macos-15` (Apple Silicon) runner | Exclude `x86_64` from simulator archs — check `EXCLUDED_ARCHS` / `VALID_ARCHS` in `app/app/ios/iosApp.xcodeproj/project.pbxproj`, or narrow the `xcodebuild -destination` in `build-app.yml` to `platform=iOS Simulator,arch=arm64` |
| `Unable to download artifact(s): Artifact not found for name: X` in `publish-release` | An upstream job's build/upload step failed or was skipped, so the artifact was never uploaded — this is a symptom, not the root cause | Diagnose the upstream job (`build-android`/`build-ios`) that should have produced `X`; this step already has `continue-on-error: true`, don't "fix" it by suppressing the error further |
| `failed to run git: fatal: not a git repository (or any of the parent directories): .git` right before a `gh release create`/`gh release delete` step fails | The job never ran `actions/checkout` (true for `publish-release`), so `gh` has no local repo context to resolve the target repository | Add an `actions/checkout@v4` step to the job, or set `GH_REPO: ${{ github.repository }}` as an env var on the `gh` steps |
| `BUILD FAILED` with ktlint-style messages or Compose compile errors traceable to formatting | Unformatted/violating Kotlin broke compilation (CI does not run `spotlessCheck`, but bad formatting can still break `assembleDebug`) | Use the `fix-spotless-issues` skill |
| `e: file:///path/to/File.kt:12:5 ...` | Kotlin compiler error, points directly at file:line | Open the file, fix per `AGENTS.md` conventions (this directory's, i.e. `app/AGENTS.md`) |
| `Tag 'vX.Y.Z' does not match required vMAJOR.MINOR.PATCH format` or `MINOR and PATCH must be less than 100` (`release-app.yml`) | Git tag doesn't satisfy the `Parse version from tag` step's regex/range check | Re-tag with a valid `vMAJOR.MINOR.PATCH` where MINOR and PATCH are each `< 100` |
| `gradle: Execution failed for task ':...:compileDebugKotlinAndroid'` or similar per-module compile task | Compile error scoped to one module | Narrow reproduction locally: `./gradlew :module:path:compileDebugKotlinAndroid` |

If a failure doesn't match this table, treat the grepped `error:`/`FAILURE:` line as authoritative and reason from there — don't guess.

## Step 5 — Reproduce and propose a fix

1. Where possible, reproduce locally with the same Gradle task the workflow ran (see `run:` steps in the workflow YAML) before proposing a fix — this repo's CI failures are almost always reproducible with `./gradlew <same task>`.
2. State the root cause in one sentence, citing the exact log line.
3. Propose a minimal diff:
   - Workflow YAML bug → edit `.github/workflows/*.yml` directly.
   - Application/build code bug → fix the code per `AGENTS.md` conventions (this directory's, i.e. `app/AGENTS.md`) and run `fix-spotless-issues` if it's formatting-related.
4. **Never "fix" a failure by hiding it** — no removing the failing step, no `continue-on-error: true`, no `|| true`, no disabling a check — unless the user explicitly asks for that. The goal is root cause, not a green checkmark.

## Verification

- [ ] Identified the specific failing job (not just "the run failed")
- [ ] Found the actual `error:`/`FAILURE:` line, not just generic exit-code noise
- [ ] Root cause stated as one sentence with a cited log line
- [ ] Fix reproduces/resolves locally where the failure type allows it (e.g. a Gradle task)
- [ ] Fix addresses the cause, not just silences the check
