---
title: "VCF/400 仕様書 (レガシー RPG IV / CL / DDS 解析)"
subtitle: "madmerger/VCF400 — 機能仕様・DB スキーマ・業務ルール・元画面構造の台帳"
date: "2026-09-07"
---

# VCF/400 仕様書

| 項目 | 内容 |
|---|---|
| 対象リポジトリ | `madmerger/VCF400` (HEAD `83d566e Final release`, セーブファイル VCFV1R3 = 2024-04-05) |
| 対象資産 | QRPGLESRC (RPG IV), QDDSSRC (PF), QSDASRC (DSPF), QMNUSRC (メニュー), QCLSRC (CL), QCMDSRC (CMD), QRLUSRC (PRTF) |
| 解析根拠 | ソース静的解析 + 2026-09-07 clean rebuild の PUB400 (ASHIBATA2 ライブラリ) 上での実行結果 (`deliverables/logs/phase1_report.md`, `deliverables/logs/phase1_06_runtime_5250.log`, `phase1_07_runtime_alwvote.log`) |
| ID 体系 | **F-nn** 機能, **V-nn** 入力検証ルール, **B-nn** 業務ルール, **D-nn** DB スキーマ, **M-nn** メッセージ, **L-nn** 元画面構造の台帳項目。移行実装のトレーサビリティ表 (`deliverables/spec/VCF400_traceability.md`) はこれらの ID を参照する。 |

---

## 1. 機能概要

VCF/400 は Vintage Computer Festival (VCF) の展示会場に置かれた AS/400 (IBM i) 端末向けの来場者参加アプリケーションである。来場者 (attendee) は展示 (exhibit) を **アワードにノミネート (投票)** し、**ゲストブックにコメント**を残し、コメントを**閲覧**し、**LEARN/400** で AS/400 の操作を学ぶ。展示者 (exhibitor) 用には展示ごとの **キオスクメニュー** があり、管理者 (SECOFR) 用には展示の作成・編集、コメントの非表示化、投票停止、投票集計の **管理機能** がある。

### 1.1 機能一覧

| ID | 機能 | プログラム | 画面 (DSPF/RCDFMT) | 起動経路 |
|---|---|---|---|---|
| F-01 | メインメニュー (AS/400 DEMO MENU) | メニュー `VCFMAIN` (+MSGF `VCFMAINQQ`) | VCFMAIN | `GO VCFMAIN` |
| F-02 | ナビゲーション説明 (How to Navigate) | `NTRSTIT` | INTERSCR/INTERTEST | VOTESTUB / ADDGBSTUB / READGBSTUB の先頭で必ず表示 |
| F-03 | 投票 (Nominate Exhibit for Award) | `ADDVOTE` | VOTESCR/VOTE1, VOTEEND, ENDOFCON, NOVOTEALWD, DAVE | VCFMAIN 11 → `VOTESTUB` / キオスク 1 |
| F-04 | ゲストブック記入 (GUESTBOOK/400 - ADD COMMENT) | `ADDGBCMT` | GUESTBKSCR/ADDCMT, ENDCMT | VCFMAIN 12 → `ADDGBSTUB` / キオスク 3 |
| F-05 | ゲストブック閲覧 (GUESTBOOK/400 - Read a Comment) | `READGBCMT` | GUESTBKSCR/READCMT | VCFMAIN 13 → `READGBSTUB` / キオスク 4 |
| F-06 | LEARN/400 (ページ送り学習コンテンツ) | `LRN400` | LRN400SCR/MAIN | VCFMAIN 1 (`CALL LRN400`) / キオスク 2 → `LRN400STUB` |
| F-07 | 展示キオスクメニュー (WELCOME TO...) | `EXHBMENU` | EXHBMENUSC/MENU, ADMPSWRD | `STREXHB EXHBNAME(x)` / `VCFSTUB` |
| F-08 | 投票結果印刷 (投票のたびに呼ばれる) | `PRTLSTVOTE` | VOTEPRTF (PRTF) | ADDVOTE から CALL |
| F-09 | コメント一覧印刷 | `PRTLSTCMT` | CMTPRTF (PRTF) | ADDGBCMT から CALL |
| F-10 | 管理: 展示の作成・編集・削除 | `ADMCRTEXHB` | EXHBMENUSC/ADMCREATE, ADMDELETE | `STREXHBEDT PASSWORD(x) EXHBNAME(y)` |
| F-11 | 管理: コメントの表示/非表示 | `ADMHIDECMT` | GUESTBKSCR/HIDECMT | `STRCMTEDT CMTID(nnnn)` |
| F-12 | 管理: 設定 (管理パスワード・投票許可) | `ADMSETTING` | SETUPSCR/SETUPMAIN | ADMMAIN |
| F-13 | 管理: 投票集計 | `ADMVOTERPT` | ADMVOTERES/VOTERES | `STRVOTERPT EXHBNAME(x)` |
| F-14 | 管理: LEARN/400 ページ編集 | `ADMLRN400` | LRN400SCR/MAINADMIN | ADMMAIN |
| F-15 | 管理: SECOFR 登録 | `ADMADDSOFR` / `ADMOFRLIST` | ADMSCR/ADDADM | ADMMAIN |
| F-16 | 管理メニュー | メニュー `ADMMAIN` | ADMMAIN | VCFMAIN 90 |
| — | 補助/テスト | `CREDITS`, `PARAMETER`, `LRN400AUT`, `PRINTER`(コンパイル不可), `BEEMOVIE`(コンパイル不可), `OLD*` | — | 対象外 |

**移行対象 (フェーズ3)** は来場者・展示者向けの F-01〜F-07 と、それらが依存する業務ルール・DB。管理機能 F-10〜F-16 は本仕様書で挙動を記録するが、移行スコープ外 (投票停止フラグ ALWVOTE と管理パスワード ADMPSWRD は設定データとして再現する)。

### 1.2 起動パラメータと「ユーザープロファイル = 展示 ID」の規約

- 展示は **IBM i ユーザープロファイル名 (9 文字) = 展示 ID (`EXHBDB.EXHUSRPRF`)** で識別される。来場者が展示者の端末にサインオンしている前提で、CL スタブ (`VOTESTUB`, `ADDGBSTUB`, `READGBSTUB`, `VCFSTUB`, `LRN400STUB`) が `RTVJOBA CURUSER(&PROFILE)` で現在ユーザーを取得し、RPG に `LAUNCH` パラメータとして渡す。
- RPG 側 (`CHKPARM` サブルーチン) は `LAUNCH` を展示 ID として画面の展示 ID 項目に事前入力し、**保護 (DSPATR(PR), 標識 70)** する。
- 特別値 **`MM2024`** (Midrange Madness 2024 の共用端末) を `LAUNCH` に渡すと、展示 ID は空欄・入力可となり来場者が任意の展示 ID を入力できる (`ADDVOTE`, `ADDGBCMT`)。`READGBCMT` では `MM2024` の場合に全展示のコメントを閲覧できる。
- `LRN400STUB` は `OVRDBF FILE(LRN400STR) TOFILE(VCF/&PROFILE)` で展示ごとの LEARN/400 コンテンツファイル (展示 ID と同名の PF) に切り替える。VCFMAIN 1 は `CALL LRN400` 直接呼び出しのため共通の `LRN400STR` を使う。

