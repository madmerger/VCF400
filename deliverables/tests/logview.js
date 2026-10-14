// Opens the live log viewer (logview.html served from results/) in a headed Chromium window on the right side of the
// screen so the recorded video shows the 5250 screens / case results while the three environments are driven.
//   NODE_PATH=~/pwtools/node_modules node logview.js http://localhost:8765/logview.html
const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch({ headless: false, args: ['--window-position=1000,0', '--window-size=600,1180'] });
  const ctx = await browser.newContext({ viewport: null });
  const p = await ctx.newPage();
  await p.goto(process.argv[2]);
  process.on('SIGTERM', async () => { await browser.close(); process.exit(0); });
  process.on('SIGINT', async () => { await browser.close(); process.exit(0); });
  await new Promise(() => {});
})();
