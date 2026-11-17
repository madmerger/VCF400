#!/usr/bin/env python3
"""Run inside a GUI terminal; control the real tn5250 PTY via a local FIFO."""
import os
import pathlib
import select
import sys

import pexpect
import pyte

home = pathlib.Path.home()
fifo = home / 'vcf5250-control'
if fifo.exists():
    fifo.unlink()
os.mkfifo(fifo, 0o600)
fd = os.open(fifo, os.O_RDWR | os.O_NONBLOCK)
screen = pyte.Screen(80, 24)
stream = pyte.Stream(screen)
child = pexpect.spawn('tn5250', ['env.TERM=IBM-3179-2', 'map=37', 'pub400.com'],
                      dimensions=(24, 80), encoding='utf-8', codec_errors='replace',
                      env={**os.environ, 'TERM': 'vt100'})
try:
    while child.isalive():
        ready, _, _ = select.select([child.child_fd, fd], [], [], 1)
        if child.child_fd in ready:
            try:
                data = child.read_nonblocking(65536, timeout=0)
            except pexpect.EOF:
                break
            sys.stdout.write(data)
            sys.stdout.flush()
            stream.feed(data)
            (home / 'vcf5250-screen.txt').write_text('\n'.join(screen.display))
        if fd in ready:
            command = os.read(fd, 4096).decode().strip()
            if command == 'login':
                child.send(os.environ['PUB400_LOGIN'] + '\t' + os.environ['PUB400_PW'] + '\r')
            elif command == 'enter':
                child.send('\r')
            elif command == 'menu':
                child.send('ADDLIBLE ' + os.environ['PUB400_LOGIN'].upper() + '2\r')
            elif command == 'call':
                child.send('GO ' + os.environ['PUB400_LOGIN'].upper() + '2/VCFMAIN\r')
            elif command == 'exit':
                child.send('\x1b3')
            elif command == 'signoff':
                child.send('SIGNOFF\r')
            elif command == 'redraw':
                screen.reset()
                child.send('\x0c')
            elif command == 'stop':
                break
finally:
    child.close(force=True)
    os.close(fd)
    fifo.unlink(missing_ok=True)
