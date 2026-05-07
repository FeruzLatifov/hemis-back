# Endpoint Porting Guide

> Old-hemis REST endpointlarni `api-legacy` modulga ko'chirish

---

## Asosiy Qoidalar

1. **100% Backward Compatibility** — Old-hemis qabul qilgan format = yangi hemis qabul qilishi kerak. Response format, field nomlari, ketma-ketlik — hammasi bir xil
2. **Avval old-hemis dan test** — Controller yozishdan OLDIN old-hemis response olish va formati saqlash
3. **Old-hemis da ishlamagan = PORT qilmaslik** — HTTP 500/404 qaytarsa, ko'chirish kerak emas
4. **Swagger da real ma'lumot qo'ymaslik** — PINFL: `12345678901234`, UUID: `00000000-...`, Username: `username`
5. **Faqat `api-legacy` modul** — `/app/rest/v2/*` endpointlar. `api-web` ga tegmaslik

---

## Serverlar

| Tizim | URL | Port | User | University |
|-------|-----|------|------|------------|
| Old-Hemis (CUBA) | `http://localhost:8082` | 8082 | `otm351` | 351 |
| Yangi Hemis (Spring Boot) | `http://localhost:8081` | 8081 | `otm401` | 401 |

**Muhim fayllar:**
- `/home/adm1n/projects/startup/hemis-tools/docs/old_hemis.md` — Old API endpoint katalogi (workflow + tag mapping)
- `/home/adm1n/projects/startup/old-hemis/modules/portal/web/rest-services.xml` — CUBA service params
- `/home/adm1n/projects/startup/hemis-tools/docs/endpoint_tool/endpoint_tester.html` — Test UI (3 tugma: Yangi/Eski/Ikkalasi)
- `/home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js` — 175 ta side-by-side integration test

---

## Porting Trigger Formatlari

```
# Variant 1: PORT prefix (tavsiya)
PORT: GET /services/tax/rent

# Variant 2: URL pattern
GET /app/rest/v2/services/tax/rent

# Variant 3: Batch
PORT:
GET /services/bimm/disabilityCheck
GET /services/bimm/certificate
GET /services/bimm/academicDegree
```

**Porting EMAS:** code review, bug fix, refactoring, schema o'zgartirish, swagger yaxshilash — bular oddiy development.

---

## Porting Workflow

```
1. USER ENDPOINT BERADI
   PORT: GET /app/rest/v2/entities/hemishe_EStudent
        │
        ▼
2. DUPLICATE CHECK
   Mavjud controllerlarni tekshirish → topilsa xabar berish
        │
        ▼
3. OLD-HEMIS DAN RESPONSE OLISH (KRITIK!)
   curl http://localhost:8082/.../endpoint -H "Authorization: Bearer $TOKEN"
   → Response formatini saqlash va tahlil qilish
   → Agar xato qaytarsa → PORT qilmaslik!
        │
        ▼
4. METADATA EXTRACTION
   - old_hemis.md: tag, nom, description, URL→tag mapping
   - rest-services.xml: parametrlar, method
        │
        ▼
5. CONTROLLER GENERATION (old-hemis formatiga mos!)
   - Java controller + toMap() (LinkedHashMap)
   - Swagger annotations (o'zbek tilida)
   - @Transactional annotatsiyalar
        │
        ▼
6. TEST & SOLISHTIRISH
   diff old_response.json new_response.json
   → 100% mos → davom et
   → Farq bor → 5-ga qaytib tuzatish
        │
        ▼
7. ENDPOINT_TESTER.HTML GA QO'SHISH
   (faqat testlar muvaffaqiyatli bo'lsa!)
   3 tugma: Yangi Hemis | Old Hemis | Ikkalasini Ham
        │
        ▼
8. MIGRATION HISOBOT
   Controller path, Swagger tag, test natijalari
```

### Metadata Topish Holatlari

| Holat | old_hemis.md | rest-services.xml | Harakat |
|-------|----------------|-------------------|---------|
| Normal | Bor | Bor | Avtomatik tag topiladi |
| Hujjatsiz | Yo'q | Bor | User TAG bersa ishlatiladi, aks holda URL dan taxmin |
| Yo'q | Yo'q | Yo'q | Mavjud endpointlar ro'yxati ko'rsatiladi |
| Yangi | - | - | User TAG + DESCRIPTION berishi kerak |