---

## 2. 画面フロー (メニュー階層)

```
GO VCFMAIN ──── AS/400 DEMO MENU (F-01)
 │  1  Learn AS/400 Navigation ─────────────── LRN400 (F-06)  [F3 Exit / F5 Fwd / F8 Back / 最終ページ(EXTRA='END')で自動終了]
 │  2-10 History / About / OfficeVision / Calendar / Clock / Snake / Yahtzee / PacMan  (VCF リポジトリ外・マッピングなし)
 │ 11  Nominate Exhibit for Award ── VOTESTUB ── NTRSTIT(How to Navigate, ENTER) ── ADDVOTE (F-03)
 │                                                  ├ ALWVOTE='N' → ENDOFCON (SORRY, ENTER で終了)
 │                                                  ├ VOTE1: バッジ → 展示ID(保護) → アワード, F5 送信 / F12 取消
 │                                                  │    検証 NG → 同画面にエラー行 (6行目) を表示して再入力
 │                                                  └ 検証 OK → VOTINGDB 書込 → PRTLSTVOTE → VOTEEND (THANK YOU, ENTER でメニューへ)
 │ 12  Sign Exhibit Guestbook ──── ADDGBSTUB ── NTRSTIT ── ADDGBCMT (F-04)
 │                                                  ├ ADDCMT: 名前 → 展示ID(保護) → コメント, F5 送信 / F12 取消
 │                                                  └ OK → GUESTBKDB 書込 → PRTLSTCMT → ENDCMT (THANKS FOR COMMENTING, ENTER)
 │ 13  Read a Guestbook Comment ── READGBSTUB ── NTRSTIT ── READGBCMT (F-05)
 │                                                  └ READCMT: コメント ID, F5 表示 (同画面に表示・繰返し) / F12 取消
 │ 80  Sign Off ─── SIGNOFF
 │ 90  Start Admin Menu ─── ADMMAIN (F-16)  1 Create New Exhibit / 2 Edit Existing Exhibit / 3 Moderate Comment / 4 End All Voting / 5 View Voting Statistics

STREXHB EXHBNAME(x) / CALL VCFSTUB ──── EXHBMENU  "WELCOME TO..." (F-07)  ※サブ機能終了後はキオスクメニューに戻るループ
     1 Nominate This Exhibit for Award  (ELIGIBLE=1 のときのみ表示・有効) → VOTESTUB → NTRSTIT → ADDVOTE
     2 Learn More About This Exhibit    (ENLRN400=1 のときのみ表示・有効) → LRN400STUB → LRN400
     3 Sign Exhibit Guestbook           → ADDGBSTUB → NTRSTIT → ADDGBCMT
     4 Read Exhibit Guestbook           → READGBSTUB → NTRSTIT → READGBCMT
     7 (非表示) キオスク終了            → ADMPSWRD: 管理パスワード一致で終了、不一致でメニューに戻る
```

- F12 (Cancel) はすべての入力画面で「プログラム終了 → 呼び出し元 (VCFMAIN またはキオスクメニュー) に戻る」。
- ENTER は VOTE1 / ADDCMT / READCMT では F5 と同じ「送信」(標識 05 は判定に使われず、EXFMT 復帰後に必ず検証が走る)。INTERTEST / VOTEEND / ENDCMT / ENDOFCON / NOVOTEALWD は ENTER で次へ進む。
- キオスクメニューでは F3/F12 等のファンクションキーは定義されておらず `Function key not allowed.` となる。

---

## 3. DB スキーマ (DDS 物理ファイル)

すべて `UNIQUE` キー付き PF。数値は `B` (2 進) 型、文字は `A` 型。移行先 (DB2 for i 相当スキーマ) では `SMALLINT`/`INTEGER` と `CHAR(n)` で再現する。

| ID | ファイル | レコード | 項目 | 型 | 説明 |
|---|---|---|---|---|---|
| D-01 | **EXHBDB** (展示) | EXHBREC | EXHBDBID | 3B 0 | 展示連番 |
| | | | **EXHUSRPRF** | 9A **KEY** | 展示 ID = 展示者ユーザープロファイル |
| | | | EXHBITOR | 20A | 展示者名 |
| | | | EXHBCITY | 20A | 都市 |
| | | | EXHBSTATE | 2A | 州 |
| | | | EXHBTITLE | 50A | 展示タイトル |
| | | | EXHBDESC | 1000A | 説明 |
| | | | ELIGIBLE | 1B 0 | 1=アワード対象, 0=対象外 |
| | | | ENLRN400 | 1B 0 | 1=LEARN/400 有効 |
| D-02 | **VOTINGDB** (投票) | VOTINGREC | **BADGENBR** | 4B 0 **KEY** | バッジ番号 (1 バッジ 1 票) |
| | | | AWARDNBR | 3B 0 | アワード ID |
| | | | EXHBNBR | 9A | 展示 ID |
| D-03 | **GUESTBKDB** (ゲストブック) | GUESTBKRCD | **CMTID** | 4B 0 **KEY** | コメント ID (最終+1 で採番) |
| | | | VISIBLE | 1A | 'Y' 表示 / 'N' 非表示 (管理者が変更) |
| | | | EXHBID | 9A | 展示 ID |
| | | | GUESTNAME | 20A | 記入者名 (画面入力は 16 桁) |
| | | | GUESTCMT | 200A | コメント |
| D-04 | **AWARDDB** (アワード) | AWARDRCD | **AWARDID** | 3B 0 **KEY** | アワード ID |
| | | | AWARDTITLE | 100A | 名称 |
| | | | AWARDDESC | 1000A | 説明 |
| D-05 | **SETTINGS** (設定) | SETTINGSR | **SETTING** | 9A **KEY** | 設定名: `ADMPSWRD` (キオスク終了パスワード), `ALWVOTE` (投票許可 Y/N), (`PASSWORD` は ADMSETTING が参照) |
| | | | VALUE | 9A | 値 |
| D-06 | **SECOFRS** (管理者) | USRPROFR | **USERPROF** | 9A **KEY** | 管理者ユーザープロファイル |
| D-07 | **LRN400STR** (LEARN/400 ページ) | LRN400RCD | **PAGENBR** | 4B 0 **KEY** | ページ番号 |
| | | | CONTENT | 1500A | 本文 (`CALL`/`JUMP` は特殊コマンド) |
| | | | EXTRA | 9A | `END`=最終ページ / CALL 先プログラム / JUMP 先ページ |
| — | BMOVDB | — | — | — | BEEMOVIE 用 (対象外) |

