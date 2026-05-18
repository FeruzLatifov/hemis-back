---
name: runbook-create
description: Incident runbook yaratish (docs/runbooks/). Trigger - "runbook", "incident playbook", "qanday tiklash", "diagnostika qadamlari", "post-mortem".
allowed-tools: Read, Write, Edit, Bash, Grep, Glob
---

# Create Incident Runbook

> Maqsad: oncall xodim 3am'da soyu eslab qola olmaydigan diagnostika va tiklash qadamlarini fayl ko'rinishida saqlash.

## Workflow

### 1. Fayl

`docs/runbooks/<service-area>-<symptom>.md` — masalan: `kafka-consumer-lag.md`, `db-master-failover.md`. Mavjud misol: `docs/runbooks/buildings-sync-failure.md`.

### 2. Standart shape

```markdown
# Runbook: <Title>

> **Symptom:** Bir gapda — qanday alert/xato ko'rinishi.
> **Severity:** P0 (prod down) | P1 (degraded) | P2 (slow)
> **MTTR target:** N daqiqa

## Alert source

- Prometheus alert: `<alert_name>`
- Grafana panel: `<dashboard URL>`
- Log pattern: `grep "<error string>" /var/log/...`

## Quick diagnostic (≤5 min)

```bash
# 1. Service holati
curl -sf http://localhost:8081/actuator/health | jq .status

# 2. Recent errors
journalctl -u hemis-back --since "10 min ago" | grep ERROR | tail -20

# 3. DB connectivity
psql -d $DB_MASTER_NAME -c "SELECT 1;"

# 4. <service-specific check>
```

## Root cause matrix

| Symptom | Sabab | Probability | Tiklash bo'limi |
|---------|-------|-------------|-----------------|
| 503 + connection refused | App down | 60% | §A |
| 500 + DB connection pool exhausted | Slow query / leak | 25% | §B |
| 504 timeout | DB lock / deadlock | 10% | §C |
| Disk full | Log rotation buzuq | 5% | §D |

## Recovery procedures

### §A — App restart

```bash
sudo systemctl restart hemis-back
sleep 30
curl -sf http://localhost:8081/actuator/health
```

### §B — Connection pool reset

```bash
# 1. Active connection count
psql -d $DB_MASTER_NAME -c "SELECT count(*) FROM pg_stat_activity WHERE datname='$DB_MASTER_NAME';"
# 2. Long-running queries
psql -d $DB_MASTER_NAME -c "SELECT pid, now()-query_start AS dur, query FROM pg_stat_activity WHERE state='active' ORDER BY dur DESC LIMIT 10;"
# 3. Kill ham bo'lsa: SELECT pg_terminate_backend(<pid>);
```

### §C — DB lock breakdown

```bash
psql -d $DB_MASTER_NAME -c "SELECT blocked_locks.pid AS blocked_pid, blocking_locks.pid AS blocking_pid, blocked_activity.query AS blocked_query FROM pg_catalog.pg_locks blocked_locks JOIN pg_stat_activity blocked_activity ON blocked_activity.pid = blocked_locks.pid JOIN pg_catalog.pg_locks blocking_locks ON blocking_locks.locktype = blocked_locks.locktype AND blocking_locks.pid != blocked_locks.pid WHERE NOT blocked_locks.granted;"
```

### §D — Disk

```bash
df -h /var/log
du -sh /var/log/hemis-back/* | sort -h | tail -5
sudo journalctl --vacuum-time=2d
```

## Escalation

- 15 daqiqa ichida tiklanmasa → on-call lead
- DB master down → DBA lead + freeze writes
- Univer 224 OTM ta'sirlangan → tashqi communication channel

## Post-incident

- [ ] Incident report (`docs/incidents/YYYY-MM-DD-<symptom>.md`)
- [ ] Root cause analiz
- [ ] Preventive ish — ADR yoki code fix
- [ ] Runbook'ni yangilash (yangi fail rejimi)

## Related

- ADR-NNNN (agar bog'liq)
- `docs/runbooks/<related>.md`
- Grafana dashboard: `<URL>`
- On-call rotation: `<wiki/docs link>`
```

### 3. Test (chaynaks)

Yangi runbook bajariladigan ekanligini sinab ko'ring — har bash blok'ni real lokal env'da run qiling.

### 4. CHANGELOG

`[Unreleased]` ostida `### Documented`:
```
- Runbook: <symptom> recovery (docs/runbooks/<file>.md)
```

## Constraints

- ❌ "Investigate logs" tipida vague qadam — har qadam **runnable command**
- ❌ Sekret ichida (token, parol, IP) — env var ishlating
- ❌ Faqat happy path — **nima ishlamasa nima qilish** majburiy
- ❌ MTTR target yo'q
- ✅ Alert → quick diag → root cause matrix → §recovery → escalation
- ✅ 3am o'qib bo'ladigan til (jargon kam)

## See also

- `docs/runbooks/buildings-sync-failure.md` — yaxshi misol
- `docs/operations/` — bootstrap procedures
- ADR'lar — qaror sababini tushunish uchun
