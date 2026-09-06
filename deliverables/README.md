# VCF/400 検証・仕様化・マイグレーション成果物一覧

| 区分 | 成果物 | パス |
|---|---|---|
| フェーズ0 | ソース棚卸し・前提確認 | `deliverables/logs/phase0_inventory.md` |
| フェーズ1 | PUB400 復元試行 (SAVF 転送 / DSPSAVF / RSTLIB 権限エラー) ログ | `deliverables/logs/phase1_02_ftp_savf.log`, `phase1_03_dspsavf.log`, `phase1_04_rstlib.log` |
| フェーズ1 | ソースビルド (CRTPF/CRTDSPF/CRTBNDRPG/CRTCLPGM → `ASHIBATA2`) スクリプトとログ | `deliverables/logs/pub400_build.sh`, `pub400_BUILDVCF.clle`, `pub400_vcfbuild_remote.sh`, `phase1_05_build.log`, `pub400_listings/` |
| フェーズ1 | VCFMAIN からの動作確認 (5250 画面・入力・DB 書込) ログとレポート | `deliverables/logs/phase1_06_runtime_5250.log`, `phase1_07_runtime_alwvote.log`, `phase1_report.md` |
| フェーズ1 | tn5250 自動操作ドライバ | `deliverables/logs/tn5250_driver.py` |
| フェーズ2 | 仕様書 (機能概要・画面フロー・検証ルール・DB スキーマ・業務ルール・元画面構造の台帳) | `deliverables/spec/VCF400_spec.md` / `VCF400_spec.pdf` |
| フェーズ3 | Java Web 版 (Spring Boot 3 + Thymeleaf + H2 DB2 モード) | `java/` (`java/README.md`) |
| フェーズ3 | iPad 版 (Swift/SwiftUI + SQLite、XcodeGen) | `ipad/` (`ipad/README.md`) |
| フェーズ3/4 | トレーサビリティ表 (仕様書 F/V/B/D/M/L 項目 ↔ Java / iPad 実装) と適合性検証結果 | `deliverables/spec/VCF400_traceability.md` / `VCF400_traceability.pdf` |
| フェーズ3/4 | Java / iPad 画面スクリーンショット | `deliverables/screenshots/java/`, `deliverables/screenshots/ipad/` |
| フェーズ5 | 共通テストケース (37 ケース) | `deliverables/tests/cases.json` |
| フェーズ5 | 3 環境ランナー・比較・レポート生成 | `deliverables/tests/run_all.sh`, `run_pub400.py`, `run_java.js`, `run_ipad.sh` (+ `ipad/VCF400UITests/CrossValidationUITests.swift`), `compare.py`, `report.py`, `pub400_db.py` |
| フェーズ5 | 実測結果 (JSON) と実行ログ | `deliverables/tests/results/pub400.json`, `java.json`, `ipad.json`, `compare.json`, `run_all.log`, `pub400_5250.log`, `java_run.log`, `ipad_run.log` |
| フェーズ5 | 1 回目 (36/37) の結果・ログ (ルート原因分析用) | `deliverables/tests/results/run1/` |
| フェーズ5 | E2E クロス検証の録画 (1 本) | `deliverables/video/VCF400_cross_validation.mp4` |
| フェーズ6 | テスト結果レポート | `deliverables/tests/VCF400_test_report.md` / `VCF400_test_report.pdf` |
| ツール | Markdown → PDF 変換 | `deliverables/tools/md2pdf.py` |

## フェーズ5 の再実行手順

```sh
# 前提: PUB400_LOGIN / PUB400_PW (環境変数), tn5250 (~/.local/bin), Java 17 + Maven, Xcode + XcodeGen,
#       Playwright (NODE_PATH=~/pwtools/node_modules), ffmpeg
(cd java && mvn -q spring-boot:run &)                     # http://localhost:8080
cd deliverables/tests
./run_all.sh                                              # 録画開始 → PUB400 → Java → iPad → compare → 録画停止
python3 report.py                                         # VCF400_test_report.md
python3 ../tools/md2pdf.py VCF400_test_report.md VCF400_test_report.pdf
```

`NO_RECORD=1 ./run_all.sh` で録画なし、各ランナーは `python3 run_pub400.py --only CV-01,CV-04` / `node run_java.js --only CV-01` / `./run_ipad.sh CV-01` で個別実行できる。
