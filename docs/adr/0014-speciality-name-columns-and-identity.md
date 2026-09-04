---
id: ADR-0014
status: implemented
date: 2026-07-31
revised: 2026-08-25
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
  - M013_h_speciality_soft_delete.sql
entities:
  - HSpeciality
verification: |
  # 1. Per-til ustunlar + GENERATED name_search (V018 — asl qaror)
  grep -n "name_oz\|h_speciality_fold\|GENERATED ALWAYS AS (h_speciality_fold" domain/src/main/resources/db/changelog/changesets/schema/V018_create_h_speciality.sql
  # 2. Identity kaliti ENDI M013 da: partial unique, faqat TIRIK qatorlar (V018 dagi
  #    uq_h_speciality_identity jonli schemada YO'Q — faqat V018 ni grep qilish soxta GREEN beradi)
  grep -n "uq_h_speciality_identity_live\|NULLS NOT DISTINCT\|WHERE deleted_at IS NULL" domain/src/main/resources/db/changelog/changesets/migration/M013_h_speciality_soft_delete.sql
  # 3. Jonli schema: eski nom yo'q, yangisi bor
  psql -d $DB_MASTER_NAME -c "\d h_speciality" | grep uq_h_speciality_identity
  grep -rn "nameOz" service/src/main/java api-web/src/main/java
related:
  - ADR-0006
---

# ADR 0014: Speciality nomlari — per-til USTUNLAR (tarjima-jadval EMAS) + identity constraint

## Status

Implemented (2026-07-31; reviziya 2026-08-25 — M013: soft delete + identity kaliti PARTIAL unique indeksga)

> **Y-Statement:** Yagona mutaxassislik klassifikatori (`h_speciality`) nomlarini 4 ta qat'iy davlat tili (uz-UZ lotin, oz-UZ kirill, ru-RU, en-US) bo'ylab saqlash uchun, biz **per-til ustunlar** (`name_uz` / `name_oz` / `name_ru` / `name_en`) + DB-generated `name_search` + `(education_type, code, name_search)` identity noyoblik kalitini tanladik — **tarjima-jadval (`h_speciality_translation`) yoki JSONB EMAS** — chunki nom qatorning **identity qismi**, hot-path'lar dublikat-aniqlash va **yassi distribution** (224 OTM), til to'plami esa qat'iy va kichik; oqibatda `name_oz` qo'shish +1 ustun bo'ldi (butun stack yassi qoldi), va kushimcha manbasidagi kirill endi yo'qolmaydi.

## Context

`h_speciality` (V018, ADR-0006) — bakalavr+magistr yagona klassifikatori, UUID-kalitli daraxt, yillar 1:N, `review_status` workflow. Nomlar dastlab **per-til ustunlar** (`name_uz` NOT NULL, `name_ru`, `name_en`) + generated `name` (= `name_uz`) + qo'lda to'ldiriladigan `name_search` sifatida saqlanardi.

Muammolar:
1. **Kirill (oz-UZ) ustuni yo'q edi** — `kushimcha-2026` manbasi aynan **kirill** (`Давлат аудити`), lekin ETL uni lotinga o'girib tashlar, kirill faqat audit CSV'da qolar edi. Til jadvali (`languages`, V012/S005) `oz-UZ`ni aktiv deb e'lon qilgan va UI qo'llagan holda, klassifikator kirillni ko'rsata olmasdi.
2. **Identity DB darajasida yoqilmagan** — vazirlik qoidasi "bir (education_type, code, name) — bitta yozuv" hech qanday constraint bilan himoyalanmagan; seed'ning o'zida 28 ta year-versiyalangan dublikat (edu, code, name) guruh bor edi.
3. **`name_search` qo'lda** — app yozuv yo'li uni noto'g'ri hisoblasa, dublikat-aniqlash va kelajakdagi unique kaliti ishonchsiz bo'lardi.

Kodbazada **`SystemMessageTranslation`** pretsedenti bor (composite PK `(message_id, language)`, 4 til) — bu **cheksiz, siyrak, vaqt o'tib to'ldiriladigan** UI-matn katalogi uchun to'g'ri. Savol: `h_speciality` nomlari ham shu tarjima-jadval patternига o'tsinmi?

## Decision

**Variant A — per-til ustunlar** tanlandi. `h_speciality`ga:

1. **`name_oz VARCHAR(512) NULL`** qo'shildi (uz-UZ dan keyin). Kushimcha ETL uni **asl kirilldan** to'ldiradi; xlsz-manbali APPROVED qatorlarда kirill yo'q → NULL. `name_uz` — yagona NOT NULL birlamchi + identity anchor.
2. **`name_search` endi GENERATED** ustun: `GENERATED ALWAYS AS (h_speciality_fold(name_uz)) STORED`, bu yerda `h_speciality_fold()` — IMMUTABLE SQL funksiya (apostrof-variant → probel, lower, whitespace-collapse). Uni **hech qaysi yozuv yo'li (seed/ETL/JPA) o'zgartira olmaydi** → identity kaliti ishonchli. ETL `fold()` va Java `foldSearch()` **shu funksiya bilan bayt-ba-bayt** mos (NFKD unaccent olib tashlandi — generated ustun IMMUTABLE bo'lmagan unaccent'ni chaqira olmaydi).
3. **Identity kaliti `(education_type, code, name_search)`, `NULLS NOT DISTINCT` bilan.** V018 uni `CONSTRAINT uq_h_speciality_identity UNIQUE NULLS NOT DISTINCT (education_type, code, name_search)` sifatida yozgan edi; **jonli shakl 2026-08-25 dan beri boshqa** — M013 uni partial unique indeksga almashtirdi: `CREATE UNIQUE INDEX uq_h_speciality_identity_live ON h_speciality (education_type, code, name_search) NULLS NOT DISTINCT WHERE deleted_at IS NULL` (sabab: quyidagi Revisions). Ustun nomi ham o'zgargan: ADR yozilganda `education_level` edi, **V022 dan beri `education_type`** (modern `h_education_type(code)` FK) — bu yerda hamma joyda joriy nom ishlatilgan. PG18-native `NULLS NOT DISTINCT` 15 ta kodsiz qatorni ham qamraydi. `uq_h_speciality_year(speciality_id, year)` bilan birga `(edu, code, name, year)` noyobligini kafolatlaydi. **same-code/different-name** va **same-name/different-code** ikkalasi ham qonuniy qoladi (ikkisi ham kalit a'zosi). ETL 28 ta dublikatni bitta survivor'ga (yillar birlashtirilib, bolalar qayta ulanib) konsolidatsiya qiladi.

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

## Revisions

### 2026-08-25 — M013: soft delete + identity kaliti PARTIAL bo'ldi

`HSpecialityService.delete()` qatorni **jismonan** o'chirardi (`repository.delete`). Klassifikator qatori esa yo'qolmasligi kerak: unga 224 OTM ham, eski talaba-saqlash jadvallari ham **UUID bo'yicha** tayanadi — jismoniy o'chirish markazda "muvaffaqiyat", OTM tomonida esa qaytarib bo'lmaydigan uzilgan havola beradi. M013 `h_speciality`ga `deleted_at` / `deleted_by` qo'shdi (entity `AuditableEntityNoSoftDelete` → `AuditableEntity` + `@SQLRestriction("deleted_at IS NULL")`), `idx_h_speciality_deleted` (`WHERE deleted_at IS NOT NULL` — kichik tomon, `listDeleted()` aynan shuni skanerlaydi) yaratdi va **identity kalitini `uq_h_speciality_identity` (to'liq jadval) dan `uq_h_speciality_identity_live` (partial, `WHERE deleted_at IS NULL`) ga almashtirdi**.

Nega partial shart: to'liq UNIQUE bo'lsa, o'chirilgan qator `(education_type, code, name_search)` slotini **abadiy** band qilib turadi — `name_search` GENERATED, uni bo'shatib bo'lmaydi — va "xato o'chirdim, qaytadan qo'shaman" degan birinchi harakat tushunarsiz 23505 → 500 berardi (M012 da `oauth_client.client_id` bilan AYNI holat prodda kuzatilgan). `NULLS NOT DISTINCT` yangi indeksda **saqlandi** — ~15 ta kodsiz NEEDS_REVIEW qator `(education_type, name)` noyobligiga aynan shu clause orqali bo'ysunadi.

Qaytarish yo'li ochildi: `POST /classifiers/speciality/{id}/restore` — 422 `SPECIALITY_RESTORE_IDENTITY_TAKEN` (identity'ni tirik qator egallab olgan) va 422 `SPECIALITY_RESTORE_PARENT_DELETED` (ota o'chirilgan — avval otani tikla).

#### M011 (soft delete OLIB TASHLANDI) ↔ M013 (soft delete QO'SHILDI) — ziddiyat emas

Ikki migratsiya qarama-qarshi ko'rinadi, lekin mezon bitta: **qatorga tashqaridan kim tayanadi**.

| | `university_speciality_attachment` (M011) | `h_speciality` (M013) |
|---|---|---|
| Tashqi referent | yo'q — biriktirmani hech kim ID bo'yicha ko'rsatmaydi | 224 OTM + eski talaba-saqlash jadvallari, **UUID bo'yicha** |
| Qayta yaratish | bir bosish | mumkin emas (yangi UUID = boshqa qator) |
| Biznes tarixi | `status` (ACTIVE / SUSPENDED / REVOKED) da allaqachon bor | faqat qatorning o'zida |
| Yashirin qatordan zarar | bor: o'chirish guard'i soft-delete qilinganlarni ham sanardi → mutaxassislik "3 OTM'ga biriktirilgan" deb bloklanardi, biriktirma registri esa (u `deleted_at IS NULL` filtrlaydi) hech birini ko'rsatmasdi | yo'q: guard tirik qatorlar bo'yicha ishlaydi |

Ya'ni M011 soft delete'ni **hech qanday foyda bermay, guard'ni buzgani uchun** olib tashladi (detach endi HARD DELETE); M013 uni **o'chirish tashqi va qaytarib bo'lmaydigan zarar bergani uchun** qo'shdi. Uy qoidasi: soft delete — qatorga tashqi referent bog'langanda; bog'lanmagan bo'lsa `status` ustuni yetarli.

#### Ataylab qabul qilingan ikki og'ish

1. **V018 ning 5 ta indeksi to'liq qoldi (partial qilinmadi).** `idx_h_speciality_parent`, `_edu_type`, `_review`, `_code`, `_search` — ~5.4k qatorli jadvalda `WHERE deleted_at IS NULL` predikati qatorlarning ~100% iga mos keladi, ya'ni planner partialdan hech narsa yutmaydi (u faqat qo'shimcha predikat-moslik tekshiruvini talab qilardi). Partial faqat ikki joyda: `uq_h_speciality_identity_live` (semantika uchun **shart**) va `idx_h_speciality_deleted` (teskari yo'nalish, kichik tomon).
2. **O'chirish guard'larida DB tayanchi qolmadi.** `fk_h_speciality_parent` va `fk_univ_spec_attach_spec` (ikkalasi `ON DELETE RESTRICT`) soft delete bajaradigan **UPDATE**'ga qarshi **ishlamaydi**: non-key UPDATE `FOR NO KEY UPDATE` oladi, bola/biriktirma INSERT'i esa `FOR KEY SHARE` — ular to'qnashmaydi. Uchala 422 guard (APPROVED emas / bolasiz / OTM'ga biriktirilmagan) endi **faqat ilova qatlamida**. Qoldiq xavf: sekunddan kichik oynadagi ko'p-aktyorli poyga ("A o'chiryapti / B ayni paytda bola qo'shyapti"). Ataylab qabul qilindi — kodbazada **0 ta** pessimistic lock bor (uni faqat shu joy uchun kiritish yangi konvensiya ochardi), biriktirma poygasiga esa 3 aktyor kerak (biriktirish APPROVED, o'chirish esa NEEDS_REVIEW talab qiladi). **Ketma-ket, poygasiz yo'l** — "otani o'chirdim, keyin bolani tiklayman" — `SPECIALITY_RESTORE_PARENT_DELETED` bilan yopilgan.

