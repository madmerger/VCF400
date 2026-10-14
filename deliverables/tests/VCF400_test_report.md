---
title: "VCF/400 クロス検証テスト結果レポート"
subtitle: "PUB400 (オリジナル RPG) / Java Web / iPad の 3 環境同一入力比較"
date: "2026-09-07"
---

# VCF/400 クロス検証テスト結果レポート

| 項目 | 内容 |
|---|---|
| 対象 | オリジナル RPG (PUB400 `ASHIBATA2`) / Java Web 版 (`java/`) / iPad 版 (`ipad/`) |
| テストケース | `deliverables/tests/cases.json` (37 ケース) |
| 実行方式 | PUB400: tn5250 (`run_pub400.py`) / Java: Playwright headed Chromium (`run_java.js`) / iPad: XCUITest (`record_ipad.sh`) |
| 判定 | `expect` の全キーが 3 環境の `observed` と一致 = PASS |

## 1. サマリ

| 環境 | PASS | FAIL / 未実行 |
|---|---|---|
| PUB400 | 37 / 37 | 0 |
| Java Web | 37 / 37 | 0 |
| iPad | 37 / 37 | 0 |
| **3 環境一致** | **37 / 37** | 0 |

### 1.1 分類別

| 分類 | 内容 | ケース数 | 3 環境一致 |
|---|---|---|---|
| ADDVOTE | 投票 | 13 | 13 |
| ADDGBCMT | ゲストブック記入 | 6 | 6 |
| READGBCMT | ゲストブック閲覧 | 7 | 7 |
| LRN400 | LEARN/400 | 4 | 4 |
| EXHBMENU | 展示キオスク | 7 | 7 |

## 2. 不一致と対処

本 run は、Java を PR #7 の localized 実装 (`devin/1788796612-java-web-ja`, HEAD `1dabea7`)、iPad を PR #8 の localized 実装 (`devin/1788796762-ipad-ja`, HEAD `06d639a`) として実行した。`compare.py` の最終結果は PUB400 37/37、Java 37/37、iPad 37/37、3 環境一致 37/37 で、不一致は 0 件である。

実行上の workaround は、fresh H2 データベースの起動時に `SPRING_SQL_INIT_MODE=always` と `VCF_DB_API=true` を指定したことだけである。これは実行環境の初期化であり、アプリケーションおよび runner の変更はない。

前セッションの履歴 `run1` では CV-23 の 5250 入力処理が原因で 36/37 となった。これは `INCMTID` の桁数一杯入力時に余分な Field Exit を送っていた runner の問題であり、修正済みの現行 runner では再発しなかった。古い `results/run1/` 証跡は stale artifact として削除した。

最終実行における不一致は 0 件。

### UI 日本語化とクロス検証契約

比較で参照する `#errline` / `errline` 識別子は英語原文 M-xx を返し、画面上の日本語は `.errline-ja` / `errline_ja` に表示する。画面検出のアンカーとして、台帳で保持対象とした legacy title の英語文字列は残す。


## 3. ケース一覧

入力、期待値、3 環境の実測値、および合否を以下に示す。

