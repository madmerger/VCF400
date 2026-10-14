---
title: "VCF/400 元画面構造の台帳 (Legacy Screen Ledger)"
date: "2026-09-07"
---

# VCF/400 元画面構造の台帳 (Legacy Screen Ledger)

| 項目 | 内容 |
|---|---|
| 対象 | VCF/400 の元 IBM i 5250 画面構造、項目順、入力順、見出し、ファンクションキー、メニュー番号 |
| 根拠 DDS メンバー | `QSDASRC/*`、`QMNUSRC/VCFMAIN.mnudds` |
| 解析日 | 2026-09-07 |
| ID 体系 | **L-nn** 元画面構造の台帳項目 |

フェーズ3 の Java / iPad 実装が **1 対 1 で対応させる正 (source of truth)**。「項目順」は DDS の行・桁位置に基づく画面上の並び、「入力順」はカーソル移動順 (TAB 順 = 行・桁の昇順)。モダン UI では色・配置・フォントは変更してよいが、**項目の順序・グルーピング・見出し文言・ファンクションキーの役割と番号・メニュー番号**は変更しない。

## 1. L-01 VCFMAIN — AS/400 DEMO MENU (メニュー)

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

## 2. L-02 INTERSCR/INTERTEST — VCF/400 - How to Navigate

| # | 項目 | 元画面 |
|---|---|---|
| L-02-01 | 見出し | `VCF/400 - How to Navigate` / `Before you start, here is how to navigate:` |
| L-02-02 | 本文 (順) | `Use ARROW KEYS or TAB KEY to move between fields.` → `Use F5 or ENTER to perform your action.` → `Use F12 to quit at any time.` → `Keyboard not responding? If you see X II in the left corner, press RIGHT CTRL to continue.` |
| L-02-03 | 操作 | `Got all that?` `Press ENTER to continue.` → ENTER のみ (**続行 (ENTER)**) |
| L-02-04 | 遷移 | VCFMAIN 11/12/13 とキオスク 1/3/4 の直後に必ず表示 → 次画面へ |

## 3. L-03 VOTESCR/VOTE1 — NOMINATE EXHIBIT FOR AWARD

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

## 4. L-04 VOTESCR/VOTEEND, ENDOFCON — 投票結果画面

| # | 画面 | 元画面 | ボタン |
|---|---|---|---|
| L-04-01 | VOTEEND | アスキーアート `THANK YOU FOR VOTING!` + M-16 本文 + `Press ENTER to return to the main menu.` | **「メニューへ戻る (ENTER)」** |
| L-04-02 | ENDOFCON | アスキーアート `SORRY!` + M-17 本文 + `Press ENTER to exit.` | **「終了 (ENTER)」** |

## 5. L-05 EXHBMENUSC/MENU — WELCOME TO... (キオスク)

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

## 6. L-06 EXHBMENUSC/ADMPSWRD — キオスク終了

| # | 項目 | 元画面 |
|---|---|---|
| L-06-01 | 本文 | `Are you sure you want to exit the kiosk?` `Type the Administrator password, press ENTER to sign off` |
| L-06-02 | 入力 | 7 行 5 桁開始 INPWD (9 文字, 非表示推奨) |
| L-06-03 | 操作 | ENTER のみ → 一致でキオスク終了、不一致でキオスクメニューへ (**「サインオフ (ENTER)」**) |

## 7. L-07 GUESTBKSCR/ADDCMT — GUESTBOOK/400 - ADD COMMENT

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

## 8. L-08 GUESTBKSCR/ENDCMT — THANKS FOR COMMENTING!

| # | 項目 | 元画面 |
|---|---|---|
| L-08-01 | 本文 | アスキーアート `THANKS FOR COMMENTING!` + `Thank you for commenting on this exhibit! Your Comment ID Is:` (ID 出力項目なし) + `Press ENTER to exit.` |
| L-08-02 | 操作 | **「終了 (ENTER)」** → 呼び出し元 |

## 9. L-09 GUESTBKSCR/READCMT — GUESTBOOK/400 - Read a Comment

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

## 10. L-10 LRN400SCR/MAIN — LEARN/400

| # | 項目 | 元画面 |
|---|---|---|
| L-10-01 | 見出し | 1 行 `LEARN/400` / 1 行 70 桁 `Page` / OUTPAGENBR (1 行 76 桁) |
| L-10-02 | 本文 | 3〜21 行 OUTCONTENT (1500) |
| L-10-03 | **ファンクションキー** | 23 行 `Cmd3/F3 = Exit  Cmd5/F5 = Forwards  Cmd8 / F8 = Backwards` → **「戻る (F3)」(終了)「進む (F5)」「前へ (F8)」** |
| L-10-04 | 遷移 | F5 次ページ (END ページで終了), F8 前ページ, F3 終了 → 呼び出し元 |

## 11. 台帳サマリ (ファンクションキー・メニュー番号)

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

## 付録: DDS レコード様式と行/桁の対応

| ID | DSPF メンバー + RCDFMT | DDS ソース |
|---|---|---|
| L-01 | `VCFMAIN` + `VCFMAIN` | `QMNUSRC/VCFMAIN.mnudds` |
| L-02 | `INTERSCR` + `INTERTEST` | `QSDASRC/INTERSCR.dspf` |
| L-03 | `VOTESCR` + `VOTE1` | `QSDASRC/VOTESCR.dspf` |
| L-04 | `VOTESCR` + `VOTEEND`, `ENDOFCON` | `QSDASRC/VOTESCR.dspf` |
| L-05 | `EXHBMENUSC` + `MENU` | `QSDASRC/EXHBMENUSC.dspf` |
| L-06 | `EXHBMENUSC` + `ADMPSWRD` | `QSDASRC/EXHBMENUSC.dspf` |
| L-07 | `GUESTBKSCR` + `ADDCMT` | `QSDASRC/GUESTBKSCR.dspf` |
| L-08 | `GUESTBKSCR` + `ENDCMT` | `QSDASRC/GUESTBKSCR.dspf` |
| L-09 | `GUESTBKSCR` + `READCMT` | `QSDASRC/GUESTBKSCR.dspf` |
| L-10 | `LRN400SCR` + `MAIN` | `QSDASRC/LRN400SCR.dspf` |
