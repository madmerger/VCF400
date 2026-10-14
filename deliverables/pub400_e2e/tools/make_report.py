#!/usr/bin/env python3
"""Generate the PUB400 5250 E2E integrated test report (MD + PDF).

Reads fixed data only — never connects to PUB400:
  results/results.json        scenario/step results (expected/actual/PASS)
  test_scenarios.md           scenario list + DeepWiki source anchors
  build/build.log             compile summary section
  build/objects_after.txt     DSPOBJD *FULL listing
  results/db_TS-XX_{before,after}.txt  record counts per dump table
  screenshots/*.png           embedded into the PDF

Usage: python3 make_report.py   (run from anywhere; paths are repo-relative)
Outputs: ../VCF400_PUB400_E2E_test_report.md  and  .pdf
PDF rendering uses Playwright Chromium with system Noto Sans CJK.
"""
import html
import json
import re
from pathlib import Path

BASE = Path(__file__).resolve().parent.parent
RES = BASE / "results"
BUILD = BASE / "build"
MD_OUT = BASE / "VCF400_PUB400_E2E_test_report.md"
PDF_OUT = BASE / "VCF400_PUB400_E2E_test_report.pdf"

DUMP_TABLES = ["VOTINGDB", "GUESTBKDB", "SETTINGS", "EXHBDB", "LRN400STR", "AWARDDB"]

# composite 2-up screenshots produced during the run (fall back if absent)
KEY_SHOTS = [
    "menu_and_validation.png",
    "TS-04_business_errors.png",
    "TS-06_guestbook.png",
    "TS-07_read.png",
    "TS-08_privacy.png",
    "TS-09_learn.png",
    "TS-10_kiosk.png",
]


def load_json():
    return json.loads((RES / "results.json").read_text(encoding="utf-8"))


def scenario_anchors():
    """TS id -> DeepWiki anchor from the シナリオ一覧 table in test_scenarios.md."""
    anchors = {}
    for line in (BASE / "test_scenarios.md").read_text(encoding="utf-8").splitlines():
        m = re.match(r"\|\s*(TS-\d+)\s*\|([^|]*)\|([^|]*)\|", line)
        if m:
            anchors[m.group(1)] = m.group(3).strip()
    return anchors


def scenario_ops():
    """TS id -> {step_no: operation line} from the numbered lists in test_scenarios.md."""
    ops, cur = {}, None
    for line in (BASE / "test_scenarios.md").read_text(encoding="utf-8").splitlines():
        m = re.match(r"###\s+(TS-\d+)", line)
        if m:
            cur = m.group(1)
            ops[cur] = {}
            continue
        m = re.match(r"(\d+)\.\s+(.+)", line)
        if m and cur:
            ops[cur][m.group(1)] = m.group(2).strip()
    return ops


def compile_summary():
    """Parse '--- compile results ---' section of build.log -> grouped rows."""
    rows, in_sec = [], False
    for line in (BUILD / "build.log").read_text(encoding="utf-8", errors="replace").splitlines():
        if "--- compile results" in line:
            in_sec = True
            continue
        if "--- object inventory" in line:
            break
        if in_sec and ":" in line:
            name, res = line.split(":", 1)
            name = name.strip()
            if not name or name == "BUILDVCF":
                continue
            if "Program" in res:
                kind = "*PGM (RPG/CL)"
            else:
                kind = "*FILE (DSPF/PRTF)"
            ok = "placed in library" in res or "created" in res and "not created" not in res
            rows.append((name, kind, "OK" if ok else "NG"))
    # menus / cmds / msgf live in objects_after.txt
    inv = (BUILD / "objects_after.txt").read_text(encoding="utf-8", errors="replace").replace("\r", "")
    objs = re.findall(r"\n Object(?: \.)+ +: +(\S+)[\s\S]*?\n Type(?: \.)+ +: +(\*\S+)", inv)
    extra = [o for o in objs if o[1] in ("*MENU", "*CMD", "*MSGF")]
    for name, typ in sorted(extra):
        rows.append((name, typ, "OK"))
    return rows


