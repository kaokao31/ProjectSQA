#!/usr/bin/env python3
"""Run jqwik property-based tests (PBT) against Defects4J bugs and evaluate fault detection.

One run = 1 project x 1 bug x 1 round x 1 seed. For each run:
  1. checkout + compile the FIXED (f) and BUGGY (b) versions (cached under $D4J_WORK)
  2. compile the property tests in jqwik/Test/<Project>_<Bug>/ against EACH version
  3. run them with the JUnit Platform console launcher (jqwik engine only), same seed + tries
     on both versions; JaCoCo measures coverage of classes.modified on the fixed version
  4. oracle, per property:  detected  <=>  fails on buggy (real failure, not harness error)
                                         AND passes on fixed
     A property that fails on fixed is a FALSE ALARM (wrong property) and never counts.

Examples (inside the container):
  python3 scripts/run_jqwik.py -p Lang -b 1 --round 1 --tries 1000 --seeds 1
  python3 scripts/run_jqwik.py -p Lang --round 1 --tries 1000 --seeds 1,2,3,4,5 --jobs 4
  python3 scripts/run_jqwik.py -p all --round 1 --jobs 4          # every project in dataset/
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
import xml.etree.ElementTree as ET
from concurrent.futures import ProcessPoolExecutor, as_completed
from pathlib import Path

REPO = Path(os.environ.get("REPO_ROOT", "/workspace"))
WORK = Path(os.environ.get("D4J_WORK", "/work"))
JQWIK_LIB = Path(os.environ.get("JQWIK_LIB", REPO / "lib/jqwik"))
JACOCO_HOME = Path(os.environ.get("JACOCO_HOME", "/opt/jacoco"))
JACOCO_AGENT = JACOCO_HOME / "lib" / "jacocoagent.jar"
JACOCO_CLI = JACOCO_HOME / "lib" / "jacococli.jar"
RESULTS_CSV = REPO / "results/jqwik_results.csv"
HARNESS_SRC = REPO / "jqwik/Harness"
TEST_ROOT = REPO / "jqwik/Test"

# Exceptions that mean "the test/harness is broken", NOT "the bug was found".
HARNESS_ERRORS = (
    "NoClassDefFoundError", "ClassNotFoundException", "NoSuchMethodError", "NoSuchFieldError",
    "LinkageError", "IncompatibleClassChangeError", "AbstractMethodError", "ExceptionInInitializerError",
    "CannotFindArbitraryException", "TooManyFilterMissesException", "JqwikException",
    "CannotFindParameterResolverException", "OutOfMemoryError",
)

FIELDS = [
    "project", "bug_id", "round", "method", "tool", "tool_version", "target_class", "trigger_test",
    "seed", "tries", "public_methods", "properties_generated", "property_count",
    "compile_status_fixed", "compile_status_buggy", "test_status",
    "tests_run_fixed", "tests_failed_fixed", "tests_run_buggy", "tests_failed_buggy",
    "false_alarm_properties", "detecting_properties", "failure_types_buggy", "failure_in_modified_class",
    "fault_detected", "detection_kind",
    "line_coverage_percent", "branch_coverage_percent",
    "test_execution_time", "total_time", "timeout", "error_type", "error_message",
    "result_path", "log_path",
]


class RunError(Exception):
    def __init__(self, error_type, message):
        super().__init__(message)
        self.error_type, self.message = error_type, message


# --------------------------------------------------------------------------- helpers
def run(cmd, cwd=None, log=None, timeout=None, env=None):
    t0 = time.time()
    try:
        p = subprocess.run([str(c) for c in cmd], cwd=cwd, capture_output=True, text=True,
                           errors="replace", timeout=timeout, env=env)
        rc, out, err, to = p.returncode, p.stdout, p.stderr, False
    except subprocess.TimeoutExpired as e:
        def _s(x):
            return x.decode(errors="replace") if isinstance(x, bytes) else (x or "")
        rc, out, err, to = -1, _s(e.stdout), _s(e.stderr), True
    dt = time.time() - t0
    if log is not None:
        with open(log, "a", encoding="utf-8") as f:
            f.write("\n$ " + " ".join(str(c) for c in cmd) + "\n")
            f.write(out[-4000:] + err[-4000:])
            f.write(f"\n[exit={rc} timeout={to} time={dt:.1f}s]\n")
    return rc, out, err, to, dt


def load_meta(project):
    """bug_id -> {classes, trigger} from dataset/defects4j/<Project>_metadata.csv"""
    path = REPO / "dataset/defects4j" / f"{project}_metadata.csv"
    if not path.exists():
        raise SystemExit(f"metadata file {path} not found")
    with open(path, newline="", encoding="utf-8-sig") as f:
        rows = [r for r in csv.reader(f) if r]
    default_cols = ["bug_id", "project_id", "revision_id_buggy", "revision_id_fixed",
                    "report_id", "classes_modified", "tests_trigger", "tests_trigger_cause"]
    if rows and rows[0][0].strip().isdigit():
        cols = default_cols
    else:
        cols = [c.strip().lower().replace(".", "_") for c in rows[0]]
        rows = rows[1:]
    meta = {}
    for r in rows:
        d = dict(zip(cols, r))
        bid = d.get("bug_id", "").strip()
        if not bid.isdigit():
            continue
        meta[int(bid)] = {
            "classes": [c.strip() for c in re.split(r"[;,]", d.get("classes_modified", "")) if c.strip()],
            "trigger": ";".join(t.strip() for t in d.get("tests_trigger", "").split(";") if t.strip()),
        }
    return meta


def d4j_bids(project):
    """active bug ids straight from Defects4J (authoritative)."""
    rc, out, err, _, _ = run(["defects4j", "bids", "-p", project])
    return sorted(int(x) for x in out.split() if x.strip().isdigit()) if rc == 0 else []


def deprecated_bugs(project):
    p = Path(os.environ.get("D4J_HOME", "/opt/defects4j")) / "framework/projects" / project / "deprecated-bugs.csv"
    if not p.exists():
        return set()
    with open(p, newline="") as f:
        return {int(r["bug.id"]) for r in csv.DictReader(f) if r.get("bug.id", "").isdigit()}


def jqwik_version():
    for j in JQWIK_LIB.glob("jqwik-engine-*.jar"):
        return j.stem.replace("jqwik-engine-", "")
    return "unknown"


def lib_jars():
    jars = sorted(str(j) for j in JQWIK_LIB.glob("*.jar") if "console-standalone" not in j.name)
    console = next(JQWIK_LIB.glob("junit-platform-console-standalone-*.jar"), None)
    if not jars or console is None:
        raise SystemExit(f"jqwik jars / junit-platform-console-standalone not found in {JQWIK_LIB}")
    return jars, str(console)


def sanitize_cp(*cps):
    """drop missing entries and anything that would clash with the JUnit Platform launcher."""
    seen, res = set(), []
    clash = re.compile(r"(junit-platform|junit-jupiter|junit-vintage|opentest4j|jqwik)", re.I)
    for c in cps:
        for p in str(c or "").split(":"):
            p = p.strip()
            if not p or p in seen or not os.path.exists(p):
                continue
            if p.endswith(".jar") and clash.search(os.path.basename(p)):
                continue
            seen.add(p)
            res.append(p)
    return ":".join(res)


def d4j_export(prop, wd):
    rc, out, err, _, _ = run(["defects4j", "export", "-p", prop, "-w", wd])
    if rc != 0:
        raise RunError("TOOL_ERROR", f"defects4j export {prop} failed: {err[-200:]}")
    return out.strip()


def ensure_checkout(project, bug, ver, fresh, log):
    wd = WORK / f"{project}_{bug}{ver}"
    marker = wd / ".compiled_ok"
    if wd.exists() and (fresh or not (wd / ".defects4j.config").exists()):
        shutil.rmtree(wd)
    if not wd.exists():
        rc, *_ = run(["defects4j", "checkout", "-p", project, "-v", f"{bug}{ver}", "-w", wd], log=log)
        if rc != 0:
            raise RunError("CHECKOUT_ERROR", f"defects4j checkout {project}-{bug}{ver} failed")
    if not marker.exists():
        rc, *_ = run(["defects4j", "compile", "-w", wd], log=log)
        if rc != 0:
            raise RunError("D4J_COMPILE_ERROR", f"defects4j compile {project}-{bug}{ver} failed")
        marker.touch()
    return wd


def build_harness(log):
    """compile SeedControlHook once; it forces -Dpbt.seed / -Dpbt.tries on every property."""
    out = WORK / "jqwik_harness"
    srcs = sorted(str(p) for p in (HARNESS_SRC / "src").rglob("*.java"))
    if not srcs:
        raise SystemExit(f"ERROR: ไม่พบไฟล์ .java ใน {HARNESS_SRC / 'src'}\n"
                         "  ต้องมีโฟลเดอร์ jqwik/Harness/src/pbt/harness และ jqwik/Harness/src/pbt/auto "
                         "(อยู่ใน ProjectSQA-update.zip) ให้แตก zip ทับที่ root ของ repo")
    newest = max(os.path.getmtime(s) for s in srcs)
    stamp = out / ".stamp"
    if stamp.exists() and float(stamp.read_text() or 0) >= newest:
        return out
    if out.exists():
        shutil.rmtree(out)
    out.mkdir(parents=True)
    jars, _ = lib_jars()
    rc, o, e, _, _ = run(["javac", "-nowarn", "-encoding", "UTF-8", "-cp", ":".join(jars), "-d", out] + srcs, log=log)
    if rc != 0:
        raise SystemExit("cannot compile jqwik harness:\n" + o + e)
    shutil.copytree(HARNESS_SRC / "resources", out, dirs_exist_ok=True)
    stamp.write_text(str(newest))
    return out


def parse_reports(report_dir):
    """-> {property_id: {"status": PASS|FAIL|SKIP, "type": exc, "message": str, "trace": str}}"""
    res = {}
    for xml in Path(report_dir).glob("TEST-*.xml"):
        try:
            root = ET.parse(xml).getroot()
        except ET.ParseError:
            continue
        for tc in root.iter("testcase"):
            pid = f'{tc.get("classname")}#{tc.get("name")}'
            node = tc.find("failure") if tc.find("failure") is not None else tc.find("error")
            if node is not None:
                res[pid] = {"status": "FAIL", "type": (node.get("type") or "").split(".")[-1],
                            "message": (node.get("message") or "")[:800], "trace": node.text or ""}
            elif tc.find("skipped") is not None:
                res[pid] = {"status": "SKIP", "type": "", "message": "", "trace": ""}
            else:
                res[pid] = {"status": "PASS", "type": "", "message": "", "trace": ""}
    return res


def is_harness_error(r):
    # look at the exception type and "Caused by" lines only - never at the message text
    # (regression messages contain observed outputs such as "throws java.lang.OutOfMemoryError")
    causes = " ".join(l for l in r["trace"].splitlines() if l.startswith("Caused by:"))
    return any(h in r["type"] + " " + causes for h in HARNESS_ERRORS)


def jacoco_coverage(csv_path, targets):
    tot = {"LINE": [0, 0], "BRANCH": [0, 0]}
    found = False
    with open(csv_path, newline="", encoding="utf-8") as f:
        for r in csv.DictReader(f):
            fq = f'{r["PACKAGE"]}.{r["CLASS"]}'
            if any(fq == t or fq.startswith(t + ".") or fq.startswith(t + "$") for t in targets):
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


def append_row(row):
    """append one result row; if the CSV is locked (e.g. open in Excel on Windows) wait and retry
    instead of crashing the whole run."""
    RESULTS_CSV.parent.mkdir(parents=True, exist_ok=True)
    for attempt in range(120):                      # up to ~20 minutes
        try:
            with open(RESULTS_CSV.with_suffix(".lock"), "w") as lock:
                fcntl.flock(lock, fcntl.LOCK_EX)
                new = not RESULTS_CSV.exists() or RESULTS_CSV.stat().st_size == 0
                with open(RESULTS_CSV, "a", newline="", encoding="utf-8") as f:
                    w = csv.DictWriter(f, fieldnames=FIELDS)
                    if new:
                        w.writeheader()
                    w.writerow({k: row.get(k, "") for k in FIELDS})
            return
        except OSError as e:
            if attempt % 6 == 0:
                print(f"!! เขียน {RESULTS_CSV.name} ไม่ได้ ({e.__class__.__name__}) - ถ้าเปิดไฟล์นี้ใน Excel อยู่ให้ปิดก่อน "
                      f"(จะลองใหม่ทุก 10 วินาที)", flush=True)
            time.sleep(10)
    raise RuntimeError(f"cannot write {RESULTS_CSV}")


def done_keys():
    if not RESULTS_CSV.exists():
        return set()
    with open(RESULTS_CSV, newline="", encoding="utf-8") as f:
        return {(r["project"], r["bug_id"], r["round"], r["tries"], r["seed"])
                for r in csv.DictReader(f) if r.get("error_type") not in ("TOOL_ERROR", "CHECKOUT_ERROR", "NO_PROPERTY", "RUNTIME_FAIL", "GENERATOR_ERROR")}


# --------------------------------------------------------------------------- one run
def compile_tests(srcs, cp, dest, log):
    if dest.exists():
        shutil.rmtree(dest)
    dest.mkdir(parents=True)
    rc, out, err, _, _ = run(["javac", "-nowarn", "-encoding", "UTF-8", "-cp", cp, "-d", dest] + srcs,
                             log=log, timeout=600)
    return rc == 0, (out + err)[-400:]


def generate_properties(a, bug, classes, base_f, base_b, extra, test_dir, log):
    """pbt.auto.Generate: one regression property per public method common to buggy+fixed (+ contracts)."""
    info_file = test_dir / "AUTO_GENERATED.json"
    if a.regen or not info_file.exists():
        if test_dir.exists():
            shutil.rmtree(test_dir)
        test_dir.mkdir(parents=True)
        if not classes:
            raise RunError("NO_PROPERTY", "no classes.modified in metadata")
        rc, out, err, to, _ = run(["java", "-cp", extra, "pbt.auto.Generate", "--targets", ",".join(classes),
                                   "--fixed-cp", base_f, "--buggy-cp", base_b, "--out", test_dir,
                                   "--max-methods", str(a.max_methods)], log=log, timeout=300)
        if rc != 0 or not info_file.exists():
            raise RunError("GENERATOR_ERROR", (out + err)[-300:])
    info = json.loads(info_file.read_text(encoding="utf-8"))
    tot = {"public_methods": 0, "properties": 0}
    for c in info.get("classes", []):
        for k in tot:
            tot[k] += int(c.get(k, 0) or 0)
    return tot


def run_props(classes_dir, cp, console, seed, tries, report_dir, cwd, log, timeout, jacoco_exec=None, props=()):
    """run the properties; if the JVM died without results (e.g. OutOfMemoryError) retry once with a bigger heap."""
    res, to, dt, out = _run_props(classes_dir, cp, console, seed, tries, report_dir, cwd, log, timeout,
                                  jacoco_exec, props, "1g")
    if not res and not to:
        if log is not None:
            with open(log, "a", encoding="utf-8") as f:
                f.write("\n[no results - JVM crashed?] retrying once with -Xmx3g\n")
        if jacoco_exec is not None:
            Path(jacoco_exec).unlink(missing_ok=True)
        res, to, dt2, out = _run_props(classes_dir, cp, console, seed, tries, report_dir, cwd, log, timeout,
                                       jacoco_exec, props, "3g")
        dt += dt2
    return res, to, dt, out


def _run_props(classes_dir, cp, console, seed, tries, report_dir, cwd, log, timeout, jacoco_exec, props, heap):
    if report_dir.exists():
        shutil.rmtree(report_dir)
    report_dir.mkdir(parents=True)
    db = report_dir / "jqwik-database"                     # fresh DB -> no replay of old failures
    jvm = ["java", "-Djava.awt.headless=true", "-Xss4m", f"-Xmx{heap}", f"-Dpbt.seed={seed}", f"-Dpbt.tries={tries}"] + list(props)
    if jacoco_exec is not None:
        jvm.append(f"-javaagent:{JACOCO_AGENT}=destfile={jacoco_exec}")
    cmd = jvm + ["-jar", console, "--disable-banner", "--details", "tree",
                 "--class-path", f"{classes_dir}:{cp}",
                 "--scan-class-path", classes_dir, "--include-classname", ".*",
                 "--include-engine", "jqwik",
                 "--config", f"jqwik.database={db}",
                 "--config", f"jqwik.tries.default={tries}",
                 "--reports-dir", report_dir]
    env = dict(os.environ)
    env.pop("JAVA_TOOL_OPTIONS", None)
    rc, out, err, to, dt = run(cmd, cwd=cwd, log=log, timeout=timeout, env=env)
    return parse_reports(report_dir), to, dt, (out + err)


def evaluate(a, bug, seed, meta, row, log, outdir):
    t_start = time.time()
    m = meta.get(bug) or {"classes": [], "trigger": ""}
    row["target_class"] = ";".join(m["classes"])
    row["trigger_test"] = m["trigger"]

    test_dir = tests_for(a, bug)
    if not a.auto:
        srcs = sorted(str(p) for p in test_dir.rglob("*.java")) if test_dir.exists() else []
        if not srcs:
            raise RunError("NO_PROPERTY", f"no property tests in {test_dir}")

    jars, console = lib_jars()
    harness = WORK / "jqwik_harness"          # built once in main()
    fixed = ensure_checkout(a.project, bug, "f", a.fresh, log)
    buggy = ensure_checkout(a.project, bug, "b", a.fresh, log)
    # classes.modified / tests.trigger straight from Defects4J (dataset/*.csv is only a fallback)
    try:
        cls = [c.strip() for c in re.split(r"[;,\s]+", d4j_export("classes.modified", fixed)) if c.strip()]
        if cls:
            m = {"classes": cls, "trigger": m["trigger"]}
        trig = d4j_export("tests.trigger", fixed)
        m["trigger"] = ";".join(t for t in re.split(r"[;,\s]+", trig) if t) or m["trigger"]
    except RunError:
        pass
    row["target_class"] = ";".join(m["classes"])
    row["trigger_test"] = m["trigger"]
    base_f = sanitize_cp(fixed / d4j_export("dir.bin.classes", fixed), d4j_export("cp.test", fixed))
    base_b = sanitize_cp(buggy / d4j_export("dir.bin.classes", buggy), d4j_export("cp.test", buggy))
    extra = ":".join([str(harness)] + jars)
    cp_f, cp_b = f"{base_f}:{extra}", f"{base_b}:{extra}"

    auto_info = None
    if a.auto:
        auto_info = generate_properties(a, bug, m["classes"], base_f, base_b, extra, test_dir, log)
        row["public_methods"] = auto_info["public_methods"]
        row["properties_generated"] = auto_info["properties"]
        srcs = sorted(str(p) for p in test_dir.rglob("*.java"))
        if not srcs:
            raise RunError("NO_PROPERTY", "generator: no testable public API in " + ";".join(m["classes"]))
    n_props = sum(open(f, encoding="utf-8").read().count("@Property") for f in srcs)
    timeout = a.timeout + (n_props * a.prop_budget if a.auto else 0)

    scratch = WORK / "jqwik_out" / f"{a.project}_{bug}" / f"{a.tag or 'hand'}_round{a.round}_tries{a.tries}_seed{seed}"
    ok_f, msg_f = compile_tests(srcs, cp_f, scratch / "classes_f", log)
    ok_b, msg_b = compile_tests(srcs, cp_b, scratch / "classes_b", log)
    row["compile_status_fixed"] = "SUCCESS" if ok_f else "COMPILE_FAIL"
    row["compile_status_buggy"] = "SUCCESS" if ok_b else "COMPILE_FAIL"
    if not ok_f:
        raise RunError("TEST_COMPILE_FAIL_FIXED", msg_f)
    if not ok_b:
        # property uses an API that only exists after the fix -> invalid for this bug
        raise RunError("TEST_COMPILE_FAIL_BUGGY", msg_b)

    props_rec, props_chk, cwd_f, cwd_b = (), (), fixed, buggy
    if auto_info is not None:
        # generated tests run in a scratch cwd (never write into the D4J checkout)
        snap = scratch / "snapshots"
        if snap.exists():
            shutil.rmtree(snap)
        # same working directory for both versions (outputs such as user.dir must not differ)
        cwd_f = cwd_b = scratch / "cwd"
        cwd_f.mkdir(parents=True, exist_ok=True)
        # version-specific paths (checkout dirs, test class dirs) are masked in observed outputs
        mask = os.pathsep.join(str(x) for x in (fixed, buggy, scratch / "classes_f", scratch / "classes_b"))
        common = [f"-Dpbt.snapshot.dir={snap}", f"-Dpbt.propertyBudgetMs={a.prop_budget * 1000}",
                  f"-Dpbt.mask={mask}"]
        props_rec, props_chk = common + ["-Dpbt.mode=record"], common + ["-Dpbt.mode=check"]
        # ---- step 0: record the reference behaviour on the FIXED version
        res_r, to_r, dt_r, _ = run_props(scratch / "classes_f", cp_f, console, seed, a.tries,
                                         outdir / "reports_record", cwd_f, log, timeout, props=props_rec)
        if to_r:
            row["timeout"] = True
            raise RunError("TIMEOUT_FIXED", f"record run on fixed exceeded {timeout}s")
        if not res_r:
            raise RunError("RUNTIME_FAIL", "no jqwik results in the record run")

    # ---- fixed version (+ coverage)
    exec_file = outdir / "jacoco.exec"
    exec_file.unlink(missing_ok=True)
    res_f, to_f, dt_f, _ = run_props(scratch / "classes_f", cp_f, console, seed, a.tries,
                                     outdir / "reports_fixed", cwd_f, log, timeout, exec_file, props=props_chk)
    row["tests_run_fixed"] = len(res_f)
    row["tests_failed_fixed"] = sum(r["status"] == "FAIL" for r in res_f.values())
    row["property_count"] = len(res_f)
    if to_f:
        row["timeout"] = True
        raise RunError("TIMEOUT_FIXED", f"fixed version exceeded {timeout}s")
    if not res_f:
        raise RunError("RUNTIME_FAIL", "no jqwik results on fixed (no @Property found or JVM crashed)")
    false_alarm = [p for p, r in res_f.items() if r["status"] == "FAIL"]
    row["false_alarm_properties"] = ";".join(false_alarm)

    if exec_file.exists() and exec_file.stat().st_size > 0 and m["classes"]:
        cov_csv = outdir / "jacoco.csv"
        bin_f = fixed / d4j_export("dir.bin.classes", fixed)
        rc, *_ = run(["java", "-jar", JACOCO_CLI, "report", exec_file, "--classfiles", bin_f,
                      "--csv", cov_csv], log=log)
        if rc == 0 and cov_csv.exists():
            line, branch = jacoco_coverage(cov_csv, m["classes"])
            row["line_coverage_percent"] = "" if line is None else line
            row["branch_coverage_percent"] = "" if branch is None else branch

    # ---- buggy version (same seed, same tries)
    res_b, to_b, dt_b, _ = run_props(scratch / "classes_b", cp_b, console, seed, a.tries,
                                     outdir / "reports_buggy", cwd_b, log, timeout, props=props_chk)
    row["test_execution_time"] = round(dt_f + dt_b, 1)
    row["tests_run_buggy"] = len(res_b)
    row["tests_failed_buggy"] = sum(r["status"] == "FAIL" for r in res_b.values())

    good_on_fixed = {p for p, r in res_f.items() if r["status"] == "PASS"}
    detecting, types, harness_fail, in_mod = [], [], [], False
    for p, r in res_b.items():
        if r["status"] != "FAIL":
            continue
        if is_harness_error(r):
            harness_fail.append(p)
            continue
        if p in good_on_fixed:
            detecting.append(p)
            types.append(r["type"] or "AssertionError")
            if any(c in r["trace"] for c in m["classes"]):
                in_mod = True
    row["detecting_properties"] = ";".join(detecting)
    row["failure_types_buggy"] = ";".join(sorted(set(types)))
    row["failure_in_modified_class"] = in_mod if detecting else ""

    if to_b:
        row["timeout"] = True
        row["test_status"] = "TIMEOUT_BUGGY"
        # fixed finished within the limit with the same seed -> the buggy version hangs.
        row["fault_detected"] = not false_alarm
        row["detection_kind"] = "TIMEOUT" if not false_alarm else ""
        row["error_type"] = "TIMEOUT_BUGGY"
    else:
        row["test_status"] = "SUCCESS"
        row["fault_detected"] = bool(detecting)
        row["detection_kind"] = ("ASSERTION" if any(t in ("AssertionError", "AssertionFailedError")
                                                    or "Assertion" in t for t in types)
                                 else "EXCEPTION") if detecting else ""
        if harness_fail:
            row["error_type"] = "HARNESS_ERROR_BUGGY"
            row["error_message"] = ";".join(harness_fail)[:300]
    if false_alarm and not row.get("error_type"):
        row["error_type"] = "FALSE_ALARM_ON_FIXED"
        row["error_message"] = "; ".join(f'{p}: {res_f[p]["type"]}' for p in false_alarm)[:300]
    row["total_time"] = round(time.time() - t_start, 1)

    # compact per-run record: counts for everything, details only for failing properties
    summary = {}
    for name, res in (("fixed", res_f), ("buggy", res_b)):
        summary[name] = {"passed": sum(r["status"] == "PASS" for r in res.values()),
                         "failed": {p: {"type": r["type"], "message": r["message"], "trace": r["trace"][:600]}
                                    for p, r in res.items() if r["status"] == "FAIL"}}
    summary["detecting"] = detecting
    summary["false_alarm"] = false_alarm
    (outdir / "properties.json").write_text(json.dumps(summary, indent=1), encoding="utf-8")
    if not a.keep_reports:
        for d in ("reports_record", "reports_fixed", "reports_buggy"):
            shutil.rmtree(outdir / d, ignore_errors=True)
        (outdir / "jacoco.exec").unlink(missing_ok=True)


def tests_for(a, bug):
    """jqwik/Test/<Project>_<Bug>/ ; a --tests-root that holds .java files directly is used for every bug
    (e.g. the negative control)."""
    root = Path(a.tests_root)
    if any(root.glob("*.java")):
        return root
    return root / f"{a.project}_{bug}"


def result_root(a):
    return REPO / "jqwik" / (f"Result_Round{a.round}" if not a.tag else f"Result_{a.tag}_Round{a.round}")


def run_one(a, bug, seed, meta):
    outdir = result_root(a) / f"{a.project}_{bug}" / f"tries{a.tries}_seed{seed}"
    if outdir.exists():
        shutil.rmtree(outdir)
    outdir.mkdir(parents=True)
    log = outdir / "run.log"
    row = dict(project=a.project, bug_id=bug, round=a.round, method="PBT", tool="jqwik",
               tool_version=jqwik_version(), seed=seed, tries=a.tries, timeout=False,
               fault_detected=False, result_path=str(outdir.relative_to(REPO)),
               log_path=str(log.relative_to(REPO)))
    try:
        evaluate(a, bug, seed, meta, row, log, outdir)
    except RunError as e:
        row["error_type"], row["error_message"] = e.error_type, e.message[-300:].replace("\n", " ")
        row["test_status"] = row.get("test_status") or "NOT_RUN"
        row["fault_detected"] = False
    except Exception as e:  # never lose a row
        row["error_type"], row["error_message"] = "TOOL_ERROR", repr(e)[-300:]
        row["fault_detected"] = False
    append_row(row)
    return row


def main():
    ap = argparse.ArgumentParser(description="jqwik PBT runner for Defects4J")
    ap.add_argument("-p", "--project", default="Lang", help="Defects4J project or 'all'")
    ap.add_argument("-b", "--bugs", default="", help="e.g. 1,3,5-10 (default: all active bugs)")
    ap.add_argument("--round", type=int, default=1)
    ap.add_argument("--tries", type=int, default=1000, help="jqwik tries per property (search budget)")
    ap.add_argument("--seeds", default="1,2,3,4,5", help="comma separated seeds")
    ap.add_argument("--timeout", type=int, default=600, help="seconds per JVM run")
    ap.add_argument("--jobs", type=int, default=1, help="parallel bugs")
    ap.add_argument("--fresh", action="store_true", help="re-checkout D4J working dirs")
    ap.add_argument("--rerun", action="store_true", help="ignore rows already in the CSV")
    ap.add_argument("--tests-root", default=str(TEST_ROOT), help="default jqwik/Test")
    ap.add_argument("--tag", default="", help="separate CSV/result dir, e.g. control_negative")
    ap.add_argument("--auto", action="store_true",
                    help="auto-generate properties (pbt.auto.Generate) into jqwik/Auto/<P>_<B>/; tag defaults to 'auto'")
    ap.add_argument("--regen", action="store_true", help="re-generate auto properties even if present")
    ap.add_argument("--prop-budget", type=int, default=5, help="auto mode: max seconds per property")
    ap.add_argument("--max-methods", type=int, default=150, help="auto mode: max properties per class")
    ap.add_argument("--keep-reports", action="store_true", help="keep raw JUnit XML + jacoco.exec per run")
    ap.add_argument("--shard", default="", help="k/n: run only every n-th bug starting at k (split work over machines)")
    a = ap.parse_args()
    if a.auto:
        if a.tests_root == str(TEST_ROOT):
            a.tests_root = str(REPO / "jqwik/Auto")
        a.tag = a.tag or "auto"
    global RESULTS_CSV
    if a.tag:
        RESULTS_CSV = REPO / f"results/jqwik_{a.tag}.csv"
    shard_k = shard_n = 0
    if a.shard:
        shard_k, shard_n = (int(x) for x in a.shard.split("/"))
        RESULTS_CSV = RESULTS_CSV.with_name(f"{RESULTS_CSV.stem}_shard{shard_k}of{shard_n}.csv")

    projects = ([p.name.split("_metadata")[0] for p in sorted((REPO / "dataset/defects4j").glob("*_metadata.csv"))]
                if a.project == "all" else [a.project])
    seeds = [int(s) for s in a.seeds.split(",") if s.strip()]
    done = set() if a.rerun else done_keys()

    jobs = []
    bug_index = 0
    for proj in projects:
        meta = load_meta(proj) if (REPO / "dataset/defects4j" / f"{proj}_metadata.csv").exists() else {}
        dep = deprecated_bugs(proj)
        if a.bugs:
            ids = []
            for part in a.bugs.split(","):
                lo, _, hi = part.partition("-")
                ids += list(range(int(lo), int(hi or lo) + 1))
        else:
            ids = d4j_bids(proj) or sorted(meta)
        for bug in ids:
            if bug in dep:
                continue
            bug_index += 1
            if shard_n and (bug_index - 1) % shard_n != shard_k - 1:
                continue
            for seed in seeds:
                if (proj, str(bug), str(a.round), str(a.tries), str(seed)) in done:
                    continue
                jobs.append((proj, bug, seed, meta))

    print(f"{len(jobs)} runs to do (jobs={a.jobs})")
    WORK.mkdir(parents=True, exist_ok=True)
    build_harness(None)

    def spec(j):
        b = argparse.Namespace(**vars(a))
        b.project = j[0]
        return b, j[1], j[2], j[3]

    def show(r):
        print(f"[{r['project']}-{r['bug_id']} seed={r['seed']}] detected={r['fault_detected']} "
              f"props={r.get('property_count', '')} falseAlarm={bool(r.get('false_alarm_properties'))} "
              f"cov={r.get('line_coverage_percent', '')} {r.get('error_type', '')}", flush=True)

    if a.jobs <= 1:
        for j in jobs:
            show(run_one(*spec(j)))
    else:
        # bugs in parallel; seeds of the same bug stay sequential (they share the checkout)
        by_bug = {}
        for j in jobs:
            by_bug.setdefault((j[0], j[1]), []).append(j)
        with ProcessPoolExecutor(max_workers=a.jobs) as ex:
            futs = [ex.submit(_run_bug, [spec(j) for j in js]) for js in by_bug.values()]
            for f in as_completed(futs):
                for r in f.result():
                    show(r)


def _run_bug(specs):
    return [run_one(*s) for s in specs]


if __name__ == "__main__":
    sys.exit(main())
