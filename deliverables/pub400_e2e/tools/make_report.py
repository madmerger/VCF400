"""results.json から PUB400 E2E テストレポート (Markdown + PDF) を生成する。"""
import json, pathlib, sys, html
import markdown

ROOT = pathlib.Path(__file__).resolve().parents[1]
R = json.loads((ROOT / "results/results.json").read_text())
cases_def = {c["id"]: c for c in json.loads((ROOT / "cases.json").read_text())["cases"]}
verify = (ROOT / "build/verify.txt").read_text().splitlines()
inv = next(l for l in verify if "inventory verification" in l).split("verification: ")[1]
sev = next(l for l in verify if "RPG severity verification" in l).split("verification: ")[1]

steps = sum(len(c["steps"]) for c in R["cases"])
passed = sum(c["result"] == "PASS" for c in R["cases"])
L = []
a = L.append
a("# VCF400 PUB400 実機 E2E テストレポート (10 基本機能)\n")
a(f"- 対象: madmerger/VCF400 の RPG/CL/DDS を PUB400 (pub400.com) の検証用ライブラリ `<USR>2` へソースビルドしたもの")
a(f"- 実行環境: {R['environment']}（Devin E2E テスト機能が GUI 端末上の同一 tn5250 セッションを操作、録画あり）")
a("- シナリオ根拠: DeepWiki (madmerger/VCF400) + AGENTS.md の業務ルール（`test_scenarios.md` / `cases.json`）")
a(f"- 照合ルール: {R['normalization']}\n")
a("## 1. サマリー\n")
a("| 項目 | 結果 |\n|---|---|")
a(f"| ソースビルド: オブジェクト | {inv} |")
a(f"| ソースビルド: RPG コンパイル | {sev} |")
a(f"| E2E シナリオ | **{passed}/{len(R['cases'])} PASS**（{steps} ステップ） |")
a(f"| 実 DB before/after 照合 | {', '.join(R['coverage']['db_cases'])} |")
a(f"| 後処理 | reset {R['cleanup']['reset']} / サインオフ {R['cleanup']['signoff']}（最終ダンプは初期状態と一致） |")
a(f"| 動画 | `{R['video']['path']}`（{int(R['video']['duration_seconds']//60)} 分 {int(R['video']['duration_seconds']%60)} 秒） |\n")
a("## 2. ケース別結果\n")
a("| ID | 基本機能 | DeepWiki 根拠 | ステップ | 結果 |\n|---|---|---|---|---|")
for c in R["cases"]:
    dw = cases_def.get(c["id"], {}).get("deepwiki", "")
    a(f"| {c['id']} | {c['name']} | {dw} | {len(c['steps'])} | **{c['result']}** |")
a("")
a("## 3. 観察事項・制約\n")
for o in R["observations"]:
    sevl = f"**{o['severity']}** " if o.get("severity") else ""
    a(f"- {sevl}{o['case']}: {o['observed']} {o.get('impact', o.get('normalization', ''))}")
for s in R["setup_observations"]:
    a(f"- 環境: {s}")
a(f"- 未検証: {R['coverage']['out_of_scope']}\n")
a("## 4. ステップ詳細\n")
for c in R["cases"]:
    a(f"### {c['id']} {c['name']} — {c['result']}\n")
    a("| # | 操作 | 期待 | 判定 | 証跡 |\n|---|---|---|---|---|")
    for s in c["steps"]:
        exp = " / ".join(f"`{e}`" for e in s["expected"]) if isinstance(s["expected"], list) else str(s["expected"])
        ev = ", ".join(s["evidence"]) if isinstance(s.get("evidence"), list) else str(s.get("evidence", ""))
        act = str(s.get("action", "")).replace("|", "\\|")
        a(f"| {s['no']} | {act} | {exp.replace('|', chr(92) + '|')} | **{s['result']}** | {ev} |")
    db = c.get("db") or {}
    if c.get("db_expected") or db:
        a(f"\n- 実 DB 期待: {c.get('db_expected', '')} → **{db.get('result', '')}** — " + "; ".join(f"`{x}`" for x in db.get("actual", [])) + f"（{', '.join(db.get('evidence', []))}）")
    a("")
    first = ROOT / f"screenshots/{c['id']}_step1.png"
    if first.exists():
        a(f"![{c['id']}](screenshots/{c['id']}_step1.png)\n")
a("## 5. 成果物\n")
for p in ["test_scenarios.md", "cases.json", "results/results.json", "results/screens/", "screenshots/",
          "results/db_*.txt / *_reset.log", R["video"]["path"], "build/ (build.log, verify.txt, listings)", "tools/"]:
    a(f"- `{p}`")
md = "\n".join(L) + "\n"
out = ROOT / "VCF400_PUB400_test_report.md"
out.write_text(md)

body = markdown.markdown(md.replace("<USR>", "@@USR@@"), extensions=["tables"]).replace("@@USR@@", "&lt;USR&gt;")
css = ("body{font-family:'Noto Sans CJK JP','Noto Sans JP',sans-serif;font-size:10pt;margin:0}"
       "table{border-collapse:collapse;width:100%;margin:6px 0}th,td{border:1px solid #999;padding:3px 5px;vertical-align:top;overflow-wrap:anywhere}td:first-child,td strong{white-space:nowrap}"
       "th{background:#eee}code{font-size:8.5pt}img{max-width:70%;border:1px solid #ccc}h3{page-break-after:avoid}")
htmlf = ROOT / "VCF400_PUB400_test_report.html"
htmlf.write_text(f"<html><head><meta charset='utf-8'><style>{css}</style></head><body>{body}</body></html>")
from playwright.sync_api import sync_playwright
with sync_playwright() as p:
    b = p.chromium.launch()
    pg = b.new_page()
    pg.goto(htmlf.as_uri()); pg.wait_for_load_state("networkidle")
    pg.pdf(path=str(out.with_suffix(".pdf")), format="A4", margin={"top": "12mm", "bottom": "12mm", "left": "10mm", "right": "10mm"})
    b.close()
htmlf.unlink()
print(out, out.with_suffix(".pdf"))
