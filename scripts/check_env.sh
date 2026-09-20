#!/usr/bin/env bash
# ตรวจสภาพแวดล้อม + ทดสอบ Lang 1b (สำหรับ jqwik)
set -u
echo "== Java =="; java -version 2>&1 | head -1; echo "JAVA_HOME=$JAVA_HOME"
echo "== Timezone =="; date +"%Z (%z)"
echo "== Defects4J =="; defects4j info -p Lang -b 1 | head -15
echo "== Python =="; python3 --version

W=/work/Lang_1b
rm -rf "$W"
echo "== checkout Lang 1b =="
defects4j checkout -p Lang -v 1b -w "$W" || exit 1
cd "$W" || exit 1
echo "== compile =="
defects4j compile || exit 1
echo "== trigger test (คาดว่า FAIL บน buggy) =="
TRIG=$(defects4j export -p tests.trigger)
echo "$TRIG"
defects4j test -t "$TRIG"
echo "== classes.modified =="
defects4j export -p classes.modified
