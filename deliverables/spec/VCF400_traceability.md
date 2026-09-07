# VCF/400 トレーサビリティ表 (仕様書 / 元画面構造台帳 ↔ Java 版 ↔ iPad 版)

- 仕様書: `deliverables/spec/VCF400_spec.md` (ID: F-xx 機能, D-xx DB, B-xx 業務ルール, V-xx 検証, M-xx メッセージ, L-xx-yy 元画面構造台帳)
- Java 版: `java/src/main/java/com/vcf400/...` / `java/src/main/resources/templates/*.html`
- iPad 版: `ipad/VCF400Kit/Sources/VCF400Kit/*.swift` / `ipad/VCF400/Views/*.swift`
- 元画面構造台帳: `deliverables/spec/VCF400_screen_ledger.md`（`VCF400_spec.md` 第 7 章の独立した正本）
- 「適合」列は フェーズ4 (仕様適合性の検証) の結果。相違があった項目は末尾「フェーズ4 相違一覧と対処」に記載。

判定記号: ✅ 一致 / ✅* 一致 (修正後) / ➖ 移行対象外 (仕様書で明示) / ⚠ 意図的な差異 (備考参照)

## 0. 独立台帳・共通テストケース参照

L-xx / L-xx-yy の画面構造・DDS 座標・文言は、仕様書の要約ではなく
`deliverables/spec/VCF400_screen_ledger.md` を正とする。仕様書
`deliverables/spec/VCF400_spec.md` 第 7 章はこの台帳へのポインタと一覧表を掲載する。

業務ルール (B-xx) と入力検証 (V-xx) の「テストケース」列は、Java/iPad の
単体テスト名に加えて、共通クロス検証定義 `deliverables/tests/cases.json` の
`rules` に付与された CV-xx ID を参照する。

| ルール | `cases.json` の共通ケース ID |
|---|---|
| B-01 | CV-05 |
| B-02 | CV-10, CV-31, CV-32, CV-37 |
| B-03 | CV-13 |
| B-04 | CV-04, CV-11 |
| B-05 | CV-01, CV-12, CV-14 |
| B-06 | CV-17, CV-22 |
| B-07 | CV-24 |
| B-08 | CV-23, CV-25, CV-26 |
| B-09 | CV-23, CV-26 |
| B-10 | CV-29 |
| B-11 | CV-34, CV-35 |
| B-12 | CV-31, CV-32, CV-33 |
| B-13 | CV-08, CV-11, CV-36 |
| B-15 | CV-07 |
| V-01 | CV-01, CV-03 |
| V-02 | CV-01, CV-02, CV-07 |
| V-03 | CV-08 |
| V-04 | CV-05 |
| V-05 | CV-10 |
| V-06 | CV-09 |
| V-07 | CV-06 |
| V-08 | CV-04 |
| V-09 | CV-13 |
| V-10 | CV-18 |
| V-11 | CV-14, CV-16 |
| V-12 | CV-14, CV-15 |
| V-13 | CV-19 |
| V-14 | CV-20 |
| V-15 | CV-21 |
| V-16 | CV-24 |
| V-17 | CV-25 |
| V-18 | CV-23 |
| V-19 | CV-27 |
| V-20 | CV-28 |
| V-21 | CV-30 |

## 1. 機能 (F-xx)

