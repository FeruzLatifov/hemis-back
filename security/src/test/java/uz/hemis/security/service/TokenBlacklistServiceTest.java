package uz.hemis.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TokenBlacklistService}.
 *
 * <p><strong>Scope:</strong> JTI blacklist add/check/remove flows, Redis TTL math
 * (token expiry → TTL in seconds), null/empty JTI guards, already-expired skip,
 * idempotent operations.</p>
 *
 * <p>Pure Mockito with {@code RedisTemplate} and {@code ValueOperations} mocks.
 * No Spring context, no Testcontainers Redis — service logic is pure
 * (no internal scheduling, no event listeners).</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenBlacklistService Tests")
class TokenBlacklistServiceTest {

    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOps;

    private TokenBlacklistService service;

    @BeforeEach
    void setUp() {
        service = new TokenBlacklistService(redisTemplate);
    }

    // =========================================================
    // addToBlacklist — happy path
    // =========================================================

    @Test
    @DisplayName("addToBlacklist with future expiry → Redis SET with correct TTL")
    void addToBlacklist_futureExpiry_setsWithCorrectTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        String jti = "valid-jti-001";
        Instant expiry = Instant.now().plus(Duration.ofMinutes(15));

        service.addToBlacklist(jti, expiry);

        ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);
        verify(valueOps).set(
            eq("token:blacklist:" + jti),
            eq("revoked"),
            ttlCaptor.capture(),
            eq(TimeUnit.SECONDS)
        );
        // ~900s (15 min), allow ±5s for execution drift
        assertThat(ttlCaptor.getValue()).isBetween(895L, 905L);
    }

    @Test
    @DisplayName("addToBlacklist key uses 'token:blacklist:' prefix")
    void addToBlacklist_keyHasCorrectPrefix() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        String jti = "abc-123-def";

        service.addToBlacklist(jti, Instant.now().plus(Duration.ofHours(1)));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOps).set(keyCaptor.capture(), any(), any(Long.class), any(TimeUnit.class));
        assertThat(keyCaptor.getValue()).isEqualTo("token:blacklist:abc-123-def");
    }

    // =========================================================
    // addToBlacklist — guards
    // =========================================================

    @Test
    @DisplayName("addToBlacklist with null JTI → no-op (no Redis call)")
    void addToBlacklist_nullJti_isNoOp() {
        service.addToBlacklist(null, Instant.now().plus(Duration.ofMinutes(15)));

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("addToBlacklist with empty JTI → no-op (no Redis call)")
    void addToBlacklist_emptyJti_isNoOp() {
        service.addToBlacklist("", Instant.now().plus(Duration.ofMinutes(15)));

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("addToBlacklist with already-expired token → skip (TTL would be ≤0)")
    void addToBlacklist_alreadyExpired_skips() {
        Instant pastExpiry = Instant.now().minus(Duration.ofMinutes(5));

        service.addToBlacklist("expired-jti", pastExpiry);

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("addToBlacklist with expiry exactly now → skip (TTL = 0)")
    void addToBlacklist_expiryNow_skips() {
        service.addToBlacklist("now-jti", Instant.now());

        // TTL is computed and may be 0 or -1; either way the value-op path must skip.
        verify(redisTemplate, never()).opsForValue();
    }

    // =========================================================
    // isBlacklisted
    // =========================================================

    @Test
    @DisplayName("isBlacklisted true → returns true with WARN log")
    void isBlacklisted_keyExists_returnsTrue() {
        when(redisTemplate.hasKey("token:blacklist:revoked-jti")).thenReturn(true);

        assertThat(service.isBlacklisted("revoked-jti")).isTrue();
    }

    @Test
    @DisplayName("isBlacklisted false → returns false")
    void isBlacklisted_keyAbsent_returnsFalse() {
        when(redisTemplate.hasKey("token:blacklist:fresh-jti")).thenReturn(false);

        assertThat(service.isBlacklisted("fresh-jti")).isFalse();
    }

    @Test
    @DisplayName("isBlacklisted hasKey returns null (Redis quirk) → returns false (fail-open)")
    void isBlacklisted_hasKeyReturnsNull_returnsFalse() {
        when(redisTemplate.hasKey(anyString())).thenReturn(null);

        // Boolean.TRUE.equals(null) == false → service must not throw NPE
        assertThat(service.isBlacklisted("some-jti")).isFalse();
    }

    @Test
    @DisplayName("isBlacklisted with null JTI → returns false (no Redis call)")
    void isBlacklisted_nullJti_returnsFalseNoRedisCall() {
        assertThat(service.isBlacklisted(null)).isFalse();

        verify(redisTemplate, never()).hasKey(anyString());
    }

    @Test
    @DisplayName("isBlacklisted with empty JTI → returns false (no Redis call)")
    void isBlacklisted_emptyJti_returnsFalseNoRedisCall() {
        assertThat(service.isBlacklisted("")).isFalse();

        verify(redisTemplate, never()).hasKey(anyString());
    }

    // =========================================================
    // removeFromBlacklist
    // =========================================================

    @Test
    @DisplayName("removeFromBlacklist → Redis DELETE with prefixed key")
    void removeFromBlacklist_deletesKey() {
        service.removeFromBlacklist("admin-revert-jti");

        verify(redisTemplate).delete("token:blacklist:admin-revert-jti");
    }

    @Test
    @DisplayName("removeFromBlacklist with null/empty → no-op")
    void removeFromBlacklist_nullOrEmpty_isNoOp() {
        service.removeFromBlacklist(null);
        service.removeFromBlacklist("");

        verify(redisTemplate, never()).delete(anyString());
    }

    // =========================================================
    // clearAllBlacklist — admin operation
    // =========================================================

    @Test
    @DisplayName("clearAllBlacklist scans 'token:blacklist:*' and deletes each match")
    void clearAllBlacklist_deletesAllMatchingKeys() {
        Set<String> matches = Set.of(
            "token:blacklist:jti1",
            "token:blacklist:jti2",
            "token:blacklist:jti3"
        );
        when(redisTemplate.keys("token:blacklist:*")).thenReturn(matches);

        service.clearAllBlacklist();

        // Each key passed to delete (order-independent)
        verify(redisTemplate, times(3)).delete(anyString());
        for (String key : matches) {
            verify(redisTemplate).delete(key);
        }
    }

    @Test
    @DisplayName("clearAllBlacklist with no matching keys → no delete call (forEach over empty set)")
    void clearAllBlacklist_noMatches_noDeletes() {
        when(redisTemplate.keys("token:blacklist:*")).thenReturn(Set.of());

        assertThatCode(() -> service.clearAllBlacklist()).doesNotThrowAnyException();

        verify(redisTemplate, never()).delete(anyString());
    }
}
