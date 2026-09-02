# フェーズ3 仕様適合性検証レポート

## 1.概要

- 対象仕様書: 18ファイル（`docs/specs/programs/*.md`）
- BR-ID照合件数: 71件
- PARM／画面フォーマット／DBアクセス／エラー定数・固定文言を含む総照合件数: **280件**
- 判定区分: `一致` / `相違(修正済)` / `未実装` / `解釈差(前提)`

| 判定 | 件数 |
|---|---:|
| 一致 | 243 |
| 相違(修正済) | 6 |
| 未実装 | 25 |
| 解釈差(前提) | 6 |

照合対象はBR-IDだけでなく、各仕様書のPARM表、画面入力・出力表、DBアクセス表、エラーCONST表（表がない場合は「なし」の記載）を個別行として扱った。`ADMOFRLIST` は `ADMADDSOFR.md` 内の記載に従い、`AdminSecurityOfficerService#list` として照合した。

## 2.ADDGBCMT

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-ADDGBCMT-01` | `INID` 空は `Must enter Exhibit ID`、`*IN40`。 | `AddGuestbookCommentService#submit` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADDGBCMT-02` | `INNAME` 空は `Must enter your name`、`*IN41`。 | `AddGuestbookCommentService#submit` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADDGBCMT-03` | `INCMT` 空は `Must enter a comment`、`*IN42`。 | `AddGuestbookCommentService#submit` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADDGBCMT-04` | `CMTID` は EOF まで読んだ最後の ID に 1 を加える。空ファイルは 1 とする。 | `AddGuestbookCommentService#submit` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADDGBCMT-05` | 新規コメントの `VISIBLE='Y'`。 | `AddGuestbookCommentService#submit` | **相違(修正済)** | EXHBDB存在チェックを削除し、RPGの実アクセスなしに一致させた |
| `PARM-ADDGBCMT-01` | 1 / `LAUNCH` / 文字 20A / CL から渡す現在ユーザプロファイル | `AddGuestbookCommentService#submit（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-ADDGBCMT-01` | `INID` / 文字 / 9A / `ADDCMT` / Exhibit ID | `AddGuestbookCommentService#submit + UI /guestbook/add, /api/guestbook` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADDGBCMT-02` | `INNAME` / 文字 / 16A / `ADDCMT` / 投稿者名（DB は 20A） | `AddGuestbookCommentService#submit + UI /guestbook/add, /api/guestbook` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADDGBCMT-03` | `INCMT` / 文字 / 200A / `ADDCMT` / コメント | `AddGuestbookCommentService#submit + UI /guestbook/add, /api/guestbook` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADDGBCMT-04` | `ERRLINE` / 文字 / 21A / `ADDCMT` / エラー | `AddGuestbookCommentService#submit + UI /guestbook/add, /api/guestbook` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADDGBCMT-05` | — / 固定文言 / — / `ENDCMT` / `Press ENTER to exit.` 等 | `AddGuestbookCommentService#submit + UI /guestbook/add, /api/guestbook` | **一致** | 画面フォーマットの入出力項目を確認 |
| `DB-ADDGBCMT-01` | `GUESTBKDB` / `READ`（EOF まで） / キー順 | `AddGuestbookCommentService#submit + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADDGBCMT-02` | `GUESTBKDB` / `WRITE` / `CMTID` | `AddGuestbookCommentService#submit + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADDGBCMT-03` | `EXHBDB` / 参照ファイル定義のみ / 実アクセスなし | `AddGuestbookCommentService#submit + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `ERR-ADDGBCMT-01` | `SUCCESS` / `Thanks for commenting` / 定義のみ | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADDGBCMT-02` | `VISYES` / `Y` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADDGBCMT-03` | `ERREXID` / `Must enter Exhibit ID` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADDGBCMT-04` | `ERRNOCMT` / `Must enter a comment` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADDGBCMT-05` | `ERREXIST` / `Exhibit not found` / 未使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADDGBCMT-06` | `ERRNONAME` / `Must enter your name    ` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |

## 2.ADDVOTE

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-ADDVOTE-01` | `INPUTBADGE=0` の場合、`ERRLINE='Must enter badge number'`、`*IN40` を設定する。 | `AddVoteService#submit` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADDVOTE-02` | `INEXHB` が空の場合、`ERRLINE='Must enter Exhibit ID'`、`*IN41` を設定する。 | `AddVoteService#submit` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADDVOTE-03` | `INPUTAWARD=0` の場合、`ERRLINE='Must enter Award ID'`、`*IN42` を設定する。 | `AddVoteService#submit` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADDVOTE-04` | `SETTINGS` の `ALWVOTE` が `N` の場合、投票を受け付けず `ENDOFCON` を表示する。 | `AddVoteService#submit` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADDVOTE-05` | `VOTINGDB` に `BADGENBR` が存在する場合、`You have already voted.` とする（1バッジ1票）。 | `AddVoteService#submit` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADDVOTE-06` | `EXHBDB` の `ELIGIBLE=0` の場合、`Exhibit ineligible for award` とする。 | `AddVoteService#submit` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADDVOTE-07` | 展示が存在しない場合、`Exhibit does not exist` とする。 | `AddVoteService#submit` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADDVOTE-08` | 賞が存在しない場合、`Award does not exist.` とする。 | `AddVoteService#submit` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADDVOTE-09` | `INEXHB='DAVE'` の場合、DB 登録せず `DAVE` を表示する。 | `AddVoteService#submit` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `PARM-ADDVOTE-01` | 1 / `LAUNCH` / 文字 9A / CL が取得した現在ユーザプロファイル（出展者 ID） | `AddVoteService#submit（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-ADDVOTE-01` | `INPUTBADGE` / 10進数 / 4D0 / `VOTE1` 入力 / SFGE Badge Number | `AddVoteService#submit + UI /vote, /api/vote` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADDVOTE-02` | `INEXHB` / 文字 / 9A / `VOTE1` 入出力 / Exhibit ID。通常は保護 | `AddVoteService#submit + UI /vote, /api/vote` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADDVOTE-03` | `INPUTAWARD` / 10進数 / 3D0 / `VOTE1` 入力 / Award ID | `AddVoteService#submit + UI /vote, /api/vote` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADDVOTE-04` | `ERRLINE` / 文字 / 45A / `VOTE1` 出力 / 検証エラー | `AddVoteService#submit + UI /vote, /api/vote` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADDVOTE-05` | — / 固定文言 / — / `VOTEEND` / 投票完了 | `AddVoteService#submit + UI /vote, /api/vote` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADDVOTE-06` | — / 固定文言 / — / `DAVE` / `DAVE` 入力時の画面 | `AddVoteService#submit + UI /vote, /api/vote` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADDVOTE-07` | — / 固定文言 / — / `NOVOTEALWD`/`ENDOFCON` / 不適格/投票期間終了 | `AddVoteService#submit + UI /vote, /api/vote` | **一致** | 画面フォーマットの入出力項目を確認 |
| `DB-ADDVOTE-01` | `VOTINGDB` / `CHAIN` / `BADGENBR=INPUTBADGE` | `AddVoteService#submit + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADDVOTE-02` | `VOTINGDB` / `WRITE` / `BADGENBR, AWARDNBR, EXHBNBR` | `AddVoteService#submit + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADDVOTE-03` | `EXHBDB` / `SETLL`/`READ`、`CHAIN` / `EXHUSRPRF=INEXHB` | `AddVoteService#submit + 対応Repository（H2）` | **解釈差(前提)** | 仕様書9節の前提に基づくJava置換 |
| `DB-ADDVOTE-04` | `AWARDDB` / `CHAIN` / `AWARDID=INPUTAWARD` | `AddVoteService#submit + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADDVOTE-05` | `SETTINGS` / `SETLL *LOVAL`/`READ` / `SETTING` 全件、`ALWVOTE` | `AddVoteService#submit + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `ERR-ADDVOTE-01` | `SUCCESS` / `You have voted successfully` / 定義のみ（画面では固定文言） | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADDVOTE-02` | `ERRBLKBG` / `Must enter badge number` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADDVOTE-03` | `ERRBLKEX` / `Must enter Exhibit ID` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADDVOTE-04` | `ERRBLKAW` / `Must enter Award ID` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADDVOTE-05` | `ERREXIST` / `You have already voted.` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADDVOTE-06` | `ERRENTER` / `Use F5 key to submit` / 定義のみ | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADDVOTE-07` | `ERRPROHB` / `Exhibit ineligible for award` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADDVOTE-08` | `ERRNOEXB` / `Exhibit does not exist` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADDVOTE-09` | `ERRNOAWD` / `Award does not exist.` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADDVOTE-10` | `ERRDBG1`/`ERRDBG2` / `DBG: MM2024 parm` / `DBG: other parm` / 未使用 | `RpgMessages#対応文言定数` | **未実装** | CONST／固定メッセージの文言と使用有無を確認 |

