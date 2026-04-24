# Buildings Module — Initial Bootstrap Plan

**Maqsad:** 224 OTM'ning ~4,500 binosini yangi `university_building` jadvalga yuklash.
**Davom etish vaqti:** 6 hafta
**Stakeholder'lar:** Vazirlik, 224 OTM admin, Backend team, Support

---

## 📅 Faza 1: Tayyorgarlik (Hafta 1)

### 1.1 Excel template tarqatish
- **Fayl:** `docs/Бино ва иншоотлар жадвали.xlsx`
- **Yo'l:** Email + Google Drive links to 224 OTM
- **Sana:** Dushanba — yuborish, Juma — OTM admin qabul qilish

### 1.2 OTM admin onboarding
- 15 daqiqali video meeting (har viloyat bo'yicha)
- Demo: qanday to'ldirish, qanday yuklash
- FAQ dokument + Telegram support kanali

### 1.3 Vazirlik rahbariyati ma'lum:
- Deadline: Hafta 3 oxirida
- Kasb javobgar: OTM rektor
- Non-compliance: Hafta 4'da eslatma

---

## 📝 Faza 2: Data Input (Hafta 2-3)

### 2.1 OTM ish yuki bo'yicha:

| OTM hajmi | Binolar soni | Taxminiy vaqt | OTM soni |
|---|---|---|---|
| Kichik (filiallar) | 5-10 | 1-2 soat | ~80 |
| O'rta (viloyat) | 15-25 | 3-5 soat | ~100 |
| Katta (Toshkent) | 30-50 | 6-10 soat | ~44 |

### 2.2 Qo'llab-quvvatlash kanali:

- **Telegram:** @hemis_buildings_support (L1)
- **Email:** buildings@hemis.uz (L2)
- **FAQ:** `docs/faq/buildings-input.md`

### 2.3 Monitoring:
- Excel qabul qilingan OTM % (Kibana dashboard)
- Hafta 2 oxiri: 50%+ to'ldirgan bo'lishi kerak

---

## 🚀 Faza 3: Bulk Upload (Hafta 4)

### 3.1 Univer tomondan import

Har OTM univer admin UI'da:
1. Excel fayl upload qiladi
2. Univer validate qiladi (local)
3. Univer bulk POST qiladi:
   ```
   POST /api/v1/university/{code}/buildings/sync
   Authorization: Bearer <univer-token>
   Content-Type: application/json

   [
     {
       "sourceUid": "univer-bld-123",
       "name": "1-o'quv korpusi",
       "categoryCode": "ACADEMIC",
       "yearBuilt": 1985,
       ...
     },
     ...
   ]
   ```

### 3.2 Expected natijalar:
- **~4,500 bino** har xil OTM'lardan
- **~5-10% failure rate** (coordinates yo'q, invalid data)
- `buildings.sync.count` dashboard'da kuzatish

### 3.3 Backend infrastructure talablari:

| Parametr | Qiymat |
|---|---|
| HikariCP pool | 20 write, 50 read |
| Sync endpoint rate limit | 50 req/min per OTM |
| Timeout | 60 seconds |
| Payload size | Max 5MB (~500 bino per batch) |

---

## 🔧 Faza 4: Correction Round (Hafta 5)

### 4.1 Failed items dashboard
Ministry admin UI'da:
- Filter: `source='univer_sync' AND content_hash IS NULL`
- Ko'rsatadi: "univer tomondan keldi, lekin validation fail"
- Har row'da: sabab + "Tuzatish" tugmasi

### 4.2 Manual completion
- Coordinates yo'q → Ministry admin Google Maps'dan qo'shadi
- Kadastr raqami yo'q → kadastr API search orqali
- Noto'g'ri year/area → OTM bilan bog'lanib aniqlash

### 4.3 Maqsad: **95%+ data coverage** Hafta 5 oxirida

---

## ⚙️ Faza 5: Ongoing Sync (Hafta 6+)

### 5.1 Daily cron (univer tomondan)
- Har kuni 02:00 (kam yuklama) — o'zgargan bino'lar sync
- Hash-based idempotency: o'zgarmaganlar skip

### 5.2 Real-time events
- OTM admin bino qo'shsa/tahrirlasa → darhol POST /sync
- Max latency: 1 daqiqa

### 5.3 Monitoring rules:

| Alert | Kondisiya | Severity |
|---|---|---|
| OTM idle | sync_count == 0 for 7+ days | WARNING |
| Failure spike | failure_rate > 10% | CRITICAL |
| Slow sync | p95 duration > 30s | WARNING |
| Coordinates missing | count(latitude IS NULL) > 100 | INFO |

---

## 📊 Success Metrics

| Metrika | Maqsad | Baseline | Haqiqiy |
|---|---|---|---|
| OTM sync coverage | 100% | 0% | TBD |
| Data completeness | 95% field'lar to'ldirilgan | 0% | TBD |
| Coordinates coverage | 90% bino'lar | 0% | TBD |
| Cadastre auto-fill hit rate | 50%+ | — | TBD |
| Sync failure rate | < 5% | — | TBD |

---

## 🔄 Rollback Plan

Agar Bosqich 2-3 davomida kritik muammo topilsa:

### Level 1: Feature flag disable
```bash
kubectl set env deployment/hemis-back BUILDINGS_MODULE_ENABLED=false
```
Sync endpoint 503 qaytaradi. Univer vaqtincha sync to'xtatadi.

### Level 2: Data rollback
```bash
# Test DB'da — xavfsiz
kubectl exec -it hemis-back-0 -- bash -c "
  ./gradlew :domain:liquibase rollbackCount 1 -Pcontexts=test
"
# Production DB: DBA bilan koordinatsiya
```

### Level 3: Code revert
```bash
git revert <commit-hash>
# Deploy previous build version
```

---

## 📞 Vazifalar taqsimoti

| Rol | Mas'uliyat |
|---|---|
| **Vazirlik rahbariyati** | Deadline qo'yish, OTM'lardan talab qilish |
| **OTM admin (224 ta)** | Excel to'ldirish + univer orqali yuklash |
| **Backend team** | API'ni stabilize qilish, bug fix |
| **Support team (L1)** | Univer admin'lariga yordam |
| **SRE team** | Monitoring, alerting, capacity |
| **DBA team** | Performance, backup |

---

## 📚 Qo'shimcha hujjatlar

- [ADR-001 Table Design](../adr/ADR-001-building-table-design.md)
- [Runbook: Sync Failure](../runbooks/buildings-sync-failure.md)
- [Excel Template](../Бино ва иншоотлар жадвали.xlsx)
- [Swagger API Docs](http://hemis.uz/swagger-ui.html)