| ID | 機能 | Java 版 | iPad 版 | テスト | 適合 |
|---|---|---|---|---|---|
| F-01 | VCFMAIN メインメニュー | `MainMenuController` (`/menu`), `vcfmain.html` | `MainMenuView`, `AppModel.selectMain` | `WebFlowTest.menu*`, `SmokeUITests` | ✅ |
| F-02 | NTRSTIT How to Navigate | `MainMenuController` (`/navigate`), `navigate.html` | `NavigateView`, `Route.navigate(next:)` | `WebFlowTest`, `SmokeUITests` | ✅ |
| F-03 | ADDVOTE 投票 | `VoteService`, `VoteController` (`/vote`), `vote1/voteend/endofcon.html` | `VoteService` (Kit), `VoteView`, `VoteEndView`, `EndOfConView` | `VoteServiceTest`, `ServicesTests` (Kit) | ✅ |
| F-04 | ADDGBCMT ゲストブック記入 | `GuestbookService.add`, `GuestbookController` (`/guestbook/add`), `addcmt/endcmt.html` | `GuestbookService.add` (Kit), `AddCommentView`, `EndCommentView` | `GuestbookServiceTest`, `ServicesTests` | ✅ |
| F-05 | READGBCMT ゲストブック閲覧 | `GuestbookService.read`, `/guestbook/read`, `readcmt.html` | `GuestbookService.read` (Kit), `ReadCommentView` | 同上 | ✅ |
| F-06 | LRN400 LEARN/400 | `LearnService`, `LearnController` (`/learn`), `lrn400.html` | `LearnService` (Kit), `LearnView` | `LearnKioskServiceTest`, `ServicesTests.testLearnNavigation` | ✅ |
| F-07 | EXHBMENU キオスク | `KioskService`, `KioskController` (`/kiosk/{x}`, `/exit`), `kiosk/admpswrd.html` | `KioskService` (Kit), `KioskView`, `AdmPswrdView`, 起動引数 `-VCF_KIOSK` | `WebFlowTest.kiosk*`, `SmokeUITests.testKioskHidesOptionsAndPassword` | ✅ |
| F-08 | PRTLSTVOTE 投票印刷 | 投票 DB 一覧 API `GET /api/db/votes` で代替 (印刷スプールなし) | `Repositories.allVotes` (sqlite で参照) | — | ⚠ 印刷は Web/iPad に存在しないため DB 一覧で代替 (仕様書 8 章) |
| F-09 | PRTLSTCMT コメント印刷 | `GET /api/db/comments` で代替 | `Repositories.allComments` | — | ⚠ 同上 |
| F-10〜F-16 | 管理機能 (ADMCRTEXHB, ADMHIDECMT, ADMSETTING, ADMVOTERPT, ADMLRN400, ADMADDSOFR, ADMMAIN) | 対象外。テスト用に `/api/db/settings`, `/api/db/comments/{id}/visible` で SETTINGS/VISIBLE を操作可 | 対象外。`Repositories.updateSetting/setVisible` (テスト用) | — | ➖ 仕様書 8 章 11 項 (移行対象外) / VCFMAIN 90 は無効表示 (L-01-07) |

## 2. DB スキーマ (D-xx) — DB2 for i 相当

| ID | ファイル | Java (`schema.sql`, H2 MODE=DB2) | iPad (`Database.swift`, SQLite) | 適合 |
|---|---|---|---|---|
| D-01 | EXHBDB (EXHUSRPRF KEY, ELIGIBLE, ENLRN400 …) | `CREATE TABLE EXHBDB` 同列名・CHAR 桁数・PK | 同一 DDL | ✅ |
| D-02 | VOTINGDB (BADGENBR KEY, AWARDNBR, EXHBNBR) | 同 | 同 | ✅ |
| D-03 | GUESTBKDB (CMTID KEY, VISIBLE, EXHBID, GUESTNAME, GUESTCMT) | 同 | 同 | ✅ |
| D-04 | AWARDDB (AWARDID KEY, AWARDTITLE, AWARDDESC) | 同 | 同 | ✅ |
| D-05 | SETTINGS (SETTING KEY, VALUE) | 同 (`NON_KEYWORDS=VALUE`) | 同 | ✅ |
| D-06 | SECOFRS (USERPROF KEY) | 同 (管理機能対象外のため未使用) | 同 | ✅ |
| D-07 | LRN400STR (PAGENBR KEY, CONTENT, EXTRA) | 同 | 同 | ✅ |
| 参照データ | PUB400 ASHIBATA2 の観測値 | `data.sql` | `Database.seed()` (同値) | ✅ |

## 3. 業務ルール (B-xx)

