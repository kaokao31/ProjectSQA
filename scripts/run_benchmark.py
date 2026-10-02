#!/usr/bin/env python3
"""วนรัน scripts/run_evosuite.py กับหลายโปรเจกต์ / หลายบั๊ก / หลาย budget พร้อมระบบ resume

- อ่านรายชื่อบั๊กจาก dataset/defects4j/<Project>_metadata.csv (มีเฉพาะบั๊ก active)
- อ่าน results/evosuite_results.csv เพื่อข้ามงานที่ทำเสร็จแล้ว (รันซ้ำคำสั่งเดิมได้ = resume)
- แผนการรัน --plan "60:1,120:2" คือ budget 60 วินาที -> Result_Round1, budget 120 -> Result_Round2

ตัวอย่าง:
  python3 scripts/run_benchmark.py --dry-run                      # ดูแผนและเวลาโดยประมาณ ยังไม่รัน
  python3 scripts/run_benchmark.py --projects Chart --limit 2     # ลองเล็ก ๆ
  python3 scripts/run_benchmark.py                                # รันทุกโปรเจกต์ที่มี metadata
"""
import argparse
import csv
import json
import os
import random
import re
import shutil
import subprocess
import sys
import threading
import time
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

REPO = Path(os.environ.get("REPO_ROOT", "/workspace"))
WORK = Path(os.environ.get("D4J_WORK", "/work"))
META_DIR = REPO / "dataset" / "defects4j"
RESULTS_CSV = REPO / "results" / "evosuite_results.csv"
BATCH_LOG = REPO / "results" / "batch.log"
RUNNER = REPO / "scripts" / "run_evosuite.py"
DEFAULT_COLS = ["bug_id", "project_id", "revision_id_buggy", "revision_id_fixed",
                "report_id", "classes_modified", "tests_trigger", "tests_trigger_cause"]

_lock = threading.Lock()


def log(msg):
    line = f"{time.strftime('%Y-%m-%d %H:%M:%S')} {msg}"
    with _lock:
        print(line, flush=True)
        BATCH_LOG.parent.mkdir(parents=True, exist_ok=True)
        with open(BATCH_LOG, "a", encoding="utf-8") as f:
            f.write(line + "\n")


def list_projects():
    return sorted(p.name[: -len("_metadata.csv")] for p in META_DIR.glob("*_metadata.csv"))


def read_bugs(project):
    """คืน [(bug_id, จำนวนคลาสที่ถูกแก้)] ของบั๊กทั้งหมดในไฟล์ metadata"""
    path = META_DIR / f"{project}_metadata.csv"
    with open(path, newline="", encoding="utf-8-sig") as f:
        rows = [r for r in csv.reader(f) if r]
    if not rows:
        return []
    if rows[0][0].strip().isdigit():          # ไม่มี header
        cols = DEFAULT_COLS
    else:                                     # มี header
        cols = [c.strip().lower().replace(".", "_") for c in rows[0]]
        rows = rows[1:]
    out = []
    for r in rows:
        d = dict(zip(cols, r))
        b = d.get("bug_id", "").strip()
        if b.isdigit():
            n = len([c for c in re.split(r"[;,]", d.get("classes_modified", "")) if c.strip()])
            out.append((int(b), max(n, 1)))
    return out


def load_done():
    """(project, bug, budget, seed) -> error_type ของแถวล่าสุดใน CSV ผลรวม"""
    done = {}
    if not RESULTS_CSV.exists():
        return done
    with open(RESULTS_CSV, newline="", encoding="utf-8") as f:
        for r in csv.DictReader(f):
            try:
                key = (r["project"], int(r["bug_id"]), int(r["search_budget"]), str(r["seed"]).strip())
            except (KeyError, ValueError, TypeError):
                continue
            done[key] = (r.get("error_type") or "").strip()
    return done


def parse_plan(s):
    plan = []
    for item in s.split(","):
        b, r = item.split(":")
        plan.append((int(b), int(r)))
    return plan


