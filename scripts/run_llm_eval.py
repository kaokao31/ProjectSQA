#!/usr/bin/env python3
"""Evaluate LLM-generated JUnit tests (Gemini/, deepseek/) on Defects4J with the SAME oracle as jqwik/EvoSuite.

For <Provider>/TestCode/<Project>_<Bug>b/*.java:
  compile against fixed + buggy  ->  run (JUnit 4 via JUnit Platform vintage engine)  ->
  per test:  detected  <=>  passes on FIXED and fails on BUGGY ;  JaCoCo coverage of classes.modified on fixed.
Tests that fail on fixed are false alarms (wrong assertions) and never count.

  python3 scripts/run_llm_eval.py --providers Gemini,deepseek --jobs 6          # everything (resumable)
  python3 scripts/run_llm_eval.py --providers Gemini -p Lang -b 1
Output: results/llm_results.csv  (+ <Provider>/Result/<P>_<B>b/eval.json)
"""
import argparse
import csv
import fcntl
import json
import os
import re
import shutil
import sys
import time
from concurrent.futures import ProcessPoolExecutor, as_completed
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import run_jqwik as rj  # noqa: E402  (reuses checkout / classpath / report parsing / jacoco helpers)

REPO, WORK = rj.REPO, rj.WORK
CSV = REPO / "results/llm_results.csv"
JUNIT4 = Path(os.environ.get("D4J_HOME", "/opt/defects4j")) / "framework/projects/lib/junit-4.12-hamcrest-1.3.jar"
FIELDS = ["tool", "project", "bug_id", "target_class", "prompt_has_hint", "test_files",
          "compile_status_fixed", "compile_status_buggy", "tests_run_fixed", "tests_failed_fixed",
          "tests_run_buggy", "tests_failed_buggy", "detecting_tests", "fault_detected",
          "line_coverage_percent", "branch_coverage_percent", "total_time", "error_type", "error_message"]


def classpath(wd):
    """bin + cp.test, with JUnit 4.12 first and every other junit/hamcrest jar removed."""
    cp = rj.sanitize_cp(wd / rj.d4j_export("dir.bin.classes", wd), rj.d4j_export("cp.test", wd))
    parts = [p for p in cp.split(":") if not (p.endswith(".jar") and re.search(r"junit|hamcrest", os.path.basename(p), re.I))]
    return ":".join([str(JUNIT4)] + parts)


def run_tests(classes, cp, console, report_dir, cwd, log, timeout, jacoco_exec=None):
    if report_dir.exists():
        shutil.rmtree(report_dir)
    report_dir.mkdir(parents=True)
    jvm = ["java", "-Djava.awt.headless=true", "-Xmx768m"]
    if jacoco_exec is not None:
        jvm.append(f"-javaagent:{rj.JACOCO_AGENT}=destfile={jacoco_exec}")
    cmd = jvm + ["-jar", console, "--disable-banner", "--details", "none",
                 "--class-path", f"{classes}:{cp}", "--scan-class-path", classes,
                 "--include-classname", ".*", "--include-engine", "junit-vintage", "--reports-dir", report_dir]
    env = dict(os.environ)
    env.pop("JAVA_TOOL_OPTIONS", None)
    rc, out, err, to, dt = rj.run(cmd, cwd=cwd, log=log, timeout=timeout, env=env)
    return rj.parse_reports(report_dir), to


