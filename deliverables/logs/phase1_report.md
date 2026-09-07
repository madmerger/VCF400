# フェーズ1: PUB400 上での復元と動作確認 — 結果報告

実行日: 2026-09-07 (UTC) / ホスト: pub400.com / ユーザー: ASHIBATA
専用ライブラリ: **ASHIBATA1** (ソース・SAVF・コンパイルリスト), **ASHIBATA2** (オブジェクト・データ)
共有ライブラリへの書込・上書きは行っていない。最初に ASHIBATA1/ASHIBATA2 の状態を記録し、ASHIBATA2 の VCF 所有オブジェクトを削除、ASHIBATA1 のソースメンバーを全削除してから再ビルドした。ASHIBATA2 に残したのは既存の PF `ASHIBATA` のみである。

## 1. セーブファイル復元 → 権限不足のためソースビルドへフォールバック

### 1.1 クリーン再実行手順

- `phase1_01_prepare.log` に `DSPLIB ASHIBATA1/ASHIBATA2` の前後状態を記録。
- ASHIBATA2 の VCF 所有 PF 8 件、DSPF、PRTF、メニュー、MSGF、`*PGM`、`*CMD` を削除。`ASHIBATA` PF は削除していない。
- ASHIBATA1 の QRPGLESRC/QCLSRC/QDDSSRC/QSDASRC/QMNUSRC/QCMDSRC/QRLUSRC のメンバーを `RMVM ... MBR(*ALL)` で削除し、`BUILDVCF` と旧 `VCFV1R3` を削除。
- `QRPGLESRC/PRTLSTVOTE.rpgle` の F 仕様補正 (`FVOTINGDB IF E DISK` → `IF E K DISK`、`*LOVAL SETLL` に必要) は本ブランチのソースに含まれている (master との差分 1 行) ことを確認し、そのソースをビルドした。

| 手順 | 結果 | ログ |
|---|---|---|
| `savf/VCFV1R3.SAVF` を FTP (binary) で `/QSYS.LIB/ASHIBATA1.LIB/VCFV1R3.FILE` へ転送 | 成功 (5,892,480 bytes, `226 File transfer completed`) | `phase1_02_ftp_savf.log` |
| `DSPSAVF` で内容確認 | SAVLIB VCF, 2024-04-04 20:40:52, TGTRLS V4R4M0, 11,160 レコード | `phase1_03_dspsavf.log` |
| `RSTLIB SAVLIB(VCF) RSTLIB(ASHIBATA1) ...` | **失敗 CPD0032: Not authorized to command RSTLIB** | `phase1_04_rstlib.log` |
| `RSTOBJ ... RSTLIB(ASHIBATA1)` | **失敗 CPD0032: Not authorized to command RSTOBJ** | `phase1_04_rstlib.log` |
| フォールバック: ソースから CRTPF / CRTDSPF / CRTPRTF / CRTMSGF / CRTMNU / CRTBNDRPG / CRTBNDCL / CRTCMD | 成功 (下記) | `phase1_05_build.log`, `pub400_listings/*.txt` |

PUB400 の一般ユーザーには RSTLIB/RSTOBJ 権限が付与されていない (不足権限として明示報告)。復元は不可のため、ユーザー指示どおりソースビルドで代替した。

### ビルド方式 (`pub400_build.sh` → `pub400_BUILDVCF.clle`)

- ソースを FTP でソース PF (ASHIBATA1/QRPGLESRC 等) へ転送。NUL パディング除去・行番号プレフィクス除去・UTF-8→ISO-8859-1 変換・`VCF/`→`ASHIBATA2/` 置換 (VCF ライブラリは復元不可のため)。
- PASE `system` コマンドはコマンドごとに別ジョブとなりライブラリリストが引き継がれないため、CL プログラム BUILDVCF を生成し 1 ジョブ内で CHGLIBL + 全コンパイルを実行。
- コンパイルリストは QSYSPRT を捕捉し `pub400_listings/<OBJ>.txt` に分割保存 (46 件)。CRLF の listing を分割できるよう awk に CR 除去を追加した。

### ビルド結果

