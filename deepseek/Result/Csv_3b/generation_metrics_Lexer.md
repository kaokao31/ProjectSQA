# 📊 สถิติการใช้งาน AI: deepseek-v4-flash (via KKU API)

- **วัน-เวลาที่ทดลอง**: 2026-10-02 14:46:25
- **โมเดลที่ใช้**: `deepseek-v4-flash`
- **คลาสเป้าหมาย**: `org.apache.commons.csv.Lexer`
- **Token Slot**: Token #2 (sk_5...Em6M)

## 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)

| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
|---|---|---|
| **เวลาที่ใช้สร้าง (Generation Time)** | 54.012 วินาที | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | 427 tokens | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | 6,718 tokens | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | 7,145 tokens | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | สำเร็จ (Code Extracted) | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน (Token #2)**: ใช้ไปแล้ว 395685 / 1000000 tokens (เหลือ 604315 tokens)
