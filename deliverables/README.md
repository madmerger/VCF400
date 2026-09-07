# VCF/400 検証・仕様化・マイグレーション成果物一覧

## Definition of Done 成果物

| 区分 | 成果物 | 固定パス |
|---|---|---|
| 仕様 | 仕様書 Markdown | `deliverables/spec/VCF400_spec.md` |
| 仕様 | 仕様書 PDF | `deliverables/spec/VCF400_spec.pdf` |
| 仕様 | 元画面構造台帳 Markdown | `deliverables/spec/VCF400_screen_ledger.md` |
| 仕様 | 元画面構造台帳 PDF | `deliverables/spec/VCF400_screen_ledger.pdf` |
| 仕様 | トレーサビリティ表 Markdown | `deliverables/spec/VCF400_traceability.md` |
| 仕様 | トレーサビリティ表 PDF | `deliverables/spec/VCF400_traceability.pdf` |
| テスト | テスト結果レポート Markdown | `deliverables/tests/VCF400_test_report.md` |
| テスト | テスト結果レポート PDF | `deliverables/tests/VCF400_test_report.pdf` |
| 録画 | PUB400 RPG 画面フレーム連続表示、等速 | `deliverables/video/1_PUB400_RPG.mp4`（492.500 秒 / 8.65 MB） |
| 録画 | Java Web Chromium 実画面、等速 | `deliverables/video/2_Java_Web.mp4`（154.633 秒 / 5.60 MB） |
| 録画 | iPad シミュレータ実画面、等速 | `deliverables/video/3_iPad.mp4`（426.500 秒 / 6.81 MB） |
| 成果物一覧 | 本 README | `deliverables/README.md` |
| 実装 | Java Web 版 | `java/` |
| 実装 | iPad 版 | `ipad/` |
| フェーズ0 | 棚卸し・前提確認 | `deliverables/logs/phase0_inventory.md` |
| フェーズ1 | 準備・削除前後 | `deliverables/logs/phase1_01_prepare.log` |
| フェーズ1 | SAVF FTP | `deliverables/logs/phase1_02_ftp_savf.log` |
| フェーズ1 | DSPSAVF | `deliverables/logs/phase1_03_dspsavf.log` |
| フェーズ1 | RSTLIB 権限エラー | `deliverables/logs/phase1_04_rstlib.log` |
| フェーズ1 | ソースビルド | `deliverables/logs/phase1_05_build.log` |
| フェーズ1 | 初期 seed | `deliverables/logs/phase1_05b_seed.log` |
| フェーズ1 | 5250 ランタイム検証 | `deliverables/logs/phase1_06_runtime_5250.log` |
| フェーズ1 | ALWVOTE 検証 | `deliverables/logs/phase1_07_runtime_alwvote.log` |
| フェーズ1 | 最終 seed・ベースライン復元 | `deliverables/logs/phase1_08_final_seed.log` |
| フェーズ1 | 実行レポート | `deliverables/logs/phase1_report.md` |
| フェーズ4 | Java / iPad 台帳適合性 | `deliverables/logs/phase4_conformance.md` |
| フェーズ5 | 録画設計 | `deliverables/video/recording_design.md` |

補助的なテスト定義・比較結果・ランナーは `deliverables/tests/` に、Java/iPad の
画面証跡は `deliverables/screenshots/java/` と `deliverables/screenshots/ipad/` にある。

## 再実行手順

### Phase 1: PUB400 ソース再ビルドとベースライン

```sh
./deliverables/logs/pub400_build.sh
python3 deliverables/tests/pub400_db.py reset
# 必要に応じて deliverables/logs/pub400_seed.sql を PASE db2 から実行
```

PUB400 では `ASHIBATA1`（ソース）と `ASHIBATA2`（オブジェクト・データ）だけを
使用する。`CLRLIB` は使用しない。

### Phase 2: 仕様書 PDF

```sh
NODE_PATH=$HOME/pwtools/node_modules \
python3 deliverables/tools/md2pdf.py \
  deliverables/spec/VCF400_spec.md deliverables/spec/VCF400_spec.pdf
NODE_PATH=$HOME/pwtools/node_modules \
python3 deliverables/tools/md2pdf.py \
  deliverables/spec/VCF400_screen_ledger.md \
  deliverables/spec/VCF400_screen_ledger.pdf
NODE_PATH=$HOME/pwtools/node_modules \
python3 deliverables/tools/md2pdf.py \
  deliverables/spec/VCF400_traceability.md \
  deliverables/spec/VCF400_traceability.pdf
```

### Phase 3: Java / iPad ビルド

```sh
(cd java && mvn -q test)
(cd java && mvn -q spring-boot:run)
(cd ipad && xcodegen generate && \
  xcodebuild -project VCF400.xcodeproj -scheme VCF400 \
    -destination 'platform=iOS Simulator,name=iPad Pro 13-inch (M5)' \
    -derivedDataPath build build-for-testing)
(cd ipad/VCF400Kit && swift test)
```

### Phase 5/6: 3 環境の全件比較と録画

PUB400 は実行前後にリセットする。

```sh
python3 deliverables/tests/pub400_db.py reset
python3 deliverables/tests/run_pub400.py \
  --frames deliverables/tests/results/pub400_frames.json
NODE_PATH=$HOME/pwtools/node_modules \
node deliverables/tools/frames_to_video.js \
  deliverables/tests/results/pub400_frames.json \
  deliverables/video/1_PUB400_RPG.mp4
python3 deliverables/tests/pub400_db.py reset
```

Java は Spring Boot が `:8080` で起動済みであることを確認し、headed Chromium で
録画する。

```sh
NODE_PATH=$HOME/pwtools/node_modules \
node deliverables/tests/run_java.js \
  --record "$HOME/vcf_rec/java_webm" \
  --out deliverables/video/2_Java_Web.mp4
```

iPad は build-for-testing 済みの simulator に対して録画する。

```sh
VCF_RAW_DIR="$HOME/vcf_rec" \
deliverables/tests/record_ipad.sh "" deliverables/video/3_iPad.mp4
```

比較とレポート生成:

```sh
python3 deliverables/tests/compare.py
python3 deliverables/tests/report.py
NODE_PATH=$HOME/pwtools/node_modules \
python3 deliverables/tools/md2pdf.py \
  deliverables/tests/VCF400_test_report.md \
  deliverables/tests/VCF400_test_report.pdf
```

一括実行する場合は `deliverables/tests/run_all.sh` が上記の 3 環境別録画を実行する。
`NO_RECORD=1 ./deliverables/tests/run_all.sh` では録画を省略して比較だけを行う。

ffmpeg に `drawtext` がないため、iPad のケース帯オーバーレイは Playwright で
PNG をレンダリングし、ffmpeg の `overlay` / `scale` / `pad` で合成する。

追加のスモーク録画（`/Users/devin/vcf_smoke`、`~/vcf_rec`）は成果物に含めない。
