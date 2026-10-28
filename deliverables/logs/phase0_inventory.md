# VCF400 既存移行資産 棚卸し表（フェーズ0 / s0.2）

- 作成日: 2026-10-28
- 対象リポジトリ: madmerger/VCF400（基準: master `81c8aaa` = AGENTS.md 追加マージ後）
- 方針（決定済み d-reuse）: 既存資産を土台に再利用し、全項目を検証し直す。本表は「どのファイルを、どのフェーズで直すか」の作業範囲表として使う。
- 表記: PUB400 のログイン ID と同名の文字列（ライブラリ名の接頭辞・展示 ID・ユーザープロファイル）は本書では書かず、ライブラリは「検証用ライブラリ（ソース）／（オブジェクト）」、展示 ID は「検証用展示 ID」と記す。
- 判定記号: ○ 再利用可（軽微修正のみ） / △ 修正して再利用 / ▲ 要作り直し・要再検証 / － 対象外

## 1. ブランチ一覧（master 未マージ）

すべて master `81c8aaa` から分岐し、master に対する遅れは 0 コミット（コンフリクトなし）。

### 1.1 主要ブランチ（再利用対象）

| ブランチ | PR | 内容 | master との差分 | 先頭コミット | 判定 |
|---|---|---|---|---|---|
| `devin/1791969521-vcf400-pub400-spec` | [#12](https://github.com/madmerger/VCF400/pull/12)（Open） | PUB400 ソース再ビルド記録、仕様書・画面台帳・トレーサビリティ（MD+PDF）、`cases.json` 37 ケース、ランナー（PUB400 / Java / iPad）、比較・レポート生成ツール、結果 JSON、動画 3 本 | 97 ファイル追加 / +51,249 行（`deliverables/` と `.gitignore` のみ） | `e51a3b5` | △ |
| `devin/1791970110-vcf400-java-web` | [#11](https://github.com/madmerger/VCF400/pull/11)（Open） | Spring Boot 3.3 / Java 17 / Thymeleaf / H2 `MODE=DB2` の Java Web 版。JUnit 53 件 | 57 ファイル追加 / +3,263 行（`deliverables/java/` のみ） | `26f53b8` | △ |
| `devin/1791959810-pub400-e2e-report` | [#10](https://github.com/madmerger/VCF400/pull/10)（Open） | PUB400 実 5250（tn5250）での E2E 10 シナリオ TS-01〜10、画面テキスト 56 件、PNG 63 枚、DB 前後証跡、動画、ビルドログ、再現ツール | 207 ファイル追加 / +28,077 行（`deliverables/pub400_e2e/` のみ） | `7185ae6` | △ |
| `devin/1791970125-ipad-deliverables` | [#13](https://github.com/madmerger/VCF400/pull/13)（Open） | SwiftUI + SQLite の iPad 版（`VCF400Kit` 単体テスト 30 件、XCUITest、XcodeGen `project.yml`） | 23 ファイル追加 / +2,476 行（`deliverables/ipad/` のみ） | `e67316a` | △ |

チケット記載の 3 ブランチに加え、#12 のテストレポートが 3 環境目として参照している iPad ブランチ（#13）も再利用対象として載せた。

### 1.2 旧試作ブランチ（再利用しない・参照のみ）

| ブランチ | PR | 差分 | 備考 |
|---|---|---|---|
| `devin/1788362604-rpg-to-java-migration` | #1（Open） | 307 ファイル / +22,228 | 最初期の全自動 Java 移行。現行 #11 に置換済み |
| `devin/1788705434-vcf400-migration` | #2（Open） | 203 ファイル / +73,598 | 旧一括成果物。#12 に置換済み |
| `devin/1788771334-vcf400-deliverables` | #4（Closed） | － | 旧成果物 |
| `devin/1788793956-vcf400-deliverables` | #9（Open） | 130 ファイル / +59,969 | 旧成果物。#12 README の欠落ファイル（後述）はこの系統の名残 |
| `devin/1788796612-java-web-ja` | #7（Open） | 54 ファイル / +2,670 | #11 の移設元（リポジトリ直下 `java/`） |
| `devin/1788796762-ipad-ja` | #8（Open） | 26 ファイル / +2,955 | #13 の移設元（リポジトリ直下 `ipad/`） |
| `devin/1788790707-agents-md-v2` | #6（Closed） | － | AGENTS.md 改訂案（不採用） |
| `devin/update-skills-1788770004` | #3（Open） | 1 ファイル / +33 | testing-java-web スキル案 |

旧試作 PR（#1, #2, #7, #8, #9, #3）は Open のまま残っている。どれを残すかは本チケットの範囲外とし、移行完了時にクローズを判断する。

## 2. ブランチ別 内容と既知の不備

### 2.1 `devin/1791969521-vcf400-pub400-spec`（#12：仕様・テスト基盤・レポート）

主な構成: `deliverables/README.md`、`spec/`（仕様書・画面台帳・トレーサビリティ 各 MD+PDF）、`tests/`（`cases.json`、`run_pub400.py`、`run_java.js`、`run_ipad.sh`、`record_ipad.sh`、`compare.py`、`report.py`、`pub400_db.py`、`results/`）、`logs/`（ビルドログ・リスティング 45 本・`pub400_build.sh`・`pub400_seed.sql`・`tn5250_driver.py`）、`tools/`（`md2pdf.py`、`frames_to_video.js`、`overlay_ipad.js`、`java_screenshots.js`）、`video/` 3 本。テストレポート記載の結果は PUB400 / Java / iPad 各 37/37、3 環境一致 37/37（2026-10-14 実行）。

| # | 不備 | 該当ファイル | 根拠 |
|---|---|---|---|
| S-1 | `cases.json` に VCFMAIN の見出し 3 行（L-01-01: `AS/400 DEMO MENU` / `WELCOME TO...` / `V I N T A G E  C O M P U T E R  F E S T I V A L  2 0 2 4`）の期待値がない。ランナーは VCFMAIN を 1 行目だけで判定している（`run_pub400.py` `classify()`、`run_java.js` は `h1` のみ、iPad はスクリーンコードのみ）。そのため Java で 2・3 行目が欠けていても 37/37 になった（#11 の E2E で発見し `03ed528` で修正済みだが、テスト側は未対応） | `tests/cases.json`、`tests/run_pub400.py`、`tests/run_java.js`、iPad `VCF400UITests/CrossValidationUITests.swift`、`tests/compare.py` | 本調査で grep 確認 |
| S-2 | トレーサビリティの適合マーク（○ 印）が旧試作の状態のまま。パスが `java/...`・`ipad/...`（実体は `deliverables/java`・`deliverables/ipad`）、参照データが `data.sql`（実体は `seed.sql`）、D-07 LRN400STR を「PAGENBR KEY・同」と記載（Java は `LRNFILE`+`PAGENBR` の複合 PK）、件数が「Java 29 件 / iPad 19 件 + XCUITest 3 件」（実体は Java 53 件 / iPad 30 件）。フェーズ4相違一覧の修正内容も試作時点のもの | `spec/VCF400_traceability.md`、`spec/VCF400_traceability.pdf` | 本調査で照合 |
| S-3 | README の成果物一覧が旧系統（#9）のまま。`logs/phase0_inventory.md`、`phase1_01_prepare.log`、`phase1_04_rstlib.log`、`phase1_06_runtime_5250.log`、`phase1_07_runtime_alwvote.log`、`phase1_08_final_seed.log`、`phase4_conformance.md`、`screenshots/java/`、`screenshots/ipad/`、`video/recording_design.md` の 10 件がブランチに存在しない。実装欄が `java/`（PR #7）・`ipad/`（PR #8）を指している | `deliverables/README.md` | 存在チェックで確認 |
| S-4 | PUB400 の READCMT で `errline` を固定位置（4 行目先頭 44 桁）から取っており、エラーがないケース（CV-21〜26）では展示 ID 行を拾っている（観測値が検証用展示 ID になる）。`expect` に `errline` がないため判定には影響しないが、観測値が誤っている | `tests/run_pub400.py`（READCMT 取得部）、`tests/results/pub400.json` | 結果 JSON で確認 |
| S-5 | `compare.py` は `expect` に書かれたキーだけを比較する。見出し・F キー表示・メニュー番号など、台帳で「保持」とした要素はケースに書かない限り検証されない | `tests/compare.py`、`tests/cases.json` | コード確認 |
| S-6 | テストレポートの Java 結果は `03ed528` で取得したもの。#11 の最終 `26f53b8`（CSS 2 コミット）での 37 ケース再実行は「影響なし」とした説明のみ | `tests/VCF400_test_report.md/.pdf`、`tests/results/java.json` | レポート本文 |
| S-7 | ログイン ID と同名の文字列が、成果物 75 ファイル（`cases.json` の `launch` / `exhibit`、シード、レポート、ログ、仕様書）に平文で含まれる。共通ルール（外向け文書に出さない）に反する。展示 ID はテストデータでもあるため、置換方針（別名の展示 ID でシードし直すか、文書側だけ伏せるか）を決める必要がある。PUB400 の実ライブラリ名は AGENTS.md の確定済み前提で、秘密情報ではないため変更しない | `tests/cases.json`、`logs/pub400_seed.sql`、`spec/*.md`、`tests/VCF400_test_report.md` ほか | grep で件数確認 |
| S-8 | Devin Review の未解決スレッド 1 件（`tests/cases.json` 91-92 行 CV-35 の管理者パスワード関連）が残る | `tests/cases.json` | #12 のレビュー |
| S-9 | Phase1 のビルド記録・リスティングが #10 の `pub400_e2e/build/` と重複（`pub400_build.sh` の差分はパス・ログ出力先の 3 行のみ、`pub400_seed.sql` は同一） | `logs/pub400_build.sh`、`logs/pub400_seed.sql`、`logs/pub400_listings/` | diff 確認 |

### 2.2 `devin/1791970110-vcf400-java-web`（#11：Java Web 版）

主な構成: `deliverables/java/`（`pom.xml`、`config/`（`CsrfFilter`、`DataSeeder`）、`domain/`、`repository/`、`service/`（Vote / Guestbook / Learn / Kiosk / Messages）、`web/`（各 Controller、`DbApiController`）、`resources/`（`schema.sql`、`seed.sql`、templates 14 本、`vcf.css`、`vcf.js`）、テスト 6 クラス）。Devin Review は 4 ラウンド対応済みで、未解決スレッドは 0 件。

本調査での再実行: `mvn test`（Maven Central が 429 を返したため、リポジトリ外の Google ミラー設定で実行）→ **53 件実行、失敗 0、エラー 0、スキップ 0**。37 ケースのクロス検証（`run_java.js`）と画面 E2E は本チケットでは再実行していない（未検証）。

| # | 不備 | 該当ファイル |
|---|---|---|
| J-1 | VCFMAIN の見出し 2・3 行目は `03ed528` 以降で追加済み。ただし 37 ケース側で検証されていない（S-1 と同じ問題。テスト基盤側で直す） | `templates/vcfmain.html`（確認のみ） |
| J-2 | 配置が `deliverables/java/`。AGENTS.md は `deliverables/` 配下の固定名リストに `java/` を含めて書いているが、org スキル vcf400-pub400 は「リポジトリ直下の `java/`、`deliverables/` には置かない」と書いており、両者が食い違う。どちらに置くかを確定して AGENTS.md・スキル・トレーサビリティ・README を揃える必要がある | 配置全体、`deliverables/java/README.md` |
| J-3 | 既定設定では DB API・サインオン・管理者パスワードがすべて無効で、クロス検証には `VCF_DB_API=true VCF_SEED_ADMPSWRD=...` が必要。テスト手順書と README に起動条件を明記する | `src/main/resources/application.properties`、`README.md` |
| J-4 | `mvn test` のログに `LRN400STR を LRNFILE 付きスキーマへアップグレードしました` が毎回出る。テスト用 DB でもアップグレード経路を通っている。仕様上の問題ではないが、新規 DB で旧スキーマ扱いになっていないか確認が必要 | `config/DataSeeder.java`、`DataSeederTest.java` |
| J-5 | 再検証（d-reuse）: 業務ルール B-01〜B-15、検証 V-01〜V-21、台帳 L-xx-yy を、トレーサビリティの記載ではなくコードで 1 件ずつ照合し直す | `service/*.java`、`templates/*.html` |

### 2.3 `devin/1791959810-pub400-e2e-report`（#10：PUB400 E2E 10 シナリオ）

主な構成: `deliverables/pub400_e2e/`（`test_scenarios.md`、`VCF400_PUB400_E2E_test_report.md/.pdf`、`results/`（`results.json`、`screens/` 56 件、`db_*`/`reset_*`）、`screenshots/` 63 枚、`video/pub400_e2e.mp4`（字幕付き編集版）、`build/`、`tools/`（`pub400_build.sh`、`pub400_db.sh`、`pub400_seed.sql`、`make_report.py`））。結果は 10/10 PASS（50 ステップ + 補助確認 6 件、2026-10-14）。

| # | 不備 | 該当ファイル |
|---|---|---|
| E-1 | 期待値の根拠が DeepWiki とソースの読み取りで、#12 の `cases.json`（PUB400 実測値）・画面台帳 L-xx と ID が紐付いていない。TS-xx と CV-xx / L-xx の対応表がない | `test_scenarios.md`、`VCF400_PUB400_E2E_test_report.md` |
| E-2 | PR 本文の英語見出し（Summary）が組織 PR テンプレート（概要・やったこと・テスト・備考）に沿っていない。本文に検証用ライブラリ名（ログイン ID と同名）が平文で出ている | PR #10 本文 |
| E-3 | ログイン ID と同名の文字列が 88 ファイル（画面テキスト・DB 証跡・レポート・ツール）に含まれる（S-7 と同じ） | `results/screens/*.txt`、`results/db_*.txt`、`VCF400_PUB400_E2E_test_report.md`、`tools/*` |
| E-4 | Devin Review の未解決スレッド 2 件: `tools/pub400_seed.sql` 12 行（公開済みの管理者パスワードでキオスクを終了できる、セキュリティ指摘）、`tools/pub400_build.sh` 27-28 行 | `tools/pub400_seed.sql`、`tools/pub400_build.sh` |
| E-5 | `tools/pub400_seed.sql` 冒頭コメントが「`java/src/main/resources/data.sql` と同一」と書いている（実体は `deliverables/java/.../seed.sql`。さらに Java の `seed.sql` からは管理者パスワードを削除済みのため同一ではない） | `tools/pub400_seed.sql` |
| E-6 | ビルドツール・ログ・リスティングが #12 の `logs/` と重複。正本をどちらに置くか決める必要がある（S-9） | `tools/pub400_build.sh`、`build/` |
| E-7 | 動画は編集版で、AGENTS.md の動画規定（環境ごとに 1 本・テストケース操作のみ・ケース番号と名称を表示）の対象である `video/1_PUB400_RPG.mp4` とは別物。参考資料の扱いにする | `video/pub400_e2e.mp4` |

### 2.4 `devin/1791970125-ipad-deliverables`（#13：iPad 版、参考）

主な構成: `deliverables/ipad/`（`project.yml`、`VCF400/`（App / Views 6 本 / `Strings.swift` / `Theme.swift`）、`VCF400Kit/`（Database / Models / Repositories / Services + テスト 30 件）、`VCF400UITests/`（CrossValidation / Screenshot / Smoke））。Devin Review の未解決スレッドは 0 件。

本チケットの VM は Linux のため、`swift test` と XCUITest は再実行していない（未検証。macOS が必要）。

| # | 不備 | 該当ファイル |
|---|---|---|
| I-1 | LRN400STR に `LRNFILE` 列がなく（PK は `PAGENBR` のみ）、展示ごとの LEARN/400 ファイル（仕様書 §1.2: `LRN400STUB` の `OVRDBF ... TOFILE(VCF/&PROFILE)`）を再現していない。Java（`LRNFILE`+`PAGENBR`、キオスクのオプション 2 は展示ファイル）と挙動が違う | `VCF400Kit/Sources/VCF400Kit/Database.swift`、`Repositories.swift`、`Services.swift`、`Views/LearnKioskViews.swift` |
| I-2 | 配置が `deliverables/ipad/`（J-2 と同じ食い違い） | 配置全体、`README.md` |
| I-3 | 37 ケースの VCFMAIN 判定がスクリーンコードのみ（S-1）。`SmokeUITests` は `WELCOME TO...` だけを確認し、3 行目の祭典名は確認していない | `VCF400UITests/CrossValidationUITests.swift`、`SmokeUITests.swift` |

## 3. 以降のフェーズで直す箇所（ファイル単位の作業範囲）

再利用方針に従い、下表を各チケットの作業範囲とする。「元ブランチ」の資産を master 起点の新ブランチへ取り込み、修正したうえで全項目を再検証する。

| 区分（担当フェーズの目安） | ファイル | 元ブランチ | 作業 | 関連不備 |
|---|---|---|---|---|
| 配置の確定（準備） | `AGENTS.md`、org スキル vcf400-pub400 | master | `java/`・`ipad/` をリポジトリ直下に置くか `deliverables/` 配下に置くかを 1 つに決めて両方を揃える | J-2, I-2 |
| 秘匿表記（準備） | 外向け文書（`spec/*.md`、`tests/VCF400_test_report.md`、`pub400_e2e/*.md`、PR 本文）、テストデータの展示 ID（`tests/cases.json`、各シード、Java `seed.sql`・`DataSeeder.java`、iPad `Database.swift`） | #12, #10, #11, #13 | ログイン ID と同名の文字列（#12: 75、#10: 88、#11: 11、#13: 9 ファイル）を 2 種類に分けて扱う。(1) 外向け文書の表記は「検証用ライブラリ（ソース/オブジェクト）」「検証用展示 ID」に置き換える。(2) テストデータの展示 ID を別名にする場合は、シード・`cases.json` の期待値・各ランナーを同時に揃える。PUB400 の実ライブラリ名（AGENTS.md の確定済み前提）は変更しない。シード SQL のライブラリ修飾子も変えない | S-7, E-3 |
| 仕様・台帳（仕様再検証） | `spec/VCF400_spec.md/.pdf`、`spec/VCF400_screen_ledger.md/.pdf` | #12 | PUB400 実機で全 L-xx-yy・B-xx・V-xx を確認し直す（台帳は凍結してから実装照合する） | d-reuse |
| トレーサビリティ（仕様再検証 → 実装照合） | `spec/VCF400_traceability.md/.pdf` | #12 | パス（`deliverables/java`・`deliverables/ipad` か直下か）、`seed.sql`、D-07 の PK、テスト件数（Java 53 / iPad 30）、適合列の ○ 印をすべてコードと照合して付け直す。iPad の D-07 / F-06 は不一致（I-1）として扱う | S-2, I-1 |
| 共通テストケース（テスト基盤） | `tests/cases.json` | #12 | VCFMAIN 遷移ケース（CV-29, 30, 35 と起動直後）に見出し 3 行の期待値を追加。PUB400 で実測して期待値を確定する | S-1 |
| ランナー（テスト基盤） | `tests/run_pub400.py` | #12 | VCFMAIN の見出し 3 行を観測値に出す。READCMT の `errline` を固定位置でなくエラー行の位置で取る | S-1, S-4 |
| ランナー（テスト基盤） | `tests/run_java.js` | #12 | VCFMAIN の見出し 3 行（`.screen-subtitle`）を観測値に出す | S-1 |
| ランナー（テスト基盤） | iPad `VCF400UITests/CrossValidationUITests.swift`、`tests/run_ipad.sh` | #13, #12 | `main.welcome` 等の見出し 3 行を観測値に出す | S-1, I-3 |
| 比較（テスト基盤） | `tests/compare.py` | #12 | 見出し比較を追加（空白の扱いを含む）。必要なら保持要素のチェックを共通化 | S-1, S-5 |
| PUB400 ツールの一本化（テスト基盤） | `logs/pub400_build.sh`、`logs/pub400_seed.sql`、`pub400_e2e/tools/*`、`tests/pub400_db.py` | #12, #10 | ビルド・シード・DB 操作の正本を 1 か所にし、もう一方は参照にする。コメントの古いパス（`data.sql`）を直す | S-9, E-5, E-6 |
| 管理者パスワード（テスト基盤 / セキュリティ） | `pub400_e2e/tools/pub400_seed.sql`、`pub400_e2e/test_scenarios.md`、`tests/cases.json`（CV-35） | #10, #12 | 公開リポジトリに平文で載るテスト用パスワードの扱いを決め（環境変数から投入する等）、未解決のレビュー指摘 3 件に回答・解決する | S-8, E-4 |
| E2E シナリオ（PUB400 検証） | `pub400_e2e/test_scenarios.md`、`VCF400_PUB400_E2E_test_report.md/.pdf` | #10 | TS-xx と CV-xx / L-xx の対応表を追加。PR 本文を組織テンプレートで書き直す | E-1, E-2 |
| Java 実装（Java） | `deliverables/java/src/main/java/com/vcf400/**`、`templates/*.html`、`application.properties`、`README.md` | #11 | B/V/L を 1 件ずつコードで照合し直す。クロス検証の起動条件を README に明記。`DataSeeder` のアップグレードログを確認 | J-3, J-4, J-5 |
| iPad 実装（iPad） | `VCF400Kit/Sources/VCF400Kit/Database.swift`、`Repositories.swift`、`Services.swift`、`VCF400/Views/LearnKioskViews.swift`、`VCF400Kit/Tests/**` | #13 | LRN400STR に展示別ファイル（`LRNFILE`）を追加し、キオスクのオプション 2 を展示ファイルにする（Java と揃える） | I-1 |
| テストレポート・動画（レポート） | `tests/VCF400_test_report.md/.pdf`、`tests/results/*.json`、`video/1_PUB400_RPG.mp4`・`2_Java_Web.mp4`・`3_iPad.mp4` | #12 | 修正後のランナーで 3 環境を再実行し、最終コミットで結果・動画を取り直す | S-6 |
| 成果物一覧（レポート） | `deliverables/README.md` | #12 | 存在しない 10 件を削除または作成し、実装欄の PR・パスを現行に直す。本書（`logs/phase0_inventory.md`）はこの欄に対応する | S-3 |

## 4. 検証した内容と、検証できなかった内容

| 項目 | 結果 |
|---|---|
| 各ブランチの master との差分（ファイル数・行数・分岐点） | `git diff --stat master...origin/<branch>` で確認 |
| README 記載パスの存在 | 10 件欠落を確認 |
| `cases.json` / ランナー / 比較の見出し判定 | grep・コード読みで確認（見出し 3 行は未判定） |
| トレーサビリティの記載と実体（パス・`seed.sql`・D-07・テスト件数） | 照合で相違を確認 |
| PUB400 READCMT の `errline` 観測値 | `results/pub400.json` で確認 |
| Java `mvn test` | 再実行して 53/53 成功（Google の Maven ミラーを使用） |
| PR #10〜#13 の未解決レビュー | #12: 1 件、#10: 2 件、#11・#13: 0 件 |
| 37 ケースのクロス検証（PUB400 / Java / iPad） | **検証できなかった**（本チケットでは再実行していない。以降のフェーズで再実行する） |
| iPad `swift test`・XCUITest | **検証できなかった**（Linux VM のため。macOS 環境が必要） |
| PUB400 実機の状態（検証用ライブラリのオブジェクト・データ） | **検証できなかった**（本チケットでは PUB400 に接続していない） |
| 動画 3 本・PDF の体裁 | **検証できなかった**（ファイルの存在のみ確認） |
