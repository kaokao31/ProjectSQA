import csv
import os

RESULTS_CSV = "/workspace/results/jqwik_results.csv"

def summarize():
    if not os.path.exists(RESULTS_CSV):
        print("ยังไม่มีไฟล์ผลลัพธ์ กรุณารัน run_jqwik.py ก่อนครับ")
        return

    print("=" * 60)
    print(f"{'BUG ID':<12} | {'LINE COVERAGE':<18} | {'STATUS':<15}")
    print("=" * 60)

    success_count = 0
    total_count = 0

    with open(RESULTS_CSV, 'r') as f:
        reader = csv.DictReader(f)
        for row in reader:
            total_count += 1
            bug_name = f"{row['project']}-{row['bug_id']}"
            covered = int(row['line_covered'])
            total = int(row['line_total'])
            status = row['status']

            pct = (covered / total * 100) if total > 0 else 0
            cov_str = f"{covered}/{total} ({pct:.1f}%)"

            if status == "SUCCESS":
                success_count += 1

            print(f"{bug_name:<12} | {cov_str:<18} | {status:<15}")

    print("=" * 60)
    print(f"สรุปผลรวม: ผ่าน {success_count}/{total_count} บั๊ก")

if __name__ == "__main__":
    summarize()
