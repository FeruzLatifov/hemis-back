# api-legacy module — CUBA Platform Compatibility

> **MARKAZIY HEMIS-back ning CUBA Platform 7.3 mosligi qatlami.** Format buzilishi = production'da xato.
>
> **Mijozlari:** **224 ta OTM Univer Yii2 PHP backend** (per-OTM, har biri o'z `hemis_NNN` lokal DB'siga ega) — markaziy HEMIS-back'ga **network REST API** orqali ulanadi.
>
> **Qoida:** Format conversion only. Business logic NOL. 175/175 contract test (`compare_endpoints.js`).

---

## ⚠️ ENG MUHIM QOIDA — Old-hemis bilan 1:1 mos (BUZILMASDAN)

> **api-legacy modul — `/home/adm1n/projects/startup/old-hemis` (eski CUBA loyihasi) bilan AYNI XULQ ko'rsatishi kerak.**

### Solishtirish manbai

| Element | Manba | Maqsad |
|---------|-------|--------|
| Old-hemis kod | `/home/adm1n/projects/startup/old-hemis` | Reference implementation (8082 portda ishga tushadi) |
| Bizning api-legacy | `api-legacy/src/main/java/uz/hemis/api/legacy/...` | Yangi implementatsiya, 1:1 ayni xulq |
| Integration test | `/home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js` | 175 ta test, ikkala server'ni solishtiradi |

### 1:1 mosligi nimani anglatadi?

✅ **Bir xil bo'lishi shart:**
1. **HTTP status code** — old: 200 → biz: 200 (400 → 400)
2. **Response body shape** — field nomlari, tartibi, type'lari
3. **Validation behavior** — qaysi field'lar majburiy, qachon xato qaytaradi
4. **Error format** — `{error: ..., details: ...}` (CUBA convention)
5. **Permission model** — old-hemis (CUBA Java) OTM scope cheklov yo'q edi — markaziy server, har OTM admin barcha ma'lumotni ko'ra olardi → biz ham shu xulq saqlashimiz kerak (api-legacy faqat)
6. **Field default qiymatlari** — old yetishmagan field'larga default qo'yardi → biz ham qo'yishimiz kerak

❌ **O'zgartirib bo'lmaydi:**
1. URL pattern (`/app/rest/v2/...`)
2. JSON field nomlari va tartibi
3. CUBA `_entityName`, `_instanceName` field'lari
4. Datetime format
5. FK serialization (nested object)
6. Pagination (`offset`, `limit`, `data`, `totalCount`)

### Tekshirish jarayoni

Har bir o'zgarishdan keyin:
```bash
# 1. Old-hemis ishga tushadi (port 8082)
cd /home/adm1n/projects/startup/old-hemis && ./gradlew bootRun &

# 2. hemis-back ishga tushadi (port 8081)
cd /home/adm1n/projects/startup/hemis-back && ./gradlew :app:bootRun &

# 3. 175 ta integration test
cd /home/adm1n/projects/startup/hemis-tools/docs/univer_tool
node compare_endpoints.js

# 4. Maqsad: MATCH 175/175 (100%)
```

### Old-hemis kodga murojaat qilish (kelajakdagi shubhalar)

Agar yangi qaror qabul qilish kerak bo'lsa:
```bash
# Old-hemis'da qanday qilingan?
grep -rn "<keyword>" /home/adm1n/projects/startup/old-hemis/modules/

# Yoki:
find /home/adm1n/projects/startup/old-hemis -name "*Controller*.java" \
  -exec grep -l "<endpoint>" {} \;
```

**Qoida:** "Yangi yondashuv" o'rniga "old-hemis bilan ayni xulq" tanlanadi.

### Eng tez-tez xato qilinadigan farqlar

| Old-hemis xulqi | Bizning yangi xulq | Tuzatish |
|-----------------|--------------------|---------| 
| OTM scope cheklov yo'q (cross-OTM ruxsat) | Strict OTM scope isolation | `isAccessAllowed` → `return true`, TenantGuard chaqirish'larni komment qilish |
| Strict validation yo'q (default qo'yardi) | Strict validation (xato qaytaradi) | DTO `validate()` metodida default qo'yish |
| Permission check yo'q | `@PreAuthorize("hasAuthority('...')")` | `@PreAuthorize("isAuthenticated()")` |
| Yangi schema bilmasdan eski jadval'ga yozardi | Yangi schema'ga yozadi | Yangi `Legacy*` entity yaratish, eski jadvalga map |

