# VCF/400 iPad (Swift/SwiftUI 版)

VCF/400 (IBM i / RPG IV) の iPadOS マイグレーション。
仕様は `../deliverables/spec/VCF400_spec.md` を正とし、画面構造は「元画面構造の台帳 (L-xx)」に 1 対 1 で対応する。

- `VCF400Kit/` … 業務ロジック (Swift Package)。`Database` (SQLite に DDS と同一の表・列・キーを再現)、
  `Repositories` (CHAIN / SETLL+READ)、`VoteService` / `GuestbookService` / `LearnService` / `KioskService`。
  Java 版 `com.vcf400.service.*` と同じ判定順・同じメッセージ文言。`swift test` で macOS 上でも検証できる。
- `VCF400/` … SwiftUI アプリ (iPad 専用)。`NavigationStack` で VCFMAIN → 各機能の階層を再現。
- `VCF400UITests/` … XCUITest (スモーク / スクリーンショット / クロス検証ケース実行)。
- `project.yml` … [XcodeGen](https://github.com/yonaskolb/XcodeGen) 定義。`xcodegen generate` で `VCF400.xcodeproj` を生成する。

## ビルド・テスト (シミュレータ、署名不要)

```sh
brew install xcodegen && xcodegen generate
cd VCF400Kit && swift test && cd ..
xcodebuild -project VCF400.xcodeproj -scheme VCF400 \
  -destination 'platform=iOS Simulator,name=iPad Pro 13-inch (M5)' -derivedDataPath build test
```

## 起動引数

| 引数 | 意味 |
|---|---|
| `-VCF_PROFILE ASHIBATA` | サインオンユーザー (LAUNCH)。`MM2024` で共用端末 (展示 ID 入力可) |
| `-VCF_KIOSK ASHIBATA` | `STREXHB EXHBNAME(ASHIBATA)` 相当 (キオスクメニューから開始) |
| `-VCF_DB <path>` | SQLite ファイルパス (既定: Documents/vcf400.sqlite) |
| `-VCF_RESET YES` | 起動時に DB を初期データで再作成 |

DB 状態の確認は `xcrun simctl get_app_container booted com.vcf400.ipad data` 配下の `Documents/vcf400.sqlite` を `sqlite3` で参照する。
