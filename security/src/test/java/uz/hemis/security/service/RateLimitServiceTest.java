package uz.hemis.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link RateLimitService} unit testlar — sliding window counter + fail-closed.
 *
 * <p>Critical security infrastructure — Redis-based rate limit for login,
 * password reset, OAuth endpoints. Fail-closed (Redis xato bo'lsa REJECT).</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitService — sliding window counter")
class RateLimitServiceTest {

    @SuppressWarnings("rawtypes")
    @Mock private RedisTemplate redisTemplate;

    @SuppressWarnings("rawtypes")
    @Mock private ValueOperations valueOps;

    @InjectMocks
    private RateLimitService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // lenient — null/empty identifier testlar opsForValue chaqirmaydi (fail closed early).
        org.mockito.Mockito.lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Nested
    @DisplayName("isAllowed(identifier) — default login window")
    class DefaultIsAllowed {

        @Test
        @DisplayName("1-attempt — allowed, TTL belgilanadi")
        void firstAttempt_allowedAndTtlSet() {
            when(valueOps.increment("ratelimit:login:1.2.3.4")).thenReturn(1L);

            assertThat(service.isAllowed("1.2.3.4")).isTrue();

            verify(redisTemplate).expire("ratelimit:login:1.2.3.4", 15, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("under-limit (5-attempt) — allowed, TTL set only once")
        void underLimit_allowed() {
            when(valueOps.increment("ratelimit:login:user1")).thenReturn(5L);

            assertThat(service.isAllowed("user1")).isTrue();

            // 5 != 1, so no TTL re-set
            verify(redisTemplate, never()).expire(eq("ratelimit:login:user1"), anyLong(), eq(TimeUnit.MINUTES));
        }

        @Test
        @DisplayName("over-limit (6th attempt) — rejected")
        void overLimit_rejected() {
            when(valueOps.increment("ratelimit:login:bad-ip")).thenReturn(6L);

            assertThat(service.isAllowed("bad-ip")).isFalse();
        }

        @Test
        @DisplayName("null identifier → fail closed (reject)")
        void nullIdentifier_failClosed() {
            assertThat(service.isAllowed(null)).isFalse();
            verify(valueOps, never()).increment(anyLong());
        }

        @Test
        @DisplayName("empty identifier → fail closed")
        void emptyIdentifier_failClosed() {
            assertThat(service.isAllowed("")).isFalse();
        }

        @Test
        @DisplayName("Redis increment returns null → fail closed")
        void redisNull_failClosed() {
            when(valueOps.increment("ratelimit:login:x")).thenReturn(null);

            assertThat(service.isAllowed("x")).isFalse();
        }

        @Test
        @DisplayName("Redis throws → fail closed (not propagated)")
        void redisException_failClosed() {
            when(valueOps.increment("ratelimit:login:x"))
                    .thenThrow(new RuntimeException("Redis down"));

            assertThat(service.isAllowed("x")).isFalse();
        }
    }

    @Nested
    @DisplayName("isAllowed(prefix, identifier, max, window) — generic overload")
    class GenericIsAllowed {

        @Test
        @DisplayName("custom prefix + window — TTL belgilanadi")
        void customPrefix_firstAttempt() {
            when(valueOps.increment("ratelimit:forgot-password:ip:1.2.3.4")).thenReturn(1L);

            boolean allowed = service.isAllowed("ratelimit:forgot-password:ip:", "1.2.3.4", 5, 15);

            assertThat(allowed).isTrue();
            verify(redisTemplate).expire("ratelimit:forgot-password:ip:1.2.3.4", 15, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("over custom limit → rejected")
        void overCustomLimit_rejected() {
            when(valueOps.increment("ratelimit:reset:x")).thenReturn(11L);

            assertThat(service.isAllowed("ratelimit:reset:", "x", 10, 60)).isFalse();
        }

        @Test
        @DisplayName("null identifier → fail closed")
        void genericNull_failClosed() {
            assertThat(service.isAllowed("p:", null, 5, 15)).isFalse();
        }

        @Test
        @DisplayName("empty prefix → fail closed (defensive)")
        void emptyPrefix_failClosed() {
            assertThat(service.isAllowed("", "x", 5, 15)).isFalse();
        }
    }

    @Nested
    @DisplayName("getRemainingAttempts()")
    class GetRemainingAttempts {

        @Test
        @DisplayName("no attempts yet — returns MAX (5)")
        void noAttempts_returnsMax() {
            when(valueOps.get("ratelimit:login:x")).thenReturn(null);

            assertThat(service.getRemainingAttempts("x")).isEqualTo(5);
        }

        @Test
        @DisplayName("3 attempts → 2 remaining")
        void someAttempts_remaining() {
            when(valueOps.get("ratelimit:login:x")).thenReturn("3");

            assertThat(service.getRemainingAttempts("x")).isEqualTo(2);
        }

        @Test
        @DisplayName("limit exceeded → 0 remaining (not negative)")
        void limitExceeded_zero() {
            when(valueOps.get("ratelimit:login:x")).thenReturn("10");

            assertThat(service.getRemainingAttempts("x")).isEqualTo(0);
        }

        @Test
        @DisplayName("null identifier → returns MAX")
        void nullIdentifier_returnsMax() {
            assertThat(service.getRemainingAttempts(null)).isEqualTo(5);
        }

        @Test
        @DisplayName("Redis exception → fail-open (return MAX)")
        void redisException_failOpen() {
            when(valueOps.get("ratelimit:login:x"))
                    .thenThrow(new RuntimeException("Redis down"));

            // Read-only get — fail open OK (display purpose)
            assertThat(service.getRemainingAttempts("x")).isEqualTo(5);
        }
    }
}
