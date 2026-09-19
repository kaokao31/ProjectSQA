# ProjectSQA — AI-Assisted Testing vs. Automatic Test Case Generation Algorithms

CP353201 Software Quality Assurance (1/2569), Khon Kaen University

## สมาชิก
| รหัสนักศึกษา | ชื่อ-สกุล | หน้าที่ |
|---|---|---|
| 673380388-2 | กฤษฎา นามมนต์เทียน | |
| 673380392-1 | กานดิทัต นามสุดตา | |
| 673380402-4 | ดรัณภพ สุริเตอร์ | |

## Quick start
```bash
git clone <repo-url> && cd ProjectSQA
docker compose -f docker/docker-compose.yml up -d --build
docker compose -f docker/docker-compose.yml exec sqa bash
bash scripts/check_env.sh       # ทดสอบ Lang 1b
```

## ขอบเขต
- Defects4J 3.0.1, project `Lang`, 61 active bugs (`dataset/defects4j/active-bugs.csv`)
- ตัด deprecated: 2, 18, 25, 48 (`deprecated-bugs.csv`)
- Java 11, timezone `America/Los_Angeles`

## โครงสร้าง
- `EvoSuite/`, `jqwik/` — Code, Configuration, Result_Round1, Result_Round2, Test
- `Claude/`, `Gemini/` — Prompt, Result, TestCode
- `dataset/`, `scripts/`, `results/`, `docker/`
