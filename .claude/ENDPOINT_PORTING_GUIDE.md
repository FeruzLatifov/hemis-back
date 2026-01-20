
# HEMIS-BACK - Endpoint Porting Guide

## ⚠️ MUHIM QOIDALAR

**1. Faqat user endpoint berganda ishlayman!**

**2. 🎯 AVVAL OLD-HEMIS DAN RESPONSE OLISH - KEYIN CONTROLLER YOZISH!**

   ```
   ┌─────────────────────────────────────────────────────────────┐
   │  OPTIMAL WORKFLOW (Solishtirish kamayadi!)                  │
   ├─────────────────────────────────────────────────────────────┤
   │  1️⃣ AVVAL: Old-hemis dan so'rov yuborish                    │
   │     curl http://localhost:8082/app/rest/v2/entities/...     │
   │     → Response formatini SAQLASH                            │
   │                                                              │
   │  2️⃣ KEYIN: Shu response ga MOS controller yozish            │
   │     - Aynan shu maydonlar                                   │
   │     - Aynan shu tartib                                      │
   │     - Aynan shu format                                      │
   │                                                              │
   │  3️⃣ TEST: Avtomatik 100% mos bo'ladi!                       │
   └─────────────────────────────────────────────────────────────┘
   ```

   **NOTO'G'RI:** Controller yozish → Test → Farq topish → Tuzatish → Qayta test (ko'p qadam!)
   **TO'G'RI:** Old-hemis response olish → Shu formatga mos controller → 100% mos!

**3. Serverlar ishlamasa test qilib bo'lmaydi:**
   - Old-hemis server: `http://localhost:8082` (port 8082)
   - Yangi hemis server: `http://localhost:8081` (port 8081)
   - Agar serverlar ishlamasa, endpoint_tester.html ga qo'sha olmayman!

**4. ⚠️ Old-hemis da ishlamagan endpointlarni PORT qilmaslik:**
   - Agar old-hemis HTTP 500, 404 yoki boshqa xato qaytarsa
   - Bu endpointni yangi hemis ga ko'chirish KERAK EMAS
   - Sabab: Ishlamagan endpointlarni migration qilishning ma'nosi yo'q
   - Test skripti avtomatik "PORT qilish kerak emas" deb xabar beradi

**5. 🔒 Swagger da haqiqiy ma'lumotlarni qo'ymaslik (SECURITY!):**
   - ❌ **QILMASLIK:** Real database ma'lumotlar (PINFL, UUID, username, password, etc.)
   - ✅ **QILISH:** Generic/fake qiymatlar ishlatish
   - Sabab: Swagger UI ochiq API documentation - security risk!
   - Misollar:
     - PINFL: `12345678901234` (real emas)
     - UUID: `00000000-0000-0000-0000-000000000000`
     - Username: `username` (real emas)
     - Passport: `AB1234567` (fake)
     - Date: `1990-01-01` (generic)

### 🔧 SERVER URL VA PORT KONFIGURATSIYA

| Tizim | Base URL | Port | Yo'l |
|-------|----------|------|------|
| **Old-Hemis (CUBA)** | `http://localhost:8082` | 8082 | `/home/adm1n/startup/old-hemis` |
| **Yangi Hemis (Spring Boot)** | `http://localhost:8081` | 8081 | `/home/adm1n/startup/hemis-back` |

### 📁 MUHIM FAYL YO'LLARI

| Fayl | To'liq yo'l | Tavsif |
|------|-------------|--------|
| **Old-Hemis API hujjati** | `/home/adm1n/startup/old_hemis.json` | Eski HEMIS API Swagger/OpenAPI hujjati |
| **Endpoint Tester** | `/home/adm1n/startup/hemis-back/docs/endpoint_tester.html` | Test interfeysi (3 tugmali) |

⚠️ **DIQQAT:**
- `old_hemis.json` - endpoint metadata olish uchun ishlatiladi (tag, description, parameters)
- `endpoint_tester.html` - test muvaffaqiyatli bo'lgandan keyin yangilanadi
- Boshqa joylardagi fayllar (masalan, `hemis-back/app/src/.../endpoint-tester.html`) **ESKIRGAN** - ularni yangilamaslik!

**Serverlarni ishga tushirish:**

```bash
# 1️⃣ Old-Hemis (port 8082)
cd /home/adm1n/startup/old-hemis
# Start old-hemis server...

# 2️⃣ Yangi Hemis (port 8081)
cd /home/adm1n/startup/hemis-back
./gradlew :app:bootRun
```

**Serverlarni tekshirish:**

```bash
# Old-hemis
curl http://localhost:8082/app/rest/v2/oauth/token -I

# Yangi hemis
curl http://localhost:8081/app/rest/v2/oauth/token -I
```

**⚠️ DIQQAT:** Agar port 8081 yoki 8082 band bo'lsa, serverlar ishga tushmaydi! Portlarni tekshirish:

```bash
netstat -tuln | grep ':808[12]'
# yoki
lsof -i :8081
lsof -i :8082
```

---

## ⚠️ CRITICAL: "Porting" vs "Migration"

### 1️⃣ ENDPOINT PORTING (BU FAYL)
**Nima:** Old-hemis dan REST endpoint ko'chirish
**Trigger:** `PORT: GET /services/tax/rent`
**Fayllar:** old_hemis.json, rest-services.xml
**Natija:** Controller.java + Swagger + Test
**So'z:** "Port", "Porting", "Ko'chirish"

### 2️⃣ DATABASE MIGRATION (LIQUIBASE)
**Nima:** Database schema o'zgartirish
**Trigger:** "database", "table", "column", "changeset"
**Fayllar:** `V*.sql`, `db.changelog-master.yaml`
**Natija:** SQL changeset + Rollback
**So'z:** "Migration", "Migrate", "Schema"

**JUDA HAR XIL ISHLAR!** Aralashtirib yubormaslik kerak! ⚠️

---

## 🔑 ENDPOINT PORTING TRIGGER (ANIQ BELGI)

Endpoint porting ishini boshlash uchun quyidagi formatlardan birini ishlating:

### ✅ Variant 1: PORT prefix (TAVSIYA ETILADI!)

```
PORT: GET /services/tax/rent
```

### ✅ Variant 2: Oddiy endpoint URL

```
GET /app/rest/v2/services/tax/rent
```

**Farqi:**
- `PORT:` bilan → **100% endpoint porting ishi**
- URL faqat → Men tekshiraman: agar `/services/*` yoki `/app/rest/*` bo'lsa → porting

### ✅ Variant 3: Bir nechta endpoint

```
PORT:
GET /services/bimm/disabilityCheck
GET /services/bimm/certificate
GET /services/bimm/academicDegree
```

---

## 📝 SIZ QANDAY YOZASIZ

### Format 1: Oddiy endpoint (eng tez)

```
GET /app/rest/v2/services/bimm/disabilityCheck
```

Yoki:

```
POST /app/rest/v2/services/student/create
```

**Bu kifoya!** Men avtomatik:
1. `old-hemis/rest-services.xml` dan topaman
2. Swagger tag yarataman (masalan: `03.BIMM Service API`)
3. Controller yozaman
4. endpoint_tester.html ga qo'shaman
5. Test qilaman

---

### Format 2: Swagger tag ko'rsatish (IXTIYORIY - kerak emas!)

**⚠️ TAG shart emas!** Men avtomatik `old_hemis.json` dan topaman.

Lekin agar o'zingiz ko'rsatmoqchi bo'lsangiz:

```
GET /app/rest/v2/services/student/get
TAG: 03.Student ma'lumotlari
```

Yoki:

```
ENDPOINT: GET /app/rest/v2/services/tax/rent
SWAGGER TAG: 04.Soliq
```

**Men qanday topaman:**
1. URL dan servisni ajrataman: `/services/tax/rent` → "tax"
2. `old_hemis.json` da izlayman → "Soliq" kategoriyasi topiladi
3. Tag nomini avtomatik yarataman: `04.Soliq`
4. Endpoint nomini olaman: "Ijara shartnomasi"
5. Description olaman (agar mavjud bo'lsa)

**Shuning uchun odatda TAG kiritmasangiz ham bo'ladi!**

---

### Format 3: Yangi endpoint (TAG va DESCRIPTION bilan)

Agar endpoint old-hemis da yo'q bo'lsa yoki yangi xizmat yaratmoqchi bo'lsangiz:

```
GET /services/attendance/test
TAG: 09.Davomat
DESCRIPTION: Talabalar davomati uchun test xizmati
```

Yoki to'liq format:

```
ENDPOINT: POST /services/myservice/create
TAG: 25.Mening Xizmatim
DESCRIPTION: Yangi yozuv yaratish uchun xizmat
PARAMS: name, description, status
```

---

### Format 4: Bir nechta endpoint (list)

```
GET /app/rest/v2/services/bimm/disabilityCheck
GET /app/rest/v2/services/bimm/certificate
GET /app/rest/v2/services/bimm/academicDegree
```

Men hammасini ketma-ket bajaraman!

---

## ✅ NAMUNALAR (Copy-Paste qiling)

### ✅ Eng sodda (TAG siz - TAVSIYA ETILADI!)
```
GET /app/rest/v2/services/social/singleRegister
```
Men avtomatik `old_hemis.json` dan topaman:
- Tag: "Ijtimoiy himoya"
- Endpoint nomi: "Yagona ro'yxat"

---

### ✅ TAG bilan (ixtiyoriy)
```
GET /app/rest/v2/services/tax/rent
TAG: 04.Soliq
```
Siz TAG ko'rsatsangiz, men uni ishlataman.

---

### ✅ POST endpoint
```
POST /app/rest/v2/services/student/create
```

---

### ✅ Yangi endpoint yaratish
```
GET /services/attendance/check
TAG: 09.Davomat
DESCRIPTION: Talaba davomatini tekshirish
```

### ✅ Bir nechta endpoint (batch)
```
GET /app/rest/v2/services/otm/studentListByTutor
GET /app/rest/v2/services/otm/studentInfoById
GET /app/rest/v2/services/otm/studentInfoByPinfl
```
Hammasi uchun avtomatik tag topaman!

---

## ❌ NOTO'G'RI FORMATLAR

```
❌ student/get ni migratsiya qil
❌ BIMM endpointlarni yoz
❌ rest-services.xml dan ko'chir
❌ yangi controller yarat
```

**Bu noto'g'ri!** Menga aniq endpoint URL kerak!

---

## 🚫 BU API MIGRATION EMAS (Aralashmaslik!)

Quyidagi holatlar **API MIGRATION EMAS**:

### ❌ Endpoint Porting emas:
```
❌ "DiplomaController ni kod review qil"          → Oddiy development
❌ "Swagger dokumentatsiyani yaxshila"            → Oddiy development
❌ "Test coverage ni ko'tar"                      → Oddiy development
❌ "Bug fix qil"                                  → Oddiy development
❌ "Refactoring qil"                              → Oddiy development
❌ "CLAUDE.md ni yangilang"                       → Oddiy development
```

### ❌ Bu LIQUIBASE migration (API migration emas!):
```
❌ "diploma jadvalida qr_code ustuni qo'sh"      → LIQUIBASE (@LIQUIBASE_GUIDE.md)
❌ "Student table ga phone_number column qo'sh"  → LIQUIBASE
❌ "CREATE TABLE yangi_jadval"                   → LIQUIBASE
❌ "Database schema ni o'zgartir"                → LIQUIBASE
❌ "Liquibase changeset yarat"                   → LIQUIBASE
```

### ✅ Endpoint Porting:
```
✅ "PORT: GET /services/tax/rent"
✅ "GET /app/rest/v2/services/bimm/certificate"
✅ "PORT: GET /services/social/singleRegister"
```

**Qoida:**
- Agar `PORT:` + URL yoki `/services/*` pattern → **Endpoint Porting** (BU FAYL)
- Agar "table", "column", "schema", "changeset" → **Liquibase Migration** (@LIQUIBASE_GUIDE.md)
- Aks holda → **Oddiy development**

**Shunda hech narsa aralashmaydi!** ✅

---

## 🤖 MEN NIMA QILAMAN (avtomatik)

Siz endpoint yozganingizdan keyin:

### 1️⃣ TEKSHIRISH (Duplicate oldini olish)

✅ Mavjud controllerlarni tekshiraman:
   - Agar endpoint allaqachon migrate qilingan bo'lsa → ⚠️ "Bu endpoint allaqachon mavjud!" deb xabar beraman
   - Agar yo'q bo'lsa → ✅ davom etaman

### 2️⃣ 🎯 AVVAL: OLD-HEMIS DAN RESPONSE OLISH (KRITIK!)

**Bu qadam eng muhim! Controller yozishdan OLDIN bajariladi!**

```bash
# Token olish
OLD_TOKEN=$(curl -s -X POST "http://localhost:8082/app/rest/v2/oauth/token" \
  -u "client:secret" \
  -d "grant_type=password&username=otm351&password=XCZDAb7qvGTXxz" \
  | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

# Response olish va saqlash
curl -s "http://localhost:8082/app/rest/v2/entities/hemishe_XXX/entityId" \
  -H "Authorization: Bearer $OLD_TOKEN" \
  | python3 -m json.tool > /tmp/old_response.json
```

✅ Response formatini TAHLIL qilaman:
   - Qaysi maydonlar bor?
   - Qaysi tartibda?
   - Nested objectlar bormi?
   - Audit fields (createTs, updateTs) bormi?

⚠️ AGAR OLD-HEMIS XATO QAYTARSA → PORT qilmaslik!

### 3️⃣ METADATA EXTRACTION

✅ `old_hemis.json` dan qidiramam:
   - Tag nomini (masalan: "Soliq", "Passport ma'lumotlari")
   - Endpoint o'zbek nomini (masalan: "Ijara shartnomasi")
   - Description/izohni

### 4️⃣ CONTROLLER GENERATION (OLD-HEMIS RESPONSE GA MOS!)

✅ `api-legacy` ga controller yarataman
✅ **toMap() metodini OLD-HEMIS formatiga 100% moslashtirib yozaman:**
   - Faqat old-hemis qaytargan maydonlarni qaytarish
   - Aynan shu tartibda (LinkedHashMap)
   - Qo'shimcha maydonlar QO'SHMASLIK!

```java
// ✅ TO'G'RI: Faqat old-hemis qaytargan maydonlar
private Map<String, Object> toMap(Entity entity) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("_entityName", ENTITY_NAME);
    map.put("_instanceName", entity.getName());
    map.put("id", entity.getCode());
    map.put("code", entity.getCode());
    map.put("version", entity.getVersion());
    map.put("nameUz", entity.getNameUz());
    map.put("nameRu", entity.getNameRu());
    map.put("status", entity.getStatus());
    // ❌ university, deparmentType, createTs, etc. - OLD-HEMIS QAYTARMAYDI!
    return map;
}
```

✅ Swagger qo'shaman (o'zbek tilida)

### 5️⃣ TEST (Avtomatik 100% mos bo'lishi kerak!)

**Agar 2️⃣-4️⃣ qadamlar to'g'ri bajarilgan bo'lsa, test avtomatik o'tadi!**

```bash
curl -s "http://localhost:8081/app/rest/v2/entities/..." \
  -H "Authorization: Bearer $NEW_TOKEN" > /tmp/new_response.json

diff /tmp/old_response.json /tmp/new_response.json
# ✅ Farq yo'q = 100% mos!
```

3. **Agar farq bor:**
   - ⚠️ 4️⃣ ga qaytib toMap() ni to'g'rilash kerak
   - Response formatini tekshirish (wrapper yo'qmi?)
     - Field nomlarini tekshirish
     - Field typelarini tekshirish
     - Ma'lumot strukturasini tekshirish
     - Controllerni to'g'rilash va qayta test qilish

4. **Tuzatish kerak bo'lsa:**
   - Old-hemis qanday format qaytarsa, yangi hemis ham xuddi shunday qaytarishi kerak
   - Agar old-hemis wrapper (`{success, data}`) ishlatsa → yangi hemis ham ishlat
   - Agar old-hemis to'g'ridan-to'g'ri obyekt qaytarsa → yangi hemis ham to'g'ridan-to'g'ri qaytarsin
   - Field nomlari va typelari bir xil bo'lishi shart

5. **Faqat javoblar bir xil bo'lgandan keyin:**
   ✅ `endpoint_tester.html` ga 3 ta tugma qo'shaman
   ✅ Default test ma'lumotlarni bazadan olaman
   ✅ Migration hisobot beraman

**DIQQAT:** Agar test muvaffaqiyatsiz bo'lsa yoki javoblar farq qilsa, endpoint_tester.html ga qo'shmaslik kerak! Avval tuzatish kerak!

**Hammasi avtomatik!** Siz faqat endpoint URL yozing.

---

## 📋 TURLI HOLATLAR

### ✅ HOLAT 1: old_hemis.json DA BOR

```
GET /services/tax/rent
```

Men qilaman:
1. ✅ old_hemis.json dan topaman → Tag: "Soliq", Nom: "Ijara shartnomasi"
2. ✅ rest-services.xml dan parametrlar
3. ✅ Controller yarataman

**Hammasi avtomatik!** ✅

---

### ✅ HOLAT 2: old_hemis.json DA YO'Q, LEKIN rest-services.xml DA BOR

```
GET /services/attendance/test
```

Men qilaman:
1. ⚠️ old_hemis.json da yo'q
2. ✅ rest-services.xml da topaman
3. ✅ old-hemis controller kodidan izohlarni olaman
4. ❓ **Tag nima?**

**A) Agar siz TAG ko'rsatgan bo'lsangiz:**
```
GET /services/attendance/test
TAG: 09.Davomat
```
→ Sizning tagingizdagi yarataman: `09.Davomat`

**B) Agar TAG yo'q bo'lsa:**
→ URL dan taxmin qilaman: `/services/attendance/*` → `09.Attendance`
→ Yoki sizdan so'rayman: "Qaysi tag ostida yaratishni xohlaysiz?"

