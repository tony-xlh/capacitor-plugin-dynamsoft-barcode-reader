// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorPluginDynamsoftBarcodeReader",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "CapacitorPluginDynamsoftBarcodeReader",
            targets: ["DBRPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", branch: "main")
    ],
    targets: [
        .target(
            name: "DBRPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/DBRPlugin"),
        .testTarget(
            name: "DBRPluginTests",
            dependencies: ["DBRPlugin"],
            path: "ios/Tests/DBRPluginTests")
    ]
)