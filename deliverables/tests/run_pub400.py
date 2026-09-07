#!/usr/bin/env python3
"""Phase 5 runner: execute cases.json on PUB400 (library ASHIBATA2) over 5250.

Every screen is echoed to stdout (for the recording) and appended to
results/pub400_5250.log. Observed behaviour is written to results/pub400.json.

    python3 run_pub400.py [--only CV-01,CV-04] [--no-reset] [--frames PATH]
"""
import argparse
import json
import os
import re
import sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "logs"))
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tn5250_driver import Session, TAB, FIELD_EXIT  # noqa: E402
import pub400_db  # noqa: E402
from common import Results, banner, load_cases, RESULTS_DIR  # noqa: E402

LIB = pub400_db.LIB


def classify(t):
    if "AS/400 DEMO MENU" in t:
        return "VCFMAIN"
    if "NOMINATE EXHIBIT FOR AWARD" in t:
        return "VOTE1"
    if "Press ENTER to return to the main menu" in t:
        return "VOTEEND"
    if "The voting period has ended" in t:
        return "ENDOFCON"
    if "GUESTBOOK/400 - ADD COMMENT" in t:
        return "ADDCMT"
    if "Thank you for commenting" in t:
        return "ENDCMT"
    if "Read a Comment" in t:
        return "READCMT"
    if "LEARN/400" in t and "Page" in t:
        return "LRN400"
    if "Are you sure you want to exit the kiosk" in t:
        return "ADMPSWRD"
    if "WELCOME TO..." in t:
        return "KIOSK"
    if "How to Navigate" in t:
        return "NTRSTIT"
    return "UNKNOWN"


def fix_cp37(s):
    # tn5250 map=37 renders '!' as '|' (CCSID 37 box-drawing substitution)
    return s.replace("|", "!")


class P400:
    def __init__(self, log, frames_path=None):
        self.s = Session(log)
        self.frames_path = frames_path
        self.frames = []
        self.case_id = ""
        self.case_title = ""

    def set_case(self, case):
        self.case_id = case["id"]
        self.case_title = case["title"]

    def _frame(self, step_label):
        if not self.frames_path:
            return
        rows = list(self.s.screen.display)
        lines = [(row[:80]).ljust(80) for row in rows[:24]]
        lines.extend([" " * 80] * (24 - len(lines)))
        self.frames.append({
            "case_id": self.case_id,
            "case_title": self.case_title,
            "step_label": step_label,
            "lines": lines,
        })

    def save_frames(self):
        if not self.frames_path:
            return
        parent = os.path.dirname(os.path.abspath(self.frames_path))
        os.makedirs(parent, exist_ok=True)
        tmp = self.frames_path + ".tmp"
        with open(tmp, "w", encoding="utf-8") as f:
            json.dump(self.frames, f, ensure_ascii=False, indent=2)
        os.replace(tmp, self.frames_path)

    def rows(self):
        return list(self.s.screen.display)

    def text(self):
        return self.s.text()

    def show(self, title):
        t = self.s.snap(title)
        self._frame(title)
        print(f"----- 5250: {title} [{classify(t)}]")
        for line in self.rows():
            print("|" + line.rstrip().ljust(80) + "|")
        sys.stdout.flush()
        return t

    def key(self, k):
        self.s.key(k)
        self._frame(f"key {k}")

    def type_(self, v, label, exit_key=FIELD_EXIT):
        self.s.send(v, label=f"type {label}")
        self.s.send(exit_key)
        self._frame(f"type {label}")

    def wait(self, needle, timeout=15):
        assert self.s.wait_for(needle, timeout), f"'{needle}' not shown:\n{self.text()}"
        self.s.settle(quiet=1.0)

    def command(self, cmd, label=None):
        self.s.command(cmd, label=label)

    def to_vcfmain(self):
        for _ in range(6):
            k = classify(self.text())
            if k == "VCFMAIN":
                return
            if k in ("VOTEEND", "ENDOFCON", "ENDCMT", "NTRSTIT"):
                self.key("ENTER")
            elif k in ("VOTE1", "ADDCMT", "READCMT"):
                self.key("F12")
            elif k == "LRN400":
                self.key("F3")
            elif k == "KIOSK":
                self.type_("7", "INOPT", exit_key="")
                self.key("ENTER")
                self.type_("VCF2024", "INPWD", exit_key="")
                self.key("ENTER")
            else:
                self.key("ENTER")
        raise AssertionError("could not return to VCFMAIN:\n" + self.text())

    def skip_ntrstit(self):
        if classify(self.text()) == "NTRSTIT":
            self.show("NTRSTIT (How to Navigate)")
            self.key("ENTER")


