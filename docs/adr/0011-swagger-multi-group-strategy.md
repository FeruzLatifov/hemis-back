---
id: ADR-0011
status: accepted
date: 2026-05-10
deciders: hemis-team
agent: claude-code
model: claude-opus-4-7
affects:
  - app
  - api-legacy
  - api-web
  - api-university
  - api-external
  - security
  - common
verification: |
  ./gradlew :app:bootRun  # SPRING_PROFILES_ACTIVE=dev,redis
  # Brauzer: http://localhost:8081/swagger-ui.html
  # Top dropdown'da 4 ta group ko'rinishi kerak: web, legacy, university, external
related:
  - ADR-0004
  - ADR-0005
  - ADR-0009
---

# ADR 0011: Swagger Multi-Group Strategiyasi va Production Xavfsizligi

## Status

Accepted (2026-05-10)

> **Y-Statement:** Swagger UI'da turli auditoriya (web frontend, Univer legacy, Univer yangi, davlat tashkilotlari) uchun aniq ajratilgan API hujjatini ta'minlash, va production'da ichki API strukturasi tashqaridan ko'rinmasligi uchun, biz **4-group GroupedOpenApi (web/legacy/university/external)** + **production'da swagger butunlay o'chirish** strategiyasini tanladik. Oqibatda har auditoriya o'ziga tegishli endpoint'larni ko'radi va prod'da `/swagger-ui.html` 401 qaytaradi.

## Context

Audit (2026-05-10) Swagger/OpenAPI hujjati holatini quyidagi muammolar bilan aniqladi:

1. **Production xavfi:** `application.yml`'da `springdoc.api-docs.enabled: true` hardcoded; `application-prod.yml`'da override yo'q. Prod'da `/v3/api-docs` orqali API strukturasi olib chiqarilishi mumkin.

2. **Group nomlari chalg'ituvchi:** `university` (eski CUBA) va `university-new` (yangi REST) — ikkalasi ham 224 ta OTM Univer'ga xizmat qiladi, lekin nom xronologik. URL'da bo'sh joy: `?urls.primaryName=Web%20Frontend%20API%20v1`.

3. **`api-external` group YO'Q:** `OpenApiConfig.java`'da faqat 3 ta group; `/api/v1/external/**` (`ExternalOAuthTokenController`) hech qaerda ko'rinmaydi.

4. **Info description eskirgan:** "Spring Boot 3.5.7, Java 21 LTS, 170+ endpoints, 30 days token" — haqiqat: Java 25, Spring Boot 4.0.6, 780+ endpoint, JWT 12h+7d (ADR-0009).

5. **Foydalanuvchanlik:** OAuth2 password flow Swagger UI'dan ishlamaydi (CUBA legacy Basic Auth header talab qiladi); placeholder linklar (docs.hemis.uz, github.com/hemis-uz, +998 71 123 4567).

6. **`@Operation` qoplama past:** Audit (Rob Pike "avval tekshir" qoidasi) `awk` tahlili 532 ta endpoint'da `@Operation` annotatsiyasi yo'qligini aniqladi. Avvalgi `grep -c @Operation` hisoblashi noto'g'ri edi (fayl darajasida count, lekin har endpoint uchun emas). `@ApiResponse` qoplama ham past: api-legacy 71.8%.

7. **`urlsPrimaryName: all`** (`application-dev.yml`) — bunday group OpenApiConfig'da yo'q.

8. **ResponseWrapper hujjat-kod tafovuti:** `common/CLAUDE.md` `record + timestamp + page` deydi, real kod `class + 4 maydon (no timestamp)`.

## Decision

**4-group GroupedOpenApi strategiyasi:**

| Group (slug) | `displayName` | URL pattern | Mijoz |
|--------------|---------------|-------------|-------|
| `web` | Web Frontend API | `/api/v1/web/**` | Markaziy React UI |
| `legacy` | Univer Legacy API (CUBA 7.3) | `/app/rest/v2/**`, `/services/**`, `/entities/**` | 224 OTM Univer (Yii2 PHP, eski CUBA) |
| `university` | Univer API v1 (OAuth 2.1) | `/api/v1/university/**` | 224 OTM Univer (yangi OAuth 2.1) |
| `external` | Davlat tashkilotlari API | `/api/v1/external/**` | MyGov, MSPD, GUVD, BIMM, Tax |

