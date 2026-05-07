# University Domain — Entity Relationship Diagram

> **Hujjat maqsadi:** Project Manager / Team Lead tasdig'i uchun HEMIS-back loyihasidagi universitetga tegishli barcha jadvallar, ularning bog'lanishlari, ma'lumot manbai va biznes mantiqi.
>
> **Versiya:** 1.0 | **Sana:** 2026-05-04 | **Status:** ⏳ Tasdiqlash kutilmoqda
> **Stack:** PostgreSQL 18 · Spring Boot 4.0.6 · Liquibase 4.31.1 · 224 OTM ekosistemi

---

## 📋 Executive Summary

HEMIS-back universitet domeni **9 ta clean schema jadvali** + **3 ta klassifikator** (`h_*`) + **1 ta foundational CUBA legacy** (`hemishe_e_university`) atrofida qurilgan. Arxitektura **6 ta qatlamga** ajratilgan: Foundation, Core 1:1, Composite 1:N, Immutable Event Log, Klassifikator, va Cross-cutting (auth/HR). 224 universitet (`hemis_NNN` bazalari) ekosistemi bilan **3 ta tashqi API** orqali sync qilinadi (`api_legal`, `api_kadastr`, `univer_sync`). Fayllar (logo, hujjatlar) **MinIO obyekt-storage**'da, DB faqat metadata. Auditlash uchun **2 ta immutable event log** (`university_lifecycle`, `building_lifecycle`) — UPDATE/DELETE ruxsat etilmaydi.

**Tasdiqlash kerak:** quyidagi diagramma va biznes mantiqi to'g'ri va to'liqmi? Yetishmagan jadval bormi (department, contacts, partnership, accreditation)?

---

## 1️⃣ High-Level Conceptual View — qatlamli arxitektura

```mermaid
flowchart TB
    subgraph L1["🏛️ Foundation Layer (CUBA Legacy — FROZEN)"]
        UNI["hemishe_e_university<br/><i>code PK · 224 OTM</i>"]
    end

    subgraph L2["📋 Core 1:1 Layer (V005)"]
        ULEG["university_legal<br/><i>Soliq · OPF · Direktor</i>"]
        UPRO["university_profile<br/><i>Logo · Aloqa · Hujjatlar</i>"]
    end

    subgraph L3["🧱 Composite 1:N Layer (V008-V011)"]
        UFON["university_founder<br/><i>INDIVIDUAL ⊕ LEGAL</i>"]
        UCAD["university_cadastre<br/><i>Yer uchastkalari</i>"]
        UBLD["university_building<br/><i>Binolar (Excel)</i>"]
    end

    subgraph L4["📜 Immutable Event Log (audit integrity)"]
        ULIF["university_lifecycle<br/><i>CLOSED · MERGED · SPLIT</i>"]
        BLIF["building_lifecycle<br/><i>RENOVATED · DEMOLISHED</i>"]
    end

    subgraph L5["🏷️ Classifier Layer (h_* prefix · ADR-0006)"]
        HBC["h_building_category<br/><i>6 tur</i>"]
        HCM["h_construction_material<br/><i>7 tur</i>"]
        HRT["h_roof_type<br/><i>6 tur</i>"]
    end

    subgraph L6["🔗 Cross-Cutting (Auth · HR)"]
        USR["users.university_id<br/><i>V006 — odam akkaunt</i>"]
        OAC["oauth_client.university_code<br/><i>V006 — Univer machine (per-OTM)</i>"]
        EJB["employee_job.university_code<br/><i>V004 — HR assignment</i>"]
    end

    subgraph L7["📦 Reference Master"]
        ORG["organization<br/><i>TIN UNIQUE</i>"]
        EMP["employee<br/><i>PINFL UNIQUE</i>"]
    end

    UNI -->|1:1| ULEG
    UNI -->|1:1| UPRO
    UNI -->|1:N| UFON
    UNI -->|1:N| ULIF
    UNI -->|1:N| UCAD
    UNI -->|1:N| UBLD
    UBLD -->|1:N| BLIF

    UBLD -.FK.-> HBC
    UBLD -.FK.-> HCM
    UBLD -.FK.-> HRT
    BLIF -.FK.-> HBC

    ULEG -.FK.-> ORG
    ULEG -.FK.-> EMP
    UFON -.FK XOR.-> EMP
    UFON -.FK XOR.-> ORG

    UNI --> USR
    UNI --> OAC
    UNI --> EJB

    classDef foundation fill:#fce4ec,stroke:#c2185b,stroke-width:2px,color:#000
    classDef core fill:#e3f2fd,stroke:#1565c0,stroke-width:2px,color:#000
    classDef composite fill:#fff3e0,stroke:#ef6c00,stroke-width:2px,color:#000
    classDef event fill:#f3e5f5,stroke:#6a1b9a,stroke-width:2px,color:#000
    classDef classifier fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px,color:#000
    classDef crosscut fill:#eceff1,stroke:#455a64,stroke-width:1px,color:#000
    classDef reference fill:#fffde7,stroke:#f57f17,stroke-width:2px,color:#000

    class UNI foundation
    class ULEG,UPRO core
    class UFON,UCAD,UBLD composite
    class ULIF,BLIF event
    class HBC,HCM,HRT classifier
    class USR,OAC,EJB crosscut
    class ORG,EMP reference
```

