---
id: ADR-0001
status: implemented
date: 2026-04-21
deciders: hemis-team
agent: human
affects: [domain, api-web, api-university]
liquibase:
  - V011_create_university_buildings.sql
entities: [UniversityBuilding, BuildingLifecycle, HBuildingCategory, HConstructionMaterial, HRoofType]
verification: ./gradlew :domain:liquibaseStatus
related: [ADR-0006]
---

# ADR 0001: University Buildings — Alohida jadval + Lifecycle log + Coordinates NULLABLE

## Status

Accepted (2026-04-21)

**Deciders:** hemis-team
**Kontekst:** OTM bino va inshootlari ma'lumotlarini saqlash talabi

> **Eslatma:** Bu ADR uchta o'zaro bog'liq sub-qarorni o'z ichiga oladi:
> - 1A: Alohida `university_building` jadval (Cadastre'ni kengaytirish emas) — quyida
> - 1B: `building_lifecycle` immutable event log — quyida
> - 1C: `latitude`/`longitude` NULLABLE pair-CHECK bilan — quyida
>
> Sub-qarorlar bitta domen (university buildings) ichida — kelajakda alohida bo'lishlari mumkin (ADR-0001a, ADR-0001b, ADR-0001c).

---

## 1A. Alohida `university_building` jadval (Cadastre'ni kengaytirish emas)

---

## Kontekst

`/home/adm1n/projects/startup/docs/Бино ва иншоотлар жадвали.xlsx` Excel template 224 OTM uchun yuborilgan — 14 ustun bino ma'lumoti (kategoriya, qurilish yili, maydon, material, tom, rekonstruksiya, koordinatalar, kadastr raqami).

Loyihada allaqachon `university_cadastre` jadvali (V012) mavjud — Kadastr API (172.18.9.171/kadastr/) dan sync qilinadigan **huquqiy** ma'lumot (kadastr raqami, yer maydoni, narx, huquqiy hujjatlar).

### Savol:

1. `university_cadastre`'ni kengaytirish (academic fields qo'shish)?
2. Yangi `university_building` yaratish, cadastre bilan FK orqali bog'lash?
3. Inheritance (table-per-type) pattern?

## Qaror

**Variant 2** — yangi `university_building` jadvali yaratiladi, `cad_number` orqali `university_cadastre`'ga optional FK bilan bog'lanadi.

## Mulohaza

### Concern separation (domain-driven):

| Aspekt | `university_cadastre` | `university_building` |
|---|---|---|
| **Ma'no** | Huquqiy/mulkiy ko'rinish | Akademik/operatsional ko'rinish |
| **Manba** | Kadastr davlat API (`172.18.9.171/kadastr/`) | Univer admin (OTM tomondan) |
| **O'zgarish chastotasi** | Kadastr sinxronizatsiyasi (har kun) | OTM tomondan kamdan-kam (rekonstruksiya paytida) |
| **Sohibi** | Davlat kadastr sistemasi | Universitet ma'muriyati |
| **Auditi** | Kadastr API javobi snapshot | JPA auditing (OTM admin) |

Bu **Bounded Contexts** (DDD) — ikki manba, ikki hayot aylanishi, ikki business domain.

### Har tomonlama tahlil:

