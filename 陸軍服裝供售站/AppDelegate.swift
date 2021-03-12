//
//  AppDelegate.swift
//  陸軍服裝供售站
//
//  Created by 19003471Claud on 2021/1/19.
//  Copyright © 2021 19003471Claud. All rights reserved.
//

import UIKit
import UserNotifications
import Firebase
@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    var shouldLandscape = false
    var window: UIWindow?
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        firebaseInit()
        registerForPushNotifications(application: application)
        requestAuthorization(application: application)
        Thread.sleep(forTimeInterval: 2.0)
        return true
    }

    // MARK: UISceneSession Lifecycle
    @available(iOS 13.0, *)
    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        // Called when a new scene session is being created.
        // Use this method to select a configuration to create the new scene with.
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }
    @available(iOS 13.0, *)
    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        // Called when the user discards a scene session.
        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
    }

    func application(_ application: UIApplication, supportedInterfaceOrientationsFor window: UIWindow?) -> UIInterfaceOrientationMask {
        if shouldLandscape {
            return UIInterfaceOrientationMask.landscape
        }
       return UIInterfaceOrientationMask.portrait
    }
    
    private func requestAuthorization(application: UIApplication){
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert ,.sound ,.badge ,.carPlay], completionHandler: {(granted, error) in
            if granted {
                print("允許")
            } else {
                print("不允許")
            }
            DispatchQueue.main.async {
                application.registerForRemoteNotifications()
            }
        })
    }
    
    private func firebaseInit(){
        FirebaseApp.configure()
        Messaging.messaging().delegate = self
    }

    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any]) {
        // Print full message.
        print(userInfo)
        object.sharedInstance.currentNotificationInfo?.store_href = userInfo["store_href"] as? String
        object.sharedInstance.currentNotificationInfo?.action = notificationAction.init(rawValue: userInfo["action"] as! String)
        NotificationCenter.default.post(name: .reloadWebview, object: nil)
    }
    
    func applicationWillEnterForeground(_ application: UIApplication) {
        removeBlackView(windows: window!)
    }
    
    func applicationDidEnterBackground(_ application: UIApplication) {
        ToBackground(windows: window!)
    }
}

extension AppDelegate:UNUserNotificationCenterDelegate {
    
    func registerForPushNotifications(application: UIApplication) {
        
        if #available(iOS 10, *) {
            print("我在iOS 10")
            UNUserNotificationCenter.current().delegate = self
            UNUserNotificationCenter.current().requestAuthorization(options:[.badge, .alert, .sound]){ (granted, error) in }
            application.registerForRemoteNotifications()
        }else if #available(iOS 9, *) {
            print("我在iOS 9")
            UIApplication.shared.registerUserNotificationSettings(UIUserNotificationSettings(types: [.badge, .sound, .alert], categories: nil))
            UIApplication.shared.registerForRemoteNotifications()
        }
    }
    
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        var tokenString = ""
        Messaging.messaging().apnsToken = deviceToken
        for byte in deviceToken {
           let hexString = String(format: "%02x", byte)
           tokenString += hexString
        }
        print(tokenString)
    }
    
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print(error)
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void){
        completionHandler([.alert, .badge, .sound])
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo
        object().currentNotificationInfo?.store_href = userInfo["store_href"] as? String
        object().currentNotificationInfo?.action = notificationAction.init(rawValue: userInfo["action"] as! String)
        NotificationCenter.default.post(name: .reloadWebview, object: nil,userInfo: userInfo)
        print(userInfo)
        
    }
    
}

extension AppDelegate: MessagingDelegate {
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        if let deviceToken = fcmToken {
            object.sharedInstance.deviceToken = deviceToken
        }
    }
}

