package uz.hemis.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis Configuration for Permission Caching
 *
 * <p><strong>Architecture - Best Practice:</strong></p>
 * <ul>
 *   <li>JWT tokens: MINIMAL (only iss, sub, exp)</li>
 *   <li>Permissions: Cached in Redis (NOT in JWT)</li>
 *   <li>Cache strategy: user:{username}:permissions (TTL: 1 hour)</li>
 *   <li>Fallback: If Redis down → Load from DB</li>
 * </ul>
 *
 * <p><strong>Permission Loading Pipeline:</strong></p>
 * <pre>
 * 1. JWT decode → extract username
 * 2. Check Redis: GET user:{username}:permissions
 * 3. If cache miss → Load from DB → Save to Redis
 * 4. Return Set&lt;String&gt; permissions
 * </pre>
 *
 * <p><strong>Performance Benefits:</strong></p>
 * <ul>
 *   <li>Minimal JWT size (no permission bloat)</li>
 *   <li>Zero DB queries for cached users</li>
 *   <li>Horizontal scaling (Redis cluster)</li>
 *   <li>Fast permission updates (cache eviction)</li>
 * </ul>
 *
 * <p><strong>Configuration:</strong></p>
 * <pre>
 * spring.data.redis.host=localhost
 * spring.data.redis.port=6379
 * spring.data.redis.password=
 * spring.data.redis.timeout=2000ms
 * spring.data.redis.jedis.pool.max-active=8
 * spring.data.redis.jedis.pool.max-idle=8
 * spring.data.redis.jedis.pool.min-idle=0
 * </pre>
 *
 * @since 2.0.0
 */
@Configuration
@EnableCaching
@Slf4j
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.timeout:2000ms}")
    private Duration redisTimeout;

    /**
     * Redis Connection Factory
     *
     * <p>Uses Jedis (NOT Lettuce) for better connection pooling</p>
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("Configuring Redis connection - host: {}, port: {}", redisHost, redisPort);

        // Redis standalone configuration
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);

        if (redisPassword != null && !redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }

        // Jedis client configuration
        JedisClientConfiguration.JedisClientConfigurationBuilder jedisBuilder =
                JedisClientConfiguration.builder();

        jedisBuilder.connectTimeout(redisTimeout);
        jedisBuilder.readTimeout(redisTimeout);
        jedisBuilder.usePooling();

        JedisConnectionFactory connectionFactory =
                new JedisConnectionFactory(redisConfig, jedisBuilder.build());

        connectionFactory.afterPropertiesSet();

        log.info("✅ Redis connection factory configured successfully");

        return connectionFactory;
    }

    /**
     * Redis Template for String → Object caching
     *
     * <p>Used for user permissions caching</p>
     *
     * <p><strong>Serialization:</strong></p>
     * <ul>
     *   <li>Key: String (user:admin:permissions)</li>
     *   <li>Value: JSON (Set of permission strings)</li>
     * </ul>
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // JSON serializer for values (permissions are Set<String>)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();

        log.info("✅ RedisTemplate configured with JSON serialization");

        return template;
    }

    /**
     * Redis Template for Pub/Sub Messages
     *
     * <p>Separate template for String → String messages (cache invalidation signals)</p>
     *
     * <p><strong>Use Case:</strong></p>
     * <ul>
     *   <li>Admin triggers cache refresh → Publishes "refresh" signal</li>
     *   <li>All 10 pods subscribe → Receive signal → Clear L1 cache</li>
     *   <li>Channel: cache:invalidate:all</li>
     * </ul>
     */
    @Bean
    public RedisTemplate<String, String> redisMessageTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String serializer for both keys and values
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();

        log.info("✅ RedisMessageTemplate configured for Pub/Sub");

        return template;
    }

    /**
     * Redis Message Listener Container (Pub/Sub)
     *
     * <p><strong>Enterprise Distributed Cache Invalidation</strong></p>
     *
     * <p>10 pods scenario:</p>
     * <pre>
     * POD-1 ─┐
     * POD-2 ─┤
     * POD-3 ─┤
     * POD-4 ─┤                  ┌─────────────┐
     * POD-5 ─┼─ Subscribe to ──→│ Redis       │
     * POD-6 ─┤    Channel        │ Pub/Sub     │
     * POD-7 ─┤                   └─────────────┘
     * POD-8 ─┤                         ↑
     * POD-9 ─┤                         │
     * POD-10─┘                   Admin publishes
     *                            "cache:invalidate:all"
     * </pre>
     *
     * <p><strong>Channels:</strong></p>
     * <ul>
     *   <li>cache:invalidate:all - Barcha cache tozalash</li>
     *   <li>cache:invalidate:i18n - Faqat tarjimalar</li>
     *   <li>cache:invalidate:menu - Faqat menular</li>
     *   <li>cache:invalidate:permissions - Faqat permissionlar</li>
     * </ul>
     *
     * <p><strong>Flow:</strong></p>
     * <pre>
     * 1. Admin POST /api/v1/admin/cache/refresh
     * 2. Backend publishes Redis message: "refresh-{timestamp}"
     * 3. All 10 pods receive message (MessageListener)
     * 4. Each pod clears L1 cache (JVM Caffeine)
     * 5. Leader pod loads from DB → Redis (L2)
     * 6. Other 9 pods load from Redis → L1
     * 7. All pods synchronized ✅
     * </pre>
     *
     * <p><strong>@Primary Annotation:</strong></p>
     * <ul>
     *   <li>Primary bean for distributed cache management</li>
     *   <li>Resolves conflict with legacy translationCacheListenerContainer</li>
     *   <li>Used by CacheInvalidationListener for enterprise cache</li>
     * </ul>
     */
    @Bean
    @Primary
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        log.info("✅ RedisMessageListenerContainer configured for distributed cache invalidation");
        log.info("📡 Waiting for CacheInvalidationListener to register channels...");

        return container;
    }
}
