#!/usr/bin/env python3
"""
Multi-Model LLM Test Generation Script (Gemini & DeepSeek)
Auto Key Failover, Token & Duration Tracking, Isolated Folder Hierarchy,
and Detailed Per-Bug Markdown Metrics & Prompt Archiving.
"""

import argparse
import csv
import json
import os
import re
import sys
import time
import urllib.request
import urllib.error
from pathlib import Path

# Deprecated Defects4J Lang bug IDs
DEPRECATED_BUGS = {2, 18, 25, 48}

def load_env(env_path):
    """Loads environment variables from a .env file."""
    env_vars = {}
    if not os.path.exists(env_path):
        return env_vars
    with open(env_path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, val = line.split("=", 1)
            key = key.strip()
            val = val.strip().strip("'\"")
            env_vars[key] = val
    return env_vars

def get_api_keys(env_vars, env_path=".env"):
    """
    Extracts all API keys from environment variables or .env file.
    Supports single-line comma-separated, multi-line values, and regex scanning for sk_... keys.
    """
    keys = []
    
    # 1. Check explicit key variables from parsed env_vars
    for var_name in ["KKU_API_KEYS", "INTELSPHERE_API_KEYS", "GEMINI_API_KEYS", "DEEPSEEK_API_KEYS"]:
        if var_name in env_vars and env_vars[var_name]:
            for k in env_vars[var_name].split(","):
                k = k.strip()
                if k and k not in keys:
                    keys.append(k)
                    
    # Check numbered key variables (e.g., KKU_API_KEY_1, KKU_API_KEY_2...)
    for i in range(1, 50):
        for prefix in ["KKU_API_KEY_", "INTELSPHERE_API_KEY_", "GEMINI_API_KEY_", "DEEPSEEK_API_KEY_"]:
            key_name = f"{prefix}{i}"
            if key_name in env_vars and env_vars[key_name]:
                val = env_vars[key_name].strip()
                if val and val not in keys:
                    keys.append(val)
                    
    # Check single key variables
    for key_name in ["KKU_API_KEY", "INTELSPHERE_API_KEY", "GEMINI_API_KEY", "DEEPSEEK_API_KEY", "OPENAI_API_KEY"]:
        if key_name in env_vars and env_vars[key_name]:
            val = env_vars[key_name].strip()
            if val and val not in keys:
                keys.append(val)

    # 2. Robust Regex Scan of entire .env file for any sk_... keys (handles multi-line pastes)
    if os.path.exists(env_path):
        try:
            with open(env_path, "r", encoding="utf-8") as f:
                content = f.read()
                # Matches sk_... API keys
                matched_keys = re.findall(r"sk_[A-Za-z0-9_\-]+", content)
                for k in matched_keys:
                    k = k.strip(", \t\r\n")
                    if k and k not in keys:
                        keys.append(k)
        except Exception:
            pass

    # 3. Also check OS environment variables
    for key_name in ["KKU_API_KEYS", "INTELSPHERE_API_KEYS", "GEMINI_API_KEY", "DEEPSEEK_API_KEY", "OPENAI_API_KEY"]:
        val = os.environ.get(key_name, "").strip()
        if val:
            for k in val.split(","):
                k = k.strip()
                if k and k not in keys:
                    keys.append(k)

    return keys

def mask_key(key):
    """Masks API key for logging security."""
    if not key or len(key) < 8:
        return "***"
    return f"{key[:4]}...{key[-4:]}"

def call_llm_api(prompt, key, base_url, model):
    """
    Calls LLM API endpoint (OpenAI/Intelsphere format or Google Gemini format).
    Returns (status_code, response_dict, error_message)
    """
    import ssl
    ctx = ssl._create_unverified_context()
    
    url = base_url.rstrip("/")
    if not url.endswith("/chat/completions") and not "generativelanguage" in url:
        url = f"{url}/chat/completions"
        
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {key}"
    }
    
    payload = {
        "model": model,
        "messages": [
            {"role": "system", "content": "You are a software testing assistant specialized in Java JUnit test generation."},
            {"role": "user", "content": prompt}
        ],
        "temperature": 0.2
    }

    req = urllib.request.Request(
        url,
        data=json.dumps(payload).encode("utf-8"),
        headers=headers,
        method="POST"
    )
    
    try:
        with urllib.request.urlopen(req, context=ctx, timeout=180) as resp:
            body = resp.read().decode("utf-8")
            data = json.loads(body)
            return resp.status, data, None
    except urllib.error.HTTPError as e:
        err_body = e.read().decode("utf-8", errors="ignore")
        return e.code, None, f"HTTPError {e.code}: {err_body}"
    except urllib.error.URLError as e:
        return 0, None, f"URLError: {str(e.reason)}"
    except Exception as e:
        return 0, None, f"Unexpected error: {str(e)}"

