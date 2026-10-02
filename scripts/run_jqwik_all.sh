#!/usr/bin/env bash
# Full jqwik evaluation, run INSIDE the container:
#   docker compose -f docker/docker-compose.yml exec sqa bash scripts/run_jqwik_all.sh Lang
# Arg 1: project (default Lang, or "all").  Env: JOBS (default 4), SEEDS (default 1,2,3,4,5)
set -euo pipefail
cd "$(dirname "$0")/.."
PROJECT="${1:-Lang}"
JOBS="${JOBS:-4}"
SEEDS="${SEEDS:-1,2,3,4,5}"

echo "== 0) controls: the pipeline itself must be correct before we trust any number =="
python3 scripts/run_jqwik.py -p Lang -b 1,3 --seeds 1 --tests-root jqwik/Controls/Positive   --tag control_positive
python3 scripts/run_jqwik.py -p Lang -b 1,3,5 --seeds 1 --tests-root jqwik/Controls/Negative --tag control_negative
python3 scripts/run_jqwik.py -p Lang -b 1,3 --seeds 1 --tests-root jqwik/Controls/FalseAlarm --tag control_falsealarm
python3 scripts/summarize_jqwik.py --check-controls || { echo "CONTROLS FAILED - fix the pipeline first"; exit 1; }

echo "== 1) Round 1: tries=1000 =="
python3 scripts/run_jqwik.py -p "$PROJECT" --round 1 --tries 1000  --seeds "$SEEDS" --jobs "$JOBS"
echo "== 2) Round 2: tries=10000 =="
python3 scripts/run_jqwik.py -p "$PROJECT" --round 2 --tries 10000 --seeds "$SEEDS" --jobs "$JOBS"

echo "== 3) summary =="
python3 scripts/summarize_jqwik.py
