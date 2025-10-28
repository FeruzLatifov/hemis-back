package uz.hemis.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration for OAuth2 Token Storage
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Configure RedisTemplate for token storage</li>
 *   <li>OLD-HEMIS compatibility: Same Redis structure</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Configuration
public class RedisConfig {

    /**
     * RedisTemplate bean configuration
     *
     * <p><strong>Serializers:</strong></p>
     * <ul>
     *   <li>Key: StringRedisSerializer (access_token:uuid)</li>
     *   <li>Value: GenericJackson2JsonRedisSerializer (JSON)</li>
     *   <li>Hash Key: StringRedisSerializer (username, roles, etc.)</li>
     *   <li>Hash Value: GenericJackson2JsonRedisSerializer (values)</li>
     * </ul>
     *
     * @param connectionFactory Redis connection factory (auto-configured)
     * @return configured RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key serializer (String)
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Hash serializers
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
