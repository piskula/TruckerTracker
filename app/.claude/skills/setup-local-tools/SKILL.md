---
name: setup-local-tools
description: Use when setting up a new machine for this repo, checking whether required CLI tools (JDK, Android SDK, Xcode, gh, Node, firebase-tools) are installed and authenticated, or diagnosing why an agent can't reach Firebase via MCP. Triggered by phrases like "set up my environment", "what do I need to install", "onboard me on this repo", "check my dev tools", "set up the firebase cli", "set up gh cli", "why can't you access firebase".
---

# Skill: Set Up Local Tools for TruckTrack + AI Agents

> The recommended, from-scratch local setup for building this repo and letting an AI coding agent
> work in it at full capability (Firebase MCP, `gh`, Gradle, Xcode). Always install the
> **latest stable** release of each tool unless a specific minimum is called out below — don't
> assume whatever happens to already be on the machine is adequate; verify with the check command.
>
> There is no GitHub MCP server here — it would need Docker, and `gh` alone already covers
> everything the `analyze-ci-failure` and `release-app` skills need (inspecting CI runs, PRs,
> cutting releases). Firebase MCP is kept: Firebase App Distribution setup and Crashlytics
> analysis both need it.

## What this repo needs

| Tool | Version | Needed for | Check | Platform |
|---|---|---|---|---|
| JDK | 25 (Temurin/Adoptium recommended) | Gradle toolchain — `jvmToolchain(25)` in `app/build-logic/convention` | `java -version` | all |
| Android SDK | cmdline-tools + platform 37 + build-tools (latest) | `./gradlew :app:app:android:assembleDebug` (from the repo root), Android Studio | Android SDK path env var set and exists (`ANDROID_HOME` on macOS/Linux, `%LOCALAPPDATA%\Android\Sdk` on Windows) | all |
| Xcode | latest stable from the App Store (15+ minimum) + CLT | Building `app:ios` (`app/app/ios/iosApp.xcodeproj`) | `xcode-select -p` | **macOS only** — iOS can't be built elsewhere |
| `gh` (GitHub CLI), logged in | latest | Agents running `gh run list`/`gh release create` etc. directly (`analyze-ci-failure`, `release-app` skills) | `gh auth status` | all |
| Node.js | current LTS (20+) | Runs the Firebase MCP server (`npx -y firebase-tools@latest mcp`, see `.mcp.json`) — an old Node breaks `npx` resolution for modern packages | `node --version` | all |
| `firebase-tools` CLI, logged in | latest (must support the `mcp` subcommand — anything recent does) | Firebase App Distribution setup, Crashlytics analysis, and the underlying tool the Firebase MCP server wraps | `firebase --version` and `firebase login:list` | all |

## Step 1 — Install

Pick the section for your OS. Everything except Xcode/iOS builds applies to macOS, Linux, and
Windows equally.

### macOS (Homebrew)

```bash
brew install --cask temurin@25          # JDK 25
brew install --cask android-commandlinetools   # or install SDK via Android Studio's SDK Manager
xcode-select --install                  # Xcode itself: install from the App Store first
brew install gh && gh auth login
brew install node                       # current LTS
npm install -g firebase-tools@latest && firebase login
```

### Linux (Debian/Ubuntu — adapt package manager as needed)

```bash
# JDK 25
sudo apt update && sudo apt install -y openjdk-25-jdk
# or use sdkman: sdk install java 25-tem

# Android SDK — download cmdline-tools from developer.android.com/studio#command-tools,
# unzip, then: sdkmanager "platform-tools" "platforms;android-37" "build-tools;37.0.0"

# GitHub CLI
type -p curl >/dev/null && curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg \
  | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" \
  | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
sudo apt update && sudo apt install -y gh
gh auth login

# Node.js (current LTS) — via nvm avoids fighting distro package versions
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash
nvm install --lts

# Firebase CLI
npm install -g firebase-tools@latest && firebase login
```

### Windows (PowerShell, winget)

```powershell
winget install EclipseAdoptium.Temurin.25.JDK   # JDK 25
# Android SDK — install via Android Studio's SDK Manager (simplest on Windows)
winget install GitHub.cli
gh auth login
winget install OpenJS.NodeJS.LTS                # current LTS
npm install -g firebase-tools@latest
firebase login
```

