//
//  Utils.swift
//  Plugin
//
//  Created by xulihang on 2022/11/14.
//  Copyright © 2022 Max Lynch. All rights reserved.
//

import Foundation
import DynamsoftBarcodeReader

class Utils {
    static public func convertBase64ToImage(_ imageStr:String) ->UIImage?{
        if let data: NSData = NSData(base64Encoded: imageStr, options:NSData.Base64DecodingOptions.ignoreUnknownCharacters)
        {
            if let image: UIImage = UIImage(data: data as Data)
            {
                return image
            }
        }
        return nil
    }
    
    static func getBase64FromImage(_ image:UIImage) -> String{
        let dataTmp = image.jpegData(compressionQuality: 100)
        if let data = dataTmp {
            return data.base64EncodedString()
        }
        return ""
    }
    
    static func removeDataURLHead(_ str: String) -> String {
        var finalStr = str
        do {
            let pattern = "data:.*?;base64,"
            let regex = try NSRegularExpression(pattern: pattern, options: NSRegularExpression.Options.caseInsensitive)
            finalStr = regex.stringByReplacingMatches(in: str, options: NSRegularExpression.MatchingOptions(rawValue: 0), range: NSMakeRange(0, str.count), withTemplate: "")
        }
        catch {
            print(error)
        }
        return finalStr
    }
    
    static func convertPoints(_ points:[[String:NSNumber]]) -> [CGPoint] {
        var CGPoints:[CGPoint] = [];
        for point in points {
            let x = point["x"]!
            let y = point["y"]!
            let intX = x.intValue
            let intY = y.intValue
            let cgPoint = CGPoint(x: intX, y: intY)
            CGPoints.append(cgPoint)
        }
        return CGPoints
    }
    
    static func wrapBarcodeResult (result:BarcodeResultItem) -> [String: Any] {
        var dict: [String: Any] = [:]
        dict["barcodeText"] = result.text
        dict["barcodeFormatString"] = result.formatString
        dict["barcodeBytesBase64"] = result.bytes.base64EncodedString()
        dict["x1"] = (result.location.points[0] as! CGPoint).x
        dict["x2"] = (result.location.points[1] as! CGPoint).x
        dict["x3"] = (result.location.points[2] as! CGPoint).x
        dict["x4"] = (result.location.points[3] as! CGPoint).x
        dict["y1"] = (result.location.points[0] as! CGPoint).y
        dict["y2"] = (result.location.points[1] as! CGPoint).y
        dict["y3"] = (result.location.points[2] as! CGPoint).y
        dict["y4"] = (result.location.points[3] as! CGPoint).y
        return dict
    }
}
