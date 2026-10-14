# VCF400 PUB400 E2E テストシナリオ (10 基本機能)

- 対象: madmerger/VCF400 master の RPG/CL/DDS を PUB400 (pub400.com) のライブラリ ASHIBATA2 にソースビルドしたもの
- 根拠仕様: DeepWiki (madmerger/VCF400) — 3 Kiosk Application Flow / 3.1 EXHBMENU / 3.2 Menus and CL Stubs / 4.1 Casting a Vote / 5.1 Adding Comments / 5.2 Reading and Moderating Comments / 6.1 LEARN/400 Runtime
- 実行方法: 5250 端末 (tn5250, telnet pub400.com:23) でサインイン → `CHGLIBL LIBL(ASHIBATA2 ASHIBATA1 QGPL QTEMP) CURLIB(ASHIBATA2)` → `GO ASHIBATA2/VCFMAIN`
- 利用者プロファイル (CURUSER) = ASHIBATA → メニュー 11/12/13 の LAUNCH パラメータは `ASHIBATA` (展示 ID 固定・保護)
- 共用端末モードは コマンドラインから `CALL ASHIBATA2/<PGM> PARM('MM2024')`、キオスクは `CALL ASHIBATA2/EXHBMENU PARM('<展示>')`
- 各シナリオ開始前に DB をベースラインへ再シード (pub400_seed.sql)。DB 確認は SSH (2222) の `qsh -c 'db2 ...'`

## ベースラインデータ
| ファイル | 内容 |
|---|---|
| EXHBDB | ASHIBATA (ELIGIBLE=1, ENLRN400=1, "IBM i on PUB400 Demo") / DEMO400 (1, 0, "AS/400 Model 150") / NOVOTE (0, 0, "Ineligible test exhibit (ELIGIBLE=0)") |
| AWARDDB | 1 = Best in Show Award / 2 = The Ed Fair Award |
| SETTINGS | ADMPSWRD=VCF2024 / ALWVOTE=Y |
| VOTINGDB | (1,1,ASHIBATA) / (28,2,ASHIBATA) |
| GUESTBKDB | 1: Y, ASHIBATA, "Great exhibit", "VCF/400 running on PUB400." / 2: Y, ASHIBATA, "Devin", "VCF/400 is running on PUB400." |
| LRN400STR | p1 "Welcome to LEARN/400! ..." / p2 "The AS/400 was introduced by IBM in June 1988 ... page 2" / p3 "This is the last page. ..." EXTRA='END' |

## シナリオ一覧
| ID | 基本機能 | DeepWiki 根拠 |
|---|---|---|
| TS-01 | メインメニュー VCFMAIN 表示とメニュー遷移 | 3.2 |
| TS-02 | 投票: 入力必須チェック | 4.1 (VOTE1 / 入力検証) |
| TS-03 | 投票: 正常登録 | 4.1 (ADDTODB, CHECKOK=4, VOTEEND) |
| TS-04 | 投票: 業務チェック (重複/資格外/展示なし/アワードなし) | 4.1 (4 チェック) |
| TS-05 | 投票: 投票期間終了 (ALWVOTE='N') | 4.1 (CHKALWVOTE → ENDOFCON) |
| TS-06 | ゲストブック記入 (入力チェック + CMTID 採番) | 5.1 |
| TS-07 | ゲストブック閲覧 (ID 指定 / 末尾超え) | 5.2 |
| TS-08 | ゲストブック閲覧の可視性・帰属制御 | 5.2 (VISIBLE='N', EXHBID≠USERPRF) |
| TS-09 | LEARN/400 ページ送り (F5/F8/END/F3) | 6.1 |
| TS-10 | 展示キオスク EXHBMENU (機能ゲーティング + 管理者終了) | 3 / 3.1 |

---

### TS-01 メインメニュー VCFMAIN 表示とメニュー遷移
1. サインイン → `GO ASHIBATA2/VCFMAIN`
   - 期待: 1 行目 `AS/400 DEMO MENU`、2 行目 `WELCOME TO...`、`V I N T A G E  C O M P U T E R  F E S T I V A L`、`11. Nominate Exhibit for Award` / `12. Sign Exhibit Guestbook` / `13. Read a Guestbook Comment` / `80. Sign Off the System` / `90. Start Admin Menu`
2. `11` + Enter
   - 期待: インタースティシャル NTRSTIT `VCF/400 - How to Navigate` (F5/ENTER, F12 の説明)
3. Enter → 期待: VOTE1 `NOMINATE EXHIBIT FOR AWARD` が表示
4. F12 → 期待: VCFMAIN に戻る

### TS-02 投票: 入力必須チェック (VCFMAIN 11, LAUNCH=ASHIBATA)
1. VCFMAIN 11 → Enter (NTRSTIT) → VOTE1
   - 期待: 展示 ID 欄に `ASHIBATA` が事前入力・保護 (入力不可)
2. 全項目空で F5 → 期待: 画面 VOTE1 のまま、エラー行 `Must enter Award ID` (最後に失敗したチェックが表示される)
3. バッジ `7001`、アワード空で F5 → 期待: `Must enter Award ID`
4. バッジ空、アワード `1` で F5 → 期待: `Must enter badge number`
5. バッジ `7002`、アワード `0` で F5 → 期待: `Must enter Award ID` (数値 0 は未入力扱い)
6. F12 で終了 → DB: VOTINGDB はベースライン 2 件のまま

### TS-03 投票: 正常登録
1. VCFMAIN 11 → VOTE1、バッジ `7001`、アワード `1` で F5
   - 期待: VOTEEND (`THANK YOU` アスキーアート、`Your vote has been RECORDED!`)
2. Enter → VCFMAIN に戻る
3. DB: VOTINGDB に (BADGENBR=7001, AWARDNBR=1, EXHBNBR='ASHIBATA') が追加され計 3 件

