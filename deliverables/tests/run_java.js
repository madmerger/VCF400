#!/usr/bin/env node
// Phase 5 runner: execute cases.json against the Java Web app through the browser (Playwright, headed).
//   BASE=http://localhost:8080 node run_java.js [--only CV-01,CV-04]
// The app must already run on BASE with a fresh (baseline) database. Results -> results/java.json
const fs = require('fs');
const path = require('path');
const { chromium } = require('playwright');

const BASE = process.env.BASE || 'http://localhost:8080';
const HERE = __dirname;
const RESULTS = path.join(HERE, 'results');
const only = (() => { const i = process.argv.indexOf('--only'); return i > 0 ? new Set(process.argv[i + 1].split(',')) : null; })();
const cases = JSON.parse(fs.readFileSync(path.join(HERE, 'cases.json'), 'utf8')).cases.filter(c => !only || only.has(c.id));

const out = { env: 'java', started: new Date().toISOString(), cases: {} };
function record(c, observed, error) {
  out.cases[c.id] = error ? { observed, error } : { observed };
  console.log(`[java] ${c.id} ${c.title}\n    -> ${JSON.stringify(observed)}${error ? '\n    !! ' + error : ''}`);
  out.finished = new Date().toISOString();
  fs.mkdirSync(RESULTS, { recursive: true });
  fs.writeFileSync(path.join(RESULTS, 'java.json'), JSON.stringify(out, null, 2));
}
const banner = t => console.log(`\n${'='.repeat(78)}\n${t}\n${'='.repeat(78)}`);

async function api(method, p, body) {
  const r = await fetch(BASE + '/api/db' + p, { method, headers: { 'content-type': 'application/json' }, body: body ? JSON.stringify(body) : undefined });
  return r.json();
}
async function dbOp(op) {
  if (op.op === 'setting') await api('PUT', `/settings/${op.name}`, { value: op.value });
  else if (op.op === 'visible') await api('PUT', `/comments/${op.id}/visible`, { value: op.value });
  console.log(`    db op: ${JSON.stringify(op)}`);
}
async function dump() {
  const votes = (await api('GET', '/votes')).map(v => ({ badge: v.badgenbr, award: v.awardnbr, exhibit: v.exhbnbr }));
  const comments = (await api('GET', '/comments')).map(c => ({ id: c.cmtid, visible: c.visible, exhibit: c.exhbid, name: c.guestname, comment: c.guestcmt }));
  const settings = Object.fromEntries((await api('GET', '/settings')).map(s => [s.setting, s.value]));
  return { votes, comments, settings };
}
// same baseline as pub400_db.py reset / iPad -VCF_RESET: votes 1,28 / comments 1,2 visible / ADMPSWRD=VCF2024, ALWVOTE=Y
async function reset() {
  const db = await dump();
  for (const v of db.votes) if (![1, 28].includes(v.badge)) await api('DELETE', `/votes/${v.badge}`);
  for (const c of db.comments) if (c.id > 2) await api('DELETE', `/comments/${c.id}`);
  for (const id of [1, 2]) await api('PUT', `/comments/${id}/visible`, { value: 'Y' });
  await api('PUT', '/settings/ADMPSWRD', { value: 'VCF2024' });
  await api('PUT', '/settings/ALWVOTE', { value: 'Y' });
}

async function classify(p) {
  await p.waitForLoadState('networkidle');
  const h1 = (await txt(p, 'h1.screen-title')).trim();
  const big = (await txt(p, 'p.big')).trim();
  if (h1 === 'AS/400 DEMO MENU') return 'VCFMAIN';
  if (h1 === 'NOMINATE EXHIBIT FOR AWARD') return 'VOTE1';
  if (h1 === 'GUESTBOOK/400 - ADD COMMENT') return 'ADDCMT';
  if (h1 === 'GUESTBOOK/400 - Read a Comment') return 'READCMT';
  if (h1 === 'LEARN/400') return 'LRN400';
  if (h1.startsWith('Are you sure you want to exit the kiosk')) return 'ADMPSWRD';
  if (h1 === 'VCF/400 - How to Navigate') return 'NTRSTIT';
  if (big === 'THANK YOU FOR VOTING!') return 'VOTEEND';
  if (big === 'THANKS FOR COMMENTING!') return 'ENDCMT';
  if (big === 'SORRY!') return 'ENDOFCON';
  if (p.url().includes('/kiosk/')) return 'KIOSK';
  return 'UNKNOWN';
}
const show = async (p, title) => console.log(`----- web: ${title} [${await classify(p)}] ${p.url()}`);

