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
import subprocess
import sys

LIB = os.environ.get("VCF_PUB400_LIB", "ASHIBATA2")
SSH = os.path.expanduser("~/p400")

BASELINE = [
    f"DELETE FROM {LIB}.VOTINGDB WHERE BADGENBR NOT IN (1, 28)",
    f"DELETE FROM {LIB}.GUESTBKDB WHERE CMTID > 2",
    f"UPDATE {LIB}.GUESTBKDB SET VISIBLE='Y', GUESTNAME='Great exhibit', GUESTCMT='VCF/400 running on PUB400.' WHERE CMTID = 1",
    f"UPDATE {LIB}.GUESTBKDB SET VISIBLE='Y' WHERE CMTID = 2",
    f"DELETE FROM {LIB}.AWARDDB WHERE AWARDID NOT IN (1, 2)",
    f"UPDATE {LIB}.AWARDDB SET AWARDTITLE='Best in Show Award', AWARDDESC='This award is given to the exhibit who you believe to be the best in show for 2024.' WHERE AWARDID = 1",
    f"UPDATE {LIB}.AWARDDB SET AWARDTITLE='The Ed Fair Award', AWARDDESC='This award is given to the exhibit that is deemed the most informative of the show.' WHERE AWARDID = 2",
    f"UPDATE {LIB}.SETTINGS SET VALUE='Y' WHERE SETTING = 'ALWVOTE'",
]


def sql(statements):
    """Run statements (str or list) with PASE db2 and return raw output."""
    if isinstance(statements, str):
        statements = [statements]
    script = ";\n".join(statements) + ";\n"
    b64 = base64.b64encode(script.encode()).decode()
    remote = (f"echo {b64} | /QOpenSys/pkgs/bin/base64 -d > /tmp/{LIB}_x.sql && "
              f"/QOpenSys/usr/bin/qsh -c 'db2 -f /tmp/{LIB}_x.sql'")
    return subprocess.run([SSH, remote], capture_output=True, text=True).stdout


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
