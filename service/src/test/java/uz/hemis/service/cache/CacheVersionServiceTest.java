package uz.hemis.service.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for versioned cache system.
 *
 * Tests:
 * - Version management (get, increment)
 * - Versioned key generation
 * - Distributed lock acquisition
 * - Pub/Sub invalidation
 *
 * @author Senior Architect
 * @since 2025-11-13
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Versioned Cache System Tests")
class CacheVersionServiceTest {

    @Autowired
    private CacheVersionService cacheVersionService;

    @Autowired
    private StringRedisTemplate redisMessageTemplate;

    private static final String TEST_NAMESPACE = "test-cache";

    @BeforeEach
    void setUp() {
        // Clean up test namespace before each test
        String versionKey = "cache:version:" + TEST_NAMESPACE;
        redisMessageTemplate.delete(versionKey);
    }

    @Test
    @DisplayName("Should initialize version to 1 when not exists")
    void testGetCurrentVersion_InitializesToOne() {
        // When
        long version = cacheVersionService.getCurrentVersion(TEST_NAMESPACE);

        // Then
        assertThat(version).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should increment version atomically")
    void testIncrementVersion() {
        // Given
        long v1 = cacheVersionService.getCurrentVersion(TEST_NAMESPACE);

        // When
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
        // Given
        cacheVersionService.getCurrentVersion(TEST_NAMESPACE); // Initialize to v1

        // When
        String key1 = cacheVersionService.buildVersionedKey(TEST_NAMESPACE, "messages:uz-UZ");

        // Then
        assertThat(key1).isEqualTo("test-cache:v1:messages:uz-UZ");

        // When version increments
        cacheVersionService.incrementVersion(TEST_NAMESPACE);
        String key2 = cacheVersionService.buildVersionedKey(TEST_NAMESPACE, "messages:uz-UZ");

        // Then
        assertThat(key2).isEqualTo("test-cache:v2:messages:uz-UZ");
    }

    @Test
    @DisplayName("Should increment version and publish invalidation")
    void testIncrementVersionAndPublish() {
        // Given
        long versionBefore = cacheVersionService.getCurrentVersion(TEST_NAMESPACE);

        // When
        long newVersion = cacheVersionService.incrementVersionAndPublish(TEST_NAMESPACE);

        // Then
        assertThat(newVersion).isGreaterThan(versionBefore);
        assertThat(newVersion).isEqualTo(versionBefore + 1);
    }

    @Test
    @DisplayName("Should acquire and release distributed lock")
    void testDistributedLock() {
        // Given
        String lockKey = "test-operation";

        // When - First lock acquisition
        boolean acquired1 = cacheVersionService.acquireLock(lockKey);

        // Then
        assertThat(acquired1).isTrue();

        // When - Try to acquire same lock (should fail)
        boolean acquired2 = cacheVersionService.acquireLock(lockKey);

        // Then
        assertThat(acquired2).isFalse();

        // When - Release lock
        cacheVersionService.releaseLock(lockKey);

        // When - Acquire lock again (should succeed now)
        boolean acquired3 = cacheVersionService.acquireLock(lockKey);

        // Then
        assertThat(acquired3).isTrue();

        // Cleanup
        cacheVersionService.releaseLock(lockKey);
    }

    @Test
    @DisplayName("Should handle concurrent version increments correctly")
    void testConcurrentVersionIncrements() throws InterruptedException {
        // Given
        int numberOfThreads = 10;
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

        // Then - Version should be exactly numberOfThreads + 1 (initial version is 1)
        long finalVersion = cacheVersionService.getCurrentVersion(TEST_NAMESPACE);
        assertThat(finalVersion).isEqualTo(numberOfThreads + 1);
    }

    @Test
    @DisplayName("Should handle multiple namespaces independently")
    void testMultipleNamespaces() {
        // Given
        String namespace1 = "cache-ns1";
        String namespace2 = "cache-ns2";

        // When
        long v1_ns1 = cacheVersionService.getCurrentVersion(namespace1);
        long v1_ns2 = cacheVersionService.getCurrentVersion(namespace2);

        cacheVersionService.incrementVersion(namespace1);
        cacheVersionService.incrementVersion(namespace1);

        long v2_ns1 = cacheVersionService.getCurrentVersion(namespace1);
        long v2_ns2 = cacheVersionService.getCurrentVersion(namespace2);

        // Then
        assertThat(v1_ns1).isEqualTo(1L);
        assertThat(v1_ns2).isEqualTo(1L);
        assertThat(v2_ns1).isEqualTo(3L); // Incremented twice
        assertThat(v2_ns2).isEqualTo(1L); // Not incremented

        // Cleanup
        redisMessageTemplate.delete("cache:version:" + namespace1);
        redisMessageTemplate.delete("cache:version:" + namespace2);
    }
}