---

### ✅ HOLAT 3: old_hemis.json DA YO'Q, rest-services.xml DA HAM YO'Q

```
GET /services/newfeature/getData
```

Men sizga aytaman:
```
❌ Bu endpoint old-hemis da topilmadi!

📋 Mavjud endpointlar (rest-services.xml):
- /services/bimm/disabilityCheck
- /services/social/singleRegister
- /services/tax/rent
- ...

💡 Agar yangi endpoint yaratmoqchi bo'lsangiz, TAG ko'rsating:

GET /services/newfeature/getData
TAG: 10.Yangi Xizmat
DESCRIPTION: Yangi xizmat uchun ma'lumot olish
```

---

### ✅ HOLAT 4: YANGI TAG YARATISH

Agar yangi kategoriya yaratmoqchi bo'lsangiz:

```
GET /services/myservice/getData
TAG: 25.Mening Xizmatim
DESCRIPTION: Mening yangi xizmatim uchun ma'lumot olish
```

Men qilaman:
1. ✅ Yangi tag yarataman: `25.Mening Xizmatim`
2. ✅ Controller yarataman
3. ✅ Swagger qo'shaman (sizning description)
4. ✅ endpoint_tester.html ga yangi kategoriya qo'shaman

---

### 🎯 QISQASI

