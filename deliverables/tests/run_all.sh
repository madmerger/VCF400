#!/bin/bash
# Phase 5/6 cross-validation: run PUB400, Java Web, and iPad independently.
#   ./run_all.sh                 (runs all environments and records three videos)
#   NO_RECORD=1 ./run_all.sh     (runs all environments without recording)
# Prerequisites: Java app running on $VCF_URL (default http://localhost:8080), iPad
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

step() { printf '\n\033[1;36m######## %s  (%s)\033[0m\n' "$1" "$(date -u +%H:%M:%SZ)"; }

LOG="$RESULTS/run_all.log"
: > "$LOG"
exec > >(tee -a "$LOG") 2>&1

step "VCF/400 Phase 5/6 cross-validation start $STAMP (37 common cases)"

step "1/3 PUB400 (ASHIBATA2, tn5250 -> pub400.com) reset + 37 cases"
(cd "$HERE" && python3 pub400_db.py reset)
if [ "$RECORD" -eq 1 ]; then
  (cd "$HERE" && python3 -u run_pub400.py --frames "$RESULTS/pub400_frames.json") 2>&1 | tee "$RESULTS/pub400_run.log"
  NODE_PATH="$HOME/pwtools/node_modules" node "$ROOT/deliverables/tools/frames_to_video.js" \
    "$RESULTS/pub400_frames.json" "$VIDEO_DIR/1_PUB400_RPG.mp4"
  (cd "$HERE" && python3 pub400_db.py reset)
else
  (cd "$HERE" && python3 -u run_pub400.py) 2>&1 | tee "$RESULTS/pub400_run.log"
fi

step "2/3 Java Web (${VCF_URL:-http://localhost:8080}, headed Chromium) reset + 37 cases"
if [ "$RECORD" -eq 1 ]; then
  NODE_PATH="$HOME/pwtools/node_modules" node "$HERE/run_java.js" \
    --record "$HOME/vcf_rec/java_webm" --out "$VIDEO_DIR/2_Java_Web.mp4" \
    2>&1 | tee "$RESULTS/java_run.log"
else
  NODE_PATH="$HOME/pwtools/node_modules" node "$HERE/run_java.js" \
    2>&1 | tee "$RESULTS/java_run.log"
fi

step "3/3 iPad (iPad Pro 13-inch simulator, XCUITest) reset + 37 cases"
open -a Simulator 2>/dev/null || true
if [ "$RECORD" -eq 1 ]; then
  VCF_RAW_DIR="$HOME/vcf_rec" "$HERE/record_ipad.sh" "" "$VIDEO_DIR/3_iPad.mp4" \
    2>&1 | tee "$RESULTS/ipad_run.log"
else
  (cd "$HERE" && ./run_ipad.sh) 2>&1 | tee "$RESULTS/ipad_run.log"
fi

step "compare expected vs observed for PUB400 / Java / iPad"
(cd "$HERE" && python3 compare.py) 2>&1 | tee "$RESULTS/compare.log"
RC=${PIPESTATUS[0]}
step "cross-validation finished (rc=$RC)"

if [ "$RECORD" -eq 1 ]; then
  echo "videos:"
  echo "  $VIDEO_DIR/1_PUB400_RPG.mp4"
  echo "  $VIDEO_DIR/2_Java_Web.mp4"
  echo "  $VIDEO_DIR/3_iPad.mp4"
fi
exit "$RC"
