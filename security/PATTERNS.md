# security/PATTERNS.md — Kod Misollar

> **Manual reference** — `security/CLAUDE.md` qoidalarini batafsil misollar bilan to'ldiradi.

---

## 1. Password Encoding — BCrypt strength 12 (OWASP 2025)

```java
// ✓ TO'G'RI — OWASP 2025 minimum
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);  // 2^12 = 4096 iterations
}
```

**Strength:** 10→80ms (zaif), **12→250ms (OWASP min)**, 14→1s (yuqori).

**Argon2id alternativasi (yangi loyihalar):**
```java
return new Argon2PasswordEncoder(16, 32, 1, 19_456, 2);  // memory-hard
```

**Hybrid encoder** (CUBA legacy PBKDF2 + yangi BCrypt):
```java
public class LegacyPasswordEncoder implements PasswordEncoder {
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(12);
    private final Pbkdf2PasswordEncoder pbkdf2 = ...;

    public boolean matches(CharSequence raw, String stored) {
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$"))
            return bcrypt.matches(raw, stored);
        return pbkdf2.matches(raw, stored);
    }

    public String encode(CharSequence raw) {
        return bcrypt.encode(raw);  // har doim BCrypt yangilarini
    }
}
```

## 2. JWT Configuration

```java
// HS256 (lokal/dev) | RS256 (production, JWT_JWK_SET_URI env)
JwtClaimsSet claims = JwtClaimsSet.builder()
    .issuer(issuer)
    .issuedAt(now)
    .expiresAt(expiry)
    .subject(userId)                         // UUID
    .claim("username", userDetails.getUsername())
    .claim("full_name", fullName)
    .claim("scope", "rest-api")
    // Tavsiya (yangi sprint):
    .claim("university_id", universityId)    // UNIVERSITY_ADMIN scope
    .claim("client_id", oauthClientId)       // OAuth client_credentials (224 Univer)
    .build();
```

### Token Revocation (mavjud)

```java
@PostMapping("/logout")
public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
    String token = authHeader.substring(7);
    Jwt jwt = jwtDecoder.decode(token);
    long ttlSeconds = jwt.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond();
    if (ttlSeconds > 0) {
        blacklistService.revoke(token, Duration.ofSeconds(ttlSeconds));
    }
    return ResponseEntity.noContent().build();
}
```

### JWT Modernization (kelajakdagi sprint)

