# Univer ↔ HEMIS-back Integratsiya Oqimi

> **Univer** = `/home/adm1n/projects/startup/univer` (Yii2 framework + PHP 7.4) — har 230 ta OTM da **alohida deploy**, har biri o'z `hemis_NNN` lokal DB bilan.
>
> **HEMIS-back** = vazirlik darajasidagi **markaziy** Spring Boot 4 + Java 25 server (1 deploy/cluster).
>
> **Bizning loyiha (HEMIS-back) Univer'ni qayta yozmaydi** — faqat backend (old-hemis CUBA Java) modernizatsiyasi.

---

## 1. Deploy modeli

```
                ┌────────────────────────────────────────────────┐
                │  HEMIS-back (MARKAZIY — vazirlik)              │
                │  ┌────────────────────────────────────────┐    │
                │  │  Spring Boot 4 + Java 25 (3 instances) │    │
                │  │  ↳ api-legacy   (/app/rest/v2/*)        │    │
                │  │  ↳ api-web      (/api/v1/web/*)         │    │
                │  │  ↳ api-university (/api/v1/university/*)│    │
                │  │  ↳ api-external  (/api/v1/external/*)   │    │
                │  └────────────────────────────────────────┘    │
                │  PostgreSQL: hemis (env: DB_MASTER_NAME)       │
                │  Redis cluster (markaziy cache)                │
                │  PostgreSQL: hemis_audit (markaziy audit)      │
                └────────────┬───────────────────────────────────┘
                             ▲
                             │ HTTPS REST API (network)
                             │
       ┌─────────────────────┼─────────────────────┐
       │                     │                     │
┌──────▼──────┐      ┌───────▼─────┐      ┌────────▼────┐
│ OTM #337    │      │ OTM #401    │ ...  │ OTM #NNN    │
│ ─────────── │      │ ─────────── │      │ ─────────── │
│ Univer      │      │ Univer      │      │ Univer      │
│ (Yii2 PHP   │      │ (Yii2 PHP   │      │ (Yii2 PHP   │
│  7.4)       │      │  7.4)       │      │  7.4)       │
│             │      │             │      │             │
│ DB:         │      │ DB:         │      │ DB:         │
│ hemis_337   │      │ hemis_401   │      │ hemis_NNN   │
└─────────────┘      └─────────────┘      └─────────────┘
   per-OTM              per-OTM             per-OTM
```

