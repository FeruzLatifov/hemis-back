# security module — Authentication & Authorization

> **Markaziy ministry server** (1 deploy) → 230 OTM × ~5K admin × ~1.15M talaba metadata. **Vazirlik darajasidagi xavfsizlik**: bu yerda xato = milliy darajadagi ma'lumot leak.
>
> JWT (vazirlik admin + UNIVERSITY_ADMIN), BCrypt (CUBA legacy + modern), OAuth client_credentials (224 Univer client_id), RBAC (90+ permission, OTM scope filter).

---

## TOP 10 Security Patterns

> **Batafsil misollar va kod bloklari:** [`security/PATTERNS.md`](PATTERNS.md)

| # | Pattern | Qoida |
|---|---------|-------|
| 1 | **Password Encoding** | `BCryptPasswordEncoder(12)` (OWASP 2025 min). Argon2id yangi loyihalar uchun. CUBA legacy → `LegacyPasswordEncoder` (BCrypt + PBKDF2 hybrid) |
| 2 | **JWT Configuration** | HS256 (dev) / RS256 (prod). Claims: `iss`, `sub` (UUID), `exp` (12h), `username`, `university_id` (UNIVERSITY_ADMIN), `client_id` (224 Univer OAuth) |
| 3 | **Token Validation** | `oauth2ResourceServer.jwt()` + `STATELESS` session + CSRF **ENABLED** (`CookieCsrfTokenRepository` double-submit; public auth/oauth/ack/swagger path'lar `ignoringRequestMatchers`). Redis blacklist `token:blacklist:` ~1ms check |
| 4 | **`@PreAuthorize`** | `hasAuthority('students.create')` + custom SpEL `@studentSecurity.canEdit(#id, auth)` for OTM scope. SUPER_ADMIN bypass via `admin.full` |
| 5 | **RBAC Format** | `{resource}.{action}` (e.g. `students.view`, `admin.full`). Cache Redis 1h TTL, evict on role/permission change |
| 6 | **PII Logging** | TAQIQ: `password`, `pinfl`, `student.toString()`. ✓ Mask: `pinfl.substring(0,8)+"****"` |
| 7 | **Rate Limiting** | `RateLimitFilter` — in-memory `ConcurrentHashMap` counters: global + per-IP login (brute-force, login/token path) + per-university (JWT `university_code`) + per-IP fallback (anonim). **Default `security.rate-limit.enabled=false`** — prod'da `=true` qo'yish shart. Bucket4j YO'Q (kelajak sprint). Redis sliding-window FAQAT OAuth token endpoint uchun (`RateLimitService`, prefix `ratelimit:oauth:`). 429 + `Retry-After` |
| 8 | **CORS** | `setAllowedOriginPatterns(...)` explicit list. `setAllowCredentials(true)` + `*` = security hole |
| 9 | **SQL Injection** | JPQL `@Query` parametr, `JdbcTemplate ?` placeholder, Specification API. String concat MUTLAQO TAQIQ |
| 10 | **XSS** | API JSON → Jackson auto-escape ✓. HTML template (email) → manual `StringEscapeUtils.escapeHtml4` |

**Eng kritik (1, 4, 6, 9):** weak password = brute-force; `@PreAuthorize` yo'q = IDOR; PII leak = compliance violation; SQL injection = data leak.

### JWT Modernization

**Bajarildi** (ADR-0009 implemented): `TokenService` har token uchun `jti = UUID.randomUUID()` (access + refresh), `kid` JWS header (key rotation). Refresh token rotation + replay detection — refresh consume bo'lganda eski `jti` qolgan umri davomida blacklist'ga (`token:blacklist:`) yoziladi, takror ishlatilsa rad etiladi.

**Keyingi qadam:** `kid` + RS256 prod migratsiya (hozir HS256 secret key; asimmetrik signing prod uchun).

### Implemented security facts (kod bilan tasdiqlangan)

- **Weak-default soft-fail guard** — `LegacyOAuthClientProperties`: `WEAK_VALUES` ro'yxati + `MIN_SECRET_LENGTH=16`. Prod profilda zaif/qisqa credential → `log.error`, lekin **boot fail YO'Q** (200+ legacy klient buzilmasin). Blank → `@NotBlank` boot fail.
- **K2 ack endpoint** — `/api/v1/university/hemis-events/ack` = `permitAll` + **HMAC** (`X-Hemis-Signature`, `WebhookAckService` verify, `secret_enc`). JWT EMAS (ADR-0012, inbound webhook teskarisi).
- **Univer sync endpointlar** — `/api/v1/university/employees/sync` va `/buildings/sync` `permitAll` → **`authenticated()`** (OAuth client_credentials majburiy, ADR-0005). Avval profile guard yo'q edi → har kim PINFL massa yuborishi mumkin edi.
- **`full_name` claim olib tashlandi** — OWASP A09 (token PII'ni localStorage'ga leak qiladi). Frontend `/me` endpoint orqali oladi.

---

## Sensitive Data Storage

### Encryption at Rest

| Field | Storage | Rationale |
|-------|---------|-----------|
| Password | BCrypt hash | Bir tomonga, brute-force qiyin |
| JWT secret | Vault/Secrets Manager | Plain'ni hech qachon |
| API keys (3rd party) | DB encrypted (AES-256) | Decryption faqat runtime |
| PINFL | Plain (DB level encryption optional) | Application access only via permission |
| Audit log | Plain (immutable) | Compliance |

### TLS at Transit

- Production: TLS 1.3 (TLS 1.2 minimum)
- Internal services: mTLS (Service-to-Service auth)
- Database: SSL connection (`sslmode=require`)
- Redis: TLS + AUTH

---

## Audit Log Requirements

Har CRUD action audit'ga yoziladi:

```java
// AuditLog ALOHIDA `hemis_audit` DB'da (ADR-0003 Audit DB Isolation).
// Markaziy DB'da emas — hemis_audit datasource orqali (AuditDataSourceConfig.java).
@Entity
@Table(name = "activity_log")  // hemis_audit DB, default schema
public class AuditLog extends ImmutableEntity {
    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String action;  // CREATE, UPDATE, DELETE, LOGIN, ACCESS

    @Column(nullable = false, length = 100)
    private String entityType;

    @Column(length = 100)
    private String entityId;

    @Column(columnDefinition = "JSONB")
    private String oldValue;

    @Column(columnDefinition = "JSONB")
    private String newValue;

    @Column(nullable = false, length = 45)
    private String ipAddress;  // IPv6 max 45 chars

    @Column(length = 500)
    private String userAgent;
}
```

**Retention:** 7 yil (Vazirlik talabi).

**`AuditEventListener`** (allaqachon mavjud) — async, dedicated executor.

---

## Common Vulnerabilities Checklist (OWASP Top 10 - 2025)

- [ ] **A01 Broken Access Control** — `@PreAuthorize` har endpoint'da, university-scope check, BOLA/IDOR himoyasi
- [ ] **A02 Cryptographic Failures** — BCrypt-12 (yoki Argon2id), TLS 1.3, JWT signing, secrets at rest encrypted
- [ ] **A03 Injection** — JPQL parametrize, prepared statements, NoSQL injection, LDAP injection, command injection
- [ ] **A04 Insecure Design** — Threat modeling, secure SDLC, security review har feature uchun
- [ ] **A05 Security Misconfiguration** — Production config review, default credential'lar o'zgartirilgan, error message PII chiqarmaydi
- [ ] **A06 Vulnerable & Outdated Components** — Dependency-Check, Snyk, monthly scan, SBOM (Software Bill of Materials)
- [ ] **A07 Identification & Authentication Failures** — Strong password (BCrypt-12), MFA (kelajakda), session timeout, brute-force protection
- [ ] **A08 Software & Data Integrity Failures** — Dependency signing, Liquibase checksum, supply chain (Sigstore/Cosign)
- [ ] **A09 Security Logging & Monitoring Failures** — Audit log, Sentry alerting, SIEM integration, anomaly detection
- [ ] **A10 Server-Side Request Forgery (SSRF)** — External URL whitelist, no user-controlled URL fetch, internal IP block

> **Eslatma:** OWASP standartlar uzluksiz yangilanadi. Har yarim yilda
> [OWASP Top 10](https://owasp.org/Top10/) va
> [OWASP Cheat Sheets](https://cheatsheetseries.owasp.org/) tekshirilishi kerak.

---

## Secrets Management

| Secret | Rotation | Storage |
|--------|----------|---------|
| JWT signing key | 90 kun | Vault |
| DB master parol | 180 kun | Vault |
| Redis parol | 180 kun | Vault |
| 3rd party API key | 365 kun | DB encrypted |
| TLS certificate | Let's Encrypt 90 kun | Auto-rotate |

**Rotation procedure:**
1. Yangi secret yaratish
2. Both old + new ishlatilishi (overlap window)
3. Klient migration
4. Old secret revoke

---

## PR Checklist (security)

- [ ] BCrypt strength 12
- [ ] JWT secret `.env` dan, hardcoded emas
- [ ] `@PreAuthorize` har sensitive method'da
- [ ] Custom SpEL university-scope check
- [ ] Permission Redis cache TTL config
- [ ] PII log'da yo'q (PINFL, parol, telefon, email, ism)
- [ ] SQL parametrize (concat yo'q)
- [ ] CORS origin explicit
- [ ] Audit log har CRUD'da
- [ ] Rate limit per-role
- [ ] Test: 401, 403 scenarios
- [ ] Penetration test (OWASP ZAP, Burp Suite) muhim feature uchun

---

## See Also
- `../.claude/architecture.md` — Auth flow + RBAC
- `../service/CLAUDE.md` — `@PreAuthorize` patterns
