//
//  ViewController.swift
//  陸軍服裝供售站
//
//  Created by 19003471Claud on 2021/1/19.
//  Copyright © 2021 19003471Claud. All rights reserved.
//

import UIKit
import WebKit
import SwiftyRSA
class ViewController: UIViewController {
    private var CDKey = "CDKey"
    private var WKWebview:WKWebView?
    private var isNeedPost = false
    private var currentPostType:PostType?
    private var cd = ""
    private var timer:Timer?
    private var detectIsQRCode = false
    
    private struct checkstatValue {
        var TP:String? = ""
        var Status:SignatureStatus?
        var OrderNo:String? = ""
        var delayTime:TimeInterval = 0
        var cd:String? = ""
    }
    
    private enum SignatureStatus:String{
        case keep = "1"
        case startSign = "2"
        case stop = "3"
    }
    
    private enum PostType {
        case Register
        case ChangePhone
        case Verify
        case WebVerify
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        notificationInit()
        RSAUtils().generateRSAKeyPair(ReCreate: false)
        UIInit()
        // Do any additional setup after loading the view.
    }
    
    private func notificationInit(){
        let notificationCenter = NotificationCenter.default
        //從背景回來監聽
        notificationCenter.addObserver(self, selector: #selector(appMovedToForeground) ,name:UIApplication.willEnterForegroundNotification, object: nil)
        //webview reload
        notificationCenter.addObserver(self, selector: #selector(reloadWebviewAction(noti:)), name: .reloadWebview , object: nil)
        //監聽畫面截圖
        notificationCenter.addObserver(self, selector: #selector(screenshots), name: UIApplication.userDidTakeScreenshotNotification, object: nil)
    }
    
    @objc private func screenshots(){
        Alert(title: "截圖內可能包含個人資訊，敬請妥善保管確保使用安全", message: "", action: nil)
    }
    
    @objc func reloadWebviewAction(noti:Notification){
        DispatchQueue.main.async {
            let useInfo = noti.userInfo
            self.WKWebview!.load(URLRequest(url: URL(string: useInfo!["store_href"] as! String)!))
        }
    }
    
    @objc func appMovedToForeground() {
        if judgmeny() {
            DispatchQueue.main.async {
                self.closeAppAction()
            }
        }else {
            detectAction()
        }
    }
    
    private func addGesture(){
        let swipeRight = UISwipeGestureRecognizer( target:self, action:#selector(swipeBack(recognizer:)))
        swipeRight.delegate = self
        swipeRight.direction = .right
        swipeRight.numberOfTouchesRequired = 1
        WKWebview!.addGestureRecognizer(swipeRight)
        
        let swipeLeft = UISwipeGestureRecognizer(target: self, action: #selector(swipeFoward(recognizer:)))
        swipeLeft.delegate = self
        swipeLeft.direction = .left
        swipeLeft.numberOfTouchesRequired = 1
        WKWebview!.addGestureRecognizer(swipeLeft)
    }
    
    @objc func swipeBack(recognizer:UISwipeGestureRecognizer) {
        if WKWebview?.canGoBack != nil {
            WKWebview?.goBack()
        }
    }
    
    @objc func swipeFoward(recognizer:UISwipeGestureRecognizer) {
        if WKWebview?.canGoForward != nil {
            WKWebview?.goForward()
        }
    }
    
    private func closeAppAction(){
        Alert(title: "您的手機有越獄或是模擬器器開啟app", message: "即將關閉", action: {
            exit(2)
        })
    }
    
    private func detectAction(){
        if let cd = keyChainReadData(identifier: CDKey) as? String {
            let decryptCd = RSAUtils().decrypt(encryptStr: cd)
            poolingSGTime(cd: decryptCd, delayTime: 0, postURL: "\(CommonURL.sharedInstance.Domain)\(CommonURL.sharedInstance.checkstat)")
        }
    }
    
    private func poolingSGTime(cd:String ,delayTime:TimeInterval ,postURL:String){
        let param:[String:String] =
            [
                "cd":cd,
                "sg":"1"
            ]
        alamofirePost(postURL: postURL, param: param, completion: { data in
            guard let dic = data else {return}
            let checkstat = self.dicToCheckstat(dic: dic)
            self.afterResponseAction(checkstat: checkstat)
        })
    }

    private func dicToCheckstat(dic:[String:Any]) -> checkstatValue {
        var checkstat = checkstatValue()
        checkstat.TP = dic["TP"] as? String
        checkstat.OrderNo = dic["OrderNo"] as? String
        checkstat.delayTime = dic["Timer"] as! TimeInterval
        checkstat.cd = dic["cd"] as? String
        checkstat.Status = SignatureStatus(rawValue: (dic["Status"] as? String)!)
        return checkstat
    }
    
    private func afterResponseAction(checkstat:checkstatValue){
        let status = checkstat.Status
        switch status {
        case .keep:
            poolingTime(deplayTime: checkstat.delayTime)
        case .startSign:
            DispatchQueue.main.async {
                let vc = SignatureVC()
                vc.OrderNo = checkstat.OrderNo
                vc.TP = checkstat.TP
                vc.cd = checkstat.cd
                self.present(vc, animated: true, completion: nil)
            }
        case .stop:
            timer?.invalidate()
            print("stop pooling")
        case .none:
            break
        }
    }
    
    private func UIInit(){
        WKWebview = wkwebInit(VC: self)
        WKWebview!.uiDelegate = self
        WKWebview!.navigationDelegate = self
        view.addSubview(WKWebview!)
        addGesture()
        if object().currentNotificationInfo?.store_href == nil || object().currentNotificationInfo?.store_href == "" {
            WKWebview!.load(URLRequest(url: URL(string: "\(CommonURL.sharedInstance.Domain)\(CommonURL.sharedInstance.DomainMain)")!))
//            WKWebview!.load(URLRequest(url: URL(string: "https://tw.yahoo.com")!))
        }else {
            WKWebview!.load(URLRequest(url: URL(string: (object().currentNotificationInfo?.store_href)!)!))
        }
        
    }
    
    
    
    private func loadPostHtml(type:PostType ,urlString:String){
        cd = (getInfo(urlStr:urlString , type: .account)?.urlEncoded().uppercased())!
        isNeedPost = true
        currentPostType = type
        let request = Bundle.main.url(forResource: "JSPOST",withExtension: "html")!
        WKWebview!.load(URLRequest(url: request))
    }
    
    private func poolingTime(deplayTime:TimeInterval){
        if deplayTime > 0 {
            if detectIsQRCode {
                detectIsQRCode = false
                WKWebview?.load(URLRequest(url: URL(string: "\(CommonURL.sharedInstance.Domain)\(CommonURL.sharedInstance.DomainMain)")!))
            }
            timer = Timer.scheduledTimer(timeInterval: deplayTime, target: self, selector: #selector(poolingAction), userInfo: self, repeats: false)
        }
    }
    
    @objc private func poolingAction(){
        let postURL = "\(CommonURL.sharedInstance.Domain)\(CommonURL.sharedInstance.checkstat)"
        let param:[String:String] =
        [
            "cd":cd
        ]
        alamofirePost(postURL: postURL, param: param, completion: { dic in
            let checkstat = self.dicToCheckstat(dic: dic!)
            self.afterResponseAction(checkstat: checkstat)
        })
    }
    
    private func postAction(type:PostType){
        let device = "ios"
        var postData = ""
        var pc = RSAUtils().getPemString(seckeyType: .PublicKey).urlEncoded()
        var (_ ,privateKey) = RSAUtils().getRSAKey()
        let bundleIdentifier =  Bundle.main.bundleIdentifier!
        let uuid = getUniqueDeviceIdentifierAsString()
        let deviceID = UIDevice.modelName
        let daValue = "\(String(describing: bundleIdentifier))\(String(describing: uuid))\(deviceID)"
        var da = RSAUtils().signature(str: daValue, privateKey: privateKey).urlEncoded()
        let ds = daValue.base64Encoding().urlEncoded()
        var postURL:String? = nil
        switch type {
        case .Register:
//            let result = keyChainSaveData(data: RSAUtils().encrypt(source: cd), withIdentifier: CDKey)
//            print(result)
//            postURL = "\(CommonURL.sharedInstance.Domain)\(CommonURL.sharedInstance.DomainRegister)"
//            postData =  "\"cd\":\"\(String(describing: cd))\",\"da\":\"\(da)\",\"device\":\"\(device)\",\"pc\":\"\(pc)\",\"dt\":\"\(object.sharedInstance.deviceToken)\""
            postURL = "\(CommonURL.sharedInstance.Domain)\(CommonURL.sharedInstance.DomainRegister)"
            RSAUtils().generateRSAKeyPair(ReCreate: true)
            privateKey = try! PrivateKey(pemEncoded: RSAUtils().getPemString(seckeyType: .PrivateKey))
            let result = keyChainSaveData(data: RSAUtils().encrypt(source: cd), withIdentifier: CDKey)
            print(result)
            da = RSAUtils().signature(str: daValue, privateKey: privateKey).urlEncoded()
            pc = RSAUtils().getPemString(seckeyType: .PublicKey).urlEncoded()
            postData =  "\"cd\":\"\(String(describing: cd))\",\"da\":\"\(da)\",\"device\":\"\(device)\",\"pc\":\"\(pc)\",\"dt\":\"\(object.sharedInstance.deviceToken)\""
        case .Verify:
            let result = keyChainSaveData(data: RSAUtils().encrypt(source: cd), withIdentifier: CDKey)
            print(result)
            postURL = "\(CommonURL.sharedInstance.Domain)\(CommonURL.sharedInstance.DomainVerify)"
            postData =  "\"cd\":\"\(String(describing: cd))\",\"da\":\"\(da)\",\"device\":\"\(device)\",\"ds\":\"\(ds)\""
        case .ChangePhone:
            postURL = "\(CommonURL.sharedInstance.Domain)\(CommonURL.sharedInstance.DomainChangephone)"
            RSAUtils().generateRSAKeyPair(ReCreate: true)
            privateKey = try! PrivateKey(pemEncoded: RSAUtils().getPemString(seckeyType: .PrivateKey))
            let result = keyChainSaveData(data: RSAUtils().encrypt(source: cd), withIdentifier: CDKey)
            print(result)
            da = RSAUtils().signature(str: daValue, privateKey: privateKey).urlEncoded()
            pc = RSAUtils().getPemString(seckeyType: .PublicKey).urlEncoded()
            postData =  "\"cd\":\"\(String(describing: cd))\",\"da\":\"\(da)\",\"device\":\"\(device)\",\"pc\":\"\(pc)\",\"dt\":\"\(object.sharedInstance.deviceToken)\""
        case .WebVerify:
            let result = keyChainSaveData(data: RSAUtils().encrypt(source: cd), withIdentifier: CDKey)
            print(result)
            postURL = "\(CommonURL.sharedInstance.Domain)\(CommonURL.sharedInstance.DomainWebVerify)"
            postData =  "\"cd\":\"\(String(describing: cd))\",\"da\":\"\(da)\",\"device\":\"\(device)\",\"ds\":\"\(daValue)\""

        }
        webviewPost(postStr: postData, postURL: postURL!)
        isNeedPost = false
        currentPostType = nil
    }
    
    private func webviewPost(postStr:String ,postURL:String){
        let jscript = "post('\(postURL)', {\(postStr)});"
        WKWebview?.evaluateJavaScript(jscript, completionHandler: {(object, error) in
            if error != nil{
                print("js發生問題 \(error!)")
            }else{
                if object != nil{
                    print(object!)
                    self.currentPostType = nil
                }else{
                    print("object is nil")
                }
            }
        })
    }
    
    private func judgmeny() -> Bool {
        return Platform.isSimulator || isJailBroken()
    }
}
extension ViewController : WKUIDelegate ,WKNavigationDelegate {
    
    func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
        if let urlStr = webView.url?.absoluteString{
          print(urlStr)
        }
    }
    
//    func webView(_ webView: WKWebView,didReceive challenge: URLAuthenticationChallenge,completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void){
////        let authMethod = challenge.protectionSpace.authenticationMethod
////        let host = challenge.protectionSpace.host
////        guard authMethod == NSURLAuthenticationMethodServerTrust,host == CommonURL().Domain else {
////            return completionHandler(.cancelAuthenticationChallenge, nil)
////        }
//        let serverTrust = challenge.protectionSpace.serverTrust
//        let certificate = SecTrustGetCertificateAtIndex(serverTrust!, 0)
//
//        let policies = NSMutableArray()
//        policies.add(SecPolicyCreateSSL(true, challenge.protectionSpace.host as CFString))
//        SecTrustSetPolicies(serverTrust!, policies)
//
//        let remoteCertificateData:NSData = SecCertificateCopyData(certificate!)
//        if let pathToCert = Bundle.main.path(forResource: "serverForssl", ofType: "cer"){
//            let localCertificate = NSData(contentsOfFile: pathToCert)
//
//            if(remoteCertificateData.isEqual(localCertificate! as Data)){
//                let credentail:URLCredential = URLCredential(trust: serverTrust!)
//                completionHandler(.useCredential,credentail)
//            }else{
//                print("protectionSpace \(challenge.protectionSpace)")
//                print("連線失敗 權限不足")
//                completionHandler(.cancelAuthenticationChallenge,nil)
//            }
//        }else{
//            Alert(title: "讀取cer檔失敗", message: "", action: nil)
//        }
//    }
    
    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        if let urlStr = webView.url?.absoluteString{
          print(urlStr)
        }
        if isNeedPost {
            switch currentPostType {
            case .Register:
                postAction(type: currentPostType!)
            case .Verify:
                postAction(type: currentPostType!)
            case .ChangePhone:
                postAction(type: currentPostType!)
            case .WebVerify:
                break
            case .none:
                break
            }
        }
        
    }
    
    func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        if let urlStr = navigationAction.request.url?.absoluteString{
            if urlStr.contains("/account/phoneauth/go/register") {
                loadPostHtml(type: .Register, urlString: urlStr)
            }else if urlStr.contains("/account/phoneauth/go/verify"){
                loadPostHtml(type: .Verify, urlString: urlStr)
            }else if urlStr.contains("/account/phoneauth/go/changephone"){
                loadPostHtml(type: .ChangePhone, urlString: urlStr)
            }else if urlStr.contains("/account/phoneauth/go/webverify"){
                loadPostHtml(type: .WebVerify, urlString: urlStr)
            }else if urlStr.contains("/account/logout"){
                let result = keyChainUpdata(data: "", withIdentifier: CDKey)
                print(result)
            }else if urlStr.contains("/account/order/info/go") || urlStr.contains("/account/login/go"){
                detectAction()
            }else if urlStr.contains("/api/phonesign/go/checkstat/"){
                WKWebview?.load(URLRequest(url: URL(string: CommonURL.sharedInstance.Domain)!))
                cd = getInfo(urlStr: urlStr, type: .account)!
                let delayTime:TimeInterval = (getInfo(urlStr: urlStr, type: .time)?.convertToTimeInterval())!
                poolingTime(deplayTime: delayTime)
            }else if urlStr.contains("/account/order/info/go"){
                detectIsQRCode = true
            }
        }
        decisionHandler(.allow)
    }
}

extension UIViewController:UIGestureRecognizerDelegate {
    private func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
        return true
    }
}
