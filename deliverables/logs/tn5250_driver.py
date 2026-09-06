#!/usr/bin/env python3
"""Headless 5250 driver for PUB400 runtime validation (Phase 1 / Phase 5).

Runs the curses tn5250 client inside a pty (pexpect), renders its output with a
VT100 emulator (pyte) and exposes screen text + key helpers. Every screen that
is read is appended to a log so the input / screen transition evidence can be
kept in deliverables/logs.

Environment: PUB400_LOGIN, PUB400_PW (never written to the log).
tn5250 curses key mapping: ESC+n = Fn (ESC = for F12), Ctrl-K = Field Exit,
Ctrl-R = Reset, CR = Enter.
"""
import os
import sys
import time

import pexpect
import pyte

ROWS, COLS = 24, 80
FKEY = {1: "\x1b1", 2: "\x1b2", 3: "\x1b3", 4: "\x1b4", 5: "\x1b5", 6: "\x1b6",
        7: "\x1b7", 8: "\x1b8", 9: "\x1b9", 10: "\x1b0", 11: "\x1b-", 12: "\x1b="}
ENTER, FIELD_EXIT, RESET, TAB = "\r", "\x0b", "\x12", "\t"


class Session:
    def __init__(self, log_path, host="pub400.com", tn5250="tn5250"):
        self.log = open(log_path, "a", encoding="utf-8")
        self.screen = pyte.Screen(COLS, ROWS)
        self.stream = pyte.ByteStream(self.screen)
        env = dict(os.environ, TERM="xterm", LINES=str(ROWS), COLUMNS=str(COLS), LANG="C")
        self.child = pexpect.spawn(tn5250, ["env=TERM=IBM-3179-2", "map=37", host],
                                   env=env, dimensions=(ROWS, COLS), encoding=None)
        self.pump(3)

    def pump(self, seconds=1.0):
        end = time.time() + seconds
        while time.time() < end:
            try:
                data = self.child.read_nonblocking(65536, timeout=0.2)
                self.stream.feed(data)
            except pexpect.TIMEOUT:
                pass
            except pexpect.EOF:
                break

    def text(self):
        return "\n".join(line.rstrip() for line in self.screen.display)

    def wait_for(self, needle, timeout=15, absent=False):
        end = time.time() + timeout
        while time.time() < end:
            self.pump(0.5)
            found = needle in self.text()
            if found != absent:
                return True
        return False

    def settle(self, quiet=1.5, timeout=20):
        """Wait until the rendered screen has not changed for `quiet` seconds."""
        end = time.time() + timeout
        last, since = self.text(), time.time()
        while time.time() < end:
            self.pump(0.3)
            cur = self.text()
            if cur != last:
                last, since = cur, time.time()
            elif time.time() - since >= quiet:
                return

    def snap(self, title):
        t = self.text()
        self.log.write(f"\n===== {title} =====\n")
        self.log.write("+" + "-" * COLS + "+\n")
        for line in self.screen.display:
            self.log.write("|" + line + "|\n")
        self.log.write("+" + "-" * COLS + "+\n")
        self.log.flush()
        return t

    def note(self, msg):
        self.log.write(f"--- {msg}\n")
        self.log.flush()

    def send(self, s, label=None, secret=False):
        if label is not None:
            self.note(f"input: {label if secret else repr(s) + ' ' + label}")
        self.child.send(s)
        self.pump(0.6)

    def type_field(self, value, field_exit=True, label=""):
        """Type into the field under the cursor and leave it with Field Exit
        (numeric fields need Field Exit so digits are right-adjusted)."""
        self.send(value, label=f"type {label}")
        self.send(FIELD_EXIT if field_exit else TAB)

    def key(self, name):
        if name == "ENTER":
            self.send(ENTER, label="[Enter]")
        elif name.startswith("F"):
            self.send(FKEY[int(name[1:])], label=f"[{name}]")
        elif name == "RESET":
            self.send(RESET, label="[Reset]")
        self.settle()

    def signon(self, user, pw):
        # PUB400 uses a customised sign-on display: "Your user name:" / "Password"
        assert self.wait_for("Your user name", 20), "no sign-on screen"
        self.settle()
        self.snap("Sign On")
        self.send(user, label="user")
        self.send(TAB)
        self.send(pw, label="password (hidden)", secret=True)
        self.key("ENTER")
        for _ in range(4):
            self.pump(1)
            t = self.text()
            if "Press Enter to continue" in t or "Display Program Messages" in t or "Display Messages" in t:
                self.snap("post sign-on message")
                self.key("ENTER")
            else:
                break

    def command(self, cmd, label=None):
        """Type a CL command on the current menu command line and press Enter."""
        self.send(cmd, label=label or "command line")
        self.key("ENTER")

    def signoff(self):
        self.child.send("signoff\r")
        self.pump(2)
        self.child.close(force=True)
        self.log.close()


if __name__ == "__main__":
    s = Session(sys.argv[1] if len(sys.argv) > 1 else "/tmp/tn5250_probe.log")
    s.wait_for("Sign On", 20)
    print(s.snap("probe"))
    s.child.close(force=True)