def db_op(op):
    if op["op"] == "setting":
        name = op["name"]
        value = op["value"]
        if name not in {"ALWVOTE"} or value not in {"Y", "N"}:
            raise ValueError(f"invalid setting operation: {op!r}")
        pub400_db.sql(f"UPDATE {LIB}.SETTINGS SET VALUE='{value}' WHERE SETTING='{name}'")
    elif op["op"] == "visible":
        value = op["value"]
        if value not in {"Y", "N"}:
            raise ValueError(f"invalid visibility operation: {op!r}")
        try:
            comment_id = int(op["id"])
        except (TypeError, ValueError) as exc:
            raise ValueError(f"invalid visibility operation: {op!r}") from exc
        pub400_db.sql(f"UPDATE {LIB}.GUESTBKDB SET VISIBLE='{value}' WHERE CMTID={comment_id}")
    else:
        raise ValueError(f"unknown database operation: {op!r}")
    print(f"    db op: {op}", flush=True)


def vote_row(db, badge):
    return next((v for v in db["votes"] if v["badge"] == int(badge)), None) if badge else None


def comment_row(db, cid):
    return next((c for c in db["comments"] if c["id"] == int(cid)), None)


# ------------------------------------------------------------------ flows
def flow_vote(p, c):
    i = c["in"]
    if c["launch"] == "MM2024":
        p.command(f"CALL {LIB}/ADDVOTE PARM('MM2024')", label="shared terminal launch (LAUNCH='MM2024')")
    else:
        p.command("11", label="VCFMAIN 11 Nominate Exhibit for Award")
        p.skip_ntrstit()
    p.s.settle(quiet=1.0)
    kind = classify(p.text())
    if kind == "ENDOFCON":
        p.show("ADDVOTE start")
        p.key("ENTER")
        return {"screen": "ENDOFCON"}
    p.wait("NOMINATE EXHIBIT FOR AWARD")
    p.show("VOTE1 initial")
    badge, award = i.get("badge", ""), i.get("award", "")
    if badge:
        p.s.send(badge, label="type INPUTBADGE")
        if len(badge) < 4:
            p.s.send(FIELD_EXIT)
    else:
        p.s.send(TAB, label="[Tab] skip INPUTBADGE")
    if c["launch"] == "MM2024":
        ex = i.get("exhibit", "")
        if ex:
            p.type_(ex, "INEXHB")
        else:
            p.s.send(TAB, label="[Tab] skip INEXHB")
    if award:
        p.type_(award, "INPUTAWARD")
    p.show("VOTE1 filled")
    p.key("F5")
    t = p.show("VOTE1 after F5")
    kind = classify(t)
    obs = {"screen": kind}
    if kind == "VOTE1":
        rows = p.rows()
        idx = next(k for k, r in enumerate(rows) if "Third, type" in r)
        obs["errline"] = rows[idx + 1].strip()
    return obs


def flow_gb_add(p, c):
    i = c["in"]
    if c["launch"] == "MM2024":
        p.command(f"CALL {LIB}/ADDGBCMT PARM('MM2024')", label="shared terminal launch (LAUNCH='MM2024')")
    else:
        p.command("12", label="VCFMAIN 12 Sign Exhibit Guestbook")
        p.skip_ntrstit()
    p.wait("GUESTBOOK/400 - ADD COMMENT")
    p.show("ADDCMT initial")
    name, cmt = i.get("name", ""), i.get("comment", "")
    if name:
        p.type_(name, "INNAME", exit_key=TAB)
    else:
        p.s.send(TAB, label="[Tab] skip INNAME")
    if c["launch"] == "MM2024":
        ex = i.get("exhibit", "")
        if ex:
            p.type_(ex, "INID", exit_key=TAB)
        else:
            p.s.send(TAB, label="[Tab] skip INID")
    if cmt:
        p.s.send(cmt, label="type INCMT")
    p.show("ADDCMT filled")
    p.key("F5")
    t = p.show("ADDCMT after F5")
    kind = classify(t)
    obs = {"screen": kind}
    if kind == "ADDCMT":
        rows = p.rows()
        idx = next(k for k, r in enumerate(rows) if r.startswith("  Comment"))
        obs["errline"] = next((r.strip() for r in rows[idx + 1:22] if r.strip()), "")
    return obs


def flow_gb_read(p, c):
    i = c["in"]
    if c["launch"] != "ASHIBATA":
        p.command(f"CALL {LIB}/READGBCMT PARM('{c['launch']}')", label=f"launch READGBCMT for {c['launch']}")
    else:
        p.command("13", label="VCFMAIN 13 Read a Guestbook Comment")
        p.skip_ntrstit()
    p.wait("Read a Comment")
    p.show("READCMT initial")
    if i.get("cmtid"):
        # INCMTID is 4Y: a full 4-digit entry auto-advances the cursor, so Field Exit would blank it
        p.type_(i["cmtid"], "INCMTID", exit_key="" if len(i["cmtid"]) >= 4 else FIELD_EXIT)
    p.key("F5")
    t = p.show("READCMT after F5")
    rows = p.rows()
    obs = {"screen": classify(t)}
    m = re.search(r"Currently hosting\s+(\d+)", t)
    if m:
        obs["total"] = int(m.group(1))
    obs["errline"] = rows[3][:44].strip()
    dashes = [k for k, r in enumerate(rows) if r.strip().startswith("----")]
    if len(dashes) >= 2:
        block = rows[dashes[0] + 1:dashes[1]]
        head = block[0]
        if "says to:" in head:
            name, title = head.split("says to:", 1)
            cmt = fix_cp37("".join(block[1:]))
            obs["out"] = {"name": name.strip(), "title": title.strip(), "cmt": " ".join(cmt.split())}
    return obs


