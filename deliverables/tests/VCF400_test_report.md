---
title: "VCF/400 クロス検証テスト結果レポート"
subtitle: "PUB400 (オリジナル RPG) / Java Web / iPad の 3 環境同一入力比較"
date: "2026-09-06"
---

# VCF/400 クロス検証テスト結果レポート

| 項目 | 内容 |
|---|---|
| 対象 | `madmerger/VCF400` — オリジナル (PUB400 `ASHIBATA2` ライブラリ、ソースからビルド) / Java Web 版 (`java/`) / iPad 版 (`ipad/`) |
| テストケース | `deliverables/tests/cases.json` (37 ケース、仕様書 4 章 業務ルール・5 章 検証ルールから生成) |
| 実行方式 | PUB400: tn5250 自動操作 (`run_pub400.py`) / Java: Playwright headed Chromium (`run_java.js`) / iPad: XCUITest on iPad Pro 13-inch シミュレータ (`run_ipad.sh` → `CrossValidationUITests`) |
| 共通ベースライン | VOTINGDB {1/1/ASHIBATA, 28/2/ASHIBATA}, GUESTBKDB {1,2 (VISIBLE=Y, ASHIBATA)}, SETTINGS {ADMPSWRD=VCF2024, ALWVOTE=Y}, EXHBDB {ASHIBATA(1,1), DEMO400(1,0), NOVOTE(0,0)}, AWARDDB {1,2}, LRN400STR 3 ページ (3 = END)。各環境とも実行前にリセット |
| 判定 | 期待値 (`expect`) の全キーが実測値 (`observed`) と一致 = PASS。画面種別・エラー行・遷移経路・出力項目・DB 状態 (投票行 / コメント行) を比較 |
| E2E 録画 | `deliverables/video/VCF400_cross_validation.mp4` (1600x1200, 22 分 8 秒, 45 MB) — クロス検証工程のみを 1 本で録画 |

## 1. サマリ

| 環境 | PASS | FAIL / 未実行 | 実行開始 (UTC) | 実行終了 (UTC) |
|---|---|---|---|---|
| PUB400 | 37 / 37 | 0 | 2026-09-06T18:05:30Z | 2026-09-06T18:19:16Z |
| Java Web | 37 / 37 | 0 | 2026-09-06T18:19:26.235Z | 2026-09-06T18:21:12.955Z |
| iPad | 37 / 37 | 0 | 2026-09-06T18:21:21Z | 2026-09-06T18:27:21Z |
| **3 環境一致** | **37 / 37** | 0 | | |

### 1.1 分類別

| 分類 | 内容 | ケース数 | 3 環境一致 |
|---|---|---|---|
| ADDVOTE | 投票 (F-03): 必須入力 V-01..V-03 / 重複・資格・存在チェック V-04..V-07 / CHECKOK=4 書込 B-01 / ALWVOTE B-03 | 13 | 13 |
| ADDGBCMT | ゲストブック記入 (F-04): 必須入力 V-10..V-12 / CMTID 採番 B-06 / 展示存在チェックなし B-08 | 6 | 6 |
| READGBCMT | ゲストブック閲覧 (F-05): 必須入力 V-15 / SETLL+READ B-09 / 非表示 B-07 / 帰属外 B-10 / 共用端末 B-11 | 7 | 7 |
| LRN400 | LEARN/400 (F-06): F5 進む / F8 前へ / F3 終了 / EXTRA='END' で終了 B-12 | 4 | 4 |
| EXHBMENU | 展示キオスク (F-07): ELIGIBLE / ENLRN400 によるオプション表示 B-13 / 隠しオプション 7 + ADMPSWRD B-14 | 7 | 7 |

## 2. 不一致と対応

最終実行では 37 ケースすべてが 3 環境で一致した。

### 2.1 クロス検証で検出・修正した相違 (最終実行前に修正済み)

