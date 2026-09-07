import XCTest
@testable import VCF400Kit

final class DatabaseTests: XCTestCase {
    func testDatabasePreservesExistingSeedDataAndSeedsFreshDatabase() throws {
        let directory = FileManager.default.temporaryDirectory
        let path = directory.appendingPathComponent("vcf400-\(UUID().uuidString).sqlite").path
        defer {
            try? FileManager.default.removeItem(atPath: path)
            try? FileManager.default.removeItem(atPath: path + "-wal")
            try? FileManager.default.removeItem(atPath: path + "-shm")
        }

        do {
            let db = try Database(path: path)
            try db.execute("UPDATE EXHBDB SET EXHBTITLE='X' WHERE EXHUSRPRF='ASHIBATA'")
        }
        do {
            let db = try Database(path: path)
            let title = try db.query("SELECT EXHBTITLE FROM EXHBDB WHERE EXHUSRPRF='ASHIBATA'") { $0.str(0) }.first
            XCTAssertEqual(title, "X")
        }

        let freshPath = directory.appendingPathComponent("vcf400-\(UUID().uuidString).sqlite").path
        defer {
            try? FileManager.default.removeItem(atPath: freshPath)
            try? FileManager.default.removeItem(atPath: freshPath + "-wal")
            try? FileManager.default.removeItem(atPath: freshPath + "-shm")
        }
        let fresh = try Database(path: freshPath)
        let exhibitCount = try fresh.query("SELECT COUNT(*) FROM EXHBDB") { $0.int(0) }.first
        let awardCount = try fresh.query("SELECT COUNT(*) FROM AWARDDB") { $0.int(0) }.first
        let pageCount = try fresh.query("SELECT COUNT(*) FROM LRN400STR") { $0.int(0) }.first
        let adminPassword = try fresh.query("SELECT VALUE FROM SETTINGS WHERE SETTING='ADMPSWRD'") { $0.str(0) }.first
        XCTAssertEqual(exhibitCount, 3)
        XCTAssertEqual(awardCount, 2)
        XCTAssertEqual(pageCount, 3)
        XCTAssertEqual(adminPassword, "VCF2024")
    }
}
