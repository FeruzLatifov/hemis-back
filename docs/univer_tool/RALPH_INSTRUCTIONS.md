---
active: false
iteration: 1
max_iterations: 50
completion_promise: "ALL_TESTS_PASSED"
started_at: "2026-01-29T23:00:00Z"
---

# HEMIS-BACK: 176 ta endpointni birma-bir solishtirish va tuzatish

## Maqsad

old-hemis (CUBA, :8082) ni hemis-back (Spring Boot, :8081) bilan almashtiryapmiz. 200 ta universitetda Univer (PHP) ishlaydi — uzilishsiz o'tishi kerak. **Har bir endpointni old-hemis bilan solishtir, hemis-back ni to'g'irla.**

---

## Muhit

| Xizmat | Port | Izoh |
|--------|------|------|
| hemis-back | 8081 | **Shuning kodini tuzatamiz** |
| old-hemis | 8082 | **Etalon — to'g'ri format va javobni o'rganish uchun** |
| proxy | 9001 | DB bootstrap uchun |

**Autentifikatsiya:**
```bash
# Token olish (ikkala server uchun bir xil)
curl -s -X POST http://localhost:8082/app/rest/v2/oauth/token \
  -H 'Authorization: Basic Y2xpZW50OnNlY3JldA==' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&username=otm401&password=XCZDAb7qvGTXxz'
```

---

## MUHIM QOIDALAR

### 1. RUXSAT — HECH NARSA SO'RAMA

- **Hech qanday ruxsat so'raMA** — barcha ruxsatlar berilgan
- **AskUserQuestion ishlatMA** — hech qachon
- Fayllarni o'zgartir, server restart qil, test ishla — **hammasini avtomatik qil**

### 2. BAZANI O'ZGARTIRMA

- old-hemis ga **faqat GET** so'rovlar yubor (o'qish uchun)
- POST/PUT/DELETE ni old-hemis ga yuborMA (bazani buzishi mumkin)
- POST/PUT/DELETE endpointlar uchun — old-hemis dan **Swagger/docs yoki GET javoblari** asosida to'g'ri formatni o'rgan

### 3. FORMAT — ENG MUHIM NARSA

**"Ishlamadi" deyishdan oldin, sababi nimada ekanini aniq tushun:**

- **Endpoint ishlamaydi** = hemis-back kodi xato (500, NullPointer, etc.) → hemis-back kodni tuzat
- **Noto'g'ri format yuborildi** = test xato ma'lumot yuborayapti (400) → old-hemis dan to'g'ri formatni o'rgan, testni yoki hemis-back validatsiyani tuzat
- **Permission yo'q** = role/permission konfiguratsiyasi xato (403) → SecurityConfig yoki @PreAuthorize ni tuzat
- **Endpoint yo'q** = hali implement qilinmagan (404) → old-hemis da bor-yo'qligini tekshir, agar bor bo'lsa implement qil

### 4. HAR BIR ENDPOINT UCHUN ISH TARTIBI

```
1. old-hemis (:8082) ga GET so'rov yubor → javob formatini o'rgan
2. hemis-back (:8081) ga xuddi shu so'rovni yubor → farqni ko'r
3. Agar farq bo'lsa:
   a. hemis-back KODNI tuzat (500/404/403 uchun)
   b. yoki TEST faylni tuzat (agar test noto'g'ri format yuborayotgan bo'lsa)
   c. yoki VALIDATSIYANI moslashtir (agar hemis-back o'zgacha validatsiya qilayotgan bo'lsa)
4. RESTART ber
5. Qayta test qil
6. Keyingi endpointga o't
```

---

## Har bir iteratsiyada ish tartibi

### 1-QADAM: Muhitni tekshir