| ID | ルール | Java 版 | iPad 版 | テスト (Java / iPad) | 適合 |
|---|---|---|---|---|---|
| B-01 | 1 バッジ 1 票 (`You have already voted.`) | `VoteService.submit` → `votes.findByBadge` | `VoteService.submit` → `repo.vote(badge:)` | `duplicateBadge` / `testDuplicateBadge` | ✅ |
| B-02 | ELIGIBLE 判定 (`Exhibit ineligible for award`, キオスク 1 非表示) | `exhibits.findFirstAtOrAfter(...).isEligible()`; `kiosk.html th:if=isEligible()` | `repo.exhibitAtOrAfter`; `KioskView if e.isEligible` | `ineligibleExhibit`, `kioskHidesIneligible` / `testIneligibleExhibit`, `testKioskHidesOptions…` | ✅ |
| B-03 | ALWVOTE='N' → ENDOFCON | `VoteController.form` → `endofcon` | `VoteView.onAppear` → `.endOfCon` | `alwvoteN*` / `testAlwvote` | ✅ |
| B-04 | CHECKOK=4 で WRITE、失敗時同画面+入力保持 | `submit` checkOk カウンタ | 同 | `successfulVote*` / `testSuccessfulVoteCheckOk4` | ✅ |
| B-05 | エラー行は最後に失敗した検査 (バッジ→アワード→展示 / 重複→資格→展示→アワード) | 判定順を RPG と同一に実装 | 同 | `emptyAll…MustEnterExhibit` / `testEmptyExhibitLastErrorWins` | ✅ |
| B-06 | CMTID = 最終 + 1、VISIBLE='Y' | `GuestbookService.add` (`findLast`) | 同 (`lastComment`) | `newIdIsLastPlusOne` / `testGuestbookNewIdAndVisible` | ✅ |
| B-07 | 非表示コメント置換 | `read` → `ERRHNAME/ERRHCMT` | 同 | `hidden*` / `testReadHidden` | ✅ |
| B-08 | 帰属 (他展示 → `not part of this guestbook`, MM2024 全閲覧) | `read` | 同 | `foreign*`, `shared*` / `testReadForeign`, `testReadShared` | ✅ |
| B-09 | 存在しない ID → 次レコード / 末尾超 → 最終 | `findFirstAtOrAfter(...).or(findLast)` | `commentAtOrAfter ?? lastComment` | `highIdFallsBackToLast` / `testReadBeyondLast…` | ✅ |
| B-10 | LEARN ページ遷移 (F5/F8/F3, END で終了) | `LearnService.forward/back` | 同 | `learn*` / `testLearnNavigation` | ✅ |
| B-11 | キオスク終了パスワード (隠し 7, SETTINGS 先頭) | `KioskService.exitAllowed`, `KioskController.exit` | `KioskService.exitAllowed`, `AppModel.exitKiosk` | `kioskPassword*` / `testKiosk`, UI テスト | ✅ |
| B-12 | ENLRN400=0 → メニュー 2 非表示 | `kiosk.html th:if=isLearnEnabled()` | `KioskView if e.isLearnEnabled` | `kiosk*` / `testKiosk` | ✅ |
| B-13 | 展示 ID の固定 (LAUNCH≠MM2024 は保護) | `protectedExhibit`, `vote1.html readonly` | `protectedExhibit`, `.disabled(exhibitProtected)` | `ownerLaunchForcesExhibit` / 同名 | ✅ |
| B-14 | イースターエッグ DAVE | 再現なし | 再現なし | — | ➖ 仕様書で「移行では再現不要」 |
| B-15 | 数値項目 (RZ、未入力=0、桁数) | `VoteController.numeric`, `maxlength`, `inputmode=numeric` | `numeric(_:digits:)`, `.keyboardType(.numberPad)`, `onChange` 桁数制限 | `emptyBadge` 等 | ✅ |

## 4. 入力検証 (V-xx)