**参考データ (PUB400 ASHIBATA2、移行実装の初期データとして使用)**

```
AWARDDB : (1,'Best in Show Award','This award is given to the exhibit who you believe to be the best in show for 2024.')
           (2,'The Ed Fair Award','This award is given to the exhibit that is deemed the most informative of the show.')
EXHBDB  : (1,'ASHIBATA','Akira Shibata','Tokyo','JP','IBM i on PUB400 Demo','VCF/400 demo exhibit running on pub400.com',1,1)
          (2,'DEMO400','Demo Exhibitor','Mountain View','CA','AS/400 Model 150','A vintage AS/400 9401-150 exhibit',1,0)
          (3,'NOVOTE','Ineligible Exhibitor','Atlanta','GA','Ineligible test exhibit (ELIGIBLE=0)','Test data for eligibility check',0,0)
SETTINGS: ('ADMPSWRD','VCF2024') ('ALWVOTE','Y')
LRN400STR: (1,'Welcome to LEARN/400! This is page 1. Press F5 for next page, F8 for previous, F3 to exit.','')
           (2,'The AS/400 was introduced by IBM in June 1988 ... page 2','')  (3,'This is the last page. Thank you for visiting VCF/400.','END')
VOTINGDB: (1,1,'ASHIBATA') (28,2,'ASHIBATA')
GUESTBKDB: (1,'Y','ASHIBATA','Great exhibit','VCF/400 running on PUB400.') (2,'Y','ASHIBATA','Devin','VCF/400 is running on PUB400.')
```

---

## 4. 業務ルール

| ID | ルール | 根拠 |
|---|---|---|
| B-01 | **1 バッジ 1 票**: `VOTINGDB` は BADGENBR がユニークキー。ADDVOTE は `CHAIN VOTINGREC` でヒットしたら `You have already voted.` を表示し書込しない。アワードや展示が異なっても再投票不可。 | ADDVOTE ADDTODB |
| B-02 | **アワード資格 (ELIGIBLE)**: 展示の `ELIGIBLE=0` なら `Exhibit ineligible for award`。キオスクでは ELIGIBLE=0 の展示のメニュー 1 が非表示 (標識 41 DSPATR(ND)) かつ選択しても何も起きない。 | ADDVOTE / EXHBMENU CHKPARM, DOVOTE |
| B-03 | **投票期間フラグ (ALWVOTE)**: `SETTINGS.ALWVOTE='N'` のとき ADDVOTE は入力画面を出さずに ENDOFCON (`SORRY... The voting period has ended and you can no longer vote. However, you may sign this exhibit guestbook if you would like.`) を表示して終了。 | ADDVOTE CHKALWVOTE (実行確認済: phase1_07) |
| B-04 | **投票の書込条件 CHECKOK=4**: 未投票 / 展示が資格あり / 展示が存在 / アワードが存在 の 4 検査すべて合格 (`CHECKOK = 4`) のときだけ `WRITE VOTINGREC` し、`PRTLSTVOTE` を呼び、VOTEEND を表示する。1 つでも不合格なら同じ入力画面に戻りエラー行を表示 (入力値は保持)。 | ADDVOTE ADDTODB |
| B-05 | **エラー行は「最後に失敗した検査」のメッセージ**: 必須検査は バッジ → アワード → 展示 ID、DB 検査は 重複 → 資格 → 展示存在 → アワード存在 の順で評価され、ERRLINE は後の検査の失敗で上書きされる。例: 重複バッジ + 存在しないアワード → `Award does not exist.`。 | ADDVOTE (実行確認: 全項目空欄で `Must enter Award ID`) |
| B-06 | **コメント ID 採番**: GUESTBKDB を末尾まで読み最終 CMTID + 1 を採番 (`READ` ループ)。空のときは 1。新規コメントは常に `VISIBLE='Y'`。 | ADDGBCMT ADDTODB |
| B-07 | **コメントの可視性**: `VISIBLE='N'` のコメントは閲覧時に名前 `Name Hidden`、本文 `This comment hidden by an admin - offensive content.` に置き換えて表示。 | READGBCMT READDB |
| B-08 | **コメントの帰属**: 閲覧は「その端末の展示 (LAUNCH) 宛のコメント」のみ本文表示。他展示宛は名前・タイトル空欄、本文 `This comment is not part of this guestbook.`。LAUNCH=`MM2024` は全展示閲覧可。 | READGBCMT READDB |
| B-09 | **存在しないコメント ID の挙動 (レガシーの癖)**: `SETLL` + `READ` で読むため、指定 ID 以上で最初のレコードが表示される。末尾を超える ID (例 9999) はエラーにならず、直前に読んだ最終レコード (最新コメント) が表示される。 | READGBCMT (実行確認: ID 9999 → 最新コメント表示) |
| B-10 | **LEARN/400 ページ遷移**: 起点はファイル先頭ページ。F5 で次ページ (存在しなければ現ページのまま)、F8 で前ページ (ページ 0 未満にはならない)。到達ページの `EXTRA='END'` なら即終了 (メニューへ)。`CONTENT='CALL'` は `EXTRA` のプログラムを CALL して次ページへ、`CONTENT='JUMP'` は `EXTRA` のページへジャンプ。 | LRN400 PAGEFWD/PAGEBACK/PARSESCRIPT/PRFRMACTN |
| B-11 | **キオスク終了の保護**: キオスクメニューの隠しオプション 7 で管理パスワード (SETTINGS 先頭レコード = `ADMPSWRD` の VALUE) を要求。一致で EXHBMENU 終了、不一致でメニュー再表示。他のオプション終了後もメニューに戻る (RPG サイクルにより *INLR がオンになるまでループ)。 | EXHBMENU ADMKIOSK/GETPSWRD (実行確認) |
| B-12 | **キオスクの LEARN/400 可否 (ENLRN400)**: `ENLRN400=0` の展示はメニュー 2 が非表示・無効。 | EXHBMENU CHKPARM, DOLRN400 |
| B-13 | **展示 ID の固定**: VCFMAIN 経由 (LAUNCH = ユーザープロファイル) では展示 ID は事前入力・保護され変更不可。MM2024 のみ入力可。 | ADDVOTE / ADDGBCMT CHKPARM |
| B-14 | **イースターエッグ**: ADDVOTE で展示 ID `DAVE` を入力すると DAVE 画面を表示して終了 (投票なし)。移行では再現不要 (台帳外)。 | ADDVOTE DODAVE |
| B-15 | **数値項目の入力**: バッジ (4 桁), アワード (3 桁), コメント ID (4 桁) は数字のみ (5250 では英字入力でキーボードロック)。右詰めゼロ埋め `CHECK(RZ)`、未入力は 0 として扱われ必須エラー。 | DDS CHECK(RZ) |

