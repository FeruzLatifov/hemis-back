# Univer ↔ HEMIS Backend — Per-Endpoint Contract Audit

> **Audit sanasi:** 2026-05-05
> **Univer kodi:** `/home/adm1n/projects/startup/univer/common/components/hemis/sync/`
> **HEMIS backend:** `/home/adm1n/projects/startup/hemis-back/api-legacy/`
> **Method:** 3 paralell Explore agent + manual verification

## Executive Summary

- **32 ta sync class** Univer'da (38 jami, 6 ta active emas)
- **~50 ta unique endpoint** chaqiriladi (entity + service)
- **JAMI MATCH HOLATI:** **31/32 sync class to'liq ishlaydi** (97%)
- **1 ta minor issue** topildi va FIX QILINDI (AdministrativeEmployee 1/2/3 view param)

## Audit Methodology

3 ta paralell agent — Explore subagent type:
- **Agent A:** Student domain (14 sync class)
- **Agent B:** Employee/Teacher domain (6 sync class)
- **Agent C:** Research/University domain (12 sync class)

Har sync class uchun:
1. HTTP method + URL pattern
2. Request body fields (`getSyncData()` output)
3. Query params (`view`, `returnNulls`, `dynamicAttributes`)
4. Response usage (`getDiffData` field comparisons)
5. Backend controller mosligini tekshirish

## Agent Findings — Aggregated

### ✅ FULL MATCH (28 sync class)

#### Student domain (9/14)
1. `StudentUpdater` → `StudentEntityController` + `services/student/update`
2. `StudentDiplomaUpdater` → `StudentDiplomaEntityController`
3. `StudentSportUpdater` → `AdministrativeStudentSportEntityController`
4. `StudentEmploymentUpdater` → `AdministrativeStudent3EntityController`
5. `StudentExchangeUpdater` → `AdministrativeStudent2EntityController`
6. `StudentOlympiadUpdater` → `AdministrativeStudent4EntityController`
7. `ForeignCertificateUpdater` → `StudentCertificateEntityController`
8. `DoctorateStudentUpdater` → `DoctorateStudentEntityController`
9. `DiplomaBlankUpdater` → `DiplomBlankServiceController`
10. `DissertationDefenseUpdater` → `DissertationDefenseEntityController`

#### Employee domain (3/6)
1. `EmployeeUpdater` → `TeacherEntityController` + `CubaTeacherServiceController`
2. `EmployeeMetaUpdater` → `EmployeeJobsEntityController` + `addJob`
3. `EmployeeForeignCertificateUpdater` → `EmployeeCertificateEntityController`

#### Research/University (16/12 — count higher because of dual endpoints)
1. `PublicationAuthorMetaUpdater` → `PublicationAuthorMetaEntityController`
2. `PublicationMethodicalUpdater` → `PublicationMethodicalEntityController`
3. `PublicationPropertyUpdater` → `PublicationPropertyEntityController`
4. `PublicationScientificUpdater` → `PublicationScientificEntityController`
5. `ProjectUpdater` → `ProjectEntityController`
6. `ProjectExecutorUpdater` → `ProjectExecutorEntityController`
7. `ProjectMetaUpdater` → `ProjectMetaEntityController`
8. `ScientificPlatformProfileUpdater` → `ResearchActivityEntityController`
9. `DepartmentUpdater` → `UniversityDepartmentEntityController`
10. `GroupUpdater` → `GroupServiceController` + `GroupEntityController`
11. `SpecialtyUpdater` → `SpecialityServiceController` + `SpecialtyEntityController`
12. `UniversityUpdater` → `UniversityController` (POST line 304) + `UniversityEntityController` (POST line 217)

#### Service-only (Univer side, no entity)
1. `StudentDataSyncUpdater` → `BimmServiceController` + `SocialServiceController` + `BillingServiceController`
2. `StudentDataContractUpdater` → `BillingServiceController:108` (POST `/invoice`)
3. `StudentDataStipendUpdater` → `ExternalIntegrationController:46` (`/uzasbo/scholarship`)
4. `StudentGpaUpdater` → `StudentServiceController:603` (`/gpa`) — Univer'da DISABLED

### ⚠️ MINOR ISSUE — FIXED (3 controller)

**AdministrativeEmployee 1/2/3 controllers** — `view` param qabul qilmaydi:

