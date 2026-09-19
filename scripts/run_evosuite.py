#!/usr/bin/env python3
"""รัน EvoSuite กับ Defects4J Lang "1 บั๊ก / 1 budget / 1 seed" แล้วประเมินผลครบวงจร

ขั้นตอน (ตาม BENCHMARK_PROTOCOL):
  generate บน FIXED (f) -> คอมไพล์เทสต์ -> รันบน FIXED พร้อม JaCoCo (coverage)
  -> รันชุดเดียวกันบน BUGGY (b) -> ตัดสิน fault detection

รันใน Docker container:
  python3 scripts/run_evosuite.py --bug 1 --budget 60 --seed 1
"""
import argparse
import csv
import json
import os
import re
import shutil
import subprocess
import sys
import time
from pathlib import Path

REPO = Path(os.environ.get("REPO_ROOT", "/workspace"))
WORK = Path(os.environ.get("D4J_WORK", "/work"))
EVO_JAR = os.environ.get("EVOSUITE_JAR", "/opt/evosuite/evosuite-1.2.0.jar")
EVO_RT = os.environ.get("EVOSUITE_RT", "/opt/evosuite/evosuite-standalone-runtime-1.2.0.jar")
JACOCO_HOME = Path(os.environ.get("JACOCO_HOME", "/opt/jacoco"))
JACOCO_AGENT = JACOCO_HOME / "lib" / "jacocoagent.jar"
JACOCO_CLI = JACOCO_HOME / "lib" / "jacococli.jar"
META_CSV = REPO / "dataset/defects4j/Lang_metadata.csv"
RESULTS_CSV = REPO / "results/evosuite_results.csv"

FIELDS = [
    "project", "bug_id", "method", "tool", "tool_version", "target_class", "trigger_test",
    "seed", "search_budget", "criterion", "algorithm", "jacoco_mode",
    "generated_test_count", "generation_time", "test_compile_time", "test_execution_time", "total_time",
    "compile_status", "test_status",
    "line_coverage_percent", "branch_coverage_percent",
    "evosuite_line_percent", "evosuite_branch_percent",
    "tests_run_fixed", "tests_failed_fixed", "tests_run_buggy", "tests_failed_buggy",
    "failing_tests_buggy", "fault_detected", "timeout", "error_type", "error_message",
    "result_path", "log_path",
]


class RunError(Exception):
    def __init__(self, error_type, message):
        super().__init__(message)
        self.error_type = error_type
        self.message = message


def _s(x):
    if isinstance(x, bytes):
        return x.decode(errors="replace")
    return x or ""


def run(cmd, cwd=None, log=None, timeout=None):
    """รันคำสั่ง คืน (returncode, stdout, stderr, timed_out, seconds) และต่อ log"""
    t0 = time.time()
    try:
        p = subprocess.run([str(c) for c in cmd], cwd=cwd, capture_output=True,
                           text=True, errors="replace", timeout=timeout)
        rc, out, err, to = p.returncode, p.stdout, p.stderr, False
    except subprocess.TimeoutExpired as e:
        rc, out, err, to = -1, _s(e.stdout), _s(e.stderr), True
    dt = time.time() - t0
    if log is not None:
        with open(log, "a", encoding="utf-8") as f:
            f.write("\n$ " + " ".join(str(c) for c in cmd) + "\n")
            f.write(out + err)
            f.write(f"\n[exit={rc} timeout={to} time={dt:.1f}s]\n")
    return rc, out, err, to, dt


# ---------- ตัว parse (แยกเป็นฟังก์ชันเพื่อทดสอบได้) ----------
def load_meta(bug_id, path=META_CSV):
    with open(path, newline="", encoding="utf-8") as f:
        for row in csv.reader(f):
            if row and row[0].strip() == str(bug_id):
                classes = [c.strip() for c in re.split(r"[;,]", row[5]) if c.strip()]
                trig = [t.strip() for t in row[6].split(";") if t.strip()] if len(row) > 6 else []
                return {"classes": classes, "trigger": ";".join(trig)}
    raise RunError("TOOL_ERROR", f"bug {bug_id} not found in {path}")


def parse_evosuite(out):
    def g(pat):
        m = re.search(pat, out)
        return int(m.group(1)) if m else None
    return {
        "tests": g(r"Generated (\d+) tests"),
        "line": g(r"Coverage of criterion LINE: (\d+)%"),
        "branch": g(r"Coverage of criterion BRANCH: (\d+)%"),
    }


