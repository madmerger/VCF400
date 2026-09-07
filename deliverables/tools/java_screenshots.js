const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch();
  const ctx = await b.newContext({ viewport: { width: 1180, height: 900 } });
  const p = await ctx.newPage();
  const out = process.env.HOME + '/repos/VCF400/deliverables/screenshots/java/';
  const base = 'http://localhost:8080';
  await p.goto(base + '/menu'); await p.screenshot({ path: out + '01_vcfmain.png', fullPage: true });
  await p.click('[data-option="11"]'); await p.waitForURL(/navigate/); await p.screenshot({ path: out + '02_navigate.png' });
  await p.click('[data-fkey="ENTER"]'); await p.waitForURL(/\/vote$/); await p.screenshot({ path: out + '03_vote1.png', fullPage: true });
  await p.fill('#inputBadge', ''); await p.keyboard.press('F5'); await p.waitForLoadState(); await p.screenshot({ path: out + '04_vote1_err.png', fullPage: true });
  await p.fill('#inputBadge', '8801'); await p.fill('#inputAward', '1'); await p.keyboard.press('F5'); await p.waitForLoadState();
  await p.screenshot({ path: out + '05_voteend.png' });
  await p.goto(base + '/kiosk/ASHIBATA'); await p.screenshot({ path: out + '06_kiosk.png', fullPage: true });
  await p.click('[data-option="3"]'); await p.waitForURL(/navigate/); await p.keyboard.press('Enter'); await p.waitForURL(/guestbook\/add/);
  await p.screenshot({ path: out + '07_addcmt.png', fullPage: true });
  await p.fill('#inName', 'Playwright'); await p.fill('#inCmt', 'Screenshot run'); await p.keyboard.press('F5'); await p.waitForLoadState();
  await p.screenshot({ path: out + '08_endcmt.png' });
  await p.goto(base + '/guestbook/read'); await p.fill('#inCmtId', '1'); await p.keyboard.press('F5'); await p.waitForLoadState();
  await p.screenshot({ path: out + '09_readcmt.png', fullPage: true });
  await p.goto(base + '/learn'); await p.screenshot({ path: out + '10_lrn400.png' });
  await p.keyboard.press('F5'); await p.waitForLoadState(); await p.screenshot({ path: out + '11_lrn400_p2.png' });
  await p.goto(base + '/kiosk/ASHIBATA/exit'); await p.screenshot({ path: out + '12_admpswrd.png' });
  // mobile
  const m = await (await b.newContext({ viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true })).newPage();
  await m.goto(base + '/vote'); await m.screenshot({ path: out + '13_vote1_mobile.png', fullPage: true });
  await b.close();
})().catch(e => { console.error(e); process.exit(1); });