#### Daraja (`hierarchy_level`) invarianti — nega o'chirilgan bolalar ham hisoblanadi

Ko'rik paytida haqiqiy konteynerda tasdiqlangan ketma-ketlik: *bargni o'chir → otasining darajasini o'zgartir → bargni tikla* — bola `parent.level + 1` qoidasini buzgan chuqurlikda qaytardi (B=2, C=4), chunki daraja-o'zgartirish guard'i `findAllChildren()` ni o'qiydi, u esa `@SQLRestriction` bilan filtrlanadi. Buzilgan chuqurlik ichki qolmaydi: `SpecialityDistItemDto` `parentId` va `hierarchyLevel` ni 224 OTM'ga jo'natadi.

Yechim **harakat tomonida**: `applyPlacement` daraja o'zgarganda endi `countChildrenIncludingDeleted()` (native — restriction'siz) ni sanaydi, ya'ni o'chirilgan bola ham otasining chuqurligini **qotirib turadi** (`SPECIALITY_HAS_CHILDREN_MOVE_FIRST`, xabar "O'chirilganlar ro'yxatidan tiklang" deb yo'l ko'rsatadi). Muqobil — tiklash tomonida rad etish — rad qilindi: u qatorni **hech kim joylashtira olmaydigan** holatga tushirib qo'yishi mumkin edi.

O'CHIRISH guard'i esa ataylab `findAllChildren()` (faqat tirik) da qoladi: bolalari allaqachon o'chirilgan otani o'chirish nomuvofiqlik yaratmaydi — tiklashda tartib `SPECIALITY_RESTORE_PARENT_DELETED` bilan majburlanadi (avval ota, keyin bola).

#### Saqlash muddati (retention) — o'chirilganlar QOLADI

Soft-delete qilingan `h_speciality` qatorlari **muddatsiz saqlanadi**; avtomatik purge/TTL **ATAYLAB yo'q** (qaror: 2026-08-25).

Sabablari: (a) o'chirilishi mumkin bo'lgan to'plam kichik va chegaralangan — faqat `NEEDS_REVIEW` curation backlog (~53 qator), ya'ni hajm hech qachon muammo bo'lmaydi; (b) qator UUID'si 224 OTM'ga va eski talaba-saqlash jadvallariga tarqalgan bo'lishi mumkin — keyinchalik jismonan yo'q qilish aynan M013 oldini olgan zararni qaytaradi; (c) barcha o'qish yo'llari tirik qatorlar bo'yicha ishlaydi (`uq_h_speciality_identity_live` + JPQL restriction), o'chirilganlar esa faqat `idx_h_speciality_deleted` (kichik tomon) orqali ko'riladi — ya'ni ular na so'rov rejasiga, na indeks hajmiga sezilarli ta'sir qiladi.

Agar kelajakda hajm haqiqatan muammo bo'lsa: SUPER_ADMIN uchun **aniq `purge` amali** (audit yozuvi bilan), avtomatik TTL emas — chunki "eskirgani uchun jimgina yo'qoldi" holati M013 bekor qilgan xatoning aynan o'zi.
