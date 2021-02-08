//
//  SignatureVC.swift
//  陸軍服裝供售站
//
//  Created by 19003471Claud on 2021/1/19.
//  Copyright © 2021 19003471Claud. All rights reserved.
//

import UIKit

class SignatureVC: UIViewController {
    
    private var submitButton:UIButton?
    private var cancelButton:UIButton?
    private var clearButton:UIButton?
    private var signatureView:DrawSignatureView?
    
    var TP:String?
    var cd:String?
    var OrderNo:String?
    
    private enum singnatureResult:String {
        case finish = "1"
        case cancel = "2"
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        UIInit()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        object().delegate.shouldLandscape = false
        UIDevice.current.setValue(Int(UIInterfaceOrientation.portrait.rawValue), forKey: "orientation")
    }
        
    func UIInit(){
        view.backgroundColor = .white
        object().delegate.shouldLandscape = true
        let value = UIInterfaceOrientation.landscapeLeft.rawValue
        UIDevice.current.setValue(value, forKey: "orientation")
        signatureView = DrawSignatureView(frame: CGRect(x: 0, y: 0, width: getFrame().height, height: getFrame().width * 0.8))
        signatureView?.delegate = self
        let space:CGFloat = 150
        let buttonWidth:CGFloat = getFrame().width/4
        let buttonHeight:CGFloat = 60
        let clearButtonX:CGFloat = getFrame().height/2 - buttonWidth/2
        let submitButtonX:CGFloat = clearButtonX + space
        let cancelButtonX:CGFloat = clearButtonX - space
        clearButton = UIButton(frame: CGRect(x: clearButtonX, y: (signatureView?.frame.maxY)!, width: buttonWidth, height: buttonHeight))
        clearButton?.tag = 2
        clearButton?.setImage(UIImage(named: "clearButton"), for: .normal)
        clearButton?.addTarget(self, action: #selector(buttonAction(button:)), for: .touchUpInside)
        clearButton?.isEnabled = false
        
        submitButton = UIButton(frame: CGRect(x: submitButtonX , y: (clearButton?.frame.minY)!, width: buttonWidth, height: buttonHeight))
        submitButton?.addTarget(self, action: #selector(buttonAction(button:)), for: .touchUpInside)
        submitButton?.tag = 0
        submitButton?.isEnabled = false
        submitButton?.setImage(UIImage(named: "submitButton"), for: .normal)
        
        cancelButton = UIButton(frame: CGRect(x: cancelButtonX, y: (clearButton?.frame.minY)!, width: buttonWidth, height: buttonHeight))
        cancelButton?.tag = 1
        cancelButton?.setImage(UIImage(named: "cancelButton"), for: .normal)
        cancelButton?.addTarget(self, action: #selector(buttonAction(button:)), for: .touchUpInside)
        

        
        view.addSubview(signatureView!)
        view.addSubview(clearButton!)
        view.addSubview(cancelButton!)
        view.addSubview(submitButton!)
    
    }
    
    @objc func buttonAction(button:UIButton){
        let tag = button.tag
        switch tag {
        case 0:
            let image = signatureView?.getSignature()
            upload(signatureImage: image!, result: .finish)
        case 1:
            upload(signatureImage: nil, result: .cancel)
            self.dismiss(animated: true, completion: nil)
        case 2:
            signatureView?.clearSignature()
            submitButton?.isEnabled = false
            clearButton?.isEnabled = false
        default:
            break
        }
    }
    
    private func upload(signatureImage:UIImage? ,result:singnatureResult){
        let param:[String:Any] =
        [
            "cd":cd!,
            "OrderNo":OrderNo!,
            "Status":result.rawValue,
            "sf":signatureImage?.jpegData(compressionQuality: 0.7)!
        ]
         
        var postURL = ""
        if TP == "1"{
            switch result {
            case .finish:
                postURL = "\(CommonURL.sharedInstance.Domain)\(CommonURL.sharedInstance.pos_sign)"
            case .cancel:
                postURL = "\(CommonURL.sharedInstance.Domain)\(CommonURL.sharedInstance.pos_CancelSign)"
            }
        }else if TP == "2"{
            postURL = "\(CommonURL.sharedInstance.Domain)\(CommonURL.sharedInstance.store_sign)"
        }
        
        alamofireUploadPost(postURL: postURL, param: param, imageParam: nil, completion: {(str) in
            self.dismiss(animated: true, completion: nil)
        })
        
    }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

}

extension SignatureVC : DrawSignatureDelegate {
    
    func signatureStart() {
        submitButton?.isEnabled = true
        clearButton?.isEnabled = true
    }
    
    
    
}
