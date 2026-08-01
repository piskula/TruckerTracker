# TruckTrack Copilot instructions

`AGENTS.md` files are the canonical project instructions. Before changing a file, read and follow
the root `AGENTS.md` plus the nearest nested `AGENTS.md` for each affected area — `app/`, `server/`,
and `shared/` each have their own, as do most modules beneath them. Do not copy their guidance into
this file.

The rules below are repeated here only because they must hold even before you have opened any other
file.

## This repository is public

`piskula/TruckerTracker` is a public GitHub repository. Commits, file contents, and Actions
logs are world-readable.

* Never commit secret values — passwords, private keys, certificates, API tokens, `.p12` /
  `.mobileprovision` / `.keystore` files. Only secret *names* belong in tracked files.
* Never print secrets or anything derived from them in CI. Debug output is limited to byte counts,
  file types, variable lengths, and exit codes.
* Don't hardcode environment-specific identifiers (Team IDs, Firebase project/app IDs, tester or
  profile names) into committed files, even when they aren't strictly secret.

## Never write code comments

No `//` or block comments, no KDoc/Javadoc, no section banners, no TODO markers — in any language,
anywhere in the repo. Express intent through naming and structure. Do not add comments to code you
modify, and do not document surrounding code you happen to touch. Narrow exceptions: license
headers, tool directives that must be comments (`// ktlint-disable`, `// noinspection`), and
generated-file banners. This applies to source code only — Markdown and commit messages are prose.

## Kotlin everywhere

Kotlin 2.x for both client and server. Take dependencies from `gradle/libs.versions.toml`, shared by
all three builds — never put a version literal in a module's `build.gradle.kts`.
