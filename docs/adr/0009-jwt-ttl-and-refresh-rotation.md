---
id: ADR-0009
status: proposed
date: 2026-05-07
deciders: hemis-team
agent: claude-code
model: claude-opus-4-7
affects: [security, app, api-web, api-university, api-legacy]
liquibase: []
entities: [TokenBlacklistEntry]
verification: |
  # 1. Access token TTL 1h ekanligi
  grep "access-token-validity\|accessTokenValiditySeconds" app/src/main/resources/application.yml
  # 2. Refresh token endpoint mavjudligi
  grep -rn "POST.*refresh\|/oauth/refresh" security/src/main/java api-*/src/main/java
  # 3. jti claim TokenService'da
  grep -n "claim(\"jti\"\|.id(jti)" security/src/main/java/uz/hemis/security/service/TokenService.java
  # 4. kid header TokenService'da
  grep -n "JwsHeader.*keyId\|kid" security/src/main/java/uz/hemis/security/service/TokenService.java
related: [ADR-0005]
---

# ADR 0009: JWT TTL qisqartirish + Refresh token rotation + jti/kid

## Status

Proposed (2026-05-07)

> **Y-Statement:** Vazirlik markaziy server JWT token security uchun: access token 12h TTL'ni 1h'ga qisqartirib, refresh token rotation + `jti` claim + `kid` header qo'shamiz, chunki 12h leak'lik milliy darajadagi xavf, oqibatda token compromise window 92% kamayadi (12h→1h).

## Context

**Hozirgi holat (`security/service/TokenService.java`):**

```yaml
hemis.jwt.access-token-validity: 43200   # 12 soat
```

- **Access token TTL:** 12 soat (juda uzoq markaziy ministry server uchun)
- **Refresh token:** YO'Q (re-login har 12 soat)
- **`jti` claim:** YO'Q — Redis blacklist butun token'ni saqlaydi (~500 bayt key)
- **`kid` header:** YO'Q — JWT secret rotation qilishda eski tokenlar darhol invalid

**Markaziy ministry server xavfi:**
- 1.15M talaba metadata + 5K admin × 230 OTM bo'ylab
- Token leak (XSS, network sniffing, browser cache) → 12 soat foydalanuvchi sifatida ishlatish mumkin
- Vazirlik darajasi compliance (UZ qonunchilik + SOC2/ISO 27001) qisqaroq TTL talab qiladi

**Industry standart (OAuth 2.1 BCP, RFC 9700):**
- Access token: **5 min – 1h**
- Refresh token: 12h – 7 kun (rotated har refresh)
- `jti` claim: blacklist key minimization
- `kid` header: graceful key rotation

## Decision

3 ta o'zgarish bir sprint ichida implement qilinadi:

### 1. Access + Refresh token split

```yaml
hemis.jwt.access-token-validity: 3600       # 1 soat (12h dan 1h)
hemis.jwt.refresh-token-validity: 43200     # 12 soat (yangi)
```

**Logout/refresh oqimi:**
```
POST /api/v1/auth/login        → {access_token (1h), refresh_token (12h)}
POST /api/v1/auth/refresh      → eski refresh blacklist + yangi pair
POST /api/v1/auth/logout       → access + refresh ikkalasi blacklist
```

### 2. `jti` claim — blacklist key optimization

```java
String jti = UUID.randomUUID().toString();
JwtClaimsSet claims = JwtClaimsSet.builder()
    .id(jti)                    // ← yangi: 36-char UUID
    .issuer(issuer)
    .subject(userId)
    // ... boshqa claim'lar
    .build();

// Redis blacklist key:
// Eski: "token:blacklist:eyJhbGciOiJIUzI1NiIs..." (~500 bayt)
// Yangi: "token:blacklist:550e8400-e29b-41d4..." (36 bayt)
```

**Foyda:** Redis xotira 92% tejash, faster lookup.

### 3. `kid` header — Key Rotation Support

```java
JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256)
    .keyId("hemis-jwt-2026-q2")    // ← yangi: kalit ID
    .build();
```

**Multi-key parallel ishlash (graceful rotation):**
- Eski key `2026-q1` — 7 kun overlap (eski tokenlar hali valid)
- Yangi key `2026-q2` — yangi tokenlar shu key bilan
- Eski key 7 kun keyin disable

## Alternatives Considered

### Alternative 1: Status quo (12h, refresh yo'q)

- ✅ Hech qanday ish yo'q
- ❌ Markaziy ministry xavfsizlik standartiga zid
- ❌ Token leak window juda uzoq
- **Rad etish sababi:** Compliance va security risk minimization vazirlik darajasi.

### Alternative 2: Faqat access TTL qisqartirish (refresh yo'q)

- ✅ Sodda
- ❌ User experience yomonlashadi (har 1 soatda re-login)
- ❌ 5K admin × 230 OTM session interrupt
- **Rad etish sababi:** UX ↔ security balance refresh token bilan optimal.

### Alternative 3: Sliding session (har request'da TTL extend)

- ✅ User experience yaxshi
- ❌ DB hit har request'da
- ❌ Stateless JWT printsipini buzadi
- **Rad etish sababi:** Markaziy server 1000+ rps → DB load yomonlashadi.

