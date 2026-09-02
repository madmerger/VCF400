---
name: testing-vcf400
description: How to build, run and end-to-end test the VCF/400 Java (Spring Boot) migration under java/, including the 5250-style UI routes, seed data and known pitfalls.
---

# Testing the VCF/400 Java app

## Build / test / run (from repo root)
- Build: `cd java && mvn -q -DskipTests package` (Maven Central returns 429 here; a local mirror is already configured in `~/.m2/settings.xml` — do not remove it).
- Tests + coverage: `cd java && mvn verify` → Surefire summary at the end; JaCoCo HTML at `java/target/site/jacoco/index.html`.
- Run: `cd java && java -jar target/vcf400-java-0.0.1-SNAPSHOT.jar` → port 8080, wait for `Started Vcf400Application`. H2 is in-memory, so restarting the app resets all votes/comments (handy for repeatable tests: restart before recording).
- Stop: `pkill -f "vcf400[-]java"` — use the bracket form, otherwise pkill matches (and kills) your own shell whose command line contains the string.

## UI routes and seed data (src/main/resources/data.sql)
- `/` VCFMAIN main menu (profile select + numeric option; 80 → exhibit menu, 90 → /admin).
- `/menu?profile=GERTIE` EXHBMENU; option 1 = vote (only if exhibit ELIGIBLE=1 and setting ALWVOTE != N), 2 = LEARN/400, 3/4 = guestbook, 7 = exit kiosk (password `VCF400`).
- `/help?next=...` NTRSTIT instruction screen, click "RIGHT CTRL(=ENTER) - Continue".
- `/vote?profile=X`: badge / exhibitId (prefilled with the profile, editable) / award select. Messages come from `service/RpgMessages.java`.
- Seed exhibits: `GERTIE` (eligible), `NOVOTE` (ELIGIBLE=0 → "Exhibit ineligible for award"), security officer profile `MM2024`.
- `/guestbook/add|read?profile=X`, `/learn400?owner=LRN400STR`, `/learn400/auto`, `/admin`, `/admin/votes`, `/admin/spool` (in-memory print spool shows vote/comment tickets).
- LRN400STR seed pages: 1,2,3, page 4 = `JUMP`/`0006`, 5, 6 = `Goodbye.`/`END` → forward from page 3 must display page 6 directly. `PAGEBACK` returns to the recorded from-page (3), not 5.

## Known pitfalls
- Thymeleaf reserves `${session}`: templates that add a model attribute named `session` render blank because `${session}` resolves to the HttpSession instead. This bit `learn400.html` / `learn400-auto.html` once (fixed by renaming the attribute to `learn` in commit fc4516e, re-verified working). If any screen looks empty, compare the `/api/...` JSON with the HTML before blaming the service layer, and check for reserved model attribute names (`session`, `request`, `param`).
- `web/UiPagesTest` asserts rendered content for representative UI routes; continue checking both status and visible text when adding screens.
- Vote success message color: previously rendered with the `error` (red) class; now green `.success` (fc4516e). Distinguish success vs failure by text plus the THANK YOU box, not color alone.
- LEARN/400 resets `ended` before PAGEFWD/PAGEBACK; after F8 from the END page (6), the green `END` label is removed from page 3.
- LEARN/400 F3 Exit clears the HttpSession attribute and redirects to `/` for `LRN400STR` or `/menu?profile=...` for another owner.
- Sending a numeric input twice via a browser `type` action appends (e.g. "1" + "80" = "180"); clear the field first (click, Ctrl+A, Delete) before typing.
- For a visible terminal in recordings: `konsole` is installed but there is no window manager, so `wmctrl` fails. Launch with `konsole --nofork -e /bin/bash`, size/raise via `xdotool windowsize/windowmove/windowraise` and type into it with `xdotool type --window <id> "cmd\n"` (`windowactivate` is unsupported).

## Devin Secrets Needed
- None.
