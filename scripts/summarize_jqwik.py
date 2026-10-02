#!/usr/bin/env python3
"""Summarise results/jqwik_results.csv (one row per bug x seed) into per-bug and per-project tables.

  python3 scripts/summarize_jqwik.py                 # main results
  python3 scripts/summarize_jqwik.py --check-controls  # verify the pipeline with the control runs

Per bug (for one round/tries):
  detected_any   : fault detected with at least one seed
  detection_rate : detected seeds / valid seeds         (0<rate<1 -> "flaky", report it)
  false_alarm    : some property failed on the FIXED version (property is wrong)
Output: results/jqwik_summary_bugs.csv, results/jqwik_summary_projects.csv
"""
import argparse
import os
import sys
from pathlib import Path

import pandas as pd

REPO = Path(os.environ.get("REPO_ROOT", Path(__file__).resolve().parents[1]))
RES = REPO / "results"
INVALID = {"NO_PROPERTY", "TEST_COMPILE_FAIL_FIXED", "TEST_COMPILE_FAIL_BUGGY", "TOOL_ERROR",
           "CHECKOUT_ERROR", "D4J_COMPILE_ERROR", "RUNTIME_FAIL", "TIMEOUT_FIXED", "GENERATOR_ERROR"}


def load(name):
    """name may be a glob, e.g. 'jqwik_auto*.csv' merges the shard files of all team members."""
    files = sorted(RES.glob(name))
    files = [f for f in files if "summary" not in f.name]
    if not files:
        return None
    df = pd.concat([pd.read_csv(f, dtype=str) for f in files], ignore_index=True).fillna("")
    # keep the LAST row per (project, bug, round, tries, seed): reruns overwrite older rows
    df = df.drop_duplicates(["project", "bug_id", "round", "tries", "seed"], keep="last")
    df["fault_detected"] = df["fault_detected"].str.lower() == "true"
    df["valid"] = ~df["error_type"].isin(INVALID)
    for c in ("line_coverage_percent", "branch_coverage_percent", "total_time"):
        df[c] = pd.to_numeric(df[c], errors="coerce")
    return df


def per_bug(df):
    rows = []
    for (proj, bug, rnd, tries), g in df.groupby(["project", "bug_id", "round", "tries"]):
        v = g[g["valid"]]
        n = len(v)
        det = int(v["fault_detected"].sum())
        fa = (v["false_alarm_properties"] != "").any()
        status = ("NO_PROPERTY" if (g["error_type"] == "NO_PROPERTY").all() else
                  g["error_type"].iloc[-1] if n == 0 else
                  "DETECTED" if det == n else "FLAKY" if det > 0 else "NOT_DETECTED")
        # false_alarm = some property failed on FIXED (it is excluded from the oracle, reported separately)
        rows.append(dict(project=proj, bug_id=int(bug), round=rnd, tries=tries, seeds=len(g), valid_seeds=n,
                         detected_seeds=det, detection_rate=round(det / n, 2) if n else "",
                         detected_any=det > 0, false_alarm=bool(fa), status=status,
                         detection_kind=";".join(sorted(set(v.loc[v["fault_detected"], "detection_kind"]) - {""})),
                         failure_in_modified_class=(v.loc[v["fault_detected"], "failure_in_modified_class"]
                                                    .str.lower().eq("true").any()),
                         line_cov=round(v["line_coverage_percent"].mean(), 2) if n else "",
                         branch_cov=round(v["branch_coverage_percent"].mean(), 2) if n else "",
                         avg_time_s=round(v["total_time"].mean(), 1) if n else ""))
    return pd.DataFrame(rows).sort_values(["project", "round", "tries", "bug_id"])


def per_project(b):
    out = []
    for (proj, rnd, tries), g in b.groupby(["project", "round", "tries"]):
        withp = g[g["status"] != "NO_PROPERTY"]
        valid = g[g["valid_seeds"] > 0]
        out.append(dict(project=proj, round=rnd, tries=tries, bugs=len(g), bugs_with_properties=len(withp),
                        valid_bugs=len(valid),
                        detected_any_seed=int(g["detected_any"].sum()),
                        detected_all_seeds=int((g["status"] == "DETECTED").sum()),
                        flaky=int((g["status"] == "FLAKY").sum()),
                        false_alarm_bugs=int(g["false_alarm"].sum()),
                        detection_rate_all_bugs=round(g["detected_any"].sum() / len(g), 3),
                        detection_rate_valid_bugs=round(valid["detected_any"].sum() / len(valid), 3) if len(valid) else "",
                        avg_line_cov=round(pd.to_numeric(valid["line_cov"]).mean(), 2) if len(valid) else "",
                        avg_branch_cov=round(pd.to_numeric(valid["branch_cov"]).mean(), 2) if len(valid) else ""))
    return pd.DataFrame(out)


def check_controls():
    ok = True
    checks = [("jqwik_control_negative.csv", "negative: never detected", lambda d: (~d["fault_detected"]).all()),
              ("jqwik_control_falsealarm.csv", "false-alarm: never detected, always flagged",
               lambda d: (~d["fault_detected"]).all() and (d["error_type"] == "FALSE_ALARM_ON_FIXED").all()),
              ("jqwik_control_positive.csv", "positive: always detected", lambda d: d["fault_detected"].all())]
    for f, name, pred in checks:
        d = load(f)
        if d is None or d.empty:
            print(f"[SKIP] {name} ({f} not found)")
            continue
        passed = bool(pred(d))
        ok &= passed
        print(f"[{'PASS' if passed else 'FAIL'}] {name}  ({len(d)} runs, bugs={sorted(int(x) for x in d['bug_id'].unique())})")
        if not passed:
            print(d[["bug_id", "seed", "fault_detected", "error_type", "error_message"]].to_string(index=False))
    return ok


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--check-controls", action="store_true")
    ap.add_argument("--csv", default="jqwik_results.csv",
                    help="results file in results/ (jqwik_results.csv = hand-written, jqwik_auto.csv = generated)")
    a = ap.parse_args()
    if a.check_controls:
        sys.exit(0 if check_controls() else 1)

    df = load(a.csv)
    if df is None or df.empty:
        sys.exit(f"results/{a.csv} not found / empty")
    stem = Path(a.csv.replace("*", "")).stem.replace("_results", "")
    b = per_bug(df)
    p = per_project(b)
    fb, fp = RES / f"{stem}_summary_bugs.csv", RES / f"{stem}_summary_projects.csv"
    b.to_csv(fb, index=False)
    p.to_csv(fp, index=False)
    pd.set_option("display.width", 250)
    pd.set_option("display.max_columns", 30)
    print(p.to_string(index=False))
    print("\nbug status counts:\n" + b.groupby(["round", "tries", "status"]).size().to_string())
    print(f"\nwritten: {fb}, {fp}")


if __name__ == "__main__":
    main()
