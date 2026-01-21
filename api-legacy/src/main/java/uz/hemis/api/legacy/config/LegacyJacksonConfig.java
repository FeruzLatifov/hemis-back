package uz.hemis.api.legacy.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Legacy API Jackson Configuration
 *
 * Configures Jackson ObjectMapper to include null values in JSON responses
 * for backward compatibility with OLD-HEMIS API.
 *
 * IMPORTANT: This overrides the global Jackson configuration (non_null)
 * to ensure legacy API responses include null fields.
 *
 * @since 2.0.0
 */
@Configuration
public class LegacyJacksonConfig {

    /**
     * Primary ObjectMapper that includes null values.
     * This is necessary for OLD-HEMIS compatibility where null fields
     * must be present in the response.
     */
    @Bean
    @Primary
    public ObjectMapper legacyObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Include null values in JSON output (OLD-HEMIS compatibility)
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);

        // Date/Time configuration
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Pretty print for debugging
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        return mapper;
    }
}
