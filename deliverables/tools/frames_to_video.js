#!/usr/bin/env node
const fs = require('fs');
const os = require('os');
const path = require('path');
const { execFileSync } = require('child_process');
const { chromium } = require('playwright');

const [framesPath, outputPath] = process.argv.slice(2);
if (!framesPath || !outputPath) {
  console.error('usage: frames_to_video.js <frames.json> <output.mp4>');
  process.exit(2);
}

const frames = JSON.parse(fs.readFileSync(framesPath, 'utf8'));
if (!Array.isArray(frames) || frames.length === 0) {
  throw new Error('frames JSON must contain at least one frame');
}

const START_DURATION = 2.5;
const FRAME_DURATION = 1.6;
const totalNormal = frames.reduce(
  (sum, frame) => sum + (frame.step_label === 'CASE START' ? START_DURATION : FRAME_DURATION),
  0
);
const factor = totalNormal > 600
  ? (600 - frames.filter(f => f.step_label === 'CASE START').length * START_DURATION) /
    (frames.filter(f => f.step_label !== 'CASE START').length * FRAME_DURATION)
  : 1;
if (factor < 1) console.log(`non-start duration factor: ${factor.toFixed(6)}`);
const durationFor = frame => frame.step_label === 'CASE START'
  ? START_DURATION
  : FRAME_DURATION * factor;
const totalDuration = frames.reduce((sum, frame) => sum + durationFor(frame), 0);

const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'vcf400-frames-'));
const concatPath = path.join(tempDir, 'frames.txt');
const pngPaths = [];

function esc(value) {
  return String(value ?? '').replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));
}

async function main() {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1280, height: 720 } });
  for (let i = 0; i < frames.length; i += 1) {
    const frame = frames[i];
    const caseText = `${frame.case_id || ''} ${frame.case_title || ''}`.trim();
    const lines = (frame.lines || []).slice(0, 24).map(line => String(line).slice(0, 80).padEnd(80, ' '));
    while (lines.length < 24) lines.push(' '.repeat(80));
    await page.setContent(`<!doctype html>
<style>
  * { box-sizing: border-box; }
  html, body { margin: 0; width: 1280px; height: 720px; overflow: hidden; }
  body { background: #000; color: #19ff62; font-family: Menlo, Monaco, monospace; }
  #band { height: 64px; padding: 8px 20px 5px; background: #09264b; color: #fff;
          font-family: -apple-system, BlinkMacSystemFont, sans-serif; position: relative; }
  #case { font-size: 22px; line-height: 28px; font-weight: 700; white-space: nowrap; }
  #step { position: absolute; right: 20px; top: 11px; font-size: 18px; font-weight: 700; }
  #caption { font-size: 11px; line-height: 14px; opacity: .82; }
  #terminal { height: 656px; padding: 18px 24px; background: #000; white-space: pre;
              font-size: 18px; line-height: 25px; }
</style>
<div id="band"><div id="case">${esc(caseText)}</div><div id="step">${esc(frame.step_label)}</div>
  <div id="caption">PUB400 pub400.com ASHIBATA2 — 5250</div></div>
<div id="terminal">${esc(lines.join('\n'))}</div>`);
    const png = path.join(tempDir, `${String(i).padStart(5, '0')}.png`);
    await page.screenshot({ path: png });
    pngPaths.push(png);
  }
  await browser.close();

  const concat = [];
  for (let i = 0; i < pngPaths.length; i += 1) {
    concat.push(`file '${pngPaths[i].replace(/'/g, "'\\''")}'`);
    concat.push(`duration ${durationFor(frames[i]).toFixed(6)}`);
  }
  concat.push(`file '${pngPaths[pngPaths.length - 1].replace(/'/g, "'\\''")}'`);
  fs.writeFileSync(concatPath, `${concat.join('\n')}\n`);
  fs.mkdirSync(path.dirname(path.resolve(outputPath)), { recursive: true });
  execFileSync('ffmpeg', [
    '-y', '-f', 'concat', '-safe', '0', '-i', concatPath,
    '-vf', 'format=yuv420p', '-r', '30', '-c:v', 'libx264',
    '-pix_fmt', 'yuv420p', outputPath
  ], { stdio: 'inherit' });
  console.log(`total duration: ${totalDuration.toFixed(3)} seconds`);
}

main().catch(error => {
  console.error(error.stack || error);
  process.exit(1);
});
