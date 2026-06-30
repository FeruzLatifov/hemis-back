package uz.hemis.service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * ENTERPRISE 2-Level Cache Configuration - PRODUCTION READY
 *
 * <p><strong>Architecture:</strong></p>
 * <ul>
 *   <li>L1: Caffeine (JVM memory, per-pod, ~1ms)</li>
 *   <li>L2: Redis (shared, distributed, ~50ms)</li>
 *   <li>L3: PostgreSQL (source of truth, ~1000ms)</li>
 * </ul>
 *
 * <p><strong>Cache Configuration (Unified 30 min TTL):</strong></p>
 * <ul>
 *   <li>menu: L1=1000 entries/30min, L2=30min</li>
 *   <li>i18n: L1=5000 entries/30min, L2=30min</li>
 *   <li>userPermissions: L1=1000 entries/30min, L2=30min</li>
 *   <li>stats: L1=100 entries/30min, L2=30min</li>
 * </ul>
 *
 * <p><strong>Performance Benefits:</strong></p>
 * <ul>
 *   <li>Menu API: 1300ms (DB) → 1ms (L1) = 1300x faster ⚡</li>
 *   <li>Cross-pod: 1300ms (DB) → 50ms (L2) = 26x faster</li>
 *   <li>Zero database load for cached requests</li>
 *   <li>Horizontal scaling ready (10+ pods)</li>
 * </ul>
 *
 * <p><strong>10 Pods Scenario:</strong></p>
 * <pre>
 * Request 1 (POD-1): 1000ms (DB) → Populate L1 + L2
 * Request 2 (POD-1): 1ms (L1 Caffeine) ✅
 * Request 3 (POD-2): 50ms (L2 Redis) → Populate L1
 * Request 4 (POD-2): 1ms (L1 Caffeine) ✅
 * </pre>
 *
 * @since 2.0.0
 */
@Configuration
@EnableCaching
@Slf4j
public class DashboardCacheConfig implements CachingConfigurer {

