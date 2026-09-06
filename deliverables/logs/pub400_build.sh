#!/bin/bash
# Phase 1 fallback build: RSTLIB/RSTOBJ are not authorized on PUB400 (CPD0032),
# so the VCF library is rebuilt from the GitHub source into the dedicated
# library ASHIBATA2 (objects) using source PFs in ASHIBATA1.
#
#   SRCLIB = ASHIBATA1 (source physical files, build CL, compile listings)
#   OBJLIB = ASHIBATA2 (compiled objects + data; replaces the hard-coded VCF)
#
# PUB400 runs every `system` call from PASE in a separate job, so the whole
# build is generated as one CL program (BUILDVCF) that sets the library list
# once; PASE `system` echoes all spooled compile listings, which are kept in the log.
#
# Requires: PUB400_LOGIN / PUB400_PW in the environment, curl, iconv.
set -euo pipefail
REPO=${REPO:-$HOME/repos/VCF400}
SRCLIB=ASHIBATA1
OBJLIB=ASHIBATA2
STAGE=$(mktemp -d)
LOGDIR=$REPO/deliverables/logs
LOG=$LOGDIR/phase1_05_build.log
NETRC=$(mktemp); printf 'machine pub400.com login %s password %s\n' "$PUB400_LOGIN" "$PUB400_PW" > "$NETRC"
ftpput_ascii() { curl -sS -B --netrc-file "$NETRC" --ftp-method nocwd -Q "site namefmt 1" -T "$1" "ftp://pub400.com/$2"; }
ftpget_ascii() { curl -sS -B --netrc-file "$NETRC" --ftp-method nocwd -Q "site namefmt 1" "ftp://pub400.com/$1" -o "$2"; }

