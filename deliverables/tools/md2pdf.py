#!/usr/bin/env python3
"""Convert a Markdown deliverable to PDF (python-markdown -> HTML -> headless Chromium via Playwright).

usage: md2pdf.py input.md output.pdf
Requires: pip install markdown ; npm i playwright (in ~/pwtools) + npx playwright install chromium
"""
import os
import re
import subprocess
import sys
import tempfile

import markdown

CSS = """
@page { size: A4; margin: 16mm 14mm; }
body { font-family: -apple-system, 'Hiragino Sans', 'Noto Sans JP', Helvetica, Arial, sans-serif;
       font-size: 9.5pt; line-height: 1.45; color: #1f2937; }
h1 { font-size: 20pt; border-bottom: 3px solid #1e40af; padding-bottom: 4px; color: #1e3a8a; }
h2 { font-size: 14pt; margin-top: 22px; border-left: 6px solid #1e40af; padding-left: 8px; color: #1e3a8a; page-break-after: avoid; }
h3 { font-size: 11.5pt; margin-top: 16px; color: #374151; page-break-after: avoid; }
table { border-collapse: collapse; width: 100%; margin: 6px 0 12px; font-size: 8.3pt; page-break-inside: auto; }
th, td { border: 1px solid #cbd5e1; padding: 3px 5px; vertical-align: top; word-break: break-word; }
th { background: #e0e7ff; text-align: left; }
tr { page-break-inside: avoid; }
code { font-family: Menlo, 'SF Mono', Consolas, monospace; font-size: 8.2pt; background: #f1f5f9; padding: 0 3px; border-radius: 3px; }
pre { background: #f8fafc; border: 1px solid #e2e8f0; padding: 8px; font-size: 7.6pt; overflow: hidden; white-space: pre-wrap; }
pre code { background: none; padding: 0; }
hr { border: 0; border-top: 1px solid #e5e7eb; margin: 18px 0; }
blockquote { border-left: 4px solid #94a3b8; margin: 0; padding: 2px 10px; color: #475569; }
.meta { color: #6b7280; font-size: 8.5pt; margin-bottom: 14px; }
"""

JS = r"""
const { chromium } = require('playwright');
(async () => {
  const [html, pdf] = process.argv.slice(2);
  const browser = await chromium.launch();
  const page = await browser.newPage();
  await page.goto('file://' + html, { waitUntil: 'load' });
  await page.pdf({ path: pdf, format: 'A4', printBackground: true,
    margin: { top: '16mm', bottom: '16mm', left: '14mm', right: '14mm' },
    displayHeaderFooter: true,
    headerTemplate: '<div></div>',
    footerTemplate: '<div style="font-size:7pt;color:#6b7280;width:100%;text-align:center;">' +
      '<span class="pageNumber"></span> / <span class="totalPages"></span></div>' });
  await browser.close();
})();
"""


def main(src, dst):
    text = open(src, encoding="utf-8").read()
    meta = ""
    m = re.match(r"^---\n(.*?)\n---\n", text, re.S)
    if m:
        text = text[m.end():]
        meta = "<div class='meta'>" + "<br>".join(
            l.split(":", 1)[1].strip().strip('"') for l in m.group(1).splitlines() if ":" in l) + "</div>"
    body = markdown.markdown(text, extensions=["tables", "fenced_code", "toc", "sane_lists"])
    html = f"<!doctype html><html><head><meta charset='utf-8'><style>{CSS}</style></head><body>{meta}{body}</body></html>"
    workdir = os.path.join(os.path.expanduser("~"), "pwtools")
    with tempfile.NamedTemporaryFile("w", suffix=".html", delete=False, encoding="utf-8", dir=workdir) as fh:
        fh.write(html)
        html_path = fh.name
    js_path = os.path.join(workdir, "md2pdf_render.js")
    with open(js_path, "w") as fh:
        fh.write(JS)
    subprocess.run(["node", js_path, html_path, os.path.abspath(dst)], cwd=workdir, check=True)
    os.unlink(html_path)
    print(dst, os.path.getsize(dst), "bytes")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
