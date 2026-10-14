#!/bin/bash
# Reusable DB seed/dump helper for PUB400 library ASHIBATA2.
#
#   pub400_db.sh reset          -> re-run the baseline seed (pub400_seed.sql)
#   pub400_db.sh dump           -> dump VOTINGDB / GUESTBKDB / SETTINGS / EXHBDB / LRN400STR / AWARDDB
#   pub400_db.sh sql "<SQL>"    -> run an arbitrary SQL statement via PASE db2
#
# Auth comes from env PUB400_LOGIN / PUB400_PW (never printed). SQL is sent to
# PUB400 over SSH (port 2222) and executed with PASE `qsh -c 'db2 -f <file>'`.
set -euo pipefail
LIB=${VCF_PUB400_LIB:-ASHIBATA2}
SEED="$(cd "$(dirname "$0")" && pwd)/pub400_seed.sql"
export SSHPASS=$PUB400_PW
SSH="sshpass -e ssh -p 2222 -o StrictHostKeyChecking=no -o ConnectTimeout=20 $PUB400_LOGIN@pub400.com"
# Drop only SSH banner/prompt/empty lines (each starts with '*'); data rows are kept. Banner is:
#   *****...\n* WELCOME TO PUB400.COM ...\n* - ... https://pub400.com\n*****...\n-> Enter your password
FILTER='^\*|Enter your password|^\s*$'

run_sql_file() { # $1 = local .sql file
  local b64 remote
  b64=$(base64 -w0 "$1")
  remote="/tmp/${LIB}_$$_seed.sql"
  $SSH "echo $b64 | /QOpenSys/pkgs/bin/base64 -d > $remote && /QOpenSys/usr/bin/qsh -c 'db2 -f $remote'; rc=\$?; rm -f $remote; exit \$rc" \
    | grep -vE "$FILTER"
}

run_sql_str() {
  local f; f=$(mktemp --suffix=.sql)
  printf '%s;\n' "$1" > "$f"
  run_sql_file "$f"
  rm -f "$f"
}

cmd=${1:-dump}
case "$cmd" in
  reset) run_sql_file "$SEED" ;;
  sql)   shift; run_sql_str "$*" ;;
  dump)
    run_sql_str "SELECT * FROM $LIB.VOTINGDB ORDER BY BADGENBR"
    run_sql_str "SELECT * FROM $LIB.GUESTBKDB ORDER BY CMTID"
    run_sql_str "SELECT * FROM $LIB.SETTINGS ORDER BY SETTING"
    run_sql_str "SELECT * FROM $LIB.EXHBDB ORDER BY EXHBDBID"
    run_sql_str "SELECT * FROM $LIB.LRN400STR ORDER BY PAGENBR"
    run_sql_str "SELECT * FROM $LIB.AWARDDB ORDER BY AWARDID"
    ;;
  *) echo "usage: $0 reset|dump|sql \"<SQL>\"" >&2; exit 2 ;;
esac