echo "=== $(date -u +%FT%TZ) staging sources" | tee "$LOG"
# 1) stage: strip SEU seq/date prefix + NUL padding, retarget hard-coded library
#    VCF -> OBJLIB (CL, menu commands) or *LIBL (DDS REFFLD), Latin-1 for EBCDIC.
stage() { # $1=srcdir $2=ext $3=target srcpf $4=sed expression for library retarget
  mkdir -p "$STAGE/$3"
  for f in "$REPO/$1"/*."$2"; do
    m=$(basename "$f" ."$2" | tr a-z A-Z)
    tr -d '\000' < "$f" | sed -E 's/^[0-9]{6} [0-9]{6} ?//' | sed -E "$4" \
      | iconv -f UTF-8 -t ISO-8859-1 > "$STAGE/$3/$m"
    echo "  staged $1/$(basename "$f") -> $SRCLIB/$3($m)" | tee -a "$LOG"
  done
}
stage QDDSSRC   pf     QDDSSRC   ''
stage QSDASRC   dspf   QSDASRC   's# VCF/([A-Z])# \1#g'
stage QRLUSRC   dds    QRLUSRC   's# VCF/([A-Z])# \1#g'
stage QMNUSRC   mnudds QMNUSRC   ''
stage QMNUSRC   mnucmd QMNUSRC   "s#VCF/#$OBJLIB/#g; s#vcf/#$OBJLIB/#g"
stage QCLSRC    clp    QCLSRC    "s#VCF/#$OBJLIB/#g"
stage QCMDSRC   CMD    QCMDSRC   ''
stage QRPGLESRC rpgle  QRPGLESRC ''

# 2) generate the build CL program
CL="$STAGE/QCLSRC/BUILDVCF"
emit() { # wrap long CL statements with '+' continuation (source record is 100 cols)
  for line in "$@"; do
    while [ ${#line} -gt 90 ]; do
      cut=$(printf '%s' "${line:0:88}" | awk '{print length($0)-length($NF)}')
      printf '%s+\n' "${line:0:$cut}" >> "$CL"; line="               ${line:$cut}"
    done
    printf '%s\n' "$line" >> "$CL"
  done
}
cl_cmd() { # $1,$2 kept for call-site readability (spool file / object)  $3=command
  emit "             $3" \
       "             MONMSG MSGID(CPF0000 RNS0000)"
}
cl_quiet() { emit "             $1" "             MONMSG MSGID(CPF0000)"; }
: > "$CL"
emit "             PGM" \
     "             CHGLIBL LIBL($OBJLIB $SRCLIB QGPL QTEMP) CURLIB($OBJLIB)" \
# physical files: create only if missing (existing data is retained)
for f in AWARDDB BMOVDB EXHBDB GUESTBKDB LRN400STR SECOFRS SETTINGS VOTINGDB; do
  emit "             CHKOBJ OBJ($OBJLIB/$f) OBJTYPE(*FILE)" \
       "             MONMSG MSGID(CPF9801) EXEC(DO)"
  cl_cmd QPDDSSRC "$f" "CRTPF FILE($OBJLIB/$f) SRCFILE($SRCLIB/QDDSSRC) SRCMBR($f) SIZE(*NOMAX)"
  emit "             ENDDO"
done
# display files
for f in ADMSCR ADMVOTERES BMOVSCR CREDITSSCR EXHBMENUSC GUESTBKSCR INTERSCR LRN400SCR LRNAUTO PASSSCR SETUPSCR TESTSUITE VOTESCR; do
  cl_quiet "DLTF FILE($OBJLIB/$f)"
  extra=""; [ "$f" = LRNAUTO ] && extra=" WAITRCD(10)"
  cl_cmd QPDDSSRC "$f" "CRTDSPF FILE($OBJLIB/$f) SRCFILE($SRCLIB/QSDASRC) SRCMBR($f)$extra"
done
# printer files
for f in CMTPRTF CMTTST TESTPRT2 VOTEPRTF; do
  cl_quiet "DLTF FILE($OBJLIB/$f)"
  cl_cmd QPDDSSRC "$f" "CRTPRTF FILE($OBJLIB/$f) SRCFILE($SRCLIB/QRLUSRC) SRCMBR($f)"
done
# menus: DSPF + MSGF(USRnnnn = command for option nnnn) + *MENU
for m in VCFMAIN MAIN ADMMAIN; do
  cl_quiet "DLTMNU MENU($OBJLIB/$m)"
  cl_quiet "DLTF FILE($OBJLIB/$m)"
  cl_quiet "DLTMSGF MSGF($OBJLIB/${m}QQ)"
  cl_cmd QPDDSSRC "$m" "CRTDSPF FILE($OBJLIB/$m) SRCFILE($SRCLIB/QMNUSRC) SRCMBR($m)"
  cl_quiet "CRTMSGF MSGF($OBJLIB/${m}QQ)"
  cl_quiet "CRTMNU MENU($OBJLIB/$m) TYPE(*DSPF) DSPF($OBJLIB/$m) MSGF($OBJLIB/${m}QQ)"
  tail -n +2 "$STAGE/QMNUSRC/${m}QQ" | while read -r opt cmd; do
    [ -z "$opt" ] && continue
    cl_quiet "ADDMSGD MSGID(USR$opt) MSGF($OBJLIB/${m}QQ) MSG('$cmd')"
  done
done
# RPG IV programs (same object set as the VCFV1R3 save file; OLD* members are excluded)
for p in NTRSTIT PARAMETER PRTLSTVOTE PRTLSTCMT PRINTER CREDITS BEEMOVIE ADDVOTE ADDGBCMT READGBCMT LRN400 LRN400AUT EXHBMENU ADMADDSOFR ADMCRTEXHB ADMHIDECMT ADMLRN400 ADMOFRLIST ADMSETTING ADMVOTERPT; do
  cl_quiet "DLTPGM PGM($OBJLIB/$p)"
  cl_cmd QSYSPRT "$p" "CRTBNDRPG PGM($OBJLIB/$p) SRCFILE($SRCLIB/QRPGLESRC) SRCMBR($p) DBGVIEW(*SOURCE)"
done
# CL launcher stubs
for p in VOTESTUB ADDGBSTUB READGBSTUB VCFSTUB LRN400STUB; do
  cl_quiet "DLTPGM PGM($OBJLIB/$p)"
  cl_cmd QSYSPRT "$p" "CRTCLPGM PGM($OBJLIB/$p) SRCFILE($SRCLIB/QCLSRC) SRCMBR($p)"
done
# commands
for c in STREXHB:VCFSTUB STREXHBEDT:ADMCRTEXHB STRVOTERPT:ADMVOTERPT STRCMTEDT:ADMHIDECMT; do
  cmd=${c%%:*}; pgm=${c##*:}
  cl_quiet "DLTCMD CMD($OBJLIB/$cmd)"
  cl_quiet "CRTCMD CMD($OBJLIB/$cmd) PGM($OBJLIB/$pgm) SRCFILE($SRCLIB/QCMDSRC) SRCMBR($cmd)"
done
emit "             DSPJOBLOG OUTPUT(*PRINT)" \
     "             MONMSG MSGID(CPF0000)" \
     "             ENDPGM"
cp "$CL" "$LOGDIR/pub400_BUILDVCF.clle"

# 3) upload sources (source PFs QSDASRC/QRLUSRC are created if missing)
/Users/devin/p400 "for f in QSDASRC QRLUSRC; do system \"CRTSRCPF FILE($SRCLIB/\$f) RCDLEN(112)\"; done; true" | tee -a "$LOG"
for d in "$STAGE"/*; do
  pf=$(basename "$d")
  for f in "$d"/*; do
    m=$(basename "$f")
    ftpput_ascii "$f" "/QSYS.LIB/$SRCLIB.LIB/$pf.FILE/$m.MBR" && echo "  uploaded $pf/$m" | tee -a "$LOG"
  done
done

# 4) compile + run the build program
echo "=== $(date -u +%FT%TZ) compiling on PUB400" | tee -a "$LOG"
/Users/devin/p400 "system 'DLTPGM $SRCLIB/BUILDVCF'; system 'CRTBNDCL PGM($SRCLIB/BUILDVCF) SRCFILE($SRCLIB/QCLSRC) SRCMBR(BUILDVCF)' && system 'CALL $SRCLIB/BUILDVCF'; echo rc=\$?" | tee -a "$LOG"

# 5) collect listings + object inventory
echo "=== $(date -u +%FT%TZ) collecting listings" | tee -a "$LOG"
# PASE `system` echoes every spooled file of the command to stdout, so the compile
# listings are already in $LOG; split them into one file per object locally.
rm -rf "$LOGDIR/pub400_listings"; mkdir -p "$LOGDIR/pub400_listings"
awk -v dir="$LOGDIR/pub400_listings" '
  /^ 5770(SS1|WDS) .*(Data Description|IBM ILE RPG|Control Language) +ASHIBATA[12]\/[A-Z0-9]+.*Page +1 *$/ {
    for (i=1;i<=NF;i++) if ($i ~ /^ASHIBATA[12]\//) { split($i,a,"/"); out=dir "/" a[2] ".txt" }
    if (out in seen) { } else { seen[out]=1; printf "" > out }
  }
  out != "" { print >> out }
  /E N D   O F   C O M P I L A T I O N/ { out="" }
' "$LOG"
{
  echo "--- compile results (from listings) ---"
  for f in "$LOGDIR"/pub400_listings/*.txt; do
    n=$(basename "$f" .txt)
    [ "$n" = JOBLOG ] && continue
    res=$(grep -aoE 'File [A-Z0-9]+ created|File [A-Z0-9]+ not created|Program [A-Z0-9]+ created in library [A-Z0-9]+|Program [A-Z0-9]+ placed in library [A-Z0-9]+\. [0-9]+ highest severity|Compilation stopped[^.]*\.' "$f" | sort -u | tr '\n' ';')
    echo "$n: $res"
  done
  echo "--- object inventory $OBJLIB ---"
  /Users/devin/p400 "system 'DSPOBJD OBJ($OBJLIB/*ALL) OBJTYPE(*ALL) DETAIL(*BASIC)' | grep -E '^ *[A-Z0-9]+ +\\*'"
} | tee -a "$LOG"
echo "=== $(date -u +%FT%TZ) done" | tee -a "$LOG"
rm -rf "$STAGE" "$NETRC"
