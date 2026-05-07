# Univer 224 OTM — API Contract (FROZEN)

> **CRITICAL:** Univer (224 universitetlarda deploy qilingan Yii2 PHP backend (per-OTM)) **67 ta endpoint** ishlatadi. Har bir o'zgarish 224 OTM uchun ta'sir qilishi mumkin.
>
> **Manbalar:**
> - `/home/adm1n/projects/startup/univer/common/components/hemis/` — sync klasslar + HemisApi
> - `/home/adm1n/projects/startup/univer/common/models/report/EGenericStat.php` — entity stats
> - `/home/adm1n/projects/startup/univer/api/config/main.php` — route aliases
>
> **Detal audit:** `docs/UNIVER_ENDPOINT_AUDIT.md`

## Endpoint Soni — Glossariy (chalkashishni oldini olish uchun)

`api-legacy` doirasida 3 xil son uchraydi — har biri boshqa narsani sanaydi:

| Son | Manba | Nimani sanaydi |
|-----|-------|----------------|
| **67** | Ushbu hujjat | Univer Yii2 (224 OTM) **haqiqatan chaqiradigan unique endpoint URL'lar** — frozen contract |
| **175** | `hemis-tools/.../compare_endpoints.js` | Side-by-side **integration test variantlari** (har endpoint uchun success/edge-case kombinatsiyalar) |
| **~659** | `grep -rn '@*Mapping' api-legacy/src/main/java/` | Modulda mavjud **Java method** `@*Mapping` bilan (admin/audit/eksperimental aralash) |

