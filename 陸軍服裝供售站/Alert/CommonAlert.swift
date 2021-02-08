//
//  CommonAlert.swift
//  陸軍服裝供售站
//
//  Created by 19003471Claud on 2021/1/22.
//  Copyright © 2021 19003471Claud. All rights reserved.
//

import Foundation
import UIKit
import SwiftEntryKit

public func Alert(title:String ,message:String , action: (()->())?){
    let displayMode = EKAttributes.DisplayMode.inferred
    let attributes = PresetsDataSource()[3,4].attributes
    let title = EKProperty.LabelContent(
        text: title,
        style: .init(
            font: MainFont.medium.with(size: 15),
            color: .black,
            alignment: .center,
            displayMode: displayMode
        )
    )
    
    let description = EKProperty.LabelContent(
        text: message,
        style: .init(
            font: MainFont.light.with(size: 13),
            color: .black,
            alignment: .center,
            displayMode: displayMode
        )
    )
    
    let simpleMessage = EKSimpleMessage(
        title: title,
        description: description
    )
    
    let buttonFont = MainFont.medium.with(size: 16)
    let closeButtonLabelStyle = EKProperty.LabelStyle(
        font: buttonFont,
        color: Color.Gray.a800,
        displayMode: displayMode
    )
    let closeButtonLabel = EKProperty.LabelContent(
        text: "確定",
        style: closeButtonLabelStyle
    )
    let defaultAction = {() in
        SwiftEntryKit.dismiss()
    }
    let closeButton = EKProperty.ButtonContent(
        label: closeButtonLabel,
        backgroundColor: .clear,
        highlightedBackgroundColor: Color.Gray.a800.with(alpha: 0.05),
        displayMode: displayMode,
        action: action ?? defaultAction
        )
        
    
    let buttonsBarContent = EKProperty.ButtonBarContent(
        with: closeButton,
        separatorColor: Color.Gray.light,
        displayMode: displayMode,
        expandAnimatedly: true
    )
    
    let alertMessage = EKAlertMessage(
        simpleMessage: simpleMessage,
        buttonBarContent: buttonsBarContent
    )
    
    let contentView = EKAlertMessageView(with: alertMessage)
    SwiftEntryKit.display(entry: contentView, using: attributes)
}


