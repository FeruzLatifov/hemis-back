# api-external module — Server-to-Server Integration

> **Markaziy HEMIS-back** ↔ davlat tashkilotlari S2S integratsiya qatlami. **Outbound** (HEMIS → tashqi) va **inbound** (tashqi → HEMIS) ikkalasi ham.
>
> **Tashqi davlat sistemalari (outbound):** MyGov (yagona kirish), MSPD (sotsiologik), BIMM (sertifikatlar), Tax/Soliq (sub'ekt tekshirish), GUVD (passport ma'lumoti), OneID (auth federation).
>
> **URL:** `/api/v1/external/*`
> **Security:** API Key + IP Whitelist (JWT EMAS — machine-to-machine)
>
> **Eslatma:** "Ministry HEMIS" eski terminologiyasi ishlatilmaydi — **biz o'zimiz vazirlik markaziy server**.

---

## Critical Rules

### 1. Authentication — API Key + IP Whitelist

```java
// API Key header'da
X-API-Key: <secret>

// IP whitelist application.yml
hemis:
  external:
    allowed-ips:
      - 10.0.0.0/8        # internal network
      - 172.18.0.0/16     # MSPD private
      - <ministry-ip>/32
```

JWT EMAS — external system'lar JWT bilan ishlamaydi. Filter chain:
1. `IpWhitelistFilter` — IP allow listdan tekshiradi
2. `ApiKeyAuthenticationFilter` — header'dan API Key, DB'da hash check
3. `RateLimitFilter` — per-API-key (har integration alohida limit)

```java
@RestController
@RequestMapping("/api/v1/external")
public class ExternalController {

    @PostMapping("/student-data")
    @PreAuthorize("hasAuthority('api.external.student-data')")
    public ResponseEntity<...> receiveStudentData(@Valid @RequestBody ...) { ... }
}
```

### 2. Outbound Call Timeout — bugun aniq belgilanishi

`AbstractGovernmentApiService` RestTemplate ishlatadi, global timeout aniq belgilanmagan. Yangi client'da timeout sozlash:

```java
// Misol: MyGov yagona kirish federation client
@Service
@RequiredArgsConstructor
@Slf4j
public class MyGovClient {

    private final RestClient myGovClient;  // configured with timeouts

    public MyGovTokenResponse exchangeAuthCode(String authCode) {
        try {
            return myGovClient.post()
                .uri("/oauth/token")
                .header("Authorization", "Bearer " + tokenService.get())
                .body(Map.of("code", authCode))
                .retrieve()
                .body(MyGovTokenResponse.class);
        } catch (RestClientException ex) {
            log.error("MyGov auth failed: reason={}", ex.getMessage());
            retryQueue.enqueue(authCode);
            return MyGovTokenResponse.queued();
        }
    }
}

// Bean config — har davlat sistemasi (MyGov, MSPD, BIMM, Tax, GUVD) uchun alohida
@Bean
public RestClient myGovClient(@Value("${hemis.external.mygov.base-url}") String baseUrl) {
    SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
    f.setConnectTimeout(10_000);
    f.setReadTimeout(30_000);
    return RestClient.builder().baseUrl(baseUrl).requestFactory(f).build();
}
```

External integration kelajakda Resilience4j kabi kutubxona bilan kuchaytirish mumkin (circuit breaker, retry, fallback). Hozir manual try/catch + retry queue.

### 3. Idempotency — outbound POST/PUT MAJBURIY

External system retry → duplikat ish bajarmaslik. Idempotency-Key header:

```java
@PostMapping("/student-data")
public ResponseEntity<...> receiveStudentData(
    @RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,
    @Valid @RequestBody StudentDataDto dto
) {
    // Check Redis cache — already processed?
    Optional<ProcessResult> existing = idempotencyService.get(idempotencyKey);
    if (existing.isPresent()) {
        return ResponseEntity.ok(existing.get().response());
    }

    // Process + cache result
    ProcessResult result = service.process(dto);
    idempotencyService.put(idempotencyKey, result, Duration.ofDays(7));
    return ResponseEntity.ok(result.response());
}
```

### 4. Webhook Pattern — Async + Persistent Queue

External system bizdan webhook chaqiradi (PayMe payment confirmation, MSPD response):

```java
@PostMapping("/webhook/payment-confirmation")
public ResponseEntity<Map<String, Object>> paymentConfirmation(
    @RequestHeader("X-Signature") String signature,
    @RequestBody Map<String, Object> payload
) {
    // 1. Verify signature (HMAC-SHA256)
    if (!signatureService.verify(payload, signature)) {
        log.warn("Invalid webhook signature from IP={}", request.getRemoteAddr());
        return ResponseEntity.status(401).build();
    }

    // 2. Persist to queue (idempotent)
    webhookQueue.enqueue(WebhookEvent.fromPayload(payload));

    // 3. Return immediately (5xx = external retries)
    return ResponseEntity.ok(Map.of("status", "queued"));
}

// Async processor
@EventListener(WebhookEvent.class)
@Async("webhookExecutor")
public void processWebhook(WebhookEvent event) { ... }
```

