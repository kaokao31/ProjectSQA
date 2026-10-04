# 📊 สถิติการใช้งาน AI: deepseek-v4-flash (via KKU API)

- **วัน-เวลาที่ทดลอง**: 2026-10-03 05:50:24
- **โมเดลที่ใช้**: `deepseek-v4-flash`
- **คลาสเป้าหมาย**: `com.fasterxml.jackson.databind.ObjectReader`
- **Token Slot**: Token #8 (sk_t...Zcy7)

## 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)

| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
|---|---|---|
| **เวลาที่ใช้สร้าง (Generation Time)** | 219.911 วินาที | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | 491 tokens | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | 5,442 tokens | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | 5,933 tokens | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | สำเร็จ (Code Extracted) | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน (Token #8)**: ใช้ไปแล้ว 443192 / 1000000 tokens (เหลือ 556808 tokens)