### Alternative 4: OAuth 2.0 to'liq Spring Authorization Server

- ✅ RFC standart
- ❌ Katta refactor (3-6 oy)
- ❌ ADR-0004 (api-university) bilan parallel ish ko'p
- **Rad etish sababi:** Hozirgi infra bilan kichik delta + sprint scope.

## Consequences

### Positive

- ✅ Token leak compromise window 12h → 1h (92% kamayish)
- ✅ Refresh token rotation — old refresh blacklist har refresh'da
- ✅ Redis blacklist 36 bayt key (`jti`) — xotira tejash
- ✅ Graceful key rotation (`kid`) — secret leak'da darhol downtime yo'q
- ✅ OAuth 2.1 BCP / RFC 9700 mosligi
- ✅ Vazirlik compliance (SOC2, ISO 27001)

### Negative

- ⚠️ Frontend (React) refresh token logic implement qilishi kerak (interceptor)
- ⚠️ 224 Univer Yii2 PHP backend (`HemisApi.php`) — refresh oqimi qo'shish
- ⚠️ Token migration sprint paytida user re-login zarur (1-3 daqiqa downtime)

### Risks

- **Risk:** Refresh token leak ham xavfli (12h)
  **Mitigation:** Refresh token rotation har refresh'da → bir martalik. Anomaly detection (IP/UA mismatch) — refresh refuse + force re-login.

- **Risk:** Univer 224 OTM PHP kod yangilash kerak
  **Mitigation:** ADR-0005 OAuth client_credentials migration parallel — Univer uchun refresh token ham client_credentials grant orqali (avtomatik token regeneration, refresh kerak emas).

- **Risk:** Existing session'lar implement vaqtida invalid
  **Mitigation:** `kid` overlap 7 kun — eski tokenlar 7 kun ichida amaldagi. Yangi kid har yangi login'da.

## Implementation

### Bosqich 1: Tayyorgarlik (1 hafta)
- [ ] `JwtProperties` ga `refreshTokenValiditySeconds` qo'shish
- [ ] `application.yml` access TTL 12h → 1h
- [ ] `TokenService.generateToken()` ga `jti` UUID
- [ ] `TokenService.generateToken()` ga `JwsHeader.keyId(...)`
- [ ] `JwtDecoder` `kid` header validation

### Bosqich 2: Refresh endpoint (1 hafta)
- [ ] `TokenService.issueTokenPair(userId)` — access + refresh
- [ ] `RefreshController` — `POST /api/v1/auth/refresh`
- [ ] Old refresh → blacklist; new pair issue
- [ ] Frontend axios interceptor (401 → /refresh → retry)

### Bosqich 3: Univer parallel (ADR-0005 bilan)
- [ ] api-legacy `password` grant — `refresh_token` ham qaytaradi
- [ ] Univer `HemisApi.php` da refresh logic
- [ ] Yoki: Univer'lar `client_credentials` (ADR-0005 Stage 2-5)

### Bosqich 4: Key rotation infra (1 hafta)
- [ ] `JwtKeySet` Redis (multi-key support)
- [ ] Cron task — har 90 kunda yangi `kid` generate
- [ ] Eski `kid` 7 kun overlap → keyin disable

### Bosqich 5: Production rollout
- [ ] Staging deploy + load test
- [ ] Production rollout (off-peak — yarim tunda)
- [ ] Monitor: refresh latency, blacklist Redis hit ratio, key rotation health

## Verification

```bash
# 1. Access token TTL 1h
grep "access-token-validity" app/src/main/resources/application.yml
# Expected: 3600

# 2. jti claim
grep "claim(\"jti\"\|\.id(jti)" security/src/main/java/uz/hemis/security/service/TokenService.java

# 3. kid header
grep "keyId" security/src/main/java/uz/hemis/security/service/TokenService.java

# 4. Refresh endpoint
grep -rn "POST.*refresh\|/oauth/refresh" security/src/main/java api-web/src/main/java

# 5. Integration test
./gradlew :security:test --tests "*TokenRefreshTest*"
```

**Acceptance criteria:**
- [ ] Access token TTL = 1h (3600s)
- [ ] Refresh token TTL = 12h (43200s)
- [ ] `jti` claim har token'da
- [ ] `kid` header har token'da
- [ ] `POST /api/v1/auth/refresh` ishlamoqda
- [ ] Old refresh blacklist har refresh'da
- [ ] Frontend interceptor tested
- [ ] Univer 224 OTM regression test (175/175 MATCH)

## References

- Code: `security/src/main/java/uz/hemis/security/service/TokenService.java`
- Code: `security/src/main/java/uz/hemis/security/service/TokenBlacklistService.java`
- Code: `app/src/main/resources/application.yml` (`hemis.jwt.*`)
- RFC 9700: Best Current Practice for OAuth 2.0 Security
- OAuth 2.1: https://oauth.net/2.1/
- Related ADRs: ADR-0005 (OAuth client_credentials)
- `security/CLAUDE.md` — JWT Modernization tavsiyalari