---

## 5. 機能仕様詳細

### 5.1 F-03 ADDVOTE — 投票

**入力**: `LAUNCH` (9A: ユーザープロファイル or `MM2024`)。ファイル: VOTINGDB (更新), EXHBDB, AWARDDB, SETTINGS (参照), VOTESCR。

処理フロー:

1. `CHKPARM`: LAUNCH=`MM2024` → INEXHB 空欄・入力可; それ以外 → INEXHB=LAUNCH, 標識 70 (保護) オン。
2. `CHKALWVOTE`: SETTINGS を全件読み `ALWVOTE` の値を取得。`'N'` なら `EXFMT ENDOFCON` → 終了 (B-03)。
3. 入力ループ (`DOW ALWEXIT = 0`): 標識 40/41/42 をオフ → `EXFMT VOTE1` → 必須検査 (V-01〜V-03、各失敗で ERRLINE 設定 + 該当項目を反転表示) → 3 項目とも入力済み (`VALIDATE=3`) でループ脱出。F12 (*IN12) なら終了。
4. `ADDTODB`: CHECKOK=0 から、(a) `CHAIN VOTINGREC` by INPUTBADGE — ヒット(=既投票)で `You have already voted.` else +1; (b) `SETLL/READ EXHBDB` by INEXHB — `ELIGIBLE=0` で `Exhibit ineligible for award` else +1; (c) `CHAIN EXHBREC` — 見つからなければ `Exhibit does not exist` else +1; (d) `CHAIN AWARDRCD` by INPUTAWARD — 見つからなければ `Award does not exist.` else +1。
5. `CHECKOK=4` なら BADGENBR/AWARDNBR/EXHBNBR をセットして `WRITE VOTINGREC`、`CALL PRTLSTVOTE`、`ENDVOTE` (`EXFMT VOTEEND` → `CALL PRTLSTVOTE` → *INLR)。不合格なら *INLR オフのまま RPG サイクルが先頭に戻り、手順 3 の入力画面がエラー行付きで再表示される (入力値保持)。

#### 5.1.1 CHECKOK=4 までの検証手順 (実機確認)

`deliverables/logs/phase1_06_runtime_5250.log` の 5250 実機相当操作で、ADDTODB の
チェックを次の順序で確認した。各ケースは VCFMAIN option 11 または
`CALL ADDVOTE PARM('MM2024')` から入力し、ERRLINE の実際の表示を記録した。

1. **入力必須検査** — badge `7707`、展示 ID `ASHIBATA`、award ID を空欄のまま F5。
   期待/実測 ERRLINE: `Must enter Award ID`。
2. **(a) 重複 badge** — badge `7707`、展示 `ASHIBATA`、award `002` を F5。
   期待/実測 ERRLINE: `You have already voted.`。
3. **(b) ELIGIBLE 検査** — badge `7708`、展示 `NOVOTE`、award `001` を F5。
   期待/実測 ERRLINE: `Exhibit ineligible for award`。
4. **(c) 展示存在検査** — badge `7708`、展示 `NOSUCH`、award `001` を F5。
   期待/実測 ERRLINE: `Exhibit does not exist`。
5. **(d) award 存在検査** — badge `7708`、展示 `ASHIBATA`、award `009` を F5。
   期待/実測 ERRLINE: `Award does not exist.`。
6. **CHECKOK=4 / 書込** — badge `7707`、展示 `ASHIBATA`、award `001`、および
   badge `7708`、展示 `DEMO400`、award `002` を F5。ERRLINE は表示されず、
   `Your vote has been RECORDED` となり、VOTINGDB にそれぞれ書き込まれた。

入力検証ルール:

| ID | 項目 | ルール | メッセージ (ERRLINE, 45 桁, 6 行目) | 反転表示 |
|---|---|---|---|---|
| V-01 | INPUTBADGE (4 桁数値) | 0 / 未入力は不可 | M-02 `Must enter badge number` | 標識 40 |
| V-02 | INPUTAWARD (3 桁数値) | 0 / 未入力は不可 | M-04 `Must enter Award ID` | 標識 42 |
| V-03 | INEXHB (9 文字) | 空欄不可 (MM2024 起動時のみ入力可) | M-03 `Must enter Exhibit ID` | 標識 41 |
| V-04 | バッジ | VOTINGDB に存在しない (B-01) | M-05 `You have already voted.` | — |
| V-05 | 展示 | ELIGIBLE=1 (B-02) | M-07 `Exhibit ineligible for award` | — |
| V-06 | 展示 | EXHBDB に存在 | M-08 `Exhibit does not exist` | — |
| V-07 | アワード | AWARDDB に存在 | M-09 `Award does not exist.` | — |
| V-08 | 全体 | V-04〜V-07 すべて合格 (CHECKOK=4) で書込 (B-04)。評価順・上書き規則は B-05 | 成功時 VOTEEND `Your vote has been RECORDED! Thank you for participating in the awards show...` | — |
| V-09 | 前提 | ALWVOTE='N' なら入力画面を出さず ENDOFCON (B-03) | `SORRY... The voting period has ended and you can no longer vote.` | — |

### 5.2 F-04 ADDGBCMT — ゲストブック記入

1. 入力ループ: `CHKPARM` (LAUNCH≠MM2024 → INID=LAUNCH, 保護) → `EXFMT ADDCMT` → 必須検査 (順: 展示 ID → 名前 → コメント; ERRLINE 21 桁, 10 行目) → 3 項目入力済みで脱出。F12 で終了。
2. `ADDTODB`: 末尾まで READ して CMTID = 最終+1 (B-06)、GUESTNAME=INNAME, EXHBID=INID, GUESTCMT=INCMT, VISIBLE='Y' で `WRITE GUESTBKRCD`、`CALL PRTLSTCMT`、`EXFMT ENDCMT` (THANKS FOR COMMENTING) → 終了。
3. ENDCMT には「Your Comment ID Is:」の文言があるが出力項目は定義されておらず、ID は表示されない (レガシーの癖。移行版では ID を表示してよいが、台帳上は「文言のみ」)。

| ID | 項目 | ルール | メッセージ |
|---|---|---|---|
| V-10 | INID (9 文字) | 空欄不可 | M-10 `Must enter Exhibit ID` |
| V-11 | INNAME (16 文字, 小文字可) | 空欄不可 | M-11 `Must enter your name` |
| V-12 | INCMT (200 文字, 小文字可) | 空欄不可 | M-12 `Must enter a comment` |
| V-13 | 全体 | 3 項目入力済みで書込。展示 ID の存在チェックは **行わない** (`Exhibit not found` 定数は未使用) | 成功時 ENDCMT |