| ID | 検証 | Java 版 | iPad 版 | 適合 |
|---|---|---|---|---|
| V-01〜V-03 | バッジ/アワード/展示 ID 必須 (標識 40/42/41 で反転) | `Result.badgeErr/awardErr/exhibitErr` → `.field.error` | `Result.badgeErr/…` → `FieldRow(error:)` 赤枠 | ✅ |
| V-04〜V-07 | 重複 / 資格 / 展示存在 / アワード存在 | `submit` | `submit` | ✅ |
| V-08 | CHECKOK=4 → VOTEEND | `voteend.html` | `VoteEndView` | ✅ |
| V-09 | ALWVOTE → ENDOFCON | `endofcon.html` | `EndOfConView` | ✅ |
| V-10〜V-13 | 展示 ID / 名前 / コメント必須、展示存在チェックなし | `GuestbookService.add` | 同 | ✅ |
| V-14 | コメント ID 必須 (`Must enter CommentID`) | `GuestbookController.read` | `ReadCommentView.submit` | ✅ |
| V-15 | `Currently hosting nnnn comments` | `totalComments()` | `totalComments` | ✅ |
| V-16〜V-18 | 非表示 / 他展示 / 存在しない ID | `read` | `read` | ✅ |
| V-19〜V-21 | LEARN F5 / F8 / F3 | `LearnController` action fwd/back/exit | `LearnView` F5/F8/F3 ボタン | ✅ |

## 5. メッセージ (M-xx)

| ID | 文言 | Java 版 | iPad 版 | 適合 |
|---|---|---|---|---|
| M-02〜M-05, M-07〜M-09 | ADDVOTE エラー行 | `Messages.java` (同一文字列) | `Messages` (Kit, 同一文字列) | ✅ |
| M-10〜M-13 | ADDGBCMT / READGBCMT エラー行 | 同 | 同 | ✅ |
| M-14, M-15 | Name Hidden / hidden by an admin / not part of this guestbook | 同 | 同 | ✅ |
| M-16 | VOTEEND 本文 | `voteend.html` (アスキーアートは大見出しに置換) | `VoteEndView` | ✅ |
| M-17 | ENDOFCON 本文 | `endofcon.html` | `EndOfConView` | ✅ |
| M-19 | ENDCMT 本文 (+ Comment ID 表示) | `endcmt.html` | `EndCommentView` | ✅ |
| M-20 | ADMPSWRD 本文 | `admpswrd.html` | `AdmPswrdView` | ✅ |
| M-21 | NTRSTIT 本文 | `navigate.html` | `NavigateView` | ✅ |
| M-01, M-06, M-18 | 未使用定数 / 未使用画面 | 再現なし | 再現なし | ➖ 元プログラム未使用 |

## 6. 元画面構造台帳 (L-xx-yy) ↔ モダン UI

### L-01 VCFMAIN
| ID | 台帳 | Java `vcfmain.html` | iPad `MainMenuView` | 適合 |
|---|---|---|---|---|
| L-01-01 | 見出し `AS/400 DEMO MENU` / `WELCOME TO...` / `VINTAGE COMPUTER FESTIVAL 2024` | `h1.screen-title` + `.screen-subtitle` | `ScreenHeader(title:subtitle:)` | ✅ |
| L-01-02 | Start Here 1〜4 (1 のみ有効) | `menu-group` 1 有効 / 2–4 disabled | `group("Start Here")` 1 有効 / 2–4 `enabled:false` | ✅ |
| L-01-03 | Office Tasks 5〜7 (無効) | disabled | `enabled:false` | ✅ |
| L-01-04 | Entertainment 8〜10 (無効) | disabled | `enabled:false` | ✅ |
| L-01-05 | Exhibit Tasks 11/12/13 (クリックで起動) | `menu-item` → hidden option + submit (`vcf.js`) | `MenuItem` → `selectMain` | ✅ |
| L-01-06 | System Tasks 80 | 同 | 同 | ✅ |
| L-01-07 | Administration 90 (無効表示可) | `<h3>Administration</h3>` 90 disabled | `group("Administration")` 90 `enabled:false` | ✅* (iPad: 当初 System Tasks 内に配置 → 独立グループへ修正) |
| L-01-08 | 説明文 (番号入力の案内) | 保持 | 保持 | ✅ |
| L-01-09 | `Type Number, then press ENTER` + 番号入力欄 | `<input name=option maxlength=2>` + 「選択 (ENTER)」 | `OptionLine(maxLength:2)` + 「選択 (ENTER)」 | ✅ |
| L-01-10 | 遷移 1/11/12/13/80 | `MainMenuController.select` switch | `AppModel.selectMain` switch | ✅ |

