# 📊 สถิติการใช้งาน AI: deepseek-v4-flash (via KKU API)

- **วัน-เวลาที่ทดลอง**: 2026-10-03 01:27:17
- **โมเดลที่ใช้**: `deepseek-v4-flash`
- **คลาสเป้าหมาย**: `com.fasterxml.jackson.databind.deser.impl.CreatorCollector`
- **Token Slot**: Token #6 (sk_7...yxOv)

## 1. ข้อมูลประสิทธิภาพ (Empirical Metrics from KKU IntelSphere API)

| พารามิเตอร์ | ค่าที่วัดได้จริง | แหล่งที่มาของข้อมูล |
|---|---|---|
| **เวลาที่ใช้สร้าง (Generation Time)** | 1128.622 วินาที | จับเวลาผ่าน Python System Clock |
| **Input Tokens (Prompt + Source Code)** | 502 tokens | คืนค่าจาก API (`usage.prompt_tokens`) |
| **Output Tokens (Generated Test Code)** | 2,966 tokens | คืนค่าจาก API (`usage.completion_tokens`) |
| **Total Tokens** | 3,468 tokens | คืนค่าจาก API (`usage.total_tokens`) |
| **สถานะการสร้าง** | สำเร็จ (Code Extracted) | สกัดบล็อก JUnit 4 เรียบร้อย |

> **Token Quota ประจำวัน (Token #6)**: ใช้ไปแล้ว 169739 / 1000000 tokens (เหลือ 830261 tokens)
