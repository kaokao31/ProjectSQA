# 📊 สถิติการใช้งาน AI: gemini-3.7-flash (via KKU API)

- **วัน-เวลาที่ทดลอง**: 2026-10-05 01:27:40
- **โมเดลที่ใช้**: `gemini-3.7-flash`
- **คลาสเป้าหมาย**: `org.apache.commons.cli.CommandLine`
- **Token Slot**: Token #1 (sk_O...fCaS)

## 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)

| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
|---|---|---|
| **เวลาที่ใช้สร้าง (Generation Time)** | 15.622 วินาที | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | 423 tokens | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | 2,435 tokens | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | 3,313 tokens | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | สำเร็จ (Code Extracted) | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน (Token #1)**: ใช้ไปแล้ว 10355 / 350000 tokens (เหลือ 339645 tokens)
