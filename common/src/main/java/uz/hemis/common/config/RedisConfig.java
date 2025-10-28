package uz.hemis.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis Cache Configuration for HEMIS
 *
 * Purpose:
 * - Cache frequently accessed data (students, courses, schedules)
 * - Reduce database load from 200+ universities
 * - Improve read performance
 *
 * Cache Strategy:
 * - Read operations: Cache with TTL
 * - Create/Update/Delete: Cache eviction
 * - Replica DB for reads (cache miss)
 * - Master DB for writes
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = false)
@Slf4j
public class RedisConfig {

    /**
     * Redis Cache Manager with custom TTL per cache
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("Configuring Redis Cache Manager for HEMIS");

        // Object Mapper for JSON serialization with type info
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Default cache configuration (30 minutes TTL)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        // Custom cache configurations per entity
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                // Student cache - 15 min (frequently updated)
                .withCacheConfiguration("students", defaultConfig.entryTtl(Duration.ofMinutes(15)))
                // Teacher cache - 30 min
                .withCacheConfiguration("teachers", defaultConfig.entryTtl(Duration.ofMinutes(30)))
                // Course cache - 60 min (rarely changes)
                .withCacheConfiguration("courses", defaultConfig.entryTtl(Duration.ofHours(1)))
                // Schedule cache - 30 min
                .withCacheConfiguration("schedules", defaultConfig.entryTtl(Duration.ofMinutes(30)))
                // Exam cache - 60 min
                .withCacheConfiguration("exams", defaultConfig.entryTtl(Duration.ofHours(1)))
                // Attendance cache - 10 min (frequently updated)
                .withCacheConfiguration("attendances", defaultConfig.entryTtl(Duration.ofMinutes(10)))
                // Grade cache - 15 min (frequently updated)
                .withCacheConfiguration("grades", defaultConfig.entryTtl(Duration.ofMinutes(15)))
                // Group cache - 60 min
                .withCacheConfiguration("groups", defaultConfig.entryTtl(Duration.ofHours(1)))
                // University cache - 24 hours (rarely changes)
                .withCacheConfiguration("universities", defaultConfig.entryTtl(Duration.ofHours(24)))
                // Faculty cache - 12 hours
                .withCacheConfiguration("faculties", defaultConfig.entryTtl(Duration.ofHours(12)))
                // Specialty cache - 12 hours
                .withCacheConfiguration("specialties", defaultConfig.entryTtl(Duration.ofHours(12)))
                // Curriculum cache - 24 hours
                .withCacheConfiguration("curriculums", defaultConfig.entryTtl(Duration.ofHours(24)))
                // Enrollment cache - 30 min
                .withCacheConfiguration("enrollments", defaultConfig.entryTtl(Duration.ofMinutes(30)))
                .build();
    }

    /**
     * Redis Template for custom operations
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // JSON serializer for values
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
