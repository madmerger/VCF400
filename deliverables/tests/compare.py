#!/usr/bin/env python3
"""Phase 5: compare expected (cases.json) vs observed results of PUB400 / Java / iPad.

    python3 compare.py                 -> prints a summary table, writes results/compare.json
    python3 compare.py --md FILE       -> also writes the Markdown case table for the test report

Comparison rules (per case, per environment):
  * every key in `expect` must be present in `observed` with the same value
  * strings are compared after trimming/collapsing whitespace
  * `out.title` is compared only when non-empty in `expect` (5250 shows no title line for hidden comments)
"""
import json
import os
import re
import sys

from common import RESULTS_DIR, load_cases

ENVS = ["pub400", "java", "ipad"]
ENV_LABEL = {"pub400": "PUB400", "java": "Java Web", "ipad": "iPad"}


def norm(v):
    if isinstance(v, str):
        return re.sub(r"\s+", " ", v).strip()
    if isinstance(v, dict):
        return {k: norm(x) for k, x in v.items()}
    if isinstance(v, list):
        return [norm(x) for x in v]
    return v


def diff(expect, observed, path=""):
    """Return list of 'path: expected != observed' strings."""
    out = []
    if isinstance(expect, dict):
        if not isinstance(observed, dict):
            return [f"{path or '.'}: expected object, got {json.dumps(observed, ensure_ascii=False)}"]
        for k, ev in expect.items():
            p = f"{path}.{k}" if path else k
            if k == "title" and ev == "":
                continue
            if k not in observed:
                out.append(f"{p}: missing (expected {json.dumps(ev, ensure_ascii=False)})")
            else:
                out.extend(diff(ev, observed[k], p))
        return out
    if norm(expect) != norm(observed):
        out.append(f"{path}: expected {json.dumps(expect, ensure_ascii=False)} != observed {json.dumps(observed, ensure_ascii=False)}")
    return out


def load(env):
    p = os.path.join(RESULTS_DIR, f"{env}.json")
    return json.load(open(p, encoding="utf-8")) if os.path.exists(p) else {"cases": {}}


def main():
    cases = load_cases()
    results = {e: load(e) for e in ENVS}
    report = {"cases": [], "summary": {}}
    ok_all = 0
    for c in cases:
        row = {"id": c["id"], "group": c["group"], "title": c["title"], "rules": c.get("rules", []),
               "in": c.get("in", {}), "expect": c["expect"], "envs": {}}
        all_ok = True
        for e in ENVS:
            entry = results[e]["cases"].get(c["id"])
            if entry is None:
                row["envs"][e] = {"status": "NOT RUN", "diff": ["no result"], "observed": None}
                all_ok = False
                continue
            d = diff(c["expect"], entry["observed"])
            if entry.get("error"):
                d.append("runner error: " + entry["error"])
            row["envs"][e] = {"status": "PASS" if not d else "FAIL", "diff": d, "observed": entry["observed"]}
            all_ok = all_ok and not d
        row["match"] = all_ok
        ok_all += all_ok
        report["cases"].append(row)
    report["summary"] = {
        "total": len(cases),
        "all_match": ok_all,
        **{e: sum(1 for r in report["cases"] if r["envs"][e]["status"] == "PASS") for e in ENVS},
    }
    json.dump(report, open(os.path.join(RESULTS_DIR, "compare.json"), "w", encoding="utf-8"),
              ensure_ascii=False, indent=1)

    print(f"{'ID':6} {'PUB400':7} {'Java':7} {'iPad':7} 3環境一致  title")
    for r in report["cases"]:
        print(f"{r['id']:6} {r['envs']['pub400']['status']:7} {r['envs']['java']['status']:7} "
              f"{r['envs']['ipad']['status']:7} {'OK' if r['match'] else 'NG':9}  {r['title']}")
        for e in ENVS:
            for d in r["envs"][e]["diff"]:
                print(f"         [{e}] {d}")
    print(json.dumps(report["summary"], ensure_ascii=False))

    if "--md" in sys.argv:
        write_md(report, sys.argv[sys.argv.index("--md") + 1])
    return 0 if ok_all == len(cases) else 1


def fmt_in(d):
    return "<br>".join(f"`{k}`={json.dumps(v, ensure_ascii=False)}" for k, v in d.items()) or "(なし)"


def fmt_obs(o):
    if o is None:
        return "未実行"
    return "`" + json.dumps(o, ensure_ascii=False, sort_keys=True).replace("|", "\\|") + "`"


def write_md(report, path):
    lines = ["| ID | 分類 | ケース | 入力 | 期待値 | PUB400 | Java | iPad | 3環境一致 |",
             "|---|---|---|---|---|---|---|---|---|"]
    for r in report["cases"]:
        st = lambda e: r["envs"][e]["status"]
        lines.append(f"| {r['id']} | {r['group']} | {r['title']}<br>({', '.join(r['rules'])}) | {fmt_in(r['in'])} | "
                     f"{fmt_obs(r['expect'])} | {st('pub400')} | {st('java')} | {st('ipad')} | "
                     f"{'一致' if r['match'] else '**不一致**'} |")
    lines += ["", "### 実測値 (環境別)", ""]
    for r in report["cases"]:
        lines.append(f"#### {r['id']} {r['title']}")
        lines.append("")
        for e in ENVS:
            lines.append(f"- {ENV_LABEL[e]}: {fmt_obs(r['envs'][e]['observed'])}")
            for d in r["envs"][e]["diff"]:
                lines.append(f"  - 差異: {d}")
        lines.append("")
    open(path, "w", encoding="utf-8").write("\n".join(lines) + "\n")


if __name__ == "__main__":
    sys.exit(main())
