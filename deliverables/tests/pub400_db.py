#!/usr/bin/env python3
"""PUB400 (library ASHIBATA2) DB helpers for the Phase 5 cross-validation.

    python3 pub400_db.py reset      -> reset VOTINGDB/GUESTBKDB/AWARDDB/SETTINGS to the common baseline
    python3 pub400_db.py dump       -> print VOTINGDB / GUESTBKDB / SETTINGS as JSON
    python3 pub400_db.py sql "..."  -> run one statement

Only the dedicated library ASHIBATA2 is touched (never shared PUB400 objects).
SQL runs over SSH in PASE (`db2`), credentials come from PUB400_LOGIN / PUB400_PW.
"""
import base64
import json
import os
import re
import secrets
import subprocess
import sys

LIB = os.environ.get("VCF_PUB400_LIB", "ASHIBATA2")
SSH = os.path.expanduser("~/p400")

SEED_SQL = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "logs", "pub400_seed.sql")


def _load_baseline():
    """All baseline tables (EXHBDB, AWARDDB, SETTINGS, LRN400STR, GUESTBKDB, VOTINGDB) from pub400_seed.sql."""
    with open(SEED_SQL, encoding="utf-8") as f:
        src = "\n".join(l for l in f.read().splitlines() if not l.strip().startswith("--"))
    return [st.strip().replace("ASHIBATA2.", f"{LIB}.") for st in src.split(";") if st.strip()]


BASELINE = _load_baseline()


def sql(statements):
    """Run statements (str or list) with PASE db2 and return raw output."""
    if isinstance(statements, str):
        statements = [statements]
    script = ";\n".join(statements) + ";\n"
    b64 = base64.b64encode(script.encode()).decode()
    remote_path = f"/tmp/{LIB}_{os.getpid()}_{secrets.token_hex(4)}.sql"
    remote = (
        f"echo {b64} | /QOpenSys/pkgs/bin/base64 -d > {remote_path} && "
        f"/QOpenSys/usr/bin/qsh -c 'db2 -f {remote_path}'; "
        f"rc=$?; rm -f {remote_path}; exit $rc"
    )
    result = subprocess.run([SSH, remote], capture_output=True, text=True)
    output = result.stdout + result.stderr
    if result.returncode != 0:
        raise RuntimeError(f"db2 failed with exit code {result.returncode}:\n{output}")
    sqlstate_errors = [
        line for line in output.splitlines()
        if "SQLSTATE" in line and "02000" not in line and "SQL0100W" not in line
    ]
    if sqlstate_errors:
        raise RuntimeError("db2 reported SQLSTATE errors:\n" + "\n".join(sqlstate_errors))
    return output


def rows(select):
    """Parse db2 fixed-width output into a list of dicts (string values, trimmed)."""
    out = sql(select)
    lines = [l.rstrip("\n") for l in out.splitlines() if l.strip()]
    for i, l in enumerate(lines):
        if re.fullmatch(r"[- ]+", l) and i > 0:
            header, dash = lines[i - 1], l
            break
    else:
        return []
    spans, pos = [], 0
    for seg in re.finditer(r"-+", dash):
        spans.append((seg.start(), seg.end()))
    names = [header[a:b].strip() for a, b in spans]
    result = []
    for l in lines[i + 1:]:
        if "RECORD(S) SELECTED" in l:
            break
        vals = [l[a:b].strip() if a < len(l) else "" for a, b in spans]
        # last column may run past the dash width
        if spans:
            vals[-1] = l[spans[-1][0]:].strip()
        result.append(dict(zip(names, vals)))
    return result


def dump():
    return {
        "votes": [{"badge": int(r["BADGENBR"]), "award": int(r["AWARDNBR"]), "exhibit": r["EXHBNBR"]}
                  for r in rows(f"SELECT BADGENBR, AWARDNBR, EXHBNBR FROM {LIB}.VOTINGDB ORDER BY BADGENBR")],
        "comments": [{"id": int(r["CMTID"]), "visible": r["VISIBLE"], "exhibit": r["EXHBID"],
                      "name": r["GUESTNAME"], "comment": r["GUESTCMT"]}
                     for r in rows(f"SELECT CMTID, VISIBLE, EXHBID, GUESTNAME, GUESTCMT FROM {LIB}.GUESTBKDB ORDER BY CMTID")],
        "settings": {r["SETTING"]: r["VALUE"] for r in rows(f"SELECT SETTING, VALUE FROM {LIB}.SETTINGS ORDER BY SETTING")},
    }


if __name__ == "__main__":
    cmd = sys.argv[1] if len(sys.argv) > 1 else "dump"
    if cmd == "reset":
        print(sql(BASELINE))
    elif cmd == "sql":
        print(sql(sys.argv[2]))
    else:
        print(json.dumps(dump(), indent=2, ensure_ascii=False))
