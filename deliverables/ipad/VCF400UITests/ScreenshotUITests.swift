import XCTest

/// 成果物用スクリーンショット。環境変数 VCF_SHOTS_DIR が設定されているときのみ PNG を書き出す。
final class ScreenshotUITests: XCTestCase {
    var app: XCUIApplication!
    var dir: String? { ProcessInfo.processInfo.environment["VCF_SHOTS_DIR"] }

    override func setUp() {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launchArguments = ["-VCF_RESET", "YES", "-VCF_PROFILE", "ASHIBATA"]
        app.launch()
    }

    private func shot(_ name: String) {
        guard let d = dir else { return }
        let png = XCUIScreen.main.screenshot().pngRepresentation
        try? FileManager.default.createDirectory(atPath: d, withIntermediateDirectories: true)
        try? png.write(to: URL(fileURLWithPath: d).appendingPathComponent(name + ".png"))
    }

    func testCaptureAllScreens() throws {
        try XCTSkipIf(dir == nil, "VCF_SHOTS_DIR not set")
        XCTAssertTrue(app.staticTexts["AS/400 DEMO MENU"].waitForExistence(timeout: 5)); shot("01_vcfmain")
        app.buttons["menu.11"].tap(); _ = app.staticTexts["VCF/400 - How to Navigate"].waitForExistence(timeout: 3); shot("02_navigate")
        app.buttons["ENTER"].tap(); _ = app.staticTexts["NOMINATE EXHIBIT FOR AWARD"].waitForExistence(timeout: 3); shot("03_vote1")
        app.buttons["F5"].tap(); _ = app.staticTexts["errline"].waitForExistence(timeout: 3); shot("04_vote1_err")
        app.textFields["inputBadge"].tap(); app.textFields["inputBadge"].typeText("8801")
        app.textFields["inputAward"].tap(); app.textFields["inputAward"].typeText("1")
        app.buttons["F5"].tap(); _ = app.staticTexts["THANK YOU FOR VOTING!"].waitForExistence(timeout: 3); shot("05_voteend")
        app.buttons["ENTER"].tap()
        app.buttons["menu.12"].tap(); app.buttons["ENTER"].tap()
        _ = app.staticTexts["GUESTBOOK/400 - ADD COMMENT"].waitForExistence(timeout: 3); shot("07_addcmt")
        app.textFields["inName"].tap(); app.textFields["inName"].typeText("XCUITest")
        app.textFields["inCmt"].tap(); app.textFields["inCmt"].typeText("Screenshot run")
        app.buttons["F5"].tap(); _ = app.staticTexts["THANKS FOR COMMENTING!"].waitForExistence(timeout: 3); shot("08_endcmt")
        app.buttons["ENTER"].tap()
        app.buttons["menu.13"].tap(); app.buttons["ENTER"].tap()
        _ = app.staticTexts["GUESTBOOK/400 - Read a Comment"].waitForExistence(timeout: 3)
        app.textFields["inCmtId"].tap(); app.textFields["inCmtId"].typeText("1"); app.buttons["F5"].tap()
        _ = app.staticTexts["out.cmt"].waitForExistence(timeout: 3); shot("09_readcmt")
        app.buttons["F12"].tap()
        app.buttons["menu.1"].tap(); _ = app.staticTexts["LEARN/400"].waitForExistence(timeout: 3); shot("10_lrn400")
        app.buttons["F5"].tap(); sleep(1); shot("11_lrn400_p2")
        app.buttons["F3"].tap()

        app.terminate()
        app.launchArguments = ["-VCF_KIOSK", "ASHIBATA"]
        app.launch()
        _ = app.staticTexts["WELCOME TO..."].waitForExistence(timeout: 5); shot("06_kiosk")
        app.textFields["option"].tap(); app.textFields["option"].typeText("7"); app.buttons["ENTER"].tap()
        _ = app.secureTextFields["inPwd"].waitForExistence(timeout: 3); shot("12_admpswrd")
    }
}
