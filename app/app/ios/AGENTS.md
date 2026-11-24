# app:ios

SwiftUI iOS entry point. **Not a Gradle module** — a separate Xcode project (`iosApp.xcodeproj`) that consumes `app:shared`'s Kotlin/Native `Shared.framework` via direct integration (Gradle's `embedAndSignAppleFrameworkForXcode`, invoked from the repo root as `:app:app:shared:embedAndSignAppleFrameworkForXcode`).

## Key Files

```
iosApp/
  iOSApp.swift        ← @main App entry point. Hands the DeviceKit device name to Kotlin, reads the TT* Info.plist keys and calls IosAppInitializerKt.bootstrapIosApp(isDebug:apiBaseUrl:realmUrl:oauthClientId:appScheme:) before rendering ContentView(). Also holds the AppDelegate (UIApplicationDelegateAdaptor) for push notifications
  ContentView.swift    ← Wraps MainViewControllerKt.MainViewController() (from app:shared, iosMain) in a UIViewControllerRepresentable
  Info.plist           ← App display name/icon, CADisableMinimumFrameDurationOnPhone (required — its absence caused a 20s watchdog kill on every launch, see git history), UIBackgroundModes remote-notification, FirebaseMessagingInstallationIdEnabled (FCM registration by Firebase Installation ID instead of the deprecated registration token)
  iosApp.entitlements  ← aps-environment (push notifications), set as CODE_SIGN_ENTITLEMENTS for every configuration of the iosApp target
  GoogleService-Info.plist  ← Firebase config, not committed — see setup-local-tools skill
  PrivacyInfo.xcprivacy     ← App privacy manifest: collected data + required-reason APIs (UserDefaults, file timestamps/disk space via Coil's disk cache, boot time via Compose). Update it when adding a dependency or platform API in one of Apple's required-reason categories; keep it consistent with docs/privacy-policy.md
Environment.xcconfig   ← Per-environment values (bundle ID, display name, API/realm URLs, OAuth client, scheme), selected by APP_ENVIRONMENT (staging|prod, default staging). Project-level base configuration — CocoaPods owns the target-level one. See app/README.md → "Build variants"
Podfile                ← CocoaPods deps (FirebaseCore, FirebaseCrashlytics, FirebaseMessaging, DeviceKit)
```

## Responsibilities

- `iOSApp.swift` is the only place platform bootstrap happens (`bootstrapIosApp`) — mirrors what `TruckTrackApplication` does on Android.
- `iOSApp.init` sets `IosPushBridge.shared.deviceName` before bootstrapping: DeviceKit's `Device.current.realDevice.description` (the simulator reports the simulated model), or `UIDevice.current.model` when DeviceKit doesn't know the device. `UIDevice.name` is not used — it's generic ("iPhone") since iOS 16.
- `AppDelegate` (in `iOSApp.swift`) is the only place Swift talks to Firebase Cloud Messaging. Firebase itself is configured earlier, from Kotlin (`bootstrapIosApp` → `CrashReporting.init()`), because `App.init` runs before `didFinishLaunching`. In `didFinishLaunching` it hands the notification settings URL (`UIApplication.openNotificationSettingsURLString` on iOS 16+, otherwise the app's Settings page) to Kotlin, becomes the `UNUserNotificationCenter` and `Messaging` delegate and always calls `registerForRemoteNotifications()` — that needs no user permission, and the app registers its push device regardless of the permission. The APNs token is mapped explicitly to `Messaging.apnsToken` (required for SwiftUI apps, where method swizzling can't find the delegate); the Firebase Installation ID from `messaging(_:didReceiveRegistration:)` goes to `IosPushBridge.shared.onFirebaseInstallationIdReceived(firebaseInstallationId:)`; foreground notifications are shown with `[.banner, .sound, .list]` and their string `userInfo` values go to `IosPushBridge.shared.onMessageReceived(data:)`. A tap just completes. Everything else (registration, permission gate) lives in Kotlin, see `core/push/AGENTS.md`.
- `ContentView.swift` is the only bridge between SwiftUI and Compose — it hosts `app:shared`'s `MainViewController()` and does nothing else. All actual UI lives in Compose Multiplatform code in `app:shared`/`feature/*/impl`, not here.

## Rules

- Do **not** add SwiftUI screens here. Every screen lives in a `feature/*/impl` module as a Compose Multiplatform Composable, shared with Android.
- Do **not** add business logic here — this target is a thin shell, same as `app:android`.
- Building requires macOS + Xcode 15+ + CocoaPods (`pod install` in this directory generates `iosApp.xcworkspace` — open that, not the `.xcodeproj`, after the first `pod install`).

## Depends On

- `app:shared`'s `Shared.framework` (Kotlin/Native), embedded via Gradle, not CocoaPods/SPM.
