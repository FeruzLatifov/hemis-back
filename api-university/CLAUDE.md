# api-university module — Univer ↔ Markaz Integration Channel

> **Markaziy HEMIS-back** ichida **224 ta Univer Yii2 PHP backend** uchun integratsiya kanali (OTM → vazirlik markaz yo'nalishi).
>
> **Modul roli:** CRUD EMAS — **write/sync-oriented** integratsiya kanali. Univer'lar (per-OTM Yii2) markazga ma'lumot **PUSH** qiladi (employees, buildings sync), token oladi, webhook apply-status ack qaytaradi va kadastr gateway proxy orqali so'rov yuboradi. Bu modulda `/students`, `/faculties`, `/curriculum` kabi CRUD endpoint **YO'Q**, `@Cacheable` **YO'Q** (write/sync xulq).
>
> **3 maqsad (loyiha 4 maqsadidan):**
> 1. **Aggregation:** 224 ta Univer (per-OTM Yii2) ma'lumotini markaziy DB'ga yig'ish (employees/buildings sync → Kafka)
> 2. **Klassifikator distribution:** `h_*` jadvallari (gender, soato, position_type) — markazdan Univer'larga sync (ADR-0006)
> 3. **Qoidalar joriy qilish:** vazirlik biznes konstraint (talaba kiritish vaqt cheklov, baho lock) — Univer'lar markazdan oladi
>
> **Auth:** OAuth 2.1 `client_credentials` (per-OTM `client_id` + secret + IP whitelist) — ADR-0005.
>
> **Mijozlari:** faqat **224 ta Univer Yii2 PHP** (per-OTM client_credentials, `ClientType.UNIVERSITY`).
> Davlat sistemalari (MyGov, MSPD, BIMM, Tax, GUVD) — alohida `api-external` modul (S2S, `ClientType.EXTERNAL_SYSTEM`).

---

## Real Endpoints (6 controller, 7 mapping)

Barchasi `/api/v1/university` prefix ostida.

| Endpoint | Controller | Tavsif |
|----------|-----------|--------|
| `POST /oauth/token` (form) | `UniversityOAuthTokenController#tokenForm` | OAuth 2.1 `client_credentials`, form/multipart body (ADR-0005) |
| `POST /oauth/token` (JSON) | `UniversityOAuthTokenController#tokenJson` | Bir xil issuer, JSON body variant (legacy `univer.php` PHP clientlar uchun) |
| `POST /employees/sync` | `EmployeeSyncController` | Univer xodim batch'i → Kafka `EmployeeSyncProducer.publish` (ADR-0010, idempotent `INSERT ... ON CONFLICT`) |
| `POST /buildings/sync` | `BuildingSyncController` | Univer bino batch'i → `(universityCode, sourceUid)` bo'yicha upsert |
| `POST /hemis-events/ack` | `WebhookAckController` | K2 apply-status feedback loop — Univer markaz webhook'ini apply qilib ack qaytaradi (ADR-0012) |
| `GET /gateway/kadastr/by-cadnum` | `GatewayController` | `api-mspd` kadastr passthrough proxy, `@PreAuthorize("isAuthenticated()")` |
| `GET /health` | `UniversityApiHealthController` | Liveness probe |

### OAuth token (ADR-0005)

`POST /oauth/token` ikki Content-Type qabul qiladi (form-urlencoded/multipart **va** JSON) — ikkalasi ham `OAuthClientTokenIssuer.issue(...)` ga delegate qiladi. `grant_type=client_credentials`, `Authorization: Basic <client_id:secret>`, IP whitelist tekshiruvi. Univer Yii2 PHP clientlar ba'zi versiyada JSON RPC orqali token oladi, shu sabab dual-format.

> **api-external bilan farq:** `api-external` tashqi davlat tizimlari S2S token beradi (`ClientType.EXTERNAL_SYSTEM`); `api-university` — Univer OTM (`ClientType.UNIVERSITY`). Ikkala modul ham bir xil `OAuthClientTokenIssuer.issue` ni ulashadi, faqat client turi va scope farq qiladi.

### Sync endpoints (ADR-0007 Kafka, ADR-0010 employee-sync)

`POST /employees/sync` — kelgan batch har item bo'yicha `EmployeeSyncProducer.publish(batchId, universityCode, syncUser, dto)` orqali Kafka'ga yoziladi (async, future timeout monitoring). Consumer markaziy DB'ga idempotent yozadi: `pinfl` (employee), `(universityCode, sourceUid)` (job) bo'yicha `INSERT ON CONFLICT`. `POST /buildings/sync` — `BuildingSyncService.syncFromUniver(universityCode, items)`, `(universityCode, sourceUid)` upsert.

### Webhook ack (ADR-0012 — K2 feedback loop)

`POST /hemis-events/ack` — markaz outbound webhook yuborgach, Univer event'ni apply qilib **apply-status** ni shu kanal orqali qaytaradi. Auth **JWT EMAS** — HMAC:
- `X-Hemis-Signature` — HMAC imzo
- `X-Hemis-Timestamp` — replay himoyasi
- `X-Hemis-University-Code` — OTM identifikatori

`WebhookAckService.processAck(universityCode, signature, timestamp, rawBody)` raw body ustidan imzoni tekshiradi.

### Gateway proxy

`GET /gateway/kadastr/by-cadnum` — `api-mspd` kadastr xizmatiga passthrough proxy (Univer foydalanuvchisi kadastr raqami bo'yicha so'rov yuboradi). `@PreAuthorize("isAuthenticated()")`.

---

## universityCode resolution — spoofing himoyasi

`universityCode` **hech qachon URL'dan kelmaydi** (caller spoofing'ning oldini olish — ministry convention). Sync endpointlarda ikki bosqichli resolution:

1. **Production:** JWT `university_code` claim (OAuth2 `client_credentials` token'idan).
2. **Dev/test fallback:** `X-University-Code` HTTP header (faqat dev profile, security filter `permitAll`, localhost).

Hech biri topilmasa → `401` (`universityCode aniqlanmadi`).

```java
// EmployeeSyncController / BuildingSyncController — resolveUniversityCode(request)
if (auth instanceof JwtAuthenticationToken jwtAuth) {
    String fromToken = jwtAuth.getToken().getClaimAsString("university_code");
    if (fromToken != null) return fromToken;
}
String fromHeader = request.getHeader("X-University-Code");   // dev fallback
if (fromHeader != null) return fromHeader;
throw new ...("universityCode aniqlanmadi — JWT 'university_code' claim yoki 'X-University-Code' header kerak");
```

`hemis-events/ack` esa universityCode'ni HMAC bilan birga `X-Hemis-University-Code` header'dan oladi (JWT yo'q).

---

## Markaziy DB + OTM Scope

> Klassik "multi-tenant deploy" EMAS — markaziy server, bitta DB, rows-level isolation `university_code` filter orqali. Har Univer client (224 ta) o'z `university_code` bilan kelgan ma'lumotini yozadi/yangilaydi.

Sync entity'lar markaziy DB'da `university_code` column bilan yoziladi; vazirlik aggregation reportlari 224 OTM bo'yicha bitta SQL bilan olinadi.

---

## ADR bog'lanishlar

- **ADR-0005** — OAuth `client_credentials` token issuance (`OAuthClientTokenIssuer`)
- **ADR-0007** — Kafka sync (outbox/producer/consumer/DLQ)
- **ADR-0010** — employee-sync (idempotent `ON CONFLICT`, job tarixi)
- **ADR-0012** — K2 webhook apply-status ack (HMAC feedback loop)

---

## PR Checklist

- [ ] Sync endpoint — `universityCode` JWT claim'dan (URL'da EMAS); dev fallback faqat `X-University-Code`
- [ ] Sync yozuvi idempotent (`ON CONFLICT` / upsert key)
- [ ] Kafka publish — future timeout/error handling (ADR-0007/0010)
- [ ] Webhook ack — HMAC (`X-Hemis-Signature` + timestamp + university-code) tekshiriladi, JWT emas
- [ ] Gateway endpoint — `@PreAuthorize("isAuthenticated()")`
- [ ] Yangi OAuth client turi to'g'ri (`ClientType.UNIVERSITY`)
- [ ] CRUD/`@Cacheable` qo'shilmadi (bu modul write/sync-oriented)

---

## See Also
- `../api-external/CLAUDE.md` — tashqi davlat S2S token (`ClientType.EXTERNAL_SYSTEM`)
- `../security/CLAUDE.md` — OAuth issuer, `@PreAuthorize` patterns
- `../api-mspd/CLAUDE.md` — kadastr gateway upstream
