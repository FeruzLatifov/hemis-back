---
id: ADR-0014
status: implemented
date: 2026-07-31
deciders: hemis-team
agent: claude-code
model: claude-opus-4-8
affects:
  - domain
  - service
  - api-web
  - hemis-front
liquibase:
  - V018_create_h_speciality.sql
  - S014_seed_h_speciality.sql
  - S017_seed_h_speciality_2026.sql
entities:
  - HSpeciality
verification: |
  grep -n "name_oz\|uq_h_speciality_identity\|h_speciality_fold\|GENERATED ALWAYS AS (h_speciality_fold" domain/src/main/resources/db/changelog/changesets/schema/V018_create_h_speciality.sql
  grep -rn "nameOz" service/src/main/java api-web/src/main/java
related:
  - ADR-0006
---

# ADR 0014: Speciality nomlari — per-til USTUNLAR (tarjima-jadval EMAS) + identity constraint

## Status

Implemented (2026-07-31)

> **Y-Statement:** Yagona mutaxassislik klassifikatori (`h_speciality`) nomlarini 4 ta qat'iy davlat tili (uz-UZ lotin, oz-UZ kirill, ru-RU, en-US) bo'ylab saqlash uchun, biz **per-til ustunlar** (`name_uz` / `name_oz` / `name_ru` / `name_en`) + DB-generated `name_search` + `(education_level, code, name_search)` identity UNIQUE'ni tanladik — **tarjima-jadval (`h_speciality_translation`) yoki JSONB EMAS** — chunki nom qatorning **identity qismi**, hot-path'lar dublikat-aniqlash va **yassi distribution** (224 OTM), til to'plami esa qat'iy va kichik; oqibatda `name_oz` qo'shish +1 ustun bo'ldi (butun stack yassi qoldi), va kushimcha manbasidagi kirill endi yo'qolmaydi.

## Context

`h_speciality` (V018, ADR-0006) — bakalavr+magistr yagona klassifikatori, UUID-kalitli daraxt, yillar 1:N, `review_status` workflow. Nomlar dastlab **per-til ustunlar** (`name_uz` NOT NULL, `name_ru`, `name_en`) + generated `name` (= `name_uz`) + qo'lda to'ldiriladigan `name_search` sifatida saqlanardi.

Muammolar:
1. **Kirill (oz-UZ) ustuni yo'q edi** — `kushimcha-2026` manbasi aynan **kirill** (`Давлат аудити`), lekin ETL uni lotinga o'girib tashlar, kirill faqat audit CSV'da qolar edi. Til jadvali (`languages`, V012/S005) `oz-UZ`ni aktiv deb e'lon qilgan va UI qo'llagan holda, klassifikator kirillni ko'rsata olmasdi.
2. **Identity DB darajasida yoqilmagan** — vazirlik qoidasi "bir (education_level, code, name) — bitta yozuv" hech qanday constraint bilan himoyalanmagan; seed'ning o'zida 28 ta year-versiyalangan dublikat (edu, code, name) guruh bor edi.
3. **`name_search` qo'lda** — app yozuv yo'li uni noto'g'ri hisoblasa, dublikat-aniqlash va kelajakdagi unique kaliti ishonchsiz bo'lardi.

Kodbazada **`SystemMessageTranslation`** pretsedenti bor (composite PK `(message_id, language)`, 4 til) — bu **cheksiz, siyrak, vaqt o'tib to'ldiriladigan** UI-matn katalogi uchun to'g'ri. Savol: `h_speciality` nomlari ham shu tarjima-jadval patternига o'tsinmi?

## Decision

**Variant A — per-til ustunlar** tanlandi. `h_speciality`ga:

1. **`name_oz VARCHAR(512) NULL`** qo'shildi (uz-UZ dan keyin). Kushimcha ETL uni **asl kirilldan** to'ldiradi; xlsz-manbali APPROVED qatorlarда kirill yo'q → NULL. `name_uz` — yagona NOT NULL birlamchi + identity anchor.
2. **`name_search` endi GENERATED** ustun: `GENERATED ALWAYS AS (h_speciality_fold(name_uz)) STORED`, bu yerda `h_speciality_fold()` — IMMUTABLE SQL funksiya (apostrof-variant → probel, lower, whitespace-collapse). Uni **hech qaysi yozuv yo'li (seed/ETL/JPA) o'zgartira olmaydi** → identity kaliti ishonchli. ETL `fold()` va Java `foldSearch()` **shu funksiya bilan bayt-ba-bayt** mos (NFKD unaccent olib tashlandi — generated ustun IMMUTABLE bo'lmagan unaccent'ni chaqira olmaydi).
3. **`CONSTRAINT uq_h_speciality_identity UNIQUE NULLS NOT DISTINCT (education_level, code, name_search)`** — PG18-native `NULLS NOT DISTINCT` 15 ta kodsiz qatorni ham qamraydi. `uq_h_speciality_year(speciality_id, year)` bilan birga `(edu, code, name, year)` noyobligini kafolatlaydi. **same-code/different-name** va **same-name/different-code** ikkalasi ham qonuniy qoladi (ikkisi ham kalit a'zosi). ETL 28 ta dublikatni bitta survivor'ga (yillar birlashtirilib, bolalar qayta ulanib) konsolidatsiya qiladi.

`name_oz` distribution DTO'ga (`SpecialityDistItemDto`) **additiv** qo'shildi (224 OTM eski consumer'lari toza qoladi), shuningdek 4 DTO + Excel eksport + FE create/edit/detail'ga.

## Rad etilgan variantlar

- **B — har til uchun alohida jadval** (`h_speciality_name_uz`, `..._ru`, …): N jadval, har o'qishda N-join/union, til qo'shish = schema o'zgarishi, referensial/qidiruv birligi yo'q — 4 qat'iy tilda hech qanday foyda yo'q. **YOMON.**
- **C — bitta tarjima-jadval** (`h_speciality_translation(speciality_id, language, name)`, `SystemMessageTranslation` kabi): nomni **identity/`name_search`dan ajratadi**, **yassi distribution payload**ni har push/pull'да join bilan qayta yig'ishga majbur qiladi, NOT NULL birlamchi-tilni trigger'siz yoqib bo'lmaydi, va bounded identity-bearing hot jadvalга EAV yukини import qiladi. `SystemMessage` pretsedenti **cheksiz-siyrak** muammoni hal qiladi — bu jadvalда u yo'q. **Bu yerда noto'g'ri.**
- **D — JSONB `names`**: per-til NOT NULL typing yo'qoladi, oddiy-ustun qidiruv + generated `name`ni tiklash uchun ifoda/GIN index kerak, baribir wire uchun yassi maydon ajratish shart — faqat til to'plami dinamik/cheksiz bo'lganda foyda berardi (bu yerда emas). **Rad etildi.**

## Consequences

**Ijobiy:**
- Kirill (oz-UZ) endi to'liq saqlanadi va toza `liquibaseUpdate`'да qayta tiklanadi (curator lotinni asl kirilldan tekshiradi).
- Identity qoidasi DB darajasida majburiy; dublikat seed vaqtida "loud" xato beradi, migration o'rtasида emas.
- `name_search` DB-avtoritar → dublikat-aniqlash va identity kaliti chetlab o'tib bo'lmas.
- Butun consumer stack yassi qoldi (DTO/mapper/FE/eksport) — churn = +1 maydon.

**Salbiy / e'tibor:**
- `name_search` GENERATED bo'lgani uchun seed'lar uni **INSERT qilmaydi** (ETL emitidan olib tashlandi), va JPA entity'да `insertable=false, updatable=false`. Uch fold (SQL/ETL/Java) **bir xil ta'rifда** bo'lishi shart — ETL self-check (`identity collision = 0`) buni himoyalaydi.
- Yangi 5-til qo'shilsa — bu ADR qayta ko'rib chiqiladi (ustun qo'shish yoki C'ga o'tish). Qat'iy 4-til gipotezasi buzilса, savol ochiladi.

## Bog'liq o'zgarishlar (shu ish doirasida)

- `edu_form` ustuni **butunlay olib tashlandi** (create-then-drop minasi: V018 yaratardi → S014/S017 to'ldirardi → M007 drop qilardi → runOnChange re-seed'да "column does not exist" boot uzardi). M007 changeset o'chirildi.
- V018 `h_education_year` seed'i **legacy-mustaqil** qilindi (generate_series fallback) — toza bazada speciality-year FK doim hal bo'ladi.
- M003 legacy-guard qo'shildi (M002b-e kabi).
