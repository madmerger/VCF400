#!/usr/bin/env python3
"""Phase 1 supplement: SETTINGS.ALWVOTE='N' closes the voting (ADDVOTE shows
ENDOFCON and exits before the form).  Restores ALWVOTE='Y' afterwards."""
import os
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from tn5250_driver import Session  # noqa: E402
from phase1_runtime_check import sql, OBJLIB, LOGDIR  # noqa: E402

LOG = os.path.join(LOGDIR, "phase1_07_runtime_alwvote.log")


def main():
    if os.path.exists(LOG):
        os.remove(LOG)
    s = Session(LOG)
    s.note(f"run started {time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())}")
    s.note("SETTINGS before: " + sql(f"select setting, value from {OBJLIB}.settings").replace("\n", " | "))
    s.note("SQL: update ASHIBATA2.SETTINGS set VALUE='N' where SETTING='ALWVOTE'")
    s.note(sql(f"update {OBJLIB}.settings set value = 'N' where setting = 'ALWVOTE'"))
    s.note("SETTINGS after update: " + sql(f"select setting, value from {OBJLIB}.settings").replace("\n", " | "))
    try:
        s.signon(os.environ["PUB400_LOGIN"], os.environ["PUB400_PW"])
        s.settle()
        s.command(f"CHGLIBL LIBL({OBJLIB} ASHIBATA1 QGPL QTEMP) CURLIB({OBJLIB})")
        s.command(f"GO {OBJLIB}/VCFMAIN")
        assert s.wait_for("AS/400 DEMO MENU", 15)
        s.command("11", label="VCFMAIN option 11 (Nominate Exhibit for Award) with ALWVOTE=N")
        if "Press ENTER" in s.text() and "NOMINATE" not in s.text():
            s.snap("NTRSTIT intro")
            s.key("ENTER")
        s.snap("ADDVOTE with ALWVOTE=N (ENDOFCON expected)")
        s.key("ENTER")
        s.snap("after ENTER")
        s.key("F3")
        s.signoff()
    finally:
        sql(f"update {OBJLIB}.settings set value = 'Y' where setting = 'ALWVOTE'")
        with open(LOG, "a") as f:
            f.write("--- SETTINGS restored: " + sql(f"select setting, value from {OBJLIB}.settings").replace("\n", " | ") + "\n")


if __name__ == "__main__":
    main()
