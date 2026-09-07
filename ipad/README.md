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

## UI 日本語化

SwiftUI の表示ラベル、ボタン、案内、入力プレースホルダー、エラーメッセージ、完了メッセージ、ナビゲーション文言は日本語化しています。台帳で保存する画面タイトル（例: `AS/400 DEMO MENU`、`LEARN/400`、`WELCOME TO...`）は英語のまま独立した `Text` とし、その横または下に日本語の副題を表示します。エラー行は日本語本文を太字で表示し、`errline_ja` として、元の英語（`VCF400Kit.Messages` の値）を小さく `errline` として併記します。展示名、アワード名・説明、コメント本文、LEARN/400 の内容、`Name Hidden`、キオスクボタンのプロファイル ID など DB 由来の値は翻訳しません。

| ID | 英語原文 | 日本語訳 |
|---|---|---|
| M-02 | Must enter badge number | バッジ番号を入力してください |
| M-03 | Must enter Exhibit ID | 展示 ID を入力してください |
| M-04 | Must enter Award ID | アワード ID を入力してください |
| M-05 | You have already voted. | すでに投票済みです。 |
| M-07 | Exhibit ineligible for award | この展示はアワード対象外です |
| M-08 | Exhibit does not exist | 展示が存在しません |
| M-09 | Award does not exist. | アワードが存在しません。 |
| M-10 | Must enter your name | お名前を入力してください |
| M-11 | Must enter a comment | コメントを入力してください |
| M-12 | Must enter CommentID | コメント ID を入力してください |
| M-13 | Name Hidden | Name Hidden（DB 由来の値のため原文維持） |
| M-16 | Your vote has been RECORDED! | 投票を記録しました！ |
| M-17 | Thank you for commenting on this exhibit! Your Comment ID Is: | この展示にコメントをお寄せいただきありがとうございます。コメント ID: |
| M-19 | Currently hosting N comments and counting. | 現在のコメント総数: N |
| M-20 | Are you sure you want to exit the kiosk? | キオスクを終了しますか？ |
| M-21 | Before you start, here is how to navigate: | 開始する前に、操作方法をご案内します。 |

観測 JSON の形は維持し、画面コードは `screen`、エラー原文と訳は `errline` / `errline_ja`、コメント総数は `out.total`、LEARN/400 のページは `out.page` で取得します。