def flow_learn(p, c):
    p.command("1", label="VCFMAIN 1 Learn AS/400 Navigation")
    p.wait("LEARN/400")
    p.show("LRN400 page 1")
    for k in c["in"]["keys"]:
        p.key(k)
        p.show(f"LRN400 after {k}")
    t = p.text()
    kind = classify(t)
    obs = {"screen": kind}
    if kind == "LRN400":
        rows = p.rows()
        m = re.search(r"Page\s+(\d+)", rows[0])
        obs["page"] = int(m.group(1))
        unders = [k for k, r in enumerate(rows) if r.strip().startswith("____")]
        content = fix_cp37("".join(rows[unders[0] + 1:unders[1]]))
        obs["content"] = " ".join(content.split())
    return obs


def flow_kiosk(p, c):
    i = c["in"]
    p.command(f"CALL {LIB}/EXHBMENU PARM('{c['exhibit']}')", label=f"STREXHB equivalent for {c['exhibit']}")
    p.wait("WELCOME TO")
    t = p.show(f"EXHBMENU kiosk for {c['exhibit']}")
    obs = {"screen": "KIOSK", "options": re.findall(r"^\s+(\d)\. ", t, re.M)}
    if i.get("option"):
        path = []
        p.type_(i["option"], "INOPT", exit_key="")
        p.key("ENTER")
        kind = classify(p.show(f"after option {i['option']}"))
        if kind == "NTRSTIT":
            path.append(kind)
            p.key("ENTER")
            kind = classify(p.show("after NTRSTIT"))
        if kind == "VOTE1":
            path.append(kind)
            row = next(r for r in p.rows() if "nominating" in r)
            obs["exhibit"] = row.split(":", 1)[1].strip()
            p.key("F12")
            kind = classify(p.show("after F12"))
        if kind == "ADMPSWRD":
            path.append(kind)
            p.s.send(i.get("password", ""), label="type INPWD", secret=True)
            p.key("ENTER")
            kind = classify(p.show("after password"))
        path.append(kind)
        obs["path"] = path
        obs["screen"] = kind
    return obs


FLOWS = {"vote": flow_vote, "gb_add": flow_gb_add, "gb_read": flow_gb_read, "learn": flow_learn, "kiosk": flow_kiosk}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--only")
    ap.add_argument("--no-reset", action="store_true")
    ap.add_argument("--frames", metavar="PATH")
    a = ap.parse_args()
    cases = load_cases(a.only)
    os.makedirs(RESULTS_DIR, exist_ok=True)
    banner(f"PUB400 ({LIB}) cross-validation: {len(cases)} cases")
    if not a.no_reset:
        print("resetting PUB400 baseline ...", flush=True)
        pub400_db.sql(pub400_db.BASELINE)
    print("baseline:", pub400_db.dump(), flush=True)
    res = Results("pub400")
    p = P400(os.path.join(RESULTS_DIR, "pub400_5250.log"), a.frames)
    p.s.signon(os.environ["PUB400_LOGIN"], os.environ["PUB400_PW"])
    p.s.settle()
    p.command(f"CHGLIBL LIBL({LIB} ASHIBATA1 QGPL QTEMP) CURLIB({LIB})")
    p.command(f"GO {LIB}/VCFMAIN")
    p.wait("AS/400 DEMO MENU")
    p.show("VCFMAIN")
    for c in cases:
        banner(f"{c['id']} {c['group']}: {c['title']}")
        p.set_case(c)
        p._frame("CASE START")
        for op in c.get("pre", []):
            db_op(op)
        err = None
        obs = {}
        try:
            obs = FLOWS[c["flow"]](p, c)
        except Exception as e:  # keep going, record the failure
            err = repr(e)
            p.show("screen at failure")
        try:
            p.to_vcfmain()
        except AssertionError as e:
            err = (err or "") + " " + str(e)
        for op in c.get("post", []):
            db_op(op)
        db = pub400_db.dump()
        if c["flow"] == "vote":
            obs["vote"] = vote_row(db, c["in"].get("badge"))
        elif c["flow"] == "gb_add":
            cid = c["expect"]["comment"]["id"]
            obs["comment"] = {"id": cid, "row": comment_row(db, cid)}
        res.record(c, obs, err)
        p.save_frames()
    print("final DB:", pub400_db.dump(), flush=True)
    p.save_frames()
    p.s.signoff()


if __name__ == "__main__":
    main()