## 2.ADMADDSOFR

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-ADMADDSOFR-01` | Enter 後、`INADMNAME` を `SECOFRS.USERPROF` として登録する。 | `AdminSecurityOfficerService#add/list` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADMADDSOFR-02` | `SECOFRS` の UNIQUE キー重複は登録できない。 | `AdminSecurityOfficerService#add/list` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `PARM-ADMADDSOFR-01` | （入口パラメータなし／コメントアウト） | `AdminSecurityOfficerService#add/list（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-ADMADDSOFR-01` | `INADMNAME` / 文字 / 9A / `ADDADM` / 追加する USRPRF | `AdminSecurityOfficerService#add/list + UI /admin/officers, /api/admin/officers` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMADDSOFR-02` | — / 固定文言 / — / `LISTADMRCD` / 一覧未実装プレースホルダ | `AdminSecurityOfficerService#add/list + UI /admin/officers, /api/admin/officers` | **一致** | 画面フォーマットの入出力項目を確認 |
| `DB-ADMADDSOFR-01` | `SECOFRS` / `READ` / 全件 | `AdminSecurityOfficerService#add/list + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADMADDSOFR-02` | `SECOFRS` / `SETLL *LOVAL` / 先頭 | `AdminSecurityOfficerService#add/list + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADMADDSOFR-03` | `SECOFRS` / `WRITE` / `USERPROF=INADMNAME` | `AdminSecurityOfficerService#add/list + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `ERR-ADMADDSOFR-01` | `STSOK` / `Status updated` / 定義のみ | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |

## 2.ADMCRTEXHB

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-ADMCRTEXHB-01` | `EXHIBIT='NONE'` は新規モード。 | `AdminExhibitService#loadSession/writeExhb/delRcd` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADMCRTEXHB-02` | 対象が存在すれば項目を読込み編集モード、なければ `USRPRF not found, creating new`。 | `AdminExhibitService#loadSession/writeExhb/delRcd` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADMCRTEXHB-03` | `INUSRPRF` 空は `USRPRF cannot be blank     `。 | `AdminExhibitService#loadSession/writeExhb/delRcd` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADMCRTEXHB-04` | 新規時に同一 `INUSRPRF` が存在すれば `Error - USRPRF exists`。 | `AdminExhibitService#loadSession/writeExhb/delRcd` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADMCRTEXHB-05` | 更新時は `EXHBREC` を UPDATE する。 | `AdminExhibitService#loadSession/writeExhb/delRcd` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADMCRTEXHB-06` | F5 は削除確認を経て Enter で DELETE、F12 で取消。 | `AdminExhibitService#loadSession/writeExhb/delRcd` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADMCRTEXHB-07` | 削除対象不存在は `Not found - not deleted`、成功は `USRPRF was deleted from DB`。 | `AdminExhibitService#loadSession/writeExhb/delRcd` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `PARM-ADMCRTEXHB-01` | 1 / `LAUNCH` / 文字 9A / 呼出し元ユーザ | `AdminExhibitService#loadSession/writeExhb/delRcd（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `PARM-ADMCRTEXHB-02` | 2 / `EXHIBIT` / 文字 9A / 編集対象 ID、`NONE` で新規 | `AdminExhibitService#loadSession/writeExhb/delRcd（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-ADMCRTEXHB-01` | `INUSRPRF` / 文字 / 9A / `ADMCREATE` / 展示 ID | `AdminExhibitService#loadSession/writeExhb/delRcd + UI /admin/exhibits, /api/admin/exhibits` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMCRTEXHB-02` | `INTITLE` / 文字 / 60A / `ADMCREATE` / タイトル（DB 50A） | `AdminExhibitService#loadSession/writeExhb/delRcd + UI /admin/exhibits, /api/admin/exhibits` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMCRTEXHB-03` | `INNAME` / 文字 / 20A / `ADMCREATE` / 出展者名 | `AdminExhibitService#loadSession/writeExhb/delRcd + UI /admin/exhibits, /api/admin/exhibits` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMCRTEXHB-04` | `INCITY` / 文字 / 15A / `ADMCREATE` / 市（DB 20A） | `AdminExhibitService#loadSession/writeExhb/delRcd + UI /admin/exhibits, /api/admin/exhibits` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMCRTEXHB-05` | `INSTATE` / 文字 / 2A / `ADMCREATE` / 州 | `AdminExhibitService#loadSession/writeExhb/delRcd + UI /admin/exhibits, /api/admin/exhibits` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMCRTEXHB-06` | `INDESC` / 文字 / 1000A / `ADMCREATE` / 説明 | `AdminExhibitService#loadSession/writeExhb/delRcd + UI /admin/exhibits, /api/admin/exhibits` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMCRTEXHB-07` | `INELIGIBLE` / 文字 / 1A / `ADMCREATE` / 0/1 | `AdminExhibitService#loadSession/writeExhb/delRcd + UI /admin/exhibits, /api/admin/exhibits` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMCRTEXHB-08` | `INLRN400EN` / 数値 / 1 / `ADMCREATE` / LEARN/400 0/1 | `AdminExhibitService#loadSession/writeExhb/delRcd + UI /admin/exhibits, /api/admin/exhibits` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMCRTEXHB-09` | `ERRLINE` / 文字 / 30A / `ADMCREATE` / 状態メッセージ | `AdminExhibitService#loadSession/writeExhb/delRcd + UI /admin/exhibits, /api/admin/exhibits` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMCRTEXHB-10` | `DLTEXHNAM` / 文字 / 9A / `ADMDELETE` / 削除対象 | `AdminExhibitService#loadSession/writeExhb/delRcd + UI /admin/exhibits, /api/admin/exhibits` | **一致** | 画面フォーマットの入出力項目を確認 |
| `DB-ADMCRTEXHB-01` | `EXHBDB` / `CHAIN`/`READE` / `EXHUSRPRF=EDTEXHB` | `AdminExhibitService#loadSession/writeExhb/delRcd + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADMCRTEXHB-02` | `EXHBDB` / `SETLL`/`READ` / `EDTEXHB`（更新前） | `AdminExhibitService#loadSession/writeExhb/delRcd + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADMCRTEXHB-03` | `EXHBDB` / `CHAIN` / `INUSRPRF`（重複確認） | `AdminExhibitService#loadSession/writeExhb/delRcd + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADMCRTEXHB-04` | `EXHBDB` / `WRITE` / `EXHUSRPRF=INUSRPRF` | `AdminExhibitService#loadSession/writeExhb/delRcd + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADMCRTEXHB-05` | `EXHBDB` / `UPDATE` / 現在レコード | `AdminExhibitService#loadSession/writeExhb/delRcd + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADMCRTEXHB-06` | `EXHBDB` / `DELETE` / `INUSRPRF` | `AdminExhibitService#loadSession/writeExhb/delRcd + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADMCRTEXHB-07` | `SECOFRS` / 定義のみ / 認可未実装 | `AdminExhibitService#loadSession/writeExhb/delRcd + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `ERR-ADMCRTEXHB-01` | `EMPTY` / 10個の空白 / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADMCRTEXHB-02` | `ERREXIST` / `Error - USRPRF exists` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADMCRTEXHB-03` | `ERRNF` / `USRPRF not found, creating new` / 使用 | `RpgMessages#USER_CREATED` | **相違(修正済)** | Java定数の文言を仕様と完全一致させた |
| `ERR-ADMCRTEXHB-04` | `ERRNODEL` / `Not found - not deleted` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADMCRTEXHB-05` | `ERRDELOK` / `USRPRF was deleted from DB` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADMCRTEXHB-06` | `ERRBLANK` / `USRPRF cannot be blank     ` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |

