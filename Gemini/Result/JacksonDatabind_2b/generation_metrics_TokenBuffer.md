# 📊 สถิติการใช้งาน AI: gemini-3.5-flash-lite (via KKU API)

- **วัน-เวลาที่ทดลอง**: 2026-09-23 23:40:41
- **โมเดลที่ใช้**: `gemini-3.5-flash-lite`
- **คลาสเป้าหมาย**: `com.fasterxml.jackson.databind.util.TokenBuffer`
- **Token Slot**: Token #3 (sk_4...ivXZ)

## 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)

| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
|---|---|---|
| **เวลาที่ใช้สร้าง (Generation Time)** | 9.022 วินาที | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | 425 tokens | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | 3,103 tokens | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | 3,528 tokens | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | สำเร็จ (Code Extracted) | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน (Token #3)**: ใช้ไปแล้ว 159454 / 350000 tokens (เหลือ 190546 tokens)