### 5.3 F-05 READGBCMT — ゲストブック閲覧

1. `CHKPARM`: USERPRF=LAUNCH。
2. 入力ループ: `GETTLCMT` (全件 READ でコメント総数 WTFCMTNUM = 最終 CMTID) → `EXFMT READCMT` → INCMTID=0 なら M-13 `Must enter CommentID`(ERRLINE 20 桁, 4 行目) で再表示。F12 で終了。
3. `READDB`: `SETLL/READ GUESTBKRCD` by INCMTID (B-09) → VISIBLE='N' なら B-07 の置換; EXHBID=USERPRF または USERPRF=MM2024 なら OUTNAME/OUTCMT を表示し EXHBDB から展示タイトル (OUTTITLE) を取得; それ以外は B-08 の置換。
4. *INLR はオフのまま → 同じ READCMT 画面に結果が表示され、続けて別 ID を入力できる。

| ID | 項目 | ルール | メッセージ |
|---|---|---|---|
| V-14 | INCMTID (4 桁数値) | 0 / 未入力は不可 | M-13 `Must enter CommentID` |
| V-15 | 表示 | 「Currently hosting nnnn comments and counting.」に総数 | — |
| V-16 | 表示 | 非表示コメント → `Name Hidden` / `This comment hidden by an admin - offensive content.` (B-07) | — |
| V-17 | 表示 | 他展示のコメント → `This comment is not part of this guestbook.` (B-08) | — |
| V-18 | 表示 | 存在しない ID → 次のレコード / 末尾超は最終コメント (B-09) | — |

### 5.4 F-06 LRN400 — LEARN/400

- 先頭レコードを読みページ 1 を表示。`DOW *IN03 = *OFF`: F5 (`*IN05`) → `PAGEFWD`, F8 (`*IN08`) → `PAGEBACK`, `EXFMT MAIN`。F3 で終了。
- `PAGEFWD`: CURPAGENBR+1 を `CHAIN`; 見つかれば表示し `PARSESCRIPT` (`EXTRA='END'`→終了, `CONTENT='CALL'`→CALL EXTRA 後に次ページ, `CONTENT='JUMP'`→EXTRA ページへ) → `PRFRMACTN`。見つからなければ ALWFWD=0 (末尾で停止)。
- `PAGEBACK`: CURPAGENBR-1 (0 未満にしない) を CHAIN して表示。
- 画面: 「LEARN/400」、右上「Page nnnn」、本文 1500 桁、下部「Cmd3/F3 = Exit  Cmd5/F5 = Forwards  Cmd8 / F8 = Backwards」。

| ID | ルール |
|---|---|
| V-19 | F5: 次ページが存在すれば表示、EXTRA='END' のページに到達したら即終了 (B-10)。存在しなければ現ページ維持 |
| V-20 | F8: 前ページを表示。ページ 1 では 0 ページ (存在せず) を CHAIN → 空表示にはならず直前値保持 (実装上は「ページ 1 のまま」と等価に扱う) |
| V-21 | F3: 終了して呼び出し元へ |

### 5.5 F-07 EXHBMENU — 展示キオスク

- `CHKPARM`: LAUNCH で EXHBDB を読み OUTTITLE(75)/OUTNAME(25)/OUTCITY(15)/OUTSTATE(2)/OUTDESC(1000) と ELIGIBLE→標識 41, ENLRN400→標識 40 を設定。`GETPSWRD`: SETTINGS 先頭レコードの VALUE を EXITPSWRD に。
- `EXFMT MENU` → INOPT 1: ELIGIBLE=1 なら `CALL VOTESTUB`; 2: ENLRN400=1 なら `CALL LRN400STUB`; 3: `CALL ADDGBSTUB`; 4: `CALL READGBSTUB`; 7: `ADMKIOSK` (ADMPSWRD 画面、INPWD=EXITPSWRD で *INLR/RETURN)。
- *INLR はオプション 7 成功時のみオン → それ以外は RPG サイクルで先頭に戻りメニューを再表示 (B-11)。
- INOPT は 1 桁数値 `CHECK(ME) CHECK(MF)` (必須入力・必須埋め)。定義外の番号 (5, 6, 8, 9, 0) は何もせずメニュー再表示。

### 5.6 F-01 VCFMAIN — メインメニュー

メニューコマンド (`VCFMAINQQ.mnucmd`): `0001 CALL VCF/LRN400`, `0011 CALL VCF/VOTESTUB`, `0012 CALL VCF/ADDGBSTUB`, `0013 CALL VCF/READGBSTUB`, `0080 SIGNOFF`。2〜10, 90 は本リポジトリにマッピング定義がない (実機では他ライブラリのオブジェクト)。コマンド行 (`===>`) には CL コマンドも入力可 (標準メニュー機能)。

### 5.7 F-02 NTRSTIT — How to Navigate

INTERTEST 形式を `EXFMT` して ENTER で終了するだけ。VOTESTUB / ADDGBSTUB / READGBSTUB は毎回 NTRSTIT を先に呼ぶ (VCFMAIN 11/12/13 とキオスク 1/3/4 の共通前置き画面)。

### 5.8 管理機能 (移行スコープ外・記録のみ)

| 機能 | 概要 |
|---|---|
| F-10 ADMCRTEXHB | `LAUNCH`(SECOFR 検証用) と `EXHIBIT` を受け、EXHBDB を CHAIN。存在すれば ADMCREATE 画面に現在値を表示し ENTER で UPDATE、F5 で ADMDELETE 確認→DELETE、F12 取消。存在しなければ `USRPRF not found, creating` で WRITE。項目: タイトル(60)/名前(20)/都市(15)/州(2)/説明(1000)/USRPRF(9)/Award Eligible(0/1)/LRN400(0/1)。メッセージ `Error - USRPRF exists`, `Not found - not deleted`, `USRPRF was deleted from DB`, `USRPRF cannot be blank`。 |
| F-11 ADMHIDECMT | `LAUNCH`=コメント ID。HIDECMT 画面に名前・本文・現在の VISIBLE を表示し、`Change Status? (Y/N)` に Y/N で VISIBLE を UPDATE、`Status updated`。それ以外は `Must type Y or N`。 |
| F-12 ADMSETTING | SETTINGS の `PASSWORD` と `ALWVOTE` を SETUPMAIN に表示し、ENTER で UPDATE (設定名は `PASSWORD` を参照しており、キオスクが参照する `ADMPSWRD` とは不一致 — 元ソースの不整合として記録)。 |
| F-13 ADMVOTERPT | `LAUNCH`=展示 ID (または `NONE` で入力画面)。SECOFRS に登録されたユーザーのみ。VOTINGDB を全件読み、展示宛の票数を AWARDNBR=1 (Best in Show) / 2 (Ed Fair) 別に集計して VOTERES に表示。`USRPRF cannot be blank`, `USRPRF was not found`。 |
| F-14 ADMLRN400 | LRN400STR のページを MAINADMIN 画面 (F3 Exit / F5 Fwd / F8 Back / F10 Save) で編集・追加 (`NEW` 表示)。 |
| F-15 ADMADDSOFR / ADMOFRLIST | ADDADM 画面でユーザープロファイルを入力し SECOFRS に WRITE (`Status updated`)。 |
| F-08/F-09 PRTLSTVOTE / PRTLSTCMT | VOTINGDB / GUESTBKDB を先頭から全件読み、印刷ファイル (VOTEPRTF / CMTPRTF) にヘッダ 3 行 + 明細を出力。投票・コメントのたびに呼ばれる監査印字。 |

