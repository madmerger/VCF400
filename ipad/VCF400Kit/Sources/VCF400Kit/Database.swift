import Foundation
import SQLite3

/// DB2 for i (DDS 物理ファイル) と同じ表名・列名・キーを SQLite 上に再現する。
/// SQL は Java 版 (H2 DB2 モード) と同一の文を使い、3 環境で DB 状態を比較できるようにする。
public final class Database {
    public enum DBError: Error, CustomStringConvertible {
        case open(String)
        case sql(String, String)
        public var description: String {
            switch self {
            case .open(let m): return "open: \(m)"
            case .sql(let s, let m): return "\(m) in [\(s)]"
            }
        }
    }

    private var db: OpaquePointer?
    private let queue = DispatchQueue(label: "vcf400.db")
    public let path: String

    public init(path: String) throws {
        self.path = path
        if sqlite3_open(path, &db) != SQLITE_OK {
            throw DBError.open(String(cString: sqlite3_errmsg(db)))
        }
        try execute("PRAGMA journal_mode=WAL")
        try createSchema()
        try seed()
    }

    public convenience init(inMemory: Bool) throws {
        try self.init(path: inMemory ? ":memory:" : Database.defaultPath())
    }

    public static func defaultPath() -> String {
        let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        return dir.appendingPathComponent("vcf400.sqlite").path
    }

    deinit { sqlite3_close(db) }

    // MARK: - schema (schema.sql と同一構造)

    private func createSchema() throws {
        let ddl = [
            """
            CREATE TABLE IF NOT EXISTS EXHBDB (
              EXHBDBID  SMALLINT     NOT NULL DEFAULT 0,
              EXHUSRPRF CHAR(9)      NOT NULL,
              EXHBITOR  CHAR(20)     NOT NULL DEFAULT '',
              EXHBCITY  CHAR(20)     NOT NULL DEFAULT '',
              EXHBSTATE CHAR(2)      NOT NULL DEFAULT '',
              EXHBTITLE CHAR(50)     NOT NULL DEFAULT '',
              EXHBDESC  CHAR(1000)   NOT NULL DEFAULT '',
              ELIGIBLE  SMALLINT     NOT NULL DEFAULT 0,
              ENLRN400  SMALLINT     NOT NULL DEFAULT 0,
              PRIMARY KEY (EXHUSRPRF)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS VOTINGDB (
              BADGENBR SMALLINT NOT NULL,
              AWARDNBR SMALLINT NOT NULL DEFAULT 0,
              EXHBNBR  CHAR(9)  NOT NULL DEFAULT '',
              PRIMARY KEY (BADGENBR)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS GUESTBKDB (
              CMTID     SMALLINT  NOT NULL,
              VISIBLE   CHAR(1)   NOT NULL DEFAULT 'Y',
              EXHBID    CHAR(9)   NOT NULL DEFAULT '',
              GUESTNAME CHAR(20)  NOT NULL DEFAULT '',
              GUESTCMT  CHAR(200) NOT NULL DEFAULT '',
              PRIMARY KEY (CMTID)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS AWARDDB (
              AWARDID    SMALLINT   NOT NULL,
              AWARDTITLE CHAR(100)  NOT NULL DEFAULT '',
              AWARDDESC  CHAR(1000) NOT NULL DEFAULT '',
              PRIMARY KEY (AWARDID)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS SETTINGS (
              SETTING CHAR(9) NOT NULL,
              VALUE   CHAR(9) NOT NULL DEFAULT '',
              PRIMARY KEY (SETTING)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS SECOFRS (
              USERPROF CHAR(9) NOT NULL,
              PRIMARY KEY (USERPROF)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS LRN400STR (
              PAGENBR SMALLINT   NOT NULL,
              CONTENT CHAR(1500) NOT NULL DEFAULT '',
              EXTRA   CHAR(9)    NOT NULL DEFAULT '',
              PRIMARY KEY (PAGENBR)
            )
            """,
        ]
        for s in ddl { try execute(s) }
    }

