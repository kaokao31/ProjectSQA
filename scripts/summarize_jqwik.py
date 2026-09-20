import pandas as pd
import sys
from pathlib import Path

csv_path = Path("results/jqwik_results.csv")

if not csv_path.exists():
    print("❌ ไม่พบไฟล์ results/jqwik_results.csv")
    sys.exit(1)

try:
    df = pd.read_csv(csv_path)
except Exception:
    # กรณี CSV มีแถวไม่สมบูรณ์ค้างอยู่
    df = pd.read_csv(csv_path, on_bad_lines='skip')

# ตัดข้อมูล Deprecated Bugs ออกเพื่อความชัวร์
DEPRECATED_BUGS = [2, 18, 25, 48]
if "bug_id" in df.columns:
    df = df[~df["bug_id"].isin(DEPRECATED_BUGS)]

# แยกข้อมูลตาม Round
r1 = df[df["round"].astype(str).str.strip() == "1"]
r2 = df[df["round"].astype(str).str.strip() == "2"]

print("\n==================== สรุปผลการทดลอง jqwik (Round 1 vs Round 2) ====================")
print(f"{'Metric':<32} | {'Round 1 (60s)':<15} | {'Round 2 (120s)':<15}")
print("-" * 72)
print(f"{'Total Valid Bugs':<32} | {len(r1):<15} | {len(r2):<15}")

if "compile_status" in df.columns:
    c1 = (r1['compile_status'] == 'SUCCESS').sum()
    c2 = (r2['compile_status'] == 'SUCCESS').sum()
    print(f"{'Compile SUCCESS':<32} | {c1:<15} | {c2:<15}")

if "test_status" in df.columns:
    t1 = (r1['test_status'] != 'SUCCESS').sum()
    t2 = (r2['test_status'] != 'SUCCESS').sum()
    print(f"{'TOOL_ERROR / TIMEOUT':<32} | {t1:<15} | {t2:<15}")

if "line_coverage_percent" in df.columns:
    cov1_vals = pd.to_numeric(r1['line_coverage_percent'], errors='coerce').dropna()
    cov2_vals = pd.to_numeric(r2['line_coverage_percent'], errors='coerce').dropna()
    cov1 = f"{cov1_vals.mean():.2f}%" if not cov1_vals.empty else "N/A"
    cov2 = f"{cov2_vals.mean():.2f}%" if not cov2_vals.empty else "N/A"
    print(f"{'Avg Line Coverage (%)':<32} | {cov1:<15} | {cov2:<15}")

if "branch_coverage_percent" in df.columns:
    bcov1_vals = pd.to_numeric(r1['branch_coverage_percent'], errors='coerce').dropna()
    bcov2_vals = pd.to_numeric(r2['branch_coverage_percent'], errors='coerce').dropna()
    bcov1 = f"{bcov1_vals.mean():.2f}%" if not bcov1_vals.empty else "N/A"
    bcov2 = f"{bcov2_vals.mean():.2f}%" if not bcov2_vals.empty else "N/A"
    print(f"{'Avg Branch Coverage (%)':<32} | {bcov1:<15} | {bcov2:<15}")

if "fault_detected" in df.columns:
    f1 = (r1['fault_detected'].astype(str).str.lower() == 'true').sum()
    f2 = (r2['fault_detected'].astype(str).str.lower() == 'true').sum()
    print(f"{'Fault Detected (Bugs Found)':<32} | {f1:<15} | {f2:<15}")

print("====================================================================================")
