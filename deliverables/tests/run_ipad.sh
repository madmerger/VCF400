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

cd "$IPAD"
[ -d VCF400.xcodeproj ] || xcodegen generate
TEST_RUNNER_VCF_CASES="$HERE/cases.json" \
TEST_RUNNER_VCF_RESULTS="$RESULTS/ipad.json" \
TEST_RUNNER_VCF_DB="$RESULTS/ipad.sqlite" \
TEST_RUNNER_VCF_ONLY="${1:-}" \
xcodebuild -project VCF400.xcodeproj -scheme VCF400 \
  -destination "platform=iOS Simulator,name=${VCF_SIM:-iPad Pro 13-inch (M5)}" \
  -derivedDataPath build \
  -only-testing:VCF400UITests/CrossValidationUITests \
  test 2>&1 | tee "$RESULTS/ipad_xcuitest.log" | grep --line-buffered -E '^\[ipad\]|^    ->|^    !!|^----- ipad|^CV-|baseline:|final DB:|error:|\*\* TEST'
