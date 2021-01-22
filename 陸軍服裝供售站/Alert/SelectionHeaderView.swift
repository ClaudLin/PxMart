//
//  SelectionHeaderView.swift
//  陸軍服裝供售站
//
//  Created by 19003471Claud on 2021/1/22.
//  Copyright © 2021 19003471Claud. All rights reserved.
//

import UIKit
import SwiftEntryKit

final class SelectionHeaderView: UITableViewHeaderFooterView {
    var text: String {
        set {
            textLabel?.text = newValue
        }
        get {
            return textLabel?.text ?? ""
        }
    }
    
    var displayMode = EKAttributes.DisplayMode.inferred {
        didSet {
            setupInterfaceStyle()
        }
    }
    
    override init(reuseIdentifier: String?) {
        super.init(reuseIdentifier: reuseIdentifier)
        backgroundView = UIView()
        textLabel?.font = MainFont.bold.with(size: 17)
        setupInterfaceStyle()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupInterfaceStyle() {
        backgroundView?.backgroundColor = EKColor.headerBackground.color(
            for: traitCollection,
            mode: PresetsDataSource.displayMode
        )
        textLabel?.textColor = EKColor.standardContent.with(alpha: 0.8).color(
            for: traitCollection,
            mode: PresetsDataSource.displayMode
        )
    }
    
    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        setupInterfaceStyle()
    }
    /*
    // Only override draw() if you perform custom drawing.
    // An empty implementation adversely affects performance during animation.
    override func draw(_ rect: CGRect) {
        // Drawing code
    }
    */

}
