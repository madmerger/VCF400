# フェーズ0: 準備 — 出力ディレクトリとソース棚卸し

更新日: **2026-09-07**

- リポジトリ: `madmerger/VCF400` (HEAD `83d566e Final release`)
- 出力ディレクトリ: `deliverables/` (`logs/` = PUB400 作業ログ・コンパイルリスト・5250 ランタイムログ, `spec/` = 仕様書, `test/` = テストレポート・録画), `java/`, `ipad/` = 移行実装

## ソース構成 (README 記載ライブラリ)

| ライブラリ | 種別 | 件数 | メンバー |
|---|---|---|---|
| QRPGLESRC | RPG IV (ILE) プログラム | 22 | ADDGBCMT, ADDVOTE, ADMADDSOFR, ADMCRTEXHB, ADMHIDECMT, ADMLRN400, ADMOFRLIST, ADMSETTING, ADMVOTERPT, BEEMOVIE, CREDITS, EXHBMENU, LRN400, LRN400AUT, NTRSTIT, OLDADDVOTE*, OLDVOTE*, PARAMETER, PRINTER, PRTLSTCMT, PRTLSTVOTE, READGBCMT |
| QDDSSRC | 物理ファイル DDS | 8 | AWARDDB, BMOVDB, EXHBDB, GUESTBKDB, LRN400STR, SECOFRS, SETTINGS, VOTINGDB |
| QSDASRC | 表示ファイル DDS | 13 | ADMSCR, ADMVOTERES, BMOVSCR, CREDITSSCR, EXHBMENUSC, GUESTBKSCR, INTERSCR, LRN400SCR, LRNAUTO, PASSSCR, SETUPSCR, TESTSUITE, VOTESCR |
| QMNUSRC | メニュー DDS / コマンド | 6 | ADMMAIN(+QQ), MAIN(+QQ), VCFMAIN(+QQ) |
| QCLSRC | CL プログラム (ランチャ) | 5 | ADDGBSTUB, LRN400STUB, READGBSTUB, VCFSTUB, VOTESTUB |
| QCMDSRC | コマンド定義 | 4 | STRCMTEDT, STREXHB, STREXHBEDT, STRVOTERPT |
| QRLUSRC | 印刷ファイル DDS | 4 | CMTPRTF, CMTTST, TESTPRT2, VOTEPRTF |
| savf | セーブファイル | 4 (+HISTORY.md) | VCFV1R0, VCFV1R1, VCFV1R2, **VCFV1R3** (最新 2024-04-05, SAVLIB VCF, V4R4M0 target) |

`*` OLDADDVOTE / OLDVOTE は旧版 (ビルド対象外)。

## 主要機能とオブジェクトの対応

| 機能 | RPG | 画面 DDS | 起動経路 |
|---|---|---|---|
| 投票 (Nominate Exhibit for Award) | ADDVOTE | VOTESCR | VCFMAIN 11 → VOTESTUB → NTRSTIT → ADDVOTE / EXHBMENU 1 |
| 展示キオスク | EXHBMENU | EXHBMENUSC | STREXHB (VCFSTUB) |
| ゲストブック記入 | ADDGBCMT | GUESTBKSCR (ADDCMT/ENDCMT) | VCFMAIN 12 → ADDGBSTUB / EXHBMENU 3 |
| ゲストブック閲覧 | READGBCMT | GUESTBKSCR (READCMT) | VCFMAIN 13 → READGBSTUB / EXHBMENU 4 |
| LEARN/400 | LRN400 (+LRN400AUT) | LRN400SCR | VCFMAIN 1 / EXHBMENU 2 |
| 管理 | ADM* | ADMSCR, ADMVOTERES, PASSSCR, SETUPSCR | VCFMAIN 90 → ADMMAIN |

## 前提確認結果

| 前提 | 結果 |
|---|---|
| PUB400 接続 | pub400.com: 5250 (23/992), SSH **2222** (22 は拒否), FTP 21。ユーザー ASHIBATA、専用ライブラリ ASHIBATA1 (ソース/SAVF/リスト) と ASHIBATA2 (オブジェクト/データ) |
| 5250 エミュレータ | tn5250 **0.19.0** (`~/.local/bin/tn5250`) + pexpect ドライバ `deliverables/logs/tn5250_driver.py` |
| Apple 環境 | Xcode **26.6** + iPad シミュレータ。実機・署名情報なし → シミュレータのみ |
| Java | OpenJDK **17.0.20.1** |
| Playwright | **1.63**, Chromium **1243** |
| 録画ツール | ffmpeg **9.0.1** / `xcrun simctl io recordVideo` (フェーズ5 のみ使用) |
| XcodeGen | **2.46.0** |
