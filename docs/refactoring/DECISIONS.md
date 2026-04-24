# Classifier Refactor — Arxitektura qarorlari

**Sana:** 2026-04-21

## 1. Legacy CUBA entity'lar (hemishe_*) tegilmadi

**Qaror:** `domain/entity/` dagi `hemishe_*` jadvallariga bog'langan entity'lar (Student, Expel, REmployment, Verification, Teacher, Curriculum, UniversityDepartment, AdministrativeStudent*, ...) **hozircha qoldiriladi**.

**Sabab:**
- `rules.md`: "Eski CUBA jadvallar (`hemishe_*`) TEGILMAYDI"
- Bu jadvallar CUBA Platform infrastructure bilan tightly-coupled (create_ts/delete_ts/version audit, _gender/_nationality CUBA naming)
- 224 universitetda ishlayotgan univer (Yii2 PHP) bu jadvallarni hali to'g'ridan-to'g'ri o'qiydi

**Applies to:** 51 ta `extends BaseEntity` entity'lar + 3 ta Serializable entity (Speciality*) + R-table'lar (REmployment, RAcademicAttendance, ...)

## 2. Speciality* (Bachelor/Master/Doctoral/Ordinatura)

**Qaror:** 4 ta Speciality* entity **o'zgarishsiz qoladi**.

**Sabab:** Foydalanuvchi aniq aytdi: "bularni hali bir qarorga kelmadik". Ehtimol keyinchalik `h_speciality` + `h_speciality_academic_year` yangi struktura bilan almashtiriladi (2,760 ta mutaxassislik Excel'dan).

## 3. Data parity yangi jadvallarga (V009-V013)

**Status:** ✅ **TO'LIQ** — har bir yangi classifier migration'da `INSERT ... SELECT FROM hemishe_h_*` bajarilgan. Test DB restart'idan keyin yangi jadvallar avtomatik to'ldiriladi.

## 4. Dual-mapping FK pattern

**Qaror:** Yangi entity'lar'da classifier FK'lar ikki ko'rinishda:

```java
@Column(name = "gender_code", length = 2)
private String genderCode;  // write path, backward compat

@ManyToOne(fetch = LAZY)
@JoinColumn(name = "gender_code", referencedColumnName = "code",
            insertable = false, updatable = false)
private Gender gender;  // read path, FK-style access
```

**Sabab:**
- Mavjud kod (`setGenderCode(...)`) buzilmaydi
- FK navigatsiya (`employee.getGender().getName()`) qo'shimcha imkoniyat
- DB constraint saqlanadi
- N+1 query risk yo'q (LAZY)

**Applied:** Employee (6 FK), EmployeeJobs (4 FK)

## 5. ClassifierWebService — adaptive schema

**Qaror:** `SchemaInfo` helper eski va yangi jadval ustun nomlari farqini qo'llab-quvvatlaydi:
- `active` vs `is_active`
- `create_ts/update_ts` vs `created_at/updated_at`
- `delete_ts IS NULL` soft filter vs `is_active = false` soft disable

SQL aliasing orqali DTO (`ClassifierItemDto.active`, `createTs`, `updateTs`) o'zgarmaydi.

## 6. CUBA entity name preservation

`ClassifierLegacyService.getCubaEntityName()` — univer'ga yuboriladigan `_entityName` maydoni.

**Renamed jadvallar uchun eski entity nomi saqlanadi:**
- `hemis_version` → `hemishe_HHemisVersionType` (eski: hemis_version_type)
- `employee_rate` → `hemishe_HUniversityEmployeeRate`
- `contract_class` → `hemishe_HContractTypes`
- `certificate_grade/subject/name` → ...Grades/Subjects/Names (ko'plik)
- `outside_activity` → `...OutsideActivities`

**Sabab:** Univer CUBA Platform deserialization'da `_entityName` ni ishlatadi.

## 7. Integration-critical SQL (Bosqich 3+4)

Quyidagi fayllar yangi jadvallarga yo'naltirildi:
1. `ClassifierLegacyService.OLD_CLASSIFIER_MAP` (93 entry) — univer `/v2/services/classifiers/*` endpoint
2. `ClassifierMetadataRegistry` (96 entry) — web API `/api/v1/web/classifiers/*`
3. `ClassifierWebService` — web API CRUD (read + write)
4. `ClassifierLookupService` — in-memory cache (universities)
5. `HokimiyatClassifierService` — hokimiyat university JOIN
6. `MethodicalPublicationTypeRepository` — upsert/restore native query

## 8. Test DB assumption

**Qaror:** Migration fayllarini o'z joyida to'g'irlaymiz (V014, V021 yaratilmaydi), chunki test DB hali production'ga chiqmagan. Foydalanuvchi `./gradlew :domain:liquibaseUpdate` ni zarurat bo'lganida `:liquibaseDropAll` bilan qayta ishga tushiradi.

## Tegilmagan soha'lar (keyingi session)

- Student entity'lari (FK refactor, lekin hemishe_* tegilmaydi qoidasi ostida)
- UniversityCadastre (typeCode, kindCode — external cadastre classifier yaratish kerak)
- Speciality modulni qayta qurish (2,760 mutaxassislik Excel import)
- Integration test: auto_compare.js 98.4%+ verification
- Univer stage test (real sync)
