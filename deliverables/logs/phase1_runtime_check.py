#!/usr/bin/env python3
"""Phase 1-2/1-3: launch the main VCF400 functions from menu VCFMAIN on PUB400
(library ASHIBATA2) over 5250 and log every input, screen and DB effect.

Flows exercised (all started from VCFMAIN, exactly like a festival attendee):
  11 -> VOTESTUB  -> NTRSTIT + ADDVOTE      (VOTESCR)
  12 -> ADDGBSTUB -> NTRSTIT + ADDGBCMT     (GUESTBKSCR/ADDCMT)
  13 -> READGBSTUB-> READGBCMT              (GUESTBKSCR/READCMT)
   1 -> LRN400                              (LRN400SCR)
  CALL VCFSTUB -> EXHBMENU (exhibit kiosk)  (EXHBMENUSC)
  CALL ADDVOTE PARM('MM2024') -> kiosk mode (exhibit ID field open)

Observed 5250 field behaviour (drives the key sequences below):
  * VOTESCR launched with the user profile: INEXHB is pre-filled with the
    profile and protected (*IN70 / DSPATR(PR)), so the tab order is
    badge -> award.  A 4-digit badge auto-advances the cursor to the award.
  * Numeric fields are CHECK(RZ); a Field Exit right-adjusts them.
  * A data-entry error (e.g. letters in a numeric field) locks the keyboard;
    Reset unlocks it.

DB state (VOTINGDB / GUESTBKDB) is dumped over SSH before and after.
"""
import base64
import os
import subprocess
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tn5250_driver import Session, TAB, FIELD_EXIT  # noqa: E402

LOGDIR = os.path.dirname(os.path.abspath(__file__))
LOG = os.path.join(LOGDIR, "phase1_06_runtime_5250.log")
OBJLIB = "ASHIBATA2"
BADGE = os.environ.get("TEST_BADGE", str(int(time.time()) % 9000 + 1000))  # unused 4-digit badge


def sql(q):
    """Run one SQL statement on PUB400 via ssh/PASE `db2` and return its output."""
    b64 = base64.b64encode(q.encode()).decode()
    remote = (f"echo {b64} | /QOpenSys/pkgs/bin/base64 -d > /tmp/{OBJLIB}_q.sql && "
              f"/QOpenSys/usr/bin/qsh -c 'db2 -f /tmp/{OBJLIB}_q.sql'")
    out = subprocess.run(["/Users/devin/p400", remote], capture_output=True, text=True).stdout
    return out.strip()


def dump_db(s, title):
    s.note(f"DB snapshot: {title}")
    for t in ("VOTINGDB", "GUESTBKDB"):
        res = sql(f"select * from {OBJLIB}.{t}")
        s.log.write(f"--- {OBJLIB}/{t}\n{res}\n")
    s.log.flush()


def back_to_menu(s, title):
    """Leave whatever confirmation/intro screen is showing and return to VCFMAIN."""
    for _ in range(4):
        t = s.text()
        if "DEMO MENU" in t:
            break
        if "Press ENTER" in t:
            s.key("ENTER")
        elif "NOMINATE" in t or "GUESTBOOK/400" in t or "LEARN/400" in t:
            s.key("F12")
        else:
            s.snap(f"unexpected screen [{title}]")
            s.key("ENTER")
    s.snap(f"back on VCFMAIN [{title}]")