Set `ANDROID_HOME` as a persistent user environment variable if not already set (Android Studio's
SDK Manager installs the SDK but doesn't always set this):

```powershell
[System.Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
```

`gh auth login`'s interactive arrow-key menu needs a real TTY — if running through an agent's
non-interactive shell pass-through, use the flag form instead so it only needs one code paste in
the browser:

```bash
gh auth login --hostname github.com --git-protocol https --web
```

`firebase login` refuses to run at all in a non-interactive shell — run it directly in your own
terminal window, not through an agent's shell pass-through.

The Firebase MCP server is already declared in `.mcp.json` — nothing to configure per-machine
beyond Node.js and being logged in via `firebase login`; `.mcp.json` itself is OS-agnostic (`npx`
resolves the same way on every platform once Node is installed).

## Step 2 — Verify

Run all of these and confirm each passes before considering the machine ready (skip the
Xcode/iOS-specific one off macOS):

```bash
java -version                # 25
gh auth status                # logged in
node --version                # 20+
firebase --version
firebase login:list            # shows an authenticated account
xcode-select -p               # macOS only — points at an installed Xcode
```

- [ ] `./gradlew :app:app:android:assembleDebug` succeeds from the repo root (or `cd app && ./gradlew :app:android:assembleDebug` for a decoupled client-only build)
- [ ] (macOS only) `open app/app/ios/iosApp.xcodeproj` builds and runs in Xcode
- [ ] A fresh Claude Code session shows the `firebase` MCP server in its tool list
      (MCP servers load at session start — restart the session after finishing setup)

## Step 3 — Firebase config files for local builds

`app/app/android/google-services.json` and `app/app/ios/iosApp/GoogleService-Info.plist` are required for
Crashlytics (and Firebase generally) to compile/link, but neither is committed — both are
gitignored and fetched fresh in CI on every build via `firebase apps:sdkconfig` (see the "Fetch
Firebase Android config" / "Fetch Firebase iOS config" steps in `.github/workflows/build-app.yml`
and `release-app.yml`, both at the repo root). A fresh local checkout has neither file, so local builds need one of:

- **Firebase Console** (no CLI needed): `console.firebase.google.com` → `trucktrack-cf134` → ⚙️
  Project settings → General → "Your apps" → download `google-services.json` /
  `GoogleService-Info.plist` for the respective app → place at the paths above.
- **`firebase-tools` CLI** (once logged in per Step 1):
  ```bash
  firebase apps:list --project trucktrack-cf134   # get the ANDROID/IOS app IDs
  firebase apps:sdkconfig ANDROID <ANDROID_APP_ID> --project trucktrack-cf134 -o app/app/android/google-services.json
  firebase apps:sdkconfig IOS <IOS_APP_ID> --project trucktrack-cf134 -o app/app/ios/iosApp/GoogleService-Info.plist
  ```

This is a one-time step per machine — the files don't change unless the Firebase project
reconfigures. iOS additionally needs `pod install` run from `app/app/ios` (CocoaPods) before opening
`iosApp.xcworkspace` — see the `manage-ios-signing` skill for the rest of the iOS build setup.

## Gotchas worth knowing

| Gotcha | Why it happens |
|---|---|
| `./gradlew` fails with "No matching toolchains found for requested specification: {languageVersion=25}" even though JDK 25 is installed | `settings.gradle.kts` doesn't apply the `org.gradle.toolchains.foojay-resolver-convention` plugin, so Gradle only discovers JDKs already on the machine — it won't auto-download one. Point `JAVA_HOME` at the JDK 25 install (env var on macOS/Linux, System Environment Variables on Windows), or add its path to `org.gradle.java.installations.paths` in `gradle.properties` |
| Firebase MCP tool list looks sparse | No `firebase.json` at the repo root yet, so the MCP server has nothing to detect and can only offer project-agnostic tools. Run `firebase init` for the features you actually need (App Distribution, Crashlytics, etc.) to get the full tool set |
| A newly edited `.mcp.json` doesn't show new tools in the running session | MCP servers are only loaded at session start — start a new Claude Code session after any `.mcp.json` or auth change |
| A tool installed via `winget`/`brew`/`apt` mid-session isn't found (`command not found` / `not recognized`) | The shell process was started before install and cached the old `PATH`. Open a new terminal/session, or on Windows reload it: `$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")` |
