# フェーズ5 録画設計 (3 環境 × 1 本)

共通: `deliverables/tests/cases.json` の 37 ケース (CV-01〜CV-37) を各環境で同一順序で実行し、**テストケースの操作と結果のみ**を収録する。ビルド・依存導入・端末準備・ログ閲覧は録画開始前に完了させ、動画に含めない。各ケース開始時に画面上部のバンドに `CV-05 重複バッジ (7001 で再投票)` の形式でケース番号と名称を表示する。出力は 1280x720 以上、H.264 (libx264, yuv420p) MP4。10 分を超える場合のみ等速→1.5〜2 倍速へ再エンコードし README に明記する。

| 動画 | 収録対象 | 取得方法 | オーバーレイ | 所要見込み |
|---|---|---|---|---|
| `1_PUB400_RPG.mp4` | pub400.com 上の実 5250 画面 (tn5250 で取得した 24x80 のスクリーン) | `run_pub400.py --frames` が各操作 (入力・キー送信・画面遷移) ごとの 5250 スクリーンをキャプチャし `results/pub400_frames.json` に保存 → `tools/frames_to_video.js` (Playwright Chromium で 5250 端末風に描画) → ffmpeg concat (1 フレーム 1.6 s) | 描画時に上部バンド (ケース番号・名称、操作ラベル) を合成 | 実行 ≈ 14 分 (5250 応答待ち) / 動画 ≈ 8〜9 分 (約 320 フレーム) |
| `2_Java_Web.mp4` | Chromium 実画面 (Spring Boot `http://localhost:8080`) | Playwright `recordVideo` (1280x720, headed Chromium) で `run_java.js --record` を実行 → webm を ffmpeg で H.264 MP4 へ変換 | `context.addInitScript` で各ページ読込時に上部バンド DOM を描画 (ケースごとに `localStorage.vcfCase` を更新) | ≈ 2〜3 分 (操作を目視可能な速度に間引き: 1 ステップ 400 ms 待機) |
| `3_iPad.mp4` | iPad Pro 13-inch (M5) シミュレータ実画面 | 事前に `xcodebuild build-for-testing` → `xcrun simctl io <udid> recordVideo --codec h264` 開始 → `xcodebuild test-without-building -only-testing:VCF400UITests/CrossValidationUITests` → 停止 | XCUITest が各ケース開始時刻 (`caseStarted`) を `results/ipad.json` に記録 → `tools/overlay_ipad.js`+ffmpeg で対応区間にバンド PNG を合成 (シミュレータ画面自体は無加工) | ≈ 6〜7 分 |

## 手順 (環境ごと)
1. ベースライン DB リセット (PUB400: `pub400_db.py reset` / Java: `/api/db` で reset / iPad: `-VCF_RESET` 起動引数)。
2. 録画開始 → 37 ケース実行 → 録画停止。実行結果は `results/{pub400,java,ipad}.json`。
3. `compare.py` で 3 環境の観測値 (画面種別・エラー行・DB 行) を機械比較。不一致があれば原因を修正し、該当環境のみ再実行・再録画。
4. 3 本を `deliverables/video/` に固定名で配置。他の録画 (スモーク等) は成果物に含めない。

## 制約
- ffmpeg (9.0.1) は `drawtext` (freetype) 非搭載のため、文字オーバーレイは Playwright で描画した PNG/DOM を合成する。
- PUB400 は tn5250 を pexpect で駆動するため端末ウィンドウを直接録画せず、取得した 5250 スクリーンをそのまま描画したフレームを連続表示する (ログテキスト表示ではない)。
