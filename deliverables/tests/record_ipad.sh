#!/bin/sh
set -eu

HERE=$(cd "$(dirname "$0")" && pwd)
ROOT=$(cd "$HERE/../.." && pwd)
SMOKE_DIR=${VCF_SMOKE_DIR:-/Users/devin/vcf_smoke}
UDID=${1:-}
OUTPUT=${2:-"$SMOKE_DIR/3_iPad.mp4"}
RAW_DIR=${VCF_RAW_DIR:-$(dirname "$OUTPUT")}
RAW="$RAW_DIR/$(basename "${OUTPUT%.mp4}.raw.mp4")"
RESULTS="$HERE/results"

if [ -z "$UDID" ]; then
  UDID=$(xcrun simctl list devices available | sed -n 's/.*iPad Pro 13-inch (M5) (\([[:xdigit:]-]\{36\}\)) .*/\1/p' | head -1)
fi
[ -n "$UDID" ] || { echo "iPad Pro 13-inch (M5) simulator not found" >&2; exit 1; }
mkdir -p "$SMOKE_DIR" "$RAW_DIR" "$RESULTS"

STATE=$(xcrun simctl list devices | sed -n "s/.*(\($UDID\)) (\(Booted\|Shutdown\)).*/\2/p" | head -1)
if [ "$STATE" != "Booted" ]; then
  xcrun simctl boot "$UDID" 2>/dev/null || true
  xcrun simctl bootstatus "$UDID" -b
fi

rm -f "$RAW" "$OUTPUT" "$RESULTS/ipad.json"
T0=$(python3 -c 'import time; print(f"{time.time():.6f}")')
printf '%s\n' "$T0" > "${RAW}.t0"
xcrun simctl io "$UDID" recordVideo --codec h264 -f "$RAW" &
REC_PID=$!
stop_recording() {
  kill -INT "$REC_PID" 2>/dev/null || true
  wait "$REC_PID" 2>/dev/null || true
}
trap stop_recording EXIT INT TERM

VCF_XCODEBUILD_ACTION=test-without-building \
VCF_SIM_UDID="$UDID" \
"$HERE/run_ipad.sh" "${VCF_ONLY:-}" 
stop_recording
trap - EXIT INT TERM

NODE_PATH="$HOME/pwtools/node_modules" node "$ROOT/deliverables/tools/overlay_ipad.js" \
  "$RAW" "$RESULTS/ipad.json" "$T0" "$OUTPUT"
echo "recording: $OUTPUT"
