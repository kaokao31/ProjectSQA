#!/usr/bin/env python3
"""รัน EvoSuite กับ Defects4J แบบ Dynamic Project "1 บั๊ก / 1 budget / 1 seed" แล้วประเมินผลครบวงจร
"""
import argparse
import csv
import fcntl
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
RESULTS_CSV = REPO / "results/evosuite_results.csv"

FIELDS = [
    "project", "bug_id", "method", "tool", "tool_version", "target_class", "trigger_test",
    "seed", "search_budget", "criterion", "algorithm", "jacoco_mode", "evo_jvm_cp", "extra_cp",
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


def load_meta(project, bug_id):
    path = REPO / "dataset/defects4j" / f"{project}_metadata.csv"
    if not path.exists():
        raise RunError("TOOL_ERROR", f"metadata file {path} not found")
    default_cols = ["bug_id", "project_id", "revision_id_buggy", "revision_id_fixed",
                    "report_id", "classes_modified", "tests_trigger", "tests_trigger_cause"]
    with open(path, newline="", encoding="utf-8-sig") as f:
        rows = [r for r in csv.reader(f) if r]
    if not rows:
        raise RunError("TOOL_ERROR", f"metadata file {path} is empty")
    if rows[0][0].strip().isdigit():      # ไม่มี header -> ใช้ลำดับคอลัมน์มาตรฐาน
        cols = default_cols
    else:                                 # มี header -> map ตามชื่อ (ยอมรับทั้ง bug.id และ bug_id)
        cols = [c.strip().lower().replace(".", "_") for c in rows[0]]
        rows = rows[1:]
    for r in rows:
        d = dict(zip(cols, r))
        if d.get("bug_id", "").strip() == str(bug_id):
            target = d.get("classes_modified", "")
            classes = [c.strip() for c in re.split(r"[;,]", target) if c.strip()]
            trig = [t.strip() for t in d.get("tests_trigger", "").split(";") if t.strip()]
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
    m = re.search(r"^OK \((\d+) tests?\)", out, re.M)
    if m:
        return int(m.group(1)), 0, []
    m = re.search(r"Tests run: (\d+),\s+Failures: (\d+)", out)
    if m:
        failed = re.findall(r"^\d+\) (\S+)", out, re.M)
        return int(m.group(1)), int(m.group(2)), failed
    return None, None, []


def jacoco_coverage(csv_path, targets):
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


def ensure_checkout(project, bug, ver, fresh, log):
    wd = WORK / f"{project}_{bug}{ver}"
    if wd.exists() and (fresh or not (wd / ".defects4j.config").exists()):
        shutil.rmtree(wd)
    if not wd.exists():
        rc, *_ = run(["defects4j", "checkout", "-p", project, "-v", f"{bug}{ver}", "-w", wd], log=log)
        if rc != 0:
            raise RunError("TOOL_ERROR", f"checkout {project} {bug}{ver} failed")
    rc, *_ = run(["defects4j", "compile", "-w", wd], log=log)
    if rc != 0:
        raise RunError("TOOL_ERROR", f"compile {project} {bug}{ver} failed")
    return wd


def d4j_export(prop, wd):
    rc, out, err, _, _ = run(["defects4j", "export", "-p", prop, "-w", wd])
    if rc != 0:
        raise RunError("TOOL_ERROR", f"export {prop} failed: {err[-200:]}")
    return out.strip()


def evaluate(a, row, log, outdir):
    meta = load_meta(a.project, a.bug)
    row["target_class"] = ";".join(meta["classes"])
    row["trigger_test"] = meta["trigger"]
    if not meta["classes"]:
        raise RunError("TOOL_ERROR", "no classes.modified in metadata")

    extra_s = "".join(":" + p for p in (a.extra_cp or "").split(":") if p)
    t_start = time.time()
    fixed = ensure_checkout(a.project, a.bug, "f", a.fresh, log)
    buggy = ensure_checkout(a.project, a.bug, "b", a.fresh, log)
    cp_f = d4j_export("cp.compile", fixed)
    bin_f = fixed / d4j_export("dir.bin.classes", fixed)
    tcp_f = d4j_export("cp.test", fixed)
    tcp_b = d4j_export("cp.test", buggy)

    junit4_jar = "/opt/defects4j/framework/projects/lib/junit-4.12-hamcrest-1.3.jar"
    def filter_old_junit(cp):
        parts = [p.strip() for p in str(cp).split(":") if p.strip() and os.path.exists(p.strip())]
        clean_parts = [p for p in parts if "junit" not in os.path.basename(p).lower() and "hamcrest" not in os.path.basename(p).lower()]
        return f"{junit4_jar}:" + ":".join(clean_parts)

    tcp_f = filter_old_junit(tcp_f)
    tcp_b = filter_old_junit(tcp_b)
    junit4_jar = "/opt/defects4j/framework/projects/lib/junit-4.12-hamcrest-1.3.jar"
    if junit4_jar not in tcp_b:
        tcp_b = f"{junit4_jar}:{tcp_b}"

    def sanitize_cp(*cps):
        import os
        seen = set()
        res = []
        junit4 = "/opt/defects4j/framework/projects/lib/junit-4.12-hamcrest-1.3.jar"
        if os.path.exists(junit4):
            seen.add(junit4)
            res.append(junit4)
        for c in cps:
            if not c: continue
            for p in str(c).split(":"):
                p = p.strip()
                if not p or not os.path.exists(p):
                    continue
                # ตัด JUnit เวอร์ชันอื่นออกเพื่อป้องกัน classloader ชนกัน
                if "junit" in os.path.basename(p).lower() and p != junit4:
                    continue
                if p not in seen:
                    seen.add(p)
                    res.append(p)
        return ":".join(res)

    full_cp = sanitize_cp(bin_f, cp_f, tcp_f, extra_s)
    evo_launcher = ["java", "-Djava.awt.headless=true", "-jar", EVO_JAR]
    if a.evo_jvm_cp:
        jvm_cp = sanitize_cp(EVO_JAR, bin_f, cp_f, tcp_f, extra_s)
        evo_launcher = ["java", "-Djava.awt.headless=true", "-cp", jvm_cp, "org.evosuite.EvoSuite"]

    run_dir = WORK / "evo_out" / f"{a.project}_{a.bug}f" / f"budget{a.budget}_seed{a.seed}"
    if run_dir.exists():
        shutil.rmtree(run_dir)
    tests_dir, report_dir, classes_dir = run_dir / "tests", run_dir / "report", run_dir / "classes"
    classes_dir.mkdir(parents=True)

    gen_time, gen_tests, evo_line, evo_branch = 0.0, 0, [], []
    for cls in meta["classes"]:
        cmd = evo_launcher + ["-generateSuite", "-class", cls,
        "-projectCP", full_cp,
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
            raise RunError("TOOL_ERROR", f"EvoSuite failed on {cls} (rc={rc}): " + (out + err)[-400:])
        info = parse_evosuite(out)
        gen_tests += info["tests"] or 0
        if info["line"] is not None:
            evo_line.append(info["line"])
        if info["branch"] is not None:
            evo_branch.append(info["branch"])
    row["generation_time"] = round(gen_time, 1)
    row["generated_test_count"] = gen_tests
    row["evosuite_line_percent"] = round(sum(evo_line) / len(evo_line), 1) if evo_line else ""
    row["evosuite_branch_percent"] = round(sum(evo_branch) / len(evo_branch), 1) if evo_branch else ""

    srcs = sorted(str(p) for p in tests_dir.rglob("*.java"))
    test_classes = [f"{c}_ESTest" for c in meta["classes"]
                    if (tests_dir / (c.replace(".", "/") + "_ESTest.java")).exists()]
    if not srcs or not test_classes:
        raise RunError("TOOL_ERROR", "EvoSuite produced no test files")

    # Inject JUnit 4.12 into test classpath
    junit4_jar = "/opt/defects4j/framework/projects/lib/junit-4.12-hamcrest-1.3.jar"
    if junit4_jar not in tcp_f:
        tcp_f = f"{junit4_jar}:{tcp_f}"

    rc, out, err, to, dt = run(["javac", "-nowarn", "-cp", f"{bin_f}:{tcp_f}:{EVO_RT}{extra_s}",
                                "-d", classes_dir] + srcs, log=log, timeout=600)
    row["test_compile_time"] = round(dt, 1)
    if rc != 0:
        row["compile_status"] = "COMPILE_FAIL"
        raise RunError("COMPILE_FAIL", (out + err)[-300:])
    row["compile_status"] = "SUCCESS"

    exec_file = outdir / "jacoco.exec"
    if exec_file.exists():
        exec_file.unlink()
    exec_time = 0.0
    if a.jacoco_mode == "agent":
        jvm = ["java", f"-javaagent:{JACOCO_AGENT}=destfile={exec_file}",
               "-cp", f"{classes_dir}:{tcp_f}:{EVO_RT}{extra_s}"]
    else:
        instr = run_dir / "instrumented"
        rc, *_ = run(["java", "-jar", JACOCO_CLI, "instrument", bin_f, "--dest", instr], log=log)
        if rc != 0:
            raise RunError("TOOL_ERROR", "jacoco instrument failed")
        jvm = ["java", f"-Djacoco-agent.destfile={exec_file}",
               "-cp", f"{instr}:{classes_dir}:{tcp_f}:{EVO_RT}{extra_s}:{JACOCO_AGENT}"]
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

    rc, out, err, to, dt = run(["java", "-cp", f"{classes_dir}:{tcp_b}:{EVO_RT}{extra_s}",
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

    row["fault_detected"] = bool(fail_f == 0 and fail_b and fail_b > 0)
    row["total_time"] = round(time.time() - t_start, 1)

    dst = REPO / "EvoSuite" / "Test" / f"{a.project}_{a.bug}" / f"budget{a.budget}_seed{a.seed}"
    if dst.exists():
        shutil.rmtree(dst)
    shutil.copytree(tests_dir, dst)


def append_row(row):
    """ต่อแถวลง CSV รวม: ล็อกไฟล์กันเขียนชนกัน และย้ายไฟล์เก่าให้เป็น header ใหม่อัตโนมัติ"""
    RESULTS_CSV.parent.mkdir(parents=True, exist_ok=True)
    with open(RESULTS_CSV.with_suffix(".lock"), "w") as lk:
        fcntl.flock(lk, fcntl.LOCK_EX)
        old_rows, header_ok = [], False
        if RESULTS_CSV.exists() and RESULTS_CSV.stat().st_size > 0:
            with open(RESULTS_CSV, newline="", encoding="utf-8") as f:
                rd = csv.DictReader(f)
                header_ok = (rd.fieldnames == FIELDS)
                if not header_ok:
                    old_rows = list(rd)
        if not header_ok:   # ไฟล์ใหม่ หรือ schema เก่า -> เขียน header ใหม่ คงแถวเดิมไว้ (คอลัมน์ที่ไม่มีเว้นว่าง)
            tmp = RESULTS_CSV.with_suffix(".tmp")
            with open(tmp, "w", newline="", encoding="utf-8") as f:
                w = csv.DictWriter(f, fieldnames=FIELDS)
                w.writeheader()
                for r in old_rows:
                    w.writerow({k: r.get(k, "") for k in FIELDS})
            os.replace(tmp, RESULTS_CSV)
        with open(RESULTS_CSV, "a", newline="", encoding="utf-8") as f:
            csv.DictWriter(f, fieldnames=FIELDS).writerow(row)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("-p", "--project", default="Lang", help="Defects4J project (default: Lang)")
    ap.add_argument("--bug", type=int, required=True)
    ap.add_argument("--budget", type=int, default=60, help="EvoSuite search_budget (วินาที)")
    ap.add_argument("--seed", type=int, default=1)
    ap.add_argument("--criterion", default="LINE:BRANCH:EXCEPTION")
    ap.add_argument("--algorithm", default=None)
    ap.add_argument("--population", type=int, default=None)
    ap.add_argument("--jacoco-mode", choices=["agent", "offline"], default="offline")
    ap.add_argument("--round", type=int, default=1, choices=[1, 2])
    ap.add_argument("--fresh", action="store_true")
    ap.add_argument("--evo-jvm-cp", action="store_true",
                    help="ใส่ jar dependency ลง classpath ของ JVM ที่รัน EvoSuite (แก้ 'Class not found' ใน ComputeClassWriter)")
    ap.add_argument("--extra-cp", default="", help="jar เสริม คั่นด้วย : (ใช้กับทุกขั้นตอน)")
    a = ap.parse_args()
    a.seed = str(a.seed)

    tag = f"budget{a.budget}_seed{a.seed}"
    outdir = REPO / "EvoSuite" / f"Result_Round{a.round}" / f"{a.project}_{a.bug}" / tag
    outdir.mkdir(parents=True, exist_ok=True)
    log = outdir / "run.log"
    log.write_text("", encoding="utf-8")
    (outdir / "config.json").write_text(json.dumps(vars(a), indent=2, ensure_ascii=False), encoding="utf-8")

    row = {k: "" for k in FIELDS}
    row.update(project=a.project, bug_id=a.bug, method="SBST", tool="EvoSuite",
               tool_version=re.sub(r"^evosuite-|\.jar$", "", os.path.basename(EVO_JAR)),
               seed=a.seed, search_budget=a.budget, criterion=a.criterion,
               algorithm=a.algorithm or "default", jacoco_mode=a.jacoco_mode,
               evo_jvm_cp=a.evo_jvm_cp, extra_cp=a.extra_cp,
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

    append_row(row)

    print(f"\n[{a.project}-{a.bug}] Status: {row['compile_status']} | Pass Fixed: {(row['tests_run_fixed'] - (row['tests_failed_fixed'] or 0)) if isinstance(row['tests_run_fixed'], int) else 0} | Fault Detected: {row['fault_detected']} | Cov: {row['line_coverage_percent']}%")
    if row["error_type"]:
        print(f"ERROR : {row['error_type']} | {row['error_message']}")
    print(f"LOG   : {row['log_path']}")


if __name__ == "__main__":
    sys.exit(main())