| Holat | old_hemis.json | rest-services.xml | Siz TAG berish | Men nima qilaman |
|-------|----------------|-------------------|----------------|------------------|
| 1 | ✅ Bor | ✅ Bor | ❌ Yo'q | Avtomatik tag topaman |
| 2 | ❌ Yo'q | ✅ Bor | ✅ Ha | Sizning tagingizdagi yarataman |
| 2 | ❌ Yo'q | ✅ Bor | ❌ Yo'q | URL dan taxmin qilaman yoki so'rayman |
| 3 | ❌ Yo'q | ❌ Yo'q | - | Mavjud endpointlar ko'rsataman |
| 4 | - | - | ✅ Ha | Yangi tag yarataman |

---

## 🎯 QISQASI

**SIZ (faqat URL yozing):**
```
GET /app/rest/v2/services/bimm/disabilityCheck
```

**MEN (hammasi avtomatik):**

1. ✅ **Tekshirish:**
   - Bu endpoint allaqachon migratsiya qilinganmi?
   - Yo'q bo'lsa → davom etaman

2. ✅ **old_hemis.json dan ma'lumot olaman:**
   - Tag: "BIMM"
   - Nom: "Nogironlik tekshiruvi"
   - Description: "..."

3. ✅ **rest-services.xml dan parametrlar:**
   - pinfl, document

4. ✅ **Controller yarataman:**
   - Swagger o'zbek tilida
   - Best practice kodlar

5. ✅ **TEST & SOLISHTIRISH (MAJBURIY!):**
   - Old-hemis dan test: `curl http://localhost:8082/app/rest/v2/services/bimm/disabilityCheck?pinfl=...`
   - Yangi hemis dan test: `curl http://localhost:8081/app/rest/v2/services/bimm/disabilityCheck?pinfl=...`
   - Javoblarni solishtirish:
     - ✅ 100% bir xil → davom etaman
     - ⚠️ Farq bor → controllerni tuzataman va qayta test qilaman
     - ❌ Farq hal qilinmasa → endpoint_tester.html ga qo'shmayman!

6. ✅ **endpoint_tester.html ga qo'shaman (faqat testlar muvaffaqiyatli bo'lsa):**
   - 🆕 Yangi Hemis tugmasi
   - 🏛️ Old Hemis tugmasi
   - Ikkalasini Ham Test tugmasi

7. ✅ **Hisobot:**
   - Migration holati
   - Test natijasi (old vs new comparison)
   - endpoint_tester.html dagi yangi endpoint ID

**HAMMASI!** 🎉

**⚠️ MUHIM:** Agar old-hemis (`http://localhost:8082`) yoki yangi hemis (`http://localhost:8081`) serverlari ishlamasa, test qilolmayman va endpoint_tester.html ga qo'sha olmayman!

---

## 💡 TAGNI MAN KIRITISHIM KERAKMI?

**YO'Q, KERAK EMAS!** ✅

Men avtomatik `old_hemis.json` dan topaman:

```
GET /app/rest/v2/services/tax/rent
```

Men topaman:
- Tag: "Soliq" → `04.Soliq` (swagger tag)
- Nom: "Ijara shartnomasi"

Lekin agar siz TAG ko'rsatsangiz, uni ishlataman:
```
GET /app/rest/v2/services/tax/rent
TAG: 04.Soliq xizmati
```

**⚡ ENG YAXSHI:** TAG siz yozing, men o'zim topaman!

## 📊 URL → TAG MAPPING (Avtomatik)

Men quyidagi jadvaldan foydalanaman:

| URL Pattern | Tag Nomi | Swagger Tag |
|-------------|----------|-------------|
| `/services/bimm/*` | BIMM | `03.BIMM` |
| `/services/passport-data/*` | Passport ma'lumotlari | `02.Passport ma'lumotlari` |
| `/services/personal-data/*` | Passport ma'lumotlari | `02.Passport ma'lumotlari` |
| `/services/tax/*` | Soliq | `04.Soliq` |
| `/services/social/*` | Ijtimoiy himoya | `05.Ijtimoiy himoya` |
| `/services/student/*` | Talaba | `06.Talaba` |
| `/services/teacher/*` | O'qituvchi | `07.O'qituvchi` |
| `/services/scholarship/*` | Stipendiya | `08.Stipendiya` |
| `/services/billing/*` | Billing | `09.Billing` |
| `/services/captcha/*` | Captcha | `10.Captcha` |
| `/services/university/*` | OTM | `11.OTM` |
| `/services/group/*` | Guruhlar | `12.Guruhlar` |
| `/services/speciality/*` | Mutaxassisliklar | `13.Mutaxassisliklar` |
| `/services/faculty/*` | Fakultetlar | `14.Fakultetlar` |
| `/services/diploma/*` | Diplomlar | `15.Diplomlar` |
| `/services/transcript/*` | Transkript | `16.Transkript` |
| `/services/classifiers/*` | Klassifikatorlar | `17.Klassifikatorlar` |
| `/services/translate/*` | Tarjima | `18.Tarjima` |
| `/services/mail/*` | Mail | `19.Mail` |
| `/services/contract/*` | Contract | `20.Contract` |
| `/services/employment/*` | Bandlik statistikasi | `21.Bandlik statistikasi` |
| `/services/mandat/*` | DTM | `22.DTM` |
| `/services/oak/*` | OAK | `23.OAK` |
| `/services/uzasbo/*` | UzASBO | `24.UzASBO` |
| `/oauth/token/*` | Token | `01.Token` |

**Misol:**
- Siz: `GET /services/tax/rent`
- Men: URL dan `/services/tax/*` ni topaman → Tag: `04.Soliq`
- old_hemis.json dan: Endpoint nomi "Ijara shartnomasi"

---

## CLAUDE CODE AVTOMATIK BAJARADI

1. ✅ Old-hemis dan endpoint topish
2. ✅ api-legacy ga yozish  
3. ✅ Swagger qo'shish (o'zbek tilida)
4. ✅ endpoint_tester.html ga IKKI test qo'shish (old + new)
5. ✅ .env bazadan default qiymatlar
6. ✅ Test va solishtirish
7. ✅ Migration hisobot

---

## ENDPOINT_TESTER.HTML STRUKTURA ✅ IMPLEMENTED

### ✅ Dual Config Panel (Yangi vs Eski)

Har bir tizim uchun alohida konfiguratsiya:
- **🆕 Yangi Hemis-Back**: `http://localhost:8081` (yashil)
- **🏛️ Old-Hemis CUBA**: `http://localhost:8081/app` (qizil)

### ✅ Har Bir Endpoint - 3 Ta Tugma

```html
<button onclick="testSingle('new', id)">🆕 Yangi Hemis</button>
<button onclick="testSingle('old', id)">🏛️ Old Hemis</button>
<button onclick="testBoth(id)">Ikkalasini Ham Test</button>
```

### ✅ Side-by-Side Response Display

```
┌─────────────────────────┬─────────────────────────┐
│ 🆕 Yangi Hemis Response │ 🏛️ Old Hemis Response   │
│ (Yashil border)         │ (Qizil border)          │
├─────────────────────────┼─────────────────────────┤
│ { ... JSON ... }        │ { ... JSON ... }        │
└─────────────────────────┴─────────────────────────┘
         ✅ Javoblar 100% bir xil!
         OR
         ⚠️ Javoblarda farq bor
```

### ✅ CSS Ranglari (old_hemis.md mos)

```css
/* Yangi Hemis - Yashil */
.new-hemis {
    background: #f0fff4;
    border: 2px solid #51cf66;
    color: #2b8a3e;
}

