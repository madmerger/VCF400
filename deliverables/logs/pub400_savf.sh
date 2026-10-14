#!/bin/bash
# Phase 1 record-only step: FTP the original save file to ASHIBATA1/VCFV1R3 and
# DSPSAVF it once. RSTLIB/RSTOBJ are not attempted (CPD0032, no authority on PUB400);
# the source rebuild (pub400_build.sh) is the canonical procedure.
set -euo pipefail
REPO=${REPO:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}
LOGDIR=$REPO/deliverables/logs
SAVF=$REPO/savf/VCFV1R3.SAVF
NETRC=$(mktemp); chmod 600 "$NETRC"; trap 'rm -f "$NETRC"' EXIT
printf 'machine pub400.com login %s password %s\n' "$PUB400_LOGIN" "$PUB400_PW" > "$NETRC"
FLOG=$LOGDIR/phase1_02_ftp_savf.log
{
  echo "=== FTP SAVF $(date -u +%FT%TZ) ==="
  echo "local file: savf/VCFV1R3.SAVF ($(stat -c %s "$SAVF") bytes, sha256 $(sha256sum "$SAVF" | cut -c1-16)...)"
  echo "--- ensure save file ASHIBATA1/VCFV1R3 exists and is empty ---"
  "$HOME/p400" "system 'CHKOBJ OBJ(ASHIBATA1/VCFV1R3) OBJTYPE(*FILE)' >/dev/null 2>&1 || system 'CRTSAVF FILE(ASHIBATA1/VCFV1R3)'; system 'CLRSAVF FILE(ASHIBATA1/VCFV1R3)'" 2>&1 | grep -E 'CP[CFD][0-9]{4}' || true
  echo "--- binary transfer (FTP port 21, BINARY) ---"
  set +e; curl -sS --netrc-file "$NETRC" --ftp-method nocwd -Q "site namefmt 1" -T "$SAVF" "ftp://pub400.com//QSYS.LIB/ASHIBATA1.LIB/VCFV1R3.FILE" -w 'bytes uploaded: %{size_upload}\n'; rc=$?; set -e
  echo "transfer exit status: $rc"
} 2>&1 | tee "$FLOG"
DLOG=$LOGDIR/phase1_03_dspsavf.log
{
  echo "=== DSPSAVF $(date -u +%FT%TZ) ==="
  echo "--- DSPSAVF FILE(ASHIBATA1/VCFV1R3) OUTPUT(*PRINT) ---"
  "$HOME/p400" "system 'DSPSAVF FILE(ASHIBATA1/VCFV1R3) OUTPUT(*PRINT)'" 2>&1 | grep -vE 'WELCOME|polite|logged|support|pub400\.com|^\*|Enter your password|other users' || true
} | tee "$DLOG"
