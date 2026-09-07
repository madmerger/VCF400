import XCTest
import VCF400Kit

/// Phase 5 runner: executes deliverables/tests/cases.json against the iPad app through the UI.
/// Environment: VCF_CASES (cases.json), VCF_RESULTS (output json), VCF_DB (sqlite path shared with the app), VCF_ONLY (CV-01,CV-04)
/// Skipped when VCF_CASES is not set so the regular test run is unaffected.
final class CrossValidationUITests: XCTestCase {
    struct Input: Decodable {
        var badge: String?, award: String?, exhibit: String?, name: String?, comment: String?, cmtid: String?
        var option: String?, password: String?, keys: [String]?
    }
    struct DbOp: Decodable { var op: String; var name: String?; var id: Int?; var value: String }
    struct ExpectComment: Decodable { var id: Int }
    struct Expect: Decodable { var comment: ExpectComment? }
    struct Case: Decodable {
        var id: String, group: String, flow: String, title: String
        var launch: String?, exhibit: String?
        var `in`: Input?, pre: [DbOp]?, post: [DbOp]?
        var expect: Expect
    }
    struct Catalog: Decodable { var cases: [Case] }

    /// JSON value for the observed results (mirrors run_java.js / run_pub400.py output)
    indirect enum J: Encodable {
        case s(String), i(Int), null, a([J]), o([String: J])
        func encode(to e: Encoder) throws {
            var c = e.singleValueContainer()
            switch self {
            case .s(let v): try c.encode(v)
            case .i(let v): try c.encode(v)
            case .null: try c.encodeNil()
            case .a(let v): try c.encode(v)
            case .o(let v): try c.encode(v)
            }
        }
    }
    struct Entry: Encodable { var observed: [String: J]; var error: String? }
    struct Output: Encodable { var env = "ipad"; var started: String; var finished: String?; var cases: [String: Entry] = [:] }

    var app: XCUIApplication!
    var repo: Repositories!
    var out: Output!
    let env = ProcessInfo.processInfo.environment
    var resultsPath: String { env["VCF_RESULTS"] ?? "" }

    override func setUpWithError() throws {
        try XCTSkipIf(env["VCF_CASES"] == nil, "VCF_CASES not set")
        continueAfterFailure = true
        let dbPath = env["VCF_DB"] ?? NSTemporaryDirectory() + "vcf400_cv.sqlite"
        app = XCUIApplication()
        app.launchArguments = ["-VCF_RESET", "YES", "-VCF_PROFILE", "ASHIBATA", "-VCF_DB", dbPath]
        app.launch()
        XCTAssertTrue(app.staticTexts["AS/400 DEMO MENU"].waitForExistence(timeout: 10))
        repo = Repositories(db: try Database(path: dbPath))
        out = Output(started: iso(Date()))
    }

    private func iso(_ d: Date) -> String { ISO8601DateFormatter().string(from: d) }

    // MARK: DB (same shape as the Java /api/db dump and pub400_db.py dump)
    private func dump() -> [String: J] {
        [
            "votes": .a(repo.allVotes().map { .o(["badge": .i($0.badgenbr), "award": .i($0.awardnbr), "exhibit": .s($0.exhbnbr)]) }),
            "comments": .a(repo.allComments().map { .o(["id": .i($0.cmtid), "visible": .s($0.visible), "exhibit": .s($0.exhbid), "name": .s($0.guestname), "comment": .s($0.guestcmt)]) }),
            "settings": .o(Dictionary(uniqueKeysWithValues: repo.allSettings().map { ($0.setting, J.s($0.value)) }))
        ]
    }
    private func dbOp(_ op: DbOp) {
        switch op.op {
        case "setting": if let n = op.name { repo.updateSetting(n, op.value) }
        case "visible": if let id = op.id { repo.setVisible(id: id, op.value) }
        default: break
        }
        log("    db op: \(op.op) \(op.name ?? "") \(op.id.map(String.init) ?? "") = \(op.value)")
    }

    // MARK: screen helpers
    private let titles: [(String, String)] = [
        ("AS/400 DEMO MENU", "VCFMAIN"), ("NOMINATE EXHIBIT FOR AWARD", "VOTE1"), ("THANK YOU FOR VOTING!", "VOTEEND"),
        ("SORRY!", "ENDOFCON"), ("GUESTBOOK/400 - ADD COMMENT", "ADDCMT"), ("THANKS FOR COMMENTING!", "ENDCMT"),
        ("GUESTBOOK/400 - Read a Comment", "READCMT"), ("LEARN/400", "LRN400"),
        ("Are you sure you want to exit the kiosk?", "ADMPSWRD"), ("VCF/400 - How to Navigate", "NTRSTIT"), ("WELCOME TO...", "KIOSK")
    ]
    private func classify() -> String {
        let deadline = Date().addingTimeInterval(4)
        repeat {
            for (text, screen) in titles where app.staticTexts[text].exists { return screen }
            usleep(150_000)
        } while Date() < deadline
        return "UNKNOWN"
    }
    private func show(_ title: String) -> String {
        let k = classify()
        log("----- ipad: \(title) [\(k)]")
        return k
    }
    private func log(_ s: String) { print(s); fflush(stdout) }

