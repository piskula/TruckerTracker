---
name: release-app
description: Use when cutting a signed release (Android + iOS, staging + prod) or explaining how to release the app. Triggered by phrases like "release the app", "cut a release", "release android", "publish a new version", "tag a release", "how do I release", "release next patch", "release next minor", "release next major", "bump the version".
---

# Skill: Cut a Signed Release

> `.github/workflows/release-app.yml` builds signed staging + prod release APKs, AABs and ad-hoc `.ipa`s, publishes them as one GitHub Release and distributes them through Firebase App Distribution whenever a `vMAJOR.MINOR.PATCH` tag is pushed. There is no separate version-bump commit or manifest edit — the tag *is* the version, parsed at build time.

## Triggers

- "release the app", "cut a release", "release version X.Y.Z"
- "release next patch" / "release next minor" / "release next major", "bump the version"
- "how do I publish a new Android build"
- Debugging a failed `release-app.yml` run (pair with the `analyze-ci-failure` skill)

## How to cut a release

```bash
git tag v1.2.3
git push origin v1.2.3
```

That's the entire release process. Pushing the tag runs:

1. **`prepare`** — parses `vMAJOR.MINOR.PATCH` from `GITHUB_REF_NAME` (**fails fast** if the tag doesn't match, or if `MINOR`/`PATCH` are ≥ 100), computes `versionCode = MAJOR * 10000 + MINOR * 100 + PATCH` (this is why `MINOR`/`PATCH` are capped under 100 — keeps codes from colliding across versions), and writes the changelog since the previous tag.
2. **`build-android`** — decodes the release keystore from `ANDROID_KEYSTORE_BASE64` and runs `assembleStagingRelease bundleStagingRelease assembleProdRelease bundleProdRelease` in one Gradle run with `-PappVersionName`/`-PappVersionCode` from the tag (they flow into `app/app/android/build.gradle.kts` via `stringProperty("appVersionName"/"appVersionCode")`). Outputs are renamed to `truck-track-<env>-<version>.apk` / `.aab`.
3. **`build-ios`** (parallel to Android) — archives staging and prod with the `Release` configuration through `.github/actions/archive-ios-app`, producing `truck-track-<env>-<version>.ipa` (ad hoc) and uploading dSYMs to Crashlytics. The prod archive is also exported with the App Store profile into the separate `ios-appstore` artifact (not attached to the GitHub Release). Skipped with a warning when iOS signing secrets are missing.
4. **`publish-release`** — `gh release create "$GITHUB_REF_NAME"` with all artifacts and the changelog. Requires all 4 Android artifacts; publishes without iOS (with a warning) if the `.ipa`s are missing.
5. **`distribute-android`** / **`distribute-ios`** — each build to its own Firebase app (staging / prod), `release` group.
6. **`upload-testflight`** — uploads the App Store prod `.ipa` with `xcrun altool` using the App Store Connect API key (`APP_STORE_CONNECT_API_KEY_*`, `APP_STORE_CONNECT_ISSUER_ID`). Skipped with a warning when those secrets or `IOS_PROD_APPSTORE_PROFILE_BASE64` are missing. App Store Connect rejects a build number it has already seen, so re-running this job for the same tag fails.

Verified end to end against tag `v0.0.1` (before the staging/prod split, so the asset names were unprefixed): release assets were `truck-track-0.0.1.apk` / `truck-track-0.0.1.aab`, and `aapt dump badging` on the APK confirmed `versionCode='1' versionName='0.0.1'` (`0*10000 + 0*100 + 1 = 1`) — the version flows through correctly from tag → Gradle → manifest → filename.

## Releasing the next patch / minor / major

There is **no** version state anywhere in the repo — no `version.properties`, no manifest entry — the only source of truth is the highest existing `vMAJOR.MINOR.PATCH` git tag. "Release next patch/minor/major" means: find that tag, bump the requested segment, tag, push. Compute it yourself with git, don't guess or hand-type the next number:

```bash
git fetch --tags --quiet
LATEST=$(git tag --list 'v*' | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' | sort -V | tail -1)
LATEST=${LATEST:-v0.0.0}   # no release yet -> next patch/minor/major starts from 0.0.0
IFS='.' read -r MAJOR MINOR PATCH <<< "${LATEST#v}"

# pick exactly one, matching what was asked for:
PATCH=$((PATCH + 1))                              # next patch
# MINOR=$((MINOR + 1)); PATCH=0                    # next minor
# MAJOR=$((MAJOR + 1)); MINOR=0; PATCH=0            # next major

NEXT="v${MAJOR}.${MINOR}.${PATCH}"
echo "Latest: $LATEST -> Next: $NEXT"

git tag "$NEXT"
git push origin "$NEXT"
```

`sort -V` (version sort) is what makes this correct across width changes, e.g. `v1.9.0` sorting before `v1.10.0`. The `grep -E` filter matters too — it excludes any non-release tags so an unrelated tag can't be mistaken for the latest version. Reject the bump before tagging if the resulting `MINOR`/`PATCH` would hit the `< 100` cap (see Tag rules below) — that can only happen on `next minor` when `MINOR` is 98/99, or already at 99 for `next patch`.

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
| Must match `^v([0-9]+)\.([0-9]+)\.([0-9]+)$` exactly | e.g. `v1.2.3` — not `1.2.3` (missing `v`), not `v1.2` (missing patch) |
| `MINOR` and `PATCH` each `< 100` | Guarantees `versionCode = MAJOR*10000 + MINOR*100 + PATCH` never collides between two different `MAJOR.MINOR.PATCH` combinations |

## If you tag the wrong thing

`gh release create` fails outright if a release for that tag already exists — you can't just re-push the same tag to retry. Delete both the tag and release first:

```bash
gh release delete v1.2.3 --yes --cleanup-tag   # deletes the GitHub release + remote tag
git tag -d v1.2.3                              # delete the local tag too (cleanup-tag only removes remote)
```

Then re-tag and push as above.

## If the run fails

Use the `analyze-ci-failure` skill against the `release-app.yml` run. Known-specific failure modes for this workflow:

| Symptom | Cause |
|---|---|
| `Tag 'vX.Y.Z' does not match required vMAJOR.MINOR.PATCH format` | Tag pushed without the `v` prefix, or wrong segment count |
| `MINOR and PATCH must be less than 100 to avoid versionCode collisions` | e.g. `v1.150.0` |
| Build fails at "Build staging and prod release APKs and AABs" with a signing-related Gradle error | One of the four `ANDROID_KEYSTORE_*`/`ANDROID_KEY_*` secrets is missing, wrong, or the keystore file is corrupt/mismatched with the alias |
| `gh release create` fails with "release already exists" | The tag was already released once — delete the release + tag first (see above), don't just push again |

## Verification

- [ ] Tag matches `vMAJOR.MINOR.PATCH` with `MINOR`/`PATCH` under 100
- [ ] `gh run view <run-id>` shows `build-android`, `build-ios`, `publish-release` and both `distribute-*` jobs succeeded
- [ ] `gh release view vX.Y.Z` shows `truck-track-{staging,prod}-X.Y.Z.apk`, `.aab` and `.ipa` assets (6 total)
- [ ] (optional) `aapt dump badging truck-track-prod-X.Y.Z.apk | head -1` confirms `versionName`/`versionCode` match the tag