def parse_junit(out):
    """คืน (tests_run, failures, failed_names) หรือ (None, None, []) ถ้ารันไม่จบ"""
    m = re.search(r"^OK \((\d+) tests?\)", out, re.M)
    if m:
        return int(m.group(1)), 0, []
    m = re.search(r"Tests run: (\d+),\s+Failures: (\d+)", out)
    if m:
        failed = re.findall(r"^\d+\) (\S+)", out, re.M)
        return int(m.group(1)), int(m.group(2)), failed
    return None, None, []


def jacoco_coverage(csv_path, targets):
    """รวม coverage ของ target classes (และ inner class) จาก CSV ของ jacococli"""
    tot = {"LINE": [0, 0], "BRANCH": [0, 0]}
    found = False
    with open(csv_path, newline="", encoding="utf-8") as f:
        for r in csv.DictReader(f):
            fq = f'{r["PACKAGE"]}.{r["CLASS"]}'
            if any(fq == t or fq.startswith(t + "$") for t in targets):
                found = True
                for k in tot:
                    tot[k][0] += int(r[f"{k}_MISSED"])
                    tot[k][1] += int(r[f"{k}_COVERED"])
    if not found:
        return None, None

    def pct(k):
        m, c = tot[k]
        return round(100.0 * c / (m + c), 2) if (m + c) else None
    return pct("LINE"), pct("BRANCH")


# ---------- ขั้นตอนหลัก ----------
def ensure_checkout(bug, ver, fresh, log):
    wd = WORK / f"Lang_{bug}{ver}"
    if wd.exists() and (fresh or not (wd / ".defects4j.config").exists()):
        shutil.rmtree(wd)
    if not wd.exists():
        rc, *_ = run(["defects4j", "checkout", "-p", "Lang", "-v", f"{bug}{ver}", "-w", wd], log=log)
        if rc != 0:
            raise RunError("TOOL_ERROR", f"checkout Lang {bug}{ver} failed")
    rc, *_ = run(["defects4j", "compile", "-w", wd], log=log)
    if rc != 0:
        raise RunError("TOOL_ERROR", f"compile Lang {bug}{ver} failed")
    return wd


def d4j_export(prop, wd):
    rc, out, err, _, _ = run(["defects4j", "export", "-p", prop, "-w", wd])
    if rc != 0:
        raise RunError("TOOL_ERROR", f"export {prop} failed: {err[-200:]}")
    return out.strip()


