package uz.hemis.app.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * RestTemplate configuration with connection pooling and timeouts.
 *
 * <p>Uses Apache HttpClient 5 for proper connection management.
 * Prevents resource exhaustion from unclosed connections and
 * hanging requests without timeouts.</p>
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // Connection pool configuration
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);          // Max total connections
        connectionManager.setDefaultMaxPerRoute(20); // Max connections per host

        // Request timeout configuration
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(10, TimeUnit.SECONDS)  // Time to get connection from pool
                .setResponseTimeout(30, TimeUnit.SECONDS)           // Time to wait for response
                .build();

        // Build HTTP client with pooling and timeouts
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(factory);
    }
}
