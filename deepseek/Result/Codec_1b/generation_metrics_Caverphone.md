# 📊 สถิติการใช้งาน AI: deepseek-v4-flash (via KKU API)

- **วัน-เวลาที่ทดลอง**: 2026-10-05 01:59:37
- **โมเดลที่ใช้**: `deepseek-v4-flash`
- **คลาสเป้าหมาย**: `org.apache.commons.codec.language.Caverphone`
- **Token Slot**: Token #1 (sk_O...fCaS)

## 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)

| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
|---|---|---|
| **เวลาที่ใช้สร้าง (Generation Time)** | 629.081 วินาที | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | 429 tokens | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | 6,012 tokens | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | 6,441 tokens | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | สำเร็จ (Code Extracted) | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน (Token #1)**: ใช้ไปแล้ว 483832 / 1000000 tokens (เหลือ 516168 tokens)
