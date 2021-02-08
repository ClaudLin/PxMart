//
//  RSAUtils.swift
//  陸軍服裝供售站
//
//  Created by 19003471Claud on 2021/1/19.
//  Copyright © 2021 19003471Claud. All rights reserved.
//

import Foundation
import Security
import SwiftyRSA
import SwCrypt

enum RSAKeySize: Int {
    case size512 = 512
    case size768 = 768
    case size1024 = 1024
    case size2048 = 2048
}

public enum RSAKeyOfKeyChain:String {
    case PublicKey = "PublicKeyPem"
    case PrivateKey = "PrivateKeyPem"
}

private let publicKeyIdentifier = "baseModule.publicKey"
private let privateKeyIdentifier = "baseModule.privateKey"
private let publicKeyTag = publicKeyIdentifier.data(using: .utf8)!
private let privateKeyTag = privateKeyIdentifier.data(using: .utf8)!


public class RSAUtils {

    static let shared = RSAUtils()
    private let keySizeType: RSAKeySize = .size2048
    
    // RSA秘鍵を生成する
    // sizes for RSA keys are: 512, 768, 1024, 2048.
    public func generateRSAKeyPair(ReCreate:Bool) {
        if ReCreate {
            deleteRSAKey()
        }
        let publickeyPem = keyChainReadData(identifier: RSAKeyOfKeyChain.PublicKey.rawValue) as? String ?? nil
        if publickeyPem == nil {
            let (privateKey, publicKey) = try! CC.RSA.generateKeyPair(1024)
            let privateKeyPEM = try SwKeyConvert.PrivateKey.derToPKCS1PEM(privateKey)
            let publicKeyPEM = SwKeyConvert.PublicKey.derToPKCS8PEM(publicKey)
            let publicKeyResult = keyChainSaveData(data: publicKeyPEM, withIdentifier: RSAKeyOfKeyChain.PublicKey.rawValue)
            let privateKeyResult = keyChainSaveData(data: privateKeyPEM, withIdentifier: RSAKeyOfKeyChain.PrivateKey.rawValue)
            print("publicKeyResult \(publicKeyResult)")
            print("privateKeyResult \(privateKeyResult)")
        }
    }
    
    public func getPemString(seckeyType:RSAKeyOfKeyChain) -> String {
        return keyChainReadData(identifier: seckeyType.rawValue) as! String
    }
    
    private func deleteRSAKey(){
        keyChianDelete(identifier: RSAKeyOfKeyChain.PrivateKey.rawValue)
        keyChianDelete(identifier: RSAKeyOfKeyChain.PublicKey.rawValue)
    }

    
    public func signature(str:String ,privateKey:PrivateKey) -> String {
        let clear = try! ClearMessage(string: str, using: .utf8)
        let signature = try! clear.signed(with: privateKey, digestType: .sha1)
        let base64String = signature.base64String
        return base64String
    }
    
    public func getRSAKey() -> (PublicKey, PrivateKey) {
        let publickey = try! PublicKey(pemEncoded: RSAUtils().getPemString(seckeyType: .PublicKey))
        let privateKey = try! PrivateKey(pemEncoded: RSAUtils().getPemString(seckeyType: .PrivateKey))
        return (publickey,privateKey)
    }
    
    public func encrypt(source:String) -> String{
        let (publicKey ,_) = getRSAKey()
        let clear = try! ClearMessage(string: source , using: .utf8)
        let encrypted = try! clear.encrypted(with: publicKey, padding: .PKCS1)
        return encrypted.base64String
    }
    
    public func decrypt(encryptStr:String) -> String{
        let (_ ,privatekey) = getRSAKey()
        let encrypted = try! EncryptedMessage(base64Encoded: encryptStr)
        let clear = try! encrypted.decrypted(with: privatekey, padding: .PKCS1)
        let string = try! clear.string(encoding: .utf8)
        return string
    }
}
