package uz.hemis.service.integration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.hemis.common.port.cache.DistributedCachePort;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * API-MSPD Gateway Token Service
 * <p>
 * 172.18.9.171 (api_mspd) dan JWT token olish va Redis da cache qilish.
 * Bu gateway barcha tashqi integratsiyalarni (GUVD, BIMM, DTM, va boshqalar)
 * markazlashgan holda boshqaradi.
 * </p>
 *
 * <p>Token flow:</p>
 * <ol>
 *   <li>POST /auth/token (form-urlencoded) → JWT access_token + refresh_token</li>
 *   <li>api_mspd tokenni o'z Redis da saqlaydi, har safar o'shani qaytaradi</li>
 *   <li>access_token_expires = tugashgacha QOLGAN sekund (total emas!)</li>
 *   <li>hemis-back Redis TTL = aynan shu qolgan muddat (uzilish bo'lmasligi uchun)</li>
 *   <li>Token tugashi bilan refresh_token orqali yangilanadi</li>
 * </ol>
 *
 * @since 2.0.0
 */
@Service
@Slf4j
public class ApiMspdTokenService {

    private final DistributedCachePort cachePort;

    private static final String ACCESS_TOKEN_KEY = "api-mspd:access_token";
    private static final String REFRESH_TOKEN_KEY = "api-mspd:refresh_token";
    private static final String FAILURE_CACHE_KEY = "api-mspd:token:failure";
    private static final Duration FAILURE_TTL = Duration.ofMinutes(2);
    private static final long MIN_TTL_SECONDS = 60;
    // Token tugashidan 90 sekund oldin proaktiv yangilash
    private static final long REFRESH_BEFORE_EXPIRY_SECONDS = 90;

    private final ReentrantLock tokenLock = new ReentrantLock();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "api-mspd-token-refresh");
        t.setDaemon(true);
        return t;
    });
    private volatile ScheduledFuture<?> scheduledRefresh;

    public ApiMspdTokenService(DistributedCachePort cachePort) {
        this.cachePort = cachePort;
    }

    @Value("${hemis.integration.api-mspd.base-url:http://172.18.9.171}")
    private String baseUrl;

    @Value("${hemis.integration.api-mspd.username:}")
    private String username;

    @Value("${hemis.integration.api-mspd.password:}")
    private String password;

    @PostConstruct
    void validateConfig() {
        if (username == null || username.isBlank()) {
            log.warn("API-MSPD username not configured (hemis.integration.api-mspd.username)");
        }
        if (password == null || password.isBlank()) {
            log.warn("API-MSPD password not configured (hemis.integration.api-mspd.password)");
        }
        if (baseUrl != null && !baseUrl.isBlank()) {
            log.info("API-MSPD gateway configured: {}", baseUrl);
        }
    }

    @PreDestroy
    void shutdown() {
        scheduler.shutdownNow();
    }

    /**
     * Get API-MSPD JWT access token.
     * Token is cached in Redis. If expired, fetches new one or uses refresh token.
     *
     * @return JWT access token, or null if unavailable
     */
    public String getAccessToken() {
        // 1. Try cache (lock kerak emas — Redis atomik)
        String cached = cachePort.<String>retrieve(ACCESS_TOKEN_KEY).orElse(null);
        if (cached != null && !cached.isEmpty()) {
            log.debug("Using cached API-MSPD token");
            return cached;
        }

        // 2. Lock — faqat 1 thread token oladi, qolganlari kutadi
        tokenLock.lock();
        try {
            // Double-check: boshqa thread allaqachon olgan bo'lishi mumkin
            cached = cachePort.<String>retrieve(ACCESS_TOKEN_KEY).orElse(null);
            if (cached != null && !cached.isEmpty()) {
                log.debug("Using cached API-MSPD token (after lock)");
                return cached;
            }

            // 3. Check negative cache
            String failureMarker = cachePort.<String>retrieve(FAILURE_CACHE_KEY).orElse(null);
            if (failureMarker != null) {
                log.debug("API-MSPD token fetch skipped — recent failure ({})", failureMarker);
                return null;
            }

            // 4. Try refresh token
            String refreshToken = cachePort.<String>retrieve(REFRESH_TOKEN_KEY).orElse(null);
            if (refreshToken != null && !refreshToken.isEmpty()) {
                String refreshed = refreshAccessToken(refreshToken);
                if (refreshed != null) {
                    return refreshed;
                }
            }

            // 5. Fetch new token
            return fetchNewToken();
        } finally {
            tokenLock.unlock();
        }
    }

    /**
     * Base URL of the API-MSPD gateway.
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Invalidate all cached tokens (force re-login).
     */
    public void invalidateTokens() {
        cachePort.delete(ACCESS_TOKEN_KEY);
        cachePort.delete(REFRESH_TOKEN_KEY);
        cachePort.delete(FAILURE_CACHE_KEY);
        log.info("API-MSPD token cache invalidated");
    }

    /**
     * Fetch new token pair from POST /auth/token
     */
    @SuppressWarnings("unchecked")
    private String fetchNewToken() {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            log.debug("API-MSPD credentials not configured, skipping");
            cachePort.store(FAILURE_CACHE_KEY, "not_configured", FAILURE_TTL);
            return null;
        }

        String tokenUrl = baseUrl + "/auth/token";
        log.info("Fetching new API-MSPD token from: {}", tokenUrl);

        try {
            // api_mspd uses OAuth2PasswordRequestForm (form-urlencoded)
            String body = "grant_type=password&username=" +
                    URLEncoder.encode(username, StandardCharsets.UTF_8) +
                    "&password=" +
                    URLEncoder.encode(password, StandardCharsets.UTF_8);

            HttpURLConnection conn = (HttpURLConnection) URI.create(tokenUrl).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int statusCode = conn.getResponseCode();
            if (statusCode == 200) {
                String responseStr = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                conn.disconnect();

                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> resp = mapper.readValue(responseStr, Map.class);

                String accessToken = resp.get("access_token") != null ? resp.get("access_token").toString() : null;
                String refToken = resp.get("refresh_token") != null ? resp.get("refresh_token").toString() : null;

                if (accessToken == null) {
                    log.error("API-MSPD response missing access_token");
                    cachePort.store(FAILURE_CACHE_KEY, "no_access_token", FAILURE_TTL);
                    return null;
                }

                // TTL = api_mspd qaytargan qolgan muddat (expires = remaining seconds)
                // api_mspd tokenni o'z Redis da saqlaydi, har safar o'sha tokenni qaytaradi
                // shuning uchun hemis-back TTL = aynan shu qolgan muddat
                Duration ttl = extractTtl(resp.get("access_token_expires"));
                cachePort.store(ACCESS_TOKEN_KEY, accessToken, ttl);
                log.info("API-MSPD access_token cached (TTL: {} soat, {} daqiqa)", ttl.toHours(), ttl.toMinutes() % 60);

                // refresh_token — qolgan muddatga cache
                if (refToken != null) {
                    Duration refreshTtl = extractTtl(resp.get("refresh_token_expires"));
                    cachePort.store(REFRESH_TOKEN_KEY, refToken, refreshTtl);
                    log.info("API-MSPD refresh_token cached (TTL: {} kun, {} soat)", refreshTtl.toDays(), refreshTtl.toHours() % 24);
                }

                // Tugashidan oldin avtomatik yangilashni rejalashtirish
                scheduleProactiveRefresh(ttl);

                return accessToken;
            } else {
                String errorBody = "";
                try {
                    errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                } catch (Exception ignored) {}
                conn.disconnect();
                log.error("API-MSPD token request failed: status={}, body={}", statusCode, errorBody);
                cachePort.store(FAILURE_CACHE_KEY, "http_" + statusCode, FAILURE_TTL);
                return null;
            }
        } catch (Exception e) {
            log.error("API-MSPD token fetch error: {}", e.getMessage());
            cachePort.store(FAILURE_CACHE_KEY, "error_" + e.getClass().getSimpleName(), FAILURE_TTL);
            return null;
        }
    }

    /**
     * Token tugashidan 90 sekund oldin background da yangilash rejalashtirish.
     * Bu so'rov threadlari hech qachon token olishni kutmasligini ta'minlaydi.
     */
    private void scheduleProactiveRefresh(Duration ttl) {
        // Oldingi schedule ni bekor qilish
        if (scheduledRefresh != null) {
            scheduledRefresh.cancel(false);
        }

        long refreshAfter = ttl.toSeconds() - REFRESH_BEFORE_EXPIRY_SECONDS;
        if (refreshAfter < MIN_TTL_SECONDS) {
            // TTL juda kichik — proaktiv refresh imkoni yo'q
            return;
        }

        scheduledRefresh = scheduler.schedule(() -> {
            log.info("Proactive token refresh triggered ({}s before expiry)", REFRESH_BEFORE_EXPIRY_SECONDS);
            tokenLock.lock();
            try {
                // FAQAT refresh-token orqali — /auth/refresh-token YANGI token yaratadi
                // /auth/token eski tokenni qaytaradi, shuning uchun fetchNewToken() CHAQIRILMAYDI
                String refreshToken = cachePort.<String>retrieve(REFRESH_TOKEN_KEY).orElse(null);
                if (refreshToken != null && !refreshToken.isEmpty()) {
                    String refreshed = refreshAccessToken(refreshToken);
                    if (refreshed != null) {
                        return;
                    }
                }
                // Refresh token yo'q yoki ishlamadi — token tugaganda getAccessToken() da fetchNewToken() ishlaydi
                log.warn("Proactive refresh failed — no refresh token available");
            } finally {
                tokenLock.unlock();
            }
        }, refreshAfter, TimeUnit.SECONDS);

        log.info("Proactive refresh scheduled in {} soat {} daqiqa",
                Duration.ofSeconds(refreshAfter).toHours(),
                Duration.ofSeconds(refreshAfter).toMinutes() % 60);
    }

    /**
     * api_mspd javobidan TTL ajratish.
     * expires = qolgan sekund (tugashgacha). Aynan shu qiymatni Redis TTL qilib qo'yamiz.
     */
    private Duration extractTtl(Object expiresObj) {
        if (expiresObj instanceof Number) {
            long seconds = ((Number) expiresObj).longValue();
            if (seconds > MIN_TTL_SECONDS) {
                return Duration.ofSeconds(seconds);
            }
        }
        // Default: response da expires yo'q yoki juda kichik — 1 soat
        return Duration.ofHours(1);
    }

    /**
     * Refresh access token using refresh_token.
     * POST /auth/refresh-token?refresh_token=XXX
     */
    @SuppressWarnings("unchecked")
    private String refreshAccessToken(String refreshToken) {
        String refreshUrl = baseUrl + "/auth/refresh-token?refresh_token=" +
                URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);
        log.info("Refreshing API-MSPD token via refresh_token");

        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(refreshUrl).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int statusCode = conn.getResponseCode();
            if (statusCode == 200) {
                String responseStr = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                conn.disconnect();

                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> resp = mapper.readValue(responseStr, Map.class);

                String newAccessToken = resp.get("access_token") != null ? resp.get("access_token").toString() : null;
                if (newAccessToken != null) {
                    Duration ttl = extractTtl(resp.get("access_token_expires"));
                    cachePort.store(ACCESS_TOKEN_KEY, newAccessToken, ttl);
                    log.info("API-MSPD token refreshed (TTL: {} soat, {} daqiqa)", ttl.toHours(), ttl.toMinutes() % 60);

                    // Yangi refresh_token ham saqlash (token rotation)
                    String newRefToken = resp.get("refresh_token") != null ? resp.get("refresh_token").toString() : null;
                    if (newRefToken != null) {
                        Duration refreshTtl = extractTtl(resp.get("refresh_token_expires"));
                        cachePort.store(REFRESH_TOKEN_KEY, newRefToken, refreshTtl);
                        log.info("API-MSPD refresh_token rotated (TTL: {} kun, {} soat)", refreshTtl.toDays(), refreshTtl.toHours() % 24);
                    }

                    // Keyingi yangilashni rejalashtirish
                    scheduleProactiveRefresh(ttl);

                    return newAccessToken;
                }
            }
            conn.disconnect();
            log.warn("API-MSPD refresh failed: status={}", statusCode);
        } catch (Exception e) {
            log.warn("API-MSPD refresh error: {}", e.getMessage());
        }
        return null;
    }
}
