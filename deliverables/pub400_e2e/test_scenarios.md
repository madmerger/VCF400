# VCF400 PUB400 E2E テストシナリオ (10 基本機能)

- 対象: madmerger/VCF400 master の RPG/CL/DDS を PUB400 (pub400.com) の検証用オブジェクトライブラリ (`<USR>2`) へ `tools/pub400_build.sh` でソースビルドしたもの。`<USR>` は PUB400 ユーザープロファイル (= シークレット PUB400_LOGIN の大文字、ログ上は伏字)。
- 根拠仕様: DeepWiki (madmerger/VCF400) — 3 Kiosk Application Flow / 3.1 EXHBMENU / 3.2 Menus and CL Stubs / 4.1 Casting a Vote / 5 Guestbook Subsystem (5.1 追加 / 5.2 閲覧) / 6 LEARN/400 Scripting Engine / 6.1 Runtime and Page Language。AGENTS.md の業務ルールも併用。
- 実行方法: Devin E2E テスト機能 (testing_agent) が GUI 端末上の実 tn5250 (`env.TERM=IBM-3179-2 map=37 pub400.com`) でサインオン → `CHGLIBL LIBL(<USR>2 <USR>1 QGPL QTEMP) CURLIB(<USR>2)` → `GO <USR>2/VCFMAIN`。
- 起動プロファイル: メニュー 11/12/13 の CL スタブは RTVJOBA の USER (= `<USR>`) を LAUNCH として渡す (展示 ID 固定・保護)。共用端末モードは `CALL <USR>2/<PGM> PARM('MM2024')`、キオスクは `CALL <USR>2/EXHBMENU PARM('<展示>')`。
- 各シナリオ開始前と全体終了時に `tools/pub400_db.sh reset` でベースラインへ再シードし、登録・拒否系は `pub400_db.sh dump` の before/after で実 DB を確認する。
- 判定: 画面識別 (タイトル)・業務テキスト・DB で判定。map=37 によるグリフ差 (`!`→`|`、ASCII アートの記号) は観察事項として記録し機能判定に含めない。
- ケース定義 (機械可読): `cases.json`

## ベースラインデータ (`tools/pub400_seed.sql`)
| ファイル | 内容 |
|---|---|
| EXHBDB | `<USR>` (ELIGIBLE=1, ENLRN400=1, "IBM i on PUB400 Demo") / DEMO400 (1, 0, "AS/400 Model 150") / NOVOTE (0, 0, "Ineligible test exhibit (ELIGIBLE=0)") |
| AWARDDB | 1 = Best in Show Award / 2 = The Ed Fair Award |
| SETTINGS | ADMPSWRD=VCF2024 / ALWVOTE=Y |
| VOTINGDB | (1,1,`<USR>`) / (28,2,`<USR>`) |
| GUESTBKDB | 1: Y, `<USR>`, "Great exhibit", "VCF/400 running on PUB400." / 2: Y, `<USR>`, "Devin", "VCF/400 is running on PUB400." |
| LRN400STR | p1 "Welcome to LEARN/400! ..." / p2 "The AS/400 was introduced by IBM in June 1988 ... page 2" / p3 "This is the last page. ..." EXTRA='END' |

## シナリオ一覧
| ID | 基本機能 | DeepWiki 根拠 |
|---|---|---|
| TS-01 | メインメニュー VCFMAIN 表示とメニュー遷移 | 3.2 Menus and CL Stubs |
| TS-02 | 投票: 入力必須チェック | 4.1 Casting a Vote (VOTE1 / 入力検証) |
| TS-03 | 投票: 正常登録 | 4.1 (ADDTODB, CHECKOK=4, VOTEEND) |
| TS-04 | 投票: 業務チェック (重複/資格外/展示なし/アワードなし) | 4.1 (4 チェック、最後の失敗が ERRLINE) |
| TS-05 | 投票: 投票期間終了 (ALWVOTE='N') | 4.1 (SETTINGS.ALWVOTE → ENDOFCON) |
| TS-06 | ゲストブック記入 (入力チェック + CMTID 採番) | 5 / 5.1 |
| TS-07 | ゲストブック閲覧 (ID 指定 / 末尾超え) | 5 / 5.2 |
| TS-08 | ゲストブック閲覧の可視性・帰属制御 | 5.2 (VISIBLE='N', EXHBID≠USERPRF, MM2024) |
| TS-09 | LEARN/400 ページ送り (F5/F8/END/F3) | 6 / 6.1 |
| TS-10 | 展示キオスク EXHBMENU (機能ゲーティング + 隠しオプション 7) | 3 / 3.1 |

---

### TS-01 メインメニュー VCFMAIN 表示とメニュー遷移
1. サインオン → `GO <USR>2/VCFMAIN`
   - 期待: `AS/400 DEMO MENU`、`WELCOME TO...`、`V I N T A G E  C O M P U T E R  F E S T I V A L`、`11. Nominate Exhibit for Award` / `12. Sign Exhibit Guestbook` / `13. Read a Guestbook Comment` / `80. Sign Off the System` / `90. Start Admin Menu`
2. `11` + Enter → 期待: NTRSTIT `VCF/400 - How to Navigate`
3. Enter → 期待: VOTE1 `NOMINATE EXHIBIT FOR AWARD`
4. F12 → 期待: VCFMAIN に戻る

