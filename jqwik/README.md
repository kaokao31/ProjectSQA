# jqwik (PBT) กับ Defects4J

มี 2 โหมด:

| โหมด | property มาจากไหน | ใช้กับ | ผลลัพธ์ |
|---|---|---|---|
| **auto** (หลัก) | สร้างอัตโนมัติด้วย `pbt.auto.Generate` | ทุก project, 17 project | `results/jqwik_auto*.csv` |
| hand-written | คนเขียนไว้ใน `jqwik/Test/<P>_<B>/` | เฉพาะบักที่มีคนเขียน property | `results/jqwik_results.csv` |

## โครงสร้าง
```
jqwik/
  Harness/src/pbt/harness/SeedControlHook.java  บังคับ seed/tries เดียวกันให้ทุก property (-Dpbt.seed, -Dpbt.tries)
  Harness/src/pbt/auto/                          generator + runtime ของโหมด auto
  Auto/<P>_<B>/                                  property ที่ generate แล้ว (+ AUTO_GENERATED.json = สถิติ API ที่ทดสอบ/ข้าม)
  Test/<P>_<B>/                                  property ที่คนเขียน
  Controls/{Positive,Negative,FalseAlarm}/       ชุดตรวจว่า pipeline ถูกต้อง
  Result_auto_Round<R>/<P>_<B>/tries<T>_seed<S>/ run.log, reports_record|fixed|buggy/ (JUnit XML), properties.json, jacoco.csv
```

## โหมด auto ทำงานอย่างไร
1. **Generate** (ครั้งเดียวต่อบัก): โหลดคลาสใน `classes.modified` ของทั้งเวอร์ชัน buggy และ fixed ด้วย reflection
   แล้วสร้าง **property 1 ตัวต่อ 1 method ที่ไม่ใช่ private** โดยใช้เฉพาะ method, constructor และ factory
   ที่มี signature เหมือนกันทั้งสองเวอร์ชัน ถ้าคลาสเป็น abstract จะใช้ subclass ที่เป็น concrete ในโค้ดเดียวกันแทน
   ถ้าคลาส override `equals` หรือ implement `Comparable` จะสร้าง contract property เพิ่ม
   generator **ไม่เห็น diff ของ fix, trigger test หรือ developer test** เห็นแค่ API
2. **Input**: jqwik สุ่ม "recipe" ของ argument และ receiver (primitive, String รวมถึง string ที่หน้าตาเป็นตัวเลข,
   array, collection, map, enum, Date, Locale, in-memory IO และ object ที่สร้างผ่าน constructor ลึกไม่เกิน 2 ชั้น)
   ตัว recipe เป็นข้อมูลล้วน ไม่รันโค้ดที่ทดสอบระหว่างสุ่ม ดังนั้น seed เดียวกันจะได้ input ชุดเดียวกันทั้งสองเวอร์ชัน
3. **Oracle แบบ regression** (หลักเดียวกับ EvoSuite และ Randoop ในงานวิจัยบน Defects4J):
   - `record` บน fixed: บันทึกพฤติกรรมที่สังเกตได้ ได้แก่ return value (รวม type), ชนิดของ exception,
     state ของ receiver หลังเรียก และ argument ที่ถูกแก้ไข
   - `check` บน fixed อีกรอบ: ถ้าผลไม่ตรงแปลว่าพฤติกรรมไม่นิ่ง (ขึ้นกับเวลา ค่าสุ่ม หรือ identity hash)
     property นั้นจะถูกตัดเป็น `FALSE_ALARM_ON_FIXED` อัตโนมัติ
   - `check` บน buggy: ถ้าพฤติกรรมต่างจาก snapshot ของ fixed ถือว่า **detected**
   - ผลที่ขึ้นกับทรัพยากรเครื่อง (OutOfMemoryError, TIMEOUT ของ fixed) ถือว่าสรุปไม่ได้และไม่นำมาเทียบ
     กรณี buggy TIMEOUT จะรันซ้ำโดยให้เวลา 5 เท่าก่อนตัดสิน
   - path ที่ต่างกันเฉพาะเวอร์ชัน (checkout dir) จะถูก mask ก่อนเทียบ และทั้งสองเวอร์ชันรันใน working dir เดียวกัน
4. **Budget**: `--tries` คือจำนวน input ต่อ property (Round 1 = 200, Round 2 = 1000)
   แต่ละ property ใช้เวลาได้ไม่เกิน `--prop-budget` วินาที และแต่ละ call ใช้ได้ไม่เกิน 2 วินาที

ข้อจำกัดที่ควรเขียนในรายงาน:
- method ที่ใช้ parameter เป็น File, Path, Socket, Thread หรือ ClassLoader ถูกข้ามเพื่อความปลอดภัย
- คลาสที่สร้าง instance ไม่ได้ (ไม่มี constructor หรือ factory ที่เข้าถึงได้ และไม่มี subclass) จะถูกข้าม instance method
  ตัวเลขทั้งหมดดูได้ใน `AUTO_GENERATED.json`
- oracle เป็นแบบ regression จึงวัด "ความสามารถในการกระตุ้นพฤติกรรมที่ต่าง" ไม่ได้วัด specification
  ซึ่งเป็นเงื่อนไขเดียวกับที่ใช้ประเมิน EvoSuite ใน repo นี้ (generate และ assert จากเวอร์ชัน fixed)

## วิธีรัน (ใน container)
```bash
bash scripts/run_jqwik_auto_all.sh                             # ทุก project, controls -> R1 -> R2 -> summary
SHARD=1/3 bash scripts/run_jqwik_auto_all.sh                   # แบ่งงาน 3 เครื่อง (สมาชิกคนที่ 1)
python3 scripts/run_jqwik.py -p Math -b 5 --auto --seeds 1     # บักเดียว
python3 scripts/summarize_jqwik.py --csv 'jqwik_auto*.csv'     # รวมผลทุก shard
```

## นิยาม "จับบักได้" (ใช้เหมือนกันทั้งสองโหมด)
ตัดสินทีละ property:
- property **ผ่านบน fixed** และ **fail บน buggy** ด้วย seed และ tries เดียวกัน -> detected
- fail เพราะ harness พัง (NoClassDefFoundError, CannotFindArbitraryException ฯลฯ) -> ไม่นับ
- property ที่ **fail บน fixed** -> false alarm, ไม่นับ
- รันหลาย seed: จับได้ทุก seed = `DETECTED`, จับได้บาง seed = `FLAKY` (รายงาน detection rate)
- Coverage: JaCoCo วัดตอนรันบน fixed เฉพาะ `classes.modified`

## กติกาการเขียน property เอง (โหมด hand-written)
1. เขียนจาก Javadoc หรือ spec ของคลาสใน `classes.modified` **ห้ามดู diff ของ fix และ trigger test**
2. ต้อง compile ได้ทั้ง buggy และ fixed
3. ห้ามใช้ `catch (Exception e) {}` กลืน exception ที่เป็นอาการของบัก
4. ห้ามกำหนด `@Property(seed=...)` เอง
5. property ที่ถูกตีเป็น false alarm ให้แก้ แล้วจดไว้ในรายงานว่าแก้กี่รอบ
