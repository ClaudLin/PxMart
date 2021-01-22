//
//  Extension.swift
//  陸軍服裝供售站
//
//  Created by 19003471Claud on 2021/1/19.
//  Copyright © 2021 19003471Claud. All rights reserved.
//

import Foundation
import UIKit
extension UIViewController {
    func getFrame()->CGRect{
        var result:CGRect?
        var v = self.view.bounds
        if #available(iOS 11.0, *) {
            v = self.view.safeAreaLayoutGuide.layoutFrame
        }
        result = v
        return result!
    }
}
extension SecKey {
    func toPemString() -> String {
        var error:Unmanaged<CFError>?
        if let cfdata = SecKeyCopyExternalRepresentation(self, &error) {
            let data:Data = cfdata as Data
            let b64Key = data.base64EncodedString()
            return b64Key
        }
        return ""
    }
}

extension String {
    
    func isJson() -> Bool{
        let jsonData = self.data(using: .utf8)
        do {
            try JSONSerialization.jsonObject(with: jsonData!, options: .mutableContainers)
            return true
        } catch {
            return false
        }
    }
    
    func urlEncoded() -> String {
        let encodeUrlString = self.addingPercentEncoding(withAllowedCharacters:
            .urlQueryAllowed)
        return encodeUrlString ?? ""
    }
    
    func urlDecoded() -> String {
        return self.removingPercentEncoding ?? ""
    }
    
    func base64Encoding() -> String{
        let plainData = self.data(using:String.Encoding.utf8)

        let base64String = plainData?.base64EncodedString(options:NSData.Base64EncodingOptions.init(rawValue: 0))

        return base64String!
    }
    
    func base64Decoding() -> String{
        let decodedData = NSData(base64Encoded: self, options:NSData.Base64DecodingOptions.init(rawValue: 0))

        let decodedString = NSString(data: decodedData!as Data, encoding:String.Encoding.utf8.rawValue)!as String

        return decodedString
    }
    
    func convertToTimeInterval() -> TimeInterval {
        guard self != "" else {
            return 0
        }

        var interval:Double = 0

        let parts = self.components(separatedBy: ":")
        for (index, part) in parts.reversed().enumerated() {
            interval += (Double(part) ?? 0) * pow(Double(60), Double(index))
        }

        return interval
    }
}

extension UIDevice {
    static let modelName: String = {
        var systemInfo = utsname()
        uname(&systemInfo)
        let machineMirror = Mirror(reflecting: systemInfo.machine)
        let identifier = machineMirror.children.reduce("") { identifier, element in
            guard let value = element.value as? Int8, value != 0 else { return identifier }
            return identifier + String(UnicodeScalar(UInt8(value)))
        }
        return identifier
    }()
}

extension NSObject {
    var className: String {
        return String(describing: type(of: self))
    }
    
    class var className: String {
        return String(describing: self)
    }
}

extension CGRect {
    var minEdge: CGFloat {
        return min(width, height)
    }
}

extension UIScreen {
    var minEdge: CGFloat {
        return UIScreen.main.bounds.minEdge
    }
}

extension UIView {

    @discardableResult
    func fromNib<T : UIView>() -> T? {
        guard let contentView = Bundle(for: type(of: self)).loadNibNamed(type(of: self).className, owner: self, options: nil)?.first as? T else {
            return nil
        }
        addSubview(contentView)
        contentView.fillSuperview()
        return contentView
    }
}