- `jti` claim — Redis blacklist key 36-char UUID (token o'rniga)
- `kid` header — multi-key parallel rotation
- Refresh token rotation — eski refresh blacklist + yangi pair

## 3. Token Validation

```java
http
    .oauth2ResourceServer(oauth -> oauth
        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
    )
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
        .requestMatchers("/api/v1/external/**").hasAuthority("api.external")
        .anyRequest().authenticated()
    )
    .csrf(AbstractHttpConfigurer::disable)
    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
```

## 4. `@PreAuthorize` — Method Security

```java
// Basic
@PreAuthorize("hasAuthority('students.create')")
public StudentDto create(...) { ... }

// OR
@PreAuthorize("hasAuthority('students.edit') or hasAuthority('admin.full')")
public void update(...) { ... }

// Custom SpEL — university scope (UNIVERSITY_ADMIN)
@PreAuthorize("hasAuthority('students.edit') and @studentSecurity.canEdit(#id, authentication)")
public void update(@PathVariable UUID id, ...) { ... }

@Component("studentSecurity")
@RequiredArgsConstructor
public class StudentSecurity {
    private final StudentRepository repo;

    public boolean canEdit(UUID studentId, Authentication auth) {
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("admin.full")))
            return true;  // SUPER_ADMIN bypass
        UUID userUniversityId = ((CustomPrincipal) auth.getPrincipal()).universityId();
        return repo.findUniversityIdByStudentId(studentId)
            .map(uniId -> uniId.equals(userUniversityId))
            .orElse(false);
    }
}
```

## 5. RBAC Permission Format

```
{resource}.{action}
```

**Resources** (markaziy DB'da):
- `students`, `users`, `roles`, `permissions`, `reports`, `settings`
- `faculty`, `university_building`

**Resources** (Univer'da, bizda yo'q — REST proxy yoki aggregated read-only):
- `curriculum`, `grades`, `attendance`, `course`, `schedule`, `exam`, `enrollment`, `contract`, `employment`, `department`

**Actions:** `view`, `create`, `edit`, `delete`, `export`, `import`

**Misollar:** `students.view`, `students.create`, `admin.full` (SUPER_ADMIN bypass).

**Cache:** Redis 1h TTL, key `user:permissions:<user-id>`, evict role/permission o'zgartirilganda.

## 6. PII Logging Forbidden

```java
// ✗ HAR DOIM TAQIQ
log.info("Login: username={}, password={}", username, password);
log.info("PINFL: {}", student.getPinfl());

// ✓ Masked
log.info("Login attempt: username={}", username);  // password yo'q
log.info("PINFL: {}****", pinfl.substring(0, 8));
```

**Class-level documentation:**
```java
/**
 * Loggable: id, facultyId, status
 * NOT loggable: pinfl, firstName, lastName, email, phone, address
 */
@Entity
public class Student { ... }
```

## 7. Rate Limiting — Per-Role + Per-Client

```yaml
hemis:
  rate-limit:
    per-role:
      VIEWER: 60                # req/min
      UNIVERSITY_ADMIN: 300
      MINISTRY_ADMIN: 600
      SUPER_ADMIN: 1000
    per-client:
      UNIVER_CLIENT: 600        # 224 Univer Yii2 OAuth client_credentials
    per-ip-anonymous: 100
    burst-multiplier: 2
```

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

## 8. CORS — Strict Origins

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

> **Diqqat:** `setAllowCredentials(true)` + `setAllowedOriginPatterns("*")` = security hole.

## 9. SQL Injection Prevention

```java
// ✗ MUTLAQO TAQIQ
String sql = "SELECT * FROM students WHERE name = '" + name + "'";

// ✓ JPQL parametr
@Query("SELECT s FROM Student s WHERE s.name = :name")
List<Student> findByName(@Param("name") String name);

// ✓ JdbcTemplate parametr
jdbcTemplate.query(
    "SELECT * FROM students WHERE name = ?",
    new Object[]{name},
    rowMapper
);

// ✓ Specification
public static Specification<Student> nameContains(String name) {
    return (root, query, cb) -> cb.like(root.get("name"), "%" + name + "%");
}
```

## 10. XSS — Output Encoding

```java
// API JSON qaytaradi → Jackson auto-escape ✓
public record StudentDto(String firstName, ...) {}

// HTML email template — manual escape
String safe = StringEscapeUtils.escapeHtml4(userInput);
```

---

## Audit Log

> AuditLog ALOHIDA `hemis_audit` DB'da (ADR-0003 Audit DB Isolation).

```java
@Entity
@Table(name = "activity_log")  // hemis_audit DB
public class AuditLog extends ImmutableEntity {
    @Column(nullable = false)
    private UUID userId;
    @Column(nullable = false, length = 50)
    private String action;
    @Column(nullable = false, length = 100)
    private String entityType;
    @Column
    private UUID entityId;
    @Column(columnDefinition = "JSONB")
    private String oldValue;
    @Column(columnDefinition = "JSONB")
    private String newValue;
    @Column
    private String ipAddress;
    @Column
    private String userAgent;
}
```

---

## OWASP Top 10:2025 Checklist

- [ ] A01 Broken Access Control — `@PreAuthorize` har endpoint
- [ ] A02 Cryptographic Failures — BCrypt-12, TLS 1.3
- [ ] A03 Injection — JPQL/JdbcTemplate parametrized
- [ ] A04 Insecure Design — RBAC + audit log
- [ ] A05 Security Misconfig — actuator IP whitelist
- [ ] A06 Vulnerable Components — Dependabot
- [ ] A07 Auth Failures — rate limit + token revocation
- [ ] A08 Data Integrity — `@Version` optimistic lock
- [ ] A09 Logging Failures — PII mask
- [ ] A10 SSRF — URL whitelist davlat sistemalari uchun

---

## See also

- `security/CLAUDE.md` — qisqa qoidalar
- ADR-0003 (Audit DB), ADR-0005 (OAuth client_credentials)
- `.claude/agents/security-auditor.md` — automated OWASP audit
