import Foundation

@objc public class DBR: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