## 2.ADMHIDECMT

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-ADMHIDECMT-01` | `INHIDEYN='Y'` は `VISIBLE='Y'` に更新する。 | `AdminHideCommentService#read/list/update` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADMHIDECMT-02` | `INHIDEYN='N'` は `VISIBLE='N'` に更新する。 | `AdminHideCommentService#read/list/update` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADMHIDECMT-03` | その他は `Must type Y or N` とし更新しない。 | `AdminHideCommentService#read/list/update` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADMHIDECMT-04` | 更新成功時は `Status updated`。 | `AdminHideCommentService#read/list/update` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `PARM-ADMHIDECMT-01` | 1 / `LAUNCH` / 文字 20A / Comment ID（RPG 内では数値 ID へ移送） | `AdminHideCommentService#read/list/update（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-ADMHIDECMT-01` | `HIDECMTID` / 文字 / 4A / `HIDECMT` / コメント ID | `AdminHideCommentService#read/list/update + UI /admin/comments, /api/admin/comments/{id}/visibility` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMHIDECMT-02` | `OUTNAME` / 文字 / 11A / `HIDECMT` / 投稿者 | `AdminHideCommentService#read/list/update + UI /admin/comments, /api/admin/comments/{id}/visibility` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMHIDECMT-03` | `OUTCMT` / 文字 / 200A / `HIDECMT` / コメント | `AdminHideCommentService#read/list/update + UI /admin/comments, /api/admin/comments/{id}/visibility` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMHIDECMT-04` | `CURRHIDE` / 文字 / 1A / `HIDECMT` / 現在の状態 | `AdminHideCommentService#read/list/update + UI /admin/comments, /api/admin/comments/{id}/visibility` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMHIDECMT-05` | `INHIDEYN` / 文字 / 1A / `HIDECMT` / 入力 Y/N | `AdminHideCommentService#read/list/update + UI /admin/comments, /api/admin/comments/{id}/visibility` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMHIDECMT-06` | `ERRLINE` / 文字 / 20A / `HIDECMT` / 結果/エラー | `AdminHideCommentService#read/list/update + UI /admin/comments, /api/admin/comments/{id}/visibility` | **一致** | 画面フォーマットの入出力項目を確認 |
| `DB-ADMHIDECMT-01` | `GUESTBKDB` / `SETLL`/`READ` / `CMTID=ID` | `AdminHideCommentService#read/list/update + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADMHIDECMT-02` | `GUESTBKDB` / `UPDATE` / 現在レコード | `AdminHideCommentService#read/list/update + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADMHIDECMT-03` | `SECOFRS` / 参照定義のみ / `CHKUSRPRF` 未実装 | `AdminHideCommentService#read/list/update + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `ERR-ADMHIDECMT-01` | `HIDEY` / `Y` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADMHIDECMT-02` | `HIDEN` / `N` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADMHIDECMT-03` | `ERRCMTID` / `Must enter CommentID` / 定義のみ | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADMHIDECMT-04` | `ERRYN` / `Must type Y or N` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADMHIDECMT-05` | `STSOK` / `Status updated` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |

## 2.ADMLRN400

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-ADMLRN400-01` | 初期レコードを編集対象として表示する。 | `AdminLearn400Service#start/pageFwd/pageBack/save` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADMLRN400-02` | F5 で次ページが存在すれば既存ページを読み、なければ `NEWORUPD=1` とする。 | `AdminLearn400Service#start/pageFwd/pageBack/save` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADMLRN400-03` | `NEWORUPD=0` の F10 は `UPDRCD`、`1` は `CRTRCD`。 | `AdminLearn400Service#start/pageFwd/pageBack/save` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADMLRN400-04` | 新規保存後は既存ページモードに戻す。 | `AdminLearn400Service#start/pageFwd/pageBack/save` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADMLRN400-05` | F8 後は `NEWORUPD=0`、`ALWFWD=1` とする。 | `AdminLearn400Service#start/pageFwd/pageBack/save` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `PARM-ADMLRN400-01` | — / `LAUNCH`/`EXHIBIT` / 9A（コメントアウト） / 管理者・出展者スコープの候補 | `AdminLearn400Service#start/pageFwd/pageBack/save（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-ADMLRN400-01` | `OUTPAGENBR` / 文字 / 4A / `MAINADMIN` / ページ番号 | `AdminLearn400Service#start/pageFwd/pageBack/save + UI /admin/learn400, /api/admin/learn400` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMLRN400-02` | `INCONTENT` / 文字 / 1500A / `MAINADMIN` / 本文 | `AdminLearn400Service#start/pageFwd/pageBack/save + UI /admin/learn400, /api/admin/learn400` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMLRN400-03` | `INEXTRA` / 文字 / 9A / `MAINADMIN` / 命令/追加情報 | `AdminLearn400Service#start/pageFwd/pageBack/save + UI /admin/learn400, /api/admin/learn400` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMLRN400-04` | `*IN60` / 指標 / — / `MAINADMIN` / 新規/既存表示属性 | `AdminLearn400Service#start/pageFwd/pageBack/save + UI /admin/learn400, /api/admin/learn400` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMLRN400-05` | F3/F5/F8/F10 / 指標 / — / `MAINADMIN` / 終了/前進/後退/保存 | `AdminLearn400Service#start/pageFwd/pageBack/save + UI /admin/learn400, /api/admin/learn400` | **一致** | 画面フォーマットの入出力項目を確認 |
| `DB-ADMLRN400-01` | `LRN400STR` / `READ` / 初期先頭 | `AdminLearn400Service#start/pageFwd/pageBack/save + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADMLRN400-02` | `LRN400STR` / `SETLL`/`READ` / `PAGENBR=CURPAGENBR` | `AdminLearn400Service#start/pageFwd/pageBack/save + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADMLRN400-03` | `LRN400STR` / `UPDATE` / `PAGENBR=CURPAGENBR` | `AdminLearn400Service#start/pageFwd/pageBack/save + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADMLRN400-04` | `LRN400STR` / `WRITE` / `PAGENBR=CURPAGENBR` | `AdminLearn400Service#start/pageFwd/pageBack/save + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADMLRN400-05` | `SECOFRS` / 参照定義のみ / 認可チェック未実装 | `AdminLearn400Service#start/pageFwd/pageBack/save + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `ERR-ADMLRN400-01` | CONST メッセージなし | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |

## 2.ADMSETTING

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-ADMSETTING-01` | `SETTING='PASSWORD'` の `VALUE` を `INPWD` に表示する。 | `AdminSettingService#load/save` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADMSETTING-02` | `SETTING='ALWVOTE'` の `VALUE` を `INALWVOTE` に表示する。 | `AdminSettingService#load/save` | **相違(修正済)** | 画面1/0をDBのY/Nへ変換する処理を追加した |
| `BR-ADMSETTING-03` | Enter 後、両設定を対応する行へ UPDATE する。 | `AdminSettingService#load/save` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `PARM-ADMSETTING-01` | （入口パラメータなし／コメントアウト） | `AdminSettingService#load/save（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-ADMSETTING-01` | `INPWD` / 文字 / 9A / `SETUPMAIN` / 管理者パスワード | `AdminSettingService#load/save + UI /admin/settings, /api/admin/settings` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMSETTING-02` | `INALWVOTE` / 文字 / 1 / `SETUPMAIN` / 投票有効/無効 | `AdminSettingService#load/save + UI /admin/settings, /api/admin/settings` | **一致** | 画面フォーマットの入出力項目を確認 |
| `DB-ADMSETTING-01` | `SETTINGS` / `SETLL *LOVAL`/`READ` 全件 / `SETTING` | `AdminSettingService#load/save + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADMSETTING-02` | `SETTINGS` / `UPDATE` / `SETTING='PASSWORD'`/`ALWVOTE` | `AdminSettingService#load/save + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `ERR-ADMSETTING-01` | CONST メッセージなし | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |

## 2.ADMVOTERPT

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-ADMVOTERPT-01` | `VOTINGDB` 全件を読み `NUMBR` を総票数とする。 | `AdminVoteReportService#readVote` | **相違(修正済)** | 総票数を対象出展者限定からVOTINGDB全件へ修正した |
| `BR-ADMVOTERPT-02` | `EXHBNBR=USERPROF` かつ `AWARDNBR=1` を `VOTEEFA`、2 を `VOTEBIS` に加算する。 | `AdminVoteReportService#readVote` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADMVOTERPT-03` | `EXHBDB` に対象があれば `OUTTITLE=EXHBTITLE`、なければ `USRPRF was not found`。 | `AdminVoteReportService#readVote` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-ADMVOTERPT-04` | F3 で終了し、それ以外の入力で再集計する。 | `AdminVoteReportService#readVote` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `PARM-ADMVOTERPT-01` | 1 / `LAUNCH` / 文字 9A / 初期の `USERPROF` | `AdminVoteReportService#readVote（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-ADMVOTERPT-01` | `INUSRPRF` / 文字 / 9A / `VOTERES` / 集計対象の出展者 | `AdminVoteReportService#readVote + UI /admin/votes, /api/admin/votes/{profile}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMVOTERPT-02` | `OUTTITLE` / 文字 / 50A / `VOTERES` / 展示名 | `AdminVoteReportService#readVote + UI /admin/votes, /api/admin/votes/{profile}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMVOTERPT-03` | `OUTEFA` / 文字 / 4A / `VOTERES` / Ed Fair 票数 | `AdminVoteReportService#readVote + UI /admin/votes, /api/admin/votes/{profile}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMVOTERPT-04` | `OUTBIS` / 文字 / 4A / `VOTERES` / Best in Show 票数 | `AdminVoteReportService#readVote + UI /admin/votes, /api/admin/votes/{profile}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMVOTERPT-05` | `OUTVOTEN` / 文字 / 4A / `VOTERES` / DB 総票数 | `AdminVoteReportService#readVote + UI /admin/votes, /api/admin/votes/{profile}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-ADMVOTERPT-06` | `ERRLINE` / 文字 / 40A / `VOTERES` / エラー | `AdminVoteReportService#readVote + UI /admin/votes, /api/admin/votes/{profile}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `DB-ADMVOTERPT-01` | `VOTINGDB` / `SETLL *LOVAL`/`READ` 全件 / なし | `AdminVoteReportService#readVote + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADMVOTERPT-02` | `EXHBDB` / `CHAIN`/`READE` / `EXHUSRPRF=USERPROF` | `AdminVoteReportService#readVote + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-ADMVOTERPT-03` | `SECOFRS` / 参照定義のみ / 認可処理は空 | `AdminVoteReportService#readVote + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `ERR-ADMVOTERPT-01` | `ERRBLANK` / `USRPRF cannot be blank     ` / 未使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADMVOTERPT-02` | `ERRNOTF` / `USRPRF was not found       ` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-ADMVOTERPT-03` | `DBGNAME` / `NL2024` / 定義のみ | `RpgMessages#対応文言定数` | **未実装** | CONST／固定メッセージの文言と使用有無を確認 |

