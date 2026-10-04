# 📊 สถิติการใช้งาน AI: deepseek-v4-flash (via KKU API)

- **วัน-เวลาที่ทดลอง**: 2026-09-23 04:53:08
- **โมเดลที่ใช้**: `deepseek-v4-flash`
- **คลาสเป้าหมาย**: `org.jfree.data.time.TimePeriodValues`
- **Token Slot**: Token #3 (sk_4...ivXZ)

## 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)

| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
|---|---|---|
| **เวลาที่ใช้สร้าง (Generation Time)** | 48.592 วินาที | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | 489 tokens | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | 6,775 tokens | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | 7,264 tokens | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | สำเร็จ (Code Extracted) | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน (Token #3)**: ใช้ไปแล้ว 325817 / 1000000 tokens (เหลือ 674183 tokens)