**Asosiy raqamlar:**
- 230 ta OTM — vazirlik tasarrufida (umumiy)
- 224 ta — Univer Yii2 PHP ishlatuvchi (api-legacy/api-university mijozlari)
- 6 ta — boshqa stack (Univer'siz)

---

## 2. Integratsiya yo'nalishlari (3 ta)

### 🔵 Yo'nalish #1: INBOUND (Univer → HEMIS-back)

Univer foydalanuvchi (admin, o'qituvchi) action qiladi → Univer PHP markaziy serverga ma'lumot yuboradi.

**Misol:** Univer admin yangi xodimni qo'shadi
```
1. Univer UI form: yangi employee_jobs yozuvi
2. Univer PHP (HemisApi.php) → POST https://hemis-back/app/rest/v2/entities/hemishe_EEmployeeJobs
3. HEMIS-back api-legacy controller:
   - Auth tekshirish (JWT yoki client_credentials)
   - University scope filter (faqat o'z OTM)
   - Qoida tekshirish (talaba kiritish vaqti, baho lock)
   - INSERT INTO hemishe_e_employee_jobs (eski jadval — Univer keyin shu yerdan o'qiydi)
4. CUBA format response (LinkedHashMap, _entityName, _instanceName)
5. Univer keyingi GET: shu jadvaldan o'qib UI ko'rsatadi
```

**Volume (taxminiy):**
- 224 OTM × 100 student/day = ~22,000 POST/day
- 224 OTM × 10 teacher update/day = ~2,200 POST/day
- Peak (semestr boshi): ~50 OTM simultaneously

### 🟢 Yo'nalish #2: OUTBOUND #1 (HEMIS-back → Univer)

Markaziy server tomondan klassifikator yangilanish, qoidalar push, notification.

**Hozirgi holat:** REST back-channel YO'Q. Univer cron orqali pull qiladi.

**Kelajak (ADR-0007 Kafka):**
```
1. Vazirlik admin: h_position klassifikatorga yangi qiymat qo'shadi (HEMIS-back)
2. Kafka topic: outbound.classifier.events.v1
3. Univer (per-OTM) Kafka consumer → o'z lokal hemis_NNN ga sync
4. Yoki: REST pull endpoint /api/v1/university/classifiers/h_position?since=<ts>
```

**Push misollari:**
- Klassifikator yangilanishi (gender, soato, position_type)
- Qoidalar (talaba kiritish vaqti boshlandi/yopildi)
- Notification (vazirlikdan OTM ga xabar)

**Volume:**
- 230 OTM × 50 classifier sync = ~11,500 events/day
- Qoidalar push (event-driven): ~100 events/day

### 🟡 Yo'nalish #3: OUTBOUND #2 (HEMIS-back ↔ Davlat sistemalari)

Markaziy server davlat tashkilotlari bilan S2S integratsiya (api-external modul).

**Outbound misollari:**
- `MyGov` — yagona kirish (OneID auth federation)
- `MSPD` — sotsiologik tekshirish (yetim, nogiron)
- `BIMM` — sertifikatlar
- `Tax/Soliq` — sub'ekt PINFL check
- `GUVD` — passport ma'lumoti

**Inbound misollari:**
- Vazirlik markaziy talaba qo'shilishi cheklovlari (top-down qaror)
- MSPD sotsial monitoring webhook

**Volume:**
- MyGov auth: ~5,000 calls/day
- MSPD verify: ~1,000 calls/day
- Tax PINFL check: ~2,000 calls/day

---

## 3. Auth oqimi (224 OTM Univer)

### Hozirgi: `password` grant (deprecated, ADR-0005)

Har OTM Univer Yii2'ning `users` jadvalida `username` (masalan `tatu_otm`) saqlanadi. Univer login form orqali user kiradi.

```php
// /home/adm1n/projects/startup/univer/common/components/hemis/HemisApi.php:728
$response = $this->_client->post('v2/oauth/token', [
    'grant_type' => 'password',
    'username'   => $otm_username,
    'password'   => $otm_password,
])->send();

// JWT keladi → keyingi REST chaqiruvlarda Authorization header
$jwt = $response->getData()['access_token'];
```

**Muammolar:**
- `password` grant OAuth 2.1 da deprecated
- 224 ta machine account `users` jadvalida HUMAN bilan aralash
- Secret rotation manual
- IP whitelist yo'q

### Kelajak: `client_credentials` (api-university, ADR-0005)

Har OTM uchun alohida `oauth_client` yozuvi (markaziy DB):
```sql
INSERT INTO oauth_client (client_id, client_secret_hash, university_code,
                          allowed_ip_cidr, rate_limit_rpm, grant_types)
VALUES ('univer_337', crypt(:secret, gen_salt('bf', 12)), '337',
        ARRAY['10.0.0.0/8'], 300, ARRAY['client_credentials']);
```

```php
// Univer feature flag bilan yangi formatga o'tadi
if (Config::get('USE_CLIENT_CREDENTIALS')) {
    $response = $this->_client->post('/api/v1/university/oauth/token', [
        'grant_type'    => 'client_credentials',
        'client_id'     => 'univer_337',
        'client_secret' => $secret,
    ])->send();
}
```

**Migration plan:** 12 oy parallel ishlash, har OTM o'z tezligida.

---

## 4. URL pattern va modul taqsimoti

| URL | Modul | Auth | Mijoz | Format |
|-----|-------|------|-------|--------|
| `/app/rest/v2/*` | api-legacy | JWT (`password` grant) | 224 Univer Yii2 | CUBA legacy (`hemishe$Entity`, LinkedHashMap, `_entityName`) |
| `/api/v1/web/*` | api-web | JWT (browser session) | Vazirlik admin React | Modern REST (camelCase, ResponseWrapper) |
| `/api/v1/university/*` | api-university | OAuth client_credentials | 224 Univer (yangi format) | Modern REST |
| `/api/v1/external/*` | api-external | API Key + IP whitelist | MyGov, MSPD, BIMM, Tax, GUVD | Davlat schema |

### api-legacy ↔ api-university o'zgarish

**12 oy parallel:**
- Eski: `/app/rest/v2/oauth/token` + `password` grant — 224 Univer hozir shu yerda
- Yangi: `/api/v1/university/oauth/token` + `client_credentials` — har OTM bosqichli ko'chadi

**Sunset header (eski endpoint):**
```http
HTTP/1.1 200 OK
Sunset: Sat, 31 Dec 2027 23:59:59 GMT
Deprecation: true
Link: </api/v1/university/oauth/token>; rel="successor-version"
```

---

## 5. Schema mapping (KRITIK — Golden Rule #3)

### api-legacy → faqat eski jadvallar

Univer Yii2 PHP `hemishe_EStudent`, `hemishe_EEmployeeJobs` URL'larida POST/GET qiladi va **shu nomli jadval shaklida javob kutadi**.

```
✅ TO'G'RI:
Univer POST /app/rest/v2/entities/hemishe_EEmployeeJobs
  → api-legacy controller INSERT INTO hemishe_e_employee_jobs (eski jadval)
  → Univer keyingi GET shu jadvaldan o'qib chiqaradi

❌ XATO (split-brain bug):
Univer POST /app/rest/v2/entities/hemishe_EEmployeeJobs
  → api-legacy YANGI employee_job jadvalga yozadi
  → Univer keyin hemishe_e_employee_jobs dan o'qiydi → BO'SH
  → "Yangi xodim qo'shildi, lekin ko'rinmaydi"
```

**Hozirgi 3 buzilgan import** (ADR-0008): User/Employee/EmployeeJobs api-legacy ichida yangi schema. Bosqichma-bosqich tuzatilmoqda.

### api-university → faqat yangi schema

```
✅ TO'G'RI:
Univer (yangi format) POST /api/v1/university/{code}/employee-jobs
  → api-university controller INSERT INTO employee_job (yangi)
  → Yangi format response (camelCase, UUID)
```

---

## 6. Backward compatibility (api-legacy)

### 175/175 contract test (CRITICAL)

`/home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js`

Har api-legacy o'zgartirishdan keyin majburiy:
```bash
node /home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js
# Maqsad: 175/175 MATCH
```

Test old-hemis (CUBA Java :8082) va HEMIS-back (Spring Boot :8081) ni real Univer scenariosida solishtiradi:
- HTTP status
- Response body shape
- Field tartib (LinkedHashMap)
- `_entityName`, `_instanceName`
- FK nested object format
- Datetime: `yyyy-MM-dd'T'HH:mm:ss.SSS`

### CUBA legacy format (api-legacy)

```java
// ✅ TO'G'RI — LinkedHashMap (tartib saqlanadi)
private Map<String, Object> toMap(Student e) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("_entityName", "hemishe$Student");
    m.put("_instanceName", buildInstanceName(e));
    m.put("id", e.getId().toString());
    m.put("createTs", formatDateTime(e.getCreatedAt()));     // CUBA pattern
    m.put("_employee", nestedRef(e.getEmployee()));           // {"id": "uuid"}
    return m;
}

// ❌ HashMap — tartib yo'qoladi
// ❌ MapStruct — CUBA dynamic field bilan ishlamaydi (api-legacy 0 ta MapStruct)
```

---

## 7. Per-OTM filter (Multi-tenant)

Markaziy DB ichida 230 OTM ma'lumoti. Har OTM admin faqat o'z `university_code` rows'ini ko'radi (rows-level isolation).

### Implementation

```java
@GetMapping
@Transactional(readOnly = true)
public ResponseEntity<List<Map<String, Object>>> getAll(...) {
    String universityCode = authFacade.getCurrentUser().getUniversity().getCode();
    Page<EmployeeJobs> page = repository.findByUniversityCode(universityCode, pageable);
    return ResponseEntity.ok(toMapList(page.getContent()));
}
```

### Entity vs Classifier

| Endpoint turi | Filter kerakmi? | Sabab |
|---------------|-----------------|-------|
| Entity (EStudent, EEmployeeJobs) | HA | Per-OTM ma'lumot |
| Classifier (HGender, HPositionType) | YO'Q | Markaziy yagona qiymat (har OTM bir xil) |
| Service (create/update) | HA | Yangi yozuv yaratadi |

---

## 8. Tegishli ADR va hujjatlar

| ADR/hujjat | Mavzu |
|------------|-------|
| [ADR-0004](../docs/adr/0004-api-university-module.md) | api-university yangi modul (224 OTM B2B) |
| [ADR-0005](../docs/adr/0005-oauth-client-credentials.md) | OAuth client_credentials migration (12 oy plan) |
| [ADR-0007](../docs/adr/0007-sync-architecture-evolution.md) | Kafka markaziy aggregation backbone |
| [ADR-0008](../docs/adr/0008-api-legacy-entity-rebinding.md) | api-legacy entity ownership (split-brain fix) |
| [`docs/UNIVER_CONTRACT.md`](../docs/UNIVER_CONTRACT.md) | 67 frozen endpoint contract |
| [`api-legacy/CLAUDE.md`](../api-legacy/CLAUDE.md) | CUBA format qoidalari |
| [`.claude/ENDPOINT_PORTING_GUIDE.md`](ENDPOINT_PORTING_GUIDE.md) | Endpoint ko'chirish workflow |
| `compare_endpoints.js` | 175/175 contract test |

---

## 9. Tezkor savol-javob (FAQ)

**S: HEMIS-back har OTM da deploy qilinadimi?**
J: YO'Q. HEMIS-back markaziy (vazirlik darajasidagi 1 deploy/cluster). Univer per-OTM (230 ta).

**S: `hemis_337` qaysi serverda?**
J: OTM 337 ning Univer Yii2 PHP server'ida (lokal). Bu HEMIS-back DB EMAS.

**S: Univer'ni qayta yozamizmi?**
J: YO'Q. Faqat backend (old-hemis CUBA Java → HEMIS-back Spring Boot) modernizatsiyasi.

**S: 6 ta OTM (230-224) Univer'siz nima qiladi?**
J: Boshqa stack (web admin orqali yoki vazirlik bevosita boshqaradi).

**S: api-legacy nima uchun saqlanmoqda?**
J: 224 Univer Yii2 PHP eski URL/format kutadi. 12 oy parallel ishlash, keyin api-university'ga to'liq ko'chish.

**S: Auth qanday — Univer foydalanuvchi har safar login qiladimi?**
J: Univer Yii2 PHP backend session boshida HEMIS-back'dan JWT oladi (yoki yangi: client_credentials), keyingi REST chaqiruvlarda shu token. Token TTL ~1 soat.
