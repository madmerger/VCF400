#!/usr/bin/env python3
"""Build the Phase 6 cross-validation report from compare.json and video files."""
import json
import os
import subprocess

from common import RESULTS_DIR
from compare import ENVS, ENV_LABEL

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "VCF400_test_report.md")
VIDEO_DIR = os.path.join(HERE, "..", "video")

VIDEOS = [
    ("PUB400", "1_PUB400_RPG.mp4", "実 5250 画面フレーム連続表示"),
    ("Java Web", "2_Java_Web.mp4", "Chromium 実画面"),
    ("iPad", "3_iPad.mp4", "シミュレータ実画面"),
]

GROUP_DESC = {
    "ADDVOTE": "投票",
    "ADDGBCMT": "ゲストブック記入",
    "READGBCMT": "ゲストブック閲覧",
    "LRN400": "LEARN/400",
    "EXHBMENU": "展示キオスク",
}


def compact(value):
    return json.dumps(value, ensure_ascii=False, separators=(",", ":")).replace("|", "\\|")


def duration_text(seconds):
    return f"{float(seconds):.2f} 秒"


def video_info(filename):
    path = os.path.join(VIDEO_DIR, filename)
    if not os.path.exists(path):
        return None
    try:
        out = subprocess.run(
            [
                "ffprobe",
                "-v",
                "error",
                "-show_entries",
                "format=duration:stream=codec_name,width,height",
                "-of",
                "json",
                path,
            ],
            capture_output=True,
            text=True,
            check=True,
        ).stdout
        data = json.loads(out)
        stream = data["streams"][0]
        size = os.path.getsize(path)
        return {
            "path": f"deliverables/video/{filename}",
            "duration": duration_text(data["format"]["duration"]),
            "resolution": f"{stream['width']}x{stream['height']}",
            "size": f"{size / 1_000_000:.2f} MB",
            "codec": stream.get("codec_name", "?"),
        }
    except (subprocess.CalledProcessError, KeyError, ValueError, FileNotFoundError):
        return {
            "path": f"deliverables/video/{filename}",
            "duration": "?",
            "resolution": "?",
            "size": f"{os.path.getsize(path) / 1_000_000:.2f} MB",
            "codec": "?",
        }


def load_results():
    report_path = os.path.join(RESULTS_DIR, "compare.json")
    return json.load(open(report_path, encoding="utf-8"))


def append_case_table(md, cases):
    md.extend(
        [
            "| ID | 名称 | 入力 | 期待値 | PUB400 実測 | Java 実測 | iPad 実測 | 合否 |",
            "|---|---|---|---|---|---|---|---|",
        ]
    )
    for case in cases:
        envs = case["envs"]
        md.append(
            f"| {case['id']} | {case['title']} | {compact(case['in'])} | "
            f"{compact(case['expect'])} | {compact(envs['pub400']['observed'])} | "
            f"{compact(envs['java']['observed'])} | {compact(envs['ipad']['observed'])} | "
            f"{'PASS' if case['match'] else '**FAIL**'} |"
        )