## 2.BEEMOVIE

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-BEEMOVIE-01` | `BMOVDB` をキー順に全件読み、1行ずつ `SFLDATA` に書く。 | `BeeMovieService#all` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-BEEMOVIE-02` | 表示後、F3 で終了する。 | `BeeMovieService#all` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `PARM-BEEMOVIE-01` | （入口パラメータなし／コメントアウト） | `BeeMovieService#all（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-BEEMOVIE-01` | `SPEAKER` / 文字 / 9A / `SFLDATA` 出力 / 話者 | `BeeMovieService#all + UI /admin/beemovie, /api/beemovie` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-BEEMOVIE-02` | `LINE` / 文字 / 50A / `SFLDATA` 出力 / Spoken Line | `BeeMovieService#all + UI /admin/beemovie, /api/beemovie` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-BEEMOVIE-03` | `RRN#` / 数値 / 4S0 / サブファイル制御 / 相対レコード番号 | `BeeMovieService#all + UI /admin/beemovie, /api/beemovie` | **一致** | 画面フォーマットの入出力項目を確認 |
| `DB-BEEMOVIE-01` | `BMOVDB` / `SETLL *LOVAL`/`READ` 全件 / `LINE` | `BeeMovieService#all + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `ERR-BEEMOVIE-01` | CONST メッセージなし | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |

## 2.CREDITS

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-CREDITS-01` | 起動時に `SCREEN1` を一度表示する。 | `StaticScreenService#credits` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `PARM-CREDITS-01` | （入口パラメータなし／コメントアウト） | `StaticScreenService#credits（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-CREDITS-01` | — / 固定文字列 / — / `SCREEN1` / `VCF/400 - Credits and Copyright` 等 | `StaticScreenService#credits + UI /credits, /api/static/credits` | **一致** | 画面フォーマットの入出力項目を確認 |
| `DB-CREDITS-01` | DB アクセスなし | `StaticScreenService#credits + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `ERR-CREDITS-01` | CONST メッセージなし | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |

## 2.EXHBMENU

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-EXHBMENU-01` | `LAUNCH` を `EXHBDB.EXHUSRPRF` で読み、展示情報を表示する。 | `ExhibitMenuService#load/select/exitKiosk` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-EXHBMENU-02` | `ELIGIBLE≠1` の場合、`*IN41` で投票選択肢を非表示にする。 | `ExhibitMenuService#load/select/exitKiosk` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-EXHBMENU-03` | `ENLRN400≠1` の場合、`*IN40` で LEARN/400 選択肢を非表示にする。 | `ExhibitMenuService#load/select/exitKiosk` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-EXHBMENU-04` | `INOPT=1` は `CHKALWVOTE=1` の場合のみ `VOTESTUB`。 | `ExhibitMenuService#load/select/exitKiosk` | **相違(修正済)** | ALWVOTE=N時にVOTESTUBへ遷移しない判定を追加した |
| `BR-EXHBMENU-05` | `INOPT=2` は `CHKLRN400=1` の場合のみ `LRN400STUB`。 | `ExhibitMenuService#load/select/exitKiosk` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-EXHBMENU-06` | `INOPT=3/4` は各ゲストブックスタブ、`7` は `ADMPSWRD`。 | `ExhibitMenuService#load/select/exitKiosk` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-EXHBMENU-07` | 入力パスワードが終了値と一致したら LR をオンにする。 | `ExhibitMenuService#load/select/exitKiosk` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `PARM-EXHBMENU-01` | 1 / `LAUNCH` / 文字 9A / 出展者 ID | `ExhibitMenuService#load/select/exitKiosk（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-EXHBMENU-01` | `OUTTITLE` / 文字 / 75A / `MENU` / 展示タイトル | `ExhibitMenuService#load/select/exitKiosk + UI /menu, /api/menu/{profile}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-EXHBMENU-02` | `OUTNAME` / 文字 / 25A / `MENU` / 出展者名 | `ExhibitMenuService#load/select/exitKiosk + UI /menu, /api/menu/{profile}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-EXHBMENU-03` | `OUTCITY` / 文字 / 15A / `MENU` / 市 | `ExhibitMenuService#load/select/exitKiosk + UI /menu, /api/menu/{profile}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-EXHBMENU-04` | `OUTSTATE` / 文字 / 2A / `MENU` / 州 | `ExhibitMenuService#load/select/exitKiosk + UI /menu, /api/menu/{profile}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-EXHBMENU-05` | `OUTDESC` / 文字 / 1000A / `MENU` / 説明 | `ExhibitMenuService#load/select/exitKiosk + UI /menu, /api/menu/{profile}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-EXHBMENU-06` | `INOPT` / 数値 / 1Y0 / `MENU` / 1〜4、7 | `ExhibitMenuService#load/select/exitKiosk + UI /menu, /api/menu/{profile}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-EXHBMENU-07` | `INPWD` / 文字 / 9A / `ADMPSWRD` / 終了パスワード | `ExhibitMenuService#load/select/exitKiosk + UI /menu, /api/menu/{profile}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-EXHBMENU-08` | `DLTEXHNAM` / 文字 / 9A / `ADMDELETE`/`ADMDELETE2` / 削除確認表示 | `ExhibitMenuService#load/select/exitKiosk + UI /menu, /api/menu/{profile}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `DB-EXHBMENU-01` | `EXHBDB` / `SETLL`/`READ` / `EXHUSRPRF=LAUNCH` | `ExhibitMenuService#load/select/exitKiosk + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-EXHBMENU-02` | `SETTINGS` / `SETLL *START`/`READ` / 先頭レコード（RPG） | `ExhibitMenuService#load/select/exitKiosk + 対応Repository（H2）` | **解釈差(前提)** | 仕様書9節の前提に基づくJava置換 |
| `ERR-EXHBMENU-01` | CONST メッセージなし | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |

## 2.LRN400

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-LRN400-01` | 初期表示は `LRN400STR` の先頭レコード。 | `Learn400Service#start/pageFwd/pageBack` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-LRN400-02` | `ALWFWD=1` のとき F5 で `CURPAGENBR+1`。 | `Learn400Service#start/pageFwd/pageBack` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-LRN400-03` | 次ページがない場合 `ALWFWD=0` とし、それ以降 F5 で進まない。 | `Learn400Service#start/pageFwd/pageBack` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-LRN400-04` | `EXTRA='END'` は終了、本文完全一致 `CALL` は `EXTRA` のプログラムを呼び、本文完全一致 `JUMP` は `EXTRA` ページへ移動する。 | `Learn400Service#start/pageFwd/pageBack` | **解釈差(前提)** | 仕様書9節の前提に基づくJava置換 |
| `BR-LRN400-05` | F8 は `CURPAGENBR` を減算し、`FRMPAGENBR` があればそのページを表示してリセットする。 | `Learn400Service#start/pageFwd/pageBack` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `PARM-LRN400-01` | — / `LAUNCH` / 9A（コメントアウト） / `LRN400STUB` がプロファイル別 DB を切り替える | `Learn400Service#start/pageFwd/pageBack（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-LRN400-01` | `OUTPAGENBR` / 文字 / 4A / `MAIN` / 現在ページ | `Learn400Service#start/pageFwd/pageBack + UI /learn400, /api/learn400/{owner}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-LRN400-02` | `OUTCONTENT` / 文字 / 1500A / `MAIN` / ページ本文 | `Learn400Service#start/pageFwd/pageBack + UI /learn400, /api/learn400/{owner}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-LRN400-03` | F3 / 指標 / — / `MAIN` / 終了 | `Learn400Service#start/pageFwd/pageBack + UI /learn400, /api/learn400/{owner}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-LRN400-04` | F5/F8 / 指標 / — / `MAIN` / 前進/後退 | `Learn400Service#start/pageFwd/pageBack + UI /learn400, /api/learn400/{owner}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `DB-LRN400-01` | `LRN400STR` / `READ` / 初期先頭 | `Learn400Service#start/pageFwd/pageBack + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-LRN400-02` | `LRN400STR` / `CHAIN`/`READE` / `PAGENBR=CURPAGENBR` | `Learn400Service#start/pageFwd/pageBack + 対応Repository（H2）` | **解釈差(前提)** | 仕様書9節の前提に基づくJava置換 |