---

## URL → Tag Mapping

| URL Pattern | Swagger Tag |
|-------------|-------------|
| `/oauth/token/*` | `01.Token` |
| `/services/passport-data/*`, `/services/personal-data/*` | `02.Passport ma'lumotlari` |
| `/services/bimm/*` | `03.BIMM` |
| `/services/tax/*` | `04.Soliq` |
| `/services/social/*` | `05.Ijtimoiy himoya` |
| `/services/student/*` | `06.Talaba` |
| `/services/teacher/*` | `07.O'qituvchi` |
| `/services/scholarship/*` | `08.Stipendiya` |
| `/services/billing/*` | `09.Billing` |
| `/services/captcha/*` | `10.Captcha` |
| `/services/university/*` | `11.OTM` |
| `/services/group/*` | `12.Guruhlar` |
| `/services/speciality/*` | `13.Mutaxassisliklar` |
| `/services/faculty/*` | `14.Fakultetlar` |
| `/services/diploma/*` | `15.Diplomlar` |
| `/services/transcript/*` | `16.Transkript` |
| `/services/classifiers/*` | `17.Klassifikatorlar` |
| `/services/translate/*` | `18.Tarjima` |
| `/services/mail/*` | `19.Mail` |
| `/services/contract/*` | `20.Contract` |
| `/services/employment/*` | `21.Bandlik statistikasi` |
| `/services/mandat/*` | `22.DTM` |
| `/services/oak/*` | `23.OAK` |
| `/services/uzasbo/*` | `24.UzASBO` |

Tag format: Legacy API — `XX.Nom` (probelsiz: `02.Passport ma'lumotlari`). Modern API — `Legacy Entity APIs - Student`.

---

## Response Format Qoidalari

### LinkedHashMap (field ketma-ketlik)

```java
// TO'G'RI: LinkedHashMap ketma-ketlikni saqlaydi
private Map<String, Object> toMap(Entity entity) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("_entityName", ENTITY_NAME);
    map.put("_instanceName", entity.getName());
    map.put("id", entity.getCode());
    map.put("code", entity.getCode());
    // Faqat old-hemis qaytargan maydonlar, aynan shu tartibda!
    return map;
}

// NOTO'G'RI: HashMap ketma-ketlikni saqlamaydi!
Map<String, Object> result = new HashMap<>();
```

### @JsonPropertyOrder (DTO uchun)

```java
@Data
@JsonPropertyOrder({
    "_entityName", "id", "isGraduate", "country", "educationType",
    "groupId", "language", "socialCategory"
    // old-hemis tartibida
})
public class StudentLegacyDto {
    @JsonProperty("_entityName")
    private String entityName = "hemishe_EStudent";
    // ...
}
```

Ichki DTO lar ham o'z `@JsonPropertyOrder` ga ega bo'lishi kerak.

---

## CUBA Foreign Key Format

**Qattiq qoida:** Foreign key faqat CUBA nested object formatda qabul qilinadi. Flat string QABUL QILINMAYDI!

```json
// TO'G'RI
{ "_employee": {"id": "6b3c0dfc-e269-3df5-894e-85b8c2386e9d"} }
{ "_university": {"code": "401"} }

// NOTO'G'RI — qabul qilinmaydi!
{ "_employee": "6b3c0dfc-e269-3df5-894e-85b8c2386e9d" }
```

### extractUuid / extractStringId

```java
// TO'G'RI: Faqat Map qabul qiladi, flat string → null
@SuppressWarnings("unchecked")
private UUID extractUuid(Object value) {
    if (value == null) return null;
    if (value instanceof UUID) return (UUID) value;
    if (value instanceof Map) {
        Map<String, Object> nested = (Map<String, Object>) value;
        Object id = nested.get("id");
        if (id instanceof String str && !str.isEmpty()) {
            try { return UUID.fromString(str); }
            catch (IllegalArgumentException e) { return null; }
        }
    }
    return null;  // Flat string uchun null
}

// TO'G'RI: Faqat Map qabul qiladi
private String extractStringId(Object value) {
    if (value == null) return null;
    if (value instanceof Map) {
        Map<String, Object> nested = (Map<String, Object>) value;
        Object id = nested.get("id");
        return id != null ? id.toString() : null;
    }
    return null;  // Flat string uchun null
}
```