| # | 検出環境 | 内容 | ルート原因 | 対応 |
|---|---|---|---|---|
| 1 | Java / iPad | AWARDDB のタイトルがオリジナル VOTESCR DDS の表示 (`Best in Show Award` / `The Ed Fair Award`) と不一致 | 参照データを PUB400 の観測値ではなく仮値で作成していた | `data.sql` / `Database.swift` の seed を DDS どおりに修正 |
| 2 | iPad | VOTINGDB ベースライン (4992/4993) と LRN400STR 本文が PUB400・Java (1/28, ページ文言) と不一致 | フェーズ 1 の実行時データを seed に採用していた | `Database.swift` の seed を共通ベースラインに統一 (CV-27/28 の `content` 比較が対象) |
| 3 | Java | キオスクメニューでオプション 1/2 が ELIGIBLE / ENLRN400 に関係なく非表示 | Thymeleaf の `th:if` が boolean アクセサ (`isEligible()`) を解決できていなかった | `kiosk.html` を `${exhibit.isEligible()}` 形式に修正 (CV-31..33) |
| 4 | iPad | VCFMAIN のオプション 90 が台帳 L-01-07 と異なるグループに配置 | 画面構成の転記ミス | `MainMenuView.swift` で Administration グループへ移動 |
| 5 | PUB400 | フェーズ 1 の実行で残った投票・コメントがベースラインを汚染 | 共有環境上の実行データ | `pub400_db.py reset` を追加し、実行前に `ASHIBATA2` のデータを共通ベースラインへ戻す |
| 6 | Java (ランナー) | Playwright ランナーが `p.big` 等の非存在セレクタ待ちでタイムアウトし、ブラウザクローズ後に操作していた | `locator.textContent()` が要素出現までブロック | `page.evaluate` による非ブロック取得へ変更 (実装側の相違ではない) |
| 7 | PUB400 (ランナー) | 録画付き 1 回目の全件実行で CV-23 (コメント ID 9999) のみ PUB400 が `Must enter CommentID` となり 36/37 (`results/run1/`) | `INCMTID` は `4Y 0` のため 4 桁入力でカーソルが自動的に次フィールド (=同一フィールド先頭) へ進み、続く Field Exit がフィールドを消去していた | `run_pub400.py` で桁数一杯の入力時は Field Exit を送らないよう修正し、全件を再実行 (実装側の相違ではない: 5250 上で 9999 を手入力すると帰属外メッセージが表示される) |

## 3. ケース一覧 (入力 / 期待値 / 3 環境の判定)