def extract_code_block(response_text):
    """Extracts Java code from markdown ```java ... ``` response."""
    match = re.search(r"```java\s*\n(.*?)```", response_text, re.DOTALL)
    if match:
        return match.group(1).strip()
    match_any = re.search(r"```\s*\n(.*?)```", response_text, re.DOTALL)
    if match_any:
        return match_any.group(1).strip()
    return response_text.strip()

def is_quota_error(status_code, error_msg):
    """Checks if error is due to token exhaustion / rate limit / quota exceeded / invalid key."""
    if status_code in (401, 403, 429):
        return True
    if error_msg:
        low_msg = error_msg.lower()
        quota_keywords = [
            "quota", "rate limit", "rate_limit", "insufficient_quota",
            "resource_exhausted", "exceeded", "token", "credit", "invalid api key", "invalid_key", "unauthorized"
        ]
        return any(kw in low_msg for kw in quota_keywords)
    return False

def process_bug(bug_info, prompt_template, api_keys, state, provider_root, base_url, model, skip_existing, dry_run, project_name="Lang"):
    """
    Processes a single Defects4J bug with per-bug folder structure:
      <provider_root>/Prompt/<project_name>_<bug_id>b/actual_prompt_<class_name>.md
      <provider_root>/Result/<project_name>_<bug_id>b/generation_metrics_<class_name>.md
      <provider_root>/TestCode/<project_name>_<bug_id>b/<class_name>Test.java
    """
    bug_id = bug_info["bug_id"]
    target_class = bug_info["classes_modified"].split(";")[0].strip()
    package_name = ".".join(target_class.split(".")[:-1])
    class_name = target_class.split(".")[-1]
    bug_folder = f"{project_name}_{bug_id}b"
    
    prompt_dir = Path(provider_root) / "Prompt" / bug_folder
    result_dir = Path(provider_root) / "Result" / bug_folder
    testcode_dir = Path(provider_root) / "TestCode" / bug_folder
    
    actual_prompt_file = prompt_dir / f"actual_prompt_{class_name}.md"
    metrics_md_file = result_dir / f"generation_metrics_{class_name}.md"
    output_code_file = testcode_dir / f"{class_name}Test.java"
    
    # Check if existing and skip if required
    if skip_existing and not dry_run and output_code_file.exists():
        print(f"[SKIP] Bug {bug_id} test code already exists at {output_code_file}", flush=True)
        return True

    # Render prompt using safe string replacement
    prompt = (
        prompt_template
        .replace("<TargetClassName>", str(class_name))
        .replace("[TargetClassName]", str(class_name))
        .replace("{bug_id}", str(bug_id))
        .replace("{target_class}", str(target_class))
        .replace("{package_name}", str(package_name))
        .replace("{class_name}", str(class_name))
        .replace("{tests_trigger}", str(bug_info.get("tests_trigger", "N/A")))
        .replace("{tests_trigger_cause}", str(bug_info.get("tests_trigger_cause", "N/A")))
    )
    
    # Auto append context if not in prompt
    if str(bug_id) not in prompt and str(target_class) not in prompt:
        prompt += f"\n\n[TARGET CLASS & BUG CONTEXT]\n- Project: {project_name}\n- Bug ID: {bug_id}\n- Target Class: {target_class}\n- Package: {package_name}\n- Triggering Failing Test: {bug_info.get('tests_trigger', 'N/A')}\n- Failure Reason: {bug_info.get('tests_trigger_cause', 'N/A')}\n"

    # Save actual prompt to Prompt/<project_name>_<bug_id>b/actual_prompt_<class_name>.md
    prompt_dir.mkdir(parents=True, exist_ok=True)
    with open(actual_prompt_file, "w", encoding="utf-8") as f:
        f.write(prompt)

    if dry_run:
        print(f"[DRY-RUN] Bug {bug_id} ({class_name}) prompt saved to {actual_prompt_file}", flush=True)
        return True

    if not api_keys:
        print("[ERROR] No API keys available! Please check .env file.", flush=True)
        return False

    max_attempts = len(api_keys) * 2
    attempts = 0
    t0 = time.time()
    
    while attempts < max_attempts:
        attempts += 1
        key_idx = state["active_key_index"] % len(api_keys)
        current_key = api_keys[key_idx]
        masked = mask_key(current_key)
        
        print(f" -> [{model}] Bug {bug_id} [Attempt {attempts}]: Key #{key_idx + 1} ({masked})...", flush=True)
        
        status_code, resp_data, error_msg = call_llm_api(
            prompt=prompt,
            key=current_key,
            base_url=base_url,
            model=model
        )
        
        choices = resp_data.get("choices", []) if (resp_data and isinstance(resp_data, dict)) else []
        has_valid_content = bool(choices and len(choices) > 0 and (choices[0].get("message", {}).get("content") or choices[0].get("message", {}).get("reasoning")))
        
        # Check quota exhaustion or empty response
        if is_quota_error(status_code, error_msg) or (status_code == 200 and not has_valid_content):
            reason = error_msg or "Empty response / No choices"
            print(f" [!] Token/Quota Exhausted or empty response on Key #{key_idx + 1} ({masked}): {reason}", flush=True)
            state["active_key_index"] = (state["active_key_index"] + 1) % len(api_keys)
            print(f" [-->] Switching to Key #{state['active_key_index'] + 1} ({mask_key(api_keys[state['active_key_index']])})...", flush=True)
            time.sleep(2)
            continue
            
        if status_code == 200 and resp_data and has_valid_content:
            t1 = time.time()
            duration_sec = round(t1 - t0, 3)
            
            # Extract content & token usage
            choices = resp_data.get("choices", [])
            msg = choices[0].get("message", {}) if choices else {}
            content = msg.get("content") or msg.get("reasoning") or ""
            usage = resp_data.get("usage", {})
            model_quota = resp_data.get("model_quota", {})
            
            prompt_tokens = usage.get("prompt_tokens", 0)
            completion_tokens = usage.get("completion_tokens", 0)
            total_tokens = usage.get("total_tokens", prompt_tokens + completion_tokens)
            
            daily_used = model_quota.get("daily_usage_tokens", "N/A")
            daily_quota = model_quota.get("daily_quota_tokens", "N/A")
            daily_rem = model_quota.get("daily_remaining_tokens", "N/A")

            # Save test code
            test_code = extract_code_block(content)
            testcode_dir.mkdir(parents=True, exist_ok=True)
            with open(output_code_file, "w", encoding="utf-8") as f:
                f.write(test_code)

            # Save Markdown metrics report inside Result/Lang_<bug_id>b/generation_metrics_<class_name>.md
            result_dir.mkdir(parents=True, exist_ok=True)
            timestamp_str = time.strftime("%Y-%m-%d %H:%M:%S")
            
            metrics_md_content = f"""# 📊 สถิติการใช้งาน AI: {model} (via KKU API)

- **วัน-เวลาที่ทดลอง**: {timestamp_str}
- **โมเดลที่ใช้**: `{model}`
- **คลาสเป้าหมาย**: `{target_class}`
- **Token Slot**: Token #{key_idx + 1} ({masked})

## 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)

| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
|---|---|---|
| **เวลาที่ใช้สร้าง (Generation Time)** | {duration_sec} วินาที | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | {prompt_tokens:,} tokens | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | {completion_tokens:,} tokens | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | {total_tokens:,} tokens | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | สำเร็จ (Code Extracted) | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน (Token #{key_idx + 1})**: ใช้ไปแล้ว {daily_used} / {daily_quota} tokens (เหลือ {daily_rem} tokens)
"""
            with open(metrics_md_file, "w", encoding="utf-8") as f:
                f.write(metrics_md_content)
                
            # Log metrics to summary CSV at Result/generation_results.csv
            root_result_dir = Path(provider_root) / "Result"
            root_result_dir.mkdir(parents=True, exist_ok=True)
            csv_path = root_result_dir / "generation_results.csv"
            write_header = not csv_path.exists()
            
            with open(csv_path, "a", newline="", encoding="utf-8") as f:
                writer = csv.writer(f)
                if write_header:
                    writer.writerow([
                        "bug_id", "target_class", "status", "duration_sec",
                        "prompt_tokens", "completion_tokens", "total_tokens",
                        "model", "key_used", "timestamp"
                    ])
                writer.writerow([
                    bug_id, target_class, "SUCCESS", duration_sec,
                    prompt_tokens, completion_tokens, total_tokens,
                    model, masked, timestamp_str
                ])
                
            # Update state summary
            state["total_bugs_processed"] += 1
            state["total_duration_sec"] += duration_sec
            state["total_prompt_tokens"] += prompt_tokens
            state["total_completion_tokens"] += completion_tokens
            state["total_tokens"] += total_tokens
            save_summary(state, root_result_dir)
            
            print(f" [+] Bug {bug_id} ({class_name}) DONE in {duration_sec}s | Tokens: {total_tokens} (P: {prompt_tokens}, C: {completion_tokens}) | Saved to {output_code_file}", flush=True)
            return True
        else:
            print(f" [!] Call failed for Bug {bug_id}: {error_msg}", flush=True)
            time.sleep(3)
            
    print(f" [X] Bug {bug_id} failed after {attempts} attempts.", flush=True)
    return False