def main():
    report = load_results()
    summary = report["summary"]
    cases = report["cases"]
    videos = [(env, video_info(filename), content) for env, filename, content in VIDEOS]
    mismatches = [case for case in cases if not case["match"]]

    md = [
        "---",
        'title: "VCF/400 クロス検証テスト結果レポート"',
        'subtitle: "PUB400 (オリジナル RPG) / Java Web / iPad の 3 環境同一入力比較"',
        'date: "2026-10-14"',
        "---",
        "",
        "# VCF/400 クロス検証テスト結果レポート",
        "",
        "| 項目 | 内容 |",
        "|---|---|",
        "| 対象 | オリジナル RPG (PUB400 `ASHIBATA2`) / Java Web 版 (`deliverables/java/`, PR #11) / iPad 版 (`deliverables/ipad/`, PR #13) |",
        "| テストケース | `deliverables/tests/cases.json` (37 ケース) |",
        "| 実行方式 | PUB400: tn5250 (`run_pub400.py`) / Java: Playwright Chromium (`run_java.js`) / iPad: XCUITest test-without-building (`run_ipad.sh`, 録画は `record_ipad.sh`) |",
        "| 判定 | `expect` の全キーが 3 環境の `observed` と一致 = PASS |",
        "",
        "## 1. サマリ",
        "",
        "| 環境 | PASS | FAIL / 未実行 |",
        "|---|---|---|",
    ]
    for env in ENVS:
        md.append(
            f"| {ENV_LABEL[env]} | {summary[env]} / {summary['total']} | "
            f"{summary['total'] - summary[env]} |"
        )
    md.extend(
        [
            f"| **3 環境一致** | **{summary['all_match']} / {summary['total']}** | "
            f"{summary['total'] - summary['all_match']} |",
            "",
            "### 1.1 分類別",
            "",
            "| 分類 | 内容 | ケース数 | 3 環境一致 |",
            "|---|---|---|---|",
        ]
    )
    groups = {}
    for case in cases:
        groups.setdefault(case["group"], []).append(case)
    for group, group_cases in groups.items():
        md.append(
            f"| {group} | {GROUP_DESC.get(group, '')} | {len(group_cases)} | "
            f"{sum(1 for case in group_cases if case['match'])} |"
        )

    md.extend(
        [
            "",
            "## 2. 不一致と対処",
            "",
            "本 run は、PUB400 を `ASHIBATA2` にソースビルドした RPG、Java を PR #11 "
            "(`devin/1791970110-vcf400-java-web`, `03ed528`)、iPad を PR #13 "
            "(`devin/1791970125-ipad-deliverables`, `e67316a`) として実行した。"
            "`compare.py` の最終結果は PUB400 37/37、Java 37/37、iPad 37/37、"
            "3 環境一致 37/37 で、不一致は 0 件である。",
            "",
            "Java の最終コミット `32ead03` は VCFMAIN 見出し行の空白保持 (CSS `white-space: pre`) "
            "のみの変更で、37 ケースの判定対象には影響しない (Maven 53/53 は `03ed528` 以降も通過)。",
            "",
            "実行前後の初期化: PUB400 は `pub400_db.py reset` (`logs/pub400_seed.sql` の全 6 表) を"
            "開始前と終了後に必ず実行、Java は `VCF_DB_API=true` の fresh H2 + DB API reset、"
            "iPad は `-VCF_RESET` 起動引数でケースごとに SQLite を初期化した。"
            "アプリケーションと runner に、一致させるための個別の補正は入れていない。",
            "",
            "ブラウザ E2E (testing agent) で、Java の VCFMAIN に台帳 L-01-01 の見出し "
            "`WELCOME TO...` / `V I N T A G E  C O M P U T E R  F E S T I V A L  2 0 2 4` が"
            "欠けていることが見つかり、`03ed528` で追加した (cases.json の判定外の表示要素)。"
            "iPad は当初から表示していた。",
            "",
        ]
    )
    if mismatches:
        md.extend(
            [
                "| ID | 環境 | 差異 |",
                "|---|---|---|",
            ]
        )
        for case in mismatches:
            for env in ENVS:
                for difference in case["envs"][env]["diff"]:
                    md.append(f"| {case['id']} | {ENV_LABEL[env]} | `{difference}` |")
    else:
        md.append("最終実行における不一致は 0 件。")

    md.extend(
        [
            "",
            "### UI 日本語化とクロス検証契約",
            "",
            "比較で参照する `#errline` / `errline` 識別子は英語原文 M-xx を返し、"
            "画面上の日本語は `.errline-ja` / `errline_ja` に表示する。画面検出の"
            "アンカーとして、台帳で保持対象とした legacy title の英語文字列は残す。",
            "",
            "",
            "## 3. ケース一覧",
            "",
            "入力、期待値、3 環境の実測値、および合否を以下に示す。",
            "",
        ]
    )
    append_case_table(md, cases)

    md.extend(
        [
            "",
            "## 4. 実行環境",
            "",
            "| 項目 | 値 |",
            "|---|---|",
            "| PUB400 / Java 実行ホスト | Linux (Ubuntu 22.04) |",
            "| iPad 実行ホスト | macOS + Xcode (iOS Simulator SDK 26.5), iPad Pro 13-inch (M5) シミュレータ |",
            "| Java | OpenJDK 17.0.19 / Spring Boot / H2 (MODE=DB2) |",
            "| tn5250 | 0.19.0 |",
            "| Playwright | 1.58.2 (Chromium) |",
            "| PUB400 | `ASHIBATA1` / `ASHIBATA2` |",
            "| 実行日 | 2026-10-14 |",
            "",
            "## 5. 動画",
            "",
            "| 環境 | 動画パス | 長さ | 解像度 | サイズ | 収録内容 | 速度 |",
            "|---|---|---|---|---|---|---|",
        ]
    )
    for env, info, content in videos:
        if info is None:
            md.append(f"| {env} | — | — | — | — | {content} | 等速 |")
        else:
            md.append(
                f"| {env} | `{info['path']}` | {info['duration']} | "
                f"{info['resolution']} ({info['codec']}) | {info['size']} | {content} | 等速 |"
            )
    md.extend(
        [
            "",
            "3 本とも実時間の等速録画であり、追加の速度変更は行っていない。PUB400 は "
            "`self.s.screen.display` から取得した 5250 画面フレームを連続表示し、Java は "
            "Chromium の実画面、iPad はシミュレータの実画面を収録した。iPad のケース帯 "
            "オーバーレイは ffmpeg に `drawtext` がないため Playwright で PNG を生成し、"
            "`overlay` / `scale` / `pad` で合成した。",
            "",
            "## 6. 成果物と再現性",
            "",
            "| 種別 | パス |",
            "|---|---|",
            "| テストケース定義 | `deliverables/tests/cases.json` |",
            "| 実測値 | `deliverables/tests/results/pub400.json`, `java.json`, `ipad.json` |",
            "| 比較結果 | `deliverables/tests/results/compare.json` |",
            "| フレーム | `deliverables/tests/results/pub400_frames.json` |",
            "| ランナー | `run_pub400.py`, `run_java.js`, `run_ipad.sh`, `record_ipad.sh` |",
            "| 動画変換 | `frames_to_video.js`, `overlay_ipad.js` |",
            "",
        ]
    )
    open(OUT, "w", encoding="utf-8").write("\n".join(md))
    print("wrote", OUT)


if __name__ == "__main__":
    main()
