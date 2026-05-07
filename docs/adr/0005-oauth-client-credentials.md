---
id: ADR-0005
status: in-progress
date: 2026-05-04
deciders: hemis-team
agent: human
affects: [api-university, security, api-legacy]
liquibase:
  - V006_create_users.sql  # oauth_client jadval
entities: [OAuthClient, OAuthClientRole]
verification: ./scripts/check_table_mappings.sh
related: [ADR-0004]
---

# ADR 0005: OAuth `client_credentials` grant 224 OTM B2B uchun

## Status

Accepted (server-side: 2026-05-04); 224 OTM rollout — In Progress

**Implementation:**
- ✅ Server tomon (`OAuthClient`, `OAuthClientAuthenticationService`, `UniversityOAuthTokenController`) — to'liq
- ⏳ 224 OTM PHP feature flag — bosqich 2 boshlanmagan
- ⏳ Canary deploy — bosqich 3
- ⏳ Decommission (12 oy) — bosqich 5

## Context

**Hozirgi holat:** 224 ta per-OTM Univer Yii2 PHP backend (`/home/adm1n/projects/startup/univer/` — har OTM da alohida deploy) **MARKAZIY** (vazirlik darajasidagi) HEMIS-back'ga shu pattern bilan ulanadi:

```php
// HemisApi.php:724-735
$response = $this->_client->post('v2/oauth/token', [
    'grant_type' => 'password',           // ← deprecated grant
    'username'   => $otm_username,        // ← users jadvalga qaraydi
    'password'   => $otm_password,
]);
```

**Auth oqimi:**
1. PHP yuboradi: `username + password`
2. HEMIS-back: `HybridUserDetailsService` → `users` jadval (4 ustun: username, password, enabled, account_non_locked)
3. JWT token qaytadi

