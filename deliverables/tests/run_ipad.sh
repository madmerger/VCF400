#!/bin/sh
# Phase 5 runner: execute cases.json against the iPad app (XCUITest on the iPad simulator).
#   ./run_ipad.sh [CV-01,CV-04]            results -> results/ipad.json, log -> results/ipad_xcuitest.log
# The app is launched with -VCF_RESET so every run starts from the common baseline.
set -eu
HERE=$(cd "$(dirname "$0")" && pwd)
IPAD="$HERE/../../ipad"
RESULTS="$HERE/results"
mkdir -p "$RESULTS"
rm -f "$RESULTS/ipad.sqlite" "$RESULTS/ipad.sqlite-wal" "$RESULTS/ipad.sqlite-shm"
VCF_XCODEBUILD_ACTION="${VCF_XCODEBUILD_ACTION:-test}"

cd "$IPAD"
[ -d VCF400.xcodeproj ] || xcodegen generate
if [ "$VCF_XCODEBUILD_ACTION" = "test-without-building" ]; then
  XCTESTRUN=$(find build/Build/Products -maxdepth 1 -name '*.xctestrun' -print -quit)
  [ -n "$XCTESTRUN" ] || { echo "xctestrun not found; run build-for-testing first" >&2; exit 1; }
  RUNFILE="$XCTESTRUN"
  set_env() {
    if /usr/libexec/PlistBuddy -c "Print :VCF400UITests:EnvironmentVariables:$1" "$RUNFILE" >/dev/null 2>&1; then
      /usr/libexec/PlistBuddy -c "Set :VCF400UITests:EnvironmentVariables:$1 $2" "$RUNFILE"
    else
      /usr/libexec/PlistBuddy -c "Add :VCF400UITests:EnvironmentVariables:$1 string $2" "$RUNFILE"
    fi
  }
  set_env VCF_CASES "$HERE/cases.json"
  set_env VCF_RESULTS "$RESULTS/ipad.json"
  set_env VCF_DB "$RESULTS/ipad.sqlite"
  set_env VCF_ONLY "${1:-}"
  xcodebuild test-without-building -xctestrun "$RUNFILE" \
    -destination "platform=iOS Simulator,name=${VCF_SIM:-iPad Pro 13-inch (M5)}" \
    -only-testing:VCF400UITests/CrossValidationUITests 2>&1
else
  VCF_CASES="$HERE/cases.json" VCF_RESULTS="$RESULTS/ipad.json" VCF_DB="$RESULTS/ipad.sqlite" VCF_ONLY="${1:-}" \
  xcodebuild -project VCF400.xcodeproj -scheme VCF400 \
    -destination "platform=iOS Simulator,name=${VCF_SIM:-iPad Pro 13-inch (M5)}" \
    -derivedDataPath build \
    -only-testing:VCF400UITests/CrossValidationUITests \
    test 2>&1
fi | tee "$RESULTS/ipad_xcuitest.log" | grep --line-buffered -E '^\[ipad\]|^    ->|^    !!|^----- ipad|^CV-|baseline:|final DB:|error:|\*\* TEST'