**Variant 1 (Cadastre'ni kengaytirish)** REDDI ETILDI:
- Cadastre Kadastr API'dan avtomatik yangilanadi — `year_built`, `capacity` kabi maydonlar overwrite bo'ladi
- Cadastre'da `type_code` (kadastr obyekt turi) bor — akademik `category_code` bilan chalkashlikdagi concepts
- Cadastre'da ayrim yozuvlar BINO emas (yer, kommunal obyektlar) — `category` NULL bo'ladi
- Single Responsibility principle buziladi

**Variant 3 (Inheritance)** REDDI ETILDI:
- CUBA legacy (`hemishe_*`) bilan mos kelmaydi — konsistentlik yo'q
- JPA inheritance performance ko'p JOIN bilan yomonlashadi
- PostgreSQL native inheritance ORM bilan yaxshi moslashmaydi

### Overlap analizi (3 ustun):

Ikkala jadvalda ham: `address`, `total_area`, `usable_area`

**Yechim:** Auto-fill orqali denormalization with authoritative override:
- `cad_number` kiritilsa — cadastre'dan pre-fill
- User override qilsa — saqlanadi (OTM amaliyoti ustun)
- Ikkinchi keyin cadastre yangilansa — BINO'ga propagate bo'lmaydi (user autoritet)

Bu **pragmatic trade-off** — soddaroq query + aniq ownership vs purest normalization.

## Oqibatlar

### Ijobiy:
- ✅ **Concern separation**: legal vs operational, ikki manba
- ✅ **Evolvability**: Building schema mustaqil o'zgarishi mumkin
- ✅ **Performance**: Ko'pgina query'larda JOIN kerak emas (address building'ning o'zida)
- ✅ **OTM ownership**: Universitet ma'muriyati o'zining ma'lumotini boshqaradi

### Salbiy:
- ⚠️ **3 ustun overlap**: `address`, `total_area`, `usable_area` — drift xavfi
- ⚠️ **Auto-fill complexity**: Service qatlamida logic (saqlash paytida)

### Mitigatsiya:
- Auto-fill **faqat yaratish paytida** — subsequent cadastre update propagate qilmaydi
- Comprehensive logging `source` ustuni orqali (qayerdan keladi)
- Cadastre bilan fark chiqsa — dashboard warning (keyingi iteratsiya)

## Muqobillar ko'rib chiqilgan

1. **Junction table (N:M cad_number ↔ building)** — REDDI (YAGNI, 1:1 hozircha yetarli)
2. **Cadastre bilan JOIN (building'da no overlap)** — REDDI (har doim kadastr bor deb bo'lmaydi)
3. **Inheritance (`cadastre` superclass)** — REDDI (CUBA legacy bilan nomos)

## Tegishli sub-qarorlar

- **1B** (quyida): Lifecycle event tracking — data irreversibility
- **1C** (quyida): Coordinates NULLABLE — sync flexibility

## Havolalar

- Excel template: `docs/Бино ва иншоотлар жадвали.xlsx`
- Schema: `domain/src/main/resources/db/changelog/changesets/schema/V011_create_university_buildings.sql`
- Entity: `domain/src/main/java/uz/hemis/domain/entity/infrastructure/UniversityBuilding.java`
- Domain-Driven Design (Eric Evans) — Bounded Contexts bobi

---

## 1B. Lifecycle event tracking — data irreversibility

**Status:** Accepted (2026-04-21)

### Kontekst

Excel'da faqat **oxirgi ta'mir sanasi** (`last_renovation_date`) bor. Har safar rekonstruksiya bo'lganda — oldingi qiymat yo'qoladi.

### Muammo:

- 2026-yil bino ta'mirlandi → `last_renovation_date = 2026-05-10`
- 2030-yil yana ta'mir → `last_renovation_date = 2030-08-15` (2026 abadiy yo'qoldi)
- 2035-yil vazirlik savol beradi: *"2026 ta'mirning xarajati?"* → **JAVOB YO'Q**

Bu **Data Irreversibility** — kelajakda backfill qilib bo'lmaydigan ma'lumot.

### Qaror

Hozirgi o'zida `building_lifecycle` immutable event log jadvali yaratish:
- Har rekonstruksiya, yopilish, qaytadan maqsadga o'tkazish — alohida row
- Immutable (faqat INSERT, UPDATE/DELETE ruxsat emas)
- Avtomatik populatsiya: service qatlamida `last_renovation_date` yangilansa → `RENOVATED` event yoziladi

### Nega YAGNI emas?

YAGNI printsipi: "Keraksiz narsa qo'shma".
Lekin bu holat YAGNI emas:
- **Probability**: 80%+ (bino'lar qariyi, rekonstruksiya amaliyoti neizbezh)
- **Cost to add now**: 50 qator kod + 1 jadval
- **Cost to add later**: 50 qator kod + **abadiy ma'lumot yo'qolishi**

Asymmetric cost — hozir qo'shish zarar ko'proq kam.

### Avtomatik populatsiya (Option C — Application Event Listener):

`UniversityBuildingService.save()` ichida `building.lastRenovationDate` o'zgargani tekshiriladi. Agar o'zgargan bo'lsa, `BuildingLifecycle` yaratiladi (`eventType=RENOVATED`).

Bu pattern kelajakda boshqa triggerlarga kengayadi (CLOSED, DEMOLISHED).

---

## 1C. Coordinates NULLABLE — sync flexibility

**Status:** Accepted (2026-04-21)

### Kontekst

Har bino uchun coordinates (lat/lng) kerak (xaritada ko'rsatish uchun).

### Savol

`latitude`/`longitude` NOT NULL bo'lishi kerakmi?

### Qaror

**NULLABLE**, lekin `(latitude, longitude)` juft CHECK constraint bilan (birga bor yoki birga yo'q).

### Sabab

- Legacy binolarida coordinates bo'lmasligi mumkin
- OTM admin darhol kiritishi shart emas
- Univer sync strict NOT NULL'ga takalamasin

### Qo'shimcha

Service qatlamida WARN log + Grafana alert — coordinates yo'q binolar ro'yxati. Business logic layer'da *soft validation*, DB layer'da *hard avoidance*.

### CHECK constraint:

```sql
CONSTRAINT chk_ub_coords_pair CHECK
    ((latitude IS NULL) = (longitude IS NULL))
```

Faqat ikkalasi bor yoki ikkalasi yo'q — yarim-to'ldirilgan yozuvlarni blokirovka qiladi.