| ID | 分類 | ケース | 入力 | 期待値 | PUB400 | Java | iPad | 3環境一致 |
|---|---|---|---|---|---|---|---|---|
| CV-01 | ADDVOTE | 全項目未入力 (展示 ID は保護・事前入力)<br>(V-01, V-02, B-05, L-03-03) | `badge`=""<br>`award`="" | `{"errline": "Must enter Award ID", "screen": "VOTE1", "vote": null}` | PASS | PASS | PASS | 一致 |
| CV-02 | ADDVOTE | アワード未入力<br>(V-02) | `badge`="7001"<br>`award`="" | `{"errline": "Must enter Award ID", "screen": "VOTE1", "vote": null}` | PASS | PASS | PASS | 一致 |
| CV-03 | ADDVOTE | バッジ未入力<br>(V-01) | `badge`=""<br>`award`="1" | `{"errline": "Must enter badge number", "screen": "VOTE1", "vote": null}` | PASS | PASS | PASS | 一致 |
| CV-04 | ADDVOTE | 正常投票 (新規バッジ 7001, アワード 1)<br>(B-04, V-08, L-03-11) | `badge`="7001"<br>`award`="1" | `{"screen": "VOTEEND", "vote": {"award": 1, "badge": 7001, "exhibit": "ASHIBATA"}}` | PASS | PASS | PASS | 一致 |
| CV-05 | ADDVOTE | 重複バッジ (7001 で再投票)<br>(B-01, V-04) | `badge`="7001"<br>`award`="2" | `{"errline": "You have already voted.", "screen": "VOTE1", "vote": {"award": 1, "badge": 7001, "exhibit": "ASHIBATA"}}` | PASS | PASS | PASS | 一致 |
| CV-06 | ADDVOTE | 存在しないアワード (9)<br>(V-07) | `badge`="7002"<br>`award`="9" | `{"errline": "Award does not exist.", "screen": "VOTE1", "vote": null}` | PASS | PASS | PASS | 一致 |
| CV-07 | ADDVOTE | 境界値: アワード 0 (数値 0 = 未入力扱い)<br>(B-15, V-02) | `badge`="7002"<br>`award`="0" | `{"errline": "Must enter Award ID", "screen": "VOTE1", "vote": null}` | PASS | PASS | PASS | 一致 |
| CV-08 | ADDVOTE | 共用端末: 展示 ID 未入力<br>(V-03, B-13) | `badge`="7002"<br>`exhibit`=""<br>`award`="1" | `{"errline": "Must enter Exhibit ID", "screen": "VOTE1", "vote": null}` | PASS | PASS | PASS | 一致 |
| CV-09 | ADDVOTE | 共用端末: 存在しない展示 NOSUCH<br>(V-06) | `badge`="7002"<br>`exhibit`="NOSUCH"<br>`award`="1" | `{"errline": "Exhibit does not exist", "screen": "VOTE1", "vote": null}` | PASS | PASS | PASS | 一致 |
| CV-10 | ADDVOTE | 共用端末: 資格外展示 NOVOTE (ELIGIBLE=0)<br>(B-02, V-05) | `badge`="7002"<br>`exhibit`="NOVOTE"<br>`award`="1" | `{"errline": "Exhibit ineligible for award", "screen": "VOTE1", "vote": null}` | PASS | PASS | PASS | 一致 |
| CV-11 | ADDVOTE | 共用端末: 正常投票 (7002, DEMO400, アワード 2)<br>(B-04, B-13) | `badge`="7002"<br>`exhibit`="DEMO400"<br>`award`="2" | `{"screen": "VOTEEND", "vote": {"award": 2, "badge": 7002, "exhibit": "DEMO400"}}` | PASS | PASS | PASS | 一致 |
| CV-12 | ADDVOTE | 複合エラー (重複バッジ+存在しない展示+存在しないアワード) → 最後の検査が勝つ<br>(B-05) | `badge`="7001"<br>`exhibit`="NOSUCH"<br>`award`="9" | `{"errline": "Award does not exist.", "screen": "VOTE1", "vote": {"award": 1, "badge": 7001, "exhibit": "ASHIBATA"}}` | PASS | PASS | PASS | 一致 |
| CV-13 | ADDVOTE | 投票停止 ALWVOTE='N' → ENDOFCON<br>(B-03, V-09, L-04-02) | `badge`="7003"<br>`award`="1" | `{"screen": "ENDOFCON", "vote": null}` | PASS | PASS | PASS | 一致 |
| CV-14 | ADDGBCMT | 全項目未入力 (展示 ID は保護・事前入力)<br>(V-11, V-12, B-05) | `name`=""<br>`comment`="" | `{"comment": {"id": 3, "row": null}, "errline": "Must enter a comment", "screen": "ADDCMT"}` | PASS | PASS | PASS | 一致 |
| CV-15 | ADDGBCMT | コメント未入力<br>(V-12) | `name`="Devin Tester"<br>`comment`="" | `{"comment": {"id": 3, "row": null}, "errline": "Must enter a comment", "screen": "ADDCMT"}` | PASS | PASS | PASS | 一致 |
| CV-16 | ADDGBCMT | 名前未入力<br>(V-11) | `name`=""<br>`comment`="Hello" | `{"comment": {"id": 3, "row": null}, "errline": "Must enter your name", "screen": "ADDCMT"}` | PASS | PASS | PASS | 一致 |
| CV-17 | ADDGBCMT | 正常記入 → ENDCMT, CMTID=3 (最終+1), VISIBLE=Y<br>(B-06, L-07-08, L-08-01) | `name`="Devin Tester"<br>`comment`="Cross validation run" | `{"comment": {"id": 3, "row": {"comment": "Cross validation run", "exhibit": "ASHIBATA", "id": 3, "name": "Devin Tester", "visible": "Y"}}, "screen": "ENDCMT"}` | PASS | PASS | PASS | 一致 |
| CV-18 | ADDGBCMT | 共用端末: 展示 ID 未入力<br>(V-10) | `name`="Guest"<br>`exhibit`=""<br>`comment`="x" | `{"comment": {"id": 4, "row": null}, "errline": "Must enter Exhibit ID", "screen": "ADDCMT"}` | PASS | PASS | PASS | 一致 |
| CV-19 | ADDGBCMT | 共用端末: 存在しない展示 NOSUCH でも記入できる (存在チェックなし)<br>(V-13) | `name`="Guest"<br>`exhibit`="NOSUCH"<br>`comment`="Unchecked exhibit" | `{"comment": {"id": 4, "row": {"comment": "Unchecked exhibit", "exhibit": "NOSUCH", "id": 4, "name": "Guest", "visible": "Y"}}, "screen": "ENDCMT"}` | PASS | PASS | PASS | 一致 |
| CV-20 | READGBCMT | コメント ID 未入力<br>(V-14) | `cmtid`="" | `{"errline": "Must enter CommentID", "screen": "READCMT"}` | PASS | PASS | PASS | 一致 |
| CV-21 | READGBCMT | ID 1 → 名前 11 桁 / 展示タイトル / コメント<br>(V-15, L-09-05) | `cmtid`="1" | `{"out": {"cmt": "VCF/400 running on PUB400.", "name": "Great exhib", "title": "IBM i on PUB400 Demo"}, "screen": "READCMT", "total": 4}` | PASS | PASS | PASS | 一致 |
| CV-22 | READGBCMT | ID 3 (この実行で追加したコメント)<br>(B-06) | `cmtid`="3" | `{"out": {"cmt": "Cross validation run", "name": "Devin Teste", "title": "IBM i on PUB400 Demo"}, "screen": "READCMT", "total": 4}` | PASS | PASS | PASS | 一致 |
| CV-23 | READGBCMT | 存在しない ID 9999 → 最終レコード (4, 他展示) → 帰属外メッセージ<br>(B-09, B-08, V-18) | `cmtid`="9999" | `{"out": {"cmt": "This comment is not part of this guestbook.", "name": "", "title": ""}, "screen": "READCMT", "total": 4}` | PASS | PASS | PASS | 一致 |
| CV-24 | READGBCMT | 非表示コメント (ID 2 を VISIBLE=N)<br>(B-07, V-16) | `cmtid`="2" | `{"out": {"cmt": "This comment hidden by an admin - offensive content.", "name": "Name Hidden", "title": ""}, "screen": "READCMT", "total": 4}` | PASS | PASS | PASS | 一致 |
| CV-25 | READGBCMT | 他展示 (DEMO400) から ASHIBATA のコメント 1 を読む → 帰属外<br>(B-08, V-17) | `cmtid`="1" | `{"out": {"cmt": "This comment is not part of this guestbook.", "name": "", "title": ""}, "screen": "READCMT", "total": 4}` | PASS | PASS | PASS | 一致 |
| CV-26 | READGBCMT | 共用端末は全展示を閲覧可 / 展示タイトルは SETLL+READ (NOSUCH → NOVOTE)<br>(B-08, B-09) | `cmtid`="4" | `{"out": {"cmt": "Unchecked exhibit", "name": "Guest", "title": "Ineligible test exhibit (ELIGIBLE=0)"}, "screen": "READCMT", "total": 4}` | PASS | PASS | PASS | 一致 |
| CV-27 | LRN400 | F5 → 2 ページ目<br>(V-19, L-10-04) | `keys`=["F5"] | `{"content": "The AS/400 was introduced by IBM in June 1988 ... page 2", "page": 2, "screen": "LRN400"}` | PASS | PASS | PASS | 一致 |
| CV-28 | LRN400 | F5, F8 → 1 ページ目に戻る<br>(V-20) | `keys`=["F5", "F8"] | `{"content": "Welcome to LEARN/400! This is page 1. Press F5 for next page, F8 for previous, F3 to exit.", "page": 1, "screen": "LRN400"}` | PASS | PASS | PASS | 一致 |
| CV-29 | LRN400 | F5, F5 → 3 ページ目 (EXTRA='END') で終了 → VCFMAIN<br>(B-10) | `keys`=["F5", "F5"] | `{"screen": "VCFMAIN"}` | PASS | PASS | PASS | 一致 |
| CV-30 | LRN400 | F3 → 終了 → VCFMAIN<br>(V-21) | `keys`=["F3"] | `{"screen": "VCFMAIN"}` | PASS | PASS | PASS | 一致 |
| CV-31 | EXHBMENU | ASHIBATA (ELIGIBLE=1, ENLRN400=1) → 1,2,3,4<br>(L-05-04, B-02, B-12) | (なし) | `{"options": ["1", "2", "3", "4"], "screen": "KIOSK"}` | PASS | PASS | PASS | 一致 |
| CV-32 | EXHBMENU | NOVOTE (ELIGIBLE=0, ENLRN400=0) → 3,4 のみ<br>(B-02, B-12) | (なし) | `{"options": ["3", "4"], "screen": "KIOSK"}` | PASS | PASS | PASS | 一致 |
| CV-33 | EXHBMENU | DEMO400 (ELIGIBLE=1, ENLRN400=0) → 1,3,4<br>(B-12) | (なし) | `{"options": ["1", "3", "4"], "screen": "KIOSK"}` | PASS | PASS | PASS | 一致 |
| CV-34 | EXHBMENU | 隠しオプション 7 + 誤パスワード → キオスクメニューに戻る<br>(B-11, L-06-03) | `option`="7"<br>`password`="WRONGPW" | `{"options": ["1", "2", "3", "4"], "path": ["ADMPSWRD", "KIOSK"], "screen": "KIOSK"}` | PASS | PASS | PASS | 一致 |
| CV-35 | EXHBMENU | 隠しオプション 7 + 正パスワード (SETTINGS.ADMPSWRD) → キオスク終了 → VCFMAIN<br>(B-11, L-05-06) | `option`="7"<br>`password`="VCF2024" | `{"options": ["1", "2", "3", "4"], "path": ["ADMPSWRD", "VCFMAIN"], "screen": "VCFMAIN"}` | PASS | PASS | PASS | 一致 |
| CV-36 | EXHBMENU | オプション 1 → NTRSTIT → VOTE1 (展示 ASHIBATA 固定) → F12 でキオスクへ戻る<br>(L-05-08, L-02-04, B-13) | `option`="1" | `{"exhibit": "ASHIBATA", "options": ["1", "2", "3", "4"], "path": ["NTRSTIT", "VOTE1", "KIOSK"], "screen": "KIOSK"}` | PASS | PASS | PASS | 一致 |
| CV-37 | EXHBMENU | 資格外展示でオプション 1 を入力 → 無視 (キオスクメニューのまま)<br>(B-02, L-05-04) | `option`="1" | `{"options": ["3", "4"], "path": ["KIOSK"], "screen": "KIOSK"}` | PASS | PASS | PASS | 一致 |

