# VCF400 / PUB400 5250 E2E 実行レポート

## 実行範囲と判定

既存の `test_scenarios.md` の TS-01〜TS-10を、PUB400 の ASHIBATA2 にある実RPGアプリへ5250接続して実行した。Konsoleに実tn5250のPTY出力を同期表示し、同一セッションから画面テキストを採取した。モック・別実装・ローカルDBは使用していない。

機能判定は **10シナリオ PASS / 0 FAIL**。56件のステップ・補助確認を `results.json` に保存した。期待値の原本は変更していない。

| シナリオ | 結果 | 確認した実際の動作 |
|---|---|---|
| TS-01 メニュー・遷移 | PASS | 必須メニュー文言、11→操作説明→投票画面、F12で復帰 |
| TS-02 投票の入力必須 | PASS | 空欄・アワード空・バッジ空・アワード0のエラー、展示ID保護、投票DB2件維持 |
| TS-03 投票正常登録 | PASS | VOTEENDと記録完了文言、Enter復帰、(7001,1,ASHIBATA)追加・計3件 |
| TS-04 投票の業務検証 | PASS | 重複、アワード不在、資格外、展示不在、複数違反時のエラー優先順位、書込みなし |
| TS-05 投票期間終了 | PASS | ALWVOTE=Nで投票入力画面を飛ばし期間終了画面、Enter復帰、Y復旧・DB2件維持 |
| TS-06 ゲストブック記入 | PASS | 名前・本文の必須検証、完了画面、CMTID=3/Y/ASHIBATA/Devin Tester/E2E test runを実DBで確認 |
| TS-07 ゲストブック閲覧 | PASS | 総数0002、ID空のエラー、0001の名前・タイトル・本文、9999からID2へのフォールバック |
| TS-08 可視性・帰属 | PASS | 非表示の代替文言、他展示の名前・タイトルを空欄にし本文非公開、MM2024は閲覧可能 |
| TS-09 LEARN/400 | PASS | F5で1→2、F8で2→1、ENDページで終了、再起動後F3でも終了 |
| TS-10 キオスク | PASS | 通常は1〜4、NOVOTEは3/4のみ、禁止選択肢1は無効、誤パスワード継続・正パスワード終了 |

## 観察事項・制約

- **文字グリフの差異:** tn5250 `map=37` の表示には `!→|`、ASCIIアートの縦棒・バックスラッシュ等のグリフ化けが見られた。実画面テキストとPNGは未修正のまま保存している。機能判定時のみ既知の `!→|` を正規化した。
- TS-03は当初グリフ差をFAILと注記したが、リードからの判定方針に従い、ASCIIアートのグリフ差を「観察事項（機能影響なし、コードページ差）」とし、VOTEENDの画面識別・業務テキスト・DBを根拠にPASSへ訂正した。録画には当初注記と訂正の両方が含まれる。TS-05/06のASCIIアートも同じ扱いであり、**グリフが完全一致することを検証済みとはしていない**。
- Konsole起動時、既定のDISPLAYにXサーバーがなかったためXvfbを起動した。GUI最大化後にCtrl-L再描画し、全24行が可視であることを確認してから録画を開始した。
- 最終 `reset` 後のDB dumpは、TS-02開始前のdumpとバイト単位で一致した。投票2件・コメント2件、ALWVOTE=Yを確認。
- 最後に5250へ `signoff` + Enterを送信し、tn5250・GUI制御プロセスの終了を確認した。終了後の制御ソケット読み取りでは接続終了エラーとなったため、**サインオフ後の最終画面は取得できていない**。これはTS-01〜TS-10の機能判定には含めていない。
- シナリオ原本の期待値・手順に修正を要する不整合は発見していない。対象外の管理・印刷機能やすべてのRPG分岐を網羅した試験ではない。

## DB証跡

- `db_TS-02_before.txt` / `db_TS-02_after.txt`: 投票2件のまま。
- `db_TS-03_before.txt` / `db_TS-03_after.txt`: 投票2→3件、追加行を確認。
- `db_TS-04_before.txt` / `db_TS-04_after.txt`: 投票2件のまま。
- `db_TS-05_before.txt` / `db_TS-05_after.txt`: 投票2件のまま、設定をYへ復旧。
- `db_TS-06_before.txt` / `db_TS-06_after.txt`: コメント2→3件、ID3の各属性を確認。
- `db_final_baseline.txt`: 最終ベースライン復旧後のdump。
- `reset_TS-01.txt`〜`reset_TS-10.txt` / `reset_final.txt`: 各シナリオ前および終了時の再投入出力。

DB操作は提供された `tools/pub400_db.sh` を使用した。このヘルパーの出力フィルターは `pub400.com` を含む行も除去するため、説明文に同ドメインを含むEXHBDBの行はdump上では省略される。投票・コメント・設定の検証対象行は保存されている。

## 主要画面

各画像は実スクリーンショットの2列レイアウトである。アプリ画面は改変していない。

### 投票の業務検証

![TS-04 重複・アワード不在・資格外・展示不在・優先順位](https://devin-gtm.devinenterprise.com/attachments/1730971c-2e25-4658-a504-e3f0994e3b76/TS-04_business_errors.png)

### ゲストブック記入

![TS-06 必須検証と正常登録](https://devin-gtm.devinenterprise.com/attachments/24fb0501-c949-45a6-b451-16fa05d395a0/TS-06_guestbook.png)

### 可視性・帰属制御

![TS-08 非表示・他展示・共用端末](https://devin-gtm.devinenterprise.com/attachments/6c27ff04-27d3-4537-869a-ba99c21d1d55/TS-08_privacy.png)

### キオスクの機能制限

![TS-10 通常展示・資格外展示・終了認証](https://devin-gtm.devinenterprise.com/attachments/a6c47e07-c532-45f3-b974-c5e586709c4b/TS-10_kiosk.png)

## 成果物

`deliverables/pub400_e2e/` を基準とする。

- `results/results.json`: シナリオ・ステップ別の期待値、実際、PASS/FAIL、画面証跡参照。
- `results/screens/TS-xx_stepN.txt`: 5250画面テキスト56件（補助確認を含む）。
- `screenshots/TS-xx_stepN.png`: 対応する実画面PNG。
- `screenshots/TS-04_business_errors.png` / `TS-06_guestbook.png` / `TS-07_read.png` / `TS-08_privacy.png` / `TS-09_learn.png` / `TS-10_kiosk.png`: 主要画面の2列一覧。
- `video/pub400_e2e.mp4`: TS-01〜TS-10の日本語字幕付き編集済み録画（約47秒、待機を自動短縮）。
- `video/pub400_e2e_annotations.json`: 録画アノテーション。
- この文書: `results/test_report.md`。

[録画を開く](https://devin-gtm.devinenterprise.com/attachments/06a58555-da62-4593-a3d2-6dd8121d9528/pub400_e2e.mp4)
