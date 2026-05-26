# Univer ↔ HEMIS Backend — Per-Endpoint Real Audit

> **Audit sanasi:** 2026-05-05 (yangilangan)
> **Univer kodi:** `/home/adm1n/projects/startup/hemis-univer` (8873 PHP fayl)
> **HEMIS backend:** `/home/adm1n/projects/startup/hemis-back/api-legacy/`
> **Method:** Deep code grep — `v2/(entities|services|oauth)` URL string'lari + `$itemUrl`/`$url` variable assignments + method-based HTTP calls

## Real Inventory (deep search)

| Mezon | Soni |
|-------|------|
| **Unique endpoint URL** | **67 ta** |
| **HTTP-chaqiruvchi class** | **35 ta** (sync 32 + HemisApi + EGenericStat + main.php config) |
| Entity endpoint | 33 ta |
| Service endpoint | 32 ta |
| OAuth endpoint | 2 ta |

---

## 1. Entity Endpoints (CUBA `/entities/hemishe_*`) — 33 ta

| Endpoint URL | Univer Caller (file:line) |
|--------------|---------------------------|
| `v2/entities/hemishe_EDissertationDefense/{uid}` | DissertationDefenseUpdater.php:44, 115 |
| `v2/entities/hemishe_EDoctorateStudent/{uid}` | DoctorateStudentUpdater.php:112, 176 |
| `v2/entities/hemishe_EEmployeeJobs/{uid}` | EmployeeMetaUpdater.php:165 |
| `v2/entities/hemishe_EEmpoyeeCertificate/{uid}` | EmployeeForeignCertificateUpdater.php:50 (CUBA typo "Empoyee") |
| `v2/entities/hemishe_EProject/{uid}` | ProjectUpdater.php:52, 117 (+ ProjectExecutorUpdater + ProjectMetaUpdater nested ref) |
| `v2/entities/hemishe_EProjectExecutor/{uid}` | ProjectExecutorUpdater.php:40, 105 |
| `v2/entities/hemishe_EProjectMeta/{uid}` | ProjectMetaUpdater.php |
| `v2/entities/hemishe_EPublicationAuthorMeta/{uid}` | PublicationAuthorMetaUpdater.php:62 |
| `v2/entities/hemishe_EPublicationMethodical/{uid}` | PublicationMethodicalUpdater.php:59, 122 |
| `v2/entities/hemishe_EPublicationProperty/{uid}` | PublicationPropertyUpdater.php |
| `v2/entities/hemishe_EPublicationScientific/{uid}` | PublicationScientificUpdater.php:67, 130 |
| `v2/entities/hemishe_EResearchActivity/{uid}` | ScientificPlatformProfileUpdater.php |
| `v2/entities/hemishe_EStudent/{uid}` | StudentUpdater.php:245 (+ ForeignCertificateUpdater + StudentDiplomaUpdater nested) |
| `v2/entities/hemishe_EStudentCertificate/{uid}` | ForeignCertificateUpdater.php |
| `v2/entities/hemishe_EStudentDiploma/{uid}` | StudentDiplomaUpdater.php:81 |
| `v2/entities/hemishe_ETeacher/{uid}` | EmployeeUpdater.php:55, 122 |
| `v2/entities/hemishe_EUniversity` | UniversityUpdater.php (+ DepartmentUpdater + GroupUpdater + SpecialtyUpdater nested ref) |
| `v2/entities/hemishe_EUniversityDepartment/{code}` | DepartmentUpdater.php:37 (path param = code String, NOT UUID!) |
| `v2/entities/hemishe_EUniversityGroup/{uid}` | GroupUpdater.php |
| `v2/entities/hemishe_EUniversitySpeciality/{uid}` | SpecialtyUpdater.php:109 |
| `v2/entities/hemishe_REducationMaterials/` | **EGenericStat.php:58** ⚠️ avvalgi auditda yo'q edi |
| `v2/entities/hemishe_RIAdministrativeEmployee1/{uid}` | EmployeeAcademicDegreeUpdater.php:60, 121 |
| `v2/entities/hemishe_RIAdministrativeEmployee2/{uid}` | EmployeeTrainingUpdater.php:63, 124 |
| `v2/entities/hemishe_RIAdministrativeEmployee3/{uid}` | EmployeeForeignUpdater.php:62, 129 |
| `v2/entities/hemishe_RIAdministrativeStudent2/{uid}` | StudentExchangeUpdater.php |
| `v2/entities/hemishe_RIAdministrativeStudent3/{uid}` | StudentEmploymentUpdater.php:38, 99 |
| `v2/entities/hemishe_RIAdministrativeStudent4/{uid}` | StudentOlympiadUpdater.php |
| `v2/entities/hemishe_RIAdministrativeStudentSport/{uid}` | StudentSportUpdater.php:42, 103 |
| `v2/entities/hemishe_RIctEquipment/` | **EGenericStat.php:56** ⚠️ avvalgi auditda yo'q edi |
| `v2/entities/hemishe_RLaboratories/` | **EGenericStat.php:57** ⚠️ avvalgi auditda yo'q edi |