### L-02 NTRSTIT
| ID | 台帳 | Java `navigate.html` | iPad `NavigateView` | 適合 |
|---|---|---|---|---|
| L-02-01 | 見出し 2 行 | 保持 | 保持 | ✅ |
| L-02-02 | 本文 4 項目 (順) | 保持 | 保持 | ✅ |
| L-02-03 | `Got all that?` → 続行 (ENTER) | `続行 <kbd>ENTER</kbd>` | `FKeyButton("続行","ENTER")` | ✅ |
| L-02-04 | 11/12/13, キオスク 1/3/4 の直後に表示 | `redirect:/navigate?next=` | `Route.navigate(next:)` | ✅ |

### L-03 VOTE1
| ID | 台帳 | Java `vote1.html` | iPad `VoteView` | 適合 |
|---|---|---|---|---|
| L-03-01 | `VINTAGE COMPUTER FESTIVAL` / `NOMINATE EXHIBIT FOR AWARD` | 保持 | 保持 | ✅ |
| L-03-02 | 入力 1 `First, type your SFGE Badge Number` 4 桁 RZ | `inputBadge` maxlength 4, `.error` 反転相当 | `inputBadge` 4 桁, `FieldRow(step:1, error:)` | ✅ |
| L-03-03 | 入力 2 `Second, type the Exhibit ID you are nominating` 9 文字, 保護 | `inExhb` readonly (LAUNCH≠MM2024) | `inExhb` `.disabled` | ✅ |
| L-03-04 | 入力 3 `Third, type the Award ID you are selecting` 3 桁 RZ | `inputAward` maxlength 3 | `inputAward` 3 桁 | ✅ |
| L-03-05 | エラー行 (赤) | `.errline` | `ErrorLine` | ✅ |
| L-03-06 | `Available Awards for This Year:` `001. タイトル - 説明` | AWARDDB から `%03d.` 表示 | `Award.number` (`%03d`) | ✅ (文言はデータ: PUB400 参照データに一致) |
| L-03-07 | `You may only vote for ONE award. Choose your nomination carefully!` (赤) | 保持 | 保持 | ✅ |
| L-03-08 | `TAB = Switch Fields`, 版数 | legend 表示 (版数は任意のため省略) | legend 表示 | ✅ |
| L-03-09 | F5 Submit / F12 Cancel → 「送信 (F5)」「キャンセル (F12)」、ENTER も送信 | 両ボタン + `vcf.js` で物理 F5/F12/ENTER | 両ボタン (ENTER = onSubmit, Esc = F12) | ✅ |
| L-03-10 | 入力順 バッジ → 展示 → アワード | DOM 順 | View 順 | ✅ |
| L-03-11 | 成功→VOTEEND / 失敗→同画面+入力保持 / F12→呼出元 | `VoteController.submit`, `Nav.returnTo` | `submit()`, `returnToCaller()` | ✅ |

### L-04 VOTEEND / ENDOFCON
| L-04-01 | `THANK YOU FOR VOTING!` + M-16 + 「メニューへ戻る (ENTER)」 | `voteend.html` | `VoteEndView` | ✅ |
| L-04-02 | `SORRY!` + M-17 + 「終了 (ENTER)」 | `endofcon.html` | `EndOfConView` | ✅ |

