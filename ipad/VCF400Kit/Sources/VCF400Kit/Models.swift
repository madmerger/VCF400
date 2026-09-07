import Foundation

/// EXHBDB (D-01)
public struct Exhibit: Equatable, Sendable {
    public var exhbdbid: Int
    public var exhusrprf: String
    public var exhbitor: String
    public var exhbcity: String
    public var exhbstate: String
    public var exhbtitle: String
    public var exhbdesc: String
    public var eligible: Int
    public var enlrn400: Int

    public var isEligible: Bool { eligible == 1 }
    public var isLearnEnabled: Bool { enlrn400 == 1 }
}

/// AWARDDB (D-04)
public struct Award: Equatable, Sendable {
    public var awardid: Int
    public var awardtitle: String
    public var awarddesc: String
    /// 元画面の "001." 形式
    public var number: String { String(format: "%03d", awardid) }
}

/// VOTINGDB (D-02)
public struct Vote: Equatable, Sendable, Codable {
    public var badgenbr: Int
    public var awardnbr: Int
    public var exhbnbr: String
}

/// GUESTBKDB (D-03)
public struct GuestbookComment: Equatable, Sendable, Codable {
    public var cmtid: Int
    public var visible: String
    public var exhbid: String
    public var guestname: String
    public var guestcmt: String
    public var isVisible: Bool { visible == "Y" }
}

/// SETTINGS (D-05)
public struct Setting: Equatable, Sendable, Codable {
    public var setting: String
    public var value: String
}

/// LRN400STR (D-07)
public struct LearnPage: Equatable, Sendable {
    public var pagenbr: Int
    public var content: String
    public var extra: String
}

/// 5250 ジョブのサインオンユーザー (LAUNCH)。MM2024 = 共用端末 (展示 ID 入力可)。
public struct Launch: Equatable, Sendable {
    public static let mm2024 = "MM2024"
    public let profile: String
    public init(_ profile: String) {
        self.profile = profile.trimmingCharacters(in: .whitespaces).uppercased()
    }
    public var isShared: Bool { profile == Launch.mm2024 }
}

/// 元 RPG のメッセージ定数 (仕様書 M-xx)。文言は変更しない。
public enum Messages {
    public static let errblkbg = "Must enter badge number"
    public static let errblkex = "Must enter Exhibit ID"
    public static let errblkaw = "Must enter Award ID"
    public static let errexist = "You have already voted."
    public static let errprohb = "Exhibit ineligible for award"
    public static let errnoexb = "Exhibit does not exist"
    public static let errnoawd = "Award does not exist."
    public static let errnoname = "Must enter your name"
    public static let errnocmt = "Must enter a comment"
    public static let errcmtid = "Must enter CommentID"
    public static let errhname = "Name Hidden"
    public static let errhcmt = "This comment hidden by an admin - offensive content."
    public static let errpriv = "This comment is not part of this guestbook."
}