### 実測値 (環境別)

#### CV-01 全項目未入力 (展示 ID は保護・事前入力)

- PUB400: `{"errline": "Must enter Award ID", "screen": "VOTE1", "vote": null}`
- Java Web: `{"errline": "Must enter Award ID", "screen": "VOTE1", "vote": null}`
- iPad: `{"errline": "Must enter Award ID", "screen": "VOTE1", "vote": null}`

#### CV-02 アワード未入力

- PUB400: `{"errline": "Must enter Award ID", "screen": "VOTE1", "vote": null}`
- Java Web: `{"errline": "Must enter Award ID", "screen": "VOTE1", "vote": null}`
- iPad: `{"errline": "Must enter Award ID", "screen": "VOTE1", "vote": null}`

#### CV-03 バッジ未入力

- PUB400: `{"errline": "Must enter badge number", "screen": "VOTE1", "vote": null}`
- Java Web: `{"errline": "Must enter badge number", "screen": "VOTE1", "vote": null}`
- iPad: `{"errline": "Must enter badge number", "screen": "VOTE1", "vote": null}`

#### CV-04 正常投票 (新規バッジ 7001, アワード 1)

- PUB400: `{"screen": "VOTEEND", "vote": {"award": 1, "badge": 7001, "exhibit": "ASHIBATA"}}`
- Java Web: `{"screen": "VOTEEND", "vote": {"award": 1, "badge": 7001, "exhibit": "ASHIBATA"}}`
- iPad: `{"screen": "VOTEEND", "vote": {"award": 1, "badge": 7001, "exhibit": "ASHIBATA"}}`