## 2.LRN400AUT

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-LRN400AUT-01` | `WAITRCD` 秒ごとに自動進行する。 | `Learn400AutoService#start/pageFwd` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-LRN400AUT-02` | `EXTRA='END'` のみ認識し、`CALL`/`JUMP` は無効である。 | `Learn400AutoService#start/pageFwd` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-LRN400AUT-03` | END で `CURPAGENBR=0` とし、次の前進で 1 ページ目に戻る。 | `Learn400AutoService#start/pageFwd` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-LRN400AUT-04` | F3 で終了する。 | `Learn400AutoService#start/pageFwd` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `PARM-LRN400AUT-01` | （入口パラメータなし／コメントアウト） | `Learn400AutoService#start/pageFwd（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-LRN400AUT-01` | `OUTPAGENBR` / 文字 / 4A / `MAIN` / 現在ページ | `Learn400AutoService#start/pageFwd + UI /learn400/auto, /api/learn400/auto/{owner}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-LRN400AUT-02` | `OUTCONTENT` / 文字 / 1500A / `MAIN` / 本文 | `Learn400AutoService#start/pageFwd + UI /learn400/auto, /api/learn400/auto/{owner}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-LRN400AUT-03` | F3/F5/F8 / 指標 / — / `MAIN` / 終了/前進/後退 | `Learn400AutoService#start/pageFwd + UI /learn400/auto, /api/learn400/auto/{owner}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `DB-LRN400AUT-01` | `LRN400STR` / `READ` / 初期先頭 | `Learn400AutoService#start/pageFwd + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-LRN400AUT-02` | `LRN400STR` / `CHAIN`/`READE` / `PAGENBR` | `Learn400AutoService#start/pageFwd + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `ERR-LRN400AUT-01` | CONST メッセージなし | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |

## 2.NTRSTIT

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-NTRSTIT-01` | 呼出し時に `INTERTEST` を表示し、Enter 後に呼出し元へ戻る。 | `StaticScreenService#help` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `PARM-NTRSTIT-01` | （入口パラメータなし／コメントアウト） | `StaticScreenService#help（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-NTRSTIT-01` | — / 固定文字列 / — / `INTERTEST` / 操作説明 | `StaticScreenService#help + UI /help, /api/static/help` | **一致** | 画面フォーマットの入出力項目を確認 |
| `DB-NTRSTIT-01` | DB アクセスなし | `StaticScreenService#help + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `ERR-NTRSTIT-01` | CONST メッセージなし | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |

## 2.OLDADDVOTE

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-OLDADDVOTE-01` | `INPUTBADGE=0` の場合 `Must enter badge number`。 | `旧版・非移行のため対応なし` | **未実装** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-OLDADDVOTE-02` | 旧版の入力値を `VOTINGREC` に移送して WRITE する。 | `旧版・非移行のため対応なし` | **未実装** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-OLDADDVOTE-03` | `OLDVOTE` は `Must fill out all fields` を設定するが、処理を中断する RETURN はコメントアウトされている。 | `旧版・非移行のため対応なし` | **未実装** | BR-IDの業務ルールをサービスメソッドで確認 |
| `PARM-OLDADDVOTE-01` | （入口パラメータなし／コメントアウト） | `旧版・非移行のため対応なし` | **未実装** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-OLDADDVOTE-01` | `INPUTBADGE` / 10進数 / 4D0 / `VOTE1` / Badge Number | `旧版・非移行のため対応なし` | **未実装** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-OLDADDVOTE-02` | `INPUTEXHB` / 数値 / RPG 由来 / `VOTE1` / Exhibit ID（旧版） | `旧版・非移行のため対応なし` | **未実装** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-OLDADDVOTE-03` | `INPUTAWARD` / 10進数 / 3D0 / `VOTE1` / Award ID | `旧版・非移行のため対応なし` | **未実装** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-OLDADDVOTE-04` | `ERRLINE` / 文字 / 45A / `VOTE1` / エラー | `旧版・非移行のため対応なし` | **未実装** | 画面フォーマットの入出力項目を確認 |
| `DB-OLDADDVOTE-01` | `VOTINGDB` / `CHAIN`（旧 `ADDTODB`） / `INPUTBADGE` | `旧版・非移行のため対応なし` | **未実装** | DB命令・対象キーをRepository対応へ確認 |
| `DB-OLDADDVOTE-02` | `VOTINGDB` / `READ` / 全件/EOF | `旧版・非移行のため対応なし` | **未実装** | DB命令・対象キーをRepository対応へ確認 |
| `DB-OLDADDVOTE-03` | `VOTINGDB` / `WRITE` / `BADGENBR` | `旧版・非移行のため対応なし` | **未実装** | DB命令・対象キーをRepository対応へ確認 |
| `DB-OLDADDVOTE-04` | `EXHBDB` / `SETLL`/`READ`、`CHAIN` / `INPUTEXHB` | `旧版・非移行のため対応なし` | **未実装** | DB命令・対象キーをRepository対応へ確認 |
| `DB-OLDADDVOTE-05` | `AWARDDB` / `CHAIN` / `INPUTAWARD` | `旧版・非移行のため対応なし` | **未実装** | DB命令・対象キーをRepository対応へ確認 |
| `ERR-OLDADDVOTE-01` | `SUCCESS` / `You have voted successfully` / 定義のみ | `旧版・非移行のため対応なし` | **未実装** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-OLDADDVOTE-02` | `ERRBLKBG` / `Must enter badge number` / `OLDADDVOTE` 使用 | `旧版・非移行のため対応なし` | **未実装** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-OLDADDVOTE-03` | `ERRBLKEX` / `Must enter Exhibit ID` / `OLDADDVOTE` 使用 | `旧版・非移行のため対応なし` | **未実装** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-OLDADDVOTE-04` | `ERRBLKAW` / `Must enter Award ID` / `OLDADDVOTE` 使用 | `旧版・非移行のため対応なし` | **未実装** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-OLDADDVOTE-05` | `ERREXIST` / `You have already voted.` / 定義のみ | `旧版・非移行のため対応なし` | **未実装** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-OLDADDVOTE-06` | `ERRENTER` / `Use F5 key to submit` / 定義のみ | `旧版・非移行のため対応なし` | **未実装** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-OLDADDVOTE-07` | `ERRPROHB` / `Exhibit ineligible for award` / `OLDADDVOTE` 使用 | `旧版・非移行のため対応なし` | **未実装** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-OLDADDVOTE-08` | `ERRNOEXB` / `Exhibit does not exist` / `OLDADDVOTE` 使用 | `旧版・非移行のため対応なし` | **未実装** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-OLDADDVOTE-09` | `ERRNOAWD` / `Award does not exist.` / `OLDADDVOTE` 使用 | `旧版・非移行のため対応なし` | **未実装** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-OLDADDVOTE-10` | `ERRBLANK` / `Must fill out all fields` / `OLDVOTE` 使用 | `旧版・非移行のため対応なし` | **未実装** | CONST／固定メッセージの文言と使用有無を確認 |

## 2.PARAMETER

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-PARAMETER-01` | `LAUNCH` を `PARMOUT`、`LAUNCH2` を `PARMOUT2` に表示する。 | `StaticScreenService#parameters` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `PARM-PARAMETER-01` | 1 / `LAUNCH` / 文字 20A / 第1パラメータ | `StaticScreenService#parameters（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `PARM-PARAMETER-02` | 2 / `LAUNCH2` / 文字 20A / 第2パラメータ | `StaticScreenService#parameters（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-PARAMETER-01` | `PARMOUT` / 文字 / 20 / `TESTPARM` / `LAUNCH` の表示 | `StaticScreenService#parameters + UI /parameter, /api/static/parameters` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-PARAMETER-02` | `PARMOUT2` / 文字 / 20 / `TESTPARM` / `LAUNCH2` の表示 | `StaticScreenService#parameters + UI /parameter, /api/static/parameters` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-PARAMETER-03` | `SOMETHING` / 文字 / 5A / `TESTPARM` / テスト入力（RPG では未使用） | `StaticScreenService#parameters + UI /parameter, /api/static/parameters` | **一致** | 画面フォーマットの入出力項目を確認 |
| `DB-PARAMETER-01` | DB アクセスなし | `StaticScreenService#parameters + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `ERR-PARAMETER-01` | CONST メッセージなし | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |

## 2.PRTLSTVOTE

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-PRTLSTVOTE-01` | `VOTINGDB` を EOF まで読み、最後に読んだ投票をレシートへ出力する。 | `PrintSpoolService#printVoteTicket/printCommentTicket` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-PRTLSTVOTE-02` | コメント印刷では `GUESTBKDB` の最後のコメントを出力する。 | `PrintSpoolService#printVoteTicket/printCommentTicket` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-PRTLSTVOTE-03` | `PRINTER` は `X'1B40'` 初期化、帳票出力後 `X'1D56'` を送る。 | `PrintSpoolService#printVoteTicket/printCommentTicket` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `PARM-PRTLSTVOTE-01` | （入口パラメータなし／コメントアウト） | `PrintSpoolService#printVoteTicket/printCommentTicket（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-PRTLSTVOTE-01` | — / プリンタ出力 / — / `VOTEPRTF` / `RCD001`、`RCD002`、`RCD003`、`EXHBOUT` | `PrintSpoolService#printVoteTicket/printCommentTicket（スプール）` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-PRTLSTVOTE-02` | — / プリンタ出力 / — / `CMTPRTF` / `RCD001`、`RCD002`、`RCD003`、`CMTNBR`、`CMTDETAIL` | `PrintSpoolService#printVoteTicket/printCommentTicket（スプール）` | **一致** | 画面フォーマットの入出力項目を確認 |
| `DB-PRTLSTVOTE-01` | `VOTINGDB` / `SETLL *LOVAL`/`READ` EOF / キー順 | `PrintSpoolService#printVoteTicket/printCommentTicket + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-PRTLSTVOTE-02` | `GUESTBKDB` / `SETLL *LOVAL`/`READ` EOF / キー順 | `PrintSpoolService#printVoteTicket/printCommentTicket + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-PRTLSTVOTE-03` | `VOTEPRTF` / `WRITE` / プリンタレコード | `PrintSpoolService#printVoteTicket/printCommentTicket + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-PRTLSTVOTE-04` | `CMTPRTF` / `WRITE` / プリンタレコード | `PrintSpoolService#printVoteTicket/printCommentTicket + 対応Repository（H2）` | **解釈差(前提)** | 仕様書9節の前提に基づくJava置換 |
| `DB-PRTLSTVOTE-05` | `CMTTST` / `WRITE` / `PRINTER` のテスト帳票 | `PrintSpoolService#printVoteTicket/printCommentTicket + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `ERR-PRTLSTVOTE-01` | CONST メッセージなし | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |

## 2.READGBCMT

| BR-ID／項目ID | 仕様要約（原文） | Java対応箇所（クラス#メソッド／API・UI） | 判定 | 備考 |
|---|---|---|---|---|
| `BR-READGBCMT-01` | `INCMTID=0` は `Must enter CommentID`、`*IN43`。 | `ReadGuestbookCommentService#read` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-READGBCMT-02` | `VISIBLE='N'` は名前を `Name Hidden`、本文を `This comment hidden by an admin - offensive content.` とする。 | `ReadGuestbookCommentService#read` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-READGBCMT-03` | 公開コメントは `EXHBID=USERPRF` または `USERPRF='MM2024'` の場合のみ表示する。 | `ReadGuestbookCommentService#read` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-READGBCMT-04` | 権限のないコメントは `This comment is not part of this guestbook.` とし、名前とタイトルを空にする。 | `ReadGuestbookCommentService#read` | **一致** | BR-IDの業務ルールをサービスメソッドで確認 |
| `BR-READGBCMT-05` | `WTFCMTNUM` は最終 `CMTID` を総コメント数として表示する。 | `ReadGuestbookCommentService#read` | **相違(修正済)** | 総コメント数を行数から最終CMTIDへ修正した |
| `PARM-READGBCMT-01` | 1 / `LAUNCH` / 文字 20A / 現在ユーザプロファイル | `ReadGuestbookCommentService#read（入口引数）` | **一致** | PARM行または入口パラメータ記載を確認 |
| `SCREEN-READGBCMT-01` | `INCMTID` / 数値 / 4Y0 / `READCMT` / Comment ID | `ReadGuestbookCommentService#read + UI /guestbook/read, /api/guestbook/{id}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-READGBCMT-02` | `WTFCMTNUM` / 数値 / 4 / `READCMT` / 現在の総コメント数 | `ReadGuestbookCommentService#read + UI /guestbook/read, /api/guestbook/{id}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-READGBCMT-03` | `OUTNAME` / 文字 / 11A / `READCMT` / 投稿者名または `Name Hidden` | `ReadGuestbookCommentService#read + UI /guestbook/read, /api/guestbook/{id}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-READGBCMT-04` | `OUTCMT` / 文字 / 200A / `READCMT` / コメントまたは状態文言 | `ReadGuestbookCommentService#read + UI /guestbook/read, /api/guestbook/{id}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-READGBCMT-05` | `OUTTITLE` / 文字 / 50A / `READCMT` / 展示タイトル | `ReadGuestbookCommentService#read + UI /guestbook/read, /api/guestbook/{id}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `SCREEN-READGBCMT-06` | `ERRLINE` / 文字 / 20A / `READCMT` / エラー/初期表示 | `ReadGuestbookCommentService#read + UI /guestbook/read, /api/guestbook/{id}` | **一致** | 画面フォーマットの入出力項目を確認 |
| `DB-READGBCMT-01` | `GUESTBKDB` / `READ` 全件 / `GETTLCMT` | `ReadGuestbookCommentService#read + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `DB-READGBCMT-02` | `GUESTBKDB` / `SETLL`/`READ` / `CMTID=INCMTID` | `ReadGuestbookCommentService#read + 対応Repository（H2）` | **解釈差(前提)** | 仕様書9節の前提に基づくJava置換 |
| `DB-READGBCMT-03` | `EXHBDB` / `SETLL`/`READ` / `EXHUSRPRF=EXHBID` | `ReadGuestbookCommentService#read + 対応Repository（H2）` | **一致** | DB命令・対象キーをRepository対応へ確認 |
| `ERR-READGBCMT-01` | `ERRCMTID` / `Must enter CommentID` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-READGBCMT-02` | `ERRHNAME` / `Name Hidden` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-READGBCMT-03` | `ERRPNAME` / `Private Comment` / 未使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-READGBCMT-04` | `ERREXIST` / `Exhibit not found` / 定義のみ | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-READGBCMT-05` | `ERRHCMT` / `This comment hidden by an admin - offensive content.` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-READGBCMT-06` | `ERRPRIV` / `This comment is not part of this guestbook.             ` / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |
| `ERR-READGBCMT-07` | `BLNK` / 空白 / 使用 | `RpgMessages#対応文言定数` | **一致** | CONST／固定メッセージの文言と使用有無を確認 |

