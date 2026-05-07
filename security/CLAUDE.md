# security module — Authentication & Authorization

> JWT, BCrypt, RBAC. **Eng yuqori standartlar — bu yerda xato = ma'lumot tarqalishi.**

---

## TOP Security Patterns

### 1. Password Encoding — BCrypt strength 12 (OWASP 2025)

```java
// ✓ TO'G'RI — OWASP Password Storage Cheat Sheet 2025
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);  // 2^12 = 4096 iterations
}

// ✗ XATO — default 10 (zaif, OWASP 2025 talabidan past)
return new BCryptPasswordEncoder();

// ✗ TAQIQ — plain text
user.setPassword(rawPassword);
```

**Strength factor 12 (OWASP 2025 minimum):**
- 10 → ~80ms hash (zaif, GPU brute-force tezroq)
- 12 → ~250ms hash (**OWASP 2025 minimum talabi**)
- 14 → ~1s hash (yuqori xavfsizlik, lekin login sekin)

**Argon2id alternativasi (OWASP 2025 tavsiya yangi loyihalar uchun):**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    // Argon2id — OWASP 2025 da bcrypt'dan tavsiyaroq (memory-hard)
    // Parameters: saltLength=16, hashLength=32, parallelism=1, memory=19456 (19 MB), iterations=2
    return new Argon2PasswordEncoder(16, 32, 1, 19_456, 2);
}
```

**Bizdagi tanlov:** legacy compat sababli BCrypt-12. Yangi micro-service'lar uchun Argon2id ko'rib chiqilishi kerak.

**Hybrid encoder** (eski PBKDF2 + yangi BCrypt):
```java
public class LegacyPasswordEncoder implements PasswordEncoder {
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(12);
    private final Pbkdf2PasswordEncoder pbkdf2 = ...; // CUBA format

    @Override
    public boolean matches(CharSequence raw, String stored) {
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$")) {
            return bcrypt.matches(raw, stored);  // BCrypt
        }
        return pbkdf2.matches(raw, stored);  // legacy PBKDF2
    }

    @Override
    public String encode(CharSequence raw) {
        return bcrypt.encode(raw);  // har doim BCrypt yangilarini
    }
}
```

---

### 2. JWT Configuration — Real holat

**Bizdagi mavjud config (`TokenService.java`):**

```java
// Algorithm — HS256 (lokal/dev, .env secret >= 32 bytes validation)
//          — RS256 (production, JWT_JWK_SET_URI env orqali)

// Claims (hozirgi minimal set, jti YO'Q):
JwtClaimsSet claims = JwtClaimsSet.builder()
    .issuer(issuer)                                   // "hemis-backend"
    .issuedAt(now)
    .expiresAt(expiry)                                // now + 12 soat
    .subject(userId)                                  // UUID
    .claim("username", userDetails.getUsername())
    .claim("full_name", fullName)                     // audit log uchun snapshot
    .claim("scope", "rest-api")
    .build();
```

**Diqqat:** Token `jti` claim'siz chiqariladi. Blacklist Redis'da **butun token (yoki SHA-256 hash)** kalit sifatida ishlatiladi.

### 2.1 Token Revocation — `TokenBlacklistService` (mavjud)

Real implementation:
- `security/service/TokenBlacklistService.java` — Redis blacklist
- `security/filter/CookieJwtAuthenticationFilter.java` — har request'da check (~1ms)
- Redis key prefix: `token:blacklist:`
- TTL = token expiry - now

```java
// Logout pattern (mavjud holatga mos)
@PostMapping("/logout")
public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
    String token = authHeader.substring(7);  // "Bearer "
    Jwt jwt = jwtDecoder.decode(token);

    long ttlSeconds = jwt.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond();
    if (ttlSeconds > 0) {
        // Token'ning o'zi (yoki SHA-256 hash) Redis key sifatida
        blacklistService.revoke(token, Duration.ofSeconds(ttlSeconds));
    }
    return ResponseEntity.noContent().build();
}
```

### 2.2 JWT Modernizatsiya — Tavsiya (kelajakdagi yaxshilashlar)

Hozirgi token'da yo'q, lekin Vazirlik miqyosi xavfsizlik uchun **tavsiya etiladi**:

#### `jti` claim qo'shish

```java
// TokenService.generateToken() ga qo'shish
String jti = UUID.randomUUID().toString();
JwtClaimsSet claims = JwtClaimsSet.builder()
    .id(jti)                              // ← YANGI: jti = JWT ID
    // ... boshqa claim'lar
    .build();
```

**Foyda:** Blacklist key katta token'ni butunligicha emas, faqat 36-char UUID. Redis xotira tejash, faster lookup.

#### `kid` header — Key Rotation Support

```java
JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256)
    .keyId("hemis-jwt-2026-q1")           // ← YANGI
    .build();
```

**Foyda:** Multi-key parallel ishlash (eski token + yangi key birgalikda), graceful rotation.

#### Refresh Token Rotation

```java
@PostMapping("/refresh")
public TokenPair refresh(@RequestBody RefreshRequest req) {
    Jwt oldRefresh = jwtDecoder.decode(req.refreshToken());
    long ttl = oldRefresh.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond();

    // Eski refresh — blacklist
    blacklistService.revoke(req.refreshToken(), Duration.ofSeconds(Math.max(0, ttl)));

    // Yangi access + refresh pair
    return tokenService.issuePair(oldRefresh.getSubject());
}
```

**Tavsiya:** Bu 3 ta yaxshilash (jti + kid + refresh rotation) **bir sprint** ichida birga implement qilinishi maqbul. Avval ADR yozish: "ADR-NNN: JWT kid+jti+refresh-rotation pattern".

**Secret rotation (mavjud strategy):**
- Lokal: `.env` da, 90 kun rotation
- Production: HashiCorp Vault yoki Kubernetes Secrets
- Old key 7 kun overlap (`kid` header bilan parallel)

---

### 3. Token Validation — Spring Security

```java
// SecurityConfig
http
    .oauth2ResourceServer(oauth -> oauth
        .jwt(jwt -> jwt
            .jwtAuthenticationConverter(jwtAuthConverter())
        )
    )
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
        .requestMatchers("/api/v1/external/**").hasAuthority("api.external")
        .requestMatchers("/api/v1/admin/**").hasAuthority("admin.access")
        .anyRequest().authenticated()
    )
    .csrf(AbstractHttpConfigurer::disable)  // JWT → CSRF kerak emas
    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
