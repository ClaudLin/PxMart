//
//  WkWebviewMana.swift
//  陸軍服裝供售站
//
//  Created by 19003471Claud on 2021/1/19.
//  Copyright © 2021 19003471Claud. All rights reserved.
//

import Foundation
import WebKit


public func wkwebInit(VC:UIViewController) -> WKWebView{
    let preferences = WKPreferences()
    preferences.javaScriptEnabled = true
    let webConfiguration = WKWebViewConfiguration()
    webConfiguration.preferences = preferences
    webConfiguration.selectionGranularity = WKSelectionGranularity.character
    webConfiguration.userContentController = WKUserContentController()
    let webview = WKWebView(frame: VC.getFrame(),configuration: webConfiguration)
    let path = Bundle.main.path(forResource: "JSPOST", ofType: "html")
    let html = try? String(contentsOfFile: path!, encoding: .utf8)
    if html != nil {
        webview.loadHTMLString(html!, baseURL: Bundle.main.bundleURL)
    }
    return webview
}
