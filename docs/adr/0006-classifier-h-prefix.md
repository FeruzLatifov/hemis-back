---
id: ADR-0006
status: implemented
date: 2026-05-04
deciders: hemis-team
agent: claude-code
model: claude-opus-4-7
affects: [domain, service]
liquibase:
  - V003_create_positions.sql
  - V004_create_employee.sql
  - V011_create_university_buildings.sql
  - S008_seed_activity_statuses.sql
entities: [HPositionType, HPosition, HBuildingCategory, HConstructionMaterial, HRoofType]
verification: |
  grep -rn "@Table(name = \"h_" domain/src/main/java/uz/hemis/domain/entity/ | wc -l  # 5+ bo'lishi kerak
related: [ADR-0001]
---

# ADR 0006: Klassifikator jadvallariga `h_*` prefiks konventsiyasi

## Status

Accepted (Sana: 2026-05-04)

## Context

**Markaziy HEMIS-back** klassifikatorlarni 230 ta OTM uchun **yagona manba** sifatida saqlaydi va distribute qiladi (markaz → OTM push). Per-OTM Univer Yii2 PHP stack'lar (224 ta) bu klassifikatorlardan markazdan sync qiladi — har OTM bir xil qiymatlarni biladi (gender, soato, position_type, va h.k.).

Eski CUBA ekosistemida klassifikator jadvallar `h_*` prefiks bilan
nomlangan: `hemishe_h_gender`, `hemishe_h_soato`, `hemishe_h_academic_degree` va
shu kabi. Bu konventsiya:

- **Sync skripti uchun yagona standart** — markazdan OTM ga klassifikator sync qiluvchi vositalar prefiks orqali entity'ni klassifikatordan ajratadi (push direction: markaz → 230 OTM).
- **Entity discovery uchun ravshanlik** — yangi developer schema ko'rganda
  `h_*` jadvallarini darhol klassifikator deb taniydi.
- **Markaziy boshqaruv:** vazirlik adminlari markaziy `h_*` jadvallarini yangilaydi → Univer'lar (per-OTM) keyingi sync'da yangi qiymatni oladi.

Yangi 5 ta klassifikator (`position_type`, `position`, `building_category`,
`construction_material`, `roof_type`) hozircha prefisksiz yaratilgan edi.
Bu nomenklatura ekosistem bilan mos emas va kelajakdagi sync vositalarini
murakkablashtiradi.

DB hali production'ga deploy qilinmagan — V003 va V011 migration'larni
bevosita yangilash xavfsiz.

## Decision

5 ta klassifikator butun stack bo'ylab `h_*`/`H` prefiks oladi:

| Eski (DB / Java) | Yangi (DB / Java) |
|------------------|-------------------|
| `position_type` / `PositionType` | `h_position_type` / `HPositionType` |
| `position` / `Position` | `h_position` / `HPosition` |
| `building_category` / `BuildingCategory` | `h_building_category` / `HBuildingCategory` |
| `construction_material` / `ConstructionMaterial` | `h_construction_material` / `HConstructionMaterial` |
| `roof_type` / `RoofType` | `h_roof_type` / `HRoofType` |

**Mezon — qaysi yangi klassifikator `h_*` oladi:**

1. **FK reference target** — boshqa entity'lar tomonidan FK target sifatida ishlatilsa
2. **Ekosistem sync mantiqiy** — universitet bazasi (hemis_NNN) bilan
   sync ma'noli (klassifikator hayot kechirgich biz tomonimizdan boshqariladi,
   lekin universitetlar shu qiymatlarni biladi)
3. **Refdata semantikasi** — code-based PK (`code VARCHAR`), kam o'zgaradigan
   stable enumeration (e.g., gender, nationality, position_type)

Uchchala mezon **AND** mantiqida — barchasi bajarilishi shart.

### Mezon BAJARILMAGAN holatlar (`h_` prefiks YO'Q)

Shu mezonni bajarmaydigan jadvallar prefiks-siz yoziladi:

| Jadval | Sabab |
|--------|-------|
| `role` (V001) | RBAC biznes domeni — Univer ekosistemi bilan sync emas (har stack o'zining roli) |
| `permission` (V002) | RBAC biznes domeni — server-spetsifik, kod-da hardcoded ham bo'ladi |
| `users` (V006) | HUMAN actor jadvali — refdata emas, oqim ma'lumoti |
| `language` (V013) | i18n config — texnik, refdata semantikasi yo'q |
| `menu` (V014) | UI struktura — refdata emas, hierarchical config |
| `oauth_client` (V006) | Auth principal — server-spetsifik machine identity |
| `university_lifecycle` (V009) | Event log — append-only operational jadval |

### Tezkor qaror chizig'i

Yangi jadval qachon `h_*` oladi?
1. ❓ Boshqa entity FK target sifatida ko'rsatadimi? Yo'q → `h_` YO'Q
2. ❓ 224 OTM Univer bazasida shu nomli jadval bor (`hemishe_h_*`)mi yoki sync mantiqiymi? Yo'q → `h_` YO'Q
3. ❓ Code-based PK (`code VARCHAR`) bilan stable enumeration'mi? Yo'q → `h_` YO'Q

Uchovi **HA** — `h_` qo'sh. Birortasi **YO'Q** — prefiks-siz.

## Alternatives Considered

### Alternative 1: Faqat DB darajasida `h_*` qo'shish (Java klass nomi qoldirish)

- **Tasvir:** `@Table(name = "h_position")` lekin `class Position`. Spring
  Boot odatiy konventsiyasi (`@Table(name = "users") class User`).
- **Afzalligi:** Java refactor scope kichik (faqat 5 ta `@Table` annotation),
  domain modeli ravshan (`Position` semantik nomi).
- **Kamchiligi:** Domain layer'da klass nomi DB jadval bilan vizual mos emas
  (`Position` ↔ `h_position`). Yangi developer kontekst topishda chalkashishi
  mumkin.
- **Rad etish sababi:** Foydalanuvchi to'liq prefiks konvensiyasi tanladi —
  butun stack'da `h_*`/`H` izchillik.

### Alternative 2: Yangi V015 migration yaratish (V003/V011 saqlash)

- **Tasvir:** `RENAME TABLE position_type TO h_position_type;` ALTER
  statementlari V015 migration'ida.
- **Afzalligi:** Liquibase changeset id integrity saqlanadi. Production'da
  deploy qilingan bo'lsa, RENAME yagona safe yo'l.
- **Kamchiligi:** DB hali production'ga deploy qilinmagan — V015 keraksiz
  qatlam, V003/V011 oldingi deploy'lardan beri allaqachon ko'p marta
  o'zgartirilgan.
- **Rad etish sababi:** Avvalgi `users` jadval cleanup (V006 commit
  `2264867`) bilan bir xil pattern — production'ga chiqarilmagan migration
  bevosita yangilanadi.

## Consequences

### Positive

- **Ekosistem moslashish** — `hemis_NNN` bazalari bilan sync skripti yagona
  konvensiya asosida ishlaydi.
- **Future-proofing** — yangi klassifikator qo'shilganda qaror chizig'i
  ravshan: FK target + ekosistem sync mezoni → `h_*`.
- **Domain ↔ DB ravshanlik** — `HPosition` ↔ `h_position` ko'z bilan
  ko'rinadigan moslik.

### Negative

- **`hemishe_h_*` (CUBA frozen) bilan nomenklatura shovqini** —
  `hemishe_h_gender` (eski CUBA legacy) va `h_position` (yangi clean) bir
  bazada yashaydi. Developer'larga ikki konvensiya farqini bilish kerak:
  - `hemishe_*` — eski CUBA jadvallar (FROZEN, hech narsa o'zgartirilmaydi)
  - `h_*` — yangi clean klassifikatorlar
- **Java import'lar massiv update** — refactoring bir martalik xarajat
  (5 entity + 3 repository + 2 field type + 4 orphan import = 14 fayl).
- **`ClassifierLegacyService` legacy mappinglar** — eski `hemishe_h_*` ga
  havolalar saqlanadi (forward-compat). Yangi clean tomon bilan
  aralashtirilmaydi.

### Risks

- **Risk:** Native query'da eski jadval nomi qoldirib ketish.
  **Mitigation:** `grep -rn "FROM position\|FROM building_category\|..." --include="*.java"` butun loyiha bo'yicha tekshirildi (5 ta joy topildi va yangilandi).

- **Risk:** Test fayllarida hardcoded SQL string'lar.
  **Mitigation:** Test fayllarida hardcoded references topilmadi. JPA repository orqali ishlaydi (entity klass nomi rename'ga ergashadi).

- **Risk:** Spring Data repository auto-discovery noto'g'ri ishlashi.
  **Mitigation:** `@Repository` annotatsiya saqlanadi, Spring Data klass
  nomidan derive qiladi. `./gradlew clean compileJava` BUILD SUCCESSFUL.

## Implementation

1. **SQL migrations** (V003, V004, V011, S008):
   - `CREATE TABLE position_type` → `h_position_type`
   - `CREATE TABLE position` → `h_position`
   - `CREATE TABLE building_category` → `h_building_category`
   - `CREATE TABLE construction_material` → `h_construction_material`
   - `CREATE TABLE roof_type` → `h_roof_type`
   - REFERENCES, INDEX, INSERT, COMMENT — yangi nom
   - Rollback fayllar yangilandi

2. **Java entity rename** (5 fayl):
   - `PositionType.java` → `HPositionType.java`
   - `Position.java` → `HPosition.java`
   - `BuildingCategory.java` → `HBuildingCategory.java`
   - `ConstructionMaterial.java` → `HConstructionMaterial.java`
   - `RoofType.java` → `HRoofType.java`
   - File rename + class rename + `@Table(name = "h_*")` annotation

3. **Java repository rename** (3 fayl):
   - `BuildingCategoryRepository.java` → `HBuildingCategoryRepository.java`
   - `ConstructionMaterialRepository.java` → `HConstructionMaterialRepository.java`
   - `RoofTypeRepository.java` → `HRoofTypeRepository.java`
   - `PositionRepository`/`PositionTypeRepository` mavjud emas (Spring Data hech qachon yaratmagan)

4. **Field type reference** (2 fayl):
   - `EmployeeJobs.java`: `Position position` → `HPosition position`,
     `PositionType positionType` → `HPositionType positionType`
   - `UniversityBuilding.java`: `BuildingCategory category` → `HBuildingCategory`,
     `ConstructionMaterial constructionMaterial` → `HConstructionMaterial`,
     `RoofType roofType` → `HRoofType`

5. **Native query** (3 fayl, 5 joy):
   - `TeacherService.java`, `UniversityInfoService.java` (2 joy),
     `UniversityOfficialService.java` (2 joy) — `FROM position` → `FROM h_position`

6. **Orphan import tozalash** (4 fayl):
   - `User.java`, `SecUser.java`, `DoctoralStudent.java`, `Language.java` —
     ishlatilmagan `import uz.hemis.domain.entity.employee.Position;` o'chirildi

7. **Hujjat (docx)** Versiya 3.1 → 3.2:
   - 5 jadval va klass nomlari yangilandi
   - "h_* tavsiya etilgan" → "APPLIED — ADR-0006"

## References

- **Code (SQL):**
  - `domain/src/main/resources/db/changelog/changesets/schema/V003_create_positions.sql`
  - `domain/src/main/resources/db/changelog/changesets/schema/V004_create_employee.sql`
  - `domain/src/main/resources/db/changelog/changesets/schema/V011_create_university_buildings.sql`
  - `domain/src/main/resources/db/changelog/changesets/seed/S008_seed_activity_statuses.sql`

- **Code (Java entities):**
  - `domain/src/main/java/uz/hemis/domain/entity/employee/HPositionType.java`
  - `domain/src/main/java/uz/hemis/domain/entity/employee/HPosition.java`
  - `domain/src/main/java/uz/hemis/domain/entity/infrastructure/HBuildingCategory.java`
  - `domain/src/main/java/uz/hemis/domain/entity/infrastructure/HConstructionMaterial.java`
  - `domain/src/main/java/uz/hemis/domain/entity/infrastructure/HRoofType.java`

- **Documentation:**
  - `docs/db-analysis/HEMIS_DB_Jadvallar_Tahlili.docx` (Versiya 3.2)

- **Related ADRs:**
  - ADR-0001 (university_building alohida jadval)
  - ADR-0002 (Java 25 LTS)