---

## 🔒 GOLDEN RULE — Faqat eski jadvallar (`hemishe_*`, `sec_*`)

**api-legacy controller'lar HECH QACHON yangi schema'ga (yangi jadvallar) yozmasligi kerak.**

### Modul ↔ Jadval mosligi

| Modul | Vazifa | Jadvallar | Misol entity |
|-------|--------|-----------|--------------|
| **api-legacy** | Univer (Yii2 PHP, 224 OTM) endpoint'larini xizmat qilish, **eski hemis behavior 1:1** | `hemishe_e_*`, `hemishe_h_*`, `hemishe_r_*`, `sec_user`, `sec_role`, `sec_permission` | `Student` (`hemishe_e_student`), `Teacher` (`hemishe_e_teacher`), `SecUser` (`sec_user`) |
| **api-university** | Vazirlik markaz ↔ 224 OTM Univer integratsiya kanali (yangi format) — yangi schema | Yangi `employee_job`, `users`, `university_building`, `h_*` | `User`, `Employee`, `EmployeeJob`, `UniversityBuilding` |
| **api-web** | Modern web frontend | Yangi schema | Bir xil yangi entity'lar |
| **api-external** | S2S integratsiya (vazirlik, MyGov) | Vaziyatga qarab | (alohida) |

### 📌 Asosiy qoida (kechirim qilmasdan)

> **api-legacy = eski jadvallar (mavjud xulq, mantiq o'zgarmasin).**
> **Yangi jadvallar uchun api-university (yoki yangi modul) yangi endpoint chiqarib, mapping/sync bilan ko'chiramiz.**

### Misol — to'g'ri yondashuv

```
1. Univer Yii2 (per-OTM, lokal hemis_337/hemis_401/... PHP DB) → markaziy hemis-back ga POST /app/rest/v2/entities/hemishe_EEmployeeJobs
   → api-legacy controller markaziy DB eski jadvalga yozadi:  INSERT INTO hemishe_e_employee_jobs
   → ✅ TO'G'RI — Univer keyingi GET shu jadvaldan o'qiydi (175/175 contract MATCH)

2. Modern web frontend (vazirlik admin) → POST /api/v1/web/employee-jobs
   → api-web controller yangi jadvalga yozadi:  INSERT INTO employee_job
   → ✅ TO'G'RI — yangi schema, modern auth

3. Yangi 224 OTM Univer (yangi format, OAuth client_credentials) → POST /api/v1/university/{code}/employee-jobs
   → api-university controller yangi jadvalga yozadi
   → Optional sync: yangi jadvaldan eski jadvalga ko'chirish (alohida service, async)
```

### ❌ XATO yondashuv (mavjud bug)

```
Univer (per-OTM Yii2 PHP) → POST /app/rest/v2/entities/hemishe_EEmployeeJobs
   → api-legacy markaziy DB YANGI jadvalga yozadi:  INSERT INTO employee_job
   → ❌ Split-brain: Univer keyingi GET hemishe_e_employee_jobs'dan o'qisa — bo'sh
   → "Yangi xodim qo'shildi, lekin Univer UI'da ko'rinmaydi"
```

### Entity nomlanish konventsiyasi (BUZILMASDAN)

| Vaziyat | Eski jadval (api-legacy) | Yangi jadval (api-web/university) |
|---------|-------------------------|----------------------------------|
| **Konflikt yo'q** — bir jadval = bir entity | Prefiks-siz: `Student` (`hemishe_e_student`), `Teacher` (`hemishe_e_teacher`), `Faculty` (`hemishe_e_faculty`) | (yangi schema yo'q) |
| **Konflikt bor** — ikkala schema kerak (eski Univer + yangi modern) | **`Legacy*` prefiksi**: `LegacyEmployeeJobs` (`hemishe_e_employee_jobs`), `SecUser` (`sec_user`) | **Prefiks-siz**: `EmployeeJobs` (`employee_job`), `User` (`users`) |
| **Kelajak** — barcha eski entity'larni `Legacy*` ga rename | (refactor kerak) | (refactor kerak) |

#### Qoidalar:

1. **api-legacy controller'lar HECH QACHON yangi schema entity'ni import qilmasligi kerak** (auto-check)
2. Yangi schema'da entity yaratish kerak bo'lsa, **avval konflikt tekshiruvi**:
   - Eski jadval bormi (`hemishe_*`)? → eski uchun `Legacy*` entity yaratiladi
   - Yangi jadval yaratiladi → modern nomli entity (prefiks-siz)
3. **Tarixiy istisno (hozirgi loyiha):** 60+ eski entity prefiks-siz nom bilan (`Student`, `Teacher`). Bularni hozirda rename qilmaymiz, lekin yangi konflikt'larda `Legacy*` ishlatamiz.

### Hozirgi xatolar (rejalashtirilgan refactor — [ADR-0008](../docs/adr/0008-api-legacy-entity-rebinding.md))

`api-legacy` modul'da 3 ta entity yangi schema'ga noto'g'ri map qilingan. Tuzatish reja va sprint
bosqichlari uchun **ADR-0008** o'qing.

| Xato entity | Qaysi jadvalga map | api-legacy ishlatadigan controller | Kerak entity |
|-------------|--------------------|-----------------------------------|--------------|
| `User` | `users` (yangi) | `LegacyUserInfoController`, `UserController`, `EmployeeJobsEntityController`, `LegacySecurityHelper` | `SecUser` (`sec_user`) — mavjud, almashtirish kerak |
| `Employee` | `employee` (yangi) | `EmployeeJobsEntityController` | `Teacher` (`hemishe_e_teacher`) yoki yangi `LegacyEmployee` |
| `EmployeeJobs` | `employee_job` (yangi) | `EmployeeJobsEntityController` | **`LegacyEmployeeJobs` (`hemishe_e_employee_jobs`) — 2026-05-06'da yaratildi** |

### Service-layer komponentlar — bir xil konventsiya

Service, validator, loader, repository class'lar ham **eski jadvalga ulansa `Legacy*` prefiks bilan** bo'ladi.

#### Konventsiya (BUZILMASDAN)

| Komponent turi | Eski jadval (api-legacy uchun) | Yangi jadval (api-web uchun) |
|----------------|-------------------------------|------------------------------|
| **Entity** | `Legacy*` prefiks: `LegacyEmployeeJobs` (`hemishe_e_employee_jobs`) | Prefiks-siz: `EmployeeJobs` (`employee_job`) |
| **Repository** | `Legacy*` prefiks: `LegacyEmployeeJobsRepository` | Prefiks-siz: `EmployeeJobsRepository` |
| **Service** | `Legacy*` prefiks: `LegacyContractStatisticsService`, `LegacyOtmIntegrationService`, `LegacyBimmTokenService` | Prefiks-siz: `DashboardService` (api-web chaqirsa, eski ma'lumot o'qiydi — saqlanadi) |
| **Validator** | `Legacy*` prefiks: `LegacyCitizenshipValidator` | Prefiks-siz: `CitizenshipValidator` (kelajakda yangi `citizenship` jadval bilan) |
| **Loader** | `Legacy*` prefiks: `LegacyClassifierReferenceLoader` | Prefiks-siz |
| **Adapter / Helper** | `Legacy*` prefiks: `LegacyEntityAdapter`, `LegacyResponseHelper`, `LegacySecurityHelper` | Prefiks-siz |
| **Tarixiy istisno** | `SecUser`, `SecUserRepository` (CUBA `Sec*` konventsiyasi) | — |
| **Suffix istisno** (eski paketda) | `*LegacyService`: `EmployeeJobsLegacyService`, `ClassifierLegacyService`, `DiplomaLegacyService` (`service/legacy/` paketda) | — |

#### Qaror algoritmi (yangi class yaratayotganda)

```
1. Class qaysi jadval bilan ishlaydi?
   ├─ Eski (hemishe_*, sec_*): "Legacy" prefiks/suffix kerak
   │   ├─ Yangi class — `Legacy*` prefiks (LegacyXxx)
   │   ├─ `service/legacy/` paketda — `*LegacyService` suffix (XxxLegacyService)
   │   └─ Tarixiy `SecUser` — saqlanadi (rename xavfli)
   └─ Yangi (users, employee, h_*, ...): prefiks-siz
       └─ Misol: `User`, `Employee`, `EmployeeJobs`, `HBuildingCategory`

2. Class qaysi modul tomonidan chaqiriladi?
   ├─ Faqat api-legacy → `Legacy*` prefiks majburiy
   ├─ Faqat api-web/university → prefiks-siz
   └─ Ikkalasi → har ikki versiya kerak (LegacyXxx + Xxx)
```

