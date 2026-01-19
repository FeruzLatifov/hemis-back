
# HEMIS-BACK - Claude Code Qoidalari

## ⚠️ MUHIM QOIDA

**Faqat user endpoint berganda ishlayman!**

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

## 🤖 MEN NIMA QILAMAN (avtomatik)

Siz endpoint yozganingizdan keyin:

### 1️⃣ TEKSHIRISH (Duplicate oldini olish)

✅ `old_hemis.json` dan qidiramam:
   - Tag nomini (masalan: "Soliq", "Passport ma'lumotlari")
   - Endpoint o'zbek nomini (masalan: "Ijara shartnomasi")
   - Description/izohni
   - Parametrlarni

✅ Mavjud controllerlarni tekshiraman:
   - Agar endpoint allaqachon migrate qilingan bo'lsa → ⚠️ "Bu endpoint allaqachon mavjud!" deb xabar beraman
   - Agar yo'q bo'lsa → ✅ migration boshlayman

### 2️⃣ MIGRATION (Yangi endpointlar uchun)

✅ `rest-services.xml` dan parametrlarni olaman
✅ `old_hemis.json` dan nom va izohlarni olaman
✅ `api-legacy` ga controller yarataman
✅ Swagger qo'shaman (o'zbek tilida, old_hemis.json dan):
   ```java
   @Tag(name = "04.Soliq", description = "Soliq xizmati API")
   @Operation(
       summary = "Ijara shartnomasi",  // old_hemis.json dan
       description = "..."              // old_hemis.json dan
   )
   ```
✅ `endpoint_tester.html` ga 3 ta tugma qo'shaman
✅ Default test ma'lumotlarni bazadan olaman
✅ Migration hisobot beraman

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

5. ✅ **endpoint_tester.html ga qo'shaman:**
   - 🆕 Yangi Hemis tugmasi
   - 🏛️ Old Hemis tugmasi
   - Ikkalasini Ham Test tugmasi

6. ✅ **Hisobot:**
   - Migration holati
   - Test natijasi

**HAMMASI!** 🎉

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
- **🏛️ Old-Hemis CUBA**: `http://localhost:8082/app` (qizil)

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

Har safar user endpoint berganda:

1. Old-hemis dan topish (`rest-services.xml`)
2. api-legacy ga controller yaratish
3. Swagger to'g'ri qo'shish
4. endpoint_tester.html ga qo'shish
5. Test qilish
6. Migration hisobot yozish

---

**Faqat user endpoint berganda ishlayman!**
