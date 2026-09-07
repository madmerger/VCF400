#!/usr/bin/env python3
"""Phase 6: build the Markdown test report from results/compare.json (+ run logs / video)."""
import json
import os
import subprocess
import time

from common import RESULTS_DIR
from compare import ENVS, ENV_LABEL, write_md

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "VCF400_test_report.md")
VIDEO = os.path.join(HERE, "..", "video", "VCF400_cross_validation.mp4")

GROUP_DESC = {
    "ADDVOTE": "投票 (F-03): 必須入力 V-01..V-03 / 重複・資格・存在チェック V-04..V-07 / CHECKOK=4 書込 B-01 / ALWVOTE B-03",
    "ADDGBCMT": "ゲストブック記入 (F-04): 必須入力 V-10..V-12 / CMTID 採番 B-06 / 展示存在チェックなし B-08",
    "READGBCMT": "ゲストブック閲覧 (F-05): 必須入力 V-15 / SETLL+READ B-09 / 非表示 B-07 / 帰属外 B-10 / 共用端末 B-11",
    "LRN400": "LEARN/400 (F-06): F5 進む / F8 前へ / F3 終了 / EXTRA='END' で終了 B-12",
    "EXHBMENU": "展示キオスク (F-07): ELIGIBLE / ENLRN400 によるオプション表示 B-13 / 隠しオプション 7 + ADMPSWRD B-14",
}


def video_info():
    if not os.path.exists(VIDEO):
        return None
    try:
        out = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration:stream=width,height",
                              "-of", "json", VIDEO], capture_output=True, text=True, check=True).stdout
        j = json.loads(out)
        dur = float(j["format"]["duration"])
        s = j["streams"][0]
        return {"duration": f"{int(dur // 60)} 分 {int(dur % 60)} 秒", "size": f"{s['width']}x{s['height']}",
                "bytes": os.path.getsize(VIDEO)}
    except (subprocess.CalledProcessError, KeyError, ValueError, FileNotFoundError):
        return {"duration": "?", "size": "?", "bytes": os.path.getsize(VIDEO)}


def env_run(env):
    p = os.path.join(RESULTS_DIR, f"{env}.json")
    if not os.path.exists(p):
        return {}
    d = json.load(open(p, encoding="utf-8"))
    return {"started": d.get("started", ""), "finished": d.get("finished", "")}


