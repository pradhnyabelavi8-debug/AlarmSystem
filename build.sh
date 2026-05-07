#!/usr/bin/env bash
# build.sh — Compile and optionally run the Alarm System
set -e

SRC="src"
OUT="out"

echo "── Compiling Java sources ──"
mkdir -p "$OUT"
javac -d "$OUT" $(find "$SRC" -name "*.java")
echo "✅  Compilation successful. Classes in ./$OUT"

echo ""
echo "To run the interactive CLI:"
echo "  java -cp $OUT alarm.Main"
echo ""
echo "To run the automated demo:"
echo "  java -cp $OUT alarm.Main demo"
echo ""
echo "To run the unit tests:"
echo "  java -cp $OUT alarm.AlarmManagerTest"