    /// data.sql と同一の参照データ (PUB400 ASHIBATA2 の観測値)。
    private func seed() throws {
        try execute("INSERT OR REPLACE INTO AWARDDB (AWARDID, AWARDTITLE, AWARDDESC) VALUES (1,'Best in Show Award','This award is given to the exhibit who you believe to be the best in show for 2024.'),(2,'The Ed Fair Award','This award is given to the exhibit that is deemed the most informative of the show.')")
        try execute("""
            INSERT OR REPLACE INTO EXHBDB (EXHBDBID, EXHUSRPRF, EXHBITOR, EXHBCITY, EXHBSTATE, EXHBTITLE, EXHBDESC, ELIGIBLE, ENLRN400) VALUES
            (1,'ASHIBATA','Akira Shibata','Tokyo','JP','IBM i on PUB400 Demo','VCF/400 demo exhibit running on pub400.com',1,1),
            (2,'DEMO400','Demo Exhibitor','Mountain View','CA','AS/400 Model 150','A vintage AS/400 9401-150 exhibit',1,0),
            (3,'NOVOTE','Ineligible Exhibitor','Atlanta','GA','Ineligible test exhibit (ELIGIBLE=0)','Test data for eligibility check',0,0)
            """)
        try execute("INSERT OR IGNORE INTO SETTINGS (SETTING, VALUE) VALUES ('ADMPSWRD','VCF2024'),('ALWVOTE','Y')")
        try execute("""
            INSERT OR REPLACE INTO LRN400STR (PAGENBR, CONTENT, EXTRA) VALUES
            (1,'Welcome to LEARN/400! This is page 1. Press F5 for next page, F8 for previous, F3 to exit.',''),
            (2,'The AS/400 was introduced by IBM in June 1988 ... page 2',''),
            (3,'This is the last page. Thank you for visiting VCF/400.','END')
            """)
        try execute("INSERT OR IGNORE INTO VOTINGDB (BADGENBR, AWARDNBR, EXHBNBR) VALUES (1,1,'ASHIBATA'),(28,2,'ASHIBATA')")
        try execute("INSERT OR IGNORE INTO GUESTBKDB (CMTID, VISIBLE, EXHBID, GUESTNAME, GUESTCMT) VALUES (1,'Y','ASHIBATA','Great exhibit','VCF/400 running on PUB400.'),(2,'Y','ASHIBATA','Devin','VCF/400 is running on PUB400.')")
    }

    // MARK: - low level

    public func execute(_ sql: String, _ params: [Any] = []) throws {
        try queue.sync {
            var stmt: OpaquePointer?
            guard sqlite3_prepare_v2(db, sql, -1, &stmt, nil) == SQLITE_OK else {
                throw DBError.sql(sql, String(cString: sqlite3_errmsg(db)))
            }
            defer { sqlite3_finalize(stmt) }
            bind(stmt, params)
            let rc = sqlite3_step(stmt)
            guard rc == SQLITE_DONE || rc == SQLITE_ROW else {
                throw DBError.sql(sql, String(cString: sqlite3_errmsg(db)))
            }
        }
    }

    public var changes: Int { Int(sqlite3_changes(db)) }

    public func query<T>(_ sql: String, _ params: [Any] = [], map: (Row) -> T) throws -> [T] {
        try queue.sync {
            var stmt: OpaquePointer?
            guard sqlite3_prepare_v2(db, sql, -1, &stmt, nil) == SQLITE_OK else {
                throw DBError.sql(sql, String(cString: sqlite3_errmsg(db)))
            }
            defer { sqlite3_finalize(stmt) }
            bind(stmt, params)
            var out: [T] = []
            while sqlite3_step(stmt) == SQLITE_ROW {
                out.append(map(Row(stmt: stmt!)))
            }
            return out
        }
    }

    private func bind(_ stmt: OpaquePointer?, _ params: [Any]) {
        let transient = unsafeBitCast(-1, to: sqlite3_destructor_type.self)
        for (i, p) in params.enumerated() {
            let idx = Int32(i + 1)
            switch p {
            case let v as Int: sqlite3_bind_int64(stmt, idx, Int64(v))
            case let v as String: sqlite3_bind_text(stmt, idx, v, -1, transient)
            default: sqlite3_bind_null(stmt, idx)
            }
        }
    }

    /// 1 行。CHAR(n) の末尾空白は DB2 と同様に読み出し時にトリムする (RPG の比較は空白埋め等価)。
    public struct Row {
        let stmt: OpaquePointer
        public func int(_ i: Int32) -> Int { Int(sqlite3_column_int64(stmt, i)) }
        public func str(_ i: Int32) -> String {
            guard let c = sqlite3_column_text(stmt, i) else { return "" }
            return String(cString: c).trimmingTrailingSpaces()
        }
    }
}

extension String {
    func trimmingTrailingSpaces() -> String {
        var s = Substring(self)
        while s.last == " " { s.removeLast() }
        return String(s)
    }
    /// RPG の固定長フィールドへの代入 (左詰め、超過分は切り捨て)。
    public func left(_ n: Int) -> String { count <= n ? self : String(prefix(n)) }
}
