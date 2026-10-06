# app:ios

SwiftUI iOS entry point. **Not a Gradle module** — a separate Xcode project (`iosApp.xcodeproj`) that consumes `app:shared`'s Kotlin/Native `Shared.framework` via direct integration (Gradle's `embedAndSignAppleFrameworkForXcode`, invoked from the repo root as `:app:app:shared:embedAndSignAppleFrameworkForXcode`).

## Key Files

```
iosApp/
  iOSApp.swift        ← @main App entry point. Reads the TT* Info.plist keys and calls IosAppInitializerKt.bootstrapIosApp(isDebug:apiBaseUrl:realmUrl:oauthClientId:appScheme:) before rendering ContentView()
  ContentView.swift    ← Wraps MainViewControllerKt.MainViewController() (from app:shared, iosMain) in a UIViewControllerRepresentable
  Info.plist           ← App display name/icon, CADisableMinimumFrameDurationOnPhone (required — its absence caused a 20s watchdog kill on every launch, see git history)
  GoogleService-Info.plist  ← Firebase config, not committed — see setup-local-tools skill
Environment.xcconfig   ← Per-environment values (bundle ID, display name, API/realm URLs, OAuth client, scheme), selected by APP_ENVIRONMENT (staging|prod, default staging). Project-level base configuration — CocoaPods owns the target-level one. See app/README.md → "Build variants"
Podfile                ← CocoaPods deps (Firebase Crashlytics linking only)
```

## Responsibilities

- `iOSApp.swift` is the only place platform bootstrap happens (`bootstrapIosApp`) — mirrors what `TruckTrackApplication` does on Android.
- `ContentView.swift` is the only bridge between SwiftUI and Compose — it hosts `app:shared`'s `MainViewController()` and does nothing else. All actual UI lives in Compose Multiplatform code in `app:shared`/`feature/*/impl`, not here.

## Rules

- Do **not** add SwiftUI screens here. Every screen lives in a `feature/*/impl` module as a Compose Multiplatform Composable, shared with Android.
- Do **not** add business logic here — this target is a thin shell, same as `app:android`.
- Building requires macOS + Xcode 15+ + CocoaPods (`pod install` in this directory generates `iosApp.xcworkspace` — open that, not the `.xcodeproj`, after the first `pod install`).

## Depends On

- `app:shared`'s `Shared.framework` (Kotlin/Native), embedded via Gradle, not CocoaPods/SPM.