def save_summary(state, result_dir):
    """Saves execution summary json."""
    summary_path = Path(result_dir) / "execution_summary.json"
    summary_path.parent.mkdir(parents=True, exist_ok=True)
    with open(summary_path, "w", encoding="utf-8") as f:
        json.dump(state, f, indent=2)

def run_provider(provider_name, bugs, env_vars, api_keys, state, args, project_name="Lang"):
    """Runs generation for a specific provider (Gemini or DeepSeek)."""
    if provider_name == "gemini":
        provider_root = "Gemini"
        prompt_template_path = args.prompt or "Gemini/Prompt/master_prompt.md"
        model = args.model or env_vars.get("INTELSPHERE_GEMINI_MODEL", "gemini-3.7-flash")
    elif provider_name == "deepseek":
        provider_root = "deepseek"
        prompt_template_path = args.prompt or "deepseek/Prompt/master_prompt.md"
        model = args.model or env_vars.get("INTELSPHERE_DEEPSEEK_MODEL", "deepseek-v4-flash")
    else:
        raise ValueError(f"Unknown provider: {provider_name}")

    base_url = args.base_url or env_vars.get("INTELSPHERE_BASE_URL", "https://gen.ai.kku.ac.th/api/v1")

    print("==================================================")
    print(f" Executing Provider: {provider_name.upper()} | Project: {project_name}")
    print("==================================================")
    print(f"Base URL      : {base_url}")
    print(f"Model         : {model}")
    print(f"Master Prompt : {prompt_template_path}")
    print(f"Provider Root : {provider_root}")
    print("==================================================\n", flush=True)

    if not os.path.exists(prompt_template_path):
        print(f"[ERROR] Master prompt template file not found: {prompt_template_path}", flush=True)
        return

    with open(prompt_template_path, "r", encoding="utf-8") as f:
        prompt_template = f.read()

    for bug in bugs:
        process_bug(
            bug_info=bug,
            prompt_template=prompt_template,
            api_keys=api_keys,
            state=state,
            provider_root=provider_root,
            base_url=base_url,
            model=model,
            skip_existing=not args.force,
            dry_run=args.dry_run,
            project_name=project_name
        )

