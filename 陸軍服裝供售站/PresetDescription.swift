//
//  PresetDescription.swift
//  陸軍服裝供售站
//
//  Created by 19003471Claud on 2021/1/22.
//  Copyright © 2021 19003471Claud. All rights reserved.
//

import Foundation
import SwiftEntryKit
// Description of a single preset to be presented
struct PresetDescription {
    let title: String
    let description: String
    let thumb: String
    let attributes: EKAttributes
    
    init(with attributes: EKAttributes, title: String, description: String = "", thumb: String) {
        self.attributes = attributes
        self.title = title
        self.description = description
        self.thumb = thumb
    }
}
