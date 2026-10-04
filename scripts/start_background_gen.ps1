# PowerShell script for running LLM JUnit Test Generation for both Gemini and DeepSeek in Background
Write-Host "Starting LLM JUnit Test Generation (Gemini + DeepSeek) in Background..." -ForegroundColor Green

Start-Process -FilePath "python" -ArgumentList "scripts/run_llm_testgen.py --provider all --all-projects" -RedirectStandardOutput "Gemini/Result/generator.log" -RedirectStandardError "Gemini/Result/generator_err.log" -NoNewWindow

Write-Host "Background task started for Gemini and DeepSeek. Logs are being written to Gemini/Result/generator.log" -ForegroundColor Yellow