```bash
# hemis-back ishlayaptimi?
curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/actuator/health
# old-hemis ishlayaptimi?
curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/app/rest/v2/oauth/token -X POST -H 'Authorization: Basic Y2xpZW50OnNlY3JldA==' -H 'Content-Type: application/x-www-form-urlencoded' -d 'grant_type=password&username=otm401&password=XCZDAb7qvGTXxz'
# proxy ishlayaptimi?
curl -s -o /dev/null -w "%{http_code}" http://localhost:9001/db/bootstrap -X POST -H 'Content-Type: application/json' -d '{"host":"localhost","port":5432,"dbname":"hemis_401","user":"postgres","password":"postgres"}'
```

Ishlamasa ishga tushir:
```bash
# hemis-back
cd /home/adm1n/startup/hemis-back && nohup ./gradlew :app:bootRun > /tmp/hemis-back.log 2>&1 & sleep 45
# proxy
cd /home/adm1n/startup/hemis-back/docs/univer_tool && nohup python3 integration-proxy.py http://localhost:8081 hemis_401 postgres postgres > /tmp/proxy.log 2>&1 & sleep 3
```

### 2-QADAM: Solishtirish testini ishga tushir

```bash
node /home/adm1n/startup/hemis-back/docs/univer_tool/compare_endpoints.js --json 2>&1
```

Bu skript har bir endpointni **ikkala serverda** sinab, natijani ko'rsatadi:
- `[✓] MATCH` — ikkala serverda bir xil ishlaydi
- `[✗] MISMATCH` — old-hemis ishlaydi, hemis-back ISHLAMAYDI → **TUZATISH KERAK**
- `[~] BOTH_FAIL` — ikkalasida ham ishlamaydi → test formatini tekshir
- `[+] NEW_BETTER` — hemis-back yaxshiroq ishlaydi

### 3-QADAM: MISMATCH endpointlarni tuzat

Har bir MISMATCH endpoint uchun:

#### A. old-hemis dan to'g'ri formatni o'rgan

```bash
# Token olish
OLD_TOKEN=$(curl -s -X POST http://localhost:8082/app/rest/v2/oauth/token \
  -H 'Authorization: Basic Y2xpZW50OnNlY3JldA==' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&username=otm401&password=XCZDAb7qvGTXxz' | jq -r '.access_token')

# GET endpoint — to'g'ri formatni o'rganish
curl -s http://localhost:8082/app/rest/v2/entities/hemishe_EStudent \
  -H "Authorization: Bearer $OLD_TOKEN" \
  -H "Accept: application/json" | jq '.[0]' | head -50

# old-hemis javob strukturasini yaxshilab o'rgan:
# - qaysi fieldlar bor?
# - qaysi fieldlar required?
# - nested objectlar qanday formatda?
# - classifier fieldlar qanday (code/name yoki id)?
```

#### B. Xato sababini aniqlash

**Agar hemis-back 500 qaytarsa:**
```bash
# Log dan stack trace o'qi
tail -200 /tmp/hemis-back.log | grep -A 20 "Exception\|Error\|ERROR"
```

**Agar hemis-back 400 qaytarsa:**
```bash
# Qaysi field validatsiyadan o'tmayapti?
NEW_TOKEN=$(curl -s -X POST http://localhost:8081/app/rest/v2/oauth/token \
  -H 'Authorization: Basic Y2xpZW50OnNlY3JldA==' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&username=otm401&password=XCZDAb7qvGTXxz' | jq -r '.access_token')

curl -s http://localhost:8081/app/rest/v2/entities/hemishe_EStudent \
  -X POST \
  -H "Authorization: Bearer $NEW_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fieldlar": "shu yerda"}' | jq .
# Xato javobni yaxshilab o'qi — qaysi field muammo?
```

**Agar hemis-back 403 qaytarsa:**
```bash
# SecurityConfig.java ni tekshir
grep -n "hemishe_RIctEquipment\|PreAuthorize\|requestMatchers" \
  /home/adm1n/startup/hemis-back/security/src/main/java/uz/hemis/security/config/SecurityConfig.java
# Controller da @PreAuthorize ni tekshir
grep -rn "PreAuthorize" /home/adm1n/startup/hemis-back/api-legacy/src/main/java/uz/hemis/api/legacy/controller/
```

