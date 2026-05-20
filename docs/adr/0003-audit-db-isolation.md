---
id: ADR-0003
status: implemented-disabled-by-default
date: 2026-05-04
revised: 2026-05-18
deciders: hemis-team
agent: human
model: n/a
affects: [app, service]
liquibase:
  - app/src/main/resources/db/audit/V001_create_activity_log.sql
  - app/src/main/resources/db/audit/V002_create_error_log.sql
  - app/src/main/resources/db/audit/V003_create_login_log.sql
# JPA @Entity sinflari YO'Q — audit DB JdbcTemplate orqali AuditRepository ichida yoziladi.
# common/audit/{ActivityEvent,ErrorEvent,LoginEvent}.java — bu event record'lar (DTO), entity emas.
entities: []
verification: |
  # Audit kod mavjud
  grep -rn "@ConditionalOnProperty.*audit.enabled" service/ app/ | wc -l
  # Production'da yoqilgan bo'lsa
  psql -h $DB_AUDIT_HOST -d $DB_AUDIT_NAME -c "\dt"  # AUDIT_ENABLED=true bo'lganda
related: []
---

# ADR 0003: Audit DB alohida `hemis_audit` bazasi

## Status

**Implemented (kod) + DISABLED by default (config)** (2026-05-04, 2026-05-18 audit clarification)

