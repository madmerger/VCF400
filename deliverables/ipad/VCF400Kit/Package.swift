// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "VCF400Kit",
    platforms: [.iOS(.v17), .macOS(.v14)],
    products: [.library(name: "VCF400Kit", targets: ["VCF400Kit"])],
    targets: [
        .target(name: "VCF400Kit", linkerSettings: [.linkedLibrary("sqlite3")]),
        .testTarget(name: "VCF400KitTests", dependencies: ["VCF400Kit"]),
    ]
)