### TS-04 投票: 業務チェック
1. VCFMAIN 11、バッジ `1` (既存)、アワード `2` で F5 → 期待: `You have already voted.`
2. 同画面でバッジ `7002`、アワード `9` で F5 → 期待: `Award does not exist.`
3. F12 → コマンドラインで `CALL ASHIBATA2/ADDVOTE PARM('MM2024')` (共用端末: 展示 ID 入力可)
4. バッジ `7002`、展示 `NOVOTE`、アワード `1` で F5 → 期待: `Exhibit ineligible for award`
5. 展示を `NOSUCH` に変えて F5 → 期待: `Exhibit does not exist`
6. バッジ `1`、展示 `NOSUCH`、アワード `9` で F5 → 期待: `Award does not exist.` (複数違反時は最後のチェック = アワードが勝つ)
7. F12 → DB: VOTINGDB はベースライン 2 件のまま (書込なし)

### TS-05 投票: 投票期間終了
1. 事前: SETTINGS.ALWVOTE を 'N' に更新
2. VCFMAIN 11 → Enter (NTRSTIT)
   - 期待: VOTE1 は表示されず ENDOFCON (`SORRY!` アスキーアート、`The voting period has ended and you can no longer vote.`)
3. Enter → VCFMAIN に戻る
4. 事後: ALWVOTE を 'Y' に戻す。DB: VOTINGDB は 2 件のまま

### TS-06 ゲストブック記入 (VCFMAIN 12, LAUNCH=ASHIBATA)
1. VCFMAIN 12 → Enter (NTRSTIT) → ADDCMT `GUESTBOOK/400 - ADD COMMENT`、展示 ID `ASHIBATA` が保護表示
2. 名前空、コメント `Hello` で F5 → 期待: `Must enter your name`
3. 名前 `Devin Tester`、コメント空で F5 → 期待: `Must enter a comment`
4. 名前 `Devin Tester`、コメント `E2E test run` で F5 → 期待: ENDCMT (THANK YOU FOR COMMENTING アスキーアート)
5. Enter → VCFMAIN
6. DB: GUESTBKDB に CMTID=3 (既存最大 2 + 1)、VISIBLE='Y'、EXHBID='ASHIBATA'、GUESTNAME='Devin Tester'、GUESTCMT='E2E test run'

### TS-07 ゲストブック閲覧 (VCFMAIN 13, LAUNCH=ASHIBATA)
1. VCFMAIN 13 → Enter (NTRSTIT) → READCMT。総コメント数欄に `2` (最終 CMTID)
2. ID 空で Enter → 期待: `Must enter CommentID`
3. ID `0001` (4 桁入力、Field Exit 不要) で Enter
   - 期待: 名前 `Great exhib` (表示欄 11 桁)、展示タイトル `IBM i on PUB400 Demo`、コメント `VCF/400 running on PUB400.`
4. 再度 VCFMAIN 13、ID `9999` → 期待: 末尾超えのため最終レコード (ID 2) にフォールバック: 名前 `Devin`、コメント `VCF/400 is running on PUB400.`

### TS-08 ゲストブック閲覧の可視性・帰属制御
1. 事前: GUESTBKDB ID 2 の VISIBLE を 'N' に更新
2. VCFMAIN 13、ID `0002` → 期待: 名前 `Name Hidden`、コメント `This comment hidden by an admin - offensive content.`
3. コマンドラインで `CALL ASHIBATA2/READGBCMT PARM('DEMO400')`、ID `0001` → 期待: 名前・タイトル空欄、コメント `This comment is not part of this guestbook.` (他展示のコメントは非公開)
4. `CALL ASHIBATA2/READGBCMT PARM('MM2024')`、ID `0001` → 期待: 共用端末は閲覧可: `Great exhib` / `IBM i on PUB400 Demo` / `VCF/400 running on PUB400.`
5. 事後: VISIBLE を 'Y' に戻す

### TS-09 LEARN/400 ページ送り (VCFMAIN 1)
1. VCFMAIN `1` → 期待: LEARN/400 画面、ページ 1 `Welcome to LEARN/400! This is page 1. ...`
2. F5 → 期待: ページ 2 `The AS/400 was introduced by IBM in June 1988 ... page 2`
3. F8 → 期待: ページ 1 に戻る
4. F5, F5 → 期待: ページ 3 は EXTRA='END' のためプログラム終了 → VCFMAIN
5. 再度 VCFMAIN 1 → F3 → 期待: VCFMAIN に戻る

### TS-10 展示キオスク EXHBMENU
1. `CALL ASHIBATA2/EXHBMENU PARM('ASHIBATA')`
   - 期待: `WELCOME TO...`、展示タイトル `IBM i on PUB400 Demo`、出展者 `Akira Shibata`、オプション 1〜4 (`1. Nominate This Exhibit for Award` / `2. Learn More About This Exhibit` / `3. Sign Exhibit Guestbook` / `4. Read Exhibit Guestbook`) が表示。7 はメニューに表示されない (隠しオプション)
2. `7` → 期待: ADMPSWRD `Are you sure you want to exit the kiosk?`、パスワード `WRONGPW` → キオスクメニューに戻る
3. `7` → パスワード `VCF2024` (SETTINGS.ADMPSWRD) → 期待: キオスク終了しコマンド画面へ戻る
4. `CALL ASHIBATA2/EXHBMENU PARM('NOVOTE')` → 期待: タイトル `Ineligible test exhibit (ELIGIBLE=0)`、オプション 1・2 は非表示、3・4 のみ表示
5. `1` を入力 → 期待: 無視されキオスクメニューのまま (投票画面に遷移しない)
6. `7` → `VCF2024` で終了