def evaluate(a, row, log, outdir):
    meta = load_meta(a.bug)
    row["target_class"] = ";".join(meta["classes"])
    row["trigger_test"] = meta["trigger"]
    if not meta["classes"]:
        raise RunError("TOOL_ERROR", "no classes.modified in metadata")

    t_start = time.time()
    fixed = ensure_checkout(a.bug, "f", a.fresh, log)
    buggy = ensure_checkout(a.bug, "b", a.fresh, log)
    cp_f = d4j_export("cp.compile", fixed)
    bin_f = fixed / d4j_export("dir.bin.classes", fixed)
    tcp_f = d4j_export("cp.test", fixed)
    tcp_b = d4j_export("cp.test", buggy)

    run_dir = WORK / "evo_out" / f"Lang_{a.bug}f" / f"budget{a.budget}_seed{a.seed}"
    if run_dir.exists():
        shutil.rmtree(run_dir)
    tests_dir, report_dir, classes_dir = run_dir / "tests", run_dir / "report", run_dir / "classes"
    classes_dir.mkdir(parents=True)

    # 1) generate (บน FIXED)
    gen_time, gen_tests, evo_line, evo_branch = 0.0, 0, [], []
    for cls in meta["classes"]:
        cmd = ["java", "-jar", EVO_JAR, "-generateSuite", "-class", cls,
               "-projectCP", f"{bin_f}:{cp_f}",
               f"-Dcriterion={a.criterion}", f"-Dsearch_budget={a.budget}", "-seed", a.seed,
               f"-Dtest_dir={tests_dir}", f"-Dreport_dir={report_dir}"]
        if a.algorithm:
            cmd.append(f"-Dalgorithm={a.algorithm}")
        if a.population:
            cmd.append(f"-Dpopulation={a.population}")
        rc, out, err, to, dt = run(cmd, cwd=fixed, log=log, timeout=a.budget * 3 + 300)
        gen_time += dt
        if to:
            row["timeout"] = True
            raise RunError("TIMEOUT", f"EvoSuite timeout on {cls}")
        if rc != 0:
            raise RunError("TOOL_ERROR", f"EvoSuite failed on {cls} (rc={rc})")
        info = parse_evosuite(out)
        gen_tests += info["tests"] or 0
        if info["line"] is not None:
            evo_line.append(info["line"])
        if info["branch"] is not None:
            evo_branch.append(info["branch"])
    row["generation_time"] = round(gen_time, 1)
    row["generated_test_count"] = gen_tests
    # ถ้ามีหลายคลาส ค่าที่ EvoSuite รายงานเป็นค่าเฉลี่ยแบบหยาบ ๆ (ใช้เทียบ sanity check เท่านั้น)
    row["evosuite_line_percent"] = round(sum(evo_line) / len(evo_line), 1) if evo_line else ""
    row["evosuite_branch_percent"] = round(sum(evo_branch) / len(evo_branch), 1) if evo_branch else ""

    srcs = sorted(str(p) for p in tests_dir.rglob("*.java"))
    test_classes = [f"{c}_ESTest" for c in meta["classes"]
                    if (tests_dir / (c.replace(".", "/") + "_ESTest.java")).exists()]
    if not srcs or not test_classes:
        raise RunError("TOOL_ERROR", "EvoSuite produced no test files")

    # 2) compile tests
    rc, out, err, to, dt = run(["javac", "-nowarn", "-cp", f"{bin_f}:{tcp_f}:{EVO_RT}",
                                "-d", classes_dir] + srcs, log=log, timeout=600)
    row["test_compile_time"] = round(dt, 1)
    if rc != 0:
        row["compile_status"] = "COMPILE_FAIL"
        raise RunError("COMPILE_FAIL", (out + err)[-300:])
    row["compile_status"] = "SUCCESS"

    # 3) รันบน FIXED + JaCoCo
    exec_file = outdir / "jacoco.exec"
    if exec_file.exists():
        exec_file.unlink()
    exec_time = 0.0
    if a.jacoco_mode == "agent":
        jvm = ["java", f"-javaagent:{JACOCO_AGENT}=destfile={exec_file}",
               "-cp", f"{classes_dir}:{tcp_f}:{EVO_RT}"]
    else:  # offline instrumentation
        instr = run_dir / "instrumented"
        rc, *_ = run(["java", "-jar", JACOCO_CLI, "instrument", bin_f, "--dest", instr], log=log)
        if rc != 0:
            raise RunError("TOOL_ERROR", "jacoco instrument failed")
        jvm = ["java", f"-Djacoco-agent.destfile={exec_file}",
               "-cp", f"{instr}:{classes_dir}:{tcp_f}:{EVO_RT}:{JACOCO_AGENT}"]
    rc, out, err, to, dt = run(jvm + ["org.junit.runner.JUnitCore"] + test_classes, cwd=fixed, log=log, timeout=600)
    exec_time += dt
    if to:
        row["timeout"] = True
        row["test_status"] = "TIMEOUT"
        raise RunError("TIMEOUT", "running tests on fixed timed out")
    n_f, fail_f, _ = parse_junit(out)
    row["tests_run_fixed"], row["tests_failed_fixed"] = n_f, fail_f
    if n_f is None:
        row["test_status"] = "RUNTIME_FAIL"
        raise RunError("RUNTIME_FAIL", "JUnit did not finish on fixed: " + (out + err)[-300:])
    row["test_status"] = "SUCCESS" if fail_f == 0 else "RUNTIME_FAIL"

    # coverage (JaCoCo)
    cov_csv = outdir / "jacoco.csv"
    if exec_file.exists() and exec_file.stat().st_size > 0:
        rc, *_ = run(["java", "-jar", JACOCO_CLI, "report", exec_file, "--classfiles", bin_f,
                      "--csv", cov_csv], log=log)
        if rc == 0 and cov_csv.exists():
            line, branch = jacoco_coverage(cov_csv, meta["classes"])
            row["line_coverage_percent"] = "" if line is None else line
            row["branch_coverage_percent"] = "" if branch is None else branch
    if row["line_coverage_percent"] == "":
        row["error_type"] = "JACOCO_NO_DATA"
        row["error_message"] = "JaCoCo produced no coverage for target class"

    # 4) รันบน BUGGY (ไม่ต้อง JaCoCo)
    rc, out, err, to, dt = run(["java", "-cp", f"{classes_dir}:{tcp_b}:{EVO_RT}",
                                "org.junit.runner.JUnitCore"] + test_classes, cwd=buggy, log=log, timeout=600)
    exec_time += dt
    row["test_execution_time"] = round(exec_time, 1)
    n_b, fail_b, failed_names = parse_junit(out)
    row["tests_run_buggy"], row["tests_failed_buggy"] = n_b, fail_b
    row["failing_tests_buggy"] = ";".join(failed_names[:5])
    if to:
        row["timeout"] = True
        raise RunError("TIMEOUT", "running tests on buggy timed out")
    if n_b is None:
        raise RunError("RUNTIME_FAIL", "JUnit did not finish on buggy: " + (out + err)[-300:])

    # fault detection: fail บน buggy และ pass ทั้งหมดบน fixed (ต้องอ่าน failure ยืนยันด้วยมือว่าเกี่ยวกับบั๊ก)
    row["fault_detected"] = bool(fail_f == 0 and fail_b and fail_b > 0)
    row["total_time"] = round(time.time() - t_start, 1)

    # เก็บเทสต์ที่ generate ไว้ใน repo
    dst = REPO / "EvoSuite" / "Test" / f"Lang_{a.bug}" / f"budget{a.budget}_seed{a.seed}"
    if dst.exists():
        shutil.rmtree(dst)
    shutil.copytree(tests_dir, dst)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--bug", type=int, required=True)
    ap.add_argument("--budget", type=int, default=60, help="EvoSuite search_budget (วินาที)")
    ap.add_argument("--seed", type=int, default=1)
    ap.add_argument("--criterion", default="LINE:BRANCH:EXCEPTION")
    ap.add_argument("--algorithm", default=None, help="เช่น MONOTONIC_GA (ไม่ใส่ = ค่า default ของ EvoSuite)")
    ap.add_argument("--population", type=int, default=None)
    ap.add_argument("--jacoco-mode", choices=["agent", "offline"], default="offline")
    ap.add_argument("--round", type=int, default=1, choices=[1, 2])
    ap.add_argument("--fresh", action="store_true", help="checkout ใหม่ทุกครั้ง")
    a = ap.parse_args()
    a.seed = str(a.seed)

    tag = f"budget{a.budget}_seed{a.seed}"
    outdir = REPO / "EvoSuite" / f"Result_Round{a.round}" / f"Lang_{a.bug}" / tag
    outdir.mkdir(parents=True, exist_ok=True)
    log = outdir / "run.log"
    log.write_text("", encoding="utf-8")
    (outdir / "config.json").write_text(json.dumps(vars(a), indent=2, ensure_ascii=False), encoding="utf-8")

    row = {k: "" for k in FIELDS}
    row.update(project="Lang", bug_id=a.bug, method="SBST", tool="EvoSuite",
               tool_version=re.sub(r"^evosuite-|\.jar$", "", os.path.basename(EVO_JAR)),
               seed=a.seed, search_budget=a.budget, criterion=a.criterion,
               algorithm=a.algorithm or "default", jacoco_mode=a.jacoco_mode,
               timeout=False, fault_detected=False,
               result_path=str(outdir.relative_to(REPO)), log_path=str((outdir / "run.log").relative_to(REPO)))
    try:
        evaluate(a, row, log, outdir)
    except RunError as e:
        row["error_type"] = e.error_type
        row["error_message"] = e.message.replace("\n", " ")[:300]
        if not row["test_status"]:
            row["test_status"] = e.error_type if e.error_type in ("TIMEOUT", "TOOL_ERROR") else row["test_status"]
        if e.error_type == "COMPILE_FAIL":
            row["compile_status"] = "COMPILE_FAIL"

    RESULTS_CSV.parent.mkdir(parents=True, exist_ok=True)
    new = not RESULTS_CSV.exists()
    with open(RESULTS_CSV, "a", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=FIELDS)
        if new:
            w.writeheader()
        w.writerow(row)

    print("\n===== สรุป =====")
    for k in ["target_class", "generated_test_count", "generation_time", "compile_status", "test_status",
              "tests_run_fixed", "tests_failed_fixed", "tests_run_buggy", "tests_failed_buggy",
              "failing_tests_buggy", "fault_detected",
              "line_coverage_percent", "branch_coverage_percent",
              "evosuite_line_percent", "evosuite_branch_percent", "error_type", "error_message"]:
        print(f"{k:26}: {row[k]}")
    try:
        d = abs(float(row["line_coverage_percent"]) - float(row["evosuite_line_percent"]))
        if d > 15:
            print(f"\n[WARN] Line coverage ของ JaCoCo ต่างจากที่ EvoSuite รายงาน {d:.0f} จุด -> ตรวจวิธีวัด (ลอง --jacoco-mode offline)")
    except (TypeError, ValueError):
        pass
    print(f"\nผลเพิ่มลง {RESULTS_CSV.relative_to(REPO)} | log: {row['log_path']}")


if __name__ == "__main__":
    sys.exit(main())