### L-05 EXHBMENU
| ID | 台帳 | Java `kiosk.html` | iPad `KioskView` | 適合 |
|---|---|---|---|---|
| L-05-01 | `WELCOME TO...` → 展示タイトル | 保持 | 保持 | ✅ |
| L-05-02 | `HOSTED BY 名 OF 市 州` | 保持 | 保持 | ✅ |
| L-05-03 | 説明 OUTDESC | 保持 | 保持 | ✅ |
| L-05-04 | 1 (ELIGIBLE) / 2 (ENLRN400) / 3 / 4 | `th:if` 条件表示 | `if` 条件表示 | ✅ |
| L-05-05 | `Select Menu Option, Press ENTER:` 1 桁 | `<input name=option maxlength=1>` | `OptionLine(maxLength:1)` | ✅ |
| L-05-06 | 隠し 7 → ADMPSWRD | `case "7"` | `case "7"` | ✅ |
| L-05-07 | F キーなし、番号ボタン + 入力欄 + 「選択 (ENTER)」 | 同 | 同 | ✅ |
| L-05-08 | 遷移 1→NTRSTIT→ADDVOTE, 2→LRN400, 3/4→NTRSTIT→…; 終了後この画面へ | `Nav.RETURN_TO=/kiosk/{x}` | `returnToCaller()` (path 内の `.kiosk` まで pop) | ✅ |

### L-06 ADMPSWRD
| L-06-01 | 本文 2 行 | `admpswrd.html` | `AdmPswrdView` | ✅ |
| L-06-02 | INPWD 9 文字 非表示 | `type=password maxlength=9` | `SecureField` | ✅ |
| L-06-03 | ENTER のみ (「サインオフ (ENTER)」) 一致→終了 / 不一致→メニュー | `KioskController.exit` | `AppModel.exitKiosk` | ✅ (F12 戻るボタンを補助的に追加) |

### L-07 ADDCMT
| ID | 台帳 | Java `addcmt.html` | iPad `AddCommentView` | 適合 |
|---|---|---|---|---|
| L-07-01 | `VINTAGE COMPUTER FESTIVAL` / `GUESTBOOK/400 - ADD COMMENT` | 保持 | 保持 | ✅ |
| L-07-02 | 入力 1 `Your Name` 16 文字 | `inName` maxlength 16 | `inName` 16 | ✅ |
| L-07-03 | 入力 2 `Exhibit ID` 9 文字 保護 | `inId` readonly | `inId` disabled | ✅ |
| L-07-04 | 入力 3 `Comment` 200 文字 | `inCmt` textarea maxlength 200 | `inCmt` (axis .vertical) 200 | ✅ |
| L-07-05 | エラー行 | `.errline` | `ErrorLine` | ✅ |
| L-07-06 | 「送信 (F5)」「キャンセル (F12)」 | 同 | 同 | ✅ |
| L-07-07 | 入力順 名前 → 展示 ID → コメント | DOM 順 | View 順 | ✅ |
| L-07-08 | 成功→ENDCMT / 失敗→同画面 / F12→呼出元 | 同 | 同 | ✅ |

### L-08 ENDCMT
| L-08-01 | `THANKS FOR COMMENTING!` + `Your Comment ID Is:` | `endcmt.html` (ID を表示) | `EndCommentView` (ID を表示) | ✅ (元は ID 出力項目なし → 採番結果を表示、仕様書 8 章 10 項) |
| L-08-02 | 「終了 (ENTER)」→ 呼出元 | 同 | 同 | ✅ |

### L-09 READCMT
| ID | 台帳 | Java `readcmt.html` | iPad `ReadCommentView` | 適合 |
|---|---|---|---|---|
| L-09-01 | `VINTAGE COMPUTER FESTIVAL` / `GUESTBOOK/400 - Read a Comment` | 保持 | 保持 | ✅ |
| L-09-02 | 入力 1 `Enter a Comment ID:` 4 桁 RZ | `inCmtId` maxlength 4 | `inCmtId` 4 | ✅ |
| L-09-03 | `Currently hosting nnnn comments and counting.` | 保持 | 保持 | ✅ |
| L-09-04 | エラー行 M-13 | `.errline` | `ErrorLine` | ✅ |
| L-09-05 | 出力 OUTNAME(11) `says to:` OUTTITLE / OUTCMT | 保持 (11 桁切り) | 保持 (`left(11)`) | ✅ |
| L-09-06 | 注意文 `Is this comment inappropriate? …` | 保持 | 保持 | ✅ |
| L-09-07 | 「送信 (F5)」「キャンセル (F12)」 | 同 | 同 | ✅ |
| L-09-08 | F5 → 同画面に結果 (繰返し) / F12 → 呼出元 | 同 | 同 | ✅ |

