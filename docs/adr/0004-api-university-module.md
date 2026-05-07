---
id: ADR-0004
status: implemented
date: 2026-05-04
deciders: hemis-team
agent: human
affects: [api-university, security, settings.gradle.kts]
liquibase: []
entities: [OAuthClient, OAuthClientRole]
verification: curl -X POST http://localhost:8081/api/v1/university/oauth/token
related: [ADR-0005]
---

# ADR 0004: `api-university` yangi modul yaratish

## Status

Accepted (2026-05-04)

## Context

Loyiha 3 ta API moduldan boshlangan:
- `api-legacy` — old-hemis (CUBA Platform 7.3, Java) bilan birxil URL pattern (`/app/rest/v2/*`), 224 ta Univer Yii2 PHP backend shu yerga keladi
- `api-web` — vazirlik admin paneli (`/api/v1/web/*`), browser session, RBAC
- `api-external` — tashqi tashkilotlar (`/api/v1/external/*`), API Key + IP whitelist

**Yangi talab:** Per-OTM Univer Yii2 PHP backend (224 ta)ni **markaziy HEMIS-back**'ga ulash kanali — secret rotation, IP whitelist, per-OTM rate limit, OAuth 2.1 `client_credentials`. Bu vazirlik (markaz) ↔ OTM (subordinate) integratsiya yo'nalishi (mikroservis B2B emas).

**Muammo:**
- `api-legacy` da yangi yo'l qilish — eski moduldan yangi feature chiqishi anti-pattern
- `api-web` ga qo'shish — browser session uchun mo'ljallangan, machine emas
- `api-external` ga qo'shish — chalkash (university ham external bo'lib qolar)

## Decision

Yangi `api-university` modul yaratildi. Maqsad: **224 OTM PHP backend bilan B2B integratsiya**.

URL prefix: `/api/v1/university/*`
Auth: OAuth 2.0 `client_credentials` grant (`oauth_client` jadval)
Security: secret rotation 180 kun, IP whitelist (`allowed_ip_cidr`), per-client rate limit (60-1000 rpm)

## Alternatives Considered

### Alternative 1: `api-legacy` da yangi endpoint
- ✅ Yangi modul yaratmaslik
- ❌ Eski modul yangi feature olib yuradi — confusing
- ❌ `api-legacy` decommission qilinganda yangi feature ham buziladi
- ❌ URL pattern aralashishi: `/app/rest/v2/oauth/token` ga ikki xil grant (password + client_credentials)
- **Rad etish sababi:** decommission roadmap'iga zid

### Alternative 2: `api-external` ga qo'shish
- ❌ Tushuncha aralashishi: 224 OTM "external" emas, "trusted partner"
- ❌ Auth siyosati farq: OTM secret rotation, MyGov API key
- **Rad etish sababi:** semantic clarity yo'qotiladi

### Alternative 3: Bitta yagona OAuth endpoint (security modul)
- ✅ RFC 6749 standart
- ✅ Spring Authorization Server default
- ❌ Loyihaning modul boundary qoidasiga zid
- ❌ Har modulning o'z auth siyosati bo'lishi mumkin
- **Qisman qabul qilingan:** security modul OAuth endpoint'larni boshqaradi, lekin har resource server modul o'z auth annotatsiyalariga ega (token validation orqali)

## Consequences

### Positive
- Modul boundary aniq: 224 OTM uchun maxsus modul
- URL pattern toza: `/api/v1/university/oauth/token` (password emas, client_credentials)
- `oauth_client` jadval bilan to'liq integratsiya (FK to `university_code`)
- Decommission roadmap: `api-legacy` o'chganda `api-university` qoladi
- Security: per-client rate limit, secret rotation, IP whitelist
- Audit: client_id orqali har OTM aniq tracking

### Negative
- 4-modul (3-modul o'rniga) — build vaqti biroz oshadi
- Documentation 4 ta API uchun
- 224 OTM PHP kodini yangilash kerak (3-6 oy migration)

### Risks
- **Risk:** 224 OTM PHP kod o'zgartirishni rad etadi
  **Mitigation:** `api-legacy` `password` grant 12 oy parallel ishlaydi (Sunset header bilan)
- **Risk:** Yangi modul test coverage past
  **Mitigation:** `UniversityOAuthTokenControllerTest` 100% coverage, integration test'lar

## Implementation

Bajarildi:
- `api-university/` modul yaratildi va `settings.gradle.kts`'ga qo'shildi
- `UniversityOAuthTokenController` (client_credentials grant)
- Integration test'lar

> **224 OTM migration bosqichlari (timeline):** `@0005-oauth-client-credentials.md`
> **Modul struktura va base path:** `@../../.claude/architecture.md` (api-university bo'limi)

## References

- Code: `api-university/src/main/java/uz/hemis/api/university/`
- RFC 6749: OAuth 2.0 Authorization Framework (§4.4 client_credentials)
- PHP integration: `/home/adm1n/projects/startup/univer/common/components/hemis/HemisApi.php:728`
- Related: ADR-0005 (OAuth client_credentials migration plan)
