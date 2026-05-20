---
id: ADR-0009
status: implemented
date: 2026-05-07
revised: 2026-05-18
deciders: hemis-team
agent: claude-code
model: claude-opus-4-7
affects: [security, app]
liquibase: []
entities: []
verification: |
  # Access token TTL 1h ekanligi
  grep "access-token-validity\|accessTokenValiditySeconds" app/src/main/resources/application.yml
related: [ADR-0005]
---

# ADR 0009: JWT Access Token TTL Reduction (12h → 1h)

## Status

**Proposed** (2026-05-07, qisqartirilgan: 2026-05-18 — over-engineering trim).

> **Asl ADR 5 ta sub-feature'ni bir paketda taklif qilgan edi:** access TTL, refresh rotation,
> `jti` claim, `kid` header, key rotation cron. Realiteti — 0 satr kod 11 kun.
>
> **Qaror (2026-05-18 audit):** Ushbu ADR faqat **eng kritik va arzon** o'zgarishni
> saqlaydi — access TTL 12h → 1h. Qolgan 4 ta feature (refresh, jti, kid, key rotation)
> **DEFERRED** — real talab tug'ilganda alohida ADR sifatida qayta ko'rib chiqiladi
> (ADR-0009b, ADR-0009c).

## Context

**Hozirgi holat:**
```yaml
hemis.jwt.access-token-validity: 43200   # 12 soat
```

Vazirlik markaziy server uchun **12 soatlik access token TTL** — milliy darajadagi xavf.
Token leak (XSS, browser cache, network sniffing) → 12 soat to'liq foydalanuvchi sifatida ishlatish mumkin.

**Industry standart (OAuth 2.1 BCP, RFC 9700):** access token 5 min – 1 soat.

## Decision

**Bitta o'zgarish:** access token TTL `12h → 1h`.

```yaml
hemis.jwt.access-token-validity: 3600   # 1 soat
```

**Foyda:** token leak compromise window 92% kamayadi (43200s → 3600s).

**UX impact:** Foydalanuvchi har 1 soatda re-login qilishi kerak. Bu **qabul qilinadi** chunki:
- Vazirlik admin paneli (50-100 active admin) — kichik audience
- 224 OTM Univer `client_credentials` ishlatadi (ADR-0005) — re-login emas, avtomatik token regeneration
- Refresh token (alohida ADR-0009b'da) — UX yumshatish uchun keyinroq qo'shilishi mumkin

## Deferred sub-decisions

Asl ADR'dan **olib tashlangan** narsalar (real talab kerak bo'lganda alohida ADR):

| Sub-decision | Sabab DEFER | Re-evaluate trigger |
|--------------|-------------|---------------------|
| **Refresh token rotation** | Hozirgi UX (1h re-login) qabul qilinadi. Frontend interceptor + Yii2 PHP refresh logic — katta scope | Foydalanuvchi shikoyat qilsa yoki active admin > 500 bo'lsa |
| **`jti` claim (UUID)** | Redis blacklist hozirgi keylar bilan ishlaydi (500 bayt × 100 admin = 50KB, ahamiyatsiz) | Active session > 10K yoki Redis memory pressure |
| **`kid` header + key rotation cron** | HMAC secret rotation amaliyoti hali yo'q. Bu speculative | Secret rotation tartibi kiritilganda |
| **OAuth 2.0 Spring Authorization Server** | Katta refactor (3-6 oy), hozirgi infra ishlaydi | Spring infra eskirganda |

## Alternatives Considered

### Alternative 1: Status quo (12h)
- ❌ Vazirlik darajasidagi compliance (UZ qonunchilik + SOC2/ISO 27001) bilan zid
- ❌ Token leak window juda uzoq (12 soat)
- **Rad etish sababi:** security risk minimization majburiy.

### Alternative 2: 5 minute access TTL
- ✅ OAuth 2.1 BCP "5min – 1h" diapazonining quyi chegarasi
- ❌ Refresh token bo'lmasdan — har 5 minutda re-login UX katastrofa
- **Rad etish sababi:** Refresh token DEFERRED, hozir 1h optimal balance.

### Alternative 3: Asl ADR (5 sub-feature bir paketda) — SUPERSEDED 2026-05-18
- ❌ 0 satr kod 11 kun (proposed → no impl)
- ❌ Refresh + jti + kid + key rotation = 5 ta alohida feature, big-bang risk
- ❌ Frontend (React) + Univer 224 OTM PHP — multi-team coordination
- **Rad etish sababi:** over-engineering. Birinchi navbatda eng arzon-ROI o'zgarishni amalga oshirish, qolganlari kerak bo'lganda alohida.

## Consequences

### Positive
- ✅ Token leak window 12h → 1h (92% kamayish)
- ✅ Vazirlik compliance yaxshilanadi
- ✅ Bitta config satr o'zgarishi — risk past, deploy oson

### Negative
- ⚠️ Admin har 1 soatda re-login (UX impact)
- ⚠️ Logout/refresh tugmasi yo'q — re-login majburiy
- ⚠️ Mitigatsiya (refresh token) DEFERRED — UX shikoyat bo'lsa alohida ADR ochiladi

### Neutral
- Univer 224 OTM ta'sir qilmaydi (ADR-0005 client_credentials, avtomatik token regeneration)
- Backend kod o'zgarmaydi — faqat config
- Frontend axios interceptor 401 ni darhol login sahifasiga yo'naltirsa kifoya

## Implementation

### Bosqich 1: Config o'zgarishi (1 commit, 5 daqiqa)
- [ ] `application.yml` `hemis.jwt.access-token-validity: 3600`
- [ ] `JwtProperties` default qiymat yangilash (agar bor)
- [ ] Acceptance test: token TTL = 3600 ekanini tasdiqlash

### Bosqich 2: Frontend communication (1 hafta)
- [ ] Frontend (React) team xabardor qilish: 401 → login redirect
- [ ] Documentation: "Sessiya 1 soat — re-login talab qilinadi"
- [ ] Monitoring: 401 rate metric (Grafana dashboard)

### Bosqich 3: Production rollout
- [ ] Staging deploy + smoke test
- [ ] Production rollout (off-peak — yarim tunda)
- [ ] Monitor: 401 spike, user complaint rate

## Verification

```bash
# 1. Access token TTL 1h
grep "access-token-validity" app/src/main/resources/application.yml
# Expected: 3600

# 2. Integration test
./gradlew :security:test --tests "*Token*"
```

**Acceptance criteria:**
- [ ] Access token TTL = 1h (3600s)
- [ ] Univer 224 OTM regression test (175/175 MATCH)
- [ ] 24-soat production monitoring — 401 rate baseline

## References

- Code: `app/src/main/resources/application.yml` (`hemis.jwt.*`)
- Code: `security/src/main/java/uz/hemis/security/service/TokenService.java`
- RFC 9700: Best Current Practice for OAuth 2.0 Security
- OAuth 2.1: https://oauth.net/2.1/
- Related: ADR-0005 (OAuth client_credentials — Univer ta'sir qilmaydi)

## Deferred (future ADRs)

- **ADR-0009b (kelajakda):** Refresh token rotation — UX shikoyat bo'lganda
- **ADR-0009c (kelajakda):** `jti` claim + Redis blacklist optimization — memory pressure bo'lganda
- **ADR-0009d (kelajakda):** `kid` header + secret rotation cron — rotation amaliyoti kiritilganda
