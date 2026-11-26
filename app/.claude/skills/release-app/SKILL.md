---
name: release-app
description: Use when cutting a signed release (Android + iOS, staging + prod) or explaining how to release the app. Triggered by phrases like "release the app", "cut a release", "release android", "publish a new version", "tag a release", "how do I release", "release next patch", "release next minor", "release next major", "bump the version".
---

# Skill: Cut a Signed Release

> `.github/workflows/release-app.yml` builds signed staging + prod release APKs, AABs and ad-hoc `.ipa`s, publishes them as one GitHub Release and distributes them through Firebase App Distribution whenever a `mobile-MAJOR.MINOR.PATCH` tag is pushed on a commit already on `main`. There is no separate version-bump commit or manifest edit — the tag *is* the version. Server releases use independent `server-*` tags.

## Triggers

- "release the app", "cut a release", "release version X.Y.Z"
- "release next patch" / "release next minor" / "release next major", "bump the version"
- "how do I publish a new Android build"
- Debugging a failed `release-app.yml` run (pair with the `analyze-ci-failure` skill)

## How to cut a release

```bash
git fetch origin main --tags
git tag mobile-1.2.3 origin/main
git push origin mobile-1.2.3
```

That's the entire release process. Pushing the tag runs:

1. **`prepare`** — validates the `mobile-MAJOR.MINOR.PATCH` tag and main ancestry, requires a newer version than existing mobile/legacy `v*` tags, computes `versionCode = MAJOR * 10000 + MINOR * 100 + PATCH`, and writes mobile/shared changes since the previous mobile release. Leading zeros, `MINOR`/`PATCH` ≥ 100, and version codes outside 1–2100000000 are rejected.
2. **`build-android`** — decodes the release keystore from `ANDROID_KEYSTORE_BASE64` and runs `assembleStagingRelease bundleStagingRelease assembleProdRelease bundleProdRelease` in one Gradle run with `-PappVersionName`/`-PappVersionCode` from the tag (they flow into `app/app/android/build.gradle.kts` via `stringProperty("appVersionName"/"appVersionCode")`). Outputs are renamed to `truck-track-<env>-<version>.apk` / `.aab`.
3. **`build-ios`** (parallel to Android) — archives staging and prod with the `Release` configuration through `.github/actions/archive-ios-app`, producing `truck-track-<env>-<version>.ipa` (ad hoc) and uploading dSYMs to Crashlytics. The prod archive is also exported with the App Store profile into the separate `ios-appstore` artifact (not attached to the GitHub Release). Skipped with a warning when iOS signing secrets are missing.
4. **`publish-release`** — `gh release create "$GITHUB_REF_NAME"` with all artifacts and the changelog. Requires all 4 Android artifacts; publishes without iOS (with a warning) if the `.ipa`s are missing.
5. **`distribute-android`** / **`distribute-ios`** — each build to its own Firebase app (staging / prod), `release` group.
6. **`upload-testflight`** — uploads the App Store prod `.ipa` with `xcrun altool` using the App Store Connect API key (`APP_STORE_CONNECT_API_KEY_*`, `APP_STORE_CONNECT_ISSUER_ID`). Skipped with a warning when those secrets or `IOS_PROD_APPSTORE_PROFILE_BASE64` are missing. App Store Connect rejects a build number it has already seen, so re-running this job for the same tag fails.
7. **`upload-google-play`** — uploads the prod AAB and mapping file to the internal track when `PLAY_SERVICE_ACCOUNT_JSON` is configured. Public store rollout remains manual.

Verified end to end against tag `v0.0.1` (before the staging/prod split, so the asset names were unprefixed): release assets were `truck-track-0.0.1.apk` / `truck-track-0.0.1.aab`, and `aapt dump badging` on the APK confirmed `versionCode='1' versionName='0.0.1'` (`0*10000 + 0*100 + 1 = 1`) — the version flows through correctly from tag → Gradle → manifest → filename.

## Releasing the next patch / minor / major

There is **no** mobile version state in the repo. Use the highest version across `mobile-MAJOR.MINOR.PATCH` and legacy `vMAJOR.MINOR.PATCH` tags; ignore `server-*`. The prefix change does not reset app-store numbering. Find that version, bump the requested segment, tag `origin/main`, and push:

```bash
git fetch origin main --tags --quiet
LATEST=$(git tag --list 'mobile-*' 'v*' | sed -E 's/^(mobile-|v)//' | grep -E '^[0-9]+\.[0-9]+\.[0-9]+$' | sort -V | tail -1)
LATEST=${LATEST:-0.0.0}
IFS='.' read -r MAJOR MINOR PATCH <<< "$LATEST"

# pick exactly one, matching what was asked for:
PATCH=$((PATCH + 1))                              # next patch
# MINOR=$((MINOR + 1)); PATCH=0                    # next minor
# MAJOR=$((MAJOR + 1)); MINOR=0; PATCH=0            # next major

NEXT="mobile-${MAJOR}.${MINOR}.${PATCH}"
echo "Latest: $LATEST -> Next: $NEXT"

git tag "$NEXT" origin/main
git push origin "$NEXT"
```

`sort -V` keeps numeric ordering across width changes. Reject the bump before tagging if `MINOR`/`PATCH` would reach 100 or the version code would exceed the allowed range.

## Required secrets

Four Android signing secrets must exist (`gh secret list` to confirm) — `build-android` fails during "Build staging and prod release APKs and AABs" if any are missing, since `AndroidSigningPlugin` only wires the `release` signing config when all four are non-blank:

| Secret | Used for |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | base64-encoded `.keystore`/`.jks` file, decoded to `$RUNNER_TEMP/release.keystore` |
| `ANDROID_KEYSTORE_PASSWORD` | keystore password |
| `ANDROID_KEY_ALIAS` | signing key alias |
| `ANDROID_KEY_PASSWORD` | signing key password |

Firebase and iOS signing secrets (`FIREBASE_{STAGING,PROD}_{ANDROID,IOS}_APP_ID`, `IOS_{STAGING,PROD}_ADHOC_PROFILE_BASE64`, `IOS_TEAM_ID`, `IOS_DISTRIBUTION_CERTIFICATE_*`) are covered in `app/README.md` → "Releasing" and the `manage-ios-signing` skill.

## Tag rules

| Rule | Why |
|---|---|
| Must match `mobile-MAJOR.MINOR.PATCH` without leading zeros | Legacy `v*` tags and `server-*` tags do not trigger mobile releases |
| Commit must already be on `main`; version must increase | Prevents unreviewed branch releases and app-store version reuse |
| `MINOR` and `PATCH` each `< 100` | Guarantees `versionCode = MAJOR*10000 + MINOR*100 + PATCH` never collides between two different `MAJOR.MINOR.PATCH` combinations |

## If you tag the wrong thing

Do not move published release tags. Fix the issue on `main` and release a newer version.
For transient signing/distribution failures, fix the configuration and rerun only the failed
jobs. Do not republish a GitHub Release or upload an app-store build number that already exists.

## If the run fails

Use the `analyze-ci-failure` skill against the `release-app.yml` run. Known-specific failure modes for this workflow:

| Symptom | Cause |
|---|---|
| `Expected mobile-MAJOR.MINOR.PATCH without leading zeros` | Wrong prefix, leading zeros, or wrong segment count |
| `Release tags must point to the checked-out commit reachable from origin/main` | Tag points to an unmerged commit |
| `Release version must be newer than ...` | Version does not exceed an existing mobile/legacy version |
| `Mobile MINOR and PATCH must be below 100 ...` | e.g. `mobile-1.150.0` |
| Build fails at "Build staging and prod release APKs and AABs" with a signing-related Gradle error | One of the four `ANDROID_KEYSTORE_*`/`ANDROID_KEY_*` secrets is missing, wrong, or the keystore file is corrupt/mismatched with the alias |
| `gh release create` fails with "release already exists" | Release is already published; rerun failed downstream jobs instead |

## Verification

- [ ] Tag matches `mobile-MAJOR.MINOR.PATCH`, advances mobile numbering, and points to `main`
- [ ] `gh run view <run-id>` shows `build-android`, `build-ios`, `publish-release` and both `distribute-*` jobs succeeded
- [ ] `gh release view mobile-X.Y.Z` shows `truck-track-{staging,prod}-X.Y.Z.apk`, `.aab` and `.ipa` assets (6 total when iOS is configured)
- [ ] (optional) `aapt dump badging truck-track-prod-X.Y.Z.apk | head -1` confirms `versionName`/`versionCode` match the tag