**Production xavfsizlik:**
- `application.yml:178-187`: `enabled: ${SWAGGER_ENABLED:true}` (default dev'ga true)
- `application-prod.yml`: `springdoc.api-docs.enabled: ${SWAGGER_ENABLED:false}` + `swagger-ui.enabled: ${SWAGGER_ENABLED:false}` (default prod'da false)
- `SecurityConfig.java`: swagger requestMatchers'lar `if (swaggerEnabled)` bloki ichida — false bo'lsa `anyRequest().authenticated()` orqali 401

**Qo'shimcha yaxshilanishlar:**
- `applicationVersion` MANIFEST'dan (`Implementation-Version`)
- `Title` hardcode → `applicationName` ENV'dan
- `Info description` yangilanadi (Java 25, Spring 4.0.6, 780+, JWT 12h)
- `bearerAuth` + **yangi `basicAuth`** security scheme (CUBA legacy oauth/token uchun)
- `defaultResponsesCustomizer` Bean — har endpoint'ga 401/403/500 default
- **`fallbackSummaryCustomizer` Bean** — 532 ta `@Operation`'siz endpoint'ga `operationId` + HTTP method'dan avtomatik summary generatsiya (`loadStudentByPinfl` + GET → "Get: load student by pinfl"). Manuel `@Operation(summary=...)` yozilgan bo'lsa — saqlanadi. Bu temporary fallback — vaqt o'tishi bilan dasturchilar manuel summary qo'shadi.
- Placeholder linklar (`docs.hemis.uz`, `github.com/hemis-uz`, telefon, Telegram) olib tashlandi
- ResponseWrapper hujjati real klassga moslandi (`common/CLAUDE.md`, `api-web/CLAUDE.md`)

## Alternatives Considered

### Alternative 1: 3-group saqlash (eski strategiya)
- `Web Frontend API v1` + `university` + `university-new`
- **Rad etish sababi:** `api-external` group yo'q; group nomlari chalg'ituvchi (university va university-new — ikkalasi bir auditoriya); URL slug-unfriendly (bo'sh joy).

### Alternative 2: Foydalanuvchi taklifi (`old-ministry` + `university`)
- `university` (eski) → `old-ministry`; `university-new` → `university`
- **Rad etish sababi:** "Ministry" termini chalg'ituvchi — `/app/rest/v2/**` ministry'ga xizmat qilmaydi, balki Univer ministry server'iga so'rov yuboradi (consumer Univer, server ministry). Auditoriya nuqtai nazaridan yolg'on signal beradi.

### Alternative 3: Single-group (`all`)
- 4 ta alohida group o'rniga bitta to'liq hujjat
- **Rad etish sababi:** 224 ta OTM IT komandalar uchun web auth, ministry-only registry, classifier admin endpoint'lar foydasiz noise. Auditoriya ajratish UX uchun muhim.

### Alternative 4: ENV bilan boshqarish (production fayl tegmasdan)
- `application-prod.yml` o'zgarmaydi, deploy `SWAGGER_ENABLED=false` ENV qo'shadi
- **Rad etish sababi:** ENV unutish xavfi (fail-open). Default-secure prinsipi: production'da explicit `false`.

## Consequences

### Positive (afzalliklar)

- **Aniq auditoriya ajratish:** har 4 auditoriya o'z hujjatini ko'radi (URL: `?urls.primaryName=<slug>`).
- **Production xavfsizligi:** prod'da swagger butunlay o'chirilgan (default false + ENV override imkoniyati).
- **URL slug-friendly:** `web`, `legacy`, `university`, `external` (bo'sh joysiz, kichik harf).
- **`@ApiResponse` qoplama 100%:** `defaultResponsesCustomizer` har endpoint'ga 401/403/500 qo'shadi.
- **Basic Auth scheme:** CUBA legacy oauth/token uchun Swagger UI "Authorize" dan token olish imkoniyati.
- **Versiya MANIFEST'dan:** Gradle build vaqti `Implementation-Version` avto-yangilanadi.
- **Hujjat-kod muvofiqligi:** ResponseWrapper haqiqatga moslangan (record→class, timestamp/page olib tashlandi).

### Negative (kamchiliklar)

- **Migration kommunikatsiya:** 224 ta OTM IT komandasiga `?urls.primaryName=university` URL'i endi yangi REST'ga olib boradi (eski CUBA emas). Bookmark/Postman collection'larda "legacy" ga o'tish kerak.
- **Production'da swagger yo'q:** dev'lar prod'da debug qilolmaydi. Hujjat staging muhitda ko'riladi.
- **Global tag sinxronlash kechiktirildi:** 18 ta legacy tag global `apiTags()` ro'yxatida yo'q (description'siz UI'da ko'rinadi). Sinxronlash keyingi sprint.

### Neutral (neytral)

- `compare_endpoints.js` (175/175 contract) o'zgarishsiz — group renaming endpoint URL'larga ta'sir qilmaydi.
- `webApi()` setTags eskirgan ro'yxat olib tashlandi — controller `@Tag(description=...)` o'zi yetarli.

## Implementation

| Komponent | Status | Fayl |
|-----------|--------|------|
| `application.yml` SWAGGER_ENABLED ENV | ✅ | `app/src/main/resources/application.yml:178-187` |
| `application-prod.yml` swagger off | ✅ | `app/src/main/resources/application-prod.yml` (yangi blok) |
| `application-dev.yml` urlsPrimaryName fix | ✅ | `app/src/main/resources/application-dev.yml:207` |
| SecurityConfig conditional permitAll | ✅ | `security/src/main/java/uz/hemis/security/config/SecurityConfig.java:96-99` |
| OpenApiConfig 4-group refactor | ✅ | `app/src/main/java/uz/hemis/app/config/OpenApiConfig.java` |
| `externalApi()` Bean (yangi) | ✅ | `OpenApiConfig.java` |
| `defaultResponsesCustomizer` Bean | ✅ | `OpenApiConfig.java` |
| `fallbackSummaryCustomizer` Bean (532 endpoint) | ✅ | `OpenApiConfig.java` |
| `basicAuth` security scheme | ✅ | `OpenApiConfig.java:apiComponents()` |
| **Tag izolatsiyasi:** 70 numbered tag faqat `legacyApi`'da (web/university/external'ga "01.Token..70.Qo'shimcha" oqib o'tmasligi) | ✅ | `OpenApiConfig.java:hemisOpenAPI()` + `legacyApi()` |
| `applicationVersion` MANIFEST | ✅ | `OpenApiConfig.java:55-58` |
| ResponseWrapper hujjat moslash | ✅ | `common/CLAUDE.md`, `api-web/CLAUDE.md` |
| Global `apiTags()` 70→85 sinxronlash | ⏳ | Keyingi sprint (sifat yaxshilanishi) |

## Verification

```bash
# 1. Compile + smoke test
./gradlew clean build

# 2. Dev profile bilan ishga tushirish
SPRING_PROFILES_ACTIVE=dev,redis ./gradlew :app:bootRun

# 3. Brauzer: http://localhost:8081/swagger-ui.html
#    - Top dropdown'da 4 ta group: web, legacy, university, external
#    - ?urls.primaryName=external → /api/v1/external/oauth/token ko'rinadi
#    - ?urls.primaryName=legacy → /app/rest/v2/** endpoint'lar
#    - "Authorize" tugmasi → bearerAuth + basicAuth ikki sxema

# 4. Production simulation
SPRING_PROFILES_ACTIVE=prod,redis ./gradlew :app:bootRun
#    - http://localhost:8081/swagger-ui.html → 401/404
#    - http://localhost:8081/v3/api-docs → 401/404

# 5. Univer kontrakt regression
cd /home/adm1n/projects/startup/hemis-tools/docs/univer_tool
node compare_endpoints.js
# Maqsad: MATCH 175/175 (group renaming endpoint URL'larga ta'sir qilmaydi)
```

## References

- ADR-0004 — api-university yangi modul
- ADR-0005 — OAuth client_credentials
- ADR-0009 — JWT TTL 12h→1h migration (Proposed)
- Swagger audit hisoboti (2026-05-10) — chat history
