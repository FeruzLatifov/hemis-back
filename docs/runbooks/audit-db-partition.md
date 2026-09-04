# Audit DB Partition Strategy — Runbook

> **Maqsad:** `hemis_audit` DB ichidagi 3 ta katta append-only jadval
> (`activity_log`, `error_log`, `login_log`) uchun **yearly partition** strategiyasi.
>
> **Ma'lumotnoma:** [ADR-0003 Audit DB Isolation](../adr/0003-audit-db-isolation.md),
> `security/CLAUDE.md` "Audit Log Requirements" (7 yil retention).

---

## 0. Hozirgi vaziyat (2026-05)

- `audit.enabled: ${AUDIT_ENABLED:false}` — production'da yoqilmagan (ADR-0003 status: implemented-disabled-by-default)
- `activity_log`/`error_log`/`login_log` jadvallari **bo'sh** (deploy paytida yaratiladi, ma'lumot yo'q)
- Partitioning hali **kerak emas** — actual write traffic yo'q
- Retention 7 yil talab (vazirlik PDP qoidasi)

**Trigger** (qachon partitioning kerak bo'ladi):
- `AUDIT_ENABLED=true` prod'da yoqilganda
- `activity_log` >100k row/oy bo'lganda (~6 oy operatsion)
- `SELECT pg_size_pretty(pg_relation_size('activity_log'))` >5 GB

---

## 1. Strategy — RANGE partition by `created_at`

```
activity_log (parent)
├── activity_log_2026
├── activity_log_2027
├── activity_log_2028
...
└── activity_log_2032  (7 yil retention oxiri)
```

**Yillik partition** afzal:
- ✅ Yearly retention DROP TABLE (instant) — partition drop = O(1)
- ✅ Statistik so'rovlar (oylik report) bitta partition'ga tushadi
- ✅ Index lokal (partition-level) — har yil alohida
- ✅ `pg_partman` extension bilan avto-create (manual yo'q)
- ❌ JOIN'lar partition-pruning bilan ishlamasligi mumkin — qaytarib tekshirish kerak

**Oylik partition** keyinchalik kerak bo'lsa:
- 1M+ row/oy bo'lganda
- Eng so'nggi 30 kun trafigi 90% bo'lganda (partition pruning effekti yuqori)

---

## 2. Migration template (kelajakda qo'llaniladi)

> **Hozir ishlatmaslik!** Audit DB hali yoqilmagan. Bu template kelajakda
> `AUDIT_ENABLED=true` qilishdan oldin V005 migration sifatida yoziladi.

### 2.1 Yangi partitioned parent jadval

```sql
-- hemis_audit DB ichida (alohida datasource — domain.master emas)
-- Diqqat: PostgreSQL'da partitioning uchun PRIMARY KEY partition key'ni
-- O'Z ICHIGA OLISHI SHART. Demak (id, created_at) composite.

CREATE TABLE activity_log_v2 (
    id              UUID DEFAULT gen_random_uuid(),
    user_id         UUID,
    username        VARCHAR(255),
    full_name       VARCHAR(255),
    user_ip         VARCHAR(45),
    user_agent      VARCHAR(512),
    action          VARCHAR(20) NOT NULL,
    entity_type     VARCHAR(255),
    entity_id       VARCHAR(255),
    entity_name     VARCHAR(500),
    old_value       JSONB,
    new_value       JSONB,
    changed_fields  TEXT[],
    request_id      VARCHAR(64),
    endpoint        VARCHAR(500),
    description     TEXT,
    scope_key       VARCHAR(64),   -- V004: egasi (OTM kodi) bo'yicha tarix — o'chirilgan qatorlar uchun
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

-- Yillik partition'lar (2026-2032, 7 yil)
CREATE TABLE activity_log_2026 PARTITION OF activity_log_v2
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');
CREATE TABLE activity_log_2027 PARTITION OF activity_log_v2
    FOR VALUES FROM ('2027-01-01') TO ('2028-01-01');
-- ... 2032 gacha

-- Default partition — vaqt oralig'idan tashqarisi (xato sifatida flag)
CREATE TABLE activity_log_default PARTITION OF activity_log_v2 DEFAULT;

-- Indekslar partition'ga (har birida)
CREATE INDEX idx_activity_v2_user_ts ON activity_log_v2 (user_id, created_at DESC);
CREATE INDEX idx_activity_v2_entity ON activity_log_v2 (entity_type, entity_id);
CREATE INDEX idx_activity_v2_action_ts ON activity_log_v2 (action, created_at DESC);
CREATE INDEX idx_activity_v2_request ON activity_log_v2 (request_id);
-- V004 ning ekvivalenti: egasi bo'yicha tarix (tenglik + created_at DESC tartibi indeksda)
CREATE INDEX idx_activity_v2_scope ON activity_log_v2 (entity_type, scope_key, created_at DESC)
    WHERE scope_key IS NOT NULL;

-- Immutability
REVOKE UPDATE, DELETE ON activity_log_v2 FROM PUBLIC;
```

### 2.2 Migration script

```sql
-- Agar mavjud data bo'lsa (ehtimol yo'q, AUDIT_ENABLED=false):
-- Ustunlar ANIQ sanaladi: `SELECT *` kelajakda ustun qo'shilsa jimgina joyini almashtiradi
-- (V004 `scope_key` aynan shunday yo'qolishi mumkin edi).
INSERT INTO activity_log_v2 (id, user_id, username, full_name, user_ip, user_agent, action,
                             entity_type, entity_id, entity_name, old_value, new_value,
                             changed_fields, request_id, endpoint, description, scope_key, created_at)
SELECT id, user_id, username, full_name, user_ip, user_agent, action,
       entity_type, entity_id, entity_name, old_value, new_value,
       changed_fields, request_id, endpoint, description, scope_key, created_at
FROM activity_log;

-- Eslatma: V005 immutability trigger'i UPDATE/DELETE ni bloklaydi. Migratsiya/retention sessiyasida:
--   SET LOCAL audit.purge = 'on';

-- Atomik switch
BEGIN;
ALTER TABLE activity_log RENAME TO activity_log_legacy;
ALTER TABLE activity_log_v2 RENAME TO activity_log;
COMMIT;

-- Legacy'ni 30 kun saqlash, keyin drop:
-- DROP TABLE activity_log_legacy;  (separate migration, post-validation)
```

### 2.3 Bir xil yondashuv `error_log` va `login_log` uchun

Pattern identik — `PARTITION BY RANGE (created_at)`, yearly child'lar.

---

## 3. pg_partman avto-rotation (tavsiya)

Manual `CREATE TABLE activity_log_2033` har yili — operatsion yuk.
`pg_partman` extension avtomatik yangi partition yaratadi va eski'larni
retention end'da drop qiladi.

```sql
CREATE EXTENSION pg_partman;

SELECT partman.create_parent(
    p_parent_table => 'public.activity_log',
    p_control => 'created_at',
    p_type => 'range',
    p_interval => '1 year',
    p_premake => 2                  -- 2 yil oldindan tayyorlab qo'yadi
);

-- Retention 7 yil
UPDATE partman.part_config
SET retention = '7 years',
    retention_keep_table = false   -- DROP eski partition'larni
WHERE parent_table = 'public.activity_log';
```

Background worker (`pg_partman_bgw`) cron-style har soat tekshiradi.

---

## 4. Migration rejasi (qachon kerak bo'lganda)

| Qadam | Effort | Risk |
|-------|--------|------|
| ADR-0014 yozish (yoki ADR-0003 update) | 1 soat | YO'Q |
| V005 migration (hemis_audit/audit/V005_partition_activity_log.sql) | 2 soat | YO'Q (jadval bo'sh) |
| Liquibase changeset master.yaml | 30 daqiqa | YO'Q |
| `pg_partman` extension prod'ga install | 1 soat | DBA approval kerak |
| Smoke test (`@AuditEnabled` lokal'da) | 1 soat | Past |
| Production rollout (maintenance window emas — additive) | 30 daqiqa | Past |
| Eski `activity_log` drop (30 kun keyin) | 5 daqiqa | YO'Q |

**Jami: ~6 soat, 2 hafta calendar (DBA + audit team coordinate)**

---

## 5. Retention monitoring

```sql
-- Joriy partition'lar ro'yxati
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_relation_size(schemaname || '.' || tablename)) AS size,
    pg_get_expr(c.relpartbound, c.oid) AS partition_range
FROM pg_tables t
JOIN pg_class c ON c.relname = t.tablename
WHERE t.tablename LIKE 'activity_log_%'
ORDER BY tablename;

-- Yillik insert count
SELECT
    date_trunc('year', created_at) AS year,
    count(*) AS row_count,
    pg_size_pretty(sum(pg_column_size(t.*))) AS approx_size
FROM activity_log t
GROUP BY 1 ORDER BY 1;
```

---

## 6. Backup va restore

- Backup: `pg_dump` partition-aware (har partition alohida fayl)
- Restore: `pg_restore` har partition'ni mustaqil restore qiladi (parallel imkoniyat)
- Tezroq: partition tablespace'ga ko'chirib, FS-level snapshot

Vazirlik talabi: backup 90 kun saqlanadi, off-site copy 1 nuxsa.

---

## Ma'lumotnoma fayllar

- `app/src/main/resources/db/audit/V001_create_activity_log.sql` — hozirgi schema
- `app/src/main/resources/db/audit/V002_create_error_log.sql` — error log
- `app/src/main/resources/db/audit/V003_create_login_log.sql` — login log
- `service/src/main/java/uz/hemis/service/audit/` — `@Audited` AOP
- ADR-0003 — Audit DB isolation
- `security/CLAUDE.md` "Audit Log Requirements" — 7 yil retention