| ID | 名称 | 入力 | 期待値 | PUB400 実測 | Java 実測 | iPad 実測 | 合否 |
|---|---|---|---|---|---|---|---|
| CV-01 | 全項目未入力 (展示 ID は保護・事前入力) | {"badge":"","award":""} | {"screen":"VOTE1","errline":"Must enter Award ID","vote":null} | {"screen":"VOTE1","errline":"Must enter Award ID","vote":null} | {"screen":"VOTE1","errline":"Must enter Award ID","vote":null} | {"errline":"Must enter Award ID","screen":"VOTE1","vote":null} | PASS |
| CV-02 | アワード未入力 | {"badge":"7001","award":""} | {"screen":"VOTE1","errline":"Must enter Award ID","vote":null} | {"screen":"VOTE1","errline":"Must enter Award ID","vote":null} | {"screen":"VOTE1","errline":"Must enter Award ID","vote":null} | {"errline":"Must enter Award ID","screen":"VOTE1","vote":null} | PASS |
| CV-03 | バッジ未入力 | {"badge":"","award":"1"} | {"screen":"VOTE1","errline":"Must enter badge number","vote":null} | {"screen":"VOTE1","errline":"Must enter badge number","vote":null} | {"screen":"VOTE1","errline":"Must enter badge number","vote":null} | {"errline":"Must enter badge number","screen":"VOTE1","vote":null} | PASS |
| CV-04 | 正常投票 (新規バッジ 7001, アワード 1) | {"badge":"7001","award":"1"} | {"screen":"VOTEEND","vote":{"badge":7001,"award":1,"exhibit":"ASHIBATA"}} | {"screen":"VOTEEND","vote":{"badge":7001,"award":1,"exhibit":"ASHIBATA"}} | {"screen":"VOTEEND","vote":{"badge":7001,"award":1,"exhibit":"ASHIBATA"}} | {"screen":"VOTEEND","vote":{"award":1,"badge":7001,"exhibit":"ASHIBATA"}} | PASS |
| CV-05 | 重複バッジ (7001 で再投票) | {"badge":"7001","award":"2"} | {"screen":"VOTE1","errline":"You have already voted.","vote":{"badge":7001,"award":1,"exhibit":"ASHIBATA"}} | {"screen":"VOTE1","errline":"You have already voted.","vote":{"badge":7001,"award":1,"exhibit":"ASHIBATA"}} | {"screen":"VOTE1","errline":"You have already voted.","vote":{"badge":7001,"award":1,"exhibit":"ASHIBATA"}} | {"errline":"You have already voted.","screen":"VOTE1","vote":{"award":1,"badge":7001,"exhibit":"ASHIBATA"}} | PASS |
| CV-06 | 存在しないアワード (9) | {"badge":"7002","award":"9"} | {"screen":"VOTE1","errline":"Award does not exist.","vote":null} | {"screen":"VOTE1","errline":"Award does not exist.","vote":null} | {"screen":"VOTE1","errline":"Award does not exist.","vote":null} | {"errline":"Award does not exist.","screen":"VOTE1","vote":null} | PASS |
| CV-07 | 境界値: アワード 0 (数値 0 = 未入力扱い) | {"badge":"7002","award":"0"} | {"screen":"VOTE1","errline":"Must enter Award ID","vote":null} | {"screen":"VOTE1","errline":"Must enter Award ID","vote":null} | {"screen":"VOTE1","errline":"Must enter Award ID","vote":null} | {"errline":"Must enter Award ID","screen":"VOTE1","vote":null} | PASS |
| CV-08 | 共用端末: 展示 ID 未入力 | {"badge":"7002","exhibit":"","award":"1"} | {"screen":"VOTE1","errline":"Must enter Exhibit ID","vote":null} | {"screen":"VOTE1","errline":"Must enter Exhibit ID","vote":null} | {"screen":"VOTE1","errline":"Must enter Exhibit ID","vote":null} | {"errline":"Must enter Exhibit ID","screen":"VOTE1","vote":null} | PASS |
| CV-09 | 共用端末: 存在しない展示 NOSUCH | {"badge":"7002","exhibit":"NOSUCH","award":"1"} | {"screen":"VOTE1","errline":"Exhibit does not exist","vote":null} | {"screen":"VOTE1","errline":"Exhibit does not exist","vote":null} | {"screen":"VOTE1","errline":"Exhibit does not exist","vote":null} | {"errline":"Exhibit does not exist","screen":"VOTE1","vote":null} | PASS |
| CV-10 | 共用端末: 資格外展示 NOVOTE (ELIGIBLE=0) | {"badge":"7002","exhibit":"NOVOTE","award":"1"} | {"screen":"VOTE1","errline":"Exhibit ineligible for award","vote":null} | {"screen":"VOTE1","errline":"Exhibit ineligible for award","vote":null} | {"screen":"VOTE1","errline":"Exhibit ineligible for award","vote":null} | {"errline":"Exhibit ineligible for award","screen":"VOTE1","vote":null} | PASS |
| CV-11 | 共用端末: 正常投票 (7002, DEMO400, アワード 2) | {"badge":"7002","exhibit":"DEMO400","award":"2"} | {"screen":"VOTEEND","vote":{"badge":7002,"award":2,"exhibit":"DEMO400"}} | {"screen":"VOTEEND","vote":{"badge":7002,"award":2,"exhibit":"DEMO400"}} | {"screen":"VOTEEND","vote":{"badge":7002,"award":2,"exhibit":"DEMO400"}} | {"screen":"VOTEEND","vote":{"award":2,"badge":7002,"exhibit":"DEMO400"}} | PASS |
| CV-12 | 複合エラー (重複バッジ+存在しない展示+存在しないアワード) → 最後の検査が勝つ | {"badge":"7001","exhibit":"NOSUCH","award":"9"} | {"screen":"VOTE1","errline":"Award does not exist.","vote":{"badge":7001,"award":1,"exhibit":"ASHIBATA"}} | {"screen":"VOTE1","errline":"Award does not exist.","vote":{"badge":7001,"award":1,"exhibit":"ASHIBATA"}} | {"screen":"VOTE1","errline":"Award does not exist.","vote":{"badge":7001,"award":1,"exhibit":"ASHIBATA"}} | {"errline":"Award does not exist.","screen":"VOTE1","vote":{"award":1,"badge":7001,"exhibit":"ASHIBATA"}} | PASS |
| CV-13 | 投票停止 ALWVOTE='N' → ENDOFCON | {"badge":"7003","award":"1"} | {"screen":"ENDOFCON","vote":null} | {"screen":"ENDOFCON","vote":null} | {"screen":"ENDOFCON","vote":null} | {"screen":"ENDOFCON","vote":null} | PASS |
| CV-14 | 全項目未入力 (展示 ID は保護・事前入力) | {"name":"","comment":""} | {"screen":"ADDCMT","errline":"Must enter a comment","comment":{"id":3,"row":null}} | {"screen":"ADDCMT","errline":"Must enter a comment","comment":{"id":3,"row":null}} | {"screen":"ADDCMT","errline":"Must enter a comment","comment":{"id":3,"row":null}} | {"comment":{"id":3,"row":null},"errline":"Must enter a comment","screen":"ADDCMT"} | PASS |
| CV-15 | コメント未入力 | {"name":"Devin Tester","comment":""} | {"screen":"ADDCMT","errline":"Must enter a comment","comment":{"id":3,"row":null}} | {"screen":"ADDCMT","errline":"Must enter a comment","comment":{"id":3,"row":null}} | {"screen":"ADDCMT","errline":"Must enter a comment","comment":{"id":3,"row":null}} | {"comment":{"id":3,"row":null},"errline":"Must enter a comment","screen":"ADDCMT"} | PASS |
| CV-16 | 名前未入力 | {"name":"","comment":"Hello"} | {"screen":"ADDCMT","errline":"Must enter your name","comment":{"id":3,"row":null}} | {"screen":"ADDCMT","errline":"Must enter your name","comment":{"id":3,"row":null}} | {"screen":"ADDCMT","errline":"Must enter your name","comment":{"id":3,"row":null}} | {"comment":{"id":3,"row":null},"errline":"Must enter your name","screen":"ADDCMT"} | PASS |
| CV-17 | 正常記入 → ENDCMT, CMTID=3 (最終+1), VISIBLE=Y | {"name":"Devin Tester","comment":"Cross validation run"} | {"screen":"ENDCMT","comment":{"id":3,"row":{"id":3,"visible":"Y","exhibit":"ASHIBATA","name":"Devin Tester","comment":"Cross validation run"}}} | {"screen":"ENDCMT","comment":{"id":3,"row":{"id":3,"visible":"Y","exhibit":"ASHIBATA","name":"Devin Tester","comment":"Cross validation run"}}} | {"screen":"ENDCMT","shownId":3,"comment":{"id":3,"row":{"id":3,"visible":"Y","exhibit":"ASHIBATA","name":"Devin Tester","comment":"Cross validation run"}}} | {"comment":{"id":3,"row":{"comment":"Cross validation run","exhibit":"ASHIBATA","id":3,"name":"Devin Tester","visible":"Y"}},"screen":"ENDCMT","shownId":3} | PASS |
| CV-18 | 共用端末: 展示 ID 未入力 | {"name":"Guest","exhibit":"","comment":"x"} | {"screen":"ADDCMT","errline":"Must enter Exhibit ID","comment":{"id":4,"row":null}} | {"screen":"ADDCMT","errline":"Must enter Exhibit ID","comment":{"id":4,"row":null}} | {"screen":"ADDCMT","errline":"Must enter Exhibit ID","comment":{"id":4,"row":null}} | {"comment":{"id":4,"row":null},"errline":"Must enter Exhibit ID","screen":"ADDCMT"} | PASS |
| CV-19 | 共用端末: 存在しない展示 NOSUCH でも記入できる (存在チェックなし) | {"name":"Guest","exhibit":"NOSUCH","comment":"Unchecked exhibit"} | {"screen":"ENDCMT","comment":{"id":4,"row":{"id":4,"visible":"Y","exhibit":"NOSUCH","name":"Guest","comment":"Unchecked exhibit"}}} | {"screen":"ENDCMT","comment":{"id":4,"row":{"id":4,"visible":"Y","exhibit":"NOSUCH","name":"Guest","comment":"Unchecked exhibit"}}} | {"screen":"ENDCMT","shownId":4,"comment":{"id":4,"row":{"id":4,"visible":"Y","exhibit":"NOSUCH","name":"Guest","comment":"Unchecked exhibit"}}} | {"comment":{"id":4,"row":{"comment":"Unchecked exhibit","exhibit":"NOSUCH","id":4,"name":"Guest","visible":"Y"}},"screen":"ENDCMT","shownId":4} | PASS |
| CV-20 | コメント ID 未入力 | {"cmtid":""} | {"screen":"READCMT","errline":"Must enter CommentID"} | {"screen":"READCMT","total":4,"errline":"Must enter CommentID","out":{"name":"","title":"","cmt":""}} | {"screen":"READCMT","total":4,"errline":"Must enter CommentID"} | {"errline":"Must enter CommentID","screen":"READCMT","total":4} | PASS |
| CV-21 | ID 1 → 名前 11 桁 / 展示タイトル / コメント | {"cmtid":"1"} | {"screen":"READCMT","total":4,"out":{"name":"Great exhib","title":"IBM i on PUB400 Demo","cmt":"VCF/400 running on PUB400."}} | {"screen":"READCMT","total":4,"errline":"ASHIBATA","out":{"name":"Great exhib","title":"IBM i on PUB400 Demo","cmt":"VCF/400 running on PUB400."}} | {"screen":"READCMT","total":4,"errline":"","out":{"name":"Great exhib","title":"IBM i on PUB400 Demo","cmt":"VCF/400 running on PUB400."}} | {"errline":"","out":{"cmt":"VCF/400 running on PUB400.","name":"Great exhib","title":"IBM i on PUB400 Demo"},"screen":"READCMT","total":4} | PASS |
| CV-22 | ID 3 (この実行で追加したコメント) | {"cmtid":"3"} | {"screen":"READCMT","total":4,"out":{"name":"Devin Teste","title":"IBM i on PUB400 Demo","cmt":"Cross validation run"}} | {"screen":"READCMT","total":4,"errline":"ASHIBATA","out":{"name":"Devin Teste","title":"IBM i on PUB400 Demo","cmt":"Cross validation run"}} | {"screen":"READCMT","total":4,"errline":"","out":{"name":"Devin Teste","title":"IBM i on PUB400 Demo","cmt":"Cross validation run"}} | {"errline":"","out":{"cmt":"Cross validation run","name":"Devin Teste","title":"IBM i on PUB400 Demo"},"screen":"READCMT","total":4} | PASS |
| CV-23 | 存在しない ID 9999 → 最終レコード (4, 他展示) → 帰属外メッセージ | {"cmtid":"9999"} | {"screen":"READCMT","total":4,"out":{"name":"","title":"","cmt":"This comment is not part of this guestbook."}} | {"screen":"READCMT","total":4,"errline":"ASHIBATA","out":{"name":"","title":"","cmt":"This comment is not part of this guestbook."}} | {"screen":"READCMT","total":4,"errline":"","out":{"name":"","title":"","cmt":"This comment is not part of this guestbook."}} | {"errline":"","out":{"cmt":"This comment is not part of this guestbook.","name":"","title":""},"screen":"READCMT","total":4} | PASS |
| CV-24 | 非表示コメント (ID 2 を VISIBLE=N) | {"cmtid":"2"} | {"screen":"READCMT","total":4,"out":{"name":"Name Hidden","title":"","cmt":"This comment hidden by an admin - offensive content."}} | {"screen":"READCMT","total":4,"errline":"ASHIBATA","out":{"name":"Name Hidden","title":"","cmt":"This comment hidden by an admin - offensive content."}} | {"screen":"READCMT","total":4,"errline":"","out":{"name":"Name Hidden","title":"","cmt":"This comment hidden by an admin - offensive content."}} | {"errline":"","out":{"cmt":"This comment hidden by an admin - offensive content.","name":"Name Hidden","title":""},"screen":"READCMT","total":4} | PASS |
| CV-25 | 他展示 (DEMO400) から ASHIBATA のコメント 1 を読む → 帰属外 | {"cmtid":"1"} | {"screen":"READCMT","total":4,"out":{"name":"","title":"","cmt":"This comment is not part of this guestbook."}} | {"screen":"READCMT","total":4,"errline":"DEMO400","out":{"name":"","title":"","cmt":"This comment is not part of this guestbook."}} | {"screen":"READCMT","total":4,"errline":"","out":{"name":"","title":"","cmt":"This comment is not part of this guestbook."}} | {"errline":"","out":{"cmt":"This comment is not part of this guestbook.","name":"","title":""},"screen":"READCMT","total":4} | PASS |
| CV-26 | 共用端末は全展示を閲覧可 / 展示タイトルは SETLL+READ (NOSUCH → NOVOTE) | {"cmtid":"4"} | {"screen":"READCMT","total":4,"out":{"name":"Guest","title":"Ineligible test exhibit (ELIGIBLE=0)","cmt":"Unchecked exhibit"}} | {"screen":"READCMT","total":4,"errline":"MM2024","out":{"name":"Guest","title":"Ineligible test exhibit (ELIGIBLE=0)","cmt":"Unchecked exhibit"}} | {"screen":"READCMT","total":4,"errline":"","out":{"name":"Guest","title":"Ineligible test exhibit (ELIGIBLE=0)","cmt":"Unchecked exhibit"}} | {"errline":"","out":{"cmt":"Unchecked exhibit","name":"Guest","title":"Ineligible test exhibit (ELIGIBLE=0)"},"screen":"READCMT","total":4} | PASS |
| CV-27 | F5 → 2 ページ目 | {"keys":["F5"]} | {"screen":"LRN400","page":2,"content":"The AS/400 was introduced by IBM in June 1988 ... page 2"} | {"screen":"LRN400","page":2,"content":"The AS/400 was introduced by IBM in June 1988 ... page 2"} | {"screen":"LRN400","page":2,"content":"The AS/400 was introduced by IBM in June 1988 ... page 2"} | {"content":"The AS/400 was introduced by IBM in June 1988 ... page 2","page":2,"screen":"LRN400"} | PASS |
| CV-28 | F5, F8 → 1 ページ目に戻る | {"keys":["F5","F8"]} | {"screen":"LRN400","page":1,"content":"Welcome to LEARN/400! This is page 1. Press F5 for next page, F8 for previous, F3 to exit."} | {"screen":"LRN400","page":1,"content":"Welcome to LEARN/400! This is page 1. Press F5 for next page, F8 for previous, F3 to exit."} | {"screen":"LRN400","page":1,"content":"Welcome to LEARN/400! This is page 1. Press F5 for next page, F8 for previous, F3 to exit."} | {"content":"Welcome to LEARN/400! This is page 1. Press F5 for next page, F8 for previous, F3 to exit.","page":1,"screen":"LRN400"} | PASS |
| CV-29 | F5, F5 → 3 ページ目 (EXTRA='END') で終了 → VCFMAIN | {"keys":["F5","F5"]} | {"screen":"VCFMAIN"} | {"screen":"VCFMAIN"} | {"screen":"VCFMAIN"} | {"screen":"VCFMAIN"} | PASS |
| CV-30 | F3 → 終了 → VCFMAIN | {"keys":["F3"]} | {"screen":"VCFMAIN"} | {"screen":"VCFMAIN"} | {"screen":"VCFMAIN"} | {"screen":"VCFMAIN"} | PASS |
| CV-31 | ASHIBATA (ELIGIBLE=1, ENLRN400=1) → 1,2,3,4 | {} | {"screen":"KIOSK","options":["1","2","3","4"]} | {"screen":"KIOSK","options":["1","2","3","4"]} | {"screen":"KIOSK","options":["1","2","3","4"]} | {"options":["1","2","3","4"],"screen":"KIOSK"} | PASS |
| CV-32 | NOVOTE (ELIGIBLE=0, ENLRN400=0) → 3,4 のみ | {} | {"screen":"KIOSK","options":["3","4"]} | {"screen":"KIOSK","options":["3","4"]} | {"screen":"KIOSK","options":["3","4"]} | {"options":["3","4"],"screen":"KIOSK"} | PASS |
| CV-33 | DEMO400 (ELIGIBLE=1, ENLRN400=0) → 1,3,4 | {} | {"screen":"KIOSK","options":["1","3","4"]} | {"screen":"KIOSK","options":["1","3","4"]} | {"screen":"KIOSK","options":["1","3","4"]} | {"options":["1","3","4"],"screen":"KIOSK"} | PASS |
| CV-34 | 隠しオプション 7 + 誤パスワード → キオスクメニューに戻る | {"option":"7","password":"WRONGPW"} | {"screen":"KIOSK","options":["1","2","3","4"],"path":["ADMPSWRD","KIOSK"]} | {"screen":"KIOSK","options":["1","2","3","4"],"path":["ADMPSWRD","KIOSK"]} | {"screen":"KIOSK","options":["1","2","3","4"],"path":["ADMPSWRD","KIOSK"]} | {"options":["1","2","3","4"],"path":["ADMPSWRD","KIOSK"],"screen":"KIOSK"} | PASS |
| CV-35 | 隠しオプション 7 + 正パスワード (SETTINGS.ADMPSWRD) → キオスク終了 → VCFMAIN | {"option":"7","password":"VCF2024"} | {"screen":"VCFMAIN","options":["1","2","3","4"],"path":["ADMPSWRD","VCFMAIN"]} | {"screen":"VCFMAIN","options":["1","2","3","4"],"path":["ADMPSWRD","VCFMAIN"]} | {"screen":"VCFMAIN","options":["1","2","3","4"],"path":["ADMPSWRD","VCFMAIN"]} | {"options":["1","2","3","4"],"path":["ADMPSWRD","VCFMAIN"],"screen":"VCFMAIN"} | PASS |
| CV-36 | オプション 1 → NTRSTIT → VOTE1 (展示 ASHIBATA 固定) → F12 でキオスクへ戻る | {"option":"1"} | {"screen":"KIOSK","options":["1","2","3","4"],"path":["NTRSTIT","VOTE1","KIOSK"],"exhibit":"ASHIBATA"} | {"screen":"KIOSK","options":["1","2","3","4"],"exhibit":"ASHIBATA","path":["NTRSTIT","VOTE1","KIOSK"]} | {"screen":"KIOSK","options":["1","2","3","4"],"exhibit":"ASHIBATA","path":["NTRSTIT","VOTE1","KIOSK"]} | {"exhibit":"ASHIBATA","options":["1","2","3","4"],"path":["NTRSTIT","VOTE1","KIOSK"],"screen":"KIOSK"} | PASS |
| CV-37 | 資格外展示でオプション 1 を入力 → 無視 (キオスクメニューのまま) | {"option":"1"} | {"screen":"KIOSK","options":["3","4"],"path":["KIOSK"]} | {"screen":"KIOSK","options":["3","4"],"path":["KIOSK"]} | {"screen":"KIOSK","options":["3","4"],"path":["KIOSK"]} | {"options":["3","4"],"path":["KIOSK"],"screen":"KIOSK"} | PASS |

