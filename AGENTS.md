# AGENTS.md — VCF400 再ビルド・移行の技術前提

このリポジトリ (IBM i / RPG IV・CL・DDS のキオスクアプリ) を PUB400 で再ビルド・検証し、他プラットフォームへ移行・クロス検証する際の確定済み前提。AI エージェント・Ask Devin はこの内容を再調査せずそのまま前提として使うこと。


## PUB400 接続とライブラリ
- host pub400.com、SSH 2222、FTP 21、5250 telnet 23 / TLS 992。認証はシークレット PUB400_LOGIN / PUB400_PW (値を出力・コミットしない)。
- ソースライブラリ ASHIBATA1、オブジェクト/データライブラリ ASHIBATA2 (ライブラリ名であり秘密情報ではない)。共有環境のため他ライブラリは触らない。ASHIBATA2 内の VCF400 と無関係な既存オブジェクト (PF `ASHIBATA` 等) は削除しない。
- RSTLIB / RSTOBJ は一般ユーザー権限がなく CPD0032 で失敗する。`savf/VCFV1R3.SAVF` (SAVLIB VCF, V4R4M0) は FTP 転送 + DSPSAVF を記録として 1 回だけ行い、復元は試みずソースビルドを正規手順とする。
- PASE の `system` はジョブ間でライブラリリストを保持しない。ビルドは 1 本の CL にまとめて 1 ジョブで実行する。CL スタブは `VCF/xxxx` 修飾で CALL しているため、ビルド先ライブラリに合わせて修正する。

## ビルドの既知事項
- ビルド順: PF → LF → DSPF / PRTF → MSGF / MNU → RPG (CRTBNDRPG) → CL (CRTBNDCL) → CMD。
- `QRPGLESRC/PRTLSTVOTE.rpgle` の F 仕様は `K DISK` が必要 (現状 `IF E DISK` のまま)。
- OLDVOTE / OLDADDVOTE / PRINTER / BEEMOVIE はコンパイル不可でスコープ外。それ以外の RPG 18 本は severity 00 でコンパイルできる。
- ADMVOTERPT.rpgle のコメント (001=Ed Fair, 002=Best in Show) は AWARDDB の実データ (001=Best in Show, 002=Ed Fair) と逆。アワード対応は実データを正とする。

## 移行スコープ
- 対象: VCFMAIN 配下の利用者機能 — 投票 (ADDVOTE)、ゲストブック (ADDGBCMT / READGBCMT)、LEARN/400 (LRN400)、EXHBMENU キオスク (隠しオプション 7 → 管理者パスワード SETTINGS.ADMPSWRD)。
- 対象外: 管理機能 (ADMMAIN 系、VCFMAIN 90)、印刷スプール (PRTLSTVOTE / PRTLSTCMT)。仕様書では言及のみ。
- DB: EXHBDB / VOTINGDB / GUESTBKDB / AWARDDB / SETTINGS / SECOFRS / LRN400STR (BMOVDB は対象外)。

## 移行時に台帳どおり保持する要素 (UI はモダンで良く、グリーンスクリーン模写は不可)
- メニュー階層 VCFMAIN → 各機能とメニュー番号併記、入力順 (投票: バッジ → 展示 ID → アワード ID)、F キー番号と役割 (「送信 (F5)」等)、画面タイトル文言 ("NOMINATE EXHIBIT FOR AWARD", "GUESTBOOK/400", "LEARN/400", "AS/400 DEMO MENU", "VCF/400 - How to Navigate", "WELCOME TO...")、エラー行の位置と文言。
- 業務ルール: ADDVOTE は 4 チェック (重複バッジ / ELIGIBLE / 展示存在 / アワード存在) 全通過 (CHECKOK=4) 時のみ書込、エラー行は最後に失敗したチェック。SETTINGS.ALWVOTE='N' で投票画面をスキップし ENDOFCON。GUESTBKDB.CMTID は max+1、VISIBLE='N' は "Name Hidden"。READGBCMT で末尾を超える ID は最終レコードにフォールバック。LRN400 は EXTRA='END' で終了、CONTENT='CALL' / 'JUMP' (対象プログラム / ページ番号は EXTRA) で分岐。

## 5250 自動操作
- tn5250 ($HOME/.local/bin/tn5250) + pexpect。READGBCMT の INCMTID (4Y) は 4 桁入力後に Field Exit を送らない。
- ベースラインデータは投入スクリプト (SQL) で管理し、テスト前後で必ず再シードする。

## 工程の順序
- 画面台帳と仕様書を先に作成して凍結し、`deliverables/tests/cases.json` の期待値を PUB400 実測で確定してから、Java / iPad の実装 (別々の子セッションで並行) を開始する。期待値を仕様の推測で埋めない。
- 自己判断した事項とその理由は `deliverables/README.md` に記録する。

## ローカルツール
- Chrome 未導入。Playwright Chromium ($HOME/pwtools)。ffmpeg に drawtext がないためオーバーレイは Playwright / PNG で描く。
- Xcode + iPad Pro 13-inch シミュレータ (実機・署名なし)、XcodeGen。Java 17 / Maven。

## 成果物の固定名
- 実装はリポジトリ直下の `java/` (Spring Boot / Java 17 / H2 DB2 互換、画面あり) と `ipad/` (SwiftUI + SQLite、XcodeGen project.yml、単体アプリ)。`deliverables/` 配下には置かない。
- 以下は `deliverables/` 配下:
- 文書 (各 Markdown と同名 PDF の 2 ファイル): `spec/VCF400_spec.md` + `spec/VCF400_spec.pdf`、`spec/VCF400_screen_ledger.md` + `spec/VCF400_screen_ledger.pdf`、`spec/VCF400_traceability.md` + `spec/VCF400_traceability.pdf`、`tests/VCF400_test_report.md` + `tests/VCF400_test_report.pdf`
- `tests/cases.json`、`video/1_PUB400_RPG.mp4`・`video/2_Java_Web.mp4`・`video/3_iPad.mp4`、`README.md`。
- 動画: 環境ごとに 1 本、テストケース操作のみ、ケース番号と名称を画面内表示、1280x720 以上 H.264、10 分以内目標、ビルド映像なし、iPad は build-for-testing 事前実行 → 録画中は test-without-building、100 MB 超は Git LFS。
