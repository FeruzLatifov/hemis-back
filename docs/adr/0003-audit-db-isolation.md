# ADR 0002: Audit DB alohida `hemis_audit` bazasi

## Status

Accepted (2026-05-04)

## Context

Compliance va xavfsizlik talablari (SOC2 / ISO 27001):
- Har CRUD operatsiya audit log'da yozilishi kerak
- Login attempts (success/failure) saqlanishi kerak
- 5xx xatolar to'liq stack trace bilan
- Audit log immutable bo'lishi kerak (no UPDATE, no DELETE)
- 5+ yil saqlash (compliance)

**Production hisob:**
- 224 OTM × ~10K kunlik operatsiya = 2.24M audit log/kun
- Yiliga ~800M yozuv
- 5 yil = 4 milliard yozuv

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