#### C. hemis-back kodni tuzat

**Loyiha tuzilishi:**
```
/home/adm1n/startup/hemis-back/
├── api-legacy/src/main/java/uz/hemis/api/legacy/controller/          ← Entity controllerlar (98 ta)
├── api-legacy/src/main/java/uz/hemis/api/legacy/controller/services/ ← Service controllerlar (31 ta)
├── api-legacy/src/main/java/uz/hemis/api/legacy/dto/                 ← DTO lar
├── service/src/main/java/uz/hemis/service/                           ← Biznes logika
├── domain/src/main/java/uz/hemis/domain/entity/                      ← JPA entitylar
├── domain/src/main/java/uz/hemis/domain/repository/                  ← Repository
├── security/src/main/java/uz/hemis/security/config/                  ← Xavfsizlik
└── app/src/main/resources/application.yml                            ← Konfiguratsiya
```

**Tuzatish patternlari:**

1. **500 → NullPointer/JPA xato:**
   - Entity da field mapping xato → `@Column`, `@JoinColumn` tekshir
   - Service da null check yo'q → old-hemis javob strukturasidan required fieldlarni o'rgan
   - DTO→Entity mapping xato → Mapper classni tekshir

2. **400 → Validatsiya farqi:**
   - old-hemis required qilmagan fieldni hemis-back required qilgan → `@NotNull` ni olib tashla
   - Format farqi (date, enum, classifier) → old-hemis formatiga moslash
   - Nested object farqi → old-hemis dan javob olil, request formatini mosla

3. **403 → Permission:**
   - Controller da `@PreAuthorize` annotatsiyani olib tashla yoki to'g'ri role qo'sh
   - SecurityConfig da endpoint ni authenticated() ga qo'sh

4. **404 → Endpoint yo'q:**
   - Controller da method qo'shish
   - URL mapping tekshirish (`@GetMapping`, `@PostMapping`)

5. **Status code farqi (201→200, 204→200):**
   - `return ResponseEntity.status(201).body(...)` → `return ResponseEntity.ok(...)` ga o'zgartir
   - `return ResponseEntity.noContent().build()` → `return ResponseEntity.ok().build()` ga o'zgartir

#### D. RESTART (MAJBURIY)

```bash
# Eski processni to'xtat
lsof -ti:8081 | xargs kill -9 2>/dev/null
sleep 3

# Build + Run
cd /home/adm1n/startup/hemis-back && nohup ./gradlew :app:bootRun > /tmp/hemis-back.log 2>&1 &

# Yuklanishini kutish
sleep 45

# Health check
curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/actuator/health
# 200 bo'lishi kerak

# Agar 200 emas — log tekshir:
tail -50 /tmp/hemis-back.log
```

#### E. Qayta test

```bash
# Bitta endpointni test qilish:
node /home/adm1n/startup/hemis-back/docs/univer_tool/compare_endpoints.js --test=student-create 2>&1

# Yoki hammasini:
node /home/adm1n/startup/hemis-back/docs/univer_tool/compare_endpoints.js --json 2>&1
```

### 4-QADAM: BOTH_FAIL endpointlarni tekshir

Agar ikkala serverda ham ishlamasa:
1. Test noto'g'ri format/ma'lumot yuborayotgan bo'lishi mumkin
2. old-hemis da bu endpoint boshqa parametrlar kutishi mumkin
3. Test JS faylni tekshir va to'g'ri format bilan yangilash

**Test fayllari:**
```
/home/adm1n/startup/hemis-back/docs/univer_tool/integration/tests/
├── 00-auth.js ... 13-entity-stats.js
```

### 5-QADAM: Natijani yozib bor

Har bir iteratsiya oxirida shu faylning **Hozirgi holat** bo'limini yangilab bor.

---

## Test fayllarni o'zgartirish qoidalari