async function signon(p, profile) {
  await p.goto(BASE + '/menu');
  await p.locator('details summary').click();
  await p.fill('#profile', profile);
  await p.getByRole('button', { name: 'Sign on' }).click();
  await p.waitForURL(/\/menu/);
}
async function menu(p, option) {                       // VCFMAIN: click the numbered item
  await p.click(`[data-option="${option}"]`);
  await p.waitForLoadState('networkidle');
  if ((await classify(p)) === 'NTRSTIT') { await show(p, 'NTRSTIT'); await fkey(p, 'ENTER'); await p.waitForLoadState('networkidle'); }
}
async function toMain(p) {
  for (let i = 0; i < 6; i++) {
    const k = await classify(p);
    if (k === 'VCFMAIN') return;
    if (['VOTEEND', 'ENDOFCON', 'ENDCMT', 'NTRSTIT'].includes(k)) await fkey(p, 'ENTER');
    else if (['VOTE1', 'ADDCMT', 'READCMT'].includes(k)) await fkey(p, 'F12');
    else if (k === 'LRN400') await fkey(p, 'F3');
    else if (k === 'KIOSK') { await p.fill('#option', '7'); await fkey(p, 'ENTER'); await p.waitForLoadState('networkidle'); await p.fill('#inPwd', 'VCF2024'); await fkey(p, 'ENTER'); }
    else await p.goto(BASE + '/menu');
    await p.waitForLoadState('networkidle');
  }
}
async function fkey(p, key) {               // press the labelled function-key button (送信 (F5) etc.)
  await p.locator(`[data-fkey="${key}"]`).first().click();
  await p.waitForLoadState('networkidle');
}
// textContent of the first match without waiting for it to appear (locator.textContent would block until timeout)
const txt = async (p, sel) => (await p.evaluate(s => { const e = document.querySelector(s); return e ? e.textContent : ''; }, sel)).replace(/\s+/g, ' ').trim();