/* Old Hemis - Qizil */
.old-hemis {
    background: #fff5f5;
    border: 2px solid #ff6b6b;
    color: #c92a2a;
}
```

### ✅ Progress Bars

- Yangi Hemis: Yashil `#51cf66` → Qora-yashil `#2b8a3e` (success) yoki Qizil `#c92a2a` (error)
- Old Hemis: Qizil `#ff6b6b` → Qora-yashil `#2b8a3e` (success) yoki Qizil `#c92a2a` (error)

### 📦 Default Collapse Holati (UI Qoidasi)

**Barcha kategoriyalar default holatda YOPIQ (collapsed) bo'lishi kerak:**

- 01.Token, 02.Captcha, 03.BIMM va boshqa kategoriyalar sahifa yuklanganda yopiq turadi
- Foydalanuvchi kerakli kategoriyani bosib ochadi va ichidagi endpointlarni test qiladi
- Bu UI/UX yaxshilash - foydalanuvchi ko'p kategoriyalar orasidan keraklisini tezroq topadi

**JavaScript kodida kategoriya yaratilganda `collapsed` class avtomatik qo'shiladi:**

```javascript
// Toggle ikonasi - yopiq holatda (strelka o'ngga qaragan)
toggle.className = 'fas fa-chevron-down category-toggle collapsed';

// Kategoriya kontenti - yopiq holatda (yashirin)
row.className = 'row category-content collapsed';
```

**Yangi kategoriya qo'shganda** bu qoida avtomatik ishlaydi - JavaScript kodi barcha kategoriyalarga `collapsed` class qo'shadi.

---

## TEST MA'LUMOTLARI (.env bazadan)

```sql
-- Student PINFL
SELECT pinfl FROM hemishe_e_student WHERE pinfl IS NOT NULL LIMIT 1;

-- University
SELECT code FROM hemishe_e_university LIMIT 1;

-- Teacher
SELECT employee_id_number FROM hemishe_e_employee WHERE employee_id_number IS NOT NULL LIMIT 1;
```

**JavaScript ga qo'shish:**
```javascript
const defaults = {
    pinfl: '12345678901234',      // bazadan
    university: 'TATU',            // bazadan
    teacher: '98765432109876'      // bazadan
};
```

---

## SWAGGER FORMAT ✅ STANDARDLASHTIRILDI

### Tag Nomlari (QATTIQ QOIDA)

**Raqamli taglar (Legacy API):**
- `01.Token` - OAuth2 token endpoints
- `02.Passport ma'lumotlari` - GUVD passport ma'lumotlari (PROBELSIZ!)

**Raqamsiz taglar (Boshqa API):**
- `Legacy Entity APIs - Student`
- `Modern Web APIs - Diplomas`
- `External Integration APIs - BIMM`
- `Public APIs - Captcha`

### Controller Namuna

```java
@Tag(
    name = "02.Passport ma'lumotlari",  // PROBELSIZ!
    description = "GUVD passport ma'lumotlarini olish va tekshirish xizmatlari"
)
@Operation(
    summary = "PINFL bo'yicha passport ma'lumoti",
    description = """
        PINFL va passport seria-raqam orqali GUVD bazasidan ma'lumot olish.

        **OLD-HEMIS Compatible** - 100% backward compatibility

        **Endpoint:** GET /services/passport-data/getData
        **Auth:** Bearer token (required)
        """,
    security = @SecurityRequirement(name = "bearerAuth")
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
    @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
    @ApiResponse(responseCode = "404", description = "Passport topilmadi")
})
```

---

## 🔄 MASTER/REPLICA DATABASE ROUTING (MAJBURIY!)

### ⚠️ QOIDA: Har bir controller metodiga `@Transactional` qo'yish SHART!

Tizim Master/Replica database arxitekturasidan foydalanadi:
- **Master database** → WRITE operatsiyalar (INSERT, UPDATE, DELETE)
- **Replica database** → READ operatsiyalar (SELECT)

### 📋 Routing Qoidalari

| HTTP Method | Operatsiya | Annotatsiya | Database |
|-------------|------------|-------------|----------|
| **GET** | READ | `@Transactional(readOnly = true)` | Replica |
| **POST** (read) | READ (search) | `@Transactional(readOnly = true)` | Replica |
| **POST** (create) | WRITE | `@Transactional` | Master |
| **PUT** | WRITE | `@Transactional` | Master |
| **PATCH** | WRITE | `@Transactional` | Master |
| **DELETE** | WRITE | `@Transactional` | Master |

### ✅ TO'G'RI Namuna (Entity Controller)

```java
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EStudent")
public class StudentEntityController {

    // ✅ GET - READ operatsiya → Replica database
    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID entityId) {
        // ...
    }

    // ✅ GET (list) - READ operatsiya → Replica database
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        // ...
    }

    // ✅ POST (search) - READ operatsiya → Replica database
    @PostMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> search(@RequestBody Map<String, Object> filter) {
        // ...
    }

    // ✅ POST (create) - WRITE operatsiya → Master database
    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        // ...
    }

    // ✅ PUT - WRITE operatsiya → Master database
    @PutMapping("/{entityId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body) {
        // ...
    }

    // ✅ DELETE - WRITE operatsiya → Master database
    @DeleteMapping("/{entityId}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        // ...
    }
}
```

### ✅ TO'G'RI Namuna (Service Controller)

```java
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/app/rest/v2/services/student")
public class StudentServiceController {

    // ✅ GET - READ operatsiya → Replica database
    @GetMapping("/verify")
    @Transactional(readOnly = true)
    public ResponseEntity<?> verify(@RequestParam String pinfl) {
        // ...
    }

    // ✅ GET - READ operatsiya → Replica database
    @GetMapping("/contractInfo")
    @Transactional(readOnly = true)
    public ResponseEntity<?> contractInfo(@RequestParam String pinfl) {
        // ...
    }

    // ✅ POST (yaratish/yangilash) - WRITE operatsiya → Master database
    @PostMapping("/id")
    @Transactional
    public ResponseEntity<?> getOrCreateId(@RequestBody Map<String, Object> request) {
        // ...
    }

    // ✅ POST (update) - WRITE operatsiya → Master database
    @PostMapping("/update")
    @Transactional
    public ResponseEntity<?> update(@RequestBody Map<String, Object> request) {
        // ...
    }
}
```

### ❌ NOTO'G'RI Namunalar

```java
// ❌ NOTO'G'RI - @Transactional yo'q!
@GetMapping("/{entityId}")
public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID entityId) {
    // Routing ishlamaydi - default Master ga ketadi (keraksiz yuk!)
}

// ❌ NOTO'G'RI - GET uchun readOnly=true yo'q!
@GetMapping("/{entityId}")
@Transactional  // <- readOnly yo'q!
public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID entityId) {
    // Master ga ketadi - Replica ishlatilmaydi!
}

// ❌ NOTO'G'RI - POST (create) uchun readOnly=true qo'yilgan!
@PostMapping
@Transactional(readOnly = true)  // <- XATO! Write qilolmaydi!
public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
    // SQLException: Read-only transaction!
}
```

### 📊 Xulosa Jadvali

| Metod turi | Annotatsiya | Noto'g'ri bo'lsa |
|------------|-------------|------------------|
| `getById()`, `getAll()`, `search()` | `@Transactional(readOnly = true)` | Master ga keraksiz yuk |
| `create()`, `update()`, `delete()` | `@Transactional` | Read-only error yoki routing xatosi |

### ⚠️ Import qilishni unutmang!

```java
import org.springframework.transaction.annotation.Transactional;
```

---

### ⚠️ XATO NAMUNALAR

```java
// ❌ NOTO'G'RI - probel bor
@Tag(name = "02. Passport ma'lumotlari")

// ❌ NOTO'G'RI - legacy API da raqam yo'q
@Tag(name = "Passport ma'lumotlari")

// ❌ NOTO'G'RI - modern API da raqam bor
@Tag(name = "01. Legacy Entity APIs - Student")

// ✅ TO'G'RI
@Tag(name = "02.Passport ma'lumotlari")          // Legacy API
@Tag(name = "Legacy Entity APIs - Student")      // Modern API
```