#### CV-05 重複バッジ (7001 で再投票)

- PUB400: `{"errline": "You have already voted.", "screen": "VOTE1", "vote": {"award": 1, "badge": 7001, "exhibit": "ASHIBATA"}}`
- Java Web: `{"errline": "You have already voted.", "screen": "VOTE1", "vote": {"award": 1, "badge": 7001, "exhibit": "ASHIBATA"}}`
- iPad: `{"errline": "You have already voted.", "screen": "VOTE1", "vote": {"award": 1, "badge": 7001, "exhibit": "ASHIBATA"}}`

#### CV-06 存在しないアワード (9)

- PUB400: `{"errline": "Award does not exist.", "screen": "VOTE1", "vote": null}`
- Java Web: `{"errline": "Award does not exist.", "screen": "VOTE1", "vote": null}`
- iPad: `{"errline": "Award does not exist.", "screen": "VOTE1", "vote": null}`

#### CV-07 境界値: アワード 0 (数値 0 = 未入力扱い)

- PUB400: `{"errline": "Must enter Award ID", "screen": "VOTE1", "vote": null}`
- Java Web: `{"errline": "Must enter Award ID", "screen": "VOTE1", "vote": null}`
- iPad: `{"errline": "Must enter Award ID", "screen": "VOTE1", "vote": null}`

#### CV-08 共用端末: 展示 ID 未入力

- PUB400: `{"errline": "Must enter Exhibit ID", "screen": "VOTE1", "vote": null}`
- Java Web: `{"errline": "Must enter Exhibit ID", "screen": "VOTE1", "vote": null}`
- iPad: `{"errline": "Must enter Exhibit ID", "screen": "VOTE1", "vote": null}`

#### CV-09 共用端末: 存在しない展示 NOSUCH

- PUB400: `{"errline": "Exhibit does not exist", "screen": "VOTE1", "vote": null}`
- Java Web: `{"errline": "Exhibit does not exist", "screen": "VOTE1", "vote": null}`
- iPad: `{"errline": "Exhibit does not exist", "screen": "VOTE1", "vote": null}`

#### CV-10 共用端末: 資格外展示 NOVOTE (ELIGIBLE=0)

- PUB400: `{"errline": "Exhibit ineligible for award", "screen": "VOTE1", "vote": null}`
- Java Web: `{"errline": "Exhibit ineligible for award", "screen": "VOTE1", "vote": null}`
- iPad: `{"errline": "Exhibit ineligible for award", "screen": "VOTE1", "vote": null}`

#### CV-11 共用端末: 正常投票 (7002, DEMO400, アワード 2)

- PUB400: `{"screen": "VOTEEND", "vote": {"award": 2, "badge": 7002, "exhibit": "DEMO400"}}`
- Java Web: `{"screen": "VOTEEND", "vote": {"award": 2, "badge": 7002, "exhibit": "DEMO400"}}`
- iPad: `{"screen": "VOTEEND", "vote": {"award": 2, "badge": 7002, "exhibit": "DEMO400"}}`

#### CV-12 複合エラー (重複バッジ+存在しない展示+存在しないアワード) → 最後の検査が勝つ