| 種別 | 結果 |
|---|---|
| 物理ファイル (8) | 全件作成 (AWARDDB, BMOVDB, EXHBDB, GUESTBKDB, LRN400STR, SECOFRS, SETTINGS, VOTINGDB) |
| 表示ファイル (13) | 全件作成 |
| 印刷ファイル (4) | 全件作成 |
| メニュー (3) + MSGF (3) | 全件作成 (VCFMAIN, MAIN, ADMMAIN) |
| RPG (20; OLD* 除く) | **18 件 severity 00** (ADDVOTE, ADDGBCMT, READGBCMT, EXHBMENU, LRN400, LRN400AUT, NTRSTIT, PARAMETER, CREDITS, PRTLSTCMT, PRTLSTVOTE, ADM* 7 件) |
| RPG 失敗 (2) | **PRINTER** (RNF7030/RNF7260/RNF7503: PRTCMD 未定義・INITP 不正 — 元ソースの欠陥), **BEEMOVIE** (RNF7055: *LOVAL SETLL が非キー付きファイル — 元ソースの欠陥)。いずれも主要機能外 (印刷ユーティリティ・娯楽) |
| CL (5) | 全件作成 (VOTESTUB, ADDGBSTUB, READGBSTUB, VCFSTUB, LRN400STUB) |
| コマンド (4) | 全件作成 (STREXHB, STREXHBEDT, STRVOTERPT, STRCMTEDT) |

**ソース修正 (1 件、既知)**: `PRTLSTVOTE` は `K DISK` 補正済みソースで severity 00 (`pub400_listings/PRTLSTVOTE.txt`) となった。

## 2. VCFMAIN からの主要機能起動 (5250 / tn5250 自動操作)

ドライバ: `tn5250_driver.py` (pexpect + tn5250 0.19.0)、シナリオ: `phase1_runtime_check.py`、ログ: **`phase1_06_runtime_5250.log`** (全入力 + 画面スナップショット + DB スナップショット)。
起動: `CHGLIBL LIBL(ASHIBATA2 ASHIBATA1 QGPL QTEMP)` → `GO ASHIBATA2/VCFMAIN` → "AS/400 DEMO MENU" 表示を確認。

### 2.1 投票 ADDVOTE (VCFMAIN 11 → VOTESTUB → NTRSTIT(ナビ説明) → VOTESCR)

VCFMAIN 経由では展示 ID がユーザープロファイル (ASHIBATA) で保護・事前入力される。展示 ID を入力する経路は `CALL ADDVOTE PARM('MM2024')` (キオスク起動) で検証。

| # | 入力 (バッジ / 展示 / アワード) | 画面出力 (6 行目エラー行) | VOTINGDB |
|---|---|---|---|
| a | 空 / ASHIBATA(固定) / 空 + F5 | `Must enter Award ID` (必須検査はバッジ→アワード→展示 ID の順で、エラー行は最後に失敗した項目のメッセージで上書きされる。DB 検査も同様に 重複→資格→展示存在→アワード存在 の順で上書き) | 変化なし |
| b | 7707 / ASHIBATA / 1 + F5 | `Your vote has been RECORDED` → 確認画面 | **追加 (7707,1,ASHIBATA)** |
| c | 7707 / ASHIBATA / 2 + F5 (重複バッジ) | `You have already voted.` | 変化なし |
| d | 7708 / ASHIBATA / 9 + F5 (存在しないアワード) | `Award does not exist.` | 変化なし |
| e | 7708 / NOSUCH / 1 (MM2024) | `Exhibit does not exist` | 変化なし |
| f | 7708 / (空) / 1 (MM2024) | `Must enter Exhibit ID` | 変化なし |
| g | 7708 / NOVOTE / 1 (MM2024; ELIGIBLE=0 のテスト展示) | `Exhibit ineligible for award` | 変化なし |
| h | 7708 / DEMO400 / 2 (MM2024) | `Your vote has been RECORDED` | **追加 (7708,2,DEMO400)** |
| i | ALWVOTE='N' で VCFMAIN 11 (別ログ `phase1_07_runtime_alwvote.log`) | `SORRY... The voting period has ended and you can no longer vote.` (ENDOFCON) → ENTER でメニューへ | 変化なし。実行後 ALWVOTE='Y' に復元 |

テストデータ: `EXHBDB` に ELIGIBLE=0 の行 `(3,'NOVOTE','Ineligible Exhibitor','Atlanta','GA',...)` を追加 (専用ライブラリ ASHIBATA2 のみ)。既存の DEMO400 は ELIGIBLE=1 のため資格外ケースに使えなかった。

F12 は各画面からメニューへ戻ることを確認 (`back on VCFMAIN`)。数字項目への英字入力はキーボードロック (Reset で解除) となることも記録。