def est_seconds(ncls, budget):
    # ประมาณคร่าว ๆ จากที่วัดกับ Lang: generate ≈ budget+25 วินาทีต่อคลาส + คอมไพล์/รันเทสต์/JaCoCo ≈ 45 วินาทีต่อรัน
    return ncls * (budget + 25) + 45


def cleanup(project, bug):
    for ver in ("b", "f"):
        shutil.rmtree(WORK / f"{project}_{bug}{ver}", ignore_errors=True)
    shutil.rmtree(WORK / "evo_out" / f"{project}_{bug}f", ignore_errors=True)


def run_bug(project, bug, ncls, runs, args, stats):
    use_jvm = (not args.no_evo_jvm_cp) and project not in args.jvm_cp_exclude
    for budget, rnd, seed in runs:
        cmd = [sys.executable, str(RUNNER), "-p", project, "--bug", str(bug), "--budget", str(budget),
               "--seed", seed, "--round", str(rnd)]
        if use_jvm:
            cmd.append("--evo-jvm-cp")
        timeout = ncls * (budget * 3 + 300) + 1800
        t0 = time.time()
        try:
            p = subprocess.run(cmd, capture_output=True, text=True, errors="replace", timeout=timeout)
            out = p.stdout
        except subprocess.TimeoutExpired:
            out = "BATCH_TIMEOUT"
        dt = time.time() - t0
        summary = next((l.strip() for l in out.splitlines() if "Status:" in l), "")
        err = next((l.strip() for l in out.splitlines() if l.startswith("ERROR")), "")
        if out == "BATCH_TIMEOUT":
            summary, err = "", "ERROR : BATCH_TIMEOUT (ไม่ได้บันทึกผล จะถูกรันใหม่ตอน resume)"
        with _lock:
            stats["done"] += 1
            stats["secs"] += dt
            if err:
                stats["errors"] += 1
            remaining = stats["total"] - stats["done"]
            eta_h = (stats["secs"] / stats["done"]) * remaining / max(args.workers, 1) / 3600
        log(f"[{stats['done']}/{stats['total']}] {project}-{bug} b{budget} s{seed} ({dt:.0f}s, ETA ~{eta_h:.1f} ชม.) "
            f"{summary or 'ไม่มีสรุป'} {err}")
    if not args.keep_work:
        cleanup(project, bug)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--projects", default="", help="คั่นด้วย , (ค่าเริ่มต้น: ทุกโปรเจกต์ที่มีไฟล์ metadata)")
    ap.add_argument("--bugs", default="", help="เลือกเฉพาะบั๊ก เช่น 1,2,3 (ใช้กับโปรเจกต์เดียว)")
    ap.add_argument("--plan", default="60:1,120:2", help="budget:round คั่นด้วย , (เริ่มต้น 60:1,120:2)")
    ap.add_argument("--seeds", default="1", help="เช่น 1 หรือ 1,2,3")
    ap.add_argument("--limit", type=int, default=0, help="รันแค่ N บั๊กแรกของแต่ละโปรเจกต์ (ไว้ลองระบบ)")
    ap.add_argument("--sample", type=int, default=0, help="สุ่ม K บั๊กต่อโปรเจกต์ (เฉพาะกรณีเวลาไม่พอ ต้องเขียนลงรายงาน)")
    ap.add_argument("--sample-seed", type=int, default=42)
    ap.add_argument("--workers", type=int, default=1, help="จำนวนบั๊กที่รันพร้อมกัน (ยังไม่ได้ทดสอบ >1)")
    ap.add_argument("--retry-errors", action="store_true", help="รันซ้ำงานที่จบด้วย TOOL_ERROR")
    ap.add_argument("--no-evo-jvm-cp", action="store_true", help="ไม่ส่ง --evo-jvm-cp ให้ทุกโปรเจกต์")
    ap.add_argument("--jvm-cp-exclude", default="Lang", help="โปรเจกต์ที่ไม่ใช้ --evo-jvm-cp (Lang รันไปก่อนมีตัวเลือกนี้)")
    ap.add_argument("--keep-work", action="store_true", help="ไม่ลบโฟลเดอร์ checkout หลังจบแต่ละบั๊ก")
    ap.add_argument("--dry-run", action="store_true", help="แสดงแผนและเวลาโดยประมาณ แล้วออก")
    args = ap.parse_args()
    args.jvm_cp_exclude = {x.strip() for x in args.jvm_cp_exclude.split(",") if x.strip()}

    plan = parse_plan(args.plan)
    seeds = [s.strip() for s in args.seeds.split(",") if s.strip()]
    projects = [p.strip() for p in args.projects.split(",") if p.strip()] or list_projects()
    done = load_done()

    jobs, sampled = [], {}
    print(f"{'project':14}{'bugs':>6}{'classes':>9}{'pending':>9}{'est.hours':>11}")
    tot_pending = tot_secs = 0
    for project in projects:
        if not (META_DIR / f"{project}_metadata.csv").exists():
            log(f"[ข้าม] ไม่พบ {project}_metadata.csv")
            continue
        bugs = read_bugs(project)
        if args.bugs:
            want = {int(x) for x in args.bugs.split(",") if x.strip()}
            bugs = [b for b in bugs if b[0] in want]
        if args.sample and len(bugs) > args.sample:
            rng = random.Random(f"{args.sample_seed}-{project}")
            bugs = sorted(rng.sample(bugs, args.sample))
            sampled[project] = [b for b, _ in bugs]
        if args.limit:
            bugs = bugs[: args.limit]
        n_cls = n_pend = 0
        secs = 0.0
        for bug, ncls in bugs:
            n_cls += ncls
            runs = []
            for budget, rnd in plan:
                for seed in seeds:
                    key = (project, bug, budget, seed)
                    st = done.get(key)
                    if key in done and not (args.retry_errors and st == "TOOL_ERROR"):
                        continue
                    runs.append((budget, rnd, seed))
                    secs += est_seconds(ncls, budget)
            if runs:
                jobs.append((project, bug, ncls, runs))
                n_pend += len(runs)
        tot_pending += n_pend
        tot_secs += secs
        print(f"{project:14}{len(bugs):>6}{n_cls:>9}{n_pend:>9}{secs / 3600 / max(args.workers, 1):>11.1f}")
    print(f"{'รวม':14}{'':>6}{'':>9}{tot_pending:>9}{tot_secs / 3600 / max(args.workers, 1):>11.1f}"
          f"   (ประมาณคร่าว ๆ, workers={args.workers})")

    if sampled:
        (REPO / "results" / "sample_bugs.json").write_text(json.dumps(sampled, indent=1), encoding="utf-8")
        print("บันทึกรายชื่อบั๊กที่สุ่มไว้ที่ results/sample_bugs.json")
    if args.dry_run or not jobs:
        if not jobs:
            print("ไม่มีงานค้าง (ทุกอย่างมีผลใน CSV แล้ว)")
        return

    stats = {"done": 0, "total": tot_pending, "secs": 0.0, "errors": 0}
    log(f"เริ่มรัน {tot_pending} งาน จาก {len(jobs)} บั๊ก (workers={args.workers}, plan={args.plan}, seeds={args.seeds})")
    try:
        with ThreadPoolExecutor(max_workers=max(args.workers, 1)) as ex:
            futs = [ex.submit(run_bug, p, b, n, r, args, stats) for p, b, n, r in jobs]
            for f in futs:
                f.result()
    except KeyboardInterrupt:
        log("หยุดโดยผู้ใช้ (Ctrl+C) รันคำสั่งเดิมซ้ำเพื่อทำต่อ (resume)")
        os._exit(130)
    log(f"เสร็จ: {stats['done']} งาน, มี error {stats['errors']} งาน (ดูรายละเอียดใน {BATCH_LOG.relative_to(REPO)})")


if __name__ == "__main__":
    sys.exit(main())
