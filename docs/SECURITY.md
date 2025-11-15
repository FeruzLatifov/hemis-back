# 🔐 HEMIS Security Best Practices

> **Version:** 1.0.0  
> **Last Updated:** 2025-11-15  
> **Stack:** Spring Boot 3.5.7 + Spring Security 6.2 + JWT

---

## 📋 Table of Contents

1. [Authentication & Authorization](#authentication--authorization)
2. [JWT Configuration](#jwt-configuration)
3. [Token Storage](#token-storage)
4. [CORS & CSRF](#cors--csrf)
5. [Role-Based Access Control](#role-based-access-control)
6. [Monitoring & Observability](#monitoring--observability)
7. [Security Checklist](#security-checklist)

---

## 🔑 Authentication & Authorization

### Current Implementation

**Algorithm:** HS256 (HMAC-SHA256)  
**Token Type:** Bearer JWT  
**Storage:** Redis + localStorage (frontend)

```yaml
hemis:
  security:
    jwt:
      secret: ${JWT_SECRET}  # Base64-encoded, min 256 bits
      expiration: 43200      # 12 hours (access token)
      refresh-expiration: 604800  # 7 days (refresh token)
      issuer: hemis-backend
```

### ⚠️ Security Considerations

| Issue | Risk | Status |
|-------|------|--------|
| **HS256 (Symmetric)** | Secret key shared between services | ⚠️ Current |
| **localStorage** | Vulnerable to XSS attacks | ⚠️ Current |
| **No HTTPOnly cookies** | Token accessible via JavaScript | ⚠️ Current |

---

## 🎯 JWT Configuration

### Current Setup (Production)

```properties
# Current (HS256 - Symmetric)
JWT_SECRET=base64-encoded-secret-key-min-256-bits
JWT_EXPIRATION=43200        # 12 hours
JWT_REFRESH_EXPIRATION=604800  # 7 days
```

### 🚀 Recommended: RS256 Migration

**Why RS256?**
- ✅ Asymmetric encryption (public/private key pair)
- ✅ Private key NEVER leaves auth server
- ✅ Public key can be shared safely for token validation
- ✅ Better for microservices architecture

**Migration Path:**

```yaml
# Step 1: Generate RSA key pair
openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -outform PEM -pubout -out public.pem

# Step 2: Update application.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: https://auth.hemis.uz/.well-known/jwks.json
          # OR
          issuer-uri: https://auth.hemis.uz
```

**Alternative: EdDSA (Modern, Faster)**
```properties
# Edwards-curve Digital Signature Algorithm
# Better performance, smaller keys, same security level
JWT_ALGORITHM=EdDSA
```

---

## 🍪 Token Storage

### ❌ Current (Vulnerable)

```javascript
// Frontend - VULNERABLE TO XSS
localStorage.setItem('access_token', data.access_token);
localStorage.setItem('refresh_token', data.refresh_token);
```

**Risk:** XSS attack → Token stolen → Account compromised

### ✅ Recommended: HTTPOnly Cookies

**Backend Changes:**

```java
@PostMapping("/login")
public ResponseEntity<Void> login(@RequestBody LoginRequest request, HttpServletResponse response) {
    // Authenticate user
    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);
    
    // Set HTTPOnly cookies
    ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
        .httpOnly(true)           // ❌ JavaScript cannot access
        .secure(true)             // ✅ HTTPS only
        .sameSite("Strict")       // ✅ CSRF protection
        .path("/")
        .maxAge(43200)            // 12 hours
        .build();
        
    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .path("/api/v1/web/auth/refresh")  // Restricted path
        .maxAge(604800)           // 7 days
        .build();
        
    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    
    return ResponseEntity.ok().build();
}
```

**Frontend Changes:**

```javascript
// No need to store tokens manually!
// Cookies are automatically sent with requests

// Login
const response = await fetch('/api/v1/web/auth/login', {
    method: 'POST',
    credentials: 'include',  // ← CRITICAL: Send cookies
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
});

// API calls
const data = await fetch('/api/v1/web/students', {
    credentials: 'include'  // ← CRITICAL: Send cookies
});
```

**Security Benefits:**

| Feature | Protection |
|---------|------------|
| `httpOnly: true` | ❌ JavaScript cannot read token |
| `secure: true` | ✅ HTTPS only transmission |
| `sameSite: "Strict"` | ✅ CSRF attack protection |
| `path: "/"` | ✅ Scoped to specific endpoints |

---

## 🌐 CORS & CSRF

### CORS Configuration (Current)

```yaml
hemis:
  cors:
    allowed-origins:
      - http://localhost:3000  # Development
      - https://hemis.uz       # Production
    allowed-methods:
      - GET
      - POST
      - PUT
      - PATCH
      - OPTIONS
    allow-credentials: true    # Required for HTTPOnly cookies
```

### CSRF Protection

**Current:** Disabled (REST API, stateless)

```java
http.csrf(AbstractHttpConfigurer::disable)
```

**With HTTPOnly Cookies:** Enable CSRF for state-changing requests

```java
http.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
)
```

---

## 👥 Role-Based Access Control

### Role Hierarchy

```
SUPER_ADMIN (SYSTEM)
├── Full system access
├── Ministry-level operations
└── Cannot be deleted

MINISTRY_ADMIN (SYSTEM)
├── Ministry operations
├── Report generation
└── User management

UNIVERSITY_ADMIN (UNIVERSITY)
├── University-scoped
├── Student/staff management
└── Per-university configuration

VIEWER (SYSTEM)
├── Read-only access
└── No write operations

REPORT_VIEWER (CUSTOM)
├── Report generation
└── Data export
```

### RoleCode Enum (NEW)

```java
// Type-safe role codes (no magic strings)
import uz.hemis.common.enums.RoleCode;

// Usage
if (user.hasRole(RoleCode.SUPER_ADMIN)) {
    // Grant access
}

Role role = roleRepository.findByCode(RoleCode.MINISTRY_ADMIN.getCode());
```

### Method-Level Security

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(UUID userId) { }

@PreAuthorize("hasAnyRole('ADMIN', 'MINISTRY_ADMIN')")
public List<Report> generateReport() { }

@PreAuthorize("hasAuthority('students:write')")
public void updateStudent(UUID id) { }
```

---

## 📊 Monitoring & Observability

### Actuator Endpoints (Security)

**Current Configuration:**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,mappings
```

**Security Rules:**

| Endpoint | Access | Reason |
|----------|--------|--------|
| `/actuator/health` | ✅ Public | Health checks (load balancers) |
| `/actuator/info` | ✅ Public | Version info (safe) |
| `/actuator/metrics` | 🔒 Admin only | Performance data (sensitive) |
| `/actuator/mappings` | 🔒 Admin only | Endpoint discovery (sensitive) |
| `/actuator/env` | ❌ DISABLED | Environment variables (SECRETS!) |

**Implementation:**

```java
// SecurityConfig.java
.authorizeHttpRequests(authz -> authz
    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
    .requestMatchers("/actuator/**").hasRole("ADMIN")  // ← NEW
    // ...
)
```

### Sensitive Data Masking

```yaml
# Mask sensitive values in /actuator/env
management:
  endpoint:
    env:
      show-values: when-authorized  # Only for admin
      keys-to-sanitize:
        - password
        - secret
        - token
        - key
        - jwt
```

---

## ✅ Security Checklist

### Production Deployment

- [ ] **JWT Algorithm**
  - [ ] Migrate from HS256 to RS256 or EdDSA
  - [ ] Store private key in secure vault (HashiCorp Vault, AWS Secrets Manager)
  - [ ] Rotate keys periodically (90 days)

- [ ] **Token Storage**
  - [ ] Replace localStorage with HTTPOnly cookies
  - [ ] Set `sameSite: "Strict"`
  - [ ] Enable `secure: true` (HTTPS only)

- [ ] **CORS**
  - [ ] Whitelist specific origins (no `*`)
  - [ ] Enable `allow-credentials: true` for cookies
  - [ ] Restrict to HTTPS origins only

- [ ] **Actuator**
  - [ ] Restrict `/actuator/**` to admin role
  - [ ] Disable `/actuator/env` endpoint
  - [ ] Enable HTTPS for actuator endpoints

- [ ] **Password Security**
  - [ ] Enforce strong passwords (min 12 chars, complexity)
  - [ ] Enable password expiry (90 days)
  - [ ] Implement account lockout (5 failed attempts)

- [ ] **Monitoring**
  - [ ] Enable Sentry error tracking
  - [ ] Log authentication failures
  - [ ] Monitor suspicious activity (rate limiting)

---

## 🔗 References

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [HttpOnly Cookie Security](https://owasp.org/www-community/HttpOnly)

---

## 📝 Changelog

**v1.0.0 (2025-11-15)**
- Initial security documentation
- RoleCode enum implementation
- Actuator endpoint protection
- HTTPOnly cookie recommendations