    private func fkey(_ key: String) { app.buttons[key].firstMatch.tap() }
    private func type(_ field: XCUIElement, _ text: String) {
        field.tap()
        let current = (field.value as? String) ?? ""
        if !current.isEmpty && current != field.placeholderValue {
            field.typeText(String(repeating: XCUIKeyboardKey.delete.rawValue, count: current.count))
        }
        if !text.isEmpty { field.typeText(text) }
    }
    private func label(_ id: String) -> String { app.staticTexts[id].exists ? app.staticTexts[id].label : "" }
    private func labelStarting(_ prefix: String) -> String {
        let q = app.staticTexts.matching(NSPredicate(format: "label BEGINSWITH %@", prefix))
        return q.count > 0 ? q.firstMatch.label : ""
    }
    private func firstInt(_ s: String) -> Int {
        Int(s.split(whereSeparator: { !$0.isNumber }).first.map(String.init) ?? "") ?? 0
    }

    /// VCFMAIN: sign on as the case's LAUNCH profile through the sign-on disclosure (kiosk exhibit / MM2024)
    private func signon(_ profile: String) {
        let field = app.textFields["profile"]
        if !field.exists { app.staticTexts["Sign on as a different user profile (kiosk exhibit / MM2024 shared terminal)"].tap() }
        XCTAssertTrue(field.waitForExistence(timeout: 3))
        type(field, profile)
        app.buttons["signon"].tap()
        XCTAssertTrue(app.staticTexts["AS/400 DEMO MENU"].waitForExistence(timeout: 3))
    }
    /// VCFMAIN: tap the numbered item, then ENTER through NTRSTIT when shown
    private func menu(_ option: String) {
        app.buttons["menu.\(option)"].tap()
        if classify() == "NTRSTIT" { _ = show("NTRSTIT"); fkey("ENTER") }
    }
    private func toMain() {
        for _ in 0..<6 {
            let k = classify()
            switch k {
            case "VCFMAIN": return
            case "VOTEEND", "ENDOFCON", "ENDCMT", "NTRSTIT": fkey("ENTER")
            case "VOTE1", "ADDCMT", "READCMT": fkey("F12")
            case "LRN400": fkey("F3")
            case "KIOSK": type(app.textFields["option"], "7"); fkey("ENTER")
            case "ADMPSWRD": type(app.secureTextFields["inPwd"], "VCF2024"); fkey("ENTER")
            default: XCTFail("cannot return to VCFMAIN from \(k)"); return
            }
        }
    }

    // MARK: flows
    private func flowVote(_ c: Case) -> [String: J] {
        signon(c.launch ?? "ASHIBATA")
        menu("11")
        var k = show("VOTE1 initial")
        if k == "ENDOFCON" { return ["screen": .s(k)] }
        type(app.textFields["inputBadge"], c.in?.badge ?? "")
        if c.launch == "MM2024" { type(app.textFields["inExhb"], c.in?.exhibit ?? "") }
        type(app.textFields["inputAward"], c.in?.award ?? "")
        _ = show("VOTE1 filled")
        fkey("F5")
        k = show("VOTE1 after F5")
        var obs: [String: J] = ["screen": .s(k)]
        if k == "VOTE1" { obs["errline"] = .s(label("errline")) }
        return obs
    }
    private func flowGbAdd(_ c: Case) -> [String: J] {
        signon(c.launch ?? "ASHIBATA")
        menu("12")
        _ = show("ADDCMT initial")
        type(app.textFields["inName"], c.in?.name ?? "")
        if c.launch == "MM2024" { type(app.textFields["inId"], c.in?.exhibit ?? "") }
        type(app.textFields["inCmt"], c.in?.comment ?? "")
        _ = show("ADDCMT filled")
        fkey("F5")
        let k = show("ADDCMT after F5")
        var obs: [String: J] = ["screen": .s(k)]
        if k == "ADDCMT" { obs["errline"] = .s(label("errline")) }
        if k == "ENDCMT" { obs["shownId"] = .i(firstInt(label("out.cmtid"))) }
        return obs
    }
    private func flowGbRead(_ c: Case) -> [String: J] {
        signon(c.launch ?? "ASHIBATA")
        menu("13")
        _ = show("READCMT initial")
        type(app.textFields["inCmtId"], c.in?.cmtid ?? "")
        fkey("F5")
        let k = show("READCMT after F5")
        var obs: [String: J] = ["screen": .s(k), "total": .i(firstInt(labelStarting("Currently hosting"))), "errline": .s(label("errline"))]
        if app.staticTexts["out.cmt"].waitForExistence(timeout: 1) {
            obs["out"] = .o(["name": .s(label("out.name")), "title": .s(label("out.title")), "cmt": .s(label("out.cmt"))])
        }
        return obs
    }
    private func flowLearn(_ c: Case) -> [String: J] {
        signon(c.launch ?? "ASHIBATA")
        menu("1")
        _ = show("LRN400 page 1")
        for key in c.in?.keys ?? [] { fkey(key); _ = show("LRN400 after \(key)") }
        let k = classify()
        var obs: [String: J] = ["screen": .s(k)]
        if k == "LRN400" {
            obs["page"] = .i(firstInt(labelStarting("Page ")))
            obs["content"] = .s(label("out.content"))
        }
        return obs
    }
    private func flowKiosk(_ c: Case) -> [String: J] {
        let exhibit = c.exhibit ?? "ASHIBATA"
        if !app.buttons["kiosk.\(exhibit)"].exists {
            app.staticTexts["Sign on as a different user profile (kiosk exhibit / MM2024 shared terminal)"].tap()
        }
        app.buttons["kiosk.\(exhibit)"].tap()                                  // STREXHB EXHBNAME(exhibit)
        _ = show("EXHBMENU kiosk for \(exhibit)")
        let options = ["1", "2", "3", "4"].filter { app.buttons["menu.\($0)"].exists }
        var obs: [String: J] = ["screen": .s("KIOSK"), "options": .a(options.map { .s($0) })]
        if let option = c.in?.option {
            var path: [String] = []
            type(app.textFields["option"], option)
            fkey("ENTER")
            var k = show("after option \(option)")
            if k == "NTRSTIT" { path.append(k); fkey("ENTER"); k = show("after NTRSTIT") }
            if k == "VOTE1" {
                path.append(k)
                obs["exhibit"] = .s((app.textFields["inExhb"].value as? String) ?? "")
                fkey("F12"); k = show("after F12")
            }
            if k == "ADMPSWRD" {
                path.append(k)
                type(app.secureTextFields["inPwd"], c.in?.password ?? "")
                fkey("ENTER"); k = show("after password")
            }
            path.append(k)
            obs["path"] = .a(path.map { .s($0) })
            obs["screen"] = .s(k)
        }
        return obs
    }