- PUB400: `{"errline": "Award does not exist.", "screen": "VOTE1", "vote": {"award": 1, "badge": 7001, "exhibit": "ASHIBATA"}}`
- Java Web: `{"errline": "Award does not exist.", "screen": "VOTE1", "vote": {"award": 1, "badge": 7001, "exhibit": "ASHIBATA"}}`
- iPad: `{"errline": "Award does not exist.", "screen": "VOTE1", "vote": {"award": 1, "badge": 7001, "exhibit": "ASHIBATA"}}`

#### CV-13 投票停止 ALWVOTE='N' → ENDOFCON

- PUB400: `{"screen": "ENDOFCON", "vote": null}`
- Java Web: `{"screen": "ENDOFCON", "vote": null}`
- iPad: `{"screen": "ENDOFCON", "vote": null}`

#### CV-14 全項目未入力 (展示 ID は保護・事前入力)

- PUB400: `{"comment": {"id": 3, "row": null}, "errline": "Must enter a comment", "screen": "ADDCMT"}`
- Java Web: `{"comment": {"id": 3, "row": null}, "errline": "Must enter a comment", "screen": "ADDCMT"}`
- iPad: `{"comment": {"id": 3, "row": null}, "errline": "Must enter a comment", "screen": "ADDCMT"}`

#### CV-15 コメント未入力

- PUB400: `{"comment": {"id": 3, "row": null}, "errline": "Must enter a comment", "screen": "ADDCMT"}`
- Java Web: `{"comment": {"id": 3, "row": null}, "errline": "Must enter a comment", "screen": "ADDCMT"}`
- iPad: `{"comment": {"id": 3, "row": null}, "errline": "Must enter a comment", "screen": "ADDCMT"}`

#### CV-16 名前未入力

- PUB400: `{"comment": {"id": 3, "row": null}, "errline": "Must enter your name", "screen": "ADDCMT"}`
- Java Web: `{"comment": {"id": 3, "row": null}, "errline": "Must enter your name", "screen": "ADDCMT"}`
- iPad: `{"comment": {"id": 3, "row": null}, "errline": "Must enter your name", "screen": "ADDCMT"}`

#### CV-17 正常記入 → ENDCMT, CMTID=3 (最終+1), VISIBLE=Y

- PUB400: `{"comment": {"id": 3, "row": {"comment": "Cross validation run", "exhibit": "ASHIBATA", "id": 3, "name": "Devin Tester", "visible": "Y"}}, "screen": "ENDCMT"}`
- Java Web: `{"comment": {"id": 3, "row": {"comment": "Cross validation run", "exhibit": "ASHIBATA", "id": 3, "name": "Devin Tester", "visible": "Y"}}, "screen": "ENDCMT", "shownId": 3}`
- iPad: `{"comment": {"id": 3, "row": {"comment": "Cross validation run", "exhibit": "ASHIBATA", "id": 3, "name": "Devin Tester", "visible": "Y"}}, "screen": "ENDCMT", "shownId": 3}`

#### CV-18 共用端末: 展示 ID 未入力

- PUB400: `{"comment": {"id": 4, "row": null}, "errline": "Must enter Exhibit ID", "screen": "ADDCMT"}`
- Java Web: `{"comment": {"id": 4, "row": null}, "errline": "Must enter Exhibit ID", "screen": "ADDCMT"}`
- iPad: `{"comment": {"id": 4, "row": null}, "errline": "Must enter Exhibit ID", "screen": "ADDCMT"}`

#### CV-19 共用端末: 存在しない展示 NOSUCH でも記入できる (存在チェックなし)

- PUB400: `{"comment": {"id": 4, "row": {"comment": "Unchecked exhibit", "exhibit": "NOSUCH", "id": 4, "name": "Guest", "visible": "Y"}}, "screen": "ENDCMT"}`
- Java Web: `{"comment": {"id": 4, "row": {"comment": "Unchecked exhibit", "exhibit": "NOSUCH", "id": 4, "name": "Guest", "visible": "Y"}}, "screen": "ENDCMT", "shownId": 4}`
- iPad: `{"comment": {"id": 4, "row": {"comment": "Unchecked exhibit", "exhibit": "NOSUCH", "id": 4, "name": "Guest", "visible": "Y"}}, "screen": "ENDCMT", "shownId": 4}`

#### CV-20 コメント ID 未入力

- PUB400: `{"errline": "Must enter CommentID", "out": {"cmt": "", "name": "", "title": ""}, "screen": "READCMT", "total": 4}`
- Java Web: `{"errline": "Must enter CommentID", "screen": "READCMT", "total": 4}`
- iPad: `{"errline": "Must enter CommentID", "screen": "READCMT", "total": 4}`

#### CV-21 ID 1 → 名前 11 桁 / 展示タイトル / コメント