### TS-02 投票: 入力必須チェック (VCFMAIN 11)
1. VCFMAIN 11 → Enter → VOTE1。期待: 展示 ID 欄に `<USR>` が事前入力・保護
2. 全項目空で F5 → 期待: VOTE1 のまま、エラー行 `Must enter Award ID` (最後に失敗したチェック)
3. バッジ `7001`、アワード空で F5 → 期待: `Must enter Award ID`
4. バッジ空、アワード `1` で F5 → 期待: `Must enter badge number`
5. F12 で終了 → DB: VOTINGDB はベースライン 2 件のまま

### TS-03 投票: 正常登録
1. VCFMAIN 11 → VOTE1、バッジ `7001`、アワード `1` で F5 → 期待: VOTEEND (`Your vote has been RECORDED!`)
2. Enter → VCFMAIN
3. DB: VOTINGDB に (7001, 1, `<USR>`) が追加され計 3 件

### TS-04 投票: 業務チェック
1. VCFMAIN 11、バッジ `1` (投票済み)、アワード `2` で F5 → 期待: `You have already voted.`
2. バッジ `7002`、アワード `9` で F5 → 期待: `Award does not exist.`
3. F12 → `CALL <USR>2/ADDVOTE PARM('MM2024')` (共用端末: 展示 ID 入力可)
4. バッジ `7002`、展示 `NOVOTE`、アワード `1` で F5 → 期待: `Exhibit ineligible for award`
5. 展示 `NOSUCH` で F5 → 期待: `Exhibit does not exist`
6. バッジ `1`、展示 `NOSUCH`、アワード `9` で F5 → 期待: `Award does not exist.` (複数違反時は最後のチェック)
7. F12 → DB: VOTINGDB は 2 件のまま

### TS-05 投票: 投票期間終了
1. 事前: `UPDATE SETTINGS SET VALUE='N' WHERE SETTING='ALWVOTE'`
2. VCFMAIN 11 → Enter → 期待: VOTE1 を出さず ENDOFCON (`The voting period has ended and you can no longer vote.`)
3. Enter → VCFMAIN。事後: 再シードで ALWVOTE='Y'、VOTINGDB 2 件のまま

### TS-06 ゲストブック記入 (VCFMAIN 12)
1. VCFMAIN 12 → Enter → ADDCMT `GUESTBOOK/400`、展示 ID `<USR>` 保護
2. 名前空、コメント `Hello` で F5 → 期待: `Must enter your name`
3. 名前 `Devin Tester`、コメント空で F5 → 期待: `Must enter a comment`
4. 名前 `Devin Tester`、コメント `E2E test run` で F5 → 期待: ENDCMT (完了画面)
5. Enter → VCFMAIN。DB: GUESTBKDB に CMTID=3 (既存最大 2 + 1)、VISIBLE='Y'、EXHBID=`<USR>`

### TS-07 ゲストブック閲覧 (VCFMAIN 13)
1. VCFMAIN 13 → Enter → READCMT。総コメント数 `2`
2. ID 空で F5 → 期待: `Must enter CommentID`
3. ID `0001` (4 桁、Field Exit なし) → 期待: 名前 `Great exhib` (表示 11 桁)、タイトル `IBM i on PUB400 Demo`、本文 `VCF/400 running on PUB400.`
4. 再度 ID `9999` → 期待: 最終レコード (ID 2) にフォールバック: `Devin` / `VCF/400 is running on PUB400.`

### TS-08 ゲストブック閲覧の可視性・帰属制御
1. 事前: GUESTBKDB ID 2 の VISIBLE='N'
2. VCFMAIN 13、ID `0002` → 期待: `Name Hidden`、`This comment hidden by an admin - offensive content.`
3. `CALL <USR>2/READGBCMT PARM('DEMO400')`、ID `0001` → 期待: 名前・タイトル空欄、`This comment is not part of this guestbook.`
4. `CALL <USR>2/READGBCMT PARM('MM2024')`、ID `0001` → 期待: 閲覧可 `Great exhib` / `VCF/400 running on PUB400.`
5. 事後: 再シード

### TS-09 LEARN/400 ページ送り (VCFMAIN 1)
1. VCFMAIN `1` → 期待: LEARN/400、ページ 1 `Welcome to LEARN/400! ...`
2. F5 → ページ 2 `The AS/400 was introduced by IBM in June 1988 ... page 2`
3. F8 → ページ 1
4. F5, F5 → ページ 3 は EXTRA='END' のため終了 → VCFMAIN
5. 再度 `1` → F3 → VCFMAIN

### TS-10 展示キオスク EXHBMENU
1. `CALL <USR>2/EXHBMENU PARM('<USR>')` → 期待: `WELCOME TO...`、`IBM i on PUB400 Demo`、`Akira Shibata`、オプション 1〜4 表示、7 は非表示
2. `7` → ADMPSWRD 画面、`WRONGPW` → キオスクに戻る
3. `7` → `VCF2024` → キオスク終了しコマンド画面へ
4. `CALL <USR>2/EXHBMENU PARM('NOVOTE')` → 期待: タイトル `Ineligible test exhibit (ELIGIBLE=0)`、オプション 1・2 非表示、3・4 のみ
5. `1` → 無視されキオスクのまま
6. `7` → `VCF2024` で終了。最後に `signoff`