Test JS faylidagi har bir test:
```javascript
{
    id: 'student-create',
    category: '07. Entity: Student',
    name: 'Talaba yaratish (EStudent POST)',
    order: 3,
    method: 'POST',
    url: '/app/rest/v2/entities/hemishe_EStudent',
    auth: 'bearer',
    expectedStatus: 200,
    bodyBuilder: (data, stored) => ({
        // body shu yerda
    }),
    validate: (body, status) => ([
        // validatsiya shu yerda
    ])
}
```

**O'zgartirish mumkin bo'lgan narsalar:**
1. `expectedStatus` — agar old-hemis da ham boshqa status qaytarsa
2. `body` / `bodyBuilder` — agar noto'g'ri format yuborayotgan bo'lsa (old-hemis formatiga moslab)
3. `validate` — agar validatsiya logikasi noto'g'ri bo'lsa
4. `params` — agar noto'g'ri parametr yuborayotgan bo'lsa

---

## Endpointlar ro'yxati (176 ta, 14 kategoriya)

### 00. Auth (3)
auth-token, auth-refresh, auth-userinfo

### 01. Classifiers (3)
classifiers-info, classifiers-all-items, captcha-generate

### 02. Student Services (8)
student-id, student-validate, student-update, student-gpa-post, student-contract-statistics, student-check-scholarship, doctoral-student-id, student-gpa-delete

### 03. Teacher Services (2)
teacher-id, teacher-add-job

### 04. Passport (3)
passport-by-sn, passport-by-pinfl-birthdate, passport-address

### 05. University (3)
university-config, university-get, verify-code-send

### 06. External Services (15)
bimm-certificate, bimm-academic-degree, bimm-poverty-register, social-single-register, social-women, employment-workbook, employment-graduate-list, legal-entity-bank-requisites, billing-scholarship, billing-invoice, uzasbo-scholarship, speciality-get, group-get, diplom-blank-get, diplom-blank-set-status

### 07. Entity: Student (10)
student-list, student-single, student-create, student-entity-update, student-gpa-list, student-gpa-single, student-gpa-create, student-gpa-update, student-delete, student-gpa-entity-delete

### 08. Entity: Teacher (10-12)
teacher-list, teacher-single, teacher-create, teacher-update, teacher-delete, employee-jobs-list, employee-jobs-single, employee-jobs-create, employee-jobs-update, employee-jobs-delete

### 09. Entity: Structure (19)
university-entity-list/single/create/update, department-list/single/create/update, group-list/single/create/update, speciality-list/single/create/update, department-delete, group-delete, speciality-delete

### 10. Entity: Diploma (15)
diploma-list/single/create/update, student-certificate-list/single/create/update, employee-certificate-list/single/create/update, diploma-delete, student-certificate-delete, employee-certificate-delete

### 11. Entity: Science (44)
project-*, project-meta-*, project-executor-*, publication-scientific-*, publication-methodical-*, publication-property-*, publication-author-meta-*, research-activity-*, doctorate-student-*, dissertation-defense-*

### 12. Entity: Admin (28)
admin-student2-*, admin-student3-*, admin-student4-*, admin-student-sport-*, admin-employee1-*, admin-employee2-*, admin-employee3-*

### 13. Entity: Stats (12)
ict-equipment-*, laboratories-*, education-materials-*

---

## Hozirgi holat

**Iteratsiya:** 1
**Boshlanish vaqti:** 2026-01-29

### old-hemis bilan solishtirish natijasi (TUZATILGANDAN KEYIN):
| Ko'rsatkich | Soni | Izoh |
|-------------|------|------|
| **MATCH** | 81 | Ikkala serverda bir xil ishlaydi (MATCH + NEW_BETTER) |
| **MISMATCH** | 0 | ✅ BARCHA MISMATCH TUZATILDI |
| **BOTH_FAIL** | 92 | Ikkalasida ham ishlamaydi → test format xato yoki endpoint unused |
| **SKIP** | 2 | auth (allaqachon bajarilgan) |

### FAQAT 10 TA HAQIQIY MISMATCH — hemis-back da TUZATISH KERAK:

