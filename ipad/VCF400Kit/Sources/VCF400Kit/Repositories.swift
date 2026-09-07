import Foundation

/// DDS 物理ファイルへのアクセス。RPG の CHAIN (完全一致) と SETLL/READ (キー以上の先頭) を区別して提供する。
public final class Repositories {
    public let db: Database
    public init(db: Database) { self.db = db }

    // EXHBDB
    private func exhibit(_ r: Database.Row) -> Exhibit {
        Exhibit(exhbdbid: r.int(0), exhusrprf: r.str(1), exhbitor: r.str(2), exhbcity: r.str(3), exhbstate: r.str(4),
                exhbtitle: r.str(5), exhbdesc: r.str(6), eligible: r.int(7), enlrn400: r.int(8))
    }
    private let exhibitCols = "EXHBDBID, EXHUSRPRF, EXHBITOR, EXHBCITY, EXHBSTATE, EXHBTITLE, EXHBDESC, ELIGIBLE, ENLRN400"

    /// CHAIN EXHBREC
    public func exhibit(id: String) -> Exhibit? {
        (try? db.query("SELECT \(exhibitCols) FROM EXHBDB WHERE EXHUSRPRF = ?", [id], map: exhibit))?.first
    }
    /// SETLL + READ EXHBDB
    public func exhibitAtOrAfter(_ id: String) -> Exhibit? {
        (try? db.query("SELECT \(exhibitCols) FROM EXHBDB WHERE EXHUSRPRF >= ? ORDER BY EXHUSRPRF LIMIT 1", [id], map: exhibit))?.first
    }
    public func allExhibits() -> [Exhibit] {
        (try? db.query("SELECT \(exhibitCols) FROM EXHBDB ORDER BY EXHUSRPRF", map: exhibit)) ?? []
    }

    // AWARDDB
    private func award(_ r: Database.Row) -> Award { Award(awardid: r.int(0), awardtitle: r.str(1), awarddesc: r.str(2)) }
    public func award(id: Int) -> Award? {
        (try? db.query("SELECT AWARDID, AWARDTITLE, AWARDDESC FROM AWARDDB WHERE AWARDID = ?", [id], map: award))?.first
    }
    public func allAwards() -> [Award] {
        (try? db.query("SELECT AWARDID, AWARDTITLE, AWARDDESC FROM AWARDDB ORDER BY AWARDID", map: award)) ?? []
    }

    // VOTINGDB
    private func vote(_ r: Database.Row) -> Vote { Vote(badgenbr: r.int(0), awardnbr: r.int(1), exhbnbr: r.str(2)) }
    public func vote(badge: Int) -> Vote? {
        (try? db.query("SELECT BADGENBR, AWARDNBR, EXHBNBR FROM VOTINGDB WHERE BADGENBR = ?", [badge], map: vote))?.first
    }
    public func allVotes() -> [Vote] {
        (try? db.query("SELECT BADGENBR, AWARDNBR, EXHBNBR FROM VOTINGDB ORDER BY BADGENBR", map: vote)) ?? []
    }
    public func insert(_ v: Vote) throws {
        try db.execute("INSERT INTO VOTINGDB (BADGENBR, AWARDNBR, EXHBNBR) VALUES (?, ?, ?)", [v.badgenbr, v.awardnbr, v.exhbnbr])
    }
    @discardableResult public func deleteVote(badge: Int) -> Int {
        try? db.execute("DELETE FROM VOTINGDB WHERE BADGENBR = ?", [badge]); return db.changes
    }

    // GUESTBKDB
    private func comment(_ r: Database.Row) -> GuestbookComment {
        GuestbookComment(cmtid: r.int(0), visible: r.str(1), exhbid: r.str(2), guestname: r.str(3), guestcmt: r.str(4))
    }
    private let cmtCols = "CMTID, VISIBLE, EXHBID, GUESTNAME, GUESTCMT"
    public func commentAtOrAfter(_ id: Int) -> GuestbookComment? {
        (try? db.query("SELECT \(cmtCols) FROM GUESTBKDB WHERE CMTID >= ? ORDER BY CMTID LIMIT 1", [id], map: comment))?.first
    }
    public func lastComment() -> GuestbookComment? {
        (try? db.query("SELECT \(cmtCols) FROM GUESTBKDB ORDER BY CMTID DESC LIMIT 1", map: comment))?.first
    }
    public func allComments() -> [GuestbookComment] {
        (try? db.query("SELECT \(cmtCols) FROM GUESTBKDB ORDER BY CMTID", map: comment)) ?? []
    }
    public func insert(_ c: GuestbookComment) throws {
        try db.execute("INSERT INTO GUESTBKDB (CMTID, VISIBLE, EXHBID, GUESTNAME, GUESTCMT) VALUES (?, ?, ?, ?, ?)",
                       [c.cmtid, c.visible, c.exhbid, c.guestname, c.guestcmt])
    }
    @discardableResult public func deleteComment(id: Int) -> Int {
        try? db.execute("DELETE FROM GUESTBKDB WHERE CMTID = ?", [id]); return db.changes
    }
    @discardableResult public func setVisible(id: Int, _ value: String) -> Int {
        try? db.execute("UPDATE GUESTBKDB SET VISIBLE = ? WHERE CMTID = ?", [value, id]); return db.changes
    }

    // SETTINGS
    private func setting(_ r: Database.Row) -> Setting { Setting(setting: r.str(0), value: r.str(1)) }
    public func setting(_ name: String) -> Setting? {
        (try? db.query("SELECT SETTING, VALUE FROM SETTINGS WHERE SETTING = ?", [name], map: setting))?.first
    }
    public func allSettings() -> [Setting] {
        (try? db.query("SELECT SETTING, VALUE FROM SETTINGS ORDER BY SETTING", map: setting)) ?? []
    }
    @discardableResult public func updateSetting(_ name: String, _ value: String) -> Int {
        try? db.execute("UPDATE SETTINGS SET VALUE = ? WHERE SETTING = ?", [value, name]); return db.changes
    }

    // LRN400STR
    private func page(_ r: Database.Row) -> LearnPage { LearnPage(pagenbr: r.int(0), content: r.str(1), extra: r.str(2)) }
    public func firstPage() -> LearnPage? {
        (try? db.query("SELECT PAGENBR, CONTENT, EXTRA FROM LRN400STR ORDER BY PAGENBR LIMIT 1", map: page))?.first
    }
    public func page(_ nbr: Int) -> LearnPage? {
        (try? db.query("SELECT PAGENBR, CONTENT, EXTRA FROM LRN400STR WHERE PAGENBR = ?", [nbr], map: page))?.first
    }
}
