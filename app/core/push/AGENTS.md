# core:push

Push notification plumbing for the client: device registration against the backend's single push slot per user, the Firebase Installation ID, the OS notification permission, and incoming push messages. KMP module with real code in `commonMain`, `androidMain` and `iosMain`. The design, flows and API contract live in `../../../docs/push-notifications.md` — read it before changing behaviour here.

The Firebase Installation ID (FCM's installation-based registration, which replaces the deprecated registration token) is the only device identifier. Always call it "Firebase Installation ID" / `firebaseInstallationId` — never abbreviate it. Notifications are in one language (Slovak, rendered by the server), so the client sends no locale.

## Architecture

Two independent parts, both started by Koin (`createdAtStart`) — `app:shared` has no push-specific startup code:

- **Registration** — `PushRegistrationManager` is the single owner of every registration request. It decides *when* to send; `PushDeviceRegistrar` (internal) knows *what* to send and the backend rules.
- **Messages** — `PushMessageDispatcher` (public interface, internal `PushMessageDispatcherImpl`) parses raw platform messages and delivers them to every `PushMessageHandler` registered for the message type and to its `messages` flow. It never registers.

Coordinators depend on interfaces only (`PushRegistrationManager`, `PushDeviceRegistrar`, `PushMessagingClient`, `FirebaseInstallationIdProvider`, `PushMessageHandler`) — never on an `…Impl` class.

## Public API

| Symbol | Description |
|--------|-------------|
| `PushRegistrationManager` | Interface, Koin `single(createdAtStart = true)`. `state: StateFlow<PushDeviceState>`, `registerThisDevice(): Result<Unit>`, `unregisterThisDevice(): Result<Unit>`, `refreshState(): Result<PushDeviceState>`. Every operation runs in the app-lifetime `CoroutineScope` (injected from `app:shared`), so it survives ViewModel/navigation cancellation, and all operations are serialized by one `Mutex`. |
| `PushMessageHandler` | Interface for reacting to one push message type: `type: PushMessageTypeDto`, `suspend fun handle(message: IncomingPushMessage)`. A feature implements it and registers it in its own Koin module with `single { MyHandler(get()) } bind PushMessageHandler::class`; the dispatcher collects all of them with `getAll()`. A failing handler is logged and doesn't affect other handlers. |
| `PushMessageDispatcher` | `messages: Flow<IncomingPushMessage>` — every received message, for UI that only needs to observe (no replay). |
| `UnregisterPushDeviceOnSignOut` | Internal `SignOutHook` (from `core:user`), bound in `pushModule` with `bind SignOutHook::class`. `AuthManager.signOut()` calls it after Keycloak logged out (or when the session is cleared anyway — offline, or the end-session request failed) and before the local session is cleared, so `unregisterThisDevice()` still runs with a valid access token. A cancelled browser logout never reaches it, so the registration simply stays. Features don't know about it — they just call `AuthManager.signOut()`. |
| `NotificationPermission` | Interface, Koin `single`. `suspend fun status(): NotificationPermissionStatus` checks the OS and publishes the result to `currentStatus: StateFlow<NotificationPermissionStatus?>` (`null` until the first check), `openSettings()`. |
| `rememberNotificationPermissionRequester(onResult)` | `@Composable expect fun` returning a lambda that shows the system permission dialog, then calls `onResult`. Android: `rememberLauncherForActivityResult(RequestPermission())` for `POST_NOTIFICATIONS` (API 33+; below 33 it just calls `onResult`). iOS: `requestAuthorization([.alert, .sound, .badge])`. |
| `FirebaseInstallationIdProvider` | `suspend fun currentFirebaseInstallationId(): String?` (waits up to 10 s for the first ID, then `null`), `firebaseInstallationIdChanges: Flow<String>`. |
| `IosPushEvents` (iosMain) | The Swift → Kotlin entry point for the Firebase Installation ID, foreground messages, the device name and the notification settings URL. Swift never calls it directly — it calls `app:shared`'s `IosPushBridge`, which forwards here. |
| `PushDeviceState` | `Unknown`, `ThisDevice`, `OtherDevice(deviceName)`, `None`. `ThisDevice` when the server's Firebase Installation ID equals this device's current or last sent one. |
| `IncomingPushMessage` | `DeviceUnregistered(newDeviceName)`, `Unrecognized(rawType)`, each with `type: PushMessageTypeDto?` (`null` for unrecognized) — parsed from the FCM `data` block (`type`, `deviceName`). |
| `NotificationPermissionStatus` | `Granted`, `CanAsk`, `Denied`. |

## Registration rules

`PushRegistrationManagerImpl` observes, in the app scope, from its `init` block:

- `AuthManager.loginCompletions` → `registerThisDevice()` (`PUT`). Emitted by both `signIn()` and a login resumed after Android killed the process during the browser step. The flow is backed by a buffered channel, so a login completed before the manager subscribes is not lost, and each login registers exactly once. If the registration still fails after its retries, the failure is logged and reported through `ErrorReporter` (root snackbar); the Profile switch then shows the real state and lets the user retry. Nothing is retried later or across restarts.
- Signed out (`Guest`) → `state` back to `Unknown`.
- At start (restored session) and on every Firebase Installation ID change → send the change, never register:
  - nothing sent yet (no last sent Firebase Installation ID) → nothing;
  - current Firebase Installation ID equals the last sent → nothing;
  - otherwise `PATCH /api/v1/push-device/{last sent Firebase Installation ID}` with the current ID. The server applies it only while this device holds the slot; `200` and `409` both store the current ID as last sent, and a `409` never falls back to a `PUT`.

Registering explicitly (`registerThisDevice()` from the Profile switch) returns a `Result`; those callers report failures themselves.

- **Registration waits and retries briefly, then gives up.** `register()` waits for the Firebase Installation ID (the provider's 10 s timeout) and sends one `PUT` that opts into the client's retry: `registrationRetry()` (`registration/api/RegistrationRetry.kt`) — up to 3 retries on transport failures (`isTransientNetworkFailure`, never cancellation) and 5xx, never on 4xx, exponential backoff 0.5 s → 1 s → 2 s (+ up to 0.25 s jitter, `Retry-After` ignored), so about 4 s at most. It is the only push request that retries; `PATCH`, `GET` and `DELETE` don't (see `core/network/AGENTS.md`).
- `unregisterThisDevice()` is best effort with a 5 s timeout: `DELETE /api/v1/push-device/{last sent Firebase Installation ID, else the current one}`. On sign-out it runs from `UnregisterPushDeviceOnSignOut` (after Keycloak logged out, before the tokens are cleared).
- `DEVICE_UNREGISTERED` → `DeviceUnregisteredHandler` (internal) calls `refreshState()`. It never registers.
- Never log the Firebase Installation ID.

## Networking

`PushDeviceApi` is a Ktorfit interface (`trucktrack.ktor`), created in `pushModule` with `get<Ktorfit>().createPushDeviceApi()`. `register` takes a `@ReqBuilder` parameter so the registrar can pass `registrationRetry()`. The registrar reads the rest through `currentPushDevice()` and `updatePushDevice()` (`registration/api/PushDeviceResponses.kt`):

- `GET` returns `HttpResponse`; `204 No Content` means no device holds the slot (`null`). Don't change it to `PushDeviceDto?` — Ktorfit's default converter returns `null` for nullable return types on **any** failure, which would turn "offline" into "no device".
- `PATCH` `409 Conflict` (the shared client's `ApiException.HttpError`) means this device no longer holds the slot → `PushDeviceUpdateResult.NotSlotHolder`.

## Platform implementations

| | Android (`androidMain`) | iOS (`iosMain`) |
|---|---|---|
| Registration state (last sent Firebase Installation ID) | `SharedPreferences` file `push_registration` | JSON file in `Application Support/PushRegistration/`, a directory marked `NSURLIsExcludedFromBackupKey` |
| Firebase Installation ID | `TruckTrackMessagingService.onRegistered` → `FirebasePushMessagingClient`, which persists the last received ID in `push_registration`; with none yet it calls `FirebaseMessaging.register()` and waits up to 10 s for `onRegistered` | `IosPushMessagingClient` reading `IosPushEvents`, fed by the Swift `AppDelegate`'s `messaging(_:didReceiveRegistration:)`; waits up to 10 s for the first ID |
| Messages | `TruckTrackMessagingService.onMessageReceived` | `IosPushEvents`, fed by the `AppDelegate`'s `willPresent` |
| Permission | `AndroidNotificationPermission` (prompt history in `push_registration`) | `IosNotificationPermission` (`UNUserNotificationCenter`) |
| Device name | `AndroidDeviceNames` market name lookup (manufacturer + market name unless the market name already starts with the manufacturer, e.g. "Samsung Galaxy S8+"), falling back to `Build.MANUFACTURER` + `Build.MODEL` | Set by Swift from DeviceKit (`Device.current.realDevice.description`, so the simulator reports the simulated model; `UIDevice.model` when unknown), falling back to `UIDevice.model` |

- The registration state must never move to another device: a phone restored from another phone's backup would otherwise come up with the old phone's last sent Firebase Installation ID and its start-up `PATCH` would silently move the slot to itself. Android: `push_registration` is excluded from Auto Backup and device transfer by `app:android`'s `res/xml/backup_rules.xml` and `res/xml/data_extraction_rules.xml` — renaming the file means updating those rules. iOS: the backup-excluded `Application Support` directory (`NSUserDefaults` is backed up and restored to new devices).
- Firebase installation-based registration is switched on by `firebase_messaging_installation_id_enabled` in the module manifest (Android) and `FirebaseMessagingInstallationIdEnabled` in `app/ios`'s `Info.plist`.
- Android `CanAsk` vs `Denied`: Android can't tell "never asked" from "permanently denied", so the requester records after each system dialog whether the dialog can still be shown (`shouldShowRequestPermissionRationale`). Granted on API 33+ but notifications or the `trucktrack_notifications` channel switched off is `Denied`. Below API 33 there is no runtime prompt: notifications enabled is `Granted`, otherwise `Denied`.
- `PushNotificationPresenter` creates the `trucktrack_notifications` channel (importance high) at Koin start and shows foreground notifications itself — FCM only calls `onMessageReceived` for notification messages while the app is in the foreground. Tapping a notification opens the launcher activity.
- The module manifest declares `POST_NOTIFICATIONS`, the messaging service, the Firebase Installation ID switch, and FCM's default channel, small icon (`res/drawable/ic_push_notification.xml`) and colour. Android resources are enabled for this module (`androidResources { enable = true }`) for the channel name, icon and colour.
- iOS opens `UIApplication.openNotificationSettingsURLString` when the Swift shell provided it (iOS 16+), otherwise the app's Settings page. Kotlin doesn't reference the iOS 16 constant itself because the deployment target is iOS 15.

## Android-only dependencies

- `com.google.firebase:firebase-messaging` (via the Firebase BOM) — the FCM client (`FirebaseMessaging`, `FirebaseMessagingService.onRegistered`). iOS path: the FirebaseMessaging CocoaPod used from Swift in `app/ios`, bridged into Kotlin through `IosPushBridge` → `IosPushEvents`.
- `com.jaredrummler:android-device-names` — bundled database mapping Android models to market names (`DeviceName.with(context).request`). Its database ends in 2022, so newer phones fall back to manufacturer + model. It adds nothing to the merged manifest. iOS path: the DeviceKit CocoaPod used from Swift in `app/ios`, passed to Kotlin as `IosPushBridge.deviceName`.
- `androidx.activity:activity-compose` — `rememberLauncherForActivityResult`/`LocalActivity` for the runtime permission request. iOS path: `UNUserNotificationCenter.requestAuthorization`.

## Key Files

Packages are grouped by concern; visibility comes from the `internal` modifier, not from folders.
Each platform source set uses the same packages as `commonMain`.

```
registration/   ← this device's registration with the backend
  PushRegistrationManager.kt / PushRegistrationManagerImpl.kt  ← public API + single owner of registration requests (createdAtStart)
  PushDeviceState.kt                 ← public state (Unknown/ThisDevice/OtherDevice/None)
  PushDeviceRegistrar.kt / PushDeviceRegistrarImpl.kt  ← register, send a Firebase Installation ID change, unregister, current state
  PushRegistrationStorage.kt         ← last sent Firebase Installation ID (android: AndroidPushRegistrationStorage, ios: IosPushRegistrationStorage)
  PushDeviceInfo.kt                  ← device name + platform (android: AndroidPushDeviceInfo, ios: IosPushDeviceInfo)
  UnregisterPushDeviceOnSignOut.kt   ← SignOutHook: unregister before the session is cleared
  DeviceUnregisteredHandler.kt       ← PushMessageHandler: DEVICE_UNREGISTERED → refreshState()
  PushFailureLogging.kt
  api/PushDeviceApi.kt               ← Ktorfit: PUT/PATCH/GET/DELETE api/v1/push-device
  api/PushDeviceResponses.kt         ← GET 204 → null, PATCH 409 → NotSlotHolder (PushDeviceUpdateResult.kt)
  api/RegistrationRetry.kt           ← registrationRetry(): the registration PUT's retry policy
message/        ← incoming pushes
  PushMessageHandler.kt, PushMessageDispatcher.kt, IncomingPushMessage.kt  ← public extension points + model
  PushMessageDispatcherImpl.kt       ← createdAtStart: parses messages, fans out to handlers and the messages flow
  IncomingPushMessageParser.kt
  (android: PushNotificationPresenter.kt — channel + foreground notifications)
permission/     ← OS notification permission
  NotificationPermission.kt          ← interface + expect rememberNotificationPermissionRequester
  NotificationPermissionStatus.kt
  (android: AndroidNotificationPermission.kt, NotificationPermissionPromptHistory.kt, NotificationPermissionRequester.android.kt
   ios: IosNotificationPermission.kt, NotificationPermissionRequester.ios.kt)
firebase/       ← Firebase Cloud Messaging SDK integration
  FirebaseInstallationIdProvider.kt, PushMessagingClient.kt, FirebaseInstallationIdUnavailableException.kt
  (android: TruckTrackMessagingService.kt, FirebasePushMessagingClient.kt, ReceivedFirebaseInstallationIdStorage.kt
   ios: IosPushEvents.kt — called from Swift through app:shared's IosPushBridge, IosPushMessagingClient.kt)
di/             ← pushModule + expect platformPushModule()
androidMain/AndroidManifest.xml, res/
```

## Tests

`src/commonTest` runs the real generated Ktorfit client against a `MockEngine` fake server (`PushTestEnvironment`, engine on the test scheduler so `runCurrent()` covers observer-driven requests; the client installs `core:network`'s `installOptInRequestRetry()` like production; the manager starts lazily so a test can complete a login before it subscribes).

- `PushRegistrationManagerTest` — a login completed before the manager starts registers exactly once; a Firebase Installation ID change isn't sent before this device registered, is `PATCH`ed to the last sent ID once, and a `409` stores it and never registers; a registration still failing after its retries is reported through `ErrorReporter`; only the registration `PUT` is retried (`GET`, `PATCH`, `DELETE` are sent once); signing out resets the state.
- `PushMessageDispatcherTest` — parsed messages reach the handler for their type and the `messages` flow; `DEVICE_UNREGISTERED` refreshes the state and never registers.
- `UnregisterPushDeviceOnSignOutTest` — the sign-out hook unregisters this device.
- `PushDeviceResponsesTest` — `204` → no device, a failed `GET` failing instead of reporting no device, `PATCH` `409` → not slot holder.

Run `.\gradlew.bat :core:push:testAndroidHostTest` from `app/`.

## Depends On

- `:core:common` — `Logger`, `ErrorReporter`, `ApiException`, `isTransientNetworkFailure`, `runCatchingCancellable`
- `:core:network` — `Ktorfit` on the authenticated `HttpClient` (retries opt-in per request)
- `:core:user` — `AuthManager` (`authenticationState`, `loginCompletions`), `SignOutHook`
- `com.momosi.trucktrack:shared` — `PushDeviceRegistrationDto`, `PushDeviceUpdateDto`, `PushDeviceDto`, `PushPlatformDto`, `PushMessageTypeDto`