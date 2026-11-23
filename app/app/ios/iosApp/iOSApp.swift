import DeviceKit
import FirebaseMessaging
import Shared
import SwiftUI
import UIKit
import UserNotifications

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    init() {
        #if DEBUG
        let isDebug = true
        #else
        let isDebug = false
        #endif
        IosPushBridge.shared.deviceName = Self.deviceName
        IosAppInitializerKt.bootstrapIosApp(
            isDebug: isDebug,
            apiBaseUrl: Self.infoValue("TTApiBaseUrl"),
            realmUrl: Self.infoValue("TTRealmUrl"),
            oauthClientId: Self.infoValue("TTOAuthClientId"),
            appScheme: Self.infoValue("TTAppScheme")
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { activity in
                    guard let url = activity.webpageURL else { return }
                    IosAppInitializerKt.openIosAppLink(url: url.absoluteString)
                }
        }
    }

    private static var deviceName: String {
        let device = Device.current.realDevice
        if case .unknown = device {
            return UIDevice.current.model
        }
        return device.description
    }

    private static func infoValue(_ key: String) -> String {
        guard let value = Bundle.main.object(forInfoDictionaryKey: key) as? String, !value.isEmpty else {
            fatalError("Missing \(key) in Info.plist")
        }
        return value
    }
}

final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        IosPushBridge.shared.notificationSettingsUrl = Self.notificationSettingsUrl
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        application.registerForRemoteNotifications()
        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }

    func messaging(_ messaging: Messaging, didReceiveRegistration firebaseInstallationId: String?) {
        guard let firebaseInstallationId = firebaseInstallationId else { return }
        IosPushBridge.shared.onFirebaseInstallationIdReceived(firebaseInstallationId: firebaseInstallationId)
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        IosPushBridge.shared.onMessageReceived(data: Self.stringValues(of: notification.request.content.userInfo))
        completionHandler([.banner, .sound, .list])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        completionHandler()
    }

    private static var notificationSettingsUrl: String {
        if #available(iOS 16.0, *) {
            return UIApplication.openNotificationSettingsURLString
        }
        return UIApplication.openSettingsURLString
    }

    private static func stringValues(of userInfo: [AnyHashable: Any]) -> [String: String] {
        userInfo.reduce(into: [String: String]()) { values, entry in
            if let key = entry.key as? String, let value = entry.value as? String {
                values[key] = value
            }
        }
    }
}