def object_counts():
    inv = (BUILD / "objects_after.txt").read_text(encoding="utf-8", errors="replace")
    types = re.findall(r" Type .*?:\s+(\*\S+)", inv)
    counts = {}
    for t in types:
        counts[t] = counts.get(t, 0) + 1
    today = len(re.findall(r"Creation date/time .*?:\s+26-10-14", inv))
    return counts, today


def db_counts(ts):
    """Return {table: (before, after)} record counts for a scenario."""
    out = {}
    for tag in ("before", "after"):
        f = RES / f"db_{ts}_{tag}.txt"
        if not f.exists():
            continue
        counts = re.findall(r"(\d+) RECORD\(S\) SELECTED", f.read_text(errors="replace"))
        for tbl, c in zip(DUMP_TABLES, counts):
            out.setdefault(tbl, {})[tag] = int(c)
    return out


def added_rows(ts):
    """Lines present in the after-dump but not before (new DB rows)."""
    fb, fa = RES / f"db_{ts}_before.txt", RES / f"db_{ts}_after.txt"
    if not (fb.exists() and fa.exists()):
        return []
    bset = set(x.strip() for x in fb.read_text(errors="replace").splitlines())
    return [x.strip() for x in fa.read_text(errors="replace").splitlines()
            if x.strip() and x.strip() not in bset and "RECORD" not in x
            and "-----" not in x and not re.match(r"^[A-Z0-9_ ]+$", x)]


def first_lines(text, n=2, width=90):
    ls = [l.strip() for l in (text or "").splitlines() if l.strip()]
    return " / ".join(ls[:n])[:width]