def main():
    if os.path.exists(LOG):
        os.remove(LOG)
    s = Session(LOG)
    s.note(f"run started {time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())}, test badge={BADGE}")
    s.signon(os.environ["PUB400_LOGIN"], os.environ["PUB400_PW"])
    s.settle()
    s.snap("IBM i MAIN after sign-on")
    dump_db(s, "before")
    s.command(f"CHGLIBL LIBL({OBJLIB} ASHIBATA1 QGPL QTEMP) CURLIB({OBJLIB})")
    s.snap("after CHGLIBL")
    s.command(f"GO {OBJLIB}/VCFMAIN")
    assert s.wait_for("AS/400 DEMO MENU", 15), "VCFMAIN not shown"
    s.snap("VCFMAIN (AS/400 DEMO MENU)")

    # ---------------------------------------------------------------- 11 vote
    def open_votescr(title, via="11"):
        if via == "11":
            s.command("11", label="VCFMAIN option 11 (Nominate Exhibit for Award)")
            if "NOMINATE" not in s.text():
                s.snap(f"NTRSTIT intro (INTERSCR) [{title}]")
                s.key("ENTER")
        else:
            s.command(f"CALL {OBJLIB}/ADDVOTE PARM('MM2024')",
                      label="kiosk launch: ADDVOTE with LAUNCH='MM2024' (exhibit ID field open)")
        assert s.wait_for("NOMINATE EXHIBIT FOR AWARD", 10), "VOTESCR not shown"
        s.snap(f"VOTESCR initial [{title}]")

    def submit_vote(title):
        s.snap(f"VOTESCR filled [{title}]")
        s.key("F5")
        s.snap(f"VOTESCR after F5 [{title}]")
        back_to_menu(s, title)

    # (a) empty input via VCFMAIN 11 -- exhibit ID is protected/prefilled with the profile
    open_votescr("empty input")
    submit_vote("empty input (F5 with nothing typed)")

    # (b) valid vote: badge (4 digits auto-advance) -> award
    open_votescr("valid vote")
    s.send(BADGE, label="type INPUTBADGE (4 digits, auto-advance) (4 digits -> auto-advance)")
    s.type_field("1", label="INPUTAWARD")
    submit_vote("valid vote")

    # (c) duplicate badge
    open_votescr("duplicate badge")
    s.send(BADGE, label="type INPUTBADGE (same badge again) (4 digits -> auto-advance)")
    s.type_field("2", label="INPUTAWARD")
    submit_vote("duplicate badge")

    # (d) nonexistent award
    b2 = str(int(BADGE) + 1)
    open_votescr("nonexistent award")
    s.send(b2, label="type INPUTBADGE (4 digits -> auto-advance)")
    s.type_field("9", label="INPUTAWARD")
    submit_vote("nonexistent award")

    # (e) kiosk mode (MM2024): exhibit ID field is open -> nonexistent exhibit
    open_votescr("kiosk MM2024 / nonexistent exhibit", via="MM2024")
    s.send(b2, label="type INPUTBADGE (4 digits -> auto-advance)")
    s.type_field("NOSUCH", label="INEXHB")
    s.type_field("1", label="INPUTAWARD")
    submit_vote("kiosk MM2024 / nonexistent exhibit")

    # (f) kiosk mode: blank exhibit ID with badge+award typed
    open_votescr("kiosk MM2024 / blank exhibit", via="MM2024")
    s.send(b2, label="type INPUTBADGE (4 digits -> auto-advance)")
    s.send(TAB, label="[Tab] skip INEXHB")
    s.type_field("1", label="INPUTAWARD")
    submit_vote("kiosk MM2024 / blank exhibit")

    # (g) kiosk mode: ineligible exhibit (EXHBDB row 'NOVOTE' has ELIGIBLE=0)
    open_votescr("kiosk MM2024 / ineligible exhibit NOVOTE", via="MM2024")
    s.send(b2, label="type INPUTBADGE (4 digits -> auto-advance)")
    s.type_field("NOVOTE", label="INEXHB")
    s.type_field("1", label="INPUTAWARD")
    submit_vote("kiosk MM2024 / ineligible exhibit NOVOTE")

    # (h) kiosk mode: eligible exhibit DEMO400 with a new badge -> written
    open_votescr("kiosk MM2024 / eligible exhibit DEMO400", via="MM2024")
    s.send(b2, label="type INPUTBADGE (4 digits -> auto-advance)")
    s.type_field("DEMO400", label="INEXHB")
    s.type_field("2", label="INPUTAWARD")
    submit_vote("kiosk MM2024 / eligible exhibit DEMO400")
    dump_db(s, "after votes")

    # ------------------------------------------------------------ 12 guestbook
    s.command("12", label="VCFMAIN option 12 (Sign Exhibit Guestbook)")
    if "GUESTBOOK/400" not in s.text():
        s.snap("NTRSTIT intro (INTERSCR) [guestbook]")
        s.key("ENTER")
    assert s.wait_for("GUESTBOOK/400 - ADD COMMENT", 10), "ADDCMT not shown"
    s.snap("GUESTBKSCR/ADDCMT initial")
    s.key("F5")
    s.snap("GUESTBKSCR/ADDCMT F5 with empty input")
    if "GUESTBOOK/400 - ADD COMMENT" not in s.text():
        back_to_menu(s, "guestbook empty")
        s.command("12", label="VCFMAIN option 12 again")
        if "GUESTBOOK/400" not in s.text():
            s.key("ENTER")
        s.wait_for("GUESTBOOK/400 - ADD COMMENT", 10)
    s.key("RESET")
    # input order: name -> exhibit ID (protected, prefilled) -> comment
    s.type_field("Devin Tester", field_exit=False, label="INNAME")
    s.send(f"Phase1 runtime check badge {BADGE}", label="type INCMT")
    s.snap("GUESTBKSCR/ADDCMT filled")
    s.key("F5")
    s.snap("GUESTBKSCR after F5 (ENDCMT expected)")
    back_to_menu(s, "after guestbook add")
    dump_db(s, "after guestbook add")

    # ------------------------------------------------------- 13 read guestbook
    s.command("13", label="VCFMAIN option 13 (Read a Guestbook Comment)")
    if "Read a Comment" not in s.text() and "Press ENTER" in s.text():
        s.snap("intro [read guestbook]")
        s.key("ENTER")
    assert s.wait_for("Read a Comment", 10), "READCMT not shown"
    s.snap("GUESTBKSCR/READCMT initial")
    s.key("F5")
    s.snap("READCMT F5 with empty comment ID")
    s.key("RESET")
    s.type_field("1", label="INCMTID")
    s.key("F5")
    s.snap("READCMT after F5 with comment ID 1")
    if "Read a Comment" in s.text():
        s.key("RESET")
        s.type_field("9999", field_exit=False, label="INCMTID (nonexistent)")
        s.key("F5")
        s.snap("READCMT after F5 with comment ID 9999")
    back_to_menu(s, "after read guestbook")

    # --------------------------------------------------------------- 1 LRN400
    s.command("1", label="VCFMAIN option 1 (Learn AS/400 Navigation)")
    assert s.wait_for("LEARN/400", 10), "LRN400SCR not shown"
    s.snap("LRN400SCR page 1")
    s.key("F5"); s.snap("LRN400SCR after F5 (forward -> page 2)")
    s.key("F8"); s.snap("LRN400SCR after F8 (back -> page 1)")
    s.key("F5"); s.snap("LRN400SCR after F5 (page 2)")
    s.key("F3"); s.snap("after F3 (exit LRN400)")
    back_to_menu(s, "after LRN400 F3")
    s.command("1", label="VCFMAIN option 1 again (walk to the END page)")
    s.key("F5"); s.key("F5")
    s.snap("LRN400SCR after F5 x2 (page 3 has EXTRA='END' -> program exits)")
    back_to_menu(s, "after LRN400 END")

    # ------------------------------------------------------- exhibit kiosk menu
    s.command(f"CALL {OBJLIB}/VCFSTUB", label="STREXHB equivalent: EXHBMENU for current user")
    assert s.wait_for("WELCOME TO", 10), "EXHBMENUSC not shown"
    s.snap("EXHBMENUSC (kiosk) for exhibit ASHIBATA")
    # EXHBMENU shows the menu once, runs the chosen option and returns to the caller
    s.send("4", label="type INOPT=4 (Read Exhibit Guestbook)")
    s.key("ENTER")
    if "Press ENTER to continue" in s.text():
        s.snap("kiosk option 4 -> NTRSTIT intro")
        s.key("ENTER")
    s.snap("kiosk option 4 -> READCMT")
    s.type_field("2", label="INCMTID")
    s.key("F5")
    s.snap("kiosk READCMT after F5 with comment ID 2")
    s.key("F12")
    back_to_menu(s, "after F12 from kiosk READCMT")

    s.command(f"CALL {OBJLIB}/VCFSTUB", label="EXHBMENU again")
    s.wait_for("WELCOME TO", 10)
    s.send("2", label="type INOPT=2 (Learn More / LEARN/400, allowed when ENLRN400=1)")
    s.key("ENTER")
    if "Press ENTER to continue" in s.text():
        s.key("ENTER")
    s.snap("kiosk option 2 -> LRN400")
    s.key("F3")
    back_to_menu(s, "after F3 from kiosk LRN400")

    s.command(f"CALL {OBJLIB}/VCFSTUB", label="EXHBMENU again")
    s.wait_for("WELCOME TO", 10)
    s.send("7", label="type INOPT=7 (hidden option: exit kiosk, admin password)")
    s.key("ENTER")
    s.snap("kiosk exit password prompt (ADMPSWRD)")
    s.send("WRONGPW", label="type INPWD (wrong password)")
    s.key("ENTER")
    s.snap("after wrong kiosk exit password")
    if "WELCOME TO" in s.text():   # wrong password -> RPG cycle redisplays the kiosk menu
        # EXHBMENU compares INPWD with the VALUE of the first SETTINGS record (key order -> ADMPSWRD)
        pw = sql(f"select value from {OBJLIB}.settings where setting = 'ADMPSWRD'")
        pw = [l.strip() for l in pw.splitlines() if l.strip() and not l.startswith(("VALUE", "---")) and "RECORD" not in l]
        assert len(pw) == 1 and "ERROR" not in pw[0], pw
        s.send("7", label="type INOPT=7")
        s.key("ENTER")
        s.send(pw[0] if pw else "VCF2024", label="type INPWD = SETTINGS.ADMPSWRD (hidden)", secret=True)
        s.key("ENTER")
    s.snap("after kiosk exit")
    back_to_menu(s, "after kiosk")
    dump_db(s, "final")

    s.key("F3")
    s.snap("final screen")
    s.note(f"run finished {time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())}")
    s.signoff()


if __name__ == "__main__":
    main()