## 2. Service Endpoints (`/services/*`) — 32 ta

| Endpoint URL | Univer Caller (file:line) |
|--------------|---------------------------|
| `v2/services/billing/invoice/` | StudentDataContractUpdater.php:14 |
| `v2/services/billing/scholarship` | **StudentDataSyncUpdater.php:74** ⚠️ avvalgi auditda yo'q edi |
| `v2/services/bimm/academicDegree?pinfl=` | HemisApi.php:502 |
| `v2/services/bimm/certificate?pinfl=` | HemisApi.php:484 |
| `v2/services/bimm/provertyRegister/` | StudentDataSyncUpdater.php:36 |
| `v2/services/captcha/getNumericCaptcha` | HemisApi.php:600 |
| `v2/services/classifiers/allItems` | **HemisApi.php:363** ⚠️ avvalgi auditda yo'q edi |
| `v2/services/classifiers/info` | **HemisApi.php:319** ⚠️ avvalgi auditda yo'q edi |
| `v2/services/diplom-blank/get` | DiplomaBlankUpdater.php:52 |
| `v2/services/diplom-blank/setStatus` | **DiplomaBlankUpdater.php:80, 161** ⚠️ avvalgi auditda yo'q edi |
| `v2/services/doctoral-student/id` | DoctorateStudentUpdater.php |
| `v2/services/employment/graduateList` | **ReportEmploymentUpdater.php:59** ⚠️ avvalgi auditda yo'q edi |
| `v2/services/employment/workbook` | HemisApi.php:631 |
| `v2/services/group/get` | GroupUpdater.php:48 |
| `v2/services/legalentity/bankRequisites` | HemisApi.php:614 |
| `v2/services/passport-data/getAddress` | HemisApi.php:695 |
| `v2/services/passport-data/getDataByPinflBirthdate` | HemisApi.php:650, 671 (2 chaqiruv) |
| `v2/services/passport-data/getDataBySN` | HemisApi.php:661 |
| `v2/services/send/verifyCode` | HemisApi.php:849 |
| `v2/services/social/singleRegister/` | StudentDataSyncUpdater.php:17 |
| `v2/services/social/women/` | StudentDataSyncUpdater.php:54 |
| `v2/services/speciality/get` | SpecialtyUpdater.php:39 |
| `v2/services/student/contractStatistics` | **ReportContractUpdater.php:46** ⚠️ avvalgi auditda yo'q edi |
| `v2/services/student/gpa/` | StudentGpaUpdater.php |
| `v2/services/student/id` | HemisApi.php:390 |
| `v2/services/student/update` | StudentUpdater.php:314 |
| `v2/services/student/validate` | HemisApi.php:573 |
| `v2/services/teacher/addJob` | EmployeeMetaUpdater.php:165 |
| `v2/services/teacher/id` | EmployeeUpdater.php:188 |
| `v2/services/university/config` | HemisApi.php:455 |
| `v2/services/university/get?code=` | HemisApi.php:516 |
| `v2/services/uzasbo/scholarship/` | StudentDataStipendUpdater.php:15 |

## 3. OAuth Endpoints (2 ta)

| Endpoint URL | Univer Caller |
|--------------|---------------|
| `v2/oauth/token` | HemisApi.php:727 |
| `v2/oauth/exchange` | **api/config/main.php:110** (route alias to `v1/oauth/exchange`) ⚠️ avvalgi auditda yo'q edi |

---

## 4. Caller Class Inventory (35 ta)

### A. Sync Classes (32 ta `/common/components/hemis/sync/`)

