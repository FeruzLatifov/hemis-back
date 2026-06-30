# HEMIS Backend – Project Memory

> **HEMIS-back kim:** Oliy ta'lim vazirligi tasarrufidagi **MARKAZIY** server (bitta deploy/cluster). `/home/adm1n/projects/startup/old-hemis` (**CUBA Platform 7.3 — Java + Groovy**, Haulmont) ning **Spring Boot 4 + Java 25** ga **qayta yozilishi va optimizatsiyasi** (Java → Java modernizatsiya).
>
> **Maqsadlari:**
> 1. **230 OTM dan ma'lumot AGGREGATION** (224 — Univer orqali, 6 — markaziy admin web orqali)
> 2. **Klassifikatorlarni UMUMIY saqlash** (har OTM bir xil qiymatlardan foydalanadi)
> 3. **Qoidalarni JORIY qilish** (talaba kiritish cheklash, baho o'zgartirish cheklash, vaqt cheklov)
> 4. **Davlat tashkilotlari INTEGRATSIYA** (MyGov, MSPD, BIMM, Tax/Soliq, GUVD, OneID)
>
> **Univer (`/home/adm1n/projects/startup/hemis-univer`)** — **Yii2 framework** (prod 224 OTM hozir **PHP 7.4** da ishlab turibdi; lokal `hemis-univer` stack **PHP 8.4 + Yii2 2.0.51** — modernizatsiyaga tayyorlangan), har 230 OTM da **alohida deploy** (`hemis_337`, `hemis_401`, …, `hemis_NNN` — 224 ta Univer ishlatuvchi). Univer'lar **network REST API** orqali markaziy HEMIS-back ga ulanadi. **Univer ilovasini to'liq qayta yozmaymiz** — faqat integratsiya glue (webhook receiver `HemisCallbackController`, employee sync console, OAuth client) yoziladi/tuzatiladi.

Java 25 LTS + Spring Boot 4.0.6 modular monolith. PostgreSQL 18 master/replica + Redis 7.
**Stack:** Spring Boot 4.0.6 · Liquibase 4.31.1 · MapStruct · Lombok · Auxiliary `hemis_audit` DB.

---

## 🔒 GOLDEN RULES (BUZILMASDAN)

### 1. DB nomi har doim `.env`'dan keladi

Hech qachon hard-code qilmang DB nomi/jadvalni. **Markaziy HEMIS-back DB:** lokal dev `test1_hemis`, prod turli (env: `DB_MASTER_NAME`).

**`hemis_NNN`** (337, 401, …) — bu **bizning DB EMAS**. Bu 224 ta OTM tomonidagi Univer Yii2 PHP ekosistemining lokal bazalari nomi (har OTM o'zinikida deploy qilingan). Code'da DB nomi sifatida hard-code qilish — XATO. Test fixture/demo data ichida `university_code='337'` qiymat sifatida ruxsat (bu OTM identifikator, DB nomi emas).

| Soha | Markaziy HEMIS-back (vazirlik) | Univer (per-OTM, 224 ta) |
|------|--------------------|----------------------|
| **DB nomi** | `${DB_MASTER_NAME}` (`.env`) | `hemis_337`, `hemis_401`, …, `hemis_NNN` |
| **Stack** | Java 25 + Spring + JPA | Yii2 PHP |
| **Tegish** | `@Entity`/`@Table` + Hibernate | REST (`UniverApiService`) |
| **Bizdagi jadvallar** | `hemishe_e_student`, `hemishe_e_teacher`, `hemishe_e_university`, … | — |
| **Univer'da bor (bizda YO'Q)** | — | `hemishe_e_grade`, `hemishe_e_attendance`, `hemishe_e_course`, `hemishe_e_exam`, `hemishe_e_schedule`, `hemishe_e_contract`, `hemishe_e_curriculum`, `hemishe_e_employment`, `hemishe_e_enrollment`, `hemishe_e_department` |

Yangi `@Table(name = "hemishe_e_*")` qo'shilganda majburiy: `./scripts/check_table_mappings.sh` (pre-commit hook).

### 2. api-legacy old-hemis bilan **1:1** mos

`api-legacy` modul `/home/adm1n/projects/startup/old-hemis` bilan **AYNI XULQ**: status, body shape, validation, permission, error format. Tekshirish:

```bash
cd /home/adm1n/projects/startup/hemis-tools/docs/univer_tool && node compare_endpoints.js
# Maqsad: MATCH 175/175
```

Yangi qaror qabul qilishdan oldin: `grep -rn "<endpoint>" /home/adm1n/projects/startup/old-hemis/modules/`. Tafsilot: `api-legacy/CLAUDE.md`.

### 3. Modul ↔ Jadval mosligi — split-brain'ni oldini olish

Tezkor qoida: **`api-legacy` faqat eski (`hemishe_*`, `sec_*`); qolganlari yangi schema**.

Univer eski URL bilan POST → api-legacy yangi jadvalga yozsa → Univer keyingi GET eski jadvaldan o'qiydi → ma'lumot yo'qoladi (split-brain). Univer'dan ma'lumot kerak bo'lsa — `UniverApiService` (REST), `@Entity` qilmang.

**Batafsil jadval va konventsiya:** [`api-legacy/CLAUDE.md`](api-legacy/CLAUDE.md) "Modul ↔ Jadval mosligi" + Entity nomlanish konventsiyasi (Legacy* prefiks).

**ADR-0008 holati (2/3 hal qilindi):** `Employee` import olib tashlandi, `EmployeeJobs`→`LegacyEmployeeJobs` ko'chirildi. `User`→`users` — documented permanent exception (3 fayl; `check_table_mappings.sh` `ALLOWED_NEW_SCHEMA_IN_LEGACY=("User")`). Tafsilot: [`ADR-0008`](docs/adr/0008-api-legacy-entity-rebinding.md). Pre-commit hook yangi violation'larni bloklab turadi (`scripts/git-hooks-pre-commit`).

---

## Daily Commands

```bash
./gradlew clean build                 # Build
./gradlew :app:bootRun                # Run (port 8081)
./gradlew test                        # Tests (TESTS_ENABLED=true)
./gradlew :domain:liquibaseUpdate     # Apply migrations
./gradlew :domain:liquibaseStatus     # Migration status
```

---

## Workflow (senior tarz)

- **Plan Mode 80%** — kod yozishdan oldin reja (`EnterPlanMode`). Reja tayyor bo'lgach: *"buni staff muhandis sifatida qayta ko'rib chiq"* — zaif joylar chiqadi.
- **Simplicity Mandate** (Boris Cherny) — iloji boricha **sodda**, minimal kod. Qator qo'shish o'rniga **o'chirish** mumkin bo'lsa — o'chir. Senior "ko'p kod" emas, "kam, lekin to'g'ri kod" yozadi.
- **Avval mavjud kodni o'rgan** — assumption asosida yangi yozmaslik. DB schema + cross-project konvensiya majburiy audit.
- **Repetition → command** — ikki marta bir xil prompt = `.claude/commands/`'ga skill bo'lib o'tadi.
- **Xato → qoida** — Claude xato qilsa, darhol `CLAUDE.md` yoki [`.claude/rules.md`](.claude/rules.md)'ga qoida sifatida qo'shing, qaytarmasin.

---

## Porting Triggers (PORT: …)

URL pattern `/services/*` yoki `/app/rest/*` + `PORT:` prefix → endpoint ko'chirish workflow.

**Pattern:** `toMap()` + `LinkedHashMap` (NOT MapStruct — api-legacy 126 `@RestController`, 82 fayl shu patterni ishlatadi).

**Ishlatma:** code review, schema o'zgartirish, oddiy savol — bular porting EMAS.

**Canonical workflow:** [`.claude/ENDPOINT_PORTING_GUIDE.md`](.claude/ENDPOINT_PORTING_GUIDE.md) (8 qadam) · `/port-endpoint` slash command (avtomatlashtirilgan).
**Module-level CUBA format rules:** [`api-legacy/CLAUDE.md`](api-legacy/CLAUDE.md) (Eng Kritik Qoidalar bo'limi).

---

## Subagent, Skills va Slash Commands

`.claude/agents/`: 6 ta domen-spetsifik audit agent — `cuba-format-checker`, `liquibase-reviewer`, `n-plus-one-detector`, `webhook-outbox-reviewer` (`model: sonnet`); `security-auditor`, `cache-strategist` (`model: opus`). PR review yoki audit ishida `Task` tool orqali parallel chaqiriladi (`webhook-outbox-reviewer` — webhook/outbox/employee-sync o'zgarganda).

`.claude/commands/`: `/check-coverage`, `/audit-cache` (cache-strategist agent wrapper), `/review-pr` (5 agent parallel orkestrator).

`.claude/skills/` (Anthropic 2026 — `Skill` tool yoki `/<skill-name>` slash bilan invoke, ba'zilari trigger pattern bilan auto):

**ADR & docs:**
- `adr-create/` — yangi ADR yaratish (AgDR 2026 frontmatter)
- `adr-verify/` — ADR status drift detection (frontmatter vs kod)
- `runbook-create/` — incident playbook yozish (`docs/runbooks/`)

**Schema & data:**
- `liquibase-changeset/` — V/M/S### migration + rollback + master.yaml
- `classifier-add/` — h_* reference table to'liq pattern (5 layer)

**Backend pattern:**
- `cache-add/` — @Cacheable + TTL + evict pair + AOP safety
- `kafka-outbox-topic/` — outbox + producer + consumer + DLQ (ADR-0007)
- `menu-permission-add/` — menu + permission + i18n (system_message, 4 til)
- `webhook-target-add/` — yangi OTM webhook target + secret rotatsiya + HMAC (ADR-0012)

**api-legacy specific:**
- `endpoint-port/` — CUBA endpoint 1:1 port (`PORT:` trigger pattern)
- `univer-contract-verify/` — 175/175 MATCH tekshiruvi

---

## Univer 224 OTM — MAJBURIY o'qish

`api-legacy` o'zgarishidan oldin: `docs/UNIVER_CONTRACT.md` (67 frozen endpoint), `docs/UNIVER_ENDPOINT_AUDIT.md` (per-endpoint file:line). Side-by-side test: `hemis-tools/docs/univer_tool/compare_endpoints.js` — server'lar ishga tushganda majburiy.

---

## Further Reading

Modul `CLAUDE.md` (api-legacy/service/domain/security/…) Claude tomonidan **avtomatik** yuklanadi.

**Manual kontekst** (`Read` bilan, kerak bo'lganda):
- **Canonical (qoida — ziddiyatda ustun):** [`.claude/rules.md`](.claude/rules.md), [`.claude/UNIVER_INTEGRATION.md`](.claude/UNIVER_INTEGRATION.md), [`.claude/ENDPOINT_PORTING_GUIDE.md`](.claude/ENDPOINT_PORTING_GUIDE.md), [`.claude/LIQUIBASE_GUIDE.md`](.claude/LIQUIBASE_GUIDE.md)
- **Reference (misol):** [`.claude/MANDATORY_REQUIREMENTS.md`](.claude/MANDATORY_REQUIREMENTS.md) (Swagger+test), [`.claude/MENU_GUIDE.md`](.claude/MENU_GUIDE.md)
- **Context (domen):** [`.claude/architecture.md`](.claude/architecture.md), [`.claude/context.md`](.claude/context.md)

---

## Architecture Decision Records

**13 ta ADR** — [`docs/adr/README.md`](docs/adr/README.md) (ro'yxat + status). Yirik mavzular: Java 25 (0002), Audit DB isolation (0003), Klassifikator `h_*` (0006), Kafka sync (0007), JWT TTL+rotation (0009), Webhook outbound (0012), Business rule enforcement (0013).

Yangi qaror — [`docs/adr/template.md`](docs/adr/template.md) orqali. Tarix: [`CHANGELOG.md`](CHANGELOG.md).