---

## RESPONSE FORMAT QOIDALARI

**CRITICAL:** Old-hemis qaysi formatda qaytarsa, yangi hemis ham xuddi shunday!

- Format A: To'g'ridan-to'g'ri obyekt (wrapper yo'q)
- Format B: CUBA wrapper `{success, data, error}`

### ⚠️ JSON FIELD KETMA-KETLIGI (ORDERING) - MUHIM!

**QOIDA:** Old-hemis qaysi tartibda field qaytarsa, yangi hemis ham xuddi shu tartibda!

**Sabab:** Ba'zi klientlar JSON field ketma-ketligiga bog'liq bo'lishi mumkin. 100% backward compatibility uchun ketma-ketlik ham bir xil bo'lishi kerak.

**Jackson `@JsonPropertyOrder` annotatsiyasi:**

```java
// ❌ NOTO'G'RI - Ketma-ketlik aniqlanmagan
@Data
public class StudentLegacyDto {
    private UUID id;
    private String code;
    private String pinfl;
    // ... Jackson alifbo tartibida yoki random tartibda chiqaradi
}

// ✅ TO'G'RI - Old-hemis ketma-ketligi aniq belgilangan
@Data
@JsonPropertyOrder({
    "_entityName", "id", "isGraduate", "country", "educationType", "groupId",
    "language", "socialCategory", "educationYear", "educationForm", "faculty",
    // ... old-hemis tartibida davom etadi
})
public class StudentLegacyDto {
    @JsonProperty("_entityName")
    private String entityName = "hemishe_EStudent";
    private UUID id;
    // ...
}
```

**Ketma-ketlikni aniqlash:**

```python
# Old-hemis response dan field ketma-ketligini olish
import json
import collections

with open('old_response.json') as f:
    data = json.loads(f.read(), object_pairs_hook=collections.OrderedDict)
    print('Field order:', list(data.keys()))
```

**Ichki obyektlar:**

Har bir ichki DTO ham o'z `@JsonPropertyOrder` ga ega bo'lishi kerak:

```java
@Data
@JsonPropertyOrder({"_entityName", "id", "studentUrl", "code", "universityType", ...})
public static class UniversityReferenceDto { ... }

@Data
@JsonPropertyOrder({"_entityName", "id", "nameRu", "code", "name", "active", ...})
public static class SimpleReferenceDto { ... }
```

**LinkedHashMap ishlatish (Service layer):**

Response Map qaytarganda `LinkedHashMap` ishlatish:

```java
// ❌ NOTO'G'RI - HashMap ketma-ketlikni saqlamaydi
Map<String, Object> result = new HashMap<>();
result.put("success", true);
result.put("message", "...");
result.put("student", dto);

// ✅ TO'G'RI - LinkedHashMap ketma-ketlikni saqlaydi
Map<String, Object> result = new LinkedHashMap<>();
result.put("success", true);
result.put("message", "...");
result.put("student", dto);
```

### ⚠️ RESPONSE FORMAT TEST QOIDALARI

**PORT qilishdan OLDIN:**

1. **Old-hemis dan asl javobni olish:**
   ```bash
   curl http://localhost:8082/app/rest/v2/services/.../endpoint \
     -H "Authorization: Bearer TOKEN" \
     | jq '.' > old_response.json
   ```

2. **Controller yozish:**
   - Agar old-hemis `{success, data, error}` wrapper qaytarsa → yangi hemis ham wrapper qaytar
   - Agar old-hemis to'g'ridan-to'g'ri obyekt qaytarsa → yangi hemis ham to'g'ridan-to'g'ri qaytar

3. **Yangi hemis dan test qilish:**
   ```bash
   curl http://localhost:8081/app/rest/v2/services/.../endpoint \
     -H "Authorization: Bearer TOKEN" \
     | jq '.' > new_response.json
   ```

4. **Javoblarni solishtirish:**
   ```bash
   diff old_response.json new_response.json
   ```
   - ✅ Farq yo'q → PORT muvaffaqiyatli!
   - ⚠️ Farq bor → controllerni tuzatish va qayta test qilish kerak

5. **endpoint_tester.html ga qo'shish:**
   - Faqat javoblar 100% bir xil bo'lgandan keyin!

---

## 🔒 UNIVERSITY FILTERING (SECURITY IMPROVEMENT)

### ⚠️ MUHIM: OLD-HEMIS vs NEW-HEMIS Security Farqi

**OLD-HEMIS:**
- Entity REST API (`/app/rest/v2/entities/*`) → **FILTER YO'Q!**
- Service endpoints (`/services/teacher/id`, `/services/student/id`) → Filter BOR (service layer)
- CUBA Row-Level Security → **KONFIGURATSIYA QILINMAGAN**
- Natija: Bir OTM foydalanuvchisi boshqa OTM ma'lumotlarini ko'rishi mumkin edi!

**NEW-HEMIS:**
- Entity REST API → **UNIVERSITY FILTER QO'SHILGAN** (security improvement)
- Service endpoints → Filter BOR (xuddi old-hemis kabi)
- Natija: Har bir foydalanuvchi faqat o'z OTM ma'lumotlarini ko'radi

### 📋 Qachon University Filter Kerak?

| Endpoint Turi | Filter Kerak? | Sabab |
|---------------|---------------|-------|
| **Entity (user-specific data)** | ✅ HA | EEmployeeJobs, EStudent, ETeacher - OTM ga tegishli |
| **Classifier (global data)** | ❌ YO'Q | HTeacherPositionType, HUniversityEmployeeRate - barcha OTM uchun bir xil |
| **Service (id/create)** | ✅ HA | teacher/id, student/id - yangi yozuv yaratadi |
| **Service (read-only)** | ⚠️ TEKSHIRISH | Ma'lumot turiga qarab |

### ✅ Filter Kerak Bo'lgan Entity Endpointlar

```java
// hemishe_EEmployeeJobs - Xodim ish joylari
GET    /app/rest/v2/entities/hemishe_EEmployeeJobs           // ✅ Filter
GET    /app/rest/v2/entities/hemishe_EEmployeeJobs/{id}      // ✅ Filter
POST   /app/rest/v2/entities/hemishe_EEmployeeJobs           // ✅ Filter + Set university
PUT    /app/rest/v2/entities/hemishe_EEmployeeJobs/{id}      // ✅ Filter
DELETE /app/rest/v2/entities/hemishe_EEmployeeJobs/{id}      // ✅ Filter

// hemishe_EStudent - Talabalar
GET    /app/rest/v2/entities/hemishe_EStudent                // ✅ Filter
GET    /app/rest/v2/entities/hemishe_EStudent/{id}           // ✅ Filter
POST   /app/rest/v2/entities/hemishe_EStudent                // ✅ Filter + Set university
PUT    /app/rest/v2/entities/hemishe_EStudent/{id}           // ✅ Filter
DELETE /app/rest/v2/entities/hemishe_EStudent/{id}           // ✅ Filter

// hemishe_ETeacher - O'qituvchilar
GET    /app/rest/v2/entities/hemishe_ETeacher                // ✅ Filter
GET    /app/rest/v2/entities/hemishe_ETeacher/{id}           // ✅ Filter
// ... va hokazo
```

### ❌ Filter Kerak EMAS - Classifier Endpointlar

```java
// Lavozimlari klassifikatorlari - BARCHA OTM UCHUN BIR XIL
GET /app/rest/v2/entities/hemishe_HTeacherPositionType       // ❌ Filter yo'q
GET /app/rest/v2/entities/hemishe_HTeacherPositionType/{id}  // ❌ Filter yo'q

GET /app/rest/v2/entities/hemishe_HUniversityEmployeeRate    // ❌ Filter yo'q
GET /app/rest/v2/entities/hemishe_HUniversityEmployeeRate/{id}

GET /app/rest/v2/entities/hemishe_HUniversityEmployeeType    // ❌ Filter yo'q
GET /app/rest/v2/entities/hemishe_HUniversityEmployeeType/{id}

GET /app/rest/v2/entities/hemishe_HUniversityEmployeeStatusType // ❌ Filter yo'q
GET /app/rest/v2/entities/hemishe_HUniversityEmployeeStatusType/{id}

// Boshqa klassifikatorlar
GET /app/rest/v2/entities/hemishe_HGender                    // ❌ Filter yo'q
GET /app/rest/v2/entities/hemishe_HAcademicDegree            // ❌ Filter yo'q
GET /app/rest/v2/entities/hemishe_HAcademicRank              // ❌ Filter yo'q
// ... va hokazo
```

### 🔧 University Filter Implementatsiyasi

```java
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EEmployeeJobs")
@RequiredArgsConstructor
public class EmployeeJobsEntityController {

    private final EmployeeJobsRepository repository;
    private final AuthenticationFacade authFacade;  // ← JWT dan user olish

    @GetMapping
    @Transactional(readOnly = true)  // ← Replica database
    public ResponseEntity<List<Map<String, Object>>> getAll(...) {
        // 1️⃣ JWT dan user's university olish
        String universityCode = authFacade.getCurrentUser().getUniversity().getCode();

        // 2️⃣ Faqat shu OTM ma'lumotlarini qaytarish
        Page<EmployeeJobs> page = repository.findByUniversityCode(universityCode, pageable);

        return ResponseEntity.ok(toMapList(page.getContent()));
    }

    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID entityId) {
        String universityCode = authFacade.getCurrentUser().getUniversity().getCode();

        // ✅ Filter: faqat o'z OTM ning yozuvini qaytarish
        Optional<EmployeeJobs> entity = repository.findByIdAndUniversityCode(entityId, universityCode);

        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();  // 404 - boshqa OTM yoki yo'q
        }
        return ResponseEntity.ok(toMap(entity.get()));
    }

    @PostMapping
    @Transactional  // ← Master database
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        String universityCode = authFacade.getCurrentUser().getUniversity().getCode();

        EmployeeJobs entity = new EmployeeJobs();
        // ... map body to entity

        // ✅ Avtomatik university set qilish
        entity.setUniversity(universityCode);

        entity = repository.save(entity);
        return ResponseEntity.ok(toMap(entity));
    }

    @PutMapping("/{entityId}")
    @Transactional  // ← Master database
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body) {
        String universityCode = authFacade.getCurrentUser().getUniversity().getCode();

        // ✅ Filter: faqat o'z OTM ning yozuvini yangilash
        Optional<EmployeeJobs> existing = repository.findByIdAndUniversityCode(entityId, universityCode);

        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();  // 404 - boshqa OTM yoki yo'q
        }

        // ... update entity
        return ResponseEntity.ok(toMap(updated));
    }

    @DeleteMapping("/{entityId}")
    @Transactional  // ← Master database
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID entityId) {
        String universityCode = authFacade.getCurrentUser().getUniversity().getCode();

        // ✅ Filter: faqat o'z OTM ning yozuvini o'chirish
        Optional<EmployeeJobs> existing = repository.findByIdAndUniversityCode(entityId, universityCode);

        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();  // 404 - boshqa OTM yoki yo'q
        }

        repository.delete(existing.get());
        return ResponseEntity.ok(toMap(existing.get()));  // ← Deleted entity qaytarish
    }
}
```

### ⚠️ BACKWARD COMPATIBILITY HAQIDA

University filter qo'shish **backward-compatible**:

| Aspekt | OLD-HEMIS | NEW-HEMIS | Compatible? |
|--------|-----------|-----------|-------------|
| Response format | `{...JSON...}` | `{...JSON...}` | ✅ HA |
| Field nomlari | `id, code, name` | `id, code, name` | ✅ HA |
| HTTP kodlari | 200, 404, 401 | 200, 404, 401 | ✅ HA |
| Ma'lumot hajmi | Barcha OTM | Faqat o'z OTM | ✅ HA (security) |

**Nima uchun compatible?**
- Foydalanuvchi o'z OTM ma'lumotlarini so'raganda - xuddi old-hemis kabi javob oladi
- Foydalanuvchi boshqa OTM ma'lumotini so'raganda - 404 oladi (old-hemis da ma'lumot ko'rsatardi - XAVFSIZLIK KAMCHILIGI edi!)

### 📊 Entity vs Classifier Farqi

```
ENTITY (User-specific data):           CLASSIFIER (Global data):
┌─────────────────────────────┐        ┌─────────────────────────────┐
│ hemishe_EEmployeeJobs       │        │ hemishe_HTeacherPositionType│
│ - Har bir OTM o'z xodimlari │        │ - 11: Stajer-o'qituvchi    │
│ - FILTER KERAK!             │        │ - 12: O'qituvchi           │
│                             │        │ - 13: Katta o'qituvchi     │
│ OTM 305: 50 ta xodim        │        │ - 14: Dotsent              │
│ OTM 380: 100 ta xodim       │        │ - 15: Professor            │
│ OTM 401: 75 ta xodim        │        │ FILTER KERAK EMAS!         │
│                             │        │ (barcha OTM uchun bir xil) │
└─────────────────────────────┘        └─────────────────────────────┘
```

---

## TEGMASLIK:
- ❌ `/api/v1/web/*`
- ❌ `api-web` moduli

## FAQAT ISHLASH:
- ✅ `api-legacy` moduli
- ✅ `/app/rest/v2/services/*`

---

## MIGRATION CHECKLIST ✅

### ✅ BAJARILGAN (Hozirgi Holat)

- [x] **01.Token** - 3 endpoint migrated
  - POST `/app/rest/oauth/token` (password grant)
  - POST `/app/rest/oauth/token` (refresh grant)
  - GET `/app/rest/user/info`

- [x] **02.Passport ma'lumotlari** - 7 endpoint migrated
  - GET `/services/personal-data/getData` (PINFL + serial)
  - GET `/services/hemishe_PersonalDataService/getData` (CUBA legacy)
  - GET `/services/passport-data/getData` (PINFL + givenDate)
  - GET `/services/passport-data/getDataBySN` (PINFL + serial + captcha)
  - GET `/services/passport-data/getDataBySNBirthdate` (serial + birthdate)
  - GET `/services/passport-data/getDataByPinflBirthdate` (PINFL + birthdate)
  - GET `/services/passport-data/getAddress` (PINFL)

- [x] **endpoint_tester.html** - To'liq funktsional
  - Dual config panel (new vs old)
  - Side-by-side response display
  - Automatic comparison (✅/⚠️)
  - Progress bars for both systems
  - 3 test buttons per endpoint

- [x] **Swagger** - Standardlashtirilgan
  - Tag naming convention documented
  - `01.Token` va `02.Passport ma'lumotlari` (probelsiz!)

### ⏳ KEYINGI QADAMLAR (User kerak bo'lganda)

Har safar user endpoint berganda quyidagi tartibda:

1. **Old-hemis dan topish** (`rest-services.xml`, `old_hemis.json`)
2. **api-legacy ga controller yaratish** (Swagger + o'zbek tilida)
3. **⚠️ TEST & SOLISHTIRISH (MAJBURIY!):**
   - a) Old-hemis dan test: `curl http://localhost:8082/...`
   - b) Yangi hemis dan test: `curl http://localhost:8081/...`
   - c) Javoblarni solishtirish: `diff old_response.json new_response.json`
   - d) Agar farq bor → controllerni tuzatish va qayta test qilish
   - e) Agar farq yo'q → davom etish