def build_md(data, anchors, ops, comp, counts, today):
    S = data["scenarios"]
    L = []
    A = L.append
    A("# VCF400 PUB400 5250 E2E 統合テストレポート")
    A("")
    A("## 1. 概要")
    A("")
    A("- **対象**: madmerger/VCF400 の master ソース (RPG IV / CL / DDS) を 2026-10-14 に PUB400 のライブラリ ASHIBATA2 へソースから再ビルドしたもの")
    A("- **実行環境**: PUB400 (pub400.com) 5250 端末接続 (tn5250 / telnet:23)。DB 検証は SSH 2222 の PASE `db2`")
    A("- **根拠仕様**: DeepWiki (madmerger/VCF400) — 3 Kiosk Application Flow / 3.1 EXHBMENU / 3.2 Menus and CL Stubs / 4.1 Casting a Vote / 5.1 Adding Comments / 5.2 Reading and Moderating Comments / 6.1 LEARN/400 Runtime")
    sm = data["summary"]
    A(f"- **総合結果**: {sm['scenario_pass']}/{sm['scenario_pass']+sm['scenario_fail']} シナリオ PASS "
      f"(オリジナルステップ {sm['original_steps']} 件 + 補助確認 {sm['supplemental_checks']} 件、画面証跡 {sm['screen_texts']} 件)")
    A("- モック・別実装・ローカル DB は不使用。実 RPG プログラムに対して実施。")
    A("")
    A("## 2. ビルド結果サマリ (2026-10-14, ASHIBATA2)")
    A("")
    by_kind = {}
    for name, kind, res in comp:
        by_kind.setdefault(kind, []).append((name, res))
    A("| 種別 | オブジェクト | 件数 | 結果 |")
    A("|---|---|---|---|")
    for kind in ["*PGM (RPG/CL)", "*FILE (DSPF/PRTF)", "*MENU", "*CMD", "*MSGF"]:
        items = by_kind.get(kind, [])
        names = " ".join(n for n, _ in items)
        bad = [n for n, r in items if r != "OK"]
        res = "全件 OK" if not bad else "NG: " + ", ".join(bad)
        A(f"| {kind} | {names} | {len(items)} | {res} |")
    cnt = " / ".join(f"{k}:{v}" for k, v in sorted(counts.items()))
    A("")
    A(f"ASHIBATA2 オブジェクト総数 {sum(counts.values())} ({cnt})。本ビルドで再作成されたオブジェクトの作成日時はすべて 26-10-14 "
      f"({today} 件)。データ PF (VOTINGDB 等) は CHKOBJ で既存保持。無関係オブジェクト (PF ASHIBATA 等) は未変更。")
    A("")
    A("## 3. 基本機能シナリオ一覧 (10 件)")
    A("")
    A("| ID | 機能 | DeepWiki 根拠 | 結果 |")
    A("|---|---|---|---|")
    for s in S:
        A(f"| {s['id']} | {s['name']} | {anchors.get(s['id'], '')} | {s['result']} |")
    A("")
    A("## 4. シナリオ詳細")
    for s in S:
        A("")
        A(f"### {s['id']} {s['name']} — {s['result']}")
        A("")
        A("| ステップ | 操作 | 期待値 | 実際 (抜粋・証跡) | 判定 |")
        A("|---|---|---|---|---|")
        for st in s["steps"]:
            no = st["step"]
            op = ops.get(s["id"], {}).get(no, "補助確認" if not no.isdigit() else "")
            actual = st.get("observation") or first_lines(st.get("actual", ""))
            ev = st.get("screen", "")
            cell = f"{actual}<br>証跡: `{ev}`" if ev else actual
            A(f"| {no} | {op} | {st['expected']} | {cell} | {st['result']} |")
        dbc = db_counts(s["id"])
        if dbc:
            parts = [f"{t}: {v.get('before','-')} → {v.get('after','-')} 件"
                     for t, v in dbc.items() if v.get("before") != v.get("after") or t in ("VOTINGDB", "GUESTBKDB")]
            if parts:
                A("")
                A("DB 件数 (before → after): " + "; ".join(parts))
            add = added_rows(s["id"])
            if add:
                A("追加行: `" + "`, `".join(add) + "`")
    A("")
    A("## 5. 主要スクリーンショット")
    A("")
    for p in KEY_SHOTS:
        if (BASE / "screenshots" / p).exists():
            A(f"![{p}](screenshots/{p})")
            A("")
    A("## 6. 観察事項・制約")
    A("")
    A("- tn5250 `map=37` で `!→|` や ASCII アートの縦棒・バックスラッシュ等にコードページ由来のグリフ差が見られる。機能影響なし。画面テキスト・PNG は未修正で保存し、判定時のみ既知の `!→|` を正規化した。")
    A("- signoff 送信後は接続が終了するため最終画面は未取得 (機能判定の対象外)。")
    A("- スコープ外: 管理機能 (ADMMAIN 系 / VCFMAIN 90)・印刷スプール (PRTLSTVOTE/PRTLSTCMT)。全 RPG 分岐の網羅試験ではない。")
    cl = data.get("cleanup", {})
    if cl:
        A(f"- クリーンアップ: ベースライン復旧 {cl.get('baseline_reset')}、最終 dump がベースラインと一致 = {cl.get('baseline_dump_exact_match')}")
    A("")
    A("## 7. 証跡ファイル一覧")
    A("")
    A("`deliverables/pub400_e2e/` 基準:")
    A("")
    A("- `results/results.json` — シナリオ/ステップ別の期待値・実際・判定・証跡参照")
    A("- `results/screens/TS-xx_stepN.txt` — 5250 画面テキスト 56 件")
    A("- `screenshots/*.png` — 実画面スクリーンショット 63 枚")
    A("- `results/db_TS-XX_{before,after}.txt`, `results/reset_*.txt`, `results/db_final_baseline.txt` — DB 証跡")
    A("- `results/test_report.md` — 実行時レポート (原本)")
    A("- `build/build.log` / `build/objects_after.txt` / `build/seed_dump.txt` / `build/pub400_listings/` — ビルド証跡")
    A("- `video/pub400_e2e.mp4` — 日本語字幕付き編集済み録画 (約 47 秒), `video/pub400_e2e_annotations.json`")
    A("- `tools/pub400_build.sh` / `tools/pub400_db.sh` / `tools/pub400_seed.sql` / `tools/make_report.py` — 再現用ツール")
    A("")
    return "\n".join(L)