def main():
    rep = json.load(open(os.path.join(RESULTS_DIR, "compare.json"), encoding="utf-8"))
    s = rep["summary"]
    cases = rep["cases"]
    groups = {}
    for c in cases:
        groups.setdefault(c["group"], []).append(c)
    mismatches = [c for c in cases if not c["match"]]
    vi = video_info()
    runs = {e: env_run(e) for e in ENVS}
    today = time.strftime("%Y-%m-%d")

    md = []
    md.append("---")
    md.append('title: "VCF/400 クロス検証テスト結果レポート"')
    md.append('subtitle: "PUB400 (オリジナル RPG) / Java Web / iPad の 3 環境同一入力比較"')
    md.append(f'date: "{today}"')
    md.append("---")
    md.append("")
    md.append("# VCF/400 クロス検証テスト結果レポート")
    md.append("")
    md.append("| 項目 | 内容 |")
    md.append("|---|---|")
    md.append("| 対象 | `madmerger/VCF400` — オリジナル (PUB400 `ASHIBATA2` ライブラリ、ソースからビルド) / Java Web 版 (`java/`) / iPad 版 (`ipad/`) |")
    md.append("| テストケース | `deliverables/tests/cases.json` (37 ケース、仕様書 4 章 業務ルール・5 章 検証ルールから生成) |")
    md.append("| 実行方式 | PUB400: tn5250 自動操作 (`run_pub400.py`) / Java: Playwright headed Chromium (`run_java.js`) / iPad: XCUITest on iPad Pro 13-inch シミュレータ (`run_ipad.sh` → `CrossValidationUITests`) |")
    md.append("| 共通ベースライン | VOTINGDB {1/1/ASHIBATA, 28/2/ASHIBATA}, GUESTBKDB {1,2 (VISIBLE=Y, ASHIBATA)}, SETTINGS {ADMPSWRD=VCF2024, ALWVOTE=Y}, EXHBDB {ASHIBATA(1,1), DEMO400(1,0), NOVOTE(0,0)}, AWARDDB {1,2}, LRN400STR 3 ページ (3 = END)。各環境とも実行前にリセット |")
    md.append("| 判定 | 期待値 (`expect`) の全キーが実測値 (`observed`) と一致 = PASS。画面種別・エラー行・遷移経路・出力項目・DB 状態 (投票行 / コメント行) を比較 |")
    if vi:
        md.append(f"| E2E 録画 | `deliverables/video/VCF400_cross_validation.mp4` ({vi['size']}, {vi['duration']}, {vi['bytes'] // 1024 // 1024} MB) — クロス検証工程のみを 1 本で録画 |")
    md.append("")
    md.append("## 1. サマリ")
    md.append("")
    md.append("| 環境 | PASS | FAIL / 未実行 | 実行開始 (UTC) | 実行終了 (UTC) |")
    md.append("|---|---|---|---|---|")
    for e in ENVS:
        md.append(f"| {ENV_LABEL[e]} | {s[e]} / {s['total']} | {s['total'] - s[e]} | {runs[e].get('started', '')} | {runs[e].get('finished', '')} |")
    md.append(f"| **3 環境一致** | **{s['all_match']} / {s['total']}** | {s['total'] - s['all_match']} | | |")
    md.append("")
    md.append("### 1.1 分類別")
    md.append("")
    md.append("| 分類 | 内容 | ケース数 | 3 環境一致 |")
    md.append("|---|---|---|---|")
    for g, cs in groups.items():
        md.append(f"| {g} | {GROUP_DESC.get(g, '')} | {len(cs)} | {sum(1 for c in cs if c['match'])} |")
    md.append("")
    md.append("## 2. 不一致と対応")
    md.append("")
    if not mismatches:
        md.append("最終実行では 37 ケースすべてが 3 環境で一致した。")
    else:
        md.append("| ID | 環境 | 差異 |")
        md.append("|---|---|---|")
        for c in mismatches:
            for e in ENVS:
                for d in c["envs"][e]["diff"]:
                    md.append(f"| {c['id']} | {ENV_LABEL[e]} | `{d}` |")
    md.append("")
    md.append("### 2.1 クロス検証で検出・修正した相違 (最終実行前に修正済み)")
    md.append("")
    md.append("| # | 検出環境 | 内容 | ルート原因 | 対応 |")
    md.append("|---|---|---|---|---|")
    md.append("| 1 | Java / iPad | AWARDDB のタイトルがオリジナル VOTESCR DDS の表示 (`Best in Show Award` / `The Ed Fair Award`) と不一致 | 参照データを PUB400 の観測値ではなく仮値で作成していた | `data.sql` / `Database.swift` の seed を DDS どおりに修正 |")
    md.append("| 2 | iPad | VOTINGDB ベースライン (4992/4993) と LRN400STR 本文が PUB400・Java (1/28, ページ文言) と不一致 | フェーズ 1 の実行時データを seed に採用していた | `Database.swift` の seed を共通ベースラインに統一 (CV-27/28 の `content` 比較が対象) |")
    md.append("| 3 | Java | キオスクメニューでオプション 1/2 が ELIGIBLE / ENLRN400 に関係なく非表示 | Thymeleaf の `th:if` が boolean アクセサ (`isEligible()`) を解決できていなかった | `kiosk.html` を `${exhibit.isEligible()}` 形式に修正 (CV-31..33) |")
    md.append("| 4 | iPad | VCFMAIN のオプション 90 が台帳 L-01-07 と異なるグループに配置 | 画面構成の転記ミス | `MainMenuView.swift` で Administration グループへ移動 |")
    md.append("| 5 | PUB400 | フェーズ 1 の実行で残った投票・コメントがベースラインを汚染 | 共有環境上の実行データ | `pub400_db.py reset` を追加し、実行前に `ASHIBATA2` のデータを共通ベースラインへ戻す |")
    md.append("| 6 | Java (ランナー) | Playwright ランナーが `p.big` 等の非存在セレクタ待ちでタイムアウトし、ブラウザクローズ後に操作していた | `locator.textContent()` が要素出現までブロック | `page.evaluate` による非ブロック取得へ変更 (実装側の相違ではない) |")
    md.append("| 7 | PUB400 (ランナー) | 録画付き 1 回目の全件実行で CV-23 (コメント ID 9999) のみ PUB400 が `Must enter CommentID` となり 36/37 (`results/run1/`) | `INCMTID` は `4Y 0` のため 4 桁入力でカーソルが自動的に次フィールド (=同一フィールド先頭) へ進み、続く Field Exit がフィールドを消去していた | `run_pub400.py` で桁数一杯の入力時は Field Exit を送らないよう修正し、全件を再実行 (実装側の相違ではない: 5250 上で 9999 を手入力すると帰属外メッセージが表示される) |")
    md.append("")
    md.append("## 3. ケース一覧 (入力 / 期待値 / 3 環境の判定)")
    md.append("")
    tmp = os.path.join(RESULTS_DIR, "_cases.md")
    write_md(rep, tmp)
    md.append(open(tmp, encoding="utf-8").read().rstrip())
    os.remove(tmp)
    md.append("")
    md.append("## 4. 成果物")
    md.append("")
    md.append("| 種別 | パス |")
    md.append("|---|---|")
    md.append("| テストケース定義 | `deliverables/tests/cases.json` |")
    md.append("| ランナー | `deliverables/tests/run_pub400.py`, `run_java.js`, `run_ipad.sh` + `ipad/VCF400UITests/CrossValidationUITests.swift`, `run_all.sh` (録画付き一括実行) |")
    md.append("| 比較 | `deliverables/tests/compare.py` → `deliverables/tests/results/compare.json` |")
    md.append("| 実測値 | `deliverables/tests/results/pub400.json`, `java.json`, `ipad.json` |")
    md.append("| 実行ログ | `deliverables/tests/results/run_all.log` (全体), `pub400_5250.log` (5250 全画面ダンプ), `java_run.log`, `ipad_xcuitest.log` |")
    md.append("| 録画 | `deliverables/video/VCF400_cross_validation.mp4` |")
    md.append("")
    open(OUT, "w", encoding="utf-8").write("\n".join(md) + "\n")
    print("wrote", OUT)


if __name__ == "__main__":
    main()