> **Implementation status (2026-05-18 audit):**
>
> **Kod:** ✅ to'liq implement qilingan
> - `AuditAspect` (`@Around` `@Audited` annotation)
> - `AuditService`, `AuditEventListener`, `AuditContextHolder`
> - `AuditDataSourceConfig` (master/replica routing)
> - `AuditRequestFilter`, `AuditLogController` (admin view)
> - 3 ta schema (`V001..V003`) — `activity_log`, `error_log`, `login_log`
>
> **Default config:** ❌ `AUDIT_ENABLED=false` — barcha 4 ta `@ConditionalOnProperty(matchIfMissing = false)` sinflar load qilinmaydi.
>
> **Production deploy uchun MAJBURIY:**
> ```bash
> AUDIT_ENABLED=true
> AUDIT_DB_HOST=...
> AUDIT_DB_NAME=hemis_audit
> AUDIT_DB_USER=...
> AUDIT_DB_PASSWORD=...
> ```
> Bularsiz audit log yo'q → compliance violation. Production runbook ushbu ENV majburiy ekanini hujjatlashtirishi kerak.
>
> **Nima uchun default OFF?** Dev environment'da audit DB qo'shimcha PostgreSQL instance kerak. Lokal development tezligini saqlash uchun default-off. Production deploy esa explicit yoqishni majbur qiladi (fail-fast — `AUDIT_DB_PASSWORD` ENV bo'lmasa `AuditDataSourceConfig` config error).

## Context

**Markaziy HEMIS-back** = vazirlik darajasidagi yagona server. Davlat audit talablari (UZ qonunchilik + SOC2 / ISO 27001):
- Har CRUD operatsiya audit log'da yozilishi kerak
- Login attempts (success/failure) saqlanishi kerak
- 5xx xatolar to'liq stack trace bilan
- Audit log immutable bo'lishi kerak (no UPDATE, no DELETE)
- 5+ yil saqlash (compliance)

**Production hisob (markaziy aggregation kontekstida):**
- 224 ta Univer client REST traffic + vazirlik admin web + davlat S2S = ~10K operatsiya/kun har OTM ekvivalentida
- 230 OTM × ~10K kunlik aggregation = ~2.3M audit log/kun
- Davlat sistemalari S2S log (MyGov, MSPD, BIMM, Tax, GUVD): ~10K log/kun
- Yiliga ~800M+ yozuv (markaziy)
- 5 yil = 4+ milliard yozuv

> **Eslatma:** Bu **markaziy** audit DB. Per-OTM Univer Yii2 stack'lar o'z lokal auditiga ega — bizdan alohida.

Agar bu asosiy `hemis` DB ga yozilsa:
- ❌ asosiy DB hajmi 10x ko'payadi
- ❌ backup vaqti ko'payadi (RTO buziladi)
- ❌ VACUUM, ANALYZE oqimini sekinlashtiradi
- ❌ replicalar lag (audit insert master'ni yuklaydi)

## Decision

Audit jadvallari ALOHIDA `hemis_audit` PostgreSQL bazasida saqlanadi:
- `activity_log` — CRUD operatsiyalar
- `error_log` — Exception'lar
- `login_log` — Auth events

Schema yaratish: Spring `AuditDataSourceConfig.java` orqali boot paytida (Liquibase EMAS).

Master/Replica routing audit DB uchun ham qo'llaniladi:
- `auditJdbcTemplate` (master) — INSERT only
- `auditReadJdbcTemplate` (replica) — SELECT (audit UI)

Immutability: `REVOKE UPDATE, DELETE ON activity_log FROM PUBLIC` — DB-level himoya.

## Alternatives Considered

### Alternative 1: Asosiy DB ichida `audit_*` jadvallari
- ✅ Sodda — bitta DB, bitta migration tool
- ❌ Backup hajmi 10x — RTO buziladi
- ❌ Performance: audit insert OLTP queries bilan bir poolda
- ❌ Schema migration paytida audit log freeze
- **Rad etish sababi:** scale qiyinchiligi

### Alternative 2: Centralized log service (ELK / Grafana Loki)
- ✅ Shu vazifaga maxsus tool
- ✅ Full-text search yaxshi
- ❌ Yangi infra (Elasticsearch cluster, 16+ GB RAM)
- ❌ Compliance: log retention guarantee — DB'da kuchliroq
- ❌ Real-time audit query (foydalanuvchi UI'da) — DB tezroq
- **Rad etish sababi:** xarajat va murakkablik (alohida cluster). Kelajakda ELK ham qo'shilishi mumkin (read-only sink).

### Alternative 3: Liquibase audit DB ni ham boshqarsin
- ✅ Bir xil migration tool
- ❌ Liquibase changelog tracking — audit DB ham aralashadi
- ❌ Schema farq: audit DB'da `databasechangelog` jadval ham bo'ladi
- **Rad etish sababi:** Spring init oddiyroq, SQL fayllar versioned (db/audit/V001..V003)

## Consequences

### Positive
- Backup oqimi: asosiy DB tez (10 GB), audit DB sekin (200 GB) — alohida boshqariladi
- Performance: audit insert asosiy DB ga ta'sir qilmaydi
- Compliance: immutability DB-level (`REVOKE`)
- Sensitive fields auto-redaction (`hemis.audit.redact-fields=password`)
- JSONB max size 100KB — audit bloat oldini olish

### Negative
- Ikki DB connection — ulanish menejmenti murakkabroq
- Cross-DB JOIN imkonsiz (audit ichidan asosiy `users` jadvalga JOIN qilolmaymiz)
- Backup va monitoring 2 ta sistema uchun

### Risks
- **Risk:** Audit DB ishlamasa, asosiy app to'xtaydimi?
  **Mitigation:** AuditRepository try/catch + log warn — audit failure asosiy oqimni buzmaydi
- **Risk:** JSON snapshot (old_value/new_value) sensitive ma'lumot saqlashi
  **Mitigation:** `hemis.audit.redact-fields` config — password, token, secret avtomatik mask

## Implementation

Bajarildi:
- Master/Replica datasource'lar (Spring config)
- INSERT logic (try/catch — audit failure asosiy oqimni buzmaydi)
- 3 ta schema fayl (Spring init paytida `ResourceDatabasePopulator`)
- `REVOKE UPDATE, DELETE ... FROM PUBLIC` — immutability

> **Konfiguratsiya, HikariCP pool sizing, .env property'lari:** `@../../.claude/architecture.md` ("Audit DB" bo'limi)

## References

- Code: `app/src/main/java/uz/hemis/app/config/AuditDataSourceConfig.java`
- Code: `service/src/main/java/uz/hemis/service/audit/AuditRepository.java`
- Schema: `app/src/main/resources/db/audit/V001..V003`
- Compliance: SOC 2 Type II — audit trail immutability
- Standard: ISO/IEC 27001:2022 — A.12.4 Logging and monitoring
