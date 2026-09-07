#!/bin/bash
# Phase 5 formal cross-validation: PUB400 -> Java Web -> iPad, recorded as ONE video.
#   ./run_all.sh                 (records screen with ffmpeg to ../video/VCF400_cross_validation.mp4)
#   NO_RECORD=1 ./run_all.sh     (dry run without recording)
# Prerequisites: Java app running on $VCF_URL (default http://localhost:8080), iPad simulator booted,
#                PUB400_LOGIN / PUB400_PW in the environment, tn5250 installed, NODE_PATH pointing at playwright.
set -u -o pipefail
HERE=$(cd "$(dirname "$0")" && pwd)
VIDEO_DIR="$HERE/../video"
RESULTS="$HERE/results"
mkdir -p "$VIDEO_DIR" "$RESULTS"
VIDEO="$VIDEO_DIR/VCF400_cross_validation.mp4"
STAMP=$(date -u +%Y-%m-%dT%H:%M:%SZ)

step() { printf '\n\033[1;36m######## %s  (%s)\033[0m\n' "$1" "$(date -u +%H:%M:%SZ)"; }

LOG="$RESULTS/run_all.log"
: > "$LOG"
exec > >(tee -a "$LOG") 2>&1

# --- 1. start recording + live log window (right side of the screen) ----------
if [ -z "${NO_RECORD:-}" ]; then
  cp "$HERE/logview.html" "$RESULTS/logview.html"
  python3 -m http.server 8765 --bind 127.0.0.1 --directory "$RESULTS" >/dev/null 2>&1 &
  HTTPD=$!
  node "$HERE/logview.js" http://127.0.0.1:8765/logview.html &
  LOGVIEW=$!
  sleep 3
  ffmpeg -y -loglevel error -f avfoundation -framerate 15 -capture_cursor 1 -i "0" \
    -pix_fmt yuv420p -preset veryfast "$VIDEO" &
  FFMPEG=$!
  sleep 2
fi

step "VCF/400 Phase 5 cross-validation start $STAMP  (37 common cases: cases.json)"

# --- 2. PUB400 (tn5250) --------------------------------------------------------
step "1/3 PUB400  (ASHIBATA2, tn5250 -> pub400.com)  reset baseline + 37 cases"
(cd "$HERE" && python3 -u run_pub400.py 2>&1 | tee "$RESULTS/pub400_run.log")

# --- 3. Java Web (headed Chromium via Playwright) -------------------------------
step "2/3 Java Web  (Spring Boot ${VCF_URL:-http://localhost:8080}, headed Chromium)  reset baseline + 37 cases"
(cd "$HERE" && node run_java.js 2>&1 | tee "$RESULTS/java_run.log")

# --- 4. iPad (XCUITest on the simulator) ----------------------------------------
step "3/3 iPad  (SwiftUI on iPad Pro 13-inch simulator, XCUITest)  -VCF_RESET + 37 cases"
open -a Simulator
(cd "$HERE" && ./run_ipad.sh 2>&1 | tee "$RESULTS/ipad_run.log")

# --- 5. compare ---------------------------------------------------------------
step "compare expected vs observed for PUB400 / Java / iPad"
(cd "$HERE" && python3 compare.py 2>&1 | tee "$RESULTS/compare.log")
RC=$?
step "cross-validation finished (rc=$RC)"
sleep 3

if [ -z "${NO_RECORD:-}" ]; then
  kill -INT "$FFMPEG"; wait "$FFMPEG"
  kill "$LOGVIEW" "$HTTPD" 2>/dev/null
  rm -f "$RESULTS/logview.html"
  echo "video: $VIDEO"
fi
exit $RC