```

---

### 4. `@PreAuthorize` — Method Security

```java
// ✓ TO'G'RI — service yoki controller method
@PreAuthorize("hasAuthority('students.create')")
public StudentDto create(...) { ... }

// ✓ Multiple permissions (OR)
@PreAuthorize("hasAuthority('students.edit') or hasAuthority('admin.full')")
public void update(...) { ... }

// ✓ Custom SpEL — university scope
@PreAuthorize("hasAuthority('students.edit') and @studentSecurity.canEdit(#id, authentication)")
public void update(@PathVariable Long id, ...) { ... }

// Bean
@Component("studentSecurity")
@RequiredArgsConstructor
public class StudentSecurity {
    private final StudentRepository repo;

    public boolean canEdit(Long studentId, Authentication auth) {
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("admin.full"))) {
            return true;  // SUPER_ADMIN bypass
        }
        Long userUniversityId = ((CustomPrincipal) auth.getPrincipal()).universityId();
        return repo.findUniversityIdByStudentId(studentId)
            .map(uniId -> uniId.equals(userUniversityId))
            .orElse(false);
    }
}
```

---

### 5. RBAC Permission Format

```
{resource}.{action}

Resources:
  students, faculty, departments, curriculum, grades,
  reports, users, roles, permissions, settings

Actions:
  view, create, edit, delete, export, import
```

**Misollar:**
- `students.view` — talaba ro'yxatini ko'rish
- `students.create` — talaba qo'shish
- `students.edit` — tahrirlash
- `students.delete` — soft delete
- `students.export` — Excel export
- `admin.full` — to'liq admin (SUPER_ADMIN)

**Permission caching (Redis):**
- TTL: 1 soat
- Key: `user:permissions:<user-id>`
- Evict: role/permission o'zgartirilganda — `CacheInvalidationListener`

---

### 6. PII (Personally Identifiable Information) — Logging Forbidden

```java
// ✗ HAR DOIM TAQIQ
log.info("Login: username={}, password={}", username, password);
log.info("Student: {}", student);  // toString() PII chiqarishi mumkin
log.info("PINFL: {}", student.getPinfl());

// ✓ TO'G'RI — masked
log.info("Login attempt: username={}", username);  // password yo'q
log.info("Student created: id={}, facultyId={}", student.getId(), student.getFacultyId());
log.info("PINFL: {}****", pinfl.substring(0, 8));  // mask
```

**Loggable PII listini har class'da hujjatlash:**
```java
/**
 * Loggable: id, facultyId, status
 * NOT loggable: pinfl, firstName, lastName, email, phone, address
 */
@Entity
public class Student { ... }
```

---

### 7. Rate Limiting — RateLimitFilter

```yaml
# application.yml
hemis:
  rate-limit:
    per-role:
      VIEWER: 60               # req/min
      UNIVERSITY_ADMIN: 300
      MINISTRY_ADMIN: 600
      SUPER_ADMIN: 1000
    per-ip-anonymous: 100     # not authenticated
    burst-multiplier: 2       # short burst allowance
```

**Implementation:** Redis sliding window + Bucket4j.

**429 Response:**
```json
{
  "success": false,
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Too many requests, retry after 30s"
  }
}
```
Header: `Retry-After: 30`

---

### 8. CORS — strict origins

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of(
        "https://hemis.uz",
        "https://*.hemis.uz",
        "https://localhost:*"  // dev only
    ));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

**Diqqat:** `setAllowCredentials(true)` + `setAllowedOriginPatterns("*")` = security hole. Origin'larni explicit yozish.

---

### 9. SQL Injection — Parametrized Queries Only

```java
// ✗ MUTLAQO TAQIQ — SQL injection
String sql = "SELECT * FROM students WHERE name = '" + name + "'";

// ✓ TO'G'RI — JPQL parametr
@Query("SELECT s FROM Student s WHERE s.name = :name")
List<Student> findByName(@Param("name") String name);

// ✓ TO'G'RI — JdbcTemplate parametr
jdbcTemplate.query(
    "SELECT * FROM students WHERE name = ?",
    new Object[]{name},
    rowMapper
);

// ✓ TO'G'RI — Specification
public static Specification<Student> nameContains(String name) {
    return (root, query, cb) -> cb.like(root.get("name"), "%" + name + "%");
}
```

---

### 10. XSS — Output Encoding

API JSON qaytaradi → Jackson auto-escape.

```java
// ✓ TO'G'RI — Jackson default escape
public record StudentDto(String firstName, ...) {}

// User input "Doe<script>alert(1)</script>" →
// JSON output: "Doe<script>alert(1)</script>"
```

**Diqqat:** Email template'lar (HTML) — manual escape kerak (`StringEscapeUtils.escapeHtml4`).

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
@Entity
@Table(name = "audit_log", schema = "auth")
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
