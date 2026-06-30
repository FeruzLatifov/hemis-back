# api-external module — External Systems OAuth Token Facade

> **Markaziy HEMIS-back** ↔ tashqi davlat sistemalari uchun **OAuth 2.0 `client_credentials` token endpoint FACADE'i**. Modul atigi **2 asosiy fayl** — token controller + exception handler. Biznes-logika YO'Q, butunlay `security` modulga delegate.
>
> **Tashqi davlat sistemalari:** MyGov (yagona kirish), OneID (auth federation), Hokimiyat, GUVD, Tax/Soliq, BIMM, MSPD.
>
> **URL:** `/api/v1/external/oauth/token`
> **Security:** Basic auth (`client_id:client_secret`) → Bearer JWT. **X-API-Key EMAS.**
>
> **Eslatma:** "Ministry HEMIS" eski terminologiyasi ishlatilmaydi — **biz o'zimiz vazirlik markaziy server**.

---

## Modul tarkibi (faqat 2 asosiy fayl)

| Fayl | Rol |
|------|-----|
| `controller/auth/ExternalOAuthTokenController` | `POST /api/v1/external/oauth/token` — token issue facade |
| `exception/ExternalExceptionHandler` | `@RestControllerAdvice` external controller'lar uchun |

> **Outbound gov integratsiya bu modulda EMAS** — `service` modulida joylashgan. Quyidagi "Outbound" bo'limga qarang.

---

## 1. ExternalOAuthTokenController — token facade

`UniversityOAuthTokenController` bilan **funksional AYNI**. URL ajratish ataylab: alohida Swagger bo'limi, alohida metrika, alohida sunset / kontrakt lifecycle (audience'ga ko'ra).

`ClientType.EXTERNAL_SYSTEM` — har tashqi hamkor `oauth_client` jadvalida o'z qatori, alohida secret/IP whitelist/rate-limit bilan.

**Ikki variant, ikkalasi ham bir xil `tokenIssuer.issue(...)` ga delegate:**

```java
private final OAuthClientTokenIssuer tokenIssuer;   // security modulidan

// 1) form-urlencoded / multipart
@PostMapping(value = "/oauth/token",
    consumes = {APPLICATION_FORM_URLENCODED_VALUE, MULTIPART_FORM_DATA_VALUE})
public ResponseEntity<?> tokenForm(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @RequestParam(value = "grant_type", required = false) String grantType,
        @RequestParam(value = "scope", required = false) String scope,
        HttpServletRequest request) {
    return tokenIssuer.issue(authorization, grantType, scope, request);
}

// 2) application/json — grant_type/scope body'dan o'qiladi, keyin AYNI issue()
@PostMapping(value = "/oauth/token", consumes = APPLICATION_JSON_VALUE)
public ResponseEntity<?> tokenJson(...) { ... tokenIssuer.issue(...); }
```

**Muhim:** controller'da hech qanday biznes-logika yo'q — faqat `security.OAuthClientTokenIssuer.issue(authorization, grantType, scope, request)` ga delegate. Yangi logika qo'shilsa — `security` modulga, bu yerga emas.

**Faqat `grant_type=client_credentials`** qabul qilinadi.

### Auth modeli

- **Basic auth header:** `Authorization: Basic base64(client_id:client_secret)` → **Bearer JWT** token qaytadi.
- **X-API-Key YO'Q.** `IpWhitelistFilter` / `ApiKeyAuthenticationFilter` kabi alohida filter'lar **MAVJUD EMAS**.
- IP whitelist + rate-limit **alohida filter emas**, balki `OAuthClient` **ENTITY darajasida** maydon, `security.OAuthClientAuthenticationService` orqali `issue()` ichida enforce qilinadi.

`domain/entity/security/OAuthClient` (default qiymatlar):

| Maydon | Tur | Default |
|--------|-----|---------|
| `allowedIpCidr` | `List<String>` | — (CIDR whitelist) |
| `rateLimitRpm` | `Integer` | `60` |
| `rateLimitBurst` | `Integer` | `10` |
| `accessTokenTtlSeconds` | `Integer` | `3600` |

---

## 2. ExternalExceptionHandler

`@RestControllerAdvice(basePackages = "uz.hemis.api.external.controller")` + `@Order(HIGHEST_PRECEDENCE)`. Faqat external controller'lar scope'ida.

| Exception | Status | Code |
|-----------|--------|------|
| `ResourceNotFoundException` | 404 | `NOT_FOUND` |
| `BadRequestException` | 400 | `BAD_REQUEST` |
| `RestClientException` | 502 | `INTEGRATION_ERROR` ("External service unavailable") |
| `Exception` (generic) | 500 | `INTERNAL_ERROR` ("Internal server error") |

**Generic handler (OWASP A05/A09):** stacktrace / exception FQN / DB xato matni (e.g. `PSQLException: null value in column "university_code"`) **klientga oqib chiqmaydi** — full detail faqat server-side log'da, klient generic 500 oladi.

---

## Outbound gov integratsiya — `service` modulida (BU YERDA EMAS)

HEMIS → tashqi davlat sistemalariga **chiquvchi** chaqiriqlar `service` modulida:

- **Baza klass:** `service/base/AbstractGovernmentApiService` — `RestTemplate`, connect+read timeout **30s** (`setConnectTimeout(30000)` / `setReadTimeout(30000)`), self-signed HTTPS uchun gov RestTemplate.
- **Klientlar:**
  - `service/integration/HemisApiService`
  - `service/integration/GuvdTokenService`
  - `service/integration/ApiMspdClient`
  - `service/student/SocialService`
  - `service/shared/BimmService`
  - `service/shared/GovernmentMinorApiService`

Yangi outbound gov client kerak bo'lsa → `service` moduliga `AbstractGovernmentApiService` extend qilib qo'shiladi, **api-external'ga emas**.

---

## PR Checklist

- [ ] Controller faqat `tokenIssuer.issue(...)` ga delegate — biznes-logika `security` modulda
- [ ] Yangi grant logikasi `OAuthClientTokenIssuer` / `OAuthClientAuthenticationService` ga, controller'ga emas
- [ ] `ClientType.EXTERNAL_SYSTEM` qatori `oauth_client`'da (secret, allowedIpCidr, rateLimit, ttl)
- [ ] ExceptionHandler scope `uz.hemis.api.external.controller` saqlanadi; generic 500 stacktrace leak qilmaydi
- [ ] Swagger annotation (Operation/ApiResponses) ikkala token variant uchun
- [ ] Test: controller delegatsiya (Mockito) + exception handler (`ExternalExceptionHandlerTest`)

---

## See Also
- `../security/...` — `OAuthClientTokenIssuer`, `OAuthClientAuthenticationService` (token issue + auth/IP/rate-limit enforce)
- `../domain/...` — `entity/security/OAuthClient` (per-client config)
- `../service/CLAUDE.md` — outbound gov integratsiya (`AbstractGovernmentApiService` + klientlar)
