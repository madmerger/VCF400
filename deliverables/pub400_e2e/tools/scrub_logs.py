#!/usr/bin/env python3
"""Redact the validation account and derived libraries in textual evidence."""
import os
import pathlib
import re

root = pathlib.Path(__file__).resolve().parents[1] / 'build'
login = os.environ['PUB400_LOGIN']
for path in root.rglob('*'):
    if path.is_file() and path.suffix in {'.txt', '.log', '.clle'}:
        text = path.read_text(errors='replace')
        for value, replacement in [(login + '1', 'SRCLIB'), (login + '2', 'OBJLIB'), (login, 'USER')]:
            text = re.sub(re.escape(value), replacement, text, flags=re.IGNORECASE)
        path.write_text(text)