// ------------------------------------------------------------------ flows
async function flowVote(p, c) {
  await signon(p, c.launch);
  await menu(p, '11');
  let k = await classify(p);
  if (k === 'ENDOFCON') { await show(p, 'ADDVOTE start'); return { screen: k }; }
  await show(p, 'VOTE1 initial');
  await p.fill('#inputBadge', c.in.badge || '');
  if (c.launch === 'MM2024') await p.fill('#inExhb', c.in.exhibit || '');
  await p.fill('#inputAward', c.in.award || '');
  await show(p, 'VOTE1 filled');
  await fkey(p, 'F5');
  k = await classify(p);
  await show(p, 'VOTE1 after F5');
  const obs = { screen: k };
  if (k === 'VOTE1') obs.errline = await txt(p, '#errline');
  return obs;
}
async function flowGbAdd(p, c) {
  await signon(p, c.launch);
  await menu(p, '12');
  await show(p, 'ADDCMT initial');
  await p.fill('#inName', c.in.name || '');
  if (c.launch === 'MM2024') await p.fill('#inId', c.in.exhibit || '');
  await p.fill('#inCmt', c.in.comment || '');
  await show(p, 'ADDCMT filled');
  await fkey(p, 'F5');
  const k = await classify(p);
  await show(p, 'ADDCMT after F5');
  const obs = { screen: k };
  if (k === 'ADDCMT') obs.errline = await txt(p, '#errline');
  if (k === 'ENDCMT') obs.shownId = parseInt(await txt(p, 'b.mono'), 10);
  return obs;
}
async function flowGbRead(p, c) {
  await signon(p, c.launch);
  await menu(p, '13');
  await show(p, 'READCMT initial');
  await p.fill('#inCmtId', c.in.cmtid || '');
  await fkey(p, 'F5');
  const k = await classify(p);
  await show(p, 'READCMT after F5');
  const obs = { screen: k, total: parseInt(await txt(p, '.hint b'), 10), errline: await txt(p, '.errline') };
  if (await p.locator('.result').count()) {
    const who = await p.locator('.result .who span').allTextContents();
    obs.out = { name: who[0].trim(), title: who[2].trim(), cmt: await txt(p, '.result .text') };
  }
  return obs;
}
async function flowLearn(p, c) {
  await signon(p, c.launch);
  await menu(p, '1');
  await show(p, 'LRN400 page 1');
  for (const key of c.in.keys) { await fkey(p, key); await show(p, `LRN400 after ${key}`); }
  const k = await classify(p);
  const obs = { screen: k };
  if (k === 'LRN400') { obs.page = parseInt(await txt(p, '.page-nbr b'), 10); obs.content = await txt(p, '.page-content'); }
  return obs;
}
async function flowKiosk(p, c) {
  await p.goto(BASE + '/kiosk/' + c.exhibit);
  await show(p, `EXHBMENU kiosk for ${c.exhibit}`);
  const obs = { screen: 'KIOSK', options: await p.locator('.menu-item[data-option]').evaluateAll(els => els.map(e => e.getAttribute('data-option'))) };
  if (c.in.option) {
    const pathSeen = [];
    await p.fill('#option', c.in.option);
    await fkey(p, 'ENTER');
    let k = await classify(p); await show(p, `after option ${c.in.option}`);
    if (k === 'NTRSTIT') { pathSeen.push(k); await fkey(p, 'ENTER'); k = await classify(p); await show(p, 'after NTRSTIT'); }
    if (k === 'VOTE1') { pathSeen.push(k); obs.exhibit = await p.inputValue('#inExhb'); await fkey(p, 'F12'); k = await classify(p); await show(p, 'after F12'); }
    if (k === 'ADMPSWRD') { pathSeen.push(k); await p.fill('#inPwd', c.in.password || ''); await fkey(p, 'ENTER'); k = await classify(p); await show(p, 'after password'); }
    pathSeen.push(k);
    obs.path = pathSeen;
    obs.screen = k;
  }
  return obs;
}
const FLOWS = { vote: flowVote, gb_add: flowGbAdd, gb_read: flowGbRead, learn: flowLearn, kiosk: flowKiosk };

(async () => {
  banner(`Java Web (${BASE}) cross-validation: ${cases.length} cases`);
  if (!process.env.NO_RESET) { console.log('resetting Java baseline ...'); await reset(); }
  console.log('baseline:', JSON.stringify(await dump()));
  const browser = await chromium.launch({ headless: !!process.env.HEADLESS, slowMo: 120, args: ['--window-position=0,0', '--window-size=1000,920'] });
  const ctx = await browser.newContext({ viewport: { width: 1000, height: 880 } });
  const p = await ctx.newPage();
  p.setDefaultTimeout(15000);
  await p.goto(BASE + '/menu');
  await show(p, 'VCFMAIN');
  for (const c of cases) {
    banner(`${c.id} ${c.group}: ${c.title}`);
    for (const op of c.pre || []) await dbOp(op);
    let obs = {}, err = null;
    try { obs = await FLOWS[c.flow](p, c); } catch (e) { err = String(e); await show(p, 'screen at failure'); }
    try { await toMain(p); } catch (e) { err = (err || '') + ' ' + String(e); }
    for (const op of c.post || []) await dbOp(op);
    const db = await dump();
    if (c.flow === 'vote') obs.vote = c.in.badge ? db.votes.find(v => v.badge === parseInt(c.in.badge, 10)) || null : null;
    if (c.flow === 'gb_add') { const id = c.expect.comment.id; obs.comment = { id, row: db.comments.find(x => x.id === id) || null }; }
    record(c, obs, err);
  }
  console.log('final DB:', JSON.stringify(await dump()));
  await browser.close();
})().catch(e => { console.error(e); process.exit(1); });