4. **endpoint_tester.html ga qo'shish** (faqat testlar muvaffaqiyatli bo'lsa!)
5. **Migration hisobot yozish:**
   - Controller file path
   - Old-hemis vs yangi hemis test natijalari
   - endpoint_tester.html dagi yangi endpoint ID
   - Test natijasi (✅ 100% compatible / ⚠️ Farqlar tuzatildi)

**⚠️ MUHIM QOIDA:** Agar old-hemis (`http://localhost:8082`) yoki yangi hemis (`http://localhost:8081`) serverlari ishlamasa, test qilolmayman va endpoint_tester.html ga qo'sha olmayman! Serverlarni avval ishga tushiring.

---

## 🔧 TEST QILISH UCHUN HELPER SCRIPT

Test jarayonini osonlashtirish uchun quyidagi skriptni ishlatish mumkin:

```bash
#!/bin/bash
# test_endpoint_comparison.sh - Old-hemis vs Yangi hemis endpoint solishtirish

OLD_BASE="http://localhost:8082"
NEW_BASE="http://localhost:8081"

# Token olish (old-hemis)
OLD_TOKEN=$(curl -s -X POST "$OLD_BASE/app/rest/v2/oauth/token" \
  -H "Authorization: Basic $(echo -n 'client:secret' | base64)" \
  -F "grant_type=password" \
  -F "username=feruz" \
  -F "password=BvZzXW6oQxEEte" \
  | jq -r '.access_token')

# Token olish (yangi hemis)
NEW_TOKEN=$(curl -s -X POST "$NEW_BASE/app/rest/v2/oauth/token" \
  -H "Authorization: Basic $(echo -n 'client:secret' | base64)" \
  -F "grant_type=password" \
  -F "username=feruz" \
  -F "password=BvZzXW6oQxEEte" \
  | jq -r '.access_token')

# Endpoint test qilish
ENDPOINT="$1"  # Masalan: /app/rest/v2/services/pass/data?pinfl=31503776560016

echo "📡 Testing: $ENDPOINT"
echo ""

# Old-hemis dan test
echo "🏛️ Old-Hemis Response:"
curl -s "$OLD_BASE$ENDPOINT" \
  -H "Authorization: Bearer $OLD_TOKEN" \
  | jq '.' > /tmp/old_response.json
cat /tmp/old_response.json
echo ""

# Yangi hemis dan test
echo "🆕 Yangi-Hemis Response:"
curl -s "$NEW_BASE$ENDPOINT" \
  -H "Authorization: Bearer $NEW_TOKEN" \
  | jq '.' > /tmp/new_response.json
cat /tmp/new_response.json
echo ""

# Solishtirish
echo "📊 Comparison:"
if diff /tmp/old_response.json /tmp/new_response.json > /dev/null; then
  echo "✅ Responses are 100% identical!"
else
  echo "⚠️ Differences found:"
  diff /tmp/old_response.json /tmp/new_response.json
fi
```