## 4. 実行環境

| 項目 | 値 |
|---|---|
| OS | macOS |
| Xcode | 26.6 |
| Java | 17.0.20.1 |
| tn5250 | 0.19.0 |
| Playwright / Chromium | 1.63 / 1243 |
| ffmpeg | 9.0.1 |
| XcodeGen | 2.46.0 |
| PUB400 | `ASHIBATA1` / `ASHIBATA2` |
| 実行日 | 2026-09-07 |

## 5. 動画

| 環境 | 動画パス | 長さ | 解像度 | サイズ | 収録内容 | 速度 |
|---|---|---|---|---|---|---|
| PUB400 | `deliverables/video/1_PUB400_RPG.mp4` | 492.50 秒 | 1280x720 (h264) | 8.65 MB | 実 5250 画面フレーム連続表示 | 等速 |
| Java Web | `deliverables/video/2_Java_Web.mp4` | 153.47 秒 | 1280x720 (h264) | 5.81 MB | Chromium 実画面 | 等速 |
| iPad | `deliverables/video/3_iPad.mp4` | 427.70 秒 | 1280x720 (h264) | 7.14 MB | シミュレータ実画面 | 等速 |

3 本とも実時間の等速録画であり、追加の速度変更は行っていない。PUB400 は `self.s.screen.display` から取得した 5250 画面フレームを連続表示し、Java は Chromium の実画面、iPad はシミュレータの実画面を収録した。iPad のケース帯 オーバーレイは ffmpeg に `drawtext` がないため Playwright で PNG を生成し、`overlay` / `scale` / `pad` で合成した。

## 6. 成果物と再現性

| 種別 | パス |
|---|---|
| テストケース定義 | `deliverables/tests/cases.json` |
| 実測値 | `deliverables/tests/results/pub400.json`, `java.json`, `ipad.json` |
| 比較結果 | `deliverables/tests/results/compare.json` |
| フレーム | `deliverables/tests/results/pub400_frames.json` |
| ランナー | `run_pub400.py`, `run_java.js`, `run_ipad.sh`, `record_ipad.sh` |
| 動画変換 | `frames_to_video.js`, `overlay_ipad.js` |
