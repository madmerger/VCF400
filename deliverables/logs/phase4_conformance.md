# Phase 4 実装適合確認

**確認日:** 2026-09-07  
**正本:** `deliverables/spec/VCF400_screen_ledger.md`  
**対象:** Java templates / iPad SwiftUI views

## 結果

| Ledger row | Java | iPad | Result | Fix |
|---|---|---|---|---|
| L-01 VCFMAIN | `vcfmain.html` has `AS/400 DEMO MENU`; menu links are `1`, `11`, `12`, `13`, `80`, `90` | `MainMenuView.swift` has the same title and menu identifiers | ✅ 一致 | なし |
| L-02 INTERTEST | `navigate.html` has `VCF/400 - How to Navigate`; F3/F5/F8/ENTER controls are labelled | `MainMenuView.swift` / navigation views use the same title and F-key labels | ✅ 一致 | なし |
| L-03 VOTE1 | `vote1.html` has `NOMINATE EXHIBIT FOR AWARD`; fields are badge → exhibit → award; awards iterate from `awards` and render `%03d.` | `VoteView.swift` uses FieldRow steps 1–3 in badge → exhibit → award order and dynamic award rows | ✅ 一致 | なし |
| L-04 VOTEEND / ENDOFCON | `voteend.html` and `endofcon.html` preserve the ledger titles/messages and single result/error line | `VoteView.swift` / `RootView.swift` preserve the corresponding result screens and single error line | ✅ 一致 | なし |
| L-05 EXHB MENU | `kiosk.html` renders options 1/2/3/4 and the `WELCOME TO...` kiosk surface | `MainMenuView.swift` / kiosk views render options 1/2/3/4 and `WELCOME TO...` | ✅ 一致 | なし |
| L-06 ADMPSWRD | `admpswrd.html` preserves the administration password prompt and F-key behavior | `LearnKioskViews.swift` preserves the password prompt and ENTER behavior | ✅ 一致 | なし |
| L-07 ADDCMT | `addcmt.html` labels fields name → exhibit → comment and uses `送信 (F5)` / `キャンセル (F12)` | `GuestbookViews.swift` uses the same order and labels | ✅ 一致 | なし |
| L-08 ENDCMT | `endcmt.html` preserves the success message and `続行 (ENTER)` | `GuestbookViews.swift` preserves the success message and ENTER action | ✅ 一致 | なし |
| L-09 READCMT | `readcmt.html` uses `Currently hosting N comments and counting.` and the F3/F5/F12 controls | `GuestbookViews.swift` uses the same total-count wording and controls | ✅ 一致 | なし |
| L-10 LRN400 | `lrn400.html` renders the page number and navigation controls | `LearnKioskViews.swift` renders `Page N` and the same navigation controls | ✅ 一致 | なし |

## 確認項目

- 画面タイトルは ledger の文言を維持している。特に `NOMINATE EXHIBIT FOR AWARD`、`GUESTBOOK/400 - ADD COMMENT`、`GUESTBOOK/400 - Read a Comment`、`LEARN/400`、`AS/400 DEMO MENU`、`VCF/400 - How to Navigate`、`WELCOME TO...` を確認した。
- 投票入力順は badge → exhibit → award。
- Guestbook 追加入力順は name → exhibit → comment。
- 必須 F-key 表示は `送信 (F5)`、`キャンセル (F12)`、`戻る (F3)`、`進む (F5)`、`前へ (F8)`、`続行 (ENTER)`。
- VCFMAIN のメニュー番号は 1 / 11 / 12 / 13 / 80 / 90。
- Kiosk のメニュー番号は 1 / 2 / 3 / 4。隠し option 7 は既存の kiosk routing と cross-validation flow で維持されている。
- エラーは単一の error-line 表示領域に集約されている。
- AWARDDB は Java の `awards` 反復と iPad の動的 award rows で表示し、番号は 3 桁（001. / 002.）。
- READCMT の総数表示は `Currently hosting N comments and counting.`。
- LRN400 はページ番号を表示する。

## 実装差分・修正

今回の ledger 照合では新たな実装場所の変更を要する逸脱は見つからなかった。したがって Java / iPad のソース修正は行わず、`VCF400_traceability.md` と PDF の再生成も不要だった。既存の Phase 4 修正（Java kiosk eligibility / exact exhibit lookup、iPad Administration 配置、disabled menu number 表示）はそのまま確認できる。

## 実行済み検証

- Java: `cd java && mvn -q test` — pass。
- iPad Kit: `cd ipad/VCF400Kit && swift test` — 19 tests, 0 failures。
- iPad UI bundle: `xcodebuild ... build-for-testing` — `** TEST BUILD SUCCEEDED **`。
