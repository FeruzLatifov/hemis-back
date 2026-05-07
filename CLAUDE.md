# HEMIS Backend – Project Memory

Java 25 LTS + Spring Boot 4.0.6 modular monolith. PostgreSQL 18 master/replica + Redis 7.
**Stack:** Spring Boot 4.0.6 · Liquibase 4.31.1 · MapStruct · Lombok · Auxiliary `hemis_audit` DB.

---

## 🔒 GOLDEN RULES (BUZILMASDAN)

### 1. DB nomi har doim `.env`'dan keladi

Hech qachon hard-code qilmang DB nomi/jadvalni. Lokal: `test1_hemis`, prod: turli. **Hech qachon "biz `hemis_NNN`'ni ishlatamiz" deb taxmin qilmang** — bu Univer (OTM Yii2 PHP, 224 ta) tomonidagi alohida ekosistem.

| Soha | Bizning hemis-back | Univer (OTM, 224 ta) |
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

**Hozirgi 3 buzilgan import** (User/Employee/EmployeeJobs api-legacy ichida yangi schema): tuzatish reja [`ADR-0008`](docs/adr/0008-api-legacy-entity-rebinding.md). Pre-commit hook yangi violation'larni bloklab turadi (`scripts/git-hooks-pre-commit`).

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

## Porting Triggers (PORT: …)

URL pattern `/services/*` yoki `/app/rest/*` + `PORT:` prefix → endpoint ko'chirish workflow.

**Pattern:** `toMap()` + `LinkedHashMap` (NOT MapStruct — api-legacy 261 controller shu patterni ishlatadi).

**Ishlatma:** code review, schema o'zgartirish, oddiy savol — bular porting EMAS.

**Canonical workflow:** [`.claude/ENDPOINT_PORTING_GUIDE.md`](.claude/ENDPOINT_PORTING_GUIDE.md) (8 qadam) · `/port-endpoint` slash command (avtomatlashtirilgan).
**Module-level CUBA format rules:** [`api-legacy/CLAUDE.md`](api-legacy/CLAUDE.md) (Eng Kritik Qoidalar bo'limi).

---

## Subagent, Skills va Slash Commands

`.claude/agents/`: 5 ta domen-spetsifik audit agent — `cuba-format-checker`, `liquibase-reviewer`, `n-plus-one-detector` (`model: sonnet`); `security-auditor`, `cache-strategist` (`model: opus`). PR review yoki audit ishida `Task` tool orqali parallel chaqiriladi.

`.claude/commands/`: `/port-endpoint`, `/check-coverage`, `/audit-cache`, `/review-pr`.

`.claude/skills/` (Anthropic 2026 — on-demand yuklash):
- `adr-create/SKILL.md` — yangi ADR yaratish workflow (AgDR 2026 standart)
- `adr-verify/SKILL.md` — ADR status drift detection (frontmatter vs kod holati)

---

## Univer 224 OTM — MAJBURIY o'qish

`api-legacy` o'zgarishidan oldin: `docs/UNIVER_CONTRACT.md` (67 frozen endpoint), `docs/UNIVER_ENDPOINT_AUDIT.md` (per-endpoint file:line). Side-by-side test: `hemis-tools/docs/univer_tool/compare_endpoints.js` — server'lar ishga tushganda majburiy.

---

## Further Reading

Modul-darajadagi `CLAUDE.md` (api-legacy, service, domain, security, …) Claude tomonidan o'sha papkada fayl o'qilganda **avtomatik** yuklanadi. Quyidagilar **manual** kontekst (kerak bo'lganda `Read`):

| Fayl | Qachon | Prioritet |
|------|--------|-----------|
| `.claude/rules.md` | **MAJBURIY qoidalar** (Java 25, OWASP 2025, DB schema, cache invariant) | **1 — canonical** |
| `.claude/MANDATORY_REQUIREMENTS.md` | Swagger + test KOD MISOLLARI (rules.md ni to'ldiradi) | 2 — reference |
| `.claude/architecture.md` | Modul diagram, DB routing, cache, deploy | 3 — context |
| `.claude/ENDPOINT_PORTING_GUIDE.md` | Endpoint porting **canonical workflow** (8 qadam) | 1 — canonical |
| `.claude/LIQUIBASE_GUIDE.md` | Liquibase migration **canonical workflow** (V###/M###/S###) | 1 — canonical |
| `.claude/MENU_GUIDE.md` | Menu + i18n + xavfsizlik (real schema: V012-V014) | 2 — reference |
| `.claude/context.md` | Biznes domain (230 ta OTM, shundan 224 — Univer ekosistemi) | 3 — context |

> **Qoida vs misol**: Ziddiyat bo'lsa `rules.md` ustun. `MANDATORY_REQUIREMENTS.md` faqat misol — qoida emas.

---

## Architecture Decision Records — `docs/adr/`

| ADR | Mavzu |
|-----|-------|
| [0001](docs/adr/0001-building-table-design.md) | university_building alohida jadval |
| [0002](docs/adr/0002-java-25-upgrade.md) | Java 25 LTS + Spring Boot 4.0.6 |
| [0003](docs/adr/0003-audit-db-isolation.md) | Audit DB alohida `hemis_audit` |
| [0004](docs/adr/0004-api-university-module.md) | api-university yangi modul (224 OTM B2B) |
| [0005](docs/adr/0005-oauth-client-credentials.md) | OAuth client_credentials migration |
| [0006](docs/adr/0006-classifier-h-prefix.md) | Klassifikatorlar `h_*` prefiks |
| [0007](docs/adr/0007-sync-architecture-evolution.md) | Kafka-first sync architecture |
| [0008](docs/adr/0008-api-legacy-entity-rebinding.md) | api-legacy: User/Employee/EmployeeJobs → eski jadvallar |

Yangi qaror — `docs/adr/template.md` orqali. Tarix: `CHANGELOG.md`.
