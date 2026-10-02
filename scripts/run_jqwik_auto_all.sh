#!/usr/bin/env bash
# jqwik with AUTO-GENERATED properties on ALL Defects4J projects, run INSIDE the container.
#
#   one machine :  bash scripts/run_jqwik_auto_all.sh
#   3 machines  :  SHARD=1/3 bash scripts/run_jqwik_auto_all.sh      (member 1)
#                  SHARD=2/3 bash scripts/run_jqwik_auto_all.sh      (member 2)
#                  SHARD=3/3 bash scripts/run_jqwik_auto_all.sh      (member 3)
#                  -> commit results/jqwik_auto_shard*.csv, then on any machine:
#                     python3 scripts/summarize_jqwik.py --csv 'jqwik_auto*.csv'
#
# Env: PROJECTS (default: all 17), JOBS (default: nproc-1), SEEDS (default 1,2,3),
#      TRIES_R1 (default 200), TRIES_R2 (default 1000)
set -euo pipefail
cd "$(dirname "$0")/.."
PROJECTS="${PROJECTS:-Chart Cli Closure Codec Collections Compress Csv Gson JacksonCore JacksonDatabind JacksonXml Jsoup JxPath Lang Math Mockito Time}"
JOBS="${JOBS:-$(( $(nproc) > 1 ? $(nproc) - 1 : 1 ))}"
SEEDS="${SEEDS:-1,2,3}"
TRIES_R1="${TRIES_R1:-200}"
TRIES_R2="${TRIES_R2:-1000}"
SHARD_ARG=()
[ -n "${SHARD:-}" ] && SHARD_ARG=(--shard "$SHARD")

echo "== controls (hand-written, Lang) =="
python3 scripts/run_jqwik.py -p Lang -b 1,3 --seeds 1 --tests-root jqwik/Controls/Positive   --tag control_positive
python3 scripts/run_jqwik.py -p Lang -b 1,3,5 --seeds 1 --tests-root jqwik/Controls/Negative --tag control_negative
python3 scripts/run_jqwik.py -p Lang -b 1,3 --seeds 1 --tests-root jqwik/Controls/FalseAlarm --tag control_falsealarm
python3 scripts/summarize_jqwik.py --check-controls || { echo "CONTROLS FAILED - fix the pipeline first"; exit 1; }

for P in $PROJECTS; do
  echo "== $P : round 1 (tries=$TRIES_R1) =="
  python3 scripts/run_jqwik.py -p "$P" --auto --round 1 --tries "$TRIES_R1" --seeds "$SEEDS" --jobs "$JOBS" "${SHARD_ARG[@]}"
done
for P in $PROJECTS; do
  echo "== $P : round 2 (tries=$TRIES_R2) =="
  python3 scripts/run_jqwik.py -p "$P" --auto --round 2 --tries "$TRIES_R2" --seeds "$SEEDS" --jobs "$JOBS" "${SHARD_ARG[@]}"
done

python3 scripts/summarize_jqwik.py --csv 'jqwik_auto*.csv'
