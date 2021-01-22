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

//    let Domain = "https://pxmart.brandstudio-ec.com/"
    let Domain = "https://pxmart2.brandstudio-ec.com/"
//    let Domain = "https://www.armymart.com.tw/"
    let DomainRegister = "account/phoneauth/register"
    let DomainVerify = "account/phoneauth/verify"
    let DomainWebVerify = "account/phoneauth/webverify"
    let DomainChangephone = "account/phoneauth/changephone"
    let checkstat = "api/phonesign/checkstat"
    let pos_sign = "api/phonesign/pos_sign"
    let pos_CancelSign = "api/phonesign/pos_CancelSign"
    let store_sign = "api/phonesign/store_sign"
}

public class object {
    let delegate = UIApplication.shared.delegate as! AppDelegate
}