#### Audit skripti

`./scripts/check_table_mappings.sh` har commit oldidan ishlatiladi (`.git/hooks/pre-commit`):
- Yangi schema entity import topsa — REJECT
- `@Table(name="hemishe_*")` qo'shilsa — JPA mapping vs DB introspection
- Mavjud xatolarni hisobotda ko'rsatadi

Tafsilot: `scripts/check_table_mappings.sh` va `scripts/git-hooks-pre-commit`.

### Split-brain biznes sababi

224 ta Univer Yii2 PHP client (per-OTM, lokal `hemis_NNN` DB bilan) eski hemis-back CUBA Java endpoint kontraktini ishlatadi — `hemishe_e_*` shape kutadi. **Aralashtirilsa:** api-legacy POST markaziy DB'ning yangi jadvaliga yozadi → Univer GET eski jadvaldan o'qiydi → topmaydi → "Yangi xodim qo'shildi, lekin ko'rinmaydi" bug.

To'liq tushuntirish: [`.claude/UNIVER_INTEGRATION.md`](../.claude/UNIVER_INTEGRATION.md)

### Yangi controller checklist

- [ ] Entity `@Table(name = "hemishe_*"|"sec_*")` ga map qilingan
- [ ] Repository eski jadvalga yozadi
- [ ] Service'da `tenantGuard.verifyOwnership*` yo'q (Univer cross-OTM scope)
- [ ] `@PreAuthorize` faqat `isAuthenticated()` yoki `permitAll()`

### Forbidden imports (pre-commit hook reject)

```java
// ❌ ADR-0008 violation — pre-commit hook reject qiladi
import uz.hemis.domain.entity.security.User;
import uz.hemis.domain.entity.employee.Employee;
import uz.hemis.domain.entity.employee.EmployeeJobs;

// ✅ Legacy variant
import uz.hemis.domain.entity.security.SecUser;       // sec_user
import uz.hemis.domain.entity.legacy.LegacyEmployeeJobs;  // hemishe_e_employee_jobs
// Employee o'rniga: Teacher (hemishe_e_teacher)
```

**Mavjud 3 buzilgan import** (ADR-0008): `LegacySecurityHelper`, `UserController`, `LegacyUserInfoController`, `EmployeeJobsEntityController`. Tuzatish: ADR-0008 Stages 2-5.

---

## Eng Kritik 10 ta CUBA Qoidasi

> **Batafsil misollar va kod bloklari:** [`.claude/ENDPOINT_PORTING_GUIDE.md`](../.claude/ENDPOINT_PORTING_GUIDE.md)

| # | Qoida | Pattern | Diqqat |
|---|-------|---------|--------|
| 1 | **`LinkedHashMap` (HashMap EMAS)** | `Map<String,Object> m = new LinkedHashMap<>();` | HashMap order'ni yo'qotadi → CUBA klient parser breakdown |
| 2 | **`@JsonPropertyOrder` har DTO'da** | `@JsonPropertyOrder({"_entityName", "_instanceName", "id", ...})` | Tartib old-hemis bilan AYNAN bir xil bo'lishi shart |
| 3 | **`_entityName` + `_instanceName` MAJBURIY** | `m.put("_entityName", "hemishe$Student"); m.put("_instanceName", ...)` | `$` (dollar) separator — CUBA convention |
| 4 | **FK = nested object (flat string EMAS)** | `{"_employee": {"id": "uuid"}}` (NOT `{"_employee": "uuid"}`) | UUID FK → `{id: ...}`, code FK → `{code: ...}` |
| 5 | **Datetime CUBA format** | `@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")` | UTC saqlash, klient timezone'ni o'zi konvert qiladi |
| 6 | **Soft delete `delete_ts IS NULL`** | `@SQLRestriction("delete_ts IS NULL")` entity'da | Native query'da manual `WHERE delete_ts IS NULL` |
| 7 | **Business logic TAQIQ** | Controller faqat format konversiya. `studentService.create(...)` shared with api-web | api-legacy = thin adapter |
| 8 | **URL pattern `/app/rest/v2/*`** | `@RequestMapping("/app/rest/v2") @PostMapping("/entities/hemishe$Student")` | `$` Spring `@PathVariable` da escape kerak emas |
| 9 | **Error CUBA format** | `{error, details, exception}` (NOT `{success: false, error: {...}}`) | Alohida `@RestControllerAdvice(basePackages = "uz.hemis.api.legacy")` |
| 10 | **Pagination CUBA** | `?offset=0&limit=20` + `{data: [...], totalCount: N}` | Spring `Pageable.of(offset/limit, limit)` manual conversion |