| Maydon turi | CUBA format | Key |
|-------------|-------------|-----|
| UUID (employee, student) | `{"id": "uuid-string"}` | `id` |
| Code (university, department) | `{"code": "value"}` | `code` |

---

## University Filtering

### Entity vs Classifier

| Endpoint turi | Filter kerak? | Sabab |
|---------------|---------------|-------|
| Entity (EEmployeeJobs, EStudent) | HA | OTM ga tegishli ma'lumot |
| Classifier (HTeacherPositionType) | YO'Q | Barcha OTM uchun bir xil |
| Service (create/update) | HA | Yangi yozuv yaratadi |

### Implementatsiya

```java
@GetMapping
@Transactional(readOnly = true)
public ResponseEntity<List<Map<String, Object>>> getAll(...) {
    String universityCode = authFacade.getCurrentUser().getUniversity().getCode();
    Page<EmployeeJobs> page = repository.findByUniversityCode(universityCode, pageable);
    return ResponseEntity.ok(toMapList(page.getContent()));
}

@PostMapping
@Transactional
public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
    String universityCode = authFacade.getCurrentUser().getUniversity().getCode();
    entity.setUniversity(universityCode);  // Avtomatik set
    // ...
}
```

University filter qo'shish backward-compatible — foydalanuvchi o'z OTM ma'lumotlarini so'raganda xuddi old-hemis kabi javob oladi.

---

## Master/Replica Routing

| HTTP Method | Annotatsiya | Database |
|-------------|-------------|----------|
| GET | `@Transactional(readOnly = true)` | Replica |
| POST (search) | `@Transactional(readOnly = true)` | Replica |
| POST (create) | `@Transactional` | Master |
| PUT, PATCH, DELETE | `@Transactional` | Master |

```java
// TO'G'RI
@GetMapping("/{entityId}")
@Transactional(readOnly = true)  // Replica
public ResponseEntity<?> getById(...) { }

@PostMapping
@Transactional  // Master
public ResponseEntity<?> create(...) { }

// NOTO'G'RI — @Transactional yo'q = default Master ga ketadi (keraksiz yuk)
// NOTO'G'RI — GET uchun readOnly=true yo'q = Master ishlatiladi
// NOTO'G'RI — POST create uchun readOnly=true = SQLException!
```

---

## Swagger Namuna

```java
@Tag(
    name = "02.Passport ma'lumotlari",  // Probelsiz!
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

## Endpoint Tester — Modular Fayl Strukturasi

```
docs/
├── endpoint_tester.html          # Asosiy UI
└── endpoints/
    ├── 01-token.js               # Kategoriya fayllari
    ├── 02-captcha.js
    ├── ...
    ├── _index.js                 # Barcha kategoriyalarni birlashtiradi
    └── all-endpoints.js          # Backup/source
