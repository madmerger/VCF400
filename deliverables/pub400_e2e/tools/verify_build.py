#!/usr/bin/env python3
"""Check exact object evidence and all 18 RPG compile severities."""
import pathlib
import re
import subprocess

tools = pathlib.Path(__file__).resolve().parent
build = tools.parent / 'build'
source = (tools / 'pub400_build.sh').read_text()
objects = re.search(r'objs="(.*?)"', source, re.S)[1].replace('\\', '').split()
objects += ['VCFMAIN:*FILE', 'MAIN:*FILE', 'ADMMAIN:*FILE']
inventory = (build / 'objects_after.txt').read_text()
pairs = []
name = None
for line in inventory.splitlines():
    match = re.match(r' Object .*?:\s+(\S+)', line)
    if match:
        name = match[1]
    match = re.match(r' Type .*?:\s+(\S+)', line)
    if match and name:
        pairs.append(name + ' ' + match[1])
exact = '\n'.join(pairs) + '\n'
missing = []
for item in objects:
    expected = item.replace(':', ' ')
    result = subprocess.run(['grep', '-Fqx', expected], input=exact, text=True)
    if result.returncode:
        missing.append(expected)
lines = [f'grep -Fqx inventory verification: expected={len(objects)} found={len(objects)-len(missing)} missing={len(missing)}']
lines += ['MISSING: ' + item for item in missing]
programs = re.search(r'for p in (NTRSTIT .*?); do', source)[1].split()
passed = 0
for name in programs:
    listing = (build / 'pub400_listings' / (name + '.txt')).read_text()
    result = re.search(r'Program ' + name + r' placed in library OBJLIB\. (\d+) highest severity', listing)
    severity = result[1] if result else 'MISSING'
    lines.append(f'{name}: highest severity {severity}')
    passed += severity == '00'
lines.append(f'RPG severity verification: expected=18 severity00={passed} failed={18-passed}')
output = '\n'.join(lines) + '\n'
(build / 'verify.txt').write_text(output)
print(output, end='')
raise SystemExit(bool(missing) or passed != 18)