### L-10 LRN400
| L-10-01 | `LEARN/400` / `Page nnnn` | `lrn400.html` | `LearnView` | ✅ |
| L-10-02 | 本文 OUTCONTENT | 同 | 同 | ✅ |
| L-10-03 | 「戻る (F3)」「進む (F5)」「前へ (F8)」 | 3 ボタン + 物理キー | 3 ボタン | ✅ |
| L-10-04 | F5 次 (END で終了) / F8 前 / F3 終了 → 呼出元 | `LearnController` | `LearnView` | ✅ |

## 7. モダン UI 方針 (仕様書 10 章) の充足

| 方針 | Java 版 | iPad 版 |
|---|---|---|
| 現代的配色・余白・タイポグラフィ、黒背景緑文字の模写をしない | `vcf.css` (青系ヘッダー・白カード・角丸・システムフォント) | `Theme.swift` (同系色、SF フォント、カード) |
| レスポンシブ / タッチ・クリック | CSS グリッド、640px ブレークポイント (`13_vote1_mobile.png`) | iPad 縦横対応 `LazyVGrid(.adaptive)`、タップ操作 |
| 遷移フロー・メニュー階層 VCFMAIN → 各機能を同一に | URL 階層 + `Nav.returnTo` | `NavigationStack(path:)` + `Route` |
| 入力項目の順序・グルーピングを DDS どおり | 各テンプレートの DOM 順 (番号ステップ付き) | 各 View の `FieldRow(step:)` 順 |
| F キー番号を踏襲しボタンにラベル併記 | `<kbd>F5</kbd>` 等 + `vcf.js` 物理キー | `FKeyButton(key:)` |
| メニューは番号併記 + クリック/タップ + 番号入力欄 | `.menu-item .num` + `option` 入力 | `MenuItem(number:)` + `OptionLine` |
| 画面タイトル・見出し文言の保持 | `h1.screen-title` 等 | `ScreenHeader` |

## 8. フェーズ4 相違一覧と対処

| # | 対象 | 相違 | 対処 | 状態 |
|---|---|---|---|---|
| 1 | Java `kiosk.html` (L-05-04) | Thymeleaf の数値プロパティ比較でオプション 1/2 が常に非表示 | `th:if="${exhibit.isEligible()}"` / `isLearnEnabled()` へ修正 | 修正済 (WebFlowTest で検証) |
| 2 | Java `KioskService.exhibitFor` (F-07) | SETLL/READ 相当 (`findFirstAtOrAfter`) で別展示に解決し得た | CHKPARM は存在するプロファイルで起動される前提のため `findById` (完全一致) へ修正 | 修正済 |
| 3 | iPad `MainMenuView` (L-01-07) | `90. Start Admin Menu` を System Tasks 内に配置 | 台帳どおり `Administration` グループを独立 | 修正済 |
| 4 | iPad `MenuItem` (L-01-02〜04) | 無効項目の番号ラベルが薄すぎて判読不可 (番号併記の要件) | 無効時は灰色バッジで番号を表示 | 修正済 |
| 5 | 両版 (F-08/F-09) | 印刷 (PRTLSTVOTE/PRTLSTCMT) はスプール出力で Web/iPad に等価物なし | DB 一覧 (`/api/db/*`, sqlite) で監査可能とし、仕様書 8 章 9 項に明記 | 意図的差異 |
| 6 | 両版 (L-08-01) | ENDCMT に採番結果の出力項目がない (元は表示せず) | 採番 ID を表示 (仕様書 8 章 10 項、クロス検証で ID 比較に使用) | 意図的差異 |

上記以外の全項目 (F/D/B/V/M/L) は Java 版・iPad 版ともに一致を確認した (Java: `mvn test` 29 件、iPad: `swift test` 19 件 + XCUITest 3 件)。