def evaluate(tool, project, bug, timeout):
    t0 = time.time()
    src_dir = REPO / tool / "TestCode" / f"{project}_{bug}b"
    out_dir = REPO / tool / "Result" / f"{project}_{bug}b"
    out_dir.mkdir(parents=True, exist_ok=True)
    log = out_dir / "eval.log"
    log.unlink(missing_ok=True)
    row = dict(tool=tool, project=project, bug_id=bug, fault_detected=False)
    pdir = REPO / tool / "Prompt" / f"{project}_{bug}b"
    row["prompt_has_hint"] = any("Triggering Failing Test" in p.read_text(encoding="utf-8", errors="replace")
                                 for p in pdir.glob("*.md")) if pdir.exists() else ""
    try:
        srcs = sorted(str(p) for p in src_dir.rglob("*.java"))
        row["test_files"] = len(srcs)
        if not srcs:
            raise rj.RunError("NO_TESTS", "no generated test file")
        _, console = rj.lib_jars()
        fixed = rj.ensure_checkout(project, bug, "f", False, log)
        buggy = rj.ensure_checkout(project, bug, "b", False, log)
        classes_mod = [c for c in re.split(r"[;,\s]+", rj.d4j_export("classes.modified", fixed)) if c]
        row["target_class"] = ";".join(classes_mod)
        cp_f, cp_b = classpath(fixed), classpath(buggy)
        scratch = WORK / "llm_out" / tool / f"{project}_{bug}"
        ok_f, msg_f = rj.compile_tests(srcs, cp_f, scratch / "classes_f", log)
        row["compile_status_fixed"] = "SUCCESS" if ok_f else "COMPILE_FAIL"
        if not ok_f:
            raise rj.RunError("TEST_COMPILE_FAIL", msg_f)
        ok_b, msg_b = rj.compile_tests(srcs, cp_b, scratch / "classes_b", log)
        row["compile_status_buggy"] = "SUCCESS" if ok_b else "COMPILE_FAIL"
        if not ok_b:
            raise rj.RunError("TEST_COMPILE_FAIL_BUGGY", msg_b)

        exec_file = scratch / "jacoco.exec"
        exec_file.unlink(missing_ok=True)
        res_f, to_f = run_tests(scratch / "classes_f", cp_f, console, scratch / "rep_f", fixed, log, timeout, exec_file)
        row["tests_run_fixed"] = len(res_f)
        row["tests_failed_fixed"] = sum(r["status"] == "FAIL" for r in res_f.values())
        if to_f:
            raise rj.RunError("TIMEOUT_FIXED", f"fixed run exceeded {timeout}s")
        if not res_f:
            raise rj.RunError("RUNTIME_FAIL", "no test results on fixed")
        if exec_file.exists() and exec_file.stat().st_size > 0 and classes_mod:
            cov = scratch / "jacoco.csv"
            rc, *_ = rj.run(["java", "-jar", rj.JACOCO_CLI, "report", exec_file, "--classfiles",
                             fixed / rj.d4j_export("dir.bin.classes", fixed), "--csv", cov], log=log)
            if rc == 0 and cov.exists():
                line, branch = rj.jacoco_coverage(cov, classes_mod)
                row["line_coverage_percent"] = "" if line is None else line
                row["branch_coverage_percent"] = "" if branch is None else branch

        res_b, to_b = run_tests(scratch / "classes_b", cp_b, console, scratch / "rep_b", buggy, log, timeout)
        row["tests_run_buggy"] = len(res_b)
        row["tests_failed_buggy"] = sum(r["status"] == "FAIL" for r in res_b.values())
        good = {t for t, r in res_f.items() if r["status"] == "PASS"}
        det = [t for t, r in res_b.items() if r["status"] == "FAIL" and t in good and not rj.is_harness_error(r)]
        row["detecting_tests"] = ";".join(det)[:1000]
        row["fault_detected"] = bool(det)
        if to_b:
            row["error_type"] = "TIMEOUT_BUGGY"
        (out_dir / "eval.json").write_text(json.dumps({
            "detecting": det,
            "failed_on_fixed": {t: r["message"] for t, r in res_f.items() if r["status"] == "FAIL"},
            "failed_on_buggy": {t: r["message"] for t, r in res_b.items() if r["status"] == "FAIL"}}, indent=1),
            encoding="utf-8")
        shutil.rmtree(scratch, ignore_errors=True)
    except rj.RunError as e:
        row["error_type"], row["error_message"] = e.error_type, e.message[-300:].replace("\n", " ")
    except Exception as e:
        row["error_type"], row["error_message"] = "TOOL_ERROR", repr(e)[-300:]
    row["total_time"] = round(time.time() - t0, 1)
    for attempt in range(60):
        try:
            with open(CSV.with_suffix(".lock"), "w") as lock:
                fcntl.flock(lock, fcntl.LOCK_EX)
                new = not CSV.exists() or CSV.stat().st_size == 0
                with open(CSV, "a", newline="", encoding="utf-8") as f:
                    w = csv.DictWriter(f, fieldnames=FIELDS)
                    if new:
                        w.writeheader()
                    w.writerow({k: row.get(k, "") for k in FIELDS})
            break
        except OSError:
            print("!! เขียน llm_results.csv ไม่ได้ - ปิดไฟล์ใน Excel ก่อน", flush=True)
            time.sleep(10)
    return row


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--providers", default="Gemini,deepseek")
    ap.add_argument("-p", "--project", default="all")
    ap.add_argument("-b", "--bugs", default="")
    ap.add_argument("--jobs", type=int, default=4)
    ap.add_argument("--timeout", type=int, default=180, help="seconds per JVM run")
    ap.add_argument("--rerun", action="store_true")
    a = ap.parse_args()

    done = set()
    if CSV.exists() and not a.rerun:
        with open(CSV, newline="", encoding="utf-8") as f:
            done = {(r["tool"], r["project"], r["bug_id"]) for r in csv.DictReader(f)
                    if r.get("error_type") not in ("TOOL_ERROR", "CHECKOUT_ERROR", "RUNTIME_FAIL")}
    want_bugs = set()
    for part in a.bugs.split(","):
        if part:
            lo, _, hi = part.partition("-")
            want_bugs |= set(range(int(lo), int(hi or lo) + 1))
    jobs = []
    for tool in a.providers.split(","):
        for d in sorted((REPO / tool / "TestCode").glob("*_*b")):
            m = re.match(r"(.+)_(\d+)b$", d.name)
            if not m:
                continue
            proj, bug = m.group(1), int(m.group(2))
            if a.project != "all" and proj != a.project:
                continue
            if want_bugs and bug not in want_bugs:
                continue
            if (tool, proj, str(bug)) in done:
                continue
            jobs.append((tool, proj, bug, a.timeout))
    print(f"{len(jobs)} evaluations to do (jobs={a.jobs})", flush=True)
    WORK.mkdir(parents=True, exist_ok=True)

    def show(r):
        print(f"[{r['tool']} {r['project']}-{r['bug_id']}] detected={r['fault_detected']} "
              f"tests={r.get('tests_run_fixed', '')} failFixed={r.get('tests_failed_fixed', '')} "
              f"cov={r.get('line_coverage_percent', '')} {r.get('error_type', '')}", flush=True)

    if a.jobs <= 1:
        for j in jobs:
            show(evaluate(*j))
    else:
        with ProcessPoolExecutor(max_workers=a.jobs) as ex:
            for f in as_completed([ex.submit(evaluate, *j) for j in jobs]):
                show(f.result())


if __name__ == "__main__":
    main()