**Ishlatish:**
```bash
chmod +x test_endpoint_comparison.sh
./test_endpoint_comparison.sh "/app/rest/v2/services/pass/data?pinfl=31503776560016"
```

---

## 📊 PORT WORKFLOW DIAGRAMMASI (YANGILANGAN!)

```
┌─────────────────────────────────────────────────────────────────┐
│ 1️⃣ USER ENDPOINT BERADI                                         │
│    PORT: GET /app/rest/v2/entities/hemishe_EUniversityDepartment│
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2️⃣ DUPLICATE CHECK (existing controllers)                       │
│    ✅ Topilmadi → Davom et                                      │
│    ⚠️ Topildi → "Allaqachon mavjud!" xabar ber                 │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3️⃣ 🎯 AVVAL: OLD-HEMIS DAN RESPONSE OLISH (MUHIM!)             │
│                                                                  │
│    curl http://localhost:8082/app/rest/v2/entities/...         │
│      -H "Authorization: Bearer $TOKEN"                          │
│      > /tmp/old_response.json                                   │
│                                                                  │
│    Response formatini SAQLASH va TAHLIL qilish:                 │
│    - Qaysi maydonlar bor?                                       │
│    - Qaysi tartibda?                                            │
│    - Nested objectlar bormi?                                    │
│    - Qaysi maydonlar null/empty?                                │
│                                                                  │
│    ⚠️ AGAR OLD-HEMIS XATO QAYTARSA → PORT qilmaslik!           │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│ 4️⃣ METADATA EXTRACTION                                          │
│    - old_hemis.json: tag, nom, description                     │
│    - rest-services.xml: parametrlar, method                    │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│ 5️⃣ CONTROLLER GENERATION (OLD-HEMIS RESPONSE GA MOS!)          │
│                                                                  │
│    ✅ Faqat old-hemis qaytargan maydonlarni qaytarish          │
│    ✅ Aynan shu tartibda (LinkedHashMap)                        │
│    ✅ Aynan shu format (nested/flat)                            │
│                                                                  │
│    - Java controller class                                      │
│    - toMap() metodi - OLD-HEMIS formatiga 100% mos             │
│    - Swagger annotations (o'zbek tilida)                       │
│    - @Transactional annotatsiyalari                            │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│ 6️⃣ TEST (Avtomatik 100% mos bo'lishi kerak!)                   │
│                                                                  │
│    curl http://localhost:8081/.../endpoint > new_response.json │
│    diff old_response.json new_response.json                    │
│                                                                  │
│    ✅ 100% bir xil → Davom et                                   │
│    ⚠️ Farq bor → 5️⃣ ga qaytib toMap() ni to'g'rilash           │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│ 6️⃣ ENDPOINT_TESTER.HTML GA QO'SHISH                             │
│    (Faqat testlar muvaffaqiyatli bo'lsa!)                       │
│                                                                  │
│    - 🆕 Yangi Hemis tugmasi                                     │
│    - 🏛️ Old Hemis tugmasi                                      │
│    - Ikkalasini Ham Test tugmasi                               │
│    - Side-by-side response display                             │
│    - Automatic comparison (✅/⚠️)                              │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│ 7️⃣ MIGRATION HISOBOT                                            │
│    - Controller file path                                       │
│    - Swagger tag                                                │
│    - Test natijalari (old vs new)                              │
│    - endpoint_tester.html dagi ID                              │
│    - ✅ Status: 100% backward compatible                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📝 QUICK REFERENCE - PORT QILISH JARAYONI

### 1️⃣ User Endpoint Beradi
```
PORT: GET /app/rest/v2/services/bimm/disabilityCheck
```

### 2️⃣ Claude Code Bajaradi (AVTOMATIK)

**A) Metadata olish:**
- `old_hemis.json` → tag, nom, description
- `rest-services.xml` → parametrlar

**B) Controller yozish:**
- Java class + Swagger + DTOs

**C) ⚠️ TEST & SOLISHTIRISH (MAJBURIY!):**
```bash
# Old-hemis test (port 8082)
curl http://localhost:8082/app/rest/v2/services/bimm/disabilityCheck?pinfl=... \
  -H "Authorization: Bearer TOKEN"

# Yangi hemis test (port 8081)
curl http://localhost:8081/app/rest/v2/services/bimm/disabilityCheck?pinfl=... \
  -H "Authorization: Bearer TOKEN"

# Solishtirish
diff old_response.json new_response.json
```

**D) Natija:**
- ✅ 100% bir xil → endpoint_tester.html ga qo'shish
- ⚠️ Farq bor → controllerni tuzatish va qayta test
- ❌ Serverlar ishlamasa → ABORT!

### 3️⃣ endpoint_tester.html Yangilanadi

```javascript
// ID raqamlash: har bir kategoriya ichida 1 dan boshlanadi
// 01.Token: id 1, 2, 3
// 02.Captcha: id 1, 2
// 03.BIMM: id 1, 2, 3, ...
{
  id: 1,  // ← Kategoriya ichida birinchi endpoint
  category: "03.BIMM",
  name: "Nogironlik tekshiruvi",
  method: "GET",
  url: "/app/rest/v2/services/bimm/disabilityCheck",
  requiresAuth: true,
  params: { pinfl: "{pinfl}" },
  description: "PINFL orqali nogironlik ma'lumotlarini tekshirish",
  ported: true
}
```

### 4️⃣ Migration Hisobot

```
✅ ENDPOINT PORTING MUVAFFAQIYATLI!

📋 Ma'lumotlar:
- Endpoint: GET /app/rest/v2/services/bimm/disabilityCheck
- Controller: /hemis-back/api-legacy/src/.../BimmController.java
- Tag: 03.BIMM
- Test natijasi: ✅ 100% backward compatible