```

### Kategoriya Fayl Formati

```javascript
// XX-kategoriya-nomi.js
const endpoints_XX = [
    {
        id: 1,  // Kategoriya ichida 1 dan boshlanadi
        category: "XX.Kategoriya nomi",
        name: "Endpoint nomi",
        method: "GET",
        url: "/app/rest/v2/services/...",
        requiresAuth: true,
        dependsOn: 1,  // Token endpoint
        inputFields: { ... },
        description: "...",
        ported: true
    }
];
if (typeof module !== 'undefined' && module.exports) {
    module.exports = endpoints_XX;
}
```

### Mavjud Kategoriyaga Qo'shish

1. `docs/endpoints/XX-kategoriya.js` faylida `endpoints_XX` arrayga yangi endpoint qo'shish
2. `_index.js` ni yangilash shart emas — u avtomatik `...endpoints_XX` ishlatadi

### Yangi Kategoriya Yaratish

1. `docs/endpoints/39-yangi-kategoriya.js` yaratish
2. `_index.js` ga `...endpoints_39` qo'shish
3. `endpoint_tester.html` ga `<script src="endpoints/39-yangi-kategoriya.js">` qo'shish

| Qoida | Format |
|-------|--------|
| Fayl nomi | `XX-kategoriya-nomi.js` (kebab-case) |
| O'zgaruvchi | `endpoints_XX` (underscore) |
| Category field | `"XX.Kategoriya nomi"` |
| Script tag tartibi | Raqam bo'yicha o'sib boruvchi, `_index.js` oxirida |

### Config Panel

Ikki tizim uchun alohida default qiymatlar:

```javascript
inputFields: {
    pinfl: {
        label: "PINFL",
        default: "",              // Bo'sh — configdan olinadi
        useConfigPinfl: true,     // newPinfl / oldPinfl
        required: true
    },
    seriaNumber: {
        default: "",
        useConfigSerial: true,    // newSerial / oldSerial
    },
    birthdate: {
        default: "",
        useConfigBirthdate: true, // newBirthdate / oldBirthdate
    },
    entityId: {
        default: "",
        useConfigStudentId: true, // newStudentId / oldStudentId
    }
}
```

Endpoint `default` qiymat = Yangi Hemis (otm401) ma'lumoti. `placeholder` da ikkala variant: `"Yangi:401-102-08, Eski:351-118"`.

### CUBA Format (endpoint_tester.html)

```javascript
// TO'G'RI
body: {
    "_university": {"code": "{_university}"},
    "_employee": {"id": "{_employee}"}
}

// NOTO'G'RI
body: {
    "_university": "{_university}",  // flat string!
}
```

---

## Test Helper Script

```bash
#!/bin/bash
# test_endpoint_comparison.sh
OLD_BASE="http://localhost:8082"
NEW_BASE="http://localhost:8081"

OLD_TOKEN=$(curl -s -X POST "$OLD_BASE/app/rest/v2/oauth/token" \
  -u "client:secret" -d "grant_type=password&username=otm351&password=XCZDAb7qvGTXxz" \
  | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

NEW_TOKEN=$(curl -s -X POST "$NEW_BASE/app/rest/v2/oauth/token" \
  -u "client:secret" -d "grant_type=password&username=otm401&password=XCZDAb7qvGTXxz" \
  | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

ENDPOINT="$1"
curl -s "$OLD_BASE$ENDPOINT" -H "Authorization: Bearer $OLD_TOKEN" | jq '.' > /tmp/old_response.json
curl -s "$NEW_BASE$ENDPOINT" -H "Authorization: Bearer $NEW_TOKEN" | jq '.' > /tmp/new_response.json

if diff /tmp/old_response.json /tmp/new_response.json > /dev/null; then
  echo "Responses are 100% identical!"
else
  echo "Differences found:"
  diff /tmp/old_response.json /tmp/new_response.json
fi
```

---

## Migration Checklist

### Bajarilgan

- **01.Token** — 3 endpoint (password grant, refresh grant, user info)
- **02.Passport ma'lumotlari** — 7 endpoint (getData, getDataBySN, getAddress, etc.)
- **endpoint_tester.html** — Dual config, side-by-side response, auto-comparison

### Har Bir Yangi Port Uchun

- [ ] Old-hemis dan response olindi va format tahlil qilindi
- [ ] Controller `api-legacy` modulda yaratildi
- [ ] toMap() old-hemis formatiga 100% mos (LinkedHashMap)
- [ ] @JsonPropertyOrder old-hemis field tartibida
- [ ] Foreign key = CUBA nested object format
- [ ] @Transactional (GET: readOnly=true, POST create: default)
- [ ] University filter (entity uchun), classifier uchun yo'q
- [ ] Swagger annotations (o'zbek tilida, tag probelsiz)
- [ ] Old vs New response diff = farq yo'q
- [ ] endpoint_tester.html ga qo'shildi (requiresAuth: true, dependsOn: 1)
- [ ] Config flaglar to'g'ri (useConfigPinfl, useConfigSerial, etc.)