| # | Test ID | OLD | NEW | Tur | Izoh |
|---|---------|-----|-----|-----|------|
| 1 | passport-by-sn | 200 | 400 | Validatsiya | Passport qidirish format farqi |
| 2 | passport-by-pinfl-birthdate | 200 | 400 | Validatsiya | PINFL qidirish format farqi |
| 3 | verify-code-send | 200 | 404 | Not Found | SMS kod endpointi yo'q |
| 4 | billing-scholarship | 200 | 404 | Not Found | Billing endpointi yo'q |
| 5 | diplom-blank-get | 200 | 404 | Not Found | Diplom blank endpointi yo'q |
| 6 | teacher-delete | 200 | 500 | Server Error | O'qituvchi o'chirish xatosi |
| 7 | university-entity-create | 201 | 400 | Validatsiya | Universitet yaratish format farqi |
| 8 | ict-equipment-list | 200 | 403 | Permission | AKT jihozlar ruxsati yo'q |
| 9 | laboratories-list | 200 | 403 | Permission | Laboratoriyalar ruxsati yo'q |
| 10 | education-materials-list | 200 | 403 | Permission | O'quv materiallar ruxsati yo'q |

### 95 ta BOTH_FAIL — test format xato (ikkalasida ham ishlamaydi):
Bu testlar NOTO'G'RI FORMAT/MA'LUMOT yuborayapti. old-hemis da ham ishlamaydi — demak hemis-back ning muammosi emas. **Test fayllarni tuzatish kerak** — old-hemis dan to'g'ri formatni o'rganib, test bodyBuilder/params larni yangilash.

### Tuzatilgan testlar:

| # | Test ID | Muammo | Yechim |
|---|---------|--------|--------|
| 1 | passport-by-sn | 400 (captcha invalid) | `ResponseEntity.badRequest()` → `ResponseEntity.ok()` (old-hemis formatiga mos) |
| 2 | passport-by-pinfl-birthdate | 400 (captcha invalid) | Xuddi shu tuzatish — old-hemis 200 qaytaradi |
| 3 | verify-code-send | 404 (URL mapping xato) | `@PostMapping` ga `/app/rest/v2/services/send/verifyCode` prefix qo'shildi |
| 4 | billing-scholarship | 404→400 (URL + pinfls key) | URL prefix tuzatildi + `pinfls` key qo'llab-quvvatlanadi (PHP format) |
| 5 | diplom-blank-get | 404 (URL mapping xato) | `@RequestMapping` ga `/app/rest/v2/services/diplom-blank` prefix qo'shildi |
| 6 | teacher-delete | 500 (endpoint yo'q) | `@DeleteMapping("/{entityId}")` soft-delete methodi qo'shildi |
| 7 | university-entity-create | 400 (code majburiy) | code bo'lmasa auto-generate qilish logikasi qo'shildi |
| 8 | ict-equipment-list | 403→500 | `@PreAuthorize("hasRole('ROLE_USER')")` → `isAuthenticated()` + Entity UUID→String tuzatish |
| 9 | laboratories-list | 403→500 | Permission + `@Lob` → `columnDefinition="TEXT"` + UUID→String |
| 10 | education-materials-list | 403→500 | Permission + `@Lob` → `columnDefinition="TEXT"` + UUID→String |
| + | uzasbo-scholarship | 404 (URL mapping) | ExternalIntegrationController ga `/app/rest/v2/services` prefix qo'shildi |

### Ish tartibi:
1. **Avval 10 ta MISMATCH ni tuzat** — bu hemis-back dagi haqiqiy muammolar
2. **Keyin 95 ta BOTH_FAIL ni tekshir** — old-hemis dan formatni o'rganib test fayllarni tuzat
3. Maqsad: **MATCH + NEW_BETTER = 173/173** (SKIP=2 bundan tashqari)

### JSON natija fayli:
`/tmp/claude-0/-home-adm1n-startup/35402933-7d88-450d-917d-fdd64f23ba4e/scratchpad/compare_results.json`
