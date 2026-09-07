import XCTest
@testable import VCF400Kit

/// Java 版 VoteServiceTest / GuestbookServiceTest / LearnKioskServiceTest と同一ケース。
final class ServicesTests: XCTestCase {
    var repo: Repositories!
    let own = Launch("ASHIBATA")
    let other = Launch("DEMO400")
    let shared = Launch(Launch.mm2024)

    override func setUpWithError() throws {
        repo = Repositories(db: try Database(inMemory: true))
    }

    // MARK: ADDVOTE
    func testEmptyBadge() {
        let r = VoteService(repo: repo).submit(own, badge: 0, exhibit: "ASHIBATA", award: 1)
        XCTAssertEqual(r.errLine, Messages.errblkbg); XCTAssertTrue(r.badgeErr); XCTAssertFalse(r.recorded)
    }
    func testEmptyAward() {
        let r = VoteService(repo: repo).submit(own, badge: 7001, exhibit: "ASHIBATA", award: 0)
        XCTAssertEqual(r.errLine, Messages.errblkaw); XCTAssertTrue(r.awardErr)
    }
    func testEmptyExhibitLastErrorWins() {
        let r = VoteService(repo: repo).submit(shared, badge: 0, exhibit: "", award: 0)
        XCTAssertEqual(r.errLine, Messages.errblkex)
        XCTAssertTrue(r.badgeErr && r.awardErr && r.exhibitErr)
    }
    func testSuccessfulVoteCheckOk4() {
        let r = VoteService(repo: repo).submit(own, badge: 7002, exhibit: "ASHIBATA", award: 1)
        XCTAssertTrue(r.recorded)
        XCTAssertEqual(repo.vote(badge: 7002), Vote(badgenbr: 7002, awardnbr: 1, exhbnbr: "ASHIBATA"))
    }
    func testDuplicateBadge() {
        let s = VoteService(repo: repo)
        _ = s.submit(own, badge: 7003, exhibit: "ASHIBATA", award: 1)
        let r = s.submit(own, badge: 7003, exhibit: "ASHIBATA", award: 2)
        XCTAssertEqual(r.errLine, Messages.errexist); XCTAssertFalse(r.recorded)
        XCTAssertEqual(repo.vote(badge: 7003)?.awardnbr, 1)
    }
    func testIneligibleExhibit() {
        let r = VoteService(repo: repo).submit(shared, badge: 7004, exhibit: "NOVOTE", award: 1)
        XCTAssertEqual(r.errLine, Messages.errprohb); XCTAssertNil(repo.vote(badge: 7004))
    }
    func testUnknownExhibit() {
        let r = VoteService(repo: repo).submit(shared, badge: 7005, exhibit: "NOSUCH", award: 1)
        XCTAssertEqual(r.errLine, Messages.errnoexb)
    }
    func testUnknownAward() {
        let r = VoteService(repo: repo).submit(own, badge: 7006, exhibit: "ASHIBATA", award: 999)
        XCTAssertEqual(r.errLine, Messages.errnoawd)
    }
    func testOwnerLaunchForcesExhibit() {
        let s = VoteService(repo: repo)
        XCTAssertEqual(s.protectedExhibit(own), "ASHIBATA")
        XCTAssertTrue(s.submit(own, badge: 7001, exhibit: "DEMO400", award: 1).recorded)
        XCTAssertEqual(repo.vote(badge: 7001)?.exhbnbr, "ASHIBATA")
    }
    func testAlwvote() {
        let s = VoteService(repo: repo)
        repo.updateSetting("ALWVOTE", "N"); XCTAssertFalse(s.isVotingAllowed)
        repo.updateSetting("ALWVOTE", "Y"); XCTAssertTrue(s.isVotingAllowed)
    }

    // MARK: ADDGBCMT / READGBCMT
    func testGuestbookRequiredOrder() {
        let s = GuestbookService(repo: repo)
        let r = s.add(shared, name: "", exhibit: "", comment: "")
        XCTAssertEqual(r.errLine, Messages.errnocmt); XCTAssertTrue(r.exhibitErr && r.nameErr && r.commentErr)
        XCTAssertEqual(s.add(shared, name: "", exhibit: "", comment: "hi").errLine, Messages.errnoname)
        XCTAssertEqual(s.add(shared, name: "Bob", exhibit: "", comment: "hi").errLine, Messages.errblkex)
        XCTAssertEqual(s.add(own, name: "", exhibit: "", comment: "hi").errLine, Messages.errnoname)
    }
    func testGuestbookNewIdAndVisible() {
        let last = repo.lastComment()?.cmtid ?? 0
        let r = GuestbookService(repo: repo).add(own, name: "Tester", exhibit: "", comment: "Great exhibit")
        XCTAssertEqual(r.added?.cmtid, last + 1); XCTAssertEqual(r.added?.visible, "Y"); XCTAssertEqual(r.added?.exhbid, "ASHIBATA")
    }
    func testReadOwn() {
        let out = GuestbookService(repo: repo).read(own, id: 1)
        XCTAssertEqual(out?.outName, "Great exhib")
        XCTAssertEqual(out?.outTitle, "IBM i on PUB400 Demo")
        XCTAssertEqual(out?.outCmt, "VCF/400 running on PUB400.")
    }
    func testReadHidden() {
        repo.setVisible(id: 1, "N")
        let out = GuestbookService(repo: repo).read(own, id: 1)
        XCTAssertEqual(out?.outName, Messages.errhname); XCTAssertEqual(out?.outCmt, Messages.errhcmt)
    }
    func testReadForeign() {
        let out = GuestbookService(repo: repo).read(other, id: 1)
        XCTAssertEqual(out?.outCmt, Messages.errpriv); XCTAssertEqual(out?.outName, "")
    }
    func testReadShared() {
        XCTAssertEqual(GuestbookService(repo: repo).read(shared, id: 2)?.outName, "Devin")
    }
    func testReadBeyondLastFallsBackToLast() {
        let last = repo.lastComment()!.cmtid
        XCTAssertEqual(GuestbookService(repo: repo).read(shared, id: last + 500)?.record.cmtid, last)
    }

    // MARK: LRN400 / EXHBMENU
    func testLearnNavigation() {
        let l = LearnService(repo: repo)
        var s = l.start(); XCTAssertEqual(s.outPageNbr, "1")
        s = l.forward(s); XCTAssertEqual(s.outPageNbr, "2"); XCTAssertTrue(s.outContent.contains("AS/400"))
        XCTAssertEqual(l.back(s).outPageNbr, "1")
        XCTAssertTrue(l.forward(s).exit)
        XCTAssertEqual(l.back(l.start()).outPageNbr, "1")
    }
    func testKiosk() {
        let k = KioskService(repo: repo)
        XCTAssertTrue(k.exhibit(for: "ASHIBATA")!.isEligible && k.exhibit(for: "ASHIBATA")!.isLearnEnabled)
        XCTAssertFalse(k.exhibit(for: "DEMO400")!.isLearnEnabled)
        XCTAssertFalse(k.exhibit(for: "NOVOTE")!.isEligible)
        XCTAssertNil(k.exhibit(for: "NOSUCH"))
        XCTAssertEqual(k.exitPassword, "VCF2024")
        XCTAssertTrue(k.exitAllowed("VCF2024")); XCTAssertFalse(k.exitAllowed("vcf2024")); XCTAssertFalse(k.exitAllowed(""))
    }
}
