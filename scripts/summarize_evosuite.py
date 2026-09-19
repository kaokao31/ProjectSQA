import pandas as pd

df = pd.read_csv("results/evosuite_results.csv")

# แยกข้อมูล Round 1 (60s) และ Round 2 (120s)
r1 = df[df["search_budget"] == 60]
r2 = df[df["search_budget"] == 120]

print("==================== สรุปผลการทดลอง EvoSuite ====================")
print(f"{'Metric':<32} | {'Round 1 (60s)':<15} | {'Round 2 (120s)':<15}")
print("-" * 68)
print(f"{'Total Bugs Executed':<32} | {len(r1):<15} | {len(r2):<15}")
print(f"{'Compile SUCCESS':<32} | {(r1['compile_status'] == 'SUCCESS').sum():<15} | {(r2['compile_status'] == 'SUCCESS').sum():<15}")
print(f"{'TOOL_ERROR':<32} | {(r1['test_status'] == 'TOOL_ERROR').sum():<15} | {(r2['test_status'] == 'TOOL_ERROR').sum():<15}")
print(f"{'Avg Line Coverage (%)':<32} | {r1['line_coverage_percent'].dropna().mean():.2f}%{'':<9} | {r2['line_coverage_percent'].dropna().mean():.2f}%")
print(f"{'Avg Branch Coverage (%)':<32} | {r1['branch_coverage_percent'].dropna().mean():.2f}%{'':<9} | {r2['branch_coverage_percent'].dropna().mean():.2f}%")
print(f"{'Fault Detected':<32} | {(r1['fault_detected'] == True).sum():<15} | {(r2['fault_detected'] == True).sum():<15}")
print("=================================================================")
