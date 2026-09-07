import SwiftUI
import VCF400Kit

@main
struct VCF400App: App {
    @StateObject private var model = AppModel.fromLaunchArguments()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(model)
                .tint(Theme.brand)
        }
    }
}

/// 起動引数: -VCF_PROFILE <user> (LAUNCH)、-VCF_DB <path>、-VCF_RESET (DB 初期化)、-VCF_KIOSK <exhibit> (STREXHB 相当)
extension AppModel {
    static func fromLaunchArguments() -> AppModel {
        let d = UserDefaults.standard
        let path = d.string(forKey: "VCF_DB") ?? Database.defaultPath()
        if d.bool(forKey: "VCF_RESET"), path != ":memory:" {
            try? FileManager.default.removeItem(atPath: path)
            try? FileManager.default.removeItem(atPath: path + "-wal")
            try? FileManager.default.removeItem(atPath: path + "-shm")
        }
        let db = try! Database(path: path)
        let model = AppModel(repo: Repositories(db: db), profile: d.string(forKey: "VCF_PROFILE") ?? "ASHIBATA")
        if let kiosk = d.string(forKey: "VCF_KIOSK") { model.startKiosk(kiosk) }
        return model
    }
}
