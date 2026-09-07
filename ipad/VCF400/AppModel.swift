import SwiftUI
import VCF400Kit

/// 画面 (DDS レコード書式) = ナビゲーション先。VCFMAIN → 各機能 の階層を NavigationStack で再現する。
indirect enum Route: Hashable {
    case navigate(next: Route)          // NTRSTIT
    case vote                           // VOTESCR/VOTE1
    case voteEnd(badge: Int, exhibit: String, award: Int)   // VOTESCR/VOTEEND
    case endOfCon                       // VOTESCR/ENDOFCON
    case addComment                     // GUESTBKSCR/ADDCMT
    case endComment(id: Int)            // GUESTBKSCR/ENDCMT
    case readComment                    // GUESTBKSCR/READCMT
    case learn                          // LRN400SCR/MAIN
    case kiosk(exhibit: String)         // EXHBMENUSC/MENU
    case admPswrd(exhibit: String)      // EXHBMENUSC/ADMPSWRD
    case signoff
}

/// 5250 ジョブ状態 (LAUNCH、呼出元) と DB アクセスの保持。
final class AppModel: ObservableObject {
    let repo: Repositories
    let votes: VoteService
    let guestbook: GuestbookService
    let learn: LearnService
    let kiosk: KioskService

    @Published var launch: Launch
    @Published var path: [Route] = []
    @Published var message: String?
    @Published var dbWarning: String?
    @Published var kioskPasswordFailures = 0
    @Published var kioskPasswordLockedUntil: Date?
    let defaultProfile: String

    init(repo: Repositories, profile: String) {
        self.repo = repo
        self.votes = VoteService(repo: repo)
        self.guestbook = GuestbookService(repo: repo)
        self.learn = LearnService(repo: repo)
        self.kiosk = KioskService(repo: repo)
        self.defaultProfile = profile
        self.launch = Launch(profile)
    }

    // MARK: navigation (VCFMAIN の番号 → 機能)

    func selectMain(option raw: String) {
        let opt = raw.trimmingCharacters(in: .whitespaces).drop { $0 == "0" }
        message = nil
        switch String(opt) {
        case "1": path.append(.learn)
        case "11": path.append(.navigate(next: .vote))
        case "12": path.append(.navigate(next: .addComment))
        case "13": path.append(.navigate(next: .readComment))
        case "80": path.append(.signoff)
        case "": break
        default:
            if let n = Int(opt), n <= 10 || n == 90 {
                message = String(format: L10n.menuOptionMissing, String(opt))
            } else {
                message = L10n.invalidOption
            }
        }
    }

    /// STREXHB EXHBNAME(x): キオスク端末は展示者プロファイルでサインオン
    func startKiosk(_ exhibit: String) {
        guard let e = kiosk.exhibit(for: exhibit) else {
            message = String(format: L10n.exhibitMissing, exhibit.uppercased())
            return
        }
        message = nil
        launch = Launch(e.exhusrprf)
        path = [.kiosk(exhibit: e.exhusrprf)]
    }

    /// EXHBMENU の番号 → 機能 (1: ELIGIBLE, 2: ENLRN400 で制御、7: 隠し終了)
    func selectKiosk(_ e: Exhibit, option raw: String) {
        message = nil
        switch raw.trimmingCharacters(in: .whitespaces) {
        case "1": if e.isEligible { path.append(.navigate(next: .vote)) }
        case "2": if e.isLearnEnabled { path.append(.learn) }
        case "3": path.append(.navigate(next: .addComment))
        case "4": path.append(.navigate(next: .readComment))
        case "7": path.append(.admPswrd(exhibit: e.exhusrprf))
        case "": message = L10n.selectOption
        default: break
        }
    }

    /// プログラム終了 → 呼出元 (キオスクメニュー or VCFMAIN)
    func returnToCaller() {
        if let i = path.lastIndex(where: { if case .kiosk = $0 { return true } else { return false } }) {
            path = Array(path[...i])
        } else {
            path = []
        }
    }

    func signOn(_ profile: String) {
        message = nil
        launch = Launch(profile.isEmpty ? defaultProfile : profile)
        path = []
    }

    func recordKioskPasswordFailure() {
        let now = Date()
        if let until = kioskPasswordLockedUntil, until > now { return }
        kioskPasswordFailures += 1
        if kioskPasswordFailures >= 5 {
            kioskPasswordFailures = 0
            kioskPasswordLockedUntil = now.addingTimeInterval(30)
        }
    }

    func resetKioskPasswordThrottle() {
        kioskPasswordFailures = 0
        kioskPasswordLockedUntil = nil
    }

    /// キオスク終了 (パスワード一致) → VCFMAIN
    func exitKiosk(_ exhibit: String, password: String) {
        if kiosk.exitAllowed(password) {
            launch = Launch(defaultProfile)
            path = []
            message = String(format: L10n.kioskEnded, exhibit)
        } else {
            returnToCaller()
        }
    }

    var exhibitProtected: Bool { !launch.isShared }
}

/// DDS 数値フィールド (CHECK(RZ)) の入力解釈: 数字以外・空は 0
func numeric(_ s: String, digits: Int) -> Int {
    let d = s.trimmingCharacters(in: .whitespaces)
    guard !d.isEmpty, d.count <= digits, d.allSatisfy(\.isNumber) else { return 0 }
    return Int(d) ?? 0
}