- PUB400: `{"errline": "ASHIBATA", "out": {"cmt": "VCF/400 running on PUB400.", "name": "Great exhib", "title": "IBM i on PUB400 Demo"}, "screen": "READCMT", "total": 4}`
- Java Web: `{"errline": "", "out": {"cmt": "VCF/400 running on PUB400.", "name": "Great exhib", "title": "IBM i on PUB400 Demo"}, "screen": "READCMT", "total": 4}`
- iPad: `{"errline": "", "out": {"cmt": "VCF/400 running on PUB400.", "name": "Great exhib", "title": "IBM i on PUB400 Demo"}, "screen": "READCMT", "total": 4}`

#### CV-22 ID 3 (この実行で追加したコメント)

- PUB400: `{"errline": "ASHIBATA", "out": {"cmt": "Cross validation run", "name": "Devin Teste", "title": "IBM i on PUB400 Demo"}, "screen": "READCMT", "total": 4}`
- Java Web: `{"errline": "", "out": {"cmt": "Cross validation run", "name": "Devin Teste", "title": "IBM i on PUB400 Demo"}, "screen": "READCMT", "total": 4}`
- iPad: `{"errline": "", "out": {"cmt": "Cross validation run", "name": "Devin Teste", "title": "IBM i on PUB400 Demo"}, "screen": "READCMT", "total": 4}`

#### CV-23 存在しない ID 9999 → 最終レコード (4, 他展示) → 帰属外メッセージ

- PUB400: `{"errline": "ASHIBATA", "out": {"cmt": "This comment is not part of this guestbook.", "name": "", "title": ""}, "screen": "READCMT", "total": 4}`
- Java Web: `{"errline": "", "out": {"cmt": "This comment is not part of this guestbook.", "name": "", "title": ""}, "screen": "READCMT", "total": 4}`
- iPad: `{"errline": "", "out": {"cmt": "This comment is not part of this guestbook.", "name": "", "title": ""}, "screen": "READCMT", "total": 4}`

#### CV-24 非表示コメント (ID 2 を VISIBLE=N)

- PUB400: `{"errline": "ASHIBATA", "out": {"cmt": "This comment hidden by an admin - offensive content.", "name": "Name Hidden", "title": ""}, "screen": "READCMT", "total": 4}`
- Java Web: `{"errline": "", "out": {"cmt": "This comment hidden by an admin - offensive content.", "name": "Name Hidden", "title": ""}, "screen": "READCMT", "total": 4}`
- iPad: `{"errline": "", "out": {"cmt": "This comment hidden by an admin - offensive content.", "name": "Name Hidden", "title": ""}, "screen": "READCMT", "total": 4}`

#### CV-25 他展示 (DEMO400) から ASHIBATA のコメント 1 を読む → 帰属外

- PUB400: `{"errline": "DEMO400", "out": {"cmt": "This comment is not part of this guestbook.", "name": "", "title": ""}, "screen": "READCMT", "total": 4}`
- Java Web: `{"errline": "", "out": {"cmt": "This comment is not part of this guestbook.", "name": "", "title": ""}, "screen": "READCMT", "total": 4}`
- iPad: `{"errline": "", "out": {"cmt": "This comment is not part of this guestbook.", "name": "", "title": ""}, "screen": "READCMT", "total": 4}`

#### CV-26 共用端末は全展示を閲覧可 / 展示タイトルは SETLL+READ (NOSUCH → NOVOTE)

- PUB400: `{"errline": "MM2024", "out": {"cmt": "Unchecked exhibit", "name": "Guest", "title": "Ineligible test exhibit (ELIGIBLE=0)"}, "screen": "READCMT", "total": 4}`
- Java Web: `{"errline": "", "out": {"cmt": "Unchecked exhibit", "name": "Guest", "title": "Ineligible test exhibit (ELIGIBLE=0)"}, "screen": "READCMT", "total": 4}`
- iPad: `{"errline": "", "out": {"cmt": "Unchecked exhibit", "name": "Guest", "title": "Ineligible test exhibit (ELIGIBLE=0)"}, "screen": "READCMT", "total": 4}`

#### CV-27 F5 → 2 ページ目

- PUB400: `{"content": "The AS/400 was introduced by IBM in June 1988 ... page 2", "page": 2, "screen": "LRN400"}`
- Java Web: `{"content": "The AS/400 was introduced by IBM in June 1988 ... page 2", "page": 2, "screen": "LRN400"}`
- iPad: `{"content": "The AS/400 was introduced by IBM in June 1988 ... page 2", "page": 2, "screen": "LRN400"}`

#### CV-28 F5, F8 → 1 ページ目に戻る

- PUB400: `{"content": "Welcome to LEARN/400! This is page 1. Press F5 for next page, F8 for previous, F3 to exit.", "page": 1, "screen": "LRN400"}`
- Java Web: `{"content": "Welcome to LEARN/400! This is page 1. Press F5 for next page, F8 for previous, F3 to exit.", "page": 1, "screen": "LRN400"}`
- iPad: `{"content": "Welcome to LEARN/400! This is page 1. Press F5 for next page, F8 for previous, F3 to exit.", "page": 1, "screen": "LRN400"}`

