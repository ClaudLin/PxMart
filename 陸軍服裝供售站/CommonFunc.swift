//
//  CommonFunc.swift
//  陸軍服裝供售站
//
//  Created by 19003471Claud on 2021/1/19.
//  Copyright © 2021 19003471Claud. All rights reserved.
//

import Foundation
import UIKit
struct Platform {
    static let isSimulator: Bool = {
        var isSim = false
        #if arch(i386) || arch(x86_64)
            isSim = true
        #endif
        return isSim
    }()
}

public enum InfoType:Int {
    case account = 1
    case time = 2
}

public func isJailBroken() -> Bool {
    let apps = ["/APPlications/Cydia.app","/APPlications/limera1n.app","/APPlications/greenpois0n.app","/APPlications/blackra1n.app","/APPlications/blacksn0w.app","/APPlications/redsn0w.app","/APPlications/Absinthe.app"]
    for app in apps {
        if FileManager.default.fileExists(atPath: app){
            return true
        }
    }
    return false
}

public func getInfo(urlStr:String ,type:InfoType) -> String? {
    var info:String? = nil
    let array = urlStr.split(separator: "/")
    switch type {
    case .account:
        info = String(array[array.count - type.rawValue]) 
    case .time:
        info = String(array [array.count - type.rawValue])

    }
    return info
}


public func getUniqueDeviceIdentifierAsString() -> String {
    let appName: String? = (Bundle.main.infoDictionary?[((kCFBundleNameKey as String?)!)] as? String)
    var strApplicationUUID: String? = keyChainReadData(identifier: "incoding") as? String ?? nil
    if strApplicationUUID == nil {
        strApplicationUUID = UIDevice.current.identifierForVendor!.uuidString
        let saveResult = keyChainSaveData(data: appName!, withIdentifier: "incoding")
        print(saveResult)
    }
    return strApplicationUUID!
}

public func ToBackground(windows:UIWindow){
    
    if var rootVC = UIApplication.shared.keyWindow?.rootViewController {
        while let nextVC = rootVC.presentedViewController {
            rootVC = nextVC
        }
        let enterBackgroundVC = EnterBackgroundVC()
        enterBackgroundVC.modalPresentationStyle = .fullScreen
        rootVC.present(enterBackgroundVC, animated: false, completion: nil)
    }
    
}

public func removeBlackView(windows:UIWindow){
    if var rootVC = UIApplication.shared.keyWindow?.rootViewController {
        while let nextVC = rootVC.presentedViewController {
            rootVC = nextVC
        }
        if rootVC.isKind(of: EnterBackgroundVC.self){
            rootVC.dismiss(animated: false, completion: nil)
        }
    }
}