🔗 Test qilish:
- endpoint_tester.html: 03.BIMM kategoriyasida ID=1 (har kategoriya o'z ID tartibiga ega)
- Yangi: http://localhost:8081/api/swagger-ui.html
- Old: http://localhost:8082/app/rest/v2/services/bimm/disabilityCheck?pinfl=...
```

---

## 🎛️ ENDPOINT_TESTER.HTML CONFIG PANEL QOIDALARI

### ⚠️ MUHIM: Default Qiymatlar - User/University bo'yicha

**Ikki tizim TURLI userlar va universitetlar bilan ishlaydi:**

| Tizim | Port | User | University | Ma'lumotlar bazasi |
|-------|------|------|------------|-------------------|
| **🆕 Yangi Hemis** | 8081 | `otm401` | 401 | `hemis_401` |
| **🏛️ Eski Hemis** | 8082 | `otm351` | 351 | `hemis_351` |

**Shuning uchun default qiymatlar quyidagicha bo'lishi SHART:**

```javascript
// ✅ TO'G'RI - Yangi Hemis (otm401) uchun default
{
    entityId: {
        label: "Bo'linma kodi",
        type: "text",
        default: "401-102-08",  // ← otm401/university 401 ma'lumoti
        placeholder: "Yangi:401-102-08, Eski:351-118"
    }
}

// ❌ NOTO'G'RI - Eski hemis ma'lumoti yangi hemis uchun ishlamaydi!
{
    entityId: {
        label: "Bo'linma kodi",
        type: "text",
        default: "351-118",  // ← Bu otm351 ga tegishli, otm401 da topilmaydi!
        ...
    }
}
```

**Qoida:**
1. **default** qiymat = **Yangi Hemis (otm401/university 401)** ma'lumotlaridan
2. **placeholder** da ikkala variant ko'rsatilsin: `"Yangi:XXX, Eski:YYY"`
3. Foydalanuvchi eski hemis test qilganda placeholder dan to'g'ri qiymatni ko'radi

**Misollar:**

| Ma'lumot turi | Yangi Hemis (default) | Eski Hemis (placeholder) |
|---------------|----------------------|--------------------------|
| Bo'linma kodi | `401-102-08` | `351-118` |
| Student ID | `401-xxx-xxx` | `351-xxx-xxx` |
| Employee ID | `401-yyy-yyy` | `351-yyy-yyy` |

---

### ✅ Config Panel Struktura

`endpoint_tester.html` da ikki tizim uchun alohida config panellar mavjud:

#### 🆕 Yangi Hemis Config (Chapda - Yashil)
| Config ID | Tavsif | Misol |
|-----------|--------|-------|
| `newBaseUrl` | Base URL | `http://localhost:8081` |
| `newUsername` | Username | `otm401` |
| `newPassword` | Password | `XCZDAb7qvGTXxz` |
| `newPinfl` | PINFL | `31507976020031` |
| `newSerial` | Passport seriyasi | `AA6970877` |
| `newBirthdate` | Tug'ilgan sana | `1997-07-15` |
| `newStudentId` | Student UUID | `cdd4acfc-a616-ddde-debd-5631777ec588` |

#### 🏛️ Old Hemis Config (O'ngda - Qizil)
| Config ID | Tavsif | Misol |
|-----------|--------|-------|
| `oldBaseUrl` | Base URL | `http://localhost:9000/proxy/old-hemis` |
| `oldUsername` | Username | `otm351` |
| `oldPassword` | Password | `XCZDAb7qvGTXxz` |
| `oldPinfl` | PINFL | `31507976020031` |
| `oldSerial` | Passport seriyasi | `AA6970877` |
| `oldBirthdate` | Tug'ilgan sana | `1997-07-15` |
| `oldStudentId` | Student UUID | `cdd4acfc-a616-ddde-debd-5631777ec588` |

---

### ✅ useConfig Flags (Input Fields)

Endpoint yaratishda `inputFields` ichida config paneldan qiymat olish uchun quyidagi flaglar ishlatiladi:

```javascript
inputFields: {
    pinfl: {
        label: "PINFL",
        type: "text",
        placeholder: "14 raqamli PINFL",
        default: "",  // ⚠️ Bo'sh qoldirish - configdan olinadi!
        useConfigPinfl: true,  // ← CONFIG FLAG
        required: true
    },
    seriaNumber: {
        label: "Passport seria",
        type: "text",
        placeholder: "AA1234567",
        default: "",
        useConfigSerial: true,  // ← CONFIG FLAG
        required: true
    },
    birthdate: {
        label: "Tug'ilgan sana",
        type: "text",
        placeholder: "YYYY-MM-DD",
        default: "",
        useConfigBirthdate: true,  // ← CONFIG FLAG
        required: true
    },
    entityId: {
        label: "Student ID",
        type: "text",
        placeholder: "UUID format",
        default: "",
        useConfigStudentId: true,  // ← CONFIG FLAG
        required: true
    }
}
```

#### Flag Types:
| Flag | Config IDs | Tavsif |
|------|-----------|--------|
| `useConfigPinfl: true` | `newPinfl` / `oldPinfl` | PINFL maydoniga config qiymati |
| `useConfigSerial: true` | `newSerial` / `oldSerial` | Passport seriyasiga config qiymati |
| `useConfigBirthdate: true` | `newBirthdate` / `oldBirthdate` | Tug'ilgan sanaga config qiymati |
| `useConfigStudentId: true` | `newStudentId` / `oldStudentId` | Student UUID ga config qiymati |

#### ⚠️ MUHIM: JavaScript Render Logic

Yangi endpoint qo'shganda, `renderNewHemisInputs()` va `renderOldHemisInputs()` funksiyalarida quyidagi logic mavjud:

```javascript
// renderNewHemisInputs() ichida:
let defaultValue = field.default || '';
if (field.useConfigPinfl) {
    defaultValue = document.getElementById('newPinfl')?.value || '';
} else if (field.useConfigSerial) {
    defaultValue = document.getElementById('newSerial')?.value || '';
} else if (field.useConfigBirthdate) {
    defaultValue = document.getElementById('newBirthdate')?.value || '';
} else if (field.useConfigStudentId) {
    defaultValue = document.getElementById('newStudentId')?.value || '';
}

// renderOldHemisInputs() ichida:
let defaultValue = field.default || '';
if (field.useConfigPinfl) {
    defaultValue = document.getElementById('oldPinfl')?.value || '';
} else if (field.useConfigSerial) {
    defaultValue = document.getElementById('oldSerial')?.value || '';
} else if (field.useConfigBirthdate) {
    defaultValue = document.getElementById('oldBirthdate')?.value || '';
} else if (field.useConfigStudentId) {
    defaultValue = document.getElementById('oldStudentId')?.value || '';
}
```

---

### ✅ Authentication Requirements (requiresAuth & dependsOn)

#### ⚠️ CRITICAL: Old-Hemis uchun auth talablari

Old-Hemis **barcha** endpointlar uchun auth talab qiladi (public endpointlar ham!). Shuning uchun:

```javascript
{
    id: 1,  // ← Kategoriya ichida 1 dan boshlanadi
    category: "02.Captcha",
    name: "getNumericCaptcha",
    method: "GET",
    url: "/app/rest/v2/services/captcha/getNumericCaptcha",
    requiresAuth: true,   // ← OLD-HEMIS UCHUN MAJBURIY!
    dependsOn: 1,         // ← #1 Token olish dan keyin
    description: "..."
}
```

| Property | Qiymat | Tavsif |
|----------|--------|--------|
| `requiresAuth: true` | ✅ MAJBURIY | Old-hemis uchun har doim `true` |
| `dependsOn: 1` | ✅ MAJBURIY | Token endpoint (id=1) dan keyin ishlaydi |
| `requiresAuth: false` | ❌ QILMASLIK | Faqat yangi hemis public endpointlar uchun |

#### Noto'g'ri va To'g'ri Misollar:

```javascript
// ❌ NOTO'G'RI - Old-hemis da 401 error beradi!
{
    id: 1,  // ← Kategoriya ichida 1 dan boshlanadi
    requiresAuth: false,  // ← XATO!
    // dependsOn yo'q      // ← XATO!
}

// ✅ TO'G'RI
{
    id: 1,  // ← Kategoriya ichida 1 dan boshlanadi
    requiresAuth: true,
    dependsOn: 1,  // ← Token endpoint (01.Token kategoriyasida id=1)
}
```

---

### 📋 YANGI ENDPOINT QO'SHISH CHECKLIST

Har bir yangi endpoint qo'shganda quyidagilarni tekshiring:

1. **[ ] Category** - To'g'ri kategoriya (`01.Token`, `02.Captcha`, `03.Passport`, etc.)

2. **[ ] URL** - Old-hemis URL bilan bir xil (`/app/rest/v2/services/...`)

3. **[ ] requiresAuth** - Old-hemis uchun `true` qo'yilganmi?

4. **[ ] dependsOn** - Token endpoint ga bog'langanmi? (`dependsOn: 1`)

5. **[ ] inputFields** - Config flaglar to'g'ri qo'yilganmi?
   - `useConfigPinfl: true` - PINFL maydonlari uchun
   - `useConfigSerial: true` - Passport seriya maydonlari uchun
   - `useConfigBirthdate: true` - Tug'ilgan sana maydonlari uchun
   - `useConfigStudentId: true` - Student UUID maydonlari uchun

6. **[ ] default: ""** - Config flag bo'lsa, default bo'sh qoldirish

---

### 📊 MISOL: To'liq Endpoint Definition

```javascript
{
    id: 3,  // ← Kategoriya ichida uchinchi endpoint (1, 2, 3...)
    category: "03.Passport ma'lumotlari",
    name: "getDataByPinflBirthdate (PINFL + birthdate)",
    method: "GET",
    url: "/app/rest/v2/services/passport-data/getDataByPinflBirthdate",
    requiresAuth: true,      // ← OLD-HEMIS UCHUN
    dependsOn: 1,            // ← TOKEN OLISH DAN KEYIN
    inputFields: {
        pinfl: {
            label: "PINFL",
            type: "text",
            placeholder: "14 raqamli PINFL",
            default: "",              // ← BO'SH (configdan olinadi)
            useConfigPinfl: true,     // ← CONFIG FLAG
            required: true
        },
        birthdate: {
            label: "Tug'ilgan sana",
            type: "text",
            placeholder: "YYYY-MM-DD",
            default: "",              // ← BO'SH (configdan olinadi)
            useConfigBirthdate: true, // ← CONFIG FLAG
            required: true
        },
        captchaId: {
            label: "Captcha ID",
            type: "text",
            placeholder: "#4 dan olingan captcha ID",
            default: "",
            required: true
        },
        captchaValue: {
            label: "Captcha qiymati",
            type: "text",
            placeholder: "Rasmda ko'rsatilgan raqam",
            default: "",
            required: true
        }
    },
    description: "PINFL va tug'ilgan sana orqali passport ma'lumotlarini olish",
    ported: true
}
```

---

**Faqat user endpoint berganda ishlayman!**
