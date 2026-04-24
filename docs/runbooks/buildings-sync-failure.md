# Runbook: Building Sync Failure

**Modul:** Buildings (V014)
**Endpoint:** `POST /api/v1/university/{universityCode}/buildings/sync`
**Log tegi:** `UniversityBuildingSyncService`

---

## 🚨 Alert Signal

Quyidagilardan biri:
- `buildings.sync.count{status="failure"}` > 10% umumiy
- Univer-{OTM} so'nggi 7+ kun davomida sync qilmagan
- `buildings.sync.duration` p95 > 30 soniya (sekin sync)

## 🔍 Diagnostika — 5 daqiqa

### 1-qadam: Umumiy sync statistikasini ko'rish
```bash
# Grafana: "OTM Sync Health" dashboard
# Yoki Prometheus:
buildings_sync_count_total{status="failure",university="401"}
buildings_sync_count_total{status="success",university="401"}
```

### 2-qadam: Applicaion log'larni ko'rish
```bash
kubectl logs -n hemis hemis-back-0 | grep "Sync failed"
# Yoki filebeat/ELK:
# service:"UniversityBuildingSyncService" AND level:"WARN"
```

**Umumiy xato formati:**
```
Sync failed: university=401, sourceUid=univer-bld-123, error=<xato>
```

### 3-qadam: Xato turini aniqlash

| Log'dagi xato | Mumkin sabab | 4-qadam |
|---|---|---|
| `ConstraintViolationException` | Validation fail (year > 2100, coords) | Faza A |
| `DataIntegrityViolationException` | FK/UNIQUE/CHECK buzildi | Faza B |
| `OptimisticLockException` | Concurrent update | Faza C |
| `could not execute statement` | DB connection/timeout | Faza D |
| `JWT expired` | Auth token | Faza E |

---

## 🔧 Yechimlar

### Faza A: Validation xatolari

**Sabab:** Univer'dan kelgan data validation qoidalariga mos kelmaydi.

**Tekshirish:**
```bash
# 400 javob JSON'ida aniq field ko'rinadi
curl ... | jq '.failures[].message'
# Misol: "yearBuilt: must be between 1800 and 2100"
```

**Yechim:**
1. Univer admin bilan bog'lanish — qaysi field xato
2. Univer tomondan data tuzatish
3. Sync qayta urinish (idempotent — xavfsiz)

### Faza B: Data integrity

**Mumkin xatolar:**
- `foreign key violation`: `category_code='X'` — `building_category` jadvalda yo'q
- `unique constraint`: `cad_number` allaqachon boshqa binoda
- `check constraint violates`: area/year/coords oralig'idan tashqarida

**Yechim (FK misoli):**
```sql
-- 1. Univer kategoriya kodini tekshirish
SELECT * FROM building_category WHERE code = 'X';

-- 2. Agar yo'q bo'lsa — kategoriya kengaytirish kerak
INSERT INTO building_category (code, name, ...) VALUES ('X', 'Yangi tur', ...);
```

### Faza C: Optimistic lock

**Sabab:** 2 sync bir vaqtda bir xil sourceUid'ni yangilayapti.

**Yechim:**
- Univer cron'da bir OTM uchun parallel sync'ni cheklash (lock)
- hemis-back retry qilmaydi — univer xavfsiz qayta urinishi mumkin

### Faza D: DB muammosi

**Tekshirish:**
```bash
# Connection pool health
curl localhost:8081/actuator/health | jq '.components.db'

# Slow query log
kubectl exec -it postgres-0 -- psql -c "
  SELECT query, total_exec_time, calls FROM pg_stat_statements
  WHERE query LIKE '%university_building%' ORDER BY total_exec_time DESC LIMIT 5;"
```

**Yechim:**
- HikariCP pool kengaytirish (dev:10→20, prod:30→50)
- Slow query → EXPLAIN ANALYZE → index qo'shish

### Faza E: Authentication

**Sabab:** JWT muddati tugagan yoki client noto'g'ri token yuborgan.

**Yechim:**
- Univer `POST /oauth/token` chaqirib refresh qilsin
- JWT TTL'ni ko'paytirish kerakligi ko'rib chiqish (security review)

---

## 🎯 Emergency: barcha OTM sync buzilgan

1. **Service'ni vaqtincha pause qilish** (maintenance mode):
   ```bash
   # Feature flag orqali
   kubectl set env deployment/hemis-back BUILDINGS_SYNC_ENABLED=false
   ```

2. **Root cause aniqlash** — yuqoridagi 1-3 qadamlar

3. **Fix deploy qilinishi** — patch version (non-breaking)

4. **Re-enable + replay:**
   ```bash
   kubectl set env deployment/hemis-back BUILDINGS_SYNC_ENABLED=true
   # Univer admin'larga qayta sync so'rov yuborish
   ```

---

## 📞 Eskalatsiya

1. **L1 (Support):** Runbook'dagi Faza A-E bo'yicha 30 daqiqa
2. **L2 (Backend team):** Root cause aniqlansa — code fix
3. **L3 (Infra team):** DB/network muammolari

**On-call:**
- Backend: backend-oncall@hemis.uz
- SRE: sre-oncall@hemis.uz
- DBA: dba@hemis.uz

---

## 📊 Tegishli dashboardlar

- Grafana: **OTM Sync Health** (filter: module=buildings)
- Grafana: **DB Performance** (table=university_building)
- Kibana: `application:"hemis-back" AND logger:"*BuildingSync*"`

---

## 📚 Tegishli hujjatlar

- [ADR-001 Building Design](../adr/ADR-001-building-table-design.md)
- [Bootstrap Plan](../operations/buildings-bootstrap.md)
- Swagger: `GET /swagger-ui.html` → Buildings
