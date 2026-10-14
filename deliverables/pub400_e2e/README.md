# deliverables/pub400_e2e — PUB400 5250 E2E 証跡

madmerger/VCF400 の master ソースを PUB400 ASHIBATA2 へ再ビルドし、5250 実端末で
10 基本機能シナリオ (TS-01〜TS-10) を実行した証跡一式。**全 10 シナリオ PASS**。

## 構成

- `VCF400_PUB400_E2E_test_report.md` / `.pdf` — 統合テストレポート (本ディレクトリの要約)
- `test_scenarios.md` — シナリオ原本 (期待値・手順)
- `results/` — results.json (全ステップの期待/実際/判定)、screens/ (5250 画面テキスト 56 件)、db_*.txt / reset_*.txt (DB 証跡)、test_report.md (実行時レポート)
- `screenshots/` — 実画面 PNG 63 枚 (主要シーンは 2 列合成版あり)
- `video/pub400_e2e.mp4` — 日本語字幕付き編集済み録画
- `build/` — build.log (全コンパイルリスティング)、objects_after.txt (DSPOBJD)、seed_dump.txt、pub400_BUILDVCF.clle、pub400_listings/、5250_vcfmain.log
- `tools/` — pub400_build.sh (ソース再ビルド)、pub400_db.sh (DB リセット/ダンプ)、pub400_seed.sql、make_report.py (レポート再生成)

## 再実行手順

```sh
export PUB400_LOGIN=... PUB400_PW=...        # シークレット (値はコミットしない)
./tools/pub400_build.sh                      # 1) ソースを ASHIBATA1 へ FTP → BUILDVCF CL で ASHIBATA2 にビルド
./tools/pub400_db.sh reset                   # 2) ベースライン再シード
./tools/pub400_db.sh dump                    #    DB 確認 (sql "<SQL>" で任意 SQL)
# 3) 5250: tn5250 pub400.com でサインイン → CHGLIBL → GO ASHIBATA2/VCFMAIN → test_scenarios.md を実行
python3 tools/make_report.py                 # 4) 統合レポート MD+PDF 再生成 (results/ と build/ から読むだけ)
```

接続先は ASHIBATA1 (ソース) / ASHIBATA2 (オブジェクト・データ) のみ。RSTLIB は権限外のため使用しない。
