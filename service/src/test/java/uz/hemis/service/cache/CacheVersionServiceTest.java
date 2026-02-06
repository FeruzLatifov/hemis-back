package uz.hemis.service.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for versioned cache system.
 *
 * Tests:
 * - Version management (get, increment)
 * - Versioned key generation
 * - Distributed lock acquisition
 * - Pub/Sub invalidation
 * - Concurrent version increments
 * - Multiple namespace independence
 *
 * @author Senior Architect
 * @since 2025-11-13
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Versioned Cache System Tests")
class CacheVersionServiceTest {

    @Mock
    private RedisTemplate<String, String> redisMessageTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private CacheVersionService cacheVersionService;

    private static final String TEST_NAMESPACE = "test-cache";

    @BeforeEach
    void setUp() {
        lenient().when(redisMessageTemplate.opsForValue()).thenReturn(valueOperations);
        cacheVersionService = new CacheVersionService(redisMessageTemplate);
    }

    @Test
    @DisplayName("Should initialize version to 1 when not exists")
    void testGetCurrentVersion_InitializesToOne() {
        // Given - no version stored in Redis
        when(valueOperations.get("cache:version:" + TEST_NAMESPACE)).thenReturn(null);
        when(valueOperations.setIfAbsent("cache:version:" + TEST_NAMESPACE, "1")).thenReturn(true);

        // When
        long version = cacheVersionService.getCurrentVersion(TEST_NAMESPACE);

        // Then
        assertThat(version).isEqualTo(1L);
        verify(valueOperations).setIfAbsent("cache:version:" + TEST_NAMESPACE, "1");
    }

