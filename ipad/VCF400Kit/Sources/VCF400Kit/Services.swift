import Foundation

// Java 版 (com.vcf400.service.*) と同じ判定順・同じメッセージで実装する。

/// ADDVOTE (F-03, V-01..V-09, B-01..B-05)
public struct VoteService {
    public struct Result: Equatable {
        public var errLine: String?
        public var badgeErr = false
        public var exhibitErr = false
        public var awardErr = false
        public var recorded = false
        public var vote: Vote?
        public var hasError: Bool { errLine != nil }
    }

    let repo: Repositories
    public init(repo: Repositories) { self.repo = repo }

    /// CHKALWVOTE (B-03)
    public var isVotingAllowed: Bool { repo.setting("ALWVOTE").map { $0.value != "N" } ?? true }

    /// CHKPARM (B-13): 通常起動は展示 ID を LAUNCH に固定
    public func protectedExhibit(_ launch: Launch) -> String { launch.isShared ? "" : launch.profile }

    public var availableAwards: [Award] { repo.allAwards() }

    public func submit(_ launch: Launch, badge: Int, exhibit: String, award: Int) -> Result {
        let inexhb = launch.isShared ? exhibit.trimmingCharacters(in: .whitespaces).uppercased() : launch.profile
        var r = Result()
        var validate = 0
        if badge == 0 { r.errLine = Messages.errblkbg; r.badgeErr = true } else { validate += 1 }
        if award == 0 { r.errLine = Messages.errblkaw; r.awardErr = true } else { validate += 1 }
        if inexhb.isEmpty { r.errLine = Messages.errblkex; r.exhibitErr = true } else { validate += 1 }
        if validate != 3 { return r }

        // ADDTODB: 重複 → 資格 (SETLL/READ) → 展示存在 (CHAIN) → アワード存在 (CHAIN)
        var checkOk = 0
        var errLine: String?
        if repo.vote(badge: badge) != nil { errLine = Messages.errexist } else { checkOk += 1 }
        if let e = repo.exhibitAtOrAfter(inexhb), !e.isEligible { errLine = Messages.errprohb } else { checkOk += 1 }
        if repo.exhibit(id: inexhb) == nil { errLine = Messages.errnoexb } else { checkOk += 1 }
        if repo.award(id: award) == nil { errLine = Messages.errnoawd } else { checkOk += 1 }
        if checkOk == 4 {
            let v = Vote(badgenbr: badge, awardnbr: award, exhbnbr: inexhb)
            do { try repo.insert(v) } catch { return Result(errLine: "\(error)") }
            return Result(errLine: nil, recorded: true, vote: v)
        }
        return Result(errLine: errLine)
    }
}

/// ADDGBCMT / READGBCMT (F-04, F-05, V-10..V-18, B-06..B-09)
public struct GuestbookService {
    public struct AddResult: Equatable {
        public var errLine: String?
        public var exhibitErr = false
        public var nameErr = false
        public var commentErr = false
        public var added: GuestbookComment?
        public var hasError: Bool { errLine != nil }
    }
    /// READCMT 出力域 (OUTNAME 11 / OUTTITLE 50 / OUTCMT 200)
    public struct ReadResult: Equatable {
        public var outName: String
        public var outTitle: String
        public var outCmt: String
        public var record: GuestbookComment
    }

    let repo: Repositories
    public init(repo: Repositories) { self.repo = repo }

    public func protectedExhibit(_ launch: Launch) -> String { launch.isShared ? "" : launch.profile }

    public func add(_ launch: Launch, name inName: String, exhibit inId: String, comment inCmt: String) -> AddResult {
        let id = launch.isShared ? inId.trimmingCharacters(in: .whitespaces).uppercased() : launch.profile
        let name = inName.trimmingCharacters(in: .whitespacesAndNewlines)
        let cmt = inCmt.trimmingCharacters(in: .whitespacesAndNewlines)
        var r = AddResult()
        var validate = 0
        if id.isEmpty { r.errLine = Messages.errblkex; r.exhibitErr = true } else { validate += 1 }
        if name.isEmpty { r.errLine = Messages.errnoname; r.nameErr = true } else { validate += 1 }
        if cmt.isEmpty { r.errLine = Messages.errnocmt; r.commentErr = true } else { validate += 1 }
        if validate != 3 { return r }
        let newId = (repo.lastComment()?.cmtid ?? 0) + 1                       // B-06
        let c = GuestbookComment(cmtid: newId, visible: "Y", exhbid: id, guestname: name.left(20), guestcmt: cmt.left(200))
        do { try repo.insert(c) } catch { return AddResult(errLine: "\(error)") }
        return AddResult(errLine: nil, added: c)
    }