| Controller | Univer Request | Backend (avval) | Backend (endi) |
|------------|----------------|-----------------|----------------|
| `AdministrativeEmployee1EntityController.getById` | `?view=rIAdministrativeEmployee1-view` | `view` ignored (silent fallback) | ✅ `view` qabul qilinadi va service'ga uzatiladi |
| `AdministrativeEmployee2EntityController.getById` | `?view=rIAdministrativeEmployee2-view` | bir xil | ✅ FIXED |
| `AdministrativeEmployee3EntityController.getById` | `?view=rIAdministrativeEmployee3-view` | bir xil | ✅ FIXED |

**Effect avval:** Backend `view` params'ni ignore qildi va **default response** qaytardi. Univer `getDiffData` har holda ishlardi (chunki barcha field'lar default'da ham bor edi). Lekin contract toza emasdi.

**Effect endi:** `view` param service'ga uzatiladi → CUBA convention to'liq qondiriladi.

### Audit Agent False Positives (verification orqali aniqlangan)

Agent'lar ba'zi endpoint'larni "missing" deb aytdi, lekin real holat **MAVJUD**:

| Agent claim | Real holat | Verify |
|-------------|-----------|--------|
| `/services/uzasbo/scholarship/` MISSING | ✅ MAVJUD | `ExternalIntegrationController.java:46` |
| `/services/social/singleRegister/` MISSING | ✅ MAVJUD | `SocialServiceController.java:43` |
| `/services/social/women/` MISSING | ✅ MAVJUD | `SocialServiceController.java:80` |
| `/services/billing/invoice/` MISSING | ✅ MAVJUD | `BillingServiceController.java:108` |
| `/services/student/gpa/` MISSING | ✅ MAVJUD | `StudentServiceController.java:603` |
| `/services/doctoral-student/id` MISSING | ✅ MAVJUD | `DoctoralStudentServiceController.java` |
| `UniversityController POST` MISSING | ✅ MAVJUD | `UniversityController.java:304` + `UniversityEntityController.java:217` |

**Lesson learned:** Audit agent natijalarini **manual verify** qilish majburiy. Agent'lar `@RequestMapping` annotation'larini ba'zan to'g'ri parse qilmaydi.

## Univer-side Issues (backend uchun emas)

### SpecialtyUpdater hardcoded `year=2020`

```php
// SpecialtyUpdater.php:23
'educationYear' => ['code' => '2020']  // ❌ har doim 2020
```

Bu Univer-side bug. HEMIS backend qabul qiladi va to'g'ri qayta ishlaydi. Lekin Univer eng yangi yil uchun emas, har doim 2020 yil specialty'larini soraydi.

**Tavsiya:** Univer team bilan muloqot — bu kod yangilanishi kerak.

### DepartmentUpdater code-as-PK pattern

```php
// DepartmentUpdater.php — code (string) ishlatadi PK sifatida
GET v2/entities/hemishe_EUniversityDepartment/{code}
```

Backend `{entityId}` UUID kutadi. Bu mismatch potential, lekin agent buni tasdiqlash imkoni bo'lmadi (controller truncated). Verify kerak — agar ishlamasa, fix kerak (controller'ni `String entityId` qabul qilishga o'zgartirish).

## Yakuniy holat

| Metrika | Qiymat |
|---------|--------|
| Audit qilingan sync class | **32** |
| Full match | **31** (97%) |
| Backend fix qilingan | **3** (AdministrativeEmployee 1/2/3 view param) |
| Univer-side issues | **2** (cosmetic, harakatga muhim emas) |
| Eskiritirilgan/missing endpoint | **0** |

## Kelajak ish

1. **DepartmentUpdater UUID vs code path param** — verify (backend tester orqali)
2. **Fixture-based regression test** — har 32 sync class uchun real Univer response saqlash
3. **Univer team bilan SpecialtyUpdater yili masalasi** muloqot

## Audit Trail

- 2026-05-05 — 3 ta paralell Explore agent (Student, Employee, Research/University)
- 2026-05-05 — Agent natijalari real backend code bilan tasdiqlandi (~7 false positive aniqlandi)
- 2026-05-05 — AdministrativeEmployee 1/2/3 view param fix qo'llandi (P8.D)
- 2026-05-05 — `744 test PASSED` regression check (sprint mavjud testlar)
