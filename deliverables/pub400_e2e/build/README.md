# PUB400 master source rebuild

Rebuilt the RPG application from `origin/master`, importing and adapting the
three build/DB tools from `origin/devin/1791959810-pub400-e2e-report`.
The source directories in this branch are unchanged from master.

## Reproduce

Set `PUB400_LOGIN` and `PUB400_PW` in the environment and provide `~/p400`.
Run from the repository root:

```sh
bash deliverables/pub400_e2e/tools/pub400_build.sh
python3 deliverables/pub400_e2e/tools/verify_build.py
```

The script derives 検証用ライブラリ（ソース/オブジェクト） from the login plus
`1` / `2`. Textual evidence uses `SRCLIB`, `OBJLIB`, and `USER` instead.
FTP is plain FTP on port 21, not FTPES. Credentials are passed from the
environment and are not written to an authentication file.

The generated `BUILDVCF` CL executes the build in one job, setting its library
list once. Existing physical files/data are retained; missing PFs would be
created before dependent files. All eight PFs already existed in this run.
There are no LF source files in master. Display/printer files, menu message
files and menus, 18 RPG programs, five ILE CL launchers, and four commands
were rebuilt in dependency order. Only named application objects are deleted.
`MONMSG` follows only deletion commands or `ADDMSGD` (`CPF2412`).

Upload-only source adaptations: remove source sequence/date prefixes and NUL
padding, encode as Latin-1 for FTP conversion, resolve DDS references through
the library list, retarget menu commands/CL stubs, and add `K DISK` to
PRTLSTVOTE's VOTINGDB F-spec. OLDVOTE, OLDADDVOTE, PRINTER and BEEMOVIE are
not compiled. Original RPG/CL source files are not modified.

## Evidence

- `build.log`: staging, transfers, single-job build output and object checks.
  The build returned `rc=0`. Console-only initial `CPF7302` messages meant
  source PFs already existed, not failed application compiles.
- `pub400_BUILDVCF.clle`: the generated CL, with library identifiers redacted.
- `pub400_listings/`: compiler listings, including all 18 RPG severity results.
- `objects_after.txt`: full post-build `DSPOBJD` inventory, including unrelated
  existing objects (retained, never deleted).
- `verify.txt`: exact `grep -Fqx` inventory checks (61 expected name/type pairs)
  and 18 highest-severity-00 results. The build additionally ran remote
  `CHKOBJ` checks for its 58 expected objects; three menu display files were
  checked separately and included in inventory verification.
- `dspsavf.log`: one binary SAVF upload to the absolute FTP URL
  `ftp://pub400.com//QSYS.LIB/<srclib>.LIB/VCFV1R3.FILE`, followed by
  `DSPSAVF FILE(<srclib>/VCFV1R3) OUTPUT(*PRINT)`. It reports SAVLIB VCF,
  V4R4M0, 11,160 records. No RSTLIB/RSTOBJ was attempted.
- `vcfmain.png`, `vcfmain.txt`: real tn5250 menu screen, not a local recreation.
  `AS/400 DEMO MENU`, `WELCOME TO...`, and the festival heading are visible.
  Xvfb :99 and Konsole were used; `ADDLIBLE <objlib>` then
  `GO <objlib>/VCFMAIN` opens the menu object (it is not a callable program).
  F3 returned to the system menu and SIGNOFF returned to the sign-on screen.

## Scope and limitations

No voting, guestbook writes, or other business scenarios were exercised; this
is a rebuild and main-menu smoke check only. Existing DB contents were retained;
the imported seed/reset helper was parameterized but deliberately not executed.
No PR was opened: this branch is pushed for the lead to open the new PR.
There is no Vercel deployment or web preview for the native RPG application.
