#!/usr/bin/env bash
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
TARGET="${1:?Usage: ./install-preview.sh <TV IP> [port]}:${2:-5555}"
APK="$HERE/BlueGlassHome.apk"
if [ ! -f "$APK" ]; then APK="$HERE/app/build/outputs/apk/release/app-release.apk"; fi
if [ ! -f "$APK" ]; then echo "APK missing; run ./build.sh first" >&2; exit 1; fi
adb connect "$TARGET"
PREVIOUS=$(adb -s "$TARGET" shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME | tail -n 1 | tr -d '\r')
adb -s "$TARGET" install -r "$APK"
if [[ "$PREVIOUS" =~ ^[A-Za-z0-9_.]+/[A-Za-z0-9_.$]+$ ]] && [[ "$PREVIOUS" != android/* ]]; then
  adb -s "$TARGET" shell cmd package set-home-activity --user 0 "$PREVIOUS"
fi
adb -s "$TARGET" shell am start -W -n com.doubao.glasshome/.MainActivity