## 3.修正一覧

- `AddGuestbookCommentService#submit`: `ADDGBCMT` の仕様では `EXHBDB` は参照定義のみで、出展者存在チェックを行わないため、Java側の追加チェックを削除した。
- `ReadGuestbookCommentService#getTotalComments`: `READGBCMT` の `GETTLCMT` は件数ではなくEOFまで読んだ最終 `CMTID` を表示するため、最大IDを返すよう修正した。
- `AdminVoteReportService#readVote`: `ADMVOTERPT` の `NUMBR` は `VOTINGDB` 全件の総票数であるため、対象出展者だけでなく全票を集計するよう修正した。
- `ExhibitMenuService#select`: `EXHBMENU` の `CHKALWVOTE` を追加し、投票期間無効時は `VOTESTUB` へ遷移しないよう修正した。
- `AdminSettingService#save`: `ADMSETTING` の画面表記 `1/0` をDB値 `Y/N`へ正規化するよう修正した。
- `RpgMessages#USER_CREATED`: `ERRNF` の仕様文言 `USRPRF not found, creating new` と完全一致するよう修正した。
- `StaticScreenService` とAPI/UIコントローラ: `PARAMETER` の `TESTPARM` に対応する `/parameter` と `/api/static/parameters` を追加した。
- ADDVOTE等のサービスJavadoc: 誤っていたBR-IDの対応範囲を仕様書の各BR-IDへ訂正した。

## 4.未実装一覧

- `BR-OLDADDVOTE-01`～`BR-OLDADDVOTE-03`、旧版のVOTE1項目・VOTINGDB／EXHBDB／AWARDDBアクセス・旧版CONST群: `OLDADDVOTE.md` が「旧版・非移行」と明記しているため、Javaの業務機能としては実装しない。
- `ERRDBG1`／`ERRDBG2`（ADDVOTE）および `DBGNAME`（ADMVOTERPT）: 仕様書で定義のみ・未使用のデバッグ定数であり、実行時挙動には対応しない。

## 5.解釈差(前提)一覧

- `ADDVOTE`／`EXHBMENU`／`READGBCMT` 等の `SETLL+READ`／`READE`: 仕様書各9節の前提に従い、Javaでは主キー完全一致検索として実装した。参照: `ADDVOTE.md` 9節、`READGBCMT.md` 9節、`ADMHIDECMT.md` 9節。
- `EXHBMENU` の `GETPSWRD`: RPGの先頭レコード読み取りではなく、仕様書9節の前提に従い `SETTINGS` の `PASSWORD` を使用した。
- `LRN400` の `OVRDBF`: 仕様書9節の前提に従い、`LRN400STR.OWNER` と複合主キーで出展者別データを分離した。
- `LRN400` の `CALL`: 外部RPGプログラムを起動せず、`calledProgram`への記録とログ出力に置換した。
- `PRTLSTVOTE`／`PRTLSTCMT`／`PRINTER`: 仕様書9節の前提に従い、物理プリンタ制御をログとメモリ上の`PrintSpoolService`へ置換した。
- `READGBCMT` の `ERRLINE=USERPRF`: 仕様書9節の前提に従い、結果レコードの`errLine`へユーザプロファイルを返す。

## 6.決定

- `SECOFRS` 認可: RPG側の `CHKOFRS`／`CHKUSRPRF` はTODOのまま認可チェックを実行していないため、Javaでも認可処理は実装しない。`SECOFRS` は管理者の登録・一覧のみ提供する。
- 桁数制限: REST APIにもDDSの桁数を適用する。各サービス入口で共通ヘルパー `DdsField.truncate(String, int)` を使い、RPGの `MOVEL` と同じく超過分を拒否せず切り捨てる。UIの `maxlength` と同じ制約にする。
- `ADMLRN400` の `LAUNCH`／`EXHIBIT`: RPGソースでコメントアウトされ未使用のため、Javaでも使用しない。`owner` は画面入力値を使用し、未指定時は `LRN400STR` とする。

## 7. E2E検証で検出した相違(修正済)

フェーズ6の画面操作を想定したE2E検証で、以下の表示上の相違を検出し、Java側を修正した。仕様書は変更していない。

| 対象 | 検出内容 | 修正内容 | 検証 |
|---|---|---|---|
| `LRN400`／`LRN400AUT` | Thymeleafの`${session}`がHttpSession予約変数と衝突し、ページ番号と本文が空表示になっていた。 | `Vcf400UiController`のモデル属性を`learn`へ変更し、`learn400.html`、`learn400-auto.html`、hidden入力、JavaScript参照を追随させた。 | `UiPagesTest`で初期ページ本文、ページ番号、F5 POST×3後の`Goodbye.`、自動画面本文を確認 |
| `ADDVOTE` UI | 成功メッセージ`You have voted successfully`が`error`クラスで赤表示されていた。 | `vote.html`で`result.success`時のクラスを`success`へ変更した。 | `UiPagesTest`で`class="success"`と成功文言を確認 |
| `NTRSTIT` UI | 画面ヘッダが`Instructions`になっていた。 | `StaticScreenService#help`のtitleを`NTRSTIT`へ変更した。 | `UiPagesTest`でヘッダ文字列`NTRSTIT`を確認 |