    // MARK: runner
    func testCrossValidation() throws {
        let catalog = try JSONDecoder().decode(Catalog.self, from: Data(contentsOf: URL(fileURLWithPath: env["VCF_CASES"]!)))
        let only = env["VCF_ONLY"].flatMap { $0.isEmpty ? nil : Set($0.split(separator: ",").map(String.init)) }
        let cases = catalog.cases.filter { only?.contains($0.id) ?? true }
        log("iPad cross-validation: \(cases.count) cases")
        log("baseline: \(json(dump()))")
        for c in cases {
            log("\n\(String(repeating: "=", count: 78))\n\(c.id) \(c.group): \(c.title)\n\(String(repeating: "=", count: 78))")
            for op in c.pre ?? [] { dbOp(op) }
            var obs: [String: J] = [:]
            var error: String?
            switch c.flow {
            case "vote": obs = flowVote(c)
            case "gb_add": obs = flowGbAdd(c)
            case "gb_read": obs = flowGbRead(c)
            case "learn": obs = flowLearn(c)
            case "kiosk": obs = flowKiosk(c)
            default: error = "unknown flow \(c.flow)"
            }
            toMain()
            for op in c.post ?? [] { dbOp(op) }
            if c.flow == "vote" {
                let badge = Int(c.in?.badge ?? "") ?? 0
                obs["vote"] = repo.vote(badge: badge).map { .o(["badge": .i($0.badgenbr), "award": .i($0.awardnbr), "exhibit": .s($0.exhbnbr)]) } ?? .null
            }
            if c.flow == "gb_add", let id = c.expect.comment?.id {
                let row = repo.allComments().first { $0.cmtid == id }
                obs["comment"] = .o(["id": .i(id), "row": row.map { .o(["id": .i($0.cmtid), "visible": .s($0.visible), "exhibit": .s($0.exhbid), "name": .s($0.guestname), "comment": .s($0.guestcmt)]) } ?? .null])
            }
            out.cases[c.id] = Entry(observed: obs, error: error)
            out.finished = iso(Date())
            log("[ipad] \(c.id) \(c.title)\n    -> \(json(obs))\(error.map { "\n    !! \($0)" } ?? "")")
            save()
        }
        log("final DB: \(json(dump()))")
    }

    private func json<T: Encodable>(_ v: T) -> String {
        let e = JSONEncoder(); e.outputFormatting = [.sortedKeys, .withoutEscapingSlashes]
        return String(data: (try? e.encode(v)) ?? Data(), encoding: .utf8) ?? ""
    }
    private func save() {
        guard !resultsPath.isEmpty else { return }
        let e = JSONEncoder(); e.outputFormatting = [.prettyPrinted, .sortedKeys, .withoutEscapingSlashes]
        try? FileManager.default.createDirectory(atPath: (resultsPath as NSString).deletingLastPathComponent, withIntermediateDirectories: true)
        try? e.encode(out).write(to: URL(fileURLWithPath: resultsPath))
    }
}