---

## 6. メッセージ一覧

| ID | プログラム | 文言 | 表示位置 |
|---|---|---|---|
| M-01 | ADDVOTE | `You have voted successfully` (定数、未使用) | — |
| M-02 | ADDVOTE | `Must enter badge number` | VOTE1 6 行目 (ERRLINE 45A) |
| M-03 | ADDVOTE | `Must enter Exhibit ID` | 同上 |
| M-04 | ADDVOTE | `Must enter Award ID` | 同上 |
| M-05 | ADDVOTE | `You have already voted.` | 同上 |
| M-06 | ADDVOTE | `Use F5 key to submit` (定数、未使用) | — |
| M-07 | ADDVOTE | `Exhibit ineligible for award` | 同上 |
| M-08 | ADDVOTE | `Exhibit does not exist` | 同上 |
| M-09 | ADDVOTE | `Award does not exist.` | 同上 |
| M-10 | ADDGBCMT | `Must enter Exhibit ID` | ADDCMT 10 行目 (ERRLINE 21A) |
| M-11 | ADDGBCMT | `Must enter your name` | 同上 |
| M-12 | ADDGBCMT | `Must enter a comment` | 同上 |
| M-13 | READGBCMT | `Must enter CommentID` | READCMT 4 行目 (ERRLINE 20A) |
| M-14 | READGBCMT | `Name Hidden` / `This comment hidden by an admin - offensive content.` | OUTNAME / OUTCMT |
| M-15 | READGBCMT | `This comment is not part of this guestbook.` | OUTCMT |
| M-16 | VOTESCR/VOTEEND | `THANK YOU FOR VOTING!`(アスキーアート) `Your vote has been RECORDED! Thank you for participating in the awards show for Vintage Computer Festival Southeast 2024! Enjoy the rest of our exhibits and the rest of the Southern Fried Gaming Expo!` `Press ENTER to return to the main menu.` | 全画面 |
| M-17 | VOTESCR/ENDOFCON | `SORRY!`(アスキーアート) `The voting period has ended and you can no longer vote.` `However, you may sign this exhibit guestbook if you would like.` `Press ENTER to exit.` | 全画面 |
| M-18 | VOTESCR/NOVOTEALWD | `SORRY!` `This exhibit is not eligible to receive votes.` `However, you may sign this exhibit guestbook...` (定義のみ、ADDVOTE から未使用) | — |
| M-19 | GUESTBKSCR/ENDCMT | `THANKS FOR COMMENTING!`(アスキーアート) `Thank you for commenting on this exhibit! Your Comment ID Is:` `Press ENTER to exit.` | 全画面 |
| M-20 | EXHBMENUSC/ADMPSWRD | `Are you sure you want to exit the kiosk?` `Type the Administrator password, press ENTER to sign off` | 4–7 行目 |
| M-21 | INTERSCR | `Use ARROW KEYS or TAB KEY to move between fields.` `Use F5 or ENTER to perform your action.` `Use F12 to quit at any time.` `Keyboard not responding? If you see X II in the left corner, press RIGHT CTRL to continue.` `Got all that? Press ENTER to continue.` | 全画面 |

---

## 7. 元画面構造の台帳 (Legacy Screen Ledger)

本章は独立文書 `deliverables/spec/VCF400_screen_ledger.md` (同一内容の PDF あり) と同一内容であり、乖離時は独立台帳を正とする。フェーズ3 の Java / iPad 実装が **1 対 1 で対応させる正 (source of truth)**。「項目順」は DDS の行・桁位置に基づく画面上の並び、「入力順」はカーソル移動順 (TAB 順 = 行・桁の昇順)。モダン UI では色・配置・フォントは変更してよいが、**項目の順序・グルーピング・見出し文言・ファンクションキーの役割と番号・メニュー番号**は変更しない。

### 7.1 L-01 VCFMAIN — AS/400 DEMO MENU (メニュー)

| # | 項目 | 元画面 | 移行時の扱い |
|---|---|---|---|
| L-01-01 | 見出し | 1 行目 `AS/400 DEMO MENU`; 2 行目 `WELCOME TO...`; 3 行目 `V I N T A G E  C O M P U T E R  F E S T I V A L  2 0 2 4` | 文言保持 |
| L-01-02 | グループ 1 `Start Here` | `1. Learn AS/400 Navigation`, `2. IBM Midrange History`, `3. About This System`, `4. About Gertie the System/34` | 番号ラベル併記、1 のみ機能 (2–4 は無効表示) |
| L-01-03 | グループ 2 `Office Tasks` | `5. Start OfficeVision/400`, `6. Start a Calendar`, `7. Start the Clock` | 番号併記、無効表示 |
| L-01-04 | グループ 3 `Entertainment` | `8. Play Snake`, `9. Play Yahtzee`, `10. Play PacMan` | 番号併記、無効表示 |
| L-01-05 | グループ 4 `Exhibit Tasks` (右列) | `11. Nominate Exhibit for Award`, `12. Sign Exhibit Guestbook`, `13. Read a Guestbook Comment` | 番号併記、クリック/タップで起動 |
| L-01-06 | グループ 5 `System Tasks` | `80. Sign Off the System` | 番号併記 |
| L-01-07 | グループ 6 `Administration` | `90. Start Admin Menu` | 番号併記 (無効表示可) |
| L-01-08 | 説明文 | `Type the menu number you want and then press the ENTER key ... Or type the menu option you are curious about and press the HELP key.` | 要旨保持 (番号入力の案内) |
| L-01-09 | 入力 | 19 行目 `Type Number, then press ENTER` + コマンド行 `===>` | **番号入力欄 + ENTER** を残す (1, 11, 12, 13, 80 が有効) |
| L-01-10 | 遷移 | 1→LRN400, 11→VOTESTUB(NTRSTIT→ADDVOTE), 12→ADDGBSTUB(NTRSTIT→ADDGBCMT), 13→READGBSTUB(NTRSTIT→READGBCMT), 80→SIGNOFF | 同一 |

