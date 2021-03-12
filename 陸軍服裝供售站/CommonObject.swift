//
//  CommonObject.swift
//  陸軍服裝供售站
//
//  Created by 19003471Claud on 2021/1/19.
//  Copyright © 2021 19003471Claud. All rights reserved.
//

import Foundation
import UIKit
public class CommonURL {
    static let sharedInstance = CommonURL()
//    let Domain = "https://pxmart.brandstudio-ec.com/"
    let Domain = "https://pxmart2.brandstudio-ec.com/"
//    let Domain = "https://www.armymart.com.tw/"
    let DomainMain = "common/apphome"
    let DomainRegister = "account/phoneauth/register"
    let DomainVerify = "account/phoneauth/verify"
    let DomainWebVerify = "account/phoneauth/webverify"
    let DomainChangephone = "account/phoneauth/changephone"
    let checkstat = "api/phonesign/checkstat"
    let pos_sign = "api/phonesign/pos_sign"
    let pos_CancelSign = "api/phonesign/pos_CancelSign"
    let store_sign = "api/phonesign/store_sign"
    let appVersion = "api/app_version"
    let appSession = "api/app_session"
    let appleStoreUrl = "itms-apps://itunes.apple.com/app/id1548411228"
    
}

public class object {
    static let sharedInstance = object()

    let delegate = UIApplication.shared.delegate as! AppDelegate
    private let internalQueue = DispatchQueue(label: "SingletionInternalQueue", qos: .default, attributes: .concurrent)
    
    private var _currentNotificationInfo:notificationInfo? = nil
    var currentNotificationInfo:notificationInfo?{
        get {
            return internalQueue.sync {
                _currentNotificationInfo
            }
        }
        set(newValue) {
            // barrier flag => 告訴佇列，這個特定工作項目，必須在沒有其他平行執行的項目時執行
            internalQueue.async(flags: .barrier) {
                self._currentNotificationInfo = newValue
            }
        }
    }
    
    private var _deviceToken: String = ""
    var deviceToken: String {
        get {
            return internalQueue.sync {
                _deviceToken
            }
        }
        
        set(newValue) {
            // barrier flag => 告訴佇列，這個特定工作項目，必須在沒有其他平行執行的項目時執行
            internalQueue.async(flags: .barrier) {
                self._deviceToken = newValue
            }
        }
    }
    
    private var _isSignature = false
    var isSignature:Bool {
        get{
            return internalQueue.sync {
                _isSignature
            }
        }
        
        set (newVlaue){
            internalQueue.async(flags: .barrier) {
                self._isSignature = newVlaue
            }
        }
    }
}

public struct notificationInfo {
    var store_href:String?
    var action:notificationAction?
}

public enum notificationAction:String {
    case signature = "sign" //目前因為開app也會偵測是否要簽名故不用動作
}




