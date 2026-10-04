@echo off
REM Script for running LLM JUnit Test Generation for both Gemini and DeepSeek in background
echo Starting LLM JUnit Test Generation (Gemini + DeepSeek) in Background...
python scripts/run_llm_testgen.py --provider all --all > Gemini/Result/generator.log 2>&1
echo Done! Output saved to Gemini/Result/generator.log
