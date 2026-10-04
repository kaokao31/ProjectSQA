# 📊 สถิติการใช้งาน AI: gemini-3.7-flash (via KKU API)

- **วัน-เวลาที่ทดลอง**: 2026-09-23 00:00:31
- **โมเดลที่ใช้**: `gemini-3.7-flash`
- **คลาสเป้าหมาย**: `org.apache.commons.lang.time.StopWatch`
- **Token Slot**: Token #7 (sk_5...Em6M)

## 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)

| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
|---|---|---|
| **เวลาที่ใช้สร้าง (Generation Time)** | 25.378 วินาที | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | 531 tokens | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | 1,939 tokens | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | 2,956 tokens | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | สำเร็จ (Code Extracted) | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน (Token #7)**: ใช้ไปแล้ว 2956 / 350000 tokens (เหลือ 347044 tokens)