### 2.2 ゲストブック ADDGBCMT / READGBCMT (VCFMAIN 12 / 13、キオスク 3 / 4)

| 操作 | 出力 | GUESTBKDB |
|---|---|---|
| 12 → NTRSTIT → `GUESTBOOK/400 - ADD COMMENT`、空で F5 | `Must enter a comment` | 変化なし |
| 名前 `Devin Tester` / 展示 ASHIBATA(固定) / コメント `Phase1 runtime check badge 7707` + F5 | `Thank you for commenting on this exhibit` (ENDCMT) | **追加 CMTID=3, VISIBLE=Y** |
| 13 → `READ COMMENT`、空 (0) で F5 | `Must enter CommentID` | — |
| ID 1 + F5 | コメント 1 (Great exhibit...) と展示タイトルを表示、`Currently hosting nnnn comments and counting.` | — |
| ID 9999 + F5 (存在しない ID) | エラーは出ず、**最後のコメント**が表示される (SETLL+READ で EOF → 直前に読んだ最終レコードの値が残る。レガシーの仕様上の癖として台帳に記録) | — |
| キオスク 4 → NTRSTIT → READCMT ID 2 + F5 → F12 | コメント 2 表示 → キオスクメニューへ戻る | — |

### 2.3 LEARN/400 LRN400 (VCFMAIN 1、キオスク 2)

- ページ 1 (Welcome) → F5 → ページ 2 (AS/400 history) → F8 → ページ 1 → F5 → ページ 2 → F3 → VCFMAIN。
- F5 ×2 でページ 3 (`EXTRA='END'`) に到達するとプログラムが終了し VCFMAIN に戻る (最終ページ挙動)。
- キオスク 2 経由でも同一画面を確認。

### 2.4 展示キオスク EXHBMENU (`CALL VCFSTUB` = STREXHB 相当)

- `WELCOME TO... IBM i on PUB400 Demo / HOSTED BY Akira Shibata OF Tokyo JP / 1..4 のメニュー` 表示。
- サブ機能終了後は RPG サイクル (*INLR オフ) によりキオスクメニューが再表示される (ループ)。F3 は `Function key not allowed.`。
- 隠しオプション 7 (キオスク終了): `Are you sure you want to exit the kiosk? Type the Administrator password`。
  - 誤パスワード → キオスクメニュー再表示 (終了しない)。
  - SETTINGS.ADMPSWRD の正しいパスワード → *INLR オン、VCFMAIN に復帰 (確認済み)。

### 2.5 DB 状態 (実行前後)

実行前 (`phase1_06_runtime_5250.log`, baseline seed 後):

```
VOTINGDB  (1,1,ASHIBATA) (28,2,ASHIBATA)
GUESTBKDB (1,Y,ASHIBATA,'Great exhibit',...) (2,Y,ASHIBATA,'Devin',...)
SETTINGS  ADMPSWRD=VCF2024, ALWVOTE=Y
```

実行後:

```
VOTINGDB  + (7707,1,ASHIBATA) (7708,2,DEMO400)
GUESTBKDB + (3,Y,ASHIBATA,'Devin Tester','Phase1 runtime check badge 7707')
SETTINGS  ADMPSWRD=VCF2024, ALWVOTE=Y
```

`phase1_07_runtime_alwvote.log` では ALWVOTE=N に変更して
`The voting period has ended and you can no longer vote.` を確認し、finally で
ALWVOTE=Y に復元した。最後に seed SQL を再実行し、
`phase1_08_final_seed.log` で baseline (VOTINGDB 2 行、GUESTBKDB 2 行、
EXHBDB 3 行、LRN400STR 3 行、SETTINGS 2 行) を確認した。

## 3. 未実施・制約

- RSTLIB/RSTOBJ は権限不足で不可 (上記)。復元済みオブジェクトとの同一性は保証できないため、コンパイルリスト (severity 00) を根拠とする。
- PRINTER / BEEMOVIE は元ソースの欠陥でコンパイル不可 (主要機能外、修正は行っていない)。
- 管理メニュー (VCFMAIN 90 / ADM*) は今回の主要機能対象外のため起動確認のみ行っていない (仕様書ではソース解析で記載)。
- VCFMAIN 2–10 (History/About/OfficeVision/ゲーム) は VCF リポジトリ外のオブジェクト呼び出しで対象外。