### 7.2 L-02 INTERSCR/INTERTEST — VCF/400 - How to Navigate

| # | 項目 | 元画面 |
|---|---|---|
| L-02-01 | 見出し | `VCF/400 - How to Navigate` / `Before you start, here is how to navigate:` |
| L-02-02 | 本文 (順) | `Use ARROW KEYS or TAB KEY to move between fields.` → `Use F5 or ENTER to perform your action.` → `Use F12 to quit at any time.` → `Keyboard not responding? If you see X II in the left corner, press RIGHT CTRL to continue.` |
| L-02-03 | 操作 | `Got all that?` `Press ENTER to continue.` → ENTER のみ (**続行 (ENTER)**) |
| L-02-04 | 遷移 | VCFMAIN 11/12/13 とキオスク 1/3/4 の直後に必ず表示 → 次画面へ |

### 7.3 L-03 VOTESCR/VOTE1 — NOMINATE EXHIBIT FOR AWARD

| # | 項目 | 元画面 (行/桁) | 属性 |
|---|---|---|---|
| L-03-01 | 見出し | 1 行 `VINTAGE COMPUTER FESTIVAL` / `NOMINATE EXHIBIT FOR AWARD` | 文言保持 |
| L-03-02 | **入力 1** バッジ番号 | 3 行 `First, type your SFGE Badge Number...............:` → INPUTBADGE | 4 桁数値, 必須, RZ, エラー時反転 (40) |
| L-03-03 | **入力 2** 展示 ID | 4 行 `Second, type the Exhibit ID you are nominating...:` → INEXHB | 9 文字, 通常は事前入力・**保護** (70), MM2024 のみ入力可, エラー時反転 (41) |
| L-03-04 | **入力 3** アワード ID | 5 行 `Third, type the Award ID you are selecting.......:` → INPUTAWARD | 3 桁数値, 必須, RZ, エラー時反転 (42) |
| L-03-05 | エラー行 | 6 行 14 桁開始 ERRLINE (45 桁, 赤) | M-02〜M-09 |
| L-03-06 | 情報: アワード一覧 | 8 行 `Available Awards for This Year:` → 9 行 `001. Best in Show Award - This award is given to the exhibit who you believe to be the best in show for 2024.` → 12 行 `002. The Ed Fair Award - This award is given to the exhibit that is deemed the most informative of the show.` | 移行版では AWARDDB から動的表示 (番号 3 桁 + タイトル + 説明) |
| L-03-07 | 注意文 | 15 行 `You may only vote for ONE award.` `Choose your nomination carefully!` (赤) | 文言保持 |
| L-03-08 | 補助 | 23 行 `TAB = Switch Fields`, 23 行 67 桁 `V1R0M4` (版数) | 任意 |
| L-03-09 | **ファンクションキー** | 24 行 `Cmd5/F5 = Submit Vote` / `Cmd12/F12 = Cancel` | ボタン **「送信 (F5)」「キャンセル (F12)」**。ENTER も送信 |
| L-03-10 | 入力順 | バッジ → 展示 ID → アワード ID | 同一 |
| L-03-11 | 遷移 | 成功 → VOTEEND (L-04); 失敗 → 同画面 + エラー行 (入力保持); F12 → 呼び出し元 | 同一 |

### 7.4 L-04 VOTESCR/VOTEEND, ENDOFCON — 投票結果画面

| # | 画面 | 元画面 | ボタン |
|---|---|---|---|
| L-04-01 | VOTEEND | アスキーアート `THANK YOU FOR VOTING!` + M-16 本文 + `Press ENTER to return to the main menu.` | **「メニューへ戻る (ENTER)」** |
| L-04-02 | ENDOFCON | アスキーアート `SORRY!` + M-17 本文 + `Press ENTER to exit.` | **「終了 (ENTER)」** |

### 7.5 L-05 EXHBMENUSC/MENU — WELCOME TO... (キオスク)

| # | 項目 | 元画面 |
|---|---|---|
| L-05-01 | 見出し | 1 行 `WELCOME TO...` → 2 行 OUTTITLE (展示タイトル 75) |
| L-05-02 | 展示者情報 (順) | 3 行 `HOSTED BY` OUTNAME(25) `OF` OUTCITY(15) OUTSTATE(2) |
| L-05-03 | 説明 | 5〜18 行 OUTDESC (1000) |
| L-05-04 | メニュー (順) | 20 `1. Nominate This Exhibit for Award` (ELIGIBLE=0 で非表示), 21 `2. Learn More About This Exhibit` (ENLRN400=0 で非表示), 22 `3. Sign Exhibit Guestbook`, 23 `4. Read Exhibit Guestbook` |
| L-05-05 | 入力 | 23 行 78 桁 INOPT (1 桁数値, 必須); 同じ 23 行に `Select Menu Option, Press ENTER:` (44 桁開始) |
| L-05-06 | 隠しオプション | 7 → ADMPSWRD (L-06) |
| L-05-07 | ファンクションキー | なし (F3/F12 は `Function key not allowed.`) — モダン UI では番号ボタン + 番号入力欄 + **「選択 (ENTER)」** |
| L-05-08 | 遷移 | 1→NTRSTIT→ADDVOTE, 2→LRN400, 3→NTRSTIT→ADDGBCMT, 4→NTRSTIT→READGBCMT; サブ機能終了後はこの画面に戻る |

### 7.6 L-06 EXHBMENUSC/ADMPSWRD — キオスク終了

| # | 項目 | 元画面 |
|---|---|---|
| L-06-01 | 本文 | `Are you sure you want to exit the kiosk?` `Type the Administrator password, press ENTER to sign off` |
| L-06-02 | 入力 | 7 行 5 桁開始 INPWD (9 文字, 非表示推奨) |
| L-06-03 | 操作 | ENTER のみ → 一致でキオスク終了、不一致でキオスクメニューへ (**「サインオフ (ENTER)」**) |

### 7.7 L-07 GUESTBKSCR/ADDCMT — GUESTBOOK/400 - ADD COMMENT

| # | 項目 | 元画面 | 属性 |
|---|---|---|---|
| L-07-01 | 見出し | 1 行 `VINTAGE COMPUTER FESTIVAL` / `GUESTBOOK/400 - ADD COMMENT` | 文言保持 |
| L-07-02 | **入力 1** 名前 | 3 行 `Your Name................:` INNAME (3 行 32 桁) | 16 文字, 必須, 小文字可 |
| L-07-03 | **入力 2** 展示 ID | 4 行 `Exhibit ID...............:` → INID (4 行 32 桁) | 9 文字, 事前入力・**保護** (70), MM2024 のみ入力可 |
| L-07-04 | **入力 3** コメント | 5 行 `Comment..................:` → 6 行 INCMT (6 行 3 桁) | 200 文字, 必須, 小文字可 |
| L-07-05 | エラー行 | 10 行 5 桁開始 ERRLINE (21 桁, 赤) | M-10〜M-12 |
| L-07-06 | **ファンクションキー** | 24 行 `Cmd5 / F5 = Submit Comment` / `Cmd12 / F12 = Cancel` | **「送信 (F5)」「キャンセル (F12)」** |
| L-07-07 | 入力順 | 名前 → 展示 ID → コメント | 同一 |
| L-07-08 | 遷移 | 成功 → ENDCMT (L-08); 失敗 → 同画面 + エラー行; F12 → 呼び出し元 | 同一 |

