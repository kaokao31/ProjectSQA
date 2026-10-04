#!/usr/bin/env python3
"""One comparison table for all tools: EvoSuite (60s/120s), jqwik (R1/R2), LLMs (Gemini, deepseek).
   python3 scripts/summarize_all.py      -> results/comparison_projects.csv, results/comparison_overall.csv
Detection = fault detected for the bug (jqwik: in at least one seed). Only rows that exist are counted."""
from pathlib import Path
import pandas as pd

RES = Path(__file__).resolve().parents[1] / "results"
frames = []

f = RES / "evosuite_results.csv"
if f.exists():
    e = pd.read_csv(f)
    e["det"] = e.fault_detected.astype(str).str.lower() == "true"
    for bud, g in e.groupby("search_budget"):
        b = g.groupby(["project", "bug_id"]).agg(det=("det", "any"), cov=("line_coverage_percent", "mean")).reset_index()
        b["tool"] = f"EvoSuite_{bud}s"
        frames.append(b)

f = RES / "jqwik_auto_summary_bugs.csv"
if f.exists():
    j = pd.read_csv(f)
    for r, g in j.groupby("round"):
        b = g[["project", "bug_id"]].copy()
        b["det"] = g.detected_any.astype(str).str.lower() == "true"
        b["cov"] = pd.to_numeric(g.line_cov, errors="coerce")
        b["tool"] = f"jqwik_R{r}"
        frames.append(b)

f = RES / "llm_results.csv"
llm = None
if f.exists():
    llm = pd.read_csv(f).drop_duplicates(["tool", "project", "bug_id"], keep="last")
    llm["det"] = llm.fault_detected.astype(str).str.lower() == "true"
    llm["compiled"] = llm.compile_status_fixed == "SUCCESS"
    for t, g in llm.groupby("tool"):
        b = g[["project", "bug_id", "det"]].copy()
        b["cov"] = pd.to_numeric(g.line_coverage_percent, errors="coerce")
        b["tool"] = t
        frames.append(b)

a = pd.concat(frames, ignore_index=True)
proj = a.groupby(["project", "tool"]).agg(bugs=("det", "size"), detected=("det", "sum"), avg_line_cov=("cov", "mean")).reset_index()
proj["rate"] = (proj.detected / proj.bugs).round(3)
proj["avg_line_cov"] = proj.avg_line_cov.round(2)
wide = proj.pivot(index="project", columns="tool", values="detected").fillna(0).astype(int)
wide.insert(0, "bugs", proj.groupby("project").bugs.max())
overall = a.groupby("tool").agg(bugs=("det", "size"), detected=("det", "sum"), avg_line_cov=("cov", "mean")).reset_index()
overall["rate"] = (overall.detected / overall.bugs).round(3)
overall["avg_line_cov"] = overall.avg_line_cov.round(2)
pd.set_option("display.width", 250)
print("=== bugs detected per project ===")
print(wide.to_string())
print("\n=== overall ===")
print(overall.to_string(index=False))
if llm is not None:
    print("\n=== LLM details ===")
    d = llm.groupby("tool").agg(evaluated=("det", "size"), compiled=("compiled", "sum"), detected=("det", "sum")).reset_index()
    d["compile_rate"] = (d.compiled / d.evaluated).round(3)
    print(d.to_string(index=False))
    h = llm.groupby(["tool", "prompt_has_hint"]).agg(bugs=("det", "size"), detected=("det", "sum")).reset_index()
    h["rate"] = (h.detected / h.bugs).round(3)
    print("\nwith / without trigger-test hint in the prompt:")
    print(h.to_string(index=False))
proj.to_csv(RES / "comparison_projects.csv", index=False)
overall.to_csv(RES / "comparison_overall.csv", index=False)
print(f"\nwritten: {RES/'comparison_projects.csv'}, {RES/'comparison_overall.csv'}")
