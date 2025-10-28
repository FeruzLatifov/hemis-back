package uz.hemis.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate Configuration
 *
 * <p>Used for calling external APIs (government services, etc.)</p>
 *
 * @since 1.0.0
 */
@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate bean for external API calls
     *
     * @return RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