**261 ta controller `toMap()` patterni ishlatadi — MapStruct YO'Q api-legacy'da.**

---

## Porting Workflow (Old-Hemis → api-legacy)

### Trigger formatlari

```
PORT: GET /services/tax/rent
PORT: POST /entities/hemishe$Student
```

### Avtomatik qadamlar (qisqa ko'rinish — to'liq spec: `@.claude/ENDPOINT_PORTING_GUIDE.md`)

1. **Trigger parse** — `PORT: <METHOD> <PATH>` dan ajratish
2. **Duplicate check** — `grep -rn` mavjud controller'larni tekshirish
3. **Old-hemis live response** — :8082 dan curl + `legacy-fixtures/<name>.json` saqlash
4. **Metadata** — `old_hemis.md` (tag/desc) + `rest-services.xml` (params)
5. **Controller + DTO** — `toMap()` + `LinkedHashMap` patterni (MapStruct ishlatilmaydi). Service api-web bilan SHARED.
6. **Test va diff** — `JSONAssert.STRICT_ORDER` + live `diff old.json new.json` 100% MATCH bo'lishi shart
7. **`endpoint_tester.html`** — `endpoints/XX-*.js` ga test button (faqat test green bo'lsa)
8. **Univer kontrakt** — `node compare_endpoints.js` 175/175 (regress yo'qligini tasdiqlash)

**Canonical workflow:** `@.claude/ENDPOINT_PORTING_GUIDE.md` (ushbu fayldagi qadamlar uning qisqartmasi).

---

## CUBA Format Validation

**Pattern:** old-hemis dan real response → `legacy-fixtures/<endpoint>.json` saqla → `JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT_ORDER)`.

**STRICT_ORDER** — field tartib MAJBURIY.

To'liq misol va workflow: [`.claude/ENDPOINT_PORTING_GUIDE.md`](../.claude/ENDPOINT_PORTING_GUIDE.md).

---

## PR Checklist (api-legacy)

- [ ] Response: `LinkedHashMap` (HashMap emas)
- [ ] DTO'da `@JsonPropertyOrder` to'liq
- [ ] `_entityName`, `_instanceName` har response'da
- [ ] FK nested object format (`{"id": "...", "_entityName": "..."}`)
- [ ] Datetime: CUBA pattern (`yyyy-MM-dd'T'HH:mm:ss.SSS`)
- [ ] Business logic: NOL (faqat format conversion)
- [ ] Service: api-web bilan **bir xil** service ishlatiladi
- [ ] URL: `/app/rest/v2/*` pattern
- [ ] Error: CUBA format (`error`, `details`, `exception`)
- [ ] Test fixture: real old-hemis response bilan diff
- [ ] Swagger annotations (`@Tag`, `@Operation`, `@ApiResponses`)
- [ ] `endpoint_tester.html` ga test button qo'shilgan

---

## Forbidden patterns

```java
// ✗ HashMap response
return new HashMap<>();

// ✗ Yangi business logic
public StudentLegacyDto create(StudentLegacyDto dto) {
    // PINFL check, faculty validation — TAQIQ
    return ...;
}

// ✗ api-web service'ni override
@Override
public StudentDto findById(Long id) { ... }  // FORK - taqiq

// ✗ Modern error format
throw new ApiException(ErrorCode.NOT_FOUND, "...");
// Legacy → CUBA format kerak
```

---

## See Also
- `../.claude/ENDPOINT_PORTING_GUIDE.md` — Porting workflow
- `../.claude/context.md` — CUBA legacy schema
- `../service/CLAUDE.md` — Shared business logic