    private static final String DASHBOARD_CACHE_NAME = "hemis:dashboard:stats";
    private static final Duration DASHBOARD_TTL = Duration.ofMinutes(30);
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);  // Unified 30 min for menu, i18n, permissions

    /**
     * Cache key prefix version — deploy-time format migration.
     *
     * <p><strong>Qachon bumplash kerak:</strong></p>
     * <ul>
     *   <li>Jackson ObjectMapper konfiguratsiyasi o'zgarganda
     *       (masalan {@code activateDefaultTyping}, mixin, ignored fields)</li>
     *   <li>Cache'da saqlanayotgan DTO/record shape o'zgarganda
     *       (yangi maydon emas — type/structure o'zgarishi)</li>
     *   <li>Redis serializer o'zgarganda (Jackson → Kryo, va h.k.)</li>
     * </ul>
     *
     * <p><strong>Qachon bumplash KERAK EMAS:</strong></p>
     * <ul>
     *   <li>Oddiy controller/service/SQL fix</li>
     *   <li>Yangi cache nomi qo'shish</li>
     *   <li>TTL o'zgarishi</li>
     *   <li>Yangi DTO maydoni (Jackson backward-compatible)</li>
     * </ul>
     *
     * <p>Bumplaganingizdan keyin: yangi pod yangi prefix bilan ishlaydi
     * ({@code cache:v3:*}), eski {@code cache:v2:*} qiymatlari TTL bilan
     * tabiiy o'chadi (max 24 soat). Manual {@code redis-cli FLUSHDB} kerak emas.</p>
     *
     * <p><strong>Tarix:</strong></p>
     * <ul>
     *   <li>v1 — initial (Jackson default typing yo'q edi)</li>
     *   <li>v2 — {@code activateDefaultTyping(NON_FINAL, AS.PROPERTY)} qo'shildi
     *           ({@code @class} property majburiy)</li>
     * </ul>
     */
    private static final String CACHE_VERSION = "v2";
    private static final String CACHE_PREFIX = "cache:" + CACHE_VERSION + ":";

    /**
     * ENTERPRISE 2-Level Cache Manager
     *
     * <p><strong>L1 + L2 Configuration:</strong></p>
     * <ul>
     *   <li>L1 (Caffeine): 1000 entries, 30 minutes TTL, per-pod</li>
     *   <li>L2 (Redis): Per-cache TTL (menu=30min, stats=30min), distributed</li>
     * </ul>
     *
     * <p><strong>Read Flow:</strong></p>
     * <pre>
     * 1. Check L1 Caffeine → HIT: return (1ms) ✅
     * 2. Check L2 Redis → HIT: populate L1, return (50ms)
     * 3. Call DB → Populate L1 + L2, return (1000ms)
     * </pre>
     *
     * <p><strong>Write Flow:</strong></p>
     * <pre>
     * 1. Write to L1 Caffeine (immediate)
     * 2. Write to L2 Redis (sync for consistency)
     * </pre>
     *
     * Note: GenericJackson2JsonRedisSerializer is deprecated in Spring Data Redis 3.x
     * but still works. Will be replaced in future versions.
     */
    @Bean
    @Primary
    @SuppressWarnings("removal")
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("🚀 Initializing ENTERPRISE 2-Level Cache Manager (Caffeine + Redis)");

        // JSON serialization with JavaTimeModule support
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.registerModule(new PageJacksonModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
            objectMapper.getPolymorphicTypeValidator(),
            com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL,
            com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Default Redis L2 configuration (30 minutes)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)                                            // 30 min TTL
                .prefixCacheNameWith(CACHE_PREFIX)                                // Versioned prefix (cache:v2:)
                .serializeKeysWith(RedisSerializationContext.SerializationPair    // String keys
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair  // JSON values
                        .fromSerializer(jsonSerializer))
                .disableCachingNullValues();                                      // Don't cache null

        // Per-cache L2 (Redis) TTL configuration
        java.util.Map<String, RedisCacheConfiguration> redisCacheConfigurations = new java.util.HashMap<>();

        // Dashboard cache: 30 minutes
        redisCacheConfigurations.put("stats", defaultConfig.entryTtl(DASHBOARD_TTL));

        // Menu cache: 30 minutes
        redisCacheConfigurations.put("menu", defaultConfig.entryTtl(DEFAULT_TTL));

        // I18n cache: 30 minutes
        redisCacheConfigurations.put("i18n", defaultConfig.entryTtl(DEFAULT_TTL));

        // User permissions cache: 30 minutes
        redisCacheConfigurations.put("userPermissions", defaultConfig.entryTtl(DEFAULT_TTL));

        // University search cache (paged + filters): 30 minutes
        redisCacheConfigurations.put("universitiesSearch", defaultConfig.entryTtl(DASHBOARD_TTL));

        // University dictionaries (static): 6 hours
        redisCacheConfigurations.put("universityDictionaries", defaultConfig.entryTtl(Duration.ofHours(6)));

        // University domain caches — 230 OTM, rarely change (24 hour TTL)
        // findAllList (230 row list, dropdown'larda), findActive (dashboard widget)
        // NB: PK lookup ({@code findByCode}) cache QILMAYDI — Redis L2 ~50ms,
        //     PostgreSQL primary index ~1ms. Cache DB'dan sekinroq, anti-pattern.
        redisCacheConfigurations.put("universityList", defaultConfig.entryTtl(Duration.ofHours(24)));
        redisCacheConfigurations.put("universityActive", defaultConfig.entryTtl(Duration.ofHours(6)));
        redisCacheConfigurations.put("universityChildren", defaultConfig.entryTtl(Duration.ofHours(24)));

        // University detail caches — 1 hour (profile manual-edited)
        // universityDashboard — 4 LEFT JOIN aggregate, tab switching paytida foyda
        // NB: universityFounders alohida cache QILMAYDI — getUniversityDashboard ichidan
        //     this.getFounders(...) self-invocation chaqiriladi (AOP bypass), demak
        //     duplicate cache. universityDashboard nested natijasi yetarli.
        redisCacheConfigurations.put("universityDashboard", defaultConfig.entryTtl(Duration.ofHours(1)));
        redisCacheConfigurations.put("universityProfile", defaultConfig.entryTtl(Duration.ofHours(1)));
        redisCacheConfigurations.put("universityRector", defaultConfig.entryTtl(Duration.ofHours(1)));
        redisCacheConfigurations.put("universityLifecycle", defaultConfig.entryTtl(Duration.ofHours(1)));
        redisCacheConfigurations.put("universityCadastreList", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Student list search cache (lightweight DTO, paged + filters): 30 minutes
        redisCacheConfigurations.put("studentsListSearch", defaultConfig.entryTtl(DASHBOARD_TTL));

        // Student list COUNT cache (shared across pages, same filter = same count): 30 minutes
        redisCacheConfigurations.put("studentsListCount", defaultConfig.entryTtl(DASHBOARD_TTL));

        // Student stats cache: 30 minutes
        redisCacheConfigurations.put("studentStats", defaultConfig.entryTtl(DASHBOARD_TTL));

        // Student duplicate stats cache: 1 hour (heavy query, rarely changes)
        redisCacheConfigurations.put("studentDuplicateStats", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Student duplicate list cache (paged): 30 minutes
        redisCacheConfigurations.put("studentDuplicates", defaultConfig.entryTtl(DASHBOARD_TTL));

        // Student dictionaries (static): 6 hours
        redisCacheConfigurations.put("studentDictionaries", defaultConfig.entryTtl(Duration.ofHours(6)));

        // Hokimiyat classifier endpoints — 20 classifiers × 9 JDBC each (~180 queries/req).
        // Univer-side polls these on schedule; data changes only via admin classifier edits.
        redisCacheConfigurations.put("hokimiyatClassifiers", defaultConfig.entryTtl(Duration.ofHours(24)));
        redisCacheConfigurations.put("hokimiyatClassifiersInfo", defaultConfig.entryTtl(Duration.ofHours(24)));

        // Reference classifier findAll() caches — small static lookup tables.
        // Names match the @Cacheable values used in ClassifierLegacyService.
        redisCacheConfigurations.put("classifierEducationType", defaultConfig.entryTtl(Duration.ofHours(24)));
        redisCacheConfigurations.put("classifierEducationForm", defaultConfig.entryTtl(Duration.ofHours(24)));
        redisCacheConfigurations.put("classifierCourse", defaultConfig.entryTtl(Duration.ofHours(24)));
        redisCacheConfigurations.put("classifierEducationYear", defaultConfig.entryTtl(Duration.ofHours(24)));
        redisCacheConfigurations.put("classifierTransferType", defaultConfig.entryTtl(Duration.ofHours(24)));
        redisCacheConfigurations.put("classifierAdmissionType", defaultConfig.entryTtl(Duration.ofHours(24)));
        redisCacheConfigurations.put("classifierDepartmentType", defaultConfig.entryTtl(Duration.ofHours(24)));

        // Student GPA cache — heavy aggregation, refreshes after exam grade changes.
        redisCacheConfigurations.put("studentGpa", defaultConfig.entryTtl(Duration.ofHours(24)));

        // Classifier reference loader — per (table, entity, code) lookup cache used by
        // StudentLegacyMapper to avoid N+1 JDBC reads while mapping student → CUBA DTO.
        redisCacheConfigurations.put("classifierReference", defaultConfig.entryTtl(Duration.ofHours(24)));

        // Legacy classifier nested-map loader — used by ContractStatisticsService and other
        // OLD-HEMIS mappers that need full classifier object inside response. 24h TTL —
        // reference data (education_type, education_year, course, semester, etc.) rarely changes.
        redisCacheConfigurations.put("legacyClassifierMaps", defaultConfig.entryTtl(Duration.ofHours(24)));

        // Citizenship classifier active-flag check — used by StudentEnrollmentService
        // for every student-id generation. ~250 countries, closed list, 24h TTL.
        redisCacheConfigurations.put("citizenshipActive", defaultConfig.entryTtl(Duration.ofHours(24)));

        // Student total-count estimate — pg_class.reltuples (planner statistic, autovacuum-updated).
        // 1.15M qator full COUNT(*) ~5s; estimate ~1ms. 1 daqiqa TTL — health-check only.
        redisCacheConfigurations.put("studentCountEstimate", defaultConfig.entryTtl(Duration.ofMinutes(1)));

        // =====================================================
        // Domain caches — avval missing TTL, default 30min fallback ishlatilardi.
        // Endi explicit konfiguratsiya (memory predictability + ops grep'ablilik).
        // =====================================================

        // Students alias-key (pinfl:, id:) — hot lookup, 1 soat.
        redisCacheConfigurations.put("students", defaultConfig.entryTtl(Duration.ofHours(1)));
        redisCacheConfigurations.put("studentMetas", defaultConfig.entryTtl(Duration.ofHours(1)));
        redisCacheConfigurations.put("doctoralStudents", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Finance/Document (mutable, admin'lar edit qiladi) — 30 daqiqa.
        redisCacheConfigurations.put("contracts", defaultConfig.entryTtl(DASHBOARD_TTL));
        redisCacheConfigurations.put("employments", defaultConfig.entryTtl(DASHBOARD_TTL));
        redisCacheConfigurations.put("diplomas", defaultConfig.entryTtl(DASHBOARD_TTL));
        redisCacheConfigurations.put("diplomaBlanks", defaultConfig.entryTtl(DASHBOARD_TTL));

        // Departments + Faculty dictionaries — 6 soat (registry, statik).
        redisCacheConfigurations.put("departments", defaultConfig.entryTtl(Duration.ofHours(6)));
        redisCacheConfigurations.put("facultyDictionaries", defaultConfig.entryTtl(Duration.ofHours(6)));

        // Speciality stats — heavy aggregation, 1 soat.
        redisCacheConfigurations.put("specialityStats", defaultConfig.entryTtl(Duration.ofHours(1)));
        redisCacheConfigurations.put("specialitySummary", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Translations — admin tomondan tahrirlanmagunicha katta TTL.
        // Eski nom translations-category — camelCase translationsCategory.
        redisCacheConfigurations.put("translations", defaultConfig.entryTtl(Duration.ofHours(1)));
        redisCacheConfigurations.put("translationsCategory", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Create Redis cache manager (L2)
        RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(redisCacheConfigurations)
                .transactionAware()
                .build();

        // Create 2-level cache manager (L1 + L2)
        uz.hemis.service.cache.TwoLevelCacheManager cacheManager =
                new uz.hemis.service.cache.TwoLevelCacheManager(redisCacheManager);

        log.info("✅ ENTERPRISE 2-Level Cache configured:");
        log.info("   L1 (Caffeine): per-cache size, 30 min TTL, per-pod");
        log.info("   L2 (Redis): 30 min TTL (unified), distributed");
        log.info("   Prefix: {} (CACHE_VERSION={})", CACHE_PREFIX, CACHE_VERSION);
        log.info("   Serialization: JSON");

        return cacheManager;
    }

    /**
     * Cache Error Handler - Graceful Degradation
     * 
     * If Redis fails, log error but continue with database query
     * BEST PRACTICE: Never fail requests due to cache issues
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, 
                                           org.springframework.cache.Cache cache, 
                                           Object key) {
                log.warn("⚠️ Redis cache GET failed (key: {}), falling back to database", key, exception);
                // Continue without cache
            }

            @Override
            public void handleCachePutError(RuntimeException exception, 
                                           org.springframework.cache.Cache cache, 
                                           Object key, 
                                           Object value) {
                log.warn("⚠️ Redis cache PUT failed (key: {}), data not cached", key, exception);
                // Continue without caching
            }
        };
    }
}