#### CV-29 F5, F5 → 3 ページ目 (EXTRA='END') で終了 → VCFMAIN

- PUB400: `{"screen": "VCFMAIN"}`
- Java Web: `{"screen": "VCFMAIN"}`
- iPad: `{"screen": "VCFMAIN"}`

#### CV-30 F3 → 終了 → VCFMAIN

- PUB400: `{"screen": "VCFMAIN"}`
- Java Web: `{"screen": "VCFMAIN"}`
- iPad: `{"screen": "VCFMAIN"}`

#### CV-31 ASHIBATA (ELIGIBLE=1, ENLRN400=1) → 1,2,3,4

- PUB400: `{"options": ["1", "2", "3", "4"], "screen": "KIOSK"}`
- Java Web: `{"options": ["1", "2", "3", "4"], "screen": "KIOSK"}`
- iPad: `{"options": ["1", "2", "3", "4"], "screen": "KIOSK"}`

#### CV-32 NOVOTE (ELIGIBLE=0, ENLRN400=0) → 3,4 のみ

- PUB400: `{"options": ["3", "4"], "screen": "KIOSK"}`
- Java Web: `{"options": ["3", "4"], "screen": "KIOSK"}`
- iPad: `{"options": ["3", "4"], "screen": "KIOSK"}`

#### CV-33 DEMO400 (ELIGIBLE=1, ENLRN400=0) → 1,3,4

- PUB400: `{"options": ["1", "3", "4"], "screen": "KIOSK"}`
- Java Web: `{"options": ["1", "3", "4"], "screen": "KIOSK"}`
- iPad: `{"options": ["1", "3", "4"], "screen": "KIOSK"}`

#### CV-34 隠しオプション 7 + 誤パスワード → キオスクメニューに戻る

- PUB400: `{"options": ["1", "2", "3", "4"], "path": ["ADMPSWRD", "KIOSK"], "screen": "KIOSK"}`
- Java Web: `{"options": ["1", "2", "3", "4"], "path": ["ADMPSWRD", "KIOSK"], "screen": "KIOSK"}`
- iPad: `{"options": ["1", "2", "3", "4"], "path": ["ADMPSWRD", "KIOSK"], "screen": "KIOSK"}`

#### CV-35 隠しオプション 7 + 正パスワード (SETTINGS.ADMPSWRD) → キオスク終了 → VCFMAIN

- PUB400: `{"options": ["1", "2", "3", "4"], "path": ["ADMPSWRD", "VCFMAIN"], "screen": "VCFMAIN"}`
- Java Web: `{"options": ["1", "2", "3", "4"], "path": ["ADMPSWRD", "VCFMAIN"], "screen": "VCFMAIN"}`
- iPad: `{"options": ["1", "2", "3", "4"], "path": ["ADMPSWRD", "VCFMAIN"], "screen": "VCFMAIN"}`

#### CV-36 オプション 1 → NTRSTIT → VOTE1 (展示 ASHIBATA 固定) → F12 でキオスクへ戻る

- PUB400: `{"exhibit": "ASHIBATA", "options": ["1", "2", "3", "4"], "path": ["NTRSTIT", "VOTE1", "KIOSK"], "screen": "KIOSK"}`
- Java Web: `{"exhibit": "ASHIBATA", "options": ["1", "2", "3", "4"], "path": ["NTRSTIT", "VOTE1", "KIOSK"], "screen": "KIOSK"}`
- iPad: `{"exhibit": "ASHIBATA", "options": ["1", "2", "3", "4"], "path": ["NTRSTIT", "VOTE1", "KIOSK"], "screen": "KIOSK"}`

#### CV-37 資格外展示でオプション 1 を入力 → 無視 (キオスクメニューのまま)

- PUB400: `{"options": ["3", "4"], "path": ["KIOSK"], "screen": "KIOSK"}`
- Java Web: `{"options": ["3", "4"], "path": ["KIOSK"], "screen": "KIOSK"}`
- iPad: `{"options": ["3", "4"], "path": ["KIOSK"], "screen": "KIOSK"}`

## 4. 成果物

| 種別 | パス |
|---|---|
| テストケース定義 | `deliverables/tests/cases.json` |
| ランナー | `deliverables/tests/run_pub400.py`, `run_java.js`, `run_ipad.sh` + `ipad/VCF400UITests/CrossValidationUITests.swift`, `run_all.sh` (録画付き一括実行) |
| 比較 | `deliverables/tests/compare.py` → `deliverables/tests/results/compare.json` |
| 実測値 | `deliverables/tests/results/pub400.json`, `java.json`, `ipad.json` |
| 実行ログ | `deliverables/tests/results/run_all.log` (全体), `pub400_5250.log` (5250 全画面ダンプ), `java_run.log`, `ipad_xcuitest.log` |
| 録画 | `deliverables/video/VCF400_cross_validation.mp4` |