**Qatlam mantiqi:**
- 🏛️ **Foundation** — universitet kodi (`code`) yagona identifikator. CUBA legacy, **FROZEN** (o'zgartirilmaydi).
- 📋 **Core 1:1** — har OTM uchun yagona yuridik va profil yozuvi. CASCADE DELETE (universitet o'chsa).
- 🧱 **Composite 1:N** — bir OTM uchun bir nechta ta'sischi/kadastr/bino bo'lishi mumkin.
- 📜 **Event Log** — UPDATE/DELETE ruxsat etilmaydi. Voqealar tarixi (compliance).
- 🏷️ **Classifier** — kichik enum'lar, 224 OTM bilan sync. `h_*` prefiks (ADR-0006).
- 🔗 **Cross-cutting** — Auth va HR modullarining FK'lari. University domain'ni "ishga tushiruvchi" qatlam.
- 📦 **Reference master** — global registrlar (TIN, PINFL UNIQUE).

---

## 2️⃣ Detailed Entity Relationship Diagram

```mermaid
erDiagram
    HEMISHE_E_UNIVERSITY ||--|| UNIVERSITY_LEGAL : "1:1 CASCADE"
    HEMISHE_E_UNIVERSITY ||--|| UNIVERSITY_PROFILE : "1:1 CASCADE"
    HEMISHE_E_UNIVERSITY ||--o{ UNIVERSITY_FOUNDER : "1:N CASCADE"
    HEMISHE_E_UNIVERSITY ||--o{ UNIVERSITY_LIFECYCLE : "1:N RESTRICT"
    HEMISHE_E_UNIVERSITY ||--o{ UNIVERSITY_CADASTRE : "1:N CASCADE"
    HEMISHE_E_UNIVERSITY ||--o{ UNIVERSITY_BUILDING : "1:N CASCADE"

    UNIVERSITY_BUILDING ||--o{ BUILDING_LIFECYCLE : "1:N CASCADE"
    UNIVERSITY_CADASTRE ||--o| UNIVERSITY_BUILDING : "0:1 SET NULL"

    UNIVERSITY_BUILDING }o--|| H_BUILDING_CATEGORY : "FK RESTRICT"
    UNIVERSITY_BUILDING }o--o| H_CONSTRUCTION_MATERIAL : "FK SET NULL"
    UNIVERSITY_BUILDING }o--o| H_ROOF_TYPE : "FK SET NULL"
    BUILDING_LIFECYCLE }o--o| H_BUILDING_CATEGORY : "FK previous/new"

    ORGANIZATION ||--o| UNIVERSITY_LEGAL : "0:1 SET NULL"
    ORGANIZATION ||--o{ UNIVERSITY_FOUNDER : "0:N (LEGAL XOR)"
    EMPLOYEE ||--o{ UNIVERSITY_LEGAL : "director/accountant"
    EMPLOYEE ||--o{ UNIVERSITY_FOUNDER : "0:N (INDIVIDUAL XOR)"

    HEMISHE_E_UNIVERSITY {
        VARCHAR(255) code PK "224 OTM kodi"
        VARCHAR name "OTM nomi"
        VARCHAR short_name
        INTEGER region_id
        TIMESTAMP delete_ts "soft-delete"
    }

    UNIVERSITY_LEGAL {
        UUID id PK
        VARCHAR university_code FK "1:1 with university"
        UUID organization_id FK "TIN registry"
        UUID director_employee_id FK
        UUID accountant_employee_id FK
        INTEGER opf "OPF kod"
        VARCHAR tin "STIR snapshot"
        INTEGER status "0..9"
        BIGINT vat_number
        JSONB shipping_addresses
        JSONB bank_accounts
        JSONB api_raw_response
        TIMESTAMP synced_at
    }

    UNIVERSITY_PROFILE {
        UUID id PK
        VARCHAR university_code FK "1:1 with university"
        VARCHAR phone
        VARCHAR email
        JSONB social_links "telegram, instagram, ..."
        VARCHAR logo_key "MinIO object key"
        TEXT description
        JSONB documents "license, charter, ..."
        NUMERIC latitude "WGS84"
        NUMERIC longitude "WGS84"
        VARCHAR source "manual/hemis_sync"
        VARCHAR hash "SHA-256"
    }

    UNIVERSITY_FOUNDER {
        UUID id PK
        VARCHAR university_code FK
        VARCHAR founder_type "INDIVIDUAL or LEGAL"
        UUID employee_id FK "INDIVIDUAL only"
        UUID organization_id FK "LEGAL only"
        NUMERIC share_percent "0..100"
        BIGINT share_sum
        BOOLEAN is_current
        DATE effective_from
        DATE effective_to
    }

    UNIVERSITY_LIFECYCLE {
        UUID id PK
        VARCHAR university_code FK
        VARCHAR event_type "8 enum"
        DATE event_date
        VARCHAR successor_code FK "MERGED/SPLIT"
        VARCHAR decree_number
        INTEGER students_count "snapshot"
        INTEGER employees_count "snapshot"
    }

    UNIVERSITY_CADASTRE {
        UUID id PK
        VARCHAR university_code FK
        VARCHAR cad_number UK "10:10:02:..."
        TEXT address
        NUMERIC land_area
        NUMERIC object_area
        BIGINT cost
        JSONB subjects "egalar"
        JSONB documents
        JSONB bans "cheklovlar"
    }

    UNIVERSITY_BUILDING {
        UUID id PK
        VARCHAR university_code FK
        VARCHAR name "Bino nomi"
        VARCHAR category_code FK "h_building_category"
        INTEGER year_built "1800-2100"
        INTEGER floor_count
        NUMERIC total_area
        NUMERIC usable_area
        VARCHAR construction_material_code FK
        VARCHAR roof_type_code FK
        DATE last_renovation_date
        NUMERIC latitude
        NUMERIC longitude
        VARCHAR cad_number FK "to cadastre"
        VARCHAR source "univer_sync/manual/excel/kadastr"
    }

    BUILDING_LIFECYCLE {
        UUID id PK
        UUID building_id FK
        VARCHAR event_type "7 enum"
        DATE event_date
        VARCHAR previous_category_code FK "REPURPOSED"
        VARCHAR new_category_code FK "REPURPOSED"
        NUMERIC cost
        VARCHAR decree_number
    }

    H_BUILDING_CATEGORY {
        VARCHAR code PK "ACADEMIC, DORMITORY, ..."
        VARCHAR name
        VARCHAR name_ru
        VARCHAR name_en
        BOOLEAN is_active
    }

    H_CONSTRUCTION_MATERIAL {
        VARCHAR code PK "BRICK, CONCRETE, ..."
        VARCHAR name
    }

    H_ROOF_TYPE {
        VARCHAR code PK "METAL_SHEET, TILE, ..."
        VARCHAR name
    }

    ORGANIZATION {
        UUID id PK
        VARCHAR tin UK "STIR — 9 digits"
        VARCHAR name
        INTEGER opf
        JSONB api_raw_response
    }

    EMPLOYEE {
        UUID id PK
        VARCHAR pinfl UK "JSHSHIR — 14 digits"
        VARCHAR first_name
        VARCHAR last_name
        VARCHAR person_type "UNIVERSITY_STAFF/MINISTRY_STAFF/..."
    }
```

**Constraint diqqat punktlari:**
- `university_founder` — **chk_ufounder_xor**: INDIVIDUAL → `employee_id` NOT NULL AND `organization_id` NULL; LEGAL → teskari. ON DELETE RESTRICT (XOR'ni saqlash uchun).
- `university_legal.status` — 0..9 (0=INACTIVE, 1=ACTIVE, 2=SUSPENDED, 3=LIQUIDATED, 4=UNDER_REORGANIZATION).
- `university_lifecycle.event_type` — 8 enum: CLOSED, MERGED, SPLIT, LICENSE_REVOKED, SUSPENDED, REACTIVATED, RENAMED, REORGANIZED.
- `building_lifecycle.event_type` — 7 enum: CONSTRUCTED, RENOVATED, EXPANDED, REPURPOSED, CLOSED, REOPENED, DEMOLISHED.
- `university_building` — coords pair check (lat/lng ikkalasi NULL yoki ikkalasi NOT NULL), year 1800..2100, usable_area ≤ total_area.

---

## 3️⃣ External Data Flow — sync arxitektura

```mermaid
flowchart LR
    subgraph EXT["🌐 External Sources"]
        API1["api_legal<br/>172.18.9.171/legalentity"]
        API2["api_kadastr<br/>172.18.9.171/kadastr"]
        UNIVER["univer_sync<br/>224 OTM PHP push"]
        EXCEL["Excel template<br/>Бино жадвали"]
        MINIO["MinIO Object Storage<br/>S3-compatible"]
    end

    subgraph HEMIS["🏛️ HEMIS-back Database"]
        ORG2["organization"]
        ULEG2["university_legal"]
        UCAD2["university_cadastre"]
        UBLD2["university_building"]
        UPRO2["university_profile"]
    end

    API1 -->|"sync TIN, OPF, OKED"| ORG2
    API1 -->|"director PINFL,<br/>tax_mode, billing"| ULEG2
    API2 -->|"cad_number, land_area,<br/>subjects, bans"| UCAD2
    UNIVER -->|"univer_sync<br/>per-OTM push"| UBLD2
    UNIVER -->|"hemis_sync"| UPRO2
    EXCEL -->|"excel_import<br/>bulk admin"| UBLD2
    API2 -->|"kadastr_sync<br/>auto-fill cad_number"| UBLD2
    MINIO -.->|"logo_key,<br/>documents.file_key"| UPRO2

    classDef ext fill:#fce4ec,stroke:#c2185b,color:#000
    classDef db fill:#e3f2fd,stroke:#1565c0,color:#000
    class API1,API2,UNIVER,EXCEL,MINIO ext
    class ORG2,ULEG2,UCAD2,UBLD2,UPRO2 db
```

**Sync xususiyatlari:**

| Manba | Yo'nalish | Idempotency | Audit | Kontekst |
|-------|-----------|-------------|-------|----------|
| `api_legal` | Pull (cron) | `tin` UNIQUE + `synced_at` | `api_raw_response` JSONB snapshot | Davlat ro'yxatdan o'tkazish ma'lumotlari |
| `api_kadastr` | Pull (cron) | `cad_number` UNIQUE | `api_raw_response` JSONB | Davlat kadastr API |
| `univer_sync` | Push (224 OTM → us) | `(university_code, source_uid)` partial UNIQUE | `content_hash` SHA-256 | Universitetlar ma'lumotlarini yuboradi |
| `excel_import` | Bulk admin | `source_uid` | `synced_at` | Vazirlik admin Excel yuklaydi |
| `MinIO` | Async | `logo_key` (object key) | DB'da metadata, fayl S3'da | Logo, license PDF, charter |

**Diqqat:** `university_cadastre` — `soft-delete YO'Q` (snapshot pattern, har sync overwrite). Tarix kerak bo'lsa yangi `cadastre_history` jadvali kerak (kelajak qarori).

---

## 4️⃣ Lifecycle Events — Immutable Audit (compliance)

```mermaid
stateDiagram-v2
    [*] --> ACTIVE: university created

    state UniversityLifecycle {
        ACTIVE --> SUSPENDED: SUSPENDED event
        SUSPENDED --> ACTIVE: REACTIVATED event
        ACTIVE --> RENAMED: RENAMED event
        RENAMED --> ACTIVE
        ACTIVE --> MERGED: MERGED event<br/>(successor_code → another OTM)
        ACTIVE --> SPLIT: SPLIT event<br/>(successor_code → new OTM)
        ACTIVE --> REORGANIZED: REORGANIZED event
        ACTIVE --> LICENSE_REVOKED: LICENSE_REVOKED
        LICENSE_REVOKED --> CLOSED: CLOSED event
        ACTIVE --> CLOSED
        MERGED --> [*]
        SPLIT --> [*]
        CLOSED --> [*]
    }

    note right of MERGED
        successor_code FK majburiy.
        Decree number/date saqlanadi.
        Snapshot: students_count, employees_count.
    end note

    note left of SUSPENDED
        Vaqtinchalik to'xtatish.
        Faollik holati alohida ustun.
    end note
```

**Bino lifecycle:**
- `CONSTRUCTED` → `RENOVATED` (multiple times) → `EXPANDED` → `REPURPOSED` (kategoriya o'zgaradi) → `CLOSED` → `REOPENED` → `DEMOLISHED`
- Har voqea uchun `event_date`, `decree_number`, `cost` saqlanadi.
- `building.last_renovation_date` yangilanganda **avtomatik RENOVATED event** yoziladi (service layer trigger).

**Compliance kafolati:**
- Ikkala lifecycle jadvalida **UPDATE/DELETE ruxsat etilmaydi** (audit integrity).
- Faqat `created_at`, `created_by` audit ustunlari (Immutable pattern).

---

## 5️⃣ Cross-Cutting — Universitet domeni boshqa modullarda

```mermaid
flowchart LR
    UNI2["hemishe_e_university"]

    subgraph AUTH["Auth Module (V006)"]
        UR["users.university_id<br/>📌 SET NULL"]
        OC["oauth_client.university_code<br/>📌 RESTRICT (UNIVERSITY_BACKEND XOR)"]
    end

    subgraph HR["HR Module (V004)"]
        EJ["employee_job.university_code<br/>📌 RESTRICT"]
        ED["employee_job.department_code<br/>📌 SET NULL → hemishe_e_university_department"]
    end

    UNI2 --> UR
    UNI2 --> OC
    UNI2 --> EJ
    UNI2 --> ED

    classDef uni fill:#fce4ec,stroke:#c2185b,color:#000
    classDef auth fill:#e8f5e9,stroke:#2e7d32,color:#000
    classDef hr fill:#fff3e0,stroke:#ef6c00,color:#000
    class UNI2 uni
    class UR,OC auth
    class EJ,ED hr
```

| FK | Kim ishlatadi | ON DELETE | Maqsad |
|----|---------------|-----------|--------|
| `users.university_id` | Web/Mobile auth | SET NULL | Inson akkauntning OTM scope (rektor, dekan, ...) |
| `oauth_client.university_code` | 224 OTM Univer integratsiya | RESTRICT | Machine akkaunt (univer_101, mygov_sync) |
| `employee_job.university_code` | HR transactional | RESTRICT | Xodim qaysi OTMda ishlaydi |
| `employee_job.department_code` | HR | SET NULL | Bo'lim/kafedra (CUBA legacy FK) |

---

## 6️⃣ Risk va kelajak qo'shimchalar — diqqatga olinadigan masalalar

| # | Topilgan kamchilik | Risk darajasi | Tavsiya |
|---|--------------------|---------------|---------|
| 1 | `university_department` yangi jadval yo'q (faqat `hemishe_e_university_department` legacy) | 🟡 O'rta | V012 yangi migration: bo'lim/kafedra clean schema |
| 2 | `university_cadastre` soft-delete yo'q (snapshot only) | 🟡 O'rta | `cadastre_history` immutable jadvali yaratish (kerak bo'lsa) |
| 3 | `university_profile.phone/email` — bittagina | 🟢 Past | `university_contact` (phone_type, email_type) — kelajak |
| 4 | `university_partnership` jadvali yo'q (xalqaro/mahalliy MOU) | 🟢 Past | Talabga ko'ra V0XX |
| 5 | `university_accreditation` alohida jadval yo'q (JSONB ichida) | 🟡 O'rta | Reporting kerak bo'lsa V0XX (deadline, status indeks) |
| 6 | `tax_mode`/`taxpayer_type`/`business_type` — INTEGER 0..99 (klassifikator yo'q) | 🟢 Past | `h_tax_mode`, `h_taxpayer_type`, `h_business_type` (h_* prefiks — ADR-0006) |
| 7 | Bino fotosi (gallery) yo'q | 🟢 Past | `building_media` JSONB ustun yoki alohida jadval |
| 8 | "Hozirgi holat" flag yo'q (faqat lifecycle event) | 🟡 O'rta | `university_legal.status INTEGER` qisman yetadi |

---

## 7️⃣ Statistika — schema kichik tahlili

| Mezon | Soni | Izoh |
|-------|------|------|
| Universitetga tegishli jadvallar (jami) | **12 ta** | 9 yangi + 3 klassifikator |
| Soft-delete jadvallar | 6/9 | Immutable log + cadastre snapshot YO'Q |
| Immutable event log | 2 ta | university_lifecycle, building_lifecycle |
| External API integratsiya | 3 ta | legal, kadastr, univer_sync |
| JSONB ustunlar | 11 ta | Variable shape data + raw API snapshot |
| FK target sifatida `hemishe_e_university` | 8 ta | foundational |
| Klassifikator jadvallar (`h_*`) | 3 ta | building_category, construction_material, roof_type |
| Liquibase migration soni (universitet uchun) | 5 ta | V005, V008, V009, V010, V011 |

---

## 8️⃣ How to read this diagram — Notation Guide

### Mermaid ER syntax

| Belgi | Ma'no |
|-------|-------|
| `\|\|--\|\|` | Exactly one to exactly one (1:1) |
| `\|\|--o{` | Exactly one to zero or more (1:N) |
| `\|\|--o\|` | Exactly one to zero or one (1:0..1) |
| `}o--\|\|` | Zero or more to exactly one (N:1) |
| `}o--o{` | Zero or more to zero or more (M:N) |

### Color coding (qatlam)

| 🎨 Rang | Qatlam |
|---------|--------|
| 🌸 Pushti | Foundation (CUBA legacy, FROZEN) |
| 🔵 Ko'k | Core 1:1 (V005) |
| 🟠 To'q sariq | Composite 1:N (V008-V011) |
| 🟣 Binafsha | Immutable Event Log |
| 🟢 Yashil | Klassifikator (`h_*`) |
| ⚪ Kulrang | Cross-cutting (Auth/HR) |
| 🟡 Sariq | Reference Master (TIN, PINFL UNIQUE) |

### Risk darajalari

- 🔴 Yuqori — production'ga ta'sir, darhol hal qilish kerak
- 🟡 O'rta — keyingi sprint'da ko'rib chiqish
- 🟢 Past — kelajakda kerak bo'lsa

---

## ✅ Approval Checklist

Project Manager / Team Lead tasdiqi:

- [ ] **Schema qamrovi to'g'ri** — 9 yangi jadval + 3 klassifikator + 1 foundational kifoyami?
- [ ] **Yetishmagan jadvallar tasdiqlandi** — qarorlar (university_department, accreditation, contacts, partnership) keyingi sprintga
- [ ] **Cardinality to'g'ri** — 1:1 (legal, profile), 1:N (founder, lifecycle, cadastre, building)
- [ ] **ON DELETE strategiyasi to'g'ri** — CASCADE (composite), RESTRICT (XOR), SET NULL (cross-cut)
- [ ] **Immutable log dizayni qabul qilingan** — UPDATE/DELETE yo'q (compliance)
- [ ] **External sync uch manba mukammal** — api_legal, api_kadastr, univer_sync (224 OTM)
- [ ] **MinIO integratsiya pattern qabul qilingan** — DB'da metadata, fayl S3'da
- [ ] **`h_*` prefiks konvensiyasi tasdiqlandi (ADR-0006)** — building klassifikatorlar
- [ ] **Risk reyestri kelishildi** — 8 ta topilgan kamchilik, qaysi sprintda hal qilinadi?

**Tasdiqlash yo'li:**
1. Bu hujjat o'qildi va tushunildi
2. Diagrammalar to'g'ri (10 daqiqalik review)
3. Risk reyestri ko'rib chiqildi
4. Yetishmagan jadvallar prioritet bo'yicha tartiblandi
5. PR description'ga signature: `Approved by: [PM_NAME] / [TL_NAME] | Date: YYYY-MM-DD`

---

## 📚 References

- **Migrations:** `domain/src/main/resources/db/changelog/changesets/schema/V005, V008, V009, V010, V011*.sql`
- **Detailed schema:** `docs/db-analysis/HEMIS_DB_Jadvallar_Tahlili.docx` (Versiya 3.2)
- **Architecture decisions:**
  - [ADR-0001](../adr/0001-building-table-design.md) — university_building alohida jadval
  - [ADR-0006](../adr/0006-classifier-h-prefix.md) — h_* prefiks konvensiyasi
- **Java entities:** `domain/src/main/java/uz/hemis/domain/entity/university/`, `infrastructure/`
- **External APIs:** `service/src/main/java/uz/hemis/service/integration/`

---

*Document generated: 2026-05-04 | HEMIS Backend Team*
