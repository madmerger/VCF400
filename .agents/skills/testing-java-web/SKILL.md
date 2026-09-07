---
name: testing-java-web
description: Run local VCF400 Java Web UI smoke tests without connecting to IBM i or PUB400.
---

# Java Web UI testing

## Setup
- On macOS, run from `java/` with `JAVA_HOME=$(/usr/libexec/java_home -v 17)`.
- Build with `mvn -q -DskipTests package`, then run `java -jar target/vcf400-web-*.jar`.
- Default URL is `http://localhost:8080`; the application seeds an H2 file database in `java/data/`.
- No login credentials are needed. `/` opens the menu with the default ${PUB400_LOGIN} profile. Expand the profile details at the bottom to switch to MM2024 shared-terminal mode.
- H2 data survives restarts. Choose a fresh badge for successful-vote tests; do not delete existing data without permission. Comment IDs must be read from the success screen, not assumed.

## Browser
- If the browser tool is unavailable, use Playwright with a headed Chromium. On the macOS test host, Playwright may be installed under `~/pwtools/node_modules` (`NODE_PATH=~/pwtools/node_modules`).
- Maximize the headed window before recording, using a CDP session and `Browser.setWindowBounds` with `windowState: 'maximized'`.
- Keep browser interaction through UI clicks/fills/keyboard events. Use navigation waits around server-rendered form submissions and visually inspect screenshots.
- macOS notification banners may cover the top-right of the recording; dismiss them before starting when possible.

## Routes and behavioral expectations
- Main menu: 1 LEARN, 11 vote, 12 add guestbook, 13 read guestbook, 80 sign off. 90 is the out-of-scope administration menu, not sign off. Number input supports leading zeroes; displayed numbers are not zero-padded.
- Click menu options or type the option and Enter. Vote/add/read first show NTRSTIT instructions.
- Vote field IDs: `inputBadge`, `inExhb`, `inputAward`. Use MM2024 when testing editable exhibit input.
- Primary submit/cancel keys are F5/F12; LEARN uses F5 forward, F8 back, F3 exit. Buttons also expose these key labels.
- LEARN's `EXTRA='END'` row is a sentinel, not a displayed page: forwarding from seeded Page 2 immediately returns to the menu.
- Guestbook IDs beyond the final record fall back to the final record. MM2024 can read all exhibits. A different kiosk profile gets `This comment is not part of this guestbook.` when the returned record belongs to another exhibit.
- Kiosk links are inside the profile details: ${PUB400_LOGIN} shows 1/2/3/4; DEMO400 shows 1/3/4; NOVOTE shows 3/4.
- Hidden kiosk option 7 opens the exit prompt. The local seeded password is defined in `java/src/main/resources/data.sql`; entering it returns to VCFMAIN.
- Java-only testing requires no PUB400 interaction. Do not run the cross-environment scripts for this scope.

## Devin Secrets Needed
None for local Java Web testing.
