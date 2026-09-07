import XCTest

/// 画面遷移 (VCFMAIN → 各機能) と見出し文言・F キーラベルのスモークテスト。
final class SmokeUITests: XCTestCase {
    var app: XCUIApplication!

    override func setUp() {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launchArguments = ["-VCF_RESET", "YES", "-VCF_PROFILE", "ASHIBATA"]
        app.launch()
    }

    func testMainMenuToVoteAndValidation() {
        XCTAssertTrue(app.staticTexts["AS/400 DEMO MENU"].waitForExistence(timeout: 5))
        app.buttons["menu.11"].tap()
        XCTAssertTrue(app.staticTexts["VCF/400 - How to Navigate"].waitForExistence(timeout: 3))
        app.buttons["ENTER"].tap()
        XCTAssertTrue(app.staticTexts["NOMINATE EXHIBIT FOR AWARD"].waitForExistence(timeout: 3))
        app.buttons["F5"].tap()
        XCTAssertTrue(app.staticTexts["Must enter Award ID"].waitForExistence(timeout: 3))   // badge/award empty → last error wins
        app.buttons["F12"].tap()
        XCTAssertTrue(app.staticTexts["AS/400 DEMO MENU"].waitForExistence(timeout: 3))
    }

    func testKioskHidesOptionsAndPassword() {
        app.terminate()
        app.launchArguments = ["-VCF_RESET", "YES", "-VCF_KIOSK", "NOVOTE"]
        app.launch()
        XCTAssertTrue(app.staticTexts["WELCOME TO..."].waitForExistence(timeout: 5))
        XCTAssertFalse(app.buttons["menu.1"].exists)
        XCTAssertFalse(app.buttons["menu.2"].exists)
        XCTAssertTrue(app.buttons["menu.3"].exists)
        let opt = app.textFields["option"]
        opt.tap(); opt.typeText("7"); app.buttons["ENTER"].tap()
        XCTAssertTrue(app.secureTextFields["inPwd"].waitForExistence(timeout: 3))
        app.secureTextFields["inPwd"].tap(); app.secureTextFields["inPwd"].typeText("wrong")
        app.buttons["ENTER"].tap()
        XCTAssertTrue(app.staticTexts["WELCOME TO..."].waitForExistence(timeout: 3))
    }
}
