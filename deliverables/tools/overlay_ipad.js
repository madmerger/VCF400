#!/usr/bin/env node
const fs = require('fs');
const os = require('os');
const path = require('path');
const { execFileSync } = require('child_process');
const { chromium } = require('playwright');

const [rawVideo, resultsPath, t0Arg, outputPath] = process.argv.slice(2);
if (!rawVideo || !resultsPath || !t0Arg || !outputPath) {
  console.error('usage: overlay_ipad.js <raw.mp4> <results.json> <t0-seconds> <output.mp4>');
  process.exit(2);
}

function command(file, args) {
  return execFileSync(file, args, { encoding: 'utf8' }).trim();
}
const filters = command('ffmpeg', ['-hide_banner', '-filters']);
for (const name of ['overlay', 'scale', 'pad']) {
  if (!new RegExp(`\\b${name}\\b`).test(filters)) throw new Error(`ffmpeg lacks ${name} filter`);
}
const probe = JSON.parse(command('ffprobe', [
  '-v', 'error', '-show_entries', 'format=duration', '-of', 'json', rawVideo
]));
const duration = Number(probe.format.duration);
const result = JSON.parse(fs.readFileSync(resultsPath, 'utf8'));
const catalogPath = path.join(path.dirname(resultsPath), '..', 'cases.json');
const catalogData = fs.existsSync(catalogPath)
  ? JSON.parse(fs.readFileSync(catalogPath, 'utf8'))
  : { cases: [] };
const catalog = Array.isArray(catalogData) ? catalogData : catalogData.cases;
const titles = Object.fromEntries(catalog.map(caseItem => [caseItem.id, caseItem.title]));
const entries = Object.entries(result.cases || {})
  .filter(([, entry]) => entry && entry.startedAt)
  .map(([id, entry]) => ({ id, title: titles[id] || id, ...entry }))
  .sort((a, b) => Date.parse(a.startedAt) - Date.parse(b.startedAt));
if (!entries.length) throw new Error('results JSON contains no startedAt entries');

const t0 = Number(t0Arg);
const starts = entries.map(entry => ({
  ...entry,
  offset: Date.parse(entry.startedAt) / 1000 - t0
}));
const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'vcf400-ipad-overlay-'));

function esc(value) {
  return String(value ?? '').replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));
}

async function main() {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1280, height: 64 } });
  const bands = [];
  for (let i = 0; i < starts.length; i += 1) {
    const entry = starts[i];
    await page.setContent(`<!doctype html>
<style>
* { box-sizing: border-box; }
html, body { margin: 0; width: 1280px; height: 64px; overflow: hidden; }
body { background: #09264b; color: #fff; padding: 8px 20px 5px;
       font-family: -apple-system, BlinkMacSystemFont, sans-serif; }
#case { font-size: 22px; line-height: 28px; font-weight: 700; white-space: nowrap; }
#step { position: absolute; right: 20px; top: 11px; font-size: 18px; font-weight: 700; }
#caption { font-size: 11px; line-height: 14px; opacity: .82; }
</style>
<div id="case">${esc(`${entry.id} ${entry.title || ''}`.trim())}</div>
<div id="step">iPad</div>
<div id="caption">iPad Simulator — VCF/400</div>`);
    const band = path.join(tempDir, `band-${String(i).padStart(3, '0')}.png`);
    await page.screenshot({ path: band });
    bands.push(band);
  }
  await browser.close();

  const chain = ['[0:v]scale=-2:720,pad=1280:720:(ow-iw)/2:0[base]'];
  let current = 'base';
  for (let i = 0; i < starts.length; i += 1) {
    const a = Math.max(0, starts[i].offset);
    const b = i + 1 < starts.length ? Math.max(a, starts[i + 1].offset) : duration;
    const next = `v${i}`;
    chain.push(`[${current}][${i + 1}:v]overlay=0:0:enable='between(t,${a.toFixed(6)},${b.toFixed(6)})'${i + 1 === starts.length ? '[out]' : `[${next}]`}`);
    current = next;
  }
  fs.mkdirSync(path.dirname(path.resolve(outputPath)), { recursive: true });
  const ffmpegArgs = [
    '-y', '-i', rawVideo,
    ...bands.flatMap(band => ['-i', band]),
    '-filter_complex', chain.join(';'),
    '-map', '[out]',
    '-an', '-c:v', 'libx264', '-pix_fmt', 'yuv420p', '-r', '30', outputPath
  ];
  execFileSync('ffmpeg', ffmpegArgs, { stdio: 'inherit' });
  console.log(`overlay duration: ${duration.toFixed(3)} seconds`);
}

main().catch(error => {
  console.error(error.stack || error);
  process.exit(1);
});