def main():
    import glob
    parser = argparse.ArgumentParser(description="Multi-Model LLM JUnit Test Generator (Gemini & DeepSeek)")
    parser.add_argument("--env-file", default=".env", help="Path to .env file")
    parser.add_argument("--provider", choices=["gemini", "deepseek", "all"], default="all", help="Provider/Folder target (gemini, deepseek, or all)")
    parser.add_argument("--prompt", help="Override Master prompt path")
    parser.add_argument("--meta-csv", default="dataset/defects4j/Lang_metadata.csv", help="Metadata CSV path")
    parser.add_argument("--all-projects", action="store_true", help="Iterate continuously over all projects in dataset/defects4j/")
    parser.add_argument("--bug", type=int, help="Single bug ID to process")
    parser.add_argument("--all", action="store_true", help="Process all active bugs")
    parser.add_argument("--skip-existing", action="store_true", default=True, help="Skip existing test files")
    parser.add_argument("--force", action="store_true", help="Force overwrite existing generated files")
    parser.add_argument("--base-url", help="API Base URL")
    parser.add_argument("--model", help="Override LLM Model name")
    parser.add_argument("--dry-run", action="store_true", help="Dry run without API calls")

    args = parser.parse_args()
    
    # Load Environment
    env_vars = load_env(args.env_file)
    api_keys = get_api_keys(env_vars, args.env_file)

    print("==================================================")
    print(" Multi-Model LLM JUnit Generator (Defects4J)")
    print("==================================================")
    print(f"API Keys    : {len(api_keys)} key(s) detected: {[mask_key(k) for k in api_keys]}")
    print(f"Target      : {args.provider.upper()}")
    print("==================================================\n", flush=True)

    if not api_keys and not args.dry_run:
        print("[!] Warning: ไม่พบ API Key ในไฟล์ .env กรุณาใส่ API Key ในไฟล์ .env ก่อนเริ่มรันครับ", flush=True)
        sys.exit(1)

    # Determine which projects to process
    if args.all_projects:
        csv_files = sorted(glob.glob("dataset/defects4j/*_metadata.csv"))
    else:
        csv_files = [args.meta_csv]

    state = {
        "active_key_index": 0,
        "total_bugs_processed": 0,
        "total_duration_sec": 0,
        "total_prompt_tokens": 0,
        "total_completion_tokens": 0,
        "total_tokens": 0,
        "start_time": time.strftime("%Y-%m-%d %H:%M:%S")
    }

    for csv_file in csv_files:
        if not os.path.exists(csv_file):
            print(f"[WARN] CSV not found: {csv_file}, skipping.", flush=True)
            continue
            
        proj_name = Path(csv_file).stem.replace("_metadata", "")
        bugs = []
        with open(csv_file, "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row in reader:
                b_id = int(row["bug_id"])
                if proj_name == "Lang" and b_id in DEPRECATED_BUGS:
                    continue
                bugs.append(row)

        if args.bug:
            bugs = [b for b in bugs if int(b["bug_id"]) == args.bug]

        if not bugs:
            continue

        print(f"\n>>> Processing Project: {proj_name} ({len(bugs)} bugs) <<<\n", flush=True)

        if args.provider in ["gemini", "all"]:
            run_provider("gemini", bugs, env_vars, api_keys, state, args, project_name=proj_name)

        if args.provider in ["deepseek", "all"]:
            run_provider("deepseek", bugs, env_vars, api_keys, state, args, project_name=proj_name)


    print("\n==================================================")
    print(" All Execution Tasks Completed!")
    print(f" Total Bugs Processed: {state['total_bugs_processed']}")
    print(f" Total Duration      : {round(state['total_duration_sec'], 2)} seconds")
    print(f" Total Tokens Used   : {state['total_tokens']}")
    print("==================================================", flush=True)

if __name__ == "__main__":
    main()
