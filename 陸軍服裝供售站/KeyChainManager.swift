//
//  KeyChainManager.swift
//  陸軍服裝供售站
//
//  Created by 19003471Claud on 2021/1/19.
//  Copyright © 2021 19003471Claud. All rights reserved.
//

import Foundation


// TODO: 创建查询条件
public func createQuaryMutableDictionary(identifier:String)->NSMutableDictionary{
    // 创建一个条件字典
    let keychainQuaryMutableDictionary = NSMutableDictionary.init(capacity: 0)
    // 设置条件存储的类型
    keychainQuaryMutableDictionary.setValue(kSecClassGenericPassword, forKey: kSecClass as String)
    // 设置存储数据的标记
    keychainQuaryMutableDictionary.setValue(identifier, forKey: kSecAttrService as String)
    keychainQuaryMutableDictionary.setValue(identifier, forKey: kSecAttrAccount as String)
    // 设置数据访问属性
    keychainQuaryMutableDictionary.setValue(kSecAttrAccessibleAfterFirstUnlock, forKey: kSecAttrAccessible as String)
    // 返回创建条件字典
    return keychainQuaryMutableDictionary
}

// TODO: 存储数据
public func keyChainSaveData(data:Any ,withIdentifier identifier:String)->Bool {
    // 获取存储数据的条件
    let keyChainSaveMutableDictionary = createQuaryMutableDictionary(identifier: identifier)
    // 删除旧的存储数据
    SecItemDelete(keyChainSaveMutableDictionary)
    // 设置数据
    keyChainSaveMutableDictionary.setValue(NSKeyedArchiver.archivedData(withRootObject: data), forKey: kSecValueData as String)
    // 进行存储数据
    let saveState = SecItemAdd(keyChainSaveMutableDictionary, nil)
    if saveState == noErr  {
        return true
    }
    return false
}

// TODO: 更新数据
public func keyChainUpdata(data:Any ,withIdentifier identifier:String)->Bool {
    // 获取更新的条件
    let keyChainUpdataMutableDictionary = createQuaryMutableDictionary(identifier: identifier)
    // 创建数据存储字典
    let updataMutableDictionary = NSMutableDictionary.init(capacity: 0)
    // 设置数据
    updataMutableDictionary.setValue(NSKeyedArchiver.archivedData(withRootObject: data), forKey: kSecValueData as String)
    // 更新数据
    let updataStatus = SecItemUpdate(keyChainUpdataMutableDictionary, updataMutableDictionary)
    if updataStatus == noErr {
        return true
    }
    return false
}


// TODO: 获取数据
public func keyChainReadData(identifier:String)-> Any {
    var idObject:Any?
    // 获取查询条件
    let keyChainReadmutableDictionary = createQuaryMutableDictionary(identifier: identifier)
    // 提供查询数据的两个必要参数
    keyChainReadmutableDictionary.setValue(kCFBooleanTrue, forKey: kSecReturnData as String)
    keyChainReadmutableDictionary.setValue(kSecMatchLimitOne, forKey: kSecMatchLimit as String)
    // 创建获取数据的引用
    var queryResult: AnyObject?
    // 通过查询是否存储在数据
    let readStatus = withUnsafeMutablePointer(to: &queryResult) { SecItemCopyMatching(keyChainReadmutableDictionary, UnsafeMutablePointer($0))}
    if readStatus == errSecSuccess {
        if let data = queryResult as! NSData? {
            idObject = NSKeyedUnarchiver.unarchiveObject(with: data as Data) as Any
        }
    }
    return idObject as Any
}



// TODO: 删除数据
public func keyChianDelete(identifier:String) {
    // 获取删除的条件
    let keyChainDeleteMutableDictionary = createQuaryMutableDictionary(identifier: identifier)
    // 删除数据
    SecItemDelete(keyChainDeleteMutableDictionary)
}
