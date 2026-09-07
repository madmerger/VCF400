#!/bin/bash
# Phase 5/6 cross-validation: run PUB400, Java Web, and iPad independently.
#   ./run_all.sh                 (runs all environments and records three videos)
#   NO_RECORD=1 ./run_all.sh     (runs all environments without recording)
# Prerequisites: Java app running at $VCF_URL (preferred; BASE is also accepted,
# default http://localhost:8080), iPad
# simulator available, PUB400_LOGIN / PUB400_PW, tn5250, Playwright, and ffmpeg.
set -u -o pipefail
HERE=$(cd "$(dirname "$0")" && pwd)
ROOT=$(cd "$HERE/../.." && pwd)
VIDEO_DIR="$HERE/../video"
RESULTS="$HERE/results"
mkdir -p "$VIDEO_DIR" "$RESULTS"
STAMP=$(date -u +%Y-%m-%dT%H:%M:%SZ)
RECORD=1
[ -n "${NO_RECORD:-}" ] && RECORD=0
FAIL=0
EXIT_CODE=0

step() { printf '\n\033[1;36m######## %s  (%s)\033[0m\n' "$1" "$(date -u +%H:%M:%SZ)"; }

LOG="$RESULTS/run_all.log"
: > "$LOG"
exec > >(tee -a "$LOG") 2>&1

run_logged() {
  local label=$1
  local log=$2
  shift 2
  "$@" 2>&1 | tee "$log"
  local -a status=("${PIPESTATUS[@]}")
  if [ "${status[0]}" -ne 0 ]; then
    echo "FAIL: $label exited ${status[0]}"
    FAIL=$((FAIL + 1))
  fi
  if [ "${status[1]}" -ne 0 ]; then
    echo "FAIL: $label log capture exited ${status[1]}"
    FAIL=$((FAIL + 1))
  fi
}

trap '
  trap - EXIT
  if ! (cd "$HERE" && python3 pub400_db.py reset); then
    echo "FAIL: final PUB400 baseline reset"
    EXIT_CODE=1
  fi
  exit "$EXIT_CODE"
' EXIT

step "VCF/400 Phase 5/6 cross-validation start $STAMP (37 common cases)"

step "1/3 PUB400 (ASHIBATA2, tn5250 -> pub400.com) 37 cases"
rm -f "$RESULTS/pub400.json"
if [ "$RECORD" -eq 1 ]; then
  run_logged "PUB400 runner" "$RESULTS/pub400_run.log" \
    bash -c "cd \"$HERE\" && python3 -u run_pub400.py --frames \"$RESULTS/pub400_frames.json\""
  run_logged "PUB400 video" "$RESULTS/pub400_video.log" \
    env NODE_PATH="$HOME/pwtools/node_modules" node "$ROOT/deliverables/tools/frames_to_video.js" \
    "$RESULTS/pub400_frames.json" "$VIDEO_DIR/1_PUB400_RPG.mp4"
else
  run_logged "PUB400 runner" "$RESULTS/pub400_run.log" \
    bash -c "cd \"$HERE\" && python3 -u run_pub400.py"
fi

step "2/3 Java Web (${VCF_URL:-http://localhost:8080}, headed Chromium) reset + 37 cases"
rm -f "$RESULTS/java.json"
if [ "$RECORD" -eq 1 ]; then
  run_logged "Java runner/video" "$RESULTS/java_run.log" \
    env NODE_PATH="$HOME/pwtools/node_modules" node "$HERE/run_java.js" \
    --record "$HOME/vcf_rec/java_webm" --out "$VIDEO_DIR/2_Java_Web.mp4"
else
  run_logged "Java runner" "$RESULTS/java_run.log" \
    env NODE_PATH="$HOME/pwtools/node_modules" node "$HERE/run_java.js"
fi

step "3/3 iPad (iPad Pro 13-inch simulator, XCUITest) reset + 37 cases"
rm -f "$RESULTS/ipad.json"
open -a Simulator 2>/dev/null || true
if [ "$RECORD" -eq 1 ]; then
  run_logged "iPad runner/video" "$RESULTS/ipad_run.log" \
    env VCF_RAW_DIR="$HOME/vcf_rec" "$HERE/record_ipad.sh" "" "$VIDEO_DIR/3_iPad.mp4"
else
  run_logged "iPad runner" "$RESULTS/ipad_run.log" \
    bash -c "cd \"$HERE\" && ./run_ipad.sh"
fi

step "compare expected vs observed for PUB400 / Java / iPad"
rm -f "$RESULTS/compare.json"
(cd "$HERE" && python3 compare.py) 2>&1 | tee "$RESULTS/compare.log"
compare_status=("${PIPESTATUS[@]}")
RC=${compare_status[0]}
if [ "$RC" -ne 0 ]; then
  echo "FAIL: compare.py exited $RC"
  FAIL=$((FAIL + 1))
fi
if [ "${compare_status[1]}" -ne 0 ]; then
  echo "FAIL: compare log capture exited ${compare_status[1]}"
  FAIL=$((FAIL + 1))
fi
if [ "$RC" -ne 0 ] || [ "$FAIL" -ne 0 ]; then
  EXIT_CODE=1
fi
step "cross-validation finished (compare_rc=$RC, failures=$FAIL, exit=$EXIT_CODE)"

if [ "$RECORD" -eq 1 ]; then
  echo "videos:"
  echo "  $VIDEO_DIR/1_PUB400_RPG.mp4"
  echo "  $VIDEO_DIR/2_Java_Web.mp4"
  echo "  $VIDEO_DIR/3_iPad.mp4"
fi
exit "$EXIT_CODE"