    /// "Currently hosting nnnn comments"
    public var totalComments: Int { repo.lastComment()?.cmtid ?? 0 }

    /// READDB (SETLL/READ、EOF 時は最終レコード = レガシー挙動 B-15)
    public func read(_ launch: Launch, id inCmtId: Int) -> ReadResult? {
        guard let c = repo.commentAtOrAfter(inCmtId) ?? repo.lastComment() else { return nil }
        if !c.isVisible {
            return ReadResult(outName: Messages.errhname, outTitle: "", outCmt: Messages.errhcmt, record: c)
        }
        if c.exhbid == launch.profile || launch.isShared {
            let title = repo.exhibitAtOrAfter(c.exhbid)?.exhbtitle ?? ""
            return ReadResult(outName: c.guestname.left(11), outTitle: title, outCmt: c.guestcmt, record: c)
        }
        return ReadResult(outName: "", outTitle: "", outCmt: Messages.errpriv, record: c)
    }
}

/// LRN400 (F-06, B-10)
public struct LearnService {
    public struct State: Equatable {
        public var curPageNbr: Int
        public var frmPageNbr: Int
        public var alwFwd: Bool
        public var outPageNbr: String
        public var outContent: String
        public var exit: Bool
    }

    let repo: Repositories
    public init(repo: Repositories) { self.repo = repo }

    public func start() -> State {
        let first = repo.firstPage()
        return State(curPageNbr: first?.pagenbr ?? 0, frmPageNbr: 0, alwFwd: true,
                     outPageNbr: first.map { String($0.pagenbr) } ?? "", outContent: first?.content ?? "", exit: false)
    }

    /// F5 = PAGEFWD
    public func forward(_ s: State) -> State {
        var cur = s.curPageNbr
        var frm = s.frmPageNbr
        if s.alwFwd { cur += 1; frm = 0 }
        guard let page = repo.page(cur) else {
            return State(curPageNbr: cur, frmPageNbr: frm, alwFwd: false, outPageNbr: s.outPageNbr, outContent: "", exit: false)
        }
        if page.extra == "END" {
            return State(curPageNbr: cur, frmPageNbr: frm, alwFwd: true, outPageNbr: String(page.pagenbr), outContent: page.content, exit: true)
        }
        if page.content == "CALL" || page.content == "JUMP" {
            frm = cur - 1
            cur = page.content == "JUMP" ? (Int(page.extra.trimmingCharacters(in: .whitespaces)) ?? 0) : cur + 1
            let t = repo.page(cur)
            return State(curPageNbr: cur, frmPageNbr: frm, alwFwd: true, outPageNbr: t.map { String($0.pagenbr) } ?? "",
                         outContent: t?.content ?? "", exit: false)
        }
        return State(curPageNbr: cur, frmPageNbr: frm, alwFwd: true, outPageNbr: String(page.pagenbr), outContent: page.content, exit: false)
    }

    /// F8 = PAGEBACK
    public func back(_ s: State) -> State {
        let cur = s.curPageNbr == 0 ? 0 : s.curPageNbr - 1
        let target = s.frmPageNbr == 0 ? cur : s.frmPageNbr
        guard let p = repo.page(target) else {
            return State(curPageNbr: cur, frmPageNbr: 0, alwFwd: true, outPageNbr: s.outPageNbr, outContent: s.outContent, exit: false)
        }
        return State(curPageNbr: cur, frmPageNbr: 0, alwFwd: true, outPageNbr: String(p.pagenbr), outContent: p.content, exit: false)
    }
}

/// EXHBMENU (F-07, B-11, B-12)
public struct KioskService {
    let repo: Repositories
    public init(repo: Repositories) { self.repo = repo }

    public func exhibit(for launch: String) -> Exhibit? { repo.exhibit(id: launch.trimmingCharacters(in: .whitespaces).uppercased()) }

    /// GETPSWRD: SETTINGS 先頭レコード (キー順 = ADMPSWRD)
    public var exitPassword: String { repo.allSettings().first?.value ?? "" }

    public func exitAllowed(_ inPwd: String) -> Bool {
        let p = inPwd.trimmingCharacters(in: .whitespaces)
        return !p.isEmpty && p == exitPassword
    }
}