    @Test
    @DisplayName("Should increment version atomically")
    void testIncrementVersion() {
        // Given - getCurrentVersion returns 1 initially
        when(valueOperations.get("cache:version:" + TEST_NAMESPACE)).thenReturn(null, "2", "3");
        when(valueOperations.setIfAbsent("cache:version:" + TEST_NAMESPACE, "1")).thenReturn(true);
        when(valueOperations.increment("cache:version:" + TEST_NAMESPACE)).thenReturn(2L, 3L);

        // When
        long v1 = cacheVersionService.getCurrentVersion(TEST_NAMESPACE);
        long v2 = cacheVersionService.incrementVersion(TEST_NAMESPACE);
        long v3 = cacheVersionService.incrementVersion(TEST_NAMESPACE);

        // Then
        assertThat(v1).isEqualTo(1L);
        assertThat(v2).isEqualTo(2L);
        assertThat(v3).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should build versioned keys correctly")
    void testBuildVersionedKey() {
        // Given - sequential get() calls:
        // 1st: null (getCurrentVersion init call)
        // 2nd: "1" (buildVersionedKey -> getCurrentVersion)
        // 3rd: "2" (buildVersionedKey after increment -> getCurrentVersion)
        when(valueOperations.get("cache:version:" + TEST_NAMESPACE)).thenReturn(null, "1", "2");
        when(valueOperations.setIfAbsent("cache:version:" + TEST_NAMESPACE, "1")).thenReturn(true);
        when(valueOperations.increment("cache:version:" + TEST_NAMESPACE)).thenReturn(2L);

        // Initialize to v1
        cacheVersionService.getCurrentVersion(TEST_NAMESPACE);

        // When
        String key1 = cacheVersionService.buildVersionedKey(TEST_NAMESPACE, "messages:uz-UZ");

        // Then
        assertThat(key1).isEqualTo(TEST_NAMESPACE + ":v1:messages:uz-UZ");

        // When version increments
        cacheVersionService.incrementVersion(TEST_NAMESPACE);
        String key2 = cacheVersionService.buildVersionedKey(TEST_NAMESPACE, "messages:uz-UZ");

        // Then - key2 should use version 2
        assertThat(key2).isEqualTo(TEST_NAMESPACE + ":v2:messages:uz-UZ");
        assertThat(key2).isNotEqualTo(key1);
    }

    @Test
    @DisplayName("Should increment version and publish invalidation")
    void testIncrementVersionAndPublish() {
        // Given
        when(valueOperations.get("cache:version:" + TEST_NAMESPACE)).thenReturn(null);
        when(valueOperations.setIfAbsent("cache:version:" + TEST_NAMESPACE, "1")).thenReturn(true);
        when(valueOperations.increment("cache:version:" + TEST_NAMESPACE)).thenReturn(2L);

        long versionBefore = cacheVersionService.getCurrentVersion(TEST_NAMESPACE);

        // When
        long newVersion = cacheVersionService.incrementVersionAndPublish(TEST_NAMESPACE);

        // Then
        assertThat(newVersion).isGreaterThan(versionBefore);
        assertThat(newVersion).isEqualTo(versionBefore + 1);
        verify(redisMessageTemplate).convertAndSend(eq("cache:invalidate:" + TEST_NAMESPACE), anyString());
    }

    @Test
    @DisplayName("Should acquire and release distributed lock")
    void testDistributedLock() {
        // Given
        String lockKey = "test-operation";
        String fullLockKey = "cache:lock:" + lockKey;

        // When - First lock acquisition (succeeds)
        when(valueOperations.setIfAbsent(eq(fullLockKey), anyString(), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        boolean acquired1 = cacheVersionService.acquireLock(lockKey);

        // Then
        assertThat(acquired1).isTrue();

        // When - Try to acquire same lock (should fail)
        when(valueOperations.setIfAbsent(eq(fullLockKey), anyString(), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(false);
        boolean acquired2 = cacheVersionService.acquireLock(lockKey);

        // Then
        assertThat(acquired2).isFalse();

        // When - Release lock
        when(redisMessageTemplate.delete(fullLockKey)).thenReturn(true);
        cacheVersionService.releaseLock(lockKey);
        verify(redisMessageTemplate).delete(fullLockKey);

        // When - Acquire lock again (should succeed now)
        when(valueOperations.setIfAbsent(eq(fullLockKey), anyString(), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        boolean acquired3 = cacheVersionService.acquireLock(lockKey);

        // Then
        assertThat(acquired3).isTrue();
    }

    @Test
    @DisplayName("Should handle concurrent version increments correctly")
    void testConcurrentVersionIncrements() throws InterruptedException {
        // Given - simulate atomic Redis INCR returning sequential values
        int numberOfThreads = 10;

        // Each thread calls incrementVersion which calls INCR
        // Redis INCR is atomic, so we simulate sequential returns
        when(valueOperations.increment("cache:version:" + TEST_NAMESPACE))
                .thenReturn(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);

        Thread[] threads = new Thread[numberOfThreads];

        // When - Multiple threads increment version concurrently
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new Thread(() -> {
                cacheVersionService.incrementVersion(TEST_NAMESPACE);
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - increment was called exactly numberOfThreads times
        verify(valueOperations, times(numberOfThreads)).increment("cache:version:" + TEST_NAMESPACE);

        // After all increments, getCurrentVersion should return the final value
        when(valueOperations.get("cache:version:" + TEST_NAMESPACE)).thenReturn(String.valueOf(numberOfThreads + 1));
        long finalVersion = cacheVersionService.getCurrentVersion(TEST_NAMESPACE);
        assertThat(finalVersion).isEqualTo(numberOfThreads + 1);
    }

    @Test
    @DisplayName("Should handle multiple namespaces independently")
    void testMultipleNamespaces() {
        // Given
        String namespace1 = "cache-ns1";
        String namespace2 = "cache-ns2";

        // Sequential get() calls for ns1: null (init), "3" (after 2 increments)
        // Sequential get() calls for ns2: null (init), "1" (still 1)
        when(valueOperations.get("cache:version:" + namespace1)).thenReturn(null, "3");
        when(valueOperations.get("cache:version:" + namespace2)).thenReturn(null, "1");
        when(valueOperations.setIfAbsent("cache:version:" + namespace1, "1")).thenReturn(true);
        when(valueOperations.setIfAbsent("cache:version:" + namespace2, "1")).thenReturn(true);
        when(valueOperations.increment("cache:version:" + namespace1)).thenReturn(2L, 3L);

        // When
        long v1_ns1 = cacheVersionService.getCurrentVersion(namespace1);  // null -> init to 1
        long v1_ns2 = cacheVersionService.getCurrentVersion(namespace2);  // null -> init to 1

        cacheVersionService.incrementVersion(namespace1);  // returns 2
        cacheVersionService.incrementVersion(namespace1);  // returns 3

        long v2_ns1 = cacheVersionService.getCurrentVersion(namespace1);  // returns "3" -> 3
        long v2_ns2 = cacheVersionService.getCurrentVersion(namespace2);  // returns "1" -> 1

        // Then
        assertThat(v1_ns1).isEqualTo(1L);
        assertThat(v1_ns2).isEqualTo(1L);
        assertThat(v2_ns1).isEqualTo(3L); // Incremented twice
        assertThat(v2_ns2).isEqualTo(1L); // Not incremented
    }
}
