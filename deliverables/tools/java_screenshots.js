const { chromium } = require('playwright');
(async () => {
  const b = await chromium.launch();
  const ctx = await b.newContext({ viewport: { width: 1180, height: 900 } });
  const p = await ctx.newPage();
  const cdp = await ctx.newCDPSession(p);
  const shot = async file => {
    const result = await cdp.send('Page.captureScreenshot', { format: 'png', captureBeyondViewport: false });
    require('fs').writeFileSync(file, Buffer.from(result.data, 'base64'));
  };
  const out = process.env.HOME + '/repos/VCF400/deliverables/screenshots/java/';
  const base = 'http://localhost:8080';
  await p.goto(base + '/menu'); await shot(out + '01_vcfmain.png');
  await p.click('[data-option="11"]'); await p.waitForURL(/navigate/); await shot(out + '02_navigate.png');
  await p.click('[data-fkey="ENTER"]'); await p.waitForURL(/\/vote$/); await shot(out + '03_vote1.png');
  await p.fill('#inputBadge', ''); await p.keyboard.press('F5'); await p.waitForTimeout(500); await shot(out + '04_vote1_err.png');
  await p.fill('#inputBadge', '8801'); await p.fill('#inputAward', '1'); await p.keyboard.press('F5'); await p.waitForTimeout(500);
  await shot(out + '05_voteend.png');
  await p.goto(base + '/kiosk/ASHIBATA'); await shot(out + '06_kiosk.png');
  await p.click('[data-option="3"]'); await p.waitForURL(/navigate/); await p.keyboard.press('Enter'); await p.waitForURL(/guestbook\/add/);
  await shot(out + '07_addcmt.png');
  await p.fill('#inName', 'Playwright'); await p.fill('#inCmt', 'Screenshot run'); await p.keyboard.press('F5'); await p.waitForTimeout(500);
  await shot(out + '08_endcmt.png');
  await p.goto(base + '/guestbook/read'); await p.fill('#inCmtId', '1'); await p.keyboard.press('F5'); await p.waitForTimeout(500);
  await shot(out + '09_readcmt.png');
  await p.goto(base + '/learn'); await shot(out + '10_lrn400.png');
  await p.keyboard.press('F5'); await p.waitForTimeout(500); await shot(out + '11_lrn400_p2.png');
  await p.goto(base + '/kiosk/ASHIBATA/exit'); await shot(out + '12_admpswrd.png');
  // mobile
  const mctx = await b.newContext({ viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true });
  const m = await mctx.newPage();
  const mcdp = await mctx.newCDPSession(m);
  await m.goto(base + '/vote');
  const mobileShot = await mcdp.send('Page.captureScreenshot', { format: 'png', captureBeyondViewport: false });
  require('fs').writeFileSync(out + '13_vote1_mobile.png', Buffer.from(mobileShot.data, 'base64'));
  await b.close();
})().catch(e => { console.error(e); process.exit(1); });
