# Univer 224 OTM — API Contract (FROZEN)

> **CRITICAL:** Univer (224 universitetlarda deploy qilingan Yii2 PHP frontend) shu **18 ta endpoint**ni ishlatadi. Har bir o'zgarish 224 OTM uchun ta'sir qilishi mumkin.
>
> **Manba:** `/home/adm1n/projects/startup/univer/common/components/hemis/`

## Univer Auth Modeli

```php
// BaseApiUpdater.php:32
public static function getUniversity() {
    return EUniversity::findCurrentUniversity()->code;
}
```

- Har Univer instance o'z OTM `university_code`'i bilan ishlaydi
- JWT token Bearer auth (HemisApi.php:711)
- Cross-tenant access **qilmaydi** (har OTM faqat o'z resourcesini chaqiradi)

## Endpoint Contract — 18 ta

### CUBA Entity API (4 ta entity, 12 ta operation)

| Endpoint | Methods | Univer client | Backend implementation |
|----------|---------|---------------|------------------------|
| `/app/rest/v2/entities/hemishe_EEmployeeJobs/{uid}` | GET, PUT, DELETE | `EmployeeUpdater.php` | `EmployeeJobsEntityController` |
| `/app/rest/v2/entities/hemishe_EStudentDiploma/{uid}` | GET | `StudentDiplomaUpdater.php` | `StudentDiplomaEntityController` |
| `/app/rest/v2/entities/hemishe_ETeacher/{uid}` | GET, PUT | `EmployeeUpdater.php` | `TeacherEntityController` |
| `/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{uid}` | GET, PUT | `EmployeeAcademicDegreeUpdater.php` | `AdministrativeEmployee1EntityController` |
| `/app/rest/v2/entities/hemishe_RIAdministrativeEmployee2/{uid}` | GET, PUT | `EmployeeTrainingUpdater.php` | `AdministrativeEmployee2EntityController` |
| `/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{uid}` | GET, PUT | `EmployeeForeignUpdater.php` | `AdministrativeEmployee3EntityController` |

**Query params (GET):**
- `dynamicAttributes=true` — CUBA dynamic attributes
- `returnNulls=true` — null fields ham qaytarish
- `view=eTeacher-view`, `view=eStudentDiploma-view` — CUBA view

**PUT params:** `?responseView=_local`

### Service Endpoint (16 ta)

| Endpoint | Univer purpose |
|----------|----------------|
| `GET /services/student/id` | Student PINFL → ID |
| `GET /services/student/validate` | Student validation |
| `POST /services/teacher/addJob` | Teacher job creation |
| `GET /services/teacher/id` | Teacher PINFL → ID |
| `GET /services/doctoral-student/id` | Doctoral student lookup |
| `GET /services/passport-data/getDataBySN` | Passport by Seria+Number |
| `GET /services/passport-data/getDataByPinflBirthdate` | Passport by PINFL+Birthdate |
| `GET /services/passport-data/getAddress` | Address data |
| `GET /services/university/config` | University configuration |
| `GET /services/university/get?code=` | University by code |
| `GET /services/bimm/academicDegree?pinfl=` | Academic degree from BIMM |
| `GET /services/bimm/certificate?pinfl=` | Certificate from BIMM |
| `GET /services/employment/workbook` | Workbook (mehnat daftarchasi) |
| `GET /services/legalentity/bankRequisites` | Bank requisites |
| `GET /services/captcha/getNumericCaptcha` | Captcha generation |
| `GET /services/send/verifyCode` | SMS verification code |

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

### CAN change (klient sezishmaydi)

1. **Internal SQL** — query optimizatsiya, JOIN FETCH, indeks
2. **Cache strategy** — TTL, evict pattern, distributed lock
3. **Logging** — backend log'lar (PII mask majburiy)
4. **Audit** — `@Audited` annotation
5. **Cross-tenant 403** — Univer cross-tenant qilmaydi → ta'sir 0%
6. **Mass-assignment defense** — `body.remove("_university")` (Univer body'da yubormaydi)
7. **Soft delete** — `delete_ts` SET (Univer DELETE qilmaydi yoki `@SQLRestriction` filter qiladi)

## Regression Protection Strategy

### 1. Mevjud testlar
- Build pipeline'da `744 test PASSED` (security 72 + service 525 + api-legacy 139 + api-external 8)
- Har commitdan oldin `./gradlew test` (TESTS_ENABLED=true)

### 2. Tavsiya etilgan kelajak ish
- **Fixture-based regression test** — har 18 endpoint uchun real Univer response saqlash
- `src/test/resources/legacy-fixtures/univer/<endpoint>.json`
- `JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT_ORDER)`

### 3. Smoke test workflow (manual)

```bash
# Bitta OTM test creds bilan
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8081/app/rest/v2/entities/hemishe_ETeacher/{uid}?view=eTeacher-view&returnNulls=true&dynamicAttributes=true" \
  | jq '.' > snapshot-after.json

# Diff snapshot-before.json bilan — har refactor oldin/keyin
diff snapshot-before.json snapshot-after.json
```

## Univer Sync Class Index

| Sync class | api-legacy endpoint | Notes |
|------------|---------------------|-------|
| `EmployeeUpdater.php` | `hemishe_ETeacher`, `hemishe_EEmployeeJobs` | Asosiy o'qituvchi sync |
| `EmployeeAcademicDegreeUpdater.php` | `hemishe_RIAdministrativeEmployee1` | PhD/DSc o'qituvchilar |
| `EmployeeTrainingUpdater.php` | `hemishe_RIAdministrativeEmployee2` | Malaka oshirish |
| `EmployeeForeignUpdater.php` | `hemishe_RIAdministrativeEmployee3` | Xorijiy professorlar |
| `StudentDiplomaUpdater.php` | `hemishe_EStudentDiploma` | Talaba diplomi |
| `EmployeeMetaUpdater.php` | (boshqa entitylar — verify) | Metadata sync |
| `DepartmentUpdater.php` | (boshqa entitylar — verify) | Bo'lim sync |
| `ProjectUpdater.php` | (boshqa entitylar — verify) | Loyiha sync |

## Modification Workflow (har refactor uchun)

1. **Avval** verify: `grep "hemishe_E<Entity>" /home/adm1n/projects/startup/univer/common/components/hemis -r`
2. **Topilsa** — Univer ishlatadi, JSON shape o'zgartirmang
3. **Topilmasa** — Univer ishlatmaydi, refactor xavfsiz
4. **Har holda:** build + test → smoke test agar qulay bo'lsa
5. **Commit message** — "no Univer contract impact" eslatma yozing

## Audit History

- **2026-05-04** — 18 endpoint aniqlandi, hujjatlandi (audit P7.3)
- **2026-05-04** — Cross-tenant BOLA fix (10+ controller/service) — Univer'ga 0 ta'sir verify
- **2026-05-04** — PII PINFL log mask (50+ joy) — backend log only, klient effect 0
- **2026-05-04** — Hard delete → soft delete (AcademicEntityLegacyService 7 ta) — Univer DELETE qilmaydi