### 7.8 L-08 GUESTBKSCR/ENDCMT — THANKS FOR COMMENTING!

| # | 項目 | 元画面 |
|---|---|---|
| L-08-01 | 本文 | アスキーアート `THANKS FOR COMMENTING!` + `Thank you for commenting on this exhibit! Your Comment ID Is:` (ID 出力項目なし) + `Press ENTER to exit.` |
| L-08-02 | 操作 | **「終了 (ENTER)」** → 呼び出し元 |

### 7.9 L-09 GUESTBKSCR/READCMT — GUESTBOOK/400 - Read a Comment

| # | 項目 | 元画面 |
|---|---|---|
| L-09-01 | 見出し | 1 行 `VINTAGE COMPUTER FESTIVAL` / `GUESTBOOK/400 - Read a Comment` |
| L-09-02 | **入力 1** コメント ID | 3 行 25 桁 INCMTID (`Enter a Comment ID:` は 3 行 4 桁開始) (4 桁数値, 必須, RZ) |
| L-09-03 | 総数表示 | `Currently hosting` は 3 行 49 桁開始、WTFCMTNUM は 3 行 67 桁、`comments and counting.` は 4 行 49 桁開始 |
| L-09-04 | エラー行 | 4 行 4 桁開始 ERRLINE (20 桁, 赤) M-13 |
| L-09-05 | 出力 (順) | 6 行 OUTNAME(11) (4 桁開始) `says to:` → 6 行 29 桁開始 OUTTITLE(50) → 8 行 4 桁開始 OUTCMT(200) |
| L-09-06 | 注意文 | 21〜22 行 `Is this comment inappropriate? Please report the Comment ID to the folks at the Midrange Madness table and we will address the comment.` |
| L-09-07 | **ファンクションキー** | 24 行 `Cmd5 / F5 = Submit  Cmd12 / F12 = Cancel` → **「送信 (F5)」「キャンセル (F12)」** |
| L-09-08 | 遷移 | F5 → 同画面に結果表示 (繰返し可); F12 → 呼び出し元 |

### 7.10 L-10 LRN400SCR/MAIN — LEARN/400

| # | 項目 | 元画面 |
|---|---|---|
| L-10-01 | 見出し | 1 行 `LEARN/400` / 1 行 70 桁 `Page` / OUTPAGENBR (1 行 76 桁) |
| L-10-02 | 本文 | 3〜21 行 OUTCONTENT (1500) |
| L-10-03 | **ファンクションキー** | 23 行 `Cmd3/F3 = Exit  Cmd5/F5 = Forwards  Cmd8 / F8 = Backwards` → **「戻る (F3)」(終了)「進む (F5)」「前へ (F8)」** |
| L-10-04 | 遷移 | F5 次ページ (END ページで終了), F8 前ページ, F3 終了 → 呼び出し元 |

### 7.11 台帳サマリ (ファンクションキー・メニュー番号)

| キー | 役割 | 使用画面 | モダン UI ラベル |
|---|---|---|---|
| ENTER | 送信 / 続行 / メニュー選択 | 全画面 | 「続行 (ENTER)」「選択 (ENTER)」など |
| F3 | 終了 (Exit) | LRN400SCR | 「戻る (F3)」 |
| F5 | 送信 (Submit) / 進む (Forwards) | VOTE1, ADDCMT, READCMT / LRN400SCR | 「送信 (F5)」 / 「進む (F5)」 |
| F8 | 前へ (Backwards) | LRN400SCR | 「前へ (F8)」 |
| F12 | 取消 (Cancel) | VOTE1, ADDCMT, READCMT | 「キャンセル (F12)」 |

| メニュー | 番号 → 機能 |
|---|---|
| VCFMAIN | 1 LEARN/400, 11 投票, 12 ゲストブック記入, 13 ゲストブック閲覧, 80 サインオフ, 90 管理 (2–10 は対象外) |
| EXHBMENU | 1 投票, 2 LEARN/400, 3 ゲストブック記入, 4 ゲストブック閲覧, 7 (隠し) キオスク終了 |

---

## 8. 移行実装への非機能要件 (共通 UI 方針の具体化)

1. 画面遷移・メニュー階層は第 2 章と同一。VCFMAIN の 11/12/13 とキオスクの 1/3/4 の直後には必ず「How to Navigate」(L-02) を表示する。
2. 入力項目の順序・グルーピングは第 7 章の台帳どおり (投票: バッジ → 展示 ID → アワード ID)。展示 ID は VCFMAIN 経由では読み取り専用 (事前入力)、MM2024 モードでは編集可。
3. すべてのアクションボタンに元のファンクションキー番号を併記する (7.11)。物理キーボードの F5/F12/F3/F8 と ENTER も同じ動作にバインドする。
4. メニューは番号ラベル付きのボタン/リストとし、加えて番号入力欄 + ENTER でも選択できる。
5. 画面タイトル・見出し・メッセージ文言 (第 6 章) は英語原文のまま保持する。
6. エラーは元と同じ「同一画面上のエラー行 (1 件、最後に失敗した検査のメッセージ)」で表示し、入力値を保持する。
7. 業務ルール (第 4 章) と検証ルール (第 5 章) は DB 側の制約 (ユニークキー) を含めて再現する。
8. グリーンスクリーンの模写 (黒背景・緑文字・等幅 24x80 グリッド) は行わない。アスキーアート (THANK YOU 等) は文字見出しに置き換える。
9. 監査印字 (F-08/F-09 PRTLSTVOTE / PRTLSTCMT) はスプール出力であり Web/iPad に等価物がないため、VOTINGDB / GUESTBKDB の一覧を参照できる手段 (Java: `/api/db/votes`, `/api/db/comments`、iPad: SQLite ファイル) で代替する。
10. ENDCMT (L-08) は元画面に採番結果の出力項目がないが、移行版では採番したコメント ID を表示する (利用者が READCMT で参照するため、およびクロス検証で ID を比較するため)。
11. 管理機能 (F-10〜F-16、VCFMAIN 90) は移行対象外とし、メニュー上は番号ラベル付きの無効項目として表示する。テスト時に必要な SETTINGS / VISIBLE の操作は各版の DB 操作手段で行う。
