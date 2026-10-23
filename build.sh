#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
: "${ANDROID_HOME:?Set ANDROID_HOME to your Android SDK directory (platform 34 and build-tools 34.0.0)}"
./gradlew --no-daemon testReleaseUnitTest lintRelease assembleRelease --console=plain
echo "APK: app/build/outputs/apk/release/app-release.apk"