**Sabab:** Webhook 200 qaytarmasa, external system retry qiladi (5+ marta). Tez 200 qaytarib, async ishlash.

### 5. SSL/TLS — Self-Signed Cert per-Connection

MSPD, MyGov, BIMM ba'zilari self-signed certificate ishlatadi. JVM-wide trust store o'zgartirmaslik:

```java
// ✓ TO'G'RI — per-connection trust manager (har davlat sistemasi uchun alohida)
@Bean("mspdRestClient")
public RestClient mspdRestClient(@Value("${hemis.external.mspd.cert-path}") String certPath) throws Exception {
    SSLContext sslContext = SSLContextBuilder.create()
        .loadTrustMaterial(new File(certPath), null, (chain, authType) -> true)
        .build();

    return RestClient.builder()
        .requestFactory(new HttpComponentsClientHttpRequestFactory(
            HttpClients.custom().setSSLContext(sslContext).build()))
        .build();
}

// ✗ TAQIQ — JVM-wide
System.setProperty("javax.net.ssl.trustStore", ...);  // JVM standart property (Jakarta'ga tegishli emas), butun JVM ta'siriga uchraydi
```

### 6. Token Refresh — Proactive

External system token (MSPD, OneID) — expiration'dan oldin yangilash:

```java
@Service
public class ApiMspdTokenService {

    private static final long REFRESH_BEFORE_EXPIRY_SECONDS = 90;
    private final ScheduledExecutorService scheduler;

    public String getToken() {
        // Cache hit
        Token cached = cachePort.get(TOKEN_CACHE_KEY);
        if (cached != null && cached.expiresIn() > REFRESH_BEFORE_EXPIRY_SECONDS) {
            return cached.value();
        }
        // Refresh + reschedule
        return refreshAndCache();
    }

    private String refreshAndCache() {
        // Distributed lock — har instance bir vaqtda refresh qilmasligi uchun
        if (!cachePort.acquireLock(TOKEN_LOCK_KEY, Duration.ofSeconds(10))) {
            return cachePort.get(TOKEN_CACHE_KEY).value();  // Boshqa instance refresh qilmoqda
        }
        try {
            Token fresh = fetchFreshToken();
            cachePort.put(TOKEN_CACHE_KEY, fresh, fresh.ttl());
            scheduler.schedule(this::refreshAndCache,
                fresh.ttl().toSeconds() - REFRESH_BEFORE_EXPIRY_SECONDS,
                TimeUnit.SECONDS);
            return fresh.value();
        } finally {
            cachePort.releaseLock(TOKEN_LOCK_KEY);
        }
    }
}
```

### 7. Outbound Request Logging — Audit + No PII

```java
// ✓ TO'G'RI
log.info("Outbound: target=ministry, action=sendReport, studentId={}, attempt={}",
         studentId, attempt);

// ✗ TAQIQ — full payload (PII risk)
log.info("Sending to ministry: {}", payload);
```

Audit log'da: target system, action, IDs (ma'lumot emas), result (success/fail/retry).

---

## External System Catalog

> **Eslatma:** "HEMIS Ministry" eski entry olib tashlandi — **biz o'zimiz vazirlik markaziy server**. Tashqi davlat sistemalari quyida (S2S):

| System | URL | Purpose | Auth |
|--------|-----|---------|------|
| OneID SSO | `https://sso.egov.uz` | Yagona davlat kirish | OAuth2 + signed JWT |
| MyGov Portal | `https://my.gov.uz` | Fuqaro verifikatsiyasi | API Key |
| MSPD | `http://172.18.9.171` | Sotsiologik tekshiruv | Username/Password (`ApiMspdTokenService`) |
| BIMM | — | Sertifikatlar | API Key + IP whitelist |
| Tax/Soliq | — | Sub'ekt PINFL check | API Key |
| GUVD | — | Passport ma'lumoti | API Key + IP whitelist |
| PayMe | — | To'lov | API Key + HMAC |
| Click | — | To'lov | API Key + HMAC |

---

## PR Checklist

- [ ] API Key + IP Whitelist filter chain configured
- [ ] Outbound RestClient/RestTemplate connect+read timeout aniq belgilangan
- [ ] Failure case: log + retry queue (persistent) yoki fallback
- [ ] Idempotency-Key header support inbound POST/PUT
- [ ] Webhook signature verification (HMAC-SHA256)
- [ ] Webhook async processing (queue + handler)
- [ ] Token refresh proactive (distributed lock)
- [ ] SSL: per-connection trust manager (JVM-wide o'zgartirmaslik)
- [ ] Logging: target/action/IDs only, NO payload PII
- [ ] Test: unit (Mockito + WireMock) + integration
- [ ] Fallback method real degradation strategy (queue, default value)

---

## See Also
- `../service/CLAUDE.md` — Service patterns
- `../.claude/rules.md` — Reliability section (timeout config)