MD_CSS = """
body { font-family: 'Noto Sans CJK JP','Noto Sans CJK',sans-serif; font-size: 9.5px;
       color: #111; margin: 14mm; }
h1 { font-size: 16px; border-bottom: 2px solid #333; padding-bottom: 4px; }
h2 { font-size: 13px; margin-top: 16px; border-bottom: 1px solid #999; }
h3 { font-size: 11px; margin-top: 12px; }
table { border-collapse: collapse; width: 100%; table-layout: fixed; margin: 6px 0; }
th, td { border: 1px solid #888; padding: 2px 4px; vertical-align: top;
         word-break: break-word; font-size: 8.5px; }
th { background: #eee; }
code { font-family: 'Noto Sans Mono CJK JP',monospace; font-size: 8px; }
img { max-width: 100%; margin: 6px 0; }
"""


def md_to_html(md):
    """Minimal MD renderer sufficient for this report (tables, lists, img, code, headings)."""
    out, in_tbl, in_code = [], False, False
    for line in md.splitlines():
        if line.startswith("```"):
            out.append("<pre>" if not in_code else "</pre>")
            in_code = not in_code
            continue
        if in_code:
            out.append(html.escape(line))
            continue
        if line.startswith("|") and line.endswith("|"):
            cells = [c.strip() for c in line.strip("|").split("|")]
            if all(re.fullmatch(r":?-+:?", c) for c in cells):
                continue
            tag = "th" if not in_tbl else "td"
            if not in_tbl:
                out.append("<table>")
            in_tbl = True
            cells = [re.sub(r"`([^`]+)`", r"<code>\1</code>", c) for c in cells]
            out.append("<tr>" + "".join(f"<{tag}>{c}</{tag}>" for c in cells) + "</tr>")
            continue
        if in_tbl:
            out.append("</table>")
            in_tbl = False
        m = re.match(r"!\[(.*?)\]\((.*?)\)", line)
        if m:
            out.append(f'<img src="{html.escape(m.group(2))}" alt="{html.escape(m.group(1))}">')
            continue
        if line.startswith("### "):
            out.append(f"<h3>{html.escape(line[4:])}</h3>")
        elif line.startswith("## "):
            out.append(f"<h2>{html.escape(line[3:])}</h2>")
        elif line.startswith("# "):
            out.append(f"<h1>{html.escape(line[2:])}</h1>")
        elif line.startswith("- "):
            out.append(f"<div>• {html.escape(line[2:])}</div>")
        elif line.strip() == "":
            out.append("")
        else:
            out.append(f"<div>{html.escape(line)}</div>")
    if in_tbl:
        out.append("</table>")
    return "<html><head><meta charset='utf-8'><style>" + MD_CSS + "</style></head><body>" \
        + "\n".join(out) + "</body></html>"


def make_pdf(md):
    tmp = BASE / "_report.html"
    tmp.write_text(md_to_html(md), encoding="utf-8")
    from playwright.sync_api import sync_playwright
    with sync_playwright() as p:
        b = p.chromium.launch()
        page = b.new_page()
        page.goto("file://" + str(tmp), wait_until="networkidle")
        page.pdf(path=str(PDF_OUT), format="A4", print_background=True,
                 margin={"top": "12mm", "bottom": "12mm", "left": "10mm", "right": "10mm"})
        b.close()
    tmp.unlink()


def main():
    data = load_json()
    comp = compile_summary()
    counts, today = object_counts()
    md = build_md(data, scenario_anchors(), scenario_ops(), comp, counts, today)
    MD_OUT.write_text(md, encoding="utf-8")
    make_pdf(md)
    print(f"wrote {MD_OUT} and {PDF_OUT}")


if __name__ == "__main__":
    main()