**Muammolar:**
- ❌ OAuth 2.1 spec'da `password` grant deprecated
- ❌ `users` jadvalda 224 ta machine accounts (HUMAN bilan aralash)
- ❌ Secret rotation manual (har OTM o'zi parolini yangilashi kerak)
- ❌ IP whitelist yo'q (har joydan kirish mumkin)
- ❌ Rate limit statik (60/min — barcha OTM uchun bir xil)
- ❌ MFA mantiqsiz machine uchun, lekin `users` da MFA logic bor (kelajakda HUMAN uchun yoqilsa, OTM ham buziladi)

## Decision

224 OTM autentifikatsiyasini **`oauth_client` jadval + `client_credentials` grant**'ga ko'chiramiz.

```
ESKI:                                  YANGI:
POST /app/rest/v2/oauth/token          POST /api/v1/university/oauth/token
grant_type=password                    grant_type=client_credentials
username=tatu_otm                      client_id=univer_337
password=xxxxx                         client_secret=xxxxx

→ users jadval                         → oauth_client jadval
```

**Migration strategy:** Strangler Fig pattern, 12 oy davom etadi.

### Auth oqimi taqqoslash

| Aspekt | Eski (password grant) | Yangi (client_credentials) |
|--------|------------------------|------------------------------|
| URL | `/app/rest/v2/oauth/token` | `/api/v1/university/oauth/token` |
| Jadval | `users` | `oauth_client` |
| OAuth 2.1 | Deprecated | Tavsiya etiladi |
| Secret rotation | Manual | Auto (180 kun) |
| IP whitelist | Yo'q | `allowed_ip_cidr[]` |
| Rate limit | 60/min (statik) | Per-client (60-1000) |
| Audit tracking | username | client_id + university_code |

## Alternatives Considered

### Alternative 1: Hozirgi holat saqlash
- ✅ Hech narsa o'zgartirish kerak emas
- ❌ OAuth 2.1 deprecated grant
- ❌ Xavfsizlik kuchsiz (secret rotation, IP whitelist yo'q)
- ❌ `users` shishadi (224 ta machine yozuv)
- **Rad etish sababi:** xavfsizlik regression

### Alternative 2: `users` jadvalga `is_machine` flag qo'shish
- ✅ Tezkor (1 ta ustun qo'shish)
- ❌ Hali ham bitta jadvalda HUMAN + MACHINE
- ❌ NULL ko'p (insonda `client_secret` NULL, mashinada `email` NULL)
- **Rad etish sababi:** denormalization — schema toza bo'lmaydi

### Alternative 3: Server-side router (URL bir xil, internal split)
- ✅ PHP kodni o'zgartirish kerak emas
- ❌ Eski URL'da yangi feature
- ❌ Decommission qiyin
- **Qisman qabul qilingan:** keyingi 6 oyda parallel ishlash uchun

### Alternative 4: Bitta yagona `/oauth2/token` endpoint (Spring Authorization Server)
- ✅ RFC 6749 standart
- ✅ Spring default
- ❌ `api-legacy` migration yana qiyin
- **Kelajakda qabul qilinishi mumkin:** ADR-0003 (api-university) bilan qisman bajarildi

## Consequences

### Positive
- ✅ OAuth 2.1 standart
- ✅ Secret rotation 180 kun (auto-tracking via `secret_rotated_at`)
- ✅ IP whitelist (`allowed_ip_cidr` TEXT[] — CIDR blocks)
- ✅ Per-client rate limit (60-1000 rpm — OTM hajmiga qarab)
- ✅ Audit aniq: `client_id + university_code` har log'da
- ✅ `users` toza qoladi (faqat HUMAN — vazirlik admin, rektor)
- ✅ Universitet account lifecycle alohida (machine vs human)

### Negative
- ⚠️ 224 OTM PHP kodini yangilash kerak (3-6 oy migration)
- ⚠️ Secret distribution xavfsiz (one-time HTTPS link + email)
- ⚠️ Eski `/app/rest/v2/oauth/token` 12 oy parallel ishlaydi (texnik qarz)

### Risks

- **Risk:** OTM PHP kodini yangilashga vaqt yo'q
  **Mitigation:** Feature flag — har OTM o'z tezligida o'tadi. `password` grant 12 oy parallel ishlaydi.

- **Risk:** Client secret kompromitatsiyasi
  **Mitigation:** Secret rotation 180 kun + IP whitelist + per-client rate limit. Anomaly bo'lsa darhol revoke (`is_active=FALSE`).

- **Risk:** Migration vaqtida 224 OTM auth uzilib qoladi
  **Mitigation:** Strangler Fig — eski va yangi parallel ishlaydi. PHP feature flag bilan har OTM alohida o'tadi.

- **Risk:** `oauth_client` jadval rate limit'i pastlik tufayli OTM ishlay olmaydi
  **Mitigation:** Default 300 rpm (catalog OTM uchun yetarli). Yirik OTM uchun 1000 rpm (manual config).

## Implementation

### Bosqich 1: Hemis-back tayyorgarligi (1-2 hafta)

```sql
-- 224 ta oauth_client yozuvi
INSERT INTO oauth_client (client_id, client_secret_hash, client_name, client_type,
                          university_code, grant_types, scopes, rate_limit_rpm,
                          allowed_ip_cidr)
SELECT
    'univer_' || u.code,                              -- 'univer_337'
    crypt(:secret, gen_salt('bf', 12)),               -- BCrypt
    u.name,
    'UNIVERSITY_BACKEND',
    u.code,
    ARRAY['client_credentials'],
    ARRAY['rest-api'],
    300,                                              -- per-OTM
    ARRAY['10.0.0.0/8', '172.16.0.0/12']::TEXT[]      -- OTM network
FROM hemishe_e_university u WHERE u.active = TRUE;

-- OTM_API rolini biriktirish
INSERT INTO oauth_client_role (client_id, role_id)
SELECT oc.id, r.id FROM oauth_client oc
JOIN role r ON r.code = 'OTM_API'
WHERE oc.client_type = 'UNIVERSITY_BACKEND';
```

### Bosqich 2: PHP kodda feature flag (2 hafta)

```php
// common/components/hemis/HemisApi.php
public function apiLogin($username, $password)
{
    if (Config::get(Config::CONFIG_USE_CLIENT_CREDENTIALS)) {
        return $this->_client->post('/api/v1/university/oauth/token', [
            'grant_type'    => 'client_credentials',
            'client_id'     => Config::get('OAUTH_CLIENT_ID'),
            'client_secret' => Config::get('OAUTH_CLIENT_SECRET'),
        ])->send();
    }
    // Eski rejim — backward compat
    return $this->_client->post('v2/oauth/token', [
        'grant_type' => 'password',
        'username'   => $username,
        'password'   => $password,
    ])->send();
}
```

### Bosqich 3: Canary deploy (1 oy)

| Hafta | OTM soni | Monitoring |
|-------|----------|------------|
| 1 | 5 ta (test/dev) | Audit log, error rate |
| 2 | 50 ta (kichik OTM'lar) | Latency, success rate |
| 3 | 150 ta | Anomaly detection |
| 4 | 224 ta (hammasi) | Full deployment |

### Bosqich 4: Eski URL deprecation (6 oy)

```http
HTTP/1.1 200 OK
Sunset: Sat, 31 Dec 2027 23:59:59 GMT
Deprecation: true
Link: </api/v1/university/oauth/token>; rel="successor-version"
```

### Bosqich 5: Decommission (12 oy)

```sql
-- 224 ta machine yozuvni users dan o'chirish
UPDATE users SET deleted_at = NOW(), deleted_by = 'oauth-migration-2027'
WHERE user_type = 'UNIVERSITY' AND username LIKE 'univer_%';

-- Eski endpoint 410 Gone
-- api-legacy/.../OAuthLegacyController.java
@PostMapping("/oauth/token")
public ResponseEntity<?> token() {
    return ResponseEntity.status(410)
        .body(new ErrorResponse("This endpoint is removed. Use /api/v1/university/oauth/token"));
}
```

## Configuration

```yaml
# .env / application.yml
hemis.security.oauth.client.secret-rotation-days: 180
hemis.security.oauth.client.default-rate-limit-rpm: 300
hemis.security.oauth.client.access-token-ttl-seconds: 3600
hemis.security.oauth.client.refresh-token-ttl-seconds: 2592000  # 30 days
```

## References

- Code: `oauth_client` table — `domain/.../V006_create_users.sql`
- Code: `OAuthClientAuthenticationService` (security module)
- Code: `UniversityOAuthTokenController` (api-university module)
- PHP integration: `/home/adm1n/projects/startup/univer/common/components/hemis/HemisApi.php:728`
- RFC 6749 §4.4: client_credentials grant
- RFC 8594: Sunset HTTP Header Field
- OAuth 2.1 BCP: https://oauth.net/2.1/
- Related: ADR-0004 (api-university module)