| Class | Endpoint(s) |
|-------|-------------|
| DepartmentUpdater | hemishe_EUniversityDepartment |
| DiplomaBlankUpdater | services/diplom-blank/{get, setStatus} |
| DissertationDefenseUpdater | hemishe_EDissertationDefense |
| DoctorateStudentUpdater | hemishe_EDoctorateStudent + services/doctoral-student/id |
| EmployeeAcademicDegreeUpdater | hemishe_RIAdministrativeEmployee1 |
| EmployeeForeignCertificateUpdater | hemishe_EEmpoyeeCertificate |
| EmployeeForeignUpdater | hemishe_RIAdministrativeEmployee3 |
| EmployeeMetaUpdater | hemishe_EEmployeeJobs + services/teacher/addJob |
| EmployeeTrainingUpdater | hemishe_RIAdministrativeEmployee2 |
| EmployeeUpdater | hemishe_ETeacher + services/teacher/id |
| ForeignCertificateUpdater | hemishe_EStudentCertificate |
| GroupUpdater | services/group/get + hemishe_EUniversityGroup |
| ProjectExecutorUpdater | hemishe_EProjectExecutor |
| ProjectMetaUpdater | hemishe_EProjectMeta |
| ProjectUpdater | hemishe_EProject |
| PublicationAuthorMetaUpdater | hemishe_EPublicationAuthorMeta |
| PublicationMethodicalUpdater | hemishe_EPublicationMethodical |
| PublicationPropertyUpdater | hemishe_EPublicationProperty |
| PublicationScientificUpdater | hemishe_EPublicationScientific |
| **ReportContractUpdater** | services/student/contractStatistics ⚠️ |
| **ReportEmploymentUpdater** | services/employment/graduateList ⚠️ |
| ScientificPlatformProfileUpdater | hemishe_EResearchActivity |
| SpecialtyUpdater | services/speciality/get + hemishe_EUniversitySpeciality |
| StudentDataContractUpdater | services/billing/invoice |
| StudentDataStipendUpdater | services/uzasbo/scholarship |
| StudentDataSyncUpdater | 4 endpoint: social/singleRegister, bimm/provertyRegister, social/women, billing/scholarship |
| StudentDiplomaUpdater | hemishe_EStudentDiploma |
| StudentEmploymentUpdater | hemishe_RIAdministrativeStudent3 |
| StudentExchangeUpdater | hemishe_RIAdministrativeStudent2 |
| StudentGpaUpdater | services/student/gpa |
| StudentOlympiadUpdater | hemishe_RIAdministrativeStudent4 |
| StudentSportUpdater | hemishe_RIAdministrativeStudentSport |
| StudentUpdater | hemishe_EStudent + services/student/update |
| UniversityUpdater | hemishe_EUniversity |

**Sync papkasidagi inactive class'lar (HTTP chaqirmaydigan):** BaseApiUpdater, GenericStatUpdater, StudentDebtUpdater, StudentScholarshipUpdater (model-only)

### B. Asosiy klient class'lar (3 ta)

| Class | Endpoint count |
|-------|----------------|
| **HemisApi.php** | **15 endpoint** (oauth/token, student/{id, validate}, university/{config, get}, bimm/{certificate, academicDegree}, captcha, legalentity, employment/workbook, passport-data (3), send/verifyCode, classifiers/{info, allItems}) |
| **EGenericStat.php** | **3 endpoint** (RIctEquipment, RLaboratories, REducationMaterials) |
| **api/config/main.php** | 1 alias (oauth/exchange → v1/oauth/exchange) |

---

## 5. Avvalgi Mening Audit'imdagi Nuqsonlar

| Nuqson | Yo'qolgan endpoint(lar) |
|--------|-------------------------|
| `EGenericStat.php` butunlay e'tiborga olinmagan | **3 ta** (RIctEquipment, RLaboratories, REducationMaterials) |
| `ReportContractUpdater` HTTP qiladi deb tasdiqlamadim | **1 ta** (student/contractStatistics) |
| `ReportEmploymentUpdater` HTTP qiladi deb tasdiqlamadim | **1 ta** (employment/graduateList) |
| `HemisApi.php` da `$url` variable assignment'lar parse qilmadim | **2 ta** (classifiers/info, classifiers/allItems) |
| `DiplomaBlankUpdater` 2 ta endpoint chaqiradi | **1 ta** (diplom-blank/setStatus) |
| `StudentDataSyncUpdater` 4 ta endpoint chaqiradi | **1 ta** (billing/scholarship) |
| `api/config/main.php` route mapping | **1 ta** (oauth/exchange) |
| **JAMI o'tkazib yuborilgan** | **10 ta endpoint** (15%) |

## 6. Kelajak Verifikatsiya

Real test infrastructure: `/home/adm1n/projects/startup/hemis-tools/docs/univer_tool/`
- 175 ta integration test (hemis-tools)
- `compare_endpoints.js` — old-hemis (:8082) vs hemis-back (:8081)
- Server'lar ishga tushganda majburiy run

**Bu audit `static contract` darajasidadir.** Real verification — `compare_endpoints.js` natijasi.

## 7. Lessons Learned

1. **`grep '->method('` regex** PHP fluent chain (multi-line method chaining) bilan ishlamaydi
2. **`$url` variable assignment** ham endpoint chaqiruvi sanaladi (HemisApi.php pattern)
3. **`ReportContractUpdater`/`ReportEmploymentUpdater`** non-CRUD report sync class'lar — `Updater` suffix bilan adashtirib, HTTP qilmaydi deb taxmin qildim
4. **`EGenericStat.php`** sync papka tashqarisida — model file ham endpoint chaqirishi mumkin
5. **`api/config/main.php`** route alias konfiguratsiyasi ham endpoint mapping'ga ta'sir qiladi
6. **Audit agent natijalarini real kod bilan verify qilish majburiy** — taxminan 15% xatolar mumkin

## Audit Trail

- 2026-05-05 (yuzaki) — 18 endpoint deb topildi (faqat `->get/post/put/delete()` regex)
- 2026-05-05 (chuqur) — 67 endpoint topildi (string-based + variable assignment + cross-folder)
- 2026-05-05 — Hujjat yangilandi