Munosabat: `67 ⊆ 659` (Univer ishlatmaydigan ko'p endpointlar bor) va `175` har 67'ni o'rtacha 2.6 marta sinaydi (success + auth fail + validation + boundary).

## Univer Auth Modeli

```php
// BaseApiUpdater.php:32
public static function getUniversity() {
    return EUniversity::findCurrentUniversity()->code;
}
```

- Har Univer instance o'z OTM `university_code`'i bilan ishlaydi
- JWT token Bearer auth (HemisApi.php:711)
- Cross-OTM scope access **qilmaydi** (har OTM Univer faqat o'z resourcesini chaqiradi)

## Real Inventory (deep search natijasi)

| Mezon | Soni |
|-------|------|
| **Unique endpoint URL** | **67** |
| **HTTP-chaqiruvchi class** | **35** (sync 32 + HemisApi + EGenericStat + main.php config) |
| Entity endpoint (CUBA `/entities/*`) | 33 |
| Service endpoint (`/services/*`) | 32 |
| OAuth endpoint | 2 |

## Endpoint Categories

### A. Entity Endpoints (33 ta) — CUBA `/entities/hemishe_*`

**Student domain (8 ta):**
- `hemishe_EStudent` — StudentUpdater
- `hemishe_EStudentDiploma` — StudentDiplomaUpdater
- `hemishe_EStudentCertificate` — ForeignCertificateUpdater
- `hemishe_EDoctorateStudent` — DoctorateStudentUpdater
- `hemishe_EDissertationDefense` — DissertationDefenseUpdater
- `hemishe_RIAdministrativeStudent2` — StudentExchangeUpdater
- `hemishe_RIAdministrativeStudent3` — StudentEmploymentUpdater
- `hemishe_RIAdministrativeStudent4` — StudentOlympiadUpdater
- `hemishe_RIAdministrativeStudentSport` — StudentSportUpdater

**Employee domain (5 ta):**
- `hemishe_ETeacher` — EmployeeUpdater
- `hemishe_EEmployeeJobs` — EmployeeMetaUpdater
- `hemishe_EEmpoyeeCertificate` (CUBA typo) — EmployeeForeignCertificateUpdater
- `hemishe_RIAdministrativeEmployee1/2/3` — EmployeeAcademicDegree/Training/ForeignUpdater

**Research/Project (8 ta):**
- `hemishe_EProject` — ProjectUpdater
- `hemishe_EProjectExecutor` — ProjectExecutorUpdater
- `hemishe_EProjectMeta` — ProjectMetaUpdater
- `hemishe_EPublicationAuthorMeta` — PublicationAuthorMetaUpdater
- `hemishe_EPublicationMethodical` — PublicationMethodicalUpdater
- `hemishe_EPublicationProperty` — PublicationPropertyUpdater
- `hemishe_EPublicationScientific` — PublicationScientificUpdater
- `hemishe_EResearchActivity` — ScientificPlatformProfileUpdater

**University/Reference (4 ta):**
- `hemishe_EUniversity` — UniversityUpdater
- `hemishe_EUniversityDepartment` — DepartmentUpdater (path = code String, NOT UUID)
- `hemishe_EUniversityGroup` — GroupUpdater
- `hemishe_EUniversitySpeciality` — SpecialtyUpdater

**Stats/Infrastructure (3 ta — EGenericStat.php):**
- `hemishe_RIctEquipment` — IT jihozlari hisoboti
- `hemishe_RLaboratories` — Laboratoriyalar
- `hemishe_REducationMaterials` — O'quv materiallari

### B. Service Endpoints (32 ta) — `/services/*`

**Authentication & Reference (5):**
- `oauth/token`, `oauth/exchange`
- `classifiers/info`, `classifiers/allItems`
- `captcha/getNumericCaptcha`

**Student Services (7):**
- `student/id`, `student/validate`, `student/update`, `student/gpa`
- `student/contractStatistics` (ReportContractUpdater)
- `doctoral-student/id`
- `legalentity/bankRequisites`

**Teacher Services (2):**
- `teacher/id`, `teacher/addJob`

**Passport/Identity (4):**
- `passport-data/getDataBySN`
- `passport-data/getDataByPinflBirthdate`
- `passport-data/getAddress`
- `send/verifyCode`

**External Government (5):**
- `bimm/academicDegree`, `bimm/certificate`, `bimm/provertyRegister`
- `social/singleRegister`, `social/women`

**Finance/Reports (5):**
- `billing/invoice`, `billing/scholarship`
- `uzasbo/scholarship`
- `employment/workbook`, `employment/graduateList`

**University/Reference (5):**
- `university/config`, `university/get`
- `group/get`, `speciality/get`
- `diplom-blank/get`, `diplom-blank/setStatus`

### C. OAuth (2 ta)

- `v2/oauth/token` — login (HemisApi.php:724 apiLogin function)
- `v2/oauth/exchange` — token exchange route alias (api/config/main.php:110)

## Frozen Properties

### MUST NOT change (klient buziladi)

1. **JSON field nomi** — har bir field aynan o'sha nom bilan
2. **JSON field tartibi** — `_entityName`, `_instanceName`, `id`, ... (CUBA convention)
3. **HTTP status kod** — 200/201/204/404/403 — klient response handling unga bog'liq
4. **Authentication header** — `Authorization: Bearer <token>` only
5. **Datetime format** — `yyyy-MM-dd'T'HH:mm:ss.SSS` (CUBA convention)
6. **FK serialization** — nested object `{id: ..., _entityName: ...}` (flat ID emas)
7. **Error response shape** — `{error: ..., details: ...}` (CUBA format)
8. **Pagination params** — `?offset=N&limit=M`, response `{data: [...], totalCount: N}`
9. **CUBA query params** — `dynamicAttributes`, `returnNulls`, `view`, `responseView`

### CAN change (klient sezishmaydi)

1. **Internal SQL** — query optimizatsiya, JOIN FETCH, indeks
2. **Cache strategy** — TTL, evict pattern, distributed lock
3. **Logging** — backend log'lar (PII mask majburiy)
4. **Audit** — `@Audited` annotation
5. **Cross-OTM scope 403** — Univer cross-OTM so'rov yubormaydi → ta'sir 0%
6. **Mass-assignment defense** — `body.remove("_university")` (Univer body'da yubormaydi)
7. **Soft delete** — `delete_ts` SET (Univer DELETE qilmaydi yoki `@SQLRestriction` filter qiladi)

## Regression Protection Strategy

### Real test infrastructure (mavjud)
- `/home/adm1n/projects/startup/hemis-tools/docs/univer_tool/`
- **175 ta integration test** (14 kategoriya: 00-auth → 13-entity-stats)
- `compare_endpoints.js` — old-hemis (:8082) vs hemis-back (:8081) side-by-side
- `integration-proxy.py` — DB bootstrap from `hemis_401`
- So'nggi run (`compare_endpoints.js`): **175/175 MATCH** (commit `ac409cf`, 2026-05). Avvalgi audit: 81 MATCH (V3.0 baseline) — 175 ta test scenarioga kengaytirilgan.

### Test ishga tushirish
```bash
# 1. hemis-back
cd /home/adm1n/projects/startup/hemis-back && ./gradlew :app:bootRun &

# 2. proxy
cd /home/adm1n/projects/startup/hemis-tools/docs/univer_tool
python3 integration-proxy.py http://localhost:8081 hemis_401 postgres postgres &

# 3. test ishlatish
node /home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js --json
```

### Smoke test (manual)
```bash
TOKEN=$(curl -s -X POST http://localhost:8081/app/rest/v2/oauth/token \
  -H 'Authorization: Basic Y2xpZW50OnNlY3JldA==' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&username=otm401&password=XCZDAb7qvGTXxz' | jq -r '.access_token')

curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8081/app/rest/v2/entities/hemishe_ETeacher/{uid}?view=eTeacher-view&returnNulls=true" \
  | jq '.' > snapshot-after.json

diff snapshot-before.json snapshot-after.json
```

## Univer Sync Class Index (35 caller)

### Sync papkasi (32)
DepartmentUpdater, DiplomaBlankUpdater, DissertationDefenseUpdater, DoctorateStudentUpdater,
EmployeeAcademicDegreeUpdater, EmployeeForeignCertificateUpdater, EmployeeForeignUpdater,
EmployeeMetaUpdater, EmployeeTrainingUpdater, EmployeeUpdater, ForeignCertificateUpdater,
GroupUpdater, ProjectExecutorUpdater, ProjectMetaUpdater, ProjectUpdater,
PublicationAuthorMetaUpdater, PublicationMethodicalUpdater, PublicationPropertyUpdater,
PublicationScientificUpdater, ReportContractUpdater, ReportEmploymentUpdater,
ScientificPlatformProfileUpdater, SpecialtyUpdater, StudentDataContractUpdater,
StudentDataStipendUpdater, StudentDataSyncUpdater, StudentDiplomaUpdater,
StudentEmploymentUpdater, StudentExchangeUpdater, StudentGpaUpdater, StudentOlympiadUpdater,
StudentSportUpdater, StudentUpdater, UniversityUpdater

### Sync papkasi tashqarisi (3)
- `common/components/hemis/HemisApi.php` — **15 endpoint** (asosiy klient class)
- `common/models/report/EGenericStat.php` — **3 endpoint** (Stats reports)
- `api/config/main.php` — **1 alias** (oauth/exchange routing)

### Inactive sync class'lar (HTTP qilmaydi)
BaseApiUpdater, GenericStatUpdater, StudentDebtUpdater, StudentScholarshipUpdater (model-only)

## Modification Workflow (har refactor uchun)

1. **Avval** verify: `grep -r "v2/entities/hemishe_E<Entity>\|v2/services/<endpoint>" /home/adm1n/projects/startup/univer --include="*.php"`
2. **Topilsa** — Univer ishlatadi, JSON shape o'zgartirmang
3. **Topilmasa** — Univer ishlatmaydi, refactor xavfsiz
4. **Har holda:** build + test → smoke test agar qulay bo'lsa
5. **hemis-tools** test'lar bilan regression check qilish (server'lar ishga tushgach)

## Audit History

- **2026-05-04** — Yuzaki audit: 18 endpoint topildi (`->method('` regex faqat)
- **2026-05-05** — 32 endpoint deb taxmin qilindi (sync class count)
- **2026-05-05 (chuqur)** — **67 endpoint topildi** (string + variable + cross-folder grep)
- **2026-05-05** — 10 ta endpoint o'tkazib yuborilgan edi:
  - 3 ta EGenericStat.php (RIctEquipment, RLaboratories, REducationMaterials)
  - 1 ta ReportContractUpdater (student/contractStatistics)
  - 1 ta ReportEmploymentUpdater (employment/graduateList)
  - 2 ta HemisApi.php classifiers (info, allItems)
  - 1 ta DiplomaBlankUpdater (setStatus)
  - 1 ta StudentDataSyncUpdater (billing/scholarship)
  - 1 ta config/main.php (oauth/exchange)

**Lesson:** Audit uchun `grep` regex yetarli emas — string-based + variable assignment + cross-folder kerak.
